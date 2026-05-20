# T003_03 Define Testing Strategy And Agent Test Rules

Parent: `T003_define-codegeist-opencode-core-source-contracts`

Status: finalized

## Goal

Define the Codegeist development and testing strategy for T003 implementation
tasks before runtime, CLI, TUI, provider, tool, workspace, shell, storage, or
agent-loop Java code expands.

The strategy must make Test-Driven Development the default workflow, keep tests
fast enough for coding-agent iteration, make slow startup visible, and ensure
that tests can be run individually when an agent or developer needs a narrow
feedback loop.

## User Direction

The user explicitly requested that Codegeist define how development should work:

- Development should follow TDD.
- Test duration must be watched carefully.
- Test startup duration is especially important and must be watched carefully.
- Tests must be executable individually.

## Context

`T003_02` finalized `docs/developer/specification/java-generation-guidance.md` as
the Java/Spring generation guide. That guide records future test categories but
leaves detailed testing strategy, command shape, and runtime budgets to this
task.

The current Java baseline is a single Maven module under `app/codegeist/cli` with
`task test` delegating to `mvn --batch-mode --no-transfer-progress test`. The only
implemented test today is the Spring Boot context-load test
`CodegeistApplicationTests`.

## Scope

- Define TDD rules for future Codegeist implementation tasks.
- Define expectations for writing the smallest failing test first when behavior
  changes.
- Define how to keep unit, contract, adapter, Spring context, CLI/TUI, smoke, and
  native/posture tests separate.
- Define how tests must remain individually executable through Maven/JUnit
  selectors or repo task wrappers.
- Define how implementation tasks should measure or report full test duration,
  narrow test duration, and startup-sensitive test duration.
- Define when a test may load a Spring context and how to avoid unnecessary Spring
  startup in ordinary unit and contract tests.
- Define expected documentation handoff for slow tests, skipped native checks, and
  known performance blockers.

## Non-Goals

- Do not create Java source files, Java tests, Maven plugins, Taskfile commands,
  performance tooling, JUnit extensions, runtime behavior, provider calls, CLI
  commands, or TUI implementation in this specification task.
- Do not choose final numeric budgets for all test suites if current evidence is
  insufficient. Numeric budgets can be planned or solved after measuring the
  current baseline.
- Do not replace `T003_04`; startup, performance, and native smoke budgets can be
  refined there after this task defines the test workflow contract.
- Do not weaken the existing `task test` entrypoint.

## Direct Inputs

- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_02_define_java_generation_guidance.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/architecture/architecture.md`
- `app/codegeist/cli/Taskfile.yml`
- `app/codegeist/cli/pom.xml`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for testing and development
workflow. The preferred target is:

- `docs/developer/specification/testing-strategy-and-agent-rules.md`

The guidance should include:

- TDD workflow rules for coding agents and humans.
- Test taxonomy and ownership rules.
- Individual test execution requirements.
- Startup-sensitive test rules.
- Test duration measurement and reporting expectations.
- Rules for avoiding unnecessary Spring context startup.
- Guidance for adding or updating Taskfile/Maven wrappers later if needed.
- A checklist for implementation tasks before they mark code as solved.

## Acceptance Criteria

- The guidance is written in English and stored under `docs/developer/`.
- The guidance states that TDD is the default for behavior changes and bug fixes.
- The guidance requires future implementation tasks to keep tests individually
  executable or document why they cannot be isolated yet.
- The guidance requires future implementation tasks to report the targeted test
  command used and enough timing information to spot slow tests or slow startup.
- The guidance separates fast unit/contract tests from Spring context, CLI/TUI,
  smoke, native, network, provider, shell, and filesystem-heavy tests.
- The guidance defines how startup-sensitive tests should be minimized and made
  explicit instead of hidden inside ordinary unit suites.
- No runtime code, Java tests, Maven changes, Taskfile changes, or behavior are
  created by this task.

## Implementation Plan

### Selected Option

Create one durable developer specification for Codegeist testing and coding-agent
workflow rules. Keep this task documentation-only so later T003 implementation
tasks can use the guide before they add runtime, CLI, TUI, provider, tool,
workspace, shell, storage, or agent-loop code.

Do not split this into Maven, Taskfile, JUnit extension, CI, or performance-tooling
tasks yet. The current need is a clear test strategy contract; `T003_04` still
owns release, startup, native, and binary-smoke budget refinement.

### Concrete Solution Direction

Create `docs/developer/specification/testing-strategy-and-agent-rules.md` as the
planned Codegeist testing strategy and agent development workflow guide. The guide
should translate the user requirements into task-ready rules: TDD by default,
fast narrow tests, visible timing, startup-sensitive isolation, and individually
runnable tests.

The guide should anchor on the current baseline: one Maven module under
`app/codegeist/cli`, Java 25, Spring Boot 4, Spring Shell 4, Spring AI BOM,
Spring AI Agent Utils on the classpath, `task test` delegating to Maven test, and
one existing Spring Boot context-load test. It should label future test categories
as planned expectations, not current implemented coverage.

### Planned Files And Targets

- Add `docs/developer/specification/testing-strategy-and-agent-rules.md`.
- Update this task with solve/finalization notes after the guide is written.
- Update `docs/developer/architecture/architecture.md` only if the new guide needs
  to be listed as a related specification or if current-state test documentation
  would otherwise become stale.
- Update `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
  during solve or finalization if the completed guide changes parent progress
  notes or later child-task boundaries.
