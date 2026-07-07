# T008_04 Implement OIDC/STS MinIO Smoke Stack

Status: solved

Parent: `../task.md`

## Goal

Implement a local Docker Compose smoke stack that proves users can store Codegeist
artifact bytes in MinIO through external OIDC login and MinIO STS. This task is an
infrastructure and smoke-test slice only; it must not add Java Codegeist Server
artifact APIs or Java storage contracts.

The settled direction is that MinIO can act as an OIDC-backed S3 resource server:
users authenticate with the external OAuth2/OIDC provider, such as authentik, then
exchange a suitable web identity token through MinIO STS for short-lived S3
credentials. Codegeist Server still exists as the product API and metadata owner,
and later server workflows should access artifacts in the user's delegated context
instead of relying on long-lived user S3 keys.

The smoke stack should prove the core security flow: authentik issues a local OIDC
token for a test user, MinIO exchanges that token through STS for short-lived S3
credentials, the user can upload and read one allowed object, and the same temporary
credentials cannot write to another user's prefix.

## Scope

- Add a Docker Compose fixture under `scripts/tests/fixtures/` with authentik,
  authentik PostgreSQL, MinIO, and MinIO initialization services.
- Add authentik bootstrap data for a local test user, OIDC client/application, and a
  custom claim that maps the user to a MinIO policy.
- Add a MinIO policy for bucket `codegeist-artifacts` that allows the test user to
  read and write only the command-artifact prefix for that user's account.
- Add a PowerShell smoke script under `scripts/tests/` that starts the stack,
  obtains an OIDC token, calls MinIO STS `AssumeRoleWithWebIdentity`, performs an
  S3 upload and download with the temporary credentials, verifies the content, and
  verifies that a write to another account prefix is rejected.
- Add a Taskfile entrypoint if it keeps the smoke easy to run from the existing
  `app/codegeist/server` workflow.
- Keep Codegeist Java source, Codegeist REST APIs, durable metadata persistence,
  Codegeist-owned API tokens, and Codegeist Server delegated access out of this
  slice.

## Settled Storage Direction

Use MinIO for the first local storage stack. MinIO stands in for later production
S3-compatible storage and must not become the only supported storage assumption in
Codegeist metadata or authorization code.

Treat MinIO as a real resource server for direct S3 clients. It should trust the
configured external OIDC provider, not Codegeist-specific long-lived user S3 keys.
For local development, authentik is the first OIDC provider candidate because it is
already the planned local login provider for Codegeist Cloud. The same pattern must
also work with other external OIDC providers that MinIO can validate.

Codegeist Server does not need to become the primary OAuth2 authorization server in
this storage slice. Codegeist is still responsible for product metadata, API
behavior, sync, sharing, quotas, and later server-side operations, but the first
direct S3 credential issuance path can be MinIO STS backed by the external OIDC
provider.

The first bucket can be named `codegeist-artifacts` in local tests. Store only
artifact bytes there. Metadata for users, accounts, artifact ownership, sharing,
permissions, versions, checksums, sync state, quotas, and indexes stays in
Codegeist metadata, not in S3 object paths or bucket policies.

Candidate artifact families include:

- commands
- skills
- rules
- agent configuration
- prompts and reusable context packs
- later session exports

The first proved family in this task is command artifacts, as settled by
`T008_01_define-cloud-product-boundaries.md`. Other artifact families must fit the
same object-byte plus metadata model, but they should not be created or tested in
this smoke slice.

## Implementation Targets

Implement only these repo-owned files:

- `scripts/tests/fixtures/minio-oidc-storage/compose.yml` - local Docker Compose
  stack for authentik and MinIO.
- `scripts/tests/fixtures/minio-oidc-storage/authentik/blueprints/minio-oidc-smoke.yaml`
  - authentik blueprint or equivalent bootstrap file for the smoke OIDC application,
  claim mapping, and test user.
- `scripts/tests/fixtures/minio-oidc-storage/minio/policies/codegeist-artifacts-smoke.json`
  - MinIO policy scoped to the smoke user's command-artifact prefix.
- `scripts/tests/minio-oidc-storage-smoke.ps1` - non-interactive smoke entrypoint.

