# T004_01_06 Verify Core Dependency Boundaries

Parent: `T004_01_implement_runtime_session_event_core`

Status: planned

## Goal

Verify the completed runtime/session/event core packages keep forbidden framework
and deferred-surface types out of their public contracts.

## Context

This child task closes `T004_01` after the runtime, failure, session, event, and
projection child slices are solved. It owns the dependency-boundary test and the
architecture documentation update for the implemented packages.

## Scope

- Add `RuntimeSessionEventDependencyTests`.
- Verify public signatures in `ai.codegeist.runtime`, `ai.codegeist.session`, and
  `ai.codegeist.event` do not expose forbidden framework or deferred-surface
  packages.
- Run targeted runtime/session/event tests and the broader CLI Maven test suite.
- Update `docs/developer/architecture/architecture.md` after source and tests
  exist.
- Update the parent `T004_01` task solve status when all child slices pass.

## Planned Types

- `RuntimeSessionEventDependencyTests`
- `RuntimeSessionEventDependencyTests#coreContractsDoNotExposeFrameworkTypes`

## Planning Requirements

- Create or update a child-specific implementation plan before solving this task.
- Include a Mermaid or PlantUML class diagram covering the dependency-boundary test
  class and the core public-contract packages or representative types it verifies.
- Explain every planned test helper or verification type in detail:
  responsibility, inspected contracts, forbidden dependency set, failure reporting,
  and relationship to the completed `T004_01_01` through `T004_01_05` slices.
- Include a Spring usage section that names every Spring Framework, Spring Boot,
  Spring AI, Spring Shell, or Spring AI Agent Utils class the solve phase should
  use or explicitly forbid. This slice is expected to verify that public core
  contracts expose none of those Spring or Agent Utils types.
- Name the first failing test and the narrow Maven command for this child slice.

## Non-Goals

- Do not add new domain behavior beyond dependency verification and architecture
  documentation.
- Do not implement provider calls, context loading, tools, permissions, workspace
  reads, patch/edit, shell execution, storage adapters, CLI commands, TUI, server,
  Vaadin, PF4J, JBang, live model calls, or packaging behavior.

## Acceptance Criteria

- Dependency-boundary tests prove no public core contract signatures expose Spring,
  Spring AI, Agent Utils, provider SDK, storage adapter, CLI, TUI, HTTP, Vaadin,
  PF4J, JBang, filesystem, process, or terminal UI types.
- `docs/developer/architecture/architecture.md` describes the implemented
  runtime/session/event packages and tests.
- Parent `T004_01` can be marked solved only after all child tasks are solved and
  verification passes.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run at least:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests test
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventDependencyTests test
mvn --batch-mode --no-transfer-progress test
```

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/runtime-session-event-dependency-boundaries-implementation.md`.

Planned solve-phase test file:

```text
app/codegeist/cli/src/test/java/ai/codegeist/runtime/RuntimeSessionEventDependencyTests.java
```

Documentation and task files to update after solve:

```text
docs/developer/architecture/architecture.md
docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md
docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/tasks/T004_01_06_verify_core_dependency_boundaries.md
```

Spring usage decision: the test should explicitly forbid public exposure of
Spring Framework, Spring Boot, Spring AI, Spring Shell, and Spring AI Agent Utils
types from core contracts. The test itself should use JUnit Jupiter, AssertJ, and
Java reflection only.

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns dependency-boundary verification and final architecture
  synchronization for `T004_01`.
- Open decisions or blockers: depends on `T004_01_01` through `T004_01_05`.
- Next recommended phase: `/plan-task T004_01_06` after dependencies are solved.

## Planning Result

- Phase command: `/plan-task T004_01_06` as part of user input to plan all
  subtasks in `T004_01`.
- Context or instructions considered: user input `für alle subtasks in t004_01
  ausführen`, interpreted as explicit permission to plan every existing
  `T004_01_*` child task.
- Selected option: sharpen this existing child task with a child-specific
  implementation plan.
- Duplicate check result: no child-specific implementation plan existed for this
  task.
- Discovered hints considered:
  `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md`,
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read: T004 parent, T004_01 parent, adjacent child tasks,
  `runtime-session-event-source-generation-contract.md`,
  `testing-strategy-and-agent-rules.md`, `architecture.md`, and the existing
  `T004_01_01` implementation handoff.
- Upstream phase dependency: satisfied by specification; solve remains blocked
  until `T004_01_01` through `T004_01_05` are solved.
- Result: created
  `docs/developer/implementation/runtime-session-event-dependency-boundaries-implementation.md`
  with a class diagram, detailed test-helper catalog, Spring usage and forbidden
  dependency decision, file map, ordered implementation steps, TDD sequence,
  acceptance criteria, dependencies, risks, and verification strategy.
- Open decisions or blockers: none at planning depth.
- Next recommended phase: `/solve-task T004_01_06` after dependencies are solved.
