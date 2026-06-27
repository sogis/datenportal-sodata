#!/bin/sh
set -eu

BUCKET="${DATENPORTAL_BUCKET:-ch.so.datenportal}"
AWS_ENDPOINT_URL="${AWS_ENDPOINT_URL:-http://garage:3900}"
AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-garage}"
SEED_DIR="/seed/object-storage/${BUCKET}"

aws_call() {
  aws --endpoint-url "$AWS_ENDPOINT_URL" --region "$AWS_DEFAULT_REGION" "$@"
}

echo "Waiting for Garage S3 API at ${AWS_ENDPOINT_URL} ..."
i=0
until aws_call s3api list-buckets >/dev/null 2>&1; do
  i=$((i + 1))
  if [ "$i" -gt 90 ]; then
    echo "Garage S3 API did not become ready." >&2
    exit 1
  fi
  sleep 1
done

echo "Waiting for bucket ${BUCKET} ..."
i=0
until aws_call s3api head-bucket --bucket "$BUCKET" >/dev/null 2>&1; do
  i=$((i + 1))
  if [ "$i" -eq 10 ]; then
    echo "Bucket ${BUCKET} was not found. Trying to create it with the dev credentials ..."
    aws_call s3api create-bucket --bucket "$BUCKET" >/dev/null || true
  fi
  if [ "$i" -gt 60 ]; then
    echo "Bucket ${BUCKET} did not become available." >&2
    exit 1
  fi
  sleep 1
done

echo "Configuring public static website endpoint and permissive CORS ..."
aws_call s3api put-bucket-website \
  --bucket "$BUCKET" \
  --website-configuration file:///seed/init/website.json

aws_call s3api put-bucket-cors \
  --bucket "$BUCKET" \
  --cors-configuration file:///seed/init/cors.json

if [ ! -d "$SEED_DIR" ]; then
  echo "Seed directory does not exist: ${SEED_DIR}" >&2
  exit 1
fi

echo "Syncing ${SEED_DIR}/ to s3://${BUCKET}/ ..."
aws_call s3 sync "$SEED_DIR" "s3://${BUCKET}/" --delete

echo
echo "Seed completed. Useful endpoints:"
echo "  S3 API endpoint inside Docker: ${AWS_ENDPOINT_URL}"
echo "  Public download proxy on host: http://localhost:${DATENPORTAL_DOWNLOAD_PORT:-8081}/"
echo "  Example index: http://localhost:${DATENPORTAL_DOWNLOAD_PORT:-8081}/index.html"
echo
echo "A few uploaded objects:"
LISTING=$(aws_call s3 ls "s3://${BUCKET}/" --recursive 2>/dev/null || true)
echo "$LISTING" | head -20
