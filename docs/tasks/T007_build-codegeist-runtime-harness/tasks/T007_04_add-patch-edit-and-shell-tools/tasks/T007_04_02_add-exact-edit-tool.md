# T007_04_02 Add Exact Edit Tool

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: solved

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

## Result

- Added `CodegeistEditFileTool` as the `codegeist_edit` local callback.
- The public schema exposes only `path` and `edits[]` with `oldText` and `newText`.
- The implementation rejects outside-workspace absolute, traversal, and symlink
  targets before reading or writing by default.
- Added `CodegeistWorkingDirectoryGuard`; `workspace.dir-guard-disabled: true` is the
  only opt-out and disables active-workspace containment while keeping existence and
  regular-file checks.
- Multi-edit inputs are matched against the original LF-normalized file content,
  require unique exact matches, reject overlaps, preserve leading BOM and CRLF style,
  and write once only after all validation passes.
- Stale file bytes are checked immediately before writing so a concurrent content
  change fails instead of being overwritten.
- `ToolSessionPart` remains unchanged; edit results persist only the bounded text
  preview.
- Direct `codegeist.yml` can tune the compact edit diff preview through
  `tools.codegeist-edit.diff-preview-lines` and `diff-preview-chars`; final output is
  still capped by `ToolOutputBounds`.
- Added native artifact smoke coverage through `scripts/tests/artifact-smoke.ps1`,
  which delegates edit-specific checks to `scripts/tests/file-edit-ask-smoke.ps1`.
  The sub-harness runs the real native `ask` command against a deterministic
  Ollama-compatible fixture provider, exercises `codegeist_edit` through Spring AI
  tool calls, and checks UTF-8 BOM/multibyte, LF/CRLF, final-newline, configured
  ISO-8859-1 bytes, and persisted completed `ToolSessionPart` behavior.
- Added `docs/developer/architecture/edit-tool.md` as the detailed developer guide
  for the edit contract, planning algorithm, containment guard, normalization,
  stale-write protection, preview settings, tests, and sharp edges.

## File Targets

- `app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/chat/` if chat persistence needs
  an integration assertion.
- `docs/developer/architecture/local-file-tools.md`

## Verification

Focused verification passed from `app/codegeist/cli`:

```bash
task test TEST=CodegeistToolsConfigTest,CodegeistLocalToolsTest
```

Result: focused tools-config and local-tools tests passed.

Artifact smoke is now native-only. Jar smoke is intentionally not part of the
current verification contract.

## Source Notes

- The standalone working-directory guard child was removed before implementation;
  this slice keeps containment local to `CodegeistEditFileTool`.
- Use `../ask-project-research.md` for the accepted edit preview format and deferred
  behavior.
- Do not add typed edit fields to `ToolSessionPart` unless a focused test requires
  them.
