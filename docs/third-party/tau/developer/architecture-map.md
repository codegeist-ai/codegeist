# Tau Developer Architecture Map

Source-backed map for developers and coding agents inspecting Tau through this workspace.

## Package Boundaries

| Package | Owns | Must avoid |
| --- | --- | --- |
| `tau_ai` | Provider interfaces, provider events, fake provider, OpenAI-compatible, OpenAI Codex, Anthropic, retry helpers. | CLI, Textual widgets, session file layout, app resources. |
| `tau_agent` | Messages, events, tool contracts, pure loop, harness, cancellation, session primitives. | Rich, Textual, Typer, Tau home paths, prompt templates, slash commands. |
| `tau_coding` | CLI, provider config/runtime, resources, skills, prompt templates, system prompt, local tools, persistent sessions, renderers, Textual TUI. | Provider-specific logic in portable core. |

## High-Value Entry Points

- `src/tau_coding/cli.py` - Typer application, top-level command parsing, TUI/print/setup/provider/session/export dispatch.
- `src/tau_coding/session.py` - coding-session orchestration, durable storage, command handling, resources, provider switching, compaction, thinking levels.
- `src/tau_agent/harness.py` - reusable stateful agent brain around the pure loop.
- `src/tau_agent/loop.py` - provider/tool loop and event stream.
- `src/tau_coding/tools.py` - built-in read/write/edit/bash tools.
- `src/tau_coding/tui/app.py` - Textual interactive application and user actions.
- `src/tau_coding/tui/adapter.py` - projection from agent events into TUI state.
- `src/tau_coding/provider_runtime.py` - creation of runtime provider adapters from provider config.
- `src/tau_coding/provider_config.py` - provider settings and selection rules.
- `src/tau_agent/session/` - append-only JSONL session entry model and replay helpers.

## Test Map

- `tests/test_agent_loop.py` and `tests/test_agent_harness.py` cover the portable runtime loop.
- `tests/test_coding_session.py`, `tests/test_session.py`, and `tests/test_session_manager.py` cover persistence and session orchestration.
- `tests/test_coding_tools.py` covers local tool contracts.
- `tests/test_cli.py` covers Typer command behavior.
- `tests/test_provider_config.py`, `tests/test_provider_runtime.py`, and `tests/test_tau_ai.py` cover provider configuration and adapters.
- `tests/test_tui_adapter.py`, `tests/test_tui_app.py`, `tests/test_tui_autocomplete.py`, and `tests/test_tui_config.py` cover Textual-facing behavior.

## Search Hints

- Agent loop: `run_agent_loop`, `AgentHarness`, `QueuedMessages`, `MessageEndEvent`.
- Tools: `create_coding_tools`, `create_read_tool_definition`, `create_edit_tool_definition`, `ToolDefinition`.
- Sessions: `JsonlSessionStorage`, `SessionState`, `LeafEntry`, `CodingSession.load`, `MessageEndEvent`.
- TUI: `TauTuiApp`, `PromptInput`, `TuiEventAdapter`, `TuiState`, `build_completion_state`.
- Providers: `ProviderSettings`, `resolve_provider_selection`, `create_model_provider`, `OpenAICompatibleProviderConfig`.
- Resources: `TauPaths`, `TauResourcePaths`, `load_skills_with_diagnostics`, `build_system_prompt`.

## Sharp Edges

- Tau is under active development, so public commands and internals are not frozen.
- Graphify inferred edges should be checked against source before being treated as facts.
- Provider tests or manual runs can touch external APIs; review credential and billing implications before running hosted-provider checks.
- Local session files can contain user prompts and tool output. Keep `*.jsonl` session artifacts out of analysis packs unless explicitly requested.
