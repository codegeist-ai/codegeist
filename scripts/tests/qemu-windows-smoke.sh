#!/usr/bin/env bash
# qemu-windows-smoke.sh - Windows QEMU VM smoke entrypoint over SSH.
#
# Why this exists:
# - Keeps local Windows validation release-grade by running in a real Windows VM
#   instead of a compatibility layer.
# - Lets the final local smoke suite include Windows while reporting precise
#   failures unless developer-only skip mode is explicitly enabled.
#
# Inputs:
# - `CODEGEIST_WINDOWS_SSH_TARGET`: OpenSSH target, for example
#   `codegeist@127.0.0.1`.
# - `CODEGEIST_WINDOWS_REPO_DIR`: absolute path to this repository inside the VM.
# - Optional `CODEGEIST_WINDOWS_SSH_PORT` and `CODEGEIST_WINDOWS_SSH_KEY`.
# - Optional `CODEGEIST_WINDOWS_NATIVE_MODE`: `auto`, `skip`, or `required`.
# - Optional `CODEGEIST_WINDOWS_MSVC_CMD`: command that activates MSVC build tools
#   before Maven native compile, for example a quoted `VsDevCmd.bat` command.
# - Optional `CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS` and
#   `CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS` bound version smoke execution.
# - Optional `CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS` bounds the real Ollama ask
#   smoke. Optional `CODEGEIST_WINDOWS_OLLAMA_BASE_URL` overrides the QEMU guest
#   route to the host Ollama service.
# - Optional `CODEGEIST_WINDOWS_ALLOW_SKIP=1` converts missing host prerequisites
#   into skipped status for developer-only runs.
# - Optional `CODEGEIST_SMOKE_STATUS_FILE` writes a key-value status summary.
#
# Side effects:
# - Executes `scripts/tests/windows-smoke.ps1` inside the Windows VM.
# - Rebuilds jar and optionally native artifacts in the VM checkout.
#
# Related files:
# - scripts/tests/windows-smoke.ps1
# - scripts/tests/final-smoke-suite.sh

set -euo pipefail

status_file="${CODEGEIST_SMOKE_STATUS_FILE:-}"
ssh_target="${CODEGEIST_WINDOWS_SSH_TARGET:-}"
repo_dir="${CODEGEIST_WINDOWS_REPO_DIR:-}"
native_mode="${CODEGEIST_WINDOWS_NATIVE_MODE:-auto}"
allow_skip="${CODEGEIST_WINDOWS_ALLOW_SKIP:-0}"
smoke_start=""

codegeist_now_ms() {
  date +%s%3N
}

codegeist_print_duration() {
  local label="$1"
  local start_ms="$2"
  local end_ms
  local elapsed_ms

  end_ms="$(codegeist_now_ms)"
  elapsed_ms=$((end_ms - start_ms))
  printf 'Duration: %s: %d.%03ds\n' "$label" $((elapsed_ms / 1000)) $((elapsed_ms % 1000))
}

write_status() {
  local status="$1"
  local reason="$2"

  if [ -z "$status_file" ]; then
    return 0
  fi

  mkdir -p "$(dirname -- "$status_file")"
  {
    printf 'status=%s\n' "$status"
    printf 'platform=windows-x64\n'
    printf 'transport=ssh\n'
    printf 'reason=%s\n' "$reason"
  } > "$status_file"
}

ps_quote() {
  local value="$1"
  value="${value//\'/\'\'}"
  printf "'%s'" "$value"
}

skip_smoke() {
  local reason="$1"

  write_status skipped "$reason"
  printf 'Platform smoke status: skipped\n'
  printf 'Platform: windows-x64\n'
  printf 'Reason: %s\n' "$reason"
  exit 0
}

missing_prerequisite() {
  local reason="$1"

  if [ "$allow_skip" = '1' ]; then
    skip_smoke "$reason"
  fi

  fail_smoke "$reason"
}

