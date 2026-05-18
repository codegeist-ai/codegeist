# Spring AI Agent Utils Boundary Guide

Guidance for using `org.springaicommunity:spring-ai-agent-utils` without binding
Codegeist architecture to the Agent Utils architecture.

## Purpose

Codegeist keeps Spring AI Agent Utils on the classpath as a useful Java/Spring
tool library. That does not mean Codegeist must create a separate wrapper layer
before any use, and it does not mean Codegeist should copy Agent Utils runtime
architecture.

The immediate goal is simpler:

- Use Agent Utils where it helps implementation move faster.
- Keep Codegeist-owned runtime, provider, tool, permission, workspace, event,
  session, storage, and client contracts independent from Agent Utils types.
- Avoid exposing Agent Utils callbacks as the architecture boundary.
- Add a wrapper only when a concrete use case needs policy mediation, result
  mapping, or replacement flexibility.

The current build baseline already imports:

| Area | Value |
| --- | --- |
| Spring Boot | `4.0.6` |
| Spring AI | BOM `2.0.0-M6` |
| Spring AI Agent Utils | BOM and core artifact `0.7.0` |
| Spring Shell | `4.0.2` |
| Java | `25` |

## Core Rule

Do not let Agent Utils become Codegeist's architecture.

Allowed:

- Construct and call Agent Utils classes inside Codegeist implementation code.
- Reuse Agent Utils behavior for search, globbing, listing, file reads, skills,
  web fetches, memory, or task support when the surrounding Codegeist policy is
  clear enough.
- Use Agent Utils tests and source as behavior references.
- Introduce small adapters later if a specific boundary needs them.

Not allowed:

- Store Agent Utils request, response, repository, task, subagent, memory, or
  callback types in Codegeist public contracts.
- Make CLI, runtime, provider, API, storage, or UI packages depend on Agent Utils
  concepts as their domain model.
- Register broad Agent Utils tool objects with a provider before Codegeist has
  decided which capabilities are allowed in the active mode and workspace.
- Let Agent Utils own permission prompts, workspace trust, session lifecycle,
  persistent storage, or runtime events.

## Boundary Placement

Codegeist contracts should stay in Codegeist packages.

```text
ai.codegeist.tool       Tool descriptors, tool requests, tool results
ai.codegeist.workspace  Workspace roots, path validation, ignore policy
ai.codegeist.permission Mode and approval decisions
ai.codegeist.provider   Spring AI provider integration
ai.codegeist.session    Session projection
ai.codegeist.event      Runtime and audit events
```

Agent Utils belongs below those boundaries as an implementation dependency. A
future adapter package is optional, not mandatory:

```text
ai.codegeist.tool.agentutils  Optional adapters only when needed
```

## Direct Use Pattern

Prefer direct use when the Codegeist caller already owns the policy and the
Agent Utils method is just implementation detail.

```java
final class SourceSearchService {

    private final WorkspacePolicy workspacePolicy;
    private final GrepTool grepTool;

    String search(SourceSearchRequest request) {
        Path root = workspacePolicy.resolveReadableDirectory(request.path());

        return grepTool.grep(
            request.pattern(),
            root.toString(),
            request.glob().orElse(null),
            GrepTool.OutputMode.files_with_matches,
            null,
            null,
            null,
            true,
            false,
            request.type().orElse(null),
            request.limit().orElse(100),
            0,
            false
        );
    }
}
```

This is acceptable because `SourceSearchService` takes and returns Codegeist
types. `GrepTool` is a private implementation dependency, not the service
contract.

## When To Add An Adapter

Add a small adapter only when one of these becomes true:

- More than one Codegeist service needs the same Agent Utils setup or result
  mapping.
- Agent Utils string output must become typed `ToolResult` values.
- Codegeist needs replaceability for a tool implementation.
- A provider callback would otherwise expose Agent Utils tool objects directly.
- Permission, workspace, redaction, truncation, or output-reference behavior is
  repeated around the same utility.

Do not add an adapter just to hide every Agent Utils constructor. That creates
extra code without improving the architecture.

