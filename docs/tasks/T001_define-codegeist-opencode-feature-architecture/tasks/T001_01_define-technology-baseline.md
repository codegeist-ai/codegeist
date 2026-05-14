# T001_01 Define Technology Baseline

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the baseline technologies for Codegeist before mapping OpenCode features
or changing runtime dependencies.

## Architecture Decision

Codegeist is a Java-first coding agent platform. OpenCode is a feature
reference, but Codegeist will not copy OpenCode's Bun and TypeScript runtime
shape.

Spring AI is a central architecture component, so Spring AI stability is more
important than adopting Spring Boot 4 early. Codegeist should target Spring Boot
`3.5.x` and the stable Spring AI `1.1.x` line.

Java `25` remains the preferred Java baseline as long as it works with Spring
Boot `3.5.x`, Spring AI `1.1.x`, Spring Shell, Maven, and GraalVM. If Java `25`
causes compatibility issues, the Java baseline must be decided separately
instead of silently changing the Spring AI stability decision.

## Scope

- Document why Codegeist is Java-first.
- Record the role of Java 25, Maven, Spring Boot 3.5.x, Spring Shell, Spring AI
  1.1.x, GraalVM, Vaadin, JBang, and PF4J.
- Separate fixed decisions from technologies that still require validation.
- Document that the current `app/codegeist/cli/pom.xml` still uses Spring Boot
  `4.0.3`, and that changing it belongs to a later implementation or
  configuration task.

## Out Of Scope

- Changing `app/codegeist/cli/pom.xml`.
- Adding Spring AI dependencies.
- Downgrading or validating Spring Boot at runtime.
- Running compatibility spikes.
- Implementing providers, tools, plugins, commands, UI, or native-image fixes.

## Deliverable

Update `docs/developer/codegeist-opencode-parity.md` with a concise technology
baseline section.

The section should include a table with at least these columns:

- Technology
- Decision
- Role
- Boundary
- Validation needed

The section should cover at least:

- Java 25
- Maven
- Spring Boot 3.5.x
- Spring Shell
- Spring AI 1.1.x
- GraalVM
- Vaadin
- JBang
- PF4J

## Acceptance Criteria

- Codegeist is explicitly described as Java-first.
- Spring Boot `3.5.x` is selected because stable Spring AI `1.1.x` support is
  more important than Spring Boot 4 adoption.
- Java `25` is preferred only while compatibility remains valid.
- Each baseline technology has a stated responsibility and boundary.
- GraalVM, PF4J, Spring AI, Vaadin, and JBang compatibility are called out as
  architecture risks or validation points.
- JBang is described as a lightweight user extension runtime, not only developer
  automation.
- The section avoids implementation tasks.

## Verification

- Compare the section with the parent task's technology baseline.
- Confirm no technology is described only as a vague preference.
- Confirm no OpenCode runtime technology is treated as a Codegeist runtime
  requirement.
- Confirm `app/codegeist/cli/pom.xml` remains unchanged by this specification task.

## Specification Check Result

- Already answers the Codegeist-specific baseline questions: Java-first target,
  Spring Boot/Spring AI version posture, boundaries for Vaadin/JBang/PF4J, and
  explicit non-implementation scope.
- No further task reshaping was needed during the `/specify-task` pass.

## Solution Note

Status: completed.

The solution pass used the smallest documentation-first path. The central parity
document already contained the required technology baseline, so the implemented
change aligned technology labels with this task's exact baseline terms while
leaving runtime and build files untouched.

No user decision is pending because the task already fixes the architecture
decision: Java-first, Spring Boot `3.5.x` for stable Spring AI `1.1.x`, and Java
`25` only while compatibility remains valid.

Verification passed with `git --no-pager diff --check`. A final diff review
confirmed `app/codegeist/cli/pom.xml` remains unchanged by this task.
