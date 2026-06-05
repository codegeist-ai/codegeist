# T007_04 Add Tool Registry And Read-Only Workspace Tools

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add the first Codegeist-owned tool registry and read-only workspace tools.

This child should prove the tool boundary with low-risk capabilities before any
write, patch, shell, network, plugin, MCP, or subagent tools exist. The first useful
tool set should cover repository inspection: list directories, read files, match
glob patterns, and search text.

## Dependencies

- Prefer starting after `T007_02` so tool lifecycle can emit runtime events.
- If `T007_02` is not ready, this task may still implement a narrow tool registry
  and workspace-read service if a focused test proves the boundary without runtime
  events.

## OpenCode Evidence To Translate

- `docs/third-party/opencode/source/packages/opencode/src/tool/tool.ts` for the
  generic tool contract and execution context.
- `docs/third-party/opencode/source/packages/opencode/src/tool/registry.ts` for
  built-in and plugin tool registration behavior.
- `docs/third-party/opencode/source/packages/opencode/src/permission/index.ts` and
  `permission/evaluate.ts` for why tools must not execute side effects directly.
- OpenCode file, grep, glob, patch, shell, and plugin tool implementations should be
  inspected during this task only where they affect read-only registry shape.

## Spring AI Agent Utils Candidates

The current adoption posture allows early internal use of these Agent Utils classes
after Codegeist validates workspace and output policy:

- `org.springaicommunity.agent.tools.GrepTool`
- `org.springaicommunity.agent.tools.GlobTool`
- `org.springaicommunity.agent.tools.ListDirectoryTool`
- `org.springaicommunity.agent.tools.FileSystemTools` read/list behavior only

Useful source and tests:

- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GrepTool.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GlobTool.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/ListDirectoryTool.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/FileSystemTools.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolTest.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GlobToolTest.java`
- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/FileSystemToolsTest.java`

## Scope

- Add a Codegeist-owned tool descriptor shape only with fields needed by the first
  tests: id/name, description, capability category, read-only classification, input
  summary, and result/failure shape.
- Add a tool registry or lookup service only if more than one tool or a provider
  callback needs stable lookup.
- Add minimal workspace root/path validation required by read-only tests: stay
  inside workspace, reject path traversal, reject symlink escape where practical,
  bound output, and return workspace-relative paths.
- Implement read-only tools for list, read, glob, and grep in the smallest order
  supported by tests.
- Normalize Agent Utils string output into Codegeist result/failure values before it
  crosses into runtime events, sessions, provider callbacks, or client rendering.

## Acceptance Criteria

- A focused test proves the registry exposes only the implemented read-only tools.
- Workspace validation rejects outside-root paths and unsafe traversal before a tool
  implementation runs.
- Read/list/glob/grep results are bounded and expressed as Codegeist-owned results,
  not raw Agent Utils public contracts.
- Tool failures are typed or otherwise structured enough for runtime events and TUI
  rendering.
- No write, patch, shell, network, plugin, MCP, or subagent execution is added in
  this child.
- Architecture docs are updated with the implemented packages, services, and tests.

## Non-Goals

- Do not register raw Agent Utils tools with Spring AI providers.
- Do not implement permissions for side effects here beyond the minimal read-only
  workspace gate required by tests.
- Do not add broad JSON schema descriptors for every future tool.
- Do not implement ignored-file or secret scanning beyond a focused current test.
- Do not implement patch/edit, shell, web fetch, MCP, plugin, LSP, or task tools.

## Suggested Tests

- Temporary-directory fixtures for workspace validation.
- Read file: success, missing path, directory-as-file, outside-root path, bounded
  line or byte output.
- List directory: success, limit, outside-root path.
- Glob: relative path output, max results, no matches.
- Grep: match/no-match, include pattern, case behavior, bounded result count.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<workspace-tool-test-selector>
task test
```
