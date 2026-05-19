# T003_02 Define Java Generation Guidance

Parent: `T003_implement-codegeist-opencode-core-application`

Status: finalized

## Goal

Define Codegeist-specific Java and Spring generation guidance for coding agents
before the broad T003 implementation tasks create runtime, provider, tool,
workspace, CLI, or TUI source code.

The guidance should make later generated Java code small, testable, and aligned
with Codegeist-owned architecture instead of copying OpenCode, Spring AI Agent
Utils, or generic framework package layouts.

## Context

`T003_01` finalized the Spring AI Agent Utils boundary decision. Agent Utils may
be used directly inside implementation code when useful, but Codegeist contracts
must stay independent and adapters should appear only for concrete boundary
needs.

The T003 parent now targets both CLI and TUI core behavior while keeping JBang,
PF4J, Vaadin, headless server, and API/SDK surfaces in the backlog. Java source
generation guidance should reflect that scope so later implementation tasks do
not accidentally create deferred extension, plugin, web, or API behavior.

## Scope

- Define package and dependency direction guidance for the single current Maven
  module under `app/codegeist/cli`.
- Keep the guidance aligned with the current implemented state: only
  `ai.codegeist.app` exists in source today, while runtime, session, event,
  context, provider, tool, permission, workspace, patch/edit, shell, storage,
  CLI command, and TUI packages are planned boundaries.
- Define when to create Java records, interfaces, services, adapters,
  configuration properties, typed errors, and tests.
- Define how generated code should preserve runtime-owned boundaries for
  sessions, events, context, provider calls, tools, permissions, workspace,
  patch/edit, shell, storage, CLI, and TUI.
- Define how Spring Boot, Spring AI, Spring Shell, and Spring AI Agent Utils may
  be used without leaking framework or utility types into Codegeist domain
  contracts.
- Define test expectations that later T003 implementation tasks must satisfy when
  they generate Java source.
- Define non-goals and deferred surfaces so generated code does not implement
  JBang, PF4J, Vaadin, headless server, or API/SDK behavior in T003 core tasks.

## Specification Boundaries

- Treat this as a developer-guidance documentation task, not a Java
  implementation task.
- The guidance may name planned Codegeist packages and illustrative Java/Spring
  shapes, but it must label them as guidance for future code generation rather
  than current implementation.
- Use OpenCode and Spring AI Agent Utils as behavior and test-pattern references,
  not as package-layout or architecture blueprints.
- Keep Codegeist contracts independent from Spring AI, Spring Shell, Agent Utils,
  provider SDKs, and UI adapters. Framework and utility types may appear in
  implementation examples only below Codegeist-owned boundaries.
- Keep tests as a fixed expectation for future generated code. This task may
  define test categories and examples, but it must not add Java test source or
  run Maven verification unless build/runtime files change unexpectedly.
- Keep deferred surfaces explicit: T003 needs CLI and TUI core behavior, while
  JBang, PF4J, Vaadin, headless server, and API/SDK work remain backlog surfaces
  that should only shape adapter-ready boundaries.

## Non-Goals

- Do not create Java source files, Java tests, packages, Spring beans, runtime
  behavior, provider callbacks, storage adapters, shell executors, CLI commands,
  or TUI implementation in this guidance task.
- Do not introduce new Maven modules or move source directories.
- Do not add dependencies or change the build baseline.
- Do not reopen the finalized Agent Utils boundary decision from `T003_01`.
- Do not implement deferred JBang, PF4J, Vaadin, server, API, or SDK surfaces.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_01_analyze_spring_ai_agent_utils_adoption.md`
- `docs/developer/spring-ai-agent-utils-adoption.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `docs/developer/architecture/architecture.md`
- `.oc_local/rules/codegeist-task-specification.md`
- `docs/tasks/hints/java-spring-architecture-planning-guidance.md`

## Deliverables

Create or update a durable developer guidance document for Java generation. The
preferred target is:

- `docs/developer/specification/java-generation-guidance.md`

The guidance should include:

- Package ownership and dependency direction rules.
- Code shape rules for records, interfaces, services, adapters, configuration,
  errors, and event/result types.
- Spring Boot, Spring AI, Spring Shell, and Agent Utils boundary rules.
- CLI and TUI adapter expectations.
- Test-generation expectations for unit, contract, Spring context, CLI/TUI, and
  smoke tests.
- Examples that are clearly illustrative and not committed as source code.
- A short checklist for later T003 implementation tasks.

## Acceptance Criteria

- The guidance is written in English and stored under `docs/developer/`.
- The guidance is specific enough for later coding agents to generate Java source
  without inventing package structure or bypassing Codegeist boundaries.
- The guidance keeps Codegeist domain contracts independent from Spring AI Agent
  Utils architecture and raw broad provider callbacks.
