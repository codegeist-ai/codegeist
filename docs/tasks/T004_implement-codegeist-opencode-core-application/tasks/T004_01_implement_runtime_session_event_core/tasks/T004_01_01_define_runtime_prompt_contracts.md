# T004_01_01 Define Runtime Prompt Contracts

Parent: `T004_01_implement_runtime_session_event_core`

Status: planned

## Goal

Implement the first `ai.codegeist.runtime` prompt-intake contracts with TDD.

## Context

This child task is the first slice of `T004_01`. It creates the prompt request
and acceptance boundary that later CLI, TUI, server, Vaadin, and extension
adapters can call without owning session mutation.

## Scope

- Create runtime prompt identity and client-surface value types.
- Create the first prompt request and prompt acceptance records.
- Create the small `RuntimePromptPort` boundary interface.
- Add the narrow contract test coverage needed to prove a prompt can be accepted
  through Codegeist-owned types without framework exposure.

## Planned Types

- `PromptRequestId`
- `CorrelationId`
- `WorkspaceRef`
- `AgentMode`
- `SourceClient`
- `PromptRequest`
- `PromptAcceptance`
- `RuntimePromptPort`
- `RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes`

## Planning Requirements

- Create or update a child-specific implementation plan before solving this task.
- Include a Mermaid or PlantUML class diagram covering every planned production
  type and test class in this child task.
- Explain every planned type in detail: responsibility, key fields or methods,
  validation boundary, public contract role, and relationship to later session and
  event slices.
- Include a Spring usage section that names every Spring Framework, Spring Boot,
  Spring AI, Spring Shell, or Spring AI Agent Utils class the solve phase should
  use. The expected public contract posture for this slice is Spring-free; if the
  plan keeps that posture, it must state that no Spring classes should appear in
  the public runtime prompt contracts and explain why.
- Name the first failing test and the narrow Maven command for this child slice.

## Non-Goals

- Do not implement validation failures, sequence enforcement, event projection, or
  dependency-boundary scanning beyond what this first prompt contract test needs.
- Do not add Spring Shell commands, Spring AI calls, Agent Utils exposure,
  provider behavior, context loading, workspace validation, storage, tools,
  permissions, CLI rendering, or TUI behavior.

## Acceptance Criteria

- The first prompt request can be represented with Codegeist-owned records and
  enums.
- A `RuntimePromptPort` can return a `PromptAcceptance` without exposing Spring,
  Spring AI, Agent Utils, provider, storage, CLI, TUI, HTTP, Vaadin, PF4J, or JBang
  types.
- The test remains a plain JVM test and can be run by method selector.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run at least:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes test
```

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/runtime-prompt-contracts-implementation.md`.

Planned solve-phase production files:

```text
app/codegeist/cli/src/main/java/ai/codegeist/runtime/
  AgentMode.java
  CorrelationId.java
  PromptAcceptance.java
  PromptRequest.java
  PromptRequestId.java
  RuntimePromptPort.java
  SourceClient.java
  WorkspaceRef.java
```

Planned solve-phase test file:

```text
app/codegeist/cli/src/test/java/ai/codegeist/runtime/RuntimeSessionEventContractTests.java
```

Spring usage decision: no Spring Framework, Spring Boot, Spring AI, Spring Shell,
or Spring AI Agent Utils classes should be used in the public runtime prompt
contracts. The test should stay plain JVM and use JUnit Jupiter plus AssertJ only.

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns only the initial runtime prompt contracts.
- Open decisions or blockers: none at specification depth.
- Next recommended phase: `/plan-task T004_01_01`.

## Planning Result

- Phase command: `/plan-task t004_01`, resolved to this first ordered child task
  because `T004_01` is a grouped task and its specification recommends
  `/plan-task T004_01_01` next.
- Context or instructions considered: user input `t004_01`.
- Selected option: plan `T004_01_01 Define Runtime Prompt Contracts` as the first
  concrete implementation slice rather than creating a duplicate parent-level
  plan.
- Duplicate check result: no child-specific implementation plan existed for this
  task; the broader `runtime-session-event-core-implementation.md` remains the
  parent overview.
- Discovered hints considered:
  `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md`,
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read:
  `docs/developer/architecture/architecture.md`,
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`,
  `docs/developer/specification/testing-strategy-and-agent-rules.md`,
  `docs/developer/spring-ai-agent-utils-adoption.md`, `app/codegeist/cli/pom.xml`,
  `CodegeistApplication.java`, and `CodegeistApplicationTests.java`.
- Spring AI Agent Utils evidence considered: local source references for
  `TaskCall`, `BackgroundTask`, `TaskTool`, `TaskRepository`, and `TodoWriteTool`;
  none fit as public prompt-intake contract types.
- Upstream phase dependency: satisfied by the existing specification status and
  result in this task.
- Result: created
  `docs/developer/implementation/runtime-prompt-contracts-implementation.md` with
  a child-specific class diagram, detailed type catalog, Spring usage decision,
  file map, implementation steps, TDD sequence, acceptance criteria, dependencies,
  risks, and verification strategy.
- Open decisions or blockers: none.
- Next recommended phase: `/solve-task T004_01_01`.
