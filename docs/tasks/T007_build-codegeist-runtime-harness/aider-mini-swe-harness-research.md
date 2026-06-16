# Aider And mini-SWE-agent Harness Research

Focused source-backed answers for the smaller-harness comparison questions in
`mcp-and-readwrite-tools-question-catalog.md`.

This document applies the catalog to the local `aider` and `mini-swe-agent`
analysis workspaces. It focuses on how each project implements its harness,
tooling, MCP or MCP absence, solved problems, and Codegeist implementation
translations for T007_03 and nearby T007 children.

## Scope And Evidence

Evidence used:

- Aider source checkout: `docs/third-party/aider/source/` at
  `5dc9490bb35f9729ef2c95d00a19ccd30c26339c`.
- Aider analysis docs: `docs/third-party/aider/ANALYSIS_REPORT.md` and
  `docs/third-party/aider/developer/prompt-flow.md`.
- Aider Repomix output: `docs/third-party/aider/repomix-output.xml`.
- mini-SWE-agent source checkout: `docs/third-party/mini-swe-agent/source/` at
  `2caffc565474b8856a323ff163ffb7ab98d1ef02`.
- mini-SWE-agent analysis docs: `docs/third-party/mini-swe-agent/ANALYSIS_REPORT.md`
  and `docs/third-party/mini-swe-agent/developer/runtime-flow.md`.
- mini-SWE-agent Repomix output:
  `docs/third-party/mini-swe-agent/repomix-output.xml`.

No runtime sessions, provider calls, shell commands through either agent, MCP
servers, SWE-bench runs, or upstream tests were executed for this research pass.

## Catalog Questions Answered

```text
/ask-project aider "For Codegeist T007_03, analyze Aider's harness shape: coder state, repo map, file editing, git integration, lint/test loop, and chat history. Which concepts support the ChatHarnessService decision, and which should stay out of MCP/read/write tools?"

/ask-project mini-swe-agent "For Codegeist T007_03, analyze mini-SWE-agent's minimal model-plus-environment loop, linear message history, bash-only execution, and trajectory output. What does it imply for keeping Codegeist's first harness narrow?"
```

## Executive Findings

- Neither Aider nor mini-SWE-agent implements MCP in the inspected source. They are
  not MCP lifecycle references. Use OpenCode and Spring AI evidence for MCP client
  setup, tool callback conversion, naming, failures, and cleanup.
- Both projects still support the Codegeist `ChatHarnessService` decision because
  they put orchestration outside the provider model class.
- Aider is the cautionary broad-harness example: one `Coder` object coordinates
  chat state, repo maps, file scope, edits, git, lint/test, shell, reflection, and
  commands. It solves many practical coding-agent problems, but most of that scope
  belongs after T007_03.
- mini-SWE-agent is the minimality example: one agent loop composes a model and an
  environment, records linear messages, executes one bash action source, and saves a
  trajectory. It argues for a narrow first Codegeist harness and against speculative
  tool registries, persistent shell state, or extra lifecycle metadata.
- For Codegeist, MCP should be one dynamic tool source inside a scoped
  `CodegeistToolRun`, not the architecture center. Local tools should be
  Codegeist-owned callbacks with workspace policy, output bounds, and session
  recording.

## Aider Answer

### Harness Shape

Aider centers its runtime around a mutable `Coder` instance:

- `aider/coders/base_coder.py` defines `Coder`, including active editable files,
  read-only files, repo, repo map, function-call state, current and done messages,
  token/cost state, lint/test state, shell-command state, and edit format.
- `Coder.create(...)` selects a concrete coder subclass by `edit_format`, and mode
  changes clone state from the previous coder.
- `Coder.run()` drives the interactive loop, gets terminal input, preprocesses
  commands/mentions/URLs, sends messages, reflects failures, and shows undo hints.
