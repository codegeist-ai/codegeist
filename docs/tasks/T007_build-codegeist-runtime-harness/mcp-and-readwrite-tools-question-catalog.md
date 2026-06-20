# T007_03 MCP And Read Write Tools Question Catalog

Focused `/ask-project` question catalog for `T007_03`. Use this before
implementing Codegeist MCP callbacks, local read/list/glob/grep/write tools, and
the first chat/tool harness around `.codegeist/session.json`.

Answered in: `mcp-and-readwrite-tools-research.md`.

## Purpose

Use this catalog to gather source-backed evidence for these decisions:

- How OpenCode handles tool lifecycle, MCP, local file tools, workspace policy,
  bounded output, and persisted tool activity.
- Which Spring AI Agent Utils classes should be used directly, wrapped as private
  delegates, copied conceptually, or avoided.
- Whether Codegeist should implement `T007_03` as a small harness around provider
  calls, tool callbacks, MCP setup, session persistence, and cleanup instead of
  scattering orchestration across `AskCommands`, `CodegeistChatService`, and
  `SessionStoreService`.
- What Aider, SWE-agent, and mini-SWE-agent prove about smaller coding-agent
  harnesses, especially which repo-map, git, trajectory, Docker, and shell-first
  ideas should stay out of T007_03.
- How to update `mcp-and-readwrite-tools-spec.md` with evidence before Java source
  implementation starts.

## Preconditions

`/ask-project` expects an analyzed third-party workspace under
`docs/third-party/<project>/`. If a question reports missing or stale artifacts,
refresh that project through `/analyse-project` first.

Expected projects:

```text
opencode
spring-ai-agent-utils
```

Additional public-source comparison projects for this catalog:

```text
Aider
SWE-agent
mini-SWE-agent
```

They are not currently imported under `docs/third-party/`; use public source or add
analysis workspaces before treating their line numbers as durable evidence.

Do not answer implementation-source questions from memory when Repomix artifacts
are available. `/ask-project` owns delegating broad source-level questions to the
Repomix subagent.

## Recommended First Pass

Run these six questions first. They produce enough evidence to decide whether the
current `mcp-and-readwrite-tools-spec.md` harness shape is correct.

```text
/ask-project opencode "For Codegeist T007_03, create an evidence table for OpenCode's tool system with columns: source file, responsibility, public behavior, persisted state, runtime-only state, Codegeist translation, and should-copy/should-not-copy. Focus on MCP, read/list/glob/grep/write, tool lifecycle, output bounds, and permission/workspace policy."

/ask-project spring-ai-agent-utils "For Codegeist T007_03, create an evidence table for Agent Utils file and tool support with columns: source file, class, Spring AI API used, behavior, reusable as-is, reusable behind wrapper, not reusable, missing Codegeist policy."

/ask-project opencode "For Codegeist T007_03, identify the exact OpenCode call flow from user prompt to model tool call to tool execution to tool result persistence. Explain the orchestrating services/functions and cite source paths. Recommend whether Codegeist should introduce a ChatHarnessService or ToolHarnessService."

/ask-project spring-ai-agent-utils "From Agent Utils and Spring AI source evidence, design a minimal Codegeist T007_03 harness API. Should it be ChatHarnessService, CodegeistToolRun, CodegeistChatExecutionContext, or another shape? Include method signatures and dependency direction."

/ask-project opencode "Analyze OpenCode's session/message/part persistence for tool calls and tool results. Which fields are essential for later UI rendering and conversation continuation, and which fields are OpenCode-specific or too broad for Codegeist T007_03?"

/ask-project spring-ai-agent-utils "Recommend direct dependency versus source-inspired reimplementation for each Codegeist local tool: read, list, glob, grep, write. Include rationale, risks, test implications, and which Agent Utils classes to cite."
```

## OpenCode Evidence Questions

