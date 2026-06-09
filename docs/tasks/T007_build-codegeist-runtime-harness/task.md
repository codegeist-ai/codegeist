# T007 Build Chat File Tool Harness

Status: open

## Goal

Implement a resumable Codegeist chat harness centered on a portable `chat.json`
file. The existing `ask` command gains an optional `--chat <chat.json>` parameter,
and the same chat file becomes the source of truth for chat history, tool activity,
TUI rendering, and subsequent saves.

T007 now targets a practical local coding-agent loop: one chat file, optional MCP,
Codegeist tools, patch/edit, shell, TUI, and file-based storage. Do not add a
database or server runtime for this task.

## Current Codegeist Baseline

- `app/codegeist/cli` is the only implemented application module.
- The application uses Java 25, Spring Boot 4.0.6, Spring Shell 4.0.2, Spring AI
  `2.0.0-M6`, Spring AI Agent Utils `0.7.0`, Lombok, and GraalVM native build
  tooling.
- Implemented commands are `--version`, `--show-config`, and one-shot `ask`.
- `ask` selects the first configured provider through `CodegeistConfig`, uses the
  provider config's `defaultModel()`, calls `CodegeistChatService`, and prints the
  response text.
- Provider config currently supports typed config-only `ollama` and `openai`
  entries. Runtime provider calls are implemented only for local Ollama through
  `OllamaChatModel`.
- Direct `codegeist.yml` loading can parse the first minimal top-level `mcp:` client
  catalog with `type`, `command`, and `args`; MCP callbacks and chat tools are not
  implemented yet.
- `task test` from `app/codegeist/cli` is the implementation verification entrypoint
  and starts the fixed local Ollama container with `OLLAMA_ENTER=false` before
  Maven.
- `docs/developer/architecture/architecture.md` describes current state and must be
  updated whenever implementation changes packages, classes, configuration, runtime
  flows, or tests.

## Completion Feature Set

T007 is complete only when these features are implemented and tested:

- `ask` accepts an optional `--chat <chat.json>` parameter.
- If `--chat` points to an existing file, `ask` loads that chat, appends the new
  prompt and response/tool activity, and saves the same file.
- If `--chat` points to a missing file, `ask` creates a new resumable chat file at
  that path.
- Without `--chat`, `ask` keeps the existing one-shot behavior and stdout contract.
- `chat.json` stores only chat-relevant information needed to resume and save the
  chat: schema version, chat id, timestamps, working directory, messages, assistant
  responses, tool calls/results, patch/edit/shell summaries, and TUI-renderable
  state.
- `chat.json` does not store provider config, selected provider, selected model, MCP
  client definitions, enabled tool definitions, or chat status.
- MCP clients are configured through a Codegeist-owned top-level `mcp:` map in
  direct `codegeist.yml`; Spring AI's `spring.ai.mcp.client.*` tree is not the
  public Codegeist config contract.
- Spring AI MCP client support is installed through `spring-ai-starter-mcp-client`
  and mapped from the Codegeist MCP config where needed.
- Codegeist-owned tools are available to chats. The first useful set includes
  read/write working-directory file tools and MCP tools.
- Patch/edit tools can mutate files under the chat working directory through the chat tool path and record
  bounded results in `chat.json`.
- Shell tools can run bounded local commands through the chat tool path and record
  bounded results in `chat.json`.
- A terminal TUI can open or create a `chat.json`, render the chat, tool activity,
  file changes, shell output, runtime status, and errors needed for daily local
  coding-agent use, submit prompts, and save back to the same file.
- Existing `--version`, `--show-config`, and no-`--chat` `ask` behavior keeps
  working.

## Chat File Contract

The first `chat.json` shape should be small, versioned, and inspectable. The exact
Java records should be added only when implementation tests need them, but the file
must support this information class:

```json
{
  "schemaVersion": 1,
  "id": "2026-06-06T120000Z-abc123",
  "createdAt": "2026-06-06T12:00:00Z",
  "updatedAt": "2026-06-06T12:01:00Z",
  "workingDir": "/home/test/Projects/codegeist-ai/codegeist",
  "messages": [],
  "toolResults": []
}
```

Rules:

- Keep one chat per file.
- Treat `chat.json` as the resumable state source, not as an export format derived
  from another database.
- Store enough tool result data to render and continue the chat, but keep large
  command output and large file content bounded.
- Do not store API keys, OAuth tokens, cloud credentials, or evaluated secret values.
- Do not store provider selection, model selection, MCP client definitions, enabled
  tool definitions, or status in `chat.json`; resolve those from current config and
  runtime behavior.
- Add fields only when the active child task and tests need them.

## MCP Configuration Contract

Use Codegeist's direct `codegeist.yml` shape for MCP clients:

```yaml
mcp:
  filesystem:
    type: stdio
    command: npx
    args:
      - -y
      - "@modelcontextprotocol/server-filesystem"
      - .
```

The first implementation should support only `stdio` clients unless a focused test
requires another transport. Keep client ids as map keys. Add fields such as
environment, timeout, or enablement only when implementation tests need them.

## Child Tasks

- `T007_01_define-chat-file-tool-harness-scope.md` - completed scope definition
  for the expanded chat-file tool harness.
- `T007_02_add-chat-file-and-ask-chat-option.md` - add `ask --chat <chat.json>` and
  the versioned chat file model/save-load behavior.
- `T007_03_add-mcp-and-read-write-tools.md` - finish MCP callbacks and the first
  read/list/glob/grep/write tool path; the minimal `mcp:` config root is already
  implemented.
- `T007_04_add-patch-edit-and-shell-tools.md` - add bounded patch/edit and shell
  tools that persist tool activity into `chat.json`.
- `T007_05_add-terminal-tui-over-chat-file.md` - add a terminal TUI that opens,
  renders, updates, and saves the same chat file.
- `T007_06_verify-chat-file-tool-harness.md` - run focused and broad verification,
  update architecture docs, and ensure the parent acceptance criteria hold.

## Parent Acceptance Criteria

- `ask --chat <chat.json> <prompt>` can create a new chat file and can resume an
  existing chat file.
- The no-`--chat` `ask` path still works without requiring chat file state.
- `CodegeistConfig` can load the minimal `mcp:` client map from direct
  `codegeist.yml`.
- Configured MCP callbacks can be made available to chat calls.
- Codegeist-owned tools include read/list/glob/grep/write plus patch/edit and shell.
- Tool calls and results are persisted in the active `chat.json` with bounded output.
- A terminal TUI uses `chat.json` as its state source and does not create a second
  persistence model.
- `docs/developer/architecture/architecture.md` documents the implemented chat file,
  MCP, tools, patch/edit, shell, and TUI behavior.

## Non-Goals

- Do not implement a database, server-side session service, remote sync, API/SDK,
  Vaadin, desktop UI, PF4J, plugin marketplace, JBang, LSP, skills, memory, or
  subagents in this T007 slice.
- Do not copy OpenCode's Bun, TypeScript, Effect, Hono, OpenTUI/Solid, storage
  schemas, generated SDK, package layout, MCP runtime, or plugin surface.
- Do not broaden provider coverage or trigger hosted provider calls as part of this
  task.
- Do not store secrets in `chat.json`.
- Do not break the current noninteractive Spring Shell command path.

## Verification

Documentation-only children should run:

```bash
git --no-pager diff --check
```

Implementation children should use the Taskfile from `app/codegeist/cli`:

```bash
task test TEST=<test-selector>
task test
```

For local provider checks:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=<test-selector>
```

Do not document direct `mvn test` commands for new implementation tasks. Use
`task native-smoke`, `task qemu-windows-smoke`, or `task final-smoke-suite` only
when command runtime, packaging, native behavior, or smoke contracts change.
