# T007_04_04 Shell Tool Question Catalog

Focused `/ask-project` question catalog for `T007_04_04_add-shell-tool.md`.
Use this catalog before implementing `codegeist_shell` so the first Codegeist
shell tool is grounded in source-backed behavior from every analyzed
third-party project under `docs/third-party/`.

Answered in: `shell-tool-research.md`.

Current implementation note: `codegeist_shell` now supports explicit
`tools.codegeist-shell.command-prefix` wrapper configuration, plain `ProcessBuilder`
execution, merged stdout/stderr, optional `timeoutSeconds`, no workspace cwd
containment, and bounded completed shell summaries. Older questions in this catalog
preserve the original research shape and should be read as source-evidence prompts,
not as final current behavior.

## Purpose

Gather implementation evidence for the originally proposed bounded Codegeist-owned
shell tool shape, which targeted:

- Runs one local process per tool call.
- Accepts a command string, one cwd field, and one timeout field.
- Resolves cwd under the active workspace and rejects cwd escape before process
  startup.
- Captures stdout and stderr concurrently and returns separate bounded previews.
- Treats non-zero exit as a completed shell result with an exit code preview.
- Records timeout explicitly, terminates the process, and persists the bounded
  result through `ToolSessionPart.outputPreview`.
- Avoids permission prompts, automatic shell discovery, tree-sitter command
  scanning, plugin environment hooks, persistent shells, background process
  registries, PTY behavior, and full-output side files in this child task.

## Projects Covered

Use this catalog for all current third-party analysis workspaces:

```text
aider
mini-swe-agent
opencode
pi
spring-ai-agent-utils
```

The local `/ask-project` command expects each project to have an analyzed
workspace under `docs/third-party/<project>/`, normally including `source/`,
`ANALYSIS_REPORT.md`, and `repomix-output.xml`. If `/ask-project` reports
missing or stale artifacts, rerun `/analyse-project` for that project before
treating the answer as durable implementation evidence.

## Codegeist Context To Include In Questions

Use this context in the prompts below when the question does not already include
it:

- Codegeist is Java-first: Java 25, Spring Boot, Spring Shell, Spring AI, and
  Spring AI Agent Utils are already in the build.
- Local tools live under `ai.codegeist.app.tool` and implement
  `CodegeistLocalTool`.
- `CodegeistLocalTools` discovers local tools as Spring components and wraps
  them in `CodegeistLocalToolCallback`.
- `ToolSessionPart` currently persists only `tool`, `status`, and
  `outputPreview`; do not add typed shell fields unless a focused test requires
  them.
- Existing local callbacks are `codegeist_read`, `codegeist_list`,
  `codegeist_glob`, `codegeist_grep`, `codegeist_write`, and `codegeist_edit`.
- `codegeist_edit` already rejects outside-workspace file targets by using a
  side-effecting guard; `workspace.dir-guard-disabled` currently documents file
  mutation only, not shell cwd policy.
- The first `codegeist_shell` implementation should use one simple platform
  shell per invocation, for example `cmd.exe /c` on Windows and `sh -lc`
  elsewhere, unless source evidence strongly justifies a different first slice.
- Codegeist does not yet own a model/tool/model control loop; current tools are
  prompt-scoped Spring AI callbacks for one provider call.
- Codegeist must not claim sandboxing beyond explicit cwd containment, timeout,
  and bounded output behavior.

## Recommended First Pass

Run these first to get one concise source-backed answer per project.

```text
/ask-project opencode "For Codegeist T007_04_04, create a source-backed evidence table for OpenCode shell execution. Include source path, command schema, shell selection, cwd/workdir policy, timeout and abort behavior, stdin behavior, stdout/stderr capture, output truncation, exit-code handling, persisted session/tool fields, tests, Codegeist keep/defer/drop recommendations, and any implementation risks for a Java ProcessBuilder tool."

/ask-project pi "For Codegeist T007_04_04, create a source-backed evidence table for Pi bash/shell execution. Include source path, tool schema, cwd policy, process model, timeout or cancellation behavior, stdout/stderr handling, output bounds, AgentSession hooks, JSONL persistence, tests, and Codegeist keep/defer/drop recommendations."

/ask-project aider "For Codegeist T007_04_04, create a source-backed evidence table for Aider command execution, shell/test/lint loops, command output handling, reflection feedback, timeout or cancellation behavior, persistence or transcript behavior, tests, and Codegeist keep/defer/drop recommendations for a bounded local shell tool."

/ask-project mini-swe-agent "For Codegeist T007_04_04, create a source-backed evidence table for mini-SWE-agent local or Docker command execution. Include action parsing, cwd/environment behavior, process model, timeout handling, stdout/stderr or observation capture, return-code semantics, trajectory persistence, tests, benchmark-specific non-goals, and Codegeist keep/defer/drop recommendations."

/ask-project spring-ai-agent-utils "For Codegeist T007_04_04, create a source-backed evidence table for Spring AI Agent Utils shell or command execution utilities and ToolCallback patterns. Include exact Java classes, annotations or callback APIs, cwd support, timeout handling, stdout/stderr capture, output bounds, exit-code semantics, exception behavior, tests, and whether Codegeist should reuse, wrap, or implement its own ProcessBuilder tool."
```

