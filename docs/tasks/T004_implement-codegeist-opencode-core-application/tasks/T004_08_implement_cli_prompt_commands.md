# T004_08 Implement CLI Prompt Commands

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Implement Spring Shell `plan` and `build` prompt commands over finalized runtime
APIs with TDD.

## Context

This task consumes `docs/developer/specification/cli-prompt-command-source-generation-contract.md`
and depends on runtime/session/event core from `T004_01`.

## Scope

- Implement the first CLI adapter package, Spring Shell command class or classes,
  prompt/session input parsing, runtime delegation, stable accepted/submitted
  output, adapter errors, and tests selected by the plan.
- Keep CLI as a runtime client, not an owner of sessions, provider calls, tools,
  permissions, context loading, storage, patch/edit, shell, or TUI behavior.

## Non-Goals

- Do not implement end-to-end provider responses, full TUI, command templates,
  async/server flows, shell commands, tool execution, storage continuation, or live
  model calls in this task.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/cli-prompt-command-source-generation-contract.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`

## Planning Requirements

- Create `docs/developer/implementation/cli-prompt-command-implementation.md`.
- Include an UML class diagram for all CLI adapter, command, mapper, output, fake
  runtime, and test classes.
- Define Spring Shell command registration tests and adapter tests.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/cli-prompt-command-implementation.md`.

Planned solve-phase target package:

- `ai.codegeist.cli`

Planned solve-phase tests:

- `PromptCommandsAdapterTests`
- `PromptCommandRegistrationTests`
- `PromptCommandBoundaryDependencyTests`

## Acceptance Criteria

- `plan` and `build` command behavior selected by the plan is implemented and
  tested.
- Architecture docs describe implemented CLI package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_08` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: planned `T004_01`, specified `T004_07`, and
  specified `T004_09`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should consume
  runtime APIs and keep CLI as a client adapter instead of a session owner.
- Result: confirmed as the Spring Shell `plan` and `build` prompt command task,
  limited to prompt input, runtime delegation, stable output, adapter errors, and
  tests.
- Open decisions or blockers: exact command shape, fake runtime strategy, Spring
  Shell test depth, class list, and narrow Maven commands belong to
  `/plan-task t004_08`.
- Next recommended phase: `/plan-task t004_08` after `T004_01` is planned or
  solved enough to provide runtime APIs.

## Planning Check Result

- Phase command: `/plan-task T004_08` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing CLI prompt command implementation handoff was
  present.
- Result: created `docs/developer/implementation/cli-prompt-command-implementation.md`
  with the class diagram, file map, implementation steps, Spring Shell and adapter
  test commands, dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve should wait until `T004_01` provides runtime
  APIs; exact Spring Shell option syntax can be chosen during solve without
  changing the runtime boundary.
- Next recommended phase: `/solve-task t004_08` after `T004_01` is solved.
