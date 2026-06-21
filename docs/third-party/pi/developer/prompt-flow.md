# Pi Prompt Flow

This document traces the shipped Pi CLI prompt path from process startup to `AgentSession`, `Agent`, provider streaming, tool execution, extension hooks, and JSONL session persistence.

## Scope And Evidence

- Project revision: `bc0db643502ba0bf1b227a97d9d5885cefc2b909` from `docs/third-party/pi/source`.
- Evidence type: static source inspection only. No upstream install, test, TUI run, provider call, or smoke test was executed for this document.
- Primary path covered: the current `packages/coding-agent` CLI flow through `AgentSession` and `packages/agent/src/agent.ts`.
- `packages/agent/src/harness/agent-harness.ts` is a newer harness surface, but it is not the primary prompt path used by `packages/coding-agent/src/main.ts` in this snapshot.

## End-To-End Flow

1. `packages/coding-agent/src/bun/cli.ts` and `src/cli.ts` bootstrap the executable and call `main(process.argv.slice(2))`.
2. `packages/coding-agent/src/main.ts` parses CLI flags, selects `rpc`, `json`, `print`, or `interactive` mode, resolves the effective session cwd, loads settings/resources/extensions, resolves models, creates a `SessionManager`, and builds an `AgentSessionRuntime`.
3. `createAgentSessionRuntime(...)` stores the current `AgentSession` plus cwd-bound services and gives modes a stable host object for session replacement flows such as new, resume, fork, and switch.
4. `createAgentSessionFromServices(...)` delegates to `createAgentSession(...)` in `packages/coding-agent/src/core/sdk.ts` after cwd-bound services exist.
5. `createAgentSession(...)` restores messages, model, and thinking level from `SessionManager.buildSessionContext()` when continuing an existing session, then constructs `Agent` with provider, extension, queue, and conversion callbacks.
6. `AgentSession` subscribes to `Agent` events, installs tool hooks, builds built-in tool definitions, creates an `ExtensionRunner`, registers extension bindings, activates tools, and builds the initial system prompt.
7. The active mode calls `session.prompt(...)` when input arrives. Interactive mode calls it for initial messages and editor submissions; print mode calls it for `initialMessage` and each positional message; RPC mode calls it for JSONL `prompt` commands.
8. `AgentSession.prompt(...)` handles slash extension commands, extension `input` handlers, skill expansion, prompt-template expansion, streaming queueing, auth/model preflight, optional pre-prompt compaction, user-message construction, pending extension context messages, and `before_agent_start` mutations.
9. `AgentSession._runAgentPrompt(...)` calls `Agent.prompt(...)`, then continues while post-run retry, compaction, or queued extension work requires another agent run.
10. `Agent.prompt(...)` rejects concurrent runs, normalizes input into `AgentMessage[]`, creates a context snapshot, creates loop config, and calls `runAgentLoop(...)`.
11. `runAgentLoop(...)` emits user-message lifecycle events, streams an assistant response, executes tool calls, appends tool-result messages, drains steering and follow-up queues, and exits with `agent_end` when no more work remains.
12. `AgentSession._handleAgentEvent(...)` forwards events to extensions first, then user listeners, then persists `message_end` events through `SessionManager`.

## Mode Entry Points

- Interactive mode: `packages/coding-agent/src/modes/interactive/interactive-mode.ts` calls `session.prompt(initialMessage, { images })`, then loops over `getUserInput()` and calls `session.prompt(userInput)`. While streaming, editor submissions call `session.prompt(text, { streamingBehavior: "steer" })` so input is queued instead of rejected.
- Print and JSON modes: `packages/coding-agent/src/modes/print-mode.ts` binds extensions with mode `print` or `json`, subscribes to session events for JSON output, sends `initialMessage` and extra messages through `session.prompt(...)`, then prints final assistant text in text mode.
- RPC mode: `packages/coding-agent/src/modes/rpc/rpc-mode.ts` handles a JSONL `prompt` command by starting `session.prompt(command.message, { images, streamingBehavior, source: "rpc", preflightResult })`. It sends the success response when preflight succeeds, not when the full agent run finishes.

## Runtime Construction

