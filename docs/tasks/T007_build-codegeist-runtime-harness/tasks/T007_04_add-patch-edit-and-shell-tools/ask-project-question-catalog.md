# T007_04 Patch Edit And Shell Tools Question Catalog

Focused `/ask-project` question catalog for `T007_04`. Use this before
implementing Codegeist patch/edit and shell tools so the Java/Spring design is
grounded in source-backed behavior from Aider, OpenCode, Pi, mini-SWE-agent, and
Spring AI Agent Utils.

Answered in: `ask-project-research.md`.

Current implementation note: `T007_04` is now completed. `codegeist_edit` keeps
workspace-contained file mutation, `codegeist_patch` remains deferred, and
`codegeist_shell` uses one plain `ProcessBuilder` command with merged stdout/stderr,
optional `timeoutSeconds`, no workspace cwd containment, and bounded completed shell
summaries. Older questions in this catalog preserve the original research shape and
should not be read as the final current contract.

## Purpose

Use this catalog to gather evidence for these T007_04 decisions:

- How mature coding agents represent patch/edit tools, file mutation safety,
  diff/patch application, review summaries, and persistence.
- How mature coding agents run shell commands with cwd, timeout, output bounds,
  exit code, cancellation, and session/trajectory persistence.
- Which side-effect checks are mandatory in Codegeist before the side effect runs:
  working-directory containment, cwd escape rejection, path normalization, symlink
  handling, timeout, and bounded output.
- Which safety behavior belongs in T007_04 and which belongs later to permission
  prompts, TUI review, background process handling, patch-review UI, or a full
  model/tool/model control loop.
- How Spring AI Agent Utils can inform Java tool callback wrappers without leaking
  Codegeist policy, session-store, or workspace constraints into third-party
  abstractions.

## Preconditions

The local `/ask-project` command expects an analyzed workspace under
`docs/third-party/<project>/` with these artifacts:

- `source/`
- `ANALYSIS_REPORT.md`
- `repomix-output.xml`
- `graphify-out/GRAPH_REPORT.md`
- `graphify-out/graph.json`

Expected projects for this catalog:

```text
aider
opencode
pi
mini-swe-agent
spring-ai-agent-utils
```

If a project reports missing or stale artifacts, rerun `/analyse-project` for
that project before treating an answer as durable implementation evidence. Do not
answer source-level questions from memory when Repomix artifacts are available;
`/ask-project` owns the Repomix subagent delegation.

## Codegeist Baseline At Research Time

Use this context when asking the questions below:

- Codegeist already has `.codegeist/session.json`, `ToolSessionPart`, bounded local
  read/list/glob/grep/write tools, MCP callbacks, and a one-turn
  `ChatHarnessService` from T007_03.
- `ToolSessionPart` currently stores `tool`, `status` (`completed` or `failed`),
  and `outputPreview` only. Do not assume typed shell fields, patch hunks, timing,
  metadata maps, or attachments unless research proves a focused T007_04 test needs
  them.
- T007_04 had to add bounded side-effecting patch/edit and shell tools under the chat
  working directory and record bounded summaries in `.codegeist/session.json`.
- Outside-workingDir file mutation had to fail before mutation. The draft shell
  requirement also expected cwd escape rejection, but the implemented shell contract
  later allowed absolute cwd values and documents that no cwd containment is claimed.
- Codegeist must not claim sandboxing beyond explicit tested path/cwd checks.
- Do not add network tools, MCP server management, plugins, LSP, subagents,
  background process persistence, or patch review TUI in T007_04.

## Recommended First Pass

Run these questions first. They should produce enough evidence to refine the
T007_04 implementation shape before Java source changes begin.

