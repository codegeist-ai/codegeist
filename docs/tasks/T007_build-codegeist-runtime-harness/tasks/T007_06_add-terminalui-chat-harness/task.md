# T007_06 Add TerminalUI Chat Harness

Parent: `T007_build-codegeist-runtime-harness`

Status: solved

## Current Outcome

Codegeist now has a minimal Spring Shell `TerminalUI` chat loop. The `tui` command
lives in `ai.codegeist.app.tui.TuiCommands` and delegates to `CodegeistTerminalUi`.
`CodegeistTerminalUi` builds Spring Shell `TerminalUI` instances, installs a
bordered `GridView` root with a transcript `BoxView` and prompt `InputView`,
focuses the prompt, binds `Ctrl-Q` to interrupt the TUI loop, and preserves the
local transcript across normal `TerminalUI.run()` returns.

Pressing Enter on a non-blank prompt submits exactly one turn through
`ChatHarnessService.ask(true, prompt)`. The TUI appends user and assistant lines to
an in-memory transcript, displays handled harness failures as `Error: Command
failed: ...`, rebuilds the prompt input after each submission because `InputView`
has no public clear API, and supports repeated turns without restarting the
Codegeist process.

Provider selection, local/MCP tools, the Codegeist agent loop, and
`.codegeist/session.json` persistence remain owned by `ChatHarnessService` and the
runtime services behind it. The TUI does not store prompt drafts, focus, layout,
scroll, runtime status, provider config, tool config, or any other UI-only state.

There is still no streaming output, permission prompt, live tool timeline, or generic
`task tui-smoke` entrypoint. T007_07 later added bounded completed-tool previews over
this same surface and a focused `tui-hello-world-smoke`; it did not add a second TUI
runtime. The previous presenter, view factory, responsive layout service, Spring
Shell control wrapper package, custom JLine console, and line-renderer pipeline
remain removed.

`CodegeistLocaleService` uses optional app-wide `codegeist.locale` and otherwise
falls back to the JVM default locale for message lookup.

## Next Scope

`T007_06` and the later T007_07 native verification are complete. Future TUI slices
can add streaming, cancellation, permission prompts, live tool progress, richer
focus controls, or session browsing only behind focused tasks.

## Implementation Plan

`implementation-plan.md` is retained as the detailed implementation handoff and API
research record for this task. The implemented source follows the plan's small
Spring Shell `TerminalUI` approach: no custom JLine console, no second agent
runtime, no persisted UI state, and prompt submission through
`ChatHarnessService.ask(true, prompt)`.

## Non-Goals

- Do not recreate a custom JLine console, deterministic line-renderer pipeline, or
  second agent runtime unless a future task explicitly replaces the Spring Shell
  approach.
- Do not persist prompt drafts, layout, focus, scroll, or other UI-only state into
  `.codegeist/session.json`.
- Do not add streaming, cancellation, permission prompts, patch review UI, shell
  review panes, session browsers, or richer live transcript projection without a
  focused task.

## Completion Criteria

- Done: a user can complete provider-backed prompt/response turns from the TUI when
  the existing chat harness is configured with a provider.
- Done: repeated prompt/response turns can happen without restarting the Codegeist
  process.
- Done: TUI prompt submission goes through `ChatHarnessService.ask(true, prompt)`.
- Done: provider, tool/MCP, agent-loop, and session-store behavior remain
  harness-owned.
- Done: the TUI surfaces returned response text and handled harness failures clearly
  enough for a terminal user.
- Done: focused tests cover the TUI command, root view, i18n lookup, prompt
  submission, repeated response display, blank prompts, transcript capping, and
  handled error display contracts.

## Verification

Use the normal Codegeist test entrypoint from `app/codegeist/cli`:

```bash
task test TEST=CodegeistTerminalUiTest,TuiCommandsTest,CodegeistLocaleServiceTest,CodegeistMessagesTest
task test TEST=VersionCommandsTests,CodegeistConfigCommandTest,AskCommandsSessionStoreTest
```

Do not run or document a generic `task tui-smoke`; use `task tui-capture-smoke`
only for documentation-preview capture artifacts.

Implementation verification:

- `task cli:test TEST=CodegeistTerminalUiTest,TuiCommandsTest,CodegeistLocaleServiceTest,CodegeistMessagesTest`
  passed from the repository root with 14 tests, 0 failures, 0 errors, and 0 skipped.
- `task test TEST=VersionCommandsTests,CodegeistConfigCommandTest,AskCommandsSessionStoreTest`
  passed with 6 tests, 0 failures, 0 errors, and 0 skipped.