- `main.ts` creates a startup `SettingsManager`, resolves project trust, loads runtime services with `createAgentSessionServices(...)`, and collects diagnostics instead of printing from lower layers.
- `createAgentSessionServices(...)` creates cwd-bound `AuthStorage`, `SettingsManager`, `ModelRegistry`, and `DefaultResourceLoader`, reloads resources, registers providers pending from extensions, and applies extension flag values.
- `createAgentSession(...)` chooses an initial model from explicit options, restored session data, settings defaults, or provider defaults. If no model is available, thinking is set to `off` and the caller later errors in non-interactive modes.
- Existing session messages are restored into `agent.state.messages`; new sessions append initial model and thinking-level entries so resume can reconstruct state.
- The `Agent` receives callbacks for `convertToLlm`, `transformContext`, provider streaming, provider payload hooks, provider response hooks, steering/follow-up queue modes, session id, thinking budgets, transport, and retry delay settings.

## Tool Registry And System Prompt

- Built-in tool definitions are created by `createAllToolDefinitions(...)` from `packages/coding-agent/src/core/tools/index.ts`.
- Built-in tools are `read`, `bash`, `edit`, `write`, `grep`, `find`, and `ls`.
- The default active tool set is `read`, `bash`, `edit`, and `write` unless CLI options provide `--tools`, `--exclude-tools`, `--no-tools`, or `--no-builtin-tools` behavior.
- `AgentSession._buildRuntime(...)` creates an `ExtensionRunner`, binds core/session/provider APIs into it, applies extension bindings, and calls `_refreshToolRegistry(...)`.
- `_refreshToolRegistry(...)` merges built-in definitions, extension-registered tools, and SDK-provided custom tools into `_toolDefinitions` and `_toolRegistry`. It wraps tools through `wrapRegisteredTools(...)` so extension tools receive an extension context at execution time.
- `setActiveToolsByName(...)` updates `agent.state.tools` and rebuilds the base system prompt. The prompt includes selected tool snippets, tool guidelines, skills, context files, custom system prompt, and appended system prompt from the resource loader.
- `packages/coding-agent/src/core/extensions/wrapper.ts` is intentionally narrow: wrappers adapt tool execution context only. Tool-call and tool-result interception happen through `AgentSession` hooks installed on `Agent`.

## Prompt Preflight And Mutation

- `AgentSession.prompt(text, options)` handles extension commands first when prompt-template expansion is enabled and the text starts with `/`. A matching registered extension command executes immediately and no LLM prompt is sent.
- Extension `input` handlers run before skill and prompt-template expansion. A handler can mark the input as handled or return transformed text/images.
- Skill commands with `/skill:<name>` expand to a `<skill>` block containing the skill file body and the caller's arguments. Prompt templates then expand over the resulting text.
- If the agent is already streaming, `prompt(...)` requires `streamingBehavior`. `followUp` queues after the agent would otherwise stop; `steer` queues before the next assistant response after the current turn's tool calls.
- For a non-streaming prompt, pending bash messages are flushed before model/auth preflight. The method errors if no model is selected or if the selected model lacks configured authentication.
- If the previous assistant message needs compaction, `prompt(...)` can continue the agent first to compact before adding the new user message.
- The user message is built as a `role: "user"` `AgentMessage` with text content plus optional images. Pending extension `nextTurn` custom messages are appended alongside it.
- `before_agent_start` extension handlers can add custom messages and replace the per-turn system prompt. If no handler replaces the prompt, `AgentSession` resets `agent.state.systemPrompt` to the base prompt so prior per-turn modifications do not leak.

## Agent Loop

- `Agent.prompt(...)` rejects concurrent prompt processing and calls `runPromptMessages(...)`.
- `runPromptMessages(...)` calls `runAgentLoop(...)` with a snapshot of current system prompt, messages, and tools. State mutations happen later by processing emitted loop events.
- `Agent.createLoopConfig(...)` passes model, thinking level, transport, tools, conversion hooks, extension/provider hooks, and queue drainers into the loop.
- `Agent.processEvents(...)` updates streaming state, appends finalized messages into `agent.state.messages` on `message_end`, tracks pending tool calls, records agent errors, and awaits every subscribed listener.
- `Agent.runWithLifecycle(...)` sets `isStreaming`, creates an abort controller, runs the loop, emits a synthetic assistant error/aborted message on failure, and only clears active-run state after event listeners settle.

## Provider Boundary

