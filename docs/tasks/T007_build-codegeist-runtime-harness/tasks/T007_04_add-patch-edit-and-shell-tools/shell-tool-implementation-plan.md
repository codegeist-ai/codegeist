# T007_04_04 Shell Tool Implementation Plan

Implementation handoff for
`tasks/T007_04_04_add-shell-tool.md`. This document describes the planned
`codegeist_shell` local tool before runtime code is changed.

Status note: this plan is historical. The implemented `codegeist_shell` was later
simplified to plain `ProcessBuilder` execution with merged stdout/stderr, optional
`timeoutSeconds`, no workspace cwd containment, and bounded completed shell summaries.
Use
`tasks/T007_04_04_add-shell-tool.md` and
`docs/developer/architecture/local-file-tools.md` for the current contract.

## Purpose

Add `codegeist_shell` as a bounded Codegeist-owned local shell tool. The tool
runs exactly one local process per tool call, resolves its working directory
under the active workspace, captures stdout and stderr separately without
deadlocks, records timeout and exit-code information, and persists the bounded
result through the existing `ToolSessionPart(tool,status,outputPreview)` shape.

## Current Baseline

Codegeist already has the local tool runtime under
`app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`.

The current local tools are registered by implementing `CodegeistLocalTool`.
`CodegeistLocalTools` injects all `CodegeistLocalTool` beans as a list and wraps
them in `CodegeistLocalToolCallback`.

Current implemented local callbacks:

| Tool | Class | Behavior |
| --- | --- | --- |
| `codegeist_read` | `CodegeistReadFileTool` | Read bounded text from one file. |
| `codegeist_list` | `CodegeistListFileTool` | List direct directory entries. |
| `codegeist_glob` | `CodegeistGlobFileTool` | Match files or directories with Java NIO glob semantics. |
| `codegeist_grep` | `CodegeistGrepFileTool` | Search text files with Java regex. |
| `codegeist_write` | `CodegeistWriteFileTool` | Create or overwrite one regular text file. |
| `codegeist_edit` | `CodegeistEditFileTool` | Apply exact replacements to one existing text file. |

Relevant existing infrastructure:

| Class | Reuse |
| --- | --- |
| `CodegeistLocalTool` | Implement this interface for the new shell tool. |
| `CodegeistLocalTools` | No production change should be needed beyond Spring discovering the new component. |
| `CodegeistLocalToolCallback` | Converts successful tool results to completed `ToolSessionPart`; converts `CodegeistToolException` to failed parts. |
| `CodegeistFileToolSupport` | Reuse for JSON parsing, schema creation, active workspace resolution, display paths, line separator, and output-bounds access. |
| `ToolOutputBounds` | Reuse global preview and line caps. |
| `WorkspaceResolver` | Supplies active workspace through `CodegeistFileToolSupport.currentWorkspace()`. |
| `ToolSessionPart` | Keep unchanged. Shell details stay inside bounded `outputPreview`. |

## Original Scope

Implement `codegeist_shell` with this behavior:

| Requirement | Planned Behavior |
| --- | --- |
| One process per tool call | Start one `ProcessBuilder` process for each invocation. |
| Command input | Accept a required `command` string. |
| Working directory input | Accept optional `cwd`; default to `.`. |
| Timeout input | Accept optional `timeoutSeconds`. |
| Workspace cwd guard | Resolve `cwd` against active workspace and reject cwd escapes before process startup. |
| No stdin | Close process stdin immediately after startup. |
| No persistent shell | Do not keep processes, shells, PTYs, or sessions between calls. |
| Separate output | Capture stdout and stderr independently. |
| Bounded output | Bound each stream preview and final summary before returning or persisting. |
| Non-zero exit | Record as completed result with exit code in preview. |
| Timeout | Terminate process, record `Timed out: true`, and keep completed status. |
| Startup and validation failures | Throw `CodegeistToolException`; callback records failed part. |

## Non-Goals

