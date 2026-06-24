# T007_05 Agent Control Loop Research

Source-backed answers for the T007_05 agent-loop question catalog. This
document summarizes the follow-up research pass across Aider, OpenCode, Pi,
mini-SWE-agent, and Spring AI Agent Utils before implementing the first
Codegeist-owned model/tool/model control loop.

## Scope And Evidence

- Task: `task.md`.
- Projects researched through the local `/ask-project` pattern:
  `aider`, `mini-swe-agent`, `opencode`, `pi`, and
  `spring-ai-agent-utils`.
- Evidence type: static source analysis from existing third-party workspaces
  under `docs/third-party/<project>/`, especially `repomix-output.xml` and
  durable local analysis docs.
- No upstream runtime commands, provider calls, interactive sessions, or
  upstream tests were run for this pass.
- This research answers design questions for Codegeist. It does not implement
  Java runtime behavior and does not claim that third-party runtime behavior was
  locally verified.

## Codegeist Baseline At Research Time

Codegeist currently has the T007 session-store and tool foundation:

- `ChatHarnessService` owns one command-facing non-streaming `ask` turn.
- `CodegeistChatService` calls one selected provider model with
  `CodegeistChatRequest(model, prompt)` and optional
  `CodegeistChatExecutionContext`.
- `CodegeistToolService` opens one prompt-scoped `CodegeistToolRun` that exposes
  local and MCP Spring AI `ToolCallback` values and records ordered
  `ToolSessionPart` values.
- `.codegeist/session.json` persists user prompt, ordered bounded tool parts, and
  final assistant text through the existing exchange path.
- Codegeist does not yet own a repeated model/tool/model controller. Provider
  tool-calling behavior is currently delegated to Spring AI inside one model
  call.

## Question Catalog

The research answered these base questions for every project:

1. How does the project implement the agent control loop from user prompt to
   model request, tool-call request, tool dispatch, tool result, and model
   continuation?
2. Which runtime data structures represent model turns, assistant tool calls,
   tool results, and final assistant responses? Which state is persisted and
   which remains runtime-only?
3. How are tools exposed to the model while keeping actual tool execution owned
   by the agent application instead of hidden provider or framework behavior?
4. How does the loop feed tool results back into the next model request? What is
   the message/history shape or equivalent continuation payload?
5. How does the loop decide whether to continue, stop, handle multiple tool
   calls, handle failed tools, and prevent infinite loops?
6. How are tool outputs bounded, summarized, recorded, or rendered before being
   returned to the model, saved, or shown to users?
7. Which focused tests prove a two-turn model/tool/model loop, tool-result
   continuation, and ordered tool activity before final assistant text?
8. What should Codegeist copy conceptually, adapt for Java/Spring, reject, or
   defer for T007_05?

It also answered one project-specific follow-up per project:

- OpenCode: loop boundaries across session processing, provider calls, tool
  execution, permission checks, event emission, and persistence.
- Spring AI Agent Utils: Java/Spring utility reuse, especially whether Codegeist
  should reuse or wrap Spring AI tool-calling behavior.
- Pi: stateful agent runtime, tool boundary, message history updates, and
  extension points.
- Aider: prompt-to-model-to-edit/test/shell/reflection loop and what Codegeist
  should avoid copying.
- mini-SWE-agent: minimal model-plus-environment loop, linear message history,
  actions, observations, and stop conditions.

## Executive Recommendations For T007_05

