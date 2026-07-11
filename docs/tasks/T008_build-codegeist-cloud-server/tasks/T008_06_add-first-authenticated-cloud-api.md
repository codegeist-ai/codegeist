# T008_06 Add First Authenticated Cloud API

Status: solved

Parent: `../task.md`

## Goal

Implement the first authenticated Codegeist Cloud API slice after auth, tenant,
metadata, and storage boundaries are specified.

## Implemented API

This task implements only one small authenticated API:

```text
GET /api/v1/me
```

The endpoint returns the minimum identity projection needed by later cloud-login and
sync work:

```json
{
  "userId": "user-123",
  "accountId": "account-123",
  "issuer": "https://codegeist.cloud"
}
```

`userId` is read from the authenticated JWT `sub` claim. `accountId` is read from
the Codegeist-owned `codegeist_account_id` claim. `issuer` is copied from the JWT
issuer claim when present.

The endpoint ignores client-supplied identity headers such as
`x-codegeist-user-id` and `x-codegeist-account-id`; identity comes only from the
authenticated Spring Security principal.

## Implemented Scope

- Added the Boot 4 OAuth2 resource-server security starter and `spring-security-test`
  for deterministic server API tests.
- Added `ai.codegeist.server.auth.CodegeistServerSecurityConfiguration`.
- Kept `GET /health` public.
- Protected `GET /api/v1/me` and future `/api/**` routes behind Spring Security.
- Disabled form login, HTTP Basic, logout, and CSRF for the API slice.
- Returned `401 Unauthorized` for unauthenticated API requests.
- Enabled OAuth2 JWT resource-server handling only when a `JwtDecoder` bean is
  available. Without a decoder, API routes remain protected but no real bearer token
  mechanism is enabled, which keeps the default server fail-closed.
- Added `ai.codegeist.server.api.CloudIdentityController` for the identity endpoint.
- Reused `HealthController.HEALTH_PATH` as a public constant so security and tests do
  not duplicate the health route string.
- Added `CloudIdentityControllerTest` using MockMvc and Spring Security's `jwt()`
  request post-processor to prove the security and identity contract without live
  authentik, JWKS, browser login, or token issuance.

## Security Contract

This slice treats bearer tokens as Codegeist API tokens. The current source does not
issue those tokens yet. A later login task owns browser authorization-code flow,
external identity linking, token creation, token persistence, refresh/revocation, and
CLI storage of the returned Codegeist token.

The current resource-server path is ready for deployments or later tasks to provide a
real JWT decoder through Spring Boot resource-server configuration such as
`spring.security.oauth2.resourceserver.jwt.issuer-uri` or `jwk-set-uri`. Until then,
the default application starts with API routes protected but no real bearer-token
validator configured.

## Deferred Boundaries

- No browser login endpoint, OAuth callback, state, PKCE, or provider redirect.
- No live external identity-provider call in default tests.
- No Codegeist API token issuance, hashing, persistence, refresh, revocation, or
  expiry policy.
- No durable user, account, external-identity, tenant, organization, membership, role,
  entitlement, quota, usage, billing, or artifact metadata store.
- No Java object-storage API, MinIO client, S3 STS delegation, or command-artifact
  sync endpoint.
- No Java Envoy AI Gateway proxy endpoint, hosted-provider call, model allowlist,
  request streaming, usage capture, or trusted gateway headers.
- No CLI `codegeist login`, configured server-url store, local token storage, or sync
  behavior.

## Acceptance Criteria

- Unauthenticated access to `GET /api/v1/me` is rejected.
- Authenticated access returns the minimum Codegeist user and account identity
  projection from the security principal.
- Client-supplied identity headers are ignored.
- `GET /health` remains unauthenticated.
- Envoy AI Gateway remains internal to the existing local Docker fixture; no Java
  model proxy is added in this task.
- Full sync, billing, live hosted LLM calls, and broad storage behavior remain out of
  scope.
- Architecture docs describe the implemented source and tests.

## Verification

Run the focused server suite from `app/codegeist/server`:

```bash
task test
```

Run a repository diff whitespace check before finishing:

```bash
git --no-pager diff --check
```
