# T001_07 Define Event Model

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the event types emitted during a Codegeist session.

This task defines conceptual runtime events for session execution and client
rendering. It does not implement an event bus, event sourcing, persistence,
server streaming, or UI rendering.

## Architecture Decision

Codegeist should emit typed runtime events from the central runtime path. Events
are the shared contract for CLI output now and future server/Vaadin/TUI streaming
later. Events must carry enough structure for user-visible rendering,
audit-relevant records, and later storage projections without making clients own
session state.

The initial model should distinguish runtime events from storage events. Runtime
events describe what happened while a prompt runs. Storage may later persist an
event log or derived projections, but that decision belongs to `T001_19`.

For the OpenCode-to-Java migration, the important translation is not OpenCode's
`Bus`, `SyncEvent`, SSE route, or TypeScript schema machinery. The important
translation is a typed, ordered, session-scoped event contract that lets all
clients observe the same runtime work while the runtime remains the only writer
of session state.

## Scope

- Define user input, assistant output, tool request, permission request, tool
  result, provider response, warning, and error events.
- Decide which events are user-visible.
- Decide which events are audit-relevant.

## OpenCode Reference Behaviors

Use OpenCode as a feature reference for:

- Session-oriented events that let multiple clients render the same activity.
- Ordered events with session or aggregate identity.
- Message parts, tool activity, costs/tokens, snapshots, and projected state as
  related but distinct concerns.
- Sync/event-sourcing ideas for later replayability, without requiring them in
  the MVP model.

Do not copy OpenCode's TypeScript `Bus` or `SyncEvent` implementation. Codegeist
needs Java event types that can start as in-process runtime callbacks and later
map to server streams, Vaadin updates, storage projections, or audit records.

## Migration Questions This Task Must Answer

| Migration question | Decision for Codegeist | Follow-up owner |
| --- | --- | --- |
| What is the Java equivalent of OpenCode bus/sync events? | A typed `RuntimeEvent` hierarchy with a stable envelope and event-specific payloads. | Runtime/event implementation |
| Are events commands, state mutations, or observations? | Observations emitted by runtime-owned operations. Clients consume events but do not publish state-changing events. | Runtime boundary |
| Should the MVP use event sourcing? | No. Events are event-sourcing-friendly but not the source of truth by default. | Storage in `T001_19` |
| How is event order guaranteed? | Runtime assigns monotonic sequence numbers per session, and optionally per turn, before publishing. | Event runtime |
| How do events relate to sessions and turns? | Every prompt-run event carries `sessionId`; turn-scoped events also carry `turnId`. | `T001_06` |
| How does streaming map to durable state? | Streaming text is represented by transient `assistant.delta` events; durable text is captured by final message parts/projections. | Session/event boundary |
| How are tool and permission pairs connected? | Correlation ids link request, approval, execution, result, and failure events. | `T001_09`, `T001_10` |
| How are audit events separated from UI rendering? | Event metadata marks visibility and audit relevance; storage policy decides retention later. | `T001_19` |
| How do server/Vaadin/TUI consume events later? | They subscribe to the same conceptual event stream through adapters, not through client-specific session mutation APIs. | `T001_17`, `T001_18` |
| How are provider-specific errors represented? | Provider adapters emit typed provider/runtime error events instead of leaking SDK exceptions. | `T001_08` |

## Event Envelope

Every event should have a stable envelope before event-specific payload fields:

| Field | Purpose | Required first? |
| --- | --- | --- |
| `eventId` | Stable id for rendering, correlation, and later persistence. | Yes |
| `type` | Conceptual event name such as `session.started` or `tool.result`. | Yes |
| `occurredAt` | Runtime timestamp. | Yes |
| `sessionId` | Owning session aggregate. | Yes |
| `turnId` | Owning turn when the event belongs to a prompt run. | Usually |
| `sequence` | Monotonic order within the session or turn. | Yes |
| `source` | Runtime, provider, tool, permission, context, client, or extension. | Yes |
| `visibility` | User-visible, internal, or audit-only. | Yes |
| `auditRelevant` | Marks events that must be retained when audit storage exists. | Yes |
| `correlationId` | Connects tool request/result, permission request/decision, or provider stream chunks. | Yes for paired events |
| `summary` | Short redacted text safe for display/logging. | Yes |

