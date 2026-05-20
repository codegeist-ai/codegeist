# T004_05 Implement Patch Edit Proposal Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Implement reviewable patch/edit proposal and apply-result core with TDD.

## Context

This task consumes `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`
and the finalized tool/permission/workspace core from `T004_04`.

## Scope

- Implement proposal identity, target summaries, patch hunk summaries, text
  replacement summaries, freshness metadata, exact approval binding, typed apply
  failures, bounded summaries, output references, and event projection selected by
  the plan.
- Add tests for review, approval binding, failure classification, and bounded
  results.

## Non-Goals

- Do not implement broad file mutation, formatters, rollback, shell execution,
  provider callbacks, storage persistence, or UI rendering unless later tasks own
  them.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`
- `docs/developer/specification/patch-edit-proposal-contracts.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_04_implement_tool_permission_workspace_core.md`

## Planning Requirements

- Create `docs/developer/implementation/patch-edit-proposal-core-implementation.md`.
- Include an UML class diagram for all patch/edit classes and tests.
- Define narrow tests for proposal identity, approval binding, and typed failures.

## Acceptance Criteria

- Selected patch/edit proposal core is implemented and tested.
- Architecture docs describe implemented patch package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the patch/edit proposal implementation task.
- Open decisions or blockers: exact apply behavior and class list belong to
  `/plan-task t004_05`.
- Next recommended phase: `/plan-task t004_05` after `T004_04` is planned or solved
  enough to provide policy dependencies.
