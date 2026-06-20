# T007_03_04 Add Tool-Aware Chat Harness

Parent: `T007_03_add-mcp-and-read-write-tools`

Status: open

## Goal

Add the narrow one-turn chat harness that opens a scoped tool run, passes local tool
callbacks to the provider call path, saves recorded tool parts, and keeps
`AskCommands` as a thin shell adapter.

## Dependencies

- Depends on `T007_03_03_add-local-file-tools.md`.

## Scope

- Add `ChatHarnessService` under `ai.codegeist.app.chat`.
- Add `CodegeistChatExecutionContext` under `ai.codegeist.app.chat`.
- Add context-aware overloads to `CodegeistChatService` and `CodegeistChatModel`.
- Update `OllamaChatModel` to pass runtime `ToolCallback` values through Spring AI
  chat options for the current prompt.
- Add `CodegeistToolService` and `CodegeistToolRun` under `ai.codegeist.app.tool`.
- Add `DefaultCodegeistToolRun` as the package-private first implementation.
- Refactor `AskCommands` to delegate to `ChatHarnessService` and print only the
  returned response content.

## Acceptance Criteria

- `CodegeistChatRequest` still has exactly `model` and `prompt` components.
- `ChatHarnessService` selects the default provider and model, opens one tool run,
  calls the chat service with execution context, saves prompt/tool parts/assistant
  text, closes the run, and returns response content.
- Tool callbacks from local file tools reach the provider call path through
  `CodegeistChatExecutionContext`.
- `AskCommands` stdout remains response text only.
- Existing `ask -c` parser and command-boundary exception behavior remains intact.

## Non-Goals

- Do not add MCP runtime setup; `T007_03_05` owns that adapter.
- Do not reconstruct provider-facing context from stored session history.
- Do not add TUI, patch/edit, shell, permission prompts, or broad tool registry.

## Suggested Tests

- `ChatHarnessServiceTest` with hand-written fakes for provider config, chat service,
  tool service, and session store behavior.
- Update `AskCommandsSessionStoreTest` to focus on command delegation, stdout, parser
  flags, and exception mapper annotation.
- Add or update a focused chat model/service test proving callbacks are passed to
  the model context without changing `CodegeistChatRequest`.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=ChatHarnessServiceTest,AskCommandsSessionStoreTest,CodegeistLocalToolsTest,SessionStoreServiceTest
```
