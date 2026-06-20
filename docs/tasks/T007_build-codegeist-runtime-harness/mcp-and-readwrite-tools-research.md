# T007_03 MCP And Read Write Tools Research

Source-backed answers for
`docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-question-catalog.md`.

This document answers the focused T007_03 research questions by synthesizing local
OpenCode and Spring AI Agent Utils source evidence plus a smaller public-source
comparison against Aider, SWE-agent, and mini-SWE-agent. It is implementation
guidance, not current-state Codegeist architecture.

## Scope And Evidence

Evidence used:

- OpenCode analysis workspace at `docs/third-party/opencode/`.
- OpenCode source checkout at `docs/third-party/opencode/source/`.
- OpenCode Repomix output at `docs/third-party/opencode/repomix-output.xml`.
- Spring AI Agent Utils analysis workspace at
  `docs/third-party/spring-ai-agent-utils/`.
- Spring AI Agent Utils source checkout at
  `docs/third-party/spring-ai-agent-utils/source/`.
- Spring AI Agent Utils Repomix output at
  `docs/third-party/spring-ai-agent-utils/repomix-output.xml`.
- Current Codegeist T007_03 specification at
  `docs/tasks/T007_build-codegeist-runtime-harness/mcp-and-readwrite-tools-spec.md`.
- Coding-agent harness comparison table at
  `docs/tasks/T007_build-codegeist-runtime-harness/coding-agent-harness-implementations.md`.
- Focused local Aider and mini-SWE-agent comparison in
  `docs/tasks/T007_build-codegeist-runtime-harness/aider-mini-swe-harness-research.md`.
- Public GitHub source and documentation evidence for SWE-agent.

Limits:

- No upstream runtime tests were executed.
- OpenCode behavior is static-source evidence from revision documented in
  `docs/third-party/opencode/ANALYSIS_REPORT.md`.
- Agent Utils behavior is static-source evidence from revision documented in
  `docs/third-party/spring-ai-agent-utils/ANALYSIS_REPORT.md`.
- Aider and mini-SWE-agent now have local third-party workspaces. SWE-agent remains
  public-source-only in this research set unless it is imported later.

## Executive Answers

- MCP and tools are related but not the same. OpenCode treats MCP as one dynamic
  source of model-callable tools; local built-in tools and MCP tools are merged only
  at session execution time.
- Codegeist should implement `T007_03` as a small harness boundary. The harness
  should orchestrate provider selection, per-chat tool callback creation, MCP client
  setup/cleanup, local file tools, bounded output, and session-store persistence.
- The smallest useful Codegeist shape is a `ChatHarnessService` that owns one prompt
  turn and a subordinate `CodegeistToolRun` that owns scoped tool callbacks and
  closeable MCP clients.
- Keep `CodegeistChatRequest` unchanged with only `model` and `prompt`.
- Do not expose raw Spring AI Agent Utils file tools directly. They are useful
  references and possible private delegates, but they lack Codegeist workspace
  policy, symlink protection, session persistence, and centralized output bounds.
- Do not expose Spring AI `spring.ai.mcp.client.*` properties as Codegeist public
  config. Keep direct `codegeist.yml` `mcp:` and map it privately into Spring AI MCP
  callbacks.
- Persist tool calls and bounded tool results as assistant message parts in
  `.codegeist/session.json`. Do not persist MCP client definitions, enabled tool
  registry snapshots, provider config, selected provider/model, permission rules,
  runtime status, or TUI state.
- `write` should stay create/overwrite only in T007_03. Patch/edit and shell remain
  T007_04 work.
- Aider, SWE-agent, and mini-SWE-agent support the same narrow-harness conclusion
  from the opposite direction: useful coding agents can be effective with simple
  repo context, tool interfaces, linear history, shell execution, and trajectories.
  They argue against adding a broad registry, repo-map, git automation, Docker
  execution, or full trajectory store inside T007_03.

## Harness Decision

### Answer

Implement `T007_03` as a harness now, but keep the harness narrow.