| Decision area | Recommendation | Evidence |
| --- | --- | --- |
| Loop owner | Add a Codegeist-owned synchronous loop around provider calls and tool dispatch. Do not rely on hidden Spring AI internal tool execution for the observable loop. | OpenCode, Pi, and mini-SWE-agent keep the application responsible for dispatching tools and appending results before continuing. Spring AI Agent Utils mostly delegates continuation to Spring AI advisors and has no project-owned loop. |
| First loop shape | Implement one blocking loop: model request, inspect assistant tool calls, execute callbacks through `CodegeistToolRun`, append tool results to model history, call the model again, stop on final assistant text. | Pi and mini-SWE-agent show that a small linear loop is enough for a first useful coding-agent controller. |
| Request contract | Keep `CodegeistChatRequest` limited to `model` and `prompt`. Add a separate internal continuation request or loop state for provider-facing message history if needed. | Current Codegeist rules require the request record to stay small; third-party projects separate user input from runtime history and tool state. |
| Tool execution boundary | Reuse the existing prompt-scoped `CodegeistToolRun` callbacks and recorder. The loop should find callbacks by tool name, call them itself, and let existing wrappers record `ToolSessionPart` values. | OpenCode and Pi expose tool schemas but execute through app-owned tool definitions. Codegeist already has a recording callback boundary. |
| Continuation history | Feed the second provider call with user prompt, assistant tool-call message, and tool-result message in order. Use Spring AI `AssistantMessage.ToolCall` and `ToolResponseMessage` where they fit, but hide provider-specific details inside chat/provider classes. | OpenCode, Pi, and mini-SWE-agent all replay assistant tool calls plus matching tool results before final assistant text. |
| Persistence | For this slice, keep persistence through `SessionStoreService.saveExchangeToCurrentSession(...)`: user text, recorded tool parts, final assistant text. Do not add new persisted fields unless a failing test proves they are needed. | T007 scope says use existing exchange path unless tests force more granular persistence. OpenCode and Pi have richer persisted part schemas, but Codegeist does not need them yet. |
| Output bounds | Keep output bounding at the tool callback boundary. The loop should pass the same bounded string returned by callbacks back to the model. | OpenCode, Pi, mini-SWE-agent, and Agent Utils all bound model-visible output near tool execution. |
| Safety guard | Add a small max tool-round guard in the Codegeist loop. | Aider has `max_reflections`; mini-SWE-agent has step/cost/time/format-error limits; Pi lacks an obvious hard core-loop cap, so Codegeist should not repeat that gap. |
| Deferred scope | Defer streaming, TUI events, permission prompts, approval flows, subagents, memory, provider-specific thinking signatures, parallel tool calls, compaction, git automation, and server/runtime APIs. | All projects show these as broader product features, not necessary for the first T007_05 loop. |

## Cross-Project Comparison

| Project | Loop style | Tool-call representation | Tool result continuation | Persistence | Strongest lesson |
| --- | --- | --- | --- | --- | --- |
| OpenCode | Full session loop with processor, streaming events, tool parts, permissions, and persistence. | Persisted assistant `tool` parts with call ids and states. | Replays assistant tool-call plus `tool` result message into the next model request. | Session, message, part, and event tables. | Keep side effects and tool-result replay application-owned, but defer Effect/event/permission complexity. |
| Pi | Small stateful TypeScript loop over `AgentMessage[]`. | Assistant content block `toolCall { id, name, arguments }`. | Appends `role: "toolResult"` message with matching id, then calls the model again. | JSONL session entries in the coding-agent app. | A minimal owned loop can stay linear and readable. |
| mini-SWE-agent | Minimal model-plus-environment loop over plain message dictionaries. | Provider tool call parsed into `extra.actions`. | Appends chat `role: "tool"` or Responses API `function_call_output`, then continues. | Serialized trajectory with messages, configs, model stats, raw outputs. | Smallest useful loop: append assistant, execute action, append observation, repeat with limits. |
| Aider | Reflection loop over assistant text, edits, commands, tests, and user-style observations. | Mostly prompt/text edit blocks; deprecated function-calling coders exist. | Adds observations as ordinary user messages, not typed provider tool messages. | Workspace/git changes, optional chat and LLM history logs. | Use loop caps and app-owned side effects; do not copy prompt-only tool contracts for T007_05. |
| Spring AI Agent Utils | Tool-rich Java examples using Spring AI `ChatClient`, `ToolCallAdvisor`, `@Tool`, and callbacks. | Spring AI `ToolCallback`, `AssistantMessage.getToolCalls()`, `ToolResponseMessage`. | Delegated to Spring AI advisor/tool-calling behavior. | Mostly runtime-only examples; some tools persist their own domain data. | Useful Java tool/callback patterns, but not a Codegeist-owned control-loop blueprint. |

## Answers By Base Question

### 1. Agent Control Loop

OpenCode uses a session-level loop in `packages/opencode/src/session/prompt.ts`.
It creates user and assistant messages, resolves tools, calls the model through
`SessionProcessor.process(...)`, records tool parts while streaming, and loops
again when the assistant turn finishes with tool calls. Supporting code lives in
`packages/opencode/src/session/processor.ts`, `packages/opencode/src/session/llm.ts`,
and `packages/opencode/src/session/tools.ts`.