```text
/ask-project opencode "For Codegeist T007_04, create a source-backed evidence table for OpenCode patch/edit and shell tools. Include source path, class/function responsibility, input schema, workspace/cwd policy, side-effect gate, output bounding, persisted fields, tests, Codegeist translation, and keep/defer/drop recommendation."

/ask-project pi "For Codegeist T007_04, create a source-backed evidence table for Pi edit/write/patch-like file mutation and bash/shell execution behavior. Include source path, runtime flow, input schema, cwd/path policy, output bounding, JSONL persistence, extension hooks, tests, and Codegeist translation."

/ask-project aider "For Codegeist T007_04, analyze Aider's file edit formats and shell/test command loop. Which patch/edit and shell ideas should Codegeist preserve, simplify, or reject for a bounded Spring CLI tool slice? Cite source paths."

/ask-project mini-swe-agent "For Codegeist T007_04, analyze mini-SWE-agent's action parsing, bash execution environment, trajectory persistence, timeout/output handling, and submit flow. What does it teach about a minimal shell tool, and what should Codegeist avoid? Cite source paths."

/ask-project spring-ai-agent-utils "For Codegeist T007_04, analyze whether Spring AI Agent Utils has edit/apply-patch or shell-command utilities. Identify exact Java classes, ToolCallback patterns, side effects, output bounding, test patterns, and whether Codegeist should reuse, wrap, or implement its own tools."

/ask-project opencode "From OpenCode evidence, decide whether Codegeist T007_04 should keep patch/edit and shell as separate tools or one side-effecting tool family. Include minimal Java/Spring component names, input records, failure behavior, and session persistence summaries."

/ask-project spring-ai-agent-utils "From Spring AI Agent Utils and Spring AI evidence, design the minimal Java ToolCallback wrapper shape for Codegeist patch/edit and shell tools. Include how Codegeist should pass working directory, call id, session recorder, timeout, and output bounds without storing runtime state in .codegeist/session.json."
```

## OpenCode Questions

```text
/ask-project opencode "For Codegeist T007_04, create a source-backed evidence table for OpenCode patch/edit and shell tools. Include source path, class/function responsibility, input schema, workspace/cwd policy, side-effect gate, output bounding, persisted fields, tests, Codegeist translation, and keep/defer/drop recommendation."

/ask-project opencode "Deep dive into OpenCode's edit tool. How are target files selected, original content checked, replacements applied, errors reported, summaries produced, and results persisted? Which exact fields should Codegeist keep in a bounded outputPreview?"

/ask-project opencode "Deep dive into OpenCode's patch/apply_patch tool. How does it parse patch input, validate file paths, apply changes, handle partial failures, summarize hunks, and persist results? Which behavior is too broad for Codegeist T007_04?"

/ask-project opencode "Compare OpenCode write, edit, and apply_patch tools. What is the responsibility boundary among create/overwrite, string replacement, and patch application? Recommend the Codegeist T007_04 boundary relative to existing codegeist_write."

/ask-project opencode "Analyze OpenCode's file mutation workspace policy for write/edit/patch. How does it handle relative paths, absolute paths, traversal, symlinks, missing parents, ignored files, protected paths, and external directories? Recommend Codegeist pre-side-effect checks."

/ask-project opencode "Analyze OpenCode permission and approval gates for write/edit/patch. Which side-effect approvals are core user-visible behavior, which are database/TUI/plugin-specific, and which should Codegeist defer until a permission loop exists?"

/ask-project opencode "Analyze OpenCode's model-visible edit/patch result format versus persisted session/tool state. Which summary fields are enough for Codegeist .codegeist/session.json while ToolSessionPart only has tool/status/outputPreview?"

/ask-project opencode "Analyze OpenCode tests for edit, patch, and write tools. List source paths, test names, fixtures, and assertions Codegeist should mirror for path containment, mutation success, failed mutation, and bounded summaries."

/ask-project opencode "Deep dive into OpenCode's shell tool. Include command schema, selected shell, cwd/workdir resolution, timeout, stdin, stdout/stderr handling, exit code, abort/cancellation, environment, permissions, truncation, persistence, and tests."

/ask-project opencode "Analyze OpenCode's shell cwd and external-directory behavior. It can ask for permission instead of hard rejecting; what evidence supports Codegeist's stricter T007_04 requirement to reject cwd escape before ProcessBuilder.start()?"

/ask-project opencode "Analyze OpenCode's command scanning for shell file arguments. Which static paths can it detect, which dynamic paths are unresolved, what permission patterns are created, and why should Codegeist defer this in T007_04?"

/ask-project opencode "Analyze OpenCode shell output truncation. Include live metadata preview, final output caps, saved full-output side files, line and byte limits, stderr behavior, and how tests assert truncation. Recommend bounded Codegeist stdout/stderr preview text."

/ask-project opencode "Analyze OpenCode shell timeout and abort behavior. How does it kill processes, represent timeout/abort in output and metadata, and persist the result? Recommend whether Codegeist timeout should be completed or failed in ToolSessionPart."

/ask-project opencode "Analyze OpenCode's generic tool wrapper around side-effecting tools. How are schema validation, before/after hooks, permission context, result truncation, and session processor updates composed? Which parts map to Codegeist CodegeistToolService?"

/ask-project opencode "Create a sequence diagram for OpenCode side-effecting tool execution covering prompt, model tool call, permission check, edit/patch or shell execution, output bounding, persistence, and model continuation."

/ask-project opencode "Create a final OpenCode-to-Codegeist checklist for T007_04 implementation readiness. Include required behavior, deferred behavior, unresolved decisions, and source-backed tests."
```

