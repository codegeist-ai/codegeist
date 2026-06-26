#!/usr/bin/env bash
# codegeist-install-linux.sh - install the Linux x64 Codegeist release archive.
#
# Why this exists:
# - Gives users a small curl-downloadable bootstrap script for GitHub Release
#   assets without adding package-manager publishing or OS-native installers.
# - Installs the complete native archive directory so GraalVM sidecar libraries
#   remain beside the executable while the user runs a stable `codegeist` command.
#
# Inputs:
# - CODEGEIST_INSTALL_BASE_URL overrides the release asset base URL. Defaults to
#   the public GitHub Releases latest/download URL.
# - CODEGEIST_INSTALL_DIR overrides the user-local install root.
# - CODEGEIST_BIN_DIR overrides the directory where the `codegeist` wrapper is
#   written.
#
# Side effects:
# - Downloads codegeist-linux-x64.tar.gz and SHA256SUMS.txt into a temporary
#   directory, verifies the archive checksum, installs under CODEGEIST_INSTALL_DIR,
#   and writes CODEGEIST_BIN_DIR/codegeist.
#
# Related files:
# - scripts/install/codegeist-install-macos.sh
# - scripts/tests/qemu-linux-install-smoke.sh
# - .github/workflows/release.yml

set -euo pipefail

readonly asset_base_url_default='https://github.com/codegeist-ai/codegeist/releases/latest/download'
readonly asset_base_url="${CODEGEIST_INSTALL_BASE_URL:-$asset_base_url_default}"
readonly archive_name='codegeist-linux-x64.tar.gz'
readonly package_name='codegeist-linux-x64'
readonly checksum_name='SHA256SUMS.txt'
readonly install_root="${CODEGEIST_INSTALL_DIR:-${XDG_DATA_HOME:-$HOME/.local/share}/codegeist}"
readonly bin_dir="${CODEGEIST_BIN_DIR:-$HOME/.local/bin}"
readonly current_dir="$install_root/current"

tmp_dir=''

cleanup() {
  if [ -n "$tmp_dir" ]; then
    rm -rf "$tmp_dir"
  fi
}
trap cleanup EXIT

fail() {
  printf 'Codegeist install failed: %s\n' "$1" >&2
  exit 1
}

require_cmd() {
  local name="$1"

  command -v "$name" >/dev/null 2>&1 || fail "$name is required on PATH"
}

download_asset() {
  local name="$1"
  local output_file="$2"
  local url

  url="${asset_base_url%/}/$name"
  printf 'Downloading %s\n' "$url"
  curl --fail --location --silent --show-error --retry 3 --output "$output_file" "$url"
}

write_checksum_line() {
  local checksum_file="$1"
  local output_file="$2"

  if ! grep -E "[ *]$archive_name$" "$checksum_file" > "$output_file"; then
    fail "$checksum_name does not contain an entry for $archive_name"
  fi
}

verify_archive() {
  local checksum_line_file="$1"

  if command -v sha256sum >/dev/null 2>&1; then
    (cd "$tmp_dir" && sha256sum -c "$checksum_line_file")
    return 0
  fi

  if command -v shasum >/dev/null 2>&1; then
    (cd "$tmp_dir" && shasum -a 256 -c "$checksum_line_file")
    return 0
  fi

  fail 'sha256sum or shasum is required on PATH'
}

shell_quote() {
  local value="$1"
  printf "'%s'" "${value//\'/\'\\\'\'}"
}

write_wrapper() {
  local wrapper="$1"
  local executable="$2"
  local executable_dir

  executable_dir="$(dirname -- "$executable")"
  cat > "$wrapper" <<EOF
#!/usr/bin/env sh
CODEGEIST_HOME=$(shell_quote "$executable_dir")
LD_LIBRARY_PATH="\$CODEGEIST_HOME\${LD_LIBRARY_PATH:+:\$LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH
exec "\$CODEGEIST_HOME/codegeist" "\$@"
EOF
  chmod +x "$wrapper"
}

main() {
  if [ "$(uname -s)" != 'Linux' ]; then
    fail 'this installer only supports Linux'
  fi

  require_cmd curl
  require_cmd grep
  require_cmd tar
  require_cmd chmod
  require_cmd dirname

  tmp_dir="$(mktemp -d)"
  local archive_file="$tmp_dir/$archive_name"
  local checksum_file="$tmp_dir/$checksum_name"
  local checksum_line_file="$tmp_dir/$archive_name.sha256"
  local extract_dir="$tmp_dir/extract"
  local extracted_package="$extract_dir/$package_name"
  local release_dir="$install_root/releases/$package_name"
  local next_release_dir="$release_dir.next.$$"
  local current_tmp="$install_root/current.tmp.$$"
  local wrapper="$bin_dir/codegeist"

  download_asset "$checksum_name" "$checksum_file"
  download_asset "$archive_name" "$archive_file"
  write_checksum_line "$checksum_file" "$checksum_line_file"
  verify_archive "$(basename -- "$checksum_line_file")"

  mkdir -p "$extract_dir"
  tar -xzf "$archive_file" -C "$extract_dir"
  [ -d "$extracted_package" ] || fail "archive did not contain $package_name"
  [ -x "$extracted_package/codegeist" ] || fail 'archive did not contain an executable codegeist binary'

  mkdir -p "$install_root/releases" "$bin_dir"
  rm -rf "$next_release_dir"
  mkdir -p "$next_release_dir"
  cp -R "$extracted_package/." "$next_release_dir/"
  chmod +x "$next_release_dir/codegeist"

  rm -rf "$release_dir"
  mv "$next_release_dir" "$release_dir"
  rm -f "$current_tmp"
  ln -s "$release_dir" "$current_tmp"
  rm -rf "$current_dir"
  mv "$current_tmp" "$current_dir"

  write_wrapper "$wrapper" "$current_dir/codegeist"

  printf 'Installed Codegeist to %s\n' "$release_dir"
  printf 'Installed command wrapper to %s\n' "$wrapper"
  "$wrapper" --version >/dev/null
  printf 'Codegeist install smoke passed: %s --version\n' "$wrapper"

  case ":$PATH:" in
    *":$bin_dir:"*) ;;
    *) printf 'Add this directory to PATH if needed: %s\n' "$bin_dir" ;;
  esac
}

main "$@"
