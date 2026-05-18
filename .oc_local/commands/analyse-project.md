---
description: Deep-analyze a local or remote third-party project with Graphify and Repomix
agent: build
---

Analyze a local directory or GitHub repository URL into a complete reproducible
third-party project documentation workspace.

User request:

```text
$ARGUMENTS
```

Expected syntax:

```text
/analyse-project <github-repo-url-or-directory>
/analyse-project <github-repo-url-or-directory> --project <repo-name>
```

## Purpose

Use this command before asking project-specific follow-up questions with
`/ask-project`. This is the only command that creates or refreshes third-party
analysis artifacts. It keeps durable analysis notes under:

```text
docs/third-party/<repo-name>/
```

The command owns the complete project analysis workspace: source checkout,
Repomix packed output, Graphify knowledge graph, reproducibility manifest,
verification report, durable handoff docs, and optional feature/user/developer
docs and diagrams.

Graph generation belongs to the shared Graphify skill at
`@.opencode/skills/graphify/SKILL.md`. Repomix packaging belongs to this command
and should write `docs/third-party/<repo-name>/repomix-output.xml`.

Do not maintain a separate local analysis shell script, do not reimplement
Graphify internals, and do not send users to another local command or skill for a
normal deep project analysis.

For the shared artifact contract and escalation path, apply
`.oc_local/rules/third-party-analysis-workflow.md`.

## Workflow

1. Parse the input path or GitHub URL from `$ARGUMENTS`.
2. Derive `<repo-name>` from `--project` or the repository/directory name.
3. Ensure the stable source checkout is available under
   `docs/third-party/<repo-name>/source`:
   - If the path is already registered as a submodule but is not checked out,
     run `git submodule update --init --recursive -- docs/third-party/<repo-name>/source`.
   - If the input is a GitHub URL and no source checkout exists yet, add or
     update `docs/third-party/<repo-name>/source` as a git submodule on the
     requested branch. Ask before changing an existing source checkout.
   - If the source checkout still cannot be read after initialization, stop and
     report the missing source instead of running Graphify.
4. Create the analysis workspace directories and files as needed:

```text
docs/third-party/<repo-name>/
├── README.md
├── ANALYSIS_REPORT.md
├── REGENERATE.md
├── VERIFY_REPORT.md
├── analysis-manifest.json
├── repomix-output.xml
├── graphify-out/
├── features/
├── user/
├── developer/
└── diagrams/
    ├── source/
    └── rendered/
```

5. Run Repomix against the source checkout and write:

```text
docs/third-party/<repo-name>/repomix-output.xml
```

   Use conservative excludes for generated, dependency-heavy, build, vendored,
   secret-like, and local-environment files. Verify the packed output exists,
   contains file boundaries, and does not intentionally include obvious secrets.
6. Run Graphify by loading the shared `graphify` skill from
   `@.opencode/skills/graphify/SKILL.md` from the project analysis directory,
   using the selected source checkout or a temporary filtered corpus as input. Do
   not run the Graphify CLI directly from this command except as instructed by the
   loaded skill.
7. For third-party codebase analysis, use a temporary filtered corpus containing
   only source-code and documentation files unless the user explicitly requests a
   broader corpus.
8. Keep Graphify outputs under `docs/third-party/<repo-name>/graphify-out/`.
   This directory is ignored but intentionally useful as a local cache for
   `/ask-project`; do not delete it just because it is not commit-worthy. Never
   leave generated `graphify-out/` files inside
   `docs/third-party/<repo-name>/source`.
9. Read `GRAPH_REPORT.md`, `graph.json`, optional Graphify companion reports when
   present, and source files before writing feature, user, developer, or diagram
   documentation. Do not assume `COMMUNITIES_BY_FILE.md` exists.
10. Write or update the durable handoff docs:
    - `README.md` with source, artifact status, and next questions.
    - `ANALYSIS_REPORT.md` with architecture, feature, dependency, test,
      runtime-evidence, and risk findings.
    - `REGENERATE.md` with exact regeneration steps.
    - `VERIFY_REPORT.md` with generated artifact checks, skipped phases, failures,
      assumptions, and runtime-evidence status.
    - `analysis-manifest.json` with input, source commit, timestamp, options,
      Repomix command or parameters, Graphify invocation, generated files, skipped
      phases, and warnings.
11. Create feature/user/developer docs and Mermaid diagrams when the analysis has
    enough evidence. Mark missing runtime evidence explicitly instead of inventing
    behavior.
12. Render Mermaid sources to SVG only when needed for handoff:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/<repo-name>/diagrams"
```

13. Report the project name, source path or URL, output directory, generated docs,
    ignored reproducible artifacts, failed tool phases, verification status, and
    the next useful `/ask-project` question.

## Cooperation With Ask Project

- `/analyse-project` creates and refreshes all analysis artifacts.
- `/ask-project <project-name> "..."` consumes those artifacts for questions,
  focused docs, diagrams, source citations, and follow-up analysis.
- If `/ask-project` finds missing or stale artifacts, it should tell the user to
  rerun `/analyse-project <source-path-or-url> --project <repo-name>`.
- Do not use separate local project-analysis commands or skills; deep-analysis
  responsibilities are folded into this command and `/ask-project`.

## Git Policy

Commit only durable documentation, source submodule gitlinks, and editable
diagram sources by default:

```text
docs/third-party/<repo-name>/README.md
docs/third-party/<repo-name>/ANALYSIS_REPORT.md
docs/third-party/<repo-name>/REGENERATE.md
docs/third-party/<repo-name>/features/*.md
docs/third-party/<repo-name>/user/*.md
docs/third-party/<repo-name>/developer/*.md
docs/third-party/<repo-name>/diagrams/source/*.mmd
docs/third-party/<repo-name>/source
```

Do not commit regenerable heavy outputs unless the user explicitly asks for a
handoff artifact:

```text
docs/third-party/<repo-name>/graphify-out/
docs/third-party/<repo-name>/diagrams/rendered/
docs/third-party/<repo-name>/analysis-manifest.json
docs/third-party/<repo-name>/VERIFY_REPORT.md
docs/third-party/<repo-name>/repomix-output.*
```

## Rules

- Do not treat migration as the default conclusion.
- Do not commit generated heavy artifacts unless explicitly requested.
- Do not use `.oc_local/ai-scripts/analyse-project.sh`; that orchestration layer
  was removed intentionally.
- Do not call Graphify by duplicating its workflow here; invoke the shared
  `graphify` skill and update that skill if graph behavior needs to change.
- Keep `graphify-out/` ignored but locally available when present, because
  `/ask-project` reads it for graph-backed answers. Regenerate it through this
  command when it is missing or incomplete.
- Keep generated Graphify files out of the `source` submodule checkout. If a run
  accidentally writes `source/graphify-out/`, remove that generated directory
  before handing off.
- Limit default Graphify input to source-code and documentation files for
  third-party repository analysis; exclude image, video, binary, dependency, and
  build artifacts unless the user explicitly asks for a broader corpus.
- Create or refresh `repomix-output.xml` as part of the normal deep analysis.
- Do not answer migration or runtime behavior questions without source, docs,
  tests, graph, packed-source, or runtime evidence.
- Keep all durable docs in English.
- Mark missing runtime evidence explicitly.
- Use `/ask-project <repo-name> "..."` for follow-up questions after this
  command has created the project workspace.
