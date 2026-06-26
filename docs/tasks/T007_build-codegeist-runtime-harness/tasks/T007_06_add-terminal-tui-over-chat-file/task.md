# T007_06 Add Terminal TUI Over Session Store

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add a terminal TUI that uses `.codegeist/session.json` as its state source and is
usable for the same local coding-agent loop that current agent sessions need.

The TUI should be the first minimum usable local coding-agent interface while
preserving the single persistence contract: open the directory-local session store,
render it, submit prompts, show tool activity, review file and shell effects, and
save the same file.

## Dependencies

- Depends on `T007_05_add-agent-control-loop/task.md` so prompt submission can drive the
  Codegeist-owned model/tool/model loop instead of only a one-turn provider call.

## Scope

- Use `docs/tasks/T007_build-codegeist-runtime-harness/tui-opencode-jline-mapping.md`
  as the source-backed element inventory and JLine mapping for this implementation.
- Use `third-party-tui-deep-analysis.md` in this task directory for the current
  source-backed comparison of OpenCode, Pi, Aider, mini-SWE-agent, and Spring AI
  Agent Utils terminal UI patterns before adding UI dependencies or renderer
  abstractions.
- Use `implementation-plan.md` in this task directory as the detailed class,
  package, phase, test, and documentation handoff for implementation.
- Add the smallest terminal UI approach that supports the required coding-agent
  elements and still fits the existing Spring Shell/JLine stack. Add a new UI
  dependency only if focused tests or smoke checks prove JLine is not enough.
- Open or create `.codegeist/session.json` from the TUI entrypoint.
- Render chat messages, assistant responses, MCP/read/write tool activity,
  patch/edit summaries, and shell summaries from the session store.
- Submit prompts through the same chat services used by `ask -c/--continue`.
- Save updates back to the same session store.
- Keep line-oriented fallback behavior available when full-screen behavior is not
  practical in automation.

## Required TUI Elements

- Chat transcript view for chronological user, assistant, and tool messages.
- Multiline prompt composer with submit, cancel/interrupt, and empty-input handling.
- Header or status bar showing the active `.codegeist/session.json` path,
  `workingDir`, and current runtime provider/model when available.
- Tool activity view showing tool name, status, duration, and bounded summary for
  running, completed, failed, timed-out, and cancelled calls.
- File tool rendering for read/list/glob/grep/write results, including result counts,
  truncation markers, and affected paths.
- Change review rendering for write, patch, and edit activity with affected files,
  status, and preview or diff summary.
- Shell activity rendering with command, cwd, exit code, timeout status, duration,
  stdout preview, stderr preview, and truncation markers.
- Runtime tool/MCP status view that can show currently available Codegeist and MCP
  tools from runtime state without persisting their definitions in
  `.codegeist/session.json`.
- Error and approval-style prompt rendering for side-effecting or failed tool activity
  when the underlying services expose that state.
- Minimal help/keybinding view for submit, cancel/interrupt, quit, save, scroll, and
  help actions.
- Scroll or history navigation so older chat and tool activity remains reachable.
- Line-oriented renderer fallback that projects the same essential rows when a
  full-screen layout is unavailable or unsuitable for tests.

## Persistence Boundaries

- `.codegeist/session.json` remains the only chat persistence source.
- The TUI may render runtime provider/model, MCP connection, enabled tool, and command
  status, but those values must not be written to `.codegeist/session.json` as
  provider config, MCP definitions, enabled tool definitions, or chat status.
- UI-only state such as selected pane, scroll offset, layout, focused widget, draft
  prompt text, and transient keybinding state must stay outside
  `.codegeist/session.json`.

## Child Tasks

Solve these child tasks sequentially. Do not parallelize implementation; each child
creates the contract used by the next one.

- `tasks/T007_06_01_build-tui-view-model-projection.md` - build pure view-model and
  runtime-status projection from persisted session state and transient config/tool
  inventory.
- `tasks/T007_06_02_add-deterministic-line-renderers.md` - render the view model into
  deterministic line-oriented output for chat, tools, status, help, and errors.
- `tasks/T007_06_03_add-tui-input-parser-and-console-seam.md` - add the parsed input
  model and testable terminal seam before any real JLine integration.
- `tasks/T007_06_04_implement-terminal-tui-controller-loop.md` - implement the
  prompt loop over `SessionStoreService` and `ChatHarnessService.ask(true, prompt)`.
- `tasks/T007_06_05_add-jline-console-and-spring-command.md` - add the production
  JLine console and Spring Shell `tui` command.
- `tasks/T007_06_06_document-terminal-tui-architecture.md` - document the implemented
  TUI architecture and run final verification.

## Dependency Order

- `T007_06_01` comes first and owns data projection.
- `T007_06_02` depends on `T007_06_01` and owns deterministic rendering.
- `T007_06_03` depends on `T007_06_02` and owns input parsing plus the console seam.
- `T007_06_04` depends on `T007_06_03` and owns the controller loop.
- `T007_06_05` depends on `T007_06_04` and owns real terminal plus command wiring.
- `T007_06_06` depends on `T007_06_05` and closes docs plus verification.

## Acceptance Criteria

- A focused test proves the TUI renderer projects representative
  `.codegeist/session.json` content
  deterministically, including chat messages, tool calls, file results, change
  summaries, shell output, errors, and truncation markers.
- A focused test or smoke proves the TUI path can open or create the session store
  and save updates through `SessionStoreService` or its focused successor.
- A focused test proves the runtime status projection can show provider/model, MCP,
  and enabled-tool information without writing those runtime-only values into
  `.codegeist/session.json`.
- A focused test proves the line-oriented fallback renders the same essential chat and
  tool information deterministically for automation.
- The TUI does not own provider selection, MCP config parsing, tool execution, or a
  second persistence model.
- Existing noninteractive `--version`, `--show-config`, and `ask` command paths remain
  usable.
- Architecture docs describe the actual terminal TUI behavior.

## Non-Goals

- Do not implement Vaadin, desktop, API, SDK, server, or remote sync.
- Do not copy OpenCode's OpenTUI/Solid component structure.
- Do not implement polished full-screen parity or copy another TUI framework's
  component architecture in one task.
- Do not implement OpenCode-deferred TUI features in this child: plugins, sidebars,
  session browser, fork/share/revert, transcript export, prompt stash/history,
  full autocomplete, mouse support, LSP panels, subagents, skills, or memory.
- Do not add unrelated tools while working on TUI rendering.

## Suggested Tests

- Unit-test session-store rendering for messages, tool activity, file results, change
  summaries, shell output, errors, and truncation markers.
- Unit-test runtime status projection separately from persisted session-store rendering.
- Spring Boot test only if command registration or wiring needs it.
- Narrow noninteractive smoke only if the TUI entrypoint can run deterministically.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<terminal-tui-test-selector>
task test
```
