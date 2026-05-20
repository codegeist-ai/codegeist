# T004_01 Implement Runtime Session Event Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Implement the first runtime, session, and event Java core contracts with TDD.

## Context

This task consumes `docs/developer/specification/runtime-session-event-source-generation-contract.md`
and must stay aligned with `docs/developer/architecture/architecture.md`. It is the
first source-generating task and should establish the initial packages under
`ai.codegeist.runtime`, `ai.codegeist.session`, and `ai.codegeist.event`.

## Scope

- Create the first runtime/session/event records, identifiers, enums, sealed event
  families, projection shapes, small ports, and typed failures selected by the
  plan.
- Add plain JVM tests first; avoid Spring context startup unless the plan proves it
  is necessary.
- Update architecture documentation after source and tests exist.

## Non-Goals

- Do not implement provider calls, context loading, tools, permissions, workspace
  reads, patch/edit, shell execution, storage adapters, CLI commands, TUI, server,
  Vaadin, PF4J, JBang, live model calls, or packaging behavior.
- Do not create code before `/plan-task t004_01` produces an UML class diagram,
  file map, TDD sequence, and documentation plan.

## Direct Inputs

- `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/runtime-session-event-source-generation-contract.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`

## Planning Requirements

- Create `docs/developer/implementation/runtime-session-event-core-implementation.md`.
- Include an UML class diagram covering every planned runtime/session/event class,
  record, interface, enum, sealed interface, failure type, and test class.
- Name the exact Java and test files to add or change.
- Define the first failing test and the exact narrow Maven command.

## Acceptance Criteria

- Runtime/session/event core classes and tests are implemented only after planning.
- Tests are individually executable and prove identifiers, request validation,
  session/turn/message structures, event envelopes, sequencing, projection, and
  typed failures selected by the plan.
- Architecture docs describe the newly implemented packages and tests.

## Verification

Planning-only changes:

```bash
git --no-pager diff --check
```

The planning phase must define Java test commands for the solve phase.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the first source-generating runtime/session/event task.
- Open decisions or blockers: exact class list, package layout, tests, and
  documentation updates belong to `/plan-task t004_01`.
- Next recommended phase: `/plan-task t004_01`.
