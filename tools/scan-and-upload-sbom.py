#!/usr/bin/env python3
"""Scan one published image and upload its CycloneDX BOM, without failing CI."""

import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import time
from urllib.parse import urlsplit
import uuid

TRIVY_IMAGE = "aquasec/trivy:0.76.1"


class IntegrationError(Exception):
    """An expected scan or upload failure, reported as a CI warning."""


def warning(message):
    # Escape workflow-command control characters, and never log credentials.
    for name in ("DTRACK_API_KEY", "TRIVY_PASSWORD"):
        secret = os.environ.get(name)
        if secret:
            message = message.replace(secret, "***")
    escaped = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print(f"::warning title=Dependency-Track::{escaped}", flush=True)


def command(args, *, timeout, attempts=1):
    for attempt in range(attempts):
        try:
            result = subprocess.run(args, capture_output=True, text=True, timeout=timeout, check=False)
            if result.returncode == 0:
                return result.stdout
            reason = f"exit {result.returncode}"
            if result.stderr.strip():
                reason += ": " + result.stderr.strip().splitlines()[-1][-500:]
        except subprocess.TimeoutExpired:
            reason = "timeout"
        except OSError as exc:
            raise IntegrationError(f"{args[0]} could not be started: {exc.strerror}") from exc
        if attempt + 1 < attempts:
            time.sleep(2)
    # Arguments and response bodies are private; main() redacts stderr before logging.
    raise IntegrationError(f"{args[0]} failed ({reason})")


def resolve_image(repository, version, architecture):
    tag = f"{repository}:{version}-{architecture}"
    manifest = command(
        ["docker", "buildx", "imagetools", "inspect", "--format", "{{json .Manifest}}", tag],
        timeout=90, attempts=3,
    )
    try:
        digest = json.loads(manifest)["digest"]
    except (ValueError, KeyError, TypeError) as exc:
        raise IntegrationError("Registry returned no image digest") from exc
    if not isinstance(digest, str) or not re.fullmatch(r"sha256:[a-f0-9]{64}", digest):
        raise IntegrationError("Registry returned an invalid image digest")
    return f"{repository}@{digest}"


def remove_container(name):
    try:
        subprocess.run(["docker", "rm", "--force", name], capture_output=True, timeout=30, check=False)
    except (OSError, subprocess.TimeoutExpired):
        pass  # The runner also discards its containers at the end of the job.


def scan_image(image_ref, architecture, output):
    output = Path(output).resolve()
    output.parent.mkdir(parents=True, exist_ok=True)
    output.unlink(missing_ok=True)
    try:
        for attempt in range(2):
            name = f"datenportal-sbom-{uuid.uuid4().hex}"
            try:
                command([
                    "docker", "run", "--rm", "--name", name,
                    "--user", f"{os.getuid()}:{os.getgid()}",
                    "--volume", f"{output.parent}:/work",
                    "--env", "TRIVY_USERNAME", "--env", "TRIVY_PASSWORD",
                    "--env", "TRIVY_CACHE_DIR=/tmp/trivy-cache",
                    TRIVY_IMAGE, "image", "--image-src", "remote",
                    "--platform", f"linux/{architecture}", "--timeout", "10m",
                    "--format", "cyclonedx", "--output", f"/work/{output.name}", image_ref,
                ], timeout=720)
                break
            except IntegrationError:
                if attempt == 1:
                    raise
                time.sleep(2)
            finally:
                # Killing the Docker client on timeout alone would leave Trivy running.
                remove_container(name)
        with output.open(encoding="utf-8") as stream:
            bom = json.load(stream)
        if bom.get("bomFormat") != "CycloneDX" or not bom.get("specVersion"):
            raise IntegrationError("Trivy did not produce a CycloneDX BOM")
    except (IntegrationError, OSError, ValueError, AttributeError):
        output.unlink(missing_ok=True)
        raise


def upload_bom(base_url, api_key, project, version, output, *, connect_timeout=10,
               max_time=120, retries=2):
    url = urlsplit(base_url)
    local_http = url.scheme == "http" and url.hostname in ("localhost", "127.0.0.1", "::1")
    if (not url.hostname or (url.scheme != "https" and not local_http)
            or url.username or url.password or url.query or url.fragment
            or url.path not in ("", "/")):
        raise IntegrationError("DTRACK_URL must be an HTTPS origin without path or credentials")
    if not api_key.strip() or "\r" in api_key or "\n" in api_key:
        raise IntegrationError("DTRACK_API_KEY is missing or invalid")
    # Keep the API key out of process arguments and command output.
    with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8") as headers:
        headers.write(f"X-Api-Key: {api_key}\n")
        headers.flush()
        response = command([
            "curl", "--fail", "--silent", "--show-error",
            "--connect-timeout", str(connect_timeout), "--max-time", str(max_time),
            "--retry", str(retries), "--retry-all-errors", "--retry-delay", "2",
            "--retry-max-time", "250", "--request", "POST",
            f"{base_url.rstrip('/')}/api/v1/bom", "--header", f"@{headers.name}",
            "--form-string", "autoCreate=true", "--form-string", f"projectName={project}",
            "--form-string", f"projectVersion={version}",
            "--form", f"bom=@{Path(output).resolve()}",
        ], timeout=390)
    try:
        token = json.loads(response)["token"]
        uuid.UUID(token)
    except (ValueError, KeyError, TypeError, AttributeError) as exc:
        raise IntegrationError("Upload response did not contain a valid processing token") from exc
    return token


def report(project, version, image_ref, status):
    print(f"{project} {version}: {status}", flush=True)
    if summary := os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(summary, "a", encoding="utf-8") as stream:
            stream.write(f"### SBOM: {project}\n\nVersion: `{version}`\n\n")
            if image_ref:
                stream.write(f"Image: `{image_ref}`\n\n")
            stream.write(f"{status}\n\n")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--image", required=True, help="Published image repository without tag")
    parser.add_argument("--version", required=True)
    parser.add_argument("--architecture", required=True, choices=("amd64", "arm64"))
    parser.add_argument("--project", required=True)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args(argv)
    image_ref = None
    args.output.unlink(missing_ok=True)
    try:
        image_ref = resolve_image(args.image, args.version, args.architecture)
        scan_image(image_ref, args.architecture, args.output)
    except (IntegrationError, OSError, ValueError, AttributeError) as exc:
        warning(f"{args.project}: SBOM scan failed: {exc}")
        report(args.project, args.version, image_ref, "Warning: SBOM scan failed; upload skipped.")
        return 0

    base_url = os.environ.get("DTRACK_URL", "").strip()
    api_key = os.environ.get("DTRACK_API_KEY", "")
    if not base_url or not api_key:
        warning(f"{args.project}: DTRACK_URL or DTRACK_API_KEY missing; upload skipped")
        report(args.project, args.version, image_ref,
               "Warning: upload skipped because configuration is missing. SBOM retained as artifact.")
        return 0
    try:
        upload_bom(base_url, api_key, args.project, args.version, args.output)
    except (IntegrationError, OSError, ValueError) as exc:
        warning(f"{args.project}: BOM upload failed: {exc}")
        report(args.project, args.version, image_ref,
               "Warning: upload failed. SBOM retained as artifact for a later retry.")
        return 0
    report(args.project, args.version, image_ref,
           "Upload accepted. Dependency-Track processes and analyzes the BOM asynchronously.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
