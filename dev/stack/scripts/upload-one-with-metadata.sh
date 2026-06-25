#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
FILE="${1:?Usage: ./scripts/upload-one-with-metadata.sh <file> <object-key> [metadata]}"
KEY="${2:?Usage: ./scripts/upload-one-with-metadata.sh <file> <object-key> [metadata]}"
METADATA="${3:-uploaded_by=dev-stack,environment=local}"
BUCKET="${DATENPORTAL_BUCKET:-ch.so.datenportal}"

docker compose --profile tools run --rm \
  -v "$(realpath "$FILE"):/upload/file:ro" \
  aws s3 cp /upload/file "s3://${BUCKET}/${KEY}" --metadata "$METADATA"
