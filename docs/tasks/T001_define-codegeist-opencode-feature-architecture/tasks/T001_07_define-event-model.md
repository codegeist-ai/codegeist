# T001_07 Define Event Model

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the event types emitted during a Codegeist session.

## Scope

- Define user input, assistant output, tool request, permission request, tool
  result, provider response, warning, and error events.
- Decide which events are user-visible.
- Decide which events are audit-relevant.

## Deliverable

Add an event model section to the parity document.

## Acceptance Criteria

- Event types are explicit and typed conceptually.
- Events can support CLI output now and server/Vaadin streaming later.
- Permission and tool events are represented.

## Verification

- Check that the end-to-end prompt flow can be expressed with these events.
