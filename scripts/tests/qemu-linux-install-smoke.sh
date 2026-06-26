#!/usr/bin/env bash
# qemu-linux-install-smoke.sh - smoke-test the Linux install script in QEMU.
#
# Why this exists:
# - Verifies the curl-downloadable Linux installer in a fresh Linux guest instead
#   of only testing it on the developer host.
# - Serves local release-shaped assets over a temporary host HTTP server so the
#   smoke can run before a real GitHub Release is published.
#
# Inputs:
# - Optional command: download|smoke|stop|status. Default command is smoke.
# - CODEGEIST_LINUX_QEMU_VM_DIR controls local VM state, default
#   `.local/linux-qemu`.
# - CODEGEIST_LINUX_QEMU_IMAGE_URL and CODEGEIST_LINUX_QEMU_CHECKSUM_URL override
#   the Ubuntu cloud image and checksum source.
# - CODEGEIST_LINUX_QEMU_ALLOW_SKIP=1 converts missing prerequisites into a
#   developer-only skipped status.
# - CODEGEIST_SMOKE_STATUS_FILE optionally receives a key-value status summary.
#
# Side effects:
# - Downloads and caches an Ubuntu cloud image under `.local/linux-qemu` by
#   default, creates a fresh qcow2 overlay for each smoke, starts QEMU with local
#   SSH forwarding, and writes temporary release assets under
#   app/codegeist/cli/target/smoke-test/qemu-linux-install-assets.
#
# Related files:
# - scripts/install/codegeist-install-linux.sh
# - app/codegeist/cli/Taskfile.yml
# - docs/developer/release/local-build-smoke.md

set -euo pipefail

command_name="${1:-smoke}"
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/../.." && pwd)"
cli_dir="$repo_root/app/codegeist/cli"
vm_dir="${CODEGEIST_LINUX_QEMU_VM_DIR:-$repo_root/.local/linux-qemu}"
download_dir="$vm_dir/downloads"
asset_dir="${CODEGEIST_LINUX_QEMU_ASSET_DIR:-$cli_dir/target/smoke-test/qemu-linux-install-assets}"
smoke_root="$cli_dir/target/smoke-test/qemu-linux-install"
image_url="${CODEGEIST_LINUX_QEMU_IMAGE_URL:-https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-amd64.img}"
checksum_url="${CODEGEIST_LINUX_QEMU_CHECKSUM_URL:-https://cloud-images.ubuntu.com/noble/current/SHA256SUMS}"
image_path="${CODEGEIST_LINUX_QEMU_IMAGE:-$download_dir/$(basename -- "$image_url")}"
run_disk="$vm_dir/linux-install-smoke.qcow2"
seed_iso="$vm_dir/seed.iso"
ssh_key="$vm_dir/id_ed25519"
known_hosts="$vm_dir/known_hosts"
pid_file="$vm_dir/qemu.pid"
serial_log="$vm_dir/serial.log"
ssh_port="${CODEGEIST_LINUX_QEMU_SSH_PORT:-2223}"
memory="${CODEGEIST_LINUX_QEMU_MEMORY:-2048}"
cpus="${CODEGEIST_LINUX_QEMU_CPUS:-2}"
disk_size="${CODEGEIST_LINUX_QEMU_DISK_SIZE:-10G}"
display="${CODEGEIST_LINUX_QEMU_DISPLAY:-none}"
ssh_user='codegeist'
allow_skip="${CODEGEIST_LINUX_QEMU_ALLOW_SKIP:-0}"
status_file="${CODEGEIST_SMOKE_STATUS_FILE:-}"
http_pid=''
http_port=''

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
  printf 'Usage: %s download|smoke|stop|status\n' "$(basename -- "$0")"
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
    printf 'platform=linux-x64\n'
    printf 'transport=qemu-ssh\n'
    printf 'install_status=%s\n' "$status"
    printf 'reason=%s\n' "$reason"
  } > "$status_file"
}

skip_or_fail() {
  local reason="$1"

  if [ "$allow_skip" = '1' ]; then
    write_status skipped "$reason"
    printf 'Platform smoke status: skipped\n'
    printf 'Platform: linux-x64\n'
    printf 'Transport: qemu-ssh\n'
    printf 'Install status: skipped\n'
    printf 'Reason: %s\n' "$reason"
    exit 0
  fi

  write_status failed "$reason"
  printf 'Platform smoke status: failed\n'
  printf 'Platform: linux-x64\n'
  printf 'Transport: qemu-ssh\n'
  printf 'Install status: failed\n'
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
    || { rm -f "$temp_file"; skip_or_fail "Failed to download $url"; }
  mv "$temp_file" "$output_file"
}