Use Dockerized clients inside the smoke where practical, for example `minio/mc` for
MinIO administration and `amazon/aws-cli` or an equivalent S3 client for SigV4
object operations with temporary credentials.

## OIDC And STS Access Flow

The direct S3 access flow should be documented as:

1. The user authenticates with the external OIDC provider, such as authentik.
2. The client obtains a JWT/web identity token that is valid for MinIO's configured
   OIDC trust and policy claims.
3. The client calls MinIO STS, for example `AssumeRoleWithWebIdentity`, with that
   token.
4. MinIO validates the token and returns temporary S3 credentials: access key,
   secret key, session token, and expiration.
5. The client uses those temporary credentials for normal S3 Signature Version 4
   requests to upload, download, list, or delete allowed objects.

The ordinary S3 API is not a raw Bearer-token API. A client should not expect to
send `Authorization: Bearer <oidc-token>` directly to object operations such as S3
`PutObject` or `GetObject`. The token exchange step through MinIO STS is the
credential bridge.

## Codegeist Server Delegation

Codegeist Server should later be able to use artifact bytes on behalf of the user.
The preferred direction is delegated short-lived access:

- Codegeist validates the user's Codegeist session or Codegeist API token.
- Codegeist maps the user to a durable Codegeist user id and personal account id.
- Codegeist obtains or receives a MinIO-usable user token, or uses a future OAuth
  token-exchange path through the external identity provider.
- Codegeist calls MinIO STS and receives short-lived S3 credentials scoped to that
  user/account.
- Codegeist reads or writes the relevant object only after checking Codegeist
  artifact metadata, ownership, sharing, entitlement, and quota records.

Using a broad service-account credential for user artifact reads and writes should
stay a fallback for a later explicitly approved internal workflow. It is simpler,
but it does not preserve the same user principal at the object-store layer. If a
service account is used later, Codegeist metadata and audit records must still prove
which user/account caused each operation.

The design must also capture the token-audience sharp edge: a token issued for the
Codegeist API may not automatically be acceptable to MinIO. The later auth task must
decide whether the external provider issues separate audiences, whether token
exchange is available, or whether Codegeist brokers MinIO STS credentials after
validating the user's Codegeist session.

## Bucket And Object Layout

The local physical bucket can be named `codegeist-artifacts`. A user-facing
"personal bucket" is a logical product concept, not necessarily a separate physical
S3 bucket for every user. Prefer one local bucket plus account-scoped object keys so
local tests and later production-compatible storage can use the same contract.

Candidate object key shape:

```text
accounts/<account-id>/artifacts/<artifact-family>/<artifact-id>/versions/<version-id>/content
```

The key shape is operational partitioning only. It can help MinIO policies enforce
coarse prefix-level access for direct S3 clients, but it is not the full Codegeist
permission system. Use opaque Codegeist ids rather than mutable display names in
keys. Display names, paths, artifact names, descriptions, tags, and sync aliases
belong in metadata.

Artifact versions should be immutable after write. A new upload creates a new
version id and object key, while metadata marks which version is current for a
given artifact. If bucket-native versioning is enabled later, Codegeist metadata
should still store the logical version id and may also store the storage provider's
native version id as an implementation reference.

## Metadata Records

S3 stores bytes. Codegeist metadata should own the product model. This task does
not implement metadata persistence, but the fixture should use an object key that
matches the planned metadata model:

- smoke user id or account id: `codegeist-smoke`
- bucket: `codegeist-artifacts`
- family: `commands`
- object key:
  `accounts/codegeist-smoke/artifacts/commands/smoke-command/versions/v1/content`

The smoke test should also try a forbidden key under `accounts/other-user/...` and
expect a denied write. This verifies that MinIO policy claims are doing real
storage-layer enforcement while leaving Codegeist metadata implementation for a
later task.

## Checksums And Sync Markers

Every stored artifact version should record a checksum in Codegeist metadata. Use a
stable checksum algorithm such as SHA-256 for product-level identity and integrity.
S3 ETags or provider-native checksums can be stored as additional storage metadata,
but they are not a portable product checksum because multipart uploads and
provider-specific behavior can change ETag semantics.

