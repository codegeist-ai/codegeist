# Spring AI Agent Utils Analysis Report

Preliminary third-party analysis for Codegeist adoption planning.

Last local refresh: 2026-05-21 against source commit
`c07ea0c80050fc94f750e282270986740f171451`.

## Executive Summary

Spring AI Agent Utils is a Java 17+ Spring AI utility library that reimplements
Claude Code-inspired tools, skills, memory helpers, and subagent orchestration for
Spring AI applications.

For Codegeist, this repository is highly relevant as a behavior and API reference,
but it should not become a direct runtime dependency by default. The checked-out
source currently targets `0.8.0-SNAPSHOT`, Spring AI `2.0.0-M3`, and Spring
Framework `7.0.1`, while Codegeist currently targets Spring Boot `3.5.14`, Spring
AI `1.1.6`, Spring Shell `3.4.2`, Java `25`, and a GraalVM-aware CLI posture.
The upstream README advertises released Maven usage with `0.7.0` and says Spring
AI `2.0.0-M4` or later is needed. That means Codegeist must treat direct
dependency adoption as a compatibility risk until a later T003 solve pass verifies
released artifacts and dependency alignment.

## Modules Observed

- `spring-ai-agent-utils-common` - shared subagent SPI such as
  `SubagentDefinition`, `SubagentResolver`, `SubagentExecutor`, and related value
  records.
- `spring-ai-agent-utils` - core tools, skills, memory helpers, advisors, Claude
  subagent support, utilities, and tests.
- `spring-ai-agent-utils-a2a` - A2A protocol subagent implementation.
- `spring-ai-agent-utils-bom` - BOM for the Spring AI Agent Utils modules.
- `examples/` - demo applications for code agents, skills, subagents, A2A,
  questions, todos, and memory.

## Graphify Findings

The local `/analyse-project` run generated an AST-focused Graphify cache under
`docs/third-party/spring-ai-agent-utils/graphify-out/`.

- Corpus: 156 supported files, roughly 265,270 words.
- Graph: 1,030 nodes, 1,604 edges, 47 communities.
- High-value communities include Auto Memory Tools, Smart Web Fetch, Shell Tools,
  Subagent Execution, Skills Tooling, File System Tools, Todo Tool, Memory Advisor,
  Glob Tool, Brave Web Search, Task Repository, Subagent Resolution, Grep tests,
  Question tools, and List Directory Tool.
- God nodes in the graph include `AutoMemoryTools`, `SmartWebFetchTool`,
  `GrepTool`, `ClaudeSubagentDefinition`, `BackgroundTask`, and
  `BraveWebSearchTool`.

The graph is useful for navigation and follow-up questions, but it is not a full
semantic audit. The T003 adoption report should still inspect source files and
tests directly before making adoption decisions.

## Repomix Packed Source

The local refresh regenerated
`docs/third-party/spring-ai-agent-utils/repomix-output.xml` from the source
checkout for `/ask-project` deep dives.

- Packed files: 222.
- Output size: 1,922,089 bytes.
- Security scan: Repomix reported no suspicious files.
- Excluded by command: `.git`, Maven `target/` output, wrapper jars, class/jar
  files, image files, dependency directories, and common build output.
- Largest packed item: `spring-ai-agent-utils/src/test/resources/messages(4).json`,
  which is a test fixture and should be treated as context-heavy when queried.

## Codegeist-Relevant Surfaces

### Tool Exposure

The core library exposes many utilities as Spring AI `@Tool` methods or
`ToolCallback` builders. This is useful for understanding Spring AI integration,
but Codegeist must keep Spring AI tool execution behind its own runtime,
provider, tool, permission, workspace, event, and session contracts.

Relevant source areas:

- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/FileSystemTools.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GrepTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GlobTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/ShellTools.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/SkillsTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/TaskTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/advisors/AutoMemoryToolsAdvisor.java`

### Workspace And File Access

`FileSystemTools`, `ListDirectoryTool`, `GrepTool`, `GlobTool`, `Skills`, and
Claude subagent reference loading use filesystem paths directly. Codegeist should
study their behavior but must not let these utilities bypass Codegeist workspace
validation, ignored/generated/secret-like path posture, symlink policy, bounded
result summaries, or permission gates.

### Shell And Process Execution

`ShellTools` and `AgentEnvironment` contain process-related behavior. Codegeist's
controlled-shell posture treats shell execution as a high-risk verification tool,
not a generic process shortcut. Any reuse should be conceptual or wrapped behind
Codegeist shell request, permission, workspace-cwd, environment, timeout,
destructive-posture, and bounded-output contracts.

### Web And Network Tools

`SmartWebFetchTool` and `BraveWebSearchTool` are useful references for web access,
caching, domain checks, and search result shaping. Codegeist should classify
network tools separately from provider calls and require explicit permission or
policy before exposing them to a model.

### Skills, Memory, And Subagents

`SkillsTool`, `AutoMemoryTools`, `AutoMemoryToolsAdvisor`, `TaskTool`, Claude
subagents, and A2A subagents are conceptually close to Codegeist's planned
customization, memory, and delegation surfaces. They are also high-risk because
they can own prompt augmentation, storage, filesystem access, tool sets, or nested
agent execution. Codegeist should not adopt them directly unless a later task
proves they can be fully mediated by Codegeist-owned contracts.

## Initial Adoption Posture For T003

This workspace does not make the final adoption decision, but it strongly suggests
the following default posture for the upcoming T003 report:

| Surface | Initial Codegeist posture |
| --- | --- |
| BOM and direct dependency | Defer until Spring AI 1.1.x versus 2.0.x compatibility is proven. |
| `GrepTool` and `GlobTool` | Study closely; likely wrap or copy concepts behind Codegeist workspace and bounded-result contracts. |
| `FileSystemTools` and `ListDirectoryTool` | Do not use directly; side effects and path access require Codegeist mediation. |
| `ShellTools` | Copy concepts only or wrap after Codegeist shell contracts exist; no direct use. |
| `TodoWriteTool` | Potential concept reference for task state shape; direct use depends on runtime/session integration. |
| `AskUserQuestionTool` | Conceptually useful for approval/question UX; direct use must not own permission policy. |
| `SkillsTool` | Conceptually relevant, but Codegeist skills and repo-local guidance need a Codegeist-owned boundary. |
| `AutoMemoryTools` and advisor | Conceptually relevant, but direct use conflicts with Codegeist storage-port posture until wrapped. |
| `SmartWebFetchTool` and `BraveWebSearchTool` | Network-capable; require explicit Codegeist policy and permission gates. |
| `TaskTool`, Claude subagents, A2A subagents | Defer or copy concepts until runtime-owned nested task/subagent design exists. |

## Test Patterns Worth Reusing

The upstream repository contains focused tests for individual tools, builders,
edge cases, compatibility, task repositories, background tasks, questions, skills,
and advisors. Codegeist should reuse the testing posture, not necessarily the test
code:

- Builder validation tests for tool construction.
- Temporary-directory and path traversal tests for filesystem and search tools.
- Output mode, head limit, offset, context, and compatibility tests for grep-like
  behavior.
- Thread-safety tests for task repositories and question handling.
- Fake or mocked collaborators for tool callback and advisor behavior.
- Explicit error-path tests for missing tasks, invalid answers, unavailable paths,
  and timeout handling.

## Open Evidence Gaps

- Confirm the latest released Maven Central version and its actual dependency
  alignment.
- Verify whether a released artifact supports Spring AI `1.1.6` or only Spring AI
  `2.0.x` milestone/snapshot lines.
- Inspect native-image behavior before adding any dependency to Codegeist.
- Inspect exact tool output bounding and redaction behavior before considering any
  wrapper implementation.
- Decide whether semantic Graphify extraction is needed for a deeper research pass
  before the final T003 adoption report.
- Run upstream tests only if a future task needs runtime evidence. This refresh
  used static source, Repomix, and the existing Graphify cache only.

## Next Codegeist Step

Use `/solve-task` on `T003_01_analyze_spring_ai_agent_utils_adoption.md` to write
`docs/developer/spring-ai-agent-utils-adoption.md`. That report should use this
workspace as evidence, inspect source files directly, and record the final
candidate-by-candidate adoption classifications.
