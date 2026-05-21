# T004_04 Implement Tool Permission Workspace Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

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

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/tool-permission-workspace-core-implementation.md`.

Planned solve-phase target packages:

- `ai.codegeist.tool`
- `ai.codegeist.permission`
- `ai.codegeist.workspace`

Planned solve-phase tests:

- `ToolPermissionWorkspaceContractTests`
- `PermissionPolicyTests`
- `WorkspaceToolPolicyTests`
- `ToolBoundaryDependencyTests`

## Acceptance Criteria

- Selected policy and workspace core behavior is implemented and tested.
- Architecture docs describe implemented packages and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_04` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_03`, specified `T004_05`, and
  specified `T004_06`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should consume
  runtime/context/provider boundaries where needed without executing tools.
- Result: confirmed as the policy primitive task for descriptors, permissions,
  workspace target validation, bounded results, and later patch/shell/provider
  mediation.
- Open decisions or blockers: exact policy surface, class list, runtime/event
  integration points, workspace validation fixtures, and narrow Maven commands
  belong to `/plan-task t004_04`.
- Next recommended phase: `/plan-task t004_04` to create the implementation plan,
  UML class diagram, policy test matrix, and documentation update plan.

## Planning Check Result

- Phase command: `/plan-task T004_04` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing tool/permission/workspace implementation
  handoff was present.
- Result: created
  `docs/developer/implementation/tool-permission-workspace-core-implementation.md`
  with the class diagram, file map, implementation steps, policy test commands,
  dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve should use `T004_01` and `T004_02` source types
  when available and must not introduce concrete tool executors.
- Next recommended phase: `/solve-task t004_04` after runtime and workspace
  dependencies are available.