Pi's loop lives in `packages/agent/src/agent-loop.ts`. It appends user messages,
calls the provider with tool schemas, receives assistant content blocks, dispatches
tool calls in the app, appends `toolResult` messages, and loops until there are no
tool calls or queued messages. The stateful wrapper is in
`packages/agent/src/agent.ts` and the coding-agent session integration is in
`packages/coding-agent/src/core/agent-session.ts`.

mini-SWE-agent keeps the loop smallest. `src/minisweagent/agents/default.py`
maintains a linear `messages` list, calls `model.query(messages)`, parses
assistant actions from `message["extra"]["actions"]`, runs each action through the
environment, appends observation messages, and repeats until an `exit` message or
configured limit stops execution.

Aider's active loop in `aider/coders/base_coder.py` is a reflection loop rather
than a typed provider tool-call loop. It sends user messages, receives assistant
text, parses edits or shell/test suggestions, executes side effects in Aider, and
feeds failures or observations back as user-style messages until no reflection is
needed or `max_reflections` is reached.

Spring AI Agent Utils does not provide its own agent-loop core. Its examples build
Spring AI `ChatClient` instances with `ToolCallAdvisor`, `MessageWindowChatMemory`,
`@Tool` methods, or `FunctionToolCallback` values, then delegate recursive tool
continuation to Spring AI. Relevant files include
`examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`
and `MyLoggingAdvisor.java`.

### 2. Runtime Data And Persistence

OpenCode has the richest persisted model: `SessionV1.User`, `SessionV1.Assistant`,
and `SessionV1.Part` values, including tool parts with `pending`, `running`,
`completed`, and `error` states in `packages/core/src/v1/session.ts`. Tables and
projectors live under `packages/core/src/session/sql.ts` and
`packages/core/src/session/projector.ts`. Runtime-only state includes active stream
buffers, current tool-call maps, abort controllers, and processor context.

Pi uses typed in-memory messages such as `UserMessage`, `AssistantMessage`,
`ToolCall`, and `ToolResultMessage` from `packages/ai/src/types.ts` and
`packages/agent/src/types.ts`. The coding-agent app persists JSONL session entries
through `packages/coding-agent/src/core/session-manager.ts`; streaming fragments,
abort controllers, queues, and active execution progress are runtime-only.

mini-SWE-agent uses plain dictionaries for model history. Assistant messages carry
provider data plus `extra.actions`; observations carry `raw_output`, `returncode`,
and related metadata. `DefaultAgent.serialize()` persists messages, configs, model
stats, exit status, and raw output, while live model/environment objects and timers
remain runtime-only.

Aider uses OpenAI-style message dictionaries in `cur_messages`, `done_messages`,
and `ChatChunks`. It persists workspace changes, optional markdown chat history,
and optional LLM logs, but not durable structured tool-call/result records like
Codegeist `ToolSessionPart`.

Spring AI Agent Utils uses Spring AI request/response structures such as
`Prompt`, `Message`, `AssistantMessage.getToolCalls()`, `ToolResponseMessage`, and
`.call().content()`. Example state is mostly runtime-only. Some individual tools,
such as memory tools, persist domain data, while background task state defaults to
runtime maps.

### 3. Tool Exposure And Execution Ownership

OpenCode exposes tool descriptions and schemas to the model through
`packages/opencode/src/session/tools.ts`, but the `execute` closure calls
OpenCode-owned `Tool.Def.execute(...)` with session id, message id, call id,
permission callbacks, abort signal, and message history. MCP tools are adapted into
the same application-owned boundary.

Pi exposes `name`, `description`, and `parameters` through provider context, then
looks up active `AgentTool` values and calls `tool.execute(...)` itself.
Extension wrappers add context but do not transfer execution ownership to the
provider.

mini-SWE-agent exposes a bash tool schema through LiteLLM in
`src/minisweagent/models/litellm_model.py`, but provider output is parsed into
actions and executed by `DefaultAgent.execute_actions()` through an environment.

Aider mostly exposes tool-like behavior through prompts: edit formats, shell
blocks, lint/test instructions, and repository context. The app parses and applies
these outputs itself. Deprecated function-calling coders can expose schemas, but
execution is still Aider-owned after parsing.

Spring AI Agent Utils exposes Java tools through `@Tool`, `MethodToolCallback`, and
`FunctionToolCallback`. The implementations are local Java code, but the loop that
decides when to call and continue is generally Spring AI's advisor machinery, not a
project-owned dispatcher.

### 4. Tool Results In Model Continuation

