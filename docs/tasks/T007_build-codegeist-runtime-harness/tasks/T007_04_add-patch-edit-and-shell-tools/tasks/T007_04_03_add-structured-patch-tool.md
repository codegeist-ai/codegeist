# T007_04_03 Add Structured Patch Tool

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: open

## Goal

Add a separate structured patch tool for multi-file side effects that should not be
expressed as shell commands.

## Scope

- Add `codegeist_patch` as a separate concept from `codegeist_write`,
  `codegeist_edit`, and `codegeist_shell`.
- Parse the full patch before writing files.
- Validate every target path inside the patch tool before any side effect runs.
- Reject invalid or partial patches before mutation when feasible.
- Summarize changed files and bounded hunks in `ToolSessionPart.outputPreview`.

## Acceptance Criteria

- A focused test proves a valid patch changes the expected allowed file or files and
  records a bounded completed `ToolSessionPart`.
- A focused test proves an invalid patch fails without partial writes.
- A focused test proves a patch targeting outside the workspace fails before any
  side effect.
- The patch tool does not shell out to apply patches.

## File Targets

- `app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/chat/` if chat persistence needs
  an integration assertion.
- `docs/developer/architecture/local-file-tools.md`

## Verification

Run from `app/codegeist/cli`:

```bash
task test TEST=<structured-patch-tool-test-selector>
```

## Source Notes

- May depend on `T007_04_02_add-exact-edit-tool.md` only if the implementation
  deliberately shares non-trivial diff or preview code.
- Do not add patch side-file artifacts, TUI patch review, git auto-add, or git
  auto-commit in this child task.