Recommended first shape:

```java
@Service
class ChatHarnessService {
    CodegeistChatResponse ask(boolean continueSession, String prompt);
}

interface CodegeistToolService {
    CodegeistToolRun openRun(CodegeistConfig config, Path workingDirectory);
}

interface CodegeistToolRun {
    CodegeistChatExecutionContext executionContext();
    List<ToolSessionPart> completedToolParts();
}

record CodegeistChatExecutionContext(
        Path workingDirectory,
        List<ToolCallback> toolCallbacks,
        CodegeistToolRecorder recorder) {
}
```

`AskCommands` should become a thin Spring Shell adapter that calls
`ChatHarnessService` and prints the response. Later TUI work can call the same
service or a nearby harness method without duplicating tool/session orchestration.

### Evidence

- OpenCode resolves local and MCP tools at session execution time in
  `packages/opencode/src/session/tools.ts`.
- OpenCode's `SessionTools.resolve(...)` merges `ToolRegistry` tools and MCP tools,
  wraps execution with plugin hooks, permission checks, truncation, and processor
  callbacks.
- OpenCode's `SessionProcessor` owns tool-part lifecycle and persistence in
  `packages/opencode/src/session/processor.ts`.
- Spring AI Agent Utils shows Spring-friendly callback composition through
  `MethodToolCallbackProvider` in
  `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/advisors/AutoMemoryToolsAdvisor.java`.
- Agent Utils explicit `FunctionToolCallback` builders in
  `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/TaskTool.java`
  show that explicit callback objects are viable when a tool needs more control
  than singleton `@Tool` methods.

### Codegeist Translation

- Use `ChatHarnessService` to keep one prompt-turn operation coherent.
- Use `CodegeistToolRun` for per-turn resources, especially closeable stdio MCP
  clients.
- Use explicit recording wrappers around every local and MCP callback so session
  persistence is independent of Spring AI internal tool execution details.
- Keep `CodegeistChatService` as provider boundary, with a context-aware overload
  or equivalent that accepts runtime callbacks without changing `CodegeistChatRequest`.

## Additional Coding-Agent Harness Comparisons

The full comparison table lives in
`docs/tasks/T007_build-codegeist-runtime-harness/coding-agent-harness-implementations.md`.
The focused local Aider and mini-SWE-agent expansion lives in
`docs/tasks/T007_build-codegeist-runtime-harness/aider-mini-swe-harness-research.md`.
For T007_03, the most relevant additional open-source counterexamples are Aider,
SWE-agent, and mini-SWE-agent because they prove that a practical coding-agent
harness can be intentionally smaller than OpenCode-style MCP/plugin/server systems.

### Summary Table

| Project | Harness pattern evidence | T007_03 translation |
| --- | --- | --- |
| Aider | `Coder` state, repo map, file-edit/diff workflow, git integration, lint/test loop, chat history. | Keep Codegeist repo interaction focused on read/list/glob/grep/write now. Do not add repo-map, automatic commits, or lint/test automation in T007_03. |
| SWE-agent | `ToolHandler`, config-driven tool commands, environment execution, trajectory recording, issue-resolution loop. | Add explicit tool boundaries and record bounded tool activity, but keep `.codegeist/session.json` smaller than a full benchmark trajectory. |
| mini-SWE-agent | `DefaultAgent`, model plus environment, linear messages, bash-only independent actions, optional trajectory output. | Preserve the narrow loop. Avoid broad registries, live shell sessions, and extra lifecycle state until a focused child needs them. |

### Aider Analysis

Aider is a terminal pair-programming agent centered on repository context,
file-editing, git, and verification loops rather than MCP. Public source evidence
includes:

- `aider/coders/base_coder.py` for persistent coder state, selected files, chat
  history, repo map, and edit bookkeeping.
- `aider/repomap.py` for repository map generation and ranked context selection.
- `aider/commands.py` for command handling, token accounting, and repo-map display.
- Aider documentation for git integration, lint/test execution, and automatic
  commits.

T007_03 impact:

