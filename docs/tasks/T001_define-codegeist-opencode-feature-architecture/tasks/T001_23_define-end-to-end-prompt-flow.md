# T001_23 Define End-To-End Prompt Flow

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the primary Codegeist prompt flow from user input to output.

This task specifies a cross-component flow only. It does not implement CLI
commands, runtime orchestration, provider calls, tool execution, permissions, or
events.

## Scope

- Describe user input, CLI/shell handling, session creation, agent mode,
  context loading, provider call, tool request, permission check, tool result,
  event emission, and user-visible output.
- Mark which steps are MVP and which are later-stage.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode prompt processing? | A runtime-owned flow from client request through session/turn, mode, context, provider, tools, permissions, events, and output. |
| Which client owns the flow? | None. CLI starts as the first adapter, but runtime owns orchestration. |
| What is MVP? | One prompt path from Spring Shell into runtime with provider streaming/events and permission-gated side effects where included by MVP. |
| What is later? | Server/Vaadin transport, async runs, cancellation, subagents, plugin tools, persistence/replay, and richer UI rendering. |

## Required Flow Shape

The section should express at least:

```text
user -> CLI/Shell adapter -> runtime request -> session/turn -> mode policy
  -> context loader -> provider adapter -> assistant events
  -> optional tool request -> permission -> tool execution -> result events
  -> assistant completion -> session/turn completion -> client rendering
```

## Boundary Rules

- CLI parses and renders; runtime orchestrates.
- Session records stable history; events render progress.
- Provider adapter calls Spring AI but does not own prompt/session policy.
- Tools pass through mode, permission, and workspace boundaries.
- Storage is optional for MVP unless chosen by `T001_22`/`T001_19`.

## Implementation-Readiness Questions

- Can every step map to an architecture section and component owner?
- Can the same flow later be driven by server/Vaadin clients?
- Can Plan and Build mode differences be represented in the flow?
- Can failure and denied-permission branches be shown?
- Can verification commands later test the flow without broad UI dependencies?

## Non-Goals

- Do not implement the flow.
- Do not choose provider/tool concrete APIs here.
- Do not require server or Vaadin for the first flow.
- Do not hide side-effect branches behind a happy-path-only diagram.

## Deliverable

Add an end-to-end prompt flow section to the parity document with MVP path,
failure/approval branches, later-stage branches, component owners, and
verification mapping.

## Acceptance Criteria

- The flow uses the session, event, tool, permission, and provider concepts.
- The flow can be implemented first from the CLI without blocking server/Vaadin
  clients later.
- Later-stage steps are clearly labeled.
- Runtime remains the orchestrator throughout the flow.
- Failure and denied-permission paths are represented.

## Verification

- Check consistency with the MVP cut and component model.

## Verification Result

- Specified required prompt-flow shape, boundaries, and implementation-readiness
  questions.

## Solution Note

Status: completed.

The solution pass added `## End-To-End Prompt Flow` to
`docs/developer/codegeist-opencode-parity.md`. The section defines the MVP prompt
path, approval/denied/provider-failure branches, component owners, later-stage
branches, and verification mapping.

No user decision is pending. CLI is the first adapter, but the flow remains
runtime-owned and client-agnostic so server and Vaadin can drive the same
semantics later.

Verification passed with `git --no-pager diff --check`. A final review confirmed
the flow uses session, event, tool, permission, provider, workspace, and context
concepts without implementing orchestration.
