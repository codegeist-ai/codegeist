# T003_05 Define Runtime Session Event Source Generation Contract

Parent: `T003_define-codegeist-opencode-core-source-contracts`

Status: finalized

## Goal

Define a documentation-only source-generation contract for the first runtime,
session, and event Java slice before any Java source is created.

This task exists because the finalized T002 runtime/session/event blueprint is
rich enough to guide architecture, but it is not yet a narrow source-generation
contract for the first T003 implementation pass. The next safe step is another
documentation-only contract slice that selects the first Java boundary, test
contract, and non-goals without implementing Java yet.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement Java yet.
- Determine whether `T003_05` needs another documentation-only contract slice
  before source generation.

## Specification Decision

Yes: `T003_05` should be a documentation-only contract slice before Java source
generation.

The next source-generating task should not start directly from the broad
`runtime-session-event-contracts.md` blueprint. It should first receive a compact
handoff that names the first Java boundary, the minimum records and ports, the
runtime/session/event split, the TDD acceptance contract, and the explicit cuts
that stay out of the first source pass.

## Context

`T002_03` created
`docs/developer/specification/runtime-session-event-contracts.md` as a future Java
blueprint for prompt requests, agent modes, sessions, turns, message parts,
runtime event envelopes, event families, sequencing, and event-to-session
projection. That blueprint intentionally did not create Java source.

`T003_02` finalized
`docs/developer/specification/java-generation-guidance.md`, which defines package
ownership, dependency direction, framework boundaries, code shape, and future test
expectations. `T003_03` finalized
`docs/developer/specification/testing-strategy-and-agent-rules.md`, which makes
TDD the default and requires individually executable tests for future behavior
changes.

This task bridges those documents into one first-wave runtime/session/event
source-generation contract. It should sharpen scope without adding code.

## Scope

- Define the first source-generation boundary for runtime, session, and event
  contracts.
- Decide which planned Java concepts are in the first implementation slice and
  which remain later runtime work.
- Translate the existing runtime/session/event blueprint into a compact handoff
  for Java records, small ports, typed failures, event envelopes, and projections.
- Define the required TDD and verification contract for the later implementation
  task that will create Java source.
- Preserve CLI and TUI as future clients of runtime events and session
  projections, not owners of runtime/session/event behavior.
- Keep provider calls, tool execution, permission decisions, context loading,
  storage adapters, patch/edit, shell execution, and agent-loop orchestration out
  of this contract except as named downstream dependencies.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, CLI commands, TUI behavior, runtime services,
  provider calls, tool execution, storage adapters, shell execution, patch/edit
  behavior, or native/build behavior in this task.
- Do not reopen the broad T002 runtime/session/event architecture unless this
  specification finds a concrete contradiction.
- Do not copy OpenCode's TypeScript schema, Effect service, bus, sync, projector,
  route, or storage implementation shape.
- Do not decide provider streaming, tool-call mediation, permission approval,
  workspace validation, context assembly, or persistence behavior beyond the
  runtime/session/event placeholders required for a later source slice.
- Do not implement the planned Java file list, package structure, contract tests,
  or verification commands in this task; this task only plans and solves the
  documentation-only source-generation contract.

## Direct Inputs

- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_02_define_java_generation_guidance.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_03_define_testing_strategy_and_agent_test_rules.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_04_define_build_release_and_binary_smoke_strategy.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/runtime-vocabulary.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first runtime,
session, and event source-generation handoff. The preferred target is:

- `docs/developer/specification/runtime-session-event-source-generation-contract.md`

The guidance should include:

- The first source-generation boundary and why it is smaller than the full
  runtime/session/event blueprint.
- Planned package ownership for `ai.codegeist.runtime`, `ai.codegeist.session`,
  and `ai.codegeist.event`, clearly labeled as planned source.
- Minimum first-wave records, value objects, sealed families, ports, typed
  failures, and projection shapes.
- Boundary rules that prevent Spring AI, Spring Shell, Agent Utils, provider SDK,
  storage adapter, CLI, TUI, tool, permission, workspace, patch/edit, and shell
  types from leaking into core contracts.
- TDD handoff for the later Java implementation task, including the first narrow
  contract tests to create and the behavior each should prove.
- Explicit deferrals to later T003 tasks for provider streaming, tools,
  permissions, workspace/context loading, patch/edit, shell, storage,
  end-to-end agent loop, CLI workflow parity, and packaging/native validation.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, runtime behavior, or CLI/TUI behavior.
