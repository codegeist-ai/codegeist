# T002_08 Add Patch Edit Proposal Flow

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_13`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

## Goal

Implement the first reviewable patch/edit proposal and apply-result contracts for
workspace-scoped file changes.

## Context

The architecture prefers patch-shaped writes for reviewability. Direct writes are
deferred as an explicit later exception for trusted built-ins only.

## Concrete Solution

1. Add contracts for edit proposal, target file, patch hunk or text replacement,
   apply request, apply result, and typed apply failure.
2. Require permission and workspace validation before any apply contract can move
   from proposed to applied.
3. Add tests for proposal construction, workspace-denied targets, conflict or
   stale-file failure representation, and result summaries.
4. Keep actual patch parsing/apply logic minimal or stubbed behind a port unless a
   tiny safe implementation is needed for tests.

## Scope

- `ai.codegeist.tool` patch/edit contracts
- `ai.codegeist.workspace` integration points
- `ai.codegeist.permission` integration points
- focused tests

## Acceptance Criteria

- File changes are represented as reviewable proposals before application.
- Apply results include success, partial/failure, conflict, missing target, and
  stale input concepts.
- Permission and workspace gates are visible in the contract.
- Tests cover proposal and failure shape without broad file mutation behavior.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_07`.

## Non-Goals

- Do not implement rollback, multi-file transactions, rich diff UI, formatter
  integration, or direct-write defaults.

## Open Questions

- Which Java patch/diff library, if any, should be introduced after the contract
  shape is stable?

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- This task is correctly separated from generic tool contracts because data-loss
  and conflict behavior need focused acceptance criteria.
- Source research should focus on OpenCode patch/edit flow and failure handling,
  not on copying its TypeScript implementation shape.

## Creation Note

Status: open.

Derived from patch/edit architecture as a separate implementation slice because
write safety and data-loss risk should not be mixed into generic tool contracts.