```text
/ask-project opencode "For Codegeist T007_03, create an evidence table for OpenCode's tool system with columns: source file, responsibility, public behavior, persisted state, runtime-only state, Codegeist translation, and should-copy/should-not-copy. Focus on MCP, read/list/glob/grep/write, tool lifecycle, output bounds, and permission/workspace policy."

/ask-project opencode "For Codegeist T007_03, identify the exact OpenCode call flow from user prompt to model tool call to tool execution to tool result persistence. Explain the orchestrating services/functions and cite source paths. Recommend whether Codegeist should introduce a ChatHarnessService or ToolHarnessService."

/ask-project opencode "Analyze OpenCode's tool registry and tool descriptor creation. How are tools named, described, filtered, enabled, and passed to the model? Which parts are runtime-only and which parts, if any, are persisted?"

/ask-project opencode "Analyze OpenCode's tool lifecycle states. How does it represent pending, running, completed, and failed tool calls, and how does it attach tool results to assistant messages or session state? Identify minimal fields Codegeist should persist in ToolSessionPart."

/ask-project opencode "Analyze OpenCode's model-visible tool result format. How does it convert raw tool execution output into content returned to the model? How does that differ from what is persisted for UI/session rendering?"

/ask-project opencode "Analyze OpenCode's session/message/part persistence for tool calls and tool results. Which fields are essential for later UI rendering and conversation continuation, and which fields are OpenCode-specific or too broad for Codegeist T007_03?"

/ask-project opencode "Analyze OpenCode's bounded-output strategy for file reads, directory lists, glob results, grep results, shell output, diffs, and error text. Recommend concrete bounds and truncation fields for Codegeist ToolSessionPart."

/ask-project opencode "Analyze OpenCode's workspace boundary for tools. How does it determine working directory, validate paths, handle absolute paths, prevent traversal, handle symlinks, and avoid writing outside the project root?"

/ask-project opencode "Analyze OpenCode's file read tool behavior. Include input schema, line offsets, limits, binary-file handling, missing-file errors, directory errors, path validation, output formatting, and persisted result shape."

/ask-project opencode "Analyze OpenCode's list directory tool behavior. Include input schema, recursion/depth, ignored files, result sorting, directory markers, max results, path validation, and error cases."

/ask-project opencode "Analyze OpenCode's glob tool behavior. Include pattern syntax, base path behavior, ignored/generated files, max results, sorting, file-vs-directory matches, path validation, and error cases."

/ask-project opencode "Analyze OpenCode's grep/search tool behavior. Include regex behavior, include filters, context lines, line limits, binary-file handling, multiline support, result formatting, output bounds, and invalid-regex errors."

/ask-project opencode "Analyze OpenCode's write file behavior. Include create vs overwrite semantics, parent-directory behavior, protected paths, symlink handling, content bounds, result summary, and whether write is distinct from patch/edit."

/ask-project opencode "Analyze OpenCode's edit/patch behavior only to draw the boundary for T007_03. What must Codegeist deliberately not include in write because patch/edit belongs to T007_04?"

/ask-project opencode "Analyze OpenCode's MCP configuration and runtime lifecycle. How are MCP servers configured, started, connected, converted into tools, failed, retried, and closed? Which behavior should Codegeist defer?"

/ask-project opencode "Analyze how OpenCode combines local tools and MCP tools before sending them to the provider. How are naming collisions, permission policy, disabled tools, or runtime failures handled?"

/ask-project opencode "Analyze OpenCode permission prompts or side-effect approvals for read tools versus write/edit/shell tools. What is mandatory user-visible safety behavior for Codegeist T007_03, and what can be deferred?"

/ask-project opencode "Analyze OpenCode tests for read/list/glob/grep/write tools, MCP tools, tool lifecycle persistence, and workspace policy. Summarize test names, source paths, fixtures, and assertions Codegeist should mirror."

/ask-project opencode "Create a sequence diagram for OpenCode's full tool execution loop: prompt submission, tool descriptors sent to model, model emits tool call, tool validates policy, tool executes, result is bounded, result is persisted, model continues."

/ask-project opencode "Create a class or component diagram for OpenCode's tool/MCP/session orchestration. Then translate it into a minimal Java/Spring Codegeist harness shape without copying OpenCode's server, SQLite, SDK, plugin, or TUI architecture."
```

## Spring AI Agent Utils Evidence Questions

