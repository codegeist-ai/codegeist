# T001_21 Define Feature Parity Matrix

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Create the full OpenCode-to-Codegeist feature parity matrix.

This task specifies the matrix content and classification rules. It does not
implement features, create implementation tasks by default, or claim runtime
parity that has not been verified.

## Scope

- Cover all feature groups listed in the parent task.
- Mark each feature as MVP, later-stage, or out of scope.
- Map each feature to a Codegeist component and verification idea.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is being compared? | OpenCode feature concepts versus Codegeist Java-first target behavior, not implementation files one-to-one. |
| What evidence is allowed? | Existing OpenCode analysis docs, third-party feature notes, source inspection summaries, and explicit unknown/runtime-unverified labels. |
| What does parity mean? | Equivalent user/runtime capability where desired, translated into Codegeist's Java/Spring stack. |
| What is MVP? | Features needed for the smallest useful CLI/runtime foundation. |
| What can be out of scope? | Features that do not fit Codegeist goals, require unsupported runtime surfaces, or belong to much later product decisions. |

## Required Matrix Columns

- OpenCode feature.
- OpenCode evidence/source.
- Codegeist target behavior.
- Codegeist owning component.
- MVP/later/out-of-scope classification.
- Dependencies or prerequisite architecture tasks.
- Risks/open questions.
- Verification idea.

## Boundary Rules

- Use Java/Spring/Spring AI/Vaadin/JBang/PF4J terminology for Codegeist targets.
- Do not copy Bun/TypeScript package boundaries as Codegeist component names.
- Mark static-analysis-only claims clearly when runtime behavior is unverified.
- Do not promote later-stage features into MVP only because OpenCode has them.
- Keep feature classification separate from implementation backlog sequencing.

## Implementation-Readiness Questions

- Does every major OpenCode feature group have a Codegeist classification?
- Does every MVP row map to a component and later implementation task candidate?
- Are unknowns explicit enough to become risk-register entries?
- Can later tasks use the matrix to derive MVP cut and backlog?

## Non-Goals

- Do not implement features.
- Do not create follow-up implementation tasks unless explicitly requested by
  `T001_25` or the user.
- Do not claim runtime parity without runtime evidence.

## Deliverable

Add the full feature parity matrix to the parity document with evidence,
Codegeist target behavior, component ownership, MVP/later/out-of-scope
classification, risks/open questions, and verification ideas.

## Acceptance Criteria

- Every required feature group is represented.
- Each row includes evidence, Codegeist target behavior, priority, component,
  open questions, and verification idea.
- Rows use Java/Spring-stack terminology for Codegeist behavior.
- Runtime-unverified OpenCode claims are marked as such.
- MVP classification can feed `T001_22` without further reinterpretation.

## Verification

- Review against all OpenCode analysis files listed in the parent task.

## Verification Result

- Specified matrix structure, classification rules, evidence requirements, and
  implementation-readiness checks.