## OpenCode Questions

OpenCode is the richest reference for shell permissions, command scanning,
output truncation, and session tool metadata. Use these questions to separate
what Codegeist should copy from what must be deferred.

```text
/ask-project opencode "Deep dive into OpenCode's shell tool implementation for Codegeist T007_04_04. Identify the main source files and explain command schema, selected shell, cwd/workdir resolution, permission checks, process spawning, timeout/abort handling, output capture, truncation, metadata, session persistence, and tests."

/ask-project opencode "Analyze OpenCode's shell input schema. Why does OpenCode expose the tool id as bash, what fields does it accept, why is description required, how is timeout represented, and should Codegeist use command/cwd/timeoutSeconds without description for T007_04_04?"

/ask-project opencode "Analyze OpenCode shell selection and process command construction. How does it choose Bash, PowerShell, cmd, configured shells, login shells, or denied shells, and why is Codegeist's first implementation allowed to use cmd.exe /c on Windows and sh -lc elsewhere?"

/ask-project opencode "Analyze OpenCode workdir and external-directory behavior. How does OpenCode resolve workdir relative to the project instance, decide whether a path is internal, and ask external_directory permission? Translate that into Codegeist's requirement to reject cwd escape before ProcessBuilder.start()."

/ask-project opencode "Analyze OpenCode shell command scanning with tree-sitter. What file-oriented commands or path arguments can it detect, what permission requests are produced, what paths remain dynamic or ambiguous, and why should Codegeist defer command scanning in T007_04_04?"

/ask-project opencode "Analyze OpenCode shell process lifecycle. Include stdin handling, streaming metadata, stdout/stderr merging or separation, output tailing, exit-code handling, timeout, abort, child-process or process-tree termination, and final result assembly. Recommend a Java ProcessBuilder lifecycle for Codegeist."

/ask-project opencode "Analyze OpenCode shell output truncation and full-output side files. Include line caps, byte caps, head/tail behavior, truncation metadata, saved-output hints, tests, and why Codegeist should persist only bounded stdout/stderr previews in ToolSessionPart.outputPreview."

/ask-project opencode "Analyze OpenCode shell timeout and abort tests. What exact behaviors do tests assert for timeout, stderr, non-zero exit, truncation, and metadata updates? Recommend focused JUnit assertions for CodegeistShellTool."

/ask-project opencode "Analyze OpenCode's session processor and tool wrapper for shell results. Which shell fields are persisted or emitted as events, and how should they map to Codegeist's existing ToolSessionPart(tool,status,outputPreview)?"

/ask-project opencode "Create an OpenCode-to-Codegeist decision table for T007_04_04. Columns: source behavior, keep, simplify, defer, drop, Codegeist class/method target, and test evidence."
```

## Pi Questions

Pi is useful for one-shot bash behavior, AgentSession tool hooks, and JSONL
persistence. Use these questions to keep Codegeist's first shell tool small while
preserving useful output and persistence lessons.

```text
/ask-project pi "Deep dive into Pi's bash or shell tool implementation for Codegeist T007_04_04. Identify source files and explain tool schema, command execution flow, cwd handling, timeout or cancellation, stdout/stderr handling, output bounding, result formatting, session events, JSONL persistence, and tests."

/ask-project pi "Analyze Pi's active tool registry and bash tool prompt/schema. How does the model learn when to use bash, which fields are accepted, and how should Codegeist describe codegeist_shell without adding unrelated fields?"

/ask-project pi "Analyze Pi's command output accumulator, truncation, or tail behavior for bash output. What text is returned to the model, what is persisted, and what stable headings should Codegeist use for stdout/stderr/exit-code/timeout?"

/ask-project pi "Analyze Pi AgentSession hooks around tool_call and tool_result for bash. Which lifecycle or extension hooks are useful design evidence, and which plugin or event hooks should Codegeist defer until after T007_04_04?"

/ask-project pi "Analyze Pi JSONL persistence for bash tool calls and results. Which fields map to Codegeist ToolSessionPart.outputPreview, and which call ids, metadata, timings, or extension payloads should remain out of .codegeist/session.json?"

/ask-project pi "Analyze Pi cwd and workspace behavior for bash execution. Does Pi hard reject outside paths, rely on session cwd, or allow broader execution? Recommend Codegeist cwd validation and error messages."

/ask-project pi "Analyze Pi tests for bash command success, command failure, output truncation, tool call persistence, and session hooks. Recommend CodegeistLocalToolsTest methods and fixtures for T007_04_04."

/ask-project pi "Create a Pi-to-Codegeist decision table for T007_04_04. Columns: source behavior, keep, simplify, defer, drop, Codegeist class/method target, and test evidence."
```

