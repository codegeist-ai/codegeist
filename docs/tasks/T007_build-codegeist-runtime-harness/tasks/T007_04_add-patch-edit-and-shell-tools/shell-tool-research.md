# T007_04_04 Shell Tool Research Answers

Source-backed answers for `shell-tool-question-catalog.md` and implementation
evidence for `tasks/T007_04_04_add-shell-tool.md`.

Status note: this research preserves the earlier source-backed design evidence. The
implemented `codegeist_shell` was later simplified to plain `ProcessBuilder`
execution with merged stdout/stderr, optional `timeoutSeconds`, no workspace cwd
containment, and bounded completed shell summaries. Use
`tasks/T007_04_04_add-shell-tool.md` and
`docs/developer/architecture/local-file-tools.md` for the current contract.

## Scope And Evidence

This research answers the shell-only questions for Codegeist `T007_04_04` across
all current third-party analysis workspaces:

| Project | Evidence used | Runtime verification |
| --- | --- | --- |
| OpenCode | `docs/third-party/opencode/source/`, `repomix-output.xml`, durable analysis docs | Static only. No OpenCode commands or tests were run. |
| Pi | `docs/third-party/pi/source/`, durable analysis docs | Static only. No Pi commands or tests were run. |
| Aider | `docs/third-party/aider/source/`, `repomix-output.xml`, durable analysis docs | Static only. No Aider sessions, shell commands, or tests were run. |
| mini-SWE-agent | `docs/third-party/mini-swe-agent/source/`, durable analysis docs | Static only. No environments, provider calls, or tests were run. |
| Spring AI Agent Utils | `docs/third-party/spring-ai-agent-utils/source/` | Static only. No upstream tests were run. |

The goal is not to copy any third-party implementation directly. The goal is to
translate source-backed behavior into the smallest correct Java/Spring
`CodegeistShellTool` that fits current Codegeist contracts.

## Codegeist Baseline At Research Time

Codegeist local tools already live under
`app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`.

Important current contracts:

- Local tools implement `CodegeistLocalTool` and are discovered by
  `CodegeistLocalTools` as Spring components.
- `CodegeistLocalToolCallback` records successful tool results as completed
  `ToolSessionPart` values and converts handled `CodegeistToolException` failures
  into failed `ToolSessionPart` values.
- `ToolSessionPart` persists only `tool`, `status`, and `outputPreview`.
- Existing local callbacks are `codegeist_read`, `codegeist_list`,
  `codegeist_glob`, `codegeist_grep`, `codegeist_write`, and `codegeist_edit`.
- `CodegeistFileToolSupport` already provides JSON parsing, schema creation,
  active workspace lookup, path display, shared line separator, and access to
  `ToolOutputBounds`.
- `ToolOutputBounds` currently caps final previews at `MAX_PREVIEW_CHARS = 8000`
  and single-line previews at `MAX_LINE_CHARS = 500`.
- Codegeist has no permission loop, no plugin environment hook, no managed
  full-output artifact lifecycle, no persistent shell, and no iterative
  model/tool/model control loop yet.

## Original Executive Decisions

| Decision | Accepted Codegeist Behavior | Source Evidence |
| --- | --- | --- |
| Tool name | Add `codegeist_shell`. | Codegeist already uses `codegeist_*`; OpenCode/Pi/mini-SWE use generic `bash`, but Codegeist should preserve its product namespace. |
| Input fields | Use required `command`, optional `cwd`, optional `timeoutSeconds`. | OpenCode Core uses `command`, `workdir`, `timeout`; Pi uses `command`, optional timeout seconds with cwd bound at tool creation; Agent Utils uses `command`, `timeout`, `description`, `runInBackground`. Codegeist needs per-call cwd and readable timeout config. |
| Description field | Omit `description` in this slice. | OpenCode requires it for user-facing permission/UI context; Codegeist has no TUI permission loop yet. Agent Utils makes it optional. |
| Timeout unit | Use seconds. | OpenCode and Agent Utils use millisecond timeout. Pi uses seconds. Codegeist chose seconds for the current `codegeist.yml` and tool-call contract. |
| Shell wrapper | Use optional `tools.codegeist-shell.command-prefix` as an explicit host-side argv prefix; default to `cmd.exe /c` on Windows and `sh -lc` elsewhere. | OpenCode has broad automatic shell discovery; Pi has configured discovery; Agent Utils uses Bash or `cmd.exe`. Codegeist keeps explicit config plus defaults instead of automatic discovery. |
| Process model | One process per tool call, no stdin, no persistent shell, no background registry, no PTY. | OpenCode and Pi use one-shot tool execution; Agent Utils background support conflicts with T007_04_04; Aider's `pexpect` path is interactive and should be avoided. |
| Cwd policy | The original research recommendation was to resolve `cwd` under the active workspace and reject normalized or real-path escapes before startup. The implemented tool later allowed absolute cwd values and documented the no-containment contract in the current task and architecture docs. | OpenCode asks external-directory permission instead of hard rejection; Codegeist lacks permission UI, so the stricter draft was considered before the current simpler contract was chosen. Pi/Aider/mini-SWE do not provide enough containment. |
| `dir-guard-disabled` | Do not apply `workspace.dir-guard-disabled` to shell cwd. | Current Codegeist docs define that switch for file mutation only; shell cwd containment is a separate T007_04_04 safety requirement. |
| stdout/stderr | Capture concurrently and preserve separate previews. | Agent Utils captures separately; OpenCode legacy merges but Core separates internally; Pi/Aider/mini-SWE mostly merge. The task explicitly requires separate previews. |
| Non-zero exit | Completed shell result with `Exit code: <n>`. | OpenCode Core, Agent Utils, mini-SWE, and Aider all treat non-zero command output as command result evidence rather than a wrapper crash. |
| Timeout | Completed shell result with `Timed out: true`, bounded partial output, and best-effort termination. | mini-SWE returns timeout as command result metadata; OpenCode returns timeout metadata; Agent Utils returns timeout text. The Codegeist task allows final status selection and benefits from completed shell-result semantics. |
| Failed tool calls | Invalid input, cwd escape, missing cwd, file cwd, startup failure, interruption, collector failure. | These are tool-wrapper failures rather than command outcomes. |
| Output persistence | Keep one bounded text summary in `ToolSessionPart.outputPreview`; no typed fields or side files. | OpenCode/Pi have richer metadata and side files, but Codegeist has no artifact lifecycle and `ToolSessionPart` intentionally stays small. |
| Reuse | Implement Codegeist-owned `CodegeistShellTool`; do not directly expose Agent Utils `ShellTools`. | Agent Utils lacks cwd containment, has background registry fields, no Codegeist session recording, and no Codegeist-specific output format. |

