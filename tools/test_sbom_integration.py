"""Run with: python3 -m unittest discover -s tools -p 'test_sbom_integration.py' -v."""

from contextlib import contextmanager, redirect_stdout
from email import policy
from email.parser import BytesParser
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import importlib.util
import io
import json
import os
from pathlib import Path
import subprocess
import tempfile
import threading
import time
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("sbom", Path(__file__).with_name("scan-and-upload-sbom.py"))
sbom = importlib.util.module_from_spec(spec)
spec.loader.exec_module(sbom)

BOM = {"bomFormat": "CycloneDX", "specVersion": "1.6", "version": 1, "components": []}
TOKEN = "b2da2b00-14f2-4b1e-a480-0ce5617a2514"
DIGEST = "sha256:" + "a" * 64


@contextmanager
def endpoint(status=200, delay=0, response=None):
    requests = []

    class Handler(BaseHTTPRequestHandler):
        def do_POST(self):
            data = self.rfile.read(int(self.headers["Content-Length"]))
            message = BytesParser(policy=policy.default).parsebytes(
                f'Content-Type: {self.headers["Content-Type"]}\r\n\r\n'.encode() + data)
            fields = {part.get_param("name", header="content-disposition"): part.get_payload(decode=True)
                      for part in message.iter_parts()}
            requests.append((self.path, self.headers["X-Api-Key"], fields))
            time.sleep(delay)
            try:
                self.send_response(status)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(response if response is not None else json.dumps({"token": TOKEN}).encode())
            except (BrokenPipeError, ConnectionResetError):
                pass

        def log_message(self, *_):
            pass

    server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        yield f"http://127.0.0.1:{server.server_port}", requests
    finally:
        server.shutdown()
        server.server_close()
        thread.join()


class SbomIntegrationTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.output = Path(self.temp.name) / "sbom.json"
        self.output.write_text(json.dumps(BOM))
        cleanup = patch.object(sbom, "remove_container")
        self.cleanup_container = cleanup.start()
        self.addCleanup(cleanup.stop)

    def upload(self, base_url, **kwargs):
        return sbom.upload_bom(base_url, "test-secret", "datenportal-native-arm64", "0.1.187",
                               self.output, retries=0, **kwargs)

    def test_real_multipart_upload_keeps_four_projects_separate(self):
        with endpoint() as (url, requests):
            for runtime in ("native", "jvm"):
                for architecture in ("amd64", "arm64"):
                    project = f"datenportal-{runtime}-{architecture}"
                    token = sbom.upload_bom(url + "/", "test-secret", project, "0.1.187",
                                            self.output, retries=0)
                    self.assertEqual(token, TOKEN)
        self.assertEqual(len(requests), 4)
        self.assertEqual(len({fields["projectName"] for _, _, fields in requests}), 4)
        for path, key, fields in requests:
            self.assertEqual(path, "/api/v1/bom")
            self.assertEqual(key, "test-secret")
            self.assertEqual(fields["autoCreate"], b"true")
            self.assertEqual(fields["projectVersion"], b"0.1.187")
            self.assertEqual(json.loads(fields["bom"]), BOM)

    def test_real_http_errors_are_not_accepted(self):
        for status in (401, 403, 500):
            with self.subTest(status=status), endpoint(status=status) as (url, _):
                with self.assertRaises(sbom.IntegrationError):
                    self.upload(url)

    def test_real_timeout_is_bounded(self):
        with endpoint(delay=0.5) as (url, _):
            with self.assertRaises(sbom.IntegrationError):
                self.upload(url, max_time=0.1)

    def test_http_success_without_processing_token_is_not_accepted(self):
        with endpoint(response=b'{"unexpected": true}') as (url, _):
            with self.assertRaises(sbom.IntegrationError):
                self.upload(url)

    def test_api_key_is_passed_in_private_temporary_file(self):
        header_path = None

        def check_command(args, **_):
            nonlocal header_path
            self.assertNotIn("test-secret", " ".join(args))
            header_path = Path(args[args.index("--header") + 1][1:])
            self.assertEqual(header_path.read_text(), "X-Api-Key: test-secret\n")
            self.assertEqual(header_path.stat().st_mode & 0o777, 0o600)
            return json.dumps({"token": TOKEN})

        with patch.object(sbom, "command", side_effect=check_command):
            self.upload("https://dtrack.example.org")
        self.assertFalse(header_path.exists())

    def test_rejects_insecure_or_ambiguous_urls(self):
        for url in ("http://example.org", "https://user:pass@example.org", "https://example.org/api",
                    "https://example.org?key=secret", "https://example.org#fragment"):
            with self.subTest(url=url), patch.object(sbom, "command") as command:
                with self.assertRaises(sbom.IntegrationError):
                    self.upload(url)
                command.assert_not_called()

    def test_resolves_versioned_architecture_tag_to_digest(self):
        with patch.object(sbom, "command", return_value=json.dumps({"digest": DIGEST})) as command:
            ref = sbom.resolve_image("ghcr.io/sogis/datenportal-sodata", "0.1.187", "arm64")
        self.assertEqual(ref, f"ghcr.io/sogis/datenportal-sodata@{DIGEST}")
        self.assertEqual(command.call_args.args[0][-1], "ghcr.io/sogis/datenportal-sodata:0.1.187-arm64")

    def test_invalid_digest_is_rejected(self):
        for manifest in ('{}', '{"digest":"latest"}', 'not json'):
            with self.subTest(manifest=manifest), patch.object(sbom, "command", return_value=manifest):
                with self.assertRaises(sbom.IntegrationError):
                    sbom.resolve_image("ghcr.io/test/image", "0.1.187", "amd64")

    def test_scan_uses_remote_digest_platform_and_pinned_trivy(self):
        def scan(args, **_):
            self.assertIn("aquasec/trivy:0.76.1", args)
            self.assertEqual(args[args.index("--platform") + 1], "linux/arm64")
            self.assertEqual(args[args.index("--image-src") + 1], "remote")
            self.assertEqual(args[-1], f"ghcr.io/test/image@{DIGEST}")
            self.assertNotIn("/var/run/docker.sock", " ".join(args))
            self.output.write_text(json.dumps(BOM))
            return ""

        with patch.object(sbom, "command", side_effect=scan):
            sbom.scan_image(f"ghcr.io/test/image@{DIGEST}", "arm64", self.output)

    def test_partial_scan_output_is_removed(self):
        def fail(*_, **__):
            self.output.write_text("partial")
            raise sbom.IntegrationError("Trivy failed")

        with patch.object(sbom, "command", side_effect=fail), patch.object(sbom.time, "sleep"):
            with self.assertRaises(sbom.IntegrationError):
                sbom.scan_image(f"ghcr.io/test/image@{DIGEST}", "amd64", self.output)
        self.assertFalse(self.output.exists())
        self.assertEqual(self.cleanup_container.call_count, 2)
        names = [call.args[0] for call in self.cleanup_container.call_args_list]
        self.assertEqual(len(set(names)), 2)

    def run_main(self, env, *, scan_error=None, upload_error=None):
        def scan(*_):
            if scan_error:
                raise sbom.IntegrationError(scan_error)
            self.output.write_text(json.dumps(BOM))

        summary = Path(self.temp.name) / "summary.md"
        with patch.dict(os.environ, {**env, "GITHUB_STEP_SUMMARY": str(summary)}, clear=True), \
                patch.object(sbom, "resolve_image", return_value=f"ghcr.io/test/image@{DIGEST}"), \
                patch.object(sbom, "scan_image", side_effect=scan), \
                patch.object(sbom, "upload_bom", side_effect=upload_error) as upload, \
                redirect_stdout(io.StringIO()) as log:
            result = sbom.main(["--image", "ghcr.io/test/image", "--version", "0.1.187",
                                "--architecture", "amd64", "--project", "datenportal-native-amd64",
                                "--output", str(self.output)])
        self.assertEqual(result, 0)
        return log.getvalue(), summary.read_text(), upload

    def test_missing_config_warns_but_keeps_sbom(self):
        log, summary, upload = self.run_main({})
        self.assertIn("::warning", log)
        self.assertIn("configuration is missing", summary)
        self.assertTrue(self.output.exists())
        upload.assert_not_called()

    def test_scan_failure_warns_and_skips_upload(self):
        log, summary, upload = self.run_main({}, scan_error="registry unavailable")
        self.assertIn("::warning", log)
        self.assertIn("scan failed", summary)
        self.assertFalse(self.output.exists())
        upload.assert_not_called()

    def test_upload_failure_warns_and_keeps_sbom_without_logging_key(self):
        env = {"DTRACK_URL": "https://example.org", "DTRACK_API_KEY": "private-key"}
        log, summary, _ = self.run_main(env, upload_error=sbom.IntegrationError("private-key"))
        self.assertIn("::warning", log)
        self.assertNotIn("private-key", log + summary)
        self.assertIn("upload failed", summary)
        self.assertTrue(self.output.exists())

    def test_success_reports_accepted_not_analysis_complete(self):
        log, summary, _ = self.run_main({"DTRACK_URL": "https://example.org", "DTRACK_API_KEY": "key"})
        self.assertNotIn("::warning", log)
        self.assertIn("Upload accepted", summary)
        self.assertIn("asynchronously", summary)

    def test_subprocess_timeout_is_retried_and_reported(self):
        with patch.object(sbom.subprocess, "run", side_effect=subprocess.TimeoutExpired("docker", 1)) as run, \
                patch.object(sbom.time, "sleep"):
            with self.assertRaisesRegex(sbom.IntegrationError, "timeout"):
                sbom.command(["docker"], timeout=1, attempts=2)
        self.assertEqual(run.call_count, 2)


if __name__ == "__main__":
    unittest.main()
