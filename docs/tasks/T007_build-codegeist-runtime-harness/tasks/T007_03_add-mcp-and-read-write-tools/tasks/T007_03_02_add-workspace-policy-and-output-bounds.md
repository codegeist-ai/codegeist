# T007_03_02 Add Workspace Policy And Output Bounds

Parent: `T007_03_add-mcp-and-read-write-tools`

Status: open

## Goal

Add reusable workspace-safety and output-bound primitives for the later local file
tools and MCP recording wrappers.

## Dependencies

- Can be implemented independently from `T007_03_01`.
- Must be complete before `T007_03_03_add-local-file-tools.md`.

## Scope

- Add `WorkspacePolicy` under `ai.codegeist.app.tool`.
- Add `ToolOutputBounds` under `ai.codegeist.app.tool`.
- Add `ToolOutputPreview` under `ai.codegeist.app.tool`.
- Keep both helper classes as Spring beans because `CodegeistFileTools` and MCP
  recording wrappers will inject them later.
- Implement path containment, real-path checks, symlink escape rejection, write-target
  validation, session-store write protection, relative-path rendering, preview
  truncation, line truncation, and result-limit capping.

## Acceptance Criteria

- Existing paths cannot escape the working directory through traversal or symlinks.
- Missing write targets are accepted only under the working directory when the parent
  exists and is safely inside the workspace.
- The active session store path is rejected as a write target.
- Preview output, line previews, result limits, read limits, and error previews are
  bounded deterministically.
- Error messages remain concise and do not include secret-bearing config values.

## Non-Goals

- Do not implement local file tool callbacks.
- Do not add ignored-file or generated-file policy unless a focused test requires it.
- Do not shell out to OS tools.

## Suggested Tests

- `WorkspacePolicyTest` for relative paths, absolute paths, traversal rejection,
  symlink escape rejection, write symlink rejection, missing parent rejection,
  active session store rejection, and stable relative paths.
- `ToolOutputBoundsTest` for preview truncation, omitted counts, line caps, result
  limits, read defaults, read caps, and bounded errors.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=WorkspacePolicyTest,ToolOutputBoundsTest
```
