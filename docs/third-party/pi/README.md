# Pi Third-Party Analysis

This workspace documents the third-party project at `https://github.com/earendil-works/pi.git` for follow-up analysis with `/ask-project`.

## Source

- Project: `pi`
- Upstream: `https://github.com/earendil-works/pi.git`
- Local checkout: `docs/third-party/pi/source`
- Branch: `main`
- Revision: `bc0db643502ba0bf1b227a97d9d5885cefc2b909`

## What Pi Is

Pi is a TypeScript/Node.js monorepo for a terminal coding-agent harness. The repository contains a minimal but highly extensible coding-agent CLI, a reusable stateful agent runtime, a provider-neutral LLM package, and a terminal UI library.

The upstream documentation emphasizes extension over built-in workflow breadth: subagents, plan mode, MCP, permission prompts, and todo systems are intentionally not core features. Users can implement or install those capabilities through TypeScript extensions, skills, prompt templates, themes, and Pi packages.

## Generated Documentation

- `ANALYSIS_REPORT.md` - architecture, feature, dependency, test, runtime-evidence, and risk findings.
- `features/README.md` - feature and capability map.
- `user/README.md` - user-facing commands, modes, configuration, and security notes.
- `developer/README.md` - source layout, runtime flow, tests, and maintenance notes.
- `developer/prompt-flow.md` - detailed CLI-to-agent prompt flow with provider, tool, extension, queue, and JSONL persistence boundaries.
- `diagrams/source/*.mmd` - editable Mermaid diagram sources.
- `REGENERATE.md` - commands and constraints for rebuilding analysis artifacts.

## Reproducible Artifacts

These files are generated and intentionally ignored:

- `repomix-output.xml`
- `graphify-out/`
- `analysis-manifest.json`
- `VERIFY_REPORT.md`
- `diagrams/rendered/`

The temporary `graphify-input-focus/` corpus is generated during analysis and deleted after Graphify outputs are produced. Do not keep it as durable documentation.

`graphify-out/` is ignored but useful locally: `/ask-project` reads `GRAPH_REPORT.md`, `graph.json`, and `graph.html` from that directory for graph-backed answers. If it is missing or incomplete, regenerate it through `/analyse-project https://github.com/earendil-works/pi --project pi`.

## Runtime Evidence

No upstream npm install, build, test, interactive TUI run, provider call, browser smoke test, package install, or release command was executed during this pass. Treat behavior claims as source- and documentation-backed static analysis until verified against a running checkout.

## Detailed Prompt Flow

The prompt-flow follow-up has been captured in `developer/prompt-flow.md`. Its editable sequence diagram source is `diagrams/source/prompt-flow-sequence.mmd`.
