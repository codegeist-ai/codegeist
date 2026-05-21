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