- Aider supports keeping repository awareness as a harness responsibility, not a
  provider-model responsibility.
- Aider does not require MCP to be a useful coding agent; this reinforces that MCP
  should be only one tool source in Codegeist, not the whole harness.
- Do not add Aider-like repo maps, automatic commits, lint/test repair loops, or
  patch strategy in T007_03. Those are later context, verification, or patch/edit
  concerns.
- The useful immediate lesson is bounded, explicit context: tools should return
  concise file/search results rather than dumping large repository state into the
  model or session store.

### SWE-agent Analysis

SWE-agent is a research-oriented software-engineering agent with a configurable
agent-computer interface. Public source evidence includes:

- `sweagent/tools/tools.py`, especially `ToolHandler`, for installing tools,
  parsing model-issued commands, and handling multiline command input.
- SWE-agent configuration docs for declaring the tools available to a run.
- SWE-agent trajectory and SWE-bench docs for recording model interaction, actions,
  observations, and issue-solving evidence.

T007_03 impact:

- SWE-agent supports an explicit tool boundary: the harness interprets model intent,
  validates/parses it, executes in an environment, and returns observations.
- Its trajectory model is useful as audit evidence, but T007_03 should not copy a
  full benchmark trajectory store. Codegeist needs only bounded `ToolSessionPart`
  entries inside `.codegeist/session.json`.
- SWE-agent's configurable tool interface supports putting tool activation in
  runtime/config state, not in persisted chat history. This matches the T007 rule
  that enabled tools and permission policy stay out of `.codegeist/session.json`.
- Do not add SWE-agent-style benchmark runners, custom issue loops, or broad
  environment abstraction in T007_03.

### mini-SWE-agent Analysis

mini-SWE-agent is the strongest minimality reference. Public source and docs show:

- `src/minisweagent/agents/default.py` has a compact `DefaultAgent` that owns model,
  environment, config, and linear `messages` state.
- `src/minisweagent/agents/interactive.py` adds human/confirm/yolo interaction modes
  around the same small agent loop.
- The README explains that mini-SWE-agent intentionally uses only bash, appends every
  step to linear history, and runs independent actions through `subprocess.run`
  rather than keeping a stateful shell session.

T007_03 impact:

- mini-SWE-agent supports keeping `ChatHarnessService` small and direct. A useful
  coding-agent harness does not require a broad plugin system, server runtime,
  database, or background shell manager.
- Its linear-history choice maps well to Codegeist's `.codegeist/session.json` turn
  storage: append user text, bounded tool parts, and assistant text in order.
- Its bash-only design is a reminder not to overfit T007_03 around many local file
  tools. Codegeist needs the current read/list/glob/grep/write set because this task
  explicitly scopes those tools, but shell and richer execution remain T007_04.
- Do not copy mini-SWE-agent's bash-only interface directly because T007_03 requires
  structured Spring AI callbacks and MCP compatibility.

## OpenCode Answers

### Tool System Evidence Table

