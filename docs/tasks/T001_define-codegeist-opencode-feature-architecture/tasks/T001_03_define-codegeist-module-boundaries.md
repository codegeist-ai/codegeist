# T001_03 Define Codegeist Module Boundaries

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the logical Codegeist architecture components and their responsibilities.

## Scope

- Define boundaries for CLI, runtime, context, provider, tools, permissions,
  storage, server, plugins, and UI.
- Decide whether the first implementation should stay in one Maven module or
  start as a multi-module project.
- Keep boundaries logical even if physical modules are delayed.

## Deliverable

Add a component model section to `docs/developer/codegeist-opencode-parity.md`.

## Acceptance Criteria

- Each component has one primary responsibility.
- Runtime logic is not owned by CLI, Vaadin, or server adapters.
- Physical module timing is explicitly decided or left as an open question.

## Verification

- Confirm the component model can start from current `app/codegeist` without a
  full rewrite.
