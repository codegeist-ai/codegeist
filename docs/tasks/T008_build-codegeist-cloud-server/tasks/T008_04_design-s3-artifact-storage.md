# T008_04 Design MinIO S3 Artifact Storage

Status: open

Parent: `../task.md`

## Goal

Define the S3-compatible artifact storage contract for cloud-stored Codegeist
commands, skills, rules, agent profiles, prompts, reusable context packs, and later
session exports. Use MinIO as the first local development and test target.

## Scope

- Use MinIO as the first development/test target for S3-compatible object bytes.
- Define bucket layout, object keys, checksums, versions, and sync markers.
- Define the separate metadata records for ownership, permissions, indexing, quotas,
  and lookup.
- Define local test posture without real cloud credentials by default.

## Settled Storage Direction

Use MinIO for the first local storage stack. MinIO stands in for later production
S3-compatible storage and must not become the only supported storage assumption in
Codegeist metadata or authorization code.

The first bucket can be named `codegeist-artifacts` in local tests. Store only
artifact bytes there. Metadata for users, accounts, artifact ownership, sharing,
permissions, versions, checksums, sync state, quotas, and indexes stays in
Codegeist metadata, not in S3 object paths or bucket policies.

Candidate artifact families include:

- commands
- skills
- rules
- agent configuration
- prompts and reusable context packs
- later session exports

## Authorization Boundary

MinIO object keys may include account ids and artifact ids for operational
partitioning, but S3 paths are not the permission system. API handlers must decide
access from Codegeist-authenticated user/account context plus metadata-backed
ownership, membership, sharing, entitlement, and quota records.

Normal artifact bytes must not contain API keys, OAuth tokens, Codegeist API
tokens, provider refresh tokens, cloud credentials, or generated secret material.
Encrypted secret storage remains a separate future security task.

## Test Stack Boundary

Local MinIO tests should use fixed test credentials supplied by the local test
stack and never real AWS credentials. Fast server unit tests should mock or fake
storage behavior. Real MinIO bucket/object checks belong in an opt-in integration
or smoke task.

## Acceptance Criteria

- Artifact bytes and metadata responsibilities are clearly separated.
- MinIO is documented as the first local S3-compatible test target.
- Permission decisions are metadata-backed, not path-only.
- The design excludes raw API keys, OAuth tokens, and cloud credentials from normal
  artifact bytes.
- Implementation tasks can identify the first artifact family to store.

## Verification

```bash
git --no-pager diff --check
```