Sync markers should be metadata-backed. A later CLI sync slice should compare the
client's known artifact version, checksum, and sync marker with the server's current
metadata before deciding whether to upload, download, or report a conflict.

This task only needs to prove byte round-tripping in MinIO. Product-level checksum
and sync-marker persistence remain deferred because there is no metadata store in
this slice.

## Authorization Boundary

MinIO object keys may include account ids and artifact ids for operational
partitioning, but S3 paths are not the permission system. API handlers must decide
access from Codegeist-authenticated user/account context plus metadata-backed
ownership, membership, sharing, entitlement, and quota records.

Direct MinIO S3 access may additionally depend on MinIO policies derived from OIDC
claims. Those policies should be treated as storage-layer enforcement, not the
complete Codegeist product authorization model. Codegeist still owns artifact
metadata, search/index semantics, sharing state, quota accounting, and sync conflict
rules.

Normal artifact bytes must not contain API keys, OAuth tokens, Codegeist API
tokens, provider refresh tokens, cloud credentials, or generated secret material.
Encrypted secret storage remains a separate future security task.

## Test Stack Boundary

Local MinIO tests should use fixed test credentials supplied by the local test
stack and never real AWS credentials. Fast server unit tests should mock or fake
storage behavior. Real MinIO bucket/object checks and MinIO STS token exchange
checks belong in an opt-in integration or smoke task.

The local test stack should prefer authentik plus MinIO OIDC/STS wiring when a real
identity-backed storage check is needed. Broad unit tests must not require a live
authentik server, a live MinIO server, AWS credentials, or networked cloud storage.

## Deferred Boundaries

- Do not implement Java MinIO clients, AWS SDK wiring, Codegeist-owned bucket
  creation logic, or metadata persistence in this smoke task.
- Do not expose Codegeist Server artifact APIs until an authenticated API task
  defines the user/account/token contract and storage metadata persistence.
- Do not implement OAuth token exchange unless a later auth task confirms the
  external provider and MinIO token audience requirements.
- Do not add Java Codegeist source, Spring properties, controllers, repositories,
  model records, or tests in this task.
- Do not issue long-lived S3 access keys to normal users.
- Do not store provider API keys, OAuth refresh tokens, Codegeist API tokens, cloud
  credentials, or generated secrets in normal artifact bytes.

## Acceptance Criteria

- Docker Compose starts a local authentik and MinIO stack without external cloud
  credentials.
- authentik bootstraps a smoke user, OIDC application/client, and MinIO policy claim.
- MinIO is configured to trust authentik OIDC and map the smoke policy claim to the
  `codegeist-artifacts-smoke` policy.
- The smoke script obtains an OIDC token non-interactively from authentik by using
  a fixture app password with the client-credentials grant.
- The smoke script exchanges the token through MinIO STS for temporary S3
  credentials.
- The temporary credentials can upload and download
  `accounts/codegeist-smoke/artifacts/commands/smoke-command/versions/v1/content`.
- The temporary credentials cannot write under `accounts/other-user/`.
- Artifact bytes and metadata responsibilities are clearly separated.
- MinIO is documented as the first local S3-compatible test target and as an
  OIDC/STS-backed S3 resource server candidate.
- The direct user S3 flow uses external OIDC login, MinIO STS, and short-lived S3
  credentials rather than raw Bearer-token S3 object operations.
- Codegeist Server's later on-behalf-of-user access is documented as delegated
  short-lived access first, with service-account access deferred unless explicitly
  approved later.
- Permission decisions are metadata-backed, not path-only.
- The implementation excludes raw API keys, OAuth tokens, and cloud credentials from
  normal artifact bytes and from committed configuration.
- The task does not add Java Codegeist implementation.

## Verification

Run the smoke from `app/codegeist/server` when the Taskfile entrypoint exists:

```bash
task devenv-smoke
```

Or run the smoke script directly from the repository root:

```bash
pwsh -NoProfile -File scripts/tests/minio-oidc-storage-smoke.ps1
```

```bash
git --no-pager diff --check
```
