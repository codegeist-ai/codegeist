# T002_08 Describe Patch Edit Proposal Flow

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_13`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

status: finalized

## Goal

Describe the first reviewable patch/edit proposal and apply-result contracts for
workspace-scoped file changes without adding patch/edit implementation yet.

## Context

The architecture prefers patch-shaped writes for reviewability. Direct writes are
deferred as an explicit later exception for trusted built-ins only.

## Concrete Solution

1. Create or update `docs/developer/patch-edit-proposal-contracts.md` as the
   future patch/edit proposal blueprint.
2. Define future edit proposal, target file, patch hunk or text replacement,
   apply request, apply result, and typed apply failure shapes.
3. Describe permission and workspace validation before any apply contract can move
   from proposed to applied.
4. Document future tests for proposal construction, workspace-denied targets,
   conflict or stale-file failure representation, and result summaries.
5. Include OpenCode source evidence, future file maps, diagrams, and illustrative
   Java snippets in markdown only.

## Scope

- `docs/developer/patch-edit-proposal-contracts.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- File changes are specified as reviewable proposals before application.
- Apply results include success, partial/failure, conflict, missing target, and
  stale input concepts in the blueprint.
- Permission and workspace gates are visible in the documented contract.
- Future proposal and failure-shape tests are described, but no Java source,
  tests, or patch application behavior is created by this task.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_07`.

## Non-Goals

- Do not create Java source files, empty package directories, patch tests, patch
  parser code, or apply logic.
- Do not implement rollback, multi-file transactions, rich diff UI, formatter
  integration, or direct-write defaults.

## Open Questions

- Which Java patch/diff library, if any, should be introduced after the contract
  shape is stable?

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later patch/edit implementation task instead of creating
  patch/edit source packages now.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- This task is correctly separated from generic tool contracts because data-loss
  and conflict behavior need focused acceptance criteria.
- Source research should focus on OpenCode patch/edit flow and failure handling,
  not on copying its TypeScript implementation shape.

## Dependency Impact Notes

- Finalized `T002_07_add_tool_permission_workspace_contracts.md` defines the
  generic tool request, permission decision, workspace target validation, bounded
  result, output-reference, and event/session projection boundaries. This task
  should specialize those boundaries for reviewable patch/edit proposals and apply
  results instead of redefining generic tool policy.
- Patch application remains a Build-mode, permission-gated, workspace-validated
  side effect. Plan mode may describe or propose changes, but it must not apply
  patches.
- Patch results should use bounded summaries and output references from the
  `T002_07` blueprint; they must not store full file contents, unbounded patch
  payloads, or provider-native tool data in session parts.

## Implementation-Readiness Questions

- Which proposal fields are necessary for the first review surface: proposal id,
  target workspace paths, original content identity, hunk summaries, replacement
  summaries, creation/deletion flags, and redacted preview metadata?
- Which apply request fields must prove the user approved the exact proposal that
  is being applied, rather than a regenerated or stale proposal?
- Which stale-input signal should the blueprint require before apply: content hash,
  file timestamp, workspace revision, or a later implementation-specific check?
- Which typed failures must be distinguishable for the first implementation handoff:
  mode denied, permission denied, workspace denied, missing target, conflict,
  stale input, invalid patch, partial apply, and unexpected I/O failure?
- How should proposal and apply summaries use the bounded result and output
  reference model from `T002_07` without storing full file contents or unbounded
  patch payloads in session parts?
- Which future tests should prove that Plan mode may describe proposed edits but
  cannot apply them, while Build mode still requires permission approval and
  workspace validation before mutation?

## Phase Status

- Phase: `/specify-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_08_add_patch_edit_proposal_flow.md`.
- Context or instructions considered: user requested a specification pass by exact
  task path with no additional narrowing instructions.
- Upstream phase dependency: none; `/specify-task` is the entry phase and may be
  repeated when patch/edit, permission, workspace, or bounded-result boundaries
  need to be refreshed.
- Parent considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Adjacent and dependency tasks considered: finalized dependency
  `T002_07_add_tool_permission_workspace_contracts.md`, adjacent
  `T002_09_add_controlled_shell_verification_tool.md`, and source tasks
  `T001_13`, `T001_10`, `T001_11`, `T001_22`, and `T001_24`.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Documentation considered: `docs/developer/architecture.md`,
  `docs/developer/codegeist-opencode-parity.md`,
  `docs/developer/tool-permission-workspace-contracts.md`, and
  `docs/memory-bank/chat.md`.
- Discovered hints considered: the T002 parent `Default Solve Hints` point this
  task toward OpenCode source evidence for patch/edit tools, permission mediation,
  workspace validation, conflict handling, and bounded result summaries while
  preserving Codegeist's Java-first runtime-owned boundaries.
- Result: specified. The task remains a documentation-only patch/edit proposal and
  apply-result blueprint. It now explicitly preserves the boundary from finalized
  `T002_07`: patch application is a Build-mode side effect that requires exact
  proposal review, permission approval, deterministic workspace validation, typed
  failures, bounded summaries, and output references before later Java source or
  tests are created.
- Open decisions or blockers: no blocker for planning. The later implementation
  task still needs to choose any Java diff/patch library or implementation-specific
  stale-input signal after this contract is documented.
- Next recommended phase: run `/plan-task T002_08` as a documentation-only
  architecture plan for `docs/developer/patch-edit-proposal-contracts.md`.

## Architecture Plan

This planning pass keeps `T002_08` as one documentation-only architecture task. No
child task is needed because the source task already targets one narrow patch/edit
proposal and apply-result blueprint, and no matching developer document exists yet.

### Selected Option

Create `docs/developer/patch-edit-proposal-contracts.md` as the concrete
patch/edit proposal and apply-result contract blueprint, then cross-link it from
developer documentation and current-state architecture notes.

This option follows the active T002 posture: design the complete architecture
handoff first, then derive Java implementation tasks later only when explicitly
requested.

### Concrete Design Direction

Deepen the patch/edit architecture around eight layers:

1. Proposal lifecycle and ownership.
2. Target-file and workspace-validation posture.
3. Patch hunk and text-replacement representation.
4. Approval binding between an exact reviewed proposal and an apply request.
5. Build-mode apply flow with Plan-mode apply denial.
6. Typed apply result and failure taxonomy.
7. Bounded summaries, output references, events, and session message parts.
8. Future Java contract and test handoff, without creating Java source.

Patch application should be documented as a specialized `T002_07` tool capability.
It must not redefine generic tool policy. The blueprint should show that proposal
creation may be safe in Plan or Build mode, but applying a proposal is a Build-mode,
permission-gated, workspace-validated mutation.

### Planned Documentation Files

- `docs/developer/patch-edit-proposal-contracts.md`
- `docs/developer/README.md`
- `docs/developer/architecture.md` only for current-state cross-references and
  explicit not-implemented notes
- this task file

No Java source, tests, fixtures, Maven files, build files, package directories,
patch parser, apply executor, direct-write behavior, rollback, formatter
integration, Graphify, Repomix, or runtime behavior are planned for this task.

### Planned Blueprint Content

1. State the purpose and non-implementation boundary of the patch/edit proposal
   contract blueprint.
2. Summarize OpenCode source evidence for write, apply-patch, permission,
   workspace validation, output truncation, and tool/session event behavior.
3. Reuse `T002_07` as the generic tool, permission, workspace, bounded-result, and
   event/session boundary instead of redefining those contracts.
4. Define future Codegeist contract names for `EditProposal`, `EditProposalId`,
   `EditTarget`, `PatchHunk`, `TextReplacement`, `ApplyEditRequest`,
   `ApplyEditResult`, `ApplyEditFailure`, `ProposalSummary`, and `OutputRef`.
5. Define proposal identity and freshness posture so an apply request can prove it
   targets the exact reviewed proposal and detect stale file input before mutation.
6. Define target-file metadata for existing files, file creation, deletion,
   rename-like later behavior, generated/ignored posture, and workspace validation
   verdicts.
7. Define hunk/replacement metadata at summary level without committing to a Java
   diff/patch library.
8. Define apply flow as: construct proposal, summarize for review, check mode,
   validate workspace targets, ask permission for exact proposal, re-check
   freshness, apply, summarize result, and record typed events/session parts.
9. Define typed failure categories for mode denied, permission denied, workspace
   denied, missing target, stale input, conflict, invalid patch, partial apply,
   output overflow, and unexpected I/O failure.
10. Document direct writes as a deferred trusted-built-in exception, not a default
    MVP path.
11. Include Mermaid diagrams for proposal/apply sequence and future contract
    relationships.
12. Include illustrative Java snippets inside markdown only, clearly labeled as
    non-implemented examples.
13. Add a future file map for later Java tasks, marked illustrative only.
14. Add a future test handoff table covering proposal construction, Plan-mode apply
    denial, Build-mode approval requirement, workspace-denied targets, stale input,
    conflict/failure representation, bounded summaries, and session/event
    projection.

### Planned OpenCode Evidence Questions

Use targeted source research during `/solve-task`, not broad implementation
copying:

```text
/ask-project opencode "How do write and apply_patch tools create diffs, request edit permission, validate paths, apply changes, and summarize results? Cite source files and diagram the sequence."
/ask-project opencode "How are patch/edit tool results and failures represented in session events or message parts? Cite source files."
```

Expected source areas to verify include `tool/write.ts`, `tool/apply_patch.ts`,
`tool/tool.ts`, `permission/index.ts`, `permission/evaluate.ts`,
`tool/external-directory.ts`, `tool/truncate.ts`, `v2/session-event.ts`, and
`v2/session-message.ts` under `docs/third-party/opencode/source/`.

### Implementation Steps

1. Read the OpenCode source evidence for write/apply-patch, permission,
   workspace, truncation, and session-event behavior.
2. Draft `docs/developer/patch-edit-proposal-contracts.md` with scope,
   non-goals, evidence, ownership rules, proposal lifecycle, apply flow, failure
   taxonomy, bounded result posture, diagrams, illustrative Java snippets, future
   file map, and future test handoff.
3. Cross-link the new blueprint from `docs/developer/README.md`.
4. Update `docs/developer/architecture.md` only to add a related-document link and
   keep the patch/edit flow listed as not implemented.
5. Update this task with a solution note, solve status, acceptance-criteria check,
   and verification result.
6. Refresh `docs/memory-bank/chat.md` so future sessions know the blueprint exists
   and remains documentation-only.
7. Run `git --no-pager diff --check`.

### Acceptance Criteria For Solve

- `docs/developer/patch-edit-proposal-contracts.md` exists and describes
  reviewable proposals before application.
- Apply results include success, partial/failure, conflict, missing target, stale
  input, permission denial, workspace denial, and invalid patch concepts.
- Permission, Build-mode, workspace-validation, and proposal-freshness gates are
  visible before any apply behavior can move from proposed to applied.
- The document uses bounded summaries and output references rather than full file
  contents or unbounded patch payloads in session/event examples.
- Future proposal and failure-shape tests are described, but no Java source,
  tests, package directories, patch parser, apply executor, or runtime behavior is
  created.
- `docs/developer/README.md`, `docs/developer/architecture.md`, this task file,
  and `docs/memory-bank/chat.md` are consistent with the new blueprint.

### Verification Plan

```bash
git --no-pager diff --check
```

This proves the documentation changes are free of whitespace errors. `task test` is
not required because the planned solve changes documentation only and must not touch
Java source, tests, build files, or runtime behavior.

### Dependencies And Tradeoffs

- Depends on finalized `T002_07` for generic tool, permission, workspace, bounded
  result, output-reference, event, and session projection boundaries.
- Should stay separate from `T002_09`, which owns controlled shell verification.
- Keeps rollback, multi-file transactions, rich diff UI, formatter integration,
  direct-write defaults, and Java diff/patch library choice out of this solve so
  the blueprint remains safe and reviewable.
- Uses OpenCode source evidence for behavior and failure boundaries, but translates
  findings into Codegeist's Java-first runtime-owned architecture instead of
  copying TypeScript structure.

### Open Questions

- None for this documentation plan. The Java diff/patch library and exact
  implementation-specific stale-input mechanism remain deferred until a later Java
  implementation task.

## Plan Workflow Handoff

- Phase: `/plan-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_08_add_patch_edit_proposal_flow.md`.
- Source task resolved: `T002_08` as
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_08_add_patch_edit_proposal_flow.md`.
- User context considered: the user ran `/plan-task T002_08` with no additional
  narrowing instructions.
