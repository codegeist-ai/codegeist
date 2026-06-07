# T007_01 Define Chat File Tool Harness Scope

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Record the current T007 scope before implementation continues.

T007 is the local chat-file tool harness: `ask --chat <chat.json>`, resumable
file-based chat state, Codegeist-owned MCP client config, tools, patch/edit, shell,
and terminal TUI over the same chat file.

## Decisions

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

- Parent `task.md` names the expanded chat-file tool harness feature set.
- `docs/developer/specification/runtime-harness-implementation.md` describes the
  chat-file implementation plan.
- Earlier minimal-MCP-only child tasks are replaced by chat-file, tools, patch/shell,
  TUI, and verification slices.

## Acceptance Criteria

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
