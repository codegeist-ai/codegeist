# T004_01 Implement Runtime Session Event Core

Parent: `T004_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Implement the first runtime, session, and event Java core contracts with TDD.

## Context

This task consumes `docs/developer/specification/runtime-session-event-source-generation-contract.md`
and must stay aligned with `docs/developer/architecture/architecture.md`. It is the
first source-generating task and should establish the initial packages under
`ai.codegeist.runtime`, `ai.codegeist.session`, and `ai.codegeist.event`.

## Scope

- Create the first runtime/session/event records, identifiers, enums, sealed event
  families, projection shapes, small ports, and typed failures selected by the
  plan.
- Add plain JVM tests first; avoid Spring context startup unless the plan proves it
  is necessary.
- Update architecture documentation after source and tests exist.

## Child Tasks

- `tasks/T004_01_01_define_runtime_prompt_contracts.md`
- `tasks/T004_01_02_define_runtime_failures_and_validation.md`
- `tasks/T004_01_03_define_session_core_contracts.md`
- `tasks/T004_01_04_define_event_core_contracts.md`
- `tasks/T004_01_05_define_session_projection_core.md`
- `tasks/T004_01_06_verify_core_dependency_boundaries.md`

Prefer solving these child tasks in order. Each child owns a smaller TDD slice of
the original runtime/session/event core plan and should keep Java source creation
inside its own `/solve-task` pass.

## Non-Goals

- Do not implement provider calls, context loading, tools, permissions, workspace
  reads, patch/edit, shell execution, storage adapters, CLI commands, TUI, server,
  Vaadin, PF4J, JBang, live model calls, or packaging behavior.
- Do not create code before `/plan-task t004_01` produces an UML class diagram,
  file map, TDD sequence, and documentation plan.

## Direct Inputs

- `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/runtime-session-event-source-generation-contract.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`

## Planning Requirements

- Create `docs/developer/implementation/runtime-session-event-core-implementation.md`.
- Include a package diagram for the planned runtime, session, event, and test
  package relationships.
- Include a short description for every planned runtime/session/event class,
  record, interface, enum, sealed interface, failure type, and test class.
- For each `T004_01_*` child task, create or update a child-specific
  implementation plan before solving. The plan must include a class diagram for
  every class, record, interface, enum, sealed interface, failure type, and test
  class that child expects to create or modify.
- Each child plan must explain every planned type in detail: responsibility,
  key fields or methods, ownership boundary, validation or failure behavior, and
  how the type relates to earlier and later `T004_01` slices.
- Each child plan must include a Spring usage section that names the exact Spring
  Framework, Spring Boot, Spring AI, Spring Shell, or Spring AI Agent Utils
  classes the solve phase should use. If the correct boundary is no Spring usage,
  the plan must state that explicitly and explain why public runtime, session, and
  event contracts stay Spring-free.
- Include UML class diagrams covering every planned type, but split the diagrams
  into small readable Mermaid views instead of one oversized diagram.
- Name the exact Java and test files to add or change.
- Define the first failing test and the exact narrow Maven command.

## Implementation Plan

The implementation plan is recorded in
`docs/developer/implementation/runtime-session-event-core-implementation.md`.

Planned solve-phase target packages:

- `ai.codegeist.runtime`
- `ai.codegeist.session`
- `ai.codegeist.event`

Planned solve-phase tests:

- `RuntimeSessionEventContractTests`
- `RuntimeSessionEventDependencyTests`

## Spring AI Agent Utils Equivalent

- Closest equivalent: `TaskCall`, `BackgroundTask`, and `TodoWriteTool` event
  handling are useful concept references only.
- Classification: concept reference; no direct Agent Utils equivalent for
  Codegeist runtime, session, turn, message, event-envelope, or projection
  contracts.
- Specification consequence: keep all public runtime/session/event types in
  Codegeist packages and do not model them on Agent Utils task, todo, or subagent
  repository types.

First failing test command for the solve phase:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=RuntimeSessionEventContractTests#acceptsPromptWithoutFrameworkTypes test
```

## Acceptance Criteria

- Runtime/session/event core classes and tests are implemented only after planning.
- Tests are individually executable and prove identifiers, request validation,
  session/turn/message structures, event envelopes, sequencing, projection, and
  typed failures selected by the plan.
