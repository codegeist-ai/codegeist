# T004_07 Implement Storage Session Continuation Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Implement in-memory-first storage ports and session continuation core with TDD.

## Context

This task consumes `docs/developer/specification/storage-session-continuation-source-generation-contract.md`
and depends on runtime/session/event core from `T004_01`.

## Scope

- Implement session continuation identity, in-memory storage ports, create,
  continue, list, update, delete/archive behavior selected by the plan, message and
  event projection stores, artifact references, redaction/retention posture, typed
  storage failures, and storage health reporting.
- Add tests for continuation, projection, redaction, failure, and health behavior.

## Non-Goals

- Do not implement file-backed persistence, databases, migrations, encryption,
  event sourcing, durable audit logs, CLI/TUI behavior, provider behavior, tool
  behavior, patch/edit behavior, or shell behavior unless later tasks own them.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/storage-session-continuation-source-generation-contract.md`
- `docs/developer/specification/storage-port-posture.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`

## Planning Requirements

- Create `docs/developer/implementation/storage-session-continuation-core-implementation.md`.
- Include an UML class diagram for all storage/session-continuation classes and
  tests.
- Define in-memory store tests and skip criteria for file-backed persistence.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/storage-session-continuation-core-implementation.md`.

Planned solve-phase target package:

- `ai.codegeist.storage`

Planned solve-phase tests:

- `StorageSessionContinuationContractTests`
- `InMemoryStorageTests`
- `StorageBoundaryDependencyTests`

## Acceptance Criteria

- Selected in-memory storage and session continuation behavior is implemented and
  tested.
- Architecture docs describe implemented storage package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_07` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: planned `T004_01`, specified `T004_06`, and
  specified `T004_08`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should consume the
  runtime/session/event handoff and keep storage behind replaceable ports.
- Result: confirmed as the in-memory-first storage ports and session continuation
  implementation task, with file-backed persistence and event sourcing deferred.
- Open decisions or blockers: exact storage port set, continuation semantics,
  redaction rules, health reporting, class list, tests, and narrow Maven commands
  belong to `/plan-task t004_07`.
- Next recommended phase: `/plan-task t004_07` after `T004_01` is planned or
  solved enough to provide runtime/session dependencies.

## Planning Check Result

- Phase command: `/plan-task T004_07` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing storage/session-continuation implementation
  handoff was present.
- Result: created
  `docs/developer/implementation/storage-session-continuation-core-implementation.md`
  with the class diagram, file map, implementation steps, in-memory TDD commands,
  dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve should wait until `T004_01` provides
  runtime/session/event source types; file-backed persistence remains deferred.
- Next recommended phase: `/solve-task t004_07` after `T004_01` is solved.
