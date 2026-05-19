# T003_12 Define Storage Ports Session Continuation Source Generation Contract

Parent: `T003_implement-codegeist-opencode-core-application`

Status: finalized

## Goal

Define a documentation-only source-generation contract for storage ports and CLI
session continuation before any storage, continuation, or Java source is created.

This task replaces the earlier implementation-oriented `T003_12` slot. The next
safe step is to turn the finalized storage port posture blueprint into a compact
handoff for future in-memory-first ports, session continuation identity,
create/continue/list/delete behavior, projection storage, artifact references,
redaction, storage health, typed failures, file-backed deferral criteria, and test
contracts.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement Java yet.
- Convert `T003_12` into a documentation-only storage ports and session
  continuation source-generation contract task before source generation.

## Specification Decision

`T003_12` should be a documentation-only storage ports and session continuation
source-generation contract task.

The later source-generating task should not start directly from the broad T002
storage posture blueprint. It should first receive a reviewed contract that names
the first storage port boundary, continuation behavior, in-memory adapter posture,
projection and artifact-reference rules, redaction and retention constraints,
file-backed persistence deferral criteria, typed failure shape, non-goals, and TDD
expectations while preserving runtime, session, event, CLI, context, provider,
tool, permission, workspace, patch/edit, shell, TUI, and server boundaries.

## Context

`T002_10` finalized a documentation-only minimal storage posture blueprint. It
selected in-memory storage first behind replaceable ports, deferred file-backed
restart/continue/list persistence until a concrete CLI workflow requires it, kept
event sourcing optional, excluded credentials and sensitive tool/shell/provider
artifacts from ordinary session storage, and documented future session/message
projection, runtime event projection, artifact-reference, and health ports. That
task intentionally did not create Java source, tests, package directories, storage
ports, storage adapters, database schemas, migrations, encryption, durable audit
logs, compaction, event replay, Graphify, Repomix, or runtime behavior.

`T003_05` is the finalized runtime/session/event source-generation contract slice,
`T003_06` is the CLI prompt command source-generation contract slice, `T003_07` is
the context/workspace loading source-generation contract slice, `T003_08` is the
provider configuration and Spring AI adapter source-generation contract slice,
`T003_09` is the tool, permission, and workspace source-generation contract slice,
`T003_10` is the patch/edit proposal source-generation contract slice, and
`T003_11` is the controlled shell tool source-generation contract slice. This task
must consume those boundaries where relevant: runtime owns prompt turns, session
state transitions, continuation orchestration, events, and session summaries;
patch/edit and shell own bounded output and `OutputRef` production posture; storage
owns only replaceable persistence ports, projection records, artifact reference
metadata, health reporting, and adapter-specific failures.

## Scope

- Define the first source-generation boundary for storage port and session
  continuation code.
- Translate the T002 storage port posture blueprint into a compact implementation
  handoff for future Java contracts.
- Define planned session, turn, message projection, event projection, artifact
  reference, storage health, and storage failure shapes.
- Define planned create, continue, list, update-summary, append-projection,
  reference-artifact, and delete/archive behavior without implementing runtime
  orchestration, CLI commands, persistence, serialization, or adapters.
- Define planned in-memory-first behavior and the exact criteria that must be met
  before a later task may add file-backed restart/continue/list persistence.
- Define planned redaction, retention, bounded projection, and sensitive-output
  rules so raw prompts, provider payloads, stdout/stderr, patch contents,
  environment maps, stack traces, credentials, and secrets do not enter ordinary
  session storage.
- Define runtime/session/event integration rules for storage lifecycle outcomes
  without making storage own event sequencing, prompt execution, tool policy, shell
  execution, patch/edit apply behavior, or session behavior.
- Define the required TDD and verification contract for the later implementation
  task that will create storage and continuation Java source.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, CLI commands, TUI behavior, runtime services,
  context readers, provider calls, Spring AI tool callbacks, tool execution,
  permission approval, workspace policy code, patch/edit behavior, shell execution,
  storage ports, storage adapters, in-memory adapter code, file-backed persistence,
  database schemas, migrations, encryption, durable audit logs, compaction, event
  replay, Graphify, Repomix, or native/build behavior in this task.
- Do not implement session creation, session continuation, session listing,
  session deletion, projection storage, artifact-reference storage, health
  reporting, redaction, retention, serialization, file locking, migration,
  database access, event emission, session projection, UI rendering, or runtime
  behavior.