verify_image_checksum() {
  local checksum_file="$download_dir/SHA256SUMS"
  local image_name
  image_name="$(basename -- "$image_path")"

  require_cmd sha256sum

  if [ -n "${CODEGEIST_LINUX_QEMU_IMAGE_SHA256:-}" ]; then
    printf '%s  %s\n' "$CODEGEIST_LINUX_QEMU_IMAGE_SHA256" "$image_path" | sha256sum -c -
    return 0
  fi

  download_file "$checksum_url" "$checksum_file"
  if ! grep -E "[ *]$image_name$" "$checksum_file" > "$checksum_file.image"; then
    skip_or_fail "Could not find $image_name in $checksum_url"
  fi

  (cd "$(dirname -- "$image_path")" && sha256sum -c "$checksum_file.image")
}

ensure_image() {
  require_cmd curl

  if [ ! -f "$image_path" ]; then
    printf 'Linux cloud image not found at %s\n' "$image_path"
    printf 'Downloading Linux cloud image from %s\n' "$image_url"
    download_file "$image_url" "$image_path"
  fi

  verify_image_checksum
}

ensure_ssh_key() {
  mkdir -p "$vm_dir"
  if [ ! -f "$ssh_key" ]; then
    ssh-keygen -t ed25519 -N '' -f "$ssh_key" >/dev/null
  fi
}

write_cloud_init() {
  local user_data="$vm_dir/user-data.yaml"
  local meta_data="$vm_dir/meta-data.yaml"
  local public_key

  public_key="$(< "$ssh_key.pub")"
  cat > "$user_data" <<EOF
#cloud-config
users:
  - name: $ssh_user
    groups: sudo
    shell: /bin/bash
    sudo: ALL=(ALL) NOPASSWD:ALL
    ssh_authorized_keys:
      - $public_key
package_update: true
packages:
  - ca-certificates
  - curl
  - tar
runcmd:
  - [ cloud-init-per, once, codegeist-ready, touch, /var/tmp/codegeist-ready ]
EOF

  cat > "$meta_data" <<EOF
instance-id: codegeist-linux-install-smoke-$(date +%s)
local-hostname: codegeist-linux-smoke
EOF

  rm -f "$seed_iso"
  cloud-localds "$seed_iso" "$user_data" "$meta_data"
}

is_running() {
  if [ ! -f "$pid_file" ]; then
    return 1
  fi

  local pid
  pid="$(< "$pid_file")"
  [ -n "$pid" ] && kill -0 "$pid" >/dev/null 2>&1
}

qemu_accel() {
  if [ -e /dev/kvm ]; then
    printf 'kvm'
  else
    printf 'tcg'
  fi
}

qemu_cpu() {
  if [ -n "${CODEGEIST_LINUX_QEMU_CPU:-}" ]; then
    printf '%s' "$CODEGEIST_LINUX_QEMU_CPU"
  elif [ -e /dev/kvm ]; then
    printf 'host'
  else
    printf 'max'
  fi
}

start_vm() {
  require_cmd qemu-img
  require_cmd qemu-system-x86_64
  require_cmd cloud-localds
  require_cmd ssh-keygen
  require_cmd ssh

  stop_vm >/dev/null 2>&1 || true
  ensure_image
  ensure_ssh_key
  write_cloud_init

  rm -f "$run_disk" "$known_hosts" "$pid_file" "$serial_log"
  qemu-img create -f qcow2 -F qcow2 -b "$image_path" "$run_disk" "$disk_size" >/dev/null

  qemu-system-x86_64 \
    -machine "q35,accel=$(qemu_accel)" \
    -cpu "$(qemu_cpu)" \
    -m "$memory" \
    -smp "$cpus" \
    -drive "file=$run_disk,format=qcow2,if=virtio" \
    -drive "file=$seed_iso,format=raw,media=cdrom,readonly=on,if=ide,index=1" \
    -netdev "user,id=n0,hostfwd=tcp:127.0.0.1:$ssh_port-:22" \
    -device virtio-net-pci,netdev=n0 \
    -display "$display" \
    -serial "file:$serial_log" \
    -pidfile "$pid_file" \
    -daemonize
}

