# T002_01 Align Codegeist Build Baseline

Parent: `T002_define-codegeist-mvp-foundation-blueprints`

Source: `docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_01_define-technology-baseline.md`

## Goal

Align the Codegeist CLI build baseline under `app/codegeist/cli` with the
architecture decision to target Spring Boot `3.5.x` and Spring AI `1.1.x` while
preserving Java `25`, Spring Shell, Maven, and GraalVM posture.

## Context

`T001_01` selected a Java-first baseline and made Spring AI stability more
important than adopting Spring Boot 4 early. The implementation backlog lists
build baseline alignment as the first follow-up slice. A later specification pass
also selected the first physical layout refinement: the CLI bootstrap should live
under `app/codegeist/cli` before runtime vocabulary work adds more source files.

The current bootstrap is intentionally small:

- `app/codegeist/pom.xml` originally used `spring-boot-starter-parent` `4.0.3`,
  Java `25`, Spring Shell `4.0.1`, and the GraalVM native Maven plugin.
- At the planning pass, the active worktree already contains an uncommitted
  candidate alignment to Spring Boot `3.5.14`, Spring AI `1.1.6` dependency
  management, and Spring Shell `3.4.2`.
- The tracked CLI project now lives under `app/codegeist/cli` and exposes `test`,
  `build`, `native`, and `run` through `app/codegeist/cli/Taskfile.yml`.
- The application has a Spring Boot entrypoint and one context-load test.

## Concrete Solution

Update the Maven build so it can use the architecture baseline without expanding
runtime features yet:

1. Change the Spring Boot parent to `3.5.14` unless verification exposes a
   concrete compatibility blocker.
2. Import Spring AI `1.1.6` dependency management to prove the BOM posture while
   avoiding provider starters and model calls.
3. Use Spring Shell `3.4.2` with the selected Spring Boot line and Java `25`
   unless verification proves the version combination is invalid.
4. Keep the current Java `25` property unless verification proves it is
   incompatible; if it fails, document the compatibility blocker instead of
   silently changing Java.
5. Keep the existing GraalVM native profile present, but treat native-image
   success as a posture check rather than a required full feature guarantee.
6. Move the tracked CLI Maven project from `app/codegeist` to
   `app/codegeist/cli` without moving generated `target/` output or logs.
7. Update only minimal docs or comments that would otherwise become stale.

## Scope

- `app/codegeist/cli/pom.xml` dependency and plugin baseline alignment.
- `app/codegeist/cli/Taskfile.yml` only if command names or verification flags
  need a small compatibility adjustment.
- Tracked source and resource files moved under `app/codegeist/cli/src/`.
- Existing context-load test updates only when required by the dependency
  baseline change.
- Brief documentation update if the implemented versions differ from the exact
  architecture assumption.

## Target Files And Packages

- `app/codegeist/cli/pom.xml`
- `app/codegeist/cli/Taskfile.yml`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/CodegeistApplicationTests.java`
  only if the context-load test needs compatibility changes
- `docs/developer/specification/codegeist-opencode-parity.md` only if verification changes the
  recorded baseline decision or open risk

## Acceptance Criteria

- `app/codegeist/cli` no longer uses Spring Boot `4.0.3` as its target baseline.
- The tracked CLI project lives under `app/codegeist/cli` before later runtime,
  server, deployment, or multi-module work begins.
- The selected Spring Boot `3.5.x`, Spring AI `1.1.x` posture, Spring Shell,
  Java `25`, Maven, and GraalVM profile are intentionally aligned or explicitly
  documented as blocked.
- No provider integration, runtime contracts, tools, permissions, storage, UI,
  or plugin behavior is implemented.
- Existing Spring Boot context-load coverage still passes, or the task records a
  precise compatibility blocker with the narrowest failing command.
- The task leaves a clear baseline for the next runtime/session/event contract
  task.

## Verification

Run from `app/codegeist/cli` unless noted otherwise:

```bash
task test
task build
```

If the JVM build passes quickly, also run a native posture check when practical:

```bash
task native
```

If native compilation is too slow or fails because of known early-stage native
constraints, record the exact blocker and keep JVM test/build as the required
baseline verification.

Always run from the repository root before finishing:

```bash
git --no-pager diff --check
```

## Dependencies

- Depends on the architecture decision in `T001_01` and the implementation
  backlog in `docs/developer/specification/codegeist-opencode-parity.md`.
- Should be solved before provider integration and before assuming Spring AI APIs
  in runtime, tool, or provider tasks.

## Non-Goals

- Do not implement Spring AI providers or model calls.
- Do not introduce runtime/session/event contracts.
- Do not add tool, permission, workspace, patch/edit, shell, storage, server,
  Vaadin, PF4J, or JBang runtime behavior.
- Do not split Maven modules or add a Maven parent aggregator.
- Do not change the Java baseline away from `25` without recording the concrete
  compatibility reason and follow-up decision need.

## Open Questions

- None for the implementation slice. The plan selects Spring Boot `3.5.14`,
  Spring AI `1.1.6`, and Spring Shell `3.4.2`; `/solve-task` should verify or
  record the exact blocker.
- Native-image remains a posture check in this task. A deep native packaging pass
  belongs to `T002_11_validate_native_packaging_posture.md`.

## Specification Check Result

- Rechecked with the T002 parent default hints.
- Scope remains the right first implementation slice because provider and runtime
  work should not assume Spring Boot `4.0.3` or unverified Spring AI alignment.
- No OpenCode source lookup is required before implementation unless dependency
  choices need comparison with OpenCode packaging behavior.
- Rechecked after completion with the user request to add a component diagram for
  future Codegeist components, possible Maven modules, server authentication,
  model access, global command/skill customization storage, and deployment
  artifacts such as Helm charts.
- The new component-diagram request is recorded as target architecture context in
  `docs/developer/specification/codegeist-opencode-parity.md`, not as new implementation scope
  for this completed build-baseline task.
- Rechecked again with the user decision to include only the CLI project move in
  this task. Moving the tracked CLI Maven project to `app/codegeist/cli` is now
  in scope because it stabilizes file paths before `T002_02` adds package
  vocabulary. Adding `app/codegeist/server`, introducing authentication, storing
  global customizations, or adding Helm charts remains out of scope.

## Plan Workflow Handoff

- Phase: `/plan-task` run for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_01_align-codegeist-build-baseline.md`.
- Source task considered:
  `docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_01_define-technology-baseline.md`.
