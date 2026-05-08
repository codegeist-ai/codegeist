---
description: Analyze a local or remote third-party project with Graphify
agent: build
---

Analyze a local directory or GitHub repository URL into a reproducible
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
`/ask-project`. The command keeps durable analysis notes under:

```text
docs/third-party/<repo-name>/
```

Graph generation belongs to the shared Graphify skill at
`@.opencode/skills/graphify/SKILL.md`. Do not maintain a separate local
analysis shell script and do not reimplement Graphify internals in this command.

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
4. Create only lightweight durable directories and files as needed:

```text
docs/third-party/<repo-name>/
├── README.md
├── ANALYSIS_REPORT.md
├── REGENERATE.md
├── features/
├── user/
├── developer/
└── diagrams/source/
```

5. When graph generation is needed, load and execute the shared `graphify` skill
   from `@.opencode/skills/graphify/SKILL.md` against the selected source or a
   temporary filtered corpus. Do not run the Graphify CLI directly from this
   command except as instructed by the loaded skill.
6. For third-party codebase analysis, use a temporary filtered corpus containing
   only source-code and documentation files unless the user explicitly requests
   a broader corpus.
7. Keep Graphify outputs under `docs/third-party/<repo-name>/graphify-out/`.
   This directory is ignored but intentionally useful as a local cache for
   `/ask-project`; do not delete it just because it is not commit-worthy.
8. Read `GRAPH_REPORT.md`, `COMMUNITIES_BY_FILE.md`, `graph.json`, and source
   files before writing feature, user, developer, or diagram documentation.
9. Render Mermaid sources to SVG only when needed for handoff:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/<repo-name>/diagrams"
```

10. Report the project name, source path or URL, output directory, generated docs,
    ignored reproducible artifacts, failed tool phases, and the next useful
    `/ask-project` question.

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
- Limit default Graphify input to source-code and documentation files for
  third-party repository analysis; exclude image, video, binary, dependency, and
  build artifacts unless the user explicitly asks for a broader corpus.
- Keep all durable docs in English.
- Mark missing runtime evidence explicitly.
- Use `/ask-project <repo-name> "..."` for follow-up questions after this
  command has created the project workspace.