- `Coder.send_message()` appends the user prompt, formats model context, checks
  token limits, calls the model, applies edits, auto-commits, runs lint/test/shell
  feedback loops, and reflects failures into follow-up turns.
- `aider/main.py` is the startup assembler: it resolves config, IO, model, git, and
  commands before creating a coder.

Codegeist translation:

- Keep the one-turn orchestration idea, not the monolithic object. Use a narrow
  `ChatHarnessService` for provider selection, scoped tool run, chat call, and
  session save.
- Do not move Aider-style repo map, edit parsing, lint/test, git commit, shell
  suggestion, web scraping, voice, browser UI, or watch mode into T007_03.
- Keep `AskCommands` thin. Aider shows how quickly a command surface can become a
  second runtime if it owns tool orchestration directly.

### Tooling Shape

Aider tooling is not MCP. It is a mix of command dispatch, edit strategies, model
function/tool calls for edit formats, shell commands, and repo-aware file scope:

- `aider/commands.py` maps `/command` names to `cmd_*` methods and maps `!cmd` to
  `/run` behavior.
- `/add` and `/drop` mutate the file set in chat and enforce repository/root and
  ignore checks before files enter editable context.
- `/run` executes a shell command under the coder root, calculates output tokens,
  and asks whether to add command output to chat unless the caller wants non-zero
  test output automatically.
- `/test` delegates to `/run` and adds failing output to chat.
- `/lint` runs linting on in-chat or dirty files, asks whether to fix lint errors,
  and can spawn a cloned coder for lint repair.
- `/commit` and automatic commits route through `GitRepo.commit(...)`.
- Concrete coder subclasses parse and apply search/replace, diff, patch, whole-file,
  function-call, architect/editor, ask/help/context, and related edit formats.

Codegeist translation:

- For T007_03, implement only `read`, `list`, `glob`, `grep`, and create/overwrite
  `write` as Spring AI `ToolCallback` values. Treat patch/edit and shell as T007_04.
- Put file-scope and path checks in `WorkspacePolicy`, not in each command or tool.
- Put output previews and truncation metadata in `ToolOutputBounds`, not in every
  individual callback.
- Record model-visible output separately from persisted `ToolSessionPart` data.
  Aider adds shell output back into chat as text; Codegeist should persist structured
  bounded tool parts instead.

### MCP Finding

Source search for `mcp`, `MCP`, and `Model Context Protocol` found no relevant Aider
implementation in Python, docs, YAML, or TOML files in the inspected checkout.

Codegeist translation:

- Do not infer MCP behavior from Aider. Use OpenCode and Spring AI MCP evidence.
- Aider still proves that MCP is not required for a useful coding-agent harness.
  Treat MCP as a pluggable tool source, not a prerequisite for local tools or chat
  persistence.

### Problems Aider Solves

| Problem | Aider solution | Codegeist T007 translation |
| --- | --- | --- |
| Repository context is too large. | `RepoMap` builds a token-budgeted, ranked map from tree-sitter tags and caches it under `.aider.tags.cache.v*`. | Defer repo maps. Use `glob`/`grep`/`read` tools first and persist only bounded results. |
| The model edits files not in scope. | `allowed_to_edit()` checks active file set, git ignore, file existence, and asks before creating or editing not-yet-added files. | Add `WorkspacePolicy` and mutating-tool approval seams. T007_03 `write` should reject path escape and keep patch/edit deferred. |
| Edits can break existing dirty work. | `check_for_dirty_commit()` and `dirty_commit()` can commit dirty files before applying model edits so undo has a baseline. | Do not add git automation in T007_03. Later patch/edit work should decide explicit git/undo policy. |
| Model output can be malformed. | Edit parsing errors become `reflected_message` and can trigger another model turn up to `max_reflections`. | For file tools, return structured validation errors and persist failed `ToolSessionPart`; do not add autonomous repair loops yet. |
| Lint/test failures need feedback. | `cmd_lint()` and `cmd_test()` can add errors back into chat after confirmation. | Defer lint/test tools. The shell tool in T007_04 can later return bounded output and optional continuation prompts. |
| Shell output can explode context. | `/run` calculates token count and asks before adding output to chat. | Use deterministic output caps instead of prompt-time token estimates as the first slice. |
| Users need side-effect control. | Confirmations guard adding files, creating files, editing out-of-chat files, running shell commands, adding command output, URL scraping, and fixing lint/test errors. | Add side-effect classification now; interactive permission UI can wait, but mutating tools must have one future-ready policy seam. |

