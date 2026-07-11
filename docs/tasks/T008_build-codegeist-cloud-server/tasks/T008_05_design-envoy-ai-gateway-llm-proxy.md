# T008_05 Design Envoy AI Gateway LLM Proxy

Status: solved

Parent: `../task.md`

## Goal

Define the first hosted LLM proxy contract for Codegeist Cloud using Envoy AI
Gateway as the internal AI gateway layer behind Codegeist Server.

Envoy AI Gateway must not be a public product API. It may route to OpenRouter,
OpenAI-compatible providers, self-hosted model backends, or later provider
targets, but only after Codegeist Server authenticates the user, checks product
policy, and forwards the request through a trusted internal path.

The first local implementation slice adds only a Docker Compose test environment
extension. It protects the local Envoy AI Gateway with `oauth2-proxy` and an
authentik-issued OIDC bearer token, then routes one OpenAI-compatible
chat-completions smoke request to the existing local Codegeist Ollama container
and fixed `llama3.2:1b` model. It does not add Codegeist Server Java proxy
endpoints, hosted-provider credentials, live remote-provider calls, or product
authorization code. It also adds an optional Open WebUI service for manual browser
testing: a user signs into Open WebUI through authentik, and Open WebUI calls
Envoy AI Gateway as its internal OpenAI-compatible provider.

## Security Boundary

External clients must call Codegeist Server, not Envoy AI Gateway directly. Envoy
AI Gateway may answer only requests that Codegeist Server forwards after the user
has successfully authenticated through the configured external OIDC flow, such as
the local authentik test provider, and after Codegeist has validated the
Codegeist-owned API token or server session.

Required flow:

1. The user signs in through Codegeist Server with an external OIDC provider. The
   local test provider is authentik; production providers can later include
   Google, GitHub through an adapter, Keycloak, authentik, or another supported
   provider.
2. Codegeist creates or finds the Codegeist user, personal account, membership,
   and Codegeist API token.
3. The client calls the Codegeist AI API with the Codegeist token.
4. Codegeist validates the token and checks account membership, entitlement, model
   allowlist, quota posture, request size, and any policy gates.
5. Codegeist forwards the request to the internal Envoy AI Gateway route and sets
   trusted headers such as `x-codegeist-user-id`, `x-codegeist-account-id`,
   `x-tenant-id`, `x-codegeist-request-id`, and the selected model alias.
6. Envoy AI Gateway routes to the selected upstream, applies configured gateway
   policy, emits AI usage metadata, and returns the response through Codegeist.

Envoy must not trust `x-codegeist-*` or `x-tenant-id` headers supplied by external
clients. The first safe deployment posture is to make Envoy AI Gateway reachable
only on an internal network path from Codegeist Server. Later hardening can add
mTLS, an internal API key, Envoy Gateway `SecurityPolicy`, or external auth so
only Codegeist Server can use the gateway.

## Local Test Environment

The local Docker Compose environment uses one additional Compose file so optional
AI-gateway services stay separate from the base authentik plus MinIO fixture:

- `scripts/tests/fixtures/minio-oidc-storage/compose.yml` remains the base
  authentik and MinIO OIDC/STS stack.
- `scripts/tests/fixtures/minio-oidc-storage/compose.ai.yml` defines the optional
  `envoy-ai-gateway`, `envoy-ai-auth-proxy`, and `open-webui` services. The stack
  assigns fixed bridge IPs to every Compose service and does not publish host
  ports. The auth proxy uses `172.30.198.31:4180`, Open WebUI uses
  `172.30.198.40:8080`, and Open WebUI still calls Envoy's internal
  OpenAI-compatible URL.
- `scripts/tests/fixtures/minio-oidc-storage/authentik/blueprints/envoy-ai-gateway-smoke.yaml`
  defines the local authentik OAuth2/OIDC application used only by the AI gateway
  smoke.
- `scripts/tests/fixtures/minio-oidc-storage/authentik/blueprints/open-webui-smoke.yaml`
  defines the local authentik OAuth2/OIDC application and fixture user password
  used only by the manual Open WebUI flow.
- `scripts/tests/envoy-ai-gateway-smoke.ps1` starts or reuses the existing
  `codegeist-ollama` container through `task ollama-start`, connects it to the
  Compose network with alias `ollama`, bootstraps the authentik OIDC app, starts
  Envoy AI Gateway plus `oauth2-proxy` through `compose.yml` plus `compose.ai.yml`,
  verifies an unauthenticated chat-completions request is rejected, and verifies an
  authenticated request reaches Envoy and Ollama. With `-StartAllServices` and
  `-StartOnly`, the same script starts Open WebUI and waits for `/health` for
  manual browser testing.

