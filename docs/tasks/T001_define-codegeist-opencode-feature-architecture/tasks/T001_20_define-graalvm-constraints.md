# T001_20 Define GraalVM Constraints

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Identify GraalVM native-image constraints that affect early architecture choices.

## Scope

- Review risks for Spring, Spring AI, Vaadin, PF4J, reflection, dynamic loading,
  plugins, and HTTP clients.
- Decide what must be native-compatible from day one.
- Define acceptable JVM-only exceptions for early prototyping.

## Deliverable

Add a GraalVM constraints section to the parity document.

## Acceptance Criteria

- PF4J plugin loading risk is explicitly addressed.
- Native-image constraints influence dependency choices.
- JVM-first prototyping exceptions are named if needed.

## Verification

- Compare against current `app/codegeist` native Maven profile.