## mini-SWE-agent Answer

### Harness Shape

mini-SWE-agent has a deliberately small harness:

- `run/mini.py` loads YAML/key-value config specs, applies CLI overrides, selects a
  model, environment, and agent, then calls `agent.run(task)`.
- `DefaultAgent.run()` renders initial messages, loops until the last message has
  `role="exit"`, handles `FormatError` and `InterruptAgentFlow`, saves on every
  iteration, and returns the final exit metadata.
- `DefaultAgent.step()` is exactly `execute_actions(query())`.
- `DefaultAgent.query()` checks limits, calls `model.query(messages)`, records cost,
  and appends the assistant message.
- `DefaultAgent.execute_actions()` sends parsed actions to `env.execute(action)` and
  appends formatted observation messages.
- `InteractiveAgent` adds human/confirm/yolo modes around the same base loop.

Codegeist translation:

- Keep `ChatHarnessService` similarly small: resolve prompt/session/provider, open a
  scoped tool run, call `CodegeistChatService`, save text plus completed tool parts,
  and close resources.
- Keep `CodegeistChatRequest` focused on model and prompt. Put tools/history/working
  directory in a separate runtime context.
- Avoid extra lifecycle states until tests need them. `pending`, `running`,
  `completed`, and `failed` are enough when provider tooling can expose them;
  otherwise record completed/failed callbacks deterministically.

### Tooling Shape

mini-SWE-agent tooling is also not MCP. It exposes one model-facing capability:
bash command execution.

- `actions_toolcall.py` declares `BASH_TOOL` as a single function tool named `bash`
  with one required `command` string.
- `parse_toolcall_actions(...)` rejects missing tool calls, unknown tool names, bad
  JSON, and missing `command` with `FormatError` messages.
- `actions_text.py` supports legacy text-mode action parsing through one configured
  regex and rejects anything other than exactly one action.
- `LocalEnvironment.execute()` executes one command through a subprocess, captures
  output and return code, maps exceptions to structured output, and detects the
  submission sentinel.
- `DockerEnvironment` starts a container, executes commands through `docker exec`,
  forwards configured environment variables, and cleans up the container.
- SWE-bench batch processing creates one model/environment/agent per instance and
  writes `.traj.json` plus `preds.json`.

Codegeist translation:

- Do not copy bash-only tooling for T007_03. The current scope requires structured
  `read`, `list`, `glob`, `grep`, `write`, and MCP callbacks.
- Copy the discipline: a tiny, explicit tool surface; strict input validation; clear
  model-visible error messages; and one owned environment/tool boundary.
- Preserve the distinction between runtime tool config and durable chat/session
  output.

### MCP Finding

Source search for `mcp`, `MCP`, and `Model Context Protocol` found no relevant
mini-SWE-agent implementation in Python, docs, YAML, or TOML files in the inspected
checkout.

Codegeist translation:

- Use mini-SWE-agent only as a minimal harness reference, not an MCP reference.
- Keep Codegeist MCP support in `CodegeistMcpAdapter` and `CodegeistToolRun`, where
  it can be created lazily, fail before a provider call, and close after the turn.

### Problems mini-SWE-agent Solves