## Aider Questions

Aider is useful for command, lint, and test feedback loops, but much of its
behavior is repo/git and reflection-loop oriented. Use these questions to extract
bounded shell-result lessons without importing broader agent-loop behavior.

```text
/ask-project aider "Deep dive into Aider command execution for Codegeist T007_04_04. Identify source files for shell commands, run_cmd helpers, lint/test commands, command output capture, return-code handling, user-visible feedback, and tests."

/ask-project aider "Analyze how Aider runs shell commands requested by the model or user. Include cwd, environment assumptions, command echoing, return code, stdout/stderr handling, timeout behavior if present, and how output is fed back into the chat."

/ask-project aider "Analyze Aider lint/test command loops. How are commands selected, run, retried, summarized, and reflected back to the model? Which feedback concepts belong later to a Codegeist control loop rather than T007_04_04?"

/ask-project aider "Analyze Aider command output bounding. How are long outputs, diffs, lint results, and test failures truncated or summarized? Recommend what Codegeist should keep in one bounded ToolSessionPart.outputPreview."

/ask-project aider "Analyze Aider's repo and file safety assumptions around shell commands. Which behaviors depend on git, dirty files, auto-commit, lint/test config, or user prompts and should stay out of CodegeistShellTool?"

/ask-project aider "Analyze Aider tests related to command execution, shell output, lint/test feedback, failure handling, and path/repo context. Recommend Codegeist tests that avoid git-specific and reflection-loop behavior."

/ask-project aider "Create an Aider-to-Codegeist decision table for T007_04_04. Columns: source behavior, keep, simplify, defer, drop, Codegeist class/method target, and test evidence."
```

## mini-SWE-agent Questions

mini-SWE-agent is useful as a minimal shell-first environment and trajectory
reference. Use these questions to understand the risks of shell-only mutation and
benchmark-specific abstractions.

```text
/ask-project mini-swe-agent "Deep dive into mini-SWE-agent command execution for Codegeist T007_04_04. Identify source files for action parsing, local environment execution, Docker or sandbox environments, timeout handling, output capture, return-code semantics, observations, trajectories, and tests."

/ask-project mini-swe-agent "Analyze mini-SWE-agent's local environment process model. How are command strings executed, which shell is used, what cwd is active, how are environment variables handled, and how are return codes and exceptions represented?"

/ask-project mini-swe-agent "Analyze mini-SWE-agent timeout and process termination behavior. Does it kill the direct process, process group, descendants, containers, or sessions? Recommend a Java ProcessBuilder termination strategy and documentation cautions for Codegeist."

/ask-project mini-swe-agent "Analyze mini-SWE-agent observations and trajectories for command output. Which fields are command, stdout, stderr, return code, timeout, exception, duration, or metadata? Which map to Codegeist ToolSessionPart.outputPreview and which are benchmark-only?"

/ask-project mini-swe-agent "Analyze mini-SWE-agent's shell-first file mutation approach. What risks appear when file edits are done only through shell commands, and why should Codegeist keep codegeist_edit separate from codegeist_shell?"

/ask-project mini-swe-agent "Analyze mini-SWE-agent tests for action parsing, environment execution, timeout, failed commands, output observations, and trajectory writing. Recommend Codegeist shell tests that do not require SWE-bench or Docker."

/ask-project mini-swe-agent "Create a mini-SWE-agent-to-Codegeist decision table for T007_04_04. Columns: source behavior, keep, simplify, defer, drop, Codegeist class/method target, and test evidence."
```

## Spring AI Agent Utils Questions

Spring AI Agent Utils is useful for Java implementation patterns and possible
tool reuse. Use these questions before deciding whether to implement
`CodegeistShellTool` directly or wrap an existing utility.

