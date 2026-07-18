# Runtime Harness Specification

Historical planning model for the completed T007 runtime harness. The diagrams
preserve the vocabulary considered while the task was being split into tested
slices; they are not a current Java API or source map.

Use `docs/developer/architecture/architecture.md` and its focused session, tool,
agent-loop, and TUI documents for implemented behavior. The final runtime uses
`.codegeist/session.json`, `ChatHarnessService`, `CodegeistAgentLoopService`,
prompt-scoped `CodegeistToolRun` callbacks, exact edit, shell, MCP, and the Spring
Shell `TerminalUI`. Planned names below that do not exist in source must not be
treated as implementation requirements.

## Diagram Layout

The class model is split into grouped vertical diagrams instead of one large graph.
Each diagram uses `direction TB` so Mermaid renders the relationships top-to-bottom
where supported.

Existing Codegeist classes that T007 reuses or changes are marked as `existing`.
New planned Java types are marked as `planned`.

## 1. Harness Entry And Existing Chat Seam

```mermaid
classDiagram
    direction TB

    namespace ExistingCommandAndChat {
      class AskCommands {
        <<existing>>
        +ask(String chatPath, String prompt) void
      }

      class CodegeistChatService {
        <<existing>>
        +chat(ProviderConfig config, CodegeistChatRequest request) CodegeistChatResponse
      }

      class CodegeistChatRequest {
        <<existing record>>
        String model
        String prompt
      }

      class CodegeistChatResponse {
        <<existing record>>
        String text
      }

      class CodegeistChatModel {
        <<existing interface>>
        +chat(ProviderConfig config, CodegeistChatRequest request) CodegeistChatResponse
      }
    }

    namespace ExistingConfig {
      class CodegeistConfigService {
        <<existing>>
        +loadCurrentConfig() CodegeistConfig
      }

      class CodegeistConfig {
        <<existing>>
        List~CodegeistConfigRootElement~ rootElements
        +rootElement(Class~T~ rootElementType) Optional~T~
        +defaultProvider() Optional~ProviderConfig~
      }

      class ProviderConfig {
        <<existing>>
        +defaultModel() String
      }
    }

    namespace PlannedHarness {
      class ChatHarnessService {
        <<planned>>
        +submit(Path chatPath, String prompt) ChatFile
        +submit(ChatFile chatFile, String prompt) ChatFile
      }
    }

    AskCommands --> ChatHarnessService
    ChatHarnessService --> CodegeistConfigService
    ChatHarnessService --> ChatFileService
    ChatHarnessService --> CodegeistChatService
    ChatHarnessService --> CodegeistToolService
    CodegeistConfigService --> CodegeistConfig
    CodegeistConfig --> ProviderConfig
    CodegeistConfig --> McpClientConfig
    CodegeistChatService --> CodegeistChatModel
    CodegeistChatService --> CodegeistChatRequest
    CodegeistChatService --> CodegeistChatResponse
```

## 2. Chat File State Model

### 2.1 Chat File Service And Aggregate

```mermaid
classDiagram
    direction TB

    class ChatFileService {
      <<planned>>
      +loadOrCreate(Path chatPath, Path workingDir) ChatFile
      +save(Path chatPath, ChatFile chatFile) void
      +appendUserMessage(ChatFile chatFile, String prompt) ChatFile
      +appendAssistantResponse(ChatFile chatFile, CodegeistChatResponse response) ChatFile
      +appendToolActivity(ChatFile chatFile, ChatToolCall call, ChatToolResult result) ChatFile
    }

    class ChatFile {
      <<planned record>>
      int schemaVersion
      UUID id
      Instant createdAt
      Instant updatedAt
      Path workingDir
      List~ChatMessage~ messages
      List~ChatToolCall~ toolCalls
      List~ChatToolResult~ toolResults
    }

    class ChatMessage {
      <<planned record>>
    }

    class ChatToolCall {
      <<planned record>>
    }

    class ChatToolResult {
      <<planned record>>
    }

    class CodegeistChatResponse {
      <<existing record>>
    }

    ChatFileService --> ChatFile
    ChatFileService --> CodegeistChatResponse
    ChatFile --> ChatMessage
    ChatFile --> ChatToolCall
    ChatFile --> ChatToolResult
```

### 2.2 Chat Messages

```mermaid
classDiagram
    direction TB

    class ChatFile {
      <<planned record>>
      List~ChatMessage~ messages
    }

    class ChatMessage {
      <<planned record>>
      UUID id
      Instant createdAt
      ChatMessageRole role
      String text
    }

    class ChatMessageRole {
      <<planned enum>>
      USER
      ASSISTANT
      TOOL
      ERROR
    }

    ChatFile --> ChatMessage
    ChatMessage --> ChatMessageRole
```

### 2.3 Tool Activity Envelope

