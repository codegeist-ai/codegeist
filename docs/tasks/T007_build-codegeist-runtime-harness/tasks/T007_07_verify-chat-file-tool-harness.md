# T007_07 Verify Native TUI Hello World Tool Harness

Parent: `T007_build-codegeist-runtime-harness`

Status: solved

## Goal

Verify the native Codegeist TUI path with one small, video-recordable local coding
task:

```text
Create a shell script named hello-world.sh that prints Hello World using echo, then
run it with sh hello-world.sh.
```

This task is intentionally smaller than a complete application-development demo. It
exists to prove that the native `codegeist tui` command can accept a prompt through
the real Spring Shell `TerminalUI`, let a deterministic Ollama-compatible fixture
provider select tools, create a file, run a shell command, show the tool preview and
final assistant response, and persist tool activity in `.codegeist/session.json`.

The capture should also be useful as reproducible evidence for a later
AI-generated video. This task must not create a storyboard, voiceover, YouTube
script, subtitle file, thumbnail prompt, or other video-production content.

## Current Outcome

The native TUI hello-world smoke is implemented and verified. The deterministic
fixture returns ordered `codegeist_write` and `codegeist_shell` calls in one model
turn; Codegeist executes them in order, continues the model turn, persists both
completed tool parts, and projects their bounded previews before the final assistant
response in the TUI.

The VHS capture now waits for the visible write and shell labels, created file,
shell command, exit code, `Hello World` output, and final assistant response. The
post-run checks parse the session store structurally, rerun the generated script with
an exact stdout/stderr contract, and replace the committed README GIF only after the
behavioral assertions pass.

## Implemented Entry Point

The smoke entrypoint is:

```bash
task tui-hello-world-smoke
```

Run it from `app/codegeist/cli`. The task builds the native executable and then
runs `scripts/tests/tui-hello-world-smoke.ps1 -BuildNative`.

The script drives the native app through Charmbracelet VHS and writes ignored build
artifacts under:

```text
app/codegeist/cli/target/smoke-test/tui-hello-world/
```

Expected artifacts include:

- `drive-tui-hello-world.tape` - generated VHS script.
- `tui-hello-world.mp4` - MP4 terminal recording.
- `tui-hello-world.webm` - WebM terminal recording.
- `tui-hello-world.gif` - generated GIF staged before the README asset is replaced.
- `vhs-output.log` - VHS stdout/stderr.
- `workspace/hello-world.sh` - file created by the TUI-guided tool run.
- `session/session.json` - persisted Codegeist session store.
- `run-summary.md` - compact run evidence for later demo review.

## Scope

- Use the native executable, not the JVM `java -jar` path.
- Drive the real `codegeist tui` command through VHS.
- Use a deterministic localhost Ollama-compatible fixture provider so tool selection
  stays reproducible.
- Prompt the model to use `codegeist_write` to create `hello-world.sh`.
- Prompt the model to use `codegeist_shell` to run `sh hello-world.sh`.
- Assert the workspace side effects after the TUI recording finishes.
- Assert the session store contains completed `codegeist_write` and
  `codegeist_shell` `ToolSessionPart` entries.
- Wait for visible `Exit code: 0` shell output in the recorded TUI transcript.
- Keep generated recordings and workspace/session evidence under ignored
  `target/smoke-test/` output.

## Acceptance Criteria

- `task tui-hello-world-smoke` builds or uses the native executable and records the
  TUI run through VHS.
- The recorded TUI prompt asks Codegeist to create `hello-world.sh` with `echo` and
  run it with `sh hello-world.sh`.
- `workspace/hello-world.sh` exists after the run.
- `sh hello-world.sh` prints exactly `Hello World` with exit code 0 when run from
  the smoke workspace.
- `session/session.json` is written under the smoke artifact directory through the
  smoke-specific session-directory override.
- The session store contains the submitted TUI prompt.
- The session store contains a completed `ToolSessionPart` for `codegeist_write`.
- The session store contains a completed `ToolSessionPart` for `codegeist_shell`
  whose preview includes `Hello World`.
- The recorded TUI transcript shows the completed shell command, exit code, and
  `Hello World` output instead of only the final assistant message.
- The session store does not persist provider config, selected provider/model,
  enabled tool definitions, or runtime status.
- The smoke writes MP4/WebM recording artifacts that can be used later as raw demo
  evidence.

## Non-Goals

- Do not implement a complete app-development scenario in this task.
- Do not include MCP in this smoke. `task mcp-remote-smoke` remains the separate MCP
  verification path.
- Do not add hosted-provider calls.
- Do not add a new TUI architecture, tool timeline, streaming UI, permission UI,
  session browser, or persisted TUI state.
- Do not generate storyboard, voiceover, subtitles, thumbnail prompts, or YouTube
  script files.

## Verification

Primary command from `app/codegeist/cli`:

```bash
task tui-hello-world-smoke
```

Useful focused JVM regression checks after script or TUI changes:

```bash
task test TEST=CodegeistTerminalUiTest,TuiCommandsTest,ChatHarnessServiceTest,CodegeistAgentLoopServiceTest,CodegeistLocalToolsTest,SessionStoreServiceTest
```

The broader suite remains:

```bash
task test
```

## Verification Results

Verification completed on 2026-07-18 from `app/codegeist/cli`:

- The focused selector passed with 71 tests, 0 failures, 0 errors, and 0 skipped.
- `task tui-hello-world-smoke` passed against a newly compiled native executable.
  Native compile took `113.575s`; native recording took `15.626s`; the smoke total
  after compilation took `16.410s`.
- The generated MP4 and WebM both decode as `4.24s` videos. Final-frame review shows
  the ordered write and shell previews, `Exit code: 0`, `Hello World`, and the final
  assistant response.
- The TUI documentation gate passed through `task cli:docs`; native compile took
  `114.700s`, native capture took `6.586s`, and capture smoke total took `7.013s`.
- The full `task test` suite passed with 191 tests, 0 failures, 0 errors, and 6
  skipped provider-gated tests.
