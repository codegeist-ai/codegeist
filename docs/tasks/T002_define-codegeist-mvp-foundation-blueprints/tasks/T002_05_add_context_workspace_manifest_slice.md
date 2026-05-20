# T002_05 Describe Context Workspace Manifest Slice

Parent: `T002_define-codegeist-mvp-foundation-blueprints`

Sources: `T001_11`, `T001_14`, `T001_22`, `T001_23`

Status: finalized

## Goal

Describe a deterministic context-loading contract that reads safe repo-local
inputs through a workspace boundary and repo-owned context profile, then produces
an explainable context manifest, without adding Java source yet.

## Context

The architecture treats instruction sources, profile-managed state, active work
items, repository knowledge sources, and source snippets as selectable context
sources. Codegeist core must not hard-code this repository's `docs/` layout; repo
rules and commands own profile creation and maintenance, while workspace policy
owns path safety.

## Concrete Solution

1. Create or update `docs/developer/specification/context-workspace-manifest.md` as the future
   context/workspace contract blueprint.
2. Define the workspace service responsibilities for root identity, canonical path
   validation, generated/ignored posture, secret-like path posture, symlink escape
   handling, missing paths, and read eligibility.
3. Define the context loader request shape for profile-selected instructions,
   state, active work, repository knowledge, and source snippets by explicit path.
4. Define the context manifest fields for included sources, skipped sources,
   reasons, ordering, summaries, sizes, redaction status, and warnings.
5. Document deterministic source ordering and why external analysis outputs such
   as Graphify/Repomix remain outside core context loading.
6. Include future file maps, class/sequence diagrams, and illustrative Java
   snippets in markdown only.

## Scope

- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Context loading is specified as deterministic and explainable through a
  manifest.
- Workspace validation responsibilities are centralized and described as a
  prerequisite before future file reads.
- Instructions, state, work items, knowledge sources, and source snippets have
  explicit selection posture and ordering.
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

- Which initial command or runtime request should pass the active work path to the
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
- User correction: the whole `docs/` layout is repository-specific and must not be
  hard-coded into Codegeist core. Instruction, state, work-item, and knowledge
  paths should come from repo-owned context profiles that Codegeist rules and
  commands can create or maintain.
- User correction: third-party analysis artifacts should not be a core context
  source category. They belong behind explicit repo-specific commands or tool
  workflows instead of the default context loader.

## Phase Status

- Phase: `/specify-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_05_add_context_workspace_manifest_slice.md`.
- Context or instructions considered: user requested a repeat specification pass
  by exact task path,
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_05_add_context_workspace_manifest_slice.md`,
  with no additional narrowing instructions.
- Upstream phase dependency: none; `/specify-task` is the entry phase and may be
  repeated when the source or workflow boundary needs to be current.
- Parent considered:
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`.
- Adjacent tasks considered: finalized
  `T002_04_wire_cli_prompt_mode_contract.md`, which confirms CLI prompt-mode
  wiring does not select active work paths or own context sources, and
  `T002_06_add_provider_configuration_adapter.md`, which keeps provider
  configuration separate from context loading.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Documentation considered: `docs/developer/specification/context-workspace-manifest.md`,
  `docs/developer/README.md`, and `docs/memory-bank/chat.md`.
- Result: specified. The repeat pass confirmed that the existing
  documentation-only blueprint remains the correct scope: context selection is
  explicit and profile-driven, workspace policy owns path safety before reads,
  manifests explain included and skipped sources, and external analysis artifacts
  stay outside core context loading.
- Open decisions or blockers: future implementation work still needs to choose
  which runtime request, command, or profile-managed workspace state supplies
  `activeWorkPath` to the context loader.
- Next recommended phase: run `/plan-task T002_05` only if this slice is being
  reopened for a fresh implementation plan; otherwise continue with the next
  documentation/specification slice, `T002_06`.

## Architecture Plan

This planning pass keeps `T002_05` as one documentation-only architecture task.
No child task is needed yet. The next solve pass should complete the architecture
design before any Java implementation task is created.

### Selected Option

Complete the workspace/context architecture in this task file and
`docs/developer/specification/context-workspace-manifest.md`.

This follows the user's clarification: design the complete architecture first,
then derive implementation tasks later only when explicitly requested.

### Concrete Design Direction

Deepen the context/workspace architecture around four layers:

1. Workspace identity and path policy.
2. Repo-owned context profile model.
3. Context load request and deterministic candidate ordering.
4. Context manifest, skip reasons, warnings, redaction posture, and later
   diagnostics.

OpenCode should remain source evidence, not an implementation blueprint. It
validates the need for project/worktree boundaries, ignore/protected path posture,
instruction/config context loading, and external-directory permission, while
Codegeist should improve on OpenCode by designing deterministic ordering and an
explicit context manifest.

### Planned Documentation Files

- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/README.md` only if a new architecture document is introduced
- `docs/developer/architecture/architecture.md` only if current-state wording needs to clarify
  that the architecture remains unimplemented
- this task file

No Java source, tests, Maven files, build files, package directories, Spring
beans, provider calls, tool execution, Graphify, Repomix, or runtime behavior are
planned for this task.

### Design Steps

1. Compare the existing context/workspace blueprint against the OpenCode evidence
   gathered through `/ask-project opencode`.
2. Define the workspace boundary: root/worktree identity, canonical path posture,
   outside-root denial, symlink escape handling, generated/ignored classification,
   secret-like path posture, missing optional inputs, and the boundary with later
   permission approval.
3. Define the context profile boundary: which concepts are Codegeist-generic and
   which path selections remain repo-owned profile data.
4. Define the context load request boundary: explicit selected sources only,
   active work as context/runtime state rather than CLI prompt-mode input, and no
   opportunistic repository-wide scanning in the first design.
5. Define deterministic ordering and deduplication before manifest assembly.
6. Define manifest entries, skip reasons, warnings, redaction status, size/line
   limits, and future diagnostic/event handoff.
7. Add or refine Mermaid diagrams for layer ownership, request-to-candidate flow,
   workspace validation, manifest assembly, and later implementation-slice order.
8. Record deferred implementation slices only as future candidates, not as active
   Java tasks.

### Scope

- Architecture and documentation only.
- Keep Codegeist core free of hard-coded repository paths such as this repo's
  `docs/` layout.
- Keep external analysis artifacts outside the default context source model.
- Keep future Java names or sketches illustrative, not files to create now.

### Non-Goals

- Do not create Java source files, tests, fixtures, package directories, Maven
  changes, build files, or Spring beans.
- Do not implement workspace path classification.
- Do not implement context profile loading.
- Do not implement context manifest assembly.
- Do not read file contents or mutate workspace state.
- Do not implement provider calls, embeddings, RAG, Graphify, Repomix, LSP, shell
  commands, patch/edit behavior, permission prompts, storage, UI, or tool
  execution.

### Acceptance Criteria

- The architecture document clearly separates workspace identity, path policy,
  context profile, context request, ordering, manifest, warnings, and redaction.
- The design explains why workspace policy must precede any context file reads.
- The design explains how repo-owned profiles prevent hard-coded `docs/` layout
  assumptions in Codegeist core.
- The design maps relevant OpenCode behavior to Codegeist boundaries and states
  where Codegeist intentionally improves on OpenCode with manifest-backed
  explainability.
- Deferred implementation slices are listed in a safe order, but no active Java
  implementation task is created.
- No Java source, tests, build files, runtime behavior, provider calls, tools,
  Graphify, or Repomix are added by this task.

### Verification Plan

Run from the repository root:

```bash
git --no-pager diff --check
```

This proves the documentation diff has no whitespace errors. `task test` is not
required because this task must not change Java source, tests, Maven files, or
runtime behavior.

### Dependencies

- Depends on the current `T002_05` specification status and the solved
  `docs/developer/specification/context-workspace-manifest.md` blueprint.
- Uses the finalized `T002_04` boundary that CLI prompt-mode input does not own
  active work paths, context-source selection, or workspace policy.
- Uses OpenCode source evidence gathered through the local `/ask-project opencode`
  workflow.
- Should inform later `T002_07` tool/permission/workspace contracts without
  implementing those contracts now.

### Open Questions

None for this planning pass. The solve pass should record any remaining
architecture decisions directly in the design document.

## Plan Workflow Handoff

- Phase: `/plan-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_05_add_context_workspace_manifest_slice.md`.
- Target task: this existing `T002_05` task; no child task was created because the
  user wants complete architecture design before implementation planning.
- User context considered: the user asked how OpenCode handles this area, accepted
  the workspace-policy ordering, then clarified that they do not want direct Java
  implementation yet and want the complete architecture designed first.
- Parent task considered:
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`.
- Duplicate check result: no child tasks exist under `T002_05`; no existing task or
  Java source implements `ai.codegeist.workspace`, `WorkspacePathPolicy`, or
  context manifest behavior.
- Selected option: architecture/design-only planning in `T002_05` itself.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: `docs/developer/specification/context-workspace-manifest.md`,
  `docs/developer/architecture/architecture.md`, `app/codegeist/cli/pom.xml`,
  `app/codegeist/cli/Taskfile.yml`, and `docs/memory-bank/chat.md`.
- OpenCode evidence considered: project/worktree `InstanceContext`, path
  containment checks, file read/list boundaries, external-directory permission,
  built-in ignore/protected path posture, instruction/config context loading, and
  the absence of a single context manifest.
- Upstream phase dependency: satisfied; this task had current `/specify-task`
  status and top-level `Status: specified` before this planning pass.
