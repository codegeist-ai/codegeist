# T004_01_05 Define Session Projection Core

Parent: `T004_01_implement_runtime_session_event_core`

Status: specified

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

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns session projection contracts only.
- Open decisions or blockers: depends on `T004_01_01` through `T004_01_04`.
- Next recommended phase: `/plan-task T004_01_05` after dependencies are solved.