The Compose network uses the default subnet `172.30.198.0/24`, with static
service addresses such as authentik `172.30.198.10`, MinIO `172.30.198.20`, the
Envoy auth proxy `172.30.198.31`, and Open WebUI `172.30.198.40`. If that
disposable range conflicts locally, override `CODEGEIST_DEVENV_SUBNET` and every
`CODEGEIST_*_DEVENV_IP` value together.

Open WebUI also sets `OPENAI_API_CONFIGS` with manual `model_ids: ["llama3.2:1b"]`
because the current standalone Envoy AI Gateway smoke can route chat completions
but returns an empty `/v1/models` list for the Ollama backend.
The disposable Open WebUI fixture disables persistent config and points RAG
embedding settings at the existing internal Ollama endpoint so startup does not
block on local SentenceTransformer model preparation that is irrelevant to this
manual UI smoke.

The Envoy service uses:

```yaml
OPENAI_BASE_URL: http://ollama:11434/v1
OPENAI_API_KEY: unused
```

The smoke intentionally uses real local Ollama rather than a fake OpenAI server.
This keeps the local gateway path close to actual provider behavior while still
avoiding hosted providers and costs.

Run the targeted smoke from `app/codegeist/server`:

```bash
task devenv-ai-smoke
```

For manual inspection, start the combined local stack from `app/codegeist/server`:

```bash
task devenv-ai-up
```

Then open `http://172.30.198.40:8080` and sign in through authentik with the
local fixture account `codegeist-smoke` / `codegeist-smoke-password`.

## Authenticated Gateway Front Door

The local test environment puts an OIDC-authenticated front door in front of Envoy
AI Gateway so the local smoke exercises the same broad security shape as the
planned cloud deployment:

```text
smoke client
  -> envoy-ai-auth-proxy (oauth2-proxy, static bridge IP 172.30.198.31:4180)
    -> envoy-ai-gateway (internal only)
      -> codegeist-ollama (Docker network alias ollama)
```

The manual Open WebUI path is separate from the bearer-token API smoke:

```text
browser
  -> 172.30.198.40:8080
    -> 172.30.198.10:9000 for OIDC login
    -> open-webui container session
      -> envoy-ai-gateway:1975/v1 as OpenAI-compatible provider
        -> codegeist-ollama (Docker network alias ollama)
```

Open WebUI is the local authenticated UI front door in that manual flow. It does
not forward a per-user authentik token to Envoy as a provider API key in this
Compose slice. That is acceptable only for local UI inspection because the Compose
stack has static bridge addresses but no host port forwarding; production
Codegeist Server still owns the user, account, entitlement, trusted-header, and
usage-accounting boundary before Envoy.

Use `oauth2-proxy` as the local reverse proxy because the current standalone
`aigw run` smoke is focused on Envoy-to-Ollama routing, while `oauth2-proxy` can
validate authentik-issued OIDC bearer tokens for non-interactive API smokes. This
does not replace the future Codegeist Server auth and entitlement layer; it only
proves that the local Envoy path can be protected by the existing authentik test
provider.

The implemented `oauth2-proxy` shape is:

```text
--http-address=0.0.0.0:4180
--provider=oidc
--oidc-issuer-url=http://authentik-server:9000/application/o/envoy-ai-gateway-smoke/
--insecure-oidc-skip-issuer-verification=true
--insecure-oidc-allow-unverified-email=true
--client-id=envoy-ai-gateway-smoke-client
--client-secret=codegeist-smoke-envoy-ai-gateway-client-secret
--cookie-secret=codegeist-smoke-cookie-secret-32
--cookie-secure=false
--email-domain=*
--upstream=http://envoy-ai-gateway:1975/
--reverse-proxy=true
--skip-jwt-bearer-tokens=true
--bearer-token-login-fallback=false
```

The two `insecure-oidc-*` flags are local fixture constraints, not production
guidance: authentik's application discovery endpoint is used for the app-specific
token and JWKS metadata, but the token issuer is the local authentik root URL; the
fixture user also does not have a verified email claim.

Implemented smoke flow:

1. Start or reuse local Ollama with `task ollama-start` and fixed model
   `llama3.2:1b`.
2. Start `compose.yml` plus `compose.ai.yml` so authentik, MinIO, oauth2-proxy,
   Envoy AI Gateway, and the Compose network are available.
3. Connect the existing `codegeist-ollama` container to the Compose network with
   alias `ollama`.
4. Wait for authentik health and apply the shipped OAuth2 base blueprints plus the
   Envoy AI Gateway smoke blueprint.
5. Wait for OIDC discovery at the Envoy AI Gateway authentik application issuer.
6. Obtain a fixture token non-interactively through authentik using local-only test
   credentials.
7. Send `POST /v1/chat/completions` without a bearer token through the auth proxy
   and require rejection.
8. Send the same request with the authentik bearer token and require an Ollama
   response through Envoy AI Gateway.
9. Clean up the Compose stack and disconnect Ollama from the disposable network;
   the persistent Ollama container itself stays available for other local tests.

