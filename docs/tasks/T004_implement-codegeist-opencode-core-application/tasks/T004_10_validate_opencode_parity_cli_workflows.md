# T004_10 Validate OpenCode Parity CLI Workflows

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Validate selected OpenCode-style CLI workflows against the implemented Codegeist
core and close small parity gaps with tests and documentation.

## Context

This task depends on the end-to-end loop from `T004_09` and uses OpenCode as a
feature and behavior reference, not an implementation blueprint.

## Scope

- Select and verify concrete CLI workflows from the parity docs.
- Add tests, docs, and small implementation corrections when planned.
- Record gaps as follow-up tasks when they exceed the planned slice.

## Non-Goals

- Do not implement deferred JBang, PF4J, Vaadin, headless server, API, SDK, or full
  TUI behavior.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- Finalized `T004_09`

## Planning Requirements

- Create `docs/developer/implementation/opencode-parity-cli-workflow-validation.md`.
- Include UML diagrams only for classes touched or added by planned parity fixes.
- Define workflow tests and smoke commands.

## Acceptance Criteria

- Selected workflows are verified or gaps are recorded with precise follow-ups.
- Architecture and developer docs reflect implemented parity behavior.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned workflow and smoke tests.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as CLI workflow parity validation.
- Open decisions or blockers: exact workflow list belongs to `/plan-task t004_10`.
- Next recommended phase: `/plan-task t004_10` after `T004_09` is solved.
