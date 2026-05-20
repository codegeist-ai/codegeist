# T002_12 Describe Extension And Client Readiness Gates

Parent: `T002_define-codegeist-mvp-foundation-blueprints`

Sources: `T001_15`, `T001_16`, `T001_17`, `T001_18`, `T001_21`, `T001_22`, `T001_24`

status: finalized

## Goal

Describe implementation-readiness gates for later PF4J, JBang, server, and Vaadin
work so the CLI/runtime MVP does not accidentally absorb later-stage scope.

## Context

The architecture deliberately defers PF4J plugin loading, JBang execution, server
APIs, Vaadin views, SDK/OpenAPI, desktop, marketplace behavior, and broad UI work
until core CLI/runtime boundaries are validated. These surfaces still need clear
entry criteria so future tasks can be created without re-litigating the T001
architecture.

## Concrete Solution

1. Create or update `docs/developer/specification/extension-client-readiness-gates.md` as the
   readiness-gate blueprint for PF4J, JBang, headless server, Vaadin, SDK/OpenAPI,
   and future TUI work.
2. Tie each gate to concrete core contracts: runtime API, session/event model,
   tool/permission/workspace contracts, storage posture, auth posture, and native
   compatibility posture.
3. Identify which later surfaces are JVM-only until proven native-compatible.
4. Include future task split guidance, diagrams if useful, and readiness
   checklists in markdown only.
5. Do not add dependencies or implement adapters; this is a handoff/gate task for
   later implementation work.

## Scope

- `docs/developer/specification/extension-client-readiness-gates.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/specification/codegeist-opencode-parity.md` only if readiness gates change
  architecture decisions
- this task file

## Acceptance Criteria

- PF4J and JBang readiness gates require tool, permission, workspace, event, and
  native posture decisions.
- Server and Vaadin readiness gates require runtime/session/event APIs, storage
  posture, and auth/security decisions.
- Later surfaces stay deferred and do not block `T002_01` through `T002_11`.
- Future implementation tasks can reference the gates instead of reopening broad
  architecture decisions.
- No Java source files, marker interfaces, dependencies, adapters, tests, server
  routes, Vaadin views, PF4J plugins, or JBang integrations are created by this
  task.

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
- Do not create Java source files, empty package directories, marker interfaces,
  dependencies, adapters, or tests.

## Open Questions

- None for this documentation slice. The readiness gates live in the separate
  developer note `docs/developer/specification/extension-client-readiness-gates.md`, with
  cross-links from developer documentation and current-state architecture notes.

## Work Task Phase Status

- `/specify-task` result: specified. The existing goal, scope, non-goals,
  dependencies, acceptance criteria, and prior specification decision were clear
  enough for a documentation-only readiness-gate task. The durable user context was
  the exact task reference with no additional narrowing instructions.
- `/plan-task` result: planned. This existing `T002_12` task is the concrete
  implementation task; no duplicate task or child task is needed. Target files are
  `docs/developer/specification/extension-client-readiness-gates.md`,
  `docs/developer/README.md`, `docs/developer/architecture/architecture.md`,
  `docs/memory-bank/chat.md`, the T002 parent task, and this task file.
- `/solve-task` result: solved. Created the extension/client readiness-gates
  blueprint as markdown only, with no Java source, dependencies, tests, adapters,
  routes, Vaadin views, PF4J plugins, JBang integrations, SDK generation, TUI, or
  runtime behavior changes.
- `/finalize-task` result: finalized. Documentation impact was reviewed through
  update-documentation semantics; developer indexes and current architecture
  references were updated, and the T002 parent plus project memory now record that
  the final T002 foundation readiness slice is complete.
- Context or instructions considered: no extra user context beyond the task
  reference.
