#! /bin/sh

set -e

# Download cert from encrypted storage if on fly.io
if test -n "$FLY_MACHINE_ID"; then
  echo "Running Fly.io startup checks..."
  if ! test -f "Suffolk.pfx"; then
    echo "Installing cert"
    apk add --no-cache aws-cli
    aws s3 cp $S3_SUFFOLK_CERT .
  fi
fi