fail_smoke() {
  local reason="$1"

  write_status failed "$reason"
  printf 'Platform smoke status: failed\n'
  printf 'Platform: windows-x64\n'
  printf 'Reason: %s\n' "$reason"
  exit 1
}

case "$native_mode" in
  auto|skip|required) ;;
  *) fail_smoke "CODEGEIST_WINDOWS_NATIVE_MODE must be auto, skip, or required" ;;
esac

case "${CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS:-}" in
  ''|*[!0-9]*)
    if [ -n "${CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS:-}" ]; then
      fail_smoke 'CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS must be an integer'
    fi
    ;;
esac

case "${CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS:-}" in
  ''|*[!0-9]*)
    if [ -n "${CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS:-}" ]; then
      fail_smoke 'CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS must be an integer'
    fi
    ;;
esac

case "${CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS:-}" in
  ''|*[!0-9]*)
    if [ -n "${CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS:-}" ]; then
      fail_smoke 'CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS must be an integer'
    fi
    ;;
esac

if [ -z "$ssh_target" ]; then
  missing_prerequisite 'CODEGEIST_WINDOWS_SSH_TARGET is not set'
fi

if [ -z "$repo_dir" ]; then
  missing_prerequisite 'CODEGEIST_WINDOWS_REPO_DIR is not set'
fi

if ! command -v ssh >/dev/null 2>&1; then
  missing_prerequisite 'ssh is not available on PATH'
fi

ssh_args=(-o BatchMode=yes -o ConnectTimeout="${CODEGEIST_WINDOWS_SSH_CONNECT_TIMEOUT:-10}")

if [ -n "${CODEGEIST_WINDOWS_SSH_PORT:-}" ]; then
  ssh_args+=(-p "$CODEGEIST_WINDOWS_SSH_PORT")
fi

if [ -n "${CODEGEIST_WINDOWS_SSH_KEY:-}" ]; then
  ssh_args+=(-i "$CODEGEIST_WINDOWS_SSH_KEY")
fi

repo_arg="$(ps_quote "$repo_dir")"
native_arg="$(ps_quote "$native_mode")"
script_arg="$(ps_quote "$repo_dir\\scripts\\tests\\windows-smoke.ps1")"
remote_command="powershell -NoProfile -ExecutionPolicy Bypass -Command \"& $script_arg -RepoDir $repo_arg -NativeMode $native_arg"

if [ -n "${CODEGEIST_WINDOWS_MSVC_CMD:-}" ]; then
  msvc_arg="$(ps_quote "$CODEGEIST_WINDOWS_MSVC_CMD")"
  remote_command="$remote_command -MsvcCommand $msvc_arg"
fi

if [ -n "${CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS:-}" ]; then
  remote_command="$remote_command -JarTimeoutSeconds $CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS"
fi

if [ -n "${CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS:-}" ]; then
  remote_command="$remote_command -NativeTimeoutSeconds $CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS"
fi

if [ -n "${CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS:-}" ]; then
  remote_command="$remote_command -AskTimeoutSeconds $CODEGEIST_WINDOWS_ASK_TIMEOUT_SECONDS"
fi

if [ -n "${CODEGEIST_WINDOWS_OLLAMA_BASE_URL:-}" ]; then
  ollama_base_url_arg="$(ps_quote "$CODEGEIST_WINDOWS_OLLAMA_BASE_URL")"
  remote_command="$remote_command -OllamaBaseUrl $ollama_base_url_arg"
fi

remote_command="$remote_command\""

printf 'Platform: windows-x64\n'
printf 'Transport: ssh\n'
printf 'Command: powershell scripts/tests/windows-smoke.ps1\n'
smoke_start="$(codegeist_now_ms)"

if ssh "${ssh_args[@]}" "$ssh_target" "$remote_command"; then
  write_status passed 'none'
  printf 'Platform smoke status: passed\n'
  printf 'Platform: windows-x64\n'
  codegeist_print_duration 'windows ssh smoke command' "$smoke_start"
else
  fail_smoke 'Windows VM smoke command failed'
fi