- The guidance treats CLI and TUI as required T003 core client surfaces while
  keeping JBang, PF4J, Vaadin, server, and API/SDK work deferred.
- The guidance defines test expectations for generated code instead of leaving
  tests as optional follow-up work.
- No runtime code, tests, package directories, Maven changes, or behavior are
  created by this task.

## Implementation Plan

### Selected Option

Sharpen the existing `T003_02` task into one documentation-only implementation
slice that creates the Codegeist Java/Spring generation guide. Do not split this
into separate package, framework, test, CLI, or TUI guidance tasks yet, because
later T003 implementation tasks need one compact generation contract before they
start adding source code.

### Concrete Solution Direction

Create `docs/developer/specification/java-generation-guidance.md` as a planned
architecture and implementation-guidance document for future Java source
generation. The guide should be specific enough for coding agents to generate
small, testable Java/Spring code in the current single Maven module under
`app/codegeist/cli` without assuming planned packages already exist.

The guide should translate the existing Codegeist blueprints into code-generation
rules. It should not reopen the Agent Utils boundary decision, introduce a new
module layout, choose a TUI library, or implement runtime behavior.

### Planned Files And Targets

- Add `docs/developer/specification/java-generation-guidance.md`.
- Update this task file with solve/finalization notes when the guide is written.
- Update `docs/memory-bank/chat.md` if the solve phase changes the active T003
  focus, the current guidance status, or the next recommended T003 task.
