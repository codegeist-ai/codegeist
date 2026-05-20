# T004_07 Implement Storage Session Continuation Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

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

## Acceptance Criteria

- Selected in-memory storage and session continuation behavior is implemented and
  tested.
- Architecture docs describe implemented storage package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the storage/session-continuation implementation task.
- Open decisions or blockers: exact storage port set and continuation semantics
  belong to `/plan-task t004_07`.
- Next recommended phase: `/plan-task t004_07` after `T004_01` is planned or solved
  enough to provide runtime/session dependencies.
