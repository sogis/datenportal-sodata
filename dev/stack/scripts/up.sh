#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose up -d garage downloads
docker compose --profile seed run --rm seed
