# T004_12 Validate Core Replacement Readiness

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Validate whether the implemented Codegeist core can replace OpenCode for the
selected CLI/TUI-oriented core workflows.

## Context

This is the final T004 readiness task. It depends on implemented core behavior,
workflow parity validation, and packaging/startup posture.

## Scope

- Validate the selected replacement-readiness workflow set.
- Summarize remaining gaps and decide whether they block readiness.
- Update architecture, developer docs, and task memory with the current truth.

## Non-Goals

- Do not claim readiness for deferred JBang, PF4J, Vaadin, headless server, API,
  SDK/OpenAPI, or broad TUI behavior unless later tasks implement and validate
  those surfaces.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- Finalized `T004_01` through `T004_11`

## Planning Requirements

- Create `docs/developer/implementation/core-replacement-readiness-validation.md`.
- Include UML diagrams only for classes touched by readiness fixes.
- Define readiness scenarios, acceptance gates, test commands, and gap reporting.

## Acceptance Criteria

- Readiness is reported as passed, skipped with reasons, or failed with blockers
  for each selected workflow.
- Remaining gaps are documented as follow-up tasks instead of hidden in prose.
- Architecture and memory reflect the final T004 outcome.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned readiness checks and affected broad verification.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as final core replacement readiness validation.
- Open decisions or blockers: exact readiness matrix belongs to
  `/plan-task t004_12`.
- Next recommended phase: `/plan-task t004_12` after `T004_11` is finalized.
