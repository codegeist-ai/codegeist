# T001_17 Define Vaadin Web Client Role

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Vaadin's role as a future Codegeist web client.

This task specifies Vaadin as a later client surface only. It does not implement
views, server APIs, authentication, push transport, or runtime orchestration.

## Scope

- Define which session, event, approval, and tool-result views Vaadin should
  eventually expose.
- Keep Vaadin separate from runtime orchestration.
- Identify server/API dependencies for a future web client.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode web UI? | A future Vaadin client presenting runtime sessions, events, approvals, and tool results. |
| Does Vaadin own runtime behavior? | No. It renders runtime state and submits runtime requests through APIs/ports. |
| What is MVP? | Later-stage; architecture should not block it, but CLI comes first. |
| What is later? | Session browser, event stream view, approval UI, tool result/diff views, settings/provider UI, and server auth integration. |

## Boundary Rules

- Vaadin views render session/event projections; they do not mutate session
  internals.
- Approval screens collect decisions; permission service owns policy and audit.
- Vaadin should consume the same conceptual events as CLI and server clients.
- Runtime can be in-process at first only if the same contracts can later be
  exposed over HTTP.
- Non-local web access requires server authentication and security review.

## Implementation-Readiness Questions

- Which runtime API shape must exist before Vaadin can be useful?
- Should Vaadin connect in-process first or only through the headless API?
- How will event streaming/push be represented without changing event semantics?
- Which views are essential: sessions, current turn, approvals, tool results,
  diffs, provider config, context inspection?
- Which security requirements block browser exposure?

## Non-Goals

- Do not implement Vaadin views.
- Do not choose push/WebSocket/SSE transport.
- Do not expose server mode without auth decisions.
- Do not make Vaadin part of the first CLI MVP unless `T001_22` changes that.

## Deliverable

Add a Vaadin web client role section to the parity document with view scope,
runtime/client boundaries, event/approval handling, server dependencies, and
security assumptions.

## Acceptance Criteria

- Vaadin is treated as a client surface.
- Runtime and permission logic remain outside Vaadin views.
- Web UI is later-stage unless explicitly pulled into the MVP.
- Vaadin consumes runtime events/projections instead of owning state.
- Auth/server dependencies are identified.

## Verification

- Check consistency with headless server and event model tasks.

## Verification Result

- Specified Vaadin as a future Java web client over runtime events and APIs.

## Solution Note

Status: completed.

The solution pass added `## Vaadin Web Client Role` to
`docs/developer/specification/codegeist-opencode-parity.md`. The section defines Vaadin as a
future client surface, lists likely session/event/approval/tool/context/provider
views, preserves runtime and permission ownership, and identifies server/API/auth
dependencies.

No user decision is pending. Vaadin remains later-stage unless the MVP cut is
changed, and non-local browser access requires server authentication and security
review.

Verification passed with `git --no-pager diff --check`. A final review confirmed
Vaadin consumes runtime projections/events and does not own session or permission
state.
