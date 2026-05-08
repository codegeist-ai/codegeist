# T001_01 Define Technology Baseline

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the baseline technologies for Codegeist before mapping OpenCode features.

## Scope

- Document why Codegeist is Java-first.
- Record the role of Java, GraalVM, Spring, Spring AI, Vaadin, JBang, and PF4J.
- Separate fixed decisions from technologies that still require validation.

## Deliverable

Update `docs/developer/codegeist-opencode-parity.md` with a concise technology
baseline section.

## Acceptance Criteria

- Each baseline technology has a stated responsibility.
- GraalVM and PF4J compatibility are called out as architecture risks.
- The section avoids implementation tasks.

## Verification

- Compare the section with the parent task's technology baseline.
- Confirm no technology is described only as a vague preference.
