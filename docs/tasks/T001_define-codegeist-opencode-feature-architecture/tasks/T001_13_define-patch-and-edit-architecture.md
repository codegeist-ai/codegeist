# T001_13 Define Patch And Edit Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist should represent and apply file changes.

## Scope

- Define patch, diff, edit proposal, approval, apply result, and failure.
- Decide whether direct writes are allowed or all writes should be patch-shaped.
- Identify rollback and conflict handling questions.

## Deliverable

Add a patch and edit architecture section to the parity document.

## Acceptance Criteria

- File changes are reviewable before application.
- Permission approval is required before writes.
- Conflict and partial-apply behavior is listed as an open question if not
  decided.

## Verification

- Check consistency with workspace access and permission tasks.