- Do not let storage contracts own provider configuration, provider invocation,
  runtime prompt execution, session lifecycle, event sequencing, CLI parsing,
  context manifest construction, generic tool policy, generic permission policy,
  generic workspace validation, patch/edit apply behavior, shell/process execution,
  TUI rendering, server routes, Vaadin, PF4J, or JBang behavior.
- Do not expose Spring AI, provider SDK, OpenCode, MCP, PF4J, JBang, filesystem,
  SQL, JSON codec, lock, migration, shell, process, terminal, or patch/edit
  implementation types through runtime, session, event, CLI, context, provider,
  tool, permission, workspace, storage, TUI, server, Vaadin, PF4J, or JBang
  contracts.
- Do not copy OpenCode's TypeScript, Bun, Effect, SQLite, Drizzle, global data
  path, migration, lock, sync, projector, event bus, storage, or file layout
  implementation shape.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_05_define_runtime_session_event_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_06_define_cli_prompt_command_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_07_define_context_workspace_loading_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_08_define_provider_configuration_spring_ai_adapter_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_09_define_tool_permission_workspace_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_10_define_patch_edit_proposal_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_11_define_controlled_shell_tool_source_generation_contract.md`
- `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_10_decide_minimal_storage_ports.md`
- `docs/developer/specification/storage-port-posture.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/patch-edit-proposal-contracts.md`
- `docs/developer/specification/shell-verification-contracts.md`
- `docs/developer/specification/tool-permission-workspace-contracts.md`
- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first storage ports and
session continuation source-generation handoff. The preferred target is:

- `docs/developer/specification/storage-session-continuation-source-generation-contract.md`

The guidance should include:

- The first storage source-generation boundary and why it is smaller than broad
  persistence, file-backed restart support, event sourcing, database storage,
  runtime orchestration, generic artifact storage, TUI/server session browsing, or
  OpenCode parity behavior.
- Planned package ownership for storage ports, continuation records, projection
  records, artifact references, health reporting, and adapter handoff, clearly
  labeled as planned source.
- Planned `SessionStore`, `MessageProjectionStore`,
  `RuntimeEventProjectionStore`, `ArtifactReferenceStore`, `StorageHealth`,
  `StorageMode`, `StorageStatus`, `SessionRecord`, `SessionSummary`,
  `SessionListQuery`, `MessagePartRecord`, `RuntimeEventRecord`, `OutputRef`,
  `StorageFailure`, and `StorageFailureKind` shapes without implementing them.
- Planned session continuation behavior for create, continue by id, list recent,
  update summary, append bounded message projections, attach artifact references,
  delete or archive, and report storage health.
- Planned in-memory-first adapter posture and file-backed deferral rules, including
  the acceptance evidence required before restart/continue/list persistence across
  process restarts is allowed.
- Planned projection and artifact-reference rules that keep streaming deltas, full
  stdout/stderr, command payloads, raw patch contents, provider payloads, stack
  traces, environment maps, credentials, and secrets out of ordinary session
  records.
- Planned result and failure taxonomy for unavailable storage, not found, duplicate
  id, stale projection, redaction required, retention denied, artifact unavailable,
  serialization unsupported, adapter failure, and health degraded.
- Runtime/session/event integration rules that map storage lifecycle outcomes to
  later runtime events and session summaries without making storage policy own
  event sequencing, runtime prompts, tool execution, shell execution, patch/edit
  apply behavior, or persistence decisions above the port.
- Boundary rules that keep Spring Shell, CLI parsing, provider configuration,
  context loading, generic tool registry, generic permission policy, generic
  workspace validation, patch/edit apply, shell execution, TUI, server, Vaadin,
  PF4J, JBang, Graphify, Repomix, and external analysis outside the first source
  slice.
- TDD handoff for the later implementation task, including first narrow tests for
  in-memory create/continue/list/delete, projection append ordering, duplicate-id
  handling, missing-session handling, bounded message summaries, artifact-reference
  metadata, sensitive-output rejection, storage health, runtime/session/event
  handoff, and implementation-type isolation.
- Explicit deferrals to later T003 tasks for file-backed restart persistence,
  database storage, migration, encryption, durable audit logs, event sourcing,
  compaction, sharing, server or Vaadin session browsing, end-to-end agent loop,
  CLI/TUI parity workflows, packaging/native validation, PF4J, JBang, Vaadin,
  server, and API behavior.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, Spring beans, storage ports, storage adapters,
  persistence behavior, runtime behavior, CLI/TUI behavior, provider behavior,
  tool behavior, permission behavior, workspace behavior, patch/edit behavior, or
  shell behavior.
- The task converts `T003_12` from storage ports and session continuation
  implementation into a source-generation contract.
- The handoff documents future session storage, continuation identity, list/delete
  posture, message projection, event projection, artifact references, redaction,
  retention, storage health, typed failures, in-memory adapter behavior, and
  file-backed persistence deferral without implementing those contracts.
- The handoff uses finalized runtime/session/event, patch/edit, shell,
  tool/permission/workspace, context/workspace, provider, Java generation, testing,
  and storage posture documents as constraints for future source generation.
- The handoff keeps storage separate from runtime prompt execution, session state
  transitions, event sequencing, CLI parsing, context loading, provider invocation,
  generic tool execution, permission approval, workspace validation, patch/edit
  apply behavior, shell/process execution, TUI, server, Vaadin, PF4J, and JBang
  behavior.
- The handoff keeps Spring AI, provider SDK, OpenCode, MCP, PF4J, JBang,
  filesystem, SQL, JSON codec, migration, lock, process, terminal, shell, and
  patch/edit implementation types inside planned adapter boundaries and exposes
  only Codegeist-owned storage contracts to runtime/session/event code.
- Planned package names, Java shapes, source maps, failure taxonomy, projection
  rules, file-backed deferral criteria, health reporting, and tests are clearly
  labeled as planned, not current implementation.

## Planning-Readiness Questions

- What is the smallest storage contract a later Java task can implement without
  requiring file-backed persistence, database storage, migrations, encryption,
  durable audit logs, event sourcing, TUI, server, PF4J, JBang, or end-to-end
  agent-loop behavior?
- Which records are required first to continue a session by stable id while keeping
  runtime, session, event, projection, and storage responsibilities separate?
- Which session list, summary, delete/archive, and health fields are necessary for
  the first CLI continuation workflow, and which belong to later TUI/server work?
- How should storage consume `OutputRef` values from shell, patch/edit, tool, and
  provider boundaries without persisting full outputs, raw command payloads, raw
  diffs, provider payloads, stack traces, environment maps, credentials, or
  secrets?
- Which typed failures must be distinguishable in the first implementation handoff
  to make unavailable storage, missing sessions, duplicate ids, stale projections,
  adapter failures, and redaction denials safe to display and retry?
- What concrete user-visible requirement would justify file-backed
  restart/continue/list persistence, and how should a later task prove it before
  adding serialization, file locks, migrations, or retention policy?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: `/specify-task t003_12`.
- Context or instructions considered: user explicitly requested no Java
  implementation yet and asked to convert this into a documentation-only storage
  ports and session continuation source-generation contract task before source
  generation.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered: finalized `T003_05`, specified `T003_06`,
  specified `T003_07`, specified `T003_08`, finalized `T003_09`, specified
  `T003_10`, specified `T003_11`, and finalized `T003_02`, `T003_03`, and
  `T003_04`; the parent listed an implementation-oriented
  `T003_12_implement_storage_ports_and_session_continuation.md` slot, but that
  child task file did not exist before this pass.
- Dependency inputs considered: finalized `T002_10`, `storage-port-posture.md`,
  `runtime-session-event-contracts.md`, `patch-edit-proposal-contracts.md`,
  `shell-verification-contracts.md`, `tool-permission-workspace-contracts.md`,
  `context-workspace-manifest.md`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`, and `codegeist-opencode-parity.md`.
