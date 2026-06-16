# Coding Agent Harness Implementations

Comparison of coding agents that implement the same practical pattern Codegeist
calls a coding-agent harness: a model wrapped by deterministic runtime services
for tools, workspace access, state, policy, execution, and user interfaces.

This document is source and documentation evidence for T007 planning. It is not a
current-state Codegeist architecture document.

## Interpretation

Most projects do not use the exact term `harness pattern`. Count a project as a
match when it has several of these runtime responsibilities around model calls:

| Harness responsibility | Evidence to look for |
| --- | --- |
| Tool orchestration | Tool registry, function-call router, tool callbacks, tool scheduler, MCP tools. |
| Workspace access | Read, search, edit, patch, shell, browser, sandbox, or repository-map tools. |
| Durable state | Sessions, trajectories, checkpoints, chat history, task state, or git-backed output. |
| Policy and safety | Permissions, approvals, trusted folders, sandboxing, path boundaries, or command gates. |
| Execution loop | Prompt loop, model-tool-model continuation, autonomous steps, retries, or verification. |
| Interface layer | CLI, TUI, IDE, server/API, web UI, desktop app, or headless mode over the same core. |

## Evidence Limits

- Local analyzed workspaces currently exist only for OpenCode and Spring AI Agent
  Utils under `docs/third-party/`.
- Other open-source projects below were inspected through public GitHub source,
  README files, and code-search evidence. Runtime tests were not executed.
- Closed-source or source-available products are listed only when public docs make
  the harness responsibilities visible. Treat those rows as public-doc-backed, not
  source-backed.
- Star counts and project status change quickly; use the project links as the
  source of truth when exact popularity or maintenance state matters.

## Comparison Table

