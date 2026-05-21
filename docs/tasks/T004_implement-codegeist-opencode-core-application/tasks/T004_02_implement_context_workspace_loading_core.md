# T004_02 Implement Context Workspace Loading Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

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
- `docs/developer/implementation/runtime-session-event-core-implementation.md`

## Planning Requirements

- Create `docs/developer/implementation/context-workspace-loading-core-implementation.md`.
- Include an UML class diagram for all planned context/workspace classes and tests.
- Define the temporary-directory test matrix and narrow test commands.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/context-workspace-loading-core-implementation.md`.

Planned solve-phase target packages:

- `ai.codegeist.workspace`
- `ai.codegeist.context`

Planned solve-phase tests:

- `ContextWorkspaceLoadingContractTests`
- `ContextManifestOrderingTests`
- `ContextWorkspaceDependencyTests`

## Dependencies And Boundaries

- Planning may consume the planned runtime/session/event package boundaries and
  value types from
  `docs/developer/implementation/runtime-session-event-core-implementation.md`.
- Solving should wait until `T004_01` has created the runtime/session/event source
  types this task references, or the plan must explicitly narrow the slice so it
  does not duplicate those types.
- The context/workspace implementation must keep repository-specific paths as
  profile fixture data and must not hard-code this repository's `docs/`,
  `.oc_local/`, `.opencode/`, or third-party analysis layout into production code.

## Acceptance Criteria

- Context/workspace loading contracts selected by the plan are implemented with
  deterministic behavior and individually executable tests.
- Architecture docs describe any implemented packages, classes, and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_02`.
- Context or instructions considered: user requested a specification pass for
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_02_implement_context_workspace_loading_core.md`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: planned `T004_01` and specified `T004_03`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; `T004_01` is planned but not
  solved, so `T004_02` planning can use its implementation handoff while solving
  must avoid duplicating runtime/session/event source types.
- Result: confirmed as the deterministic context/workspace implementation task and
  clarified the dependency on the `T004_01` implementation plan.
- Open decisions or blockers: exact source kinds, class list, test matrix,
  integration points, and narrow Maven commands belong to `/plan-task t004_02`.
- Next recommended phase: `/plan-task t004_02` to create the implementation plan,
  UML class diagram, temporary-directory test matrix, and documentation update
  plan before Java source is written.

## Planning Check Result

- Phase command: `/plan-task T004_02` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing context/workspace implementation handoff was
  present.
- Result: created
  `docs/developer/implementation/context-workspace-loading-core-implementation.md`
  with the class diagram, file map, implementation steps, TDD commands,
  dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solving should wait until `T004_01` provides the
  runtime/session/event source types this plan references, or narrow explicitly.
- Next recommended phase: `/solve-task t004_02` after `T004_01` is solved.
