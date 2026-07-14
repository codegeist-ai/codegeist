# T007 Build Session Store Tool Harness

Status: open

## Goal

Implement a resumable Codegeist chat harness centered on a portable
`.codegeist/session.json` store. The existing `ask` command gains optional
`-c/--continue` support that appends to the latest stored session, while plain
`ask` creates a new stored session for the turn.

T007 targets a practical local coding-agent loop: one directory-local session store
with multiple chat sessions, optional MCP, Codegeist tools, patch/edit, shell,
TerminalUI, and file-based storage. The feature set is implemented through the TUI
chat-harness slice; the parent remains open only for final focused and broad
verification. The current implemented chat harness owns a synchronous
model/tool/model control loop over prompt-scoped callbacks. Do not add a database
or server runtime for this task.

## Current Codegeist Baseline

- `app/codegeist/cli` is the only implemented application module.
- The application uses Java 25, Spring Boot 4.0.6, Spring Shell 4.0.2, Spring AI
  `2.0.0-M6`, Spring AI Agent Utils `0.7.0`, Lombok, and GraalVM native build
  tooling.
- Implemented commands are `--version`, `--show-config`, provider-backed `ask`, and
  a `tui` command that starts a Spring Shell `TerminalUI` chat loop over the existing
  chat harness.
- `ask` selects the first configured provider through `CodegeistConfig`, uses the
  provider config's `defaultModel()`, calls `CodegeistChatService`, and prints the
  response text.
- Provider config currently supports typed config-only `ollama` and `openai`
  entries. Runtime provider calls are implemented only for local Ollama through
  `OllamaChatModel`.
- Direct `codegeist.yml` loading can parse the top-level `mcp:` client catalog as a
  YAML object keyed by client id. `stdio` clients use `type`, `command`, and `args`;
  `streamable_http` clients use `type`, `url`, and optional `endpoint`. The completed
  `T007_03` slice made MCP callbacks and local read/list/glob/grep/write tools
  available to the chat harness, and `T007_05` adds the first synchronous
  Codegeist-owned model/tool/model loop over those callbacks.
- `task test` from `app/codegeist/cli` is the implementation verification entrypoint
  and starts the fixed local Ollama container with `OLLAMA_ENTER=false` before
  Maven.
- `docs/developer/architecture/architecture.md` describes current state and must be
  updated whenever implementation changes packages, classes, configuration, runtime
  flows, or tests.

## Completion Feature Set

T007 is complete only when these features are implemented and tested:

- `ask` accepts optional `-c/--continue` support.
- Every successful `ask` saves the prompt and response/tool activity to
  `.codegeist/session.json`.
- With `-c/--continue`, `ask` loads `.codegeist/session.json`, appends the new
  prompt and response/tool activity to the session with the newest `updatedAt`, and
  saves the same store. Missing stores or empty `sessions[]` create a new session.
- Without `-c/--continue`, `ask` keeps the existing stdout contract and creates a
  new stored session instead of appending to the latest session.
- `.codegeist/session.json` stores only session-relevant information needed to
  resume and save chats: schema version, working directory, store timestamps,
  sessions, session timestamps, messages, assistant responses, tool calls/results,
  and bounded patch/edit/shell summaries that terminal or future UI surfaces can
  render.
- `.codegeist/session.json` does not store provider config, selected provider,
  selected model, MCP client definitions, enabled tool definitions, or runtime
  status.
- MCP clients are configured through a Codegeist-owned top-level `mcp:` map in
  direct `codegeist.yml`; Spring AI's `spring.ai.mcp.client.*` tree is not the
  public Codegeist config contract.
- Spring AI MCP client support is installed through `spring-ai-starter-mcp-client`
  and mapped from the Codegeist MCP config where needed.
- Codegeist-owned tools are available to chats. The first useful set includes
  read/write working-directory file tools and MCP tools.
- Patch/edit tools can mutate files under the chat working directory through the
  chat tool path and record bounded results in `.codegeist/session.json`.
- Shell tools can run bounded local commands through the chat tool path and record
  bounded results in `.codegeist/session.json`.
- A Spring Shell `TerminalUI` can submit prompts through the existing chat harness,
  display returned responses, and rely on the harness to save the same
  `.codegeist/session.json` store.
- Existing `--version`, `--show-config`, and plain `ask` stdout behavior keeps
  working.

## Session Store Contract

The first `.codegeist/session.json` shape should be small, versioned, and
inspectable. The exact Java records should be added only when implementation tests
need them, but the file must support this information class:

```json
{
  "schemaVersion": 1,
  "workingDir": "/home/test/Projects/codegeist-ai/codegeist",
  "createdAt": "2026-06-06T12:00:00Z",
  "updatedAt": "2026-06-06T12:01:00Z",
  "sessions": [
    {
      "id": "11111111-1111-4111-8111-111111111111",
      "title": "New session - 2026-06-06T12:00:00Z",
      "createdAt": "2026-06-06T12:00:00Z",
      "updatedAt": "2026-06-06T12:01:00Z",
      "messages": []
    }
  ]
}
```

Rules:

- Keep one session store per working directory at `.codegeist/session.json`.
- Keep multiple chat sessions in that store.
- Treat `.codegeist/session.json` as the resumable state source, not as an export
  format derived from another database.
- Store enough tool result data to render and continue the chat, but keep large
  command output and large file content bounded.
