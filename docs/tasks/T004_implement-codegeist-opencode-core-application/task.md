# T004 Implement Codegeist OpenCode Core Application

Status: planned

## Goal

Implement the Codegeist OpenCode-style core application from the finalized T003
source-generation contracts, using a strict specify, plan, solve, and finalize
workflow.

This task is the first phase where Java source, tests, package directories, Spring
configuration, and runtime behavior are allowed, but only inside child tasks whose
planning phase has already produced detailed class documentation and an UML class
diagram for every class the solve phase should create or modify.

## User Direction

The user explicitly changed the project direction after T003:

- T004 should be a real implementation epic, not another documentation-only
  contract sequence.
- All implementation tasks should be created first and specified carefully.
- Planning should happen before solving, and the project should go through more
  specification and planning passes before `solve-task` writes code.
- The planning phase must create an UML class diagram that covers all planned
  classes for the task.
- The planning phase must create extensive documentation.
- `solve-task` is the phase that writes the actual Java classes and runs tests.
- Implementation must be based on the current-state architecture in
  `docs/developer/architecture/architecture.md` and the finalized T003 handoffs.

## Context

`T003_define-codegeist-opencode-core-source-contracts` finalized the source
contracts that describe what can be implemented next without reopening broad
architecture decisions. T004 consumes those contracts and starts source generation
inside the existing single Maven module under `app/codegeist/cli`.

The current implemented state is still intentionally small: only
`ai.codegeist.app.CodegeistApplication` and the Spring Boot context-load test exist
in Java source. Every T004 child task must compare its plan against
`docs/developer/architecture/architecture.md` before adding packages or classes.

## Planning Contract

Every T004 child task must complete `/specify-task` and `/plan-task` before
`/solve-task`.

The `/plan-task` phase must create or update a developer implementation plan under
`docs/developer/implementation/` before code is written. Each plan must include:

- A Mermaid or PlantUML class diagram that includes every class, record,
  interface, enum, sealed interface, exception, configuration properties type, and
  test class the solve phase is expected to create or modify.
- A file-by-file implementation map for `app/codegeist/cli/src/main/java` and
  `app/codegeist/cli/src/test/java`.
- A TDD sequence that names the first failing test, the implementation step that
  should satisfy it, and the exact narrow test command to run.
- A dependency and deferral section that shows how the task consumes earlier T004
  tasks and which later T004 tasks remain out of scope.
- A documentation update plan for `docs/developer/architecture/architecture.md` and
  any relevant specification or implementation docs.

The `/solve-task` phase may then write Java source, tests, package directories,
configuration, and documentation according to the current plan. It must run the
planned narrow tests, report timings, and run the broadest practical affected
verification command before marking the task solved.

## Child Tasks

- `T004_01_implement_runtime_session_event_core.md`
- `T004_02_implement_context_workspace_loading_core.md`
- `T004_03_implement_provider_configuration_spring_ai_adapter_core.md`
- `T004_04_implement_tool_permission_workspace_core.md`
- `T004_05_implement_patch_edit_proposal_core.md`
- `T004_06_implement_controlled_shell_tool_core.md`
- `T004_07_implement_storage_session_continuation_core.md`
- `T004_08_implement_cli_prompt_commands.md`
- `T004_09_implement_end_to_end_agent_loop.md`
- `T004_10_validate_opencode_parity_cli_workflows.md`
- `T004_11_validate_packaging_native_and_startup_posture.md`
- `T004_12_validate_core_replacement_readiness.md`

All child tasks have been specified and planned. None are solved yet.

## Derivation Map

| Child task | Primary inputs | Purpose |
| --- | --- | --- |
| `T004_01` | `runtime-session-event-source-generation-contract.md`, `java-generation-guidance.md`, `testing-strategy-and-agent-rules.md`, `architecture.md` | Implement the first runtime, session, and event core contracts with TDD. |
| `T004_02` | `context-workspace-loading-source-generation-contract.md`, finalized `T004_01` contracts | Implement deterministic context/workspace loading core without provider, tool, or storage behavior. |
| `T004_03` | `provider-spring-ai-adapter-source-generation-contract.md`, finalized `T004_01` runtime contracts | Implement provider configuration validation and Spring AI adapter boundaries without live model calls by default. |
| `T004_04` | `tool-permission-workspace-source-generation-contract.md`, finalized runtime/context/provider boundaries | Implement tool descriptors, permission decisions, and workspace target validation. |
| `T004_05` | `patch-edit-proposal-source-generation-contract.md`, finalized tool/permission/workspace contracts | Implement reviewable patch/edit proposal and apply-result core without broad file mutation behavior. |
| `T004_06` | `controlled-shell-tool-source-generation-contract.md`, finalized tool/permission/workspace contracts | Implement controlled shell request/result contracts and safe executor boundary. |
| `T004_07` | `storage-session-continuation-source-generation-contract.md`, finalized runtime/session/event contracts | Implement in-memory-first storage ports and session continuation core. |
| `T004_08` | `cli-prompt-command-source-generation-contract.md`, finalized runtime/session/event contracts | Implement Spring Shell `plan` and `build` prompt commands over runtime APIs. |
| `T004_09` | Finalized runtime, provider, context, tool, permission, patch, shell, storage, and CLI tasks | Implement a minimal end-to-end prompt loop with fakes first and provider extension points. |
| `T004_10` | OpenCode parity docs and finalized T004 core implementation | Validate selected OpenCode-style CLI workflows and close parity gaps. |
| `T004_11` | Build/release/binary smoke strategy and implemented core behavior | Validate packaging, startup, JVM jar, native posture, and binary smoke readiness. |
| `T004_12` | All finalized T004 implementation tasks | Validate that Codegeist can replace OpenCode for selected CLI/TUI-oriented core workflows. |

