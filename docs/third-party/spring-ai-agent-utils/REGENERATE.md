# Regenerate Spring AI Agent Utils Analysis

How to refresh the local third-party analysis workspace.

## Source Checkout

The source repository is tracked as a git submodule:

```bash
git submodule update --init --recursive -- docs/third-party/spring-ai-agent-utils/source
```

To update it to the remote default branch, use a deliberate submodule update and
review the parent gitlink diff before committing.

## Full Analysis Cache

Regenerate Graphify, Repomix, manifest, verification, and durable handoff docs
through the local command workflow, not by inventing a separate script:

```text
/analyse-project https://github.com/spring-ai-community/spring-ai-agent-utils --project spring-ai-agent-utils
```

Expected ignored outputs:

```text
docs/third-party/spring-ai-agent-utils/graphify-out/
docs/third-party/spring-ai-agent-utils/analysis-manifest.json
docs/third-party/spring-ai-agent-utils/VERIFY_REPORT.md
docs/third-party/spring-ai-agent-utils/repomix-output.*
```

Current useful local analysis files after regeneration:

```text
docs/third-party/spring-ai-agent-utils/graphify-out/GRAPH_REPORT.md
docs/third-party/spring-ai-agent-utils/graphify-out/graph.json
docs/third-party/spring-ai-agent-utils/graphify-out/graph.html
docs/third-party/spring-ai-agent-utils/repomix-output.xml
docs/third-party/spring-ai-agent-utils/analysis-manifest.json
docs/third-party/spring-ai-agent-utils/VERIFY_REPORT.md
```

## Documentation To Keep In Git

Commit only durable handoff docs and source gitlinks by default:

```text
docs/third-party/spring-ai-agent-utils/README.md
docs/third-party/spring-ai-agent-utils/ANALYSIS_REPORT.md
docs/third-party/spring-ai-agent-utils/REGENERATE.md
docs/third-party/spring-ai-agent-utils/features/*.md
docs/third-party/spring-ai-agent-utils/user/*.md
docs/third-party/spring-ai-agent-utils/developer/*.md
docs/third-party/spring-ai-agent-utils/diagrams/source/*.mmd
docs/third-party/spring-ai-agent-utils/source
```

Do not commit `graphify-out/` unless a future handoff explicitly asks for a heavy
artifact snapshot.

## After Regeneration

- Update `ANALYSIS_REPORT.md` when upstream modules, versions, or major tool
  surfaces change.
- Keep final Codegeist adoption decisions in
  `docs/developer/spring-ai-agent-utils-adoption.md`, not in this workspace.
- Run `git --no-pager diff --check` before handoff.