| Problem | mini-SWE-agent solution | Codegeist T007 translation |
| --- | --- | --- |
| Agent loops become hard to understand. | `step()` is one expression: query model, then execute actions. | Keep `ChatHarnessService` one-turn and direct. Do not introduce planner/subagent/runtime managers in T007_03. |
| Tool formats vary by model. | Model adapters own parsing and observation formatting for tool-call, response API, and text-regex modes. | Provider models should map Spring AI callbacks privately; Codegeist-owned tool result records stay provider-neutral. |
| Invalid model actions need recovery evidence. | `FormatError` carries messages and can be appended to trajectory. | Persist failed tool calls/results with bounded error text. Do not lose raw validation failure cause. |
| Stateful shells are fragile. | Local actions run as independent subprocesses with a configured cwd and env. | Defer shell to T007_04; when added, prefer one-shot command execution with timeout and bounded output before persistent shell sessions. |
| Child processes can survive timeout. | POSIX local execution starts a new process group and kills it on timeout. | Reuse this principle in future `ShellTool`; file tools need no process lifecycle. |
| Users need control over dangerous actions. | `InteractiveAgent` has confirm/yolo/human modes and whitelists. | For T007_03, include policy seams and safe defaults; full interactive permission UX can wait. |
| Runs need auditability. | `DefaultAgent.serialize()` writes config, model/environment types, model stats, messages, exit status, and trajectory format. | Keep `.codegeist/session.json` smaller: chat history and bounded tool activity only, not provider config, MCP definitions, or enabled tool registry. |
| Batch tasks need partial failure handling. | SWE-bench runner removes stale predictions, catches per-instance exceptions, saves trajectories, and updates `preds.json` under a lock. | T007_03 does not need batch behavior, but tests should prove partial tool failures become failed session parts rather than corrupting the store. |

## Cross-Project Translation For Codegeist

| Codegeist concern | Aider evidence | mini-SWE-agent evidence | Recommended solution |
| --- | --- | --- | --- |
| Harness boundary | `Coder` proves orchestration belongs outside provider calls, but is too broad. | `DefaultAgent` proves a small model-plus-environment loop is enough. | Implement `ChatHarnessService` plus scoped `CodegeistToolRun`; keep `AskCommands` thin. |
| Tool set | Commands, edit strategies, git, lint/test, shell, web, repo map. | One bash tool and a few environment backends. | T007_03 exposes only MCP callbacks plus read/list/glob/grep/write; defer shell, patch/edit, git, lint/test, repo map. |
| MCP | Absent. | Absent. | Use OpenCode/Spring AI for MCP. Treat MCP as a dynamic tool source inside the tool run. |
| Workspace policy | File add/edit checks, repo tracking, gitignore, root checks. | Environment cwd and config are simple; local mode is host-side. | Add `WorkspacePolicy` before exposing file tools; resolve real paths and reject escapes. |
| Output bounds | Token-count prompt before adding shell output; context token checks. | Observation templates truncate long command output in configs. | Add deterministic `ToolOutputBounds` with preview, omitted counts, and result counts. |
| Session persistence | In-memory `cur_messages`/`done_messages`, optional chat history and git commits. | Linear `messages` plus `.traj.json` output. | Persist ordered user/assistant text and `ToolSessionPart` values in `.codegeist/session.json`; keep runtime config out. |
| Error behavior | Provider retries, edit reflection, lint/test reflection, command errors. | `FormatError`, `Submitted`, `LimitsExceeded`, `TimeExceeded` append trajectory messages. | Convert validation, IO, MCP setup, callback, and provider errors to clear command errors plus failed bounded tool parts when a tool call exists. |
| User safety | Confirmations around file, shell, URL, and output side effects. | confirm/yolo/human modes and action whitelists. | Add side-effect categories and future approval seam now; first slice may use conservative defaults without full UI. |

## Concrete T007 Implementation Guidance

### 1. Keep The Harness Small

Implement only this orchestration in the first T007_03 harness:

