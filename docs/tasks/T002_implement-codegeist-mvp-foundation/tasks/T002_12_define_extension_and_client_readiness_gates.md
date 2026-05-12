# T002_12 Define Extension And Client Readiness Gates

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_15`, `T001_16`, `T001_17`, `T001_18`, `T001_21`, `T001_22`, `T001_24`

## Goal

Create implementation-readiness gates for later PF4J, JBang, server, and Vaadin
work so the CLI/runtime MVP does not accidentally absorb later-stage scope.

## Context

The architecture deliberately defers PF4J plugin loading, JBang execution, server
APIs, Vaadin views, SDK/OpenAPI, desktop, marketplace behavior, and broad UI work
until core CLI/runtime boundaries are validated. These surfaces still need clear
entry criteria so future tasks can be created without re-litigating the T001
architecture.

## Concrete Solution

1. Add a short readiness document or task-local checklist that defines prerequisites
   for PF4J, JBang, headless server, Vaadin, SDK/OpenAPI, and future TUI work.
2. Tie each gate to concrete core contracts: runtime API, session/event model,
   tool/permission/workspace contracts, storage posture, auth posture, and native
   compatibility posture.
3. Identify which later surfaces are JVM-only until proven native-compatible.
4. Do not add dependencies or implement adapters; this is a handoff/gate task for
   later implementation work.

## Scope

- `docs/developer/codegeist-opencode-parity.md` or a focused developer note if a
  separate readiness checklist is clearer
- no source implementation unless a tiny marker interface already exists and is
  necessary to prevent naming drift

## Acceptance Criteria

- PF4J and JBang readiness gates require tool, permission, workspace, event, and
  native posture decisions.
- Server and Vaadin readiness gates require runtime/session/event APIs, storage
  posture, and auth/security decisions.
- Later surfaces stay deferred and do not block `T002_01` through `T002_11`.
- Future implementation tasks can reference the gates instead of reopening broad
  architecture decisions.

## Verification

```bash
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_03`, `T002_07`, `T002_10`, and `T002_11` for final readiness
  gates, but can be drafted early as a scope-control checklist.

## Non-Goals

- Do not implement PF4J, JBang, server routes, Vaadin views, auth, SDK/OpenAPI,
  desktop, marketplace behavior, or a full-screen TUI.

## Open Questions

- Should readiness gates live in the parity architecture document or in a separate
  developer note once implementation begins?

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task is correctly grouped as a later-surface readiness gate instead of
  separate premature PF4J, JBang, server, Vaadin, SDK, or TUI implementation
  tasks.
- Source research should be limited to clarifying extension/client concepts and
  should not pull those surfaces into the CLI/runtime MVP.

## Creation Note

Status: open.

Derived by grouping the later-stage plugin/script/server/Vaadin/parity/risk tasks
into one scope-control implementation-readiness task rather than creating one
premature implementation task per deferred surface.
