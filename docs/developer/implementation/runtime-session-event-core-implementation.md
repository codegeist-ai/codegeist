# Runtime Session Event Core Implementation Plan

Planning handoff for `T004_01`: implement the first Java runtime, session, and
event core contracts with plain JVM tests before any CLI, provider, tool,
workspace, storage, or UI behavior exists.

## Source Task

- Task: `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`
- Parent: `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`
- Primary contract: `docs/developer/specification/runtime-session-event-source-generation-contract.md`
- Supporting context: `docs/developer/specification/runtime-session-event-contracts.md`, `docs/developer/specification/java-generation-guidance.md`, `docs/developer/specification/testing-strategy-and-agent-rules.md`, and `docs/developer/architecture/architecture.md`

## Goal

Create the first source-backed Codegeist contracts for prompt intake, sessions,
turns, message parts, runtime events, ordered event envelopes, typed contract
failures, and client-safe session projections.

The implementation should stay inside `app/codegeist/cli`, use Java records,
enums, sealed interfaces, and small boundary interfaces, and avoid Spring context
startup in the new tests.

## Scope

- Add the first `ai.codegeist.runtime`, `ai.codegeist.session`, and
  `ai.codegeist.event` Java packages.
- Add value-object identifiers, enums, records, sealed payload/failure families,
  a minimal prompt acceptance port, and a small projection helper.
- Add plain JVM contract tests that prove the initial public contract and failure
  behavior.
- Update `docs/developer/architecture/architecture.md` during the later solve
  phase after the planned Java packages and tests exist.

## Non-Goals

- No Spring Shell commands, CLI rendering, TUI, server, Vaadin, PF4J, or JBang.
- No Spring AI prompts, provider adapters, live model calls, Agent Utils callback
  registration, or provider streaming.
- No context loading, workspace path validation, file reads, patch/edit behavior,
  shell execution, tools, permissions, storage adapters, event bus, SSE, or
  persistence.
- No Maven module split, build-file changes, native-image checks, network checks,
  or startup-heavy tests.

## Planned Class Diagram

Every planned production and test class, record, interface, enum, sealed
interface, and failure type for this task is included here.