- Update `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_04_define_build_release_and_binary_smoke_strategy.md`
  only if this strategy changes `T003_04` inputs around startup, native, or binary
  smoke-test ownership.
- Update `docs/memory-bank/chat.md` only if the solve result changes active T003
  status or next recommended work for future sessions.
- Do not change Java source, Java tests, package directories, Maven files,
  Taskfiles, CI workflows, Spring configuration, runtime behavior, provider calls,
  CLI commands, TUI implementation, shell execution, storage, or release behavior.

### Guidance Document Structure

The new guide should use this structure unless the solve phase finds a clearer
equivalent while preserving the same scope:

1. Purpose and status: explain that the document defines the development and test
   workflow for future implementation tasks and does not add current test code.
2. Current baseline: summarize `app/codegeist/cli`, `Taskfile.yml`, `pom.xml`, and
   `CodegeistApplicationTests` so future agents know what exists today.
3. TDD workflow rules: require the smallest failing test first for behavior
   changes and bug fixes; require explicit solve-note justification when a task
   cannot reasonably be test-first.
4. Test taxonomy: define fast unit, contract, adapter, Spring context, CLI/TUI,
   smoke, native/posture, network/provider, shell/process, and filesystem-heavy
   tests, with ownership and default isolation rules for each category.
5. Individual execution contract: define Maven/JUnit selector examples first,
   including class and method-level selectors, and describe when later Taskfile
   wrappers are worth adding.
6. Startup-sensitive rules: define when Spring context, CLI startup, provider
   initialization, native startup, and process startup are allowed, and require
   these costs to be explicit rather than hidden in ordinary unit suites.
7. Duration measurement and reporting: define how solve results should report the
   targeted command, broad command when run, approximate duration, startup-heavy
   checks, and slow-test concerns without pasting raw logs into task files.
8. Spring context boundaries: define when `@SpringBootTest` or similar context
   loading is justified and when plain JVM tests or sliced adapter tests are the
   default.
9. Agent checklist: provide a short checklist for future implementation tasks
   covering failing test first, narrow rerun command, broad verification, timing,
   startup posture, isolation, and skipped slow checks.
10. Handoff to later tasks: identify which rules are stable now and which numeric
    budgets or release/native smoke thresholds remain owned by `T003_04` or later
    implementation tasks.

### Implementation Steps

1. Re-read this task, the T003 parent, `T003_02`,
   `docs/developer/specification/java-generation-guidance.md`, the current-state
   architecture map, `app/codegeist/cli/Taskfile.yml`, and `app/codegeist/cli/pom.xml`.
