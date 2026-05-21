# T004_05 Implement Patch Edit Proposal Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

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

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/patch-edit-proposal-core-implementation.md`.

Planned solve-phase target package:

- `ai.codegeist.patch`

Planned solve-phase tests:

- `PatchEditProposalContractTests`
- `PatchEditApprovalBindingTests`
- `PatchEditBoundaryDependencyTests`

## Acceptance Criteria

- Selected patch/edit proposal core is implemented and tested.
- Architecture docs describe implemented patch package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_05` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_04` and specified `T004_06`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should depend on the
  `T004_04` policy handoff and must not redefine tool/permission/workspace
  ownership.
- Result: confirmed as the reviewable patch/edit proposal and apply-result core
  implementation task, not a broad formatter, rollback, or file-mutation task.
- Open decisions or blockers: exact apply behavior, proposal freshness rules,
  approval binding, class list, tests, and narrow Maven commands belong to
  `/plan-task t004_05`.
- Next recommended phase: `/plan-task t004_05` after `T004_04` is planned or
  solved enough to provide policy dependencies.

## Planning Check Result

- Phase command: `/plan-task T004_05` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing patch/edit implementation handoff was present.
- Result: created
  `docs/developer/implementation/patch-edit-proposal-core-implementation.md` with
  the class diagram, file map, implementation steps, TDD commands, dependencies,
  deferrals, and documentation targets.
- Open decisions or blockers: solve should wait until `T004_04` provides policy,
  permission, workspace target, and output-ref contracts.
- Next recommended phase: `/solve-task t004_05` after `T004_04` is solved enough
  to provide policy dependencies.
