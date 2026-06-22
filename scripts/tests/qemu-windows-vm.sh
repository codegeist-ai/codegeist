#!/usr/bin/env bash
# qemu-windows-vm.sh - automate the local Windows QEMU smoke VM.
#
# Why this exists:
# - Makes Windows local smoke validation repeatable instead of relying on a
#   manually prepared VM.
# - Keeps the Windows ISO as an explicit operator-provided input while automating
#   disk creation, answer media, QEMU startup, repo sync, and smoke execution.
#
# Inputs:
# - `CODEGEIST_WINDOWS_ISO`: optional official Windows Server Evaluation ISO path.
#   Defaults to `.local/windows-qemu/downloads/windows-server-2025-eval.iso`.
# - `CODEGEIST_WINDOWS_ISO_URL`: optional official Microsoft ISO URL. Defaults to
#   the Windows Server 2025 Evaluation English ISO fwlink.
# - Optional `CODEGEIST_WINDOWS_ISO_SHA256` verifies the downloaded or existing
#   ISO when a checksum is provided.
# - Optional `CODEGEIST_WINDOWS_VM_DIR`, default `.local/windows-qemu`.
# - Optional QEMU settings: `CODEGEIST_WINDOWS_MEMORY`,
#   `CODEGEIST_WINDOWS_CPUS`, `CODEGEIST_WINDOWS_DISK_SIZE`,
#   `CODEGEIST_WINDOWS_SSH_PORT`, `CODEGEIST_WINDOWS_DISPLAY`, and
#   `CODEGEIST_WINDOWS_CPU`.
# - Optional `CODEGEIST_WINDOWS_IMAGE_INDEX`, default `4`, selects Datacenter
#   Evaluation with Desktop Experience from current Windows Server Evaluation ISOs.
# - Optional `CODEGEIST_WINDOWS_ALLOW_SKIP=1` converts missing ISO/tooling into a
#   skipped status for developer-only runs.
# - `curl` must be available on `PATH` for automatic ISO download.
#
# Side effects:
# - Creates local VM state under `.local/windows-qemu` by default.
# - Generates a local password and SSH key under the VM directory.
# - Starts QEMU with host port forwarding from localhost to Windows SSH.
# - Copies the current working tree subset needed by smoke checks into Windows.
# - Starts the host Ollama service through `app/codegeist/cli/Taskfile.yml` so the
#   Windows guest can reach it through QEMU user networking at `10.0.2.2:11434`.
#
# Related files:
# - scripts/tests/windows-qemu/autounattend.xml
# - scripts/tests/windows-qemu/setup.ps1
# - scripts/tests/qemu-windows-smoke.ps1
# - scripts/tests/final-smoke-suite.ps1

set -euo pipefail

command="${1:-}"
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/../.." && pwd)"
vm_dir="${CODEGEIST_WINDOWS_VM_DIR:-$repo_root/.local/windows-qemu}"
download_dir="$vm_dir/downloads"
default_iso_url='https://go.microsoft.com/fwlink/?linkid=2345730&clcid=0x409&culture=en-us&country=us'
iso_path="${CODEGEIST_WINDOWS_ISO:-$download_dir/windows-server-2025-eval.iso}"
iso_url="${CODEGEIST_WINDOWS_ISO_URL:-$default_iso_url}"
disk="$vm_dir/windows.qcow2"
answer_iso="$vm_dir/autounattend.iso"
answer_label='CGSETUP'
ssh_key="$vm_dir/id_ed25519"
pid_file="$vm_dir/qemu.pid"
monitor_socket="$vm_dir/qemu-monitor.sock"
credentials_file="$vm_dir/credentials.env"
ssh_port="${CODEGEIST_WINDOWS_SSH_PORT:-2222}"
repo_dir="${CODEGEIST_WINDOWS_REPO_DIR:-C:\\codegeist}"
memory="${CODEGEIST_WINDOWS_MEMORY:-8192}"
cpus="${CODEGEIST_WINDOWS_CPUS:-4}"
disk_size="${CODEGEIST_WINDOWS_DISK_SIZE:-80G}"
display="${CODEGEIST_WINDOWS_DISPLAY:-none}"
ssh_user='codegeist'
image_index="${CODEGEIST_WINDOWS_IMAGE_INDEX:-4}"
install_timeout_seconds="${CODEGEIST_WINDOWS_INSTALL_TIMEOUT_SECONDS:-7200}"
allow_skip="${CODEGEIST_WINDOWS_ALLOW_SKIP:-0}"
status_file="${CODEGEIST_SMOKE_STATUS_FILE:-}"
ready_file='C:\codegeist-vm-ready.txt'

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

