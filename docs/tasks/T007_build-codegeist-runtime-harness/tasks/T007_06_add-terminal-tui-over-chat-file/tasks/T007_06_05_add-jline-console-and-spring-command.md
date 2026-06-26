# T007_06_05 Add JLine Console And Spring Command

Parent: `T007_06_add-terminal-tui-over-chat-file/task.md`

Status: open

## Goal

Wire the tested TUI controller into production terminal I/O and expose it through a
Spring Shell command. This is the first child that should touch JLine-specific code.

## Dependencies

- Depends on `T007_06_04_implement-terminal-tui-controller-loop.md`.
- Must be completed before `T007_06_06_document-terminal-tui-architecture.md`.

## Scope

- Add the production JLine-backed `TuiConsole` implementation.
- Add a factory that creates the JLine console.
- Add the Spring Shell `tui` command.
- Keep the command adapter thin and keep terminal details out of Spring command
  methods.
- Verify existing noninteractive command paths remain usable.

## Classes To Create

Production package `ai.codegeist.app.tui`:

- `JLineTuiConsole`
- `JLineTuiConsoleFactory`
- `TuiCommands`

## JLine Requirements

- Use `TerminalBuilder.builder().system(true).build()` to create the terminal.
- Use `LineReaderBuilder.builder().terminal(terminal).build()` to create the reader.
- Use `LineReader.readLine(...)` for prompt input.
- Map `UserInterruptException` to `TuiInputKind.INTERRUPT`.
- Map `EndOfFileException` to `TuiInputKind.EOF`.
- Map `TuiLineStyle` to JLine `AttributedStyle` only inside this adapter.
- If `Status` works safely with the current Spring Shell runtime, keep it minimal;
  the tested line output remains the core contract.

## Spring Shell Requirements

- Add `TuiCommands` as a Spring `@Component`.
- Register command name `tui` with `@Command`.
- Use `CodegeistCommandExceptionMapper.BEAN_NAME` for command boundary exceptions.
- Delegate to `CodegeistTerminalTui.run()`.
- Do not write directly to `System.out` or `System.err` in the command method.

## Dependency Rule

- First try to compile with the current Spring Shell dependency graph.
- Add an explicit JLine dependency only if the needed classes are not available at
  compile time.
- If an explicit dependency is needed, keep it narrow and document why in the task
  result.

## Acceptance Criteria

- The `tui` command delegates to `CodegeistTerminalTui`.
- JLine console maps Ctrl-C and Ctrl-D into existing input kinds.
- Styled output is adapter-local; core renderers remain ANSI-free.
- Existing `--version`, `--show-config`, and `ask` command tests still pass.
- No prompt history, prompt stash, sidebars, full-screen renderer, or extra UI
  persistence is added.

## Suggested Tests

- Add a focused Spring command wiring test only if needed.
- Re-run existing command-focused tests that could be affected by Spring Shell
  command registration.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=TuiInputParserTest,CodegeistTerminalTuiTest,VersionCommandsTests,CodegeistConfigCommandTest,AskCommandsSessionStoreTest
task test
```

## Non-Goals

- Do not implement full-screen redraw.
- Do not implement mouse support.
- Do not implement modal approval prompts.
- Do not implement provider/model selectors.
- Do not implement streaming token updates or active cancellation.
