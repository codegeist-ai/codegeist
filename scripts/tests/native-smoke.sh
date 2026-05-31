#!/usr/bin/env bash
# native-smoke.sh - native executable smoke-test helper.
#
# Why this exists:
# - Keeps native archive smoke-test assertions reusable from Taskfile.yml, local
#   Linux smoke runs, and the final local smoke suite.
# - Packages the Linux native executable with its sidecar libraries, unpacks the
#   archive into a fresh temp directory, and proves package command output stays
#   clean for `./codegeist --version` and `./codegeist --show-config`.
#
# Inputs:
# - `app/codegeist/cli/target/codegeist`, built by the native Maven profile, plus
#   any `app/codegeist/cli/target/lib*.so` sidecar libraries.
# - `app/codegeist/cli/target/classes/META-INF/build-info.properties`, generated
#   by Maven build-info.
# - Optional `CODEGEIST_NATIVE_SMOKE_TIMEOUT`, default `5s`.
# - The `timeout` command must be available on `PATH`.
#
# Side effects:
# - Writes `app/codegeist/cli/target/dist/codegeist-linux-x64.tar.gz`.
# - Unpacks the archive into a temporary directory and executes the packaged
#   `./codegeist --version` and `./codegeist --show-config`.
#
# Related files:
# - app/codegeist/cli/Taskfile.yml
# - app/codegeist/cli/src/main/resources/logback.xml
# - scripts/tests/local-linux-smoke.sh

set -euo pipefail

codegeist_read_build_version() {
  local build_info_file="$1"
  local version=""

  while IFS='=' read -r key value; do
    if [ "$key" = 'build.version' ]; then
      version="$value"
      break
    fi
  done < "$build_info_file"

  if [ -z "$version" ]; then
    printf 'build.version not found in %s\n' "$build_info_file" >&2
    return 1
  fi

  printf '%s' "$version"
}

package-linux-native-archive() {
  local cli_dir="$1"
  local package_name="codegeist-linux-x64"
  local dist_dir="$cli_dir/target/dist"
  local package_dir="$dist_dir/$package_name"
  local archive="$dist_dir/$package_name.tar.gz"
  local sidecars=()

  if [ ! -x "$cli_dir/target/codegeist" ]; then
    printf 'Native executable is missing or not executable: %s\n' "$cli_dir/target/codegeist" >&2
    return 1
  fi

  rm -rf "$package_dir" "$archive"
  mkdir -p "$package_dir"

  cp -p "$cli_dir/target/codegeist" "$package_dir/codegeist"

  shopt -s nullglob
  sidecars=("$cli_dir"/target/lib*.so)
  shopt -u nullglob

  if [ "${#sidecars[@]}" -gt 0 ]; then
    cp -p "${sidecars[@]}" "$package_dir/"
  fi

  tar -C "$dist_dir" -czf "$archive" "$package_name"
  printf '%s\n' "$archive"
}

run-native-smoke-tests() {
  local script_dir
  local repo_root
  local cli_dir
  local smoke_dir
  local package_name
  local archive
  local temp_dir
  local package_dir
  local expected
  local expected_config
  local actual
  local timeout_budget

  script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
  repo_root="$(cd -- "$script_dir/../.." && pwd)"
  cli_dir="$repo_root/app/codegeist/cli"
  smoke_dir="$cli_dir/target/smoke-test"

  cd "$cli_dir"

  expected="$(codegeist_read_build_version target/classes/META-INF/build-info.properties)"
  # Keep aligned with CodegeistConfigService and docs/developer/architecture/provider-configuration.md.
  expected_config='provider: {}'
  package_name="codegeist-linux-x64"

  rm -rf "$smoke_dir"
  mkdir -p "$smoke_dir"

  archive="$(package-linux-native-archive "$cli_dir")"
  temp_dir="$(mktemp -d)"
  package_dir="$temp_dir/$package_name"
  timeout_budget="${CODEGEIST_NATIVE_SMOKE_TIMEOUT:-5s}"

  if ! tar -xzf "$archive" -C "$temp_dir"; then
    rm -rf "$temp_dir"
    printf 'Failed to unpack native archive: %s\n' "$archive" >&2
    return 1
  fi

  if [ ! -x "$package_dir/codegeist" ]; then
    rm -rf "$temp_dir"
    printf 'Packaged native executable is missing or not executable: %s\n' "$package_dir/codegeist" >&2
    return 1
  fi

  if ! actual="$(cd "$package_dir" && LOG_FILE="$smoke_dir/codegeist.log" timeout "$timeout_budget" ./codegeist --version 2>&1)"; then
    rm -rf "$temp_dir"
    printf 'Native version smoke failed or timed out after %s: %s\n' "$timeout_budget" "$actual" >&2
    return 1
  fi

  if [ "$actual" != "$expected" ]; then
    rm -rf "$temp_dir"
    printf 'Expected version %s, got %s\n' "$expected" "$actual" >&2
    return 1
  fi

  if ! actual="$(cd "$package_dir" && LOG_FILE="$smoke_dir/codegeist.log" timeout "$timeout_budget" ./codegeist --show-config 2>&1)"; then
    rm -rf "$temp_dir"
    printf 'Native show-config smoke failed or timed out after %s: %s\n' "$timeout_budget" "$actual" >&2
    return 1
  fi

  if [ "$actual" != "$expected_config" ]; then
    rm -rf "$temp_dir"
    printf 'Expected show-config output %s, got %s\n' "$expected_config" "$actual" >&2
    return 1
  fi

  if [ ! -s "$smoke_dir/codegeist.log" ]; then
    rm -rf "$temp_dir"
    printf 'Expected non-empty native smoke log: %s\n' "$smoke_dir/codegeist.log" >&2
    return 1
  fi

  rm -rf "$temp_dir"
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  run-native-smoke-tests "$@"
fi
