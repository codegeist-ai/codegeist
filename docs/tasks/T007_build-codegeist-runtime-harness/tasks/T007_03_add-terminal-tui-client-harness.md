# T007_03 Add Terminal TUI Client Harness

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add the first terminal TUI client harness over the Codegeist runtime events.

The purpose is to make Codegeist feel like a terminal-first coding agent while
preserving the central rule: TUI is a client, not a second runtime. The TUI should
render runtime state and collect user input or approvals through runtime contracts.

## Dependency

Start after `T007_02` provides a runtime/session/event spine that the TUI can drive.
If that spine is not implemented yet, this task should only refine the TUI plan and
should not add UI dependencies or source code.

## OpenCode Evidence To Translate

Use these OpenCode TUI paths as behavior references:

- `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/context/sdk.tsx`
  for client/server SDK access, SSE subscription, batching, and reconnect behavior.
- `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/context/event.ts`
  for global/workspace/directory event filtering.
- `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/context/sync.tsx`
  for projected TUI state such as sessions, messages, permissions, questions,
  providers, and tools.
- `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/component/prompt/index.tsx`
  for prompt input, slash commands, shell mode, model/agent/variant selection, and
  file/editor context.
- `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/routes/session/index.tsx`
  for session rendering, abort/revert/unrevert, permission/question prompts, and
  tool rendering.

Translate behavior, not OpenTUI/Solid implementation details.

## Scope

- Decide the smallest Java terminal UI approach for the first harness. Spring Shell
  and JLine are already in the stack; add a new UI dependency only if a focused
  test or smoke path proves it is needed.
- Add a terminal client entrypoint only after it can call the same runtime service
  used by noninteractive commands.
- Render runtime events for prompt start, assistant output, failures, and completion.
- Prepare for approval/question rendering, but only implement approval UI when
  `T007_05` has runtime approval events.
- Keep line-oriented fallback behavior available when full-screen rendering is not
  possible.

## Acceptance Criteria

- The TUI client uses the runtime/session/event spine instead of calling providers
  or tools directly.
- A focused test or smoke verifies that a prompt can be submitted through the
  terminal harness or that the event renderer renders a representative event
  projection deterministically.
- The TUI does not own session persistence, provider selection, tool execution,
  permission policy, or workspace policy.
- The existing noninteractive `--version`, `--show-config`, and `ask` command paths
  remain usable.
- Architecture docs are updated with the actual implemented TUI/client behavior.

## Non-Goals

- Do not implement a polished OpenCode-equivalent full-screen UI in one task.
- Do not implement server, Vaadin, desktop, API, or SDK clients here.
- Do not add approval, shell mode, patch view, model picker, session list, or
  reconnect behavior unless the current runtime event model and focused tests
  require them.
- Do not copy OpenCode's OpenTUI/Solid component structure.

## Suggested Tests And Verification

- Unit-test event rendering where possible.
- Use Spring Boot tests only when command registration or runtime wiring needs it.
- Add a narrow smoke command only if the TUI entrypoint can run noninteractively in
  CI or local automation.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<new-tui-test-selector>
task test
```
