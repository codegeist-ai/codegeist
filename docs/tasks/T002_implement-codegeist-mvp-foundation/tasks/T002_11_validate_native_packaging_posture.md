# T002_11 Describe Native Packaging Posture

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_20`, `T001_01`, `T001_24`, `T001_25`

## Goal

Describe the JVM and GraalVM native packaging posture after the build baseline and
core contract blueprints exist, without running packaging as the task's main
output.

## Context

Codegeist targets Java/GraalVM packaging instead of OpenCode's Bun/Node shape.
The core CLI/runtime should remain native-aware, while PF4J, Vaadin, JBang, and
some provider integrations may remain JVM-first until proven.

## Concrete Solution

1. Create or update `docs/developer/native-packaging-posture.md` as the JVM/native
   packaging posture blueprint.
2. Describe which packaging checks a later implementation or release task should
   run for JVM jar startup and GraalVM native profile validation.
3. Define how native blockers should be recorded when dependency hints,
   reflection configuration, dynamic loading, provider libraries, PF4J, JBang,
   Vaadin, or server surfaces fail.
4. Keep native posture focused on bootstrap/core contracts, not later plugin or UI
   features.
5. Include future verification commands, decision gates, diagrams if useful, and
   illustrative configuration snippets in markdown only.

## Scope

- `docs/developer/native-packaging-posture.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/codegeist-opencode-parity.md` only if native/JVM-only posture
  changes architecture decisions
- this task file

## Acceptance Criteria

- JVM packaging verification posture is documented for the selected baseline.
- Native-image status reporting is specified as passed, skipped with reason, or
  failed with a concrete blocker for later verification tasks.
- PF4J, Vaadin, JBang, server, and provider-native compatibility are not assumed.
- The task does not broaden runtime features only to satisfy native packaging.
- No build files, Taskfile commands, Java source, or tests are changed by this
  task unless the user explicitly reopens it as implementation work.

## Verification

```bash
git --no-pager diff --check
```

`task build` and `task native` may be listed as future packaging verification
commands inside the document, but they are not required for this documentation
slice unless build files change.

## Dependencies

- Depends on `T002_01` and should run after `T002_03` if native hints depend on
  initial runtime/session/event contracts.

## Non-Goals

- Do not create Java source files, empty package directories, tests, Taskfile
  commands, or build configuration changes.
- Do not make PF4J, JBang, Vaadin, broad providers, LSP, or server mode native
  compatible in this task.

## Open Questions

- Should native packaging become release-blocking for the first CLI MVP or remain
   a tracked compatibility target until provider/tool dependencies settle?

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  native-packaging posture and verification handoff for later implementation or
  release tasks instead of changing packaging behavior now.

## Specification Check Result

- Rechecked with the T002 parent default hints.
- Native validation should stay packaging-focused and must not broaden runtime,
  provider, PF4J, JBang, Vaadin, or server scope.
- The task should report native status as passed, skipped with reason, or failed
  with concrete blocker rather than weakening JVM build verification.

## Creation Note

Status: open.

Derived from the GraalVM constraints, technology baseline, risk register, and
implementation backlog as one packaging posture documentation/specification slice.
