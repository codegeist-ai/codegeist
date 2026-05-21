# T004_12 Validate Core Replacement Readiness

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Validate whether the implemented Codegeist core can replace OpenCode for the
selected CLI/TUI-oriented core workflows.

## Context

This is the final T004 readiness task. It depends on implemented core behavior,
workflow parity validation, and packaging/startup posture.

## Scope

- Validate the selected replacement-readiness workflow set.
- Summarize remaining gaps and decide whether they block readiness.
- Update architecture, developer docs, and task memory with the current truth.

## Non-Goals

- Do not claim readiness for deferred JBang, PF4J, Vaadin, headless server, API,
  SDK/OpenAPI, or broad TUI behavior unless later tasks implement and validate
  those surfaces.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- Finalized `T004_01` through `T004_11`

## Planning Requirements

- Create `docs/developer/implementation/core-replacement-readiness-validation.md`.
- Include UML diagrams only for classes touched by readiness fixes.
- Define readiness scenarios, acceptance gates, test commands, and gap reporting.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/core-replacement-readiness-validation.md`.

Planned solve-phase test package:

- `ai.codegeist.readiness`

Planned solve-phase tests:

- `CoreReplacementReadinessTests`
- `ReadinessReportTests`

## Acceptance Criteria

- Readiness is reported as passed, skipped with reasons, or failed with blockers
  for each selected workflow.
- Remaining gaps are documented as follow-up tasks instead of hidden in prose.
- Architecture and memory reflect the final T004 outcome.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned readiness checks and affected broad verification.

## Specification Check Result

- Phase command: `/specify-task T004_12` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_10`, specified `T004_11`, and all
  earlier T004 implementation tasks as final readiness dependencies.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should consume
  finalized implementation, parity, and packaging/startup results before readiness
  claims are made.
- Result: confirmed as the final replacement-readiness validation task for selected
  CLI/TUI-oriented core workflows, not deferred JBang, PF4J, Vaadin, server, API,
  SDK, or broad TUI readiness.
- Open decisions or blockers: exact readiness matrix, pass/skip/fail criteria,
  gap follow-up format, memory updates, and affected broad verification belong to
  `/plan-task t004_12`.
- Next recommended phase: `/plan-task t004_12` after `T004_11` is finalized.

## Planning Check Result

- Phase command: `/plan-task T004_12` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing core replacement readiness validation handoff
  was present.
- Result: created
  `docs/developer/implementation/core-replacement-readiness-validation.md` with
  readiness scenarios, class diagram, file map, implementation steps, test
  commands, pass/skip/fail criteria, dependencies, deferrals, and documentation
  targets.
- Open decisions or blockers: solve depends on finalized `T004_01` through
  `T004_11`; readiness claims must not exceed implemented and verified behavior.
- Next recommended phase: `/solve-task t004_12` after `T004_11` is finalized.
