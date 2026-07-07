# T007_06 Add TerminalUI Chat Harness

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Current Outcome

Codegeist now has the smallest useful Spring Shell `TerminalUI` launcher. The
`tui` command lives in `ai.codegeist.app.tui.TuiCommands` and delegates to
`CodegeistTerminalUi`. `CodegeistTerminalUi` builds a Spring Shell `TerminalUI`,
configures one bordered `BoxView` root, renders localized text from `CodegeistMessages`,
binds `Ctrl-Q` to interrupt the TUI loop, and enters `TerminalUI.run()`.

This is not a chat harness yet. There is no prompt submission, session projection,
streaming output, permission prompt, tool transcript view, virtual-terminal smoke,
or `task tui-smoke` entrypoint. The previous presenter, view factory, responsive
layout service, Spring Shell control wrapper package, custom JLine console, and
line-renderer pipeline remain removed.

`CodegeistLocaleService` uses optional app-wide `codegeist.locale` and otherwise
falls back to the JVM default locale for message lookup.

## Next Scope

The next TUI slice should add exactly one concrete interaction. If chat is next,
connect a prompt input to `ChatHarnessService.ask(true, prompt)` and display the
returned `CodegeistChatResponse.content()` in the TerminalUI surface.

Keep provider, tool/MCP, agent-loop, and session-store behavior behind
`ChatHarnessService` and existing runtime services. Add Codegeist-owned wrappers,
layout services, or long-lived view state only when a current test or behavior
requires them.

## Non-Goals

- Do not recreate a custom JLine console, deterministic line-renderer pipeline, or
  second agent runtime unless a future task explicitly replaces the Spring Shell
  approach.
- Do not persist prompt drafts, layout, focus, scroll, or other UI-only state into
  `.codegeist/session.json`.
- Do not add streaming, cancellation, permission prompts, patch review UI, shell
  review panes, session browsers, or richer transcript projection without a focused
  task.

## Verification

Use the normal Codegeist test entrypoint from `app/codegeist/cli`:

```bash
task test TEST=CodegeistTerminalUiTest,TuiCommandsTest,CodegeistLocaleServiceTest,CodegeistMessagesTest
task test TEST=VersionCommandsTests,CodegeistConfigCommandTest,AskCommandsSessionStoreTest
```

Do not run or document `task tui-smoke`; that Taskfile entrypoint does not exist.
