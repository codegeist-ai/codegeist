# Smoke Test Contract

Smoke-test expectations for Codegeist local and platform checks.

## Scope

Smoke tests prove packaged artifacts work in their target environment. They are
not a replacement for focused unit or Spring tests.

Current smoke entrypoints:

- `task native-smoke` - build Linux native executable, package it, and smoke the
  extracted native archive, including deterministic ask-driven file-edit encoding
  checks and shell-tool checks.
- `task local-linux-smoke` - run JVM tests, build the jar as a build gate, and run
  native checks when required or available. The jar is not smoke-tested.
- `task mcp-remote-smoke` - build a local Docker MCP server fixture, start it on a
  localhost-only port, and verify Codegeist's real `streamable_http` MCP callback
  path directly and through `ask` with local Ollama.
- `task qemu-windows-smoke` - sync the repo into the Windows QEMU VM, build Windows
  native, package it, smoke the extracted native archive, and run
  `codegeist-install-windows.ps1` against local release-shaped assets.
- `task qemu-linux-install-smoke` - build the Linux native executable, boot a fresh
  Ubuntu Linux QEMU guest, serve local release-shaped assets from the host,
  download the Linux install script with guest `curl`, install the Linux archive,
  and verify the installed `codegeist` command inside the guest. This smoke is
  opt-in and is not part of
  `final-smoke-suite` by default.
- `task final-smoke-suite` - run Linux and Windows platform smokes and require
  both to pass by default.

`scripts/tests/artifact-smoke.ps1` is the shared native package smoke contract.
Platform wrappers build artifacts, prepare VM prerequisites when needed, then call
this one native-only harness so Linux, Windows, macOS, and release CI use the same
`--version`, `--show-config`, file-edit, shell-tool, package, unpack, and log
assertions.
`scripts/tests/install-script-smoke.ps1` is the release-runner install smoke
harness. The GitHub native matrix calls it after archive packaging so each Linux,
Windows, and macOS install script runs against local release-shaped assets on its
matching runner.
`scripts/tests/file-edit-ask-smoke.ps1` remains the focused sub-harness used by
`artifact-smoke.ps1` for deterministic ask-driven native file-edit side effects.
`scripts/tests/shell-ask-smoke.ps1` is the focused sub-harness for deterministic
ask-driven native shell-tool side effects through `codegeist_shell`.

## Output Contract

Smoke output must include scan-friendly status lines:

```text
Platform smoke status: passed|failed|skipped
Platform: linux|linux-x64|windows-x64|macos-x64
Jar status: passed|failed|skipped
Native status: passed|failed|skipped
Install status: passed|failed|skipped
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
- `linux native compile`
- `linux native archive smoke`
- `linux native version smoke`
- `linux native show-config smoke`
- `linux native file-edit ask utf8-lf smoke`
- `linux native file-edit ask utf8-bom-crlf smoke`
- `linux native file-edit ask no-final-newline smoke`
- `linux native file-edit ask latin1-crlf smoke`
- `linux native shell ask smoke`
- `linux platform smoke total`
- `linux qemu install smoke total`
- `<platform> install script run`
- `<platform> install command version smoke`
- `<platform> install command show-config smoke`
- `<platform> install script smoke`
- `mcp remote fixture package`
- `mcp remote docker build`
- `mcp remote container start`
- `mcp remote streamable_http test`
- `mcp remote ollama start`
- `mcp remote ask ollama test`
- `mcp remote smoke total`
- `windows native compile`
- `windows native archive smoke`
- `windows native version smoke`
- `windows native show-config smoke`
- `windows native file-edit ask utf8-lf smoke`
- `windows native file-edit ask utf8-bom-crlf smoke`
- `windows native file-edit ask no-final-newline smoke`
- `windows native file-edit ask latin1-crlf smoke`
- `windows native shell ask smoke`
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

- Native archive smokes run through `scripts/tests/artifact-smoke.ps1`. The harness
  packages native artifacts as `target/dist/codegeist-<platform>.<extension>`,
  unpacks native archives into a temporary directory, verifies `--version`, verifies
  native `--show-config`, and checks non-empty command logs.
- File-edit artifact smokes are invoked by `artifact-smoke.ps1` through
  `scripts/tests/file-edit-ask-smoke.ps1`. The sub-harness starts a deterministic
  local Ollama-compatible fixture provider, runs the artifact's real `ask` command,
  and verifies final file bytes plus persisted completed
  `ToolSessionPart(tool=codegeist_edit)`. Each native artifact path checks UTF-8 LF,
  UTF-8 BOM plus CRLF and multibyte text, no-final-newline preservation, and
  ISO-8859-1 `workspace.encoding` with CRLF.
- Shell artifact smokes are invoked by `artifact-smoke.ps1` through
  `scripts/tests/shell-ask-smoke.ps1`. The sub-harness starts the same style of
  deterministic Ollama-compatible fixture provider, makes the artifact's real `ask`
  command receive a `codegeist_shell` tool call, configures `pwsh -NoProfile
  -NonInteractive -Command` as the cross-platform shell wrapper, then verifies the
  created workspace file and a completed persisted
  `ToolSessionPart(tool=codegeist_shell)` whose preview includes `Exit code: 0`.
- Local Linux and Windows platform smokes do not run a provider-only native ask
  check. The native ask coverage stays on deterministic fixture-backed file-edit and
  shell harnesses so smoke results do not depend on local model wording when no tool
  is needed.
- GitHub release native jobs run `scripts/tests/install-script-smoke.ps1` after
  `artifact-smoke.ps1`. The Windows QEMU smoke uses the same harness inside the
  guest after creating `codegeist-windows-x64.zip`. The harness stages the matching
  install script beside the native archive, writes a local `SHA256SUMS.txt`, serves
  those assets over localhost, runs the installer in an isolated install root, and
  verifies installed `codegeist --version` plus `codegeist --show-config`.
- Linux, Windows, and macOS native archive smokes all check `--show-config`; empty
  direct `codegeist.yml` config must render exactly `{}`.
- MCP remote smoke packages the fixture jar under
  `scripts/tests/fixtures/mcp-remote-server/target/`, builds the local Docker image
  `codegeist-mcp-remote-smoke:local`, starts a temporary container, starts or reuses
  local Ollama, verifies the direct remote callback path, verifies the `ask` command
  can make Ollama invoke `remote_echo`, and removes the fixture container on exit.
- Smoke logs stay under `app/codegeist/cli/target/smoke-test`.
- Linux QEMU install-smoke assets are staged under
  `app/codegeist/cli/target/smoke-test/qemu-linux-install-assets` and served by a
  temporary host HTTP server only for the duration of the smoke.
- Generated smoke artifacts remain ignored build output unless a task explicitly
  asks for a handoff snapshot.
