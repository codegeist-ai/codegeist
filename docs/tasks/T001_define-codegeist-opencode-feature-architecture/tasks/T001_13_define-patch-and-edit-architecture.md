# T001_13 Define Patch And Edit Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist should represent and apply file changes.

This task specifies edit and patch concepts only. It does not implement file
writes, diff parsing, apply logic, rollback, or merge conflict resolution.

## Scope

- Define patch, diff, edit proposal, approval, apply result, and failure.
- Decide whether direct writes are allowed or all writes should be patch-shaped.
- Identify rollback and conflict handling questions.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode patch/edit tools? | A patch-shaped edit proposal and apply flow mediated by runtime, permission, and workspace services. |
| Are direct writes allowed? | Prefer patch-shaped writes for reviewability; direct writes are an explicit later exception for trusted built-ins only. |
| What is MVP? | Propose and apply small workspace-scoped file edits after approval. |
| What is later? | Multi-file transactions, rollback, conflict recovery, snapshots, formatter integration, and rich diff UI. |

## Boundary Rules

- Patch proposals are generated before write approval.
- Workspace validates all target paths before apply.
- Permission approval is required for any write side effect.
- Events expose proposal, approval, apply start, result, and failure summaries.
- Session stores summaries/artifact references, not unbounded file contents.

## Implementation-Readiness Questions

- Can a patch proposal be reviewed without applying it?
- Can partial apply, conflict, already-modified, and missing-file cases be
  represented as typed failures?
- Can a future Vaadin or CLI client render the same patch proposal from runtime
  data?
- Can generated/ignored file policy prevent accidental writes?
- Can rollback be deferred without making the first apply model unsafe?

## Non-Goals

- Do not implement patch parsing or file writes.
- Do not choose a Java diff/patch library yet.
- Do not implement rollback or snapshots.
- Do not make direct writes the default.

## Deliverable

Add a patch and edit architecture section to the parity document with proposal,
approval, apply result, conflict/failure, workspace, event, and storage
boundaries.

## Acceptance Criteria

- File changes are reviewable before application.
- Permission approval is required before writes.
- Conflict and partial-apply behavior is listed as an open question if not
  decided.
- Patch-shaped writes are the default architectural direction.
- Direct writes are explicitly constrained if kept as a later option.

## Verification

- Check consistency with workspace access and permission tasks.

## Verification Result

- Specified patch/edit as a reviewable, permission-gated, workspace-scoped write
  flow.
