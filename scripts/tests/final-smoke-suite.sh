#!/usr/bin/env bash
# final-smoke-suite.sh - local final smoke suite for Linux and Windows.
#
# Why this exists:
# - Makes local Linux and Windows artifact smoke checks part of one final test
#   entrypoint before GitHub release automation is added.
# - Treats Windows as required by default; developer-only skip behavior must be
#   requested explicitly with `--allow-skips`.
#
# Inputs:
# - Optional `--allow-skips`: allow missing platform prerequisites to report
#   skipped without failing the suite.
# - Windows VM inputs consumed by `scripts/tests/qemu-windows-vm.sh`.
#
# Side effects:
# - Runs the local Linux smoke script and the Windows QEMU smoke script.
# - Writes status summaries under
#   `app/codegeist/cli/target/smoke-test/final-smoke-suite`.
#
# Related files:
# - scripts/tests/local-linux-smoke.sh
# - scripts/tests/qemu-windows-vm.sh
# - app/codegeist/cli/Taskfile.yml

set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/../.." && pwd)"
suite_dir="$repo_root/app/codegeist/cli/target/smoke-test/final-smoke-suite"
allow_skips=0

usage() {
  printf 'Usage: %s [--allow-skips]\n' "$(basename -- "$0")"
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --allow-skips)
      allow_skips=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      usage >&2
      exit 2
      ;;
  esac
done

read_status() {
  local file="$1"
  local key value

  if [ ! -f "$file" ]; then
    printf 'failed'
    return 0
  fi

  while IFS='=' read -r key value; do
    if [ "$key" = 'status' ]; then
      printf '%s' "$value"
      return 0
    fi
  done < "$file"

  printf 'failed'
}

run_platform_smoke() {
  local name="$1"
  local script="$2"
  local status_file="$suite_dir/$name.status"
  local status
  local exit_code=0
  shift 2

  mkdir -p "$suite_dir"
  printf 'Suite platform: %s\n' "$name"

  set +e
  CODEGEIST_SMOKE_STATUS_FILE="$status_file" "$script" "$@"
  exit_code=$?
  set -e

  mkdir -p "$suite_dir"
  status="$(read_status "$status_file")"

  printf 'Suite platform status: %s %s\n' "$name" "$status"

  if [ "$exit_code" -ne 0 ]; then
    return 1
  fi

  if [ "$allow_skips" -eq 0 ] && [ "$status" = 'skipped' ]; then
    printf 'Final suite requires %s smoke to pass.\n' "$name" >&2
    return 1
  fi

  if [ "$status" = 'failed' ]; then
    return 1
  fi

  return 0
}

if [ "$allow_skips" -eq 0 ]; then
  export CODEGEIST_SMOKE_REQUIRE_NATIVE="${CODEGEIST_SMOKE_REQUIRE_NATIVE:-1}"
  export CODEGEIST_WINDOWS_NATIVE_MODE="${CODEGEIST_WINDOWS_NATIVE_MODE:-required}"
  export CODEGEIST_WINDOWS_ALLOW_SKIP=0
else
  export CODEGEIST_WINDOWS_ALLOW_SKIP=1
fi

rm -rf "$suite_dir"
mkdir -p "$suite_dir"

failures=0

run_platform_smoke linux "$script_dir/local-linux-smoke.sh" || failures=$((failures + 1))
run_platform_smoke windows-x64 "$script_dir/qemu-windows-vm.sh" smoke || failures=$((failures + 1))

printf 'Final smoke suite status: '
if [ "$failures" -eq 0 ]; then
  printf 'passed\n'
  exit 0
fi

printf 'failed\n'
printf 'Failed platform checks: %s\n' "$failures"
exit 1
