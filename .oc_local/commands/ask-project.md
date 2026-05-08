---
description: Ask questions about an analyzed project and generate follow-up documentation or diagrams
agent: build
---

Use this command to query a previously analyzed project and optionally generate
follow-up documentation or diagrams from its analysis artifacts.

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

3. If no analysis exists, stop and ask whether to run `/analyse-project` first.
4. Treat `analysis-manifest.json` as optional because it is ignored and
   regenerable. If it is missing, derive the source checkout from
   `docs/third-party/<project-name>/source`.
5. Check graph artifacts:
   - Prefer existing `graphify-out/GRAPH_REPORT.md`,
     `graphify-out/COMMUNITIES_BY_FILE.md`, and `graphify-out/graph.json` when
     they are present.
   - If `graphify-out/GRAPH_REPORT.md` or `graphify-out/graph.json` is missing
     and the request needs graph, cluster, route, architecture, or cross-file
     evidence, run or delegate to `/analyse-project <source-path-or-url>
     --project <project-name>` before answering.
   - Do not invoke Graphify directly from `/ask-project`; `/analyse-project`
     owns Graphify generation and must use `@.opencode/skills/graphify/SKILL.md`.
   - Keep `graphify-out/` ignored but local when present; it is the graph cache
     used by `/ask-project`, not a durable documentation file.
6. Read the available artifacts before answering:
   - `analysis-manifest.json` when present
   - `ANALYSIS_REPORT.md`
   - `graphify-out/GRAPH_REPORT.md`
   - `graphify-out/COMMUNITIES_BY_FILE.md`
   - `graphify-out/graph.json`
   - `features/*.md`
   - `developer/*.md`
   - `user/*.md`
   - `repomix-output.xml` only when needed for source-level details and present
7. For source-level implementation questions that require broad code context,
   prefer delegating to the `@repomix` subagent or to
   `/ask-project-repomix <project-name> "<question>"` when
   `repomix-output.xml` exists. This keeps the large packed-output context in a
   child agent session instead of the parent `/ask-project` context.
8. Determine the request type:
   - explanation
   - feature deep dive
   - workflow diagram
   - sequence diagram
   - state diagram
   - user documentation
   - developer documentation
   - migration assessment
9. If the request is ambiguous, ask one concise clarification question. Example:

```text
I found several parser candidates: CLI argument parsing, AI response parsing,
and document parsing. Which one should the diagram cover?
```

10. Use source, graph, docs, tests, and runtime evidence as the basis for the
   answer. Mark assumptions explicitly.
11. If creating Mermaid:
   - write `.mmd` under `docs/third-party/<project-name>/diagrams/source/`
   - render SVG under `docs/third-party/<project-name>/diagrams/rendered/`
   - use `.oc_local/ai-scripts/render-mermaid.sh` when available
   - reference the rendered SVG from any generated markdown when SVG handoff is needed
12. Verify generated files and report rendering failures.
13. Return a concise answer with paths to created or updated artifacts.
14. Ask whether the user wants a targeted follow-up.

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
  the `@repomix` subagent or `/ask-project-repomix` for broad source-level
  questions so the packed-output context stays isolated.
- Prefer focused feature or cluster diagrams over huge system diagrams.
- Keep Mermaid sources editable and render SVGs when `mmdc` is available.
- If rendering fails, keep the `.mmd` source and report the exact failure.
- Do not include secrets or local environment files in generated artifacts.
- Keep generated durable documentation in English, even when the user's prompt is
  in another language.