2. Create `docs/developer/specification/testing-strategy-and-agent-rules.md` with
   the planned structure above.
3. In the guide, separate current implemented tests from planned test categories
   so future agents do not claim unit, contract, CLI/TUI, provider, shell, native,
   or smoke coverage already exists.
4. Add concrete Maven/JUnit selector examples for individually runnable tests,
   starting from standard Surefire-style class and method selectors.
5. Add the TDD workflow and solve-result reporting rules, including what to record
   when a test-first path, slow check, native check, provider/network check, or
   startup-heavy check is skipped.
6. Add Spring startup minimization rules that prefer plain JVM unit or contract
   tests unless Spring binding, bean wiring, command registration, or application
   startup is the behavior under test.
7. Add a concise implementation-task checklist that future T003 tasks can reuse.
8. Update task and related documentation references only where the completed guide
   changes durable task state or navigation.

### Verification Strategy

- Required for this documentation-only plan and solve:

```bash
git --no-pager diff --check
```

- Do not run `task test`, `task build`, `task native`, or Maven commands unless
  the solve phase unexpectedly changes Java, Maven, Taskfile, Spring
  configuration, test, runtime, or build files.
- If the guide includes command examples, keep them clearly labeled as future or
  current examples and do not claim they were executed unless verification really
  ran them.

### Dependencies And Tradeoffs

- Depends on the finalized `T003_02` Java generation guidance, which already sets
  the default that generated Java code must include tests with behavior changes.
- Depends on the current-state architecture remaining accurate: only the Spring
  Boot context-load test exists today.
- Depends on `T003_04` for hard numeric startup, native, release, and binary smoke
  budgets; this task should define reporting shape and isolation rules without
  prematurely choosing all thresholds.
- Using Maven/JUnit selectors first avoids adding wrappers before there are enough
  repeated commands to justify them. Later tasks may add Taskfile wrappers when a
  pattern becomes stable.
- Keeping solve notes concise avoids turning task files into timing logs while
  still making slow tests and startup-heavy checks visible.

### Open Questions

None for planning. The solve phase can write the testing strategy from the current
task contract without another specification pass.

## Plan Workflow Handoff

- Phase command: `/plan-task t003_03`.
- Source task:
  `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_03_define_testing_strategy_and_agent_test_rules.md`.
- Parent task considered:
  `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`.
- User context or instructions considered: user requested planning with task
  reference `t003_03` and provided no extra narrowing instructions.