- The specification states that another documentation-only contract slice is
  needed before runtime/session/event source generation.
- The handoff separates first-wave runtime/session/event contracts from provider,
  tool, permission, workspace, context, storage, patch/edit, shell, CLI, and TUI
  implementation work.
- The handoff uses the finalized Java generation and testing strategy documents as
  constraints for future source generation.
- The handoff names a later implementation task shape that can use TDD and narrow
  individually executable tests, without adding those tests now.
- Planned package names and Java shapes are clearly labeled as planned, not
  current implementation.

## Implementation Plan

### Selected Option

Create one documentation-only source-generation contract at
`docs/developer/specification/runtime-session-event-source-generation-contract.md`.
Do not create a child implementation task or Java source yet, because the current
task already exists to bridge the broad T002 blueprint into a narrow handoff for
the first later source-generating runtime/session/event pass.

### Concrete Solution Direction

The new contract should be a compact implementation handoff, not another broad
architecture document. It should translate
`runtime-session-event-contracts.md`, `runtime-vocabulary.md`,
`java-generation-guidance.md`, and `testing-strategy-and-agent-rules.md` into the
first Java-ready boundary for runtime request intake, session/turn state, message
parts, event envelopes, minimal event payload families, typed failures, and client
projection shapes.

The document should keep all Java names clearly labeled as planned source. It
should identify the minimum records, enums, sealed interfaces, and ports a later
implementation task can create with TDD while keeping Spring Shell, Spring AI,
Agent Utils, provider SDK, storage, CLI, TUI, tool, permission, workspace,
patch/edit, and shell details outside the core contracts.

### Planned Files And Targets

- Add `docs/developer/specification/runtime-session-event-source-generation-contract.md`.
- Update this task with solve and finalization notes after the contract is
  written.