## Scope

- Implement inside the current Maven module under `app/codegeist/cli` unless a
  planned child task proves a module split is needed.
- Use TDD by default and keep tests individually executable.
- Keep Spring AI, Spring Shell, Spring AI Agent Utils, provider SDKs, process
  execution, filesystem access, and storage adapters behind Codegeist-owned
  boundaries.
- Update `docs/developer/architecture/architecture.md` after implementation tasks
  add real packages, classes, tests, configuration, or behavior.
- Keep JBang, PF4J, Vaadin, headless server, API, and SDK/OpenAPI implementation
  deferred while preserving adapter-ready core contracts.

## Non-Goals

- Do not write Java source from the parent task.
- Do not solve a child task before its plan contains class diagrams, file targets,
  test commands, and documentation updates.
- Do not implement live provider calls, process execution, file mutation,
  persistence, CLI workflows, or packaging behavior before the owning child task is
  planned and solved.
- Do not copy OpenCode's TypeScript, Bun, Hono, Effect, storage, or generated SDK
  implementation shape.
- Do not mark Codegeist as OpenCode-replaceable until the final readiness task
  proves the selected workflows.

## Acceptance Criteria

- Every T004 child task has a self-contained specification before planning starts.
- Every T004 child task plan includes an UML class diagram for all classes in that
  task, a file map, a TDD sequence, documentation targets, and verification
  commands.
- `solve-task` for implementation children writes tests and code according to the
  current plan and verifies with the narrow planned commands plus broader affected
  checks where practical.
- Architecture documentation stays synchronized with implemented packages,
  classes, configuration, and tests.
- The final T004 readiness task demonstrates whether Codegeist can replace
  OpenCode for the selected core workflows.

## Verification

Task documentation changes should run:

```bash
git --no-pager diff --check
```

Implementation child tasks must define their own narrow and broad verification in
their planning phase. At minimum, Java source changes should use targeted Maven or
Taskfile commands from `app/codegeist/cli` and report timing in solve results.

## Specification Check Result

- Phase command: direct T004 task creation from user guidance, equivalent to a
  specification phase for this new parent epic and its initial child tasks.
- Context or instructions considered: user requested a new `T004` from T003
  results, with real implementation later, all tasks created first, careful
  specification and planning before solving, UML class diagrams during planning,
  extensive documentation, and implementation based on
  `docs/developer/architecture/architecture.md`.
- Source task considered:
  `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`.
- Related source contracts considered: finalized T003 source-generation contracts
  under `docs/developer/specification/`.
- Upstream phase dependency: satisfied by finalized T003 handoffs and the current
  architecture map.
- Result: created the T004 implementation epic as specified but not planned or
  solved.
- Open decisions or blockers: child task details still need `/plan-task` passes
  before any Java source is written.
- Next recommended phase: `/plan-task t004_01` to create the first detailed
  implementation plan, UML class diagram, documentation plan, and TDD sequence for
  runtime/session/event core.

## Specification Recheck Result

- Phase command: `/specify-task T004` across the full T004 task family.
- Context or instructions considered: user input `für alle t004 tasks ausführen`,
  interpreted as rerunning specification for the parent epic and all T004 child
  tasks.
- Parent task considered: this file is the parent task.
- Adjacent child task docs considered: `T004_01` through `T004_12` under
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none for specification; finalized T003 source
  contracts and current architecture docs remain the source of truth for planning.
- Result: confirmed T004 as the real implementation epic derived from T003
  handoffs, with Java source reserved for child solve phases after implementation
  plans and UML class diagrams exist.
- Open decisions or blockers: child implementation details remain owned by each
  child `/plan-task`; no parent-level solution path is selected here.
- Next recommended phase: continue with `/solve-task t004_01`; all child tasks now
  have detailed implementation handoffs for their later solve phases.

## Planning Check Result

- Phase command: `/plan-task T004` across the full T004 task family.
- Context or instructions considered: user input `alle tasks aus t004`, interpreted
  as explicit permission to plan every existing T004 child task instead of only one
  task.
- Source task considered: this parent epic and child tasks `T004_01` through
  `T004_12`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Duplicate check result: `T004_01` already had an implementation handoff;
  implementation handoffs were missing for `T004_02` through `T004_12` and were
  created under `docs/developer/implementation/`.
- Result: all T004 child tasks now have concrete implementation plans with class
  diagrams, file maps, implementation steps, TDD or validation commands,
  dependencies, deferrals, and documentation update targets.
- Open decisions or blockers: none at planning depth; solve phases must still
  respect each task's upstream dependencies and adjust only for concrete source
  facts discovered during implementation.
- Next recommended phase: `/solve-task t004_01`.

## Creation Note

Created after `T003_12` finalized the source-generation handoff sequence and after
the user confirmed that T004 should be the real implementation phase.
