# T002_10 Describe Minimal Storage Ports

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_19`, `T001_06`, `T001_07`, `T001_22`, `T001_24`

status: finalized

## Goal

Choose and describe the minimal storage port shape needed for early CLI session
continuation without implementing storage, event sourcing, or a database.

## Context

The architecture allows in-memory storage first, with file-backed session/config
storage only if needed for the first CLI workflow. Storage persists projections
and records through ports; it must not own runtime behavior, permissions,
workspace policy, or provider selection.

## Concrete Solution

1. Create or update `docs/developer/specification/storage-port-posture.md` as the future storage
   posture and port blueprint.
2. Decide whether the first implementation should be in-memory only or file-backed
   for a narrow session-continuation reason.
3. Define future storage port shapes for sessions and optional audit/event
   projection metadata.
4. Describe a future in-memory adapter first unless a file-backed continuation
   reason is explicitly selected.
5. Document future tests for save/load or in-memory lifecycle behavior, redaction
   boundaries, and separation from runtime orchestration.
6. Include OpenCode source evidence, future file maps, diagrams, and illustrative
   Java snippets in markdown only.

## Scope

- `docs/developer/specification/storage-port-posture.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/specification/codegeist-opencode-parity.md` only if the storage decision
  changes architecture posture
- `docs/developer/architecture/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- The initial storage posture is explicit: in-memory only or file-backed for a
  narrow reason.
- Storage ports are specified as not owning runtime orchestration.
- Sensitive data and credentials are not treated as ordinary session/tool output.
- Future tests for the selected minimal persistence behavior are described, but no
  Java source, tests, or adapters are created by this task.
- Event sourcing remains optional and unimplemented.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_03`.
- Should happen before CLI session continuation features depend on persistence.

## Non-Goals

- Do not create Java source files, empty package directories, storage tests,
  storage ports, or adapters.
- Do not implement database schemas, migrations, encryption, durable audit log,
  sharing, compaction, or event replay.

## Open Questions

- None for this documentation pass. The selected posture is in-memory storage
  first behind replaceable ports; file-backed restart/continue/list behavior is
  deferred until a concrete CLI continuation workflow requires persistence across
  process restarts.

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later storage implementation task instead of creating
  `ai.codegeist.storage` or storage-facing session source packages now.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task is intentionally a storage decision and port-shape slice, not a
  database, event-sourcing, or audit-log implementation.
- Source research may inspect how OpenCode relates sessions, events, and storage,
  but Codegeist should keep event sourcing optional.

## Dependency Impact Notes

- Finalized `T002_09_add_controlled_shell_verification_tool.md` defines shell
  results as bounded summaries, typed failures, and `OutputRef` values. This
  storage posture task should treat shell logs, stdout/stderr, command payloads,
  and environment values as sensitive or potentially large artifacts, not ordinary
  session text.
- Storage ports may later persist shell result projections or output references,
  but they must not own shell execution, permission policy, workspace-cwd
  validation, environment redaction, or process lifecycle behavior.

## Phase Status

- Phase: `/specify-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_10_decide_minimal_storage_ports.md`.
- Context or instructions considered: user ran `/work-task` by exact task path with
  no additional narrowing instructions.
- Upstream phase dependency: none; `/specify-task` is the entry phase and may be
  repeated when session continuation, storage redaction, event projection, or
  persistence boundaries need to be refreshed.
- Parent considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Dependencies and adjacent tasks considered: dependency
  `T002_03_introduce-runtime-session-event-contracts.md`, finalized upstream
  `T002_09_add_controlled_shell_verification_tool.md`, adjacent open
  `T002_11_validate_native_packaging_posture.md`, and downstream open
  `T002_12_define_extension_and_client_readiness_gates.md`.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Documentation considered: `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/runtime-session-event-contracts.md`,
  `docs/developer/specification/shell-verification-contracts.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- Discovered hints considered: the T002 parent `Default Solve Hints` point this
  task toward OpenCode source evidence for sessions, events, storage, projection,
  redaction, and audit posture while preserving Codegeist's Java-first
  runtime-owned boundaries.