## Original Planned `codegeist_shell` Contract

Tool name:

```text
codegeist_shell
```

Input schema:

| Field | Required | Type | Behavior |
| --- | --- | --- | --- |
| `command` | yes | string | Non-blank shell command to execute once. |
| `cwd` | no | string | Working directory under active workspace. Defaults to `.`. |
| `timeoutSeconds` | no | integer | Positive timeout in seconds. Omitted or invalid values use the configured default. |

Stable output preview:

```text
Command: <bounded command>
Cwd: <workspace-relative cwd>
Exit code: <code or n/a>
Timed out: <true|false>
Stdout truncated: <true|false>
Stderr truncated: <true|false>
Stdout:
<bounded stdout preview or (empty)>
Stderr:
<bounded stderr preview or (empty)>
```

Recommended timeout constants:

| Constant | Value | Rationale |
| --- | --- | --- |
| `DEFAULT_TIMEOUT_SECONDS` | `120` | Bounded by default while keeping normal commands usable. |
| `TERMINATION_GRACE_MILLIS` | `500` | Gives normal termination a short chance before force kill. |

Direct wrapper config:

```yaml
tools:
  codegeist-shell:
    command-prefix: [docker, run, --rm, ubuntu, bash, -lc]
```

Codegeist appends the model-supplied `command` as the final argv entry. This makes
Docker or another wrapper possible, but it does not make a sandbox guarantee; mounts,
users, network, container working directory, and isolation flags belong to the
configured wrapper arguments.

## OpenCode Answers

### Source Files And Anchors

OpenCode has two relevant shell implementations.

Legacy/rich shell tool:

| Source | Responsibility |
| --- | --- |
| `docs/third-party/opencode/source/packages/opencode/src/tool/shell.ts` | Main shell tool: config lookup, cwd resolution, permission scanning, process execution, timeout/abort race, output truncation, final metadata. |
| `docs/third-party/opencode/source/packages/opencode/src/tool/shell/prompt.ts` | Shell-specific prompt and schema for Bash, PowerShell, and cmd. |
| `docs/third-party/opencode/source/packages/opencode/src/tool/shell/id.ts` | Exposed shell tool id and permission key, still `bash` for compatibility. |
| `docs/third-party/opencode/source/packages/opencode/src/shell/shell.ts` | Shell discovery, configured-shell fallback, denied shells, Git Bash handling. |
| `docs/third-party/opencode/source/packages/opencode/src/session/tools.ts` | Runtime bridge from tool definitions to AI SDK tools, permission context, metadata, plugin hooks. |
| `docs/third-party/opencode/source/packages/opencode/src/session/processor.ts` | Session tool-part state updates and tool success events. |
| `docs/third-party/opencode/source/packages/opencode/src/tool/truncate.ts` | Generic output truncation and full-output side-file storage. |

Core V2 shell tool:

| Source | Responsibility |
| --- | --- |
| `docs/third-party/opencode/source/packages/core/src/tool/bash.ts` | Smaller BashTool with schema, workdir approval, process run, output schema, timeout. |
| `docs/third-party/opencode/source/packages/core/src/process.ts` | Process service that captures stdout/stderr and exit code. |
| `docs/third-party/opencode/source/packages/core/src/location-mutation.ts` | Path/workdir resolution, external path permission metadata, relative escape handling. |
| `docs/third-party/opencode/source/packages/core/src/tool-output-store.ts` | Managed full-output side-file storage. |

### Tool Id And Schema

Legacy OpenCode exposes the shell tool under `bash`, even on PowerShell or cmd,
because `src/tool/shell/id.ts:14-16` keeps that id for compatibility. Its schema
is in `src/tool/shell/prompt.ts:22-30`:

- `command`: required string.
- `description`: required string.
- `timeout`: optional positive integer in milliseconds.
- `workdir`: optional string.

The prompt tells the model to use `workdir` instead of `cd` and says output is
truncated with full output saved to a file (`src/tool/shell/prompt.ts:103-127`).
Tests enforce `command` plus `description` and optional `timeout`/`workdir` in
`test/tool/parameters.test.ts:108-123`.

Core V2 still uses `bash`. It defines `DEFAULT_TIMEOUT = 120000`,
`MAX_TIMEOUT = 600000`, and a 1 MiB per-stream capture cap in
`packages/core/src/tool/bash.ts:16-19`. Its input has required `command`, optional
`workdir`, optional millisecond `timeout`, and optional `description` in
`bash.ts:21-34`. Its output schema carries `command`, `cwd`, optional `exitCode`,
compact `output`, stdout/stderr truncation flags, optional `timedOut`, and
optional warnings in `bash.ts:36-47`.

Codegeist should keep its own name and smaller schema: `codegeist_shell` with
`command`, `cwd`, and `timeoutSeconds`. OpenCode's `description` is valuable for
permission UI and TUI display, but it is not required by current Codegeist
`ToolSessionPart` or the one-turn callback harness.

### Shell Selection

OpenCode legacy shell discovery classifies known shells, denies Fish/Nu for tool
use, reads configured shell settings, handles Windows candidates, and falls back
on Unix paths. Relevant anchors are `src/shell/shell.ts:10-20`, `:91-113`, and
`:125-130`. The shell tool constructs PowerShell commands with `-NoLogo`,
`-NoProfile`, `-NonInteractive`, and `-Command`; other shells run the command
through the selected shell with ignored stdin and non-Windows detachment in
`src/tool/shell.ts:302-319`.

Core V2 uses configured `shell` when present, otherwise `/bin/sh` or
`COMSPEC`/`cmd.exe` (`packages/core/src/tool/bash.ts:51`, `:155-159`). It starts
one child process through `ChildProcess.make(...)` with cwd, shell, ignored stdin,
detached mode, and force-kill grace in `bash.ts:159-165`.

Codegeist should not copy shell discovery yet. The accepted first slice is
an explicit `tools.codegeist-shell` wrapper when configured, otherwise `cmd.exe /c`
on Windows and `sh -lc` elsewhere.

### Cwd And External Directory Policy

