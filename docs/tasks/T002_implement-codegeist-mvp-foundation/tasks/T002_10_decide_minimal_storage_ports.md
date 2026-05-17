# T002_10 Describe Minimal Storage Ports

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_19`, `T001_06`, `T001_07`, `T001_22`, `T001_24`

## Goal

Choose and describe the minimal storage port shape needed for early CLI session
continuation without implementing storage, event sourcing, or a database.

## Context

The architecture allows in-memory storage first, with file-backed session/config
storage only if needed for the first CLI workflow. Storage persists projections
and records through ports; it must not own runtime behavior, permissions,
workspace policy, or provider selection.

## Concrete Solution

1. Create or update `docs/developer/storage-port-posture.md` as the future storage
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

- `docs/developer/storage-port-posture.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/codegeist-opencode-parity.md` only if the storage decision
  changes architecture posture
- `docs/developer/architecture.md` only to keep current-state notes accurate
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

- Does the first CLI workflow need restart/continue behavior, or can it stay
   in-memory until prompt execution works?

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

## Creation Note

Status: open.

Derived from storage, session, event, MVP, and risk-register architecture as one
minimal persistence decision slice.
