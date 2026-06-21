# T007_04 Patch Edit And Shell Tools Research

Source-backed answers for `ask-project-question-catalog.md`. This document
summarizes the follow-up research pass across Aider, OpenCode, Pi,
mini-SWE-agent, and Spring AI Agent Utils before implementing Codegeist patch/edit
and shell tools.

## Scope And Evidence

- Task: `T007_04_add-patch-edit-and-shell-tools/task.md`.
- Question catalog: `ask-project-question-catalog.md`.
- Projects queried through the local `/ask-project` research pattern:
  `aider`, `opencode`, `pi`, `mini-swe-agent`, and
  `spring-ai-agent-utils`.
- Evidence type: static analysis from the existing third-party workspaces under
  `docs/third-party/<project>/`, especially their `repomix-output.xml` artifacts
  and durable local analysis docs. No upstream runtime commands, provider calls,
  or upstream tests were run for this pass.
- This research answers design questions for Codegeist. It does not implement
  Java runtime behavior and does not claim that third-party runtime behavior was
  verified locally.

## Current Codegeist Baseline

Codegeist T007_03 already provides the foundation that T007_04 should extend:

- `.codegeist/session.json` and `ToolSessionPart` persistence exist.
- Local read/list/glob/grep/write callbacks already run through the one-turn
  `ChatHarnessService` and `CodegeistToolService` path.
- MCP callbacks are prompt-scoped and bounded before persistence.
- `ToolSessionPart` currently stores only `tool`, `status`, and `outputPreview`.
  Do not add typed shell fields, patch hunks, timing, metadata maps, attachments,
  or lifecycle states unless the active implementation tests require them.
- T007_04 must add bounded side-effecting patch/edit and shell tools. File
  mutation outside the working directory and shell cwd escape must fail before the
  side effect runs.

## Executive Decisions

Use these decisions as the implementation starting point unless a focused test or
user instruction changes the scope.

| Decision | Recommendation | Evidence |
| --- | --- | --- |
| Tool split | Keep `write`, `edit`, `patch`, and `shell` as separate concepts. Existing `codegeist_write` remains create/overwrite; T007_04 adds precise edit/patch and shell. | OpenCode separates `write`, `edit`, `apply_patch`, and `bash`; Pi separates `write`, `edit`, and `bash`; Aider supports several edit formats but keeps command execution distinct. |
| Patch/edit contract | Implement a Codegeist-owned exact edit and/or structured patch tool, not shell-only mutation. Parse/validate before writing and reject ambiguous changes. | OpenCode `edit` and `apply_patch`; Pi exact multi-edit with diff/patch details; Aider parse/dry-run/edit pipeline; mini-SWE-agent shows the risks of shell-only mutation. |
| Workspace guard | Add a hard Codegeist working-directory guard for mutating file paths and shell cwd. Resolve/normalize paths and reject traversal/symlink escape before side effects. | OpenCode asks permission for external directories but Codegeist lacks a permission loop; Pi accepts broader paths; Aider's repo boundary is git-specific; Agent Utils lacks containment. |
| Shell process model | Execute one local process per tool call. No stdin, persistent shell, background process registry, or command streaming in T007_04. | OpenCode and Pi shell tools are one-shot child processes; mini-SWE-agent local environment runs one command; Agent Utils background shell support is too broad. |
| Shell result status | Treat non-zero exit as a completed shell result with exit code in the bounded summary. Treat invalid input, cwd escape, missing cwd, and startup failures as failed tool calls. Timeout should be recorded explicitly; prefer completed shell result with `Timed out: true` unless tests require `failed`. | OpenCode and Agent Utils return non-zero exit as shell output; mini-SWE-agent returns timeout as command result; current Codegeist has only completed/failed status. |
| Output bounds | Store bounded text summaries only. Do not persist raw full stdout/stderr, unbounded diffs, process ids, environment variables, or side-file paths unless a future artifact lifecycle exists. | OpenCode truncates and can write side files; Pi tail-truncates and can save full logs; Aider lacks sufficient hard command-output bounds; mini-SWE-agent raw trajectory output can bloat. |
| Session schema | Keep `ToolSessionPart(tool,status,outputPreview)` for T007_04 and encode details in stable bounded text headings. | T007 rules say add fields only when tests need them; Spring AI Agent Utils has no durable session schema. |
| Reuse of Agent Utils | Do not directly reuse `FileSystemTools` or `ShellTools` for T007_04 side effects. Use Agent Utils as source inspiration for tests and callback patterns behind Codegeist wrappers. | Agent Utils tools lack Codegeist working-dir policy, session recording, and prompt-scoped runtime state. |
| Deferred safety | Defer permission prompts, TUI patch review, tree-sitter command scanning, plugin/env hooks, background shells, patch side-file artifacts, git commits, and benchmark trajectories. | These are broad or product-specific in OpenCode, Aider, mini-SWE-agent, and Agent Utils. |