OpenCode legacy resolves `workdir` relative to the instance directory and defaults
to the instance directory (`src/tool/shell.ts:625-628`). It does not hard reject
external cwd; it adds `external_directory` permission if cwd is outside the
instance directory or git worktree (`src/tool/shell.ts:639-641`). The containment
test is in `src/project/instance-context.ts:13-24`, and permission request
construction is in `src/tool/shell.ts:266-288`.

Core V2 resolves workdir through `LocationMutation.resolve({ path: input.workdir ??
".", kind: "directory" })` (`core/src/tool/bash.ts:130`) and asks
`external_directory` before the `bash` permission when workdir is external
(`bash.ts:131-150`). `LocationMutation` rejects relative escapes, canonicalizes
real paths, and returns external permission metadata for external absolute paths
(`core/src/location-mutation.ts:119-148`).

The original Codegeist recommendation was stricter because Codegeist had no
permission loop: reject cwd escape before `ProcessBuilder.start()`, including
normalized path escape and symlink real-path escape. The implemented shell tool later
chose a simpler no-containment cwd contract, documented in the current task and
architecture docs.

### Command Scanning

Legacy OpenCode loads `web-tree-sitter` with Bash and PowerShell grammars
(`src/tool/shell.ts:320-345`), parses commands (`:260-264`), scans file-oriented
commands and cwd commands (`:28-67`), resolves static file arguments (`:177-219`,
`:387-423`), and produces external-directory and bash permission patterns
(`:416-419`).

Core V2 intentionally leaves richer scanning as TODO (`core/src/tool/bash.ts:80-83`)
and only emits advisory external absolute path warnings (`bash.ts:93-105`,
`:139-142`).

Codegeist should defer tree-sitter scanning, shell command static analysis,
permission prompts, reusable always-approval patterns, and external file-argument
permission checks.

### Process Lifecycle

Legacy OpenCode sets running metadata before spawn (`src/tool/shell.ts:485-490`),
spawns one child process per call (`:492-496`), reads merged output from
`handle.all` (`:497-543`), races exit, abort, and timeout (`:546-559`), kills on
abort/timeout with force-kill grace (`:561-570`), and appends timeout/abort details
to `<shell_metadata>` (`:574-597`).

Core V2's `AppProcess.run` concurrently collects stdout, stderr, and exit code
(`core/src/process.ts:137-145`), bounds stdout/stderr capture separately (`:110-127`),
and supports timeout and abort (`:156-169`). Its cross-spawn cleanup sends TERM and
escalates to KILL (`core/src/cross-spawn-spawner.ts:371-400`).

Codegeist should follow the Core direction: two concurrent stream drains,
separate stdout/stderr previews, one `ProcessBuilder` process per call, ignored or
closed stdin, timeout race, and best-effort termination.

### Truncation And Side Files

OpenCode legacy keeps live metadata previews around 30,000 characters
(`src/tool/shell.ts:28`, `:223-226`) and final shell output uses tail truncation by
max lines/bytes (`:228-257`). Defaults are 2,000 lines and 50 KiB; full output is
written under a truncation directory (`src/tool/truncate.ts:16-18`, `:69-83`,
`:86-142`). The shell output can include `Full output saved to: ...` and metadata
`outputPath` (`src/tool/shell.ts:584-605`).

Core V2 has producer capture caps (`core/src/tool/bash.ts:19`, `:167-172`) plus a
managed output store (`core/src/tool-output-store.ts:12-16`, `:132-168`).

Codegeist should not add full-output side files in T007_04_04. It should keep
bounded stdout and stderr previews in a single `ToolSessionPart.outputPreview`.

### Persistence And Metadata

OpenCode's tool context exposes `metadata(...)` and `ask(...)` callbacks
(`src/session/tools.ts:45-76`), runs plugin before/after hooks (`:87-117`), and
updates running/completed tool state through `SessionProcessor` (`src/session/processor.ts:191-230`).
V1 tool parts support pending/running/completed/error states and store input,
output, title, metadata, time, and attachments (`packages/core/src/v1/session.ts:250-313`).
Shell final metadata includes `output`, `exit`, `description`, `truncated`, and
optional `outputPath` (`src/tool/shell.ts:598-608`).

Codegeist should keep `ToolSessionPart` unchanged. Command, cwd, exit code,
timeout, truncation flags, stdout, and stderr can be rendered as stable bounded
text.

### OpenCode Tests To Mirror Selectively

Useful OpenCode tests:

- Schema: `packages/opencode/test/tool/parameters.test.ts:108-123`.
- External workdir: `packages/opencode/test/tool/shell.test.ts:776-800`.
- External file args: `shell.test.ts:898-932`.
- Permission patterns: `shell.test.ts:956-1035`.
- Abort, timeout, stderr, non-zero, metadata streaming: `shell.test.ts:1038-1167`.
- Truncation and full-output files: `shell.test.ts:1169-1238`.
- Shell selection: `packages/opencode/test/shell/shell.test.ts:23-99`.
- Core V2 bash tests: `packages/core/test/tool-bash.test.ts:127-159`, `:166-182`,
  `:242-292`, `:323-398`, and TODO locks at `:401-417`.

Codegeist should mirror success, stderr, non-zero exit, timeout, truncation, and
cwd rejection. It should defer permission-pattern, external approval, command
scanning, and side-file tests.

## Pi Answers

### Source Files And Anchors

| Source | Responsibility |
| --- | --- |
| `docs/third-party/pi/source/packages/coding-agent/src/core/tools/bash.ts` | Pi bash tool schema, process execution, timeout, output accumulation, result conversion. |
| `docs/third-party/pi/source/packages/coding-agent/src/core/tools/index.ts` | Built-in tool names and registration. |
| `docs/third-party/pi/source/packages/coding-agent/src/core/agent-session.ts` | Active tool defaults, runtime/tool registry, extension hooks, direct bash execution. |
| `docs/third-party/pi/source/packages/coding-agent/src/core/tools/output-accumulator.ts` | Bounded streaming accumulator and full-output temp-file support. |
| `docs/third-party/pi/source/packages/coding-agent/src/core/tools/truncate.ts` | Default max lines and bytes. |
| `docs/third-party/pi/source/packages/coding-agent/src/utils/shell.ts` | Shell discovery/config and process-tree kill helper. |
| `docs/third-party/pi/source/packages/coding-agent/src/core/messages.ts` | BashExecutionMessage and message-to-text conversion. |
| `docs/third-party/pi/source/packages/coding-agent/src/core/session-manager.ts` | JSONL session header and message append behavior. |
| `docs/third-party/pi/source/packages/coding-agent/src/core/extensions/types.ts` | Tool call/result/user bash event types and extension result types. |

