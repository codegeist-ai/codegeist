#!/usr/bin/env sh
# init.sh - prepare the MinIO bucket and OIDC policy for the local smoke stack.
#
# Why this exists:
# - Keeps compose.yml declarative while still waiting for MinIO readiness.
# - Creates only local smoke resources: the codegeist-artifacts bucket and the
#   codegeist-artifacts-smoke policy used by MinIO STS credentials.
#
# Inputs:
# - MINIO_ROOT_USER and MINIO_ROOT_PASSWORD from compose.yml.
# - /policies/codegeist-artifacts-smoke.json mounted from this fixture.
#
# Related files:
# - ../compose.yml
# - policies/codegeist-artifacts-smoke.json

set -eu

for attempt in $(seq 1 60); do
  if mc alias set local http://minio:9000 "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}" >/dev/null 2>&1; then
    break
  fi
  if [ "$attempt" -eq 60 ]; then
    printf 'MinIO did not become ready for initialization\n' >&2
    exit 1
  fi
  sleep 2
done

mc mb --ignore-existing local/codegeist-artifacts

if ! mc admin policy info local codegeist-artifacts-smoke >/dev/null 2>&1; then
  mc admin policy create local codegeist-artifacts-smoke /policies/codegeist-artifacts-smoke.json
fi

mc anonymous set none local/codegeist-artifacts
printf 'MinIO OIDC smoke bucket and policy are ready\n'
