# T001_24 Define Risk Register

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Create an architecture risk register for the Codegeist/OpenCode parity effort.

This task specifies risk tracking only. It does not solve risks, implement
mitigations, or create backlog tasks by default.

## Scope

- Capture technical, product, dependency, security, runtime, and packaging risks.
- Include impact, uncertainty, mitigation, and verification idea.
- Highlight risks that block implementation sequencing.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What risks come from translating OpenCode to Java? | Provider/tool behavior gaps, runtime model mismatches, native-image constraints, plugin/script safety, and UI/server deferral risks. |
| What risks are not implementation tasks yet? | Unverified assumptions that need mitigation or validation paths before sequencing. |
| What must block implementation? | Risks that affect baseline dependencies, security boundaries, data loss, permissions, workspace safety, or MVP feasibility. |

## Required Risk Columns

- Risk id/name.
- Area.
- Description.
- Impact.
- Likelihood/uncertainty.
- Affected tasks/components.
- Mitigation or decision path.
- Verification idea.
- Blocking status.

## Minimum Risk Areas

- Spring Boot/Spring AI version compatibility.
- Java `25` compatibility.
- Provider streaming/tool-calling behavior.
- Permission bypass or unsafe side effects.
- Workspace path/symlink/ignored-file escape.
- Shell execution safety.
- Patch/apply conflicts and data loss.
- PF4J native-image/dynamic-loading constraints.
- JBang script trust and remote dependency risk.
- Server authentication before non-local exposure.
- Storage/redaction/audit retention decisions.
- Vaadin/server scope creep.

## Non-Goals

- Do not implement mitigations.
- Do not convert every risk into a task here.
- Do not hide unresolved risks by marking them accepted without rationale.

## Deliverable

Add a risk register section to the parity document with risk area, impact,
uncertainty, affected components, mitigation path, verification idea, and blocking
status.

## Acceptance Criteria

- GraalVM, PF4J, Spring AI provider behavior, permissions, and server security
  risks are represented.
- Each risk has a mitigation or validation path.
- Risks are not mixed with implementation tasks.
- Blocking risks are clearly identified.
- Risks map back to architecture child tasks where possible.

## Verification

- Review against all previous architecture child tasks.

## Verification Result

- Specified risk-register structure, required areas, and blocking-risk criteria.

## Solution Note

Status: completed.

The solution pass added `## Risk Register` to
`docs/developer/specification/codegeist-opencode-parity.md`. The section records risks across
dependency baseline, Java compatibility, provider behavior, permissions,
workspace safety, shell execution, patch conflicts, PF4J, JBang, server auth,
storage/redaction, scope creep, and runtime-unverified OpenCode assumptions.

No user decision is pending. Each risk has a mitigation or validation path,
verification idea, affected area, and blocking status without turning risks into
implementation tasks.

Verification passed with `git --no-pager diff --check`. A final review confirmed
GraalVM, PF4J, Spring AI provider behavior, permissions, and server security
risks are represented.