### Schema And Registry

Pi's bash schema is in `src/core/tools/bash.ts:24-27`: required `command` and
optional `timeout` in seconds. `createBashToolDefinition(...)` at `bash.ts:274-286`
sets tool name `bash`, binds cwd when the tool is created, says stdout/stderr are
returned together, and documents truncation to the last default lines/bytes with
full output saved to a temp file.

Built-in tool names are in `src/core/tools/index.ts:81-84`: `read`, `bash`,
`edit`, `write`, `grep`, `find`, and `ls`. `createAllToolDefinitions(...)` at
`:156-165` registers built-ins. Agent session defaults are documented as
`[read, bash, edit, write]` in `src/core/agent-session.ts:171-172`, selected in
`_buildRuntime(...)` at `:2436-2439`, refreshed in `_refreshToolRegistry(...)` at
`:2299-2389`, and altered by `setActiveToolsByName(...)` at `:812-827`.

Codegeist should add `codegeist_shell` as a normal local callback beside existing
tools, but not add Pi's active-tool registry model in this slice.

### Cwd And Workspace Behavior

Pi SDK default cwd is `process.cwd()` (`src/core/sdk.ts:34-36`). The bash tool
binds cwd at tool/session level: `BashOperations.exec(command, cwd, ...)` in
`bash.ts:48-57`, and `createLocalBashOperations(...)` only checks that cwd exists
in `bash.ts:69-74`. Pi docs say tools run with the user account's permissions and
are not sandboxed (`docs/security.md:31-35`). Quickstart says it runs in the
current working directory (`docs/quickstart.md:77-84`).

Codegeist needs stricter per-call cwd policy because the task requires cwd escape
rejection before process startup. It should not inherit Pi's broad user-account
execution posture.

### Process Model And Shell Selection

Pi shell selection lives in `src/utils/shell.ts`. `ShellConfig` is in `:6-10`,
normal Bash uses `-c`, legacy WSL uses stdin (`:20-22`), Windows search order is
`:76-106`, and Unix discovery tries `/bin/bash`, Bash on PATH, then `sh` (`:109-119`).

Local execution uses `spawn(...)` in `src/core/tools/bash.ts:79-86`. stdout and
stderr are both piped but both feed one `onData` collector (`:106-108`).
Non-Windows uses a detached process group (`:82`).

Codegeist should not copy shell discovery or merged output. It should keep simple
platform shell selection and separate stream capture.

### Timeout And Cancellation

Pi sets a timeout and kills the process tree in `bash.ts:99-105`. Abort signals
also kill the tree (`:109-113`). Timeout throws `timeout:<seconds>` (`:120-122`),
and tool execution converts timeout/abort into thrown errors (`:390-399`).
Process-tree kill is implemented by `killProcessTree(...)` in `src/utils/shell.ts:197-220`.
Direct bash execution in `AgentSession.executeBash(...)` uses `_bashAbortController`
(`agent-session.ts:2591-2618`) and `abortBash()` (`:2651-2655`).

Codegeist should borrow best-effort termination but not Pi's status choice. The
T007 task wants timeout recorded explicitly, and a completed shell result with
`Timed out: true` is more useful than a failed wrapper status.

### Output Bounds

Pi default truncation limits are `DEFAULT_MAX_LINES = 2000` and
`DEFAULT_MAX_BYTES = 50 * 1024` in `src/core/tools/truncate.ts:11-12`. The
streaming accumulator keeps bounded memory and can preserve full output in a temp
file (`src/core/tools/output-accumulator.ts:28-35`, `:91-118`). The bash tool
combines stdout and stderr through the same `onData` path (`bash.ts:106-108`).

Codegeist should keep the bounding lesson but not the full-output temp-file
lifecycle or merged output. Use existing `ToolOutputBounds` and stable stdout/stderr
sections.

### Hooks And Persistence

Pi installs LLM tool hooks in `_installAgentToolHooks()` (`agent-session.ts:406-462`).
`tool_call` can block before execution (`:415-433`), and `tool_result` can modify
result content/details/error (`:436-460`). Event types include `UserBashEvent` in
`extensions/types.ts:765-774`, `ToolCallEvent` in `:806-865`, and
`ToolResultEvent` in `:867-924`. Result types appear in `:1020-1038`.

JSONL message models include `BashExecutionMessage` fields in `src/core/messages.ts:26-40`
and `bashExecutionToText(...)` at `:80-97`. Session header fields are in
`src/core/session-manager.ts:32-39`, message entry fields at `:53-56`, JSONL append
at `:909-935`, and `appendMessage(...)` at `:951-960`.

Codegeist should not add Pi-style extension hooks, user bash mode, or a new
`BashExecutionMessage`. Existing `ToolSessionPart` is sufficient for T007_04_04.

### Pi Tests To Mirror Selectively

The Pi research identified tests around bash success/failure, output bounds,
session events, and JSONL persistence. Codegeist should mirror only local callback
success, non-zero, timeout, bounds, and persistence-through-recorder behavior. It
should not mirror extension hook tests, interactive `!` shell mode, or temp
full-output file tests.

## Aider Answers

### Source Files And Anchors

| Source | Responsibility |
| --- | --- |
| `docs/third-party/aider/source/aider/run_cmd.py` | Low-level command runners: subprocess and pexpect. |
| `docs/third-party/aider/source/aider/commands.py` | `/run`, `/test`, `/lint`, `/git`, command output insertion into chat. |
| `docs/third-party/aider/source/aider/coders/base_coder.py` | Main coder loop, reflection cap, platform/shell/test/lint prompt context, post-edit lint/test/shell execution, edit permission/git gates, commits. |
| `docs/third-party/aider/source/aider/linter.py` | Lint command execution and contextual lint output. |
| `docs/third-party/aider/source/aider/coders/editblock_coder.py` | Shell fenced block extraction from model output. |
| `docs/third-party/aider/source/aider/coders/shell.py` | Prompt guidance for suggested shell commands. |
| `docs/third-party/aider/source/aider/io.py` | Chat-history and tool-output transcript writing. |

### Command Execution Paths

