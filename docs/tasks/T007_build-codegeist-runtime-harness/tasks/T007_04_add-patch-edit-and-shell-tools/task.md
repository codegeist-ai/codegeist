# T007_04 Add Patch Edit And Shell Tools

Parent: `T007_build-codegeist-runtime-harness`

Status: solved

## Goal

Add bounded exact-edit tooling plus a minimal shell execution tool in chats while
keeping a separate structured patch tool deferred.

## Scope

- Add exact edit behavior that mutates files under the chat working directory and
  records reviewable summaries in `.codegeist/session.json`.
- Add shell tools that run local commands with explicit cwd, exit code, and bounded
  merged process-output summaries.
- Keep local tool output bounded before it reaches the model, TUI, or session store.
- Record each side-effecting tool call and result in the active
  `.codegeist/session.json`.
- Keep working-directory path validation for file mutation minimal but real: no
  outside-workingDir file mutation.

## Acceptance Criteria

- A focused test proves exact edit mutates only an allowed working-directory file
  and records a bounded tool result in `.codegeist/session.json`.
- A focused test proves shell runs with bounded output, exit code, explicit cwd
  behavior, and session-store persistence.
- Outside-workingDir file mutation fails before the side effect runs.
- Existing read/write tools and plain no-continue `ask` command behavior remain
  unaffected.
- Architecture docs describe the implemented exact edit, deferred patch, and shell
  behavior.

## Non-Goals

- Do not claim sandboxing beyond the explicit tested working-directory checks.
- Do not implement `codegeist_patch` in this T007_04 slice; structured multi-file
  patch application is deferred until a focused task needs add/update/delete or
  multi-file patch semantics.
- Do not implement shell sandboxing, permission prompts, process supervision, or
  output side files.
- Do not implement network tools, MCP server management, plugins, LSP, subagents, or
  background process persistence.
- Do not add patch review UI in this task; the replacement `T007_06` slice is only
  a minimal TerminalUI chat-harness integration.

## Child Tasks

- `tasks/T007_04_02_add-exact-edit-tool.md` - solved: added `codegeist_edit` for
  bounded exact replacements while keeping `codegeist_write` focused on
  create/overwrite; compact diff preview limits are configurable through
  `tools.codegeist-edit` direct config.
- `tasks/T007_04_03_add-structured-patch-tool.md` - deferred: do not add
  `codegeist_patch` now; keep Pi-style exact edit as the primary file mutation
  contract and revisit structured patch only when multi-file add/update/delete is
  required.
- `tasks/T007_04_04_add-shell-tool.md` - solved: added `codegeist_shell` as one
  local process execution per tool call with configurable host-side wrapper prefix,
  bounded merged stdout/stderr output, exit code reporting, no cwd containment, no
  background process registry, and completed non-zero-exit plus timeout recording.
- `tasks/T007_04_05_document-and-verify-side-effect-tools.md` - solved: current-state
  architecture docs, memory, and parent status now describe the implemented edit,
  deferred patch, and shell behavior after focused and broad JVM verification.

## Suggested Tests

- Temporary working-directory fixtures for exact edit and shell cwd behavior.
- Simple cross-platform shell command or Java-level fake for shell behavior.
- Shell output-bound and explicit-cwd checks.
- Session-store persistence checks for side-effecting tool results.

## Source Notes

- `ask-project-question-catalog.md` is the source-backed research question catalog
  to run before implementing this task. It targets Aider, OpenCode, Pi,
  mini-SWE-agent, and Spring AI Agent Utils through the local `/ask-project`
  workflow.
- `ask-project-research.md` answers that catalog and records the accepted evidence,
  recommended Codegeist translation, test seams, deferred work, and open decisions
  for the implementation pass.
- Follow the recorded T007_04_03 decision: OpenCode and Aider show useful but
  broader structured patch formats; Pi's exact edit shape is the better current
  Codegeist fit; mini-SWE-agent's shell-only mutation is not the model for local
  file edits.
- `opencode-shell-tool-comparison.md` documents the source-backed OpenCode shell
  tool implementation and compares it with the proposed first Codegeist shell tool
  shape.
- Reuse existing tool engines before building new internals. Spring AI Agent Utils
  already documents `FileSystemTools` with read/write/edit plus allowed-directory
  checks and `ShellTools` with timeout, stdout/stderr, exit-code, and background
  process support, though the current Codegeist shell tool intentionally stays simpler;
  the MCP filesystem server already exposes `edit_file`,
  `write_file`, search, and directory access control. Treat those as candidates for
  adapters or implementation-source reuse before writing a parallel helper.
- Keep the Codegeist-owned local callback facade even when a lower-level engine is
  reused. `codegeist_*` names, workspace-relative file input, Codegeist file path
  policy, `workspace.encoding`, file-tool output bounds, handled
  `CodegeistToolException` failures, and
  `ToolSessionPart(tool,status,outputPreview)` persistence remain Codegeist contracts.
  Do not directly expose broad third-party file or shell tool surfaces to the model
  unless a focused task explicitly changes that product contract.
- Keep future research answers in this directory so the patch/edit and shell-tool
  implementation handoff stays local to `T007_04`.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<edit-shell-tools-test-selector>
task test
```
