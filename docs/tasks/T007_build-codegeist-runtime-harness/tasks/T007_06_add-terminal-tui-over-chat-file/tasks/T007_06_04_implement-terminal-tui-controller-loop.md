# T007_06_04 Implement Terminal TUI Controller Loop

Parent: `T007_06_add-terminal-tui-over-chat-file/task.md`

Status: open

## Goal

Implement the testable line-first TUI controller that opens the session store,
renders the view model, reads parsed inputs, and submits prompts through the existing
chat harness. This child still uses the `TuiConsole` seam and does not add the real
JLine adapter or Spring Shell command.

## Dependencies

- Depends on `T007_06_03_add-tui-input-parser-and-console-seam.md`.
- Must be completed before `T007_06_05_add-jline-console-and-spring-command.md`.

## Scope

- Add `CodegeistTerminalTui` as the main controller.
- Use `SessionStoreService.loadCurrentStoreForContinue()` on startup.
- Build and render the initial view model through `TuiViewModelFactory` and
  `TuiLineRenderer`.
- Read `TuiInput` from `TuiConsole` in a loop.
- Send only `PROMPT` inputs to `ChatHarnessService.ask(true, prompt)`.
- Reload the store and re-render after successful prompt completion.
- Render `/help`, `/status`, `/history`, blank-input hints, unknown-command warnings,
  input interrupts, and EOF/quit behavior deterministically.

## Classes To Create

Production package `ai.codegeist.app.tui`:

- `CodegeistTerminalTui`

## Controller Dependencies

- `SessionStoreService`
- `ChatHarnessService`
- `RuntimeStatusFactory`
- `TuiViewModelFactory`
- `TuiLineRenderer`
- `HelpRenderer`
- `TuiInputParser`
- A console provider or factory seam that can be backed by a fake console in tests
  and by JLine in `T007_06_05`.

## Behavioral Requirements

- Startup must open or create `.codegeist/session.json` through
  `SessionStoreService`.
- A normal prompt must call `ChatHarnessService.ask(true, prompt)` exactly once.
- Runtime `busy=true` may be rendered before the blocking chat call, but it must not
  be persisted.
- After the chat call, reload the current store and render the updated transcript.
- `/help`, `/status`, and `/history` must not call the model.
- `UNKNOWN_COMMAND` must not call the model.
- `EMPTY` must not call the model.
- `INTERRUPT` while reading input keeps the TUI alive in the first slice.
- `QUIT` and `EOF` exit cleanly.
- Corrupt session JSON should surface through existing session-store exception
  behavior and must not be overwritten.

## Acceptance Criteria

- A focused controller test proves startup renders initial store content.
- A focused controller test proves prompt submission calls
  `ChatHarnessService.ask(true, prompt)`, reloads, and renders again.
- Focused controller tests prove help, status, history, blank input, unknown slash
  command, interrupt, quit, and EOF behavior.
- No production JLine adapter or Spring Shell command is added in this child.

## Suggested Tests

- `CodegeistTerminalTuiTest`

Candidate command from `app/codegeist/cli`:

```bash
task test TEST=CodegeistTerminalTuiTest
```

## Non-Goals

- Do not implement provider/tool cancellation during an active blocking chat call.
- Do not implement live streaming event output.
- Do not add JLine terminal implementation yet.
- Do not add Spring Shell command registration yet.
- Do not add full-screen redraw, scroll panes, prompt history, approval UI, or
  provider/model selectors.