| Area | Source files | Behavior | Codegeist translation |
| --- | --- | --- | --- |
| Tool definition | `packages/opencode/src/tool/tool.ts` | Tool has `id`, description, parameter schema, optional JSON schema, `execute(...)`, validation error formatting, and global output truncation wrapping. | Add Codegeist-owned local tool descriptors or callback wrappers; keep schema and execution separate from persisted session state. |
| Built-in registry | `packages/opencode/src/tool/registry.ts` | Initializes built-ins, custom files, and plugin tools; filters by model/agent. | Do not add broad plugin/custom registry in T007_03; use a fixed local tool set plus dynamic MCP callbacks. |
| Tool resolution | `packages/opencode/src/session/tools.ts` | Merges registry tools and MCP tools at session execution time. | Use `CodegeistToolService.openRun(...)` to produce the per-turn callback list. |
| Tool lifecycle | `packages/opencode/src/session/processor.ts` | Creates pending tool parts, updates running input, settles completed/error states, and handles interrupted tools. | Persist `ToolSessionPart` as an assistant part with `callId`, `tool`, `status`, input, bounded output/error, and optional metadata. |
| Replay to model | `packages/opencode/src/session/message-v2.ts` | Converts completed/error tool parts back into model messages and handles interrupted pending/running tools. | Codegeist can defer provider-facing history replay, but the persisted shape should be replayable later. |
| Output bounds | `packages/opencode/src/tool/truncate.ts`, `read.ts`, `glob.ts`, `grep.ts` | Global output truncation plus tool-specific caps. | Add `ToolOutputBounds`; persist previews and truncation metadata only. |
| Permissions | `packages/opencode/src/permission/index.ts` | Rules default to `ask`; `deny` fails; `allow` proceeds; disabled tools are filtered before model exposure. | Defer interactive permission UI, but centralize side-effect classification and path policy so future permission checks have one place to plug in. |
| Workspace boundary | `packages/opencode/src/project/instance-context.ts`, `packages/opencode/src/tool/external-directory.ts` | Current directory and worktree are local; outside paths require `external_directory` permission. | Add `WorkspacePolicy` with real-path checks and no external-directory allowance in first slice unless user config later adds it. |
| MCP | `packages/core/src/v1/config/mcp.ts`, `packages/opencode/src/mcp/index.ts` | Local/remote/OAuth MCP config, status, clients, resources, prompts, tools, and sanitized names. | Support Codegeist `stdio` only in T007_03; defer remote, OAuth, resources, prompts, and management commands. |

### Prompt-To-Tool Call Flow

OpenCode flow:

1. Prompt submission enters session handling and creates or selects session/message
   state.
2. Session execution resolves agent, model, history, and tools.
3. `SessionTools.resolve(...)` builds model-visible tools from built-ins and MCP.
4. Provider streaming starts.
5. Tool input/call/result stream events are processed by `SessionProcessor`.
6. Tool parts move through pending/running/completed/error states.
7. Completed/error tool parts are persisted and can be replayed to model messages.

Relevant source paths:

- `packages/opencode/src/session/tools.ts`
- `packages/opencode/src/session/processor.ts`
- `packages/opencode/src/session/message-v2.ts`
- `packages/opencode/src/tool/tool.ts`

Codegeist answer:

- Do not wire Spring AI callbacks directly from `AskCommands` to local tool methods.
- Add a harness that can create callbacks, record activity, and save bounded parts
  in one place.
- Keep prompt admission and session store writes explicit around the provider call.

### Local File Tool Behavior

OpenCode local tools:

- `read` can read files and list directories, resolves relative paths against the
  instance directory, checks external-directory policy, detects binary files, caps
  bytes and line lengths, and returns display metadata.
  Source: `packages/opencode/src/tool/read.ts`.
- `glob` resolves the search path, asks permission, rejects file search paths, checks
  external-directory policy, caps results at 100, and returns truncation metadata.
  Source: `packages/opencode/src/tool/glob.ts`.
- `grep` searches file or directory paths, supports include filters, checks
  external-directory policy, caps matches at 100, and truncates long match lines.
  Source: `packages/opencode/src/tool/grep.ts`.
- `write` creates a diff, asks `edit` permission, writes with parent directory
  creation, preserves BOM, formats when possible, publishes file events, and returns
  diagnostics.
  Source: `packages/opencode/src/tool/write.ts`.

Codegeist answer:

- Implement separate `codegeist_read`, `codegeist_list`, `codegeist_glob`,
  `codegeist_grep`, and `codegeist_write` callbacks because Spring AI callback names
  are global within a prompt turn.
- Use OpenCode's behavior as evidence for bounds and result metadata, but keep the
  first slice smaller.
- Keep `write` create/overwrite only and reject missing parents unless the user
  explicitly wants parent creation. OpenCode creates parents, but that is broader
  and tied to edit permission and formatter/LSP flows.

### MCP Behavior

OpenCode MCP config supports:

- Local servers with `type: local`, `command`, `environment`, `enabled`, and
  `timeout`.
