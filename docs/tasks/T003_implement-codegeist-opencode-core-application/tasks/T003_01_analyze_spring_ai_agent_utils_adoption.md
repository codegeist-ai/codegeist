# T003_01 Analyze Spring AI Agent Utils Adoption

Parent: `T003_implement-codegeist-opencode-core-application`

Status: open

## Goal

Analyze `https://github.com/spring-ai-community/spring-ai-agent-utils` and decide
which parts can be used directly, wrapped behind Codegeist contracts, copied
conceptually, deferred, or rejected for the Codegeist OpenCode-core
implementation.

## Context

Codegeist should not reimplement useful Spring AI agent utilities unnecessarily.
At the same time, external tools must not bypass Codegeist-owned runtime, session,
event, permission, workspace, storage, and native-readiness boundaries.

The user explicitly wants this analysis to happen before the other T003 guidance
and implementation tasks. The prior backlog idea to analyze this repository is now
promoted into this tracked T003 child task.

## Initial Repository Findings

A read-only Repomix packaging of the remote repository found these relevant
modules and utilities:

- `spring-ai-agent-utils` core library with tools, advisors, skills, memory, and
  Claude-style subagents.
- `spring-ai-agent-utils-common` shared subagent SPI.
- `spring-ai-agent-utils-a2a` A2A protocol subagent implementation.
- `spring-ai-agent-utils-bom` Maven BOM.
- Tools and utilities including `FileSystemTools`, `GlobTool`, `GrepTool`,
  `ShellTools`, `TodoWriteTool`, `AskUserQuestionTool`, `SkillsTool`, `TaskTool`,
  `AutoMemoryTools`, `AutoMemoryToolsAdvisor`, `SmartWebFetchTool`, and
  `BraveWebSearchTool`.
- Test coverage with JUnit, Mockito, AssertJ, temporary directories, compatibility
  tests, and tool-specific behavior tests.

The public README advertises Maven coordinates such as
`org.springaicommunity:spring-ai-agent-utils:0.7.0`, while the packed source also
contains `0.8.0-SNAPSHOT` development modules. Compatibility with Codegeist's
Spring Boot `3.5.14`, Spring AI `1.1.6`, Java `25`, and GraalVM posture must be
verified instead of assumed.

## Scope

- Analyze Maven artifacts, versions, modules, and dependency risk.
- Check Spring Boot and Spring AI compatibility with Codegeist's current baseline.
- Check Java baseline and GraalVM/native-image implications.
- Review tool APIs and Spring AI `ToolCallback` integration shape.
- Review file, grep, glob, shell, todo, question, web, skills, memory, task, and
  subagent utilities.
- Review test strategy and reusable test patterns.
- Decide whether Codegeist should depend on the library directly, wrap selected
  utilities, copy concepts, defer adoption, or reject utilities.

## Deferred Surface Compatibility

This analysis must account for future JBang, Vaadin, headless server, and
API/SDK work, but it must not implement those surfaces. Adoption decisions should
record whether a utility helps or constrains those future surfaces.

## Key Questions

- Can `spring-ai-agent-utils` `0.7.0` be used with Codegeist's Spring Boot
  `3.5.14` and Spring AI `1.1.6` baseline?
- Which utilities can be useful for Codegeist immediately?
- Which utilities are unsafe unless wrapped behind Codegeist permission and
  workspace gates?
- Does `ShellTools` satisfy Codegeist controlled-shell requirements, or only
  provide implementation ideas?
- Can `GrepTool` and `GlobTool` replace or inform Codegeist source-search tools?
- Can `FileSystemTools` be used without violating Codegeist workspace policy?
- Can `SkillsTool` map to Codegeist skills without conflicting with `.opencode`
  skills and repo-local guidance?
- Can `AutoMemoryTools` inform Codegeist memory, or does it conflict with the
  storage-port posture?
- Can `TaskTool` and subagent support inform later Codegeist subagent behavior?
- Are there tests, fixtures, or performance patterns worth adopting?
- What are the native-image and startup-time risks?

## Deliverables

Create:

- `docs/developer/spring-ai-agent-utils-adoption.md`

The report should classify each candidate with one of these adoption decisions:

- `use-directly`
- `wrap-behind-codegeist-contract`
- `copy-concept-only`
- `defer`
- `reject`

## Acceptance Criteria

- The report identifies all relevant modules and utilities.
- The report records Maven coordinates and version choices or open compatibility
  checks.
- Compatibility with Codegeist's Spring Boot, Spring AI, Java, and GraalVM baseline
  is assessed.
- Every useful utility has an adoption decision and reason.
- Tool, permission, workspace, shell, storage, skill, subagent, performance, and
  native risks are explicitly noted.
- The report defines follow-up implementation tasks for selected adoption paths.
- No production dependency is added by this analysis task unless a later task
  explicitly chooses it.
- No Codegeist runtime behavior changes in this task.

## Verification

```bash
git --no-pager diff --check
```

## Non-Goals

- Do not add `spring-ai-agent-utils` as a Codegeist dependency in this analysis
  task.
- Do not implement or modify Codegeist runtime, provider, tools, permissions,
  storage, shell, skills, or subagents.
- Do not execute live provider calls or network-dependent tests.
- Do not treat external utilities as trusted tool execution paths unless a later
  implementation task wraps them behind Codegeist contracts.

## Creation Note

Status: open.

Created as the first T003 child task by user direction. The user wants to see how
much of Spring AI Agent Utils can be reused directly or adapted before Codegeist
starts implementing the core OpenCode-replacement application.
