# T004_01_03 Define Session Core Contracts

Parent: `T004_01_implement_runtime_session_event_core`

Status: specified

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

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns session aggregate contracts only.
- Open decisions or blockers: depends on `T004_01_01` and `T004_01_02`.
- Next recommended phase: `/plan-task T004_01_03` after dependencies are solved.
