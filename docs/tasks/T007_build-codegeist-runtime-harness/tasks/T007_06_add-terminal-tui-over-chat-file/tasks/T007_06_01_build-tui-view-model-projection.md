# T007_06_01 Build TUI View Model Projection

Parent: `T007_06_add-terminal-tui-over-chat-file/task.md`

Status: open

## Goal

Create the pure TUI projection layer that translates persisted
`.codegeist/session.json` state plus transient runtime status into a deterministic
view model. This child intentionally has no terminal, JLine, prompt-loop, or Spring
Shell command implementation.

## Dependencies

- Depends on the parent `implementation-plan.md` and
  `third-party-tui-deep-analysis.md` for accepted architecture and boundaries.
- Has no previous T007_06 child dependency.
- Must be completed before `T007_06_02_add-deterministic-line-renderers.md`.

## Scope

- Add the first `ai.codegeist.app.tui` package with pure view-model records and
  projection components.
- Add a public tool-package inventory service for local Codegeist tool names/counts
  without opening prompt-scoped tool runs.
- Project `SessionStore`, `CodegeistSession`, `SessionMessage`, `TextSessionPart`,
  `CompactionSessionPart`, and `ToolSessionPart` into TUI records.
- Keep runtime provider/model/tool/MCP status separate from persisted session facts.
- Ensure MCP clients are counted from config only; do not open MCP clients for
  passive status.

## Classes To Create

Production package `ai.codegeist.app.tui`:

- `TuiViewModelFactory`
- `TuiViewModel`
- `SessionStoreView`
- `TranscriptRow`
- `TranscriptRowKind`
- `ToolActivityView`
- `ToolActivityKind`
- `RuntimeStatus`
- `RuntimeStatusFactory`
- `Notification`
- `NotificationLevel`

Production package `ai.codegeist.app.tool`:

- `CodegeistToolInventoryService`

## Behavioral Requirements

- `TuiViewModelFactory` must be a pure projection. It must not mutate, load, save,
  call providers, open tools, or open MCP clients.
- If the store has sessions, the active session should follow the current latest
  session selection rule from `SessionStore.latestSessionIndex()`.
- If the store has no sessions, the model should contain an empty-session notice and
  no active session id.
- `TextSessionPart` rows should derive user or assistant row kind from the owning
  `SessionMessageRole`.
- `CompactionSessionPart` rows should show the current compaction facts: `auto`,
  `overflow`, and `tailStartMessageId` when present.
- `ToolSessionPart` rows should preserve final persisted values: `tool`, `status`,
  and `outputPreview`.
- `RuntimeStatusFactory` should display provider type and default model from
  `CodegeistConfig.defaultProvider()` when present.
- `RuntimeStatusFactory` should show configured MCP client count from
  `McpClientsRootElement`, not open MCP runtime clients.
- `CodegeistToolInventoryService` should expose local tool names and count from the
  injected `List<CodegeistLocalTool>` without building callbacks or executing tools.

## Persistence Boundaries

- `SessionStoreView` and transcript rows may contain only persisted session-store
  facts.
- `RuntimeStatus`, `Notification`, and later prompt/scroll/modal state are transient
  and must not be written into `.codegeist/session.json`.
- This child must not add any new JSON fields to the session model.

## Acceptance Criteria

- A focused test proves user text, assistant text, compaction parts, and tool parts
  project to stable `TranscriptRow` values.
- A focused test proves a store with multiple sessions selects the latest session
  consistently.
- A focused test proves runtime provider/model, local-tool count, and configured MCP
  client count are visible in `RuntimeStatus` only.
- A focused test proves runtime status construction does not write to
  `.codegeist/session.json`.
- No terminal, JLine, or Spring Shell command classes are added in this child.

## Suggested Tests

- `TuiViewModelFactoryTest`
- `RuntimeStatusFactoryTest`

Candidate command from `app/codegeist/cli`:

```bash
task test TEST=TuiViewModelFactoryTest,RuntimeStatusFactoryTest
```

## Non-Goals

- Do not add renderers.
- Do not add input parsing.
- Do not add the TUI controller loop.
- Do not add JLine or Spring Shell command integration.
- Do not add full-screen state, prompt history, approval state, cancellation state,
  streaming state, or a second persistence model.
