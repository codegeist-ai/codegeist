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
server can grow into an authenticated LLM proxy and storage service. OpenRouter is
the first likely upstream provider profile because it is OpenAI-compatible and
supports broad model routing, but the first task slices must not make paid or
credentialed remote provider calls by default.

## Product Decision

Codegeist Cloud is a separate SaaS product surface:

- Users authenticate with Codegeist before they can use hosted models or storage.
- Codegeist controls model access through entitlements, quotas, and usage policy.
- The first model backend should be an LLM proxy that can route to OpenRouter or
  another OpenAI-compatible upstream.
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
  OpenAI-compatible routing, but T008 owns cloud auth, entitlements, usage policy,
  SaaS storage, and hosted model access.
- The existing `T007` local session/tool harness stays local. T008 must not add a
  database, object store, or cloud sync back into T007.

## Cloud Architecture Boundaries

### Authentication And Tenancy

- Login is mandatory before model or storage access.
- The first design must decide the initial login method: OAuth, magic link,
  username/password, or another hosted identity provider.
- The data model should support individual users and leave room for organizations
  or teams before shared artifacts are exposed.
- Authorization decisions must be stored in a metadata system, not inferred only
  from S3 object paths.

### Model Access And LLM Proxy

- Codegeist should present a stable model-access API to clients.
- OpenRouter is the first likely upstream because it can act as an
  OpenAI-compatible gateway to many models.
- Default assumption: Codegeist owns upstream provider credentials and grants users
  access through account entitlements. Bring-your-own-key should be a separate
  later decision if needed.
- Usage accounting, quota checks, model allowlists, request size limits, and
  response streaming behavior are part of the cloud server boundary.
- Do not make live OpenRouter or hosted LLM calls until a focused task defines
  credentials, no-cost or paid-test approval, and usage controls.

### S3-Compatible Artifact Storage

- The hosted artifact store is an S3-compatible bucket.
- Store artifact bytes in S3 and store metadata separately for lookup,
  permissions, versioning, ownership, checksums, sync state, and quotas.
- Candidate artifact families include commands, skills, rules, agent profiles,
  prompts, reusable context packs, and later session exports.
- Do not store raw API keys, OAuth tokens, cloud credentials, or secrets in S3
  artifacts unless a later security task defines encrypted secret storage.

### Client Sync

- Local Codegeist clients should eventually sync commands, skills, rules, and
  agent configuration with the cloud server.
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
- `T008_03_design-auth-and-tenant-model.md` - choose the initial identity provider
  strategy and define user/organization/account metadata contracts.
- `T008_04_design-s3-artifact-storage.md` - define S3 bucket layout, metadata
  records, artifact types, versioning, checksums, and local MinIO/AWS test posture.
- `T008_05_design-openrouter-llm-proxy.md` - define OpenRouter/OpenAI-compatible
  proxy contracts, entitlement checks, model allowlists, streaming, and usage
  accounting before live calls.
- `T008_06_add-first-authenticated-cloud-api.md` - implement the first
  authenticated API slice after auth, metadata, and storage boundaries are clear.
- `T008_07_add-cli-cloud-login-and-sync-slice.md` - add the first local client flow
  that logs in and syncs one artifact family.

## Parent Acceptance Criteria

- The repository has a tracked task plan for Codegeist Cloud as a SaaS server, not
  as a local OpenCode server clone.
- The task identifies `app/codegeist/pom.xml` as the shared Java parent POM and
  `app/codegeist/server` as the second Spring Boot application target beside
  `app/codegeist/cli`.
- Login, tenancy, entitlements, model access, S3 artifact storage, and client sync
  are treated as first-class product boundaries.
- OpenRouter is captured as the first likely LLM proxy upstream while remote calls
  remain blocked until a focused safe-call task exists.
- S3 storage is specified as object storage plus separate metadata and permission
  state, not as the sole source of authorization truth.
- Existing local CLI/runtime tasks remain separate from the cloud server scope.

## Non-Goals

- Do not implement a local `opencode serve` adapter.
- Do not copy OpenCode Go, OpenCode Zen, OpenCode's TypeScript server, or
  OpenCode's hosted provider product shape.
- Do not add live OpenRouter calls, paid provider calls, or hosted provider smoke
  tests in the parent task.
- Do not add billing, payments, subscriptions, or invoice workflows before model
  access and usage accounting boundaries are defined.
- Do not store secrets in S3 artifacts.
- Do not move current CLI runtime classes into a shared module without a focused
  library-boundary task.
- Do not implement full command, skill, rule, session, or organization sync in the
  first server bootstrap.

## Implementation-Readiness Questions

- Which login method is first: GitHub OAuth, Google OAuth, magic link, hosted auth,
  or username/password?
- Should the first account model support only users, or users plus organizations
  from the start?
- Will Codegeist always own upstream model-provider credentials, or is
  bring-your-own-key planned for some accounts?
- Which S3-compatible target is first for development and tests: MinIO, AWS S3, or
  another provider?
- Which metadata store should own users, artifact indexes, permissions, usage, and
  quotas?
- Should the public model proxy API mimic OpenAI `/v1/chat/completions`, expose a
  Codegeist-specific API, or support both through separate endpoints?
- Which artifact family should sync first: commands, skills, rules, or agent
  profiles?

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
  for current OpenRouter/OpenAI-compatible provider evidence.
- Use `docs/tests/README.md` before adding server tests or any hosted provider
  verification.
- Update `docs/developer/architecture/architecture.md` when Java source, server
  project layout, API behavior, storage behavior, or tests are implemented.