```text
/ask-project spring-ai-agent-utils "Deep dive into Spring AI Agent Utils shell or command execution utilities for Codegeist T007_04_04. Identify Java classes, @Tool methods or ToolCallback implementations, input schemas, cwd support, timeout behavior, stdout/stderr capture, exit-code semantics, output bounds, exception behavior, and tests."

/ask-project spring-ai-agent-utils "Analyze Agent Utils ToolCallback registration patterns relevant to CodegeistShellTool. Compare @Tool methods, MethodToolCallbackProvider, FunctionToolCallback, direct ToolCallback implementation, and ToolContext for per-chat working directory and recorder state."

/ask-project spring-ai-agent-utils "Analyze whether Agent Utils ShellTools should be reused, wrapped, or avoided for Codegeist T007_04_04. Compare Codegeist requirements for codegeist_shell naming, cwd containment, no background process registry, no stdin, bounded stdout/stderr previews, handled failures, and ToolSessionPart recording."

/ask-project spring-ai-agent-utils "Analyze Agent Utils timeout and output handling for shell commands. Does it capture stdout and stderr separately, avoid deadlocks, return exit code, terminate on timeout, or support background commands? Recommend direct Java implementation details for Codegeist."

/ask-project spring-ai-agent-utils "Analyze Agent Utils tests for shell utilities and ToolCallbacks. Which tests can inspire CodegeistLocalToolsTest coverage for success, non-zero exit, timeout, output bounds, cwd handling, and exception mapping?"

/ask-project spring-ai-agent-utils "Analyze Spring AI ToolContext as a way to pass working directory or recorder state. Should CodegeistShellTool use ToolContext, or keep the current CodegeistLocalTool wrapper that gets workspace from WorkspaceResolver and recording from CodegeistLocalToolCallback?"

/ask-project spring-ai-agent-utils "Create a Spring-AI-Agent-Utils-to-Codegeist decision table for T007_04_04. Columns: source behavior, keep, simplify, defer, drop, Codegeist class/method target, and test evidence."
```

## Cross-Project Shell Contract Questions

Run these after at least one answer exists for every project.

```text
/ask-project opencode "Synthesize OpenCode, Pi, Aider, mini-SWE-agent, and Spring AI Agent Utils evidence for Codegeist T007_04_04. Produce one shell-tool contract table covering input fields, cwd policy, timeout, stdout/stderr capture, output bounds, exit-code handling, timeout status, session persistence, tests, and deferred behavior."

/ask-project spring-ai-agent-utils "Using all five project answers, recommend the final Java class and method design for CodegeistShellTool. Include records, helper classes, ProcessBuilder lifecycle, ExecutorService or stream-drain strategy, timeout termination, output summary formatting, and exception mapping."

/ask-project opencode "Compare how all five projects represent non-zero exit and timeout. Should Codegeist record them as completed shell results or failed tool calls while ToolSessionPart only has completed/failed status? Provide a source-backed recommendation."

/ask-project pi "Compare stdout/stderr treatment across all five projects. Should Codegeist keep streams separate in one outputPreview, merge them, stream live metadata, or persist typed fields later? Recommend the first stable summary format."

/ask-project mini-swe-agent "Compare cwd and environment assumptions across all five projects. Which behavior supports Codegeist's hard cwd containment check, and what should the docs say is not a sandbox?"

/ask-project aider "Compare shell-result feedback loops across all five projects. Which result details are useful in the current one-turn callback harness, and which belong later to an iterative model/tool/model agent loop?"
```

## Workspace, Cwd, And Safety Questions

Use these when implementation needs exact cwd containment or failure behavior.

```text
/ask-project opencode "Compare OpenCode workdir permission behavior with Codegeist's hard cwd containment requirement. Recommend exact checks for relative cwd, absolute cwd, traversal, symlink escape, missing cwd, file cwd, and workspace root cwd."

/ask-project spring-ai-agent-utils "Recommend a Java NIO implementation for Codegeist shell cwd containment. Include Path.normalize, toAbsolutePath, Files.exists, Files.isDirectory, toRealPath, symlink escapes, Windows path separators, and focused JUnit fixtures."

/ask-project pi "How should Codegeist phrase cwd-related tool failures so the model can recover? Compare Pi and OpenCode user-visible errors for missing cwd, outside cwd, command startup failure, timeout, and non-zero exit."

/ask-project mini-swe-agent "What process or container isolation assumptions in mini-SWE-agent should Codegeist explicitly avoid claiming for a local ProcessBuilder shell tool?"

/ask-project aider "Which repo-specific command safety concepts in Aider are not portable to Codegeist's current workspace-only shell tool? Include git dirty state, auto-commit, repo map, lint/test config, and human command prompts."
```