```mermaid
classDiagram
    direction TB

    class ChatFile {
      <<planned record>>
      List~ChatToolCall~ toolCalls
      List~ChatToolResult~ toolResults
    }

    class ChatToolCall {
      <<planned record>>
      UUID id
      Instant createdAt
      String toolName
      String inputSummary
    }

    class ChatToolResult {
      <<planned record>>
      UUID id
      UUID callId
      Instant completedAt
      ChatToolStatus status
    }

    class ChatToolStatus {
      <<planned enum>>
      RUNNING
      COMPLETED
      FAILED
      TIMED_OUT
      CANCELLED
    }

    ChatFile --> ChatToolCall
    ChatFile --> ChatToolResult
    ChatToolResult --> ChatToolStatus
```

### 2.4 Bounded Tool Summaries

```mermaid
classDiagram
    direction TB

    class ChatToolResult {
      <<planned record>>
      OutputPreview output
      ChatFileSummary fileSummary
      ChatChangeSummary changeSummary
      ChatShellSummary shellSummary
      ChatErrorSummary errorSummary
    }

    class OutputPreview {
      <<planned record>>
      String text
      boolean truncated
      int originalLength
    }

    class ChatFileSummary {
      <<planned record>>
      List~Path~ paths
      int resultCount
      boolean truncated
    }

    class ChatChangeSummary {
      <<planned record>>
      List~Path~ affectedPaths
      String diffPreview
      boolean truncated
    }

    class ChatShellSummary {
      <<planned record>>
      String command
      Path cwd
      int exitCode
      Duration duration
      boolean timedOut
      OutputPreview stdout
      OutputPreview stderr
    }

    class ChatErrorSummary {
      <<planned record>>
      String message
      String detail
    }

    ChatToolResult --> OutputPreview
    ChatToolResult --> ChatFileSummary
    ChatToolResult --> ChatChangeSummary
    ChatToolResult --> ChatShellSummary
    ChatToolResult --> ChatErrorSummary
    ChatShellSummary --> OutputPreview
```

## 3. MCP And Tool Service Model

### 3.1 MCP Client Configuration

```mermaid
classDiagram
    direction TB

    class CodegeistConfig {
      <<existing>>
      Map~String McpClientConfig~ mcp
    }

    class McpClientConfig {
      <<planned>>
      McpClientType type
      String command
      List~String~ args
    }

    class McpClientType {
      <<planned enum>>
      STDIO
    }

    class CodegeistMcpClientRegistry {
      <<planned>>
      +clients(Map~String McpClientConfig~ config) List~Object~
    }

    class CodegeistMcpToolCallbackProvider {
      <<planned>>
      +toolCallbacks() List~Object~
    }

    CodegeistConfig --> McpClientConfig
    McpClientConfig --> McpClientType
    CodegeistMcpClientRegistry --> McpClientConfig
    CodegeistMcpToolCallbackProvider --> CodegeistMcpClientRegistry
```

### 3.2 Tool Service Contract

```mermaid
classDiagram
    direction TB

    class CodegeistToolService {
      <<planned>>
      +descriptors() List~CodegeistToolDescriptor~
      +execute(CodegeistToolRequest request) CodegeistToolResult
    }

    class CodegeistTool {
      <<planned interface>>
      +descriptor() CodegeistToolDescriptor
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    class CodegeistToolDescriptor {
      <<planned record>>
      String name
      String description
      boolean mutatesFiles
      boolean runsCommands
    }

    class CodegeistToolContext {
      <<planned record>>
      Path workingDir
      Path chatPath
    }

    class CodegeistToolRequest {
      <<planned record>>
      String toolName
      Map~String Object~ input
    }

    class CodegeistToolResult {
      <<planned record>>
      ChatToolStatus status
      OutputPreview output
      ChatFileSummary fileSummary
      ChatChangeSummary changeSummary
      ChatShellSummary shellSummary
      ChatErrorSummary errorSummary
    }

    CodegeistToolService --> CodegeistTool
    CodegeistToolService --> CodegeistToolDescriptor
    CodegeistToolService --> CodegeistToolRequest
    CodegeistToolService --> CodegeistToolResult
    CodegeistTool --> CodegeistToolDescriptor
    CodegeistTool --> CodegeistToolContext
    CodegeistTool --> CodegeistToolRequest
    CodegeistTool --> CodegeistToolResult
```

### 3.3 Tool Result Bounds And Safety

