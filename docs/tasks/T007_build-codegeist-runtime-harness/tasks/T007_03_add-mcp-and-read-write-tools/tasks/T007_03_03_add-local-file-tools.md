# T007_03_03 Add Local File Tools

Parent: `T007_03_add-mcp-and-read-write-tools`

Status: completed

## Goal

Add Codegeist-owned local file tool callbacks for read/list/glob/grep/write with
workspace resolution, bounded model-visible output, and bounded persisted tool
results.

## Dependencies

- Depends on `T007_03_01_add-tool-session-persistence.md`.
- Depends on `T007_03_02_add-workspace-resolution-and-output-bounds.md`.

## Scope

- Add `CodegeistLocalTools` under `ai.codegeist.app.tool`.
- Add `CodegeistLocalTool` under `ai.codegeist.app.tool` as the generic local tool
  contract.
- Add `CodegeistToolInput` under `ai.codegeist.app.tool` so local tools receive a
  typed raw JSON payload instead of a bare `String`.
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

- `codegeist_read` reads bounded line-numbered text relative to the active workspace
  using the configured workspace encoding and rejects missing files, directories, and
  binary files.
- `codegeist_list` returns stable non-recursive `[DIR]` and `[FILE]` entries.
- `codegeist_glob` returns stable bounded matches relative to the active workspace
  without shelling out.
- `codegeist_grep` returns stable bounded matching line previews and records invalid
  regex as a failed tool result.
- `codegeist_write` creates or overwrites regular text files relative to the active
  workspace and rejects directories plus missing parents.
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

- `CodegeistLocalToolsTest` with `@TempDir` fixtures for all five callbacks.
- Prove success paths and focused failures for each tool.
- Prove every successful and failed call records a bounded `ToolSessionPart`.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=WorkspaceResolverTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,SessionStoreServiceTest
```

## Implementation Result

- Added `CodegeistLocalTools` under `ai.codegeist.app.tool` as the stable Spring AI
  callback assembler for Codegeist-owned local tools, currently including
  `codegeist_read`, `codegeist_list`, `codegeist_glob`, `codegeist_grep`, and
  `codegeist_write`.
- Split local file tool behavior into package-private Spring-managed per-tool
  components plus `CodegeistFileToolSupport` for shared JSON parsing, schema, path,
  configured-charset text reading, binary, and glob helpers.
- Added `CodegeistFileEncoding` so local file read, grep, and write paths use the
  global `workspace.encoding` charset, falling back to UTF-8 when it is unset.
- `CodegeistLocalTools` receives the per-tool components as a
  `List<CodegeistLocalTool>` and does not know individual tool names or tool domains;
  callback order is not part of the runtime contract because tools are selected by
  name.
- `CodegeistLocalTool.execute(...)` receives `CodegeistToolInput`, which wraps the raw
  Spring AI JSON payload and normalizes missing or blank input to `{}`.
- Added `CodegeistLocalToolCallback` to convert handled local tool success and
  failure paths into bounded model-visible strings and matching `ToolSessionPart`
  records.
- Added the minimal `CodegeistToolResult` and local input records needed by this
  slice without adding provider chat wiring, MCP callbacks, patch/edit, shell,
  ignored-file filtering, or parent-directory creation.
- Added `CodegeistLocalToolsTest` with temp-directory fixtures for the five local
  callbacks, focused failures, bounded previews, and recorded tool parts.

## Verification

- 2026-06-19: `task test TEST=WorkspaceResolverTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,SessionStoreServiceTest`
  passed from `app/codegeist/cli` with 40 tests, 0 failures, 0 errors, and 0 skips.
- 2026-06-19: `task test` passed from `app/codegeist/cli` with 106 tests, 0
  failures, 0 errors, and 6 skips.
- 2026-06-20: `task test TEST=WorkspaceResolverTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,SessionStoreServiceTest`
  passed from `app/codegeist/cli` with 40 tests, 0 failures, 0 errors, and 0 skips
  after the generic `CodegeistLocalTool`/`CodegeistLocalTools` rename, typed
  `CodegeistToolInput`, and injected `CodegeistToolJsonMapper` updates.
- 2026-06-20: `task test` passed from `app/codegeist/cli` with 106 tests, 0
  failures, 0 errors, and 6 skips.
- 2026-06-20: `task test TEST=CodegeistWorkspaceConfigTest,WorkspaceResolverTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,SessionStoreServiceTest`
  passed from `app/codegeist/cli` with 48 tests, 0 failures, 0 errors, and 0 skips
  after adding global `workspace.encoding` support through `CodegeistFileEncoding`
  and storing the parsed config value as a `Charset`.
- 2026-06-20: `task test` passed from `app/codegeist/cli` with 110 tests, 0
  failures, 0 errors, and 6 skips.
- 2026-06-20: `task final-smoke-suite` passed from `app/codegeist/cli` after the
  Windows-safe YAML quoting fix in `WorkspaceResolverTest`, with Linux and Windows
  jar/native statuses all `passed`, `linux platform smoke total: 96.305s`, and
  `windows platform smoke total: 233.946s`.