ssh_base() {
  printf '%s\0' \
    -i "$ssh_key" \
    -p "$ssh_port" \
    -o BatchMode=yes \
    -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile="$known_hosts" \
    -o ConnectTimeout=5 \
    "$ssh_user@127.0.0.1"
}

linux_ssh() {
  local -a args=()

  mapfile -d '' -t args < <(ssh_base)
  ssh "${args[@]}" "$@"
}

wait_ssh() {
  local timeout_seconds="${CODEGEIST_LINUX_QEMU_BOOT_TIMEOUT_SECONDS:-600}"
  local deadline=$((SECONDS + timeout_seconds))

  printf 'Waiting for Linux SSH on localhost:%s...\n' "$ssh_port"
  while [ "$SECONDS" -lt "$deadline" ]; do
    if linux_ssh 'test -f /var/tmp/codegeist-ready && cloud-init status --wait >/dev/null 2>&1' >/dev/null 2>&1; then
      printf 'Linux SSH is ready.\n'
      return 0
    fi
    sleep 5
  done

  if [ -f "$serial_log" ]; then
    printf 'Linux VM serial log tail:\n' >&2
    tail -n 80 "$serial_log" >&2 || true
  fi
  skip_or_fail "Linux VM SSH/cloud-init did not become ready within ${timeout_seconds}s"
}

get_expected_version() {
  local build_info="$cli_dir/target/classes/META-INF/build-info.properties"

  if [ -n "${CODEGEIST_EXPECTED_VERSION:-}" ]; then
    printf '%s' "$CODEGEIST_EXPECTED_VERSION"
    return 0
  fi

  if [ -f "$build_info" ]; then
    sed -n 's/^build.version=//p' "$build_info" | head -n 1
  fi
}

stage_linux_archive() {
  local dist_dir="$cli_dir/target/dist"
  local archive="$dist_dir/codegeist-linux-x64.tar.gz"
  local package_dir="$dist_dir/codegeist-linux-x64"
  local native="$cli_dir/target/codegeist"

  if [ -f "$archive" ]; then
    return 0
  fi

  [ -x "$native" ] || skip_or_fail "Linux native executable not found at $native; run task native-smoke or task native first"
  mkdir -p "$dist_dir"
  rm -rf "$package_dir" "$archive"
  mkdir -p "$package_dir"
  cp -p "$native" "$package_dir/codegeist"
  shopt -s nullglob
  local sidecars=("$cli_dir"/target/lib*.so)
  if [ "${#sidecars[@]}" -gt 0 ]; then
    cp -p "${sidecars[@]}" "$package_dir/"
  fi
  shopt -u nullglob
  tar -C "$dist_dir" -czf "$archive" codegeist-linux-x64
}

stage_assets() {
  require_cmd sha256sum
  require_cmd tar

  local archive="$cli_dir/target/dist/codegeist-linux-x64.tar.gz"
  local install_script="$repo_root/scripts/install/codegeist-install-linux.sh"

  stage_linux_archive
  [ -f "$archive" ] || skip_or_fail "Linux archive not found: $archive"
  [ -f "$install_script" ] || skip_or_fail "Linux install script not found: $install_script"

  rm -rf "$asset_dir"
  mkdir -p "$asset_dir"
  cp -p "$archive" "$asset_dir/codegeist-linux-x64.tar.gz"
  cp -p "$install_script" "$asset_dir/codegeist-install-linux.sh"
  (cd "$asset_dir" && sha256sum codegeist-linux-x64.tar.gz codegeist-install-linux.sh > SHA256SUMS.txt)
}

pick_http_port() {
  python3 - <<'PY'
import socket
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
    sock.bind(('127.0.0.1', 0))
    print(sock.getsockname()[1])
PY
}

