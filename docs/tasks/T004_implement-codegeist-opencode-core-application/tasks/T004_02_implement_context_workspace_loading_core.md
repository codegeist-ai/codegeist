# T004_02 Implement Context Workspace Loading Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Implement deterministic context and workspace loading core contracts with TDD.

## Context

This task consumes `docs/developer/specification/context-workspace-loading-source-generation-contract.md`
after `T004_01` provides runtime/session/event diagnostics and boundaries.

## Scope

- Implement workspace identity, path classification, context profile request
  shapes, source selection, skip reasons, deterministic ordering, and manifest
  summaries selected by the plan.
- Add tests using temporary directories and bounded fixtures.
- Keep repo-specific docs paths as context-profile data, not hard-coded runtime
  constants.

## Non-Goals

- Do not implement provider calls, embeddings, Graphify, Repomix runs, broad file
  scanning, storage adapters, patch/edit, shell execution, CLI/TUI behavior, or
  runtime orchestration beyond planned diagnostics integration.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/context-workspace-loading-source-generation-contract.md`
- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`

## Planning Requirements

- Create `docs/developer/implementation/context-workspace-loading-core-implementation.md`.
- Include an UML class diagram for all planned context/workspace classes and tests.
- Define the temporary-directory test matrix and narrow test commands.

## Acceptance Criteria

- Context/workspace loading contracts selected by the plan are implemented with
  deterministic behavior and individually executable tests.
- Architecture docs describe any implemented packages, classes, and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the context/workspace implementation task.
- Open decisions or blockers: exact source kinds, class list, and tests belong to
  `/plan-task t004_02`.
- Next recommended phase: `/plan-task t004_02` after `T004_01` is planned or solved
  enough to provide runtime diagnostics dependencies.
