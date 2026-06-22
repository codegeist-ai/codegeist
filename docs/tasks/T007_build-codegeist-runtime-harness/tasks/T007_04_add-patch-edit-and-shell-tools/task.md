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
- Do not add a patch review TUI unless `T007_06` implements the needed rendering.

## Child Tasks

- `tasks/T007_04_02_add-exact-edit-tool.md` - solved: added `codegeist_edit` for
  bounded exact replacements while keeping `codegeist_write` focused on
  create/overwrite; compact diff preview limits are configurable through
  `tools.codegeist-edit` direct config.
- `tasks/T007_04_03_add-structured-patch-tool.md` - add `codegeist_patch` for
  parse-before-write structured patches that are not applied through shell.
- `tasks/T007_04_04_add-shell-tool.md` - add `codegeist_shell` as one bounded local
  process execution per tool call with cwd, timeout, exit code, stdout, and stderr
  summaries.
- `tasks/T007_04_05_document-and-verify-side-effect-tools.md` - update current-state
  architecture docs and verify the parent acceptance criteria after implementation.

## Suggested Tests

- Temporary working-directory fixtures for patch/edit.
- Simple cross-platform shell command or Java-level fake for shell behavior.
- Timeout and output-bound checks.
- Session-store persistence checks for side-effecting tool results.

## Source Notes

- `ask-project-question-catalog.md` is the source-backed research question catalog
  to run before implementing this task. It targets Aider, OpenCode, Pi,
  mini-SWE-agent, and Spring AI Agent Utils through the local `/ask-project`
  workflow.
- `ask-project-research.md` answers that catalog and records the accepted evidence,
  recommended Codegeist translation, test seams, deferred work, and open decisions
  for the implementation pass.
- `opencode-shell-tool-comparison.md` documents the source-backed OpenCode shell
  tool implementation and compares it with the proposed first Codegeist shell tool
  shape.
- Reuse existing tool engines before building new internals. Spring AI Agent Utils
  already documents `FileSystemTools` with read/write/edit plus allowed-directory
  checks and `ShellTools` with timeout, stdout/stderr, exit-code, and background
  process support; the MCP filesystem server already exposes `edit_file`,
  `write_file`, search, and directory access control. Treat those as candidates for
  adapters or implementation-source reuse before writing a parallel helper.
- Keep the Codegeist-owned local callback facade even when a lower-level engine is
  reused. `codegeist_*` names, workspace-relative input, Codegeist path/cwd policy,
  `workspace.encoding`, output bounds, handled `CodegeistToolException` failures,
  and `ToolSessionPart(tool,status,outputPreview)` persistence remain Codegeist
  contracts. Do not directly expose broad third-party file or shell tool surfaces to
  the model unless a focused task explicitly changes that product contract.
- Keep future research answers in this directory so the patch/edit and shell-tool
  implementation handoff stays local to `T007_04`.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<patch-shell-tools-test-selector>
task test
```
