# T004_10 Validate OpenCode Parity CLI Workflows

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Validate selected OpenCode-style CLI workflows against the implemented Codegeist
core and close small parity gaps with tests and documentation.

## Context

This task depends on the end-to-end loop from `T004_09` and uses OpenCode as a
feature and behavior reference, not an implementation blueprint.

## Scope

- Select and verify concrete CLI workflows from the parity docs.
- Add tests, docs, and small implementation corrections when planned.
- Record gaps as follow-up tasks when they exceed the planned slice.

## Non-Goals

- Do not implement deferred JBang, PF4J, Vaadin, headless server, API, SDK, or full
  TUI behavior.

## Direct Inputs

- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- Finalized `T004_09`

## Planning Requirements

- Create `docs/developer/implementation/opencode-parity-cli-workflow-validation.md`.
- Include UML diagrams only for classes touched or added by planned parity fixes.
- Define workflow tests and smoke commands.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/opencode-parity-cli-workflow-validation.md`.

Planned solve-phase test package:

- `ai.codegeist.parity`

Planned solve-phase tests:

- `OpenCodeParityCliWorkflowTests`
- `CliWorkflowSmokeTests`
- `ParityGapReportTests`

## Spring AI Agent Utils Equivalent

- Closest equivalents: Agent Utils Claude Code-inspired tool docs, built-in
  subagents, and prompt resources.
- Classification: behavior reference only.
- Specification consequence: use Agent Utils as secondary Java/Spring evidence for
  Claude Code-like workflow behavior, but keep OpenCode parity docs and Codegeist
  implemented behavior as the primary validation target.

## Acceptance Criteria

- Selected workflows are verified or gaps are recorded with precise follow-ups.
- Architecture and developer docs reflect implemented parity behavior.

## Verification

Planning-only changes should run `git --no-pager diff --check`; solve must run the
planned workflow and smoke tests.

## Specification Check Result

- Phase command: `/specify-task T004_10` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_09`, specified `T004_11`, and
  `docs/developer/specification/codegeist-opencode-parity.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; planning should consume the
  solved end-to-end loop and use OpenCode as a behavior reference only.
- Result: confirmed as the selected CLI workflow parity validation and small-gap
  closure task, not a deferred-surface implementation task.
- Open decisions or blockers: exact workflow list, parity evidence, acceptable
  small fixes, smoke checks, and follow-up gap boundaries belong to
  `/plan-task t004_10`.
- Next recommended phase: `/plan-task t004_10` after `T004_09` is solved.

## Planning Check Result

- Phase command: `/plan-task T004_10` as part of user input `alle tasks aus t004`.
- Selected option: plan the existing T004 child task instead of creating a duplicate.
- Duplicate check result: no existing parity workflow validation handoff was
  present.
- Result: created
  `docs/developer/implementation/opencode-parity-cli-workflow-validation.md` with
  selected workflows, class diagram, file map, implementation steps, workflow test
  commands, dependencies, deferrals, and documentation targets.
- Open decisions or blockers: solve depends on solved `T004_09`; broader parity
  gaps must become follow-up task notes instead of hidden scope expansion.
- Next recommended phase: `/solve-task t004_10` after `T004_09` is solved.

## Agent Utils Equivalence Specification Result

- Phase command: `/specify-task T004_10` during the full T004 Agent Utils
  equivalence pass.
- Context or instructions considered: user requested a suitable Spring AI Agent
  Utils equivalent for every T004 task.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Third-party evidence considered: Agent Utils analysis report, built-in subagent
  docs, and tool documentation for Claude Code-inspired workflows.
- Upstream phase dependency: none for specification; existing planning must be
  rechecked to decide whether Agent Utils behavior evidence changes the parity
  workflow list or only the gap-report notes.
- Result: identified Agent Utils as a secondary Java/Spring behavior reference, not
  the parity source of truth.
- Open decisions or blockers: `/plan-task t004_10` must keep OpenCode parity
  primary and record any Agent Utils-derived comparison as supporting evidence.
- Next recommended phase: `/plan-task t004_10` after `T004_09` is solved.

## Agent Utils Planning Recheck Result

- Phase command: `/plan-task T004_10` during the full T004 Agent Utils planning
  recheck.
- Context or instructions considered: user requested planning for all T004 tasks
  after the Agent Utils equivalence scan.
- Selected option: keep OpenCode parity workflows as the primary validation target
  and use Agent Utils only as secondary Java/Spring behavior evidence.
- Duplicate check result:
  `docs/developer/implementation/opencode-parity-cli-workflow-validation.md`
  already exists and remains the authoritative solve handoff.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read: T004 parent, adjacent child tasks, the existing
  implementation handoff, OpenCode parity docs, Agent Utils analysis report, and
  Agent Utils built-in subagent/tool docs.
- Upstream phase dependency: satisfied by the Agent Utils equivalence
  specification result in this task.
- Result: no target files, class diagram, or workflow tests changed; solve should
  mention Agent Utils only when it helps explain a Java/Spring equivalent or
  follow-up gap.
- Open decisions or blockers: none at planning depth; solve still depends on
  solved `T004_09`.
- Next recommended phase: `/solve-task t004_10` after `T004_09` is solved.