- Result: specified. The task remains a documentation-only minimal storage posture
  blueprint. It should choose the initial in-memory versus file-backed posture,
  describe future storage ports and adapters, and keep storage out of runtime
  orchestration without creating Java source, tests, package directories, storage
  ports, adapters, database schemas, migrations, encryption, durable audit logs,
  sharing, compaction, event replay, Graphify, Repomix, or runtime behavior.
- Open decisions or blockers: none for specification. Restart/continue persistence
  is deferred until a concrete CLI continuation workflow requires it.
- Next recommended phase: run `/plan-task T002_10` as a documentation-only
  architecture plan for `docs/developer/specification/storage-port-posture.md`.

## Architecture Plan

This planning pass keeps `T002_10` as one documentation-only architecture task. No
child task is needed because the source task already targets one narrow storage
posture and port-shape blueprint, and no matching developer document existed yet.

### Selected Option

Create `docs/developer/specification/storage-port-posture.md` as the concrete storage posture
blueprint, select in-memory storage first behind replaceable ports, then cross-link
it from developer documentation and current-state architecture notes.

This option follows the active T002 posture: design the complete architecture
handoff first, then derive Java implementation tasks later only when explicitly
requested.

### Concrete Design Direction

Deepen the storage architecture around seven layers:

1. In-memory-first decision and file-backed deferral criteria.
2. Runtime/session/event ownership versus storage-port responsibility.
3. Minimal future ports for sessions, message projections, runtime/audit event
   projections, artifact references, and storage health.
4. Persistence categories for sessions, turns, message parts, events, tool/shell
   outputs, patch/edit artifacts, permission decisions, provider telemetry, and
   credentials.
5. Adapter evolution from in-memory to file-backed and later database-backed
   storage without changing runtime contracts.
6. Redaction, retention, and sensitive-output rules.
7. Future Java file maps, illustrative Java snippets, and future test handoff.

Storage should be documented as a runtime port, not a runtime owner. Event sourcing
should remain optional and unimplemented. File-backed persistence should require a
future user-visible restart/continue/list acceptance criterion.

### Planned Documentation Files

- `docs/developer/specification/storage-port-posture.md`
- `docs/developer/README.md`
- `docs/developer/specification/codegeist-opencode-parity.md` because this task turns the prior
  in-memory-or-file-backed posture into an explicit in-memory-first decision
- `docs/developer/architecture/architecture.md` only for current-state cross-references and
  explicit not-implemented notes
- this task file
- directly affected downstream task files if finalization finds durable impact

No Java source, tests, fixtures, Maven files, build files, package directories,
storage ports, storage adapters, database schemas, migrations, encryption, durable
audit logs, sharing, compaction, event replay, Graphify, Repomix, or runtime
behavior are planned for this task.

### Planned Blueprint Content

1. State the purpose and non-implementation boundary of the storage posture
   blueprint.
2. Summarize OpenCode source evidence for filesystem storage, SQLite-backed session
   tables, session services, projectors, and stable ids.
3. Select in-memory storage first because prompt execution and CLI continuation do
   not exist yet.
4. Define future Codegeist port names for `SessionStore`,
   `MessageProjectionStore`, `RuntimeEventProjectionStore`,
   `ArtifactReferenceStore`, and `StorageHealth`.
5. Define persistence categories and redaction rules so credentials, secrets, raw
   shell output, raw patch content, provider payloads, and environment values are
   not ordinary session storage.
6. Define adapter evolution from in-memory to file-backed to database-backed only
   when concrete workflow needs justify each step.
7. Define event/projection posture so final summaries may persist while streaming
   deltas stay transient by default.
8. Include Mermaid diagrams for storage ports, adapter evolution, and event
   projection.