- Remote servers with `type: remote`, `url`, `headers`, OAuth, `enabled`, and
  `timeout`.
- Runtime status values such as connected, disabled, failed, needs auth, and needs
  client registration.
- Tool, prompt, and resource listing.
- Tool-name sanitization and client-name prefixing.

Relevant source paths:

- `packages/core/src/v1/config/mcp.ts`
- `packages/opencode/src/mcp/index.ts`
- `packages/opencode/src/mcp/auth.ts`
- `packages/opencode/src/cli/cmd/mcp.ts`

Codegeist answer:

- Keep T007_03 to direct `codegeist.yml` stdio clients with `type`, `command`, and
  `args`.
- Build Spring AI MCP callbacks lazily during a chat run.
- Close stdio clients through the MCP-specific resource scope when the MCP adapter is
  implemented; the current local-only `CodegeistToolRun` is not closeable.
- Prefix/sanitize MCP callback names if Spring AI does not already guarantee unique
  names. Prefer `<mcp-id>_<tool-name>` for MCP tools and `codegeist_*` for local
  tools.
- Do not persist MCP status, client definitions, prompts, resources, OAuth, or
  tool definitions in `.codegeist/session.json`.

### Workspace And Permission Behavior

OpenCode has a two-level local boundary: current instance directory and git
worktree. Anything outside those boundaries can proceed only through an
`external_directory` permission request.

Relevant source paths:

- `packages/opencode/src/project/instance-context.ts`
- `packages/opencode/src/tool/external-directory.ts`
- `packages/opencode/src/permission/index.ts`

Codegeist answer:

- First slice should be stricter than OpenCode: reject anything outside the active
  working directory instead of asking for external-directory permission.
- Add a `WorkspacePolicy` now, even if the first implementation only rejects.
- Path rules should include normalized relative resolution, absolute path containment,
  real-path containment for existing paths, symlink escape rejection, and protected
  `.codegeist/session.json` rejection for writes.

### Output Bounds And Persistence

OpenCode uses global truncation defaults of 2000 lines and 50 KiB, plus specific
tool caps such as read 50 KiB, line length 2000, glob 100 results, grep 100
matches, and grep line length 2000.

Relevant source paths:

- `packages/opencode/src/tool/truncate.ts`
- `packages/opencode/src/tool/read.ts`
- `packages/opencode/src/tool/glob.ts`
- `packages/opencode/src/tool/grep.ts`

Codegeist answer:

- Use a centralized `ToolOutputBounds` service with smaller, deterministic defaults
  from the spec unless tests prove they are too small.
- Persist only bounded `outputPreview` plus `truncated`, `omittedCharacters`, and
  `resultCount` where known.
- Do not implement OpenCode-style spill files in T007_03 unless a focused test needs
  it. A later shell/large-output task can add `.codegeist/tool-output/`.

### Session Persistence

OpenCode stores tool activity as assistant message parts and converts those parts
back into model messages during replay. Completed tool parts include output,
metadata, title, attachments, and time. Errors include error text and time. Pending
or running parts become interrupted tool results during model replay.

Relevant source paths:

- `packages/opencode/src/session/processor.ts`
- `packages/opencode/src/session/message-v2.ts`
- `packages/core/src/session/sql.ts`
- `packages/core/src/session/projector.ts`

Codegeist answer:

- Persist tool parts inside the assistant message before the final assistant text.
- Keep T007_03 statuses to `completed` and `failed` unless the harness needs
  `running` for immediate TUI rendering. Since T007_06 owns TUI, `running` can be
  deferred.
- Include enough fields for future replay and UI rendering:
  `callId`, `tool`, `status`, `input`, `outputPreview`, `truncated`,
  `omittedCharacters`, `resultCount`, `affectedPaths`, and `errorMessage`.
- Defer timestamps, attachments, title, metadata, progress, cancellation, and
  timeout fields until patch/shell/TUI tasks need them.

## Spring AI Agent Utils Answers

### Agent Utils Evidence Table