Aider has user command execution and model-suggested shell block execution.
`Commands.run(...)` maps `!command` to `/run` by calling `do_run("run", inp[1:])`
at `commands.py:312-316`. `/run` uses `cmd_run(...)` at `commands.py:1013-1053`,
and `/test` delegates to `cmd_run(args, True)` for string commands at
`commands.py:993-1005`.

Model-suggested shell blocks are extracted by `EditBlockCoder.get_edits(...)` into
`self.shell_commands` at `editblock_coder.py:21-35`. Fenced `bash`, `sh`, `shell`,
`cmd`, `batch`, `powershell`, and `ps1` blocks can become shell commands in
`find_original_update_blocks(...)` at `editblock_coder.py:451-484`. `Coder.run_shell_commands(...)`
deduplicates and prompts before running them (`base_coder.py:2434-2485`). The shell
prompt asks for 1-3 complete one-line commands and says they run from project root
(`coders/shell.py:1-20`).

Codegeist should not parse fenced shell blocks in this task. Shell execution must
remain an explicit `codegeist_shell` callback.

### Cwd And Environment

Aider's cwd is repo-root centered. `Coder.__init__` sets `self.root` from the git
repo root when available, otherwise a common root or current cwd (`base_coder.py:446-477`).
`cmd_run(...)` passes `cwd=self.coder.root` to `run_cmd(...)` (`commands.py:1015-1017`).
Suggested shell commands also pass `cwd=self.root` (`base_coder.py:2475`). Linter
commands run with `cwd=self.root` (`linter.py:53-57`).

Aider loads `.env` files into process env (`main.py:361-387`), and
`run_cmd_subprocess(...)` inherits the process environment because it does not pass
a custom env (`run_cmd.py:62-73`). `/git` sets `GIT_EDITOR=true` (`commands.py:971-983`).

Codegeist should avoid git-root and env-file assumptions in `CodegeistShellTool`.
Use active workspace and explicit cwd validation.

### stdout/stderr, Return Codes, Timeout

Aider generally merges stderr into stdout. `run_cmd_subprocess(...)` uses
`stderr=subprocess.STDOUT`, reads one character at a time, prints live output,
waits, and returns `(returncode, output)` (`run_cmd.py:62-84`). `/git` also merges
stderr (`commands.py:974-984`). `flake8_lint(...)` captures stdout and stderr
separately but concatenates them (`linter.py:151-168`).

`run_cmd(...)` chooses an interactive `pexpect` path when stdin is a TTY, pexpect
exists, and the platform is not Windows (`run_cmd.py:11-16`). `run_cmd_pexpect(...)`
spawns `$SHELL -i -c <command>` and calls `child.interact(...)` (`run_cmd.py:107-128`).

Aider has no mandatory shell timeout. `run_cmd_subprocess(...)` calls
`process.wait()` indefinitely (`run_cmd.py:83`). General keyboard interrupt
handling exists in the coder loop (`base_coder.py:986-1000`) but not as a shell
tool timeout.

Codegeist should do the opposite for T007_04_04: no PTY, no stdin, no interactive
handoff, mandatory timeout support, and separate stdout/stderr previews.

### Output Bounding And Reflection

Aider does not hard-bound command output. `run_cmd_subprocess(...)` stores all
characters (`run_cmd.py:75-84`). `cmd_run(...)` token-counts output and asks
whether to add it, but does not truncate it (`commands.py:1022-1039`).
`handle_shell_commands(...)` accumulates command output and asks whether to add it
(`base_coder.py:2465-2485`).

Aider's reflection loop is valuable future design evidence but not shell-tool
scope. `Coder.run_one(...)` repeats while `self.reflected_message` exists, capped
by `max_reflections = 3` (`base_coder.py:932-945`). Lint errors and test failures
can become reflected messages (`base_coder.py:1599-1607`, `:1616-1623`).

Codegeist should persist only bounded shell output now. Later control-loop tasks
can decide whether shell output drives model continuation.

### Persistence And Git-Specific Behavior

Aider persists chat history as markdown-style transcript entries. `InputOutput.user_input(...)`
and `ai_output(...)` write user/assistant text (`io.py:775-795`), `tool_output(...)`
appends blockquoted tool/status output (`io.py:995-1000`), and
`append_chat_history(...)` writes to a file (`io.py:1117-1132`). Command output added
to chat becomes user content plus assistant `Ok.` (`commands.py:1036-1044`).

Aider's safety model is git-heavy: main can create git repos and `.gitignore`
(`main.py:88-174`), `allowed_to_edit(...)` uses tracked state, gitignore, chat-file
confirmation, git add, and dirty pre-commit logic (`base_coder.py:2175-2240`), and
commits are built into the coder (`base_coder.py:2375-2423`).

Codegeist should not copy markdown transcript shell persistence or git safety
policy. Keep structured `ToolSessionPart` and workspace cwd checks.

### Aider Tests To Mirror Selectively

Useful Aider tests include `tests/basic/test_run_cmd.py:6-11`, linter command
tests in `tests/basic/test_linter.py:30-80`, `/run` and `/test` non-zero output in
`tests/basic/test_commands.py:1110-1148`, suggested shell extraction in
`tests/basic/test_coder.py:975-1004`, and shell suggestion settings in
`tests/basic/test_main.py:735-763`.

Codegeist should mirror command success/non-zero output feedback and avoid
git-root, auto-commit, shell-suggestion, and reflection-loop tests.

## mini-SWE-agent Answers

### Source Files And Anchors

| Source | Responsibility |
| --- | --- |
| `docs/third-party/mini-swe-agent/source/src/minisweagent/models/utils/actions_toolcall.py` | Tool-call schema, action parsing, observation formatting. |
| `docs/third-party/mini-swe-agent/source/src/minisweagent/models/utils/actions_text.py` | Legacy regex/fenced command parsing and observation formatting. |
| `docs/third-party/mini-swe-agent/source/src/minisweagent/models/litellm_model.py` | Tool-call model integration. |
| `docs/third-party/mini-swe-agent/source/src/minisweagent/environments/local.py` | Local command execution. |
| `docs/third-party/mini-swe-agent/source/src/minisweagent/environments/docker.py` | Docker command execution backend. |
| `docs/third-party/mini-swe-agent/source/src/minisweagent/agents/default.py` | Agent loop and trajectory serialization. |
| `docs/third-party/mini-swe-agent/source/src/minisweagent/config/mini.yaml` | Prompt and observation formatting config. |

### Action Parsing

