# T008_01 Define Cloud Product Boundaries

Status: open

Parent: `../task.md`

## Goal

Specify Codegeist Cloud as a hosted SaaS control plane before deeper server source
is added. This task should settle the first product boundaries for login, tenancy,
entitlements, model access, S3-compatible artifact storage, metadata, and client
sync.

## Scope

- Define the initial login direction to evaluate first.
- Define the first account model shape: individual users now, organizations later,
  or users plus organizations from the start.
- Define entitlement, quota, usage-accounting, and model-allowlist boundaries at a
  product level.
- Define object-storage plus metadata ownership boundaries for reusable agent
  assets.
- Define the first client-sync artifact family to target later.

## Acceptance Criteria

- The chosen boundaries are documented without Java source changes.
- The document keeps implemented state separate from planned behavior.
- Live hosted LLM calls, paid provider checks, and real cloud storage effects stay
  out of scope.
- Later implementation tasks can identify which boundary they are allowed to add.

## Verification

```bash
git --no-pager diff --check
```
