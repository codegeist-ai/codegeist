# Tau Third-Party Analysis

Reproducible analysis workspace for `https://github.com/alejandro-ao/tau/tree/main`.

## Snapshot

| Field | Value |
| --- | --- |
| Project | `tau` |
| Source URL | `https://github.com/alejandro-ao/tau.git` |
| Source branch | `main` |
| Source commit | `1c10d33800d2c551bdff9f252f25ae51e8809e7e` |
| Local source | `docs/third-party/tau/source` |
| Analysis date | `2026-06-26` |

## Workspace Map

- `source/` - Tau source checkout, mounted as a git submodule.
- `ANALYSIS_REPORT.md` - current source-backed architecture and feature analysis.
- `REGENERATE.md` - how to refresh the checkout, Repomix pack, Graphify graph, and reports.
- `features/` - focused feature notes extracted from source and docs.
- `user/` - compact user-facing usage notes for later comparison work.
- `developer/` - developer-facing source map and extension notes.
- `diagrams/source/` - editable Mermaid diagram sources.
- `repomix-output.xml` - ignored Repomix packed source used by `/ask-project` deep dives.
- `graphify-out/` - ignored Graphify graph cache and report.
- `analysis-manifest.json` - ignored reproducibility manifest for this run.
- `VERIFY_REPORT.md` - ignored verification notes for this run.

## Generated Artifacts

Repomix was generated as `docs/third-party/tau/repomix-output.xml` with XML file boundaries. The final pack excluded large SVG/image and local session artifacts so later source questions can inspect code and docs without loading binary-heavy material.

Graphify was generated under `docs/third-party/tau/graphify-out/` from a filtered source/docs corpus. The final graph contains 3,064 nodes, 9,229 edges, and 135 communities. The generated report highlights the Textual TUI, slash commands, JSONL session storage, provider configuration, coding tools, and event-stream architecture as major hubs.

## Important Findings

- Tau is a Python `>=3.14` coding-agent harness inspired by Pi.
- The package split is `tau_ai -> tau_agent -> tau_coding` by dependency direction as consumed from application to provider layer.
- `tau_agent` owns the portable loop, harness, messages, events, tools, and session primitives.
- `tau_coding` owns the CLI, local coding tools, persistent sessions, resources, skills, slash commands, provider settings, renderers, and Textual TUI.
- The central runtime seam is provider-neutral event streaming from the harness into print renderers or the TUI.
- Sessions are append-only JSONL trees under Tau-owned home paths, and `MessageEndEvent` is the durable-message boundary.
- Built-in coding tools are `read`, `write`, `edit`, and `bash`.
- Provider support includes OpenAI-compatible providers, Anthropic, OpenAI Codex subscription auth, OpenRouter, Hugging Face entries, retry events, and thinking/reasoning deltas.

## Codegeist Reuse Notes

- Tau is useful evidence for event-stream contracts, session tree replay, TUI adapter boundaries, slash commands, model/provider selection, and tool result rendering.
- Tau should not be copied directly into Codegeist because it is Python/Textual/uv-based while Codegeist is Java/Spring/JLine-oriented.
- For Codegeist T007 work, use Tau as behavior evidence for session durability and UI state projection, not as an implementation blueprint.

## Ask-Project Starters

- `/ask-project tau "How does Tau persist and replay JSONL session trees?"`
- `/ask-project tau "How does the Textual TUI consume agent events and update visible state?"`
- `/ask-project tau "How are slash commands registered and executed in CodingSession?"`
- `/ask-project tau "How do Tau's read/write/edit/bash tools validate inputs and report results?"`
- `/ask-project tau "How does Tau create provider runtime adapters from provider settings?"`