```mermaid
classDiagram
    namespace ai.codegeist.runtime {
        class PromptRequestId { <<record>> String value }
        class CorrelationId { <<record>> String value }
        class WorkspaceRef { <<record>> String value }
        class AgentMode { <<enum>> PLAN BUILD REVIEW }
        class SourceClient { <<enum>> CLI TUI SERVER VAADIN EXTENSION SYSTEM }
        class Recoverability { <<enum>> RECOVERABLE TERMINAL }
        class PromptRequest { <<record>> PromptRequestId id; AgentMode mode; Optional~SessionId~ sessionId; WorkspaceRef workspace; SourceClient source; String promptText; Instant requestedAt; CorrelationId correlationId }
        class PromptAcceptance { <<record>> PromptRequestId requestId; SessionId sessionId; TurnId turnId; AgentMode acceptedMode; List~RuntimeEvent~ initialEvents; SessionProjection projection }
        class RuntimePromptPort { <<interface>> accept(PromptRequest) PromptAcceptance }
        class PromptRequestValidator { <<class>> validate(PromptRequest) Optional~RuntimeContractFailure~ }
        class RuntimeContractFailure { <<sealed interface>> redactedMessage() String; recoverability() Recoverability }
        class InvalidPromptRequest { <<record>> String redactedMessage; Recoverability recoverability }
        class InvalidIdentifier { <<record>> String redactedMessage; Recoverability recoverability }
        class InvalidSequence { <<record>> String redactedMessage; Recoverability recoverability }
        class ProjectionConflict { <<record>> String redactedMessage; Recoverability recoverability }
        class UnsupportedMode { <<record>> String redactedMessage; Recoverability recoverability }
    }

    namespace ai.codegeist.session {
        class SessionId { <<record>> String value }
        class TurnId { <<record>> String value }
        class PartId { <<record>> String value }
        class SessionStatus { <<enum>> ACTIVE COMPLETED FAILED ARCHIVED }
        class TurnStatus { <<enum>> ACCEPTED RUNNING COMPLETED FAILED }
        class MessagePartType { <<enum>> USER_PROMPT ASSISTANT_SUMMARY DIAGNOSTIC }
        class MessagePart { <<record>> PartId id; long sequence; MessagePartType type; String summary; Instant createdAt }
        class Turn { <<record>> TurnId id; long sequence; AgentMode mode; TurnStatus status; List~MessagePart~ parts; Instant startedAt }
        class Session { <<record>> SessionId id; SessionStatus status; AgentMode defaultMode; List~Turn~ turns; Instant createdAt; Instant updatedAt }
        class MessagePartProjection { <<record>> PartId id; long sequence; MessagePartType type; String summary }
        class TurnProjection { <<record>> TurnId id; long sequence; TurnStatus status; List~MessagePartProjection~ parts }
        class SessionProjection { <<record>> SessionId sessionId; SessionStatus status; List~TurnProjection~ turns; List~RuntimeEvent~ recentEvents }
        class SessionProjector { <<class>> project(Session, List~RuntimeEvent~) SessionProjection }
    }

    namespace ai.codegeist.event {
        class EventId { <<record>> String value }
        class EventType { <<enum>> SESSION_CREATED SESSION_UPDATED TURN_STARTED USER_INPUT TURN_COMPLETED WARNING_RAISED ERROR_RAISED }
        class EventSource { <<enum>> RUNTIME SESSION CLIENT SYSTEM }
        class EventVisibility { <<enum>> USER_VISIBLE INTERNAL AUDIT }
        class EventEnvelope { <<record>> EventId id; EventType type; SessionId sessionId; Optional~TurnId~ turnId; long sessionSequence; OptionalLong turnSequence; EventSource source; EventVisibility visibility; boolean auditRelevant; Optional~CorrelationId~ correlationId; Instant occurredAt; String summary }
        class RuntimeEvent { <<record>> EventEnvelope envelope; EventPayload payload }
        class EventPayload { <<sealed interface>> }
        class SessionCreated { <<record>> SessionId sessionId }
        class SessionUpdated { <<record>> SessionStatus status }
        class TurnStarted { <<record>> TurnId turnId; AgentMode mode }
        class UserInputAccepted { <<record>> String redactedSummary }
        class TurnCompleted { <<record>> TurnId turnId; TurnStatus status }
        class WarningRaised { <<record>> String redactedMessage }
        class ErrorRaised { <<record>> String redactedMessage; Recoverability recoverability }
    }

    namespace ai.codegeist.runtime.tests {
        class RuntimeSessionEventContractTests { acceptsPromptWithoutFrameworkTypes(); rejectsBlankPromptWithTypedFailure(); appendsTurnsAndPartsInOrder(); assignsMonotonicSessionEventSequence(); projectsEventsIdempotentlyByEventId() }
        class RuntimeSessionEventDependencyTests { coreContractsDoNotExposeFrameworkTypes() }
    }

    RuntimePromptPort --> PromptRequest
    RuntimePromptPort --> PromptAcceptance
    PromptRequest --> PromptRequestId
    PromptRequest --> AgentMode
    PromptRequest --> SessionId
    PromptRequest --> WorkspaceRef
    PromptRequest --> SourceClient
    PromptRequest --> CorrelationId
    PromptRequestValidator --> RuntimeContractFailure
    RuntimeContractFailure <|.. InvalidPromptRequest
    RuntimeContractFailure <|.. InvalidIdentifier
    RuntimeContractFailure <|.. InvalidSequence
    RuntimeContractFailure <|.. ProjectionConflict
    RuntimeContractFailure <|.. UnsupportedMode
    RuntimeContractFailure --> Recoverability
    PromptAcceptance --> SessionId
    PromptAcceptance --> TurnId
    PromptAcceptance --> RuntimeEvent
    PromptAcceptance --> SessionProjection
    Session "1" --> "0..*" Turn
    Turn "1" --> "0..*" MessagePart
    SessionProjection "1" --> "0..*" TurnProjection
    SessionProjection "1" --> "0..*" RuntimeEvent
    TurnProjection "1" --> "0..*" MessagePartProjection
    SessionProjector --> Session
    SessionProjector --> RuntimeEvent
    SessionProjector --> SessionProjection
    RuntimeEvent --> EventEnvelope
    RuntimeEvent --> EventPayload
    EventPayload <|.. SessionCreated
    EventPayload <|.. SessionUpdated
    EventPayload <|.. TurnStarted
    EventPayload <|.. UserInputAccepted
    EventPayload <|.. TurnCompleted
    EventPayload <|.. WarningRaised
    EventPayload <|.. ErrorRaised
```