- Update `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
  during solve or finalization when the completed contract changes parent progress
  notes or the next T003 task boundary.
- Update `docs/memory-bank/chat.md` if the solve result changes active T003 state
  or the next recommended task for future sessions.
- Do not change Java source, Java tests, package directories, Maven files,
  Taskfiles, Spring configuration, runtime behavior, provider calls, tool
  execution, storage, CLI commands, TUI behavior, patch/edit, shell execution, or
  build/native behavior.

### Contract Document Structure

Use this structure unless the solve phase finds a clearer equivalent while
preserving the same scope:

1. Purpose and status: state that the document is a source-generation handoff for
   later Java work and does not implement Java source, tests, packages, runtime
   services, or behavior.
2. Current baseline: summarize that the implemented source is still the single
   Maven module under `app/codegeist/cli` with only `ai.codegeist.app` real today.
3. First-wave boundary: define the first runtime/session/event source slice as
   core contracts for request identity, mode, session identity, turn identity,
   message parts, event envelopes, event payloads, projection summaries, and typed
   contract failures.
4. Planned package ownership: map `ai.codegeist.runtime`, `ai.codegeist.session`,
   and `ai.codegeist.event` to planned records/interfaces/enums while keeping
   `ai.codegeist.cli`, `ai.codegeist.tui`, `ai.codegeist.provider`,
   `ai.codegeist.tool`, `ai.codegeist.permission`, `ai.codegeist.workspace`,
   `ai.codegeist.patch`, `ai.codegeist.shell`, and `ai.codegeist.storage` as
   clients, dependencies, or later owners.
5. Minimum planned Java shapes: name the first records/value objects/enums/sealed
   families, including `PromptRequest`, `PromptRequestId`, `CorrelationId`,
   `SourceClient`, `AgentMode`, `RuntimePromptPort`, `PromptAcceptance`,
   `SessionId`, `TurnId`, `PartId`, `Session`, `Turn`, `MessagePart`,
   `MessagePartType`, `SessionProjection`, `RuntimeEvent`, `EventEnvelope`,
   `EventId`, `EventType`, `EventSource`, `EventVisibility`, and a small
   `RuntimeContractFailure` family.
6. Event-family cut: include only the event names required for the first source
   pass, such as `SESSION_CREATED`, `TURN_STARTED`, `USER_INPUT`, `TURN_COMPLETED`,
   `SESSION_UPDATED`, `WARNING_RAISED`, and `ERROR_RAISED`. Mention provider,
   assistant streaming, context, tool, permission, shell, patch, storage, and
   compaction events as later expansions.
7. Sequencing and projection rules: require monotonic session event order,
   optional turn-local order, idempotent rendering by event id, append-oriented
   turns and parts, and a small projection shape that can feed CLI/TUI/server or
   Vaadin later without becoming source-of-truth state.
8. Boundary and dependency rules: explicitly forbid Spring Shell, Spring AI,
   Agent Utils, provider SDK, storage adapter, CLI, TUI, HTTP, Vaadin, PF4J,
   JBang, tool, permission, workspace, patch/edit, and shell types from core
   runtime/session/event contracts.
9. Illustrative Java sketches: include concise examples for the minimum records,
   sealed payload/failure family, and port shape. Mark them as examples only, not
   implemented source.
10. TDD handoff: define the exact first tests a later source task should add
    before code, including value-object validation, prompt acceptance without
    framework types, session/turn append ordering, event envelope sequencing,
    projection idempotence, and dependency-leak checks.
11. Deferral table: name the later T003 owners for provider streaming, context and
    workspace loading, tools, permissions, patch/edit, shell, storage,
    end-to-end agent loop, CLI workflow parity, and packaging/native validation.
12. Later implementation checklist: provide a short checklist the next Java source
    task must satisfy before creating contracts and tests.

### Later Source Task Shape

The later implementation task should be narrow and TDD-first. A suitable later
source-generating task after this documentation slice and the CLI prompt command
contract slice are solved would be to insert or rename a later child slot as:

```text
T003_06_implement_runtime_session_event_core_contracts
```

That later task should add the planned core contract tests and minimum Java
records/interfaces/enums under the current `app/codegeist/cli` Maven module before
CLI prompt command implementation, provider adapters, context loading, tools,
permissions, workspace policy, storage, patch/edit, shell, or TUI behavior. The
parent `T003_06` slot has been converted to a documentation-only CLI prompt command
source-generation contract, so source generation still waits for a later dedicated
implementation task.

### Implementation Steps

1. Re-read this task, the T003 parent, `runtime-session-event-contracts.md`,
   `runtime-vocabulary.md`, `java-generation-guidance.md`,
   `testing-strategy-and-agent-rules.md`, and `codegeist-opencode-parity.md`.
2. Create `runtime-session-event-source-generation-contract.md` with the structure
   above.
3. Start the document with a clear status note that it is planned source guidance,
   not current implementation.
4. Add a compact boundary diagram or class diagram that shows runtime request,
   session/turn/message-part, event envelope/payload, and projection relationships.
5. Define the first-wave Java shape table with planned package ownership and
   explicit dependency exclusions.
6. Define first-wave event types and projection rules that stop before provider,
   context, tool, permission, storage, CLI/TUI rendering, shell, and patch/edit
   behavior.
7. Add illustrative Java snippets only where they clarify the future source shape;
   keep them inside markdown.
8. Add the TDD handoff table with individually executable future Maven/JUnit test
   names and the behavior each test should prove.
9. Update this task, the T003 parent, and memory only where the completed contract
   changes durable task state or next-step guidance.

### Verification Strategy

- Required for this documentation-only plan and solve:

```bash
git --no-pager diff --check
```

- Do not run `task test`, `task build`, `task native`, or Maven commands unless
  Java source, tests, build files, Taskfiles, or runtime configuration are changed
  unexpectedly.

- The later Java implementation task should start with narrow plain JVM tests such
  as:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests test
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes test
```

Exact test class and method names may change in the later task, but they must stay
individually executable and must avoid Spring context startup unless Spring wiring
is the behavior under test.

### Dependencies And Tradeoffs

- Depends on finalized `T003_02`, `T003_03`, and the specified T003 parent runtime
  scope.
- Uses the T002 runtime/session/event blueprint as source evidence, but deliberately
  narrows it so the later Java task does not implement provider streaming, tools,
  permissions, workspace/context loading, storage, CLI/TUI behavior, patch/edit,
  shell execution, event sourcing, or bus/SSE transport too early.
- Keeps the first source-generation task small at the cost of adding one more
  documentation slice before Java code.

### Open Questions

None. The next solve phase may choose exact section wording and illustrative
snippet names while preserving this scope.

## Planning-Readiness Questions

- What is the smallest runtime/session/event contract slice that future Java
  source can implement without needing providers, tools, permissions, workspace,
  context, storage, CLI, or TUI behavior?
- Which identifiers, records, sealed event families, typed failures, ports, and
  projection records are required in the first Java source pass?
