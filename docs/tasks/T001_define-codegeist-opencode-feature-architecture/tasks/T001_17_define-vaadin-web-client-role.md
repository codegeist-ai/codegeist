# T001_17 Define Vaadin Web Client Role

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Vaadin's role as a future Codegeist web client.

## Scope

- Define which session, event, approval, and tool-result views Vaadin should
  eventually expose.
- Keep Vaadin separate from runtime orchestration.
- Identify server/API dependencies for a future web client.

## Deliverable

Add a Vaadin web client role section to the parity document.

## Acceptance Criteria

- Vaadin is treated as a client surface.
- Runtime and permission logic remain outside Vaadin views.
- Web UI is later-stage unless explicitly pulled into the MVP.

## Verification

- Check consistency with headless server and event model tasks.