OpenCode converts persisted parts back into normalized model messages in
`packages/opencode/src/session/message-v2.ts`. A completed tool part becomes an
assistant `tool-call` content item followed by a `tool` role message containing a
matching `tool-result`. Failed tools become error tool results.

Pi appends a `role: "toolResult"` message with `toolCallId`, `toolName`, bounded
content, details, and `isError`. Provider adapters lower this internal shape to
provider-specific payloads, such as Google `functionCall` and `functionResponse`
parts.

mini-SWE-agent appends either chat-completions style messages with
`role: "tool"`, `tool_call_id`, and `content`, or Responses API items with
`type: "function_call_output"` and `call_id`. The next request strips runtime
`extra` values and sends the linear history back to the provider.

Aider feeds observations back as ordinary user messages, for example command or
test output plus an assistant `Ok.` acknowledgement. This is useful reflection
evidence but not the typed provider tool-result shape Codegeist needs.

Spring AI Agent Utils fixtures show Anthropic-style `tool_use` followed by
`tool_result` and then assistant continuation. In Spring AI terms, the equivalent
carrier is `ToolResponseMessage`, as observed by `MyLoggingAdvisor.java`.

### 5. Continue, Stop, Failure, Multiple Tools, And Loop Guards

OpenCode continues when an assistant finish indicates tool calls or tool parts are
still unresolved. It stops when the assistant is finished without unresolved tool
calls. It records failed tools as error tool parts, can handle multiple calls by
call id, detects repeated doom-loop tool calls, and uses agent step limits.

Pi continues when assistant content contains tool calls or queued follow-up
messages exist. It stops on no tools, abort/error, `shouldStopAfterTurn`, or a
batch where every tool result asks to terminate. It can run multiple tools
sequentially or in parallel while persisting results in source order. The source did
not show a hard global max-turn counter, so Codegeist should add one.

mini-SWE-agent continues while the newest message is not `role == "exit"`. It stops
on submission sentinel, step limit, cost limit, wall-time limit, or repeated format
errors. Multiple actions execute sequentially. Non-zero command exits and execution
exceptions become observations returned to the model.

Aider continues only while a reflection message exists and stops after
`max_reflections`, which defaults to three. Multiple edit blocks can apply in one
batch. Multiple shell commands can be collected, but active provider tool-call
multiplicity is not its main loop model.

Spring AI Agent Utils delegates continue/stop decisions to Spring AI advisors. The
project evidence does not show a project-owned max-iteration guard, so Codegeist
should not rely on it for T007_05.

### 6. Output Bounds, Recording, And Rendering

OpenCode bounds tool output through `packages/opencode/src/tool/truncate.ts` and
`packages/opencode/src/tool/tool.ts`, with line and byte caps plus optional full
output side files. Completed parts persist bounded output, title, metadata, and
attachments.

Pi bounds output in individual tools. The `read` tool truncates long text; `bash`
uses `OutputAccumulator` to keep bounded tail output and can persist full output to
a temp file. Tool definitions can provide renderers for UI display.

mini-SWE-agent renders observations through Jinja templates. Configs such as
`src/minisweagent/config/mini.yaml` and `default.yaml` keep model-visible output to
head/tail summaries around a fixed character limit while preserving raw output in
trajectory metadata.

Aider has token checks and chat-history summarization, but generic command-output
truncation is weaker than Codegeist needs. Test and shell observations can be added
to chat mostly as raw strings.

Spring AI Agent Utils bounds output per tool. Examples include file read line caps,
shell output character caps, grep depth/result caps, and web-fetch truncation.

### 7. Focused Tests Found Upstream

OpenCode has the closest tests for T007_05: `packages/opencode/test/session/prompt.test.ts`
for loop continuation after tool calls, `packages/opencode/test/session/processor.test.ts`
for tool execution, and `packages/opencode/test/session/message-v2.test.ts` for
tool-call/tool-result replay and error conversion.

Pi has direct loop tests in `packages/agent/test/agent-loop.test.ts`, including
tool calls and results, ordered result persistence, queued message injection,
prepared next-turn snapshots, stop-after-turn behavior, and termination after tool
batches. `packages/agent/test/e2e.test.ts` also covers tool execution and pending
tool-call tracking.

mini-SWE-agent has useful minimal tests in `tests/agents/test_default.py` for
successful completion, message history ordering, step-added messages, and captured
observations. It also tests tool schema passing and result formatting under
`tests/models/`.