## Java Event Shape

The first Java implementation should prefer explicit domain event types over
generic maps or provider/client DTOs:

| Java concept | Role | Notes |
| --- | --- | --- |
| `RuntimeEvent` | Sealed interface or equivalent event base | Exposes the common envelope. |
| `EventId` | Stable event identity | Useful for replay, deduplication, rendering, and storage. |
| `EventType` | Typed event name | Prefer enum or stable string constants at the boundary. |
| `EventSequence` | Monotonic order | Assigned by the runtime before publication. |
| `EventSource` | Origin classification | Initial values: `runtime`, `client`, `context`, `provider`, `tool`, `permission`, `workspace`, `storage`, `extension`. |
| `EventVisibility` | Rendering classification | Initial values: `user_visible`, `internal`, `audit_only`. |
| `CorrelationId` | Cross-event relation | Links provider request/response, tool request/result, permission request/decision, or streaming chunks. |
| `RuntimeEventPublisher` | Port for publishing events | Runtime depends on this port, not on a concrete bus or transport. |
| `RuntimeEventSubscriber` | Port for consuming events | CLI/server/Vaadin adapters can consume through this boundary later. |

Event payloads should be typed records/classes. They should not expose Spring AI,
Spring Shell, Vaadin, HTTP, PF4J, JBang, or raw tool implementation classes.

## Ordering And Correlation Rules

- Runtime assigns `sequence` after validating the event and before publishing it.
- Session-scoped events must be ordered monotonically within a session.
- Turn-scoped events should also carry turn-local ordering when it helps render a
  single prompt run.
- Events emitted for a tool request must share a correlation id across
  `tool.requested`, optional permission events, `tool.started`, and
  `tool.result` or `tool.failed`.
- Provider streaming chunks should share a correlation id with the provider
  request and final assistant message.
- Permission decisions should reference the permission request and the tool or
  capability request they decide.
- Events must be idempotent for display: replaying an already-seen `eventId`
  should not duplicate terminal or UI output.
- The runtime is the only component allowed to publish state-transition events
  such as `session.updated`, `turn.started`, or `turn.completed`.

## Event Families

| Family | Event examples | Primary consumer | Notes |
| --- | --- | --- | --- |
| Session lifecycle | `session.created`, `session.updated`, `session.completed` | CLI/server/Vaadin/storage later | Mirrors session aggregate transitions without exposing mutable aggregate internals. |
| Turn lifecycle | `turn.started`, `turn.completed` | CLI/server/Vaadin/storage later | Defines prompt-run boundaries. |
| User input | `user.input` | Audit/storage later, optional UI | Must be redacted or summarized according to prompt policy. |
| Context | `context.loaded`, later `context.skipped` | UI/debugging | Carries context source summaries, not full file payloads. |
| Provider | `provider.requested`, later `provider.failed`, `provider.completed` | UI/debugging/audit optional | Maps Spring AI/provider adapter activity into runtime terms. |
| Assistant output | `assistant.delta`, `assistant.message` | CLI/server/Vaadin | Deltas are rendering-oriented; final message can align with session parts. |
| Tool | `tool.requested`, `tool.started`, `tool.result`, `tool.failed` | UI/audit/storage later | Exact payloads belong to tool architecture. |
| Permission | `permission.requested`, `permission.decided` | UI/audit/storage later | Policy and caching belong to permission architecture. |
| Warning/error | `warning.raised`, `error.raised` | UI/audit depending on severity | Must carry typed source and recoverability. |
| Extension | Later plugin/JBang contribution events | UI/audit optional | Must never bypass runtime, tool, permission, or workspace policy. |

## Initial Event Types

