# T001_23 Define End-To-End Prompt Flow

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the primary Codegeist prompt flow from user input to output.

## Scope

- Describe user input, CLI/shell handling, session creation, agent mode,
  context loading, provider call, tool request, permission check, tool result,
  event emission, and user-visible output.
- Mark which steps are MVP and which are later-stage.

## Deliverable

Add an end-to-end prompt flow section to the parity document.

## Acceptance Criteria

- The flow uses the session, event, tool, permission, and provider concepts.
- The flow can be implemented first from the CLI without blocking server/Vaadin
  clients later.
- Later-stage steps are clearly labeled.

## Verification

- Check consistency with the MVP cut and component model.
