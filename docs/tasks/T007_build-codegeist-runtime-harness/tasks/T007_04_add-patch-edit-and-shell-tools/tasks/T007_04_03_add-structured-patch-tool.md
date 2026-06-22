# T007_04_03 Defer Structured Patch Tool

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: deferred

## Goal

Record the decision not to add a separate structured patch tool in the current
T007_04 implementation slice.

## Deferred Scope

- Do not implement `codegeist_patch` in T007_04.
- Keep `codegeist_write` focused on create/overwrite behavior.
- Keep `codegeist_edit` as the primary Codegeist-owned file mutation contract:
  one existing file, `edits[]` exact replacements, parse/validate before write,
  no partial mutation on validation failure, and bounded `ToolSessionPart` output.
- Keep shell mutation separate under the future `codegeist_shell` task; do not use
  shell commands as the hidden implementation path for structured edits.
- Revisit a separate patch tool only when Codegeist has a focused requirement for
  add/update/delete or multi-file patch application that cannot be expressed safely
  through exact edit plus write.

## Decision Rationale

- Pi is the closest fit for current Codegeist: it has no separate patch tool; its
  `edit` tool uses `path` plus `edits[]`, validates replacements before writing,
  updates one existing file, and returns diff/patch details for review.
- Codegeist already implements the useful subset of that shape in `codegeist_edit`,
  while staying stricter than Pi by keeping exact-only matching and existing
  `ToolSessionPart` persistence.
- OpenCode's `apply_patch` and Aider's `PatchCoder` prove that structured patch
  formats are useful for add/update/delete and multi-file changes, but they add a
  parser, path-conflict rules, create/delete semantics, partial-application choices,
  and broader review concerns that are not required by the current harness.
- mini-SWE-agent is shell-first and has no first-class patch/edit tool, which is a
  cautionary example rather than a Codegeist implementation model for safe file
  mutation.

## Future File Targets

- `app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/chat/` if chat persistence needs
  an integration assertion.
- `docs/developer/architecture/local-file-tools.md`

## Verification

No Java verification is required for this deferred documentation decision. If a
future task reopens `codegeist_patch`, use focused tests under
`app/codegeist/cli/src/test/java/ai/codegeist/app/tool/` before implementation.

## Source Notes

- `../ask-project-research.md` records the source-backed comparison across
  OpenCode, Pi, Aider, mini-SWE-agent, and Spring AI Agent Utils.
- If this task is reopened later, keep `codegeist_patch` separate from
  `codegeist_edit` and `codegeist_write`, parse the full patch before mutation,
  validate every target before side effects, and keep patch side-file artifacts,
  TUI patch review, git auto-add, and git auto-commit out of the first patch slice.