## Pi Questions

```text
/ask-project pi "For Codegeist T007_04, create a source-backed evidence table for Pi edit/write/patch-like file mutation and bash/shell execution behavior. Include source path, runtime flow, input schema, cwd/path policy, output bounding, JSONL persistence, extension hooks, tests, and Codegeist translation."

/ask-project pi "Analyze Pi's built-in edit tool. How does it define input parameters, validate paths, apply changes, handle file contents, bound results, report errors, and integrate with AgentSession events and JSONL persistence?"

/ask-project pi "Analyze Pi's write tool only as the boundary before T007_04 patch/edit. Which create/overwrite semantics already map to Codegeist codegeist_write, and what edit/patch semantics should remain separate?"

/ask-project pi "Analyze Pi's bash tool. Include command input schema, cwd handling, command prefix or shell settings, process execution, timeout or cancellation behavior, stdout/stderr output bounding, and session persistence."

/ask-project pi "Analyze how Pi's AgentSession handles tool_call and tool_result extension hooks around built-in tools. Which hook semantics should Codegeist not copy yet because T007_04 has no plugin/extension system?"

/ask-project pi "Analyze Pi's JSONL session persistence for edit and bash tool results. Which message entries or custom entries are persisted, which fields are runtime-only, and what minimal bounded Codegeist ToolSessionPart summary should preserve?"

/ask-project pi "Analyze Pi's workspace/path policy for file mutation tools. How does it resolve cwd, reject or allow paths outside the project, handle symlinks, and report path errors? Recommend Codegeist pre-side-effect checks."

/ask-project pi "Analyze Pi tests for edit, write, bash, tool result persistence, and output bounds. Summarize source paths, fixtures, test assertions, and Codegeist test cases to mirror."

/ask-project pi "Compare Pi's default active tools and prompt/tool registry behavior with Codegeist's T007_03 local tools. What should Codegeist change or keep when adding patch/edit and shell tools?"

/ask-project pi "Create a Pi-to-Codegeist translation plan for T007_04. Include patch/edit tool shape, shell tool shape, output summary text, session persistence, non-goals, and implementation order."
```

## Aider Questions

```text
/ask-project aider "For Codegeist T007_04, analyze Aider's file editing pipeline from model response to parsed edits to file mutation. Include edit formats, validation, diff generation, error handling, user-visible summaries, and source paths."

/ask-project aider "Analyze Aider's patch/diff application behavior. Does it apply unified diffs, search/replace blocks, whole-file rewrites, or multiple edit formats? Which format is safest for a first Codegeist patch/edit tool?"

/ask-project aider "Analyze Aider's file admission and repo boundary behavior for edits. How are files added to chat, protected, ignored, read-only, outside repo, missing, or renamed? Which concepts are too repo/git-specific for Codegeist T007_04?"

/ask-project aider "Analyze Aider's shell command, lint, and test execution loop. How are commands chosen, run, timed out, captured, returned to the model, and used for reflection? Which shell feedback concepts should Codegeist keep or defer?"

/ask-project aider "Analyze Aider's output bounding for command results, diffs, lint/test output, and edit summaries. Recommend what Codegeist should include in ToolSessionPart.outputPreview for patch/edit and shell."

/ask-project aider "Analyze Aider's git integration around edits and commits. Which post-edit diff, dirty-file, auto-commit, and repo-map concepts must stay out of T007_04 despite being useful in Aider?"

/ask-project aider "Analyze Aider tests that cover edit parsing, file mutation, command execution, lint/test output, and path safety. List source paths and recommend Codegeist-focused JUnit tests."

/ask-project aider "Create an Aider-to-Codegeist translation plan for T007_04. Focus on minimal patch/edit and shell tools, not repo maps, auto-commits, benchmark flows, or broad command loops."
```

## mini-SWE-agent Questions

