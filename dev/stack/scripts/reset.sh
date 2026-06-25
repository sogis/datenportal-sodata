#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ "${1:-}" != "--yes" ]]; then
  echo "This removes all local dev-stack runtime data below dev/stack/var/."
  echo "Run: $0 --yes"
  exit 1
fi
docker compose down -v --remove-orphans
find ./var -mindepth 1 ! -name .gitkeep -exec rm -rf {} +
