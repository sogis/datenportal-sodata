#!/usr/bin/env python3
"""Create a DuckDB catalog with views for PublishedCatalog Parquet downloads."""

from __future__ import annotations

import argparse
import os
import re
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import urlparse
import xml.etree.ElementTree as ET


DEFAULT_XTF = Path("spec/fixtures/published_catalog_full_62_entries.xtf")
DEFAULT_OUTPUT = Path("build/catalog.duckdb")
DEFAULT_DOWNLOAD_URL = "http://localhost:8081/ch.so.datenportal/downloads"
DEFAULT_SCHEMA = "opendata"
DOWNLOAD_URL_PLACEHOLDER = "${DOWNLOAD_URL}"
SAFE_IDENTIFIER = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")
EXPECTED_DUCKDB_VERSION = "1.5.4"
MINIMUM_PYTHON_VERSION = (3, 10)


@dataclass(frozen=True)
class ParquetView:
    name: str
    url: str


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)

    try:
        duckdb = import_duckdb()
        assert_duckdb_version(duckdb.__version__)

        schema = validate_identifier(args.schema, "schema")
        views = extract_parquet_views(args.xtf, args.download_url)
        create_database(duckdb, args.output, schema, views)
    except Exception as error:
        print(f"error: {error}", file=sys.stderr)
        return 1

    print(f"created {args.output} with schema {args.schema} and {len(views)} views")
    return 0


def parse_args(argv: list[str] | None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create a DuckDB file with one opendata view per PublishedCatalog Parquet distribution."
    )
    parser.add_argument(
        "--xtf",
        type=Path,
        default=DEFAULT_XTF,
        help=f"PublishedCatalog XTF file, default: {DEFAULT_XTF}",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT,
        help=f"Output DuckDB file, default: {DEFAULT_OUTPUT}",
    )
    parser.add_argument(
        "--download-url",
        default=DEFAULT_DOWNLOAD_URL,
        help=f"Replacement for {DOWNLOAD_URL_PLACEHOLDER}, default: {DEFAULT_DOWNLOAD_URL}",
    )
    parser.add_argument(
        "--schema",
        default=DEFAULT_SCHEMA,
        help=f"DuckDB schema for generated views, default: {DEFAULT_SCHEMA}",
    )
    return parser.parse_args(argv)


def import_duckdb():
    if sys.version_info < MINIMUM_PYTHON_VERSION:
        required = ".".join(str(part) for part in MINIMUM_PYTHON_VERSION)
        current = ".".join(str(part) for part in sys.version_info[:3])
        raise RuntimeError(
            f"Python {required}+ is required for duckdb=={EXPECTED_DUCKDB_VERSION}, "
            f"but this interpreter is Python {current}"
        )
    try:
        import duckdb  # type: ignore
    except ModuleNotFoundError as error:
        raise RuntimeError(
            f"Python package duckdb=={EXPECTED_DUCKDB_VERSION} is required. "
            f"Install it with: python3 -m pip install duckdb=={EXPECTED_DUCKDB_VERSION}"
        ) from error
    return duckdb


def assert_duckdb_version(version: str) -> None:
    if version != EXPECTED_DUCKDB_VERSION:
        raise RuntimeError(
            f"duckdb=={EXPECTED_DUCKDB_VERSION} is required, but Python imported duckdb=={version}"
        )


def extract_parquet_views(xtf_path: Path, download_url: str) -> list[ParquetView]:
    if not xtf_path.is_file():
        raise FileNotFoundError(f"XTF file does not exist: {xtf_path}")

    tree = ET.parse(xtf_path)
    views: list[ParquetView] = []
    seen_names: dict[str, str] = {}

    for distribution in tree.getroot().iter():
        if local_name(distribution.tag) != "Distribution":
            continue

        fields = {
            local_name(child.tag): (child.text or "").strip()
            for child in list(distribution)
        }
        if fields.get("format", "").lower() != "parquet":
            continue

        raw_url = fields.get("downloadURL", "")
        if not raw_url:
            raise ValueError("Found a parquet Distribution without downloadURL")

        resolved_url = raw_url.replace(DOWNLOAD_URL_PLACEHOLDER, download_url.rstrip("/"))
        view_name = view_name_from_url(resolved_url)

        previous_url = seen_names.get(view_name)
        if previous_url is not None:
            raise ValueError(
                f"Duplicate view name {view_name!r} derived from {previous_url!r} and {resolved_url!r}"
            )
        seen_names[view_name] = resolved_url
        views.append(ParquetView(name=view_name, url=resolved_url))

    if not views:
        raise ValueError(f"No parquet distributions found in {xtf_path}")

    return views


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def view_name_from_url(url: str) -> str:
    filename = Path(urlparse(url).path).name
    if not filename:
        raise ValueError(f"Cannot derive a view name from URL without filename: {url!r}")

    stem = filename[:-8] if filename.lower().endswith(".parquet") else Path(filename).stem
    view_name = stem.replace(".", "_")
    return validate_identifier(view_name, f"view name derived from {filename!r}")


def validate_identifier(identifier: str, label: str) -> str:
    if not SAFE_IDENTIFIER.fullmatch(identifier):
        raise ValueError(f"Unsafe {label}: {identifier!r}")
    return identifier


def create_database(duckdb, output_path: Path, schema: str, views: list[ParquetView]) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)

    tmp_file = tempfile.NamedTemporaryFile(
        prefix=f".{output_path.name}.",
        suffix=".tmp",
        dir=output_path.parent,
        delete=False,
    )
    tmp_path = Path(tmp_file.name)
    tmp_file.close()

    try:
        if tmp_path.exists():
            tmp_path.unlink()

        connection = duckdb.connect(str(tmp_path))
        try:
            database = connection.execute("SELECT current_database()").fetchone()[0]
            connection.execute("INSTALL httpfs")
            connection.execute("LOAD httpfs")
            connection.execute(
                f"CREATE SCHEMA IF NOT EXISTS {quote_identifier(database)}.{quote_identifier(schema)}"
            )
            for view in views:
                connection.execute(create_view_sql(database, schema, view))
        finally:
            connection.close()

        os.replace(tmp_path, output_path)
    except Exception:
        tmp_path.unlink(missing_ok=True)
        raise


def create_view_sql(database: str, schema: str, view: ParquetView) -> str:
    return (
        "CREATE OR REPLACE VIEW "
        f"{quote_identifier(database)}.{quote_identifier(schema)}.{quote_identifier(view.name)} AS\n"
        "SELECT *\n"
        f"FROM read_parquet({quote_string(view.url)})"
    )


def quote_identifier(identifier: str) -> str:
    return '"' + identifier.replace('"', '""') + '"'


def quote_string(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


if __name__ == "__main__":
    raise SystemExit(main())
