# T004_03 Implement Provider Configuration Spring AI Adapter Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

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

## Acceptance Criteria

- Provider configuration and adapter boundaries selected by the plan are
  implemented with tests and without leaking Spring AI types into runtime
  contracts.
- Architecture docs describe implemented provider packages and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as provider configuration and Spring AI adapter implementation.
- Open decisions or blockers: exact provider class list, dependencies, and tests
  belong to `/plan-task t004_03`.
- Next recommended phase: `/plan-task t004_03`.