- Architecture docs describe the newly implemented packages and tests.

## Verification

Planning-only changes:

```bash
git --no-pager diff --check
```

The planning phase must define Java test commands for the solve phase.

## Specification Check Result

- Phase command: `/specify-task T004_01` rechecked during the full T004
  specification pass.
- Context or instructions considered: user input `für alle t004 tasks ausführen`.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Adjacent task docs considered: specified `T004_02` and the T004 parent epic.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; this task already has a
  later planning result and implementation handoff.
- Result: confirmed as the first source-generating runtime/session/event task and
  preserved the existing implementation plan without selecting new source beyond
  that plan.
- Open decisions or blockers: none at specification depth; implementation remains
  bounded by the existing plan and must still be solved with TDD.
- Next recommended phase: `/solve-task t004_01` if the plan remains current.

## Planning Check Result

- Phase command: `/plan-task t004_01`.
- Context or instructions considered: user input `t004_01`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing specification check result
  in this task before planning advanced the top-level status to `planned`.
- Duplicate check result: no separate implementation plan existed, and this task is
  already the concrete implementation slice for runtime/session/event core.
- Result: created
  `docs/developer/implementation/runtime-session-event-core-implementation.md`
  with the UML class diagram, exact file map, implementation steps, TDD sequence,
  acceptance criteria, dependencies, risks, and verification strategy.
- Open decisions or blockers: none.
- Next recommended phase: `/solve-task t004_01`.

## Planning Recheck Result

- Phase command: `/plan-task T004_01` rechecked as part of user input
  `alle tasks aus t004`.
- Selected option: preserve and confirm the existing implementation handoff instead
  of creating a duplicate plan.
- Duplicate check result:
  `docs/developer/implementation/runtime-session-event-core-implementation.md`
  already existed and remains the authoritative T004_01 solve handoff.
- Result: confirmed the existing class diagram, file map, TDD sequence,
  dependencies, deferrals, and documentation update plan remain current for the
  first runtime/session/event solve phase.
- Open decisions or blockers: none at planning depth.
- Next recommended phase: `/solve-task t004_01`.

## Agent Utils Equivalence Specification Result

- Phase command: `/specify-task T004_01` during the full T004 Agent Utils
  equivalence pass.
- Context or instructions considered: user requested a suitable Spring AI Agent
  Utils equivalent for every T004 task.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Third-party evidence considered:
  `docs/developer/spring-ai-agent-utils-adoption.md`, Agent Utils `TaskTool`
  documentation, and Agent Utils task repository source names.
- Upstream phase dependency: none for specification; the existing implementation
  plan needs a recheck because this equivalence scan is new planning input.
- Result: no direct Agent Utils replacement was selected for runtime/session/event
  core.
- Open decisions or blockers: `/plan-task t004_01` must decide whether the
  implementation handoff needs any note about task/todo concepts, or remains
  unchanged.
- Next recommended phase: `/plan-task t004_01`.

## Agent Utils Planning Recheck Result

- Phase command: `/plan-task T004_01` during the full T004 Agent Utils planning
  recheck.
- Context or instructions considered: user requested planning for all T004 tasks
  after the Agent Utils equivalence scan.
- Selected option: keep the existing runtime/session/event implementation plan and
  record Agent Utils task/todo surfaces as concept references only.
- Duplicate check result:
  `docs/developer/implementation/runtime-session-event-core-implementation.md`
  already exists and remains the authoritative solve handoff.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read: T004 parent, adjacent child tasks, the existing
  implementation handoff, Agent Utils adoption guide, and Agent Utils task/tool
  docs.
- Upstream phase dependency: satisfied by the Agent Utils equivalence
  specification result in this task.
- Result: no target files, class diagram, or TDD command changed; solve must not
  expose Agent Utils task, todo, or background-task types in Codegeist runtime
  contracts.
- Open decisions or blockers: none.
- Next recommended phase: `/solve-task t004_01`.

## Diagram Readability Specification Result

- Phase command: `/specify-task t004_01`.
- Context or instructions considered: user requested a package diagram, a
  description for every class, and UML class diagrams that stay small enough to
  read comfortably in markdown and Mermaid.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; the existing implementation
  handoff already exists and was clarified without Java, test, or build changes.
