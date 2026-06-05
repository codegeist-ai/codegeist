# T007_08 Add Session Storage And Resume

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Persist enough Codegeist session, event, permission, and tool-result state to resume
or inspect a coding-agent workflow.

This child should come after the runtime can produce meaningful events and tool
results. The first storage slice should support the smallest continuation workflow
needed by the CLI/TUI harness, not a broad server-grade database architecture.

## Dependencies

- Requires `T007_02` runtime/session/event spine.
- Prefer starting after `T007_04` and `T007_05` so tool and permission events have
  real state to persist.
- Can be split further if persistence, resume, event replay, and retention become
  too large for one focused task.

## OpenCode Evidence To Translate

- `docs/third-party/opencode/source/packages/opencode/src/storage/db.ts` for SQLite
  setup and migration posture.
- `docs/third-party/opencode/source/packages/opencode/src/session/session.sql.ts`
  for session, message, part, todo, and permission tables.
- `docs/third-party/opencode/source/packages/opencode/src/sync/index.ts` for
  persisted sync events and projectors.
- `docs/third-party/opencode/source/packages/opencode/src/bus/index.ts` for runtime
  event publishing.
- `docs/third-party/opencode/source/packages/opencode/src/session/message-v2.ts` for
  message and part vocabulary.

Translate behavior only. Do not copy OpenCode database schemas or migrations unless
a focused Codegeist storage decision chooses a compatible concept.

## Scope

- Choose the smallest local storage format that supports current Codegeist needs.
  Keep SQLite, JSONL, files, or another store as implementation choices to decide
  from tests and operational constraints.
- Persist session identity, turns/messages, assistant output summaries, tool result
  summaries, permission decisions, and enough event data for TUI replay only when
  those fields exist in current runtime behavior.
- Add resume or inspect behavior through the runtime first; CLI/TUI commands should
  remain clients of runtime storage behavior.
- Keep secrets and provider credentials out of session storage.
- Bound stored output and use output references only after such references are
  implemented.

## Acceptance Criteria

- A focused test proves a completed prompt/session can be saved and loaded.
- A focused test proves resume or inspect uses the stored session state rather than
  starting an unrelated new session.
- Storage does not persist API keys, OAuth tokens, cloud credentials, or full
  unbounded tool output.
- Event replay or projection is deterministic for the implemented event subset.
- Architecture docs describe the actual storage format, files/classes, and sharp
  edges.

## Non-Goals

- Do not implement server multi-user storage, remote synchronization, cloud sync,
  or role-based access.
- Do not implement full OpenCode table parity.
- Do not implement migration tooling until the storage format has shipped data that
  requires migration.
- Do not persist raw prompts, tool output, or provider responses beyond what the
  current user-visible workflow and privacy policy require.
- Do not store provider credentials or evaluated secret values.

## Suggested Tests And Verification

- Temporary storage root fixture for save/load.
- Corrupt or missing storage file behavior if the chosen format needs it.
- Resume/inspect test through runtime services; CLI command tests only after a CLI
  surface is added.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<storage-test-selector>
task test
```
