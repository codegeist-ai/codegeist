#!/usr/bin/env bash
# mcp-remote-smoke.sh - Docker-backed remote MCP smoke entrypoint.
#
# Why this exists:
# - Proves Codegeist can connect to a remote MCP server through the
#   `streamable_http` client path without relying on hosted networks.
# - Runs both the direct MCP callback smoke and the ask/Ollama/MCP path that lets
#   a local model select the remote MCP tool.
# - Keeps Docker-only work outside `task test`; callers must opt in with
#   `task mcp-remote-smoke` from `app/codegeist/cli`.
#
# Inputs:
# - Docker must be available and able to build/run Linux containers.
# - Maven must be available on the host to package the fixture jar before building
#   the small runtime image.
# - `task ollama-start` must be able to start or reuse the local Ollama container
#   and make the default `llama3.2:1b` model available.
# - Optional `CODEGEIST_MCP_REMOTE_SMOKE_PORT`, default Docker-assigned host port.
#
# Side effects:
# - Builds `scripts/tests/fixtures/mcp-remote-server/target/codegeist-mcp-remote-smoke-server.jar`.
# - Builds a local Docker image named `codegeist-mcp-remote-smoke:local`.
# - Starts and then removes a temporary Docker container.
# - Starts or reuses the persistent local `codegeist-ollama` container.
#
# Related files:
# - app/codegeist/cli/Taskfile.yml
# - app/codegeist/cli/src/test/java/ai/codegeist/app/mcp/CodegeistMcpRemoteSmokeIT.java
# - app/codegeist/cli/src/test/java/ai/codegeist/app/provider/AskCommandsMcpRemoteSmokeIT.java
# - scripts/tests/fixtures/mcp-remote-server/

set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/../.." && pwd)"
cli_dir="$repo_root/app/codegeist/cli"
fixture_dir="$script_dir/fixtures/mcp-remote-server"
image_name="codegeist-mcp-remote-smoke:local"
container_name="codegeist-mcp-remote-smoke-$$"
requested_port="${CODEGEIST_MCP_REMOTE_SMOKE_PORT:-}"
host_port=""
smoke_start=""

source "$script_dir/native-smoke.sh"

cleanup() {
  if docker container inspect "$container_name" >/dev/null 2>&1; then
    docker rm -f "$container_name" >/dev/null 2>&1 || true
  fi
}

fail_smoke() {
  local reason="$1"

  printf 'MCP remote smoke status: failed\n'
  printf 'Reason: %s\n' "$reason"
  cleanup
  exit 1
}

trap cleanup EXIT

printf 'MCP remote smoke: streamable_http\n'
smoke_start="$(codegeist_now_ms)"

printf 'Command: mvn --batch-mode --no-transfer-progress -DskipTests package (remote MCP fixture)\n'
fixture_build_start="$(codegeist_now_ms)"
mvn --batch-mode --no-transfer-progress -DskipTests -f "$fixture_dir/pom.xml" package \
  || fail_smoke 'Remote MCP fixture package failed'
codegeist_print_duration 'mcp remote fixture package' "$fixture_build_start"

printf 'Command: docker build %s\n' "$image_name"
image_build_start="$(codegeist_now_ms)"
docker build -t "$image_name" "$fixture_dir" || fail_smoke 'Remote MCP fixture image build failed'
codegeist_print_duration 'mcp remote docker build' "$image_build_start"

if [ -n "$requested_port" ]; then
  docker_port_arg=(--publish "127.0.0.1:$requested_port:3000")
else
  docker_port_arg=(--publish "127.0.0.1::3000")
fi

printf 'Command: docker run %s\n' "$image_name"
container_start="$(codegeist_now_ms)"
docker run --detach --name "$container_name" "${docker_port_arg[@]}" "$image_name" >/dev/null \
  || fail_smoke 'Remote MCP fixture container failed to start'
codegeist_print_duration 'mcp remote container start' "$container_start"

port_mapping="$(docker port "$container_name" 3000/tcp)"
host_port="${port_mapping##*:}"
if [ -z "$host_port" ]; then
  fail_smoke 'Could not determine remote MCP fixture host port'
fi

for _ in $(seq 1 30); do
  if nc -z 127.0.0.1 "$host_port" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! nc -z 127.0.0.1 "$host_port" >/dev/null 2>&1; then
  docker logs --tail 80 "$container_name" >&2 || true
  fail_smoke "Remote MCP fixture did not open port $host_port"
fi

printf 'Command: mvn --batch-mode --no-transfer-progress -Dtest=CodegeistMcpRemoteSmokeIT test\n'
remote_test_start="$(codegeist_now_ms)"
mvn --batch-mode --no-transfer-progress \
  -Dtest=CodegeistMcpRemoteSmokeIT \
  -Dcodegeist.mcp.remote-smoke.url="http://127.0.0.1:$host_port" \
  -f "$cli_dir/pom.xml" test || fail_smoke 'Remote MCP streamable HTTP smoke test failed'
codegeist_print_duration 'mcp remote streamable_http test' "$remote_test_start"

printf 'Command: OLLAMA_ENTER=false task ollama-start\n'
ollama_start="$(codegeist_now_ms)"
(cd "$cli_dir" && OLLAMA_ENTER=false task ollama-start) || fail_smoke 'Local Ollama startup failed'
codegeist_print_duration 'mcp remote ollama start' "$ollama_start"

printf 'Command: CODEGEIST_TEST_PROVIDER_CATEGORY=local mvn --batch-mode --no-transfer-progress -Dtest=AskCommandsMcpRemoteSmokeIT test\n'
ask_test_start="$(codegeist_now_ms)"
CODEGEIST_TEST_PROVIDER_CATEGORY=local mvn --batch-mode --no-transfer-progress \
  -Dtest=AskCommandsMcpRemoteSmokeIT \
  -Dcodegeist.mcp.remote-smoke.url="http://127.0.0.1:$host_port" \
  -f "$cli_dir/pom.xml" test || fail_smoke 'Ask command Ollama MCP smoke test failed'
codegeist_print_duration 'mcp remote ask ollama test' "$ask_test_start"

printf 'MCP remote smoke status: passed\n'
codegeist_print_duration 'mcp remote smoke total' "$smoke_start"
