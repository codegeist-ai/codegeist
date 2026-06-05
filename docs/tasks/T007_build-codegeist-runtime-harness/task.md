# T007 Build Codegeist Runtime Harness

Status: open

## Goal

Implement the first Codegeist-owned runtime harness that can grow into a practical
replacement for OpenCode in local coding-agent workflows.

This parent task turns the already completed architecture and provider groundwork
into small implementation slices. The target is not a one-shot clone of OpenCode;
it is a Java-first harness that preserves Codegeist boundaries while delivering the
core behavior users need from OpenCode: terminal interaction, sessions, streaming
events, agent modes, tools, permissions, workspace safety, provider calls, and
eventual continuation.

## Why This Exists

Current Codegeist can start as a Spring Boot/Spring Shell application, load and
render provider config, call local Ollama through the provider-neutral chat seam,
and smoke that path in JVM/native artifacts. It does not yet have the central
runtime harness that OpenCode provides around prompts, sessions, tools, approval,
workspace policy, and TUI rendering.

This task creates the replacement implementation epic. It intentionally follows the
post-cleanup project rule: add Java source only when a focused test requires real
behavior. Do not recreate the removed broad implementation handoff documents or
placeholder Java contracts.

## Current Codegeist Baseline

- `app/codegeist/cli` is the only implemented application module.
- The application uses Java 25, Spring Boot 4.0.6, Spring Shell 4.0.2, Spring AI
  `2.0.0-M6`, Spring AI Agent Utils `0.7.0`, Lombok, and GraalVM native build
  tooling.
- Implemented commands are `--version`, `--show-config`, and one-shot `ask`.
- `ask` selects the first configured provider through `CodegeistConfig`, uses the
  provider config's `defaultModel()`, calls `CodegeistChatService`, and prints the
  response text.
- Provider config currently supports typed config-only `ollama` and `openai`
  entries. Runtime provider calls are implemented only for local Ollama through
  `OllamaChatModel`.
- `task test` from `app/codegeist/cli` is the implementation verification entrypoint
  and starts the fixed local Ollama container with `OLLAMA_ENTER=false` before
  Maven.
- `docs/developer/architecture/architecture.md` describes current state and must be
  updated whenever this task adds implemented packages, classes, config, runtime
  flows, or tests.

## Existing Tasks This Must Respect

- `T001_define-codegeist-opencode-feature-architecture` completed the
  documentation-only OpenCode feature architecture. Runtime owns orchestration;
  CLI/TUI/server clients render and collect input only.
- `T002_define-codegeist-mvp-foundation-blueprints` keeps the useful foundation
  baseline and explicitly says future runtime, session, event, context, provider,
  tool, permission, workspace, patch/edit, shell, storage, extension, and client
  work must be recreated as small tested implementation tasks.
- `T003_define-codegeist-opencode-core-source-contracts` is now implementation
  readiness guidance. Removed `T003_05` through `T003_12` must not be resurrected.
- `T006_build-provider-configuration-feature` provides provider config, local
  Ollama, provider feature-test categories, and the one-shot `ask` command. T007
  depends on this provider base instead of becoming another provider child.

## OpenCode Evidence Already Gathered

Use OpenCode as a behavior reference, not a Java package blueprint. The first
read-only exploration identified these high-value source paths:

