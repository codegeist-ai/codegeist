# Regenerate mini-SWE-agent Analysis

Use this file when refreshing `docs/third-party/mini-swe-agent/`.

## Preferred Command

```text
/analyse-project https://github.com/SWE-agent/mini-swe-agent --project mini-swe-agent
```

The command owns source checkout, Repomix, Graphify, manifest, verification, and
durable handoff docs. Do not create a separate local analysis script.

## Source Checkout

The source is a git submodule at:

```text
docs/third-party/mini-swe-agent/source
```

If it is registered but missing, initialize it with:

```bash
git submodule update --init --recursive -- docs/third-party/mini-swe-agent/source
```

Ask before replacing an existing checkout or changing its tracked branch.

## Repomix

Regenerate the packed source from the repository root:

```bash
repomix docs/third-party/mini-swe-agent/source --style xml --parsable-style --top-files-len 20 --output docs/third-party/mini-swe-agent/repomix-output.xml --include "**/*.py,**/*.md,**/*.rst,**/*.toml,**/*.yaml,**/*.yml,**/*.json,**/*.txt,**/*.sh,**/*.ini,**/*.cfg,**/*.html,**/*.css,**/*.js,**/*.ts,**/*.tsx" --ignore ".git/**,.github/dependabot.yml,__pycache__/**,.pytest_cache/**,.mypy_cache/**,.ruff_cache/**,.venv/**,venv/**,env/**,dist/**,build/**,site/**,htmlcov/**,.coverage,coverage/**,*.lock,uv.lock,package-lock.json,pnpm-lock.yaml,yarn.lock,*.png,*.jpg,*.jpeg,*.gif,*.webp,*.ico,*.pdf,*.zip,*.tar,*.tar.gz,*.tgz,*.whl,*.pyc,*.pyo,*.so,*.dylib,*.dll,*.exe,*.log,*.pem,*.key,.env*,**/secrets/**,**/*secret*,**/*token*"
```

Verify that the output exists, contains `<file path=` boundaries, and that
Repomix reports no suspicious files unless a future review explains the finding.

## Graphify

Load the shared Graphify skill and run it from:

```text
docs/third-party/mini-swe-agent/
```

Use a temporary source/document corpus outside the repository and delete it after
Graphify finishes. Keep output under:

```text
docs/third-party/mini-swe-agent/graphify-out/
```

Default corpus policy for this workspace:

- Include Python source, tests, Markdown, YAML, TOML, JSON, shell, and lightweight
  config/docs files.
- Exclude `.git`, caches, virtualenvs, build outputs, coverage outputs, lockfiles,
  binary/media/archive files, logs, `.env*`, key files, and secret-like names.
- Exclude `docs/data/all_models.txt` from Graphify because it is large model
  metadata and not useful for default graph communities. It can still be present
  in Repomix when a follow-up question needs model metadata.

The latest Graphify run used AST extraction for Python code and four parallel
semantic extraction chunks for documentation/config files.

## Durable Docs

After regenerating artifacts, refresh these durable files from current evidence:

- `README.md`
- `ANALYSIS_REPORT.md`
- `features/README.md`
- `user/README.md`
- `developer/README.md`
- `developer/runtime-flow.md`
- `diagrams/source/runtime-flow.mmd`
- `REGENERATE.md`

Update ignored local metadata too:

- `analysis-manifest.json`
- `VERIFY_REPORT.md`

## Optional Diagram Rendering

Render Mermaid sources only when a handoff needs SVG files:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/mini-swe-agent/diagrams"
```

Rendered files belong under `diagrams/rendered/` and are ignored by default.
