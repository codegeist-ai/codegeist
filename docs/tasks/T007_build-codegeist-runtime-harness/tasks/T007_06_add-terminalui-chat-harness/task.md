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

The end state for this task is a usable chat loop through the TUI: a user should be
able to enter prompts, submit them through `ChatHarnessService.ask(true, prompt)`,
see returned `CodegeistChatResponse.content()` values in the TerminalUI surface,
and continue the conversation from the same TUI session.

Work toward that end state one concrete interaction at a time. The next slice should
connect prompt input to `ChatHarnessService.ask(true, prompt)` and display the
returned response text. Follow-up slices can add repeated-turn ergonomics, visible
errors, prompt clearing, focus behavior, and transcript projection only when the
current behavior and tests require them.

Keep provider, tool/MCP, agent-loop, and session-store behavior behind
`ChatHarnessService` and existing runtime services. Add Codegeist-owned wrappers,
layout services, or long-lived view state only when a current test or behavior
requires them.

## Implementation Plan

Use `implementation-plan.md` as the detailed implementation handoff for this task.
It records the target behavior, Spring Shell `TerminalUI` API facts, proposed source
changes, test plan, verification commands, documentation updates, and risks. Keep
that plan current if the implementation approach changes before code is written.

## Non-Goals

- Do not recreate a custom JLine console, deterministic line-renderer pipeline, or
  second agent runtime unless a future task explicitly replaces the Spring Shell
  approach.
- Do not persist prompt drafts, layout, focus, scroll, or other UI-only state into
  `.codegeist/session.json`.
- Do not add streaming, cancellation, permission prompts, patch review UI, shell
  review panes, session browsers, or richer transcript projection without a focused
  task.

## Completion Criteria

- A user can complete at least one provider-backed prompt/response turn from the TUI.
- Repeated prompt/response turns can happen without restarting the TUI.
- TUI prompt submission goes through `ChatHarnessService.ask(true, prompt)`.
- Provider, tool/MCP, agent-loop, and session-store behavior remain harness-owned.
- The TUI surfaces returned response text and handled harness failures clearly enough
  for a terminal user.
- Focused tests cover the TUI command, root view, i18n lookup, prompt submission, and
  response/error display contracts.

## Verification

Use the normal Codegeist test entrypoint from `app/codegeist/cli`:

```bash
task test TEST=CodegeistTerminalUiTest,TuiCommandsTest,CodegeistLocaleServiceTest,CodegeistMessagesTest
task test TEST=VersionCommandsTests,CodegeistConfigCommandTest,AskCommandsSessionStoreTest
```

Do not run or document `task tui-smoke`; that Taskfile entrypoint does not exist.
