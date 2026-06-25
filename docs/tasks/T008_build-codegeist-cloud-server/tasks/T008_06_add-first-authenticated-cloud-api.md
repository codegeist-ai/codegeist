# T008_06 Add First Authenticated Cloud API

Status: open

Parent: `../task.md`

## Goal

Implement the first authenticated Codegeist Cloud API slice after auth, tenant,
metadata, and storage boundaries are specified.

## Scope

- Add only one small authenticated endpoint or workflow.
- Use the chosen auth and tenancy model from `T008_03`.
- Use metadata-backed authorization rather than S3 object paths alone.
- Add focused server tests through `app/codegeist/server/Taskfile.yml`.

## Acceptance Criteria

- Unauthenticated access is rejected for the selected API.
- Authenticated access proves the minimum user or tenant identity contract.
- The implementation does not add full sync, billing, live hosted LLM calls, or broad
  storage behavior unless the task is explicitly rescoped.
- Architecture docs describe the implemented source and tests.

## Verification

```bash
task test
```

Run the command from `app/codegeist/server`.
