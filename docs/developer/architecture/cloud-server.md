# Codegeist Cloud Server

Current-state source guide for the Codegeist Cloud server module.

## Purpose

`app/codegeist/server` is the second Codegeist Spring Boot application. It is the
starting point for the hosted SaaS control plane where authenticated users can
later access Codegeist-managed models, cloud-stored agent assets, and sync
workflows.

The server is intentionally separate from the local CLI runtime under
`app/codegeist/cli`. It is not a local `opencode serve` clone and does not expose
local file tools, shell tools, terminal TUI behavior, or local session-store state.

## Build Layout

The Java workspace now uses a Maven parent/aggregator under `app/codegeist`:

```text
app/codegeist/
  pom.xml
  Taskfile.yml
  cli/
    pom.xml
    Taskfile.yml
  server/
    pom.xml
    Taskfile.yml
```

The parent POM owns shared versions for Java 25, Spring Boot 4.0.6, Lombok,
Spring AI, Spring Shell, Spring AI Agent Utils, and GraalVM build tools. The CLI
and server keep module-specific dependencies, executable jar names, and native
image names.

## Implemented Server Slice

Implemented files:

- `app/codegeist/server/pom.xml` - standalone server module under the shared Maven
  parent, including the server `native` profile.
- `app/codegeist/server/Taskfile.yml` - server-local `test`, `build`, `native`,
  `native-smoke`, MinIO OIDC storage smoke, Envoy AI Gateway to local Ollama smoke,
  Docker environment startup, and `run` entrypoints.
- `scripts/tests/server-native-smoke.ps1` - native server smoke harness that starts
  the native executable on a temporary localhost port, polls `/health`, reports
  startup timing, and stops the process.
- `scripts/tests/envoy-ai-gateway-smoke.ps1` - Docker-backed smoke harness that
  starts or reuses local Ollama through `task ollama-start`, connects it to the
  Compose network as `ollama`, bootstraps the authentik OIDC app, starts
  `oauth2-proxy` in front of Envoy AI Gateway from the optional AI Compose
  extension, verifies unauthenticated requests are rejected, and verifies one
  authenticated chat-completions request through Envoy to Ollama. With the manual
  startup mode, it also starts Open WebUI for browser testing through authentik.
- `scripts/tests/fixtures/minio-oidc-storage/compose.ai.yml` - optional Docker
  Compose file that adds Envoy AI Gateway and the `envoy-ai-auth-proxy`
  `oauth2-proxy` front door, plus Open WebUI as a manual local UI, to the base
  authentik plus MinIO fixture without mixing optional AI services into
  `compose.yml`.
- `scripts/tests/fixtures/minio-oidc-storage/authentik/blueprints/open-webui-smoke.yaml`
  - local authentik OAuth2/OIDC application and fixture password for the Open WebUI
  manual browser login flow.
- `CodegeistServerApplication` - Spring Boot server entrypoint with application
  name `codegeist-server`.
- `HealthController` - minimal unauthenticated `GET /health` endpoint returning
  `{"status":"ok"}`.
- `ai.codegeist.server.auth.config` - static external OAuth2/OIDC provider
  configuration model under `codegeist.auth.providers`, with Bean Validation for
  provider ids, supported `oidc` type, issuer URI, and client id.
- `application-local-authentik.yaml` - non-secret local authentik OIDC provider
  profile used as the first local test posture.
- `CodegeistServerApplicationTests` - proves the Spring context loads.
- `HealthControllerTest` - proves the health endpoint contract through a random
  local HTTP port.
- `CodegeistAuthPropertiesTest` and `LocalAuthentikProfileTest` - prove static
  OIDC provider config binding, validation, and the local authentik profile.

The server uses `spring-boot-starter-webmvc`, validation, and focused Spring Boot
tests. It does not add Actuator yet; the `/health` endpoint is a small bootstrap
contract, not a final operational readiness API.

## OAuth Provider Configuration

The current auth slice only binds static provider configuration for future browser
login endpoints. `CodegeistAuthProperties` owns deployment-local provider ids under
`codegeist.auth.providers`. Each `AuthProviderProperties` entry stores the generic
OIDC provider type, issuer URI, client id, and optional client secret. Provider ids
must match `[a-z][a-z0-9-]*` so later login URLs, logs, and metadata can reuse them
without an additional normalization step.

`application-local-authentik.yaml` is the first local OIDC posture. It configures
`authentik` as one generic external OIDC provider without making authentik a
hard-coded product dependency.

## Local Envoy AI Gateway Test Environment

The current source includes an optional local Envoy AI Gateway Compose extension,
but no Java server model-proxy endpoint yet. The extension stays in one additional
Compose file:

- `compose.yml` remains the base authentik plus MinIO OIDC/STS stack.
- `compose.ai.yml` adds the concrete `envoy-ai-gateway` standalone service and
  the `envoy-ai-auth-proxy` `oauth2-proxy` service. It also adds `open-webui` for
  manual browser testing. Every Compose service has a static bridge IP and the
  stack does not publish host ports; the manual UI opens at
  `http://172.30.198.40:8080`.

`scripts/tests/envoy-ai-gateway-smoke.ps1` owns the local-only gateway check. It
starts the existing persistent `codegeist-ollama` container through
`app/codegeist/cli` `task ollama-start`, connects that container to the Compose
network with alias `ollama`, applies the authentik Envoy AI Gateway OIDC
blueprint, starts Envoy AI Gateway with `OPENAI_BASE_URL=http://ollama:11434/v1`,
starts `oauth2-proxy` with authentik OIDC bearer-token validation, rejects an
unauthenticated OpenAI-compatible `/v1/chat/completions` request, and accepts the
same request with an authentik-issued bearer token and model `llama3.2:1b`.

`task devenv-ai-up` starts the same local AI stack plus Open WebUI for manual
inspection. Open `http://172.30.198.40:8080`, sign in with the fixture authentik
user `codegeist-smoke` / `codegeist-smoke-password`, and use the Open WebUI chat
against the Envoy-backed OpenAI-compatible provider. In this manual path,
Open WebUI is the authenticated local UI front door and calls raw Envoy only on the
internal Compose network; it does not turn the user's authentik token into a
per-user provider credential.

The local Compose network defaults to `172.30.198.0/24` and pins the browser-facing
fixture services to static bridge addresses: authentik `172.30.198.10`, MinIO
`172.30.198.20`, the Envoy auth proxy `172.30.198.31`, and Open WebUI
`172.30.198.40`. Override `CODEGEIST_DEVENV_SUBNET` and every
`CODEGEIST_*_DEVENV_IP` value together if that disposable range conflicts on a
developer machine.

Open WebUI's provider config includes a manual `llama3.2:1b` model id through
`OPENAI_API_CONFIGS` because the current standalone Envoy AI Gateway to Ollama path
serves chat completions but reports an empty OpenAI-compatible `/v1/models` list.
The disposable UI fixture also disables Open WebUI persistent config and points RAG
embedding settings at the existing internal Ollama endpoint to avoid unrelated
local SentenceTransformer startup work.

This path deliberately uses real local Ollama instead of a fake OpenAI server and
does not contact OpenRouter, OpenAI, Anthropic, or any hosted provider. Codegeist
Server still owns the future public API, authentication, entitlement, model
allowlist, quota, trusted header, and durable usage-accounting boundaries before
Envoy can serve real cloud users.

## Planned Cloud Login Boundary

The browser handoff and CLI command are not implemented in current source yet.

The CLI login command is `codegeist login`. When no local Codegeist server target
is configured, it uses `https://codegeist.cloud`. A later
`codegeist login <server-id>` form may select another configured Codegeist server
URL. The configuration for those targets should store Codegeist server URLs, not
LLM-provider settings.

The selected Codegeist server owns browser authentication. It uses its own static
external OAuth2/OIDC provider configuration, such as the local authentik test
provider or later Google, Keycloak, GitHub through an adapter, or another provider.
After a later successful browser login, the server should issue a Codegeist-owned
API token for CLI/API calls to that server. Provider access and refresh tokens from
the external identity provider are not Codegeist API credentials.

## Non-Goals In Current Source

- No live external identity-provider calls or browser login endpoints.
- No users, accounts, external-identity metadata, Codegeist API tokens, token
  validation, Spring Security route protection, or authenticated principals.
- No organizations, entitlements, quotas, usage accounting, or billing.
- No durable database-backed metadata store, S3 client, MinIO/AWS test harness, or
  object-store side effects.
- No Java Codegeist Server model proxy endpoint, OpenRouter call, hosted-provider
  credentials, cloud-user model routing, streaming API, usage accounting, or live
  remote provider calls.
- No `codegeist login`, configured Codegeist server-url store, browser callback,
  local token storage, or CLI cloud sync behavior.
- No shared Java module between CLI and server yet.

## Verification

Run the server tests from `app/codegeist/server`:

```bash
task test
```

Run both Java application test suites from `app/codegeist`:

```bash
task test
```

Build both GraalVM native executables from `app/codegeist`:

```bash
task native
```

Build and smoke-test the native server executable while measuring `/health`
startup time:

```bash
task server:native-smoke
```

Run the optional local Envoy AI Gateway to Ollama smoke from
`app/codegeist/server`:

```bash
task devenv-ai-smoke
```

Start the optional Open WebUI manual UI from `app/codegeist/server`:

```bash
task devenv-ai-up
```

Future server tasks should keep using `app/codegeist/server/Taskfile.yml` for
focused verification and update this document when browser login, authentication,
storage, metadata, or model-proxy behavior becomes implemented source.
