# T004_01_05 Define Session Projection Core

Parent: `T004_01_implement_runtime_session_event_core`

Status: planned

## Goal

Implement the first client-safe session projection contracts with TDD.

## Context

This child task depends on the session and event contracts. It creates read-model
records and the small projector that lets later clients render state without
owning session transitions.

## Scope

- Create message-part, turn, and session projection records.
- Create `SessionProjector`.
- Create or complete `ProjectionConflict` usage for mismatched projection inputs.
- Add tests for idempotent replay by `EventId` and mismatched-session rejection.

## Planned Types

- `MessagePartProjection`
- `TurnProjection`
- `SessionProjection`
- `SessionProjector`
- `ProjectionConflict`
- `RuntimeSessionEventContractTests#projectsEventsIdempotentlyByEventId`

## Planning Requirements

- Create or update a child-specific implementation plan before solving this task.
- Include a Mermaid or PlantUML class diagram covering every planned production
  type and test class in this child task.
- Explain every planned type in detail: responsibility, key fields or methods,
  idempotency behavior, projection conflict handling, read-model ownership, and
  relationship to earlier runtime/session/event slices.
- Include a Spring usage section that names every Spring Framework, Spring Boot,
  Spring AI, Spring Shell, or Spring AI Agent Utils class the solve phase should
  use. The expected public contract posture for this slice is Spring-free; if the
  plan keeps that posture, it must state that no Spring classes should appear in
  the public projection contracts and explain why.
- Name the first failing test and the narrow Maven command for this child slice.

## Non-Goals

- Do not implement storage, event sourcing, SSE, server projection APIs, CLI/TUI
  rendering, continuation, compaction, archival, or persistence.

## Acceptance Criteria

- Replaying the same event id does not duplicate projection events.
- Projection rejects events from mismatched sessions using the typed failure
  boundary.
- Projection remains a read model, not the owner of state transitions.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run the focused projection test and the full
`RuntimeSessionEventContractTests` class.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/session-projection-core-implementation.md`.

Planned solve-phase files:

```text
app/codegeist/cli/src/main/java/ai/codegeist/runtime/ProjectionConflict.java
app/codegeist/cli/src/main/java/ai/codegeist/session/
  MessagePartProjection.java
  SessionProjection.java
  SessionProjector.java
  TurnProjection.java

app/codegeist/cli/src/test/java/ai/codegeist/runtime/RuntimeSessionEventContractTests.java
```

Spring usage decision: no Spring Framework, Spring Boot, Spring AI, Spring Shell,
or Spring AI Agent Utils classes should be used in public projection contracts or
their plain JVM tests.

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns session projection contracts only.
- Open decisions or blockers: depends on `T004_01_01` through `T004_01_04`.
- Next recommended phase: `/plan-task T004_01_05` after dependencies are solved.

## Planning Result

- Phase command: `/plan-task T004_01_05` as part of user input to plan all
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
  until `T004_01_01` through `T004_01_04` are solved.
- Result: created
  `docs/developer/implementation/session-projection-core-implementation.md` with a
  class diagram, detailed type catalog, Spring usage decision, file map, ordered
  implementation steps, TDD sequence, acceptance criteria, dependencies, risks,
  and verification strategy.
- Open decisions or blockers: none at planning depth.
- Next recommended phase: `/solve-task T004_01_05` after dependencies are solved.
