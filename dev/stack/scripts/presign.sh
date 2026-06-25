#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
KEY="${1:?Usage: ./scripts/presign.sh <object-key> [seconds]}"
SECONDS="${2:-3600}"
BUCKET="${DATENPORTAL_BUCKET:-ch.so.datenportal}"
docker compose --profile tools run --rm aws s3 presign "s3://${BUCKET}/${KEY}" --expires-in "$SECONDS"