- Selected option: keep `T003_03` as one documentation-only implementation plan
  that creates
  `docs/developer/specification/testing-strategy-and-agent-rules.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Related context files read:
  `docs/developer/specification/java-generation-guidance.md`,
  `docs/developer/architecture/architecture.md`, `app/codegeist/cli/Taskfile.yml`,
  and `app/codegeist/cli/pom.xml`.
- Upstream phase dependency: satisfied by the recorded specification status and
  top-level `Status: specified` that existed before this planning pass.
- Result: the task is now implementation-ready as a documentation-only testing
  strategy task.
- Open decisions or blockers: none for planning. Hard numeric startup, native,
  release, and binary smoke budgets remain owned by `T003_04` or later
  implementation tasks.
- Next recommended phase: `/solve-task t003_03` to write
  `docs/developer/specification/testing-strategy-and-agent-rules.md` without
  changing runtime code.

## Solve Workflow Handoff

- Phase command: `/solve-task t003_03`.
- User context or instructions considered: user requested solving with task
  reference `t003_03` and provided no extra narrowing instructions.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Upstream phase dependency: satisfied by the recorded `/plan-task t003_03`
  handoff and top-level `Status: planned` before this solve pass.
- Result: created
  `docs/developer/specification/testing-strategy-and-agent-rules.md` as the
  durable Codegeist testing strategy and coding-agent workflow guide.
- Related documentation updated:
  `docs/developer/architecture/architecture.md` now links the guide as planned
  workflow guidance and still describes the current implemented test state
  accurately.
- Runtime/build impact: no Java source, Java tests, package directories, Maven
  files, Taskfile commands, CI workflows, Spring configuration, runtime behavior,
  provider calls, CLI commands, TUI implementation, shell execution, storage, or
  release behavior were changed.
- Verification: `git --no-pager diff --check` passed.
- Acceptance criteria status: satisfied. The guide is in English under
  `docs/developer/`, makes TDD the default for behavior changes and bug fixes,
  requires individually executable tests or documented blockers, requires targeted
  commands and timing information in solve results, separates fast unit/contract
  tests from startup-heavy and external categories, and keeps Spring context
  startup explicit.
- Open decisions or blockers: none for this solve pass. Hard numeric startup,
  native, release, and binary-smoke thresholds remain owned by `T003_04` or later
  implementation tasks.
- Next recommended phase: `/finalize-task t003_03` to review cross-task impact,
  especially `T003_04` and later implementation child tasks.

## Finalization Workflow Handoff

- Phase command: `/finalize-task t003_03`.
- User context or instructions considered: user requested finalization with task
  reference `t003_03` and provided no extra narrowing instructions.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and the Codegeist task and
  architecture overlays.
- Upstream phase dependency: satisfied by the recorded `/solve-task t003_03`
  handoff and top-level `Status: solved` before this finalization pass.
- Impacted tasks: the T003 parent progress note now records `T003_03` as
  finalized, and `T003_04` now records that it should consume the finalized
  testing guide for timing-reporting shape, startup-sensitive checks, individual
  execution expectations, and skipped/failed slow-check reporting.
- Documentation review: `docs/developer/specification/testing-strategy-and-agent-rules.md`
  and `docs/developer/architecture/architecture.md` were already current from
  the solve pass. `docs/memory-bank/chat.md` was refreshed from solved to
  finalized task state.
- Runtime/build impact: no Java source, Java tests, package directories, Maven
  files, Taskfile commands, CI workflows, Spring configuration, runtime behavior,
  provider calls, CLI commands, TUI implementation, shell execution, storage, or
  release behavior were changed.
- Verification: `git --no-pager diff --check` passed.
- Remaining follow-ups: `T003_04` still owns hard numeric startup, release,
  native, platform, and binary-smoke thresholds.
- Result: finalization complete; this task is now finalized.
- Next recommended phase: `/plan-task t003_04` when ready.

## Planning-Readiness Questions

- Which timing numbers should be measured from the current baseline before setting
  hard budgets?
- Should individual test execution rely only on Maven/JUnit selectors first, or
  should later tasks add Taskfile wrappers for common single-test commands?
- Which test categories must be allowed to start Spring, and which must stay plain
  JVM tests by default?
- How should agents record slow-test evidence in task solve results without
  turning task files into raw command logs?
- Which rules belong in the durable developer spec and which stricter numeric
  thresholds belong in `T003_04`?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: direct task creation from user guidance, equivalent to the
  specification phase for this new child task.
- Context or instructions considered: user requested a definition for how
  development should work, specifically TDD, careful test-duration and test-startup
  monitoring, and individually executable tests.
- Parent task considered:
  `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`.
- Adjacent child tasks considered:
  `T003_01_analyze_spring_ai_agent_utils_adoption.md` and
  `T003_02_define_java_generation_guidance.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md` through the
  existing T003 Java/Spring guidance workflow.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Upstream phase dependency: none; this task records the initial specification for
  the planned `T003_03` child slot.
- Result: task is specified as a documentation-only testing and development
  strategy task.
- Open decisions or blockers: hard numeric budgets require measurement or a later
  planning pass; `T003_04` owns detailed performance/startup/native budgets.
- Next recommended phase: `/plan-task t003_03` to define the concrete developer
  document structure and verification handoff.

## Creation Note

Created after the user clarified that Codegeist development must explicitly define
TDD, test runtime monitoring, test startup monitoring, and individually runnable
tests before broad T003 Java implementation starts.
