# T001_16 Define JBang Role

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the limited role of JBang in the Codegeist architecture.

## Scope

- Decide where lightweight Java scripts are useful.
- Separate JBang automation from long-running runtime state.
- Identify possible developer workflows or migration helpers.

## Deliverable

Add a JBang role section to the parity document.

## Acceptance Criteria

- JBang is not used as the core runtime mechanism.
- JBang use cases are specific and optional.
- Boundaries with PF4J commands and Spring services are clear.

## Verification

- Review whether each proposed JBang use case could be a plain Java script.
