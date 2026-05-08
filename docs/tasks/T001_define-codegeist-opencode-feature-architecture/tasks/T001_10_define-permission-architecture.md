# T001_10 Define Permission Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Codegeist's permission model for risky and side-effecting actions.

## Scope

- Define permission categories for read, write, shell, network, plugin, and
  external integrations.
- Define approval request and approval result concepts.
- Define how agent mode affects available permissions.

## Deliverable

Add a permission architecture section to the parity document.

## Acceptance Criteria

- Permission checks sit between tool requests and side effects.
- Plan mode defaults to read-only behavior.
- Approval state can be represented in events and audit logs.

## Verification

- Check consistency with tool, shell, patch, and web/fetch concepts.
