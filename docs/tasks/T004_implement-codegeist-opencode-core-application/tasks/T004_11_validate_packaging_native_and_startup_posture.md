# T004_11 Validate Packaging Native And Startup Posture

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Validate packaging, startup, JVM jar behavior, native-image posture, and binary
smoke readiness for the implemented core.

## Context

This task consumes `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
and `docs/developer/specification/native-packaging-posture.md` after core behavior
exists.

## Scope

- Validate current `task build`, `task run`, `task native`, startup behavior,
  binary smoke posture, and packaging docs selected by the plan.
- Add or update tests, scripts, docs, or small build configuration only when the
  plan explicitly includes them.

## Non-Goals

- Do not deploy releases, add signing keys, store secrets, or require every future
  feature to be native-compatible before reporting current posture.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
- `docs/developer/specification/native-packaging-posture.md`
- Finalized implementation tasks needed for a meaningful binary smoke check

## Planning Requirements

- Create `docs/developer/implementation/packaging-native-startup-validation.md`.
- Include UML diagrams only for classes touched by planned packaging or startup
  changes.
- Define startup timing, jar smoke, native status, and skip/failure reporting.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/packaging-native-startup-validation.md`.

Planned solve-phase optional diagnostic package:

- `ai.codegeist.diagnostic`

Planned solve-phase optional tests:

- `PackagingCheckResultTests`
- `NativeBlockerClassificationTests`

## Spring AI Agent Utils Equivalent

- Closest equivalent: none beyond treating the Agent Utils dependency itself as a
  packaging, startup, classpath, and native-image risk.
- Classification: no focused Agent Utils equivalent.
- Specification consequence: planning should verify or classify Agent Utils impact
  only when the implemented core actually uses it; Agent Utils is not a packaging
  helper or startup validation framework.

## Acceptance Criteria

- Packaging and startup posture is verified with explicit `passed`, `skipped`, or
  `failed` results where applicable.
- Architecture and developer docs reflect the validated state.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned packaging, startup, and smoke commands.

## Specification Check Result

- Phase command: `/specify-task T004_11` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_10`, specified `T004_12`, and the
  build/native strategy documents named in direct inputs.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should run only once
  enough implemented behavior exists to make startup and binary smoke checks
  meaningful.
- Result: confirmed as the packaging, startup, JVM jar, native-image posture, and
  binary-smoke validation task, with explicit `passed`, `skipped`, or `failed`
  reporting.
- Open decisions or blockers: exact binary smoke matrix, startup timing budget,
  native skip/failure criteria, build changes, scripts, and commands belong to
  `/plan-task t004_11`.
- Next recommended phase: `/plan-task t004_11` after enough core behavior exists.

## Planning Check Result

- Phase command: `/plan-task T004_11` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing packaging/native/startup validation handoff
  was present.
- Result: created
  `docs/developer/implementation/packaging-native-startup-validation.md` with the
  validation ladder, optional diagnostic class diagram, file map, solve commands,
  pass/skip/fail reporting rules, dependencies, deferrals, and documentation
  targets.
- Open decisions or blockers: solve depends on enough implemented behavior to make
  startup smoke meaningful; native checks may be skipped with concrete reason.
- Next recommended phase: `/solve-task t004_11` after enough core behavior exists.

## Agent Utils Equivalence Specification Result

- Phase command: `/specify-task T004_11` during the full T004 Agent Utils
  equivalence pass.
- Context or instructions considered: user requested a suitable Spring AI Agent
  Utils equivalent for every T004 task.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Third-party evidence considered: Agent Utils analysis report dependency and
  native/startup risk notes.
- Upstream phase dependency: none for specification; existing planning must be
  rechecked only if earlier solves adopt Agent Utils utilities.
- Result: no Agent Utils packaging equivalent was selected.
- Open decisions or blockers: `/plan-task t004_11` must decide whether Agent Utils
  is an actual dependency under test or only a deferred adoption note.
- Next recommended phase: `/plan-task t004_11` after enough core behavior exists.

## Agent Utils Planning Recheck Result

- Phase command: `/plan-task T004_11` during the full T004 Agent Utils planning
  recheck.
- Context or instructions considered: user requested planning for all T004 tasks
  after the Agent Utils equivalence scan.
- Selected option: keep the packaging/native/startup validation plan and add Agent
  Utils only as a dependency impact to report if earlier solve phases adopt it.
- Duplicate check result:
  `docs/developer/implementation/packaging-native-startup-validation.md` already
  exists and remains the authoritative solve handoff.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read: T004 parent, adjacent child tasks, the existing
  implementation handoff, build/release strategy, native packaging posture, and
  Agent Utils analysis report.
- Upstream phase dependency: satisfied by the Agent Utils equivalence
  specification result in this task.
- Result: no target files, optional diagnostics, or smoke commands changed; solve
  must classify Agent Utils as actual dependency impact only if it is used by
  earlier implementation tasks.
- Open decisions or blockers: none at planning depth; solve still depends on enough
  implemented core behavior for meaningful smoke checks.
- Next recommended phase: `/solve-task t004_11` after enough core behavior exists.