| Event | Purpose | User-visible | Audit-relevant | Notes |
| --- | --- | --- | --- | --- |
| `session.created` | A session aggregate was created. | Optional | Yes | Captures mode, workspace, and source client. |
| `session.updated` | Session title/status/mode metadata changed. | Optional | Yes when status or mode changes | Does not expose full mutable session object by default. |
| `session.completed` | Session reached a completed state. | Yes | Yes | May be derived from the final turn in early MVP. |
| `turn.started` | Runtime accepted a prompt and began a turn. | Yes | Yes | Links prompt, mode, source client, and workspace. |
| `user.input` | User prompt was received. | Optional | Yes | Store/display redacted prompt summary where needed. |
| `context.loaded` | Runtime selected deterministic context. | Optional | Optional | Lists source summaries, not entire context payloads. |
| `provider.requested` | Runtime is about to call a provider/model. | Optional | Optional | Useful for diagnostics and later cost attribution. |
| `assistant.delta` | Streaming assistant text chunk. | Yes | No by default | Rendering event; final message part may be persisted separately. |
| `assistant.message` | Assistant response part completed. | Yes | Optional | Records final text or structured response summary. |
| `tool.requested` | Runtime/model requested tool execution. | Yes | Yes | Includes tool name, classified capability, and argument summary. |
| `permission.requested` | Runtime requires approval before side effect. | Yes | Yes | Client renders prompt; permission model owns policy. |
| `permission.decided` | Approval or denial was recorded. | Yes | Yes | Includes scope and decision source, not raw secrets. |
| `tool.started` | Approved tool execution began. | Yes | Yes for side-effecting tools | Links to permission decision when present. |
| `tool.result` | Tool returned successfully. | Yes | Yes for side-effecting tools | Include redacted output summary and artifact refs. |
| `tool.failed` | Tool execution failed. | Yes | Yes | Include typed error code and recoverability. |
| `warning.raised` | Runtime detected non-fatal issue. | Yes | Optional | Examples: skipped ignored file, degraded terminal output. |
| `error.raised` | Runtime, provider, tool, or permission failure occurred. | Yes | Yes when it affects execution or audit | Typed error with source and recoverability. |
| `turn.completed` | Prompt turn finished. | Yes | Yes | Captures final status, created parts, and summary refs. |

## Event Flow For A Prompt

The normal Build-mode prompt flow should be expressible as:

```text
session.created?
turn.started
user.input
context.loaded
provider.requested
assistant.delta*
tool.requested?
permission.requested?
permission.decided?
tool.started?
tool.result? / tool.failed?
assistant.delta*
assistant.message
turn.completed
session.updated? / session.completed?
```

Plan mode uses the same event shape but should not emit side-effecting
`tool.started` events unless the tool is classified as read-only and allowed by
the active mode.

Failure and approval flows should remain explicit:

```text
turn.started
user.input
context.loaded?
provider.requested
tool.requested
permission.requested
permission.decided(denied)
tool.failed? or warning.raised?
assistant.message?
turn.completed(error or completed)
```

```text
turn.started
provider.requested
error.raised(provider)
turn.completed(error)
session.updated?
```

These flows are examples only. Exact payloads and terminal/server transport are
later implementation details.

## Visibility And Audit Rules

- User-visible events must be enough for Spring Shell to render streaming text,
  tool activity, permission prompts, warnings, errors, and completion.
- Server, Vaadin, and future TUI clients should consume the same conceptual
  events instead of receiving client-specific session mutations.
- Audit-relevant events include session creation/status changes, mode selection,
  user input summaries, tool requests, permission requests/decisions,
  side-effecting tool starts/results/failures, and execution-stopping errors.
- High-volume streaming text deltas are user-visible but not audit-relevant by
  default; the final assistant message part can carry durable response text.
- Event payloads should carry redacted summaries or artifact references for
  large or sensitive data.

## Boundary Rules

- Runtime publishes state-transition events; clients and adapters subscribe and
  render.
- Clients may submit runtime requests, approval answers, or prompts through
  runtime APIs, but they must not publish session/turn/tool state events.
- Provider adapters may emit provider-scoped outcomes through runtime ports, but
  they must not expose provider SDK objects in public event payloads.
- Tool implementations may return results to the runtime; the runtime converts
  them into events after permission and workspace checks.
