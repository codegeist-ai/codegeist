# T004_01_04 Define Event Core Contracts

Parent: `T004_01_implement_runtime_session_event_core`

Status: planned

## Goal

Implement the first `ai.codegeist.event` runtime event contracts with TDD.

## Context

This child task depends on the runtime and session identifiers from earlier
children. It adds ordered event envelopes and the first payload sealed family
without creating an event bus, transport, or storage adapter.

## Scope

- Create event identity, type, source, and visibility metadata.
- Create `EventEnvelope` and `RuntimeEvent` records.
- Create the first sealed event payload family.
- Add tests for positive event sequence and optional turn sequence rules.

## Planned Types

- `EventId`
- `EventType`
- `EventSource`
- `EventVisibility`
- `EventEnvelope`
- `RuntimeEvent`
- `EventPayload`
- `SessionCreated`
- `SessionUpdated`
- `TurnStarted`
- `UserInputAccepted`
- `TurnCompleted`
- `WarningRaised`
- `ErrorRaised`
- `RuntimeSessionEventContractTests#assignsMonotonicSessionEventSequence`

## Planning Requirements

- Create or update a child-specific implementation plan before solving this task.
- Include a Mermaid or PlantUML class diagram covering every planned production
  type and test class in this child task.
- Explain every planned type in detail: responsibility, key fields or methods,
  event sequencing role, visibility or source semantics, payload boundary, and
  relationship to earlier runtime/session slices and later projection work.
- Include a Spring usage section that names every Spring Framework, Spring Boot,
  Spring AI, Spring Shell, or Spring AI Agent Utils class the solve phase should
  use. The expected public contract posture for this slice is Spring-free; if the
  plan keeps that posture, it must state that no Spring classes should appear in
  the public event contracts and explain why.
- Name the first failing test and the narrow Maven command for this child slice.

## Non-Goals

- Do not implement SSE, an event bus, event sourcing, persistence, provider-native
  stream chunks, tool events, shell events, patch events, or UI rendering.

## Acceptance Criteria

- Runtime event envelopes preserve positive session sequence values.
- Turn sequence is optional but positive when present.
- Runtime events remain Codegeist-owned and do not expose framework, provider,
  storage, transport, or UI types.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run the focused event-sequencing test and the earlier contract
tests needed by the event types.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/event-core-contracts-implementation.md`.

Planned solve-phase files:

```text
app/codegeist/cli/src/main/java/ai/codegeist/event/
  ErrorRaised.java
  EventEnvelope.java
  EventId.java
  EventPayload.java
  EventSource.java
  EventType.java
  EventVisibility.java
  RuntimeEvent.java
  SessionCreated.java
  SessionUpdated.java
  TurnCompleted.java
  TurnStarted.java
  UserInputAccepted.java
  WarningRaised.java

app/codegeist/cli/src/test/java/ai/codegeist/runtime/RuntimeSessionEventContractTests.java
```

Spring usage decision: no Spring Framework, Spring Boot, Spring AI, Spring Shell,
or Spring AI Agent Utils classes should be used in public event contracts or their
plain JVM tests.

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns event envelope and payload contracts only.
- Open decisions or blockers: depends on `T004_01_01`, `T004_01_02`, and
  `T004_01_03`.
- Next recommended phase: `/plan-task T004_01_04` after dependencies are solved.

## Planning Result

- Phase command: `/plan-task T004_01_04` as part of user input to plan all
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
  until `T004_01_01` through `T004_01_03` are solved.
- Result: created
  `docs/developer/implementation/event-core-contracts-implementation.md` with a
  class diagram, detailed type catalog, Spring usage decision, file map, ordered
  implementation steps, TDD sequence, acceptance criteria, dependencies, risks,
  and verification strategy.
- Open decisions or blockers: none at planning depth.
- Next recommended phase: `/solve-task T004_01_04` after dependencies are solved.
