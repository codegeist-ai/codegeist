# T004_11 Validate Packaging Native And Startup Posture

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

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

## Acceptance Criteria

- Packaging and startup posture is verified with explicit `passed`, `skipped`, or
  `failed` results where applicable.
- Architecture and developer docs reflect the validated state.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned packaging, startup, and smoke commands.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as packaging/native/startup validation.
- Open decisions or blockers: exact binary smoke matrix belongs to
  `/plan-task t004_11`.
- Next recommended phase: `/plan-task t004_11` after enough core behavior exists.
