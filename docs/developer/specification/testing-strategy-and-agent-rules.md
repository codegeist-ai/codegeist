# Testing Strategy And Agent Rules

Codegeist testing and development workflow guidance for future T003
implementation tasks.

## Purpose And Status

This document defines how Codegeist implementation work should be developed and
verified. It is planned workflow guidance: it does not add test source, Maven
configuration, Taskfile commands, runtime behavior, provider calls, CLI commands,
or TUI implementation.

Use this guide before adding or changing Java behavior in T003 tasks. It turns the
project requirements into practical rules for coding agents and humans:

- Use Test-Driven Development by default for behavior changes and bug fixes.
- Keep narrow tests fast enough for coding-agent feedback loops.
- Make test duration and startup-heavy checks visible in solve results.
- Keep tests individually executable through Maven, JUnit, or later repo wrappers.
- Avoid hidden Spring, CLI, provider, process, or native startup inside ordinary
  unit suites.

Hard numeric budgets for startup, release, native-image, and binary smoke checks
belong to `T003_04` or later implementation tasks after baseline measurements are
available.

## Current Baseline

The implemented Java application is still intentionally small.

| Area | Current state |
| --- | --- |
| Module | One Maven module under `app/codegeist/cli` |
| Java | Java 25 through Maven compiler release |
| Spring Boot | Spring Boot 4 parent |
| Spring Shell | Spring Shell 4 starter and BOM present; no commands yet |
| Spring AI | Spring AI BOM imported; no provider starters or model calls yet |
| Spring AI Agent Utils | Agent Utils BOM and core dependency present; no runtime utility wired yet |
| Entrypoint | `ai.codegeist.app.CodegeistApplication` starts Spring Boot |
| Test entrypoint | `task test` delegates to `mvn --batch-mode --no-transfer-progress test` |
| Implemented test | `CodegeistApplicationTests` loads the Spring Boot context with shell runner disabled |

Future unit, contract, adapter, CLI/TUI, smoke, native, provider, shell, and
filesystem-heavy test categories are planned expectations. They are not current
implemented coverage until a later task adds them.

## TDD Workflow Rules

TDD is the default for future Codegeist behavior changes and bug fixes.

- Start with the smallest failing test that proves the behavior, boundary, or
  regression owned by the task.
- Prefer a narrow unit or contract test before a Spring context, CLI/TUI, smoke,
  native, provider, network, shell, or filesystem-heavy test.
- Run the narrow failing test first and record the exact command in the solve
  result when the task is solved.
- Implement the smallest code change that makes the targeted test pass.
- Broaden verification only after the narrow test passes.
- If a task cannot reasonably be test-first, record the reason in the task solve
  result before marking the task solved.

Acceptable reasons to skip test-first work are narrow and should be explicit, for
example a documentation-only task, a pure dependency alignment with no behavior to
assert, or a temporary external blocker that the task records with a follow-up.

## Test Taxonomy

Use the smallest test category that proves the current behavior.

| Category | Owns | Default posture |
| --- | --- | --- |
| Fast unit tests | Records, value objects, validators, policies, mappers, local services | Plain JVM, no Spring context, no provider calls, no process execution |
| Contract tests | Runtime, provider, tool, permission, workspace, session, event, storage ports | Plain JVM where possible; assert Codegeist boundary types and failure semantics |
| Adapter tests | Spring AI, Spring Shell, Agent Utils, storage, CLI/TUI, filesystem edge mapping | Keep framework or external types at the edge and use fakes when practical |
| Spring context tests | Configuration binding, bean wiring, command registration, app startup | Explicitly startup-sensitive; only when Spring wiring is the behavior under test |
| CLI/TUI tests | User-visible command behavior, rendering, keyboard/input flow, event display | Prefer adapter-level tests over full application startup until startup is the subject |
| Smoke tests | A narrow usable workflow across several boundaries | Run after focused tests; avoid broad fragile setup |
| Native/posture checks | GraalVM-sensitive dependency or packaging status | Report `passed`, `skipped`, or `failed` with reason; do not hide native blockers |
| Network/provider tests | Live provider configuration, credentials, streaming, tool mediation | Disabled or skipped by default unless the task explicitly owns live checks |
| Shell/process tests | Controlled command execution, timeout, cancellation, bounded output | Use safe commands and temporary workspaces; test denial before success paths |
| Filesystem-heavy tests | Workspace scans, ignore rules, patch/edit apply, artifact references | Use temporary directories and bounded fixtures; avoid repository mutation |

Provider credentials, external network access, native-image builds, process
execution, large filesystem scans, and interactive UI behavior are never ordinary
unit-test dependencies. A task that needs them must name the category, command,
and skip or failure policy explicitly.

## Individual Execution Contract

