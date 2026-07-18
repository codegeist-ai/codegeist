# T007_03 Add MCP And Read Write Tools

Parent: `T007_build-codegeist-runtime-harness`

Status: completed

## Goal

Add Codegeist-owned MCP client configuration and the first useful read/write file
tools for resumable chats.

## Scope

- Treat this task as the aggregate for the split T007_03 implementation work. Do
  not implement it as one large runtime change; solve the child tasks in dependency
  order.
- Add `spring-ai-starter-mcp-client` to `app/codegeist/cli`.
- Add the minimal `CodegeistConfig` model needed to load MCP clients from direct
  `codegeist.yml` under the top-level `mcp:` map.
- Start with `stdio` MCP clients using the YAML key as client id plus `type`,
  `command`, and `args` fields.
- Add `streamable_http` MCP clients using the YAML key as client id plus `type`,
  `url`, and optional `endpoint` fields so a Docker-hosted MCP server can simulate a
  remote server in smoke tests.
- Map Codegeist MCP config into Spring AI MCP client/tool callback setup where
  needed. Spring AI's `spring.ai.mcp.client.*` properties are not the public
  Codegeist config contract.
- Add the first Codegeist-owned read/write tool path for list/read/glob/grep/write.
- Keep `write` focused on creating or overwriting allowed files under the chat
  working directory; patch/edit semantics remain in `T007_04`.
- Record tool calls and bounded tool results in `.codegeist/session.json` when used
  in a continued chat; keep MCP client definitions and enabled tool definitions in
  config/runtime state.

## Current Progress

- The minimal direct `codegeist.yml` `mcp:` config root is already implemented and
  tested through `McpClientsRootElement`, `McpClientsConfig`, `McpClientConfig`, and
  `CodegeistConfigServiceTest`. MCP client identity comes from the YAML key, so
  multiple clients can share the same transport `type` while the runtime model stays
  list-backed.
- `T007_03_01` is implemented and verified. `ToolSessionPart` now round-trips in
  `.codegeist/session.json`, session exchanges can append ordered tool parts before
  assistant text, missing or empty session stores create a session when continuing,
  `SessionStoreService.currentWorkingDirectory()` is available, `SessionStore` owns
  in-memory session-list mutation, and native reflection metadata includes the new
  part type.
- `T007_03_02` is implemented and verified. Direct `codegeist.yml`
  `workspace.directory` parsing, direct `workspace.encoding` parsing and validation,
  active workspace resolution, deterministic output capping, and native reflection
  metadata for the workspace config POJOs are in place.
- `T007_03_03` is implemented and verified. `CodegeistLocalTools` now exposes local
  read/list/glob/grep/write Spring AI callbacks with active workspace resolution,
  configured workspace encoding, bounded model-visible output, and matching bounded
  `ToolSessionPart` recording.
- `T007_03_04` is implemented and verified. `ChatHarnessService` now owns the
  one-turn `ask` orchestration, `CodegeistChatExecutionContext` carries prompt-scoped
  local tool callbacks beside unchanged `CodegeistChatRequest(model, prompt)`,
  `CodegeistToolService` opens local callback runs and records tool parts, and
  `AskCommands` is a thin output adapter. This is not yet an OpenCode-style
  coding-agent loop; Codegeist now exposes callbacks to one provider call but does
  not own an iterative model/tool/model control loop.
- The detailed accepted contract lives in
  `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-spec.md`.
- The implementation handoff lives in
  `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-implementation-plan.md`.
  It defines the planned Java packages, class contracts, workflows, tests, and
  verification order for the implementation pass.
- Source-backed research questions for OpenCode and Spring AI Agent Utils live in
  `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-question-catalog.md`.
- Source-backed answers live in
  `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-research.md`.
  They recommend implementing this slice as a narrow `ChatHarnessService` plus
  scoped `CodegeistToolRun`, with Agent Utils used as source inspiration rather than
  directly exposed file tools.
- A broader coding-agent harness comparison table lives in
  `docs/tasks/T007_build-codegeist-runtime-harness/coding-agent-harness-implementations.md`.
  The T007_03 research now also analyzes Aider, SWE-agent, and mini-SWE-agent as
  smaller harness references that argue against adding repo-map, git automation,
  benchmark trajectory, production Docker execution, or shell-first runtime features
  to this child. The only Docker scope now accepted here is a deterministic remote
  MCP smoke fixture.
- A focused local Aider and mini-SWE-agent expansion lives in
  `docs/tasks/T007_build-codegeist-runtime-harness/aider-mini-swe-harness-research.md`.
  It confirms that neither project is an MCP lifecycle reference, but both support
  the narrow `ChatHarnessService` plus scoped `CodegeistToolRun` boundary.
- `T007_03_05` is implemented and verified. Spring AI MCP client/callback setup now
  supports `stdio` and `streamable_http`, MCP callbacks join the prompt-scoped tool
  run with bounded recording, MCP resources close with the run, and
  `task mcp-remote-smoke` verifies the real `streamable_http` path against a local
  Docker fixture.
