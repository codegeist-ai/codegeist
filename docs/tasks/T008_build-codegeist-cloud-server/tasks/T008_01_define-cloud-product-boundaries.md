# T008_01 Define Cloud Product Boundaries

Status: solved

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

## Current Implemented State

This task is a product-boundary decision record, not an implementation task.
`app/codegeist/server` currently exposes only the unauthenticated bootstrap
`GET /health` endpoint. Codegeist Cloud still has no login, tenants,
entitlements, quota store, usage accounting, object storage, metadata database,
LLM proxy, billing, or client sync behavior in source code.

## Boundary Decisions

### Login Direction

Use generic external OIDC as the first login direction so a deployment can target
Keycloak, authentik, Google, or another normal OIDC issuer without Codegeist
hosting identity itself. GitHub OAuth can be added later as a provider adapter,
but it is not the core first path.

The user-facing CLI login target is a Codegeist server, not an LLM provider.
`codegeist login` should use `https://codegeist.cloud` by default when no local
server target is configured. A later `codegeist login <server-id>` form can select
another configured Codegeist server URL. The server then offers its own configured
external OAuth2/OIDC providers in the browser flow and returns a Codegeist-owned
API token for later CLI/API calls.

`T008_03` owns only the first implementation of settled external-OIDC provider
configuration. Internal user/account metadata, Codegeist-owned API token storage,
and the security baseline are deferred to later focused auth and API tasks.
Magic-link login and username/password accounts are deferred until a later product
task needs them.

### Account And Tenancy Shape

Start with individual user accounts. Each authenticated user receives a durable
Codegeist user id and a personal account scope for model access, artifact storage,
quota accounting, and sync state.

Organizations are planned later, but the first metadata design must not bake
authorization into S3 object paths or user-only filenames. Metadata should model
an owner or account boundary so a later organization owner can be added without
changing the object-storage authorization rule. Organization creation, membership,
roles, invitations, shared artifacts, and team quotas stay out of the first
server implementation slice.

### Entitlements, Quotas, Usage, And Model Access

Codegeist Cloud owns hosted model-access policy. The default product boundary is
that Codegeist owns upstream provider credentials, then grants each account access
through Codegeist-managed entitlements, quotas, usage accounting, and model
allowlists. Bring-your-own-key is deferred and should be introduced only by a
focused security and product task.

Entitlements should answer which cloud capabilities an account may use, such as
hosted model proxy access or artifact sync. Quotas and usage accounting should be
tracked in Codegeist metadata for at least model requests, token or provider usage
when available, storage bytes, and stored artifact counts. Model allowlists should
be Codegeist policy, not an implicit copy of an upstream provider catalog.

Envoy AI Gateway is the first internal LLM gateway target. It sits behind
Codegeist Server and may later route to OpenRouter, OpenAI-compatible providers,
self-hosted model backends, or other upstreams. `T008_05` owns the exact internal
gateway contract, streaming behavior, request limits, response usage mapping,
trusted identity headers, model allowlist shape, safe test posture, and any
live-call approval. This task does not permit live hosted LLM calls, paid provider
checks, or remote smokes.

### Artifact Storage And Metadata Ownership

Use S3-compatible storage for artifact bytes and a separate metadata store for
identity, ownership, permissions, indexing, versions, checksums, sync state, and
quota accounting. S3 object keys are storage implementation details; authorization
must be metadata-backed and must not be inferred only from bucket names or object
path prefixes.

Normal artifact bytes must not contain API keys, OAuth tokens, provider secrets,
cloud credentials, or generated secret material. Encrypted secret storage is a
separate future security task, not part of the reusable artifact store boundary.

`T008_04` owns MinIO as the first local development target, plus bucket layout,
object-key conventions, metadata records, checksums, versioning, sync markers, and
local no-credential test posture.

### First Client-Sync Artifact Family

Target command artifacts first for later client sync. Commands are small,
file-like, user-visible, and valuable without requiring the broader lifecycle of
skills, rules, agent profiles, reusable context packs, or session exports.

The first sync boundary is personal user-scoped command storage and retrieval
through the cloud server. Conflict handling, offline edits, organization sharing,
bulk sync, profile-level sync, and session export sync are future tasks. The cloud
server must not duplicate local file tools, shell tools, local session storage, or
the terminal TUI; local CLI/TUI clients remain clients of the SaaS control plane.

## Later Task Boundaries

| Task | Allowed boundary after this decision |
| --- | --- |
| `T008_03` | Implement the first static external OAuth2/OIDC provider configuration, including local authentik posture, without user/account/token/security behavior. |
| `T008_04` | Define the S3-compatible artifact byte store and metadata records, starting from command artifacts as the first sync family. |
| `T008_05` | Define Envoy AI Gateway as the internal LLM gateway behind Codegeist auth, including entitlement checks, quotas, usage accounting, model allowlists, trusted headers, streaming, and safe-call gates. |
| `T008_06` | Implement one authenticated server API only after the auth, metadata, storage, and policy boundaries needed by that endpoint are specified. |
| `T008_07` | Add the first CLI cloud login and command-sync slice using `codegeist login`, default `https://codegeist.cloud`, configured Codegeist server URLs, the server auth contract, and artifact contracts, without ad hoc credentials or live hosted-provider calls. |

## Non-Goals

- Do not add Java source, database schema, object-store clients, auth libraries,
  hosted-provider clients, or CLI sync code in this task.
- Do not make live OpenRouter, OpenAI-compatible, or other hosted LLM calls.
- Do not add paid-provider checks, billing, subscriptions, invoices, or payment
  workflows.
- Do not store secrets in S3 artifact bytes.
- Do not implement organization membership, team sharing, or bring-your-own-key in
  the first cloud boundary slice.

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
