# Regenerate Aider Analysis Artifacts

This workspace separates durable documentation from reproducible heavy outputs.

## Source Checkout

The source checkout is a git submodule:

```bash
git submodule update --init --recursive -- docs/third-party/aider/source
```

To update it to the remote default branch, update the submodule deliberately and
review the parent gitlink diff before committing.

## Full Analysis Cache

Regenerate Graphify, Repomix, manifest, verification, and durable handoff docs
through the local command workflow:

```text
/analyse-project https://github.com/Aider-AI/aider --project aider
```

Do not use a separate local analysis script. Repomix belongs to the command
workflow; Graphify must run through the shared `graphify` skill.

## Repomix

The latest run used this effective command shape from the repository root:

```bash
npx -y repomix "docs/third-party/aider/source" \
  --output "docs/third-party/aider/repomix-output.xml" \
  --style xml \
  --include "**/*.{py,md,rst,txt,toml,yml,yaml,json,sh,ini,cfg,css,html,js,ts}" \
  --ignore "**/.git/**,**/__pycache__/**,**/.pytest_cache/**,**/.mypy_cache/**,**/.ruff_cache/**,**/.tox/**,**/.nox/**,**/.venv/**,**/venv/**,**/node_modules/**,**/dist/**,**/build/**,**/_build/**,**/site/**,**/htmlcov/**,**/coverage/**,**/*.egg-info/**,**/tests/fixtures/**,**/aider/website/assets/**,**/aider/website/_data/blame.yml,**/*.min.js,**/*.pyc,**/*.pyo,**/*.log,**/.env*,**/*secret*,**/*credential*,**/*key*,**/*.pem,**/*.key,**/*.lock"
```

The output is ignored at `docs/third-party/aider/repomix-output.xml` and is used
by `/ask-project` for broad source-level follow-up questions.

## Graphify

Graph generation must be run through the shared `graphify` skill, not by
duplicating its workflow in this command.

The latest run used a temporary non-ignored focused input directory under this
workspace named `graphify-input-focus`, copied from source-code and documentation
files only, then deleted after output generation. Do not use ignored
`graphify-corpus*` directories as active Graphify inputs because Graphify's file
detector can skip ignored paths.

The focused corpus included:

```text
README.md
CONTRIBUTING.md
HISTORY.md
pyproject.toml
pytest.ini
requirements.txt
requirements/**
aider/*.py
aider/**/*.py
aider/resources/model-settings.yml
aider/resources/model-metadata.json
aider/queries/**/README.md
aider/website/docs/index.md
aider/website/docs/usage.md
aider/website/docs/usage/*.md
aider/website/docs/config.md
aider/website/docs/config/*.md
aider/website/docs/git.md
aider/website/docs/repomap.md
aider/website/docs/llms.md
aider/website/docs/install.md
aider/website/docs/troubleshooting.md
aider/website/docs/faq.md
aider/website/docs/unified-diffs.md
aider/website/docs/scripting.md
aider/website/docs/languages.md
tests/basic/test_coder.py
tests/basic/test_commands.py
tests/basic/test_main.py
tests/basic/test_models.py
tests/basic/test_repo.py
tests/basic/test_repomap.py
tests/basic/test_sendchat.py
tests/basic/test_udiff.py
tests/basic/test_watch.py
```

Expected key files:

```text
docs/third-party/aider/graphify-out/GRAPH_REPORT.md
docs/third-party/aider/graphify-out/graph.json
docs/third-party/aider/graphify-out/graph.html
```

## Mermaid Rendering

Render diagrams only when SVG handoff artifacts are needed:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/aider/diagrams"
```

Rendered SVGs are ignored and should be regenerated from
`diagrams/source/*.mmd`.

## Documentation To Keep In Git

Commit only durable handoff docs, editable diagram sources, and the source
gitlink by default:

```text
docs/third-party/aider/README.md
docs/third-party/aider/ANALYSIS_REPORT.md
docs/third-party/aider/REGENERATE.md
docs/third-party/aider/features/*.md
docs/third-party/aider/user/*.md
docs/third-party/aider/developer/*.md
docs/third-party/aider/diagrams/source/*.mmd
docs/third-party/aider/source
```

Do not commit `graphify-out/`, `repomix-output.xml`, `VERIFY_REPORT.md`,
`analysis-manifest.json`, or rendered diagrams unless a future handoff explicitly
asks for reproducible generated artifacts.
