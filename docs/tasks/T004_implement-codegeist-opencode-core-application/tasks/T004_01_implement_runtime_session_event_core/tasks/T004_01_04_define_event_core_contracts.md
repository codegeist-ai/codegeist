# T004_01_04 Define Event Core Contracts

Parent: `T004_01_implement_runtime_session_event_core`

Status: specified

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