- Which parts of `runtime-session-event-contracts.md` are broad blueprint material
  that should stay deferred until later provider, tool, permission, storage, or
  agent-loop tasks?
- Which first tests should the later implementation task write before source, and
  how can those tests stay plain JVM and individually executable?
- How should the later source task prove that Spring AI, Spring Shell, Agent Utils,
  provider SDK, storage, CLI, and TUI types do not leak into core contracts?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: `/specify-task t003_05`.
- Context or instructions considered: user explicitly requested no Java
  implementation yet and asked whether this needs another documentation-only
  contract slice before source generation.
- Parent task considered:
  `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`.
- Adjacent child tasks considered: finalized `T003_02`, `T003_03`, and `T003_04`;
  the parent listed `T003_05_implement_runtime_session_event_core.md`, but that
  child task file did not exist before this pass.
- Dependency inputs considered:
  `docs/developer/specification/runtime-session-event-contracts.md`,
  `docs/developer/specification/runtime-vocabulary.md`,
  `docs/developer/specification/java-generation-guidance.md`,
  `docs/developer/specification/testing-strategy-and-agent-rules.md`, and
  `docs/developer/specification/codegeist-opencode-parity.md`.
- Discovered hints considered:
  `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md`. They were used only for
  specification boundaries and OpenCode-to-Codegeist translation posture, not as
  implementation instructions.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`,
  `.oc_local/rules/architecture-doc.md`, and
  `.oc_local/rules/third-party-analysis-workflow.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_05` as a documentation-only source-generation contract
  slice. Java implementation should wait until this contract is planned and
  solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, first-wave contract contents, deferral table, and future TDD handoff.
- Next recommended phase: `/plan-task t003_05` to define the concrete
  documentation plan for
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`.

## Planning Result

- Phase command: `/plan-task t003_05`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_05`
  result in this task.
- Duplicate check result: no existing
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`
  or separate matching implementation task exists.
- Related context files read: T003 parent task, finalized `T003_02`, `T003_03`,
  `T003_04`, `runtime-session-event-contracts.md`, `runtime-vocabulary.md`,
  `java-generation-guidance.md`, `testing-strategy-and-agent-rules.md`,
  `codegeist-opencode-parity.md`, and `.oc_local/rules/codegeist-task-specification.md`.
- Result: planned one documentation-only contract slice for
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`.
- Open decisions or blockers: none.
- Next recommended phase: `/solve-task t003_05`.

## Solution Note

- Phase command: `/solve-task t003_05`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/plan-task t003_05`
  result and implementation plan in this task.
- Files changed:
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`,
  this task, the T003 parent task, adjacent `T003_06`, and
  `docs/memory-bank/chat.md`.
- Result: solved the documentation-only source-generation handoff for the first
  runtime/session/event Java contract slice. The new specification narrows the
  broad runtime/session/event blueprint into planned packages, minimum Java shapes,
  event-family cut, sequencing and projection rules, boundary exclusions,
  illustrative snippets, TDD handoff, deferrals, and later implementation
  checklist.
- Acceptance criteria status: satisfied. No Java source, tests, package
  directories, build files, runtime behavior, CLI/TUI behavior, provider calls,
  tools, permissions, workspace reads, storage, patch/edit, or shell behavior were
  created.
- Verification: `git --no-pager diff --check` passed after finalization updates.
- Open decisions or blockers: none.
- Next recommended phase: completed by `/finalize-task t003_05`.

## Finalization Result

- Phase command: `/finalize-task t003_05`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Upstream phase dependency: satisfied by the successful solve result above.
- Impacted tasks: updated the T003 parent progress notes and adjacent `T003_06`
  through `T003_12` context so future source-generation contract planning consumes
  the finalized runtime/session/event handoff instead of treating it as pending.
- Documentation updates: created
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`
  and refreshed `docs/memory-bank/chat.md` because future sessions need to know
  that `T003_05` is finalized and `T003_06` now depends on this completed handoff.
- Remaining follow-ups: the next workflow task is to plan and solve `T003_06` as a
  documentation-only CLI prompt command source-generation contract. A later
  dedicated Java implementation task should create the runtime/session/event core
  contracts with the TDD handoff from the new specification before CLI command
  implementation.
- Verification: `git --no-pager diff --check` passed.
- Result: finalized.

## Creation Note

Created from the T003 parent child slot after the user paused Java implementation
and requested a specification decision about whether another documentation-only
contract slice is needed before source generation.
