# T004_09 Implement End To End Agent Loop

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Implement the first end-to-end agent loop across runtime, context, provider,
tools, permissions, storage, and CLI using fakes first and live integrations only
when explicitly planned.

## Context

This task depends on the earlier T004 core implementation tasks. It should turn
the separate boundaries into a minimal usable prompt loop without broad OpenCode
parity claims.

## Scope

- Wire runtime orchestration through implemented context, provider, tool,
  permission, storage, and CLI boundaries selected by the plan.
- Use fake providers and fake tools first for deterministic tests.
- Add smoke tests for the minimal loop selected by the plan.

## Non-Goals

- Do not claim OpenCode replacement readiness, live-provider compatibility, native
  readiness, or complete CLI workflow parity in this task.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- Finalized `T004_01` through `T004_08` implementation tasks
- `docs/developer/specification/codegeist-opencode-parity.md`

## Planning Requirements

- Create `docs/developer/implementation/end-to-end-agent-loop-implementation.md`.
- Include an UML class diagram covering every orchestrator, fake, adapter, event,
  and test class planned for the loop.
- Define deterministic fake-provider and fake-tool tests before any live checks.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/end-to-end-agent-loop-implementation.md`.

Planned solve-phase target packages:

- `ai.codegeist.runtime`
- `ai.codegeist.provider.fake`
- `ai.codegeist.tool.fake`

Planned solve-phase tests:

- `EndToEndAgentLoopContractTests`
- `FakeProviderAgentLoopTests`
- `AgentLoopBoundaryDependencyTests`

## Acceptance Criteria

- A minimal prompt loop works through Codegeist-owned boundaries selected by the
  plan and is covered by deterministic tests.
- Architecture docs describe the implemented loop and verification.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_09` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_08`, specified `T004_10`, and the
  earlier T004 implementation tasks named as dependencies.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should wait for the
  required earlier T004 implementation results or explicitly narrow any unresolved
  dependencies.
- Result: confirmed as the first end-to-end prompt-loop integration task, using
  fake providers and fake tools before any live integration checks.
- Open decisions or blockers: exact minimal workflow, orchestration boundaries,
  fake-provider/fake-tool fixtures, smoke tests, class list, and narrow commands
  belong to `/plan-task t004_09`.
- Next recommended phase: `/plan-task t004_09` after required earlier T004 tasks
  are solved.

## Planning Check Result

- Phase command: `/plan-task T004_09` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing end-to-end loop implementation handoff was
  present.
- Result: created
  `docs/developer/implementation/end-to-end-agent-loop-implementation.md` with the
  class diagram, file map, implementation steps, fake-provider test commands,
  dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve depends on solved `T004_01` through `T004_08`
  or must explicitly narrow unresolved dependencies.
- Next recommended phase: `/solve-task t004_09` after required earlier T004 tasks
  are solved.