usage() {
  printf 'Usage: %s download|create|start|sync|smoke|stop|status\n' "$(basename -- "$0")"
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
    printf 'transport=qemu-ssh\n'
    printf 'reason=%s\n' "$reason"
  } > "$status_file"
}

skip_or_fail() {
  local reason="$1"

  if [ "$allow_skip" = '1' ]; then
    write_status skipped "$reason"
    printf 'Platform smoke status: skipped\n'
    printf 'Platform: windows-x64\n'
    printf 'Reason: %s\n' "$reason"
    exit 0
  fi

  write_status failed "$reason"
  printf 'Platform smoke status: failed\n'
  printf 'Platform: windows-x64\n'
  printf 'Reason: %s\n' "$reason" >&2
  exit 1
}

require_cmd() {
  local name="$1"

  if ! command -v "$name" >/dev/null 2>&1; then
    skip_or_fail "$name is not available on PATH"
  fi
}

download_file() {
  local url="$1"
  local output_file="$2"
  local temp_file="$output_file.tmp"

  mkdir -p "$(dirname -- "$output_file")"

  curl --location --fail --retry 3 --output "$temp_file" "$url" \
    || { rm -f "$temp_file"; skip_or_fail "Failed to download Windows ISO from $url"; }

  mv "$temp_file" "$output_file"
}

verify_iso_checksum() {
  if [ -z "${CODEGEIST_WINDOWS_ISO_SHA256:-}" ]; then
    return 0
  fi

  require_cmd sha256sum
  local actual
  actual="$(sha256sum "$iso_path" | cut -d ' ' -f 1)"

  if [ "$actual" != "$CODEGEIST_WINDOWS_ISO_SHA256" ]; then
    skip_or_fail "Windows ISO checksum mismatch for $iso_path"
  fi
}

ensure_iso() {
  if [ -f "$iso_path" ]; then
    verify_iso_checksum
    return 0
  fi

  if [ "$allow_skip" = '1' ] \
    && [ -z "${CODEGEIST_WINDOWS_ISO+x}" ] \
    && [ -z "${CODEGEIST_WINDOWS_ISO_URL+x}" ]; then
    skip_or_fail 'Windows ISO is not present; set CODEGEIST_WINDOWS_ISO or run without --allow-skips to download the default Microsoft evaluation ISO'
  fi

  printf 'Windows ISO not found at %s\n' "$iso_path"
  printf 'Downloading Windows Server Evaluation ISO from %s\n' "$iso_url"
  download_file "$iso_url" "$iso_path"
  verify_iso_checksum
}

ensure_password() {
  if [ -f "$credentials_file" ]; then
    # shellcheck disable=SC1090
    source "$credentials_file"
  fi

  if [ -n "${CODEGEIST_WINDOWS_PASSWORD:-}" ]; then
    return 0
  fi

  mkdir -p "$vm_dir"
  CODEGEIST_WINDOWS_PASSWORD="Cg$(od -An -tx1 -N12 /dev/urandom | tr -d ' \n')!"
  umask 077
  printf 'CODEGEIST_WINDOWS_PASSWORD=%q\n' "$CODEGEIST_WINDOWS_PASSWORD" > "$credentials_file"
}

xml_escape() {
  local value="$1"
  value="${value//&/&amp;}"
  value="${value//</&lt;}"
  value="${value//>/&gt;}"
  value="${value//\"/&quot;}"
  value="${value//\'/&apos;}"
  printf '%s' "$value"
}

render_template() {
  local template_file="$1"
  local output_file="$2"
  local content
  local password_xml
  local image_index_xml

  password_xml="$(xml_escape "$CODEGEIST_WINDOWS_PASSWORD")"
  image_index_xml="$(xml_escape "$image_index")"
  content="$(< "$template_file")"
  content="${content//__CODEGEIST_WINDOWS_PASSWORD__/$password_xml}"
  content="${content//__CODEGEIST_WINDOWS_IMAGE_INDEX__/$image_index_xml}"
  printf '%s\n' "$content" > "$output_file"
}

ps_single_quote() {
  local value="$1"
  value="${value//\'/\'\'}"
  printf "'%s'" "$value"
}

