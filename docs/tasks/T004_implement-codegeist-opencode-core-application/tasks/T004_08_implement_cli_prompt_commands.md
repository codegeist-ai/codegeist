# T004_08 Implement CLI Prompt Commands

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Implement Spring Shell `plan` and `build` prompt commands over finalized runtime
APIs with TDD.

## Context

This task consumes `docs/developer/specification/cli-prompt-command-source-generation-contract.md`
and depends on runtime/session/event core from `T004_01`.

## Scope

- Implement the first CLI adapter package, Spring Shell command class or classes,
  prompt/session input parsing, runtime delegation, stable accepted/submitted
  output, adapter errors, and tests selected by the plan.
- Keep CLI as a runtime client, not an owner of sessions, provider calls, tools,
  permissions, context loading, storage, patch/edit, shell, or TUI behavior.

## Non-Goals

- Do not implement end-to-end provider responses, full TUI, command templates,
  async/server flows, shell commands, tool execution, storage continuation, or live
  model calls in this task.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/cli-prompt-command-source-generation-contract.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`

## Planning Requirements

- Create `docs/developer/implementation/cli-prompt-command-implementation.md`.
- Include an UML class diagram for all CLI adapter, command, mapper, output, fake
  runtime, and test classes.
- Define Spring Shell command registration tests and adapter tests.

## Acceptance Criteria

- `plan` and `build` command behavior selected by the plan is implemented and
  tested.
- Architecture docs describe implemented CLI package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the CLI prompt command implementation task.
- Open decisions or blockers: exact Spring Shell command shape and tests belong to
  `/plan-task t004_08`.
- Next recommended phase: `/plan-task t004_08` after `T004_01` is planned or solved
  enough to provide runtime APIs.