Do not implement these deferred features in this child task:

| Deferred Feature | Reason |
| --- | --- |
| Permission prompts | Codegeist has no permission loop yet. |
| Automatic shell discovery | OpenCode has a broad shell-selection system; this task keeps explicit user-provided wrapper config and platform defaults only. |
| Tree-sitter command scanning | Too broad for `T007_04_04`. |
| Plugin environment hooks | No plugin runtime exists yet. |
| Persistent shells | Explicitly out of scope. |
| PTY behavior | Explicitly out of scope. |
| Background process registry | Explicitly out of scope. |
| Full-output side files | No artifact lifecycle exists yet. |
| Typed shell fields on `ToolSessionPart` | Current schema should remain unchanged unless focused tests require expansion. |
| Sandbox claims | Only cwd containment and bounded output are implemented. |

## Original Public Tool Contract

Tool name:

```text
codegeist_shell
```

Recommended input fields:

| Field | Required | Type | Behavior |
| --- | --- | --- | --- |
| `command` | yes | string | Shell command to execute. Must contain non-blank text. |
| `cwd` | no | string | Working directory relative to active workspace, or absolute path still contained by active workspace. Defaults to `.`. |
| `timeoutSeconds` | no | integer | Positive timeout in seconds. Invalid or omitted values use the configured default. |

Recommended timeout constants:

| Constant | Value | Reason |
| --- | --- | --- |
| `DEFAULT_TIMEOUT_SECONDS` | `120` | Bounded by default while keeping normal commands usable. |
| `TERMINATION_GRACE_MILLIS` | `500` | Gives normal termination a short chance before force kill. |

Use `timeoutSeconds` for the current Codegeist contract because the user-facing
`codegeist.yml` timeout should be readable in seconds.

Direct `codegeist.yml` wrapper configuration:

```yaml
tools:
  codegeist-shell:
    command-prefix:
      - docker
      - run
      - --rm
      - ubuntu
      - bash
      - -lc
```

Runtime rules:

| Setting | Behavior |
| --- | --- |
| `tools.codegeist-shell.command-prefix` | Optional host argv prefix. Missing, empty, or blank first entry uses platform defaults. Values are not shell-split. |
| Final argv | `command-prefix + [command]`. |
| Defaults | Windows uses `cmd.exe`, `/c`; other platforms use `sh`, `-lc`. |
| Sandbox claim | None. Docker or other wrappers are caller-owned host-side argv prefixes; their isolation depends entirely on configured prefix values. |

## JSON Schema

The tool definition should expose an explicit schema through
`support.schema(...)`:

```json
{
  "type": "object",
  "properties": {
    "command": {
      "type": "string",
      "description": "Shell command to run once"
    },
    "cwd": {
      "type": "string",
      "description": "Working directory under the active workspace; defaults to ."
    },
    "timeoutSeconds": {
      "type": "integer",
      "description": "Timeout in seconds; falls back to Codegeist config"
    }
  },
  "required": ["command"],
  "additionalProperties": false
}
```

The schema is model guidance. `CodegeistToolJsonMapper` currently ignores unknown
JSON fields, so implementation tests should assert the schema contract but not
rely on Jackson rejecting unknown properties.

## Output Preview Contract

Use stable headings so tests and future TUI rendering can assert
contract-bearing text without parsing platform-specific command prose.

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

Output rules:

| Item | Rule |
| --- | --- |
| `Command` | Use `ToolOutputBounds.linePreview(command)` so long commands cannot dominate the summary header. |
| `Cwd` | Use `support.displayPath(workspace, resolvedCwd)`. |
| `Exit code` | Use process exit code when available; use `n/a` on timeout before a stable code is available. |
| `Timed out` | `true` only when timeout fired. |
| `Stdout truncated` | `true` when stdout exceeded retained per-stream cap. |
| `Stderr truncated` | `true` when stderr exceeded retained per-stream cap. |
| `Stdout` | Retained bounded stdout text, or empty string. |
| `Stderr` | Retained bounded stderr text, or empty string. |