9. Include illustrative Java snippets inside markdown only, clearly labeled as
   non-implemented examples.
10. Add future file map and future test handoff notes.

### Acceptance Criteria For Solve

- `docs/developer/specification/storage-port-posture.md` exists and makes the initial storage
  posture explicit as in-memory first, with file-backed persistence deferred until
  restart/continue/list behavior is selected.
- Storage ports are specified as not owning runtime orchestration.
- Sensitive data, credentials, shell logs, patch contents, provider payloads, and
  environment values are not treated as ordinary session/tool output.
- Future tests for in-memory lifecycle, redaction boundaries, port separation,
  projection ordering, artifact references, optional file-backed continuation, and
  event-sourcing optionality are described.
- Event sourcing remains optional and unimplemented.
- `docs/developer/README.md`, `docs/developer/architecture/architecture.md`,
  `docs/developer/specification/codegeist-opencode-parity.md`, this task file, and
  `docs/memory-bank/chat.md` remain consistent with the new blueprint.

### Verification Plan

```bash
git --no-pager diff --check
```

`task test` is not required because the planned solve changes documentation only
and must not touch Java source, tests, build files, or runtime behavior.

### Open Questions

None for this documentation plan. File-backed continuation, database selection,
event log/replay, encryption, retention, deletion, and migration details remain
deferred until later implementation tasks.

## Plan Workflow Handoff