Aider does not have a focused typed tool-call continuation test. Its useful tests
prove shell suggestion extraction, command/test output feedback, function-schema
passing, and architect/editor reflection behavior.

Spring AI Agent Utils also does not have a project-owned two-turn loop test.
Relevant tests cover advisor tool injection, task callbacks, task output/status
formatting, question validation, and individual tool bounds.

### 8. What Codegeist Should Copy, Adapt, Reject, Or Defer

Copy conceptually:

- Application-owned loop and tool dispatch.
- Explicit assistant tool-call plus matching tool-result continuation history.
- Stable tool-call ids in provider-facing history when Spring AI exposes them.
- Output bounding before model continuation and persistence.
- Loop guards and deterministic tests for two model turns.
- Ordered persistence of tool activity before final assistant text.

Adapt for Java/Spring:

- Keep `CodegeistChatRequest` as the public one-prompt request and add internal
  loop/provider-turn state only where needed.
- Use Spring AI message types at the provider boundary, not as persisted session
  schema.
- Reuse existing `CodegeistToolRun` and `ToolCallback` wrappers as the dispatch and
  recording path.
- Add a Spring `CodegeistAgentLoopService` under `ai.codegeist.app.chat` that
  produces `CodegeistChatResponse` for `ChatHarnessService`.

Reject for this slice:

- Hidden provider/framework-owned tool execution as the observable loop.
- Aider's prompt-only tool contracts as the primary Codegeist path.
- OpenCode's Effect runtime, server, SQLite migrations, permission loop, event bus,
  TUI streaming, and provider-native runtime details.
- Pi's extension runtime and parallel tool execution.
- mini-SWE-agent's bash-only abstraction and sentinel-command final answer.
- Spring AI Agent Utils' broad file/shell/network tools as direct side-effecting
  runtime dependencies.

Defer:

- TUI event projection, streaming tokens, cancellation, human approval prompts,
  permissions, subagents, memory, skills, compaction, provider-specific thinking
  signatures, image outputs, background workers, git automation, and server/API
  surfaces.

## Project-Specific Follow-Up Answers

### OpenCode

OpenCode separates session processing, provider calls, tool execution,
permissions, event emission, and persistence into distinct runtime layers. The
session loop creates messages and parts, the processor streams provider events,
tools execute through OpenCode-owned definitions, permission checks happen inside
tool context, and projected events persist message/part state. For Codegeist
T007_05, the essential part is the observable model/tool/model lifecycle and the
conversion of completed tool parts into continuation messages. Permission prompts,
event streaming, server runtime, SQLite/event projection, compaction, and TUI
rendering should stay deferred.

Key source paths: `packages/opencode/src/session/prompt.ts`,
`packages/opencode/src/session/processor.ts`,
`packages/opencode/src/session/tools.ts`,
`packages/opencode/src/session/message-v2.ts`,
`packages/core/src/v1/session.ts`.

### Spring AI Agent Utils

Spring AI Agent Utils is useful for Java tool/callback patterns, but it is not a
Codegeist-owned loop blueprint. It shows how to expose local Java functions as
Spring AI tools through `@Tool`, `MethodToolCallbackProvider`, and
`FunctionToolCallback`, and how advisors can inject tools into `ChatClient`
requests. Its examples generally delegate recursive tool calling to Spring AI
`ToolCallAdvisor` rather than proving project-owned dispatch, persistence, and loop
guards. Codegeist should implement a smaller loop and may use Spring AI message and
tool abstractions privately. If Spring AI core `ToolCallingManager` behavior is
reused, wrap it so Codegeist still owns loop policy, result mapping, bounded
recording, and session persistence.