| Agent | Evidence class | Harness pattern evidence | Codegeist T007 relevance |
| --- | --- | --- | --- |
| OpenCode | Local source analysis | `ToolRegistry`, session tool resolution, MCP service, permission policy, truncation, session/message/part persistence, CLI/TUI/server surfaces. | Strongest direct behavior reference for T007_03 tool lifecycle, MCP, bounded output, and persisted `ToolSessionPart`; do not copy server, SQLite, SDK, Effect, or TUI architecture. |
| Gemini CLI | Public source | `LocalAgentExecutor`, `ToolRegistry`, `McpClientManager`, scheduler/tool-call types, built-in file/shell/web tools, checkpointing, sandbox/trusted-folder docs. | Strong JavaScript/TypeScript reference for per-run registries, MCP discovery, approvals, and CLI/headless modes. |
| Qwen Code | Public source | Gemini-CLI-derived `ToolRegistry`, `McpClientManager`, agent runtime, permission classifier transcript, skills, subagents, deferred tools. | Useful for tool-surface filtering, deferred MCP/built-in tools, and permission-classifier framing. |
| OpenAI Codex CLI | Public source | Function-call response items, tool router tests, shell tool, approval/sandbox policy, rollout traces, TUI/local execution. | Useful for namespaced tool routing, shell-first local agent behavior, and trace/test discipline. |
| Claude Code | Public docs and source-available plugin repo | Terminal engine edits files and runs commands; settings scopes, permissions, hooks, MCP, subagents, memory, IDE/desktop/web surfaces. | Good product-level behavior reference for settings scopes, MCP naming, project memory, and permission UX; not a source blueprint. |
| Cline | Public source and docs | Shared agent core, `McpHub`, task services, filesystem/terminal/browser tools, human approval loop, checkpoints, Plan/Act mode, SDK/CLI/IDE surfaces. | Strong reference for approval-centered IDE/CLI tool UX and MCP hub lifecycle; broader than T007_03. |
| Roo Code | Public source and docs | Cline-derived provider/webview runtime, `McpHub`, `McpServerManager`, mode-specific tool filtering, skills, checkpoints, code index. | Useful for mode-based tool allowlists and MCP tool-name generation; current product status should be checked before relying on it. |
| Goose | Public source and docs | Rust `Agent`, `AgentConfig`, `SessionManager`, `PermissionManager`, MCP extensions, CLI/Desktop/API surfaces, scheduler, subagents. | Strong reference for session-scoped agent managers, permissions, MCP extensions, and API/desktop split; T007 should stay smaller. |
| Continue | Public source, archived/read-only | CLI stream tool-call handling, `callTool`, `ToolCallState`, MCP manager, chat-history/tool-state mapping. | Useful historical reference for tool state attached to chat history; avoid copying archived architecture. |
| Aider | Public source and docs | `Coder` state, repo map, file editing through diffs/patches, git integration, chat history, lint/test loop, automatic commits. | Simplifying reference: repo awareness and verification can be powerful without MCP. T007_03 should not add repo-map/git automation yet. |
| SWE-agent | Public source and docs | Config-driven agent-computer interface, `ToolHandler`, custom tool commands, environment execution, trajectories, SWE-bench issue loop. | Good reference for tool interface boundaries and trajectory/audit logs; T007_03 should keep session JSON smaller than benchmark trajectories. |
| mini-SWE-agent | Public source and docs | Minimal `DefaultAgent`, model plus environment, linear message history, bash-only execution, independent `subprocess.run` actions, trajectory output. | Best minimality reference: one narrow loop can be enough. Reinforces deferring broad registries, MCP complexity, and live shell sessions unless needed. |
| Trae Agent | Public source/docs | YAML agents/models/tools config, file editing, bash, sequential thinking, MCP servers, trajectory recording, Docker mode. | Useful research-friendly reference for explicit config and trajectory recording; broader Docker/MCP features should stay out of T007_03. |
| Crush | Public source | Go `ToolCall`/`ToolResult`, bash/edit/glob/search tool renderers, MCP clients/states/tools, workspace server and TUI. | Good TUI/tool-rendering reference for later T007_05; not needed for first T007_03 Java contracts. |
| OpenHands | Public docs and source split across repos | Software Agent SDK, CLI, local GUI/API, cloud agent, repository work, plans, execution, and hosted/local surfaces. | Product-level reference for SDK/GUI/cloud split; T007 explicitly avoids server/API/cloud runtime. |
| GitHub Copilot cloud agent | Public docs | Repository research, plan creation, branch edits, tests/linters in GitHub Actions environment, PR workflow, custom agents. | Cloud-agent pattern reference only. T007 is local file/session-store work, not GitHub Actions-backed cloud automation. |
| Cursor CLI | Public docs | Terminal coding agent for scripts and automation with local command workflow. | Behavior reference only; not enough source detail for implementation decisions. |
| Amp | Public docs | Sourcegraph coding agent CLI for tasks across codebases. | Behavior reference only; no source-backed T007 design input. |
| Devin | Public docs | Autonomous software engineer with shell/browser access and PR collaboration. | Product-level reference only; too broad and closed-source for T007 design. |
| Warp agent mode | Public docs | Terminal-integrated agent mode that runs commands and edits files. | Product-level reference only. |
| Windsurf/Cascade | Public docs | IDE coding agent with codebase context, edits, commands, and workflow memory. | Product-level reference only. |

## T007_03 Takeaways

- The common implementation pattern is not a provider-specific model class. It is a
  runtime harness that decides which tools exist, validates tool input, records tool
  output, enforces workspace policy, and continues the model loop.
- OpenCode, Gemini CLI, Qwen Code, Cline/Roo, Goose, and Codex CLI support the
  T007_03 decision to add a narrow `ChatHarnessService` plus scoped tool-run state.
- Aider, SWE-agent, and mini-SWE-agent are important counterweights: they show that
  useful coding agents can stay much smaller than large MCP/plugin/server systems.
- For Codegeist, use large agents as behavior evidence and small agents as scope
  discipline. T007_03 should implement only MCP callbacks, read/list/glob/grep/write,
  output bounds, workspace policy, and `ToolSessionPart` persistence.
- Spring AI Agent Utils is not a coding-agent harness. It remains a Java/Spring tool
  and callback reference that Codegeist may wrap behind its own policy and session
  persistence.