```java
try (CodegeistToolRun toolRun = toolService.openRun(config, workingDir)) {
    CodegeistChatExecutionContext context = toolRun.executionContext();
    CodegeistChatResponse response = chatService.chat(providerConfig, request, context);
    sessionStoreService.saveExchangeToCurrentSession(
            continueSession,
            prompt,
            response.content(),
            toolRun.completedToolParts());
}
```

Do not add repo-map, git, lint/test, shell, patch/edit, web scraping, batch runners,
or a persistent runtime registry in the same slice.

### 2. Use MCP As One Tool Source

Neither comparison project proves MCP behavior. The Codegeist MCP path should stay
the one already specified by OpenCode/Spring research:

- Read direct `codegeist.yml` `mcp:` config through `McpClientConfig`.
- Build only `stdio` clients for T007_03.
- Convert clients to Spring AI `ToolCallback` values inside `CodegeistMcpAdapter`.
- Fail before the provider call if configured MCP setup fails.
- Close all created resources through `CodegeistToolRun.close()`.
- Do not persist MCP client definitions, statuses, resources, prompts, or enabled
  tool registry snapshots in `.codegeist/session.json`.

### 3. Make Local Tools Codegeist-Owned

Use the comparison projects to define behavior, not implementation classes:

- `read`: validate workspace path, reject directories/binary/oversized content where
  the first policy says so, support offset/limit, return bounded text.
- `list`: validate workspace path, include file/directory markers, apply max result
  caps, avoid generated/ignored noise where practical.
- `glob`: use explicit base path and pattern, cap results, sort deterministically.
- `grep`: validate regex, cap files/matches/lines, include line numbers and optional
  context only when specified.
- `write`: create or overwrite one allowed file under working directory; do not patch
  or edit in place beyond full content replacement.

### 4. Persist Small Tool Parts

Use mini-SWE-agent's trajectory discipline but not its broad config persistence.
Persist only what future replay/UI needs:

- `type: "tool"`
- `callId`
- `name`
- `state`
- bounded `input`
- bounded `output` or `error`
- optional `resultCount` and truncation metadata

Defer token/cost, timing, MCP server status, permission decisions, provider raw
responses, environment config, and selected model unless a focused test needs them.

### 5. Tests To Add First

1. `WorkspacePolicyTest`: relative path, absolute path, traversal, symlink escape,
   ignored/generated file behavior when implemented.
2. `ToolOutputBoundsTest`: line, byte, match, result, and error truncation.
3. `CodegeistFileToolsTest`: read/list/glob/grep/write success and failure cases.
4. `CodegeistMcpAdapterTest`: fake `ToolCallbackProvider`, stdio config mapping, MCP
   setup failure, and close behavior without launching network-dependent servers.
5. `SessionStoreServiceTest`: `ToolSessionPart` JSON round-trip and bounded output.
6. `ChatHarnessServiceTest` or `AskCommandsSessionStoreTest`: fake chat model emits a
   tool call, callback records a result, session stores ordered user/tool/assistant
   parts.

## What To Defer Explicitly

- Aider `RepoMap` and tree-sitter tag ranking.
- Aider git commits, dirty pre-commits, undo hints, and commit-message generation.
- Aider edit strategy family and patch application.
- Aider lint/test repair loops and shell command suggestions.
- mini-SWE-agent bash-only runtime as the primary Codegeist tool interface.
- mini-SWE-agent Docker/Singularity/SWE-ReX/Modal benchmark environment runners.
- Persistent shell sessions, background process managers, batch benchmark runners,
  TUI rendering state, subagents, skills, memories, server API, database, and remote
  sync.

## Bottom Line

Aider answers "how broad can a coding harness become when it owns repository work?"
mini-SWE-agent answers "how small can a useful harness be?" For Codegeist T007_03,
choose the middle: a small Java/Spring `ChatHarnessService` with a scoped
`CodegeistToolRun`, Codegeist-owned file tools, MCP callbacks as one runtime tool
source, bounded tool persistence, and no extra agent features until a later child
task explicitly asks for them.
