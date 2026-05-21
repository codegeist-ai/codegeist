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

## Spring AI Agent Utils Equivalent

- Closest equivalent: the overall Agent Utils adoption boundary guide and T004
  equivalence matrix in the parent task.
- Classification: evidence input, not a runtime readiness substitute.
- Specification consequence: readiness must report which Agent Utils candidates
  were adopted directly, wrapped, deferred, rejected, or unused, but readiness
  cannot depend on raw Agent Utils architecture bypassing Codegeist boundaries.

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

## Agent Utils Equivalence Specification Result

- Phase command: `/specify-task T004_12` during the full T004 Agent Utils
  equivalence pass.
- Context or instructions considered: user requested a suitable Spring AI Agent
  Utils equivalent for every T004 task.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Third-party evidence considered:
  `docs/developer/spring-ai-agent-utils-adoption.md` and the parent T004 Agent
  Utils equivalence matrix.
- Upstream phase dependency: none for specification; existing planning must be
  rechecked so readiness reporting includes the final Agent Utils adoption state.
- Result: classified Agent Utils adoption as readiness evidence only, not a
  replacement-readiness shortcut.
- Open decisions or blockers: `/plan-task t004_12` must define how to report Agent
  Utils direct use, adapters, deferrals, and rejected candidates after all prior
  T004 solves finish.
- Next recommended phase: `/plan-task t004_12` after `T004_11` is finalized.

## Agent Utils Planning Recheck Result

- Phase command: `/plan-task T004_12` during the full T004 Agent Utils planning
  recheck.
- Context or instructions considered: user requested planning for all T004 tasks
  after the Agent Utils equivalence scan.
- Selected option: keep the readiness validation plan and require its readiness
  report to summarize final Agent Utils adoption outcomes from all earlier T004
  tasks.
- Duplicate check result:
  `docs/developer/implementation/core-replacement-readiness-validation.md` already
  exists and remains the authoritative solve handoff.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read: T004 parent, adjacent child tasks, the existing
  implementation handoff, Codegeist/OpenCode parity docs, Agent Utils adoption
  guide, and the parent T004 equivalence matrix.
- Upstream phase dependency: satisfied by the Agent Utils equivalence
  specification result in this task.
- Result: no target files, class diagram, or readiness tests changed; solve must
  include Agent Utils direct-use, adapter, deferred, rejected, and unused outcomes
  in readiness evidence without weakening Codegeist boundaries.
- Open decisions or blockers: none at planning depth; solve still depends on
  finalized `T004_01` through `T004_11`.
- Next recommended phase: `/solve-task t004_12` after `T004_11` is finalized.