| Area | Source paths |
| --- | --- |
| CLI bootstrap and headless run | `docs/third-party/opencode/source/packages/opencode/src/index.ts`, `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/run.ts`, `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/serve.ts`, `docs/third-party/opencode/source/packages/opencode/src/server/server.ts` |
| TUI client state and rendering | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/context/sdk.tsx`, `context/event.ts`, `context/sync.tsx`, `component/prompt/index.tsx`, `routes/session/index.tsx` |
| Session and prompt loop | `docs/third-party/opencode/source/packages/opencode/src/session/prompt.ts`, `session/processor.ts`, `session/llm.ts`, `session/message-v2.ts`, `session/run-state.ts` |
| Agents and modes | `docs/third-party/opencode/source/packages/opencode/src/agent/agent.ts`, `docs/third-party/opencode/source/packages/opencode/src/config/agent.ts` |
| Tool registry and execution | `docs/third-party/opencode/source/packages/opencode/src/tool/tool.ts`, `docs/third-party/opencode/source/packages/opencode/src/tool/registry.ts` |
| Permissions | `docs/third-party/opencode/source/packages/opencode/src/permission/index.ts`, `docs/third-party/opencode/source/packages/opencode/src/permission/evaluate.ts` |
| Storage and events | `docs/third-party/opencode/source/packages/opencode/src/storage/db.ts`, `docs/third-party/opencode/source/packages/opencode/src/session/session.sql.ts`, `docs/third-party/opencode/source/packages/opencode/src/sync/index.ts`, `docs/third-party/opencode/source/packages/opencode/src/bus/index.ts` |
| Server event streams | `docs/third-party/opencode/source/packages/opencode/src/server/routes/instance/event.ts`, `docs/third-party/opencode/source/packages/opencode/src/server/routes/instance/httpapi/event.ts` |
| Provider/auth/model resolution | `docs/third-party/opencode/source/packages/opencode/src/provider/provider.ts`, `docs/third-party/opencode/source/packages/opencode/src/auth/index.ts` |
| MCP and plugins | `docs/third-party/opencode/source/packages/opencode/src/mcp/index.ts`, `docs/third-party/opencode/source/packages/opencode/src/plugin/index.ts`, `docs/third-party/opencode/source/packages/plugin/src/index.ts` |

Important OpenCode observations to carry forward:

- OpenCode's core runtime is session oriented: input arrives from CLI/TUI/web/SDK,
  session processing builds prompts, provider/model selection happens before the LLM
  stream, tool requests become message parts/events, and permission decisions gate
  side effects.
- The TUI is a client over the same core runtime and projected event/session state,
  not a separate implementation of agent behavior.
- Tool execution is broad and includes built-ins, plugin-provided tools, MCP tools,
  file operations, shell, patch/edit, web, LSP, task/subagent, and permission
  mediation.
- Permission persistence includes concepts such as ask/reply and longer-lived
  approvals, but exact wildcard and `always` behavior still needs deeper source or
  runtime verification before Codegeist copies any policy.
- OpenCode has both legacy Hono and Effect HttpApi route surfaces. Codegeist should
  not copy this shape; server parity remains deferred until the runtime harness is
  stable.

## Spring AI And Agent Utils Evidence Already Gathered

Spring AI `ToolCallback` exposes tool definition, metadata, and `call(...)` methods.
`ChatClient` can receive runtime `toolCallbacks(...)` or `@Tool` objects, and model
providers can execute tool calling internally unless the application controls that
boundary carefully.

Spring AI Agent Utils `0.7.0` is already on the Codegeist classpath. It provides
Claude Code-inspired tools and helpers that are useful implementation references or
private dependencies, but Codegeist must own public runtime, provider, tool,
permission, workspace, event, session, storage, and client contracts.

High-value Agent Utils source paths:

| Capability | Source paths | Early recommendation |
| --- | --- | --- |
| Grep and glob | `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GrepTool.java`, `GlobTool.java` | Adopt or wrap as private implementation after Codegeist validates workspace and bounds output. |
| Directory and file reads | `ListDirectoryTool.java`, `FileSystemTools.java` | Use read/list concepts behind Codegeist path, size, ignore, and redaction policy. |
| Shell execution | `ShellTools.java`, `AgentEnvironment.java` | Defer until Codegeist shell permission, cwd, timeout, destructive-command, and output policy exist. |
| User questions | `AskUserQuestionTool.java`, `CommandLineQuestionHandler.java` | Wrap through Codegeist UI/permission events; do not let a console handler own TUI approval UX. |
| Todo/session progress | `TodoWriteTool.java` | Use concepts for session progress and event shape; avoid raw durable model reuse. |
| Skills and memory | `SkillsTool.java`, `AutoMemoryTools.java`, `AutoMemoryToolsAdvisor.java` | Defer or wrap; Codegeist must own trust, storage, redaction, retention, and context-size policy. |
| Task/subagents | `tools/task/TaskTool.java`, A2A module docs | Defer until child sessions, cancellation, storage, auth, and trust decisions exist. |

Provider exposure rule:

```text
Spring AI provider
  -> Codegeist-owned tool callback/service
  -> Codegeist mode, permission, workspace, event, session policy
  -> optional Agent Utils implementation detail
