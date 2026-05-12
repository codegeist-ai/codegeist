# T002_01 Align Codegeist Build Baseline

Parent: `T002_implement-codegeist-mvp-foundation`

Source: `docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_01_define-technology-baseline.md`

## Goal

Align the `app/codegeist` build baseline with the architecture decision to target
Spring Boot `3.5.x` and Spring AI `1.1.x` while preserving Java `25`, Spring
Shell, Maven, and GraalVM posture.

## Context

`T001_01` selected a Java-first baseline and made Spring AI stability more
important than adopting Spring Boot 4 early. The parity architecture now states
that the current `app/codegeist/pom.xml` still uses Spring Boot `4.0.3`, and the
implementation backlog lists build baseline alignment as the first follow-up
slice.

The current bootstrap is intentionally small:

- `app/codegeist/pom.xml` uses `spring-boot-starter-parent` `4.0.3`, Java `25`,
  Spring Shell `4.0.1`, and the GraalVM native Maven plugin.
- `app/codegeist/Taskfile.yml` exposes `test`, `build`, `native`, and `run`.
- The application has a Spring Boot entrypoint and one context-load test.

## Concrete Solution

Update the Maven build so it can use the architecture baseline without expanding
runtime features yet:

1. Change the Spring Boot parent to a compatible `3.5.x` release.
2. Add Spring AI `1.1.x` dependency management only if it is needed to verify BOM
   compatibility in this slice; do not add provider starters or model calls.
3. Choose a Spring Shell version that works with the selected Spring Boot line and
   Java `25`.
4. Keep the current Java `25` property unless verification proves it is
   incompatible; if it fails, document the compatibility blocker instead of
   silently changing Java.
5. Keep the existing GraalVM native profile present, but treat native-image
   success as a posture check rather than a required full feature guarantee.
6. Update only minimal docs or comments that would otherwise become stale.

## Scope

- `app/codegeist/pom.xml` dependency and plugin baseline alignment.
- `app/codegeist/Taskfile.yml` only if command names or verification flags need a
  small compatibility adjustment.
- Existing context-load test updates only when required by the dependency
  baseline change.
- Brief documentation update if the implemented versions differ from the exact
  architecture assumption.

## Target Files And Packages

- `app/codegeist/pom.xml`
- `app/codegeist/Taskfile.yml`
- `app/codegeist/src/test/java/ai/codegeist/app/CodegeistApplicationTests.java`
  only if the context-load test needs compatibility changes
- `docs/developer/codegeist-opencode-parity.md` only if verification changes the
  recorded baseline decision or open risk

## Acceptance Criteria

- `app/codegeist` no longer uses Spring Boot `4.0.3` as its target baseline.
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

Run from `app/codegeist` unless noted otherwise:

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
  backlog in `docs/developer/codegeist-opencode-parity.md`.
- Should be solved before provider integration and before assuming Spring AI APIs
  in runtime, tool, or provider tasks.

## Non-Goals

- Do not implement Spring AI providers or model calls.
- Do not introduce runtime/session/event contracts.
- Do not add tool, permission, workspace, patch/edit, shell, storage, server,
  Vaadin, PF4J, or JBang runtime behavior.
- Do not split Maven modules.
- Do not change the Java baseline away from `25` without recording the concrete
  compatibility reason and follow-up decision need.

## Open Questions

- Which exact Spring Boot `3.5.x`, Spring AI `1.1.x`, and Spring Shell versions
  are mutually compatible with Java `25` in the current Maven setup?
- Is `task native` practical enough for this slice, or should native-image remain
  a documented later smoke check after JVM test/build passes?

## Specification Check Result

- Rechecked with the T002 parent default hints.
- Scope remains the right first implementation slice because provider and runtime
  work should not assume Spring Boot `4.0.3` or unverified Spring AI alignment.
- No OpenCode source lookup is required before implementation unless dependency
  choices need comparison with OpenCode packaging behavior.

## Creation Note

Status: open.

Created interactively from `T001_01_define-technology-baseline.md`. The user
selected the `Build baseline` option over narrower Java-compatibility or
native-posture tasks. This task intentionally creates the first implementation
slice under the `T002` MVP foundation parent and should be solved with
`/solve-task T002_01` before provider or runtime contract implementation begins.
