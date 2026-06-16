# mini-SWE-agent Third-Party Analysis

This workspace documents the third-party project at
`https://github.com/SWE-agent/mini-swe-agent` for follow-up analysis with
`/ask-project`.

## Source

- Project: `mini-swe-agent`
- Upstream: `https://github.com/SWE-agent/mini-swe-agent.git`
- Local checkout: `docs/third-party/mini-swe-agent/source`
- Checkout type: git submodule
- Branch: `main`
- Revision: `2caffc565474b8856a323ff163ffb7ab98d1ef02`

## What mini-SWE-agent Is

mini-SWE-agent is a Python coding agent package and CLI that keeps the agent
scaffold intentionally small. Its default loop combines one model, one execution
environment, and one agent class. The core agent appends messages linearly,
queries a model, executes parsed bash actions, appends observations, and stops
when an environment raises the `Submitted` control-flow exception.

The repository includes a local interactive `mini` CLI, batch and single-instance
SWE-bench runners, a trajectory inspector, YAML configuration, provider/model
adapters, environment backends, and a pytest-based test suite.

## Generated Documentation

- `ANALYSIS_REPORT.md` - architecture, feature, dependency, test,
  runtime-evidence, and risk findings.
- `features/README.md` - capability map for the analyzed source.
- `user/README.md` - user-facing commands and workflows.
- `developer/README.md` - implementation map and follow-up notes.
- `developer/runtime-flow.md` - focused static source-code flow for prompt,
  action, environment, and trajectory handling.
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
/analyse-project https://github.com/SWE-agent/mini-swe-agent --project mini-swe-agent
```

## Runtime Evidence

No mini-SWE-agent CLI session, model/provider call, sandboxed command execution,
SWE-bench run, trajectory inspector session, or upstream test suite was executed
during this pass. Treat behavioral claims as static-analysis findings until they
are verified against a running checkout.

## Recommended Next Question

Use:

```text
/ask-project mini-swe-agent "For Codegeist T007_03, analyze mini-SWE-agent's minimal model-plus-environment loop, linear message history, bash-only execution, and trajectory output. What does it imply for keeping Codegeist's first harness narrow?"
```
