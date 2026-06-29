# T008_03 Implement Auth And Tenant Foundation

Status: open

Parent: `../task.md`

## Goal

Implement the first Codegeist Cloud authentication and tenant foundation in
`app/codegeist/server`. This task turns the settled auth decision into source code
that later authenticated API tasks can use: static external OIDC provider config,
internal user/account metadata, Codegeist-owned API token metadata, token
validation, and the Spring Security baseline.

This task must add focused Java source and tests. It must not add live external
identity-provider calls, a database migration, artifact storage, hosted LLM calls,
or the first authenticated product API endpoint.

## Current Implemented State

`app/codegeist/server` currently exposes only the unauthenticated bootstrap
`GET /health` endpoint. The server has no Spring Security configuration, login
flow, users, accounts, API tokens, metadata database, organization support,
artifact authorization, or CLI cloud-login behavior yet.

## Settled Auth Decision

Codegeist Cloud must not host its own OAuth2/OIDC authorization server in the
first auth model. Codegeist acts as an OAuth2/OIDC client, also called a relying
party, against externally operated identity providers. Users need an external
account before they can sign in to Codegeist.

Use generic external OIDC as the first auth strategy. The initial implementation
must support multiple statically configured OIDC providers per Codegeist server
deployment, for example a self-hosted Keycloak realm, an authentik application,
or Google. GitHub can be added later as an OAuth2 provider adapter, but it should
not be the core path because GitHub login is not the same as a normal generic
OIDC issuer for end-user authentication.

Use authentik as the first local test OIDC provider. authentik stands in for the
later production providers such as Google, GitHub, Keycloak, authentik, or other
OIDC/OAuth providers. Codegeist must treat authentik as one configured external
provider, not as a product-specific identity dependency hard-coded into the
server model.

Codegeist issues its own API tokens after a successful external login. Provider
access tokens are only identity-proof inputs for the login callback. They must
not become Codegeist API credentials, must not be stored in artifact bytes, and
should be discarded after identity lookup unless a later security task defines
encrypted provider-token storage.

## Implementation Scope

- Add the server Spring Security dependencies needed for servlet security,
  OAuth2/OIDC login preparation, bearer-token API authentication, and security
  tests.
- Add a Spring-managed auth configuration model under `codegeist.auth.providers`
  for multiple static OIDC providers. Provider ids are deployment-local
  identifiers used in URLs, metadata, logs, and token records.
- Include authentik in the local test and smoke configuration as the first OIDC
  provider target. Keep the runtime contract generic enough that the same config
  model can later point at Google, GitHub through a dedicated OAuth adapter,
  Keycloak, or another external provider.
- Validate provider config with Bean Validation. Reject missing provider ids,
  unsupported provider types, blank issuer URIs, and blank client ids.
- Keep client secrets and future provider credentials out of committed examples.
  If this slice needs a secret reference for Spring Security wiring, use an
  environment-backed or external Spring configuration value, not artifact bytes.
- Add internal metadata model classes for the first auth records: user, external
  identity, account, account membership, and API token metadata.
- Add an in-memory metadata store or service so tests can prove the first identity
  and token contracts without selecting the final database technology.
- Implement external identity matching by `providerId`, `issuer`, and `subject`.
  Do not automatically merge users only because two providers return the same
  email address.
- Implement the first personal-account creation behavior: a newly linked external
  identity creates an internal user, a personal account, and an owner membership.
- Implement Codegeist-owned opaque API token issuance and validation with a secure
  stored hash or verifier. The plaintext token may be returned only at creation
  time and must not be stored.
- Add revocation, expiry, created-at, last-used-at, user, account, and scope
  metadata to API tokens.
- Add the Spring Security baseline: keep `GET /health` unauthenticated and make
  non-health server routes require Codegeist authentication once such routes exist.
  T008_06 will add the first product endpoint that consumes the authenticated
  principal.

Illustrative server configuration shape:

```yaml
codegeist:
  auth:
    providers:
      keycloak:
        type: oidc
        issuer-uri: https://sso.example.com/realms/codegeist
        client-id: codegeist
      authentik:
        type: oidc
        issuer-uri: https://auth.example.net/application/o/codegeist/
        client-id: codegeist
      google:
        type: oidc
        issuer-uri: https://accounts.google.com
        client-id: codegeist
```