## Recommended Tool Contracts

### Edit Tool

Suggested first-slice behavior:

- Input: path under the active working directory plus one or more exact
  replacements.
- Reject missing `oldText`, identical old/new text, missing target file,
  directory targets, no match, ambiguous repeated matches unless an explicit
  `replaceAll` option is supported, overlapping edits, and path escape.
- Match against the original file content for multi-edit requests.
- Preserve common file characteristics where practical, especially line endings
  and BOM, when tests cover them.
- Return a bounded summary with changed file, replacements applied, first changed
  line if available, and a bounded diff or diff preview.

Evidence:

- OpenCode `packages/opencode/src/tool/edit.ts` uses exact `oldString` / `newString`
  semantics, rejects ambiguous or unsafe replacements, supports `replaceAll`, and
  serializes same-file edits.
- Pi `packages/coding-agent/src/core/tools/edit.ts` supports `path` plus `edits[]`,
  unique/non-overlapping old text, line-ending and BOM handling, fuzzy tolerance for
  whitespace/punctuation, and returns diff/patch details.
- Aider `aider/coders/editblock_coder.py`, `aider/coders/search_replace.py`, and
  related tests show a parse-before-write flow with clear failure feedback.
- Spring AI Agent Utils `FileSystemTools.Edit` is useful as an exact-string test
  reference, but it does not enforce Codegeist policy.

### Patch Tool

Suggested first-slice behavior:

- Prefer a structured patch format only if tests need multi-file add/update/delete
  in one call. Otherwise start with exact edit and keep patch as the next focused
  child within T007_04.
- If implemented now, parse the whole patch before mutation, validate every target
  path under the working directory, reject invalid or partial patches before side
  effects when feasible, and summarize changed files plus bounded hunks.
- Do not use shell commands as the patch application path.

Evidence:

- OpenCode `packages/opencode/src/tool/apply_patch.ts` plus
  `packages/opencode/src/patch/index.ts` support `*** Begin Patch` actions for
  add/update/delete/move, parse first, then apply changes.
- Aider `aider/coders/patch_coder.py` supports a V4A-style patch format, while
  `aider/coders/udiff_coder.py` and docs under `aider/website/docs/more/` show
  model-specific edit format tradeoffs.
- Pi does not expose a separate patch tool in the inspected runtime; its edit tool
  returns patch details.
- mini-SWE-agent has no first-class patch tool, which is a useful warning against
  relying on shell-only file mutation.

### Shell Tool

Suggested first-slice behavior:

- Tool name: keep Codegeist namespace, for example `codegeist_shell`.
- Input: `command`, optional `cwd` or `workdir`, optional timeout. Prefer one name
  consistently. `cwd` matches current Codegeist wording; `workdir` mirrors
  OpenCode.
- Resolve cwd against the active working directory. Reject cwd escape before
  starting the process.
- Use `cmd.exe /c` on Windows and `sh -lc` or another simple platform shell on
  Unix-like systems. Do not add configured shell discovery until tests need it.
