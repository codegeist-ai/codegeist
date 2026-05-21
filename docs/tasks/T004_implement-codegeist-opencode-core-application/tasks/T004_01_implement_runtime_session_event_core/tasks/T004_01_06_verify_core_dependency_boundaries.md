# T004_01_06 Verify Core Dependency Boundaries

Parent: `T004_01_implement_runtime_session_event_core`

Status: specified

## Goal

Verify the completed runtime/session/event core packages keep forbidden framework
and deferred-surface types out of their public contracts.

## Context

This child task closes `T004_01` after the runtime, failure, session, event, and
projection child slices are solved. It owns the dependency-boundary test and the
architecture documentation update for the implemented packages.

## Scope

- Add `RuntimeSessionEventDependencyTests`.
- Verify public signatures in `ai.codegeist.runtime`, `ai.codegeist.session`, and
  `ai.codegeist.event` do not expose forbidden framework or deferred-surface
  packages.
- Run targeted runtime/session/event tests and the broader CLI Maven test suite.
- Update `docs/developer/architecture/architecture.md` after source and tests
  exist.
- Update the parent `T004_01` task solve status when all child slices pass.

## Planned Types

- `RuntimeSessionEventDependencyTests`
- `RuntimeSessionEventDependencyTests#coreContractsDoNotExposeFrameworkTypes`

## Non-Goals

- Do not add new domain behavior beyond dependency verification and architecture
  documentation.
- Do not implement provider calls, context loading, tools, permissions, workspace
  reads, patch/edit, shell execution, storage adapters, CLI commands, TUI, server,
  Vaadin, PF4J, JBang, live model calls, or packaging behavior.

## Acceptance Criteria

- Dependency-boundary tests prove no public core contract signatures expose Spring,
  Spring AI, Agent Utils, provider SDK, storage adapter, CLI, TUI, HTTP, Vaadin,
  PF4J, JBang, filesystem, process, or terminal UI types.
- `docs/developer/architecture/architecture.md` describes the implemented
  runtime/session/event packages and tests.
- Parent `T004_01` can be marked solved only after all child tasks are solved and
  verification passes.

## Verification

Planning and documentation changes:

```bash
git --no-pager diff --check
```

Solve phase must run at least:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests test
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventDependencyTests test
mvn --batch-mode --no-transfer-progress test
```

## Specification Result

- Phase command: subdivision of `T004_01`.
- Context or instructions considered: user requested splitting `T004_01` into
  smaller reviewable steps.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_01_implement_runtime_session_event_core/task.md`.
- Result: this child owns dependency-boundary verification and final architecture
  synchronization for `T004_01`.
- Open decisions or blockers: depends on `T004_01_01` through `T004_01_05`.
- Next recommended phase: `/plan-task T004_01_06` after dependencies are solved.
