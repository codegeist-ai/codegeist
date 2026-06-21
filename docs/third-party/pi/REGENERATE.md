# Regenerate Pi Analysis Artifacts

This workspace separates durable documentation from reproducible heavy outputs.

## Source Checkout

The source checkout is a submodule:

```bash
git submodule update --init --recursive -- docs/third-party/pi/source
```

The analysis pass documented here used:

```text
source: https://github.com/earendil-works/pi.git
branch: main
revision: bc0db643502ba0bf1b227a97d9d5885cefc2b909
```

## Repomix

Regenerate `repomix-output.xml` from `docs/third-party/pi/source` with source, docs, package metadata, and config files. Exclude dependency directories, generated output, vendored code, lockfiles, shrinkwrap data, changelogs, binary/image assets, environment files, and secret-like paths where practical.

The last run used these effective filters from `docs/third-party/pi/source`:

```bash
repomix . \
  -o ../repomix-output.xml \
  --style xml \
  --include "**/*.{ts,tsx,js,jsx,mjs,cjs,json,md,yml,yaml,toml,sh,ps1,bat}" \
  --ignore "**/node_modules/**,**/.git/**,**/dist/**,**/build/**,**/coverage/**,**/.turbo/**,**/.next/**,**/target/**,**/vendor/**,**/*.generated.ts,**/*.generated.js,**/*.lock,**/package-lock.json,**/npm-shrinkwrap.json,**/CHANGELOG.md,**/.env*,**/*secret*,**/*token*,**/*.png,**/*.jpg,**/*.jpeg,**/*.gif,**/*.webp,**/*.svg,**/*.ico,**/*.pdf,**/*.zip,**/*.tar,**/*.gz" \
  --top-files-len 20
```

Expected checks:

- `docs/third-party/pi/repomix-output.xml` exists and is non-empty.
- It contains `<file path="...">` boundaries.
- Repomix reports no suspicious files.
- Any additional secret-pattern matches are reviewed as test or documentation placeholders before handoff.

## Graphify

Graph generation must be run through the shared `graphify` skill, not by duplicating its workflow in this command. In normal project-question workflows, `/ask-project` reads existing `graphify-out/` files. If those files are missing or incomplete, regenerate them by running:

```text
/analyse-project https://github.com/earendil-works/pi --project pi
```

The first analysis used a temporary `graphify-input-focus/` corpus in this workspace. It copied source-code and documentation files from `source/` while excluding generated, vendored, dependency-heavy, lock, build, binary, image, and environment-like files. The corpus was deleted after graph generation so it does not become durable documentation.

The final Graphify output path is:

```text
docs/third-party/pi/graphify-out/
```

Expected key files:

```text
graphify-out/GRAPH_REPORT.md
graphify-out/graph.json
graphify-out/graph.html
```

This graph exceeded Graphify's default HTML visualization limit. Use `GRAPHIFY_VIZ_NODE_LIMIT=10000` for HTML export if regenerating the same-sized graph.

`graphify-out/` is ignored but can remain locally as a useful cache for `/ask-project`. Temporary input directories such as `graphify-input-focus/` are only inputs to Graphify and should be removed after the output files exist.

## Mermaid Rendering

Render diagrams only when SVG handoff artifacts are needed:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/pi/diagrams"
```

Rendered SVGs are ignored and should be regenerated from `diagrams/source/*.mmd`.

## Runtime Verification

This analysis did not run upstream runtime commands. If a follow-up needs runtime evidence, inspect the upstream guidance first:

- `docs/third-party/pi/source/AGENTS.md`
- `docs/third-party/pi/source/CONTRIBUTING.md`
- `docs/third-party/pi/source/test.sh`
- `docs/third-party/pi/source/package.json`

Do not run provider-backed tests or interactive release smokes without an explicit task and credential/sandbox decision.