- `agent-loop.ts` keeps Pi's broader `AgentMessage[]` form until `streamAssistantResponse(...)`.
- `streamAssistantResponse(...)` first applies `transformContext`, which `sdk.ts` wires to extension `context` handlers through `ExtensionRunner.emitContext(...)`.
- The transformed agent messages are converted to provider-facing `Message[]` by `convertToLlm`. The `sdk.ts` wrapper removes image content when image blocking is enabled.
- The loop builds a provider `Context` containing the system prompt, converted messages, and active tools.
- `sdk.ts` provides the stream function. It resolves auth from `ModelRegistry.getApiKeyAndHeaders(model)`, merges provider environment overrides, retry settings, timeout settings, attribution headers, and then calls `streamSimple(model, context, options)` from `pi-ai`.
- `onPayload` forwards provider payloads through extension `before_provider_request` handlers. `onResponse` emits `after_provider_response` handlers with response status and headers.
- Provider stream events update a partial assistant message for `start`, text, thinking, and tool-call deltas. On `done` or `error`, the loop asks the stream for the final assistant message, emits `message_end`, and returns that assistant message to the loop.

## Tool Execution Boundary

- The loop extracts `toolCall` content from the assistant message and dispatches sequentially if the global tool execution mode is sequential or any selected tool declares `executionMode: "sequential"`; otherwise it prepares calls first and executes prepared tools in parallel.
- `prepareToolCall(...)` looks up the active tool by name, applies optional `prepareArguments`, validates arguments with `validateToolArguments(...)`, and runs `beforeToolCall` when configured.
- `AgentSession._installAgentToolHooks()` wires `beforeToolCall` to extension `tool_call` handlers. A handler can block execution by returning a block result; non-Error thrown values are converted to blocking errors.
- `executePreparedToolCall(...)` calls the tool's `execute(toolCallId, args, signal, update)` method. Tool partial updates become `tool_execution_update` events.
- `finalizeExecutedToolCall(...)` runs `afterToolCall` when configured. `AgentSession` maps this to extension `tool_result` handlers, which can replace content, details, or error status.
- Each finalized call emits `tool_execution_end`, then the loop creates a `role: "toolResult"` message and emits its `message_start` and `message_end` events.
- Tool-result messages are appended to the loop context and become part of the next provider call. If every finalized result has `terminate: true`, the current tool batch ends the loop instead of continuing to another assistant response.

## Extension Events In Prompt Flow

| Hook | Source path | Effect |
| --- | --- | --- |
| `input` | `ExtensionRunner.emitInput(...)` | Can handle input or transform text/images before skill and template expansion. |
| Extension command | `AgentSession._tryExecuteExtensionCommand(...)` | Runs a registered slash command immediately instead of sending an LLM prompt. |
| `before_agent_start` | `ExtensionRunner.emitBeforeAgentStart(...)` | Can add custom context messages and replace the per-turn system prompt. |
| `context` | `ExtensionRunner.emitContext(...)` | Can transform `AgentMessage[]` immediately before LLM conversion. |
| `before_provider_request` | `ExtensionRunner.emitBeforeProviderRequest(...)` | Can mutate the raw provider payload before request dispatch. |
| `after_provider_response` | `Agent` `onResponse` callback in `sdk.ts` | Observes provider response status and headers. |
| `message_start`, `message_update`, `message_end` | `AgentSession._emitExtensionEvent(...)` | Mirrors agent message lifecycle. `message_end` can replace a message with the same role. |
| `tool_call` | `AgentSession.beforeToolCall` hook | Can block a tool call before execution. |
| `tool_result` | `AgentSession.afterToolCall` hook | Can mutate tool result content, details, or error status. |
| `turn_start`, `turn_end`, `agent_start`, `agent_end` | `AgentSession._emitExtensionEvent(...)` | Observes loop lifecycle. `agent_end` handlers can queue more messages, which `AgentSession._handlePostAgentRun()` then continues. |

## Session Persistence