- Ignore stdin. Do not support background processes, persistent shell sessions, or
  PTY behavior.
- Capture stdout and stderr concurrently to avoid deadlocks. Keep separate previews
  in one bounded summary.
- Timeout must terminate the process and, where practical, descendants. Record
  `Timed out: true` in output.
- Non-zero exit code is not a tool wrapper failure; it is a completed shell result.

Evidence:

- OpenCode `packages/opencode/src/tool/shell.ts`, `tool/shell/prompt.ts`,
  `tool/shell/id.ts`, and `src/shell/shell.ts` provide the richest shell evidence:
  command schema, selected shell, external-directory permission, command scanning,
  timeout/abort, output truncation, and session metadata.
- Pi `packages/coding-agent/src/core/tools/bash.ts`, `output-accumulator.ts`, and
  `truncate.ts` show one-shot shell execution, output truncation, and persisted
  bash execution messages.
- Aider `aider/run_cmd.py`, `aider/commands.py`, and `aider/coders/shell.py` show
  command feedback and reflection patterns but lack mandatory timeouts.
- mini-SWE-agent `src/minisweagent/environments/local.py` provides a minimal local
  shell environment with timeout and process-group killing.
- Spring AI Agent Utils `ShellTools` has useful tests for timeout/output, but its
  background shell process map and missing cwd containment are too broad for direct
  reuse.

## Bounded Output Preview Formats

Use stable headings so tests can assert contract-bearing text without parsing
provider- or platform-specific prose.

### Shell Preview

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

Do not persist environment variables, process ids, raw full output, or unbounded
error traces.

### Edit Preview

```text
File: <workspace-relative path>
Operation: edit
Replacements: <count>
First changed line: <line or n/a>
Diff truncated: <true|false>
Diff:
<bounded diff preview>
```

### Patch Preview

```text
Operation: patch
Files changed: <count>
Added: <count>
Updated: <count>
Deleted: <count>
Moved: <count>
Diff truncated: <true|false>
Summary:
<bounded per-file summary>
```

## Project Answers

## OpenCode

OpenCode is the richest reference for tool lifecycle, side-effect gates, output
truncation, and persisted tool parts.

Important sources:

- `packages/opencode/src/tool/write.ts`
- `packages/opencode/src/tool/edit.ts`
- `packages/opencode/src/tool/apply_patch.ts`
- `packages/opencode/src/patch/index.ts`
- `packages/opencode/src/tool/shell.ts`
- `packages/opencode/src/tool/shell/prompt.ts`
- `packages/opencode/src/tool/shell/id.ts`
- `packages/opencode/src/shell/shell.ts`
- `packages/opencode/src/tool/external-directory.ts`
- `packages/opencode/src/permission/index.ts`
- `packages/opencode/src/session/tools.ts`
- `packages/opencode/src/session/processor.ts`
- `packages/opencode/src/tool/truncate.ts`
- `packages/core/src/v1/session.ts`
- Tests under `packages/opencode/test/tool/` and `packages/opencode/test/patch/`.

Answers:

- `write` creates or overwrites whole files, creates parents, preserves some file
  characteristics, formats/touches editor integrations, and asks `edit` permission.
  Codegeist already has `codegeist_write`; do not fold precise edit/patch semantics
  into it.
- `edit` is a localized exact replacement tool with `filePath`, `oldString`,
  `newString`, and optional `replaceAll`. It rejects identical old/new strings,
  missing files, directories, missing matches, ambiguous matches, and unsafe fuzzy
  matches.
- `apply_patch` accepts `patchText`, parses the full custom patch grammar first,
  supports add/update/delete/move, asks one edit permission batch, then applies the
  mutation.
- `bash` is the shell tool id for compatibility. It accepts `command`, required
  `description`, optional millisecond `timeout`, and optional `workdir`.
- OpenCode resolves external directories through a permission system. Codegeist does
  not have that loop, so T007_04 should hard reject cwd/path escape instead.
