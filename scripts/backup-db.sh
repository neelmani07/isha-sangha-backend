#!/usr/bin/env bash
# Dumps the database and uploads it to the S3-compatible bucket (Cloudflare R2) under backups/.
# Used by .github/workflows/db-backup.yml; can also be run by hand with the same variables set.
#
#   BACKUP_DATABASE_URL  postgresql://user:password@host/dbname?sslmode=require
#   STORAGE_BUCKET, STORAGE_ENDPOINT, STORAGE_ACCESS_KEY, STORAGE_SECRET_KEY  (same values as the backend)
set -euo pipefail

: "${BACKUP_DATABASE_URL:?BACKUP_DATABASE_URL is not set}"
: "${STORAGE_BUCKET:?STORAGE_BUCKET is not set}"

stamp=$(date -u +%Y-%m-%d)
file="isha-sangha-${stamp}.dump"

# Custom format: compressed, and restorable table by table with pg_restore.
pg_dump --format=custom --no-owner --no-privileges --file="$file" "$BACKUP_DATABASE_URL"
size=$(wc -c < "$file")
if [ "$size" -lt 1024 ]; then
  echo "Backup looks empty ($size bytes); not uploading." >&2
  exit 1
fi

AWS_ACCESS_KEY_ID="$STORAGE_ACCESS_KEY" AWS_SECRET_ACCESS_KEY="$STORAGE_SECRET_KEY" AWS_DEFAULT_REGION="${STORAGE_REGION:-auto}" \
  aws s3 cp "$file" "s3://${STORAGE_BUCKET}/backups/${file}" ${STORAGE_ENDPOINT:+--endpoint-url "$STORAGE_ENDPOINT"} --only-show-errors

echo "Uploaded backups/${file} (${size} bytes)"
rm -f "$file"
