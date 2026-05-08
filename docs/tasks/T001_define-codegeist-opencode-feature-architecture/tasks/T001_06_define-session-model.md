# T001_06 Define Session Model

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the core session concepts for Codegeist.

## Scope

- Define session, turn, prompt, assistant response, tool call, approval, result,
  and error.
- Decide which fields are required in the first model.
- Identify what must be persisted later.

## Deliverable

Add a session model section to the parity document.

## Acceptance Criteria

- Session concepts are independent of CLI, server, and Vaadin clients.
- The model supports future event streaming and audit trails.
- Open questions about persistence are listed.

## Verification

- Check consistency with the event model child task.
