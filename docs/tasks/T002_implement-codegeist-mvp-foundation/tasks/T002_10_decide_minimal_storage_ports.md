# T002_10 Decide Minimal Storage Ports

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_19`, `T001_06`, `T001_07`, `T001_22`, `T001_24`

## Goal

Choose and implement the minimal storage port shape needed for early CLI session
continuation without committing to event sourcing or a database.

## Context

The architecture allows in-memory storage first, with file-backed session/config
storage only if needed for the first CLI workflow. Storage persists projections
and records through ports; it must not own runtime behavior, permissions,
workspace policy, or provider selection.

## Concrete Solution

1. Decide whether the first implementation needs in-memory only or file-backed
   session/config storage.
2. Add storage port interfaces for sessions and optional audit/event projection
   metadata.
3. Provide an in-memory adapter first unless a file-backed continuation test is
   explicitly selected.
4. Add tests for save/load or in-memory lifecycle behavior, redaction boundaries,
   and separation from runtime orchestration.
5. Document the decision if file-backed storage is deferred.

## Scope

- `ai.codegeist.storage`
- `ai.codegeist.session` storage-facing ports
- optional file-backed adapter only if selected
- focused tests
- `docs/developer/codegeist-opencode-parity.md` only if the storage decision
  changes architecture posture

## Acceptance Criteria

- The initial storage posture is explicit: in-memory only or file-backed for a
  narrow reason.
- Storage ports do not orchestrate runtime behavior.
- Sensitive data and credentials are not treated as ordinary session/tool output.
- Tests prove the selected minimal persistence behavior.
- Event sourcing remains optional and unimplemented.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_03`.
- Should happen before CLI session continuation features depend on persistence.

## Non-Goals

- Do not implement database schemas, migrations, encryption, durable audit log,
  sharing, compaction, or event replay.

## Open Questions

- Does the first CLI workflow need restart/continue behavior, or can it stay
  in-memory until prompt execution works?

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task is intentionally a storage decision and port-shape slice, not a
  database, event-sourcing, or audit-log implementation.
- Source research may inspect how OpenCode relates sessions, events, and storage,
  but Codegeist should keep event sourcing optional.

## Creation Note

Status: open.

Derived from storage, session, event, MVP, and risk-register architecture as one
minimal persistence decision slice.
