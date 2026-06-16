# T007_04 Add Patch Edit And Shell Tools

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add bounded side-effecting tools for patch/edit and shell execution in chats.

## Scope

- Add patch/edit tools that mutate files under the chat working directory and record
  reviewable summaries in `.codegeist/session.json`.
- Add shell tools that run local commands with explicit cwd, timeout, exit code, and
  bounded stdout/stderr summaries.
- Keep tool output bounded before it reaches the model, TUI, or session store.
- Record each side-effecting tool call and result in the active
  `.codegeist/session.json`.
- Keep working-directory path/cwd validation minimal but real: no outside-workingDir
  file mutation or cwd escape.

## Acceptance Criteria

- A focused test proves patch/edit mutates only an allowed working-directory file and records
  a bounded tool result in `.codegeist/session.json`.
- A focused test proves shell runs with bounded output, timeout behavior, exit code,
  and session-store persistence.
- Outside-workingDir file mutation or cwd escape fails before the side effect runs.
- Existing read/write tools and plain no-continue `ask` command behavior remain
  unaffected.
- Architecture docs describe the implemented patch/edit and shell behavior.

## Non-Goals

- Do not claim sandboxing beyond the explicit tested working-directory/cwd checks.
- Do not implement arbitrary unbounded shell execution.
- Do not implement network tools, MCP server management, plugins, LSP, subagents, or
  background process persistence.
- Do not add a patch review TUI unless `T007_05` implements the needed rendering.

## Suggested Tests

- Temporary working-directory fixtures for patch/edit.
- Simple cross-platform shell command or Java-level fake for shell behavior.
- Timeout and output-bound checks.
- Session-store persistence checks for side-effecting tool results.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<patch-shell-tools-test-selector>
task test
```
