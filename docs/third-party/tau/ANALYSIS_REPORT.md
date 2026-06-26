# Tau Analysis Report

Current-state source analysis for Tau at commit `1c10d33800d2c551bdff9f252f25ae51e8809e7e`.

## Executive Summary

Tau is a compact Python coding-agent harness that makes the usual coding-agent layers explicit. The source is organized into provider streaming, a portable agent loop and harness, and a coding application that adds local tools, persistence, resources, commands, renderers, and a Textual terminal UI.

The project is useful as behavior evidence for Codegeist because it shows a small but featureful implementation of provider-neutral events, durable session trees, local file and shell tools, print/TUI renderers, slash commands, provider selection, and context management. It is less useful as direct implementation source because its runtime assumptions are Python, Textual, Typer, Rich, Pydantic, and uv rather than Java, Spring, Spring Shell, and JLine.

## Evidence Used

- `source/README.md` for current capabilities, install/use commands, package split, and development workflow.
- `source/pyproject.toml` for Python version, dependencies, console entrypoint, package list, and test/lint configuration.
- `source/docs/01-architecture.md` through `source/docs/04-sessions.md` for architecture, agent loop, tool, and session contracts.
- `source/docs/architecture/index.md` for phase history and current implemented layers.
- `source/src/tau_agent/loop.py` and `source/src/tau_agent/harness.py` for provider-neutral loop and harness behavior.
- `source/src/tau_coding/session.py` for persistent coding-session orchestration.
- `source/src/tau_coding/tools.py` for built-in local tool behavior.
- `source/src/tau_coding/cli.py` and `source/src/tau_coding/tui/app.py` for CLI and Textual UI entrypoints.
- `graphify-out/GRAPH_REPORT.md` and `graphify-out/graph.json` for structural navigation and cross-file community detection.
- `repomix-output.xml` for later source-backed `/ask-project` deep dives.

## Architecture

Tau's layers are deliberately small:

| Layer | Responsibility | Key files |
| --- | --- | --- |
| `tau_ai` | Provider/model streaming and provider events. | `src/tau_ai/provider.py`, `src/tau_ai/events.py`, provider adapters |
| `tau_agent` | Portable messages, tools, events, loop, harness, and session primitives. | `src/tau_agent/loop.py`, `src/tau_agent/harness.py`, `src/tau_agent/session/` |
| `tau_coding` | Coding-agent application: CLI, tools, sessions, resources, skills, commands, rendering, provider settings, TUI. | `src/tau_coding/cli.py`, `src/tau_coding/session.py`, `src/tau_coding/tui/` |

The dependency direction is described by Tau as `tau_coding -> tau_agent -> tau_ai`. The practical meaning is that the application layer consumes the portable agent layer, and the portable loop consumes the provider interface. The core agent package must not import CLI, Rich, Textual, app resource loading, or local session paths.

## Runtime Flow

The provider-neutral loop in `tau_agent.loop.run_agent_loop` receives a provider, model, system prompt, transcript, tools, optional max-turn limit, cancellation token, and queued-message callbacks. It yields `AgentStartEvent`, per-turn events, message events, retry/thinking events, tool start/end events, queue updates, recoverable/nonrecoverable errors, and `AgentEndEvent`.

The loop appends completed assistant messages and tool-result messages to the caller-owned transcript. This keeps the pure loop stateless while allowing `AgentHarness` and `CodingSession` to own transcript and persistence concerns.

`AgentHarness` is the reusable stateful brain. It owns the in-memory transcript, event subscribers, cancellation token, active-running flag, steering queue, and follow-up queue. It delegates execution to `run_agent_loop` and stays independent of CLI, Rich, Textual, session files, and resource loading.

`CodingSession` is the application environment wrapper. It wires `AgentHarness` with append-only session storage, default local tools, command registry, resource discovery, prompt templates, skills, provider settings, thinking level, compaction support, and diagnostic logging.

## Feature Surface

- CLI entrypoint: `tau = "tau_coding.cli:app"`.
- Main modes: interactive Textual TUI, initial prompt into TUI, non-interactive print mode with `-p/--prompt`, provider setup, provider listing, session listing, session export, and resume.
- Built-in tools: `read`, `write`, `edit`, and `bash`.
- Session model: append-only JSONL entry tree with branchable entries, durable `MessageEndEvent` boundary, session info/model/thinking entries, compaction entries, branch summaries, labels, leaves, and export paths.
- TUI surface: prompt input, transcript view, session sidebar, completion suggestions, command palette behavior, model picker, provider login, theme settings, queued steering/follow-ups, cancel, thinking toggles, and tool-output controls.
- Provider support: OpenAI-compatible providers, Anthropic, OpenAI Codex subscription OAuth, OpenRouter, Hugging Face catalog entries, fake provider for tests, retry/backoff events, and thinking/reasoning delta events.
- Resources: Tau home paths, project-local `.tau` and `.agents` resources, `AGENTS.md`, prompt templates, skills, and system prompt assembly.
- Context management: rough context accounting, manual compaction, optional automatic compaction, branch summaries, and session export.

