# Regenerate OpenCode Analysis Artifacts

This workspace separates durable documentation from reproducible heavy outputs.

## Source Checkout

The source checkout is a submodule:

```bash
git submodule update --init --recursive -- docs/third-party/opencode/source
```

## Repomix

Regenerate `repomix-output.xml` from `docs/third-party/opencode/source` with source, docs, package metadata, and config files. Exclude dependency directories, generated output, lockfiles, localized README copies, and very large generated fixtures where practical.

The last run used these effective filters:

```text
include: **/*.{ts,tsx,js,jsx,json,md,yml,yaml,toml,nix,go,sh}
ignore: **/node_modules/**, **/.git/**, **/bun.lock, **/dist/**, **/build/**, **/.sst/**, **/coverage/**, **/*.lock, **/README.*.md
```

## Graphify

Graph generation must be run through the shared `graphify` skill, not by
duplicating its workflow in this command. In normal project-question workflows,
`/ask-project` reads existing `graphify-out/` files. If those files are missing
or incomplete, regenerate them by running `/analyse-project <source-or-url>
--project opencode`; `/ask-project` should not invoke Graphify directly.

The first analysis used a focused corpus because the full source/document corpus exceeded the default no-question threshold. Rebuild a focused corpus from:

```text
README.md
AGENTS.md
CONTRIBUTING.md
package.json
packages/opencode/package.json
packages/opencode/src/server/**
packages/opencode/src/session/**
packages/opencode/src/provider/**
packages/opencode/src/config/**
packages/opencode/src/agent/**
```

Create a temporary non-ignored input directory such as `graphify-input-focus` in
this workspace. Do not use a `graphify-corpus*` directory as the active input:
those paths are ignored by this repository and Graphify's detector skips ignored
directories, which reports a zero-file corpus. Write Graphify outputs to:

```text
docs/third-party/opencode/graphify-out/
```

Expected key files:

```text
graphify-out/GRAPH_REPORT.md
graphify-out/COMMUNITIES_BY_FILE.md
graphify-out/graph.json
graphify-out/graph.html
```

`graphify-out/` is ignored but can remain locally as a useful cache for
`/ask-project`. Temporary input directories such as `graphify-input-focus` are
only inputs to the Graphify run and should be deleted after these output files
exist.

## Mermaid Rendering

Render diagrams only when SVG handoff artifacts are needed:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/opencode/diagrams"
```

Rendered SVGs are ignored and should be regenerated from `diagrams/source/*.mmd`.