| Area | Source files | Behavior | Codegeist reuse answer |
| --- | --- | --- | --- |
| `@Tool` methods | `FileSystemTools.java`, `GlobTool.java`, `GrepTool.java`, `ListDirectoryTool.java` | Exposes simple Java methods as model-callable tools. | Useful pattern, but not enough for per-chat working dir plus persistence. |
| `MethodToolCallbackProvider` | `AutoMemoryToolsAdvisor.java`, `ClaudeSubagentType.java` | Converts `@Tool` objects into callback arrays and adds them to chat options. | Good registration reference; do not rely on singleton tools for T007_03. |
| `FunctionToolCallback` | `TaskTool.java`, `TaskOutputTool.java`, `SkillsTool.java` | Builds explicit callbacks with typed input classes. | Best pattern for Codegeist wrappers when callbacks need policy and recorder state. |
| MCP callback provider | `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java` | Injects Spring AI `ToolCallbackProvider` from Spring Boot MCP auto-config and passes it to `ChatClient`. | Use as evidence that `ToolCallbackProvider` is a good seam; keep Codegeist `mcp:` config private. |
| Read/write | `FileSystemTools.java` | Reads/writes absolute paths, returns strings, creates parents for writes, no Codegeist policy. | Reimplement or wrap very tightly; do not expose directly. |
| List | `ListDirectoryTool.java` | Lists directories with depth/limit and common ignored dirs. | Source inspiration; implement smaller deterministic Codegeist list. |
| Glob | `GlobTool.java` | Java NIO glob, follows links, max depth/results, sorts by modification time. | Reimplement for stable lexicographic output and symlink safety. |
| Grep | `GrepTool.java` | Java regex search with modes, context, type filters, caps, and truncation. | Reimplement minimal grep now; copy concepts only. |
| Shell | `ShellTools.java` | Broad shell/background process behavior. | Defer to T007_04; do not include in T007_03. |

### Tool Registration And Context

Agent Utils primarily uses singleton objects with `@Tool` methods, then registers
them through `defaultTools(...)` or `MethodToolCallbackProvider`. It also uses
explicit `FunctionToolCallback` for more structured tools.

Relevant source paths:

- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/FileSystemTools.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GlobTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GrepTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/ListDirectoryTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/advisors/AutoMemoryToolsAdvisor.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/TaskTool.java`

Codegeist answer:

- Use explicit `ToolCallback` wrappers or `FunctionToolCallback`-style builders for
  local tools.
- Avoid global singleton `@Tool` beans for file tools because T007_03 needs per-run
  working directory, active session store path, recorder, and cleanup state.
- `ToolContext` is useful in Spring AI generally, but the inspected Agent Utils
  source does not rely on it for these tools. Do not make `ToolContext` the only
  way to carry Codegeist policy state.

### File Tool Reuse

Agent Utils file tools are too permissive for direct Codegeist use:

- `FileSystemTools.read(...)` takes an absolute file path, reads with offset/limit,
  truncates lines over 2000 chars, and returns string errors for missing files and
  directories.
- `FileSystemTools.write(...)` creates parent directories and overwrites files, but
  it does not enforce the prompt text's "must read first" rule in code and does not
  protect `.codegeist/session.json`.
- `ListDirectoryTool` has useful depth/limit and ignored-directory ideas but does
  not enforce Codegeist path policy.
- `GlobTool` follows links and sorts by modification time; Codegeist wants stable
  lexicographic output and symlink escape rejection.
- `GrepTool` supports a broad feature set, including output modes and context, which
  is more than T007_03 needs.

Relevant source paths:

- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/FileSystemTools.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/ListDirectoryTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GlobTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GrepTool.java`

Codegeist answer:

- Implement Codegeist local tools directly with Java NIO and Codegeist policies.
- Reuse Agent Utils behavior only as test and UX inspiration.
- If a later implementation delegates to Agent Utils, delegate only behind
  `WorkspacePolicy`, `ToolOutputBounds`, and `RecordingToolCallback`.

### Agent Utils MCP Evidence

