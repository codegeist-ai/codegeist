# T008_06 Add First Authenticated Cloud API

Status: open

Parent: `../task.md`

## Goal

Implement the first authenticated Codegeist Cloud API slice after auth, tenant,
metadata, and storage boundaries are specified.

## Scope

- Add only one small authenticated endpoint or workflow.
- Use the static OAuth/OIDC provider configuration from `T008_03` only as the
  login-provider source. Define the needed user/account/token/security contract in
  this task or in a preceding focused auth/API task before adding the endpoint.
- Use metadata-backed authorization rather than S3 object paths alone.
- Keep Envoy AI Gateway behind Codegeist Server. If this task touches model
  access, the selected endpoint must prove that unauthenticated clients cannot
  reach Envoy responses directly and that Codegeist forwards only after validating
  the Codegeist token and policy context.
- Add focused server tests through `app/codegeist/server/Taskfile.yml`.

## Acceptance Criteria

- Unauthenticated access is rejected for the selected API.
- Authenticated access proves the minimum user or tenant identity contract.
- Any Envoy AI Gateway path remains internal to Codegeist Server and uses trusted
  server-set identity/account headers, not client-supplied identity headers.
- The implementation does not add full sync, billing, live hosted LLM calls, or broad
  storage behavior unless the task is explicitly rescoped.
- Architecture docs describe the implemented source and tests.

## Verification

```bash
task test
```

Run the command from `app/codegeist/server`.
