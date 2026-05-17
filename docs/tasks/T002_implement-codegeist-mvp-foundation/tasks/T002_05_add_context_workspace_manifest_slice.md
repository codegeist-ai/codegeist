# T002_05 Describe Context Workspace Manifest Slice

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_11`, `T001_14`, `T001_22`, `T001_23`

Status: solved

## Goal

Describe a deterministic context-loading contract that reads safe repo-local
inputs through a workspace boundary and produces an explainable context manifest,
without adding Java source yet.

## Context

The architecture treats repo rules, local overlays, memory, task docs, developer
docs, source snippets, and third-party analysis artifacts as first-class context
sources. Large generated artifacts remain on-demand and workspace policy owns path
safety.

## Concrete Solution

1. Create or update `docs/developer/context-workspace-manifest.md` as the future
   context/workspace contract blueprint.
2. Define the workspace service responsibilities for root identity, canonical path
   validation, generated/ignored posture, secret-like path posture, symlink escape
   handling, missing paths, and read eligibility.
3. Define the context loader request shape for selected rules, memory, active task
   docs, developer docs, third-party artifacts by explicit path, and source
   snippets by explicit path.
4. Define the context manifest fields for included sources, skipped sources,
   reasons, ordering, summaries, sizes, redaction status, and warnings.
5. Document deterministic source ordering and why Graphify/Repomix outputs remain
   on-demand references rather than automatic context.
6. Include future file maps, class/sequence diagrams, and illustrative Java
   snippets in markdown only.

## Scope

- `docs/developer/context-workspace-manifest.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Context loading is specified as deterministic and explainable through a
  manifest.
- Workspace validation responsibilities are centralized and described as a
  prerequisite before future file reads.
- Rules, memory, tasks, developer docs, third-party artifacts, and source snippets
  have explicit selection posture and ordering.
- Skip reasons cover generated, ignored, heavy, missing optional, outside-root,
  symlink escape, secret-like, and unsupported source cases.
- Future implementation tests are described, but no Java source, test fixtures, or
  package directories are created by this task.
- No provider calls, embeddings, RAG, Graphify, Repomix, or tool execution occurs.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_02`.
- Can proceed before provider integration.

## Non-Goals

- Do not create Java source files, empty package directories, test fixtures, or
  contract tests.
- Do not implement embeddings, indexing, LSP, graph generation, or token-budget
  optimization.
- Do not mutate workspace state.

## Open Questions

- Which initial command or runtime request should pass the active task path to the
   context loader?

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later implementation task instead of creating `ai.codegeist.context`
  or `ai.codegeist.workspace` source packages now.

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

Derived by grouping workspace/file-access and context-loading architecture into
one independently reviewable documentation/specification slice.

## Solution Note

Solved as a documentation-only handoff in
`docs/developer/context-workspace-manifest.md`. The developer document now defines
the future workspace path-validation responsibilities, explicit context loader
request shape, deterministic source ordering, manifest fields, skip reasons,
Graphify/Repomix on-demand posture, future Java file map, illustrative Java
snippets, and future test checklist without adding Java source, tests, fixtures,
package directories, provider behavior, embeddings, tool execution, Graphify, or
Repomix runs.