Agent Utils does not provide a reusable core MCP adapter for Codegeist. Its demo
uses Spring Boot MCP auto-configuration:

- `spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config.json`
  in `examples/code-agent-demo/src/main/resources/application.properties`.
- `ToolCallbackProvider mcpToolCallbackProvider` injection in
  `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`.
- `ChatClient.Builder.defaultToolCallbacks(mcpToolCallbackProvider)` for runtime
  registration.

Codegeist answer:

- Keep Spring AI `ToolCallbackProvider` as a test seam.
- Map Codegeist `McpClientConfig` to Spring AI MCP programmatically instead of
  binding Spring Boot `spring.ai.mcp.client.*` as public config.

### Agent Utils Test Patterns

Useful tests to mirror conceptually:

- `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/FileSystemToolsTest.java`
- `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GlobToolTest.java`
- `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolTest.java`
- `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/advisors/AutoMemoryToolsAdvisorTest.java`

Codegeist-specific tests still needed:

- Path traversal rejection.
- Symlink escape rejection.
- Protected `.codegeist/session.json` write rejection.
- Bounded persisted `ToolSessionPart` previews.
- Fake MCP callback provider without launching real network-dependent servers.
- Chat harness test proving tool parts are saved with the assistant response.

## Recommended Codegeist Tool Contracts

### `codegeist_read`

- Inputs: `path`, optional `offset`, optional `limit`.
- Reject outside workspace, directories, missing files, symlink escapes, and binary
  files.
- Return line-numbered bounded text.
- Persist bounded preview, result count, truncation flags.

### `codegeist_list`

- Inputs: optional `path`, optional `limit`.
- Non-recursive in T007_03.
- Reject outside workspace and file paths.
- Return relative entries with file/directory markers in stable order.

### `codegeist_glob`

- Inputs: `pattern`, optional `path`, optional `limit`.
- Use Java NIO glob or small deterministic traversal.
- Return relative paths in stable lexicographic order.
- Do not follow symlink escapes.

### `codegeist_grep`

- Inputs: `pattern`, optional `path`, optional `include`, optional
  `caseInsensitive`, optional `limit`.
- Use Java regex.
- Return relative path, line number, and bounded line preview.
- Defer context lines, output modes, type filters, multiline, and shell/ripgrep
  fallback.

### `codegeist_write`

- Inputs: `path`, `content`.
- Create or overwrite regular text files only.
- Reject outside workspace, symlink targets, directories, missing parent
  directories, and active `.codegeist/session.json`.
- Persist affected relative path, created/overwritten flag, byte or character count,
  and bounded content preview.
- Do not create parent directories in T007_03 unless the user changes the spec.
- Do not implement patch/edit semantics.

## Recommended `ToolSessionPart`

Map OpenCode and Agent Utils evidence to this minimal Codegeist shape:

| Field | Decision | Reason |
| --- | --- | --- |
| `id` | Keep | Existing `SessionPart` id pattern. |
| `type` | Keep as `tool` | Durable part discriminator. |
| `callId` | Keep | Needed to correlate model call and result. |
| `tool` | Keep | Needed for rendering and future replay. |
| `status` | Keep `completed` / `failed` first | T007_03 persists after call completion; live states can wait for TUI. |
| `input` | Keep sanitized bounded scalar map | Needed for UI and replay; must not store secrets. |
| `outputPreview` | Keep | Model-visible bounded output and UI rendering. |
| `truncated` | Keep | Explains whether output is complete. |
| `omittedCharacters` | Keep when known | Useful for bounds and diagnostics. |
| `resultCount` | Keep when relevant | Useful for list/glob/grep/read. |
| `affectedPaths` | Keep for write | Useful for mutation rendering. |
| `errorMessage` | Keep for failures | Needed for failed tool rendering. |
| `createdAt` / `completedAt` | Defer | Existing message timestamps are enough for first persisted slice. |
| `title` / `metadata` | Defer | OpenCode uses them, but T007_03 does not need broad metadata. |
| `attachments` | Defer | Not needed for read/write text tools. |
| `outputPath` spill file | Defer | Useful later for shell/large outputs. |
| `running` / `cancelled` / `timed_out` | Defer | TUI/shell tasks can add live states when needed. |

