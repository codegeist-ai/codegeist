# T008 Build Codegeist Cloud Server

Status: open

## Goal

Define and build Codegeist's own SaaS server as a second Spring Boot application.
The cloud server is not a local `opencode serve` equivalent. It is the hosted
Codegeist control plane that authenticated users can log into to access models,
stored agent assets, and later cloud-backed Codegeist workflows.

The first implementation direction is a Java/Spring workspace under
`app/codegeist` with a shared Maven parent POM, the existing CLI module in
`app/codegeist/cli`, and the SaaS server module in `app/codegeist/server`. The
server can grow into an authenticated cloud API and storage service that forwards
hosted model requests to an internal Envoy AI Gateway. OpenRouter is now only a
possible upstream behind Envoy AI Gateway; the first task slices must not make
paid or credentialed remote provider calls by default.

## Product Decision

Codegeist Cloud is a separate SaaS product surface:

- Users authenticate with Codegeist before they can use hosted models or storage.
- The local CLI login command is `codegeist login`; with no local server
  configuration it targets `https://codegeist.cloud`. This login is against a
  Codegeist server, not an LLM provider such as OpenAI or Ollama.
- Codegeist controls model access through entitlements, quotas, and usage policy.
- The first model gateway target is Envoy AI Gateway behind Codegeist Server. Only
  Codegeist-authenticated and Codegeist-authorized requests may reach Envoy
  responses.
- User and organization assets are stored in an S3-compatible bucket, with a
  metadata store for identity, ownership, indexing, permissions, and quotas.
- Commands, skills, rules, agent configuration, and related reusable agent assets
  are cloud-stored artifacts that local CLI/TUI clients can sync later.
- The local CLI runtime remains a client of the cloud server; the SaaS server does
  not duplicate the local file-tool, shell-tool, or terminal TUI implementation.

## User Use Case

A Codegeist user signs in and receives access to:

- Allowed LLM models through Codegeist-managed routing and policy.
- Personal or organization-scoped object storage for Codegeist artifacts.
- Cloud-synced commands, skills, rules, and agent configuration.
- Later shared sessions, usage history, billing visibility, and team workflows.

## Relationship To Existing Code

- `app/codegeist/pom.xml` is the shared Maven parent and aggregator for Codegeist
  Java applications.
- `app/codegeist/cli` remains the implemented local CLI application.
- `app/codegeist/server` is the second Spring Boot application for the hosted SaaS
  server.
- Current local provider configuration and chat runtime under `app/codegeist/cli`
  are not automatically lifted into the cloud server. Share code only after a
  focused task defines a stable library/module boundary.
- The existing `T006` provider matrix is useful source context for OpenRouter and
  OpenAI-compatible upstream behavior behind Envoy AI Gateway, but T008 owns cloud
  auth, entitlements, usage policy, SaaS storage, and hosted model access.
- The existing `T007` local session/tool harness stays local. T008 must not add a
  database, object store, or cloud sync back into T007.

## Cloud Architecture Boundaries

### Authentication And Tenancy

- Login is mandatory before model or storage access.
- `codegeist login` targets a Codegeist server. The default target is
  `https://codegeist.cloud`; future `codegeist login <server-id>` support may
  select another configured Codegeist server URL.
- Codegeist server login is separate from local LLM provider configuration. The
  CLI should store Codegeist server URLs and Codegeist-issued API tokens for cloud
  access, not model the server as `provider: codegeist`.
- The first local OIDC test provider is authentik. It stands in for later
  production providers such as Google, GitHub, Keycloak, authentik, or another
  external provider.
- The data model should support individual users and leave room for organizations
  or teams before shared artifacts are exposed.
- Authorization decisions must be stored in a metadata system, not inferred only
  from S3 object paths.

### Model Access And LLM Proxy

- Codegeist should present a stable model-access API to clients.
- Envoy AI Gateway is the first internal LLM gateway target. OpenRouter can be one
  upstream behind Envoy, but it is not the public Codegeist proxy contract.
- Default assumption: Codegeist owns upstream provider credentials and grants users
  access through account entitlements. Bring-your-own-key should be a separate
  later decision if needed.
- Usage accounting, quota checks, model allowlists, request size limits, and
  response streaming behavior are part of the cloud server boundary.
- Envoy AI Gateway may enforce gateway-level token or request limits from trusted
  Codegeist-set account/user headers, but Codegeist remains the source of truth for
  users, accounts, entitlements, model allowlists, sharing, and durable usage.
- Do not make live OpenRouter or other hosted LLM calls until a focused task
  defines credentials, no-cost or paid-test approval, and usage controls.

### S3-Compatible Artifact Storage

- The first local artifact-store target is MinIO as an S3-compatible bucket.
- Store artifact bytes in S3 and store metadata separately for lookup,
  permissions, versioning, ownership, checksums, sync state, and quotas.
- Candidate artifact families include commands, skills, rules, agent profiles,
  prompts, reusable context packs, and later session exports.
- Do not store raw API keys, OAuth tokens, cloud credentials, or secrets in S3
  artifacts unless a later security task defines encrypted secret storage.

### Client Sync

- Local Codegeist clients should eventually sync commands, skills, rules, and
  agent configuration with the cloud server.
- Local clients authenticate through `codegeist login` before sync. With no
  configured target, they use `https://codegeist.cloud`; configured alternatives
  are Codegeist server URLs.