- Phase: `/plan-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_10_decide_minimal_storage_ports.md`.
- Source task resolved: exact path
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_10_decide_minimal_storage_ports.md`.
- Target task: this existing `T002_10` task; no child task was created because the
  task is already the narrow documentation-only storage posture slice.
- User context considered: the user ran `/work-task` with no additional narrowing
  instructions.
- Parent task considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Dependencies and adjacent tasks considered: dependency `T002_03`, finalized
  upstream `T002_09`, adjacent open `T002_11`, and downstream open `T002_12`.
- Duplicate check result: no existing `docs/developer/specification/storage-port-posture.md`
  document existed before this solve, and no separate implementation task already
  planned this blueprint; the existing `T002_10` task is the correct target to
  sharpen.
- Selected option: create the storage posture architecture blueprint document in
  the solve pass, select in-memory storage first, and cross-link it from developer
  documentation while leaving Java source and build files untouched.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/runtime-session-event-contracts.md`,
  `docs/developer/specification/shell-verification-contracts.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- Upstream phase dependency: satisfied; this task had a current specification
  check result and dependency impact notes before planning.
- Result: planned as one documentation-only architecture task.
- Open decisions or blockers: none for planning.
- Next recommended phase: run `/solve-task T002_10` as a documentation-only
  architecture design pass.

## Solution Note

Solved as a documentation-only architecture blueprint in
`docs/developer/specification/storage-port-posture.md`. The new document selects in-memory
storage first behind replaceable ports, defers file-backed restart/continue/list
persistence until a concrete CLI workflow requires it, keeps event sourcing
optional, and defines future session, message projection, event projection,
artifact reference, storage health, redaction, retention, adapter evolution, future
Java file map, illustrative Java snippets, and future test handoff guidance.

The solve pass also linked the new blueprint from `docs/developer/README.md` and
`docs/developer/architecture/architecture.md`, and updated
`docs/developer/specification/codegeist-opencode-parity.md` so the prior in-memory-or-file-backed
posture is now an explicit in-memory-first decision. It did not create Java source,
tests, package directories, storage ports, adapters, database schemas, migrations,
encryption, durable audit logs, compaction, event replay, Graphify, Repomix, or
runtime behavior.

## Solve Status

- Phase: `/solve-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_10_decide_minimal_storage_ports.md`.
- User instructions considered: run the full `/work-task` workflow for this exact
  task path. Prior user decisions remain in effect: this slice is documentation
  only and should create `docs/developer/specification/storage-port-posture.md` without Java
  source.
- Upstream phase dependency: satisfied. The target task had a current plan handoff
  before this solve pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`, dependency
  `T002_03_introduce-runtime-session-event-contracts.md`, finalized
  `T002_09_add_controlled_shell_verification_tool.md`, adjacent open
  `T002_11_validate_native_packaging_posture.md`, downstream open
  `T002_12_define_extension_and_client_readiness_gates.md`,
  `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/runtime-session-event-contracts.md`,
  `docs/developer/specification/shell-verification-contracts.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- OpenCode evidence considered: `storage/storage.ts` for key-value storage,
  filesystem-backed JSON writes, migrations, locks, and list/read/write/update
  shape; `session/session.sql.ts` for session, message, part, session-message,
  todo, permission, summary, and timestamp persistence; `session/session.ts` for
  session create/list/fork/update/delete behavior through storage and sync
  services; `session/projectors-next.ts` for final-event-to-message projection and
  transient delta skipping; and `session/schema.ts` for stable typed session,
  message, and part ids independent of persistence.
- Documentation updates: created `docs/developer/specification/storage-port-posture.md`, updated
  `docs/developer/README.md`, updated `docs/developer/architecture/architecture.md`, updated
  `docs/developer/specification/codegeist-opencode-parity.md`, and prepared finalization updates
  for affected tasks and memory.
- Acceptance criteria status: satisfied. Initial storage posture is explicit as
  in-memory first; storage ports are specified as not owning runtime orchestration;
  sensitive data and credentials are excluded from ordinary session/tool output;
  future tests are described; and event sourcing remains optional and
  unimplemented.
- Verification: `git --no-pager diff --check` and
  `git --no-pager diff --check --no-index /dev/null docs/developer/specification/storage-port-posture.md`.
- Open decisions or blockers: none for this architecture pass. Future Java
  implementation tasks still need to choose exact port APIs, serialization, file
  persistence, retention/deletion, migration, encryption, and event log behavior if
  those become necessary.
- Next recommended phase: run `/finalize-task T002_10`.

## Finalization Status

- Phase: `/finalize-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_10_decide_minimal_storage_ports.md`.
- User instructions considered: finalize after solving by checking task impact and
  refreshing affected documentation through update-documentation semantics.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: solved` and a current successful `/solve-task` status before this
  finalization pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Impacted tasks: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md` and downstream open
  `T002_12_define_extension_and_client_readiness_gates.md`.
- Documentation reviewed through update-documentation semantics:
  `docs/developer/specification/storage-port-posture.md`, `docs/developer/README.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/specification/codegeist-opencode-parity.md`,
  affected T002 task files, and `docs/memory-bank/chat.md`.
- Documentation updates: the parent task now records that `T002_10` is finalized;
  `T002_12` records that server and Vaadin readiness gates require explicit
  storage adapter, retention, redaction, concurrency, and auth posture before
  depending on durable persistence; and `docs/memory-bank/chat.md` was refreshed to
  make `T002_11` the next useful T002 slice.
- Remaining follow-ups: continue with `T002_11_validate_native_packaging_posture.md`
  as the next documentation-first T002 slice. Future Java implementation tasks may
  be derived later from `docs/developer/specification/storage-port-posture.md` only when
  explicitly requested.
- Verification: `git --no-pager diff --check` and
  `git --no-pager diff --check --no-index /dev/null docs/developer/specification/storage-port-posture.md`.
- Result: finalized. No implementation gaps, blockers, Java source, tests, package
  directories, storage ports, adapters, database schemas, migrations, encryption,
  durable audit logs, compaction, event replay, Graphify, Repomix, or runtime
  behavior were introduced.
- Next recommended phase: start `/specify-task T002_11` or `/work-task T002_11`
  when ready to continue the native packaging posture slice.

## Creation Note

Status: open.

Derived from storage, session, event, MVP, and risk-register architecture as one
minimal persistence decision slice.
