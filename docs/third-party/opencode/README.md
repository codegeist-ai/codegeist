# OpenCode Third-Party Analysis

This workspace documents the third-party project at `https://github.com/anomalyco/opencode.git` for follow-up analysis with `/ask-project`.

## Source

- Project: `opencode`
- Upstream: `https://github.com/anomalyco/opencode.git`
- Local checkout: `docs/third-party/opencode/source`
- Branch: `dev`
- Revision: `d46af9cf1e7168d519377044f2412dea08ead5f8`

## What OpenCode Is

OpenCode is an open-source, provider-agnostic AI coding agent. Its main product surface is a terminal-first coding assistant, but the repository also includes a headless server, web UI, desktop app, plugin package, and generated SDKs.

The architecture is client/server oriented: CLI, TUI, web, desktop, SDK, and plugin consumers interact with a core runtime that manages sessions, providers, tools, permissions, storage, and event streams.

## Generated Documentation

- `ANALYSIS_REPORT.md` - initial architecture and evidence report.
- `features/README.md` - feature map and important capability clusters.
- `user/README.md` - user-facing usage surfaces and workflows.
- `developer/README.md` - developer-oriented repository and runtime notes.
- `diagrams/source/*.mmd` - editable Mermaid diagram sources.
- `REGENERATE.md` - commands and constraints for rebuilding analysis artifacts.

## Reproducible Artifacts

These files are generated and intentionally ignored:

- `repomix-output.xml`
- `graphify-out/`
- `graphify-corpus*/`
- `analysis-manifest.json`
- `VERIFY_REPORT.md`
- `diagrams/rendered/`

`graphify-out/` is ignored but useful locally: `/ask-project` reads
`GRAPH_REPORT.md`, `COMMUNITIES_BY_FILE.md`, and `graph.json` from that
directory for graph-backed answers. If it is missing or incomplete,
`/ask-project` should route regeneration through `/analyse-project`, which owns
the shared Graphify skill workflow.

Graphify was run through the shared `graphify` skill workflow against a focused
runtime corpus because the full source/document corpus is too large for the
default no-question path. Repomix was generated for the broader source tree and
copied into this workspace as reproducible source material. The latest run used
a temporary non-ignored `graphify-input-focus` directory and deleted it after
generation; do not use ignored `graphify-corpus*` directories as active Graphify
inputs because Graphify's detector skips them.

## Runtime Evidence

No runtime commands, server processes, TUI sessions, desktop app, API calls, provider authentication, plugin loading, PTY interactions, or tests were executed during this pass. Treat behavioral claims as static-analysis findings until verified against a running checkout.

## Recommended Next Question

Use:

```text
/ask-project opencode "How does a user prompt flow from the CLI or TUI through session processing, provider streaming, tool execution, permissions, and event rendering?"
```
