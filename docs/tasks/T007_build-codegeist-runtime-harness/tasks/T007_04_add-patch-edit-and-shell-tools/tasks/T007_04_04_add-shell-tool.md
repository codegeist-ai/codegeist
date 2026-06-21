# T007_04_04 Add Shell Tool

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: open

## Goal

Add a bounded Codegeist-owned shell tool for one local process execution per tool
call.

## Scope

- Add `codegeist_shell` with a command string, one consistent cwd field, and a
  timeout field.
- Resolve cwd against the active workspace and reject cwd escape before process
  startup.
- Run one process per tool call with no stdin, no persistent shell, no background
  process registry, and no PTY behavior.
- Capture stdout and stderr without deadlocks and return bounded separate previews
  in one stable summary.
- Treat non-zero exit as a completed shell result with exit code in the preview.
- Record timeout explicitly in the preview and terminate the process when the timeout
  is reached.

## Acceptance Criteria

- A focused test proves a successful command records bounded stdout/stderr and a
  completed `ToolSessionPart`.
- A focused test proves a non-zero exit is recorded as a completed shell result with
  the exit code.
- A focused test proves timeout behavior is bounded and recorded with
  `Timed out: true` or the final status selected by the implementation task.
- A focused test proves cwd escape fails before process startup.
- Existing plain no-continue `ask` behavior remains unaffected.

## File Targets

- `app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/chat/` if chat persistence needs
  an integration assertion.
- `docs/developer/architecture/local-file-tools.md`

## Verification

Run from `app/codegeist/cli`:

```bash
task test TEST=<shell-tool-test-selector>
```

## Source Notes

- Depends on `T007_04_01_add-working-directory-guard.md`.
- Use `../opencode-shell-tool-comparison.md` and `../ask-project-research.md` before
  implementation.
- Do not add permission prompts, tree-sitter command scanning, plugin environment
  hooks, configured shell discovery, or full-output side files in this child task.
