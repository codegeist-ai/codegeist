# T004_04 Implement Tool Permission Workspace Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: specified

## Goal

Implement Codegeist tool descriptors, permission decisions, and workspace target
validation core with TDD.

## Context

This task consumes `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`
and should provide policy primitives for later patch/edit, shell, and provider
tool-call mediation.

## Scope

- Implement descriptor classification, registry exposure, mode gates, permission
  request/decision shapes, workspace target validation, bounded results, output
  references, typed failures, and runtime/session/event integration selected by
  the plan.
- Add plain JVM tests for policy decisions and workspace validation.

## Non-Goals

- Do not execute tools, call providers, run shell commands, apply patches, persist
  storage, or implement UI approval flows before their owning tasks.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`
- `docs/developer/specification/tool-permission-workspace-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_02_implement_context_workspace_loading_core.md`

## Planning Requirements

- Create `docs/developer/implementation/tool-permission-workspace-core-implementation.md`.
- Include an UML class diagram for all planned tool, permission, workspace, result,
  failure, and test classes.
- Define policy and path-validation test commands.

## Acceptance Criteria

- Selected policy and workspace core behavior is implemented and tested.
- Architecture docs describe implemented packages and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: initial T004 creation.
- Result: specified as the tool/permission/workspace implementation task.
- Open decisions or blockers: exact policy surface and class list belong to
  `/plan-task t004_04`.
- Next recommended phase: `/plan-task t004_04`.
