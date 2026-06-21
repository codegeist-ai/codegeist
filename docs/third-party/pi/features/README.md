# Pi Feature Map

This feature map is source-backed by the Pi analysis workspace at `docs/third-party/pi/`.

## Product Surfaces

- Interactive coding-agent CLI: default `pi` command with TUI, editor, sessions, model selection, slash commands, tool rendering, and extension UI.
- Print mode: `pi -p` and non-TTY operation for single-shot responses.
- JSON event stream mode: `pi --mode json` emits session and agent events as JSON lines.
- RPC mode: `pi --mode rpc` accepts JSONL commands and emits responses, events, and extension UI requests for embedding.
- SDK: application code can create sessions or use lower-level agent and AI packages directly.
- Package manager: `pi install`, `pi remove`, `pi update`, `pi list`, and `pi config` manage extension/skill/prompt/theme packages.

## Core Agent Features

- Provider/model selection across many hosted, subscription, OAuth, and OpenAI-compatible providers.
- Tool-capable model registry and provider dispatch through `pi-ai`.
- Built-in local tools: `read`, `bash`, `edit`, `write`, `grep`, `find`, and `ls`.
- Default coding tool exposure: `read`, `bash`, `edit`, and `write`.
- Tool execution modes for parallel or sequential batches.
- Steering and follow-up queues for messages submitted while the agent is already working.
- Manual and automatic compaction with JSONL session history preserved.
- Session tree navigation, branching, forking, cloning, labels, and HTML export.

## Customization Features

- Extensions: TypeScript modules that can register tools, commands, shortcuts, flags, providers, event handlers, renderers, widgets, custom UI, and compaction behavior.
- Skills: Agent Skills-style Markdown capability packages loaded on demand or automatically.
- Prompt templates: reusable Markdown prompts expanded through slash commands.
- Themes: hot-reloadable theme files for the interactive TUI.
- Pi packages: npm or git packages that bundle extensions, skills, prompts, and themes.

## Terminal UI Features

- Differential rendering and synchronized output for flicker reduction.
- Component model with `Text`, `Input`, `Editor`, `Markdown`, `SelectList`, `SettingsList`, `Loader`, `Image`, `Box`, and related utilities.
- Overlay support for modal dialogs and replacement UI.
- Width-aware text utilities and tests for CJK, emoji, regional indicators, ANSI styling, and terminal edge cases.
- Clipboard image handling and Kitty/iTerm2 inline image support where the terminal supports it.

## Security And Isolation Features

- Project trust gates loading of project-local `.pi` settings/resources/packages/extensions.
- Explicit no-sandbox posture: Pi runs with the permissions of the launching process.
- Containerization guidance for Gondolin, plain Docker, and OpenShell.
- Offline mode and telemetry/update-check controls.
- Supply-chain hardening around exact direct dependencies, delayed npm release selection, shrinkwrap generation, lockfile review, and `--ignore-scripts` installs.

## Not Core By Design

The upstream docs explicitly keep these out of the core and recommend extensions, packages, files, or local tooling instead:

- MCP support.
- Built-in subagents.
- Built-in plan mode.
- Permission popups.
- Built-in todos.
- Background bash.