Every implementation task should keep its tests individually executable or record
why isolation is not possible yet.

Current Maven/JUnit selector examples for the single Maven module:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistApplicationTests test
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistApplicationTests#contextLoads test
```

Future tests should preserve class-level and method-level selectors whenever
possible:

```bash
cd app/codegeist/cli
mvn --batch-mode --no-transfer-progress -Dtest=ProviderAdapterContractTests test
mvn --batch-mode --no-transfer-progress -Dtest=ProviderAdapterContractTests#mapsProviderResponseWithoutExposingSpringAiTypes test
```

Add Taskfile wrappers only when repeated patterns become stable enough to justify
them. Until then, prefer explicit Maven/JUnit selectors in task plans and solve
results so the command remains transparent.

## Startup-Sensitive Rules

Startup cost is a first-class testing concern for Codegeist.

- Plain unit and contract tests must not load Spring unless Spring behavior is the
  subject of the test.
- Spring context tests must be named and documented as startup-sensitive.
- CLI startup tests must prove command registration, parsing, or application
  startup; ordinary command delegation can usually be tested with plain adapter
  tests.
- TUI startup tests must stay separate from rendering or projection unit tests
  unless terminal lifecycle startup is the behavior under test.
- Provider initialization and live model calls must stay out of ordinary suites.
- Shell/process startup and native executable startup must be explicit smoke or
  posture checks with clear timeout and skip behavior.

Do not hide `@SpringBootTest`, full CLI startup, native executable startup,
provider initialization, or process execution behind test names that look like
ordinary unit tests.

## Duration Measurement And Reporting

Future solve results for implementation tasks should report enough timing
information to spot slow tests without turning task files into raw logs.

Record:

- Targeted test command used for the smallest feedback loop.
- Approximate targeted command duration.
- Broader verification command used, when run.
- Approximate broader command duration, when run.
- Startup-heavy checks that were run, skipped, or deferred.
- Any slow-test concern or known blocker that should affect later tasks.

Use concise summaries such as:

```text
Targeted verification: mvn --batch-mode --no-transfer-progress -Dtest=WorkspacePolicyTests test (~2s).
Broad verification: task test (~7s).
Startup-sensitive checks: no Spring context loaded; not applicable for this plain JVM contract test.
```

Do not paste full Maven, Gradle, shell, provider, or native-image logs into task
files unless one specific line materially changes the task state.

## Spring Context Boundaries

Spring Boot may own application startup, dependency injection, configuration
binding, profiles, and test context setup. It must not be the default way to test
Codegeist domain behavior.

Use `@SpringBootTest` or equivalent context loading only when the behavior under
test is one of:

- Application context startup.
- Bean registration or conditional wiring.
- Configuration property binding.
- Spring Shell command registration or shell-specific integration.
- Spring AI adapter wiring where the mapping cannot be verified with a plain fake.

Prefer plain JVM tests for:

- Session, event, runtime, provider, tool, permission, workspace, patch/edit,
  shell, and storage contracts.
- Policy decisions, validation, typed failures, mappers, and result bounding.
- CLI/TUI delegation to runtime services when no Spring Shell registration is
  being asserted.

When Spring context startup is required, keep the context as small as the current
Spring testing setup allows and disable interactive behavior that is not part of
the assertion.

## Agent Checklist

Before marking a future implementation task solved, verify:

- A failing test was added or updated first, or the solve result explains why
  test-first work was not reasonable.
- The targeted test can be run individually by class or method selector, or the
  task records the isolation blocker.
- The implementation uses the smallest test category that proves the behavior.
- Ordinary unit and contract tests avoid Spring context startup.
- Startup-heavy tests are explicit and reported separately.
- Network, provider, shell/process, native, and filesystem-heavy checks are run
  only when the task owns them, or are skipped with a reason.
- The solve result reports targeted verification, approximate duration, broader
  verification when run, and startup-sensitive checks.
- Current-state architecture docs are updated if source packages, commands,
  runtime behavior, tests, configuration, or verification entrypoints changed.

## Handoff To Later Tasks

Stable now:

- TDD is the default for Codegeist behavior changes and bug fixes.
- Individual test execution is required for coding-agent feedback loops.
- Spring context startup is minimized and made explicit.
- Solve results should report targeted commands and approximate timing.
- Test categories must stay separate so slow or external checks do not pollute
  ordinary unit and contract suites.

Owned later:

- `T003_04` owns hard numeric startup, release, platform, native, and binary smoke
  thresholds.
- Later implementation tasks own concrete test source, fakes, fixtures, Maven
  configuration, Taskfile wrappers, CI integration, and smoke commands when those
  additions are justified by real behavior.
- Provider, shell, filesystem-heavy, native, and binary smoke checks should gain
  explicit skip and timeout policies in the task that first implements them.
