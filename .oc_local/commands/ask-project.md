---
description: Ask questions about an analyzed project and generate follow-up documentation or diagrams
agent: build
---

Use this command to query a previously analyzed project and optionally generate
follow-up documentation or diagrams from its analysis artifacts.

This is the normal question and documentation interface for a project workspace
created by `/analyse-project`. For the shared artifact contract, apply
`.oc_local/rules/third-party-analysis-workflow.md`.

User request:

```text
$ARGUMENTS
```

Expected syntax:

```text
/ask-project <project-name> "<question or documentation request>"
```

Examples:

```text
/ask-project codegeist "erstelle mir ein workflow diagramm von dem ai parser"
/ask-project legacy-shop "explain checkout runtime behavior"
/ask-project api-gateway "create a sequence diagram for token refresh"
```

## Workflow

1. Parse `<project-name>` and the quoted request from `$ARGUMENTS`.
2. Locate the project directory under:

```text
docs/third-party/<project-name>/
```

3. If no analysis exists, stop and tell the user to run `/analyse-project` first.
4. Check required analysis artifacts:
   - `source/`
   - `ANALYSIS_REPORT.md`
   - `repomix-output.xml`
   - `graphify-out/GRAPH_REPORT.md`
   - `graphify-out/graph.json`
5. If a required artifact is missing or clearly stale, stop and recommend rerunning
   `/analyse-project <source-path-or-url> --project <project-name>`. Do not create
   or refresh analysis artifacts from `/ask-project`.
6. Check graph artifacts:
    - Prefer existing `graphify-out/GRAPH_REPORT.md`,
      `graphify-out/graph.json`, and `graphify-out/graph.html` when they are
      present. Treat companion files such as `COMMUNITIES_BY_FILE.md` as optional
      because the shared Graphify skill does not guarantee them.
   - Do not invoke Graphify directly from `/ask-project`; `/analyse-project`
     owns Graphify generation and must use `@.opencode/skills/graphify/SKILL.md`.
   - Keep `graphify-out/` ignored but local when present; it is the graph cache
     used by `/ask-project`, not a durable documentation file.
7. Read the available artifacts before answering:
   - `analysis-manifest.json` when present
   - `ANALYSIS_REPORT.md`
   - `graphify-out/GRAPH_REPORT.md`
   - `graphify-out/graph.json`
   - `graphify-out/graph.html` as a handoff artifact when relevant
   - `repomix-output.xml` through the `@repomix` subagent for broad source-level
     implementation questions
   - `features/*.md`
   - `developer/*.md`
   - `user/*.md`
8. For source-level implementation questions that require broad code context,
   delegate to the `@repomix` subagent from this command. Include:
   - `project=<project-name>`
   - `repomix_path=docs/third-party/<project-name>/repomix-output.xml`
   - `question=<question>`
   The subagent must attach the packed output, search/read only relevant sections,
   and answer with source-path citations. Do not paste raw XML into the parent
   context.
9. Determine the request type:
   - explanation
   - feature deep dive
   - workflow diagram
   - sequence diagram
   - state diagram
   - user documentation
   - developer documentation
   - migration assessment
10. If the request is ambiguous, ask one concise clarification question. Example:

```text
I found several parser candidates: CLI argument parsing, AI response parsing,
and document parsing. Which one should the diagram cover?
```

11. Use source, graph, Repomix, docs, tests, and runtime evidence as the basis for the
    answer. Mark assumptions explicitly.
12. If creating Mermaid:
   - write `.mmd` under `docs/third-party/<project-name>/diagrams/source/`
   - render SVG under `docs/third-party/<project-name>/diagrams/rendered/`
   - use `.oc_local/ai-scripts/render-mermaid.sh` when available
   - reference the rendered SVG from any generated markdown when SVG handoff is needed
13. Verify generated files and report rendering failures.
14. Return a concise answer with paths to created or updated artifacts.
15. Ask whether the user wants a targeted follow-up.

## Diagram Output Rules

Use stable, feature-oriented file names:

```text
diagrams/source/feature-<slug>-flow.mmd
diagrams/source/feature-<slug>-sequence.mmd
diagrams/source/feature-<slug>-state.mmd
diagrams/rendered/feature-<slug>-flow.svg
diagrams/rendered/feature-<slug>-sequence.svg
diagrams/rendered/feature-<slug>-state.svg
```

Prefer:

- `flowchart TD` for workflow diagrams
- `sequenceDiagram` for cross-component interactions
- `stateDiagram-v2` for lifecycle or status transitions
- `classDiagram` for domain type relationships
- `erDiagram` for persistent entities

Do not overwrite an existing diagram unless the user explicitly asked to update
it. If a file exists, create a suffixed draft such as
`feature-<slug>-flow-draft.mmd` and ask whether to replace the original.

## Interactive Follow-Up

After answering, offer one or two relevant next steps, for example:

```text
Should I also create a sequence diagram for this feature?
Should I add source-file references to the diagram explanation?
Should I expand this into developer documentation?
Should I validate this flow against tests or runtime logs?
```

Keep the follow-up specific to the user's request.

## Rules

- Do not invent behavior unsupported by source, graph, docs, tests, or runtime
  evidence.
- Do not use Graphify communities as final feature boundaries without checking
  code and documentation.
- Do not invoke the Graphify skill directly from `/ask-project`. If graph output
  is missing or incomplete, regenerate it via `/analyse-project` so Graphify
  ownership stays centralized.
- Do not load large `repomix-output.xml` content into the parent context. Use
  the `@repomix` subagent from this command for broad source-level questions so
  the packed-output context stays isolated.
- Do not route users to separate local project-analysis commands or skills; rerun
  `/analyse-project` when the analysis workspace is missing or stale.
- Prefer focused feature or cluster diagrams over huge system diagrams.
- Keep Mermaid sources editable and render SVGs when `mmdc` is available.
- If rendering fails, keep the `.mmd` source and report the exact failure.
- Do not include secrets or local environment files in generated artifacts.
- Keep generated durable documentation in English, even when the user's prompt is
  in another language.
