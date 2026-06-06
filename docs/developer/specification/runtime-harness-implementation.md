# Runtime Harness Implementation Specification

Planned implementation specification for the Codegeist-owned runtime harness.

## Purpose

This document defines how the T007 runtime harness should be implemented across
small tested slices. It translates the source-backed OpenCode and Spring AI Agent
Utils analysis into Codegeist-owned Java/Spring boundaries without copying
OpenCode package structure, storage schemas, TypeScript models, or UI framework
choices.

This is planned architecture, not current-state architecture. Current implemented
state stays in `docs/developer/architecture/architecture.md`.

## Sources

- `docs/tasks/T007_build-codegeist-runtime-harness/task.md` defines the epic,
  child order, acceptance criteria, and non-goals.
- `docs/tasks/T007_build-codegeist-runtime-harness/tasks/T007_01_analyze-opencode-runtime-and-agent-utils-harness.md`
  contains the source-backed OpenCode and Spring AI Agent Utils evidence.
- `docs/developer/specification/runtime-vocabulary.md` defines the vocabulary and
  ownership direction.
- `docs/developer/specification/java-generation-guidance.md` remains the guardrail
  for adding only source required by focused tests.
- `docs/developer/specification/llm-provider-implementation.md` defines the
  existing provider-neutral chat seam that the runtime must reuse.

## Implementation Rules

- Implement one T007 child at a time. Do not create future package trees or empty
  placeholder classes before the child test needs them.
- Start with a focused failing test when practical, then add the smallest Spring
  service, record, enum, or adapter needed to pass it.
- Keep runtime orchestration in Codegeist-owned services. CLI, TUI, server, and
  later Vaadin/API clients submit input and render events only.
- Reuse `CodegeistConfig.defaultProvider()`, `ProviderConfig.defaultModel()`,
  `CodegeistChatService`, `CodegeistChatRequest`, and `CodegeistChatResponse` for
  provider calls. Do not add a second provider path.
- Register tools with providers only through Codegeist-owned callbacks. Never pass
  raw Agent Utils `@Tool` objects, raw Agent Utils `ToolCallback`s, raw MCP tools,
  or raw shell/file mutation helpers directly to Spring AI providers.
- Put mode, permission, workspace, result bounding, and event mapping between a
  model/tool request and every side effect.
- Keep durable storage deferred until runtime events and tool results have a real
  tested shape.
- Update `docs/developer/architecture/architecture.md` in the implementation task
  that adds actual packages, classes, configuration, runtime flows, or tests.

## Slice Order

| Slice | Implementation focus | Must not add |
| --- | --- | --- |
| `T007_02` | Runtime prompt service, turn identity if needed, typed in-process events, provider-backed final result. | Tools, permissions, TUI, storage, streaming deltas unless tested. |
| `T007_03` | Terminal client over runtime events, deterministic renderer, line-oriented fallback. | Second runtime, provider/tool direct calls, polished full-screen parity. |
| `T007_04` | Codegeist tool descriptors, registry/lookup, workspace boundary, read/list/glob/grep results. | Write/edit/shell/network/MCP/plugin/subagent tools. |
| `T007_05` | Mode policy, approval request/reply events, side-effect gates, post-approval workspace validation. | Persistent wildcard approvals, server auth, side-effect tool implementations except test fakes. |
| `T007_06` | Spring AI tool calling through Codegeist-owned callbacks and tool service. | Raw Agent Utils registration, broad provider-specific callback branches. |
| `T007_07` | First controlled patch/edit or shell tool after policy is proven. | Unrestricted shell, network/MCP/plugin/JBang/PF4J/LSP/subagent tools. |
| `T007_08` | Smallest session/event/tool-result storage and resume path. | OpenCode table parity, migration system before shipped data requires it. |

## Package Plan

These package names are the intended direction. Introduce each package only when a
focused task adds its first real class.