Key source paths: `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
`examples/code-agent-demo/src/main/java/org/springaicommunity/agent/MyLoggingAdvisor.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/advisors/AutoMemoryToolsAdvisor.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/TaskTool.java`.

### Pi

Pi gives the cleanest minimal owned-loop reference. Its loop keeps provider tools
as schema only, dispatches tool calls through application-owned `AgentTool`
objects, appends `toolResult` messages, and calls the model again with a linear
history. It also demonstrates preserving source order for results even when tool
execution can complete out of order. Codegeist should copy the linear turn shape
and internal/provider-message split, but defer Pi's extension runtime, renderers,
branch/session-tree features, and parallel execution.

Focused translation notes and diagrams for this project live in
`pi-agent-loop.md`.

Key source paths: `packages/agent/src/agent-loop.ts`,
`packages/agent/src/types.ts`, `packages/ai/src/types.ts`,
`packages/coding-agent/src/core/agent-session.ts`,
`packages/coding-agent/src/core/session-manager.ts`.

### Aider

Aider is most useful as a caution and reflection-loop reference. It owns file
edits, shell commands, lint/test runs, and git side effects in the app, then feeds
observations back into later model turns. However, it mostly relies on assistant
text formats and ordinary user messages rather than typed tool-call/tool-result
history. Codegeist should copy app-owned side effects and a max reflection/tool
loop cap, but should avoid Aider's prompt-only tool contracts, deprecated
function-calling coders, unstructured persistence, and broad edit/git/test
workflow scope for T007_05.

Focused translation notes and inline diagrams for this project live in
`aider-agent-loop.md`.

Key source paths: `aider/coders/base_coder.py`,
`aider/coders/chat_chunks.py`, `aider/models.py`,
`aider/coders/editblock_coder.py`, `aider/coders/shell.py`,
`aider/commands.py`.

### mini-SWE-agent

mini-SWE-agent shows the smallest practical loop: linear messages, one model, one
environment, one action dispatch path, observations appended to history, and
explicit limits. The closest Codegeist equivalent is a loop that appends a user
prompt, asks the model, dispatches named Codegeist tools, appends bounded tool
result messages, and repeats until there are no tool calls or a max tool-round cap
is hit. Codegeist should not copy the bash-only abstraction, sentinel command final
answer, raw trajectory persistence, or Python exception-as-normal-control-flow
style.

Key source paths: `src/minisweagent/agents/default.py`,
`src/minisweagent/models/litellm_model.py`,
`src/minisweagent/models/utils/actions_toolcall.py`,
`src/minisweagent/environments/local.py`,
`src/minisweagent/config/mini.yaml`.

## Suggested Codegeist Tests

- `CodegeistAgentLoopServiceTest` should prove one fake model turn requests a fake
  tool, the loop dispatches through a `ToolCallback`, and a second fake model turn
  receives a matching tool result before returning final assistant text.
- The same test class should prove the model-visible continuation order:
  user message, assistant tool-call message, tool-result message.
- It should prove failed tool callback output is represented as a model-visible
  bounded tool result and a failed `ToolSessionPart` if the existing callback
  boundary returns that shape.
- It should prove a repeated tool-call loop stops at a small max tool-round guard.
- `ChatHarnessServiceTest` should prove the harness opens and closes one
  `CodegeistToolRun`, delegates through the loop, and saves recorded tool parts
  before final assistant text.
- `CodegeistChatServiceTest` should keep proving `CodegeistChatRequest` has only
  `model` and `prompt`, while any internal continuation request remains separate.
- `AskCommandsSessionStoreTest` should keep proving stdout contains final assistant
  response text only.

Candidate focused command from `app/codegeist/cli`:

```bash
task test TEST=CodegeistAgentLoopServiceTest,ChatHarnessServiceTest,CodegeistChatServiceTest,AskCommandsSessionStoreTest,SessionStoreServiceTest
```

## Implementation Sketch

The first Codegeist loop can stay small:

```text
modelHistory = [user(prompt)]
for round in 1..maxToolRounds:
  assistant = chatModel.call(model, modelHistory, toolDefinitions)
  if assistant has no tool calls:
    return final assistant text

  modelHistory.add(assistant tool-call message)
  for toolCall in assistant.toolCalls in source order:
    callback = find callback by toolCall.name
    output = callback.call(toolCall.arguments)
    modelHistory.add(tool result message linked to toolCall.id)

fail if maxToolRounds is exhausted
```

Keep this as synchronous application logic. Streaming events, cancellation,
permission prompts, TUI projections, and more granular persisted tool-call records
belong in later tasks.

## Caveats

- OpenCode and Pi are the strongest typed-loop references. Aider and
  mini-SWE-agent are useful for loop caps, reflection, and minimalism, but their
  tool semantics differ from Codegeist's current Spring AI callback path.
- Spring AI Agent Utils is Java-relevant but mostly advisor-driven; it should not
  replace Codegeist's need for an owned loop in T007_05.
- Third-party source behavior was not run locally in this pass. Treat exact
  runtime timing, provider quirks, and UI rendering claims as static-analysis
  evidence until verified directly.
