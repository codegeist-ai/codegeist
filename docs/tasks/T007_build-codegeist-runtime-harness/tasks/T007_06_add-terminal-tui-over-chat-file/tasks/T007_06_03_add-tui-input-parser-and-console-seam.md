# T007_06_03 Add TUI Input Parser And Console Seam

Parent: `T007_06_add-terminal-tui-over-chat-file/task.md`

Status: open

## Goal

Add the minimal input model and terminal abstraction needed to test the TUI loop
without constructing a real JLine terminal. This child separates UI slash commands
from provider prompts.

## Dependencies

- Depends on `T007_06_02_add-deterministic-line-renderers.md`.
- Must be completed before `T007_06_04_implement-terminal-tui-controller-loop.md`.

## Scope

- Add a small `TuiConsole` interface for scripted test input and captured output.
- Add parsed input records and an input parser for the first TUI commands.
- Keep the console seam independent of JLine until `T007_06_05`.
- Ensure unknown slash commands are not sent to the provider.

## Classes To Create

Production package `ai.codegeist.app.tui`:

- `TuiConsole`
- `TuiInput`
- `TuiInputKind`
- `TuiInputParser`

## Input Contract

| Raw input | Parsed kind | Behavior in later controller |
| --- | --- | --- |
| blank or whitespace | `EMPTY` | Render hint, do not call model. |
| `/help` or `?` | `HELP` | Render help. |
| `/status` | `STATUS` | Render runtime status. |
| `/history` | `HISTORY` | Re-render current session transcript. |
| `/quit` or `/exit` | `QUIT` | Exit cleanly. |
| unknown `/...` | `UNKNOWN_COMMAND` | Render warning, do not call model. |
| normal text | `PROMPT` | Submit to `ChatHarnessService.ask(true, prompt)` later. |
| console interrupt | `INTERRUPT` | Clear/abort current input in first slice. |
| console EOF | `EOF` | Exit cleanly. |

## Behavioral Requirements

- `TuiInputParser` should be deterministic and side-effect free.
- Prompt text may be trimmed in this first slice. Exact whitespace preservation is
  deferred until multiline/paste support has a focused contract.
- `TuiConsole` should have enough methods for `CodegeistTerminalTuiTest` to script
  inputs and capture `TuiLine` output.
- No production JLine code should be added in this child.

## Acceptance Criteria

- A focused parser test covers all input kinds in the table.
- Unknown slash commands are classified separately from normal prompts.
- Test fakes can implement `TuiConsole` without depending on JLine.

## Suggested Tests

- `TuiInputParserTest`

Candidate command from `app/codegeist/cli`:

```bash
task test TEST=TuiInputParserTest
```

## Non-Goals

- Do not add `CodegeistTerminalTui` yet.
- Do not add JLine console implementation yet.
- Do not add multiline editing, file autocomplete, command palette, prompt history,
  prompt stash, mouse support, or modal input.
