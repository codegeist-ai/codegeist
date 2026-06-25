# T008_04 Design S3 Artifact Storage

Status: open

Parent: `../task.md`

## Goal

Define the S3-compatible artifact storage contract for cloud-stored Codegeist
commands, skills, rules, agent profiles, prompts, reusable context packs, and later
session exports.

## Scope

- Choose the first development/test target: MinIO, AWS S3, or another
  S3-compatible service.
- Define bucket layout, object keys, checksums, versions, and sync markers.
- Define the separate metadata records for ownership, permissions, indexing, quotas,
  and lookup.
- Define local test posture without real cloud credentials by default.

## Acceptance Criteria

- Artifact bytes and metadata responsibilities are clearly separated.
- Permission decisions are metadata-backed, not path-only.
- The design excludes raw API keys, OAuth tokens, and cloud credentials from normal
  artifact bytes.
- Implementation tasks can identify the first artifact family to store.

## Verification

```bash
git --no-pager diff --check
```