- Parent task considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Adjacent and dependency tasks considered: finalized dependency
  `T002_07_add_tool_permission_workspace_contracts.md`, adjacent open
  `T002_09_add_controlled_shell_verification_tool.md`, and source tasks
  `T001_13`, `T001_10`, `T001_11`, `T001_22`, and `T001_24`.
- Duplicate check result: no existing
  `docs/developer/patch-edit-proposal-contracts.md` document exists, and no
  separate task already plans this blueprint; the existing `T002_08` task is the
  correct target to sharpen.
- Selected option: create the patch/edit proposal architecture blueprint document
  in the solve pass and cross-link it from developer documentation while leaving
  Java source and build files untouched.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: `docs/developer/codegeist-opencode-parity.md`,
  `docs/developer/tool-permission-workspace-contracts.md`,
  `docs/developer/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- Upstream phase dependency: satisfied; this task had top-level
  `status: specified` and a current `/specify-task` phase status before this
  planning pass.
- Result: planned as one documentation-only architecture task.
- Open decisions or blockers: none for planning.
- Next recommended phase: run `/solve-task T002_08` as a documentation-only
  architecture design pass.

## Solution Note

Solved as a documentation-only architecture blueprint in
`docs/developer/patch-edit-proposal-contracts.md`. The new document defines
Codegeist-owned patch/edit proposal identity, target metadata, patch hunk and text
replacement summary posture, exact apply request binding, Build-mode apply gates,
typed apply result and failure shapes, bounded result summaries, output references,
runtime event/session projection, future Java file maps, illustrative Java
snippets, and future test handoff notes.

The solve pass also linked the new blueprint from `docs/developer/README.md` and
`docs/developer/architecture.md`, and refreshed `docs/memory-bank/chat.md`. It did
not create Java source, tests, package directories, patch parser code, apply
logic, file writes, rollback, formatter integration, Graphify, Repomix, or runtime
behavior.

## Solve Status

- Phase: `/solve-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_08_add_patch_edit_proposal_flow.md`.
- User instructions considered: solve `T002_08` using the existing plan. Prior
  user decisions remain in effect: this slice is documentation-only and should
  create `docs/developer/patch-edit-proposal-contracts.md` without Java source.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: planned` and a current `/plan-task` handoff before this solve pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`, finalized
  `T002_07_add_tool_permission_workspace_contracts.md`, adjacent open
  `T002_09_add_controlled_shell_verification_tool.md`,
  `docs/developer/codegeist-opencode-parity.md`,
  `docs/developer/tool-permission-workspace-contracts.md`,
  `docs/developer/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- OpenCode evidence considered: `tool/write.ts` for diff creation, edit permission,
  writes, file events, and diagnostics; `tool/apply_patch.ts` for hunk parsing,
  add/update/delete/move handling, per-file permission metadata, mutation, file
  watcher events, and summaries; `tool/tool.ts` for execution context, permission
  ask hooks, result metadata, and output truncation; `permission/index.ts` and
  `permission/evaluate.ts` for permission requests, replies, edit-tool grouping,
  and rule evaluation; `tool/external-directory.ts` for external-directory gates;
  `tool/truncate.ts` for bounded output and output paths; and
  `v2/session-event.ts` plus `v2/session-message.ts` for tool lifecycle events and
  bounded assistant tool message parts.
