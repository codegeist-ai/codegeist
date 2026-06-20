# T007_05 Add Agent Control Loop

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add the first Codegeist-owned coding-agent control loop that can continue after tool
use instead of relying only on a single provider call with prompt-scoped Spring AI
callbacks.

The loop should make Codegeist responsible for the observable agent flow:

```text
user prompt -> model request -> tool call dispatch -> tool result -> model continuation
```

This task starts the OpenCode-style loop translation for Codegeist while preserving
the current Java/Spring boundaries, file-backed session store, and minimal scope.

## Dependencies

- Depends on `T007_03_add-mcp-and-read-write-tools/tasks/T007_03_04_add-tool-aware-chat-harness.md`.
- Should account for `T007_03_05_add-mcp-callback-adapter.md` when MCP callbacks are
  available, but must not require MCP process setup for the first loop test.

## Scope

- Add a small agent-loop service under `ai.codegeist.app.chat` or a focused package
  chosen during implementation.
- Keep `ChatHarnessService` as the command-facing one-turn harness, but let it call
  the loop service once the loop owns model/tool/model continuation.
- Add the minimum loop state needed to represent the current prompt, model response,
  requested tool calls, tool results, and final assistant response.
- Dispatch tool calls through the existing `CodegeistToolRun` boundary so local and
  future MCP tools share the same recording path.
- Persist bounded tool activity and assistant text through the existing session-store
  exchange path unless a failing test proves a more granular persistence method is
  needed.
- Use hand-written fake model and fake tool components for the first loop tests.

## Acceptance Criteria

- A focused test proves the loop can run at least two model turns with one tool call
  between them.
- A focused test proves tool results are fed back into the model continuation rather
  than being saved only as side effects.
- A focused test proves recorded `ToolSessionPart` values still persist before the
  final assistant text.
- `CodegeistChatRequest` remains limited to `model` and `prompt` unless this task
  explicitly records and justifies a different request contract.
- `AskCommands` stdout remains final assistant response text only.
- The architecture docs clearly distinguish the implemented Codegeist loop from
  Spring AI's internal tool-calling behavior.

## Non-Goals

- Do not implement the terminal TUI in this task.
- Do not add patch/edit, shell, or MCP lifecycle behavior unless a dependency task has
  already implemented the tool and this loop only dispatches it through existing
  boundaries.
- Do not add permission prompts, policy engines, approvals, subagents, skills,
  memory, repo-map, git automation, background workers, server runtime, API/SDK, or
  streaming UI events in the first loop slice.
- Do not copy OpenCode's TypeScript, Effect, server, SQLite, SDK, or TUI architecture.
- Do not call hosted providers or require a live local Ollama model for the first loop
  contract tests.

## Suggested Tests

- `CodegeistAgentLoopServiceTest` with a fake model that first requests a fake tool
  and then returns a final assistant answer after receiving the tool result.
- `ChatHarnessServiceTest` update proving the harness delegates through the loop when
  the loop service exists.
- Existing `AskCommandsSessionStoreTest` should continue to prove command delegation
  and stdout-only behavior.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=CodegeistAgentLoopServiceTest,ChatHarnessServiceTest,AskCommandsSessionStoreTest,SessionStoreServiceTest
task test
```

## Implementation Notes

- Treat OpenCode as behavior evidence only: preserve the loop shape and observable
  lifecycle, not the implementation architecture.
- Prefer the smallest synchronous loop first. Streaming, cancellation, approval
  prompts, and TUI event projection can be added by later focused tasks.
- Keep provider-specific logic in provider model classes or narrow adapters. The loop
  should operate on Codegeist-level model/tool/result concepts, not Ollama-specific
  payloads.
- If Spring AI's current `ChatResponse` does not expose enough portable tool-call
  details for Codegeist-owned dispatch, add the smallest internal test seam first
  before changing provider-facing production code.
