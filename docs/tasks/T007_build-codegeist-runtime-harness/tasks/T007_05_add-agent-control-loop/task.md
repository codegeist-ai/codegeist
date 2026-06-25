# T007_05 Add Agent Control Loop

Parent: `T007_build-codegeist-runtime-harness`

Status: solved

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

## Implementation Result

- Added `CodegeistAgentLoopService` as the synchronous Codegeist-owned
  model/tool/model controller for one prompt request.
- Added `CodegeistChatTurnRequest` so provider-facing Spring AI message history stays
  internal while `CodegeistChatRequest` remains limited to `model` and `prompt`.
- Added a raw `ChatResponse` seam in `CodegeistChatService` and updated
  `CodegeistChatModel` plus `OllamaChatModel` to receive message-history turns.
- Updated `OllamaChatModel` to pass tool callback definitions while setting
  `internalToolExecutionEnabled(false)`, so Codegeist owns dispatch and
  continuation.
- Rewired `ChatHarnessService` to run through the loop and keep existing session
  persistence and stdout behavior unchanged.
- Added `docs/developer/architecture/agent-control-loop.md` and refreshed the current
  architecture map, tool-callback docs, parent T007 task, and project memory.
- Kept provider/framework-owned internal tool execution disabled in provider adapters;
  Codegeist exposes local and MCP callback definitions but owns dispatch and
  continuation.

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

Final verification from `app/codegeist/cli`:

```bash
task test TEST=CodegeistAgentLoopServiceTest,ChatHarnessServiceTest,CodegeistChatServiceTest,AskCommandsSessionStoreTest,SessionStoreServiceTest
task test
```

Results:

- Focused selector: 24 tests, 0 failures, 0 errors, 0 skipped.
- Broad JVM suite: 174 tests, 0 failures, 0 errors, 6 skipped.
- `task native-smoke` passed on Linux, including deterministic file-edit and shell
  ask smokes.
- `task local-linux-smoke` passed, including JVM tests, jar packaging, native build,
  and native artifact smokes.
- `task mcp-remote-smoke` passed, including the direct `streamable_http` callback
  path and the Ollama-backed `ask` path that persists a completed remote MCP
  `ToolSessionPart`.
- `task final-smoke-suite` passed for Linux and Windows QEMU native smokes.

## Implementation Notes

- Use `implementation-plan.md` as the detailed coding handoff for the planned
  test-first implementation sequence, source changes, Spring AI message shape,
  loop algorithm, risks, and verification commands.
- Use `ask-project-question-catalog.md` for the repeatable third-party question
  set, `ask-project-research.md` for the answered source-backed comparison,
  `opencode-agent-loop.md` for the focused OpenCode loop translation notes, and
  `pi-agent-loop.md` plus `aider-agent-loop.md` for focused loop translation
  notes.
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

## Source Notes

- `ask-project-question-catalog.md` is the source-backed research question catalog
  for Aider, OpenCode, Pi, mini-SWE-agent, and Spring AI Agent Utils. Keep future
  T007_05 third-party questions in this directory so the agent-loop handoff stays
  local to the task.
- `ask-project-research.md` answers the catalog across all five projects and
  records the accepted Codegeist translation, test seams, deferred work, and
  caveats for the implementation pass.
- `implementation-plan.md` is the detailed T007_05 implementation handoff. It
  records the planned `CodegeistAgentLoopService`, `CodegeistChatTurnRequest`, raw
  `ChatResponse` seam, provider-model message-history call contract, sequential
  tool dispatch loop, max-round guard, focused tests, architecture-doc updates,
  risks, and verification commands.
- `opencode-agent-loop.md` is the focused OpenCode behavior map for the
  model/tool/model loop. Use it when implementing the loop or updating the
  architecture docs to distinguish Codegeist-owned dispatch from Spring AI's
  internal tool-calling behavior.
- `pi-agent-loop.md` is the focused Pi behavior map for a small linear owned loop.
  Its Mermaid diagrams are embedded directly in that markdown file, so the runtime
  sequence, stop/continue states, and Codegeist translation can be reviewed without
  following external diagram files or rereading the full third-party analysis.
- `aider-agent-loop.md` is the focused Aider behavior map for the reflection loop
  around model text, app-owned edits, shell/test observations, and bounded
  reflection retries. Its Mermaid diagrams are embedded directly in markdown and
  are intended as cautionary evidence for what Codegeist should not copy into a
  typed tool-call loop.