mini-SWE-agent exposes one tool named `bash` with required `command` in
`actions_toolcall.py:11-27`. `LitellmModel._query()` passes `tools=[BASH_TOOL]`
(`litellm_model.py:64-71`). `parse_toolcall_actions()` rejects no tool calls,
unknown tool names, invalid JSON, and missing command (`actions_toolcall.py:30-75`).
The legacy text path requires exactly one fenced command
(`actions_text.py:15-40`).

Codegeist should keep explicit Spring AI tool calls and avoid regex/fenced command
parsing.

### Local And Docker Execution

`LocalEnvironment.execute()` reads `command`, chooses cwd from execute argument,
config cwd, or `os.getcwd()`, merges host env with configured env, and returns
`output`, `returncode`, and `exception_info` (`local.py:24-43`). `_run()` uses
`subprocess.Popen(command, shell=True, text=True, cwd=cwd, env=env,
encoding="utf-8", errors="replace")`, merges stderr into stdout, and starts a new
POSIX process session (`local.py:72-92`).

`DockerEnvironment` starts a long-lived container (`docker.py:74-99`) and executes
commands with `docker exec -w <cwd> ... <container_id> *interpreter command`, where
default interpreter is `bash -lc` (`docker.py:15-42`, `:101-138`).

Codegeist should use one local `ProcessBuilder` process. Container backends are
out of scope.

### Cwd, Timeout, Output, Return Codes

mini-SWE-agent docs say directory/env changes are not persistent between shell
actions (`docs/faq.md:102-119`, `config/mini.yaml:35-40`). There is no workspace
guard in the local environment.

Timeout uses `process.communicate(timeout=timeout)`. On timeout, POSIX kills the
process group with `os.killpg(process.pid, signal.SIGKILL)` and non-POSIX kills the
direct process; then it captures remaining output and re-raises
`TimeoutExpired` (`local.py:86-92`). `LocalEnvironment.execute()` converts that to
`returncode: -1`, exception text, and `exception_type: "TimeoutExpired"`
(`local.py:31-41`). Tests cover timeout result shape and POSIX child-process kill in
`tests/environments/test_local.py:127-168` and partial output before timeout in
`tests/agents/test_default.py:207-229`.

mini-SWE-agent treats non-zero exits as normal command results. `LocalEnvironment.execute()`
returns the actual return code with no exception (`local.py:29-30`), and tests cover
`exit 1`, `exit 42`, and nonexistent commands in `tests/environments/test_local.py:100-116`,
`:203-217`. Docker tests assert `exit 42` returns `42` (`test_docker.py:195-205`).

Output is merged, not separate. Observation formatting stores rendered content plus
`extra.raw_output`, `extra.returncode`, `extra.timestamp`, and `extra.exception_info`
(`actions_toolcall.py:90-112`, `actions_text.py:52-70`). Built-in config bounds
model-visible output at 10,000 chars with head/tail display, but raw output can
still be persisted in `extra.raw_output` (`config/mini.yaml:112-151`).

Codegeist should keep non-zero-as-result and partial timeout output but use separate
stdout/stderr previews and no raw output persistence.

### Trajectories And Shell-Only Mutation Risks

`DefaultAgent.serialize()` writes broad trajectory metadata, model stats, config,
exit status, submission, messages, and format (`agents/default.py:157-178`), then
`save()` writes JSON (`:180-188`). Output-file docs describe messages and `extra`
fields. ProgramBench drops `raw_output` to avoid bloated trajectories
(`run/benchmarks/programbench.py:33-43`).

mini-SWE-agent prompts models to mutate files through shell commands such as
heredocs and `sed -i` (`config/default.yaml:60-90`, `config/mini.yaml:55-87`). This
is a caution for Codegeist: shell-only mutation lacks preflight validation,
workspace target checks, no-partial-write guarantees, structured diff summaries,
and platform-independent edit semantics.

Codegeist should keep `codegeist_write` and `codegeist_edit` as first-class file
tools and not route safe file mutation through `codegeist_shell`.

## Spring AI Agent Utils Answers

### Source Files And Anchors

| Source | Responsibility |
| --- | --- |
| `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/ShellTools.java` | Shell utility with `Bash`, `BashOutput`, `KillShell`, timeout, stdout/stderr capture, background registry. |
| `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/utils/AgentEnvironment.java` | Git/system context helper with private git command execution. |
| `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/SkillsTool.java` | `FunctionToolCallback` builder example. |
| `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/TaskTool.java` | `FunctionToolCallback` builder example. |
| `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentType.java` | Converts tool objects to callbacks with `MethodToolCallbackProvider`. |

### Tool Registration And Schema

`ShellTools` uses Spring AI `@Tool` methods, not explicit `ToolCallback` builders.
`bash(...)` is annotated as `@Tool(name = "Bash", ...)` at `ShellTools.java:165`,
`bashOutput(...)` as `BashOutput` at `:369`, and `killShell(...)` as `KillShell` at
`:414`. Examples pass `ShellTools.builder().build()` through
`ChatClient.defaultTools(...)` (`README.md:165-171`,
`examples/skills-demo/.../Application.java:41-43`). `ClaudeSubagentType` uses
`MethodToolCallbackProvider.builder().toolObjects(...)` and includes `ShellTools`
(`ClaudeSubagentType.java:126-131`).

`Bash` parameters are `command` required (`ShellTools.java:238`), optional
`timeout` max 600000 ms (`:239`), optional `description` (`:240`), and optional
`runInBackground` (`:241`). `BashOutput` uses `bash_id` and optional `filter`
(`:379-380`), and `KillShell` uses `bash_id` (`:422`).

Codegeist should keep its current explicit `ToolDefinition` path through
`CodegeistLocalTool.definition()`. Do not expose Agent Utils `Bash`, `BashOutput`,
or `KillShell` directly.

### Cwd, Timeout, stdout/stderr, Exit Codes

`ShellTools` does not support cwd input. `bash(...)` has no cwd parameter
(`ShellTools.java:237-241`), and the `processBuilder.directory(...)` line is
commented out (`:260-262`). It inherits the Java process working directory and
therefore cannot enforce Codegeist's active workspace or reject cwd escape.

Synchronous `bash(...)` defaults timeout to 120000 ms and caps at 600000 ms
(`:276`). It uses `process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)` (`:310`). On
timeout it calls `process.destroy()`, waits up to five seconds, then
`destroyForcibly()` (`:312-317`) and returns `Command timed out after <timeout>ms`.
It does not include partial stdout/stderr on timeout. Background mode bypasses
timeout, starts a process, stores it, and returns a shell id (`:265-273`).

