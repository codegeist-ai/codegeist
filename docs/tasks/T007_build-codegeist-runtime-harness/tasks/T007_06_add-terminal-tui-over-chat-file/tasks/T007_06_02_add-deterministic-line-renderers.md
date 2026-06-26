# T007_06_02 Add Deterministic Line Renderers

Parent: `T007_06_add-terminal-tui-over-chat-file/task.md`

Status: open

## Goal

Render the TUI view model from `T007_06_01` into deterministic line-oriented output.
This establishes the primary automation-friendly fallback before any real terminal
or JLine integration is added.

## Dependencies

- Depends on `T007_06_01_build-tui-view-model-projection.md`.
- Must be completed before `T007_06_03_add-tui-input-parser-and-console-seam.md`.

## Scope

- Add renderer classes that convert `TuiViewModel` and `ToolActivityView` into
  stable `TuiLine` records.
- Keep styling separate from line text so tests do not depend on ANSI escape codes.
- Render chat text, compaction rows, persisted tool activity, runtime status,
  notifications, and help lines.
- Keep output readable in plain terminal scrollback.

## Classes To Create

Production package `ai.codegeist.app.tui`:

- `TuiLineRenderer`
- `TuiLine`
- `TuiLineStyle`
- `ToolActivityRenderer`
- `HelpRenderer`

## Behavioral Requirements

- The same view model must always render the same `TuiLine` sequence.
- `TuiLine.text` must not contain ANSI escape sequences.
- Header/status output should include store path, working directory, provider/model
  when present, local tool count, MCP client count, busy marker, and transient error
  marker.
- User rows should have a stable user prefix.
- Assistant rows should have a stable assistant prefix.
- Tool rows should show tool name, final status, and bounded output preview.
- Compaction rows should show compaction status without implying runtime compaction
  behavior.
- Multi-line text should be split and indented deterministically.
- `HelpRenderer` should document `/help`, `/status`, `/history`, `/quit`, Ctrl-D,
  and the first-slice Ctrl-C behavior.

## Tool Rendering Rules

- File tools: `codegeist_read`, `codegeist_list`, `codegeist_glob`, and
  `codegeist_grep`.
- Change tools: `codegeist_write` and `codegeist_edit`.
- Shell tool: `codegeist_shell`.
- Unknown tools: generic renderer with the tool name preserved.
- MCP tool source is not persisted today, so non-Codegeist tools should stay generic
  unless a later persisted source marker is added.

## Acceptance Criteria

- A focused renderer test proves deterministic lines for user, assistant,
  compaction, notification, and status output.
- A focused tool renderer test proves file, change, shell, failed, and generic tool
  output paths.
- No terminal, JLine, or Spring Shell command classes are added in this child.
- Renderer tests prove essential rows remain available for line-oriented fallback.

## Suggested Tests

- `TuiLineRendererTest`
- `ToolActivityRendererTest`

Candidate command from `app/codegeist/cli`:

```bash
task test TEST=TuiLineRendererTest,ToolActivityRendererTest
```

## Non-Goals

- Do not add input parsing.
- Do not add a controller loop.
- Do not add JLine terminal code.
- Do not parse structured shell fields out of `outputPreview` as a hidden contract.
- Do not add full-screen rendering, sidebars, prompt history, autocomplete, or theme
  configuration.