```text
/ask-project mini-swe-agent "For Codegeist T007_04, analyze mini-SWE-agent's action parsing, bash execution environment, trajectory persistence, timeout/output handling, and submit flow. What does it teach about a minimal shell tool, and what should Codegeist avoid? Cite source paths."

/ask-project mini-swe-agent "Analyze mini-SWE-agent's environment abstraction for command execution. How are cwd, command strings, return codes, exceptions, observations, and termination represented? Recommend a minimal Codegeist shell result summary."

/ask-project mini-swe-agent "Analyze mini-SWE-agent's timeout, process isolation, Docker or local environment behavior, and output capture. Which environment features are benchmark-specific and should stay out of T007_04?"

/ask-project mini-swe-agent "Analyze mini-SWE-agent trajectory output for actions and observations. Which trajectory fields map to Codegeist ToolSessionPart, and which benchmark or replay fields should be deferred?"

/ask-project mini-swe-agent "Analyze how mini-SWE-agent gets file changes done through shell commands instead of a first-class patch tool. What are the risks of shell-only mutation, and why should Codegeist keep explicit patch/edit tools?"

/ask-project mini-swe-agent "Analyze mini-SWE-agent tests for action parsing, environment execution, trajectories, and command failures. Recommend Codegeist shell tests that are independent of SWE-bench and Docker."

/ask-project mini-swe-agent "Create a mini-SWE-agent-to-Codegeist translation plan for T007_04. Focus on keeping shell execution narrow, bounded, and persisted without adopting benchmark runners, submitted exceptions, or trajectory replay."
```

## Spring AI Agent Utils Questions

```text
/ask-project spring-ai-agent-utils "For Codegeist T007_04, analyze whether Spring AI Agent Utils has edit/apply-patch or shell-command utilities. Identify exact Java classes, ToolCallback patterns, side effects, output bounding, test patterns, and whether Codegeist should reuse, wrap, or implement its own tools."

/ask-project spring-ai-agent-utils "Analyze Agent Utils tool callback implementation patterns relevant to side-effecting tools. Compare @Tool methods, MethodToolCallbackProvider, FunctionToolCallback, ToolCallback, and ToolContext for per-chat workingDir and session recording."

/ask-project spring-ai-agent-utils "Analyze Agent Utils file mutation utilities, if any. Include path handling, write/edit semantics, exception behavior, output shape, and tests. Recommend whether Codegeist patch/edit should use direct Java NIO or an Agent Utils delegate."

/ask-project spring-ai-agent-utils "Analyze Agent Utils command or shell execution utilities, if any. Include cwd, timeout, stdout/stderr capture, exit code, cancellation, environment handling, output bounds, and tests. Recommend CodegeistShellTool implementation strategy."

/ask-project spring-ai-agent-utils "Analyze how Agent Utils handles tool failures and thrown exceptions in Spring AI ToolCallback paths. How should Codegeist convert invalid input, cwd escape, timeout, non-zero exit, and process startup failure into ToolSessionPart status and outputPreview?"

/ask-project spring-ai-agent-utils "Analyze whether ToolContext can safely carry Codegeist workingDir, session id, call id, recorder, timeout, and policy objects. Should T007_04 prefer explicit CodegeistLocalTool wrappers instead?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils output truncation or bounded-result patterns for tools. If none are suitable, recommend a Codegeist-owned ToolOutputBounds usage pattern for patch summaries, diffs, stdout, and stderr."

/ask-project spring-ai-agent-utils "Analyze Agent Utils tests for ToolCallback registration, direct callback invocation, temporary file fixtures, path escape, process execution, and exception mapping. Recommend focused Codegeist JUnit test classes."

/ask-project spring-ai-agent-utils "Create a Spring-friendly Codegeist T007_04 component diagram. Include ChatHarnessService, CodegeistToolService, CodegeistLocalTools, CodegeistPatchTool, CodegeistEditTool, CodegeistShellTool, WorkspaceResolver or guard, ToolOutputBounds, and SessionStoreService."

/ask-project spring-ai-agent-utils "Create a Spring-AI-Agent-Utils-to-Codegeist translation plan for T007_04. Include reusable implementation ideas, wrapper boundaries, non-goals, risks, and minimal implementation order."
```

## Cross-Project Patch/Edit Questions

