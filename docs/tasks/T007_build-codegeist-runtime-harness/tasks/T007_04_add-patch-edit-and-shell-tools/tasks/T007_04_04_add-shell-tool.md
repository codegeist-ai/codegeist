# T007_04_04 Add Shell Tool

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: solved

## Goal

Add a minimal Codegeist-owned shell tool for one local process execution per tool
call.

## Scope

- Add `codegeist_shell` with a command string, one consistent cwd field, and optional
  `timeoutSeconds`.
- Resolve relative cwd values against the active workspace while accepting absolute
  cwd values as caller-provided filesystem paths.
- Run one process per tool call with no stdin, no persistent shell, no background
  process registry, and no PTY behavior.
- Merge stdout and stderr and return one stable, bounded process-output summary.
- Treat non-zero exit as a completed shell result with exit code in the preview.
- Treat timeout as a completed shell result with `Timed out: true`.

## Acceptance Criteria

- A focused test proves a successful command records merged shell output and a
  completed `ToolSessionPart`.
- A focused test proves a non-zero exit is recorded as a completed shell result with
  the exit code.
- A focused test proves timeout terminates the command and records a completed result.
- A focused test proves absolute cwd outside the workspace is allowed.
- A focused test proves completed shell output is stored with the normal preview cap.
- Existing plain no-continue `ask` behavior remains unaffected.

## File Targets

- `app/codegeist/cli/src/main/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/tool/`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/chat/` if chat persistence needs
  an integration assertion.
- `docs/developer/architecture/local-file-tools.md`

## Implementation Result

- Added `CodegeistShellTool` as the package-private Spring component behind
  `codegeist_shell`.
- The tool accepts required `command`, optional `cwd`, and optional `timeoutSeconds`.
- It resolves relative `cwd` against the active workspace and accepts absolute cwd
  values outside the workspace. It performs no workspace containment, symlink escape,
  missing-cwd, or file-cwd pre-check beyond `ProcessBuilder` startup behavior.
- It runs one process per tool call through optional direct
  `tools.codegeist-shell.command-prefix`, falling back to `cmd.exe /c` on Windows or
  `sh -lc` on other platforms. It closes stdin immediately and has no persistent
  shell, PTY, or background process registry.
- It runs process work in a `Future`, waits up to `timeoutSeconds` with a configurable
  `tools.codegeist-shell.default-timeout-seconds` fallback, and destroys the direct
  child process on timeout before recording a completed result with `Timed out: true`
  and exit code `-1`.
- The configured command prefix is an explicit host-side argv list. It can point at a
  path or a command such as `docker`, but Docker mounts, container working directory,
  users, network flags, and sandbox semantics remain the caller's configuration
  responsibility.
- It merges stderr into stdout, builds stable headings for command, cwd, exit code,
  and output, and lets the local callback boundary apply the normal preview cap
  before returning or recording completed output.
- Non-zero exits and timeouts after successful startup are completed shell results;
  invalid input, startup failures, and interruptions remain handled failed tool calls.
- Added native reflection metadata for `CodegeistShellTool$ShellToolInput` so the
  Jackson-bound input record is available to native-reachable tool callbacks.
- Added `CodegeistShellToolConfig` and `CodegeistShellToolSettings`, updated
  `ToolsConfig`, and registered native reflection metadata for the new config POJO.
- Updated current-state architecture docs to include the shell tool, configurable
  wrapper, and sharp edges.

## Verification

Run from `app/codegeist/cli`:

```bash
task test TEST=CodegeistToolsConfigTest,CodegeistLocalToolsTest
task test
```

Results:

- Focused regression passed, 48 tests, 0 failures, 0 errors, 0 skipped.
- Full JVM suite passed, 167 tests, 0 failures, 0 errors, 6 skipped.

## Source Notes

- Reuses `CodegeistWorkingDirectoryGuard`, implemented with the exact-edit slice.
- Use `../shell-tool-question-catalog.md`, `../shell-tool-research.md`,
  `../opencode-shell-tool-comparison.md`, `../ask-project-research.md`, and
  `../shell-tool-implementation-plan.md` before implementation.
- Do not add permission prompts, tree-sitter command scanning, plugin environment
  hooks, automatic shell discovery, sandbox guarantees, process-tree cleanup, or
  full-output side files in this child task.
