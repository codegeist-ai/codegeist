# T007_06 Wire Spring AI Tool Calling Through Codegeist Policy

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Connect model tool-calling to Codegeist-owned tool policy.

The purpose is to let a provider/model request ask for tools while ensuring every
tool call enters Codegeist's runtime, mode, permission, workspace, event, and result
mapping boundaries before any implementation detail runs.

## Dependencies

- Requires `T007_02` runtime/session/event spine.
- Requires `T007_04` tool registry or equivalent tool service.
- Requires enough `T007_05` permission/workspace policy to prevent side-effecting
  tool execution from bypassing approval.

## Spring AI Evidence To Apply

Spring AI exposes tool calling through `ToolCallback`, `toolCallbacks(...)`, and
`@Tool`-annotated objects. `ToolCallback` includes tool definition, metadata, and
`call(String input)` or `call(String input, ToolContext context)` methods. A
`ChatClient` request can register runtime tool callbacks for a prompt.

Policy implication:

```text
Spring AI provider/model
  -> Codegeist-owned ToolCallback
  -> Codegeist runtime/tool service
  -> mode and permission policy
  -> workspace validation
  -> optional Agent Utils or other implementation detail
  -> Codegeist result/event/session mapping
```

Do not pass broad raw Agent Utils tool objects directly to the provider.

## OpenCode Evidence To Translate

- `docs/third-party/opencode/source/packages/opencode/src/session/prompt.ts` for
  resolving available tools for a prompt.
- `docs/third-party/opencode/source/packages/opencode/src/session/processor.ts` for
  processing tool-call parts and tool completion/failure.
- `docs/third-party/opencode/source/packages/opencode/src/session/llm.ts` for LLM
  request construction and provider options.
- `docs/third-party/opencode/source/packages/opencode/src/tool/registry.ts` for tool
  lookup and availability.
- `docs/third-party/opencode/source/packages/opencode/src/permission/index.ts` for
  approval flow before side effects.

## Scope

- Decide whether the current `CodegeistChatModel` path should stay direct or be
  augmented with `ChatClient` for tool calling. Use the smallest change that can be
  proven with a focused test.
- Expose only implemented read-only tools first unless a side-effect gate test is
  explicitly part of the task.
- Map Codegeist tool descriptors to Spring AI tool definitions or callbacks.
- Map provider tool-call input into Codegeist tool request data and map Codegeist
  tool result/failure back to model-visible text.
- Emit runtime events around model tool-call request, tool start, tool result,
  tool failure, and denied tool request.

## Acceptance Criteria

- A focused test proves a model/tool request enters a Codegeist-owned callback or
  service, not a raw Agent Utils tool.
- Read-only tool calling can execute through Codegeist workspace and result policy.
- Side-effecting tool calls are denied or approval-gated according to `T007_05`.
- Tool results are bounded before they are sent back to the model or rendered by a
  client.
- The provider-neutral chat boundary remains Codegeist-owned; provider-specific
  Spring AI imports stay isolated where possible.
- Architecture docs describe the implemented Spring AI tool-calling path.

## Non-Goals

- Do not expose every Agent Utils tool to the model.
- Do not implement patch/edit, shell, network, MCP, plugin, skills, memory, or
  subagent tool calling unless a focused prerequisite task has already implemented
  the policy and tool behavior.
- Do not add provider-specific tool-calling branches for every provider.
- Do not treat model output as permission to run a tool without policy mediation.

## Suggested Tests

- Descriptor-to-callback mapping for one read-only tool.
- Tool callback rejects unknown tool input or unsafe workspace target.
- Tool callback returns a bounded result and emits a runtime event.
- Side-effect-capable tool is denied or approval-gated.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<spring-ai-tool-callback-test-selector>
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=<local-tool-calling-selector>
task test
```
