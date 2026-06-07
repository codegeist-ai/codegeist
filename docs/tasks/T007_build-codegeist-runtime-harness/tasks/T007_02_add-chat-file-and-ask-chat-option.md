# T007_02 Add Chat File And Ask Chat Option

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add the versioned chat file model and the optional `ask --chat <chat.json>` command
parameter.

This child establishes file-based storage before tools and TUI depend on it.

## Scope

- Add the smallest Java model needed for `chat.json` schema version, id,
  timestamps, working directory, messages, and tool results.
- Add load/save behavior for one chat per JSON file.
- Add `ask --chat <chat.json>`.
- If the chat file exists, load it, append the new prompt/response, and save it.
- If the chat file does not exist, create it from the active config, prompt, and
  response.
- Without `--chat`, preserve current one-shot `ask` stdout behavior and avoid
  mandatory file writes.
- Keep `CodegeistChatRequest` focused on model and prompt.

## Acceptance Criteria

- A focused test proves `ask --chat <new-file> <prompt>` creates a valid `chat.json`.
- A focused test proves `ask --chat <existing-file> <prompt>` appends to and saves
  the existing chat file.
- A focused test proves no-`--chat` `ask` still prints only the provider response.
- `chat.json` does not store API keys, OAuth tokens, cloud credentials, or evaluated
  secret values.
- `chat.json` does not store provider config, selected provider, selected model, MCP
  client definitions, enabled tool definitions, or status.
- Architecture docs are updated with the actual chat file model and command behavior.

## Non-Goals

- Do not add TUI, MCP wiring, patch/edit, shell, or read/write tools here unless a
  tiny test fake is needed to prove chat file persistence.
- Do not add a database, migrations, server APIs, replay commands, or remote sync.
- Do not put chat file state into `CodegeistChatRequest`.

## Suggested Tests

- Temporary chat file fixture for new-file creation.
- Existing chat file append/save.
- Corrupt or unsupported schema version behavior if the first parser needs it.
- No-secret representative config assertion.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<chat-file-test-selector>
task test
```