```text
/ask-project opencode "Compare OpenCode patch/edit behavior with Aider, Pi, and mini-SWE-agent for Codegeist T007_04. Produce a table of edit representation, validation, application, summary, persistence, tests, and Codegeist recommendation."

/ask-project aider "Compare Aider edit formats with OpenCode and Pi from a Codegeist perspective. Which edit input contract is easiest to test, least ambiguous, and safest under a working-directory-only mutation policy?"

/ask-project pi "Compare Pi's edit/write behavior with OpenCode and Aider. Which concepts should Codegeist borrow for a minimal patch/edit tool, and which extension or TUI-specific behavior should be deferred?"

/ask-project spring-ai-agent-utils "Using the evidence from OpenCode, Aider, Pi, and mini-SWE-agent, recommend Java record/class shapes for Codegeist patch/edit tool inputs and bounded result summaries."

/ask-project opencode "Should Codegeist T007_04 implement unified patch application, search/replace editing, or both? Use OpenCode evidence plus Aider and Pi comparisons, and include first-slice tests for ambiguity and failure paths."

/ask-project aider "What failure modes make model-authored edits unsafe or ambiguous? Include stale file content, repeated search blocks, missing files, binary files, large files, generated files, and partial patch failures. Recommend Codegeist first-slice rejection rules."
```

## Cross-Project Shell Questions

```text
/ask-project opencode "Compare OpenCode, Pi, Aider, and mini-SWE-agent shell execution for Codegeist T007_04. Produce a table of input schema, cwd policy, timeout, stdout/stderr behavior, exit code, output bounds, persistence, and tests."

/ask-project mini-swe-agent "Compare mini-SWE-agent's shell-first environment with OpenCode and Pi. Which ideas support a simple CodegeistShellTool, and which benchmark/sandbox/runtime concepts must stay out of T007_04?"

/ask-project aider "Compare Aider command execution, lint/test loops, and reflection with OpenCode and mini-SWE-agent. Which shell-result feedback belongs in Codegeist T007_04, and which belongs later to an agent control loop?"

/ask-project spring-ai-agent-utils "Using shell evidence from OpenCode, Pi, Aider, and mini-SWE-agent, recommend Java implementation details for ProcessBuilder, concurrent stream capture, timeout, descendant killing, and output bounding in CodegeistShellTool."

/ask-project opencode "Should Codegeist treat non-zero exit and timeout as completed shell results or failed tool calls? Compare OpenCode, Pi, Aider, and mini-SWE-agent evidence and recommend T007_04 behavior."

/ask-project pi "How should Codegeist represent shell stdout and stderr separately in one bounded ToolSessionPart.outputPreview while keeping model-visible output useful? Compare Pi and OpenCode behavior."
```

## Workspace Policy And Safety Questions

```text
/ask-project opencode "Compare path/cwd containment policies for OpenCode write/edit/patch/shell with Codegeist's T007_04 requirement: outside-workingDir mutation and cwd escape fail before side effects. What exact checks should Codegeist implement first?"

/ask-project pi "Analyze Pi's path resolution and cwd rules for side-effecting tools. Does Pi rely on process cwd, session cwd, tool cwd, or workspace root? Recommend Codegeist path/cwd validation behavior."

/ask-project aider "Analyze Aider's repository and file admission policy as a stronger but git-specific boundary. Which pieces can inform Codegeist working-directory checks without requiring git?"

/ask-project spring-ai-agent-utils "Recommend a Java NIO path containment strategy for Codegeist T007_04. Include normalize, toAbsolutePath, toRealPath for existing paths, missing target parent handling, symlink escapes, Windows paths, and test fixtures."

/ask-project mini-swe-agent "Analyze mini-SWE-agent local and Docker environment boundaries. Which safety assumptions are external to the agent and should not be claimed by Codegeist's local shell tool?"

/ask-project opencode "What should Codegeist explicitly document as not sandboxed for T007_04? Use OpenCode, Pi, Aider, and mini-SWE-agent evidence to separate cwd/path checks from real sandboxing."
```

## Output Bounds And Session Persistence Questions