- Optionally update `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
  during solve or finalization if the finished guide changes parent progress notes
  or later child-task boundaries.
- Do not change Java source, Java tests, package directories, Maven files,
  Taskfiles, Spring configuration, runtime behavior, provider callbacks, CLI
  commands, or TUI implementation.

### Guidance Document Structure

The new guide should use this structure unless the solve phase finds a clearer
equivalent while keeping the same scope:

1. Purpose and status: explain that the document is planned generation guidance,
   not current implementation architecture.
2. Current baseline: summarize the implemented state from
   `docs/developer/architecture/architecture.md`, including that only
   `ai.codegeist.app` exists in source and the build is one Maven module under
   `app/codegeist/cli`.
3. Package ownership map: define planned package responsibilities for
   `ai.codegeist.app`, `ai.codegeist.cli`, a planned TUI adapter package,
   `ai.codegeist.runtime`, `ai.codegeist.agent`, `ai.codegeist.session`,
   `ai.codegeist.event`, `ai.codegeist.context`, `ai.codegeist.provider`,
   `ai.codegeist.tool`, `ai.codegeist.permission`, `ai.codegeist.workspace`,
   `ai.codegeist.patch`, `ai.codegeist.shell`, and `ai.codegeist.storage` while
   labeling deferred server, Vaadin, PF4J, JBang, API, and SDK packages as later
   surfaces.
4. Dependency direction rules: keep clients depending inward on runtime
   contracts, keep provider/tool/workspace/permission/storage behind Codegeist
   ports, and prevent Spring Shell, Spring AI, Agent Utils, TUI, provider SDK,
   storage adapter, or deferred-surface types from leaking into domain contracts.
5. Code shape rules: define when to generate records, value objects, enums,
   sealed interfaces, small ports, services, adapters, configuration properties,
   typed errors, results, and runtime events.
6. Spring and Agent Utils boundary rules: document where Spring Boot, Spring AI,
   Spring Shell, and Agent Utils may appear as implementation dependencies and
   where Codegeist-owned contracts must hide them.
7. CLI and TUI adapter expectations: keep both as runtime clients that collect
   input, render events, and collect approval decisions without owning sessions,
   provider calls, tool execution, permission policy, storage, or context loading.
8. Test-generation expectations: name the contract, unit, Spring context,
   adapter, CLI/TUI, smoke, and native/posture tests future implementation tasks
   must add with code changes, and explain what each category proves.
9. Illustrative examples: include concise Java/Spring snippets for representative
   records, ports, service methods, adapters, configuration properties, typed
   errors, and tests. Mark examples as illustrative and not committed source.
10. Later-task checklist: provide a short checklist for future T003 tasks to use
    before adding Java source, provider callbacks, tool execution, CLI commands,
    TUI behavior, or storage adapters.

### Implementation Steps

1. Re-read the source task, parent task, Agent Utils boundary guide, current-state
   architecture map, and Codegeist/OpenCode parity architecture before writing.
2. Create `docs/developer/specification/java-generation-guidance.md` with the
   planned structure above.
3. In the guide, explicitly separate current implemented source from planned
   package boundaries so future agents do not treat planned packages as existing
   code.
4. Add a package ownership table and a small dependency-direction diagram that
   show the current single-module posture and planned logical boundaries.
5. Add code shape guidance for records, identifiers, sealed interfaces, ports,
   services, adapters, configuration properties, typed failures, results, and
   events.
6. Add Spring Boot, Spring Shell, Spring AI, and Agent Utils boundary guidance
   that preserves Codegeist-owned contracts and direct internal Agent Utils use
   only when policy boundaries are already owned by Codegeist.
7. Add CLI and TUI adapter rules that make both clients of the same runtime,
   session, event, and permission contracts.
8. Add future test-generation expectations without creating test source files.
   Include examples for fast contract/unit tests, focused Spring context tests,
   adapter tests, CLI/TUI smoke checks, and native/posture reporting.
9. Add the final checklist for later T003 implementation tasks.
10. Update this task's solve result when the guide is complete, and update memory
    only if the solve result changes active project focus or next steps.

### Verification Strategy

- Required for this documentation-only plan and solve:

```bash
git --no-pager diff --check
```

- Do not run `task test`, `task build`, `task native`, or Maven commands unless
  the solve phase unexpectedly changes Java, Maven, Taskfile, Spring
  configuration, test, or runtime files.
- If the guide discusses native status, keep it as future test-generation posture;
  do not claim native validation was run for this documentation task.

### Dependencies And Tradeoffs

- Depends on the finalized `T003_01` Agent Utils boundary guide and the T003 parent
  decision that CLI and TUI are core while JBang, PF4J, Vaadin, server, and
  API/SDK surfaces stay deferred.
- Depends on current-state architecture accuracy: only `ai.codegeist.app` exists
  in source today.
- Depends on the T002 blueprints and `docs/developer/specification/codegeist-opencode-parity.md`
  for planned runtime, session, event, context, provider, tool, permission,
  workspace, patch/edit, shell, storage, CLI, and TUI boundaries.
- The single-guide approach avoids duplicated guidance across later tasks, but the
  solve phase must keep the guide concise enough that future coding agents can use
  it as a checklist rather than a second broad architecture document.
- Detailed test command choices for implementation tasks remain owned by
  `T003_03`; this task should define categories and expectations without
  replacing that later testing-strategy task.

### Open Questions

None for planning. The solve phase can write the guide from the current task
contract without another specification pass.

## Planning-Readiness Questions

- Which package ownership rules are specific enough to guide first-wave runtime,
  session, event, context, provider, tool, permission, workspace, patch/edit,
  shell, storage, CLI, and TUI source generation without forcing a premature Maven
  module split?
- Which Codegeist contract types should be records, sealed interfaces, small
  service interfaces, configuration properties, typed errors, or adapter classes?
- Where may Spring Boot, Spring AI, Spring Shell, and Agent Utils appear as
  implementation dependencies, and where must Codegeist-owned contracts hide
  those types?
- How should the guidance distinguish current source state from planned package
  boundaries so future agents do not claim unimplemented packages already exist?
- Which test categories must future implementation tasks add with generated Java
  code, and which documentation-only examples are enough for this guidance task?
- What checklist should later T003 implementation tasks use before adding source
  files, exposing provider callbacks, executing tools, or adding CLI/TUI behavior?

## Verification

```bash
git --no-pager diff --check
```

## Solve Result

- Phase command: `/solve-task`.
- Context or instructions considered: user requested solving this task with task
  reference `t003_02` and provided no extra implementation instructions.
- Upstream phase dependency: satisfied by the current `/plan-task` status,
  implementation plan, planned file target, ordered steps, acceptance criteria,
  and verification strategy in this task.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`,
  `.oc_local/rules/architecture-doc.md`, and
  `.oc_local/rules/third-party-analysis-workflow.md`.
- Related context files read:
  `docs/developer/architecture/architecture.md`,
  `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/runtime-vocabulary.md`,
  `docs/developer/specification/runtime-session-event-contracts.md`,
  `docs/developer/specification/provider-configuration-contracts.md`,
  `docs/developer/specification/tool-permission-workspace-contracts.md`,
  `docs/developer/specification/extension-client-readiness-gates.md`, and
  `docs/developer/spring-ai-agent-utils-adoption.md`.
- Implemented solution: created
  `docs/developer/specification/java-generation-guidance.md` as the durable
  Codegeist Java/Spring generation guide.
- Documentation updates made: updated this task, the T003 parent progress note,
  the current-state architecture related-docs list, and `docs/memory-bank/chat.md`
  to point future sessions at the new guide.
- Runtime changes: none. No Java source, tests, package directories, Maven files,
  Taskfiles, Spring configuration, runtime behavior, provider callbacks, CLI
  commands, TUI implementation, storage adapters, shell executors, PF4J, JBang,
  Vaadin, server, or API/SDK behavior were added.
- Acceptance criteria status: satisfied. The guide is English, lives under
  `docs/developer/specification/`, defines package ownership and dependency
  direction, keeps Codegeist contracts independent from Agent Utils and framework
  types, treats CLI and TUI as T003 core clients while keeping deferred surfaces
  out of scope, defines future test-generation expectations, and avoids runtime
  code changes.