The final summary must still pass through `support.outputBounds().preview(...)`.

## Result Status Policy

| Scenario | ToolSessionPart Status | Preview |
| --- | --- | --- |
| Exit code `0` | `completed` | Include stdout/stderr and `Exit code: 0`. |
| Non-zero exit | `completed` | Include stdout/stderr and non-zero `Exit code`. |
| Timeout | `completed` | Include `Timed out: true`; include any captured stdout/stderr. |
| Missing command | `failed` | `Required text field is missing: command`. |
| Invalid cwd JSON shape | `failed` | Existing JSON parsing failure path. |
| Cwd escape | `failed` | `Path escapes workspace: <display-path>`. |
| Missing cwd | `failed` | `Cwd does not exist: <display-path>`. |
| Cwd is not directory | `failed` | `Cwd is not a directory: <display-path>`. |
| Startup failure | `failed` | `Failed to start shell command: <message>`. |
| Interrupted execution | `failed` | Re-interrupt thread and return handled failure. |

## New Production File

Add:

`app/codegeist/cli/src/main/java/ai/codegeist/app/tool/CodegeistShellTool.java`

Planned shape:

```java
@Component
@RequiredArgsConstructor
final class CodegeistShellTool implements CodegeistLocalTool {

    static final String TOOL_NAME = "codegeist_shell";

    private final CodegeistFileToolSupport support;

    private final CodegeistShellToolSettings settings;

    @Override
    public ToolDefinition definition() {
        // Build explicit schema for command, cwd, and timeoutSeconds.
    }

    @Override
    public CodegeistToolResult execute(CodegeistToolInput toolInput) {
        // Parse input, validate cwd, run process, return bounded summary.
    }

    private Path resolveContainedCwd(Path workspace, String cwdText) {
        // Reject normalized and real-path cwd escapes before process startup.
    }

    private List<String> shellCommand(String command) {
        // Use configured wrapper prefix or default cmd.exe/sh prefix, then append command.
    }

    private ShellExecutionResult runProcess(...) {
        // Start one process, close stdin, capture both streams, wait with timeout.
    }

    private String summary(...) {
        // Render stable bounded output-preview headings.
    }

    private record ShellToolInput(String command, String cwd, Long timeoutSeconds) {
    }

    private record ShellExecutionResult(...) {
    }

    private static final class BoundedStreamCollector {
        // Retain a bounded preview and track truncation.
    }
}
```

## Cwd Resolution Algorithm

Use the active workspace from `support.currentWorkspace()`.

```text
workspace = support.currentWorkspace().toAbsolutePath().normalize()
cwdText = blank input cwd ? "." : input cwd
candidate = support.resolvePath(workspace, cwdText).toAbsolutePath().normalize()

if candidate does not start with workspace:
    fail "Path escapes workspace: <display>"

if candidate does not exist:
    fail "Cwd does not exist: <display>"

realWorkspace = workspace.toRealPath()
realCandidate = candidate.toRealPath()

if realCandidate does not start with realWorkspace:
    fail "Path escapes workspace: <display>"

if realCandidate is not directory:
    fail "Cwd is not a directory: <display>"

return realCandidate
```

Important details:

| Detail | Decision |
| --- | --- |
| Relative cwd | Resolve under active workspace. |
| Absolute cwd | Accept only when still contained by active workspace. |
| Traversal | Normalize first; reject if normalized path escapes. |
| Symlink escape | Use `toRealPath()` check to reject. |
| Missing cwd | Fail before process startup. |
| File cwd | Fail before process startup. |
| `workspace.dir-guard-disabled` | Do not apply to shell cwd; it currently documents file mutation only. |

## Process Execution Algorithm

Build the process with the configured or default command prefix:

| Platform | Command |
| --- | --- |
| Configured | `tools.codegeist-shell.command-prefix + [command]` |
| Windows default | `cmd.exe /c <command>` |
| Other default | `sh -lc <command>` |