- Hints considered: T002 parent `Default Solve Hints`,
  `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Upstream phase dependency: satisfied by finalized `T002_03` runtime/session/event
  contracts, `T002_07` tool/permission/workspace contracts, `T002_09` controlled
  shell verification contracts, `T002_10` storage posture, and `T002_11` native
  packaging posture.
- Open decisions or blockers: none for this slice.
- Next recommended phase: derive later implementation tasks from the readiness
  gates only when the user explicitly reopens PF4J, JBang, server, Vaadin,
  SDK/OpenAPI, or TUI implementation work.

## Architecture Plan

Create `docs/developer/specification/extension-client-readiness-gates.md` as the concrete
readiness blueprint for deferred extension and client surfaces. The document should
define shared gates, surface-specific gates, implementation split guidance, future
file maps, diagrams, and future test handoff notes while leaving all source,
adapter, dependency, and runtime behavior untouched.

The solve pass should consume the finalized runtime/session/event, tool,
permission, workspace, shell/process, storage, and native packaging posture docs.
It should update developer documentation cross-references and leave
`docs/developer/architecture/architecture.md` current-state focused by marking the new document
as a future blueprint only.

## Solution Note

Status: finalized.

Created `docs/developer/specification/extension-client-readiness-gates.md` as the readiness-gate
blueprint for later PF4J, JBang, headless server, Vaadin, SDK/OpenAPI, and future
TUI work. The document requires later surfaces to satisfy runtime API,
session/event, tool/permission/workspace, storage, auth/security, native posture,
and test-readiness gates before implementation starts.

The solve pass also linked the new blueprint from `docs/developer/README.md` and
`docs/developer/architecture/architecture.md`. It intentionally did not add Java source, tests,
dependencies, adapters, server routes, Vaadin views, PF4J plugins, JBang scripts,
SDK generation, TUI behavior, or runtime behavior.

## Finalization Impact Notes

- The T002 parent now records `T002_12` as finalized and documents that deferred
  extension/client surfaces remain behind readiness gates.
- `docs/memory-bank/chat.md` now records the completed T002 foundation
  documentation sequence and removes `T002_12` as the next open point.
- No other open T002 child task is affected because `T002_12` is the final child
  in this parent slice.

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  readiness-gate handoff for later extension and client implementation tasks
  instead of creating extension/client source packages now.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task is correctly grouped as a later-surface readiness gate instead of
  separate premature PF4J, JBang, server, Vaadin, SDK, or TUI implementation
  tasks.
- Source research should be limited to clarifying extension/client concepts and
  should not pull those surfaces into the CLI/runtime MVP.

## Dependency Impact Notes

- Finalized `T002_07_add_tool_permission_workspace_contracts.md` defines initial
  tool, permission, workspace, bounded result, event, and session projection
  boundaries for later PF4J, JBang, MCP, and provider-signaled tools.
- This task should treat that blueprint as a prerequisite readiness input, but it
  must still keep PF4J, JBang, server, Vaadin, SDK/OpenAPI, desktop, marketplace,
  and TUI implementation deferred until the remaining readiness gates are also
  satisfied.
- Finalized `T002_09_add_controlled_shell_verification_tool.md` defines shell and
  process execution as a high-risk, permission-gated, workspace-cwd-validated,
  bounded-output contract. Later JBang script tools, PF4J process-like tools,
  server-triggered verification, and client approval UIs should satisfy that shell
  readiness posture instead of introducing independent process execution paths.
- Finalized `T002_10_decide_minimal_storage_ports.md` selects in-memory storage
  first behind replaceable ports. Server and Vaadin readiness gates should require
  an explicit storage adapter, retention, redaction, concurrency, and auth posture
  before depending on durable session, event, audit, or artifact persistence.
- Finalized `T002_11_validate_native_packaging_posture.md` keeps the MVP
  foundation JVM-first and native-aware. PF4J, JBang, server, Vaadin, broad
  provider, shell/process, and storage surfaces must be treated as JVM-first until
  their own tasks prove native compatibility and record native status as `passed`,
  `skipped` with reason, or `failed` with a concrete blocker.

## Creation Note

Status: open.

Derived by grouping the later-stage plugin/script/server/Vaadin/parity/risk tasks
into one scope-control readiness specification rather than creating one premature
implementation task per deferred surface.