## Local authentik Test Provider

The first integration test setup should run an authentik OIDC server owned by the
local test stack. The test stack proves that Codegeist can use a real external
OIDC issuer without making authentik part of the Codegeist product model.

Expected local-auth properties:

- The provider id can be `authentik` for local tests, but Codegeist logic must
  use the configured provider id rather than special-casing that string.
- The issuer URI points at the authentik application/provider discovery endpoint
  exposed by the local stack.
- Codegeist receives normal OIDC claims, then creates or finds Codegeist-owned
  users, personal accounts, memberships, and Codegeist API tokens.
- Provider access tokens and refresh tokens are not stored as Codegeist API
  credentials.
- Fast unit tests must not require authentik. Any real authentik flow belongs in
  an opt-in integration or smoke task.

## Identity Metadata Contract

Use Codegeist-owned durable ids for application data. External identities are
login proofs and account-linking inputs, not primary ownership ids for artifacts.

Minimal metadata records:

| Record | Purpose | Important fields |
| --- | --- | --- |
| `user` | Internal Codegeist person identity. | `id`, `display_name`, optional `primary_email`, `created_at`, `updated_at`, `disabled_at` |
| `external_identity` | Binding from an external login identity to a Codegeist user. | `id`, `user_id`, `provider_id`, `issuer`, `subject`, optional `email`, optional `email_verified`, `created_at`, `last_login_at` |
| `account` | Ownership and policy scope. The first account is a personal account for one user. | `id`, `type=personal`, `owner_user_id`, `created_at`, `disabled_at` |
| `account_membership` | Authorization join between users and accounts. | `account_id`, `user_id`, `role=owner`, `created_at`, `disabled_at` |
| `api_token` | Codegeist-issued API token metadata. | `id`, `user_id`, `account_id`, `token_hash`, `scopes`, `expires_at`, `created_at`, `last_used_at`, `revoked_at` |

Later account linking can bind multiple external identities to one Codegeist user
only through an explicit flow, such as a currently authenticated user adding
another provider identity or an admin-approved policy.

## Login And Token Flow Boundary

The first full successful auth product flow remains broader than this task:

1. A user starts login against Codegeist and selects or targets a configured OIDC
   provider.
2. Codegeist redirects the user to that external provider with the normal OAuth2
   authorization-code protections, including state and PKCE where supported by
   the client setup.
3. The provider authenticates the user and redirects back to Codegeist.
4. Codegeist validates the callback, reads the provider identity claims, and finds
   or creates the internal Codegeist user.
5. Codegeist finds or creates the user's personal account scope.
6. Codegeist issues a Codegeist-owned bearer token for later Codegeist API calls.
7. Subsequent API requests authenticate with the Codegeist bearer token, not with
   the upstream provider token.

This task implements the local contracts behind steps 4 through 7 with
deterministic tests. It may prepare Spring Security OAuth2 login wiring, but it
must not require a real Keycloak, authentik, Google, GitHub, or other external
identity provider during tests.

## Organization Posture

Do not implement organizations in this task. Keep the account model
organization-ready by using `account` and `account_membership` as the durable
authorization boundary instead of embedding user ids directly into artifact
ownership or S3 object keys.

Later organization support can add `organization` metadata and additional
`account.type=organization` records, membership roles, invitations,
organization-scoped identity policies, shared artifacts, and team quotas without
changing the basic artifact authorization rule.

## Authorization And Artifact Ownership

Authorization decisions must be based on Codegeist metadata, not on upstream
provider identity alone and not on S3 object paths. Artifact metadata should
reference an owner account and creator user, for example `owner_account_id` and
`created_by_user_id`. API handlers should authorize access by checking the
authenticated Codegeist token, the token's user, account membership, entitlement,
and artifact metadata.

S3 bucket names and object keys are storage implementation details. They may
include account ids for operational partitioning, but they must not be the only
permission boundary. Secrets, OAuth tokens, Codegeist API tokens, cloud
credentials, and provider refresh tokens must not be committed or stored in
normal artifact bytes.

