# T007_03_05 Add MCP Callback Adapter

Parent: `T007_03_add-mcp-and-read-write-tools`

Status: open

## Goal

Add the Spring AI MCP client dependency and a lazy stdio-only adapter that maps
Codegeist `mcp:` config into MCP tool callbacks for the scoped chat tool run.

## Dependencies

- Depends on `T007_03_04_add-tool-aware-chat-harness.md`.

## Scope

- Add `spring-ai-starter-mcp-client` to `app/codegeist/cli/pom.xml`.
- Add `CodegeistMcpAdapter` under `ai.codegeist.app.mcp`.
- Add `CodegeistMcpRun` under `ai.codegeist.app.mcp`.
- Add `DefaultCodegeistMcpRun` as the package-private first implementation.
- Map `CodegeistConfig.rootElement(McpClientsRootElement.class)` into Spring AI MCP
  stdio clients during `CodegeistMcpAdapter.openRun(...)` only.
- Convert MCP clients into Spring AI `ToolCallback` values and wrap them with
  recording behavior in `CodegeistToolService`.
- Close MCP resources through the tool run.

## Acceptance Criteria

- Config parsing and `--show-config` do not start MCP processes.
- Absent or empty `mcp:` config opens an empty MCP run.
- Unsupported MCP `type` fails clearly before a provider call.
- One configured `stdio` MCP client can be mapped into the client creation/callback
  path without exposing `spring.ai.mcp.client.*` as public Codegeist config.
- MCP callbacks are included in the tool run and their bounded results are recorded.
- Closeable MCP resources are closed when the tool run closes.

## Non-Goals

- Do not add SSE, HTTP, OAuth, environment variables, public timeout fields,
  enablement flags, server discovery, server management commands, resources, or
  prompt support.
- Do not run network-dependent MCP tests.
- Do not persist MCP command, args, status, resources, prompts, or tool definitions
  in `.codegeist/session.json`.

## Suggested Tests

- `CodegeistMcpAdapterTest` for empty config, unsupported type, stdio mapping seam,
  callback exposure, and close behavior.
- `CodegeistToolServiceTest` for local plus fake MCP callback assembly and recording
  wrapper behavior.
- Use hand-written fakes instead of Mockito.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=CodegeistMcpAdapterTest,CodegeistToolServiceTest,ChatHarnessServiceTest,SessionStoreServiceTest
```
