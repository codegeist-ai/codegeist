# T002_03 Introduce Runtime Session Event Contracts

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_03`, `T001_05`, `T001_06`, `T001_07`, `T001_22`, `T001_23`

## Goal

Implement the first runtime-owned contracts for prompt requests, agent modes,
sessions, turns, message parts, and runtime events.

## Context

The architecture defines Runtime as the central orchestrator, Plan and Build as
runtime modes, Session as the user-work aggregate, and RuntimeEvent as the shared
observation contract for CLI now and later server/Vaadin clients.

This task follows `T002_02`, which introduces vocabulary/package boundaries. It
adds the first behavior-free but usable contracts needed by later prompt flow,
provider, tool, permission, and storage tasks.

## Concrete Solution

1. Add small Java records, enums, and interfaces for runtime request identity,
   agent mode, session id, turn id, message part type, status enums, event id,
   event envelope, event type, event source, visibility, and correlation id.
2. Add a minimal runtime service or port interface that accepts a prompt request
   and returns or emits typed contract objects without calling a provider.
3. Keep implementation in-memory and deterministic; no persistence, streaming
   transport, tool execution, or CLI rendering belongs here.
4. Add contract-level tests for session creation, turn append shape, mode on a
   request/turn, and monotonic event sequencing.

## Scope

- `ai.codegeist.runtime`
- `ai.codegeist.session`
- `ai.codegeist.agent`
- `ai.codegeist.event`
- focused tests under `app/codegeist/cli/src/test/java/ai/codegeist/`

## Acceptance Criteria

- Runtime contracts can represent Plan and Build requests.
- Session and turn contracts include stable typed ids, status, mode, and ordered
  message/event relationships.
- Event contracts include envelope, sequence, visibility, audit relevance, and
  correlation fields.
- Tests prove create/append/event-sequence behavior without provider or tool
  integration.
- CLI, server, Vaadin, Spring AI, PF4J, JBang, and storage types do not leak into
  the core contracts.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_01` and `T002_02`.
- Feeds context, provider, tool, permission, storage, and CLI prompt-flow tasks.

## Non-Goals

- Do not call Spring AI or any provider.
- Do not execute tools, file edits, shell commands, or permissions.
- Do not implement storage, server endpoints, Vaadin views, or a full CLI prompt
  loop.

## Open Questions

- Should the first runtime service return a result object, publish events through
  an in-process publisher, or expose both contracts for tests?

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task should use targeted OpenCode source questions for session, message
  part, and event flow before finalizing contract names.
- The scope stays contract-level and should not implement provider, tool,
  permission, storage, or CLI orchestration behavior.

## Creation Note

Status: open.

Derived from the runtime, mode, session, event, MVP, and prompt-flow architecture
tasks as one grouped implementation slice.
