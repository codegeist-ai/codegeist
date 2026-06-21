# T007_03_06 Finalize Tool Docs And Verification

Parent: `T007_03_add-mcp-and-read-write-tools`

Status: completed

## Goal

Synchronize current-state documentation and run focused plus broad verification for
the completed T007_03 MCP and local read/write tool slice.

## Dependencies

- Depends on `T007_03_05_add-mcp-callback-adapter.md`.

## Scope

- Update `docs/developer/architecture/architecture.md` with implemented packages,
  classes, runtime flow, tool names, MCP callback setup, session persistence, and
  tests.
- Update `docs/tasks/T007_build-codegeist-runtime-harness/tasks/T007_03_add-mcp-and-read-write-tools/task.md`
  with completed progress and verification evidence.
- Update child task statuses when their implementation is complete.
- Update `docs/memory-bank/chat.md` only if future sessions need the new current
  implementation state.
- Run focused tests for the T007_03 slice and broad `task test`.

## Acceptance Criteria

- Architecture docs describe actual implemented MCP and read/list/glob/grep/write
  tool behavior, not planned behavior.
- Task files distinguish completed T007_03 work from remaining T007_04 patch/edit
  and shell work.
- Focused T007_03 tests pass.
- Broad JVM `task test` passes or any blocker is documented with exact failure scope.
- No direct `mvn test` commands are added to docs.

## Non-Goals

- Do not add new runtime behavior in this finalization task unless a verification
  failure requires a narrow fix.
- Do not run native or Windows smoke tests unless the implementation changed native,
  packaging, or command startup behavior beyond additive reflection metadata.

## Completion Notes

- Confirmed that current architecture docs describe the implemented T007_03 state:
  direct `mcp:` config, `stdio` and `streamable_http` callback setup, local
  `codegeist_read`, `codegeist_list`, `codegeist_glob`, `codegeist_grep`, and
  `codegeist_write` tools, bounded `ToolSessionPart` recording, prompt-scoped MCP
  cleanup, and the focused test surface.
- Marked the aggregate `T007_03` task complete while keeping `T007_04` patch/edit
  and shell tools, `T007_05` agent control loop, and `T007_06` TUI work open.

## Suggested Tests

Candidate focused command from `app/codegeist/cli`:

```bash
task test TEST=CodegeistWorkspaceConfigTest,WorkspaceResolverTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,CodegeistMcpAdapterTest,CodegeistToolServiceTest,SessionStoreServiceTest,ChatHarnessServiceTest,AskCommandsSessionStoreTest
```

Final JVM command from `app/codegeist/cli`:

```bash
task test
```

Docker-backed MCP smoke from `app/codegeist/cli`:

```bash
task mcp-remote-smoke
```

## Verification

- 2026-06-21: `task test TEST=CodegeistWorkspaceConfigTest,WorkspaceResolverTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,CodegeistMcpAdapterTest,CodegeistToolServiceTest,SessionStoreServiceTest,ChatHarnessServiceTest,AskCommandsSessionStoreTest`
  passed from `app/codegeist/cli` with 63 tests, 0 failures, 0 errors, and 0 skips.
- 2026-06-21: `task test` passed from `app/codegeist/cli` with 129 tests, 0
  failures, 0 errors, and 6 skips.
- 2026-06-21: `task mcp-remote-smoke` passed from `app/codegeist/cli`; the smoke
  reported `MCP remote smoke status: passed` and `mcp remote smoke total: 12.056s`.
- 2026-06-21: `git --no-pager diff --check` passed.
