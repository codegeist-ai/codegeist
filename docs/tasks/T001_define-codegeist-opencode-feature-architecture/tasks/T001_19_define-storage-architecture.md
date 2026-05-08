# T001_19 Define Storage Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define what Codegeist should persist and why.

## Scope

- Define session, event, configuration, audit log, tool result, and cache
  persistence needs.
- Decide what can start as file-based storage.
- Identify when Spring Data or another persistence layer becomes necessary.

## Deliverable

Add a storage architecture section to the parity document.

## Acceptance Criteria

- Persistence needs are tied to user-visible or audit behavior.
- Storage choices do not block the first CLI MVP.
- Sensitive data and credentials are called out as special cases.

## Verification

- Check consistency with session, event, provider, and permission models.