- OpenCode scans shell commands with tree-sitter for file-oriented commands and
  asks `external_directory` or `bash` permissions. This is too broad for T007_04.
- OpenCode output truncation has global line/byte caps and optional full-output side
  files. Codegeist should keep bounded previews only until an artifact lifecycle is
  designed.
- OpenCode persists richer tool parts: input, output/error, title, metadata,
  status, timing, and attachments. Codegeist should not add all of that to
  `ToolSessionPart` now; encode needed fields in `outputPreview`.

Keep:

- distinct edit, patch, and shell contracts;
- parse/validate before mutation;
- non-zero shell exit as a result, not a wrapper failure;
- timeout/cancellation text in shell output;
- source-backed tests around path policy, patch parse/apply, exact edit ambiguity,
  shell timeout, shell exit code, stderr, and truncation.

Defer or drop:

- external-directory permission prompts;
- persistent always-allow rules;
- plugin `shell.env` hooks;
- live running metadata;
- full-output side files;
- configured shell discovery;
- tree-sitter command scanning;
- LSP/format/watch integration;
- model-specific hiding of `edit`/`write` versus `apply_patch`.

`opencode-shell-tool-comparison.md` remains the deeper OpenCode shell-specific
evidence file for implementation detail.

## Pi

Pi is the closest reference for a compact CLI `AgentSession` runtime with tool
execution, extension hooks, output bounds, and JSONL persistence.

Important sources:

- `packages/coding-agent/src/core/tools/index.ts`
- `packages/coding-agent/src/core/tools/edit.ts`
- `packages/coding-agent/src/core/tools/edit-diff.ts`
- `packages/coding-agent/src/core/tools/write.ts`
- `packages/coding-agent/src/core/tools/bash.ts`
- `packages/coding-agent/src/core/tools/file-mutation-queue.ts`
- `packages/coding-agent/src/core/tools/path-utils.ts`
- `packages/coding-agent/src/core/tools/output-accumulator.ts`
- `packages/coding-agent/src/core/tools/truncate.ts`
- `packages/coding-agent/src/core/agent-session.ts`
- `packages/coding-agent/src/core/bash-executor.ts`
- `packages/coding-agent/src/core/messages.ts`
- Tests such as `packages/coding-agent/test/file-mutation-queue.test.ts` and
  `packages/coding-agent/test/suite/agent-session-bash-persistence.test.ts`.

Answers:

- Pi's built-in registry includes `read`, `bash`, `edit`, `write`, `grep`, `find`,
  and `ls`; default active tools are `read`, `bash`, `edit`, and `write`.
- Pi does not expose a separate first-class `patch` tool in the inspected runtime.
  Its `edit` tool returns diff/patch details.
- Pi `edit` accepts a path plus an `edits[]` list of `oldText`/`newText`, supports
  legacy top-level old/new fields, and can parse `edits` passed as a JSON string.
- Pi edit matching is more forgiving than a minimal Codegeist first slice: line
  ending normalization, BOM handling, trailing whitespace tolerance, smart quotes,
  Unicode dashes, and Unicode spaces.
- Pi serializes mutations per canonical file path with `withFileMutationQueue`;
  this is a valuable pattern for Codegeist if parallel tool calls can affect the
  same file.
- Pi path utilities resolve relative paths against cwd, accept absolute paths, and
  normalize some user-friendly forms. The inspected path does not prove a hard
  workspace containment policy, so Codegeist must own that safety boundary.
- Pi `bash` uses `command` plus optional timeout in seconds, captures combined
  output, kills process tree on timeout/abort, tail-truncates to 2,000 lines or
  50 KB, and can persist a full output path.
- Pi persists user bash execution as `BashExecutionMessage` with command, output,
  exit code, cancelled, truncated, optional full-output path, timestamp, and
  context-exclusion marker. This is richer than Codegeist should store now.
- Pi extension `tool_call` and `tool_result` hooks can block or mutate tool behavior.
  Codegeist should defer extension hooks.

Keep:

- exact multi-edit idea with uniqueness/non-overlap checks;
- diff/patch preview as the edit output summary;
- per-file mutation queue if the implementation can have parallel calls;
- process-tree kill on timeout;
- bounded shell summary;
- persisted bounded shell activity.

Defer or drop:

- Pi extension hooks and tool override system;
- accepting absolute/out-of-workspace mutation paths;
- full output log paths until Codegeist owns artifact cleanup;
- rich bash execution message schema;
- fuzzy edit matching beyond deterministic test-backed tolerance.

## Aider

Aider is the strongest reference for model-authored edit formats, parse/dry-run
mutation, command feedback, lint/test reflection, and git-heavy behavior to avoid.

Important sources:

- `aider/coders/base_coder.py`
- `aider/coders/editblock_coder.py`
- `aider/coders/search_replace.py`
- `aider/coders/udiff_coder.py`
- `aider/coders/wholefile_coder.py`
- `aider/coders/patch_coder.py`
- `aider/coders/shell.py`
- `aider/commands.py`
- `aider/run_cmd.py`
- `aider/linter.py`
- `aider/utils.py`
- Tests under `tests/basic/test_coder.py`, `test_editblock.py`, `test_udiff.py`,
  `test_wholefile.py`, `test_commands.py`, `test_run_cmd.py`, `test_linter.py`,
  and `test_utils.py`.

Answers:

- Aider's `Coder` loop parses model replies, gets edits, dry-runs them, checks
  allowed files, applies edits, then can reflect lint/test failures back to the
  model.
- Aider supports multiple edit formats: whole-file rewrite, search/replace blocks,
  fenced diff, unified diff, and structured patch. This is too many for a first
  Codegeist slice; pick one deterministic contract.
- Aider's allowed-to-edit gate is repo/git aware: files in chat, files not in chat,
  ignored files, new files, dirty pre-commit, and optional git add/commit behavior.
  Codegeist should not adopt git as the authorization boundary.
- Aider command execution runs model-suggested shell blocks only after confirmation,
  and `/run` or `!` commands can feed output back to chat. Failing tests can be
  reflected into the conversation.
- Aider command execution does not provide the mandatory timeout behavior Codegeist
  needs for T007_04.
- Aider has some output sensitivity through token counting and linter context, but
  it does not provide enough hard output bounding for Codegeist session persistence.

Keep:

- parse-before-write pipeline;
- dry-run validation;
- clear edit failure feedback;
- focused command/lint/test feedback as later control-loop input;
- compact edit summaries.

Defer or drop:

- multiple model-specific edit formats;
- architect/editor split;
- auto commits, `git add`, dirty commits, and commit-based undo;
- git tracked status as write authorization;
- shell execution without timeout;
- unbounded command/test/lint output injection;
- fenced shell blocks as executable commands outside an explicit tool boundary.

## mini-SWE-agent

mini-SWE-agent is a useful minimal shell/action reference and a warning about the
limits of shell-only mutation.

Important sources:

- `src/minisweagent/agents/default.py`
- `src/minisweagent/__init__.py`
- `src/minisweagent/models/litellm_model.py`
- `src/minisweagent/models/utils/actions_toolcall.py`
- `src/minisweagent/models/utils/actions_text.py`
- `src/minisweagent/environments/local.py`
- `src/minisweagent/environments/docker.py`
- `src/minisweagent/config/mini.yaml`
- `src/minisweagent/config/benchmarks/swebench.yaml`
- Tests under `tests/models/test_actions_toolcall.py`,
  `tests/environments/test_local.py`, `tests/environments/test_docker.py`,
  `tests/agents/test_default.py`, and `tests/run/`.

Answers:

- The default loop is small: model query, parse action, environment execute,
  append observation, save trajectory.
- The preferred action format is a tool call named `bash` with one required
  `command` string. Legacy regex/markdown command parsing exists but should not be
  copied.
- The local environment runs shell commands with `subprocess.Popen(..., shell=True)`,
  combines stderr into stdout, supports timeout, and kills the process group on
  POSIX.