```text
/ask-project spring-ai-agent-utils "For Codegeist T007_03, create an evidence table for Agent Utils file and tool support with columns: source file, class, Spring AI API used, behavior, reusable as-is, reusable behind wrapper, not reusable, missing Codegeist policy."

/ask-project spring-ai-agent-utils "Analyze Agent Utils tool registration. How do @Tool methods, MethodToolCallbackProvider, FunctionToolCallback, ToolCallbackProvider, and ToolContext appear in the source? Which approach best fits per-chat Codegeist workingDir and session recording?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils file read/write/list/glob/grep tools. Identify exact classes, input parameters, output shape, path handling, bounds, ignored-file behavior, binary-file behavior, errors, and tests."

/ask-project spring-ai-agent-utils "Analyze whether Agent Utils file tools can be used directly inside Codegeist T007_03. What Codegeist-specific wrappers are still required for workingDir policy, symlink escape prevention, session persistence, no-secret behavior, and bounded output?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils GlobTool and GrepTool implementation and tests. Which behaviors are strong enough to reuse, and which should Codegeist implement itself for a smaller deterministic first slice?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils FileSystemTools write behavior. Does it create, overwrite, create parent directories, reject directories, handle symlinks, or bound content? Recommend Codegeist T007_03 write semantics."

/ask-project spring-ai-agent-utils "Analyze Agent Utils bounded output patterns across file tools, grep, glob, and shell. Are bounds centralized or per-tool? Recommend a Codegeist ToolOutputBounds service shape."

/ask-project spring-ai-agent-utils "Analyze Agent Utils error handling for tool inputs, IO failures, invalid regex, missing files, and path errors. Does it return model-visible error strings, throw exceptions, or use structured results? Recommend Codegeist ToolSessionPart failure fields."

/ask-project spring-ai-agent-utils "Analyze Agent Utils use of ToolContext. Can Codegeist pass workingDir, session id, call id, recorder, or policy objects via ToolContext safely, or should Codegeist use explicit ToolCallback wrappers?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils explicit ToolCallback implementations such as FunctionToolCallback-based tools. Which patterns are better for Codegeist than @Tool singleton methods?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils advisors or orchestration classes that add tools to ChatClient or ChatModel calls. Do any provide a reusable pattern for CodegeistChatExecutionContext or ChatHarnessService?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils MCP-related support, if any. Does it wrap Spring AI MCP clients, expose MCP tool callbacks, or assume Spring Boot spring.ai.mcp.client.* properties? Recommend how Codegeist should map direct codegeist.yml mcp: config."

/ask-project spring-ai-agent-utils "Analyze whether Agent Utils has a chat/session persistence model. If yes, compare it to Codegeist .codegeist/session.json. If no, identify what Codegeist must own."

/ask-project spring-ai-agent-utils "Analyze Agent Utils tests for file tools and tool callbacks. Which test fixture patterns should Codegeist use for @TempDir, path escape, symlink escape, invalid regex, output truncation, and callback invocation?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils assumptions that conflict with Codegeist T007_03: memory systems, skills, task tools, subagents, background shell processes, broad runtime state, or server-like orchestration."

/ask-project spring-ai-agent-utils "Create a minimal Java/Spring component diagram for Codegeist T007_03 using Agent Utils where useful. Include AskCommands, CodegeistChatService, CodegeistChatExecutionContext, CodegeistToolService, CodegeistMcpAdapter, CodegeistLocalTools, ToolOutputBounds, WorkspacePolicy, and SessionStoreService."

/ask-project spring-ai-agent-utils "Recommend direct dependency versus source-inspired reimplementation for each Codegeist local tool: read, list, glob, grep, write. Include rationale, risks, test implications, and which Agent Utils classes to cite."
```

## Harness Design Questions

```text
/ask-project opencode "From OpenCode source evidence, should Codegeist T007_03 introduce a single chat/tool harness that orchestrates provider call, tool callbacks, MCP setup, local tools, output bounds, and session persistence? Compare with keeping orchestration inside AskCommands and CodegeistChatService."

/ask-project spring-ai-agent-utils "From Agent Utils and Spring AI source evidence, design a minimal Codegeist T007_03 harness API. Should it be ChatHarnessService, CodegeistToolRun, CodegeistChatExecutionContext, or another shape? Include method signatures and dependency direction."

/ask-project opencode "Identify the smallest OpenCode-inspired harness boundary Codegeist can implement without adopting OpenCode's server, SQLite, SDK, plugin, permission database, or TUI sync architecture."

/ask-project spring-ai-agent-utils "Identify the Spring-friendly harness boundary for a per-chat tool run with closeable MCP clients. How should Codegeist scope ToolCallback instances, ToolContext, resource cleanup, and recorded ToolSessionPart values?"

/ask-project opencode "Analyze where OpenCode records tool activity relative to assistant message creation. Should Codegeist persist tool parts as assistant message parts before the final assistant text, as separate messages, or as separate root-level results?"

/ask-project spring-ai-agent-utils "Analyze how Spring AI internal tool execution returns final ChatResponse after tool calls. Where can Codegeist intercept or record tool callback execution when using ToolCallback wrappers?"

/ask-project opencode "What observable behavior would users expect from a harness when a tool fails, MCP setup fails, provider fails after tools ran, or session save fails? Recommend Codegeist first-slice behavior."

/ask-project spring-ai-agent-utils "How should Codegeist close or clean up stdio MCP clients when a chat run finishes or fails? Identify relevant Spring AI MCP close/destroy patterns and test seams."
```