```text
/ask-project opencode "Map OpenCode side-effecting tool persisted fields to Codegeist ToolSessionPart for T007_04. Produce a keep/defer/drop table for command, cwd, exit code, timeout, stdout, stderr, diff, changed files, metadata, timing, and attachments."

/ask-project pi "Map Pi JSONL message and tool-result persistence for edit and bash tools to Codegeist .codegeist/session.json. Which fields are needed for continuation, rendering, and audit, and which are extension/runtime-only?"

/ask-project aider "Analyze how Aider stores or displays edit diffs, command outputs, lint/test outputs, and reflections. Which information should Codegeist persist versus only render or omit?"

/ask-project mini-swe-agent "Analyze trajectory persistence for actions and observations. Which action/observation fields should inform Codegeist ToolSessionPart.outputPreview, and which trajectory metadata is too broad?"

/ask-project spring-ai-agent-utils "Recommend a bounded text format for Codegeist patch/edit results and shell results while ToolSessionPart has only outputPreview. Include stable headings, truncation notices, and no-secret cautions."

/ask-project opencode "How should Codegeist avoid unbounded diffs and command output in .codegeist/session.json? Compare OpenCode truncation side files, Pi output bounding, Aider command summaries, and mini-SWE-agent trajectories."
```

## Test And Verification Questions

```text
/ask-project opencode "List OpenCode source tests most relevant to T007_04. Group by write/edit/patch, shell command, cwd/path policy, permission side effects, timeout/abort, truncation, and persisted tool parts."

/ask-project pi "List Pi tests most relevant to T007_04. Group by edit/write tools, bash tool, AgentSession tool hooks, output bounds, JSONL persistence, and queue/streaming interactions."

/ask-project aider "List Aider tests most relevant to T007_04. Group by edit parsing/application, file boundary checks, command execution, lint/test feedback, output truncation, and git-specific behavior to avoid."

/ask-project mini-swe-agent "List mini-SWE-agent tests most relevant to T007_04. Group by action parsing, environment execution, timeout/failure handling, trajectory writing, and benchmark-specific behavior to avoid."

/ask-project spring-ai-agent-utils "List Spring AI Agent Utils tests most relevant to T007_04. Group by ToolCallback registration, file side effects, process/shell utilities if present, ToolContext, exception handling, and test fixture patterns."

/ask-project spring-ai-agent-utils "Recommend Codegeist T007_04 focused test classes and methods. Include patch/edit mutation success, path escape failure, ambiguous edit failure, shell success, shell non-zero exit, shell timeout, stdout/stderr bounds, session persistence, and unchanged plain ask behavior."

/ask-project opencode "Which T007_04 behaviors should Codegeist deliberately not test or implement yet because they belong to permissions UI, TUI patch review, background processes, shell sessions, plugins, subagents, server APIs, or a database?"
```

## Synthesis Questions

Run these after the project-specific answers exist.

```text
/ask-project opencode "Produce a concise OpenCode-to-Codegeist T007_04 implementation readiness report. Include patch/edit contract, shell contract, path/cwd validation, output bounds, session persistence, tests, deferred behavior, and unresolved decisions."

/ask-project pi "Produce a concise Pi-to-Codegeist T007_04 implementation readiness report. Include AgentSession/tool lessons, extension behaviors to defer, JSONL persistence lessons, and test recommendations."

/ask-project aider "Produce a concise Aider-to-Codegeist T007_04 implementation readiness report. Include edit-format lessons, shell/lint/test-loop lessons, git-specific non-goals, and tests."

/ask-project mini-swe-agent "Produce a concise mini-SWE-agent-to-Codegeist T007_04 implementation readiness report. Include minimal shell-loop lessons, trajectory lessons, environment non-goals, and tests."

/ask-project spring-ai-agent-utils "Produce a concise Spring-AI-Agent-Utils-to-Codegeist T007_04 implementation readiness report. Include Java/Spring tool callback boundaries, wrapper needs, no-reuse areas, and focused test seams."

/ask-project opencode "Synthesize all five projects into a final T007_04 design decision table. Columns: decision, evidence projects, recommended Codegeist behavior, deferred behavior, tests, and documentation updates."
```

## Expected Research Outputs

After enough questions are answered, create or update these files in this
directory:

- `ask-project-research.md` - source-backed answer set for this catalog.
- `implementation-plan.md` - concrete Java/Spring implementation handoff after the
  evidence is accepted.
- `task.md` - updated task state, accepted decisions, unresolved questions, and
  verification guidance.

If implementation changes package boundaries, classes, configuration behavior, or
tests, also update current-state architecture docs under
`docs/developer/architecture/` in the same task.

Keep generated artifacts such as Repomix output, Graphify output, rendered diagrams,
and verification reports in their ignored third-party locations unless the user asks
for a durable snapshot.
