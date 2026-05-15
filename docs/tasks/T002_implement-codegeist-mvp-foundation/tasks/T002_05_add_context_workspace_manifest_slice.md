# T002_05 Add Context Workspace Manifest Slice

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_11`, `T001_14`, `T001_22`, `T001_23`

## Goal

Implement a deterministic context-loading slice that reads safe repo-local inputs
through a workspace boundary and produces an explainable context manifest.

## Context

The architecture treats repo rules, local overlays, memory, task docs, developer
docs, source snippets, and third-party analysis artifacts as first-class context
sources. Large generated artifacts remain on-demand and workspace policy owns path
safety.

## Concrete Solution

1. Add a workspace service contract for root identity, canonical path validation,
   generated/ignored posture, and read eligibility.
2. Add a context loader that can include selected rules, memory, active task docs,
   developer docs, and explicitly requested source snippets.
3. Produce a context manifest listing included sources, skipped sources, reasons,
   and ordering.
4. Keep Graphify/Repomix outputs on-demand by path, not automatically loaded.
5. Add fixture-based tests for deterministic ordering, skipped generated paths,
   missing optional docs, and manifest content.

## Scope

- `ai.codegeist.context`
- `ai.codegeist.workspace`
- test fixtures under `app/codegeist/cli/src/test/resources/` when needed
- focused context/workspace tests

## Acceptance Criteria

- Context loading is deterministic and explainable through a manifest.
- Workspace validation is centralized and used before file reads.
- Rules, memory, tasks, and docs can be selected without loading heavy generated
  artifacts by default.
- Tests cover ordering and skip reasons.
- No provider calls, embeddings, RAG, Graphify, Repomix, or tool execution occurs.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_02`.
- Can proceed before provider integration.

## Non-Goals

- Do not implement embeddings, indexing, LSP, graph generation, or token-budget
  optimization.
- Do not mutate workspace state.

## Open Questions

- Which initial command or runtime request should pass the active task path to the
  context loader?

## Dependency Impact Notes

- Finalized `T002_04_wire_cli_prompt_mode_contract.md` keeps CLI prompt-mode input
  limited to prompt text, explicit Plan/Build mode, optional session id, source,
  and request/correlation metadata. It intentionally does not pass active task
  paths, select context sources, or own workspace policy, so this task still owns
  the first context/workspace manifest decision.

## Specification Check Result

- Rechecked with the T002 parent default hints.
- The task correctly groups context loading with workspace validation because
  context must not bypass generated, ignored, or secret-like file policy.
- OpenCode source lookup is optional and should be focused on project/context
  loading behavior only if Codegeist context-source ordering remains unclear.

## Creation Note

Status: open.

Derived by grouping workspace/file-access and context-loading architecture into
one independently testable implementation slice.
