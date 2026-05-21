# T004_06 Implement Controlled Shell Tool Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Implement controlled shell tool request/result contracts and safe executor boundary
with TDD.

## Context

This task consumes `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`
and the finalized tool/permission/workspace core from `T004_04`.

## Scope

- Implement shell request identity, command shapes, command purpose, destructive
  posture, Plan-mode denial, Build-mode approval handoff, workspace cwd
  validation, env/stdin policy, timeout/cancellation shapes, typed shell failures,
  bounded stdout/stderr summaries, output references, and fake executor boundary
  selected by the plan.
- Add tests for denial, validation, fake execution, timeout classification, and
  bounded output.

## Non-Goals

- Do not run arbitrary real shell commands before the plan defines safe fixtures.
- Do not implement terminal UI, PTY support, remote execution, JBang execution,
  storage persistence, or provider callbacks.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`
- `docs/developer/specification/shell-verification-contracts.md`
- `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/T004_04_implement_tool_permission_workspace_core.md`

## Planning Requirements

- Create `docs/developer/implementation/controlled-shell-tool-core-implementation.md`.
- Include an UML class diagram for all shell classes and tests.
- Define safe fake-executor and optional real-process smoke tests.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/controlled-shell-tool-core-implementation.md`.

Planned solve-phase target package:

- `ai.codegeist.shell`

Planned solve-phase tests:

- `ControlledShellContractTests`
- `FakeShellExecutorTests`
- `ShellBoundaryDependencyTests`

## Spring AI Agent Utils Equivalent

- Closest equivalents: `ShellTools` and `AgentEnvironment`.
- Classification: deferred adapter candidates because process execution is
  high-risk.
- Specification consequence: keep fake execution first. Any real process execution
  must remain behind Codegeist command purpose, destructive posture, cwd, env,
  stdin, timeout, cancellation, permission, and bounded-output contracts.

## Acceptance Criteria

- Selected controlled shell core is implemented and tested without unsafe process
  execution by default.
- Architecture docs describe implemented shell package and tests.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned narrow tests and affected broader checks.

## Specification Check Result

- Phase command: `/specify-task T004_06` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_04`, specified `T004_05`, and
  specified `T004_07`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should depend on the
  `T004_04` policy handoff and must keep real process execution opt-in and
  fixture-scoped.
- Result: confirmed as the controlled shell request/result and safe executor
  boundary task, with fake execution as the default implementation posture.
- Open decisions or blockers: safe process-fixture depth, timeout/cancellation
  shape, output bounding, class list, tests, and narrow Maven commands belong to
  `/plan-task t004_06`.
- Next recommended phase: `/plan-task t004_06` after `T004_04` is planned or
  solved enough to provide policy dependencies.

## Planning Check Result

- Phase command: `/plan-task T004_06` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing controlled shell implementation handoff was
  present.
- Result: created
  `docs/developer/implementation/controlled-shell-tool-core-implementation.md`
  with the class diagram, file map, implementation steps, fake-executor TDD
  commands, dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve should wait until `T004_04` provides policy,
  permission, workspace target, and output-ref contracts; real process execution
  remains deferred.
- Next recommended phase: `/solve-task t004_06` after `T004_04` is solved enough
  to provide policy dependencies.

## Agent Utils Equivalence Specification Result

- Phase command: `/specify-task T004_06` during the full T004 Agent Utils
  equivalence pass.
- Context or instructions considered: user requested a suitable Spring AI Agent
  Utils equivalent for every T004 task.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Third-party evidence considered: Agent Utils `ShellTools` documentation,
  `AgentEnvironment` source name, and adoption-guide shell warnings.
- Upstream phase dependency: none for specification; existing planning must be
  rechecked before solving because `ShellTools` is now classified as deferred and
  mediated only.
- Result: identified `ShellTools` as the closest equivalent while preserving the
  fake-executor-first Codegeist shell boundary.
- Open decisions or blockers: `/plan-task t004_06` must decide whether any real
  process smoke remains out of scope or is mediated by a narrow adapter.
- Next recommended phase: `/plan-task t004_06` after `T004_04` remains current.

## Agent Utils Planning Recheck Result

- Phase command: `/plan-task T004_06` during the full T004 Agent Utils planning
  recheck.
- Context or instructions considered: user requested planning for all T004 tasks
  after the Agent Utils equivalence scan.
- Selected option: keep fake executor as the solve-phase implementation and defer
  Agent Utils `ShellTools` to a later real-process adapter after Codegeist shell
  policy exists.
- Duplicate check result:
  `docs/developer/implementation/controlled-shell-tool-core-implementation.md`
  already exists and remains the authoritative solve handoff.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read: T004 parent, adjacent child tasks, the existing
  implementation handoff, Agent Utils adoption guide, and Agent Utils `ShellTools`
  docs.
- Upstream phase dependency: satisfied by the Agent Utils equivalence
  specification result in this task.
- Result: no target files, class diagram, or tests changed; solve must not run real
  shell processes by default and must keep timeout, cwd, env, stdin, permission,
  and output policy Codegeist-owned.
- Open decisions or blockers: none beyond `T004_04` policy dependencies.
- Next recommended phase: `/solve-task t004_06` after `T004_04` is solved enough
  to provide policy dependencies.