```mermaid
flowchart TD
    subgraph app[ai.codegeist.app]
        boot[CodegeistApplication and command bootstrap]
        config[config<br/>implemented provider config]
        chat[chat<br/>implemented provider-neutral chat]
    end

    subgraph runtime[ai.codegeist.app.runtime]
        runtimeSvc[RuntimePromptService]
        turn[Runtime turn/request/result types]
    end

    subgraph events[ai.codegeist.app.runtime.event]
        eventBus[RuntimeEventBus]
        eventTypes[RuntimeEvent types]
    end

    subgraph client[ai.codegeist.app.client.terminal]
        terminalClient[TerminalRuntimeClient]
        renderer[RuntimeEventRenderer]
    end

    subgraph tools[ai.codegeist.app.tool]
        registry[ToolRegistry]
        descriptors[ToolDescriptor and ToolResult]
        readonly[Read-only workspace tools]
    end

    subgraph workspace[ai.codegeist.app.workspace]
        boundary[WorkspaceBoundary]
        paths[WorkspacePath]
    end

    subgraph permission[ai.codegeist.app.permission]
        mode[AgentModePolicy]
        approvals[PermissionService]
    end

    subgraph provider[ai.codegeist.app.provider]
        callbacks[CodegeistToolCallbackAdapter]
    end

    subgraph storage[ai.codegeist.app.storage]
        store[SessionStore]
    end

    boot --> runtimeSvc
    runtimeSvc --> config
    runtimeSvc --> chat
    runtimeSvc --> eventBus
    terminalClient --> runtimeSvc
    terminalClient --> renderer
    renderer --> eventTypes
    registry --> descriptors
    registry --> readonly
    readonly --> workspace
    callbacks --> registry
    callbacks --> permission
    callbacks --> workspace
    callbacks --> eventBus
    runtimeSvc -. T007_08 .-> store
    store -. replay .-> eventBus
```

Dependency direction:

- `runtime` may depend on `config`, `chat`, and `runtime.event`.
- `client.terminal` may depend on `runtime` and event types, but not on provider or
  tool implementation classes.
- `tool` may depend on `workspace` for validation and on event/result types only
  where a current test requires event emission.
- `provider` adapter code may depend on `tool`, `permission`, `workspace`, and
  Spring AI callback APIs, but provider-specific imports should stay isolated.
- `storage` depends on stable runtime/event/tool-result contracts; other packages
  should not depend on a concrete storage implementation.

## Runtime Event Spine

`T007_02` wraps the existing one-shot `ask` provider path in a runtime service. The
first implementation should prefer concrete Spring services over interfaces unless
tests need a seam.

```mermaid
classDiagram
    direction LR

    class RuntimePromptService {
      <<SpringService>>
      +run(RuntimePromptRequest request) RuntimePromptResult
    }

    class RuntimePromptRequest {
      <<record>>
      String prompt
      String mode
    }

    class RuntimePromptResult {
      <<record>>
      RuntimeTurnId turnId
      String content
    }

    class RuntimeTurnId {
      <<record>>
      String value
    }

    class RuntimeEventBus {
      <<SpringComponent>>
      +publish(RuntimeEvent event) void
      +eventsFor(RuntimeTurnId turnId) List~RuntimeEvent~
    }

    class RuntimeEvent {
      <<sealed interface>>
      +turnId() RuntimeTurnId
      +type() String
    }

    class PromptAcceptedEvent {
      <<record>>
      RuntimeTurnId turnId
      String promptSummary
    }

    class TurnStartedEvent {
      <<record>>
      RuntimeTurnId turnId
      String providerId
      String model
    }

    class ProviderRequestStartedEvent {
      <<record>>
      RuntimeTurnId turnId
    }

    class AssistantCompletedEvent {
      <<record>>
      RuntimeTurnId turnId
      String content
    }

    class TurnCompletedEvent {
      <<record>>
      RuntimeTurnId turnId
    }

    class TurnFailedEvent {
      <<record>>
      RuntimeTurnId turnId
      String message
    }

    class CodegeistConfigService {
      <<existing SpringService>>
      +getCurrentConfig() CodegeistConfig
    }

    class CodegeistChatService {
      <<existing SpringService>>
      +chat(ProviderConfig providerConfig, CodegeistChatRequest request) CodegeistChatResponse
    }

    RuntimePromptService --> CodegeistConfigService
    RuntimePromptService --> CodegeistChatService
    RuntimePromptService --> RuntimeEventBus
    RuntimePromptService --> RuntimePromptRequest
    RuntimePromptService --> RuntimePromptResult
    RuntimePromptResult --> RuntimeTurnId
    RuntimeEventBus --> RuntimeEvent
    RuntimeEvent <|.. PromptAcceptedEvent
    RuntimeEvent <|.. TurnStartedEvent
    RuntimeEvent <|.. ProviderRequestStartedEvent
    RuntimeEvent <|.. AssistantCompletedEvent
    RuntimeEvent <|.. TurnCompletedEvent
    RuntimeEvent <|.. TurnFailedEvent
```