- Documentation updates: created
  `docs/developer/patch-edit-proposal-contracts.md`, updated
  `docs/developer/README.md`, updated `docs/developer/architecture.md`, and
  refreshed `docs/memory-bank/chat.md`.
- Acceptance criteria status: satisfied. File changes are specified as reviewable
  proposals before application; apply results include success, partial/failure,
  conflict, missing target, stale input, permission denial, workspace denial, and
  invalid patch concepts; permission, Build-mode, workspace, and freshness gates
  are visible before apply; future tests are described; and no Java source, tests,
  package directories, patch parser, apply executor, file writes, or runtime
  behavior were created.
- Verification: `git --no-pager diff --check`.
- Open decisions or blockers: none for this architecture pass. Future Java
  implementation tasks still need to choose a diff/patch library, concrete
  freshness mechanism, persistence posture, and apply executor design.
- Next recommended phase: run `/finalize-task T002_08`.

## Finalization Status

- Phase: `/finalize-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_08_add_patch_edit_proposal_flow.md`.
- User instructions considered: finalize `T002_08` by checking task impact and
  refreshing affected documentation after the successful solve phase.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: solved` and a current successful `/solve-task` status before this
  finalization pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Impacted tasks: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md` and adjacent open
  `T002_09_add_controlled_shell_verification_tool.md`.