## Tradeoffs

| Option | Decision | Reason |
| --- | --- | --- |
| Generic external OIDC | Choose first | Works with Keycloak, authentik, Google, and enterprise/self-hosted deployments without Codegeist hosting identity. |
| Multiple static OIDC providers | Implement in the first config and metadata model | Avoids a later migration from one hardcoded issuer while staying simpler than tenant-managed IdP onboarding. |
| Codegeist-owned opaque API tokens | Implement first | Keeps provider tokens out of Codegeist API authentication and supports revocation without committing to JWT signing yet. |
| In-memory metadata store | Implement only for this slice | Proves behavior before the metadata database technology is selected. Replace or back it with persistence in a later task. |
| Codegeist-hosted OAuth/OIDC server | Reject for first model | The product goal is external-account login plus Codegeist API tokens, not operating an identity provider. |
| Username/password | Defer | Adds password storage, reset, abuse, and account-security burden that external providers already handle. |
| Magic link | Defer | Requires email deliverability and account recovery policy; it does not help self-hosted OIDC deployments. |
| GitHub OAuth adapter | Defer or add after generic OIDC | Useful for developer login, but not the generic OIDC baseline needed for Keycloak/authentik. |
| Dynamic per-tenant IdP registration | Defer | Useful later for organizations, but unnecessary for a minimal first server deployment. |

## Later Task Boundaries

| Task | Boundary after this implementation |
| --- | --- |
| `T008_04` | Store artifact bytes separately from metadata and use `owner_account_id` rather than object paths as the authorization source. |
| `T008_05` | Check model-proxy entitlement and quota against account metadata before any hosted upstream call. |
| `T008_06` | Add one authenticated endpoint that consumes this task's Codegeist principal/token contracts, without adding local password login or a Codegeist authorization server. |
| `T008_07` | Add CLI login using the server auth contract and Codegeist API token; do not invent ad hoc CLI credentials. |

## Non-Goals

- Do not make live external identity-provider calls in tests.
- Do not hard-code authentik-only behavior into Codegeist auth source. It is the
  first local test provider, not the product's only provider.
- Do not host a Codegeist OAuth2/OIDC authorization server.
- Do not add username/password login, password reset, magic-link email delivery,
  passkeys, SAML, SCIM, or dynamic tenant-managed IdP registration.
- Do not implement organizations, invitations, shared artifacts, or team quotas.
- Do not implement the first authenticated product API endpoint; that remains
  `T008_06`.
- Do not select or add a durable database schema unless this task is explicitly
  rescoped again.
- Do not implement S3 artifact storage, object metadata persistence, model proxy
  calls, entitlements, quotas, usage accounting, billing, or CLI sync.
- Do not automatically merge users by email across providers.
- Do not store provider access tokens, provider refresh tokens, Codegeist API
  tokens, API keys, or cloud credentials in artifact bytes.

## Acceptance Criteria

- `app/codegeist/server` includes a tested Spring Security baseline where
  `GET /health` stays public and non-health routes are prepared for Codegeist
  authentication.
- Server configuration supports multiple statically configured external OIDC
  providers and rejects invalid provider definitions.
- The local OIDC test posture names authentik as the first real external issuer
  used by opt-in smoke or integration tests while keeping unit tests deterministic.
- The server has source-level metadata models for users, external identities,
  personal accounts, account memberships, and API tokens.
- Linking a first external identity creates a Codegeist user, a personal account,
  and an owner membership.
- Identity matching uses `providerId`, `issuer`, and `subject`; identical emails
  from different providers do not automatically merge users.
- Codegeist-issued API tokens are separate from upstream provider tokens. Plain
  tokens are returned only on issuance, only a secure hash or verifier is stored,
  and expired, revoked, or unknown tokens are rejected.
- Architecture docs describe the implemented server auth source, tests, and
  remaining non-goals.

## Verification

Run the focused server suite from `app/codegeist/server`:

```bash
task test
```

Run a repository diff whitespace check before finishing:

```bash
git --no-pager diff --check
```
