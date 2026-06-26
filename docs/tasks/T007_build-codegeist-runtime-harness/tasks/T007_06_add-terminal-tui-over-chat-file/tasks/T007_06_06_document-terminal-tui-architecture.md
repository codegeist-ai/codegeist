# T007_06_06 Document Terminal TUI Architecture

Parent: `T007_06_add-terminal-tui-over-chat-file/task.md`

Status: open

## Goal

Close the T007_06 implementation by documenting the actual terminal TUI architecture,
updating task state, refreshing memory, and running final verification.

## Dependencies

- Depends on `T007_06_05_add-jline-console-and-spring-command.md`.
- This is the final T007_06 child task.

## Scope

- Create focused current-state architecture documentation for the implemented TUI.
- Update the top-level architecture map with the new package and behavior.
- Update parent task status, child task statuses, verification notes, and project
  memory.
- Run focused TUI tests and the broad JVM suite.

## Documentation To Create Or Update

- Create `docs/developer/architecture/terminal-tui.md`.
- Update `docs/developer/architecture/architecture.md`.
- Update `docs/tasks/T007_build-codegeist-runtime-harness/tasks/T007_06_add-terminal-tui-over-chat-file/task.md`.
- Update this child task file with final verification.
- Update `docs/memory-bank/chat.md`.

## `terminal-tui.md` Required Content

- Current command entrypoint and user-facing behavior.
- Package/class map for `ai.codegeist.app.tui`.
- Runtime flow from `tui` command to `CodegeistTerminalTui`, `SessionStoreService`,
  view model, renderer, console, and `ChatHarnessService.ask(true, prompt)`.
- Persistence boundary: `.codegeist/session.json` remains the only chat store.
- Transient runtime status boundary.
- Tool activity rendering from final-only `ToolSessionPart` values.
- JLine adapter constraints and line-first fallback behavior.
- Tests that prove the behavior.
- Deferred work: full-screen rendering, streaming, cancellation, approval UI,
  prompt history, provider/model selectors, sidebars, autocomplete, and transcript
  export.

## Acceptance Criteria

- Architecture docs match implemented source, not planned source.
- Parent task status reflects completed implementation after verification passes.
- Memory bank points future sessions to the implemented architecture and remaining
  deferred work.
- Focused TUI tests pass.
- Broad `task test` passes.
- `git --no-pager diff --check` passes.

## Suggested Verification

Run from `app/codegeist/cli`:

```bash
task test TEST=TuiViewModelFactoryTest,RuntimeStatusFactoryTest,TuiLineRendererTest,ToolActivityRendererTest,TuiInputParserTest,CodegeistTerminalTuiTest
task test
```

Run from the repository root:

```bash
git --no-pager diff --check
```

## Non-Goals

- Do not add new runtime behavior in this documentation child unless verification
  exposes a small bug in the immediately implemented TUI path.
- Do not broaden T007_06 into full-screen parity or streaming behavior.
