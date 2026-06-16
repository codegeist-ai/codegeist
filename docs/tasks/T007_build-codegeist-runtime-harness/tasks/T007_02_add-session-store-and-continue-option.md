# T007_02 Add Session Store And Continue Option

Parent: `T007_build-codegeist-runtime-harness`

Status: completed

## Goal

Add the versioned directory-local session store model and the optional
`ask -c/--continue` command parameter.

This child establishes file-based session storage before tools and TUI depend on
it. The first design target is `.codegeist/session.json`: one store per working
directory that can hold multiple chat sessions. The model is oriented by
OpenCode's durable project/session/message/part persistence but translated into a
portable Codegeist-owned JSON file instead of SQLite.

Detailed session store model notes live in
`../session-store-model-specification.md`.

## OpenCode Evidence And Codegeist Translation

Use `../opencode-workflow-analysis.md` as behavior evidence for this child.
OpenCode persists resumable chats through separate session, message, and part
tables:

- `SessionTable` stores session identity, project/workspace, directory, selected
  model, agent, permissions, summary/revert metadata, and timestamps.
- `MessageTable` stores user and assistant message metadata as JSON.
- `PartTable` stores message parts as JSON, including text, file, tool,
  reasoning, snapshot, patch, retry, and step marker parts.
- `WithParts` reconstructs a renderable and provider-facing transcript from a
  message info object plus its ordered parts.
- OpenCode compaction stores a special user message with a `compaction` part and
  a summary assistant message instead of deleting old messages. Later model
  context is rebuilt from the summary plus a retained tail selected by
  `tail_start_id`.

OpenCode also exposes a `--continue` CLI option for the TUI that continues the
last session. Codegeist should preserve the useful behavior, not the OpenCode
implementation:

- Use one portable `.codegeist/session.json` store per working directory.
- Store multiple chat sessions in that store.
- Store session root metadata, messages, and ordered message parts.
- Keep each persisted transcript chronological and directly renderable.
- Keep enough structure to rebuild model context later without storing runtime
  provider or tool configuration.
- Include compaction markers and summary assistant messages in the model so later
  compaction can summarize old context while preserving the original stored
  transcript.
- Resolve provider, model, MCP clients, enabled tools, permissions, runtime
  status, and UI state from current runtime configuration when a session
  continues.

## Scope

- Add or specify the smallest Java model needed for `.codegeist/session.json`
  schema version, working directory, store timestamps, sessions, messages, and
  ordered message parts.
- Prefer a session-with-messages-with-parts shape over one flat chat transcript or
  a flat top-level `toolResults` list. Top-level lookup indexes may be added only
  when a focused later task needs them.
- For this child, implement `text` and `compaction` parts when runtime source is
  added. Tool, patch, shell, file, reasoning, and step marker parts should be
  specified for compatibility with later T007 children, but not implemented as
  unused Java placeholders.
- Implement `CompactionSessionPart` as the durable user `compaction` part with a
  retained-tail marker. The assistant summary message should point back to that
  compaction user message through `parentMessageId`; do not add a separate
  message-level `summary` flag.
  Do not generate compaction summaries in T007_02 unless the implementation task
  is explicitly expanded again.
- Add load/save behavior for one `.codegeist/session.json` store per working
  directory.
- Add `ask -c <prompt>` and `ask --continue <prompt>`.
- Every successful `ask` saves the new prompt/response turn to
  `.codegeist/session.json`.
- Without `-c/--continue`, create a new session in the current store, creating the
  store first when needed.
- When `-c/--continue` is present, load `.codegeist/session.json` when it exists,
  find the session with the newest `updatedAt`, append the new prompt/response,
  and save the same store. If the file is missing or contains no sessions, create a
  new session instead.
- Corrupt or unsupported existing `.codegeist/session.json` content must fail
  instead of being overwritten.
- Store the new user prompt as a user message with a `text` part in the target
  session.
- Store the provider response as an assistant message with a `text` part and a
  parent message reference to the user message in the same continued session.
- Preserve current `ask` stdout behavior: stdout contains only the provider
  response even though the session turn is persisted.
- Keep `CodegeistChatRequest` focused on model and prompt.
- Do not store session store state inside `CodegeistChatRequest`.

## Session Store Model Direction

The first `.codegeist/session.json` shape should be versioned, inspectable, and
small:

```json
{
  "schemaVersion": 1,
  "workingDir": "/home/test/Projects/codegeist-ai/codegeist",
  "createdAt": "2026-06-09T12:00:00Z",
  "updatedAt": "2026-06-09T12:01:00Z",
  "sessions": [
    {
      "id": "11111111-1111-4111-8111-111111111111",
      "title": "New session - 2026-06-09T12:00:00Z",
      "createdAt": "2026-06-09T12:00:00Z",
      "updatedAt": "2026-06-09T12:01:00Z",
      "messages": [
        {
          "id": "22222222-2222-4222-8222-222222222222",
          "role": "user",
          "createdAt": "2026-06-09T12:00:05Z",
          "parts": [
            {
              "id": "33333333-3333-4333-8333-333333333333",
              "type": "text",
              "text": "Fix this test"
            }
          ]
        },
        {
          "id": "44444444-4444-4444-8444-444444444444",
          "role": "assistant",
          "createdAt": "2026-06-09T12:00:10Z",
          "completedAt": "2026-06-09T12:00:20Z",
          "parentMessageId": "22222222-2222-4222-8222-222222222222",
          "parts": [
            {
              "id": "55555555-5555-4555-8555-555555555555",
              "type": "text",
              "text": "I found the issue."
            }
          ]
        }
      ]
    }
  ]
}
```