Notes:

- `mode` should be omitted from `RuntimePromptRequest` until a test requires mode
  selection. It appears in the diagram because `T007_05` needs a natural place to
  add it.
- `RuntimeTurnId` should exist only when event-order tests or clients need
  correlation. Otherwise use the smallest deterministic correlation field in the
  first slice and introduce the record later.
- Do not add token counts, cost, reasoning parts, retry state, compaction state, or
  storage ids in `T007_02`.

### Prompt Turn Flow

```mermaid
sequenceDiagram
    title One Prompt Turn Through Runtime

    participant Client as CLI or TUI client
    participant Runtime as RuntimePromptService
    participant Events as RuntimeEventBus
    participant Config as CodegeistConfigService
    participant Chat as CodegeistChatService
    participant Provider as CodegeistChatModel

    Client->>Runtime: run(prompt)
    Runtime->>Events: runtime.prompt.accepted
    Runtime->>Config: getCurrentConfig()
    Config-->>Runtime: CodegeistConfig
    Runtime->>Runtime: defaultProvider() and defaultModel()
    Runtime->>Events: runtime.turn.started
    Runtime->>Events: runtime.provider.started
    Runtime->>Chat: chat(providerConfig, request)
    Chat->>Provider: call(CodegeistChatRequest)
    Provider-->>Chat: provider response
    Chat-->>Runtime: CodegeistChatResponse
    Runtime->>Events: runtime.assistant.completed
    Runtime->>Events: runtime.turn.completed
    Runtime-->>Client: RuntimePromptResult
```

Failure flow:

```mermaid
flowchart TD
    Start[Client submits prompt] --> Accept[Publish prompt accepted]
    Accept --> Select[Select config provider and model]
    Select --> Call[Call CodegeistChatService]
    Call --> Success{Provider call succeeds?}
    Success -- yes --> Assistant[Publish assistant completed]
    Assistant --> Done[Publish turn completed and return result]
    Success -- no --> Sanitize[Map safe user-visible failure]
    Sanitize --> Failed[Publish turn failed]
    Failed --> ThrowOrReturn[Return or throw according to command contract]
```

## Terminal Client Harness

`T007_03` adds a terminal client over runtime events. The first client should be
line-oriented and deterministic before any full-screen TUI is attempted.

```mermaid
classDiagram
    direction LR

    class TerminalRuntimeClient {
      <<SpringComponent>>
      +submit(String prompt) int
    }

    class RuntimeEventRenderer {
      <<SpringComponent>>
      +render(RuntimeEvent event) String
    }

    class TerminalProjection {
      <<record or POJO>>
      RuntimeTurnId activeTurn
      List renderedLines
    }

    class RuntimePromptService {
      <<SpringService>>
      +run(RuntimePromptRequest request) RuntimePromptResult
    }

    class RuntimeEventBus {
      <<SpringComponent>>
      +eventsFor(RuntimeTurnId turnId) List~RuntimeEvent~
    }

    TerminalRuntimeClient --> RuntimePromptService
    TerminalRuntimeClient --> RuntimeEventBus
    TerminalRuntimeClient --> RuntimeEventRenderer
    RuntimeEventRenderer --> RuntimeEvent
    TerminalProjection --> RuntimeEvent
```

