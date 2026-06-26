# Regenerate Tau Analysis

Use this file to refresh the Tau third-party workspace without guessing which artifacts are durable and which are local caches.

## Preferred Flow

Run the repository analysis workflow from the Codegeist workspace root:

```text
/analyse-project https://github.com/alejandro-ao/tau/tree/main --project tau
```

That workflow owns source checkout, Repomix, Graphify, manifest, verification, and durable handoff docs for `docs/third-party/tau/`.

## Source Checkout

The Tau source is a git submodule at:

```text
docs/third-party/tau/source
```

Refresh it with normal submodule commands when needed:

```bash
git submodule update --init docs/third-party/tau/source
git -C docs/third-party/tau/source fetch origin main
git -C docs/third-party/tau/source checkout -B main origin/main
```

Record the refreshed commit in `README.md`, `ANALYSIS_REPORT.md`, and `analysis-manifest.json`.

## Repomix

Generate the packed source as:

```text
docs/third-party/tau/repomix-output.xml
```

Use XML output so `/ask-project tau ...` can navigate `<file path="...">` boundaries. Include source code and documentation. Exclude binary, generated, dependency, build, cache, local session, secret-like, and large image/SVG artifacts.

The previous successful pack excluded at least:

- `**/*.svg`
- `**/*.png`
- `**/*.jpg`
- `**/*.jpeg`
- `**/*.gif`
- `**/*.webp`
- `**/*.jsonl`
- `session-temp.jsonl`
- `site/**`
- `.venv/**`
- `.ruff_cache/**`
- `.mypy_cache/**`
- `.pytest_cache/**`
- `__pycache__/**`

## Graphify

Use the shared Graphify skill at `@.opencode/skills/graphify/SKILL.md`. Do not duplicate the skill's workflow in a local script.

Write Graphify output to:

```text
docs/third-party/tau/graphify-out/
```

Build Graphify input from filtered source and documentation files. Keep temporary corpus directories outside the source checkout. If an external temp directory is not writable, use a repo-local ignored path named like:

```text
docs/third-party/tau/graphify-corpus-local/
```

Remove the temporary corpus after the run. Never write Graphify output under `docs/third-party/tau/source/`.

## Durable Docs

Refresh these tracked docs when source behavior or graph findings change:

- `README.md`
- `ANALYSIS_REPORT.md`
- `REGENERATE.md`
- `features/*.md`
- `user/*.md`
- `developer/*.md`
- `diagrams/source/*.mmd`

Refresh these ignored run artifacts for local reproducibility:

- `analysis-manifest.json`
- `VERIFY_REPORT.md`
- `repomix-output.xml`
- `graphify-out/GRAPH_REPORT.md`
- `graphify-out/graph.json`
- `graphify-out/graph.html`
- `graphify-out/cost.json`

## Post-Run Checks

Run focused checks from the Codegeist workspace root:

```bash
test -f docs/third-party/tau/repomix-output.xml
test -f docs/third-party/tau/graphify-out/GRAPH_REPORT.md
test -f docs/third-party/tau/graphify-out/graph.json
test ! -e docs/third-party/tau/source/graphify-out
test ! -e docs/third-party/tau/graphify-input-focus
git --no-pager diff --check
git --no-pager status --short
```

Also inspect the first lines of `repomix-output.xml` and verify it contains file-boundary entries before using `/ask-project` for deep source questions.
