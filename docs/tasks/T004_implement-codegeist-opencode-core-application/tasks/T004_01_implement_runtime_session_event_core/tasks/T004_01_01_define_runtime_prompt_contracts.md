# T004_01_01 Define Runtime Prompt Contracts

Parent: `T004_01_implement_runtime_session_event_core`

Status: specified

## Goal

Implement the first `ai.codegeist.runtime` prompt-intake contracts with TDD.

## Context

This child task is the first slice of `T004_01`. It creates the prompt request
and acceptance boundary that later CLI, TUI, server, Vaadin, and extension
adapters can call without owning session mutation.

## Scope

- Create runtime prompt identity and client-surface value types.
- Create the first prompt request and prompt acceptance records.
- Create the small `RuntimePromptPort` boundary interface.
- Add the narrow contract test coverage needed to prove a prompt can be accepted
  through Codegeist-owned types without framework exposure.

## Planned Types

- `PromptRequestId`
- `CorrelationId`
- `WorkspaceRef`
- `AgentMode`
- `SourceClient`
- `PromptRequest`
- `PromptAcceptance`
- `RuntimePromptPort`
- `RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes`

## Non-Goals

- Do not implement validation failures, sequence enforcement, event projection, or
  dependency-boundary scanning beyond what this first prompt contract test needs.
- Do not add Spring Shell commands, Spring AI calls, Agent Utils exposure,
  provider behavior, context loading, workspace validation, storage, tools,
  permissions, CLI rendering, or TUI behavior.

## Acceptance Criteria

- The first prompt request can be represented with Codegeist-owned records and
  enums.
- A `RuntimePromptPort` can return a `PromptAcceptance` without exposing Spring,
  Spring AI, Agent Utils, provider, storage, CLI, TUI, HTTP, Vaadin, PF4J, or JBang
  types.
- The test remains a plain JVM test and can be run by method selector.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run at least:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes test
```

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns only the initial runtime prompt contracts.
- Open decisions or blockers: none at specification depth.
- Next recommended phase: `/plan-task T004_01_01`.
