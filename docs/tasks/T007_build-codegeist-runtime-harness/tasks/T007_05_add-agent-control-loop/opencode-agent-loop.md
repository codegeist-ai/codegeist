# OpenCode Agent Loop Notes For T007_05

Focused source-backed notes for translating OpenCode's model/tool/model loop into
Codegeist's first synchronous Java/Spring control loop.

## Scope And Evidence

- Task: `task.md`.
- Broader third-party comparison: `ask-project-research.md`.
- Source workspace: `docs/third-party/opencode/`.
- Evidence type: static source analysis from the local OpenCode analysis workspace
  and packed source. No OpenCode runtime session or upstream test suite was run for
  this pass.

## Key OpenCode Files

| File | Role |
| --- | --- |
| `packages/opencode/src/session/prompt.ts` | Owns the outer session prompt flow and repeat loop. It creates user/assistant messages, resolves tools, invokes the processor, and decides whether another model turn is needed. |
| `packages/opencode/src/session/processor.ts` | Processes provider stream events and records text, reasoning, step, and tool parts while a model turn is active. |
| `packages/opencode/src/session/llm.ts` | Runs provider requests and returns normalized LLM events. |
| `packages/opencode/src/session/tools.ts` | Resolves OpenCode and MCP tools, exposes schemas to the model, and routes execution through OpenCode-owned tool definitions. |
| `packages/opencode/src/session/message-v2.ts` | Converts persisted session messages and parts into provider-facing model history, including assistant tool-call plus tool-result pairs. |
| `packages/core/src/v1/session.ts` | Defines persisted session messages and parts, including tool part states. |
| `packages/core/src/session/sql.ts` and `packages/core/src/session/projector.ts` | Store and project session, message, part, and event state. |
| `packages/opencode/src/tool/tool.ts` and `packages/opencode/src/tool/truncate.ts` | Define tool contracts and output truncation behavior. |

Useful tests:

- `packages/opencode/test/session/prompt.test.ts` - loop continuation after tool
  calls.
- `packages/opencode/test/session/processor.test.ts` - tool execution and recorded
  processor behavior.
- `packages/opencode/test/session/message-v2.test.ts` - conversion of tool parts
  into model continuation messages.

## Runtime Flow

OpenCode's observable flow is:

```text
user prompt
-> persist user message and parts
-> create assistant message
-> resolve available tools
-> send model request with history and tool schemas
-> receive assistant text and/or tool-call requests
-> execute selected tools through OpenCode-owned tool definitions
-> persist tool parts with call ids and completed/error state
-> rebuild model history with assistant tool-call plus tool-result messages
-> continue model request
-> stop when the assistant has final text and no unresolved tool calls
```

The important part for Codegeist is the boundary: the model sees tool schemas and
asks for tool calls, but OpenCode owns the side effect, output bounding,
permission checks, recording, and replay of the result.

## Tool Ownership Boundary

OpenCode exposes tools to providers as schemas and descriptions. Tool execution
still goes through OpenCode's runtime:

```text
model emits tool call
-> AI SDK/native adapter invokes OpenCode execute closure
-> Tool.Def.execute(args, context)
-> optional permission check and abort signal handling
-> side effect or query
-> bounded result or error
-> persisted tool part
-> tool result returned to the next model turn
```

The execution context includes session and message identifiers, call id, abort
signal, permission callback, and message history. Codegeist does not need all of
that in T007_05, but it should preserve the same ownership split: provider models
request tools; Codegeist dispatches and records them.

## Continuation Shape

OpenCode's `message-v2` conversion turns stored tool activity back into model
history. The normalized shape is effectively:

```text
user: "run tool"
assistant: tool-call(call-1, toolName, input)
tool: tool-result(call-1, output or error)
assistant: final response text
```

This is the core behavior Codegeist needs. The tool result is not only persisted
for later display; it is included in the next provider request so the model can
continue from the side effect it requested.

## Continue And Stop Rules

OpenCode continues when an assistant turn produced tool calls or unresolved tool
parts. It stops when the assistant has a final response and no unresolved tool
calls. It can handle several tool calls in one turn by tracking call ids. Failed
tools are recorded and replayed as error tool results so the model can recover.

OpenCode also includes broader safety and product behavior:

- repeated-tool-call doom-loop detection,
- agent step limits,
- permission requests,
- cancellation and abort signals,
- event emission for streaming UIs,
- compaction and old-output clearing.

T007_05 should take only the small loop guard idea now, not the full permission or
event architecture.

## Output Bounds

OpenCode bounds tool output before it reaches model-visible results or persisted
parts. Large outputs can be truncated with full output saved separately. The model
gets a bounded preview and metadata can record truncation.

Codegeist already has a simpler boundary: local and MCP callbacks return bounded
strings and record bounded `ToolSessionPart.outputPreview`. T007_05 should reuse
that behavior and pass the same bounded callback result into the continuation
message.

## Codegeist Translation

For T007_05, implement the smallest synchronous translation:

```text
ChatHarnessService
-> opens CodegeistToolRun
-> CodegeistAgentLoopService starts model history with user(prompt)
-> model call returns assistant text or tool calls
-> if tool calls exist, find matching ToolCallback by name
-> callback.call(arguments) runs through Codegeist-owned recording wrappers
-> append ToolResponseMessage or equivalent internal tool-result message
-> call model again
-> return final CodegeistChatResponse
-> ChatHarnessService persists prompt, recorded ToolSessionPart values, final text
```

Keep `CodegeistChatRequest` limited to `model` and `prompt`. Add a separate
internal continuation request or message-history input if the provider-facing call
needs more than a prompt string.

## What To Copy

- Application-owned dispatch and persistence.
- Assistant tool-call plus tool-result replay in the next model request.
- Stable call ids when available from Spring AI `AssistantMessage.ToolCall`.
- Failed tool results returned to the model as bounded error text.
- A small max tool-round guard.

## What To Defer

- OpenCode's Effect runtime and fibers.
- Server runtime and event streaming.
- SQLite/event projection model.
- Permission prompts and policy engine.
- Subagents, memory, skills, repo-map, and git automation.
- Compaction and tool-output side files.
- TUI-specific streaming state.

## Test Implications

Codegeist tests should prove what OpenCode's session tests prove at a smaller
scope:

- A fake model asks for one tool, Codegeist dispatches it, and a second model turn
  receives the tool result before final text.
- The second model request contains user message, assistant tool-call message, and
  matching tool-result message in order.
- The existing `CodegeistToolRun` recorder persists `ToolSessionPart` values before
  final assistant text.
- Failed tool output is model-visible as a bounded result.
- Repeated tool-call cycles stop at the Codegeist max-round guard.