Renderer rules:

- Render only events that exist in the runtime event set.
- Keep stdout contracts for `--version`, `--show-config`, and one-shot `ask`
  unchanged.
- Keep approval/question renderers out of this slice until permission events exist.

## Tool Registry And Workspace Tools

`T007_04` introduces Codegeist-owned tools. Tools are not raw Java methods exposed
to a provider; they are capabilities executed through Codegeist policy and result
mapping.

```mermaid
classDiagram
    direction LR

    class ToolRegistry {
      <<SpringComponent>>
      +availableTools() List~ToolDescriptor~
      +execute(ToolRequest request) ToolExecutionResult
    }

    class CodegeistTool {
      <<interface>>
      +descriptor() ToolDescriptor
      +execute(ToolRequest request) ToolExecutionResult
    }

    class ToolDescriptor {
      <<record>>
      String id
      String description
      ToolCapability capability
      boolean readOnly
    }

    class ToolCapability {
      <<enum>>
      READ
      WRITE
      SHELL
      NETWORK
      EXTERNAL
    }

    class ToolRequest {
      <<record>>
      String toolId
      RuntimeTurnId turnId
      WorkspaceRoot workspaceRoot
      Map input
    }

    class ToolExecutionResult {
      <<sealed interface>>
      +boundedText() String
      +truncated() boolean
    }

    class ToolSuccess {
      <<record>>
      String boundedText
      boolean truncated
      Map metadata
    }

    class ToolFailure {
      <<record>>
      String message
      String code
    }

    class WorkspaceBoundary {
      <<SpringComponent>>
      +resolve(WorkspaceRoot root, String inputPath) WorkspacePath
    }

    class WorkspaceRoot {
      <<record>>
      Path path
    }

    class WorkspacePath {
      <<record>>
      Path absolutePath
      String relativePath
    }

    class WorkspaceReadTool
    class WorkspaceListTool
    class WorkspaceGlobTool
    class WorkspaceGrepTool

    ToolRegistry --> CodegeistTool
    CodegeistTool --> ToolDescriptor
    CodegeistTool --> ToolRequest
    CodegeistTool --> ToolExecutionResult
    ToolDescriptor --> ToolCapability
    ToolRequest --> WorkspaceRoot
    ToolRequest --> RuntimeTurnId
    ToolExecutionResult <|.. ToolSuccess
    ToolExecutionResult <|.. ToolFailure
    WorkspaceReadTool ..|> CodegeistTool
    WorkspaceListTool ..|> CodegeistTool
    WorkspaceGlobTool ..|> CodegeistTool
    WorkspaceGrepTool ..|> CodegeistTool
    WorkspaceReadTool --> WorkspaceBoundary
    WorkspaceListTool --> WorkspaceBoundary
    WorkspaceGlobTool --> WorkspaceBoundary
    WorkspaceGrepTool --> WorkspaceBoundary
    WorkspaceBoundary --> WorkspacePath
```

Workspace validation flow:

```mermaid
flowchart TD
    Input[Tool input path or pattern] --> Root[Load explicit workspace root]
    Root --> Normalize[Normalize user input]
    Normalize --> Canonical[Resolve canonical target when path exists]
    Canonical --> Inside{Target remains under workspace root?}
    Inside -- no --> Reject[Return workspace rejection]
    Inside -- yes --> Symlink{Symlink escapes root?}
    Symlink -- yes --> Reject
    Symlink -- no --> Execute[Run read-only implementation]
    Execute --> Bound[Apply count, line, byte, and character bounds]
    Bound --> Result[Return Codegeist-owned result]
```

Agent Utils can be used only behind this boundary. `GrepTool`, `GlobTool`,
`ListDirectoryTool`, and read-only `FileSystemTools` behavior may be implementation
details after Codegeist validates paths and normalizes output.

## Permission And Side-Effect Gates

`T007_05` adds mode and approval policy before side effects. Tool implementations
must not classify their own trust level.