## Output Bounds And Persistence Questions

Use these to finalize the text summary and `ToolSessionPart` policy.

```text
/ask-project opencode "Map OpenCode shell output metadata and session fields to Codegeist ToolSessionPart.outputPreview. Which fields should be included as stable headings, and which metadata, timing, side files, or live updates should be deferred?"

/ask-project pi "Map Pi bash JSONL entries to Codegeist .codegeist/session.json. Which command/result fields should be visible in outputPreview, and which extension or runtime fields should not be persisted?"

/ask-project mini-swe-agent "Map mini-SWE-agent action/observation trajectory fields to Codegeist ToolSessionPart. Which fields are essential for shell result rendering, and which are benchmark/replay details to defer?"

/ask-project aider "Analyze Aider command result display and transcript behavior. Which command output should Codegeist store versus only return to the model, and how should large outputs be bounded?"

/ask-project spring-ai-agent-utils "Recommend a bounded stdout/stderr collector for CodegeistShellTool. Include per-stream cap, final ToolOutputBounds.preview cap, truncation flags, UTF-8 decoding behavior, and tests for large output."
```

## Test And Verification Questions

Use these to build the final test list before writing Java code.

```text
/ask-project opencode "List OpenCode tests most relevant to CodegeistShellTool. Group by command success, stderr, non-zero exit, timeout, abort, workdir, permission/external directory, truncation, and session metadata."

/ask-project pi "List Pi tests most relevant to CodegeistShellTool. Group by bash success, failure, output bounding, session event hooks, JSONL persistence, and any cwd behavior."

/ask-project aider "List Aider tests most relevant to command execution and output feedback. Group by shell command, lint/test command, output handling, failure feedback, and git-specific behavior to avoid."

/ask-project mini-swe-agent "List mini-SWE-agent tests most relevant to command execution. Group by action parsing, local environment execution, timeout, failed commands, observations, trajectories, and benchmark-specific behavior to avoid."

/ask-project spring-ai-agent-utils "List Spring AI Agent Utils tests most relevant to shell utilities and ToolCallbacks. Group by callback registration, direct invocation, process execution, timeout, stdout/stderr, exit code, and exception handling."

/ask-project spring-ai-agent-utils "Design the final Codegeist JUnit test plan for T007_04_04. Include CodegeistLocalToolsTest method names, temp-directory fixtures, platform-specific command helpers, cwd escape marker assertions, timeout test shape, bounds assertions, and existing ask no-continue regression coverage."
```

## Final Synthesis Questions

Run these after project-specific and cross-project answers are available.

```text
/ask-project opencode "Produce a final T007_04_04 implementation readiness report using all five project answers. Include the accepted codegeist_shell contract, rejected alternatives, Java component design, process lifecycle, cwd validation, output bounds, ToolSessionPart policy, tests, docs, and open risks."

/ask-project spring-ai-agent-utils "Produce a final Java/Spring implementation checklist for CodegeistShellTool using all five project answers. Include files to edit, classes to add, methods to implement, constants, records, helper types, tests, architecture docs, and verification commands."

/ask-project pi "Produce a final source-backed shell output preview recommendation for Codegeist. Include exact stable headings, truncation flags, empty stdout/stderr behavior, non-zero exit examples, timeout examples, and persistence notes."

/ask-project mini-swe-agent "Produce a final safety and non-goals checklist for CodegeistShellTool. Include what is bounded, what is explicitly not sandboxed, process termination limitations, cwd limitations, environment assumptions, and deferred features."

/ask-project aider "Produce a final agent-loop deferral checklist for CodegeistShellTool. Identify which command feedback, retry, lint/test, reflection, git, and human approval behaviors belong after T007_04_04 rather than in the first shell tool."
```

## Expected Research Output

After enough questions are answered, update or create these files in this
directory:

- `shell-tool-implementation-plan.md` - accepted implementation handoff for
  `codegeist_shell`.
- `opencode-shell-tool-comparison.md` - update only if new OpenCode details change
  the current comparison.
- `ask-project-research.md` - append source-backed shell-only findings only when
  they supersede or refine the existing broader T007_04 research.
- `tasks/T007_04_04_add-shell-tool.md` - record accepted decisions, result, and
  verification after implementation.

If implementation changes package boundaries, classes, configuration behavior, or
tests, update current-state architecture docs under
`docs/developer/architecture/` in the same task.