`ShellTools` captures stdout and stderr separately by using
`redirectErrorStream(false)` (`:258`) and reader threads for both streams
(`:281-305`). It appends stdout first, then `STDERR:` (`:329-337`). Reader IO
errors are swallowed. It treats non-zero exit as normal output by appending
`Exit code: <code>` only when non-zero (`:323`, `:339-343`), and tests assert
non-zero capture in `ShellToolsTest.java:98-106`.

Synchronous output is capped at 30,000 characters (`ShellTools.java:345-353`), but
background output uses unbounded `StringBuilder`s (`:42-44`) and `bashOutput(...)`
adds new output without truncation (`:388-410`).

Codegeist should borrow separate stream capture, timeout, and non-zero-as-result,
but not background registry, missing cwd support, unbounded background buffers, or
optional `description`/`runInBackground` schema.

### ToolContext Suitability

Spring AI Agent Utils does not use `ToolContext` in the inspected source. Current
Codegeist `CodegeistLocalToolCallback` also ignores `ToolContext` and calls the
same code path (`app/codegeist/cli/src/main/java/ai/codegeist/app/tool/CodegeistLocalToolCallback.java:59-62`).

For T007_04_04, keep workspace and recording through current Codegeist services,
not `ToolContext`.

### Agent Utils Tests To Mirror Selectively

`ShellToolsTest.java` covers simple commands (`:50-90`), non-zero exit and stderr
(`:98-139`), timeout (`:147-178`), background start/output/completion (`:186-236`),
`BashOutput` filtering (`:244-354`), `KillShell` (`:362-416`), shell choice
(`:424-441`), synchronous truncation (`:449-463`), and shell id generation
(`:472-490`).

Codegeist should mirror success, non-zero, timeout, stderr, and truncation. It
should not mirror background, `BashOutput`, `KillShell`, shell id generation, or
filtering.

## Cross-Project Contract Synthesis

### Input Contract

| Project | Fields | Codegeist Translation |
| --- | --- | --- |
| OpenCode legacy | `command`, required `description`, optional millisecond `timeout`, optional `workdir` | Keep `command`; use `timeoutSeconds`; use `cwd`; omit `description`. |
| OpenCode Core | `command`, optional `workdir`, optional millisecond `timeout`, optional `description` | Same as above. |
| Pi | `command`, optional timeout seconds; cwd bound at tool creation | Keep command/timeout idea; make cwd per-call and use seconds. |
| Aider | User `/run` command or model-suggested fenced shell blocks | Do not parse shell blocks; keep explicit tool. |
| mini-SWE-agent | Tool named `bash` with required `command` | Keep explicit command schema, not generic name. |
| Agent Utils | `command`, optional timeout, optional description, optional background | Keep command/timeout; drop background and description. |

Final Codegeist schema: `command`, `cwd`, `timeoutSeconds`.

### Cwd And Safety

| Project | Cwd/Safety Behavior | Codegeist Decision |
| --- | --- | --- |
| OpenCode | External cwd can be allowed after permission. | Hard reject because Codegeist has no permission loop. |
| Pi | Session/tool cwd; only checks cwd exists. | Stronger active-workspace containment. |
| Aider | Git root/project root. | Do not use git root as safety boundary. |
| mini-SWE-agent | Environment cwd; no workspace guard. | Stronger active-workspace containment. |
| Agent Utils | No cwd parameter. | Implement Codegeist-owned cwd resolution. |

Accepted cwd algorithm:

```text
workspace = support.currentWorkspace().toAbsolutePath().normalize()
cwdText = blank input cwd ? "." : input cwd
candidate = support.resolvePath(workspace, cwdText).toAbsolutePath().normalize()

if candidate does not start with workspace:
    fail before process startup

if candidate does not exist:
    fail before process startup

realWorkspace = workspace.toRealPath()
realCandidate = candidate.toRealPath()

if realCandidate does not start with realWorkspace:
    fail before process startup

if realCandidate is not a directory:
    fail before process startup
```

Explicitly not claimed:

- No sandbox.
- No command argument/file path policy.
- No protection from all child process escape patterns.
- No environment isolation.
- No git ignore or protected-file policy.

### Process Lifecycle

Accepted lifecycle:

1. Parse JSON into `ShellToolInput`.
2. Validate `command` non-blank.
3. Resolve and validate cwd before `ProcessBuilder.start()`.
4. Build platform shell command.
5. Start one process.
6. Close process stdin immediately.
7. Drain stdout and stderr concurrently.
8. Wait for timeout.
9. On timeout, terminate descendants and process best-effort.
10. Wait for collectors.
11. Render bounded summary.
12. Let `CodegeistLocalToolCallback` record the completed or failed part.

### Status Semantics

| Event | Codegeist Status | Reason |
| --- | --- | --- |
| Exit `0` | `completed` | Normal shell result. |
| Non-zero exit | `completed` | All projects treat command failure as useful output; task explicitly asks for completed shell result. |
| Timeout after successful startup | `completed` | It is a shell result with `Timed out: true`, not an invalid invocation. |
| Missing command | `failed` | Invalid tool input. |
| Cwd escape | `failed` | Rejected before side effect. |
| Missing cwd or file cwd | `failed` | Invalid execution location. |
| Startup failure | `failed` | Tool wrapper could not start a process. |
| Interrupted execution | `failed` | Host thread interruption. |
| Collector failure | `failed` | Tool wrapper failed to capture output safely. |

### Output Bounds And Persistence

All projects show that shell output can become large. OpenCode and Pi add side
files; mini-SWE-agent can persist raw output; Aider can add unbounded output to
chat; Agent Utils caps only synchronous output and leaves background output
unbounded. Codegeist should use its existing stronger bounded-preview rule.

Accepted output rules:

- Keep stdout and stderr separate in one `outputPreview`.
- Keep a per-stream cap before assembling the final summary.
- Include `Stdout truncated` and `Stderr truncated` flags.
- Apply `ToolOutputBounds.preview(...)` to the final summary.
- Do not persist raw full output.
- Do not write full-output side files.
- Do not persist environment variables or process ids.

### ToolSessionPart Mapping

