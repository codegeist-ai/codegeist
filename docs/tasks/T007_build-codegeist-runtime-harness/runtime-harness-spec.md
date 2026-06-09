# Runtime Harness Specification

Planned class model for the T007 chat-file tool harness. This document is an
iterative specification: refine the model slice by slice, and create Java classes
only when the focused implementation task introduces tested source.

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
        +createChatModel() CodegeistChatModel
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
    ProviderConfig --> CodegeistChatModel
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
      String id
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
      String id
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
      String id
      Instant createdAt
      String toolName
      String inputSummary
    }

    class ChatToolResult {
      <<planned record>>
      String id
      String callId
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

    class WorkingDirectoryGuard {
      <<planned>>
      +requireInsideWorkingDir(Path workingDir, Path target) Path
      +requireCommandCwd(Path workingDir, Path cwd) Path
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

    class WorkingDirectoryGuard {
      <<planned>>
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
    ListFilesTool --> WorkingDirectoryGuard
    ReadFileTool --> WorkingDirectoryGuard
    GlobFilesTool --> WorkingDirectoryGuard
    GrepFilesTool --> WorkingDirectoryGuard
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

    class WorkingDirectoryGuard {
      <<planned>>
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
    WriteFileTool --> WorkingDirectoryGuard
    ApplyPatchTool --> WorkingDirectoryGuard
    EditFileTool --> WorkingDirectoryGuard
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

    class WorkingDirectoryGuard {
      <<planned>>
    }

    class ToolOutputBounds {
      <<planned>>
    }

    class OutputPreview {
      <<planned record>>
    }

    ShellTool ..|> CodegeistTool
    ShellTool --> WorkingDirectoryGuard
    ShellTool --> ToolOutputBounds
    ShellTool --> ShellCommandRequest
    ShellTool --> ShellCommandResult
    ShellCommandResult --> OutputPreview
```

## 5. Terminal TUI Model

### 5.1 TUI Entry And Control

```mermaid
classDiagram
    direction TB

    class TerminalTuiCommands {
      <<planned>>
      +tui(Path chatPath) void
    }

    class TuiController {
      <<planned>>
      +open(Path chatPath) void
      +submitPrompt(String prompt) void
      +save() void
    }

    class TerminalPromptLoop {
      <<planned>>
      +run(TuiController controller) void
    }

    class KeyBindingHelp {
      <<planned>>
      +rows() List~String~
    }

    class ChatFileService {
      <<planned>>
    }

    class ChatHarnessService {
      <<planned>>
    }

    class TuiViewModelFactory {
      <<planned>>
    }

    TerminalTuiCommands --> TuiController
    TuiController --> ChatFileService
    TuiController --> ChatHarnessService
    TuiController --> TuiViewModelFactory
    TuiController --> TerminalPromptLoop
    TerminalPromptLoop --> TuiController
    TerminalPromptLoop --> KeyBindingHelp
```

### 5.2 TUI Projection And Runtime Status

```mermaid
classDiagram
    direction TB

    class TuiViewModelFactory {
      <<planned>>
      +from(ChatFile chatFile, RuntimeStatus runtimeStatus) TuiViewModel
    }

    class RuntimeStatusProjector {
      <<planned>>
      +project(CodegeistConfig config, CodegeistToolService tools) RuntimeStatus
    }

    class RuntimeStatus {
      <<planned record>>
      String provider
      String model
      int mcpToolCount
      int codegeistToolCount
      boolean busy
      String error
    }

    class ChatFile {
      <<planned record>>
    }

    class TuiViewModel {
      <<planned record>>
    }

    class CodegeistConfig {
      <<existing>>
    }

    class CodegeistToolService {
      <<planned>>
    }

    TuiViewModelFactory --> ChatFile
    TuiViewModelFactory --> RuntimeStatus
    TuiViewModelFactory --> TuiViewModel
    RuntimeStatusProjector --> CodegeistConfig
    RuntimeStatusProjector --> CodegeistToolService
    RuntimeStatusProjector --> RuntimeStatus
```

### 5.3 Persisted Chat View State

```mermaid
classDiagram
    direction TB

    class TuiViewModel {
      <<planned record>>
      ChatFileView chatFile
      RuntimeStatus runtimeStatus
      List~TranscriptRow~ rows
      PromptState prompt
      Notification notification
      ModalState modal
      ScrollState scroll
    }

    class ChatFileView {
      <<planned record>>
      Path chatPath
      Path workingDir
      List~ChatMessageView~ messages
      boolean dirty
    }

    class ChatMessageView {
      <<planned record>>
      String role
      String text
      Instant createdAt
    }

    class RuntimeStatus {
      <<planned record>>
    }

    TuiViewModel --> ChatFileView
    TuiViewModel --> RuntimeStatus
    ChatFileView --> ChatMessageView
```

### 5.4 Transcript Rows And Activity Views

```mermaid
classDiagram
    direction TB

    class TuiViewModel {
      <<planned record>>
      List~TranscriptRow~ rows
    }

    class TranscriptRow {
      <<planned record>>
      String role
      String text
      ToolActivityView tool
      ChangeView change
      ShellView shell
      boolean truncated
    }

    class ToolActivityView {
      <<planned record>>
      String toolName
      ChatToolStatus status
      String summary
      Duration duration
    }

    class ChangeView {
      <<planned record>>
      List~Path~ affectedPaths
      String diffPreview
      boolean truncated
    }

    class ShellView {
      <<planned record>>
      String command
      Path cwd
      int exitCode
      boolean timedOut
      OutputPreview stdout
      OutputPreview stderr
    }

    class ChatToolStatus {
      <<planned enum>>
    }

    class OutputPreview {
      <<planned record>>
    }

    TuiViewModel --> TranscriptRow
    TranscriptRow --> ToolActivityView
    TranscriptRow --> ChangeView
    TranscriptRow --> ShellView
    ToolActivityView --> ChatToolStatus
    ShellView --> OutputPreview
```

### 5.5 Transient TUI State

```mermaid
classDiagram
    direction TB

    class TuiViewModel {
      <<planned record>>
      PromptState prompt
      Notification notification
      ModalState modal
      ScrollState scroll
    }

    class PromptState {
      <<planned record>>
      String draft
      boolean disabled
    }

    class Notification {
      <<planned record>>
      String level
      String message
    }

    class ModalState {
      <<planned record>>
      String type
      String title
      List~String~ options
    }

    class ScrollState {
      <<planned record>>
      int offset
      boolean followBottom
    }

    TuiViewModel --> PromptState
    TuiViewModel --> Notification
    TuiViewModel --> ModalState
    TuiViewModel --> ScrollState
```

### 5.6 TUI Rendering

```mermaid
classDiagram
    direction TB

    class ChatScreenRenderer {
      <<planned>>
      +render(TuiViewModel model) List~String~
    }

    class LineChatRenderer {
      <<planned>>
      +render(TuiViewModel model) List~String~
    }

    class ToolActivityRenderer {
      <<planned>>
      +render(ToolActivityView tool) String
      +render(ChangeView change) String
      +render(ShellView shell) String
    }

    class TuiViewModel {
      <<planned record>>
    }

    class ToolActivityView {
      <<planned record>>
    }

    class ChangeView {
      <<planned record>>
    }

    class ShellView {
      <<planned record>>
    }

    ChatScreenRenderer --> TuiViewModel
    LineChatRenderer --> TuiViewModel
    ToolActivityRenderer --> ToolActivityView
    ToolActivityRenderer --> ChangeView
    ToolActivityRenderer --> ShellView
```

## Iteration Rule

Keep this specification synchronized with the active child task:

- `T007_02` should refine sections 1, 2.1, 2.2, 2.3, and 2.4.
- `T007_03` should refine sections 3.1, 3.2, 3.3, and 4.1.
- `T007_04` should refine sections 4.2 and 4.3.
- `T007_05` should refine sections 5.1 through 5.6.
- `T007_06` should reconcile all diagrams with the implemented source.
