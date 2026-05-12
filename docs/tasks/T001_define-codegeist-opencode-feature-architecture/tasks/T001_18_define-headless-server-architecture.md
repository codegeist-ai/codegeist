# T001_18 Define Headless Server Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the future headless server/API architecture for Codegeist.

This task specifies the server as a later adapter over the runtime. It does not
implement HTTP routes, OpenAPI, authentication, SDK generation, or streaming.

## Scope

- Map OpenCode server concepts to Spring Boot HTTP APIs.
- Define API ownership over sessions, events, approvals, and tool results.
- Identify authentication and OpenAPI/SDK implications.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode server/client architecture? | Spring Boot HTTP APIs exposing the same runtime used by CLI and future Vaadin. |
| Does the server duplicate runtime behavior? | No. It maps requests/responses and auth to runtime APIs. |
| What is MVP? | Later-stage architecture; CLI should not depend on server mode. |
| What is later? | Session APIs, event stream endpoint, approval APIs, tool result/diff APIs, provider/config APIs, OpenAPI/SDK, and auth. |

## Boundary Rules

- Server controllers/adapters call runtime services; they do not implement agent
  mode, provider, tool, permission, workspace, or storage behavior.
- Event streaming transport must preserve the event model semantics.
- Non-local exposure requires authentication and security review first.
- OpenAPI/SDK generation follows stable API contracts, not initial domain churn.
- Server routes project session/event/tool state into DTOs; they are not the
  domain source of truth.

## Implementation-Readiness Questions

- Which runtime ports must exist before server endpoints are meaningful?
- Which endpoints are needed before Vaadin or SDK work can start?
- Is localhost-only unauthenticated server mode acceptable for development?
- How are approval replies authenticated and audited?
- How are long-running prompt runs, cancellation, and streaming represented?

## Non-Goals

- Do not implement Spring Web routes, auth, OpenAPI, or SDK generation.
- Do not decide public API stability.
- Do not make server mode required for CLI MVP.
- Do not expose remote access without security decisions.

## Deliverable

Add a headless server architecture section to the parity document with API
families, runtime boundaries, event streaming, auth assumptions, OpenAPI/SDK
deferral, and Vaadin dependencies.

## Acceptance Criteria

- Server is a runtime client adapter, not a duplicate runtime.
- Auth requirements are called out before non-local exposure.
- OpenAPI/SDK generation remains later-stage unless the API exists.
- CLI and server use the same runtime semantics.
- Server DTOs are projections, not domain ownership.

## Verification

- Check consistency with Vaadin, storage, and security assumptions.

## Verification Result

- Specified headless server as a Spring Boot adapter over the shared runtime.

## Solution Note

Status: completed.

The solution pass added `## Headless Server Architecture` to
`docs/developer/codegeist-opencode-parity.md`. The section defines the future
Spring Boot HTTP adapter, API families, runtime boundaries, event streaming
posture, authentication assumptions, OpenAPI/SDK deferral, and Vaadin
dependencies.

No user decision is pending. Server controllers remain DTO/request-response
adapters over the runtime, and non-local exposure is blocked on authentication
and security review.

Verification passed with `git --no-pager diff --check`. A final review confirmed
the CLI can remain independent of server mode and no HTTP routes were added.
