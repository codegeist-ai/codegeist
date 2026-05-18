# T001_06 Define Session Model

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the core session concepts for Codegeist.

This task defines the domain model for user work over time. It does not
implement persistence, event streaming, provider calls, tools, or client APIs.

## Architecture Decision

Codegeist sessions are runtime-owned aggregates. CLI, future server APIs, and
future Vaadin clients can create, continue, inspect, and render sessions, but
they must not own session state transitions. A session records the selected agent
mode, workspace identity, ordered turns, message parts, tool interactions,
permission requests, results, errors, and enough metadata to support later event
streaming, audit trails, and persistence.

For the OpenCode-to-Java migration, the important translation is not OpenCode's
HTTP route or TypeScript schema shape. The important translation is the invariant
that all user work is anchored in a session, and every prompt, tool request,
permission decision, provider response, and recoverable failure can be related
back to a deterministic session/turn history.

## Scope

- Define session, turn, prompt, assistant response, tool call, approval, result,
  and error.
- Decide which fields are required in the first model.
- Identify what must be persisted later.

## OpenCode Reference Behaviors

Use OpenCode as a feature reference for:

- Session-oriented prompt processing.
- Message parts as the unit for prompts, assistant text, tool calls, tool
  results, errors, costs, tokens, snapshots, and projected state.
- Event streams that let CLI/TUI/server/web clients render the same runtime
  activity.
- Later session lifecycle operations such as list, continue, abort, compact,
  share, revert, and inspect files.

Do not copy OpenCode's TypeScript storage or API shape. Codegeist needs a Java
domain model that can start in memory and later persist through storage ports.

## Migration Questions This Task Must Answer

| Migration question | Decision for Codegeist | Follow-up owner |
| --- | --- | --- |
| What is the Java equivalent of OpenCode's session? | A runtime-owned `Session` aggregate with stable identity, workspace identity, current lifecycle status, selected/default mode, and ordered turns. | Runtime/session implementation |
| What is the Java equivalent of OpenCode messages and parts? | A `Turn` contains typed `MessagePart` records for prompt, assistant text, tool call, approval link, result, warning, error, and later summary/snapshot parts. | Session model, event model |
| Are sessions API resources, UI state, or domain objects? | Domain objects first. CLI/server/Vaadin expose or render them through adapters, but do not own state transitions. | Runtime boundary |
| Does a session own provider/tool/permission behavior? | No. It records references and outcomes; provider, tool, permission, workspace, and storage services own their own policy. | `T001_08`, `T001_09`, `T001_10`, `T001_11`, `T001_19` |
| How much OpenCode lifecycle behavior enters MVP? | Create, continue, append turn, wait for approval, complete, abort, and fail are first-class. Compact, summarize, fork, share, revert, unrevert, delete parts, and restore are later capabilities. | MVP cut in `T001_22` |
| Should the first model assume event sourcing? | No. The model should be event-friendly but not event-sourced by default. Event log versus projection is a storage decision. | `T001_07`, `T001_19` |
| Should the first model assume persistent storage? | No. It may start in memory, but IDs, ordering, redaction boundaries, and lifecycle statuses must be persistence-ready. | `T001_19` |
| How are Plan and Build represented? | The active mode is recorded on the session and turn so behavior is auditable and reproducible. | `T001_05` |
| How are worktrees/projects represented? | Session stores a `workspaceId` and root reference; detailed worktree, symlink, and path policy belongs to workspace architecture. | `T001_11` |
| How is streaming represented? | Streaming produces events; the session stores stable message parts or final projections, not every transient rendering chunk by default. | `T001_07` |

## Core Concepts

