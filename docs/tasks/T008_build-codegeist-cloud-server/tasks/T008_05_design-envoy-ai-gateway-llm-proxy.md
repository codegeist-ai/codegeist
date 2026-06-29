# T008_05 Design Envoy AI Gateway LLM Proxy

Status: open

Parent: `../task.md`

## Goal

Define the first hosted LLM proxy contract for Codegeist Cloud using Envoy AI
Gateway as the internal AI gateway layer behind Codegeist Server.

Envoy AI Gateway must not be a public product API. It may route to OpenRouter,
OpenAI-compatible providers, self-hosted model backends, or later provider
targets, but only after Codegeist Server authenticates the user, checks product
policy, and forwards the request through a trusted internal path.

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
approval, usage controls, and safety gates. Local tests should use a fake or local
OpenAI-compatible backend where possible.

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

## Verification

```bash
git --no-pager diff --check
```