## Smaller Harness Comparison Questions

Run these only after the corresponding third-party workspaces are imported under
`docs/third-party/`. Until then, keep the current public-source synthesis in
`mcp-and-readwrite-tools-research.md` and
`coding-agent-harness-implementations.md` as the available evidence.

```text
/ask-project aider "For Codegeist T007_03, analyze Aider's harness shape: coder state, repo map, file editing, git integration, lint/test loop, and chat history. Which concepts support the ChatHarnessService decision, and which should stay out of MCP/read/write tools?"

/ask-project swe-agent "For Codegeist T007_03, analyze SWE-agent's tool interface, ToolHandler, environment execution, configuration, and trajectory recording. Which concepts should inform ToolSessionPart and which benchmark/runtime features should be deferred?"

/ask-project mini-swe-agent "For Codegeist T007_03, analyze mini-SWE-agent's minimal model-plus-environment loop, linear message history, bash-only execution, and trajectory output. What does it imply for keeping Codegeist's first harness narrow?"
```

## MCP-Specific Questions

```text
/ask-project opencode "Deep dive only into OpenCode MCP config schema. Which fields exist for command, args, env, disabled/enabled, timeout, type/transport, cwd, and auth? Which fields should Codegeist T007_03 support or defer?"

/ask-project opencode "Deep dive only into OpenCode MCP runtime process handling. How are stdio servers spawned, monitored, initialized, exposed, failed, and shut down?"

/ask-project opencode "How does OpenCode handle MCP tool naming collisions or duplicate tool names across local tools and multiple MCP servers? Recommend Codegeist naming rules."

/ask-project opencode "Does OpenCode persist MCP tool definitions, MCP server status, or MCP resource state in session data? If yes, why; if no, where does it live?"

/ask-project spring-ai-agent-utils "Using Spring AI 2.0.0-M6 and Agent Utils source evidence, explain the minimal Java code path from Codegeist McpClientConfig(type=stdio, command, args) to ToolCallback[] via Spring AI MCP."

/ask-project spring-ai-agent-utils "How should Codegeist test MCP callback availability without launching real network-dependent MCP servers? Recommend fake ToolCallbackProvider or fake McpSyncClient seams."
```

## File Tool Contract Questions

```text
/ask-project opencode "For each file tool read/list/glob/grep/write, produce a recommended Codegeist input JSON schema based on OpenCode behavior. Include required fields, optional fields, defaults, caps, and rejected fields."

/ask-project spring-ai-agent-utils "For each file tool read/list/glob/grep/write, produce a recommended Codegeist input Java record/class shape based on Agent Utils patterns and Spring AI tool callback requirements."

/ask-project opencode "For each file tool read/list/glob/grep/write, produce a recommended model-visible output format and separate persisted ToolSessionPart fields. Keep outputs bounded."

/ask-project spring-ai-agent-utils "For each file tool read/list/glob/grep/write, identify whether the implementation should be direct Java NIO, Agent Utils delegate, or custom wrapper over Agent Utils. Explain test tradeoffs."

/ask-project opencode "Analyze how OpenCode handles binary files and large files in read and grep tools. Recommend Codegeist binary detection and max-size policy for T007_03."

/ask-project spring-ai-agent-utils "Analyze how Agent Utils detects or handles binary files and large files in file tools. Recommend whether Codegeist should copy, simplify, or defer that behavior."

/ask-project opencode "Analyze whether OpenCode's write tool creates parent directories. Should Codegeist T007_03 write create parents or reject missing parents? Provide source evidence and recommendation."

/ask-project spring-ai-agent-utils "Analyze whether Agent Utils write/edit tools create parent directories. Recommend Codegeist T007_03 write behavior and tests."
```

## Session Persistence Questions