| Concept | Definition | First-model fields | Later fields |
| --- | --- | --- | --- |
| Session | Runtime-owned aggregate for related user work in one workspace. | `sessionId`, `workspaceId`, `createdAt`, `updatedAt`, `status`, `mode`, `title`, ordered turns | persistence version, share metadata, compaction state, cost/token totals |
| Turn | One user request and the runtime response activity it triggers. | `turnId`, `sessionId`, `sequence`, `mode`, `status`, user prompt, parts | parent/child run links, cancellation reason, summary |
| Prompt | User input submitted to the runtime. | text, timestamp, source client, requested mode | attachments, selected context hints, redaction metadata |
| Assistant response | Model-generated text and structured response parts. | ordered text deltas or final text, provider/model references, status | token/cost details, citations, response snapshot |
| Message part | Typed item inside a turn. | part id, type, sequence, timestamps, payload summary | provider-native payload, storage projection metadata |
| Tool call | Runtime-approved request to execute a registered tool. | tool call id, tool name, arguments summary, status, permission reference when needed | raw arguments storage policy, retry metadata |
| Approval | Permission request and decision associated with a side effect. | approval id, requested capability, scope, decision, decided by/source, timestamps | cache key, expiry, revocation metadata, audit record id |
| Result | Tool or runtime operation outcome. | result id, status, summary, output reference or redacted output | artifact references, diff/stat summaries, cost impact |
| Error | Typed failure associated with a session, turn, part, or tool call. | error id, code, message, recoverability, source | stack/reference details, remediation hint, support bundle id |

## Java Domain Shape

The first Java implementation should prefer small domain records/classes and
explicit value objects over provider SDK objects or UI DTOs:

| Java concept | Role | Notes |
| --- | --- | --- |
| `SessionId`, `TurnId`, `PartId` | Stable typed identifiers | Prefer opaque value objects or records over bare strings at domain boundaries. |
| `WorkspaceRef` | Identifies the workspace/root for a session | Full path validation belongs to workspace services. |
| `Session` | Aggregate root | Holds metadata, lifecycle status, mode defaults, and ordered turns or turn references. |
| `Turn` | One prompt execution | Holds prompt summary, mode, status, ordered parts, and provider/model references. |
| `MessagePart` | Sealed hierarchy or equivalent typed union | Use variants for user prompt, assistant text, reasoning, tool call, tool result, approval reference, warning, error, summary, snapshot later. |
| `SessionStatus` | Lifecycle enum | Initial values: `active`, `waiting_for_approval`, `completed`, `aborted`, `error`. |
| `TurnStatus` | Prompt execution enum | Initial values: `pending`, `running`, `waiting_for_approval`, `completed`, `aborted`, `error`. |
| `SourceClient` | Origin metadata | Initial values: `cli`, `server`, `vaadin`, `extension`, `system`. |
| `OutputRef` | Reference to large/redacted payloads | Keeps large tool output, patches, generated files, or logs outside the core aggregate. |

This shape is intentionally not a database schema and not a REST response schema.
Adapters may project it into API DTOs later.

## Lifecycle Rules

- Creating a session assigns `sessionId`, `workspaceId`, initial mode/default
  mode, timestamps, source client, and `active` status.
- Appending a turn assigns a monotonic turn sequence within the session.
- A turn may transition from `pending` to `running`, then to
  `waiting_for_approval`, `completed`, `aborted`, or `error`.
- A session can be `waiting_for_approval` only because at least one active turn is
  waiting for approval.
- A session can be `completed` when its latest turn completed and no runtime work
  is pending.
- `aborted` means the runtime intentionally stopped outstanding work; it should
  not be reused for provider/tool failures.
- `error` means the runtime could not complete the current operation and the
  error is recorded as a typed part/event.
- Revert, unrevert, compact, summarize, fork, share, and delete-part operations
  must be modeled later as explicit lifecycle operations, not ad hoc mutation of
  historical parts.

## Message Part Strategy

OpenCode has rich message/part variants for text, reasoning, tool steps, files,
patches, snapshots, compaction, agents/subtasks, tokens, and costs. Codegeist
should start smaller, but leave room for the same feature categories:

| Part family | MVP role | Later expansion |
| --- | --- | --- |
| User prompt | Required | Attachments, selected context hints, redaction metadata. |
| Assistant text | Required | Streaming deltas are events; final text is a durable part/projection. |
| Reasoning/diagnostic note | Optional | Only if provider and policy allow safe display/storage. |
| Tool call | Required for tool-capable work | Exact schema belongs to tool architecture. |
| Tool result | Required for tool-capable work | Store summary plus output/artifact refs. |
| Approval reference | Required for side effects | Full policy/cache details belong to permission architecture. |
| Warning/error | Required | Typed source and recoverability. |
| Patch/file snapshot | Later/MVP candidate | Coordinate with patch/edit and workspace architecture. |
| Summary/compaction | Later | Coordinate with storage and context-loading architecture. |
| Child run/subtask | Later | Coordinate with nested runtime/subagent design. |

The session aggregate should not embed unbounded data. Large file contents,
patches, shell logs, model raw payloads, and generated artifacts should be stored
as redacted summaries plus references once storage exists.

## Required First Model

The first model should be minimal but typed enough to support CLI output and
later storage without reshaping the domain:

- Stable ids for session, turn, message part, tool call, approval, result, and
  error objects.
- Ordered sequences for turns and parts so clients can render deterministic
  history.
- Session status: `active`, `completed`, `aborted`, `error`, and later
  `compacted` or `archived` if needed.
- Turn status: `pending`, `running`, `waiting_for_approval`, `completed`,
  `aborted`, and `error`.
- Agent mode per session or turn, because `codegeist run` may later continue a
  session with explicit mode selection.
- Workspace identity and root path reference, not raw unchecked paths inside
  every part.
- Source client metadata such as `cli`, future `server`, future `vaadin`, or
  extension-originated request.
- Provider/model references as metadata, while provider-native response objects
  remain behind the provider boundary.
- Redacted summaries or references for large outputs instead of embedding every
  shell output, file payload, or artifact directly in the session aggregate.
- A clear separation between durable parts and transient rendering events, so
  streaming does not force the session to store every token or UI update.
- Explicit lifecycle transitions instead of free-form status strings.
- A way to attach provider/model references and tool/permission correlation ids
  without importing provider SDK, tool implementation, or permission cache types
  into the session model.

## Persistence Candidates

Persistence belongs to `T001_19`, but the session model should identify what
must be durable later:

- Session metadata, title, workspace identity, selected mode, and lifecycle
  status.
- Ordered turns and typed message parts.
- Permission requests and decisions that affect auditability.
- Tool calls and results, including redacted output summaries and artifact
  references.
- Runtime errors that explain why a turn failed or stopped.
- Provider/model identifiers, cost/token summaries, and compacted summaries once
  telemetry exists.
- Event log or event-derived projections, depending on the event model decision
  in `T001_07`.
- Output/artifact references for large tool results, patches, generated files,
  logs, and snapshots once storage policy exists.
- Lifecycle operation records for abort, compact, summarize, fork, share, revert,
  unrevert, and delete-part when those features are implemented.

## Boundary Rules

- Runtime owns session state transitions.
- CLI, server, Vaadin, and future TUI clients may render sessions and submit
  commands, but they must not mutate session internals directly.
- Provider adapters may contribute assistant response parts, cost/token metadata,
  and provider errors, but they must not own session lifecycle.
- Tool implementations may return results or errors, but the runtime records them
  as session parts after permission and workspace checks.
- Approval decisions are linked to session/turn context for auditability, but the
  permission model owns approval policy and caching.
- Storage adapters persist session projections through ports; they must not
  orchestrate runtime execution.
- Event emission may be derived from session changes, but the session aggregate
  should not depend on a concrete event bus or streaming transport.
- API DTOs and Vaadin view models are projections of the session domain, not the
  source model.

## Acceptance Questions For Implementation Readiness

Before implementation starts, this task should make these answers clear:

