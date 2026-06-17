# T007_03_03 Add Local File Tools

Parent: `T007_03_add-mcp-and-read-write-tools`

Status: open

## Goal

Add Codegeist-owned local file tool callbacks for read/list/glob/grep/write with
workspace policy, bounded model-visible output, and bounded persisted tool results.

## Dependencies

- Depends on `T007_03_01_add-tool-session-persistence.md`.
- Depends on `T007_03_02_add-workspace-policy-and-output-bounds.md`.

## Scope

- Add `CodegeistFileTools` under `ai.codegeist.app.tool`.
- Add `CodegeistLocalToolCallback` under `ai.codegeist.app.tool`.
- Add `CodegeistToolResult` and local input records under `ai.codegeist.app.tool`.
- Implement callback definitions for:
  - `codegeist_read`
  - `codegeist_list`
  - `codegeist_glob`
  - `codegeist_grep`
  - `codegeist_write`
- Use explicit Spring AI `ToolCallback` implementations and `ToolDefinition` input
  schemas rather than singleton `@Tool` methods.
- Record one bounded `ToolSessionPart` per local tool call.

## Acceptance Criteria

- `codegeist_read` reads bounded line-numbered UTF-8 text and rejects missing files,
  directories, binary files, outside paths, and symlink escapes.
- `codegeist_list` returns stable non-recursive `[DIR]` and `[FILE]` entries.
- `codegeist_glob` returns stable bounded relative matches without following symlink
  escapes or shelling out.
- `codegeist_grep` returns stable bounded matching line previews and records invalid
  regex as a failed tool result.
- `codegeist_write` creates or overwrites only regular text files under the working
  directory and rejects directories, symlink targets, missing parents, outside paths,
  and the active session store file.
- Every local tool returns bounded model-visible text and records the same bounded
  preview in `ToolSessionPart`.

## Non-Goals

- Do not add patch/edit semantics.
- Do not create parent directories for writes.
- Do not add shell, repo-map, git automation, ignored-file filtering, recursive list,
  grep context lines, or multiline grep support.
- Do not wire tools into the provider call path yet; that belongs to
  `T007_03_04`.

## Suggested Tests

- `CodegeistFileToolsTest` with `@TempDir` fixtures for all five callbacks.
- Prove success paths and focused failures for each tool.
- Prove every successful and failed call records a bounded `ToolSessionPart`.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=WorkspacePolicyTest,ToolOutputBoundsTest,CodegeistFileToolsTest,SessionStoreServiceTest
```
