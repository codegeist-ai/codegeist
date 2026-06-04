#!/usr/bin/env bash
# local-linux-smoke.sh - local Linux jar and native smoke entrypoint.
#
# Why this exists:
# - Gives developers and release operators one local Linux command that runs the
#   Maven tests, builds the executable jar, verifies `--version`, and optionally
#   packages and verifies GraalVM native archive command output when
#   `native-image` is available.
# - Emits explicit passed/skipped/failed status lines so the final smoke suite can
#   include Linux without hiding missing native prerequisites.
#
# Inputs:
# - Run from anywhere inside the repository checkout.
# - Optional `CODEGEIST_SMOKE_STATUS_FILE` writes a key-value status summary.
# - Optional `CODEGEIST_SMOKE_REQUIRE_NATIVE=1` turns missing native-image into a
#   failure instead of a skipped native subcheck.
# - Optional `CODEGEIST_JAR_SMOKE_TIMEOUT`, default `15s`.
# - Optional `CODEGEIST_ASK_SMOKE_TIMEOUT`, default `60s`.
# - The `timeout` command must be available on `PATH`.
#
# Side effects:
# - Rebuilds `app/codegeist/cli/target/codegeist.jar`.
# - May rebuild `app/codegeist/cli/target/codegeist` and write
#   `app/codegeist/cli/target/dist/codegeist-linux-x64.tar.gz` when
#   native-image exists.
# - Writes smoke logs under `app/codegeist/cli/target/smoke-test`.
#
# Related files:
# - scripts/tests/native-smoke.sh
# - scripts/tests/final-smoke-suite.sh
# - app/codegeist/cli/Taskfile.yml

set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/../.." && pwd)"
cli_dir="$repo_root/app/codegeist/cli"
status_file="${CODEGEIST_SMOKE_STATUS_FILE:-}"
require_native="${CODEGEIST_SMOKE_REQUIRE_NATIVE:-0}"
jar_timeout="${CODEGEIST_JAR_SMOKE_TIMEOUT:-15s}"
ask_timeout="${CODEGEIST_ASK_SMOKE_TIMEOUT:-60s}"
ask_prompt='codegeist'
platform_start=""

source "$script_dir/native-smoke.sh"

write_status() {
  local status="$1"
  local jar_status="$2"
  local native_status="$3"
  local native_reason="$4"

  if [ -z "$status_file" ]; then
    return 0
  fi

  mkdir -p "$(dirname -- "$status_file")"
  {
    printf 'status=%s\n' "$status"
    printf 'platform=linux\n'
    printf 'jar_status=%s\n' "$jar_status"
    printf 'native_status=%s\n' "$native_status"
    printf 'native_reason=%s\n' "$native_reason"
  } > "$status_file"
}

fail_smoke() {
  local reason="$1"

  write_status failed failed failed "$reason"
  printf 'Platform smoke status: failed\n'
  printf 'Platform: linux\n'
  printf 'Reason: %s\n' "$reason"
  exit 1
}

cd "$cli_dir"
platform_start="$(codegeist_now_ms)"

printf 'Platform: linux\n'
printf 'Artifact: jar\n'
printf 'Command: mvn --batch-mode --no-transfer-progress test\n'
maven_tests_start="$(codegeist_now_ms)"
mvn --batch-mode --no-transfer-progress test || fail_smoke 'Maven tests failed'
codegeist_print_duration 'linux maven tests' "$maven_tests_start"

printf 'Command: mvn --batch-mode --no-transfer-progress -DskipTests clean package\n'
jar_package_start="$(codegeist_now_ms)"
mvn --batch-mode --no-transfer-progress -DskipTests clean package || fail_smoke 'Jar package failed'
codegeist_print_duration 'linux jar package' "$jar_package_start"

expected="$(codegeist_read_build_version target/classes/META-INF/build-info.properties)"
smoke_dir="$cli_dir/target/smoke-test"
mkdir -p "$smoke_dir"
ask_config="$smoke_dir/codegeist-ask.yml"

printf 'Command: java -jar target/codegeist.jar --version\n'
jar_smoke_start="$(codegeist_now_ms)"
actual="$(LOG_FILE="$smoke_dir/codegeist-linux-jar.log" timeout "$jar_timeout" java -jar target/codegeist.jar --version 2>&1)" \
  || fail_smoke "Jar version smoke failed or timed out after $jar_timeout"
codegeist_print_duration 'linux jar version smoke' "$jar_smoke_start"

if [ "$actual" != "$expected" ]; then
  fail_smoke "Jar version smoke expected $expected but got $actual"
fi

if [ ! -s "$smoke_dir/codegeist-linux-jar.log" ]; then
  fail_smoke "Jar smoke log was not written: $smoke_dir/codegeist-linux-jar.log"
fi

printf 'Command: OLLAMA_ENTER=false task ollama-start\n'
ollama_start="$(codegeist_now_ms)"
OLLAMA_ENTER=false task ollama-start || fail_smoke 'Ollama start failed'
codegeist_print_duration 'linux ollama start' "$ollama_start"

cat > "$ask_config" <<EOF
provider:
  ollama:
    type: ollama
    base-url: http://localhost:11434
EOF

printf 'Command: java -Dcodegeist.config=%s -jar target/codegeist.jar ask <prompt>\n' "$ask_config"
jar_ask_start="$(codegeist_now_ms)"
actual="$(LOG_FILE="$smoke_dir/codegeist-linux-jar-ask.log" timeout "$ask_timeout" \
  java -Dcodegeist.config="$ask_config" -jar target/codegeist.jar ask "$ask_prompt" 2>&1)" \
  || fail_smoke "Jar ask smoke failed or timed out after $ask_timeout"
codegeist_print_duration 'linux jar ask smoke' "$jar_ask_start"

case "${actual,,}" in
  *codegeist*) ;;
  *) fail_smoke "Jar ask smoke expected response to contain codegeist but got $actual" ;;
esac

if [ ! -s "$smoke_dir/codegeist-linux-jar-ask.log" ]; then
  fail_smoke "Jar ask smoke log was not written: $smoke_dir/codegeist-linux-jar-ask.log"
fi

native_status='skipped'
native_reason='native-image is not available on PATH'

if command -v native-image >/dev/null 2>&1; then
  printf 'Artifact: native\n'
  printf 'Command: mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile\n'
  native_compile_start="$(codegeist_now_ms)"
  mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile \
    || fail_smoke 'Native compile failed'
  codegeist_print_duration 'linux native compile' "$native_compile_start"

  printf 'Command: package target/dist/codegeist-linux-x64.tar.gz and run extracted ./codegeist --version plus --show-config\n'
  run-native-smoke-tests || fail_smoke 'Native archive smoke failed'
  native_status='passed'
  native_reason='none'
elif [ "$require_native" = '1' ]; then
  fail_smoke 'native-image is required but not available on PATH'
else
  printf 'Native status: skipped\n'
  printf 'Reason: %s\n' "$native_reason"
fi

write_status passed passed "$native_status" "$native_reason"
printf 'Platform smoke status: passed\n'
printf 'Platform: linux\n'
printf 'Jar status: passed\n'
printf 'Native status: %s\n' "$native_status"
printf 'Native reason: %s\n' "$native_reason"
codegeist_print_duration 'linux platform smoke total' "$platform_start"
