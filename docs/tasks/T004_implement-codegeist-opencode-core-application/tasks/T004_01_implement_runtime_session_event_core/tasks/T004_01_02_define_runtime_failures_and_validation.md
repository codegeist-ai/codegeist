# T004_01_02 Define Runtime Failures And Validation

Parent: `T004_01_implement_runtime_session_event_core`

Status: specified

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

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns runtime failures and validation only.
- Open decisions or blockers: depends on `T004_01_01` implementation.
- Next recommended phase: `/plan-task T004_01_02` after `T004_01_01` is solved.