```

Avoid exposing raw broad Agent Utils tools directly to model providers because that
would bypass Codegeist policy.

## Child Tasks

- `T007_01_analyze-opencode-runtime-and-agent-utils-harness.md` - complete the
  source-backed harness analysis before implementation decisions.
- `T007_02_implement-runtime-session-event-spine.md` - add the first runtime,
  session/turn, and typed event path around the existing provider chat service.
- `T007_03_add-terminal-tui-client-harness.md` - add the first terminal TUI client
  over runtime events without creating a second runtime.
- `T007_04_add-tool-registry-and-read-only-workspace-tools.md` - add tool
  descriptors, registry, and read-only workspace tools for list/read/glob/grep.
- `T007_05_add-permission-and-side-effect-gates.md` - add mode, permission,
  approval, and workspace gates before side-effecting tools.
- `T007_06_wire-spring-ai-tool-calling-through-codegeist-policy.md` - expose
  Codegeist-owned tool callbacks to Spring AI and route model tool calls through
  policy.
- `T007_07_add-patch-edit-and-shell-tools.md` - add controlled patch/edit and shell
  tools after approval and workspace behavior are proven.
- `T007_08_add-session-storage-and-resume.md` - persist enough session, event,
  permission, and tool-result state for continuation and replay.

## Parent Acceptance Criteria

- The runtime harness accepts client input from CLI and TUI-oriented adapters
  without duplicating orchestration in the clients.
- A prompt run can create or continue a session/turn, emit typed runtime events,
  call the selected provider through existing provider-neutral services, and return
  renderable output.
- Tools are described by Codegeist-owned descriptors and executed only through
  Codegeist-owned services.
- Tool results can become runtime events and session parts without storing
  unbounded raw output as the durable model.
- Plan/build mode, permission policy, approval events, and workspace validation sit
  between model/tool requests and side effects.
- Read-only tools land before write, patch, shell, network, plugin, or subagent
  tools.
- Spring AI and Spring AI Agent Utils are private implementation dependencies unless
  a focused task explicitly promotes a Codegeist-owned boundary.
- Existing `--version`, `--show-config`, and `ask` command behavior keeps working.
- `docs/developer/architecture/architecture.md` is updated in each implementation
  child when packages, classes, configuration, runtime flows, or tests change.

## Non-Goals

- Do not implement PF4J, JBang, Vaadin, headless server, API, SDK/OpenAPI, plugin
  marketplace behavior, or MCP server management in the first harness slices.
- Do not copy OpenCode's Bun, TypeScript, Effect, Hono, OpenTUI/Solid, storage
  schemas, generated SDK, or package layout.
- Do not expose raw Agent Utils tools directly to Spring AI providers.
- Do not add placeholder packages, ids, ports, enums, records, interfaces, adapters,
  validation hierarchies, or empty directories before a focused test needs them.
- Do not broaden provider coverage or trigger hosted provider calls as part of the
  harness.
- Do not break the current noninteractive Spring Shell command path.

## Default Research Questions

Use `/ask-project opencode ...` and `/ask-project spring-ai-agent-utils ...` when
the task needs source-backed behavior details. High-value questions:

```text
/ask-project opencode "How does a user prompt flow from the CLI or TUI through session processing, provider streaming, tool execution, permissions, and event rendering? Cite source files."
/ask-project opencode "How are tools registered and executed, including MCP tools? Cite source files and identify policy boundaries."
/ask-project opencode "How does permission approval flow from tool request to user decision to execution? Cite source files."
/ask-project spring-ai-agent-utils "Which classes already implement grep, glob, list-directory, file read, shell, and question behavior, and what validation or output bounding do they apply? Cite source files and tests."
/ask-project spring-ai-agent-utils "How are ToolCallback and @Tool-based utilities built and registered, and where would a Codegeist wrapper need to map descriptors, permissions, and results? Cite source files."
```

## Verification

Documentation-only children should run:

```bash
git --no-pager diff --check
```

Implementation children should use the Taskfile from `app/codegeist/cli`:

```bash
task test TEST=<test-selector>
task test
```

For local provider checks:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=<test-selector>
```

Do not document direct `mvn test` commands for new implementation tasks. Use
`task native-smoke`, `task qemu-windows-smoke`, or `task final-smoke-suite` only
when command runtime, packaging, native behavior, or smoke contracts change.