- `SessionManager` stores sessions as append-only JSONL files under the configured session directory. Each file begins with a `session` header and then stores tree entries with `id`, `parentId`, and timestamps.
- `SessionManager.newSession(...)` creates the header, clears indexes, and selects a timestamped `<timestamp>_<session-id>.jsonl` file when persistence is enabled.
- `SessionManager.setSessionFile(...)` opens an existing file, loads entries, runs migrations, rebuilds indexes, and treats the latest entry as the current leaf.
- `SessionManager._appendEntry(...)` appends an entry, indexes it, advances the leaf, and persists it. Persistence delays writing a new session file until the first assistant message arrives unless the session was already flushed.
- `AgentSession._handleAgentEvent(...)` persists on `message_end`. Regular `user`, `assistant`, and `toolResult` messages call `appendMessage(...)`; custom messages call `appendCustomMessageEntry(...)`.
- `appendMessage(...)` writes a `type: "message"` entry as a child of the current leaf and advances the leaf. This is what gives normal prompt, assistant, and tool-result history its branch shape.
- Extension-only custom entries created with `appendEntry(...)` are stored as `type: "custom"` and do not participate in provider context. Custom messages created with `sendCustomMessage(...)` use `type: "custom_message"` and do participate in context.
- `buildSessionContext(...)` walks from current leaf to root, restores model and thinking state from entries, applies the latest compaction summary when present, and returns the `AgentMessage[]` used to seed `agent.state.messages` on resume.
- Compaction summaries, branch summaries, bash execution messages, model changes, thinking-level changes, labels, and session info have separate append paths; not every visible or stored entry is a normal provider-facing message.

## Queues, Retry, And Compaction

- Steering messages are queued through `Agent.steer(...)` and drained before the next assistant response once the current assistant turn and its tool calls finish.
- Follow-up messages are queued through `Agent.followUp(...)` and drained only after the agent would otherwise stop.
- `AgentSession._handleAgentEvent(...)` removes delivered steering/follow-up messages from UI tracking on user `message_start` and emits `queue_update` events.
- `AgentSession._runAgentPrompt(...)` calls `_handlePostAgentRun()` after each `Agent.prompt(...)` or `Agent.continue()` run. Post-run handling can trigger retry, compaction continuation, or extension-queued work.
- Retry is driven by assistant messages with retryable error stop reasons and settings limits. Context overflow is handled by compaction logic instead of normal retry.
- Compaction can run before a new prompt if the previous assistant message requires it, or after a run when `_checkCompaction(...)` returns true. The compacted context is persisted through dedicated session entries rather than by rewriting old message entries.

## Source Map

- `packages/coding-agent/src/main.ts` - CLI mode resolution, session manager setup, resource/model/tool option resolution, runtime creation, and mode dispatch.
- `packages/coding-agent/src/core/agent-session-services.ts` - cwd-bound service creation and extension provider registration.
- `packages/coding-agent/src/core/agent-session-runtime.ts` - current session host and session replacement lifecycle.
- `packages/coding-agent/src/core/sdk.ts` - `createAgentSession(...)`, `Agent` construction, provider streaming callback, context conversion, restored session state, and default tool selection.
- `packages/coding-agent/src/core/agent-session.ts` - prompt preprocessing, extension hooks, tool registry, event subscription, persistence coordination, retry, compaction, and queue handling.
- `packages/agent/src/agent.ts` - mutable agent state, queue ownership, context snapshots, loop config, run lifecycle, and event reduction.
- `packages/agent/src/agent-loop.ts` - assistant streaming, provider boundary, tool-call execution, tool-result messages, queue draining, and loop termination.
- `packages/coding-agent/src/core/session-manager.ts` - JSONL session files, append-only tree entries, branch traversal, and context reconstruction.
- `packages/coding-agent/src/core/extensions/runner.ts` - extension event dispatch and mutation semantics.
- `packages/coding-agent/src/core/extensions/wrapper.ts` - extension tool context adaptation.
- `packages/coding-agent/src/core/tools/index.ts` - built-in tool definitions and default tool families.
- `packages/coding-agent/src/modes/interactive/interactive-mode.ts` - interactive prompt submissions and streaming queue behavior.
- `packages/coding-agent/src/modes/print-mode.ts` - print and JSON event-stream prompt submissions.
- `packages/coding-agent/src/modes/rpc/rpc-mode.ts` - JSONL prompt command handling and preflight acknowledgements.

## Caveats

- The document describes source-observed behavior only. It does not prove provider-specific runtime behavior, terminal rendering behavior, or file-system side effects with a live Pi process.
- Pi intentionally has no built-in sandbox around tools or extensions. Built-in tools and extension code run with the process permissions unless the user supplies an external boundary.
- Project trust controls loading of project-local resources and extensions; it is not a tool permission system.
- Provider behavior can vary by selected model, auth type, transport, retry settings, and provider implementation under `packages/ai`.
