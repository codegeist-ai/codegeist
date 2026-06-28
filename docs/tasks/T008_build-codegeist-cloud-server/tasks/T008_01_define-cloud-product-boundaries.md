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

Evaluate hosted OAuth/OIDC-style login first. GitHub OAuth is the first provider
candidate because Codegeist is developer-facing and GitHub accounts are common in
the expected early user base.

`T008_03` owns the exact provider choice, callback flow, CLI-friendly login flow,
session lifetime, token storage, logout behavior, and test strategy. Magic-link
login and username/password accounts are deferred until a later product task needs
them.

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

OpenRouter remains the first likely hosted upstream profile because it is
OpenAI-compatible and supports broad routing. `T008_05` owns the exact proxy API,
streaming behavior, request limits, response usage mapping, model allowlist shape,
safe test posture, and any live-call approval. This task does not permit live
hosted LLM calls, paid provider checks, or remote smokes.

### Artifact Storage And Metadata Ownership

Use S3-compatible storage for artifact bytes and a separate metadata store for
identity, ownership, permissions, indexing, versions, checksums, sync state, and
quota accounting. S3 object keys are storage implementation details; authorization
must be metadata-backed and must not be inferred only from bucket names or object
path prefixes.

Normal artifact bytes must not contain API keys, OAuth tokens, provider secrets,
cloud credentials, or generated secret material. Encrypted secret storage is a
separate future security task, not part of the reusable artifact store boundary.

`T008_04` owns the first development target, such as MinIO or AWS S3, plus bucket
layout, object-key conventions, metadata records, checksums, versioning, sync
markers, and local no-credential test posture.

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
| `T008_03` | Choose the exact first auth strategy and define user, personal account, future organization, token, and authorization metadata contracts. |
| `T008_04` | Define the S3-compatible artifact byte store and metadata records, starting from command artifacts as the first sync family. |
| `T008_05` | Define the OpenRouter/OpenAI-compatible hosted model proxy, including entitlement checks, quotas, usage accounting, model allowlists, streaming, and safe-call gates. |
| `T008_06` | Implement one authenticated server API only after the auth, metadata, storage, and policy boundaries needed by that endpoint are specified. |
| `T008_07` | Add the first CLI cloud login and command-sync slice using the server auth and artifact contracts, without ad hoc credentials or live hosted-provider calls. |

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
