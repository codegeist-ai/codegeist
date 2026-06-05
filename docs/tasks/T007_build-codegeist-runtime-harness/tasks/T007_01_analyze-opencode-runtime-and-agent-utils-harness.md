# T007_01 Analyze OpenCode Runtime And Agent Utils Harness

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Complete a source-backed analysis pass for the runtime harness before Codegeist
starts implementing the next core slices.

The deliverable is a compact but deep handoff that explains how OpenCode implements
the user-facing runtime harness and which Spring AI or Spring AI Agent Utils pieces
Codegeist can use safely behind its own Java/Spring boundaries.

## Why This Comes First

The user goal is a real OpenCode replacement, not only a CLI prompt command. That
means Codegeist needs to understand OpenCode's full harness: TUI, session loop,
events, tools, permissions, workspace access, provider/model flow, storage, and
plugin/MCP surfaces. Implementation should start only after the exact OpenCode
behavior and Java/Spring-side replacement options are clear enough to split into
focused tested tasks.

## Already Gathered OpenCode Source Map

Use these files as the starting map for detailed questions and citations:

| Topic | Source paths |
| --- | --- |
| CLI bootstrap | `docs/third-party/opencode/source/packages/opencode/src/index.ts` |
| Headless run path | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/run.ts` |
| Server command | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/serve.ts` |
| Server assembly | `docs/third-party/opencode/source/packages/opencode/src/server/server.ts` |
| TUI SDK and SSE subscription | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/context/sdk.tsx` |
| TUI event filtering | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/context/event.ts` |
| TUI state projection | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/context/sync.tsx` |
| TUI prompt input | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/component/prompt/index.tsx` |
| TUI session screen | `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/routes/session/index.tsx` |
| Core prompt orchestration | `docs/third-party/opencode/source/packages/opencode/src/session/prompt.ts` |
| Stream processing | `docs/third-party/opencode/source/packages/opencode/src/session/processor.ts` |
| LLM integration | `docs/third-party/opencode/source/packages/opencode/src/session/llm.ts` |
| Message and part shape | `docs/third-party/opencode/source/packages/opencode/src/session/message-v2.ts` |
| Per-session run state | `docs/third-party/opencode/source/packages/opencode/src/session/run-state.ts` |
| Native agents and modes | `docs/third-party/opencode/source/packages/opencode/src/agent/agent.ts` |
| User-defined agents | `docs/third-party/opencode/source/packages/opencode/src/config/agent.ts` |
| Tool contract | `docs/third-party/opencode/source/packages/opencode/src/tool/tool.ts` |
| Tool registry | `docs/third-party/opencode/source/packages/opencode/src/tool/registry.ts` |
| Permission lifecycle | `docs/third-party/opencode/source/packages/opencode/src/permission/index.ts` |
| Permission rule evaluation | `docs/third-party/opencode/source/packages/opencode/src/permission/evaluate.ts` |
| SQLite storage | `docs/third-party/opencode/source/packages/opencode/src/storage/db.ts` |
| Session tables | `docs/third-party/opencode/source/packages/opencode/src/session/session.sql.ts` |
| Sync/event model | `docs/third-party/opencode/source/packages/opencode/src/sync/index.ts` |
| Runtime bus | `docs/third-party/opencode/source/packages/opencode/src/bus/index.ts` |
| Provider and model loading | `docs/third-party/opencode/source/packages/opencode/src/provider/provider.ts` |
| Auth storage | `docs/third-party/opencode/source/packages/opencode/src/auth/index.ts` |
| MCP | `docs/third-party/opencode/source/packages/opencode/src/mcp/index.ts` |
| Plugins | `docs/third-party/opencode/source/packages/opencode/src/plugin/index.ts`, `docs/third-party/opencode/source/packages/plugin/src/index.ts` |

## Already Gathered Spring AI And Agent Utils Map

Spring AI:

