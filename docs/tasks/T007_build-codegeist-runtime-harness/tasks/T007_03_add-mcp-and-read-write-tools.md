# T007_03 Add MCP And Read Write Tools

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add Codegeist-owned MCP client configuration and the first useful read/write file
tools for resumable chats.

## Scope

- Add `spring-ai-starter-mcp-client` to `app/codegeist/cli`.
- Add the minimal `CodegeistConfig` model needed to load MCP clients from direct
  `codegeist.yml` under the top-level `mcp:` map.
- Start with `stdio` MCP clients using `type`, `command`, and `args`.
- Map Codegeist MCP config into Spring AI MCP client/tool callback setup where
  needed. Spring AI's `spring.ai.mcp.client.*` properties are not the public
  Codegeist config contract.
- Add the first Codegeist-owned read/write tool path for list/read/glob/grep/write.
- Keep `write` focused on creating or overwriting allowed files under the chat
  working directory; patch/edit semantics remain in `T007_04`.
- Record tool calls and bounded tool results in `chat.json` when used in a chat;
  keep MCP client definitions and enabled tool definitions in config/runtime state.

## Current Progress

- The minimal direct `codegeist.yml` `mcp:` config root is already implemented and
  tested through `McpClientsRootElement`, `McpClientConfig`, and
  `CodegeistConfigServiceTest`.
- Remaining work still includes Spring AI MCP client/callback setup,
  read/list/glob/grep/write tools, and chat-file tool-result persistence.

## Acceptance Criteria

- A focused config test proves direct `codegeist.yml` can load the minimal `mcp:`
  client map.
- A focused test proves configured MCP callbacks can be made available to the chat
  call path.
- Focused tests prove read/list/glob/grep tools return bounded results.
- A focused test proves `write` creates or overwrites only an allowed working-directory
  file and records a bounded tool result in `chat.json`.
- Tool calls/results are stored in `chat.json` without unbounded output.
- `CodegeistChatRequest` still contains only runtime model and prompt.
- Architecture docs describe the actual MCP and read/write tool behavior.

## Non-Goals

- Do not implement patch/edit or shell in this child.
- Do not implement custom MCP transports, MCP OAuth, MCP server discovery, MCP server
  management, or hosted provider calls.
- Do not add broad future tool descriptors before tests need them.

## Suggested Tests

- Config test for one `stdio` MCP client.
- Chat service or model test with a fake or test `ToolCallbackProvider`.
- Temporary working-directory fixtures for read/list/glob/grep/write.
- Chat file assertions for bounded tool result persistence.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<mcp-and-readwrite-tools-test-selector>
task test
```