- The first cloud task does not need full CLI sync. It should define enough API
  and storage boundaries so later CLI tasks can add login and sync safely.
- Conflict handling, offline edits, artifact versioning, and organization sharing
  should be explicit future tasks, not implicit side effects of the first server
  bootstrap.

## Proposed Child Tasks

Create child task files before implementation work starts.

- `T008_01_define-cloud-product-boundaries.md` - specify login, tenancy,
  entitlement, LLM proxy, storage, and client-sync boundaries without Java source.
- `T008_02_bootstrap-server-spring-project.md` - add the shared
  `app/codegeist/pom.xml` parent, minimal `app/codegeist/server` Spring Boot module,
  Taskfile, health endpoint, and tests.
- `T008_03_implement-oauth-provider-configuration.md` - implement the first static
  external OAuth2/OIDC provider configuration for Codegeist Server.
- `T008_04_design-s3-artifact-storage.md` - define S3 bucket layout, metadata
  records, artifact types, versioning, checksums, and local MinIO test posture.
- `T008_05_design-envoy-ai-gateway-llm-proxy.md` - define Envoy AI Gateway as the
  internal LLM gateway behind Codegeist authentication, entitlement checks, model
  allowlists, streaming, and usage accounting before live calls.
- `T008_06_add-first-authenticated-cloud-api.md` - implement the first
  authenticated API slice after auth, metadata, and storage boundaries are clear.
- `T008_07_add-cli-cloud-login-and-sync-slice.md` - add `codegeist login`, default
  to `https://codegeist.cloud` when no server is configured, store a
  Codegeist-issued API token for the selected server, and sync one artifact family.

## Parent Acceptance Criteria

- The repository has a tracked task plan for Codegeist Cloud as a SaaS server, not
  as a local OpenCode server clone.
- The task identifies `app/codegeist/pom.xml` as the shared Java parent POM and
  `app/codegeist/server` as the second Spring Boot application target beside
  `app/codegeist/cli`.
- Login, tenancy, entitlements, model access, S3 artifact storage, and client sync
  are treated as first-class product boundaries.
- CLI login is tracked as Codegeist-server authentication, not as a generic LLM
  provider login; the default server URL is `https://codegeist.cloud`.
- Envoy AI Gateway is captured as the first internal LLM gateway target while live
  remote provider calls remain blocked until a focused safe-call task exists.
- S3 storage is specified as object storage plus separate metadata and permission
  state, not as the sole source of authorization truth.
- Existing local CLI/runtime tasks remain separate from the cloud server scope.

## Non-Goals

- Do not implement a local `opencode serve` adapter.
- Do not copy OpenCode Go, OpenCode Zen, OpenCode's TypeScript server, or
  OpenCode's hosted provider product shape.
- Do not add live OpenRouter calls, paid provider calls, or hosted provider smoke
  tests in the parent task, including through Envoy AI Gateway.
- Do not add billing, payments, subscriptions, or invoice workflows before model
  access and usage accounting boundaries are defined.
- Do not store secrets in S3 artifacts.
- Do not move current CLI runtime classes into a shared module without a focused
  library-boundary task.
- Do not implement full command, skill, rule, session, or organization sync in the
  first server bootstrap.
- Do not model Codegeist Cloud login as an LLM-provider configuration path.

## Implementation-Readiness Questions

- How much of the authentik OIDC flow should be automated in the first local smoke
  stack, and which parts should stay manual or fixture-backed?
- Which account metadata fields are required now so individual users can work
  before organizations without blocking later organization accounts?
- Which Envoy AI Gateway credential and backend resources should be generated or
  documented first while Codegeist-owned upstream credentials remain the default?
- Which MinIO bucket and metadata records should the first artifact implementation
  create for commands, skills, rules, and agent configuration?
- Which metadata store should own users, artifact indexes, permissions, usage, and
  quotas?
- Should the public Codegeist model API mimic OpenAI `/v1/chat/completions`, expose
  a Codegeist-specific API, or support both while Envoy stays internal?
- Which artifact family should sync first: commands, skills, rules, or agent
  profiles?
- What exact local config shape should store additional Codegeist server URLs for
  `codegeist login <server-id>` beyond the built-in `https://codegeist.cloud`
  default?

## Verification

Parent-level task definition and documentation-only child tasks should run:

```bash
git --no-pager diff --check
```

Implementation child tasks should add an `app/codegeist/server/Taskfile.yml` and
use it for focused server verification, for example:

```bash
task test
```

Do not document live hosted-provider checks until the active child task includes
explicit credentials, cost policy, and usage limits.

## Planning Notes

- Use `docs/developer/specification/codegeist-opencode-parity.md` only for general
  behavior posture. This cloud server is a Codegeist SaaS product, not a
  translation of OpenCode's local headless server.
- Use `docs/tasks/T006_build-provider-configuration-feature/tasks/T006_03_define-provider-credential-and-account-strategy.md`
  for current OpenRouter/OpenAI-compatible provider evidence that may become an
  upstream behind Envoy AI Gateway.
- Use `docs/tests/README.md` before adding server tests or any hosted provider
  verification.
- Update `docs/developer/architecture/architecture.md` when Java source, server
  project layout, API behavior, storage behavior, or tests are implemented.