ensure_ssh_key() {
  mkdir -p "$vm_dir"
  if [ ! -f "$ssh_key" ]; then
    ssh-keygen -t ed25519 -N '' -f "$ssh_key" >/dev/null
  fi
}

build_answer_iso() {
  local staging="$vm_dir/answer"
  local config_file="$staging/setup-config.ps1"

  require_cmd genisoimage
  ensure_password
  ensure_ssh_key

  rm -rf "$staging"
  mkdir -p "$staging"

  render_template "$script_dir/windows-qemu/autounattend.xml" "$staging/Autounattend.xml"
  cp "$script_dir/windows-qemu/setup.ps1" "$staging/setup.ps1"
  cp "$ssh_key.pub" "$staging/authorized_keys"

  {
    printf '$CodegeistRepoDir = %s\n' "$(ps_single_quote "$repo_dir")"
    printf '$CodegeistReadyFile = %s\n' "$(ps_single_quote "$ready_file")"
    printf '$CodegeistGraalVmUrl = %s\n' "$(ps_single_quote "${CODEGEIST_WINDOWS_GRAALVM_URL:-https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-25.0.2/graalvm-community-jdk-25.0.2_windows-x64_bin.zip}")"
    printf '$CodegeistMavenUrl = %s\n' "$(ps_single_quote "${CODEGEIST_WINDOWS_MAVEN_URL:-https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip}")"
    printf '$CodegeistPowerShellUrl = %s\n' "$(ps_single_quote "${CODEGEIST_WINDOWS_POWERSHELL_URL:-https://github.com/PowerShell/PowerShell/releases/download/v7.6.2/PowerShell-7.6.2-win-x64.msi}")"
    printf '$CodegeistVsBuildToolsUrl = %s\n' "$(ps_single_quote "${CODEGEIST_WINDOWS_VS_BUILDTOOLS_URL:-https://aka.ms/vs/17/release/vs_BuildTools.exe}")"
  } > "$config_file"

  genisoimage -quiet -J -r -V "$answer_label" -o "$answer_iso" "$staging"
}

is_running() {
  if [ ! -f "$pid_file" ]; then
    return 1
  fi

  local pid
  pid="$(< "$pid_file")"
  [ -n "$pid" ] && kill -0 "$pid" >/dev/null 2>&1
}

qemu_args() {
  local accel='tcg'
  local cpu_model="${CODEGEIST_WINDOWS_CPU:-max}"

  if [ -e /dev/kvm ]; then
    accel='kvm'
    cpu_model="${CODEGEIST_WINDOWS_CPU:-host}"
  fi

  printf '%s\0' \
    -machine "q35,accel=$accel" \
    -cpu "$cpu_model" \
    -m "$memory" \
    -smp "$cpus" \
    -drive "file=$disk,format=qcow2,if=ide,index=0" \
    -netdev "user,id=n0,hostfwd=tcp:127.0.0.1:$ssh_port-:22" \
    -device e1000e,netdev=n0 \
    -display "$display" \
    -monitor "unix:$monitor_socket,server,nowait" \
    -pidfile "$pid_file" \
    -daemonize
}

start_qemu() {
  local -a args=()

  require_cmd qemu-system-x86_64
  rm -f "$monitor_socket"
  mapfile -d '' -t args < <(qemu_args)
  qemu-system-x86_64 "${args[@]}" "$@"
}

send_boot_key() {
  require_cmd nc

  local attempt
  for attempt in $(seq 1 20); do
    if [ -S "$monitor_socket" ]; then
      printf 'sendkey spc\n' | nc -U -N "$monitor_socket" >/dev/null 2>&1 || true
      return 0
    fi
    sleep 1
  done

  skip_or_fail "QEMU monitor socket did not become ready: $monitor_socket"
}

create_vm() {
  require_cmd qemu-img

  mkdir -p "$vm_dir"
  ensure_iso
  build_answer_iso

  if [ ! -f "$disk" ]; then
    qemu-img create -f qcow2 "$disk" "$disk_size"
  fi

  if is_running; then
    wait_ssh "$install_timeout_seconds"
    return 0
  fi

  start_qemu \
    -drive "file=$iso_path,media=cdrom,readonly=on,if=ide,index=1" \
    -drive "file=$answer_iso,media=cdrom,readonly=on,if=ide,index=2" \
    -boot order=d,menu=off

  send_boot_key

  wait_ssh "$install_timeout_seconds"
}

start_vm() {
  if [ ! -f "$disk" ]; then
    create_vm
    return 0
  fi

  if is_running; then
    wait_ssh 300
    return 0
  fi

  start_qemu
  wait_ssh 900
}

ssh_base() {
  printf '%s\0' \
    -i "$ssh_key" \
    -p "$ssh_port" \
    -o BatchMode=yes \
    -o StrictHostKeyChecking=accept-new \
    -o ConnectTimeout=5 \
    "$ssh_user@127.0.0.1"
}

windows_ssh() {
  local -a args=()

  mapfile -d '' -t args < <(ssh_base)
  ssh "${args[@]}" "$@"
}

wait_ssh() {
  local timeout_seconds="$1"
  local deadline=$((SECONDS + timeout_seconds))

  ensure_ssh_key
  printf 'Waiting for Windows SSH on localhost:%s...\n' "$ssh_port"

  while [ "$SECONDS" -lt "$deadline" ]; do
    if windows_ssh "powershell -NoProfile -Command \"if (Test-Path -LiteralPath '$ready_file') { exit 0 } else { exit 1 }\"" >/dev/null 2>&1; then
      printf 'Windows SSH is ready.\n'
      return 0
    fi
    sleep 10
  done

  skip_or_fail "Windows VM SSH/provisioning did not become ready within ${timeout_seconds}s"
}

start_host_ollama() {
  local start

  require_cmd task
  printf 'Command: OLLAMA_ENTER=false task ollama-start\n'
  start="$(codegeist_now_ms)"
  if ! (cd "$repo_root/app/codegeist/cli" && OLLAMA_ENTER=false task ollama-start); then
    skip_or_fail 'Host Ollama start failed'
  fi
  codegeist_print_duration 'windows host ollama start' "$start"
}

sync_repo() {
  require_cmd tar
  start_vm

  windows_ssh "powershell -NoProfile -Command \"Remove-Item -Recurse -Force '$repo_dir' -ErrorAction SilentlyContinue; New-Item -ItemType Directory -Force '$repo_dir' | Out-Null\""

  tar -C "$repo_root" \
    --exclude='app/codegeist/cli/target' \
    --exclude='app/codegeist/cli/logs' \
    --exclude='.git' \
    -cf - app/codegeist/cli scripts/tests \
    | windows_ssh "powershell -NoProfile -Command \"tar -xf - -C '$repo_dir'\""
}

smoke_vm() {
  local smoke_start

  smoke_start="$(codegeist_now_ms)"
  start_host_ollama
  sync_repo

  CODEGEIST_WINDOWS_SSH_TARGET="$ssh_user@127.0.0.1" \
  CODEGEIST_WINDOWS_SSH_PORT="$ssh_port" \
  CODEGEIST_WINDOWS_SSH_KEY="$ssh_key" \
  CODEGEIST_WINDOWS_REPO_DIR="$repo_dir" \
  CODEGEIST_WINDOWS_NATIVE_MODE="${CODEGEIST_WINDOWS_NATIVE_MODE:-required}" \
  CODEGEIST_WINDOWS_ALLOW_SKIP="$allow_skip" \
  CODEGEIST_SMOKE_STATUS_FILE="$status_file" \
    pwsh -NoProfile -File "$script_dir/qemu-windows-smoke.ps1"
  codegeist_print_duration 'windows qemu smoke total' "$smoke_start"
}

stop_vm() {
  if ! is_running; then
    printf 'Windows VM is not running.\n'
    return 0
  fi

  if windows_ssh 'powershell -NoProfile -Command "Stop-Computer -Force"' >/dev/null 2>&1; then
    printf 'Windows VM shutdown requested.\n'
    return 0
  fi

  printf 'Could not request Windows shutdown over SSH. VM is still running.\n' >&2
  return 1
}

status_vm() {
  if is_running; then
    printf 'Windows VM status: running\n'
  else
    printf 'Windows VM status: stopped\n'
  fi
  printf 'VM dir: %s\n' "$vm_dir"
  printf 'ISO path: %s\n' "$iso_path"
  printf 'ISO URL: %s\n' "$iso_url"
  printf 'SSH target: %s@127.0.0.1:%s\n' "$ssh_user" "$ssh_port"
}

case "$command" in
  download) ensure_iso ;;
  create) create_vm ;;
  start) start_vm ;;
  sync) sync_repo ;;
  smoke) smoke_vm ;;
  stop) stop_vm ;;
  status) status_vm ;;
  -h|--help|'') usage; exit 0 ;;
  *) usage >&2; exit 2 ;;
esac