- `ToolCallback` owns tool definition, metadata, and execution methods.
- `ChatClient` can register `toolCallbacks(...)` for a specific request or tools
  through `@Tool`-annotated objects.
- Tool execution can be handled internally by Spring AI providers unless Codegeist
  keeps the callback boundary Codegeist-owned.

Spring AI Agent Utils:

| Topic | Source paths | Current posture |
| --- | --- | --- |
| Grep/glob | `GrepTool.java`, `GlobTool.java` | Candidate private implementation behind Codegeist workspace and output policy. |
| File/list | `ListDirectoryTool.java`, `FileSystemTools.java` | Candidate private implementation for read-only tools after path, size, and redaction checks. |
| Shell | `ShellTools.java`, `AgentEnvironment.java` | Concept/reference only until Codegeist shell policy exists. |
| Questions | `AskUserQuestionTool.java`, `CommandLineQuestionHandler.java` | Wrap through Codegeist events and client renderers. |
| Todos | `TodoWriteTool.java` | Concept reference for progress/session parts. |
| Skills/memory | `SkillsTool.java`, `AutoMemoryTools.java`, `AutoMemoryToolsAdvisor.java` | Defer or wrap because Codegeist must own trust, prompt, storage, and retention policy. |
| Task/subagents | `tools/task/TaskTool.java`, A2A module docs | Defer until child sessions, cancellation, storage, auth, and remote trust decisions exist. |

Useful Agent Utils tests to inspect as behavior references:

- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolTest.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GlobToolTest.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/FileSystemToolsTest.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/ShellToolsTest.java`

## Research Questions To Answer

- How exactly does OpenCode turn CLI/TUI prompt input into a session run?
- Which state is stored as durable messages, parts, permissions, todos, and sync
  events?
- Which events are required for a useful terminal/TUI client before full-screen UI
  polish exists?
- How do OpenCode build, plan, general, and explore agents restrict tools and
  permissions?
- How does OpenCode resolve tools from built-ins, plugins, MCP, and model/provider
  capabilities?
- How does OpenCode present, persist, and replay permission decisions such as
  one-shot approvals and longer-lived approvals?
- Which OpenCode tool behaviors are foundational for Codegeist replacement parity:
  read/list/glob/grep, patch/edit, shell, web fetch, task/subagent, MCP, plugins,
  LSP, or all of them?
- Which Spring AI Agent Utils tools can be used directly after Codegeist validates
  policy, and which must stay concept-only?
- Which Spring AI tool-calling configuration lets Codegeist preserve policy before
  a tool executes?

## Deliverables

- Add an `Evidence Result` section to this task or create a focused sibling handoff
  under `docs/tasks/T007_build-codegeist-runtime-harness/hints/` if the findings
  become too large for this file.
- Include source-path citations for OpenCode and Agent Utils claims that affect
  implementation ordering.
- Create small Mermaid diagrams when they make multi-file prompt, event, tool, or
  approval flows easier to implement later.
- Update later T007 child tasks if this analysis changes sequencing, non-goals, or
  acceptance criteria.

## Acceptance Criteria

- The task names the exact OpenCode files that implement prompt flow, session/event
  projection, TUI rendering, tool registry, permission flow, storage, MCP, and
  plugin entrypoints.
- The task identifies which OpenCode behaviors are required for the first Codegeist
  replacement harness and which stay deferred.
- The task records Spring AI tool-calling decisions relevant to Codegeist policy.
- The task classifies Agent Utils capabilities as adopt, wrap, avoid, or defer.
- The task does not add runtime Java source or implementation dependencies beyond
  documentation updates.

## Non-Goals

- Do not implement TUI, runtime, tools, permissions, storage, MCP, plugins, or
  Spring AI tool callbacks in this research task.
- Do not run OpenCode TUI/server runtime unless a later explicit verification task
  asks for it.
- Do not copy OpenCode TypeScript, storage schemas, package names, or UI framework
  structure into Codegeist.

## Verification

Run:

```bash
git --no-pager diff --check
```
