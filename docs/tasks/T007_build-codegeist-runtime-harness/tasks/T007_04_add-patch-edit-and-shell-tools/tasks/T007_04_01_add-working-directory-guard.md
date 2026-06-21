# T007_04_01 Add Working Directory Guard

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: open

## Goal

Add the shared working-directory guard needed before T007_04 side-effecting tools
mutate files or start shell processes.

## Scope

- Resolve candidate file paths and shell cwd values against the active Codegeist
  workspace.
- Reject traversal-normalized paths, symlink-resolved paths, or shell cwd values
  that escape the active working directory before any side effect runs.
- Keep the guard Codegeist-owned; do not rely on Spring AI Agent Utils file or
  shell helpers for containment policy.
- Return clear bounded error previews that can be recorded through the existing
  `ToolSessionPart(tool,status,outputPreview)` shape.

## Acceptance Criteria

- A focused test proves an allowed file path under the active workspace remains
  allowed.
- A focused test proves a mutating file path outside the workspace is rejected
  before mutation.
- A focused test proves shell cwd escape is rejected before `ProcessBuilder.start()`
  or equivalent process startup.
- The guard does not change existing read/list/glob/grep/write behavior except where
  a current test must intentionally use it for side-effecting paths.

## File Targets

- `app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/tool/`
- `docs/developer/architecture/local-file-tools.md` when the shared guard becomes
  part of current architecture.

## Verification

Run from `app/codegeist/cli`:

```bash
task test TEST=<working-directory-guard-test-selector>
```

## Source Notes

- Read `../ask-project-research.md` before implementation.
- The guard is the prerequisite for `T007_04_02`, `T007_04_03`, and `T007_04_04`.
- Do not claim sandboxing beyond the explicit tested path and cwd checks.