## Tool Behavior

Tau exposes local tools as provider-neutral `AgentTool` objects with JSON schemas and async executors.

The `read` tool opens UTF-8 text files, supports `offset` and `limit`, truncates large output to 2,000 lines or 50 KB, and returns base64 metadata for supported image MIME types. It rejects missing paths, directories, invalid offsets, and unsupported binary content.

The `write` tool creates parent directories and writes complete UTF-8 file contents. Writes and edits are serialized per resolved path within the current process.

The `edit` tool applies exact replacements. It validates every edit before writing, requires each `oldText` to be non-empty and unique in the original file, rejects overlapping edits, preserves UTF-8 BOMs, restores dominant line endings, and returns diff metadata.

The `bash` tool runs one shell command in the configured working directory, combines stdout and stderr, reports exit code, timeout state, duration, truncation metadata, and stores full output in a temp `.log` file when returned output is truncated.

## Session Behavior

Tau sessions are append-only JSONL trees rather than mutable snapshots. The storage layer can serialize and deserialize entries, append entries to local files, read them in order, reconstruct linear state, and reconstruct root-to-leaf branch paths.

`CodingSession` uses `MessageEndEvent` as the durable-message boundary. Completed messages are appended immediately, before the full agent run necessarily finishes. This protects branchability and completed-message recovery when cancellation or process failure occurs.

Empty sessions are lazy. Tau prepares initial metadata in memory and materializes the transcript file only when the first durable session entry is appended.

## UI Behavior

The Textual TUI is an adapter over the same event stream used by print mode. `tau_coding.tui.app.TauTuiApp` is the graph hub for interactive behavior, while `TuiEventAdapter`, `TuiState`, widgets, autocomplete, and config modules split event projection, state, rendering, completions, themes, and keybindings.

Important UI behaviors include multiline prompt input, prompt completions, slash-command palette, session picker, queued follow-up prompts, cancellation, scoped model cycling, thinking controls, tool-output toggles, provider login, model selection, theme persistence, transcript wrapping, and sidebar metadata.

## Test And Quality Signals

The source includes tests for the loop, harness, messages/types, sessions, coding session, tools, CLI, provider config/runtime, provider adapters, context, resources, skills, prompt templates, rendering, session export, session manager, TUI adapter, TUI app, autocomplete, TUI config, OAuth, credentials, and thinking controls.

Project commands in `README.md` and `docs/getting-started.md` use `uv run pytest`, `uv run ruff check .`, `uv run ruff format --check .`, and `uv run mypy`. This analysis did not run Tau's test suite because the active task was source analysis, not third-party runtime validation, and the host Python version/toolchain was not established as Python `>=3.14`.

## Graphify Findings

Graphify reported 3,064 nodes, 9,229 edges, and 135 communities. The most connected nodes were `TauTuiApp`, `FakeSession`, `CodingSession`, `UserMessage`, `TauPaths`, `AssistantMessage`, `AgentTool`, `FakeProvider`, `SessionManager`, and `JsonlSessionStorage`.

High-value communities include Textual TUI app, slash commands, session entries, JSONL session storage, provider selection/config, agent harness, coding tools, autocomplete, runtime provider, system prompt, session tree, TUI adapter/tests, OAuth flow, and transcript rendering.

The Graphify token-cost report recorded `0 input` and `0 output` tokens because extraction used subagent paths that did not estimate tokens. Treat the structural graph as valid navigation evidence, but do not treat that cost line as a real model-cost measurement.

## Risks And Gaps

- Tau targets Python `>=3.14`, which may be newer than many default development environments.
- The repository is explicitly under active development, so command surface and internals may change.
- Runtime validation was not executed in this analysis workspace.
- Graphify includes inferred edges, so inferred relationships should be verified against source before being used as implementation facts.
- Provider behavior that touches hosted APIs needs separate runtime verification and credential/billing review.
- The source checkout includes a local-looking `session-temp.jsonl`; it was intentionally excluded from generated analysis artifacts.

## Codegeist Relevance

Tau is directly relevant to Codegeist T007/TUI planning in these areas:

- Provider-neutral event stream consumed by both print and TUI frontends.
- Stateful harness over a pure provider/tool loop.
- Durable session boundary tied to completed messages instead of whole-run completion.
- Append-only session tree replay and branch selection.
- UI adapter that projects agent events into frontend state.
- Local read/write/edit/bash tool contracts with deterministic result objects.
- Slash-command registry and autocomplete surface.
- Model/provider selection separated from provider runtime adapters.

Tau should not drive Codegeist implementation structure directly. Codegeist should translate behavior into Java, Spring, Spring Shell, JLine, Codegeist config, and `.codegeist/session.json` contracts.