- Project overlay considered: `.oc_local/rules/codegeist-task-specification.md`.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_12` as a documentation-only storage ports and session
  continuation source-generation contract slice. Storage, continuation, and Java
  implementation should wait until this contract is planned and solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, first port boundary, continuation record shape, in-memory adapter
  posture, projection and artifact-reference rules, redaction and retention rules,
  health and failure taxonomy, file-backed deferral criteria, and future TDD
  handoff.
- Next recommended phase: `/plan-task t003_12` to define the concrete
  documentation plan for
  `docs/developer/specification/storage-session-continuation-source-generation-contract.md`.

## Dependency Update

- `T003_09` is now finalized and created
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`.
- The `T003_12` plan should treat `OutputRef`, bounded result, permission,
  workspace target, and tool lifecycle summaries as upstream handoff inputs rather
  than redefining generic tool policy.
- `T003_11` is now finalized and created
  `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`.
- The `T003_12` plan should consume shell bounded-output, typed failure,
  output-reference, and fake-executor posture where session continuation and
  artifact-reference storage interact with shell results.

## Creation Note

Created during `/specify-task t003_12` after the user paused Java implementation
and requested a documentation-only storage ports and session continuation
source-generation contract before source generation.

## Implementation Plan

- Phase command: `/plan-task t003_12` as part of `/work-task t003_12`.
- Source task: this `T003_12` documentation-only storage ports and session
  continuation source-generation contract task.
