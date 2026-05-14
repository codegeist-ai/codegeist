# T001_22 Define MVP Cut

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the smallest useful Codegeist MVP after feature mapping is complete.

This task specifies MVP scope only. It does not implement the MVP, create
backlog tasks by default, or pull later-stage clients into the first release.

## Scope

- Select MVP features from the feature matrix.
- Separate later-stage and explicitly out-of-scope features.
- Define why the chosen MVP is sufficient for the first implementation phase.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the smallest useful OpenCode-inspired Codegeist? | A CLI-first Java runtime that can run prompt sessions with modes, context, provider calls, events, permissions, and at least minimal tools. |
| What should not be in MVP by default? | Full-screen TUI, Vaadin, headless server, plugin marketplace, broad provider ecosystem, LSP, desktop, SDK, and advanced storage unless required by the matrix. |
| What must MVP preserve for future parity? | Runtime/client separation, session/event/tool/permission/workspace/provider boundaries, and native-aware packaging choices. |
| How is MVP justified? | Each included feature must unlock a usable workflow or protect a foundational architecture boundary. |

## MVP Selection Rules

- Include only features needed for a coherent first CLI workflow.
- Prefer one verified provider path over broad provider coverage.
- Prefer deterministic context and simple storage over complex retrieval.
- Prefer built-in tools before PF4J/JBang extension execution.
- Keep server/Vaadin ready architecturally but deferred unless needed by the first
  workflow.
- Mark each deferred feature with a concrete reason.

## Implementation-Readiness Questions

- Can the MVP be implemented incrementally from current `app/codegeist/cli`?
- Does every MVP feature map to an existing architecture section?
- Does every MVP side effect have permission and workspace coverage?
- Is the MVP small enough for the next three to five implementation tasks?
- Are later-stage features deferred without blocking future architecture?

## Non-Goals

- Do not implement MVP features.
- Do not create the implementation backlog here; that belongs to `T001_25`.
- Do not include a feature only for parity optics if it is not needed in the
  first usable workflow.

## Deliverable

Add an MVP cut section to the parity document with included features, deferred
features, out-of-scope items, rationale, dependencies, and verification mapping.

## Acceptance Criteria

- MVP scope is small enough to implement incrementally.
- MVP includes the minimum runtime concepts needed for future parity.
- Deferred features have a clear reason for deferral.
- MVP can start from the current Spring Boot/Spring Shell bootstrap.
- Each MVP item can map to an implementation backlog candidate.

## Verification

- Check that every MVP item can map to a follow-up implementation task.

## Verification Result

- Specified MVP selection rules, migration framing, and implementation-readiness
  criteria.

## Solution Note

Status: completed.

The solution pass added `## MVP Cut` to
`docs/developer/codegeist-opencode-parity.md`. The section defines the smallest
CLI-first MVP, included features, deferred features with reasons, out-of-scope
items, rationale, dependencies, and verification mapping.

No user decision is pending. The MVP can start from the current Spring
Boot/Spring Shell bootstrap and preserves runtime/client separation,
session/event/tool/permission/workspace/provider boundaries, and native-aware
packaging choices.

Verification passed with `git --no-pager diff --check`. A final review confirmed
each MVP item can map to an implementation backlog candidate without pulling in
server, Vaadin, PF4J, broad provider parity, or desktop work.