- Can a Java developer create a `Session` and append a `Turn` without knowing
  Spring Shell, Vaadin, HTTP, or provider SDK types?
- Can the runtime record a provider response, tool request, permission decision,
  and tool result in a stable order?
- Can the event model render the same turn without mutating the session from the
  client side?
- Can the storage task persist the model later without inventing new core
  concepts?
- Can the workspace task decide path/worktree policy without changing the session
  aggregate shape?
- Can the tool and permission tasks add exact schemas without moving ownership
  into the session model?

## Open Questions

- Does MVP store sessions only in memory, or should file-backed storage arrive
  with the first usable CLI workflow?
- Are costs/tokens required in the first persisted model, or can they remain
  optional telemetry until provider integration stabilizes?
- Should compaction create a new session part, a session snapshot, or both?
- Should branch/worktree identity be first-class in `workspaceId`, or represented
  later by the workspace model in `T001_11`?
- How much raw tool/provider output can be retained before redaction and artifact
  storage policies are defined?
- Should `waiting_for_approval` be both a session status and turn status, or only
  a derived session projection from active turns?
- Should session history keep deleted/reverted parts as tombstoned records for
  auditability, or can later MVP operations physically remove them?
- Does MVP need child sessions/forks, or should nested work wait until subagent
  architecture is explicitly defined?

## Non-Goals

- Do not implement Java session classes yet.
- Do not implement session persistence, compaction, sharing, abort, revert, or
  file-inspection commands.
- Do not define the full event taxonomy; that belongs to `T001_07`.
- Do not define storage schema or adapters; that belongs to `T001_19`.
- Do not define exact tool or permission schemas; those belong to `T001_09` and
  `T001_10`.

## Deliverable

Add `## Session Model` to `docs/developer/specification/codegeist-opencode-parity.md` with:

- runtime-owned session aggregate definition,
- core concepts and fields,
- required first-model fields,
- later persistence candidates,
- boundary rules,
- migration questions and Java domain shape,
- lifecycle and message-part strategy,
- open questions and non-goals.

## Acceptance Criteria

- Session concepts are independent of CLI, server, and Vaadin clients.
- The model supports future event streaming and audit trails.
- Open questions about persistence are listed.
- Session, turn, prompt, assistant response, tool call, approval, result, and
  error are defined.
- First-model fields are explicit enough to start implementation without locking
  in storage design.
- Provider, tool, permission, workspace, and storage ownership boundaries remain
  separate from the session aggregate.
- The task answers the OpenCode-to-Java migration questions needed to start a
  Java domain implementation later.
- Lifecycle status and message part strategy are explicit enough to align with
  event, tool, permission, workspace, provider, and storage tasks.

## Verification

- Check consistency with the event model child task.
- Confirm the parity document can express future prompt flow events without
  making clients own session state.

## Verification Result

- Added `## Session Model` to the parity document.
- Aligned session parts with the pending `T001_07` event model by keeping ordered
  turns/parts, explicit statuses, approval links, tool results, and errors
  available for future events.
- Kept client, provider, tool, permission, workspace, and storage ownership out
  of the session aggregate.
- Expanded the task with migration questions, Java domain shape, lifecycle rules,
  message part strategy, and implementation-readiness acceptance questions.

## Solution Note

Status: completed.

The solution pass used the narrow documentation-first path because
`docs/developer/specification/codegeist-opencode-parity.md` already contains the required
`Session Model` section. That section defines runtime-owned sessions, turns,
message parts, lifecycle rules, first-model fields, persistence candidates,
boundary rules, open questions, and non-goals.

No user decision is pending. The section keeps sessions as Java domain objects
owned by the runtime and leaves persistence, storage schema, exact tool schemas,
and client DTOs to their dedicated tasks.

Verification passed with `git --no-pager diff --check`. A final review confirmed
session state transitions remain outside CLI, server, Vaadin, provider, tool,
permission, and storage adapters.
