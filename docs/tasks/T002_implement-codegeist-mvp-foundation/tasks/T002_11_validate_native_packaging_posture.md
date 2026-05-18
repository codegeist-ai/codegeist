# T002_11 Describe Native Packaging Posture

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_20`, `T001_01`, `T001_24`, `T001_25`

Status: finalized

## Goal

Describe the JVM and GraalVM native packaging posture after the build baseline and
core contract blueprints exist, without running packaging as the task's main
output.

## Context

Codegeist targets Java/GraalVM packaging instead of OpenCode's Bun/Node shape.
The core CLI/runtime should remain native-aware, while PF4J, Vaadin, JBang, and
some provider integrations may remain JVM-first until proven.

## Concrete Solution

1. Create or update `docs/developer/specification/native-packaging-posture.md` as the JVM/native
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

- `docs/developer/specification/native-packaging-posture.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/specification/codegeist-opencode-parity.md` only if native/JVM-only posture
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

- None for this documentation slice. The posture keeps JVM jar packaging as the
  required baseline and native-image validation as an explicit compatibility
  target with `passed`, `skipped`, or `failed` status until later release tasks
  decide whether native packaging is release-blocking.

## Work Task Phase Status

- `/specify-task` result: specified. The existing goal, scope, non-goals,
  acceptance criteria, and open question were clear enough for a documentation-only
  packaging posture task after rechecking the T002 parent default hints.
- `/plan-task` result: planned. The existing task is the concrete implementation
  task; no duplicate task or child task is needed. Target files are
  `docs/developer/specification/native-packaging-posture.md`, `docs/developer/README.md`,
  `docs/developer/architecture/architecture.md`, `docs/memory-bank/chat.md`, this task file,
  and the T002 parent/T002_12 task only for finalization impact notes.
- `/solve-task` result: solved. Created the native packaging posture blueprint as
  markdown only, with no Java source, Maven, Taskfile, test, package-directory,
  provider, PF4J, JBang, Vaadin, server, or runtime behavior changes.
- `/finalize-task` result: finalized. Documentation impact was reviewed through
  the update-documentation workflow requirements; developer indexes and current
  architecture references were updated, and native-readiness impact was propagated
  to the parent task and `T002_12`.
- Context or instructions considered: no extra user context beyond the task
  reference.
- Hints considered: T002 parent `Default Solve Hints`,
  `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Upstream phase dependency: satisfied by completed `T002_01` build/layout
  baseline and finalized `T002_03` runtime/session/event contracts. `T002_11` also
  consumed finalized provider, tool/permission/workspace, shell, and storage
  posture docs for blocker categories.
- Open decisions or blockers: none for this slice.
- Next recommended phase: work `T002_12_define_extension_and_client_readiness_gates.md`.

## Solution Note

Status: finalized.

Created `docs/developer/specification/native-packaging-posture.md` as the JVM jar and GraalVM
native-image verification posture blueprint. The document defines JVM-first and
native-aware release posture, future packaging commands, `passed`/`skipped`/`failed`
native status reporting, native blocker categories, surface-specific native claims,
illustrative diagnostic Java sketches, and future test handoff notes.

This task intentionally did not change Java source, Maven build configuration,
Taskfile commands, tests, provider dependencies, PF4J/JBang/Vaadin/server surfaces,
or runtime behavior.

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

Status: finalized.

Derived from the GraalVM constraints, technology baseline, risk register, and
implementation backlog as one packaging posture documentation/specification slice.
