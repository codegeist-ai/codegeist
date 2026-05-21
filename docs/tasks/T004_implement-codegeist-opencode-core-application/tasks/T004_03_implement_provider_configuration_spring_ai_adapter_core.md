# T004_03 Implement Provider Configuration Spring AI Adapter Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Implement provider configuration validation and Spring AI adapter boundaries with
TDD, while avoiding live model calls by default.

## Context

This task consumes `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`
and must keep Spring AI and provider SDK types at adapter edges.

## Scope

- Implement provider ids, model refs, capabilities, option profiles,
  credential-source references, offline validation, runtime-facing adapter records,
  typed provider errors, and first-wave OpenAI-compatible/OpenAI and Ollama posture
  selected by the plan.
- Add tests with fakes or local mappers; avoid network by default.

## Non-Goals

- Do not add live credentials, live provider tests, broad provider starters, tool
  callbacks, runtime orchestration, CLI/TUI behavior, or storage behavior unless
  the plan narrows and justifies them.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`
- `docs/developer/specification/provider-configuration-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`

## Planning Requirements

- Create `docs/developer/implementation/provider-spring-ai-adapter-core-implementation.md`.
- Include an UML class diagram for all planned provider classes, adapters, errors,
  configuration types, and tests.
- Define offline validation and mapper test commands.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/provider-spring-ai-adapter-core-implementation.md`.

Planned solve-phase target packages:

- `ai.codegeist.provider`
- `ai.codegeist.provider.springai`

Planned solve-phase tests:

- `ProviderConfigurationValidationTests`
- `SpringAiProviderMappingTests`
- `ProviderBoundaryDependencyTests`

## Acceptance Criteria

- Provider configuration and adapter boundaries selected by the plan are
  implemented with tests and without leaking Spring AI types into runtime
  contracts.
- Architecture docs describe implemented provider packages and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_03` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: planned `T004_01`, specified `T004_02`, and
  specified `T004_04`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should consume the
  runtime/session/event handoff and keep provider behavior behind Codegeist-owned
  adapter boundaries.
- Result: confirmed as the provider configuration and Spring AI adapter
  implementation task, with offline validation and no live model calls by default.
- Open decisions or blockers: exact provider class list, dependency additions,
  configuration binding shape, mapper tests, and narrow Maven commands belong to
  `/plan-task t004_03`.
- Next recommended phase: `/plan-task t004_03` to create the implementation plan,
  UML class diagram, offline test matrix, and documentation update plan.

## Planning Check Result

- Phase command: `/plan-task T004_03` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing provider implementation handoff was present.
- Result: created
  `docs/developer/implementation/provider-spring-ai-adapter-core-implementation.md`
  with the class diagram, file map, implementation steps, offline TDD commands,
  dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve may adjust Spring AI adapter imports to match
  Spring AI `2.0.0-M6`, but public Codegeist provider contracts remain stable.
- Next recommended phase: `/solve-task t004_03` after `T004_01` is solved.