```mermaid
classDiagram
    direction LR

    class AgentMode {
      <<enum>>
      PLAN
      BUILD
    }

    class ModePolicy {
      <<SpringComponent>>
      +allows(AgentMode mode, ToolCapability capability) ModeDecision
    }

    class PermissionService {
      <<SpringComponent>>
      +authorize(PermissionCheck check) PermissionDecision
      +reply(PermissionReply reply) PermissionDecision
    }

    class PermissionCheck {
      <<record>>
      RuntimeTurnId turnId
      AgentMode mode
      String toolId
      ToolCapability capability
      String targetSummary
    }

    class PermissionRequest {
      <<record>>
      String requestId
      RuntimeTurnId turnId
      String toolId
      ToolCapability capability
      String targetSummary
    }

    class PermissionReply {
      <<record>>
      String requestId
      ApprovalChoice choice
    }

    class ApprovalChoice {
      <<enum>>
      ONCE
      REJECT
    }

    class PermissionDecision {
      <<sealed interface>>
    }

    class AllowedDecision
    class ModeDeniedDecision
    class ApprovalRequiredDecision
    class RejectedDecision

    PermissionService --> ModePolicy
    PermissionService --> PermissionCheck
    PermissionService --> PermissionReply
    PermissionService --> PermissionDecision
    PermissionCheck --> AgentMode
    PermissionCheck --> ToolCapability
    PermissionReply --> ApprovalChoice
    PermissionDecision <|.. AllowedDecision
    PermissionDecision <|.. ModeDeniedDecision
    PermissionDecision <|.. ApprovalRequiredDecision
    PermissionDecision <|.. RejectedDecision
    ApprovalRequiredDecision --> PermissionRequest
```

Approval flow:

```mermaid
sequenceDiagram
    title Permission Gate Before Side Effects

    participant ToolCall as Tool request
    participant Policy as PermissionService
    participant Events as RuntimeEventBus
    participant Client as CLI/TUI client
    participant Workspace as WorkspaceBoundary
    participant Tool as Tool implementation

    ToolCall->>Policy: authorize(mode, capability, target)
    alt mode denies capability
        Policy-->>ToolCall: mode denied
        ToolCall->>Events: permission.mode_denied
    else approval required
        Policy-->>ToolCall: approval required
        ToolCall->>Events: permission.requested
        Client->>Policy: reply once or reject
        alt rejected
            Policy-->>ToolCall: rejected
            ToolCall->>Events: permission.denied
        else once approved
            Policy-->>ToolCall: allowed once
            ToolCall->>Workspace: validate target
            Workspace-->>ToolCall: safe workspace path
            ToolCall->>Tool: execute
            Tool-->>ToolCall: bounded result
        end
    else allowed by mode and policy
        Policy-->>ToolCall: allowed
        ToolCall->>Workspace: validate target
        Workspace-->>ToolCall: safe workspace path
        ToolCall->>Tool: execute
        Tool-->>ToolCall: bounded result
    end
```

Persistent `always` approvals should not be implemented before storage exists
unless a focused test explicitly adds that behavior.

## Spring AI Tool Calling Boundary

`T007_06` lets a provider request tools without bypassing Codegeist policy.

```mermaid
classDiagram
    direction LR

    class CodegeistToolCallbackFactory {
      <<SpringComponent>>
      +callbacksFor(RuntimeTurnContext context) List~ToolCallback~
    }

    class CodegeistToolCallbackAdapter {
      <<implements ToolCallback>>
      +getToolDefinition() ToolDefinition
      +getToolMetadata() ToolMetadata
      +call(String input) String
    }

    class RuntimeTurnContext {
      <<record>>
      RuntimeTurnId turnId
      AgentMode mode
      WorkspaceRoot workspaceRoot
    }

    class ToolRegistry {
      <<SpringComponent>>
      +execute(ToolRequest request) ToolExecutionResult
    }

    class PermissionService {
      <<SpringComponent>>
      +authorize(PermissionCheck check) PermissionDecision
    }

    class RuntimeEventBus {
      <<SpringComponent>>
      +publish(RuntimeEvent event) void
    }

    class ToolCallback {
      <<SpringAI>>
    }

    CodegeistToolCallbackFactory --> ToolRegistry
    CodegeistToolCallbackFactory --> RuntimeTurnContext
    CodegeistToolCallbackFactory --> CodegeistToolCallbackAdapter
    CodegeistToolCallbackAdapter ..|> ToolCallback
    CodegeistToolCallbackAdapter --> ToolRegistry
    CodegeistToolCallbackAdapter --> PermissionService
    CodegeistToolCallbackAdapter --> RuntimeEventBus
    CodegeistToolCallbackAdapter --> RuntimeTurnContext
```