| Shell Concept | Persist In T007_04_04? | Location |
| --- | --- | --- |
| Tool name | yes | `ToolSessionPart.tool = codegeist_shell` |
| Status | yes | `completed` or `failed` |
| Command | yes, bounded | `outputPreview` heading |
| Cwd | yes, display path | `outputPreview` heading |
| Exit code | yes | `outputPreview` heading |
| Timed out | yes | `outputPreview` heading |
| stdout | yes, bounded | `outputPreview` section |
| stderr | yes, bounded | `outputPreview` section |
| Truncation flags | yes | `outputPreview` headings |
| Start/end time | no | Deferred typed metadata. |
| Duration | no | Deferred typed metadata. |
| Process id | no | Avoid persisting runtime internals. |
| Environment | no | Avoid secrets and runtime internals. |
| Full output path | no | No side-file lifecycle. |
| Tool input JSON | no | Existing tool part does not persist input. |

## Test And Verification Answers

### Required JUnit Coverage

Add focused tests to
`app/codegeist/cli/src/test/java/ai/codegeist/app/tool/CodegeistLocalToolsTest.java`.

| Test | Required Assertions |
| --- | --- |
| `exposesLocalCallbacksByName` | Includes `CodegeistShellTool.TOOL_NAME`. |
| `shellSchemaExposesCommandCwdAndTimeoutSeconds` | Schema contains `command`, `cwd`, `timeoutSeconds`; excludes `description`, `workdir`, `timeoutMillis`, `runInBackground`, `bash_id`, `filter`, `background`, `pty`, `stdin`. |
| `shellRunsSuccessfulCommandAndRecordsCompletedToolPart` | Command emits stdout and stderr; preview has stable headings; `Exit code: 0`; `Timed out: false`; part is completed and preview equals returned text. |
| `shellRunsInsideWorkspaceRelativeCwd` | Command runs in a workspace subdirectory when `cwd` is relative. |
| `shellRecordsNonZeroExitAsCompletedResult` | Exit `7` is completed, not failed; preview includes `Exit code: 7` and stderr/output text. |
| `shellRecordsTimeoutAsCompletedResult` | Short timeout records `Timed out: true`, completed status, and bounded partial output if available. |
| `shellRejectsCwdEscapeBeforeProcessStartup` | External cwd fails; a marker-writing command does not create the marker. |
| `shellRejectsCwdEscapeWhenDirGuardIsDisabled` | `workspace.dir-guard-disabled: true` does not bypass shell cwd containment. |
| `shellRejectsMissingAndFileCwdBeforeStartup` | Missing cwd and file cwd fail before command execution. |
| `shellRejectsSymlinkEscapedCwd` | Symlink to outside workspace fails by real-path check when symlinks are supported. |
| `shellRejectsBlankCommand` | Blank or missing command is a failed tool part. |
| `shellOutputIsBoundedAndPersistedAsTheSamePreview` | Large stdout/stderr result is within `ToolOutputBounds.MAX_PREVIEW_CHARS`; truncation flags are stable; recorded preview equals returned preview. |

### Platform-Aware Test Commands

Use helper methods rather than duplicating platform logic in each test.

| Behavior | Unix command | Windows command |
| --- | --- | --- |
| stdout and stderr | `printf 'out'; printf 'err' >&2` | `echo out& echo err 1>&2` |
| non-zero exit | `printf 'bad' >&2; exit 7` | `echo bad 1>&2 & exit /b 7` |
| timeout | `printf 'before'; sleep 2` | `echo before & ping -n 3 127.0.0.1 > nul` |
| marker write | `printf marker > marker.txt` | `echo marker> marker.txt` |

### Existing Regression Coverage

Run existing chat tests when the shell tool is implemented:

- `ChatHarnessServiceTest` proves recorded local tool parts are saved before
  assistant text.
- `AskCommandsSessionStoreTest` proves plain no-continue ask output/delegation stays
  unchanged.
- `SessionStoreServiceTest` already proves `ToolSessionPart` JSON round-trip.
- `CodegeistToolServiceTest` proves local callbacks are exposed through
  `CodegeistChatExecutionContext`.

### Verification Commands

Run from `app/codegeist/cli`:

```bash
task test TEST=CodegeistLocalToolsTest,ChatHarnessServiceTest,AskCommandsSessionStoreTest
```

Then run the broad JVM suite when the focused tests pass:

```bash
task test
```

Run repo diff hygiene:

```bash
git --no-pager diff --check
```

## Original Keep, Simplify, Defer, Drop

| Category | Items |
| --- | --- |
| Keep | One explicit local shell tool; one process per call; required command; optional cwd; optional seconds timeout; non-zero exit as completed result; timeout as completed result; bounded preview; existing `ToolSessionPart` shape. The original workspace-contained cwd and concurrent stdout/stderr recommendations were later simplified to no cwd containment and merged output in the implemented tool. |
| Simplify | Use explicit `tools.codegeist-shell.command-prefix` wrapper config plus `sh -lc` / `cmd.exe /c` defaults; no automatic shell discovery; no dynamic prompt by shell type; no `description`; no live metadata; no side files; no typed shell session fields. |
| Defer | Permission prompts; external-directory approvals; tree-sitter command scanning; command allow/deny patterns; plugin env hooks; TUI cancellation; model reflection loop; lint/test retry loop; background shell registry; PTY; Docker/sandbox backends; managed full-output artifacts; duration/timing metadata. |
| Drop/Avoid | Sandbox claims; shell-only file mutation as the preferred edit path; unbounded output; raw full-output persistence; git root as safety policy; auto-commit/undo behavior; treating `workspace.dir-guard-disabled` as a shell cwd policy. |

## Final Implementation Readiness Checklist

Before writing production code:

- Keep `shell-tool-implementation-plan.md` aligned with this research.
- Use `timeoutSeconds`; the open decision in the plan is now resolved.
- Add tests first in `CodegeistLocalToolsTest`.
- Add `CodegeistShellTool` as a package-private Spring component.
- Reuse `CodegeistFileToolSupport` for parsing, schema, workspace, display paths,
  and output bounds.
- Do not add new public APIs to support tests only.
- Do not change `ToolSessionPart`.
- Do not add `codegeist_patch`.
- Do not change plain `ask` stdout behavior.

After implementation:

- Update `docs/developer/architecture/local-file-tools.md`.
- Update `docs/developer/architecture/architecture.md`.
- Mark `tasks/T007_04_04_add-shell-tool.md` solved with result and verification.
- Refresh `docs/memory-bank/chat.md` because implemented runtime state changes.
