# T007_03_01 Add Tool Session Persistence

Parent: `T007_03_add-mcp-and-read-write-tools`

Status: completed

## Goal

Add the persisted session-store shape for bounded tool activity without changing
provider request data or adding actual tool execution yet.

## Dependencies

- Depends on completed `T007_02_add-session-store-and-continue-option.md`.
- Can be implemented independently from `T007_03_02`.

## Scope

- Add `ToolSessionPart` under `ai.codegeist.app.session`.
- Update sealed `SessionPart` permits and Jackson subtype metadata to include the
  new `tool` discriminator.
- Add `SessionStoreService.currentWorkingDirectory()` and reuse it from
  `currentStorePath()`.
- Add session-store append support for a user text message and an assistant message
  containing ordered `ToolSessionPart` values followed by the assistant text part.
  `SessionStoreService` owns file I/O and clock input; `SessionStore` owns the
  in-memory session-list changes.
- Preserve existing text-only save methods by delegating with an empty tool-part
  list.
- Add `ToolSessionPart` to `META-INF/native-image/reflect-config.json`.

## Acceptance Criteria

- `ToolSessionPart` round-trips through the existing session-store JSON mapper.
- `ToolSessionPart` stores only the tool name, enum status, and bounded output
  preview.
- Saved exchanges with tool parts store assistant tool parts before the assistant
  text part.
- Saved exchanges without tool parts keep the existing text-only behavior.
- Persisted tool output is already bounded by caller-provided fields and does not
  introduce runtime-only state.
- Missing or empty session stores create a session when continuing.
- Existing corrupt session-store JSON failures still map to `No session to continue`.

## Non-Goals

- Do not implement local file tools, MCP callbacks, or chat-harness orchestration.
- Do not change `SessionStore.SCHEMA_VERSION` unless a focused failing test proves
  the additive part type cannot remain schema version `1`.
- Do not store provider config, selected provider/model, MCP definitions, enabled
  tools, permission state, runtime status, or TUI state.

## Suggested Tests

- Update `SessionStoreServiceTest` for `ToolSessionPart` JSON round-trip.
- Update `SessionStoreServiceTest` for assistant tool-part ordering before text.
- Keep the existing text-only save assertions.
- Keep the existing runtime-configuration exclusion assertion and include tool-part
  persistence coverage.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=SessionStoreServiceTest
```

## Verification

- 2026-06-17: `task test TEST=SessionStoreServiceTest,AskCommandsSessionStoreTest`
  passed from `app/codegeist/cli` with 16 tests, 0 failures, 0 errors, and 0 skips.
- 2026-06-18: `task build` and
  `task test TEST=SessionStoreServiceTest,AskCommandsSessionStoreTest` passed from
  `app/codegeist/cli` after moving session-list mutation logic into `SessionStore`.
