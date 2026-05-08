# T001_18 Define Headless Server Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the future headless server/API architecture for Codegeist.

## Scope

- Map OpenCode server concepts to Spring Boot HTTP APIs.
- Define API ownership over sessions, events, approvals, and tool results.
- Identify authentication and OpenAPI/SDK implications.

## Deliverable

Add a headless server architecture section to the parity document.

## Acceptance Criteria

- Server is a runtime client adapter, not a duplicate runtime.
- Auth requirements are called out before non-local exposure.
- OpenAPI/SDK generation remains later-stage unless the API exists.

## Verification

- Check consistency with Vaadin, storage, and security assumptions.