Implemented authenticated gateway criteria:

- The fixture uses static bridge addresses and no host port forwarding; the API
  smoke reaches `envoy-ai-auth-proxy:4180`, not the raw Envoy AI Gateway service.
- Unauthenticated requests do not receive Envoy/Ollama chat-completions responses.
- authentik-issued bearer tokens allow the request through oauth2-proxy to Envoy
  and then local Ollama.
- `task devenv-ai-up` starts Open WebUI at `http://172.30.198.40:8080` for a
  manual authentik-login UI test against Envoy's internal provider URL.
- No fake OpenAI-compatible backend, hosted-provider call, or paid provider path is
  introduced.
- The Java Codegeist Server still does not implement the model proxy in this slice;
  auth proxying remains local Compose test infrastructure.

## Envoy AI Gateway Responsibilities

Envoy AI Gateway can own gateway-infrastructure behavior:

- Kubernetes/Gateway API integration through Envoy Gateway.
- Unified AI routes through `AIGatewayRoute`.
- Provider/backend definitions through `AIServiceBackend`.
- Upstream provider credentials through `BackendSecurityPolicy` and Kubernetes
  secrets.
- Token-aware or request-aware gateway limits through Envoy Gateway
  `BackendTrafficPolicy`.
- AI usage metadata such as input tokens, output tokens, total tokens, backend
  name, and response model when the upstream response exposes those fields.
- Access logs or telemetry that Codegeist can later ingest into durable usage
  accounting.

## Codegeist Responsibilities

Codegeist remains the source of truth for product policy:

- Codegeist users, external identities, accounts, memberships, and future
  organizations.
- Codegeist API tokens and sessions.
- Entitlements, model allowlists, bring-your-own-key policy, account quotas, and
  feature access.
- Artifact ownership and sharing.
- Durable usage and billing-grade history.
- The public API contract seen by CLI, TUI, web, and future SDK clients.

Envoy token rate limiting is useful as an enforcement layer, but it does not
replace Codegeist metadata or product accounting. Codegeist should pass a trusted
account or tenant id to Envoy for rate-limit buckets, then persist durable usage
from Codegeist-side observations, Envoy access logs, or a later usage ingestion
pipeline.

## API Shape

Codegeist may expose a Codegeist-specific model API, an OpenAI-compatible API, or
both through separate endpoints. In every case, Codegeist owns the public API and
Envoy remains internal.

The first implementation should prefer a narrow Codegeist API unless a focused
task explicitly needs OpenAI-compatible client parity. The Envoy route can still
target OpenAI-compatible upstreams internally.

## Provider And Cost Safety

OpenRouter is now a possible upstream behind Envoy AI Gateway, not the name of the
Codegeist proxy. Other OpenAI-compatible or self-hosted backends can be added by
creating Envoy AI Gateway backends and Codegeist model-policy entries.

Do not make live OpenRouter, OpenAI, Anthropic, Bedrock, Google, or other hosted
provider calls until a focused task defines credentials, no-cost or paid-test
approval, usage controls, and safety gates. The current local smoke uses local
Ollama through Envoy AI Gateway and must not use a fake OpenAI-compatible server or
hosted provider call.

## Acceptance Criteria

- Envoy AI Gateway is documented as the first internal LLM gateway target.
- Envoy AI Gateway is explicitly not a public unauthenticated client API.
- Only Codegeist-authenticated and Codegeist-authorized users may reach Envoy AI
  Gateway responses through Codegeist Server.
- Codegeist remains the source of truth for users, accounts, entitlements, model
  allowlists, quotas, sharing, and durable usage history.
- Envoy AI Gateway responsibilities are limited to gateway routing, upstream
  credentials, backend policy, usage metadata, telemetry, and gateway-level limits.
- OpenRouter is treated as a possible upstream behind Envoy AI Gateway, not as
  permission to make live calls.
- Remote calls remain blocked until credentials, costs, usage controls, and safety
  gates are explicit.
- Local Compose AI gateway support is isolated in `compose.ai.yml`, protects the
  local gateway with authentik-backed `oauth2-proxy`, and targets the existing
  local Ollama container, not a fake OpenAI-compatible server.
- `task devenv-ai-smoke` proves unauthenticated gateway requests are rejected and
  authenticated OpenAI-compatible chat-completions requests route to local Ollama
  with no hosted provider calls.
- `task devenv-ai-up` starts the optional Open WebUI manual UI, where browser login
  goes through authentik and Open WebUI uses Envoy AI Gateway as its
  OpenAI-compatible provider on the internal Compose network.

## Verification

```bash
git --no-pager diff --check
docker compose --project-name codegeist-ai-gateway-check --file scripts/tests/fixtures/minio-oidc-storage/compose.yml --file scripts/tests/fixtures/minio-oidc-storage/compose.ai.yml config
task devenv-ai-smoke
task devenv-ai-up
```