```mermaid
classDiagram
    direction TB

    class CodegeistToolResult {
      <<planned record>>
      ChatToolStatus status
      OutputPreview output
      ChatFileSummary fileSummary
      ChatChangeSummary changeSummary
      ChatShellSummary shellSummary
      ChatErrorSummary errorSummary
    }

    class ToolOutputBounds {
      <<planned>>
      +preview(String value) OutputPreview
    }

    class ChatToolStatus {
      <<planned enum>>
    }

    class OutputPreview {
      <<planned record>>
    }

    class ChatFileSummary {
      <<planned record>>
    }

    class ChatChangeSummary {
      <<planned record>>
    }

    class ChatShellSummary {
      <<planned record>>
    }

    class ChatErrorSummary {
      <<planned record>>
    }

    CodegeistToolResult --> ChatToolStatus
    CodegeistToolResult --> OutputPreview
    CodegeistToolResult --> ChatFileSummary
    CodegeistToolResult --> ChatChangeSummary
    CodegeistToolResult --> ChatShellSummary
    CodegeistToolResult --> ChatErrorSummary
    ToolOutputBounds --> OutputPreview
```

## 4. Tool Implementations

### 4.1 Read-Only File Tools

```mermaid
classDiagram
    direction TB

    class CodegeistTool {
      <<planned interface>>
    }

    class ToolOutputBounds {
      <<planned>>
    }

    class ListFilesTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    class ReadFileTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    class GlobFilesTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    class GrepFilesTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    ListFilesTool ..|> CodegeistTool
    ReadFileTool ..|> CodegeistTool
    GlobFilesTool ..|> CodegeistTool
    GrepFilesTool ..|> CodegeistTool
    ReadFileTool --> ToolOutputBounds
    GrepFilesTool --> ToolOutputBounds
```

### 4.2 Write And File Mutation Tools

```mermaid
classDiagram
    direction TB

    class CodegeistTool {
      <<planned interface>>
    }

    class ToolOutputBounds {
      <<planned>>
    }

    class WriteFileTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    class ApplyPatchTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    class EditFileTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    WriteFileTool ..|> CodegeistTool
    ApplyPatchTool ..|> CodegeistTool
    EditFileTool ..|> CodegeistTool
    WriteFileTool --> ToolOutputBounds
    ApplyPatchTool --> ToolOutputBounds
    EditFileTool --> ToolOutputBounds
```

### 4.3 Shell Tool

```mermaid
classDiagram
    direction TB

    class ShellTool {
      <<planned>>
      +execute(CodegeistToolContext context, CodegeistToolRequest request) CodegeistToolResult
    }

    class ShellCommandRequest {
      <<planned record>>
      String command
      Path cwd
      Duration timeout
    }

    class ShellCommandResult {
      <<planned record>>
      int exitCode
      boolean timedOut
      Duration duration
      OutputPreview stdout
      OutputPreview stderr
    }

    class CodegeistTool {
      <<planned interface>>
    }

    class ToolOutputBounds {
      <<planned>>
    }

    class OutputPreview {
      <<planned record>>
    }

    ShellTool ..|> CodegeistTool
    ShellTool --> ToolOutputBounds
    ShellTool --> ShellCommandRequest
    ShellTool --> ShellCommandResult
    ShellCommandResult --> OutputPreview
```

## 5. TerminalUI Chat Harness

T007_06 now has a minimal Spring Shell `TerminalUI` chat loop.
`ai.codegeist.app.tui.TuiCommands` exposes the `tui` command and delegates to
`CodegeistTerminalUi`. `CodegeistTerminalUi` builds `TerminalUI` instances from
`TerminalUIBuilder`, configures a bordered `GridView` root with transcript
`BoxView` and prompt `InputView`, focuses the prompt, binds `Ctrl-Q` to interrupt
the loop, and preserves the local transcript across normal `TerminalUI.run()`
returns.

Pressing Enter on a non-blank prompt submits exactly one turn through
`ChatHarnessService.ask(true, prompt)`, appends returned response text or handled
harness failures to an in-memory transcript, rebuilds the prompt input after each
submission, and supports repeated turns without restarting the Codegeist process.

`CodegeistLocaleService` uses optional app-wide `codegeist.locale` and otherwise
falls back to the JVM default locale for message lookup. The final T007_07 slice added
bounded completed-tool previews but not stored-session projection, streaming output,
live tool progress, permission prompts, a presenter, view factory, responsive layout
service, Spring Shell control wrapper package, or generic `task tui-smoke` entrypoint.
The documentation-specific `task tui-capture-smoke` path is a VHS-rendered native
capture smoke that writes local preview artifacts under
`target/smoke-test/tui-capture/`; it is not a separate TUI runtime architecture.

Keep provider selection, MCP/tool callbacks, the model/tool/model continuation loop,
and `.codegeist/session.json` persistence behind `ChatHarnessService` and existing
runtime services.

Do not restore a custom JLine console, deterministic line-renderer pipeline, or
second agent runtime unless a future task explicitly replaces the Spring Shell
approach. Streaming, cancellation, patch review UI, shell review panes, session
browsers, and richer transcript projection remain future work.

## Completion Note

T007 completed on 2026-07-18 after current-worktree focused tests, the native
VHS-recorded hello-world tool smoke, and the broad JVM suite passed. This file is
retained as task-planning history rather than being rewritten into a duplicate of
the current architecture documentation.
