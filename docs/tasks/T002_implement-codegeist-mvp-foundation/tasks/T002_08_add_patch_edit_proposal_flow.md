# T002_08 Describe Patch Edit Proposal Flow

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_13`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

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

## Creation Note

Status: open.

Derived from patch/edit architecture as a separate documentation/specification
slice because write safety and data-loss risk should not be mixed into generic
tool contracts.
