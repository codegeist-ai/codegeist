# T002_11 Validate Native Packaging Posture

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_20`, `T001_01`, `T001_24`, `T001_25`

## Goal

Validate the current JVM and GraalVM native packaging posture after the build and
core contract baseline exist.

## Context

Codegeist targets Java/GraalVM packaging instead of OpenCode's Bun/Node shape.
The core CLI/runtime should remain native-aware, while PF4J, Vaadin, JBang, and
some provider integrations may remain JVM-first until proven.

## Concrete Solution

1. Run the JVM jar build and inspect whether the packaged application starts for a
   minimal smoke command.
2. Run or attempt the native Maven profile when practical.
3. Record native blockers as concrete follow-up notes if dependency hints,
   reflection configuration, dynamic loading, or provider libraries fail.
4. Keep native verification focused on bootstrap/core contracts, not later plugin
   or UI features.
5. Add or adjust a narrow Taskfile smoke command only if it makes repeated
   verification clearer.

## Scope

- `app/codegeist/pom.xml`
- `app/codegeist/Taskfile.yml`
- GraalVM native profile verification notes
- docs only if native/JVM-only posture changes

## Acceptance Criteria

- JVM packaging is verified with the selected baseline.
- Native-image status is known: passed, skipped with reason, or failed with a
  concrete blocker.
- PF4J, Vaadin, JBang, and provider-native compatibility are not assumed.
- The task does not broaden runtime features only to satisfy native packaging.

## Verification

```bash
task build
task native
git --no-pager diff --check
```

If `task native` is too slow or blocked by environment constraints, record the
exact blocker and keep JVM packaging verification mandatory.

## Dependencies

- Depends on `T002_01` and should run after `T002_03` if native hints depend on
  initial runtime/session/event contracts.

## Non-Goals

- Do not make PF4J, JBang, Vaadin, broad providers, LSP, or server mode native
  compatible in this task.

## Open Questions

- Should native packaging become release-blocking for the first CLI MVP or remain
  a tracked compatibility target until provider/tool dependencies settle?

## Specification Check Result

- Rechecked with the T002 parent default hints.
- Native validation should stay packaging-focused and must not broaden runtime,
  provider, PF4J, JBang, Vaadin, or server scope.
- The task should report native status as passed, skipped with reason, or failed
  with concrete blocker rather than weakening JVM build verification.

## Creation Note

Status: open.

Derived from the GraalVM constraints, technology baseline, risk register, and
implementation backlog as one packaging validation slice.