- Source parent considered:
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`.
- User focus: plan the referenced existing implementation task; no extra context
  or variant was provided.
- Selected option: sharpen the existing build-baseline implementation task instead
  of creating a duplicate top-level task.
- Duplicate check result: this file is already the matching concrete
  implementation task under the active `T002` parent.
- Hints considered:
  `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md` from the T002 parent.
- Related context files read:
  `docs/developer/specification/codegeist-opencode-parity.md`, `app/codegeist/cli/pom.xml`,
  `app/codegeist/cli/Taskfile.yml`, and
  `app/codegeist/cli/src/test/java/ai/codegeist/app/CodegeistApplicationTests.java`.
- Current worktree observation: the build baseline aligns
  `app/codegeist/cli/pom.xml` and the parity document to Spring Boot `3.5.14`,
  Spring AI `1.1.6`, and Spring Shell `3.4.2`.
- Recommended next command: solve `T002_02`.

## Planning Note

Status: planned.

This pass confirmed the task is already the correct first implementation slice for
the MVP foundation. The implementation should stay narrow: finalize the Maven
baseline alignment, keep Java `25`, avoid provider starters or model calls, keep
Taskfile command names stable unless verification requires a tiny compatibility
adjustment, and update only documentation that would otherwise contradict the
verified build baseline.

The required verification is `task test` and `task build` from `app/codegeist/cli`,
followed by `git --no-pager diff --check` from the repository root. Run
`task native` only as a practical posture check; do not let native-image issues
expand this task beyond the build-baseline decision.

## Phase Status

- `/specify-task` dependency: satisfied by the existing `Specification Check
  Result`; no additional specification pass is needed before solving.
- `/plan-task` result: planned and sharpened this existing task with exact target
  versions, duplicate-check outcome, hint discovery, related files, and solve
  verification boundaries.
- `/solve-task` result: completed. The existing build baseline now verifies with
  Spring Boot `3.5.14`, Spring AI `1.1.6` BOM posture, Spring Shell `3.4.2`,
  Java `25`, Maven, and the existing GraalVM native profile.
- `/specify-task` repeat result: completed after the user requested a future
  component diagram. The request is clarified as target architecture context and
  explicitly not a reopened implementation scope for this task.
- `/specify-task` repeat result: reopened for a narrow CLI layout baseline after
  the user explicitly requested moving the current project from `app/codegeist`
  to `app/codegeist/cli` before continuing.
- Hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`, and the local
  Codegeist task and architecture-documentation overlays.
- Decisions remaining: none for this slice.
- Next dependency: `T002_02_introduce-runtime-vocabulary-contracts.md`.

## Solution Note

Status: completed.

The solution pass confirmed that the active Maven baseline already matches the
planned architecture: Spring Boot parent `3.5.14`, Spring AI BOM `1.1.6` for
dependency management only, Spring Shell `3.4.2`, Java `25`, and the existing
GraalVM native Maven profile. No provider starters, model calls, runtime
contracts, tools, permissions, storage, UI, or plugin behavior were added.

Verification passed from `app/codegeist/cli` with `task test`, `task build`, and
the optional native posture check `task native`. The native build completed
successfully and produced the GraalVM executable under
`app/codegeist/cli/target/codegeist`.

The task was later reopened for the narrow layout baseline move from
`app/codegeist` to `app/codegeist/cli`. The moved CLI project now produces its jar
and native executable under `app/codegeist/cli/target/`.

## Creation Note

Status: completed.

Created interactively from `T001_01_define-technology-baseline.md`. The user
selected the `Build baseline` option over narrower Java-compatibility or
native-posture tasks. This task intentionally created the first implementation
slice under the `T002` MVP foundation parent and was solved before provider or
runtime contract implementation begins.