start_asset_server() {
  require_cmd python3
  require_cmd curl

  http_port="${CODEGEIST_LINUX_QEMU_ASSET_PORT:-$(pick_http_port)}"
  local http_log="$smoke_root/asset-server.log"
  mkdir -p "$smoke_root"

  python3 -m http.server "$http_port" --bind 127.0.0.1 --directory "$asset_dir" > "$http_log" 2>&1 &
  http_pid="$!"

  for _ in $(seq 1 30); do
    if curl -fsS "http://127.0.0.1:$http_port/SHA256SUMS.txt" >/dev/null 2>&1; then
      printf 'Serving install assets at http://127.0.0.1:%s/\n' "$http_port"
      return 0
    fi
    sleep 1
  done

  skip_or_fail "Asset HTTP server did not become ready; see $http_log"
}

stop_asset_server() {
  if [ -n "$http_pid" ] && kill -0 "$http_pid" >/dev/null 2>&1; then
    kill "$http_pid" >/dev/null 2>&1 || true
    wait "$http_pid" >/dev/null 2>&1 || true
  fi
}

run_guest_install_smoke() {
  local guest_script="$vm_dir/guest-install-smoke.sh"
  local expected_version

  expected_version="$(get_expected_version)"
  cat > "$guest_script" <<EOF
#!/usr/bin/env bash
set -euo pipefail

base_url='http://10.0.2.2:$http_port'
expected_version='$expected_version'
export CODEGEIST_INSTALL_BASE_URL="\$base_url"
export CODEGEIST_INSTALL_DIR="\$HOME/.local/share/codegeist"
export CODEGEIST_BIN_DIR="\$HOME/.local/bin"
export PATH="\$CODEGEIST_BIN_DIR:\$PATH"

rm -rf "\$HOME/codegeist-install-smoke"
mkdir -p "\$HOME/codegeist-install-smoke/work"
cd "\$HOME/codegeist-install-smoke"
curl -fsSL -o codegeist-install-linux.sh "\$base_url/codegeist-install-linux.sh"
chmod +x codegeist-install-linux.sh
./codegeist-install-linux.sh

cd "\$HOME/codegeist-install-smoke/work"
version="\$(codegeist --version)"
if [ -n "\$expected_version" ] && [ "\$version" != "\$expected_version" ]; then
  printf 'Expected codegeist --version to print %s but got %s\n' "\$expected_version" "\$version" >&2
  exit 1
fi
if [ -z "\$version" ]; then
  printf 'codegeist --version printed no output\n' >&2
  exit 1
fi

config="\$(codegeist --show-config)"
if [ "\$config" != '{}' ]; then
  printf 'Expected codegeist --show-config to print {} but got %s\n' "\$config" >&2
  exit 1
fi

printf 'Guest install smoke passed: version=%s config=%s\n' "\$version" "\$config"
EOF

  printf 'Command: curl install script inside Linux QEMU guest\n'
  linux_ssh 'bash -s' < "$guest_script"
}

smoke_vm() {
  local smoke_start
  smoke_start="$(codegeist_now_ms)"

  require_cmd tail
  stage_assets
  start_asset_server
  trap 'stop_asset_server; stop_vm >/dev/null 2>&1 || true' EXIT
  start_vm
  wait_ssh
  run_guest_install_smoke

  write_status passed none
  printf 'Platform smoke status: passed\n'
  printf 'Platform: linux-x64\n'
  printf 'Transport: qemu-ssh\n'
  printf 'Install status: passed\n'
  codegeist_print_duration 'linux qemu install smoke total' "$smoke_start"
}

stop_vm() {
  if ! is_running; then
    return 0
  fi

  linux_ssh 'sudo poweroff' >/dev/null 2>&1 || true
  for _ in $(seq 1 30); do
    if ! is_running; then
      rm -f "$pid_file"
      return 0
    fi
    sleep 1
  done

  local pid
  pid="$(< "$pid_file")"
  kill "$pid" >/dev/null 2>&1 || true
  rm -f "$pid_file"
}

status_vm() {
  if is_running; then
    printf 'Linux VM status: running\n'
  else
    printf 'Linux VM status: stopped\n'
  fi
  printf 'VM dir: %s\n' "$vm_dir"
  printf 'Image path: %s\n' "$image_path"
  printf 'Image URL: %s\n' "$image_url"
  printf 'SSH target: %s@127.0.0.1:%s\n' "$ssh_user" "$ssh_port"
}

case "$command_name" in
  download) ensure_image ;;
  smoke) smoke_vm ;;
  stop) stop_vm ;;
  status) status_vm ;;
  -h|--help|'') usage; exit 0 ;;
  *) usage >&2; exit 2 ;;
esac