- It returns `output`, `returncode`, and `exception_info`; timeout is represented
  as return code `-1` with exception info, not as an agent-fatal crash.
- Trajectories can persist raw output, which Codegeist must not do for
  `.codegeist/session.json`.
- File changes are performed through shell commands, not first-class edit/patch
  tools. This is too unsafe and too opaque for Codegeist patch/edit requirements.
- Submit/finish behavior uses a magic stdout sentinel. That belongs to a future
  agent control loop, not the generic shell tool.

Keep:

- simple shell input shape;
- fresh process per command;
- non-zero exit as command result;
- timeout as observable shell result;
- bounded observation idea.

Defer or drop:

- regex/markdown action parsing;
- shell-only file mutation;
- raw trajectory output persistence;
- Docker/Podman/Singularity/SWE-bench environment matrix;
- submit sentinel;
- trajectory replay/inspector behavior.

## Spring AI Agent Utils

Spring AI Agent Utils is useful for Java/Spring tool callback patterns, exact edit
tests, and shell process tests, but its side-effecting tools are not safe direct
delegates for Codegeist.

Important sources:

- `org.springaicommunity.agent.tools.FileSystemTools`
- `org.springaicommunity.agent.tools.ShellTools`
- `org.springaicommunity.agent.tools.GlobTool`
- `org.springaicommunity.agent.tools.GrepTool`
- `org.springaicommunity.agent.tools.ListDirectoryTool`
- `org.springaicommunity.agent.utils.AgentEnvironment`
- `SkillsTool`, `TaskTool`, and `TaskOutputTool` for `FunctionToolCallback`
  patterns.
- `ClaudeSubagentType` and `AutoMemoryToolsAdvisor` for
  `MethodToolCallbackProvider` patterns.
- Existing Codegeist wrapper references from local source: `CodegeistToolService`,
  `CodegeistLocalTools`, `CodegeistLocalToolCallback`, and
  `RecordingToolCallback`.

Answers:

- Agent Utils has file `Read`, `Write`, and `Edit` methods through
  `FileSystemTools`. `Edit` is exact literal replacement, not unified patch.
- Agent Utils has `ShellTools` with Bash, background output, and kill support. The
  background shell map is too broad for T007_04.
- Agent Utils shell process code has timeout and output cap ideas, but does not
  enforce Codegeist working-directory containment and should not be reused directly.
- Agent Utils examples use `@Tool`, `MethodToolCallbackProvider`,
  `FunctionToolCallback`, and `ToolCallbackProvider`. T007_04 should keep explicit
  Codegeist wrappers because side effects need policy, output bounds, and session
  recording.
- No meaningful Agent Utils `ToolContext` usage was found for carrying Codegeist
  working directory, recorder, timeout, or policy objects. Keep explicit per-run
  context instead.

Keep:

- Java exact-replacement semantics and tests as inspiration;
- `FunctionToolCallback`-style explicit input records for schema clarity;
- shell timeout/stdout/stderr/exit-code test ideas;
- explicit wrapper composition.

Defer or drop:

- direct `FileSystemTools` or `ShellTools` reuse;
- background `BashOutput` / `KillShell` process map;
- `ToolContext` as hidden carrier for policy/session state;
- unrestricted file path behavior;
- shell-only file mutation.

## Cross-Project Patch/Edit Synthesis