- User context or instructions considered: no extra context beyond the command;
  preserve the existing user direction to avoid Java implementation and create a
  source-generation handoff first.
- Selected option: update this task directly rather than create a duplicate child
  task, because the task is already the concrete documentation slice.
- Target files: create
  `docs/developer/specification/storage-session-continuation-source-generation-contract.md`,
  update this task with plan/solve/finalize status, update the T003 parent progress
  note, update the current-state architecture reference list, and refresh
  `docs/memory-bank/chat.md` because future sessions need this handoff state.
- Solution direction: narrow `storage-port-posture.md` into a planned Java
  source-generation contract that names first-wave storage ports, session
  continuation records, projection stores, artifact-reference metadata, storage
  health, typed failure taxonomy, in-memory-first adapter posture, file-backed
  deferral criteria, runtime/session/event integration boundaries, future file map,
  and TDD handoff tests.
- Implementation steps: read upstream runtime/session/event, tool/permission,
  patch/edit, shell, context/workspace, provider, Java generation, testing, storage
  posture, and task-hint docs; add the storage/session continuation handoff; update
  impacted task and architecture docs; run `git --no-pager diff --check`.
- Verification plan: documentation-only verification with
  `git --no-pager diff --check`; do not run Java tests because no Java, build,
  test, or runtime files change.
- Dependencies: finalized `T003_05` through `T003_11`, `T002_10`,
  `storage-port-posture.md`, Java generation guidance, and testing strategy.
- Open questions: None.

## Plan Check Result

- Phase command: `/plan-task t003_12`.
- Context or instructions considered: `/work-task t003_12` with no extra user
  instructions.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_12`
  result and top-level `Status: specified` before this pass.
- Result: planned this task as the concrete implementation target and selected the
  documentation-only handoff file
  `docs/developer/specification/storage-session-continuation-source-generation-contract.md`.
- Open decisions or blockers: None.
- Next recommended phase: `/solve-task t003_12`.

## Solution Note

- Phase command: `/solve-task t003_12` as part of `/work-task t003_12`.
- Context or instructions considered: `/work-task t003_12` with no extra user
  instructions.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the `/plan-task t003_12` plan in this
  file.
- Result: created
  `docs/developer/specification/storage-session-continuation-source-generation-contract.md`
  as the storage ports and session continuation source-generation handoff. The
  document defines planned `ai.codegeist.storage` ownership, first-wave storage
  ports, session continuation behavior, projection and artifact-reference records,
  storage health, typed failures, in-memory-first adapter posture, file-backed
  persistence deferral criteria, runtime/session/event integration rules, future
  file map, TDD handoff tests, and explicit deferrals.
- Acceptance criteria status: satisfied. The solution is documentation-only and
  creates no Java source, tests, packages, build files, storage adapters,
  persistence, runtime behavior, CLI/TUI behavior, provider behavior, tool behavior,
  permission behavior, workspace behavior, patch/edit behavior, or shell behavior.
- Verification: `git --no-pager diff --check` passes.
- Open decisions or blockers: None.
- Next recommended phase: `/finalize-task t003_12`.

## Finalization Result

- Phase command: `/finalize-task t003_12` as part of `/work-task t003_12`.
- Context or instructions considered: `/work-task t003_12` with no extra user
  instructions.
- Solved dependency: satisfied by the `/solve-task t003_12` solution note in this
  file.
- Impacted tasks: the T003 parent task now records `T003_12` as finalized and points
  later implementation work at the new storage/session continuation handoff.
- Documentation updates: `docs/developer/architecture/architecture.md` now links the
  new future storage/session continuation source-generation handoff, and
  `docs/memory-bank/chat.md` records the durable T003_12 state for future sessions.
- Remaining follow-ups: later source-generation tasks may implement these contracts
  with TDD; file-backed restart persistence remains deferred until a concrete
  continuation workflow proves the need.
- Verification: `git --no-pager diff --check` passes.
- Result: finalized.
