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

- `T004_01_implement_runtime_session_event_core/task.md`
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

All child tasks have been specified and planned. The Agent Utils equivalence scan
below has been folded into each child implementation handoff, so solve phases may
start in dependency order.

## Default Phase Hints

- Apply `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md` during every
  T004 Java implementation specify, plan, and solve phase.
- Before specifying, planning, designing, or writing Java implementation code, use
  the local `/ask-project spring-ai-agent-utils "<specific implementation
  question>"` workflow from `.oc_local/commands/ask-project.md` to check what
  Spring AI Agent Utils already implements.
- If an existing Agent Utils implementation fits after Codegeist validates policy,
  workspace, permissions, session/event projection, output bounding, and result
  mapping, use it as a private implementation detail or add only a thin Codegeist
  wrapper.
- If it does not fit, keep the Codegeist-owned contract and cite Agent Utils only
  as concept or test-pattern evidence.
- When the needed behavior is not already implemented in Java or covered by a
  suitable Spring AI Agent Utils equivalent, use `/ask-project opencode
  "<specific implementation question>"` to inspect how OpenCode implements it
  before translating the behavior into Codegeist's Java-first contracts.

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

## Spring AI Agent Utils Equivalence Scan

This specification pass compared every T004 task with
`spring-ai-community/spring-ai-agent-utils` using the local third-party workspace
under `docs/third-party/spring-ai-agent-utils/` and the adoption guide in
`docs/developer/spring-ai-agent-utils-adoption.md`.

Classification rules for later planning:

- `Direct candidate` means the solve phase may call the Agent Utils class as a
  private implementation detail after Codegeist policy validates the request.
- `Adapter candidate` means a Codegeist mapper or callback boundary is probably
  needed before provider/tool exposure.
- `Concept reference` means use the upstream behavior and tests for guidance, not
  as Codegeist public architecture.
- `No equivalent` means keep the planned Codegeist-owned contract and do not add an
  Agent Utils dependency only for that task.

| Child task | Closest Agent Utils equivalent | Classification | Specification consequence |
| --- | --- | --- | --- |
| `T004_01` runtime/session/event | `TaskCall`, `BackgroundTask`, `TodoWriteTool` event handling concepts | Concept reference only | Keep runtime, session, event, turn, message, and envelope contracts Codegeist-owned; do not model them on Agent Utils task or todo types. |
| `T004_02` context/workspace loading | `GrepTool`, `GlobTool`, `ListDirectoryTool`, `FileSystemTools.read`, `Skills` path loading | Direct candidates behind workspace policy | Planning must decide where Codegeist validates roots, ignored/generated paths, size bounds, and output normalization before calling any utility. |
| `T004_03` provider/Spring AI adapter | Agent Utils `ChatClient.Builder` usages in subagents and memory advisor | Concept reference only | Keep provider configuration on Spring AI and Codegeist contracts; do not treat Agent Utils as provider configuration infrastructure. |
| `T004_04` tool/permission/workspace | Agent Utils tool catalog plus `AskUserQuestionTool` | Adapter/concept candidates | Use Agent Utils tool definitions as catalog evidence, but Codegeist owns descriptors, mode gates, permission decisions, workspace target validation, and result mapping. |
| `T004_05` patch/edit proposal | `FileSystemTools.edit` and `FileSystemTools.write` | Deferred adapter candidate | Do not expose direct writes; planning must preserve proposal, approval, freshness, and typed apply-result boundaries before any file mutation utility is considered. |
| `T004_06` controlled shell | `ShellTools` and `AgentEnvironment` | Deferred adapter candidate | Keep fake executor first; any real process execution must stay behind Codegeist shell request, cwd, permission, timeout, and bounded-output policy. |
| `T004_07` storage/session continuation | `AutoMemoryTools`, `AutoMemoryToolsAdvisor`, `TaskRepository`, `DefaultTaskRepository` | Concept reference only | Use memory and task repository ideas only as references; Codegeist session continuation, redaction, projection, and storage ports remain independent. |
| `T004_08` CLI prompt commands | `AskUserQuestionTool`, `CommandLineQuestionHandler`, bundled agent prompts | Concept reference only | Spring Shell command behavior stays Codegeist-owned; Agent Utils may inform later interactive question or approval UX, not `plan`/`build` command contracts. |
| `T004_09` end-to-end agent loop | `TaskTool`, `TaskOutputTool`, Claude/A2A subagent SPI, `AutoMemoryToolsAdvisor`, `SkillsTool` | Deferred concept/reference | Minimal loop stays runtime-owned with fakes first; nested subagents, skills, and memory advisors are later gated surfaces unless explicitly planned. |
| `T004_10` CLI parity workflows | Agent Utils Claude Code-inspired tools, built-in subagents, and prompt resources | Behavior reference | Use as secondary Java/Spring evidence for Claude Code-like workflows; OpenCode parity docs remain the primary workflow target. |
| `T004_11` packaging/native/startup | No focused equivalent beyond the dependency itself | No equivalent | Planning must treat Agent Utils as a dependency and native/startup risk to verify, not as a packaging helper. |
| `T004_12` core replacement readiness | The overall Agent Utils adoption boundary guide | Evidence input | Readiness reporting must mention which Agent Utils candidates were adopted, wrapped, deferred, or rejected, but readiness cannot depend on raw Agent Utils architecture. |

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

