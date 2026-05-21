# T004_10 Validate OpenCode Parity CLI Workflows

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Validate selected OpenCode-style CLI workflows against the implemented Codegeist
core and close small parity gaps with tests and documentation.

## Context

This task depends on the end-to-end loop from `T004_09` and uses OpenCode as a
feature and behavior reference, not an implementation blueprint.

## Scope

- Select and verify concrete CLI workflows from the parity docs.
- Add tests, docs, and small implementation corrections when planned.
- Record gaps as follow-up tasks when they exceed the planned slice.

## Non-Goals

- Do not implement deferred JBang, PF4J, Vaadin, headless server, API, SDK, or full
  TUI behavior.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- Finalized `T004_09`

## Planning Requirements

- Create `docs/developer/implementation/opencode-parity-cli-workflow-validation.md`.
- Include UML diagrams only for classes touched or added by planned parity fixes.
- Define workflow tests and smoke commands.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/opencode-parity-cli-workflow-validation.md`.

Planned solve-phase test package:

- `ai.codegeist.parity`

Planned solve-phase tests:

- `OpenCodeParityCliWorkflowTests`
- `CliWorkflowSmokeTests`
- `ParityGapReportTests`

## Acceptance Criteria

- Selected workflows are verified or gaps are recorded with precise follow-ups.
- Architecture and developer docs reflect implemented parity behavior.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned workflow and smoke tests.

## Specification Check Result

- Phase command: `/specify-task T004_10` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_09`, specified `T004_11`, and
  `docs/developer/specification/codegeist-opencode-parity.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should consume the
  solved end-to-end loop and use OpenCode as a behavior reference only.
- Result: confirmed as the selected CLI workflow parity validation and small-gap
  closure task, not a deferred-surface implementation task.
- Open decisions or blockers: exact workflow list, parity evidence, acceptable
  small fixes, smoke checks, and follow-up gap boundaries belong to
  `/plan-task t004_10`.
- Next recommended phase: `/plan-task t004_10` after `T004_09` is solved.

## Planning Check Result

- Phase command: `/plan-task T004_10` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing parity workflow validation handoff was
  present.
- Result: created
  `docs/developer/implementation/opencode-parity-cli-workflow-validation.md` with
  selected workflows, class diagram, file map, implementation steps, workflow test
  commands, dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve depends on solved `T004_09`; broader parity
  gaps must become follow-up task notes instead of hidden scope expansion.
- Next recommended phase: `/solve-task t004_10` after `T004_09` is solved.
