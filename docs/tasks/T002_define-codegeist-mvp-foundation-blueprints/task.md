# T002 Define Codegeist MVP Foundation Baseline

Status: open

## Goal

Keep the useful MVP foundation baseline documents while removing obsolete broad
blueprint tasks that encouraged placeholder implementation planning.

## Current Child Tasks

- `T002_01_align-codegeist-build-baseline.md`
- `T002_02_introduce-runtime-vocabulary-contracts.md`
- `T002_11_validate_native_packaging_posture.md`

## Obsolete Child Tasks

The previous `T002_03` through `T002_10` and `T002_12` blueprint tasks were
removed with their generated specification documents. Do not use those removed
tasks or document paths as implementation inputs.

Future runtime, session, event, context, provider, tool, permission, workspace,
patch/edit, shell, storage, extension, and client work should be recreated as
small implementation tasks with focused tests and current architecture updates.

## Scope

- Keep the completed `app/codegeist/cli` build baseline from `T002_01`.
- Keep `docs/developer/specification/runtime-vocabulary.md` as terminology and
  boundary vocabulary, not as a source package reservation.
- Keep `docs/developer/specification/native-packaging-posture.md` as planned
  packaging posture until release automation or binary-smoke work starts.
- Keep PF4J, JBang, server, Vaadin, API, and SDK/OpenAPI surfaces deferred until
  small tested workflows require them.

## Non-Goals

- Do not restore the removed blueprint documents.
- Do not add Java source files, empty package directories, contract tests, or
  runtime behavior from this parent task.
- Do not collapse future implementation into one broad task.
- Do not split Maven modules before real behavior and tests justify it.

## Verification

Documentation-only changes should run:

```bash
git --no-pager diff --check
```

## Creation Note

This parent was originally created for MVP foundation blueprints after the T001
OpenCode parity architecture work. It has been narrowed after the placeholder
blueprint cleanup so future implementation can restart from small tested Spring
workflows.