- Result: updated the T004_01 planning requirements and implementation handoff so
  the documentation now includes a package diagram, per-type class catalog, and
  smaller runtime, session, event, and cross-package Mermaid class diagrams.
- Open decisions or blockers: none.
- Next recommended phase: `/plan-task t004_01` to confirm the clarified handoff
  remains implementation-ready before any `/solve-task` writes Java source.

## Task Split Specification Result

- Phase command: chat-guided split of `T004_01` after the user confirmed the task
  should be subdivided.
- Context or instructions considered: user requested further subdivision of this
  task and approved the six-slice proposal.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for splitting at specification depth; each child
  still needs its own planning and solve phase before writing Java source.
- Result: migrated this task to canonical grouped form with `task.md` and six
  child tasks under `tasks/`.
- Open decisions or blockers: none.
- Next recommended phase: `/plan-task T004_01_01`.

## Child Diagram Detail Specification Result

- Phase command: `/specify-task t004_01`.
- Context or instructions considered: user input requested that every task create
  a class diagram for the classes it will generate, explain each class in detail,
  and show which Spring classes should be used; the original user text was in
  German and is summarized here in English for durable task documentation.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Child task docs considered: `T004_01_01` through `T004_01_06` under this task's
  `tasks/` directory.
- Discovered hints considered:
  `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md`,
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; child planning is still
  required before any child solve phase writes Java source.
- Result: clarified that every split `T004_01_*` child plan must include its own
  class diagram, detailed per-type explanations, and an explicit Spring usage
  section that either names concrete Spring classes or records why none should be
  used in the public core contracts.
- Open decisions or blockers: none at specification depth; concrete file targets,
  implementation sequence, and exact Spring usage decisions remain owned by each
  child `/plan-task` pass.
- Next recommended phase: `/plan-task T004_01_01`.

## Child Planning Routing Result

- Phase command: `/plan-task t004_01`.
- Context or instructions considered: user input `t004_01`.
- Selected option: route planning to the first ordered child task,
  `T004_01_01 Define Runtime Prompt Contracts`, because this task is now a grouped
  parent with six child slices and the specification recommends planning
  `T004_01_01` next.
- Duplicate check result: no duplicate parent-level implementation task was
  created; the existing parent overview remains
  `docs/developer/implementation/runtime-session-event-core-implementation.md`.
- Result: `T004_01_01` now owns the concrete child-specific implementation plan at
  `docs/developer/implementation/runtime-prompt-contracts-implementation.md`.
- Open decisions or blockers: none at parent planning depth.
- Next recommended phase: `/solve-task T004_01_01`.

## All Child Planning Result

- Phase command: `/plan-task T004_01` across all existing child tasks.
- Context or instructions considered: user input `für alle subtasks in t004_01
  ausführen`, interpreted as explicit permission to plan all six existing
  `T004_01_*` child tasks.
- Parent task considered:
  `docs/tasks/T004_implement-codegeist-opencode-core-application/task.md`.
- Child task docs considered: `T004_01_01` through `T004_01_06` under this task's
  `tasks/` directory.
- Duplicate check result: no duplicate task files were created. Existing child
  tasks were sharpened with child-specific implementation handoffs under
  `docs/developer/implementation/`.
- Discovered hints considered:
  `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md`,
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read:
  `docs/developer/architecture/architecture.md`,
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`,
  `docs/developer/specification/testing-strategy-and-agent-rules.md`,
  `docs/developer/spring-ai-agent-utils-adoption.md`, `app/codegeist/cli/pom.xml`,
  `CodegeistApplication.java`, and `CodegeistApplicationTests.java`.
- Result: all six `T004_01_*` child tasks now have top-level `Status: planned` and
  child-specific implementation handoffs for runtime prompt contracts, runtime
  failures and validation, session core contracts, event core contracts, session
  projection core, and dependency-boundary verification.
- Open decisions or blockers: none at planning depth. Solve order still matters:
  `T004_01_01` must be solved before `T004_01_02`, and each later child depends on
  the earlier child slices named in its plan.
- Next recommended phase: `/solve-task T004_01_01`.