- Final docs and broad T007_03 verification are complete. At T007_03 completion,
  remaining runtime work moved to T007_04 exact edit and shell tools, T007_05 agent
  control loop, and T007_06 TerminalUI integration; those later slices are now also
  complete.

## Child Tasks

- `tasks/T007_03_01_add-tool-session-persistence.md` - completed; added
  `ToolSessionPart`, session-store append overloads, and native reflection metadata
  for tool parts.
- `tasks/T007_03_02_add-workspace-resolution-and-output-bounds.md` - completed;
  added shared workspace resolution and output-bound helpers used by local and MCP
  tools.
- `tasks/T007_03_03_add-local-file-tools.md` - completed; added Codegeist-owned
  `read`/`list`/`glob`/`grep`/`write` local file callbacks with bounded persisted
  results.
- `tasks/T007_03_04_add-tool-aware-chat-harness.md` - completed; added the reusable
  one-turn `ChatHarnessService`, tool-aware chat context, local tool run, and
  `AskCommands` refactor.
- `tasks/T007_03_05_add-mcp-callback-adapter.md` - completed; added the Spring AI MCP
  dependency, `stdio` and `streamable_http` adapter support, Docker-backed remote MCP
  smoke, and MCP callback integration with the tool run.
- `tasks/T007_03_06_finalize-tool-docs-and-verification.md` - completed; refreshed
  current-state docs, task state, memory, and focused plus broad verification.

## Dependency Order

- `T007_03_01` and `T007_03_02` can be implemented first and independently.
- `T007_03_03` depends on `T007_03_01` and `T007_03_02`.
- `T007_03_04` depends on `T007_03_03`.
- `T007_03_05` depends on `T007_03_04`.
- `T007_03_06` comes last.

## Specification Reference

Use `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-spec.md`
as the implementation handoff for this child. It defines the planned public MCP
config contract, chat execution context, MCP adapter, local file tool contracts,
workspace resolution, output bounds, `ToolSessionPart` persistence shape, test plan,
implementation order, and non-goals.

Use
`docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-implementation-plan.md`
as the concrete implementation plan after the specification is accepted. It names
the target classes, method shapes, package responsibilities, local tool behavior,
MCP workflow, session-store changes, tests, and verification commands.

Use `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-question-catalog.md`
to query OpenCode and Spring AI Agent Utils with `/ask-project` before Java
implementation. Feed the source-backed answers back into the specification or a
focused research summary.

Use `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-research.md`
as the current answer set for that catalog. Refresh it only when the third-party
analysis workspaces change or a specific implementation question remains unclear.

Use `docs/tasks/T007_build-codegeist-runtime-harness/coding-agent-harness-implementations.md`
as the compact comparison of public coding-agent harness implementations. Treat the
Aider, SWE-agent, and mini-SWE-agent rows as scope discipline for this child: they
inform the harness boundary but do not add implementation requirements beyond the
accepted T007_03 MCP and read/write tool contracts.

Do not implement Java runtime changes directly from this aggregate task. Use the
focused child tasks once the implementation pass starts.

## Acceptance Criteria

- A focused config test proves direct `codegeist.yml` can load the minimal `mcp:`
  client map.
- A focused test proves configured MCP callbacks can be made available to the chat
  call path.
- Focused tests prove read/list/glob/grep tools return bounded results.
- A focused test proves `write` creates or overwrites only an allowed working-directory
  file and records a bounded tool result in `.codegeist/session.json`.
- Tool calls/results are stored in `.codegeist/session.json` without unbounded output.
- `CodegeistChatRequest` still contains only runtime model and prompt.
- Architecture docs describe the actual MCP and read/write tool behavior.

## Non-Goals

- Do not implement patch/edit or shell in this child.
- Do not implement custom MCP transports beyond `stdio` and `streamable_http`, MCP
  OAuth, MCP server discovery, MCP server management, or hosted provider calls.
- Do not add broad future tool descriptors before tests need them.

## Suggested Tests

- Config test for one `stdio` MCP client and one `streamable_http` MCP client.
- Docker-backed remote MCP smoke for the real `streamable_http` callback path.
- Chat service or model test with a fake or test `ToolCallbackProvider`.
- Temporary working-directory fixtures for read/list/glob/grep/write.
- Session-store assertions for bounded tool result persistence.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<focused-t007-03-test-selector>
task mcp-remote-smoke
task test
```

## Verification

- 2026-06-21: `task test TEST=CodegeistWorkspaceConfigTest,WorkspaceResolverTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,CodegeistMcpAdapterTest,CodegeistToolServiceTest,SessionStoreServiceTest,ChatHarnessServiceTest,AskCommandsSessionStoreTest`
  passed from `app/codegeist/cli` with 63 tests, 0 failures, 0 errors, and 0 skips.
- 2026-06-21: `task test` passed from `app/codegeist/cli` with 129 tests, 0
  failures, 0 errors, and 6 skips.
- 2026-06-21: `task mcp-remote-smoke` passed from `app/codegeist/cli`; the smoke
  reported `MCP remote smoke status: passed` and `mcp remote smoke total: 12.056s`.
- 2026-06-21: `git --no-pager diff --check` passed.