Execution steps:

```text
builder = new ProcessBuilder(shellCommand(command))
builder.directory(cwd.toFile())

process = builder.start()
process.getOutputStream().close()

start stdout collector in one task/thread
start stderr collector in one task/thread

finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

if finished:
    exitCode = process.exitValue()
else:
    timedOut = true
    terminate process and descendants
    exitCode = n/a unless process exits with a stable code

wait for collectors to finish
return ShellExecutionResult
```

## Concurrent Output Capture

stdout and stderr must be consumed concurrently to prevent process deadlocks.

Recommended implementation:

| Option | Decision |
| --- | --- |
| `ExecutorService` with two tasks | Good fit; simple and testable. |
| Virtual threads | Avoid unless the project already uses them. |
| Merged stderr/stdout | Do not use; task requires separate previews. |
| `readAllBytes()` after wait | Avoid; can deadlock if process fills a pipe. |

Use a bounded collector that reads chunks from each stream and stores only the
retained prefix.

Recommended collector behavior:

| Behavior | Rule |
| --- | --- |
| Charset | Use `StandardCharsets.UTF_8` for command output preview decoding. |
| Retained cap | Use a deterministic per-stream cap, for example `ToolOutputBounds.MAX_PREVIEW_CHARS / 2`. |
| Truncation flag | Set when more decoded text arrives after retained cap. |
| Final cap | Summary still goes through `ToolOutputBounds.preview(...)`. |

Malformed bytes can be decoded with the JDK replacement behavior from
`new String(bytes, UTF_8)` or an `InputStreamReader`.

## Process Termination On Timeout

Timeout behavior must terminate the process and record the timeout.

Recommended sequence:

```text
process.toHandle().descendants().forEach(ProcessHandle::destroy)
process.destroy()

if process does not exit within grace period:
    process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly)
    process.destroyForcibly()

wait briefly again
```

Do not claim this is a complete sandbox or process-tree guarantee. It is
best-effort local termination for the current child process and visible
descendants.

## Error Handling

Handled failures should throw `CodegeistToolException`.

Catch and translate:

| Exception | Handling |
| --- | --- |
| `JsonProcessingException` | Already handled by `support.parseInput(...)`. |
| `IOException` during cwd real path | `Failed to resolve cwd: <display>`. |
| `IOException` during process startup | `Failed to start shell command: <message>`. |
| `InterruptedException` | Re-interrupt current thread and throw `CodegeistToolException("Shell command interrupted", exception)`. |
| Collector `ExecutionException` | Convert to handled failure with a bounded message. |

Do not catch broad programming errors unless they are expected local tool
failures. `CodegeistLocalToolCallback` deliberately lets unexpected runtime
defects escape.

## Test Plan

Update:

`app/codegeist/cli/src/test/java/ai/codegeist/app/tool/CodegeistLocalToolsTest.java`

### Test Helper Changes

Update the local test wiring helper to instantiate the new tool:

```java
new CodegeistShellTool(support)
```

Update callback exposure:

```java
.containsExactlyInAnyOrder(
    CodegeistReadFileTool.TOOL_NAME,
    CodegeistListFileTool.TOOL_NAME,
    CodegeistGlobFileTool.TOOL_NAME,
    CodegeistGrepFileTool.TOOL_NAME,
    CodegeistWriteFileTool.TOOL_NAME,
    CodegeistEditFileTool.TOOL_NAME,
    CodegeistShellTool.TOOL_NAME)
```

Add a platform-aware helper:

```java
private String shellCommand(String unixCommand, String windowsCommand) {
    return isWindows() ? windowsCommand : unixCommand;
}

private boolean isWindows() {
    return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
}
```

### Focused Tests

Add these tests:

| Test | Proves |
| --- | --- |
| `shellSchemaExposesCommandCwdAndTimeoutSeconds` | Schema exposes `command`, `cwd`, `timeoutSeconds`; does not expose description, workdir, timeoutMillis, background. |
| `shellSettingsUseConfiguredCommandPrefix` | `tools.codegeist-shell.command-prefix` becomes the command prefix. |
| `shellSettingsUsePlatformDefaultWhenCommandPrefixIsBlank` | Empty or blank-first command prefix falls back to `cmd.exe /c` or `sh -lc`. |
| `shellRunsSuccessfulCommandAndRecordsCompletedToolPart` | Exit `0`, stdout preview, stderr preview, completed part. |
| `shellRecordsNonZeroExitAsCompletedResult` | Non-zero exit is not a wrapper failure. |
| `shellRecordsTimeoutAsCompletedResult` | Timeout terminates process and records `Timed out: true`. |
| `shellRejectsCwdEscapeBeforeProcessStartup` | Outside cwd fails before command can create a marker file. |
| `shellOutputIsBoundedAndPersistedAsTheSamePreview` | Large output stays within global preview cap and persisted output equals returned output. |

### Suggested Test Commands

Success command:

| Platform | Command |
| --- | --- |
| Unix | `printf 'out'; printf 'err' >&2` |
| Windows | `echo out& echo err 1>&2` |

Non-zero command:

| Platform | Command |
| --- | --- |
| Unix | `printf 'bad' >&2; exit 7` |
| Windows | `echo bad 1>&2 & exit /b 7` |

Timeout command:

| Platform | Command |
| --- | --- |
| Unix | `sleep 2` |
| Windows | `ping -n 3 127.0.0.1 > nul` |

Cwd escape command:

| Platform | Command |
| --- | --- |
| Unix | `printf marker > marker.txt` |
| Windows | `echo marker> marker.txt` |

The cwd escape test should call the tool with `cwd` pointing outside the
workspace and assert that `outside/marker.txt` does not exist afterward.

## Chat Persistence Coverage

No new chat integration test is required if `CodegeistLocalToolsTest` proves the
new local callback records a completed `ToolSessionPart`.

Existing coverage already proves the persistence path:

| Test | Existing Coverage |
| --- | --- |
| `ChatHarnessServiceTest` | Saves recorded local tool parts before assistant text. |
| `AskCommandsSessionStoreTest` | Plain no-continue ask delegates correctly and keeps stdout behavior. |
| `SessionStoreServiceTest` | Persists and reloads `ToolSessionPart`. |
| `CodegeistToolServiceTest` | Exposes local callbacks through `CodegeistChatExecutionContext`. |

Add chat test coverage only if implementation changes the tool-run or
session-store boundary.

## Documentation Updates

Update `docs/developer/architecture/local-file-tools.md`:

| Section | Update |
| --- | --- |
| Scope | Include shell execution as implemented, bounded one-shot behavior. |
| Current Status | Add `codegeist_shell`. |
| Implemented callback table | Add `codegeist_shell` and `CodegeistShellTool`. |
| Source Map | Add `CodegeistShellTool.java`. |
| Component Model diagram | Add class and `CodegeistLocalTool <|.. CodegeistShellTool`. |
| Execution flow | Mention shell uses same callback and recording wrapper as file tools. |
| Workspace And Path Semantics | Add shell cwd containment row. |
| Tool Contracts | Add `### codegeist_shell`. |
| Output Bounds | Add stdout/stderr preview behavior and timeout summary. |
| Error Behavior | Add cwd escape, missing cwd, startup failure examples. |
| Test Coverage | Add shell success, non-zero, timeout, cwd escape, bounds. |
| Extension Guide | Keep guidance generic but mention shell tools should not expose broad third-party surfaces directly. |
| Sharp Edges | Add no sandbox, no stdin, no background registry, no PTY, best-effort process termination. |

Update `docs/developer/architecture/architecture.md`:

| Section | Update |
| --- | --- |
| Current System State | Include shell callback in current implemented tools. |
| Build Baseline Tests row | Include shell-tool tests. |
| Package responsibility table | Include shell execution in `ai.codegeist.app.tool`. |
| Runtime Components | Update local tool list from six file callbacks to six file callbacks plus shell. |
| Session Store paragraph | Replace shell deferred wording with implemented bounded shell part behavior. |
| Test Architecture | Update `CodegeistLocalToolsTest` description with shell coverage. |
| Not Implemented Yet | Remove `Controlled shell execution`, or narrow it to broader future shell controls beyond the first bounded one-shot shell tool. |

Update task docs:

| File | Update |
| --- | --- |
| `T007_04_04_add-shell-tool.md` | Mark solved after verification; add implementation result and test evidence. |
| `T007_04_add-patch-edit-and-shell-tools/task.md` | Update child task summary for `T007_04_04`. |
| `docs/memory-bank/chat.md` | Refresh current tool-runtime state after implementation succeeds. |

## Verification Plan

Run focused verification from `app/codegeist/cli`:

```bash
task test TEST=CodegeistLocalToolsTest,ChatHarnessServiceTest,AskCommandsSessionStoreTest
```

Run broad JVM verification:

```bash
task test
```

Run repo diff hygiene:

```bash
git --no-pager diff --check
```

If native shell-tool behavior must be proven later, add that to a separate smoke
task. This child task only requires focused JVM tests unless implementation
changes packaging or existing smoke scripts.

## Acceptance Criteria Mapping

| Acceptance Criterion | Planned Evidence |
| --- | --- |
| Successful command records bounded stdout/stderr and completed `ToolSessionPart` | `shellRunsSuccessfulCommandAndRecordsCompletedToolPart`; output contains stdout/stderr headings; status completed. |
| Non-zero exit is completed with exit code | `shellRecordsNonZeroExitAsCompletedResult`; status completed; preview contains `Exit code: 7`. |
| Timeout behavior is bounded and recorded | `shellRecordsTimeoutAsCompletedResult`; status completed; preview contains `Timed out: true`. |
| Cwd escape fails before startup | `shellRejectsCwdEscapeBeforeProcessStartup`; output contains escape error; marker file absent. |
| Existing plain no-continue ask unaffected | `AskCommandsSessionStoreTest`; no command-path changes expected. |

## Implementation Order

1. Add failing tests in `CodegeistLocalToolsTest`.
2. Add `CodegeistShellTool`.
3. Wire the new tool into the test helper.
4. Run focused tests.
5. Fix behavior until focused tests pass.
6. Update architecture docs.
7. Update task docs and memory.
8. Run final focused verification.
9. Run broad `task test`.
10. Run `git --no-pager diff --check`.

## Risks And Mitigations

| Risk | Mitigation |
| --- | --- |
| Timeout test flakes | Use `timeoutSeconds` and a command that reliably outlives timeout. Keep assertions on preview/status, not exact elapsed time. |
| Windows command syntax differs | Use platform-aware test helper. Keep production shell choice simple. |
| Process descendants survive timeout | Use `ProcessHandle.descendants()` best-effort destroy and document no sandbox guarantee. |
| Output capture deadlocks | Consume stdout and stderr concurrently. |
| Preview truncation hides required headings | Build per-stream caps smaller than final cap, then final-cap the whole summary. |
| Native image reflection for input record | `CodegeistShellTool$ShellToolInput` is registered in `reflect-config.json` with the shell implementation so native-reachable Jackson parsing has matching metadata. |
| `workspace.dir-guard-disabled` ambiguity | Do not apply it to shell cwd; document that it remains file-mutation-specific. |

## Resolved Decision

Use `timeoutSeconds` as the shell timeout field. The source-backed research in
`shell-tool-research.md` compared millisecond timeouts from OpenCode and Spring AI
Agent Utils with Pi's seconds-based shape; Codegeist now uses seconds for the
human-facing YAML and tool-call contract.