- Documentation reviewed through update-documentation semantics:
  `docs/developer/patch-edit-proposal-contracts.md`, `docs/developer/README.md`,
  `docs/developer/architecture.md`, and `docs/memory-bank/chat.md`.
- Documentation updates: the parent task now records that `T002_08` is finalized;
  `T002_09` records that shell verification should remain separate from
  patch/edit apply behavior while reusing bounded result, typed failure,
  permission, workspace, and event/session summary posture where applicable; and
  `docs/memory-bank/chat.md` was refreshed to make `T002_09` the next useful T002
  slice. `docs/developer/README.md` and `docs/developer/architecture.md` were
  already updated during solve and remained accurate during finalization.
- Remaining follow-ups: continue with
  `T002_09_add_controlled_shell_verification_tool.md` as the next
  documentation-first T002 slice. Future Java implementation tasks may be derived
  later from `docs/developer/patch-edit-proposal-contracts.md` only when
  explicitly requested.
- Verification: `git --no-pager diff --check` and
  `git --no-pager diff --check --no-index /dev/null docs/developer/patch-edit-proposal-contracts.md`.
- Result: finalized. No implementation gaps, blockers, Java source, tests, package
  directories, patch parser code, apply logic, file writes, rollback, formatter
  integration, Graphify, Repomix, or runtime behavior were introduced.
- Next recommended phase: start `/specify-task T002_09` or `/work-task T002_09`
  when ready to continue the controlled shell verification slice.

## Creation Note

Initial status: open.

Derived from patch/edit architecture as a separate documentation/specification
slice because write safety and data-loss risk should not be mixed into generic
tool contracts.