## File Map

Production files to add in the solve phase:

```text
app/codegeist/cli/src/main/java/ai/codegeist/runtime/
  AgentMode.java
  CorrelationId.java
  InvalidIdentifier.java
  InvalidPromptRequest.java
  InvalidSequence.java
  ProjectionConflict.java
  PromptAcceptance.java
  PromptRequest.java
  PromptRequestId.java
  PromptRequestValidator.java
  Recoverability.java
  RuntimeContractFailure.java
  RuntimePromptPort.java
  SourceClient.java
  UnsupportedMode.java
  WorkspaceRef.java

app/codegeist/cli/src/main/java/ai/codegeist/session/
  MessagePart.java
  MessagePartProjection.java
  MessagePartType.java
  PartId.java
  Session.java
  SessionId.java
  SessionProjection.java
  SessionProjector.java
  SessionStatus.java
  Turn.java
  TurnId.java
  TurnProjection.java
  TurnStatus.java

app/codegeist/cli/src/main/java/ai/codegeist/event/
  ErrorRaised.java
  EventEnvelope.java
  EventId.java
  EventPayload.java
  EventSource.java
  EventType.java
  EventVisibility.java
  RuntimeEvent.java
  SessionCreated.java
  SessionUpdated.java
  TurnCompleted.java
  TurnStarted.java
  UserInputAccepted.java
  WarningRaised.java
```

Test files to add in the solve phase:

```text
app/codegeist/cli/src/test/java/ai/codegeist/runtime/
  RuntimeSessionEventContractTests.java
  RuntimeSessionEventDependencyTests.java
```

Documentation files to update in the solve phase after source exists:

```text
docs/developer/architecture/architecture.md
docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md
```

No `pom.xml`, `Taskfile.yml`, `application.yaml`, or Spring configuration changes
are planned for this slice.

## Implementation Steps

1. Add `RuntimeSessionEventContractTests` with the first failing method
   `acceptsPromptWithoutFrameworkTypes` and run the method-level Maven selector.
2. Add the smallest runtime, session, and event records/enums needed for that test
   to compile and pass.
3. Add `rejectsBlankPromptWithTypedFailure`, then implement
   `RuntimeContractFailure`, its permitted records, `Recoverability`, and
   `PromptRequestValidator`.
4. Add `appendsTurnsAndPartsInOrder`, then enforce append-oriented, monotonic
   sequence validation in focused constructors or static factories.
5. Add `assignsMonotonicSessionEventSequence`, then enforce positive event
   sequence rules for `EventEnvelope`.
6. Add `projectsEventsIdempotentlyByEventId`, then implement `SessionProjector` to
   deduplicate replayed `EventId` values and reject mismatched sessions with
   `ProjectionConflict`.
7. Add `RuntimeSessionEventDependencyTests` to assert public contract signatures
   in the three new packages do not expose forbidden framework or deferred-surface
   packages.
8. Run targeted tests, then the existing full Maven test command for the CLI
   module.
9. Update `docs/developer/architecture/architecture.md` to move
   `ai.codegeist.runtime`, `ai.codegeist.session`, and `ai.codegeist.event` from
   planned-only to implemented, including their test coverage and non-goals.
10. Update the task solve result with targeted commands, approximate timings,
    startup-sensitive check status, architecture update summary, and next phase.

## TDD Sequence

First failing test:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes test
```

Expected failure before implementation: the test class or planned production types
do not compile.

Targeted implementation commands for the solve phase:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes test
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests test
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventDependencyTests test
```