- Verification: `git --no-pager diff --check`.
- Open decisions or blockers: none for this solve. `T003_03` still owns the more
  detailed testing strategy and concrete command budgets.
- Next recommended phase: `/finalize-task t003_02` to review cross-task impact and
  confirm whether later T003 child tasks or documentation need updates before this
  task is finalized.

## Finalization Result

- Phase command: `/finalize-task t003_02`.
- Context or instructions considered: user requested finalization for `t003_02`
  and provided no extra narrowing instructions.
- Upstream phase dependency: satisfied. The task had top-level `Status: solved`
  and a current successful `/solve-task` result before this finalization pass.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`,
  `.oc_local/rules/architecture-doc.md`, and
  `.oc_local/rules/third-party-analysis-workflow.md`.
- Impacted tasks: the T003 parent now records `T003_02` as finalized;
  `T003_03` and `T003_04` now explicitly treat
  `docs/developer/specification/java-generation-guidance.md` as finalized input.
- Documentation review applied using the local `update-documentation` semantics:
  checked the current-state architecture map, project memory, T003 parent task,
  adjacent T003 child tasks, and the generated Java guidance document for stale
  references.
- Documentation updates made during finalization: updated the T003 parent progress
  note, updated `T003_03` and `T003_04` dependency notes, and refreshed
  `docs/memory-bank/chat.md` so future sessions see `T003_02` as finalized.
- Remaining follow-ups: `/plan-task t003_03` should define the detailed TDD and
  testing strategy; `/plan-task t003_04` should define the build, release,
  platform, and binary-smoke strategy.
- Runtime changes: none.
- Verification: `git --no-pager diff --check`.
- Open decisions or blockers: none for finalizing this task.
- Result: finalized.
- Next recommended phase: plan `T003_03` or `T003_04` when ready.

## Plan Workflow Handoff

- Phase command: `/plan-task`.
- Source task:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_02_define_java_generation_guidance.md`.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- User context or instructions considered: user requested planning with task
  reference `t003_02` and provided no extra narrowing instructions.
- Selected option: sharpen the existing `T003_02` task as one documentation-only
  implementation plan that creates
  `docs/developer/specification/java-generation-guidance.md`.
- Duplicate check result: no existing duplicate Java generation guidance task or
  `java-generation-guidance.md` document was found under `docs/tasks/` or
  `docs/developer/specification/`; the existing `T003_02` task is the target.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`,
  `.oc_local/rules/architecture-doc.md`, and
  `.oc_local/rules/third-party-analysis-workflow.md`.
- Related context files read:
  `docs/developer/spring-ai-agent-utils-adoption.md`,
  `docs/developer/architecture/architecture.md`, and
  `docs/developer/specification/codegeist-opencode-parity.md`.
- Upstream phase dependency: satisfied by the recorded `/specify-task` pass and
  top-level `Status: specified` that existed before this planning pass.
- Result: the task is now implementation-ready as a documentation-only developer
  guidance task.
- Open decisions or blockers: none for planning.
- Next recommended phase: `/solve-task` for this same task to write
  `docs/developer/specification/java-generation-guidance.md` without changing
  runtime code.

## Specification Check Result

- Phase command: `/specify-task`.
- Context or instructions considered: user requested a specification pass for
  this task by exact repo-relative path and provided no extra narrowing
  instructions beyond the shared `/specify-task` command contract.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_01_analyze_spring_ai_agent_utils_adoption.md`.
  The parent lists later T003 child slots, but only `T003_01` and `T003_02` are
  currently created.
- Dependency inputs considered:
  `docs/developer/spring-ai-agent-utils-adoption.md`,
  `docs/developer/specification/codegeist-opencode-parity.md`, and
  `docs/developer/architecture/architecture.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`. These were used only
  for specification boundaries, source-evidence posture, Java/Spring guidance
  shape, and planning-readiness questions, not as an implementation plan.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`,
  `.oc_local/rules/architecture-doc.md`, and
  `.oc_local/rules/third-party-analysis-workflow.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: task is specified as a documentation-only Java/Spring generation
  guidance task with explicit package-boundary, framework-boundary,
  test-expectation, current-state, and deferred-surface constraints.
- Open decisions or blockers: no blockers for planning. The next phase still
  needs to choose the concrete documentation structure, examples, checklist
  shape, and exact verification handoff without creating runtime code.
- Next recommended phase: `/plan-task` for this same task to define the concrete
  developer-document plan for `docs/developer/specification/java-generation-guidance.md`.

## Creation Note

Created as the next T003 child task after `T003_01` was finalized and the parent
scope was adjusted so T003 includes CLI and TUI core behavior while moving JBang
and PF4J implementation to the backlog.
