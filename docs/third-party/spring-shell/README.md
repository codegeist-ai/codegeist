# Spring Shell Third-Party Analysis

Local reproducible analysis workspace for
`https://github.com/spring-projects/spring-shell`.

## Source

- Checkout: `docs/third-party/spring-shell/source`
- Source type: git submodule tracking `main`
- Source commit: `fa298743fcbf7a02d9ca7a8c68d62f2005744094`
- Upstream URL: `https://github.com/spring-projects/spring-shell`

## Artifact Status

Durable docs in this directory summarize a static source analysis refresh from
2026-07-01. The local ignored analysis cache is present for `/ask-project`:

- `repomix-output.xml` - Repomix packed source, 549 file entries,
  1,729,410 bytes.
- `graphify-out/GRAPH_REPORT.md` - Graphify report for a filtered source/docs
  corpus.
- `graphify-out/graph.json` - Graphify graph, 5,217 nodes, 14,299 edges,
  25 hyperedges.
- `graphify-out/graph.html` - interactive local graph visualization.
- `analysis-manifest.json` - ignored reproducibility manifest.
- `VERIFY_REPORT.md` - ignored verification report for generated artifacts.

`graphify-out/`, `repomix-output.xml`, `analysis-manifest.json`,
`VERIFY_REPORT.md`, and rendered diagrams are intentionally ignored by the
parent repository but useful for follow-up questions.

## Durable Handoff Docs

- `ANALYSIS_REPORT.md` - architecture, features, dependencies, test evidence,
  runtime-evidence gaps, and risks.
- `REGENERATE.md` - exact local regeneration workflow.
- `features/command-model.md` - command registration, parsing, execution, and
  testing contracts.
- `features/terminal-ui.md` - TerminalUI, event loop, views, modal, and theme
  contracts.
- `user/getting-started.md` - user-facing setup and command behavior summary.
- `developer/source-map.md` - source modules and high-value files for future
  `/ask-project` dives.
- `diagrams/source/runtime-map.mmd` - editable Mermaid architecture sketch.

## Next Useful Questions

Use `/ask-project spring-shell "..."` for focused follow-up. Good first
questions:

- `How does Spring Shell choose interactive versus non-interactive command execution?`
- `How does TerminalUI route key and mouse events to focused views?`
- `What is the minimum source path to understand command registration and execution?`
- `Which Spring Shell test utilities are most relevant to Codegeist CLI and TUI tests?`

## Runtime Evidence

No Spring Shell build, sample app, or tests were executed in this refresh. The
analysis is source, docs, Repomix, and Graphify backed only.
