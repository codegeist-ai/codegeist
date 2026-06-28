# T007_07 Verify Session Store Tool Harness

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Verify the complete T007 session-store tool harness and update current-state
documentation.

This child closes T007 only after resumable `ask -c/--continue`, MCP config,
read/write tools, patch/edit, shell, Codegeist-owned agent control loop, TUI, and
`.codegeist/session.json` storage are all proven together.

## Scope

- Run focused tests from `T007_02` through `T007_06`.
- Run the broader `task test` entrypoint from `app/codegeist/cli`.
- Add local-provider smoke only if the implementation needs a real local Ollama call.
- Update `docs/developer/architecture/architecture.md` with implemented session
  store, MCP, tools, patch/edit, shell, agent loop, and TUI behavior.
- Confirm no database, server runtime, remote sync, API/SDK, Vaadin, PF4J, JBang,
  LSP, skills, memory, or subagents were introduced accidentally.

## Acceptance Criteria

- Focused session-store tests pass.
- Focused MCP/read/write tool tests pass.
- Focused patch/edit and shell tests pass.
- Focused agent-loop tests pass.
- Focused TerminalUI harness tests or bounded TUI smoke checks pass.
- `task test` passes from `app/codegeist/cli`.
- Architecture docs match the implemented current state.
- T007 parent acceptance criteria are satisfied.

## Non-Goals

- Do not add new runtime features during final verification.
- Do not run hosted provider tests unless a future task explicitly opts into a safe
  remote provider category.
- Do not add native or Windows smoke requirements unless packaging or command runtime
  behavior changed in a way that requires them.

## Verification

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<session-store-test-selector>
task test TEST=<mcp-and-readwrite-tools-test-selector>
task test TEST=<patch-shell-tools-test-selector>
task test TEST=<agent-loop-test-selector>
task test TEST=<terminalui-harness-test-selector>
task test
```

Use local provider verification only when the implementation needs a real local
Ollama call:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=<local-session-store-harness-selector>
```