Tool-calling sequence:

```mermaid
sequenceDiagram
    title Spring AI Tool Calling Through Codegeist Policy

    participant Runtime as RuntimePromptService
    participant Factory as CodegeistToolCallbackFactory
    participant Model as Spring AI provider/model
    participant Callback as CodegeistToolCallbackAdapter
    participant Policy as PermissionService
    participant Registry as ToolRegistry
    participant Events as RuntimeEventBus

    Runtime->>Factory: callbacksFor(turnContext)
    Factory-->>Runtime: request-scoped callbacks
    Runtime->>Model: prompt with callbacks
    Model->>Callback: call(toolInput)
    Callback->>Events: tool.requested
    Callback->>Policy: authorize(tool, capability, target)
    alt denied or approval rejected
        Policy-->>Callback: denied
        Callback->>Events: tool.denied
        Callback-->>Model: bounded denial text
    else allowed
        Policy-->>Callback: allowed
        Callback->>Registry: execute(ToolRequest)
        Registry-->>Callback: ToolExecutionResult
        Callback->>Events: tool.completed or tool.failed
        Callback-->>Model: bounded model-visible text
    end
```

Registration rule:

- Prefer request-scoped `toolCallbacks(...)` for the current runtime turn.
- Use builder-level `defaultToolCallbacks(...)` only if a future test proves a
  shared default is required and still policy-safe.
- Do not use raw `@Tool` object registration for Agent Utils or Codegeist tools.

## Patch/Edit And Shell Tools

`T007_07` should split patch/edit and shell if doing both makes the task too large.

```mermaid
flowchart TD
    Request[Side-effecting tool request] --> Capability[Classify capability outside implementation]
    Capability --> Mode[Mode policy]
    Mode --> ModeAllowed{Mode allows side effects?}
    ModeAllowed -- no --> Deny[Emit mode-denied event]
    ModeAllowed -- yes --> Approval[Request approval]
    Approval --> Approved{Approved once?}
    Approved -- no --> Reject[Emit denied event]
    Approved -- yes --> Workspace[Validate workspace path or cwd]
    Workspace --> Safe{Target safe?}
    Safe -- no --> RejectWorkspace[Emit workspace rejection]
    Safe -- yes --> Execute[Execute patch/edit or shell]
    Execute --> Bound[Bound output and metadata]
    Bound --> Event[Emit success or failure event]
```

Patch/edit requirements:

- Validate every path under the workspace root.
- Produce a reviewable diff or patch summary before mutation when the active task
  asks for review behavior.
- Mutate only after mode and approval allow it.

Shell requirements:

- Validate cwd under the workspace root.
- Apply timeout and output bounds.
- Avoid broad destructive-command claims unless tests define exact behavior.
- Treat Agent Utils `ShellTools` as a concept reference only until Codegeist owns
  command classification, permission, cwd, timeout, and output policy.

## Session Storage And Resume

`T007_08` persists only the state that previous slices already produce.