| Topic | OpenCode | Pi | Aider | mini-SWE-agent | Agent Utils | Codegeist T007_04 |
| --- | --- | --- | --- | --- | --- | --- |
| Exact edit | Dedicated `edit` tool | `edit` with `edits[]` | Search/replace edit format | None | `FileSystemTools.Edit` | Implement Codegeist-owned exact edit. |
| Patch | Dedicated `apply_patch` grammar | No separate patch tool; edit returns patch details | Structured patch and unified diff formats | None | None | Implement only if needed after exact edit, or add focused structured patch with parse-first validation. |
| Write boundary | Whole-file create/overwrite | Whole-file create/overwrite | Whole-file format exists | Shell writes only | `Write` creates/overwrites | Keep existing `codegeist_write`; do not merge edit/patch into it. |
| Safety gate | Permission prompts plus external directory checks | No hard workspace evidence | Git/repo admission | Environment boundary only | No containment | Hard Codegeist working-dir guard before side effects. |
| Diff summary | Tool output/metadata | Diff and patch details | Git/diff-heavy summaries | None | Simple strings | Bounded text diff/summary in `outputPreview`. |
| What to avoid | Full permission DB/TUI now | Extension hooks now | Git auto-commit/repo map now | Shell-only mutation | Direct reuse | Keep T007_04 minimal and test-shaped. |

Recommended implementation order:

1. Add reusable working-directory guard for mutating paths and shell cwd.
2. Add exact edit tool with deterministic failures and bounded diff summary.
3. Add shell tool with timeout, separate stdout/stderr capture, and bounded preview.
4. Add patch tool only if the active T007_04 implementation slice explicitly needs
   multi-file patch support; otherwise record it as a follow-up under the same task
   directory.
5. Keep session persistence through the existing local tool callback recorder.

## Cross-Project Shell Synthesis

| Topic | Evidence | Codegeist recommendation |
| --- | --- | --- |
| Input schema | OpenCode uses `command`, `description`, `timeout`, `workdir`; Pi uses `command`, optional seconds timeout; mini uses only `command`; Agent Utils uses command plus optional async/timeout flags. | Start with `command`, optional `cwd`, optional timeout. `description` can be deferred. |
| Shell choice | OpenCode has shell discovery; Pi uses configured shell; mini uses `shell=True`; Agent Utils uses `/bin/bash` or `cmd.exe`. | Use simple platform default now; defer configurable shell. |
| Cwd policy | OpenCode asks external-directory permission; Pi path policy is broader; mini uses env cwd; Agent Utils lacks containment. | Hard reject cwd escape under Codegeist workingDir. |
| Timeout | OpenCode, Pi, mini, Agent Utils all have timeout evidence. Aider does not. | Required in T007_04. |
| stdout/stderr | OpenCode, Pi, and mini merge output; Agent Utils captures separately. T007 asks for bounded stdout/stderr summaries. | Capture separately and render both in one summary. |
| Non-zero exit | OpenCode, mini, and Agent Utils return it as shell output/result. | Completed result with exit code in preview. |
| Raw full output | OpenCode/Pi can save side files; mini trajectories can retain raw output; Aider can feed large output. | Do not persist unbounded raw output. |
| Background/persistent shell | Agent Utils supports background shell; OpenCode/Pi shell calls are effectively one-shot. | Defer background and persistent shell. |

## Workspace Policy

T007_04 should be stricter than the surveyed projects because Codegeist does not
yet have a permission loop or sandbox:

- Resolve active working directory once per chat run through existing Codegeist
  workspace resolution.
- For existing file targets, use canonical or real paths to detect symlink escape.
- For missing mutation targets, validate the parent directory canonical path before
  creating the file.
- Reject absolute paths outside the working directory.
- Reject `..` traversal after normalization if it resolves outside the working
  directory.
- Reject shell cwd outside the working directory before process start.
- Do not scan shell command text for file arguments in T007_04. Document that shell
  command content is not sandboxed beyond cwd and timeout.

## Persistence Mapping

Current `ToolSessionPart` is intentionally small. Use this mapping until a test
forces typed fields.

| Third-party field | Codegeist T007_04 mapping |
| --- | --- |
| Tool name | `ToolSessionPart.tool` |
| Completed/failed | `ToolSessionPart.status` |
| Shell command | Bounded text in `outputPreview` |
| Cwd/workdir | Bounded text in `outputPreview` |
| Exit code | Bounded text in `outputPreview` |
| Timed out/cancelled | Bounded text in `outputPreview` |
| stdout/stderr | Bounded text in `outputPreview`; no raw full output |
| Diff/patch hunks | Bounded text in `outputPreview`; no unbounded diff |
| Full-output path/attachment | Defer |
| Input arguments map | Defer unless needed for TUI/audit |
| Timing/start/end | Defer |
| Tool call id | Defer |
| Process id/environment | Drop |
| Provider config/model/tool registry | Drop from session store |

