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
- No semantic chunk-agent extraction was added to the durable docs in this pass;
  use the generated graph as navigation evidence, not as a complete behavioral
  audit.

## Durable Docs

- `ANALYSIS_REPORT.md` - high-level project findings and Codegeist relevance.
- `REGENERATE.md` - how to refresh this workspace.

## Useful Follow-Up Questions

Use these after the graph cache exists:

```text
/ask-project spring-ai-agent-utils "Which utilities have side effects that must be mediated by Codegeist permissions and workspace policy? Cite source files."
/ask-project spring-ai-agent-utils "How does Spring AI Agent Utils expose ToolCallback or @Tool-based tools, and where would Codegeist need a wrapper boundary?"
/ask-project spring-ai-agent-utils "Which test patterns from Spring AI Agent Utils are worth reusing for Codegeist wrapper-boundary tests?"
```