## Test Plan Answers

Recommended implementation test order:

1. `WorkspacePolicyTest`
   - Reject traversal, absolute outside paths, existing symlink escapes, write
     symlink targets, and active session store writes.
2. `ToolOutputBoundsTest`
   - Bound characters, line lengths, result counts, omitted counts, and error text.
3. `CodegeistLocalToolsTest`
   - Prove read/list/glob/grep/write success paths and focused failure paths.
4. `CodegeistMcpAdapterTest`
   - Prove Codegeist stdio config maps to the adapter and unsupported types fail.
5. `CodegeistToolServiceTest`
    - Prove local callbacks plus fake MCP callbacks are exposed for a run and that
      any MCP-specific resource scope closes resources once MCP is implemented.
6. `SessionStoreServiceTest`
   - Prove `ToolSessionPart` JSON round-trip and bounded persistence.
7. `ChatHarnessServiceTest` or `AskCommandsSessionStoreTest`
   - Prove a tool-aware chat run saves prompt, tool parts, and assistant text while
     stdout remains response-only through the command layer.

Focused verification command after implementation:

```bash
task test TEST=WorkspacePolicyTest,ToolOutputBoundsTest,CodegeistLocalToolsTest,CodegeistMcpAdapterTest,CodegeistToolServiceTest,SessionStoreServiceTest,ChatHarnessServiceTest
```

Final JVM verification after implementation:

```bash
task test
```

## Specification Changes Recommended

Update `mcp-and-readwrite-tools-spec.md` before Java implementation with these
decisions:

- Promote the narrow harness from optional design to planned architecture:
  `ChatHarnessService` plus subordinate `CodegeistToolRun`.
- Keep local file tools as Codegeist-owned Java implementations for the first
  slice, not direct Agent Utils runtime tools.
- Keep Agent Utils as source inspiration and optional private delegate only after
  Codegeist policy and persistence wrappers exist.
- Keep MCP stdio-only in T007_03, mapped privately into Spring AI MCP callbacks.
- Keep write parent-directory creation out of T007_03 unless the user explicitly
  wants OpenCode-compatible parent creation.
- Keep live tool states, timing fields, attachments, metadata, and output spill
  files deferred unless a focused test requires them.

## Answered Catalog Coverage

This research answers the catalog at the category level:

- OpenCode evidence questions: answered through tool system table, prompt flow,
  file-tool behavior, MCP behavior, workspace policy, output bounds, session
  persistence, permission behavior, and test recommendations.
- Spring AI Agent Utils evidence questions: answered through registration APIs,
  file-tool reuse decisions, MCP callback evidence, bounds/errors, and tests.
- Harness design questions: answered with the narrow `ChatHarnessService` plus
  `CodegeistToolRun` recommendation.
- MCP-specific questions: answered with stdio-only Codegeist mapping and deferred
  remote/OAuth/resources/prompts/status persistence.
- File tool contract questions: answered with recommended input/output contracts.
- Session persistence questions: answered with `ToolSessionPart` mapping.
- Test and verification questions: answered with a concrete test order and focused
  Taskfile commands.
- Synthesis questions: answered in the executive answers and specification-change
  recommendations.

## Open Decisions For The User

- Should `codegeist_write` create parent directories like OpenCode, or reject missing
  parents for a safer first slice? This research recommends rejecting missing
  parents.
- Should `ToolSessionPart` include `createdAt` and `completedAt` now? This research
  recommends deferring timing until TUI or shell needs it.
- Should Codegeist add an output spill file path in T007_03? This research
  recommends deferring spill files until shell or large-output work.
- Should MCP tools be prefixed as `<mcp-id>_<tool-name>` even when Spring AI exposes
  names directly? This research recommends prefixing or otherwise guaranteeing
  uniqueness.