- Do not store API keys, OAuth tokens, cloud credentials, or evaluated secret values.
- Do not store provider selection, model selection, MCP client definitions, enabled
  tool definitions, or status in `.codegeist/session.json`; resolve those from
  current config and runtime behavior.
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
  remote-smoke:
    type: streamable_http
    url: http://127.0.0.1:3000
    endpoint: /mcp
```

The first implementation should support `stdio` clients and `streamable_http`
clients. Keep client ids as map keys. Use Docker only for the deterministic remote
MCP smoke fixture that exercises the `streamable_http` path. Add fields such as
environment, timeout, enablement, SSE, OAuth, or server management only when later
implementation tests need them.

## Child Tasks

- `T007_01_define-chat-file-tool-harness-scope.md` - completed original scope
  definition; this parent now supersedes the old chat-file naming with the
  session-store contract.
- `T007_02_add-session-store-and-continue-option.md` - add
  `.codegeist/session.json` and `ask -c/--continue` behavior.
- `T007_03_add-mcp-and-read-write-tools/task.md` - completed the first MCP and
  read/list/glob/grep/write tool path through focused child tasks; the minimal
  `mcp:` config root, local file callbacks, tool-aware chat harness, MCP adapter,
  Docker remote MCP smoke, final docs, and broad JVM verification are complete. Its
  planning docs include
  `mcp-and-readwrite-tools-spec.md`, `mcp-and-readwrite-tools-research.md`,
  `mcp-and-readwrite-tools-implementation-plan.md`, and
  `coding-agent-harness-implementations.md`.
- `T007_04_add-patch-edit-and-shell-tools/task.md` - add bounded patch/edit and
  shell tools that persist tool activity into `.codegeist/session.json`. Its
  research docs live in the same directory, including
  `ask-project-question-catalog.md`, `ask-project-research.md`, and
  `opencode-shell-tool-comparison.md`. Its child tasks split the work into exact
  edit, structured patch, shell, and final documentation/verification slices.
- `T007_05_add-agent-control-loop/task.md` - solved: added the first
  Codegeist-owned model/tool/model control loop so tool results drive model
  continuation instead of relying on a single provider call with hidden Spring AI
  tool execution. Its research docs include `ask-project-question-catalog.md`,
  `ask-project-research.md`, `opencode-agent-loop.md`, and `pi-agent-loop.md`
  plus `aider-agent-loop.md` with inline Mermaid diagrams for the Pi and Aider
  loop translations. Its detailed implementation handoff is
  `implementation-plan.md`. Final verification included broad JVM tests,
  `native-smoke`, `local-linux-smoke`, `mcp-remote-smoke`, and the Linux plus
  Windows QEMU `final-smoke-suite`.
- `T007_06_add-terminalui-chat-harness/task.md` - solved: connected the existing
  Spring Shell `TerminalUI` command to `ChatHarnessService.ask(true, prompt)`, with
  in-memory transcript display, repeated turns, handled harness errors, and no
  separate JLine console, second agent runtime, or persisted UI state. Its detailed
  handoff lives in `T007_06_add-terminalui-chat-harness/implementation-plan.md`.
- `T007_07_verify-chat-file-tool-harness.md` - verify the native TUI path with a
  VHS-recorded hello-world shell-script task that uses `codegeist_write` and
  `codegeist_shell`, records MP4/WebM evidence, and checks workspace plus session
  store side effects.

## Parent Acceptance Criteria

- `ask -c/--continue <prompt>` can resume the latest stored session from
  `.codegeist/session.json`.
- Missing `.codegeist/session.json` or an empty `sessions[]` creates a new session.
- The no-`--continue` `ask` path still works without requiring pre-existing session
  state and saves the turn to a new session.
- `CodegeistConfig` can load the minimal `mcp:` client map from direct
  `codegeist.yml`.
- Configured MCP callbacks can be made available to chat calls.
- Codegeist-owned tools include read/list/glob/grep/write plus patch/edit and shell.
- Tool calls and results are persisted in the active `.codegeist/session.json` with
  bounded output.
- Codegeist owns an iterative model/tool/model control loop for coding-agent turns;
  the current `T007_03_04` one-turn tool-aware harness is only the callback and
  persistence foundation for that loop.
- The TerminalUI path uses the existing chat harness and does not create a second
  persistence model.
- The native TUI hello-world smoke can record a real `codegeist tui` run through VHS,
  create `hello-world.sh`, run it with `sh hello-world.sh`, show the shell exit and
  output in the TUI transcript, and persist completed `codegeist_write` plus
  `codegeist_shell` tool parts.
- `docs/developer/architecture/architecture.md` documents the implemented session
  store, MCP, tools, patch/edit, shell, agent loop, and TUI behavior.

## Non-Goals

- Do not implement a database, server-side session service, remote sync, API/SDK,
  Vaadin, desktop UI, PF4J, plugin marketplace, JBang, LSP, skills, memory, or
  subagents in this T007 slice.
- Do not copy OpenCode's Bun, TypeScript, Effect, Hono, OpenTUI/Solid, storage
  schemas, generated SDK, package layout, MCP runtime, or plugin surface.
- Do not broaden provider coverage or trigger hosted provider calls as part of this
  task.
- Do not store secrets in `.codegeist/session.json`.
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