- Result: planned as a documentation-only architecture task.
- Open decisions or blockers: none for planning.
- Next recommended phase: run `/solve-task T002_05` as a documentation-only
  architecture design pass.

## Creation Note

Derived by grouping workspace/file-access and context-loading architecture into
one independently reviewable documentation/specification slice.

## Solution Note

Solved as a documentation-only handoff in
`docs/developer/specification/context-workspace-manifest.md`. The developer document now defines
the future workspace path-validation responsibilities, explicit context loader
request shape, deterministic source ordering, manifest fields, skip reasons,
external-analysis exclusion posture, future Java file map, illustrative Java
snippets, and future test checklist without adding Java source, tests, fixtures,
package directories, provider behavior, embeddings, tool execution, Graphify, or
Repomix runs. A follow-up correction clarifies that the whole `docs/` layout is
repository-specific profile input managed by repo rules and commands, not a
hard-coded Codegeist constant, and that third-party analysis artifacts are not a
core context source kind. Additional Mermaid diagrams now show the profile/core
boundary, request-to-candidate relation, deterministic source-order pipeline,
manifest structure, and loader validation sequence.

## Solve Status

- Phase: `/solve-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_05_add_context_workspace_manifest_slice.md`.
- User instructions considered: solve `t002_05`; prior user clarification remains
  in effect that this task should complete architecture design before any Java
  implementation task is created.
- Upstream phase dependency: satisfied; this task had top-level `Status: planned`
  and a current `/plan-task` handoff before this solve pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: parent
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`,
  `docs/developer/specification/context-workspace-manifest.md`,
  `docs/developer/architecture/architecture.md`, `app/codegeist/cli/pom.xml`,
  `app/codegeist/cli/Taskfile.yml`, and `docs/memory-bank/chat.md`.
- OpenCode evidence considered through `/ask-project opencode`: project/worktree
  identity, instance context path containment, safe read/list boundaries,
  external-directory approval, ignore/protected path posture, instruction/config
  context loading, source provenance, and OpenCode's lack of one complete context
  manifest.
- External documentation considered through Context7: Java SE documentation lookup
  for path/filesystem concepts. It did not change the architecture beyond
  reinforcing that Java filesystem implementation details belong to later source
  tasks.
- Result: solved as documentation-only architecture. The developer document now
  separates workspace identity/path policy, context profile, context request,
  deterministic candidate ordering, manifest, warnings, redaction, permission
  boundary, OpenCode lessons, and deferred implementation slices.
- Acceptance criteria status: satisfied. Context loading is deterministic and
  explainable in the design; workspace validation is the prerequisite before
  reads; source kinds and ordering are explicit; skip reasons include generated,
  ignored, heavy, missing optional, outside-root, symlink escape, secret-like, and
  unsupported source cases; future implementation tests are described; and no Java
  source, tests, package directories, provider calls, embeddings, RAG, Graphify,
  Repomix, tool execution, or runtime behavior were added.
- Verification: `git --no-pager diff --check`.
- Open decisions or blockers: none for this architecture pass. Future
  implementation tasks can be derived later from the deferred slice order.
- Next recommended phase: run `/finalize-task T002_05`.

## Finalization Status

- Phase: `/finalize-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_05_add_context_workspace_manifest_slice.md`.
- User instructions considered: finalize `T002_05` by checking task impact and
  refreshing affected documentation after the successful solve phase.
- Upstream phase dependency: satisfied. The target task had top-level
  `Status: solved` and a current successful `/solve-task` status before this
  finalization pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Impacted tasks: parent
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`, adjacent open
  `T002_06_add_provider_configuration_adapter.md`, and adjacent open
  `T002_07_add_tool_permission_workspace_contracts.md`.
- Documentation reviewed through the update-documentation semantics:
  `docs/developer/specification/context-workspace-manifest.md`, `docs/developer/README.md`,
  `docs/developer/architecture/architecture.md`, and `docs/memory-bank/chat.md`.
- Documentation updates: the parent task now records that `T002_05` is finalized;
  `T002_06` records that provider configuration must not own context profiles,
  workspace validation, or external analysis ingestion; `T002_07` records that
  workspace classification is deterministic and permission approval is layered
  above it; `docs/developer/architecture/architecture.md` and `docs/memory-bank/chat.md` were
  refreshed to match the finalized state.
- Remaining follow-ups: continue with
  `T002_06_add_provider_configuration_adapter.md` as the next documentation-first
  T002 slice. Future Java implementation tasks may be derived from the deferred
  slice order only when explicitly requested.
- Verification: `git --no-pager diff --check`.
- Result: finalized. No implementation gaps, blockers, Java source, tests, package
  directories, provider calls, tool execution, Graphify, Repomix, storage, or
  runtime behavior were introduced.
- Next recommended phase: start `/specify-task T002_06` or `/work-task T002_06`
  when ready to continue the next documentation/specification slice.