## Provider Exposure

Provider callbacks are the sharp boundary. Treat them more carefully than normal
internal service calls.

Safe direction:

```text
Spring AI provider
  -> Codegeist tool callback
  -> Codegeist policy/service
  -> optional Agent Utils call
```

Avoid this until the tool policy is explicitly designed:

```text
Spring AI provider
  -> raw Agent Utils @Tool object
  -> filesystem, shell, network, memory, or subagent behavior
```

The concern is not the dependency itself. The concern is letting raw provider
tool calls bypass Codegeist mode, permission, workspace, event, and storage
decisions.

## First Practical Uses

These Agent Utils classes are reasonable candidates for early direct internal
use, provided Codegeist owns the surrounding request and policy:

| Agent Utils class | Early use | Boundary note |
| --- | --- | --- |
| `GrepTool` | Source/content search implementation | Validate workspace path before calling; keep Codegeist request/result types. |
| `GlobTool` | File pattern matching | Normalize paths to workspace-relative values before returning to clients. |
| `ListDirectoryTool` | Directory listing | Cap depth and result count in Codegeist config. |
| `FileSystemTools.read` | File read implementation | Validate path, file type, size, and secret-like posture first. |

Defer or gate these more heavily:

| Agent Utils surface | Why |
| --- | --- |
| `FileSystemTools.write` and `edit` | They should wait for Codegeist patch/edit and approval decisions. |
| `ShellTools` | It needs controlled shell mode, destructive-command classification, cwd validation, timeout, and output policy. |
| `SmartWebFetchTool` and `BraveWebSearchTool` | They need network permission, credential references, cache policy, and provider mediation for summarization. |
| `SkillsTool` | It needs Codegeist skill discovery, repo-local precedence, trust policy, and context-size bounds. |
| `AutoMemoryTools` and `AutoMemoryToolsAdvisor` | They need Codegeist storage, retention, redaction, and prompt ownership decisions. Do not adopt the advisor as the prompt boundary. |
| `TaskTool` and A2A support | They need Codegeist child sessions, cancellation, auth, background task storage, and remote-agent trust decisions. |

## Result Handling

Most Agent Utils tools return human-readable `String` values. That is fine for
internal prototypes, but Codegeist should not store those strings as its durable
tool model.

When a result crosses into runtime events, sessions, provider responses, API
responses, or UI state, normalize it:

| Agent Utils output | Codegeist handling |
| --- | --- |
| Normal text | Bounded output or summary. |
| Empty result | Explicit no-result state. |
| Prefix `Error:` | Typed Codegeist failure. |
| Long output | Truncated summary plus optional `OutputRef`. |
| File paths | Workspace-relative paths after redaction/filtering. |

This normalization can live in the calling service first. Extract a mapper only
after it is reused.

## Tests

Tests should prove Codegeist boundaries, not Agent Utils internals.

Required first-slice tests:

| Test area | Required cases |
| --- | --- |
| Workspace validation | Deny outside-root path, symlink escape, missing path, and directory-as-file read. |
| Search behavior | Match/no-match, output modes, context limits, head limit, offset, case-insensitive search. |
| Glob behavior | Pattern match, max results, ignored/generated filtering, workspace-relative output. |
| Directory behavior | Depth, limit, ignored directory filtering. |
| File read behavior | Offset/limit, empty file, long-line behavior, secret-like or oversized file denial if policy requires it. |
| Provider boundary | Provider callback uses a Codegeist-owned callback or service boundary, not raw broad Agent Utils tool registration. |

Use temporary-directory fixtures. Do not require network, provider credentials,
native-image, shell execution, or remote agents for read-only search/list/read
work.

## Source Reference

Local third-party source lives under:

```text
docs/third-party/spring-ai-agent-utils/source/
```

Useful files:

- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GrepTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/GlobTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/ListDirectoryTool.java`
- `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/FileSystemTools.java`
- `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolTest.java`
- `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GlobToolTest.java`
- `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/FileSystemToolsTest.java`

Use upstream tests as behavior references, then write Codegeist tests against
Codegeist boundaries.
