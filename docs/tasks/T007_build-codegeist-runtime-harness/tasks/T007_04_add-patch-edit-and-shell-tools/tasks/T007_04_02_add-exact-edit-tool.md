# T007_04_02 Add Exact Edit Tool

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: open

## Goal

Add a bounded Codegeist-owned exact edit tool for precise text replacement inside
the active workspace.

## Scope

- Add `codegeist_edit` as a separate tool from existing `codegeist_write`.
- Keep `codegeist_write` focused on create/overwrite behavior.
- Accept a workspace-relative file path and one or more exact replacements.
- Reject missing `oldText`, identical old/new text, missing target files,
  directories, no match, ambiguous repeated matches unless explicitly supported,
  overlapping multi-edits, and path escape before mutation.
- Produce a bounded edit summary with stable headings suitable for model output and
  `ToolSessionPart.outputPreview`.

## Acceptance Criteria

- A focused test proves an allowed exact edit mutates the expected file and records
  a bounded completed `ToolSessionPart`.
- A focused test proves no-match and ambiguous-match inputs fail without changing
  the file.
- A focused test proves outside-workspace edit paths fail before mutation.
- Existing read/list/glob/grep/write tools and plain no-continue `ask` behavior
  remain unaffected.

## File Targets

- `app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/chat/` if chat persistence needs
  an integration assertion.
- `docs/developer/architecture/local-file-tools.md`

## Verification

Run from `app/codegeist/cli`:

```bash
task test TEST=<exact-edit-tool-test-selector>
```

## Source Notes

- Depends on `T007_04_01_add-working-directory-guard.md`.
- Use `../ask-project-research.md` for the accepted edit preview format and deferred
  behavior.
- Do not add typed edit fields to `ToolSessionPart` unless a focused test requires
  them.