Broader affected verification after targeted tests pass:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress test
```

Startup-sensitive posture: the new tests must be plain JVM tests and must not load
Spring. The existing `CodegeistApplicationTests` remains the only Spring context
test in the broad Maven suite.

## Acceptance Criteria

- `RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes` proves a
  prompt can be accepted through Codegeist-owned contracts without forbidden
  framework or deferred-surface types.
- `RuntimeSessionEventContractTests#rejectsBlankPromptWithTypedFailure` proves
  blank prompt text maps to `InvalidPromptRequest` with a redacted message and
  recoverability metadata.
- `RuntimeSessionEventContractTests#appendsTurnsAndPartsInOrder` proves sessions,
  turns, and message parts are append-oriented and monotonic.
- `RuntimeSessionEventContractTests#assignsMonotonicSessionEventSequence` proves
  event envelopes preserve positive, monotonic session order and optional turn
  order.
- `RuntimeSessionEventContractTests#projectsEventsIdempotentlyByEventId` proves
  replayed event ids do not duplicate client-visible projection events.
- `RuntimeSessionEventDependencyTests#coreContractsDoNotExposeFrameworkTypes`
  proves the three new core packages keep forbidden framework and deferred-surface
  types out of public contract signatures.
- `docs/developer/architecture/architecture.md` accurately describes the newly
  implemented packages and tests after the solve phase.

## Dependencies

- Satisfied: `T003_05` finalized
  `runtime-session-event-source-generation-contract.md`.
- Satisfied: `T003_02` finalized Java generation guidance.
- Satisfied: `T003_03` finalized testing strategy and agent rules.
- Satisfied: current architecture doc confirms only `ai.codegeist.app` exists now,
  so this task owns the first additional Java packages.

## Tradeoffs And Risks

- `WorkspaceRef` is a minimal boundary value because `PromptRequest` needs a typed
  workspace reference. It intentionally performs no path validation or
  repository-specific context loading.
- `RuntimePromptPort` is a small boundary interface so later CLI prompt commands
  can delegate to runtime contracts without owning session mutation.
- `SessionProjector` is the only planned behaviorful class beyond validation. It
  is included because idempotent projection is an acceptance criterion and should
  be tested before storage or event bus work exists.
- The first source slice is intentionally more contract-heavy than service-heavy;
  orchestration, provider streaming, tool mediation, storage, CLI, and TUI behavior
  remain later T004 tasks.

## Open Questions

None.

## Plan Workflow Handoff

- Phase command: `/plan-task t004_01`.
- User context considered: `t004_01`.
- Resolved source task: `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`.
- Parent task: `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Selected option: sharpen the existing `T004_01` implementation task; no new task
  was needed.
- Duplicate check result: no existing implementation plan file was present under
  `docs/developer/implementation/`, and `T004_01` is already the matching concrete
  implementation slice.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read:
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`,
  `docs/developer/specification/runtime-session-event-contracts.md`,
  `docs/developer/specification/java-generation-guidance.md`,
  `docs/developer/specification/testing-strategy-and-agent-rules.md`,
  `docs/developer/architecture/architecture.md`, `app/codegeist/cli/pom.xml`,
  `CodegeistApplication.java`, and `CodegeistApplicationTests.java`.
- Upstream phase dependency: satisfied by the existing `Status: specified` and the
  specification check result in `T004_01`.
- Result: one implementation-ready plan for the runtime/session/event core.
- Recommended next phase: `/solve-task t004_01`.

## Agent Utils Planning Recheck

- Agent Utils equivalent: `TaskCall`, `BackgroundTask`, and `TodoWriteTool` are
  concept references only.
- Plan decision: keep the existing Codegeist runtime/session/event file map, class
  diagram, TDD sequence, and verification commands unchanged.
- Solve constraint: do not expose Agent Utils task, todo, subagent, or background
  task types through `ai.codegeist.runtime`, `ai.codegeist.session`, or
  `ai.codegeist.event`.
- Result: the plan remains implementation-ready.
