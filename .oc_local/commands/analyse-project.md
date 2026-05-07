---
description: Analyze a local or remote third-party project into docs/third-party
agent: build
---

Analyze a local directory or GitHub repository URL and generate a reproducible
third-party project documentation workspace.

User request:

```text
$ARGUMENTS
```

Expected syntax:

```text
/analyse-project <github-repo-url-or-directory>
/analyse-project <github-repo-url-or-directory> --project <repo-name>
/analyse-project <github-repo-url-or-directory> --skip-graphify
/analyse-project <github-repo-url-or-directory> --skip-repomix
```

## Purpose

Use this command before asking project-specific follow-up questions with
`/ask-project`. The command prepares stable analysis material under:

```text
docs/third-party/<repo-name>/
```

The generated directory is split into two classes of files:

- version-worthy documentation and Mermaid sources that should be reviewed and
  can be committed
- reproducible heavy tool outputs that should stay ignored and can be regenerated

## Workflow

1. Parse the input path or GitHub URL from `$ARGUMENTS`.
2. Run the orchestration script:

```bash
bash ".oc_local/ai-scripts/analyse-project.sh" <github-repo-url-or-directory>
```

3. Pass supported options through environment variables when requested:

```bash
ARG_PROJECT_NAME=<repo-name>
ARG_SKIP_GRAPHIFY=1
ARG_SKIP_REPOMIX=1
bash ".oc_local/ai-scripts/analyse-project.sh" <github-repo-url-or-directory>
```

4. Read the generated `docs/third-party/<repo-name>/README.md` and
   `ANALYSIS_REPORT.md`.
5. If Repomix or Graphify was skipped or failed, record that clearly in the final
   response instead of presenting the analysis as complete.
6. Use `graphify-out/GRAPH_REPORT.md`, `graphify-out/graph.json`, and
   `repomix-output.xml` as source material for the deeper manual analysis phase.
7. Create or refine user docs, developer docs, feature docs, and Mermaid diagram
   sources under the same `docs/third-party/<repo-name>/` directory.
8. Render Mermaid sources to SVG only when needed for handoff:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/<repo-name>/diagrams"
```

9. Report:
   - project name
   - source path or URL
   - output directory
   - generated docs
   - ignored reproducible artifacts
   - failed or skipped tool phases
   - recommended next `/ask-project` question

## Output Layout

The script creates this structure:

```text
docs/third-party/<repo-name>/
├── README.md
├── ANALYSIS_REPORT.md
├── REGENERATE.md
├── features/
│   └── README.md
├── user/
│   └── README.md
├── developer/
│   └── README.md
├── diagrams/
│   ├── source/
│   └── rendered/        # ignored, regenerated from source/*.mmd
├── analysis-manifest.json  # ignored, regenerated
├── VERIFY_REPORT.md        # ignored, regenerated
├── repomix-output.xml      # ignored, regenerated
└── graphify-out/           # ignored, regenerated
```

## Git Policy

Commit only durable documentation and editable diagram sources by default:

```text
docs/third-party/<repo-name>/README.md
docs/third-party/<repo-name>/ANALYSIS_REPORT.md
docs/third-party/<repo-name>/REGENERATE.md
docs/third-party/<repo-name>/features/*.md
docs/third-party/<repo-name>/user/*.md
docs/third-party/<repo-name>/developer/*.md
docs/third-party/<repo-name>/diagrams/source/*.mmd
```

Do not commit regenerable heavy outputs unless the user explicitly asks for a
handoff artifact:

```text
docs/third-party/<repo-name>/repomix-output.*
docs/third-party/<repo-name>/graphify-out/
docs/third-party/<repo-name>/diagrams/rendered/
docs/third-party/<repo-name>/analysis-manifest.json
docs/third-party/<repo-name>/VERIFY_REPORT.md
```

## Interaction

After the initial analysis, ask one useful follow-up such as:

```text
Should I deepen the most important feature cluster next?
Should I generate workflow and sequence diagrams for the main runtime path?
Should I turn the initial notes into complete user and developer docs?
```

## Rules

- Do not treat migration as the default conclusion.
- Do not commit generated heavy artifacts unless explicitly requested.
- Keep all durable docs in English.
- Mark missing runtime evidence explicitly.
- Use `/ask-project <repo-name> "..."` for follow-up questions after this
  command has created the project workspace.
