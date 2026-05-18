# T001_19 Define Storage Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define what Codegeist should persist and why.

This task specifies storage responsibilities and candidate persistence strategy
only. It does not implement repositories, schemas, migrations, encryption, or
event sourcing.

## Scope

- Define session, event, configuration, audit log, tool result, and cache
  persistence needs.
- Decide what can start as file-based storage.
- Identify when Spring Data or another persistence layer becomes necessary.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode persisted messages/events/state? | Storage ports for sessions, events/audit, config, tool artifacts, and caches. |
| Is event sourcing required? | No. Event log is optional; session projections can come first. |
| What is MVP? | In-memory or file-backed session/config storage sufficient for first CLI workflows. |
| What is later? | Durable audit log, event replay/sync, server-safe storage, Spring Data, encryption, compaction, sharing, and multi-workspace persistence. |

## Boundary Rules

- Runtime owns behavior; storage persists projections and records through ports.
- Credentials and secrets are special cases and must not be mixed with ordinary
  session/tool output.
- Large outputs should be artifact references plus summaries, not unbounded
  session rows/documents.
- Storage adapters must not decide permissions, workspace policy, or provider
  selection.
- File storage should be acceptable for early CLI if it does not block migration
  to a richer backend later.

## Implementation-Readiness Questions

- Which data must survive process restart for MVP?
- Which events are audit-relevant enough to persist early?
- Where should local files live, and how are they scoped to workspace/user?
- How are redaction, retention, and deletion handled?
- When does server/Vaadin concurrency force Spring Data or database-backed
  storage?

## Non-Goals

- Do not implement storage adapters or schemas.
- Do not choose a database.
- Do not implement encryption or credential storage.
- Do not require event sourcing for MVP.

## Deliverable

Add a storage architecture section to the parity document with persistence
categories, MVP/later split, file-backed option, sensitive-data handling,
event/audit choices, and migration triggers for Spring Data or another backend.

## Acceptance Criteria

- Persistence needs are tied to user-visible or audit behavior.
- Storage choices do not block the first CLI MVP.
- Sensitive data and credentials are called out as special cases.
- Event sourcing is optional rather than assumed.
- Storage ports are separated from runtime orchestration.

## Verification

- Check consistency with session, event, provider, and permission models.

## Verification Result

- Specified storage categories, MVP/later split, sensitive-data concerns, and
  adapter boundaries.

## Solution Note

Status: completed.

The solution pass added `## Storage Architecture` to
`docs/developer/specification/codegeist-opencode-parity.md`. The section defines persistence
categories, MVP/later posture, file-backed option, sensitive-data handling,
event/audit choices, and migration triggers for Spring Data or another backend.

No user decision is pending. Event sourcing remains optional, storage ports stay
separate from runtime orchestration, and credentials/secrets are called out as
special cases outside ordinary session/tool output.

Verification passed with `git --no-pager diff --check`. A final review confirmed
storage choices do not block the first CLI MVP.
