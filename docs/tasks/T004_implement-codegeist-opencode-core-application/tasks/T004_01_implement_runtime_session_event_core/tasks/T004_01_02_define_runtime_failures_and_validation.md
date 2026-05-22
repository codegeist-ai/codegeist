# T004_01_02 Define Runtime Failures And Validation

Parent: `T004_01_implement_runtime_session_event_core`

Status: planned

## Goal

Implement typed runtime contract failures and prompt request validation with TDD.

## Context

This child task depends on the prompt contracts from `T004_01_01`. It adds the
failure vocabulary needed for request validation and later sequence/projection
failures without introducing framework or provider concerns.

## Scope

- Create the first runtime failure sealed family.
- Create recoverability metadata.
- Implement prompt request validation for blank or malformed request input.
- Add tests for redacted validation messages and recoverability.

## Planned Types

- `Recoverability`
- `RuntimeContractFailure`
- `InvalidPromptRequest`
- `InvalidIdentifier`
- `InvalidSequence`
- `UnsupportedMode`
- `PromptRequestValidator`
- `RuntimeSessionEventContractTests#rejectsBlankPromptWithTypedFailure`

## Planning Requirements

- Create or update a child-specific implementation plan before solving this task.
- Include a Mermaid or PlantUML class diagram covering every planned production
  type and test class in this child task.
- Explain every planned type in detail: responsibility, key fields or methods,
  validation boundary, redaction behavior, recoverability semantics, and
  relationship to earlier prompt contracts and later session/event slices.
- Include a Spring usage section that names every Spring Framework, Spring Boot,
  Spring AI, Spring Shell, or Spring AI Agent Utils class the solve phase should
  use. The expected public contract posture for this slice is Spring-free; if the
  plan keeps that posture, it must state that no Spring classes should appear in
  the public runtime failure contracts and explain why.
- Name the first failing test and the narrow Maven command for this child slice.

## Non-Goals

- Do not implement session aggregates, event envelopes, projection logic, storage,
  provider calls, tools, permissions, or CLI command behavior.
- Do not expose raw prompt text in failure messages.

## Acceptance Criteria

- Blank prompt text maps to `InvalidPromptRequest`.
- Runtime failures expose redacted messages and recoverability metadata.
- Identifier and sequence failure types exist for later child tasks without
  implementing their full usage outside this slice.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run at least the new focused test and the existing prompt
contract test from `T004_01_01`.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/runtime-failures-validation-implementation.md`.

Planned solve-phase files:

```text
app/codegeist/cli/src/main/java/ai/codegeist/runtime/
  InvalidIdentifier.java
  InvalidPromptRequest.java
  InvalidSequence.java
  PromptRequestValidator.java
  Recoverability.java
  RuntimeContractFailure.java
  UnsupportedMode.java

app/codegeist/cli/src/test/java/ai/codegeist/runtime/RuntimeSessionEventContractTests.java
```

Spring usage decision: no Spring Framework, Spring Boot, Spring AI, Spring Shell,
or Spring AI Agent Utils classes should be used in runtime failure contracts or
their plain JVM tests.

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns runtime failures and validation only.
- Open decisions or blockers: depends on `T004_01_01` implementation.
- Next recommended phase: `/plan-task T004_01_02` after `T004_01_01` is solved.

## Planning Result

- Phase command: `/plan-task T004_01_02` as part of user input to plan all
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
  until `T004_01_01` is solved.
- Result: created
  `docs/developer/implementation/runtime-failures-validation-implementation.md`
  with a class diagram, detailed type catalog, Spring usage decision, file map,
  ordered implementation steps, TDD sequence, acceptance criteria, dependencies,
  risks, and verification strategy.
- Open decisions or blockers: none at planning depth.
- Next recommended phase: `/solve-task T004_01_02` after `T004_01_01` is solved.