```text
/ask-project opencode "Map OpenCode persisted tool call/result fields to Codegeist ToolSessionPart. Produce a table with OpenCode field, purpose, Codegeist field, keep/defer/drop, and rationale."

/ask-project spring-ai-agent-utils "Does Agent Utils define any durable tool result object that could inform Codegeist ToolSessionPart? If not, recommend a minimal Java record/class shape."

/ask-project opencode "What timestamps or lifecycle fields does OpenCode persist for tool calls? Should Codegeist T007_03 include createdAt/completedAt or defer timing fields?"

/ask-project opencode "How does OpenCode persist failed tool calls? Include validation errors, permission denied, execution errors, timeouts, and cancellations. Which states should Codegeist T007_03 include?"

/ask-project spring-ai-agent-utils "How do Agent Utils tools represent failed execution? Recommend how Codegeist should convert thrown exceptions or error strings into failed ToolSessionPart records."

/ask-project opencode "Should Codegeist persist tool input arguments? How does OpenCode sanitize or bound tool input values before persistence or UI rendering?"

/ask-project spring-ai-agent-utils "Recommend a sanitization policy for persisted tool input maps in Codegeist. Which scalar values are safe, which values must be bounded, and which values should be omitted?"
```

## Test And Verification Questions

```text
/ask-project opencode "List OpenCode source tests that are most relevant for T007_03. Group by MCP, read, list, glob, grep, write, workspace path policy, output bounds, and persisted tool parts."

/ask-project spring-ai-agent-utils "List Agent Utils tests that Codegeist should mirror for T007_03. Group by FileSystemTools, ListDirectoryTool, GlobTool, GrepTool, ToolCallback registration, ToolContext, and error handling."

/ask-project opencode "Recommend Codegeist T007_03 tests in order: WorkspacePolicyTest, ToolOutputBoundsTest, CodegeistLocalToolsTest, CodegeistMcpAdapterTest, CodegeistToolServiceTest, SessionStoreServiceTest, AskCommandsSessionStoreTest. Include what each test should assert."

/ask-project spring-ai-agent-utils "Recommend how to test Spring AI ToolCallback wrappers without making real provider calls. Include fake ChatModel, fake ToolCallbackProvider, and direct callback invocation patterns."

/ask-project opencode "Which OpenCode behavior should Codegeist deliberately not test or implement in T007_03 because it belongs to patch/edit, shell, permissions UI, TUI, plugins, subagents, server API, or database sync?"

/ask-project spring-ai-agent-utils "Which Agent Utils behavior should Codegeist deliberately not adopt in T007_03 because it is too broad, assumes memory/skills/tasks/shell background processes, or conflicts with Codegeist session-store boundaries?"
```

## Synthesis Questions

```text
/ask-project opencode "Produce a concise OpenCode-to-Codegeist translation plan for T007_03. Include target behavior, non-goals, source citations, risks, and minimal implementation order."

/ask-project spring-ai-agent-utils "Produce a concise Agent-Utils-to-Codegeist translation plan for T007_03. Include reusable classes, wrapper requirements, non-goals, risks, and minimal implementation order."

/ask-project opencode "Review the Codegeist T007_03 specification conceptually: local tools plus MCP callbacks plus ToolSessionPart persistence, while CodegeistChatRequest remains model+prompt. What OpenCode evidence supports or contradicts this design?"

/ask-project spring-ai-agent-utils "Review the Codegeist T007_03 specification conceptually: explicit ToolCallback wrappers, CodegeistToolRun, CodegeistChatExecutionContext, WorkspacePolicy, ToolOutputBounds, and ToolSessionPart. What Agent Utils evidence supports or contradicts this design?"

/ask-project opencode "Generate a final checklist for T007_03 implementation readiness from OpenCode evidence. Include unresolved questions that need user decisions before Java code."

/ask-project spring-ai-agent-utils "Generate a final checklist for T007_03 implementation readiness from Agent Utils evidence. Include unresolved questions that need user decisions before Java code."
```

## Expected Research Outputs

After enough questions are answered, create or update:

- `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-spec.md`
- `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-research.md`
- `docs/tasks/T007_build-codegeist-runtime-harness/tasks/T007_03_add-mcp-and-read-write-tools/task.md`

The research document should include source citations, decisions, unresolved
questions, and recommended changes to the T007_03 specification. Keep it separate
from current-state architecture docs until runtime implementation actually exists.
