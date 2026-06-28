# T007_06 Add TerminalUI Chat Harness

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Connect the existing `codegeist tui` Spring Shell command to the current chat
harness through Spring Shell `TerminalUI`, keeping the TUI as a small UI adapter
over `ChatHarnessService` instead of a separate JLine console, renderer pipeline,
or agent loop.

The first useful slice should let a user type a prompt in the TerminalUI, submit it
through `ChatHarnessService.ask(true, prompt)`, and see the response in the same
TerminalUI surface. The existing harness remains responsible for provider
selection, tool/MCP callback setup, the Codegeist-owned model/tool/model loop, and
session-store persistence.

## Current Baseline

- `app/codegeist/cli` already has a minimal Spring Shell `tui` command in
  `ai.codegeist.app.TuiCommands`.
- `CodegeistTerminalUi` already builds a Spring Shell `TerminalUI` through
  `TerminalUIBuilder` and shows a static `ListView` titled `Codegeist`.
- `spring-shell-jline` is present because Spring Shell `TerminalUI` lives there;
  Codegeist should not add a separate direct JLine console layer for this task.
- `CodegeistShellRunnerConfiguration` keeps noninteractive command-argument
  dispatch stable while `spring.shell.interactive.enabled=false` is configured.
- `ChatHarnessService.ask(boolean continueSession, String prompt)` is the runtime
  submission boundary for both commands and this TUI.

## Scope

- Keep the runtime path as
  `TuiCommands -> CodegeistTerminalUi -> ChatHarnessService.ask(true, prompt)`.
- Use Spring Shell `TerminalUI` components such as `GridView`, `InputView`,
  `ListView`, `ButtonView`, `TerminalUI.redraw()`, and `TerminalUI.interrupt()`
  when they fit.
- Add the smallest testable UI state or helper needed to submit one prompt and show
  the latest response without running the real `TerminalUI.run()` loop in unit
  tests.
- Keep prompt submission synchronous for this slice. A provider or tool call may
  block the UI until `ChatHarnessService.ask(...)` returns.
- Keep `.codegeist/session.json` writes owned by `ChatHarnessService` and
  `SessionStoreService`. The TUI may display returned text and later reload stored
  state, but it must not create a second persistence path.
- Preserve existing `--version`, `--show-config`, and `ask` command behavior.

## Acceptance Criteria

- `codegeist tui` still starts a Spring Shell `TerminalUI`.
- A focused test proves submitting a non-blank prompt delegates to
  `ChatHarnessService.ask(true, prompt)`.
- A focused test proves the returned `CodegeistChatResponse.content()` is added to
  the TerminalUI-visible state.
- Blank prompt handling is deterministic and does not call the harness.
- TUI code does not call `CodegeistAgentLoopService` directly and does not duplicate
  provider, tool, MCP, or session-store orchestration.
- TUI code does not persist provider config, selected provider/model, MCP client
  definitions, tool definitions, prompt drafts, layout, scroll, or other UI-only
  state into `.codegeist/session.json`.
- Existing noninteractive command tests for `--version`, `--show-config`, and `ask`
  continue to pass.
- Architecture and memory docs describe the implemented TerminalUI behavior after
  the runtime change lands.

## Non-Goals

- Do not implement a custom JLine `TerminalBuilder`, `LineReader`, status line,
  console seam, deterministic line renderer pipeline, or full-screen renderer
  abstraction in this task.
- Do not revive the removed `T007_06` child-task sequence for view models,
  renderers, input parsers, controller loops, or JLine console wiring.
- Do not implement streaming model events, cancellation tokens, background provider
  calls, permission prompts, patch review UI, scrollback navigation, prompt history,
  autocomplete, mouse support, server runtime, Vaadin, API/SDK, plugins, subagents,
  skills, or memory.
- Do not make the TUI a second agent runtime. `ChatHarnessService` remains the
  only integration point for provider/tool/session behavior in this slice.

## Verification

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=TuiCommandsTest
task test TEST=VersionCommandsTests,CodegeistConfigCommandTest,AskCommandsSessionStoreTest
task run -- --version
```

For the interactive entrypoint, use a bounded smoke check because `TerminalUI.run()`
is expected to keep running until interrupted:

```bash
timeout 15s task run -- tui
```

Do not document direct `mvn test` commands for this implementation task.
