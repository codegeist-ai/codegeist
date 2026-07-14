# Smoke Test Contract

Smoke-test expectations for Codegeist local and platform checks.

## Scope

Smoke tests prove packaged artifacts work in their target environment. They are
not a replacement for focused unit or Spring tests.

Current smoke entrypoints:

- `task native-smoke` - build Linux native executable, package it, and smoke the
  extracted native archive, including deterministic ask-driven file-edit encoding
  checks and shell-tool checks.
- `task tui-capture-smoke` - build the native executable, drive `codegeist tui`
  through VHS with a deterministic fixture provider, verify persisted session text,
  and generate local PNG preview artifacts under `target/`.
- `task tui-hello-world-smoke` - build the native executable, start a deterministic
  Ollama-compatible fixture provider, drive `codegeist tui` through VHS, record
  MP4/WebM output, ask the TUI to create and run `hello-world.sh`, and verify the
  resulting workspace, visible shell transcript output, and session store tool
  activity.
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
`scripts/tests/tui-capture-smoke.ps1` is the native TUI documentation-capture
smoke. It uses Charmbracelet VHS to drive the real native TUI through a terminal
renderer, capture PNG screenshots, and write a manifest for local documentation
previews. VHS requires `vhs`, `ffmpeg`, and `ttyd` on `PATH`; the shared
`.devcontainer` release kit provides those tools after a rebuild.
`scripts/tests/tui-hello-world-smoke.ps1` is the native TUI hello-world video
smoke. It uses VHS to record MP4/WebM output from a real native TUI session while a
deterministic Ollama-compatible fixture provider selects `codegeist_write` and
`codegeist_shell`, then derives the README GIF preview from the recorded MP4. The
script verifies the created `hello-world.sh`, reruns `sh hello-world.sh`, waits for
visible `Exit code: 0` output in the recorded TUI, and checks completed
`codegeist_write` plus `codegeist_shell` `ToolSessionPart` entries.

## Creating The TUI Hello World Video

Use the smoke entrypoint when you need to create or refresh the native TUI demo
video. Run it from `app/codegeist/cli`:

```bash
task tui-hello-world-smoke
```

The task builds the native executable, starts a localhost-only Ollama-compatible
fixture provider, writes a temporary `codegeist.yml`, generates a VHS tape, drives
the real `codegeist tui` command, creates the primary MP4 video artifact, writes a
secondary WebM variant, regenerates the README GIF preview from the MP4, and then
verifies the workspace plus session-store side effects. The recorded prompt asks
Codegeist to create `hello-world.sh` with `echo`, run `sh hello-world.sh`, and
display the shell result in the TUI transcript. A passing run has already waited
for visible `Exit code: 0` output before VHS stops recording.

Generated artifacts are ignored build output under:

```text
app/codegeist/cli/target/smoke-test/tui-hello-world/
```

Important files in that directory:

- `tui-hello-world.mp4` - primary video artifact.
- `tui-hello-world.webm` - browser-friendly video artifact.
- `gif-output.log` - FFmpeg palette and GIF conversion log.
- `drive-tui-hello-world.tape` - generated VHS script for the recorded run.
- `vhs-output.log` - VHS command trace and render log.
- `workspace/hello-world.sh` - script created by the recorded TUI session.
- `session/session.json` - persisted prompt, tool parts, and assistant response.
- `run-summary.md` - compact run evidence for local review.

To watch the latest MP4 locally:

```bash
xdg-open app/codegeist/cli/target/smoke-test/tui-hello-world/tui-hello-world.mp4
```

To watch the WebM variant:

```bash
xdg-open app/codegeist/cli/target/smoke-test/tui-hello-world/tui-hello-world.webm
```

Do not treat `drive-tui-hello-world.tape` as a standalone durable source file. The
tape is generated for the current smoke run and depends on the temporary
localhost fixture URL written into the generated `codegeist.yml`. After the smoke
stops, that fixture provider is gone. To refresh the video reliably, rerun
`task tui-hello-world-smoke` so the fixture provider, config, tape, recordings,
workspace, and session evidence are recreated together.

For quick script iteration after a native binary already exists, run the script
without rebuilding from `app/codegeist/cli`:

```bash
pwsh -NoProfile -File ../../../scripts/tests/tui-hello-world-smoke.ps1
```

Use `task tui-hello-world-smoke` again before handing off a video, because that path
proves the current source builds into the native executable used for the recording.

The flow also refreshes the repo-owned README preview GIF:

```text
docs/user/assets/tui/tui-hello-world.gif
```

GitHub README rendering does not support inline HTML `<video>` playback for this
repository asset path; GitHub's Markdown renderer strips the tag. Use that GIF as
the inline README preview. The MP4 stays useful as a local smoke artifact, but the
README intentionally does not link to it.

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
TUI capture smoke status: passed|failed
TUI hello-world smoke status: passed|failed
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
- `tui capture native compile`
- `tui capture native run`
- `tui capture artifact generation`
- `tui capture smoke total`
- `tui hello-world native compile`
- `tui hello-world fixture start`
- `tui hello-world native recording`
- `tui hello-world gif generation`
- `tui hello-world assertions`
- `tui hello-world smoke total`
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
- TUI capture smoke runs through `scripts/tests/tui-capture-smoke.ps1`. The script
  starts a deterministic Ollama-compatible fixture provider, writes a temporary
  direct `codegeist.yml`, generates a `drive-tui.tape`, drives the native
  `codegeist tui` command with VHS, submits one prompt, waits for the fixture
  response, uses an invisible blank-space prompt key to advance TerminalUI's
  asynchronous repaint before the response screenshot, exits with `Ctrl-Q`, verifies
  the session store contains the prompt and response, and generates local preview artifacts under
  `app/codegeist/cli/target/smoke-test/tui-capture/`. Expected artifacts are
  `drive-tui.tape`, `01-initial.png`, `02-prompt.png`, `03-response.png`,
  `vhs-output.log`, and `manifest.md`. These artifacts are ignored build output;
  selected screenshots promoted for the TUI user guide live under
  `docs/user/assets/tui/`.
- TUI hello-world smoke runs through `scripts/tests/tui-hello-world-smoke.ps1`. The
  script builds or uses the native executable, starts a deterministic
  Ollama-compatible fixture provider, writes a temporary direct `codegeist.yml`,
  generates a VHS tape, records the native `codegeist tui` session as
  `tui-hello-world.mp4` and `tui-hello-world.webm`, regenerates
  `docs/user/assets/tui/tui-hello-world.gif` from the MP4, asks Codegeist to create
  `hello-world.sh` with `echo` and run `sh hello-world.sh`, waits for visible
  `Exit code: 0` output in the transcript, then verifies the script output and
  persisted tool parts.
  Expected artifacts live under
  `app/codegeist/cli/target/smoke-test/tui-hello-world/` and include
  `drive-tui-hello-world.tape`, `tui-hello-world.mp4`, `tui-hello-world.webm`,
  `vhs-output.log`, `gif-output.log`, `workspace/`, `session/session.json`, and
  `run-summary.md`. These artifacts are ignored build output and are intended as raw
  reproducible evidence for later demo or video-generation work, not as a storyboard
  or narration script.
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
