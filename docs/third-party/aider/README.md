# Aider Third-Party Analysis

This workspace documents the third-party project at
`https://github.com/Aider-AI/aider` for follow-up analysis with `/ask-project`.

## Source

- Project: `aider`
- Upstream: `https://github.com/Aider-AI/aider`
- Local checkout: `docs/third-party/aider/source`
- Checkout type: git submodule
- Branch: `main`
- Revision: `5dc9490bb35f9729ef2c95d00a19ccd30c26339c`

## What Aider Is

Aider is a Python terminal AI pair-programming tool published as the
`aider-chat` package. The main `aider` console script starts a repository-aware
interactive coding assistant that can chat with LLMs, edit files, build repo
maps, run shell commands, lint/test changes, and create git commits.

The repository also contains a Streamlit browser UI, website docs, benchmark
materials, provider/model metadata, scripts, and a broad test suite.

## Generated Documentation

- `ANALYSIS_REPORT.md` - architecture, feature, dependency, test, runtime-evidence,
  and risk findings.
- `features/README.md` - feature map for the analyzed source.
- `user/README.md` - user-facing workflows and surfaces.
- `developer/README.md` - source, runtime, and test notes for follow-up analysis.
- `developer/prompt-flow.md` - focused static map of prompt handling, model calls,
  edit application, git/lint/test loops, shell feedback, and reflection.
- `diagrams/source/runtime-flow.mmd` - editable Mermaid runtime-flow diagram.
- `REGENERATE.md` - commands and constraints for rebuilding analysis artifacts.

## Reproducible Artifacts

These files are generated and intentionally ignored:

- `repomix-output.xml`
- `graphify-out/`
- `graphify-corpus*/`
- `analysis-manifest.json`
- `VERIFY_REPORT.md`
- `diagrams/rendered/`

`graphify-out/` remains locally useful even though it is ignored. `/ask-project`
can read `GRAPH_REPORT.md`, `graph.json`, and `graph.html` from that directory
for graph-backed answers. If it is missing or incomplete, rerun:

```text
/analyse-project https://github.com/Aider-AI/aider --project aider
```

## Runtime Evidence

No Aider CLI session, Streamlit browser UI, provider API call, git edit session,
shell command run through Aider, or upstream test suite was executed during this
pass. Treat behavioral claims as static-analysis findings until verified against
a running Aider checkout.

## Recommended Next Question

Use:

```text
/ask-project aider "How does a prompt flow from CLI startup through Coder, message formatting, LiteLLM, edit parsing, git commits, lint/test, shell commands, and reflection?"
```
