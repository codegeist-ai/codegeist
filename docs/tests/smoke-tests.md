# Smoke Test Contract

Smoke-test expectations for Codegeist local and platform checks.

## Scope

Smoke tests prove packaged artifacts work in their target environment. They are
not a replacement for focused unit or Spring tests.

Current smoke entrypoints:

- `task native-smoke` - build Linux native executable, package it, start host
  Ollama through `task ollama-start`, and smoke the extracted native archive.
- `task local-linux-smoke` - run JVM tests, build the jar, smoke the jar including
  real Ollama-backed `ask`, and run native checks when required or available.
- `task mcp-remote-smoke` - build a local Docker MCP server fixture, start it on a
  localhost-only port, and verify Codegeist's real `streamable_http` MCP callback
  path directly and through `ask` with local Ollama.
- `task qemu-windows-smoke` - sync the repo into the Windows QEMU VM, run Windows
  JVM tests, build the jar, smoke the jar including real Ollama-backed `ask`, build
  Windows native, package it, and smoke the extracted native archive. The host
  wrapper starts Ollama through `task ollama-start`; the guest reaches it at
  `http://10.0.2.2:11434` by default.
- `task final-smoke-suite` - run Linux and Windows platform smokes and require
  both to pass by default.

## Output Contract

Smoke output must include scan-friendly status lines:

```text
Platform smoke status: passed|failed|skipped
Platform: linux|windows-x64
Jar status: passed|failed|skipped
Native status: passed|failed|skipped
Native reason: none|<reason>
MCP remote smoke status: passed|failed
```

Smoke output must also include duration lines for every meaningful subcheck. Use
the shape:

```text
Duration: <label>: <seconds>s
```

Labels should be stable and specific, for example:

- `linux maven tests`
- `linux jar package`
- `linux jar version smoke`
- `linux ollama start`
- `linux jar ask smoke`
- `linux native compile`
- `linux native archive smoke`
- `linux native version smoke`
- `linux native show-config smoke`
- `linux native ask smoke`
- `linux platform smoke total`
- `mcp remote fixture package`
- `mcp remote docker build`
- `mcp remote container start`
- `mcp remote streamable_http test`
- `mcp remote ollama start`
- `mcp remote ask ollama test`
- `mcp remote smoke total`
- `windows maven tests`
- `windows jar package`
- `windows ollama reachability`
- `windows jar version smoke`
- `windows jar ask smoke`
- `windows native compile`
- `windows native archive smoke`
- `windows native version smoke`
- `windows native show-config smoke`
- `windows native ask smoke`
- `windows host ollama start`
- `windows platform smoke total`

## Timing Rules

- Measure command execution time, not only build-tool-reported time.
- Print duration even when a subcheck fails whenever the script can do so without
  hiding the failure.
- Keep duration output human-readable with seconds and three decimal places when
  practical.
- Keep startup checks separate from compile/package time. For example, native
  `--version` startup duration must not be buried inside native compile duration.
- Do not add interactive prompts to capture timing.

## Artifacts And Logs

- Linux native smoke packages `target/dist/codegeist-linux-x64.tar.gz` and tests
  the extracted `./codegeist` binary, including `ask` with a generated smoke config.
- Windows native smoke packages `target/dist/codegeist-windows-x64.zip` and tests
  the extracted `codegeist.exe` binary, including `ask` with a generated smoke config.
- Linux and Windows native archive smokes both check `--show-config`; empty direct
  `codegeist.yml` config must render exactly `{}`.
- MCP remote smoke packages the fixture jar under
  `scripts/tests/fixtures/mcp-remote-server/target/`, builds the local Docker image
  `codegeist-mcp-remote-smoke:local`, starts a temporary container, starts or reuses
  local Ollama, verifies the direct remote callback path, verifies the `ask` command
  can make Ollama invoke `remote_echo`, and removes the fixture container on exit.
- Smoke logs stay under `app/codegeist/cli/target/smoke-test`.
- Generated smoke artifacts remain ignored build output unless a task explicitly
  asks for a handoff snapshot.
