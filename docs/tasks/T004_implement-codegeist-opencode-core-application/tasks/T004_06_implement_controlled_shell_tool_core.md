# T004_06 Implement Controlled Shell Tool Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Implement controlled shell tool request/result contracts and safe executor boundary
with TDD.

## Context

This task consumes `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`
and the finalized tool/permission/workspace core from `T004_04`.

## Scope

- Implement shell request identity, command shapes, command purpose, destructive
  posture, Plan-mode denial, Build-mode approval handoff, workspace cwd
  validation, env/stdin policy, timeout/cancellation shapes, typed shell failures,
  bounded stdout/stderr summaries, output references, and fake executor boundary
  selected by the plan.
- Add tests for denial, validation, fake execution, timeout classification, and
  bounded output.

## Non-Goals

- Do not run arbitrary real shell commands before the plan defines safe fixtures.
- Do not implement terminal UI, PTY support, remote execution, JBang execution,
  storage persistence, or provider callbacks.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`
- `docs/developer/specification/shell-verification-contracts.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_04_implement_tool_permission_workspace_core.md`

## Planning Requirements

- Create `docs/developer/implementation/controlled-shell-tool-core-implementation.md`.
- Include an UML class diagram for all shell classes and tests.
- Define safe fake-executor and optional real-process smoke tests.

## Acceptance Criteria

- Selected controlled shell core is implemented and tested without unsafe process
  execution by default.
- Architecture docs describe implemented shell package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the controlled shell implementation task.
- Open decisions or blockers: safe process-fixture depth belongs to
  `/plan-task t004_06`.
- Next recommended phase: `/plan-task t004_06` after `T004_04` is planned or solved
  enough to provide policy dependencies.
