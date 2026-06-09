# T007_01 Define Chat File Tool Harness Scope

Parent: `T007_build-codegeist-runtime-harness`

Status: completed

## Goal

Record the current T007 scope and the underlying coding-agent harness design before
implementation continues.

T007 is the local chat-file tool harness: `ask --chat <chat.json>`, resumable
file-based chat state, Codegeist-owned MCP client config, tools, patch/edit, shell,
and terminal TUI over the same chat file.

This child also creates a deep design document that defines what a coding agent
harness is. The document should translate the general `agent = model + harness`
model into Codegeist terms before Java implementation starts.

## Decisions

- `docs/tasks/T007_build-codegeist-runtime-harness/coding-agent-harness.md`
  defines the coding-agent harness concept for Codegeist and T007.
- Treat the model as the language intelligence engine and the harness as the
  surrounding system that owns state, tools, execution, context, policy,
  verification, and user interfaces.
- Codegeist can define multiple named harness profiles under top-level
  `harness:` in direct `codegeist.yml`.
- A top-level `mcp:` map defines reusable MCP server configurations. A harness
  profile selects which of those MCP servers are active for that profile.
- Code annotations define available Codegeist-owned harness capabilities, such as
  local tools and tool metadata. Runtime activation, limits, and profile
  composition come from `codegeist.yml`.
- `chat.json` is the source of truth for resuming and saving a chat.
- `ask` gets optional `--chat <chat.json>`.
- TUI opens, renders, updates, and saves the same chat file.
- MCP clients are configured in direct `codegeist.yml` through a top-level `mcp:`
  map.
- Codegeist-owned tools are in scope, including read/write working-directory file
  tools, MCP tools, patch/edit, and shell.
- Store only chat-relevant information needed to resume and save the chat.
- Do not store provider config, selected provider, selected model, MCP client
  definitions, enabled tool definitions, or status in `chat.json`.
- Do not add a database, server-side session service, remote sync, API/SDK, Vaadin,
  PF4J, JBang, LSP, skills, memory, or subagents in this T007 slice.

## Configurable Harness Profiles

Codegeist should treat a harness profile as a named runtime composition of provider
selection policy, MCP clients, Codegeist-owned tools, workspace policy, output
limits, and verification defaults. The first profile model belongs in direct
`codegeist.yml`, not in `chat.json`.

The intended shape is a top-level reusable MCP catalog plus a top-level `harness:`
map keyed by harness id:

```yaml
mcp:
  grep:
    type: stdio
    command: npx
    args:
      - -y
      - "@example/grep-mcp-server"

  filesystem:
    type: stdio
    command: npx
    args:
      - -y
      - "@modelcontextprotocol/server-filesystem"
      - .

harness:
  my-harness:
    mcp:
      grep:
        enabled: true
      filesystem:
        enabled: false
    tools:
      read:
        enabled: true
      write:
        enabled: false
      shell:
        enabled: true
        timeout-seconds: 30
    context:
      max-tool-output-chars: 12000
    workspace:
      root: .

  coding-harness:
    mcp:
      filesystem:
        enabled: true
    tools:
      read:
        enabled: true
      grep:
        enabled: true
      patch:
        enabled: true
      shell:
        enabled: true
```

Rules:

- Keep `mcp:` as the reusable catalog of MCP server definitions. This avoids
  duplicating the same server command under several harness profiles.
- Use `harness.<harness-id>.mcp.<mcp-id>` to select and optionally override how a
  catalog MCP server participates in that harness.
- Keep the first MCP selection object small: `enabled` plus focused per-profile
  limits or overrides only when an implementation test needs them.
- Use `harness.<harness-id>.tools.<tool-id>` to activate Codegeist-owned local
  tools such as `read`, `grep`, `write`, `patch`, and `shell` for that harness.
- Use annotations in Java source to describe available local harness capabilities,
  for example tool id, model-visible description, side-effect posture, and default
  limits.
- Resolve annotation metadata through an explicit Java registry, not broad runtime
  classpath scanning, so the design stays GraalVM-friendly.
- Let `codegeist.yml` decide profile composition and runtime limits. An annotation
  says what exists; a harness profile says what is active.
- Do not persist the active harness profile, MCP catalog, enabled tool definitions,
  or runtime limits into `chat.json`. Persist only chat messages and bounded tool
  activity needed to resume and render the chat.
- Defer final active-profile selection until a focused implementation task. Likely
  choices are a default harness in `codegeist.yml`, a CLI option such as
  `ask --harness my-harness`, and matching TUI selection.

