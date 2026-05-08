# T001_11 Define Workspace And File Access

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define safe workspace and file-system access rules for Codegeist.

## Scope

- Define workspace root, allowed paths, ignored paths, and generated output
  handling.
- Define read versus write access.
- Identify path traversal and symlink risks.

## Deliverable

Add a workspace and file access section to the parity document.

## Acceptance Criteria

- Workspace boundary checks are owned by one conceptual service.
- Read-only behavior is compatible with Plan mode.
- Write access is tied to permission approval.

## Verification

- Review against repo rules for generated artifacts and ignored files.
