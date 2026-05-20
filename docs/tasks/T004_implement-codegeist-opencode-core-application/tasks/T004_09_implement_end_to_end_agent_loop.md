# T004_09 Implement End To End Agent Loop

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

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

## Acceptance Criteria

- A minimal prompt loop works through Codegeist-owned boundaries selected by the
  plan and is covered by deterministic tests.
- Architecture docs describe the implemented loop and verification.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the first end-to-end loop implementation task.
- Open decisions or blockers: exact minimal workflow belongs to `/plan-task t004_09`.
- Next recommended phase: `/plan-task t004_09` after required earlier T004 tasks are
  solved.
