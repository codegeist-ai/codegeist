# T008_03 Design Auth And Tenant Model

Status: solved

Parent: `../task.md`

## Goal

Choose the first authentication strategy and define the account metadata model for
Codegeist Cloud users, future organizations, authorization decisions, and ownership.
This task is a documentation-only decision record; it does not add Java source,
Spring Security dependencies, database schema, or live identity-provider calls.

## Current Implemented State

`app/codegeist/server` currently exposes only the unauthenticated bootstrap
`GET /health` endpoint. The server has no Spring Security configuration, login
flow, users, accounts, API tokens, metadata database, organization support,
artifact authorization, or CLI cloud-login behavior yet.

## Auth Decision

Codegeist Cloud must not host its own OAuth2/OIDC authorization server in the
first auth model. Codegeist acts as an OAuth2/OIDC client, also called a relying
party, against externally operated identity providers. Users need an external
account before they can sign in to Codegeist.

Use generic external OIDC as the first auth strategy. The initial design should
support multiple statically configured OIDC providers per Codegeist server
deployment, for example a self-hosted Keycloak realm, an authentik application,
or Google. GitHub can be added later as an OAuth2 provider adapter, but it should
not be the core path because GitHub login is not the same as a normal generic
OIDC issuer for end-user authentication.

Codegeist issues its own API tokens after a successful external login. Provider
access tokens are only identity-proof inputs for the login callback. They must
not become Codegeist API credentials, must not be stored in artifact bytes, and
should be discarded after identity lookup unless a later security task defines
encrypted provider-token storage.

## Provider Configuration Contract

The first server configuration should model a map of externally managed OIDC
providers. Provider ids are Codegeist deployment-local identifiers used in URLs,
metadata, logs, and token records. Client secrets and other provider credentials
must come from environment variables or a later secret-manager integration, not
from artifact bytes or committed examples.

Illustrative shape:

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

The first implementation may require at least one configured provider. Dynamic
tenant-owned provider registration, user-entered OIDC issuers, organization-
specific identity providers, SAML, SCIM, passkeys, magic links, and
username/password accounts are deferred.

## Login And Token Flow

The first successful auth slice should prove this minimal flow:

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

The first token format can remain an implementation decision for `T008_06`, but
the metadata contract should allow revocation and expiry. Persist only a secure
hash or verifier for issued Codegeist API tokens, plus token metadata such as
owner, account, scopes, expiry, created time, last-used time, and revoked time.

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

Identity matching must use `provider_id`, `issuer`, and `subject`. Do not
automatically merge users only because two providers return the same email
address. Later account linking can bind multiple external identities to one
Codegeist user only through an explicit flow, such as a currently authenticated
user adding another provider identity or an admin-approved policy.

## Organization Posture

Do not implement organizations in the first schema slice. Keep the account model
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
| Multiple static OIDC providers | Support in the metadata/config design | Avoids a later migration from one hardcoded issuer while staying simpler than tenant-managed IdP onboarding. |
| Codegeist-hosted OAuth/OIDC server | Reject for first model | The product goal is external-account login plus Codegeist API tokens, not operating an identity provider. |
| Username/password | Defer | Adds password storage, reset, abuse, and account-security burden that external providers already handle. |
| Magic link | Defer | Requires email deliverability and account recovery policy; it does not help self-hosted OIDC deployments. |
| GitHub OAuth adapter | Defer or add after generic OIDC | Useful for developer login, but not the generic OIDC baseline needed for Keycloak/authentik. |
| Dynamic per-tenant IdP registration | Defer | Useful later for organizations, but unnecessary for a minimal first server deployment. |

## Later Task Boundaries

| Task | Boundary after this decision |
| --- | --- |
| `T008_04` | Store artifact bytes separately from metadata and use `owner_account_id` rather than object paths as the authorization source. |
| `T008_05` | Check model-proxy entitlement and quota against account metadata before any hosted upstream call. |
| `T008_06` | Implement one authenticated endpoint using configured external OIDC login and Codegeist-issued API tokens, without adding a local password database or authorization server. |
| `T008_07` | Add CLI login using the server auth contract and Codegeist API token; do not invent ad hoc CLI credentials. |

## Non-Goals

- Do not add Java source, Spring Security configuration, database schema, or
  provider credentials in this task.
- Do not host a Codegeist OAuth2/OIDC authorization server.
- Do not add username/password login, password reset, magic-link email delivery,
  passkeys, SAML, SCIM, or dynamic tenant-managed IdP registration.
- Do not implement organization membership, invitations, shared artifacts, or team
  quotas in the first auth slice.
- Do not automatically merge users by email across providers.
- Do not store provider access tokens, provider refresh tokens, Codegeist API
  tokens, API keys, or cloud credentials in artifact bytes.

## Acceptance Criteria

- A specific first auth strategy is recommended with tradeoffs.
- User and organization metadata contracts are documented without source stubs.
- Authorization is not inferred only from S3 object paths.
- Secrets and tokens are not committed or stored in artifact bytes.
- The design supports multiple statically configured external OIDC providers.
- Codegeist-issued API tokens are separated from upstream provider tokens.

## Verification

```bash
git --no-pager diff --check
```