## Test Recommendations

Create narrow Java tests before or alongside implementation. Suggested classes:

- `CodegeistWorkspaceGuardTest`
  - allows paths under working directory;
  - rejects traversal;
  - rejects absolute outside paths;
  - rejects symlink escapes for existing paths;
  - handles missing target parent validation.
- `CodegeistEditToolTest`
  - successful exact replacement;
  - repeated `oldText` fails unless a `replaceAll` option is implemented;
  - missing `oldText` fails;
  - missing file and directory target failures;
  - outside-workingDir mutation fails before writing;
  - summary/diff is bounded.
- `CodegeistPatchToolTest` if patch is implemented in this slice
  - valid minimal add/update/delete patch;
  - invalid patch fails without mutation;
  - path escape in patch header fails before mutation;
  - bounded per-file summary.
- `CodegeistShellToolTest`
  - shell success under temp working directory;
  - non-zero exit recorded as completed output;
  - stdout and stderr previews both present and bounded;
  - timeout records `Timed out: true` and kills process;
  - cwd escape fails before process start;
  - blank command fails as failed tool part.
- `CodegeistLocalToolCallbackTest` or existing callback tests
  - completed result is recorded;
  - `CodegeistToolException` records failed status;
  - output bounds apply before model/session exposure.
- `ChatHarnessService` or command-boundary test
  - side-effecting tool parts persist before assistant text;
  - plain no-continue `ask` stdout behavior remains unchanged.

Candidate verification from `app/codegeist/cli`:

```bash
task test TEST=<focused-t007-04-test-selector>
task test
```

## Deferred Work

Do not implement these as part of the first T007_04 pass unless the task is
explicitly rescoped:

- permission prompt UI or persistent allow/deny rules;
- patch review TUI;
- tree-sitter shell command scanning;
- external-directory approval instead of hard rejection;
- plugin hooks or extension tool overrides;
- configured shell discovery;
- background processes or persistent shell sessions;
- Docker/Podman/SWE-bench environments;
- git auto-add/auto-commit/dirty-state management;
- model-specific edit format selection;
- raw full-output side files;
- typed shell/diff metadata in `ToolSessionPart`.

## Open Implementation Questions

Resolve these before writing Java source:

- Should the shell input field be named `cwd` to match Codegeist task wording, or
  `workdir` to align with OpenCode?
- Should timeout input be `timeoutMillis` for fast tests and OpenCode parity, or a
  simpler duration/seconds field?
- Should the first Java slice implement both exact edit and structured patch, or
  exact edit first with patch as a follow-up under this task directory?
- Should timeout be `completed` with `Timed out: true` or `failed` in
  `ToolSessionPart`? Current evidence prefers completed result text, but the
  existing Codegeist status model may make failed easier for command-boundary
  assertions.
- Should an optional description/title be accepted now for future TUI display, or
  deferred until `ToolSessionPart` grows typed metadata?

## Implementation Readiness Checklist

- [ ] Decide shell `cwd` vs `workdir` name.
- [ ] Decide timeout field name and units.
- [ ] Decide exact edit only versus exact edit plus structured patch in the first
  implementation pass.
- [ ] Add or reuse a working-directory guard for mutating file paths and shell cwd.
- [ ] Write focused tests for path/cwd escape before side effects.
- [ ] Write focused tests for bounded edit/patch and shell output summaries.
- [ ] Keep `ToolSessionPart` schema unchanged unless a focused test proves a field
  is needed now.
- [ ] Update `docs/developer/architecture/architecture.md` and any focused
  architecture doc after implementation changes Java packages/classes/tests.