## Agent Utils Equivalence Specification Result

- Phase command: `/specify-task T004` across the full T004 task family.
- Context or instructions considered: user requested a suitable Spring AI Agent
  Utils equivalent for every task in T004, using
  `https://github.com/spring-ai-community/spring-ai-agent-utils` as the reference.
- Parent task considered: this file.
- Adjacent child task docs considered: `T004_01` through `T004_12` under
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Third-party evidence considered:
  `docs/third-party/spring-ai-agent-utils/README.md`,
  `docs/third-party/spring-ai-agent-utils/ANALYSIS_REPORT.md`,
  `docs/developer/spring-ai-agent-utils-adoption.md`, and Agent Utils docs for
  `FileSystemTools`, `GrepTool`, `GlobTool`, `ShellTools`, `TaskTool`,
  `AutoMemoryTools`, `AutoMemoryToolsAdvisor`, and `AskUserQuestionTool`.
- Upstream phase dependency: none for specification; existing T004 planning
  outputs are now stale enough to need a plan recheck before solving.
- Result: recorded the Agent Utils equivalence matrix and kept Codegeist-owned
  contracts independent from Agent Utils public architecture.
- Open decisions or blockers: each child `/plan-task` recheck must decide whether
  to keep the existing implementation plan unchanged, add a direct internal Agent
  Utils call, add a Codegeist adapter, or explicitly defer the equivalent.
- Next recommended phase: `/plan-task t004_01` to recheck the first implementation
  handoff against the Agent Utils equivalence scan before solving.

## Agent Utils Planning Recheck Result

- Phase command: `/plan-task T004` across the full T004 task family.
- Context or instructions considered: user requested the planning recheck for all
  T004 tasks after the Agent Utils equivalence scan.
- Parent task considered: this file.
- Child task docs considered: `T004_01` through `T004_12` under
  `docs/tasks/T004_implement-codegeist-opencode-core-application/tasks/`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Related context files read: existing implementation handoffs under
  `docs/developer/implementation/`, `docs/developer/spring-ai-agent-utils-adoption.md`,
  and Spring AI Agent Utils third-party analysis docs.
- Selected option: sharpen the existing T004 child implementation tasks and their
  handoffs instead of creating duplicates.
- Duplicate check result: matching implementation handoff files already existed for
  every T004 child task.
- Result: each implementation handoff now records how the closest Agent Utils
  equivalent affects the concrete plan, tests, deferrals, and solve boundary.
- Open decisions or blockers: none at planning depth; solve phases must still
  respect task dependency order and may adjust implementation details only for
  concrete source/API facts.
- Next recommended phase: `/solve-task t004_01`.

## Phase Hint Update Result

- Context or instructions considered: before T004 Java implementation phases,
  always use `.oc_local/commands/ask-project.md` with project
  `spring-ai-agent-utils` to check what Agent Utils already implements; when it
  fits, use it directly or through a thin Codegeist wrapper.
- Result: added `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md` and
  linked it from this task's default phase hints.
- Phase requirement: each T004 specify, plan, and solve phase must check the
  relevant Agent Utils source evidence first, then decide whether to use the
  existing implementation, add a thin wrapper for Codegeist policy/result mapping,
  or keep a Codegeist-owned implementation with Agent Utils as concept/test
  evidence only.
- OpenCode fallback: when Agent Utils has no fitting Java-side behavior, the phase
  must use `/ask-project opencode ...` to inspect the implemented OpenCode
  behavior before designing the Codegeist equivalent.

## Creation Note

Created after `T003_12` finalized the source-generation handoff sequence and after
the user confirmed that T004 should be the real implementation phase.