Illustrative Java annotation direction:

```java
@HarnessTool(
        id = "shell",
        description = "Run a bounded local shell command",
        sideEffect = true)
final class ShellTool {
}
```

The annotation is metadata, not activation. If `shell` is not enabled under the
selected `harness.<id>.tools.shell` entry, the shell tool must not be exposed to the
model for that chat run.

## Rough Package Diagram

This is planned package direction for T007, not permission to create placeholder
packages. Add each package only when the focused child task introduces tested source
that belongs there.

```mermaid
flowchart TD
    subgraph app[ai.codegeist.app]
        boot[CodegeistApplication]
        commands[VersionCommands / config commands]
    end

    subgraph config[ai.codegeist.app.config]
        cfg[CodegeistConfig]
        cfgSvc[CodegeistConfigService]
        mcpCfg[MCP client config records]
    end

    subgraph chat[ai.codegeist.app.chat]
        ask[AskCommands]
        chatSvc[CodegeistChatService]
        chatReq[CodegeistChatRequest]
        chatResp[CodegeistChatResponse]
        model[CodegeistChatModel implementations]
    end

    subgraph chatfile[ai.codegeist.app.chat.file]
        chatFileSvc[ChatFileService]
        chatFileModel[chat.json records]
    end

    subgraph tool[ai.codegeist.app.tool]
        toolSvc[ToolService]
        descriptors[Tool descriptors and results]
    end

    subgraph workingdir[ai.codegeist.app.tool.workingdir]
        readOnly[read / list / glob / grep]
    end

    subgraph mutate[ai.codegeist.app.tool.mutate]
        patchEdit[patch / edit]
    end

    subgraph shell[ai.codegeist.app.tool.shell]
        shellTools[shell execution]
    end

    subgraph mcp[ai.codegeist.app.mcp]
        mcpClients[MCP client adapter]
        mcpTools[MCP tool callbacks]
    end

    subgraph tui[ai.codegeist.app.client.tui]
        terminal[Terminal TUI]
        renderer[chat file renderer]
    end

    ask --> cfgSvc
    ask --> chatFileSvc
    ask --> chatSvc
    terminal --> chatFileSvc
    terminal --> chatSvc
    terminal --> renderer
    renderer --> chatFileModel
    cfgSvc --> cfg
    cfg --> mcpCfg
    chatSvc --> model
    chatSvc --> toolSvc
    toolSvc --> descriptors
    toolSvc --> readOnly
    toolSvc --> patchEdit
    toolSvc --> shellTools
    toolSvc --> mcpTools
    mcpClients --> mcpTools
    mcpCfg --> mcpClients
    chatFileSvc --> chatFileModel
```

## Required Parent Changes

- `docs/tasks/T007_build-codegeist-runtime-harness/coding-agent-harness.md`
  describes what a coding agent harness is and maps the concept to the T007
  chat-file tool harness.
- Parent `task.md` names the expanded chat-file tool harness feature set.
- `docs/developer/specification/runtime-harness-implementation.md` describes the
  chat-file implementation plan.
- Earlier minimal-MCP-only child tasks are replaced by chat-file, tools, patch/shell,
  TUI, and verification slices.

## Acceptance Criteria

- A deep coding-agent harness document exists at
  `docs/tasks/T007_build-codegeist-runtime-harness/coding-agent-harness.md`.
- The document defines the model/harness boundary and explains state, tools,
  filesystem/git, shell execution, MCP, context management, safety, UI, and
  verification as harness responsibilities.
- The document maps the harness concept to Codegeist T007 and clearly identifies
  what remains deferred beyond this slice.
- The task defines multiple named `codegeist.yml` harness profiles under
  top-level `harness:`.
- The task distinguishes the reusable top-level `mcp:` server catalog from
  per-harness MCP server selection under `harness.<id>.mcp`.
- The task defines the boundary between annotation-described Codegeist harness
  capabilities and runtime activation through `codegeist.yml`.
- The task keeps active harness profile, MCP definitions, enabled tool
  definitions, and harness limits out of `chat.json`.
- The parent task clearly states the T007 completion feature set.
- The parent task defines `ask --chat <chat.json>` as the resumable chat entrypoint.
- The parent task identifies TUI, tools, patch/edit, shell, and file-based chat
  storage as in scope.
- A rough package diagram captures the planned package ownership for chat file,
  tools, MCP, mutation, shell, and TUI code.
- Follow-up child tasks are small enough to implement with focused tests.

## Verification

```bash
git --no-pager diff --check
```
