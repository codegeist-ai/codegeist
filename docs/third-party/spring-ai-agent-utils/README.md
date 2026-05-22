# Spring AI Agent Utils Analysis Workspace

Reusable third-party analysis workspace for
`spring-ai-community/spring-ai-agent-utils`.

## Source

- Repository: `https://github.com/spring-ai-community/spring-ai-agent-utils`
- Local checkout: `docs/third-party/spring-ai-agent-utils/source`
- Checkout type: git submodule
- Primary upstream language: Java
- Upstream license: Apache License 2.0

## Why This Exists

Codegeist task `T003_01_analyze_spring_ai_agent_utils_adoption.md` needs evidence
about whether Spring AI Agent Utils should be used directly, wrapped behind
Codegeist contracts, copied conceptually, deferred, or rejected.

This workspace keeps reusable third-party context outside the task file so later
`/ask-project` follow-up questions can cite the same source checkout, graph cache,
and Repomix packed output after `/analyse-project` is rerun with the current full
analysis workflow.

## Current Analysis State

- `graphify-out/graph.json`, `graphify-out/graph.html`, and
  `graphify-out/GRAPH_REPORT.md` were generated locally and are intentionally
  ignored by git.
- The current Graphify run is structural/AST-focused. It found 1,030 graph nodes,
  1,604 edges, and 47 communities from 156 supported files.
- `repomix-output.xml` was regenerated locally from the source checkout with
  generated build output, binaries, dependency directories, and image files
  excluded. The packed output contains 222 files and is intentionally ignored by
  git.
- `analysis-manifest.json` and `VERIFY_REPORT.md` record the local regeneration
  inputs, skipped phases, and verification status. They are ignored because they
  are reproducible run artifacts.
- No semantic chunk-agent extraction was added to the durable docs in this pass;
  use the generated graph and packed source as navigation evidence, not as a
  complete behavioral audit.

## Durable Docs

- `ANALYSIS_REPORT.md` - high-level project findings and Codegeist relevance.
- `REGENERATE.md` - how to refresh this workspace.
- `graphify-out/GRAPH_REPORT.md` - ignored graph audit report for local follow-up
  questions.
- `repomix-output.xml` - ignored packed-source artifact for broad source-level
  `/ask-project` deep dives.

## Useful Follow-Up Questions

Use these after the graph cache exists:

```text
/ask-project spring-ai-agent-utils "Which utilities have side effects that must be mediated by Codegeist permissions and workspace policy? Cite source files."
/ask-project spring-ai-agent-utils "How does Spring AI Agent Utils expose ToolCallback or @Tool-based tools, and where would Codegeist need a wrapper boundary?"
/ask-project spring-ai-agent-utils "Which test patterns from Spring AI Agent Utils are worth reusing for Codegeist wrapper-boundary tests?"
```
