# T008_03 Design Auth And Tenant Model

Status: open

Parent: `../task.md`

## Goal

Choose the first authentication strategy and define the account metadata model for
Codegeist Cloud users, future organizations, authorization decisions, and ownership.

## Scope

- Compare the first feasible login method: OAuth, magic link, hosted identity
  provider, or username/password.
- Define user identity fields and durable ids.
- Decide whether organizations exist in the first schema or remain a later
  migration.
- Define where authorization state is stored and how it relates to artifact
  metadata.

## Acceptance Criteria

- A specific first auth strategy is recommended with tradeoffs.
- User and organization metadata contracts are documented without source stubs.
- Authorization is not inferred only from S3 object paths.
- Secrets and tokens are not committed or stored in artifact bytes.

## Verification

```bash
git --no-pager diff --check
```