- Permission policy owns approval decisions; events record request and decision
  facts for rendering and later audit.
- Storage may persist events or projections later; the event model must not depend
  on a concrete database, log, SSE endpoint, WebSocket, Reactor type, or Vaadin
  push mechanism.
- Extension-originated events must be mediated by the runtime and classified by
  source, visibility, and audit relevance.

## Persistence Candidates

Persistence belongs to `T001_19`, but the event model should identify likely
durable records:

- Audit-relevant session lifecycle, mode selection, user input summaries,
  permission request/decision, and side-effecting tool events.
- Turn start/completion records needed to reconstruct execution history.
- Error and warning events that explain failed or degraded execution.
- Provider/model identifiers and token/cost summaries once provider telemetry is
  available.
- Artifact references for tool output, patches, shell logs, or generated files
  when those are too large or sensitive to embed.
- Optional event log for replay/sync if storage later chooses an event-sourced or
  event-log-backed design.

## Acceptance Questions For Implementation Readiness

Before implementation starts, this task should make these answers clear:

- Can the runtime emit a typed event without importing Spring Shell, Vaadin, HTTP,
  or provider SDK DTOs?
- Can a CLI renderer subscribe to events and render a complete prompt run without
  reading or mutating session internals?
- Can approval prompts and decisions be correlated with the exact tool or
  capability request they decide?
- Can high-volume assistant deltas be rendered without forcing durable storage of
  every chunk?
- Can audit storage later identify which events must be retained without guessing
  from event names alone?
- Can server/Vaadin transports later project the same event stream without
  inventing alternate event types?

## Non-Goals

- Do not choose between Spring application events, Reactor, Java Flow, custom
  callbacks, server-sent events, or WebSocket transport yet.
- Do not implement event sourcing or replay in this task.
- Do not define storage schema; that belongs to `T001_19`.
- Do not define exact permission or tool payload schemas; those belong to
  `T001_09` and `T001_10`.
- Do not require Vaadin, server, or TUI implementation now.

## Open Questions

- Should MVP event publication be synchronous callbacks, an internal queue, Spring
  application events, Reactor, or another abstraction?
- Should `sequence` be per session only, or both per session and per turn?
- Which events must be persisted before the first usable CLI workflow, if any?
- Should user prompts be included as event payloads, redacted summaries, or only
  references to session parts?
- How should dropped/slow subscribers be handled once server or Vaadin streaming
  exists?
- Should provider token/cost telemetry be emitted as event metadata, assistant
  message metadata, or separate telemetry events?

## Deliverable

Add `## Event Model` to `docs/developer/codegeist-opencode-parity.md` with:

- OpenCode-to-Java migration questions,
- event envelope fields,
- Java event shape,
- ordering and correlation rules,
- initial event types,
- event families,
- user-visible and audit-relevant classification,
- a normal prompt event flow,
- boundary rules,
- persistence candidates,
- implementation-readiness questions,
- explicit non-goals.

## Acceptance Criteria

- Event types are explicit and typed conceptually.
- Events can support CLI output now and server/Vaadin streaming later.
- Permission and tool events are represented.
- User-visible and audit-relevant events are separated.
- Events reference session and turn identity without making clients mutate
  session state directly.
- The event model aligns with `T001_06` session concepts and can support the
  later prompt-flow task.
- The task answers the OpenCode-to-Java migration questions needed to implement a
  Java event domain later.
- Ordering, correlation, visibility, and audit metadata are explicit.
- Event payloads remain independent from provider SDK, UI, transport, storage,
  tool implementation, and permission cache types.

## Verification

- Check that the end-to-end prompt flow can be expressed with these events.

## Verification Result

- Added `## Event Model` to the parity document.
- Expressed a normal prompt flow from turn start through context loading,
  provider streaming, optional tool/permission activity, assistant completion,
  and turn/session completion.
- Kept transport, event sourcing, storage schema, tool schema, and permission
  schema as later decisions.
- Expanded the task with migration questions, Java event shape, ordering and
  correlation rules, event families, boundary rules, persistence candidates, and
  implementation-readiness acceptance questions.
