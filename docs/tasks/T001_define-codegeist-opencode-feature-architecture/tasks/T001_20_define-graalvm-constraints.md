# T001_20 Define GraalVM Constraints

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Identify GraalVM native-image constraints that affect early architecture choices.

This task specifies native-image constraints and risk posture only. It does not
change Maven configuration, add hints, build a native image, or remove features.

## Scope

- Review risks for Spring, Spring AI, Vaadin, PF4J, reflection, dynamic loading,
  plugins, and HTTP clients.
- Decide what must be native-compatible from day one.
- Define acceptable JVM-only exceptions for early prototyping.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| Why does GraalVM matter for OpenCode-to-Java migration? | Codegeist targets Java/GraalVM packaging instead of OpenCode's Bun/Node distribution model. |
| Must every feature be native-compatible immediately? | No. Core CLI/runtime should be native-aware; plugin loading, Vaadin, and some provider integrations may be JVM-first until verified. |
| What is MVP? | Keep dependency choices and reflection/dynamic-loading boundaries visible; verify native profile before release claims. |
| What is later? | Native hints, reflection config, plugin strategy, provider-specific checks, Vaadin/server native review, and JBang behavior. |

## Risk Areas

- Spring Boot/Spring Shell native support with the selected Boot baseline.
- Spring AI provider clients, streaming, tool callbacks, and reflection.
- PF4J dynamic class loading and plugin discovery.
- Vaadin UI dependencies and server push.
- JBang execution from native images.
- HTTP clients, JSON binding, serialization, and event payload reflection.
- File watching, process execution, LSP, and native libraries.

## Boundary Rules

- Native-image constraints should influence dependency choices and adapter
  boundaries, not block JVM-first architecture work.
- Features not yet native-compatible must be labeled JVM-only or later-stage.
- PF4J and JBang must not be assumed native-compatible until proven.
- Built-in runtime, session, event, permission, workspace, and basic CLI concepts
  should remain native-friendly where practical.

## Implementation-Readiness Questions

- Which dependencies require reflection/resource/proxy hints?
- Which features should be disabled or replaced in native mode?
- Can the current native Maven profile build after the Spring Boot/Spring AI
  baseline is adjusted?
- Which provider should be the native smoke-test candidate?
- How does native packaging affect plugin and script extension strategy?

## Non-Goals

- Do not change `pom.xml` or native profile here.
- Do not run native-image as part of this documentation task.
- Do not promise PF4J, Vaadin, Spring AI, or JBang native compatibility before
  verification.

## Deliverable

Add a GraalVM constraints section to the parity document with risk areas,
native/JVM-first split, dependency implications, verification candidates, and
open compatibility questions.

## Acceptance Criteria

- PF4J plugin loading risk is explicitly addressed.
- Native-image constraints influence dependency choices.
- JVM-first prototyping exceptions are named if needed.
- Native compatibility claims require later verification.
- Extension mechanisms are classified as risk areas.

## Verification

- Compare against current `app/codegeist` native Maven profile.

## Verification Result

- Specified GraalVM risk areas and native/JVM-first boundaries for later
  verification.

## Solution Note

Status: completed.

The solution pass added `## GraalVM Constraints` to
`docs/developer/codegeist-opencode-parity.md`. The section defines native-image
risk areas, native/JVM-first split, dependency implications, verification
candidates, and open compatibility questions for Spring, Spring AI, Vaadin,
PF4J, JBang, reflection, serialization, HTTP clients, file/process work, and
LSP/native libraries.

No user decision is pending. Core CLI/runtime stays native-aware, while PF4J,
Vaadin, JBang, and provider-specific integrations may remain JVM-first until
verified.

Verification passed with `git --no-pager diff --check`. A final review confirmed
native compatibility claims require later verification and no Maven/native config
was changed.