The initial Java surface should use names that reflect the persisted concepts:

- `SessionStore`
- `CodegeistSession`
- `SessionMessage`
- `SessionMessageRole`
- `SessionPart`
- `TextSessionPart`
- `CompactionSessionPart`
- `SessionStoreService`

Defer Java classes for planned tool and patch parts until the focused T007 child
that persists those parts needs them.

## Acceptance Criteria

- A focused test proves plain `ask <prompt>` still prints only the provider
  response and creates a new session in `.codegeist/session.json`.
- A focused test proves `ask -c <prompt>` and `ask --continue <prompt>` load
  `.codegeist/session.json`, select the session with the newest `updatedAt`, append
  to that session, and save the same store.
- A focused test proves missing `.codegeist/session.json` or an empty `sessions[]`
  creates a new session for `-c/--continue`.
- A focused test proves corrupt or unsupported existing `.codegeist/session.json`
  fails instead of being overwritten.
- A focused test proves `.codegeist/session.json` can contain multiple sessions.
- The continued session contains `messages[].parts[]`, not only flat message text
  or a top-level tool result list.
- The user prompt is persisted as a user message with a `text` part in the
  continued session.
- The assistant response is persisted as an assistant message with a `text` part
  and a parent reference to the user message for the same turn.
- The persisted shape can be used by later tasks to reconstruct a chronological
  transcript for model context and TUI rendering.
- The session store model implements compaction as a user `compaction` part,
  supports a summary assistant message linked by `parentMessageId`, and persists a retained-tail marker so
  later tasks can rebuild model context from summary plus recent tail without
  deleting old messages.
- `.codegeist/session.json` does not store API keys, OAuth tokens, cloud
  credentials, or evaluated secret values.
- `.codegeist/session.json` does not store provider config, selected provider,
  selected model, MCP client definitions, enabled tool definitions, permission
  rules, runtime status, or TUI layout state.
- Architecture docs are updated with the actual session store model and command
  behavior.

## Non-Goals

- Do not add TUI, MCP wiring, patch/edit, shell, or read/write tools here unless a
  tiny test fake is needed to prove session store persistence.
- Do not add a database, migrations, server APIs, replay commands, or remote sync.
- Do not add `--new-session` in this child.
- Do not add `--chat <chat.json>` in this child.
- Do not add `--session <id>` in this child; explicit session selection can be a
  later focused task.
- Do not overwrite corrupt or unsupported existing `.codegeist/session.json` files
  when creating a new session.
- Do not implement runtime compaction triggering, token estimation, summary
  generation, or context pruning in this child; this child implements only the
  session-store model shape needed for later compaction.
- Do not put session store state into `CodegeistChatRequest`.
- Do not copy OpenCode's SQLite schema, server/event runtime, Effect architecture,
  OpenTUI/Solid UI, plugin system, subagents, memory, session sharing, or full MCP
  surface.
- Do not persist selected provider/model metadata just because OpenCode stores it
  in session/message metadata; T007 explicitly resolves that at runtime.

## Suggested Tests

- No-`--continue` command assertion that stdout still contains only the provider
  response and `.codegeist/session.json` contains a new session with the turn.
- Existing `.codegeist/session.json` fixture with two sessions, asserting
  `-c/--continue` appends to the newest session by `updatedAt` and preserves the
  older session unchanged.
- `-c/--continue` missing-store and empty-`sessions[]` assertions that create a new
  session.
- Corrupt or unsupported schema version behavior that fails instead of overwriting
  an existing file.
- Session-store fixture with a `compaction` part and summary assistant message,
  asserting the model can load and save compaction state without generating a new
  summary.
- No-secret representative config assertion that proves provider config, selected
  provider, selected model, MCP definitions, enabled tools, permission rules,
  runtime status, and TUI state are absent from `.codegeist/session.json`.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<session-store-test-selector>
task test
```

## Result

Implemented in `app/codegeist/cli`:

- Added `ai.codegeist.app.session` with the versioned `.codegeist/session.json`
  model, `SessionStoreService`, `TextSessionPart`, and `CompactionSessionPart`.
- Added `ask -c/--continue <prompt>` to append to the current working directory
  store's newest session when one exists, and to create a new session for missing
  or empty stores. Plain `ask <prompt>` now creates a new stored session while
  keeping stdout response-only. Command-boundary failures now flow through the
  shared `CodegeistCommandExceptionMapper` via Spring Shell
  `@Command(exitStatusExceptionMapper = ...)` instead of local print-and-rethrow
  blocks.
- Kept `CodegeistChatRequest` focused on runtime model and prompt.
- Registered session store records in native reflection metadata.
- Updated `docs/developer/architecture/architecture.md` with the current session
  model, command behavior, and tests.

Verification:

```bash
task test TEST=CodegeistCommandExceptionMapperTest,SessionStoreServiceTest,AskCommandsSessionStoreTest
task test
```
