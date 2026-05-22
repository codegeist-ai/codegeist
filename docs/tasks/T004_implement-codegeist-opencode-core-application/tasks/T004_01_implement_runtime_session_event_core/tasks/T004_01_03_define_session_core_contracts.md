# T004_01_03 Define Session Core Contracts

Parent: `T004_01_implement_runtime_session_event_core`

Status: planned

## Goal

Implement the first `ai.codegeist.session` aggregate contracts with TDD.

## Context

This child task depends on the runtime prompt contracts and failure vocabulary. It
adds the session, turn, and message-part records that represent append-oriented
runtime-owned session state.

## Scope

- Create typed session, turn, and message-part identifiers.
- Create first lifecycle and message-part enums.
- Create append-oriented `Session`, `Turn`, and `MessagePart` records.
- Add tests for monotonic turn and message-part ordering.

## Planned Types

- `SessionId`
- `TurnId`
- `PartId`
- `SessionStatus`
- `TurnStatus`
- `MessagePartType`
- `MessagePart`
- `Turn`
- `Session`
- `RuntimeSessionEventContractTests#appendsTurnsAndPartsInOrder`

## Planning Requirements

- Create or update a child-specific implementation plan before solving this task.
- Include a Mermaid or PlantUML class diagram covering every planned production
  type and test class in this child task.
- Explain every planned type in detail: responsibility, key fields or methods,
  ordering rules, lifecycle boundary, validation or failure behavior, and
  relationship to runtime prompt contracts and later event/projection slices.
- Include a Spring usage section that names every Spring Framework, Spring Boot,
  Spring AI, Spring Shell, or Spring AI Agent Utils class the solve phase should
  use. The expected public contract posture for this slice is Spring-free; if the
  plan keeps that posture, it must state that no Spring classes should appear in
  the public session contracts and explain why.
- Name the first failing test and the narrow Maven command for this child slice.

## Non-Goals

- Do not implement projection, event payloads, storage ports, continuation, CLI
  rendering, TUI state, or provider streaming.
- Do not allow client adapters to own session mutation.

## Acceptance Criteria

- Sessions contain ordered turns.
- Turns contain ordered message parts.
- Invalid non-monotonic or non-positive sequences use the runtime failure boundary
  from `T004_01_02`.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run the focused session-ordering test and the earlier
runtime-prompt validation tests.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/session-core-contracts-implementation.md`.

Planned solve-phase files:

```text
app/codegeist/cli/src/main/java/ai/codegeist/session/
  MessagePart.java
  MessagePartType.java
  PartId.java
  Session.java
  SessionId.java
  SessionStatus.java
  Turn.java
  TurnId.java
  TurnStatus.java

app/codegeist/cli/src/test/java/ai/codegeist/runtime/RuntimeSessionEventContractTests.java
```

Spring usage decision: no Spring Framework, Spring Boot, Spring AI, Spring Shell,
or Spring AI Agent Utils classes should be used in public session contracts or
their plain JVM tests.

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns session aggregate contracts only.
- Open decisions or blockers: depends on `T004_01_01` and `T004_01_02`.
- Next recommended phase: `/plan-task T004_01_03` after dependencies are solved.

## Planning Result

- Phase command: `/plan-task T004_01_03` as part of user input to plan all
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
  until `T004_01_01` and `T004_01_02` are solved.
- Result: created
  `docs/developer/implementation/session-core-contracts-implementation.md` with a
  class diagram, detailed type catalog, Spring usage decision, file map, ordered
  implementation steps, TDD sequence, acceptance criteria, dependencies, risks,
  and verification strategy.
- Open decisions or blockers: none at planning depth.
- Next recommended phase: `/solve-task T004_01_03` after dependencies are solved.