```mermaid
classDiagram
    direction LR

    class SessionStore {
      <<SpringComponent or interface when needed>>
      +save(StoredSession session) void
      +load(String sessionId) Optional~StoredSession~
      +appendEvent(String sessionId, StoredEvent event) void
    }

    class StoredSession {
      <<record>>
      String sessionId
      List turns
    }

    class StoredTurn {
      <<record>>
      RuntimeTurnId turnId
      String promptSummary
      String assistantSummary
      List toolResults
    }

    class StoredEvent {
      <<record>>
      RuntimeTurnId turnId
      String type
      Map safePayload
    }

    class StoredToolResult {
      <<record>>
      String toolId
      String boundedSummary
      boolean truncated
    }

    SessionStore --> StoredSession
    SessionStore --> StoredEvent
    StoredSession --> StoredTurn
    StoredTurn --> StoredToolResult
    StoredTurn --> RuntimeTurnId
    StoredEvent --> RuntimeTurnId
```

Replay flow:

```mermaid
flowchart TD
    Load[Load stored session] --> Validate[Validate storage version and safe payload]
    Validate --> Project[Project stored events into client state]
    Project --> Render[Render deterministic terminal projection]
    Render --> Resume{User resumes?}
    Resume -- no --> Done[Inspect only]
    Resume -- yes --> Runtime[Submit new prompt to runtime with session context]
    Runtime --> Append[Append new bounded events and summaries]
```

Storage must not persist provider credentials, evaluated secrets, OAuth tokens,
cloud credentials, raw unbounded prompts, or raw unbounded tool output.

## End-To-End Target Flow

The long-term T007 harness should converge on this flow after the child slices are
implemented:

```mermaid
flowchart TD
    User[User] --> Client[CLI or terminal client]
    Client --> Runtime[RuntimePromptService]
    Runtime --> Events[RuntimeEventBus]
    Runtime --> Config[CodegeistConfigService]
    Config --> ProviderConfig[Selected ProviderConfig]
    Runtime --> Chat[CodegeistChatService or tool-aware provider path]
    Chat --> Model[Spring AI provider model]
    Model --> ToolCall{Tool call?}
    ToolCall -- no --> Assistant[Assistant text]
    ToolCall -- yes --> Callback[Codegeist ToolCallback]
    Callback --> Permission[Mode and permission policy]
    Permission --> Workspace[Workspace validation]
    Workspace --> Tool[Codegeist tool implementation]
    Tool --> Result[Bounded tool result]
    Result --> Model
    Assistant --> Runtime
    Runtime --> Events
    Events --> Client
    Runtime -. later .-> Storage[SessionStore]
    Storage -. replay .-> Client
```

## Verification Matrix

Use the Taskfile from `app/codegeist/cli` for implementation verification.

| Slice | Focused verification |
| --- | --- |
| `T007_02` | `task test TEST=<runtime-event-test-selector>` and local provider selector only when the test calls Ollama. |
| `T007_03` | `task test TEST=<terminal-renderer-test-selector>`; add a smoke only if a noninteractive terminal entrypoint exists. |
| `T007_04` | `task test TEST=<workspace-tool-test-selector>` with temporary workspace fixtures. |
| `T007_05` | `task test TEST=<permission-test-selector>` for mode denial, approval, and post-approval workspace validation. |
| `T007_06` | `task test TEST=<tool-callback-test-selector>` and local provider selector only for real model tool calling. |
| `T007_07` | `task test TEST=<patch-or-shell-test-selector>`; smoke only when command runtime or packaging behavior changes. |
| `T007_08` | `task test TEST=<storage-test-selector>` with temporary storage roots. |

Local provider-backed checks must opt in explicitly:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=<focused-selector>
```

Broad verification after implementation slices:

```bash
task test
```

Documentation-only edits should at least run:

```bash
git --no-pager diff --check
```

Do not document direct `mvn test` commands for new Codegeist implementation work.

## Non-Goals

- Do not implement PF4J, JBang, Vaadin, headless server, SDK/OpenAPI, plugin
  marketplace behavior, MCP server management, LSP, skills, memory, or subagents in
  the first runtime harness slices.
- Do not copy OpenCode's Effect layers, Hono routes, OpenTUI/Solid components,
  generated SDK, SQLite tables, Drizzle migrations, or TypeScript schemas.
- Do not broaden provider coverage or call hosted providers as part of the runtime
  harness.
- Do not create security sandboxing claims beyond explicit tested mode,
  permission, workspace, timeout, and output-bound checks.
