# T003_04 Define Build Release And Binary Smoke Strategy

Parent: `T003_implement-codegeist-opencode-core-application`

Status: finalized

## Goal

Define how Codegeist builds, packages, releases, and verifies binaries before the
T003 implementation tasks depend on distributable CLI or TUI artifacts.

The strategy must cover GitHub release deployment, Windows/Linux/macOS support,
and detailed smoke tests for the built binaries on each supported operating
system.

## User Direction

The user explicitly requested that Codegeist define build and release behavior:

- Releases must be deployed to GitHub.
- Windows, Linux, and macOS must be supported.
- The project needs an answer for how that support can be tested.
- Built binaries need detailed smoke tests on the respective systems.

## Context

`T002_11` created `docs/developer/specification/native-packaging-posture.md` for
JVM jar and native-image posture. `T003_02` finalized
`docs/developer/specification/java-generation-guidance.md` as Java/Spring input,
and `T003_03` finalized
`docs/developer/specification/testing-strategy-and-agent-rules.md` as the TDD,
test duration, startup-duration, and individually executable test guide. This
task should turn the build and release side into a dedicated strategy before
release workflow, CI, or platform-specific binary scripts are implemented.

The current application is a single Maven module under `app/codegeist/cli`.
Current repo-local commands are:

```text
task test
task build
task native
task run
```

Those commands are useful local entrypoints, but they do not yet define a release
pipeline, GitHub Actions workflow, cross-platform artifact naming, checksum or
signing posture, platform matrix, installer policy, or binary smoke-test suite.

## Scope

- Define release artifact targets for GitHub releases.
- Define supported platform expectations for Windows, Linux, and macOS.
- Define JVM jar versus native binary posture for each platform.
- Define CI/release workflow expectations without implementing the workflow yet.
- Define smoke tests for built binaries on each operating system.
- Define how platform support is proven, skipped, or failed with explicit reasons.
- Define artifact naming, checksum, provenance, and release-note expectations at
  specification depth.
- Define how startup time and binary smoke duration should be measured and
  reported during release validation.

## Non-Goals

- Do not create or modify GitHub Actions workflows, release scripts, Maven
  plugins, Taskfile commands, Java source, Java tests, installers, signing keys,
  notarization setup, package-manager manifests, or runtime behavior in this task.
- Do not require every future feature to be native-compatible before the JVM jar
  is releasable.
- Do not implement PF4J, JBang, Vaadin, server, API/SDK, shell execution, provider
  calls, storage adapters, or TUI behavior while defining release posture.
- Do not store secrets, signing keys, GitHub tokens, or certificate material in
  task docs.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_03_define_testing_strategy_and_agent_test_rules.md`
- `docs/developer/specification/native-packaging-posture.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/architecture/architecture.md`
- `app/codegeist/cli/Taskfile.yml`
- `app/codegeist/cli/pom.xml`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for build, release, and binary
smoke testing. The preferred target is:

- `docs/developer/specification/build-release-and-binary-smoke-strategy.md`

The guidance should include:

- Supported platform matrix for Windows, Linux, and macOS.
- Release artifact matrix for JVM jar and native binaries.
- GitHub release deployment expectations.
- CI/release workflow stages and required gates.
- Binary smoke-test scenarios for each platform.
- Startup timing and smoke-test duration reporting expectations.
- Cross-platform command examples or pseudocode for future CI jobs.
- Rules for checksums, artifact naming, release notes, and failure reporting.
- A checklist for release candidates.

## Acceptance Criteria

- The guidance is written in English and stored under `docs/developer/`.
- The guidance states that GitHub Releases are the deployment target for release
  artifacts.
- The guidance defines Windows, Linux, and macOS support expectations.
- The guidance explains how each platform is tested, including platform-native
  binary smoke tests.
- The guidance defines what must happen when a platform smoke test is skipped or
  fails.
- The guidance separates JVM jar verification from native binary verification.
- The guidance includes startup-time and smoke-test duration reporting
  expectations.
- The guidance avoids implementing CI, build scripts, source code, tests, Maven
  changes, or release deployment behavior in this documentation task.

## Implementation Plan

### Selected Option

Create one durable developer specification for Codegeist build, release, platform,
and binary smoke-test strategy. Keep this task documentation-only so later T003
tasks can implement CI, release automation, package naming, checksums, and
platform smoke tests from a reviewed contract instead of inventing release behavior
inside implementation work.

Do not split this plan into GitHub Actions, Maven, Taskfile, native-image,
installer, signing, notarization, or package-manager tasks yet. The current need is
a release-readiness strategy that defines the target matrix, validation gates, and
failure-reporting shape.

### Concrete Solution Direction

Create `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
as the planned Codegeist release and binary-smoke guide. The guide should combine
the existing JVM-first/native-aware packaging posture with the finalized T003 test
strategy: GitHub Releases are the artifact deployment target, Windows/Linux/macOS
are supported platforms, JVM jar verification is separated from native executable
verification, and every platform smoke result is reported as passed, skipped, or
failed with a concrete reason.

Use conservative first-wave blocking rules: the JVM jar should be release-blocking
once runtime behavior exists; platform-native binaries are release targets and
should become blocking when the corresponding runner/toolchain and smoke command
are available. Until then, skipped native or platform checks must name the missing
runner, toolchain, signing/notarization prerequisite, or implementation follow-up.

### Planned Files And Targets

- Add `docs/developer/specification/build-release-and-binary-smoke-strategy.md`.
- Update this task with solve and finalization notes after the guide is written.
- Update `docs/developer/architecture/architecture.md` only to add a related
  specification link or to keep current build/release descriptions accurate. Do
  not describe planned workflows as implemented CI.
- Update `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
  during solve or finalization if the completed guide changes parent progress
  notes or later child-task boundaries, especially `T003_15`.
- Update `docs/memory-bank/chat.md` only if the solve or finalization result
  changes active T003 state that future sessions need immediately.
- Do not change Java source, Java tests, package directories, Maven files,
  Taskfiles, CI workflows, release scripts, installer manifests, signing keys,
  notarization setup, package-manager files, runtime behavior, provider calls, CLI
  commands, TUI behavior, shell execution, storage, or actual GitHub release
  deployment.

### Guidance Document Structure

The new guide should use this structure unless the solve phase finds a clearer
equivalent while preserving the same scope:

1. Purpose and status: explain that the document is planned release strategy, not
   implemented CI, release deployment, package scripts, signing, or runtime
   behavior.
2. Current baseline: summarize `app/codegeist/cli`, `Taskfile.yml`, `pom.xml`,
   the executable jar name, native profile, Java 25/GraalVM posture, and the
   absence of release automation today.
3. Release target: state that GitHub Releases are the deployment target for
   release artifacts, checksums, provenance notes, release notes, and platform
   validation summaries.
4. Platform matrix: define initial expectations for Linux, Windows, macOS Intel,
   and macOS Apple Silicon. Prefer GitHub-hosted runners where available; record
   Apple Silicon as a target that may start as skipped when native runner capacity
   is unavailable.
5. Artifact matrix: separate the JVM jar from native artifacts such as
   `codegeist-<version>-linux-x64`, `codegeist-<version>-macos-x64`,
   `codegeist-<version>-macos-aarch64`, and
   `codegeist-<version>-windows-x64.exe`, plus checksum files.
6. Verification gates: define ordered stages for source hygiene, tests, JVM jar
   package, JVM startup smoke, native compile, native startup smoke, checksum
   verification, artifact upload dry-run or release draft validation, and GitHub
   Release publication.
7. Binary smoke scenarios: define platform-native checks for `--help`,
   `--version` or equivalent once available, non-interactive startup with Spring
   Shell interactivity disabled, exit-code behavior, basic command invocation when
   commands exist, artifact integrity, and bounded timeout handling.
8. Timing and budgets: define startup-time and smoke-duration reporting shape,
   initial provisional targets, and a requirement to refine numeric budgets after
   baseline measurements. Keep numbers conservative and clearly labeled as release
   validation budgets rather than ordinary unit-test budgets.
9. Skip and failure policy: reuse the `passed`, `skipped`, and `failed` posture
   from `native-packaging-posture.md`; require skipped checks to include reason,
   owner, and follow-up, and failed checks to include command, platform, shortest
   useful error summary, artifact, and owner.
10. GitHub Actions handoff: include pseudocode or workflow-shape examples for the
    future CI matrix without creating `.github/workflows` files in this task.
11. Release candidate checklist: provide a concise checklist for later tasks before
    a candidate can be published or marked blocked.
12. Future implementation handoff: name likely follow-up owners, especially
    `T003_15_validate_packaging_native_and_startup_posture` for implementing or
    validating the packaging/native/startup checks and any later release automation
    task that creates GitHub Actions or scripts.

### Implementation Steps

1. Re-read this task, the T003 parent, `T003_03`,
   `docs/developer/specification/native-packaging-posture.md`,
   `docs/developer/specification/testing-strategy-and-agent-rules.md`,
   `docs/developer/architecture/architecture.md`, `app/codegeist/cli/Taskfile.yml`,
   and `app/codegeist/cli/pom.xml`.
2. Create `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
   with the planned structure above.
3. In the guide, distinguish current implemented commands from planned release
   workflow stages so future agents do not claim GitHub Actions, release scripts,
   multi-platform native artifacts, checksums, signing, or smoke suites already
   exist.
4. Define the release artifact and platform matrix, including JVM jar versus native
   binary responsibilities and explicit Linux, Windows, macOS Intel, and macOS
   Apple Silicon posture.
5. Add concrete platform smoke-test examples or pseudocode, but label unavailable
   commands such as `--version` as future once the CLI owns that behavior.
6. Add timing-reporting guidance that consumes `T003_03` without turning the guide
   into raw command logs.
7. Add the skip/failure reporting shape for platform and native checks so later
   release tasks can prove, skip, or block platform support with explicit reasons.
8. Add the GitHub Release checklist and future CI/release handoff without adding
   workflow files or scripts.
9. Update task and related documentation references only where the completed guide
   changes durable task state or navigation.

### Verification Strategy

- Required for this documentation-only plan and solve:

```bash
git --no-pager diff --check
```

- Do not run `task test`, `task build`, `task native`, Maven commands, GitHub CLI
  commands, or platform-specific binary smoke commands unless the solve phase
  unexpectedly changes Java, Maven, Taskfile, CI, release, runtime, or build files.
- If the guide includes future command examples, keep them clearly labeled as
  planned examples and do not claim they were executed unless verification really
  ran them.

### Dependencies And Tradeoffs

- Depends on `docs/developer/specification/native-packaging-posture.md` for the
  JVM-first/native-aware posture and the `passed`, `skipped`, and `failed` status
  vocabulary.
- Depends on `docs/developer/specification/testing-strategy-and-agent-rules.md` for
  timing-reporting shape, startup-sensitive isolation, and concise solve-result
  reporting.
- Depends on the current-state architecture remaining accurate: the repository has
  local build commands and a native Maven profile, but no CI release workflow,
  release scripts, platform artifact matrix, checksum generation, signing,
  notarization, or binary smoke suite yet.
- Defining GitHub-hosted runners first keeps the future workflow simple, but macOS
  Apple Silicon support may require a documented skipped status until runner
  availability is proven.
- Making the JVM jar release-blocking first protects delivery while native-image
  compatibility remains feature-dependent. Native binaries should still be treated
  as named release targets with explicit skip/failure reporting, not as vague
  optional work.
- Signing, notarization, installer generation, package-manager distribution, SBOM
  generation, and SLSA-style provenance should be documented as future hardening
  unless the user later chooses them as release blockers.

### Open Questions

None for planning. The solve phase can write the release strategy from the current
task contract. Exact measured startup and smoke durations should be refined after
baseline commands are implemented and run on the target platform matrix.

## Plan Workflow Handoff

- Phase command: `/plan-task t003_04`.
- Source task:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_04_define_build_release_and_binary_smoke_strategy.md`.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- User context or instructions considered: user requested planning with task
  reference `t003_04` and provided no extra narrowing instructions.
- Selected option: keep `T003_04` as one documentation-only implementation plan
  that creates
  `docs/developer/specification/build-release-and-binary-smoke-strategy.md`.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and finalized `T003_03`
  impact notes.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Related context files read:
  `docs/developer/specification/native-packaging-posture.md`,
  `docs/developer/specification/testing-strategy-and-agent-rules.md`,
  `docs/developer/specification/java-generation-guidance.md`,
  `docs/developer/architecture/architecture.md`, `app/codegeist/cli/Taskfile.yml`,
  and `app/codegeist/cli/pom.xml`.
- Upstream phase dependency: satisfied by the recorded specification status and
  top-level `Status: specified` that existed before this planning pass.
- Result: the task is now implementation-ready as a documentation-only build,
  GitHub release, platform support, and binary-smoke strategy task.
- Open decisions or blockers: none for planning. Exact measured startup and smoke
  durations remain owned by the solve guide and later platform validation once
  runnable release checks exist.
- Next recommended phase: `/solve-task t003_04` to write
  `docs/developer/specification/build-release-and-binary-smoke-strategy.md` without
  changing runtime, build, or CI behavior.

## Planning-Readiness Questions

- Which GitHub Actions runner images should be the initial release matrix for
  Windows, Linux, macOS Intel, and macOS Apple Silicon posture?
- Which artifacts should be release-blocking first: JVM jar, native Linux binary,
  native macOS binary, native Windows executable, or all of them?
- Should macOS Apple Silicon require native build hardware immediately, or start
  as a documented compatibility target with a skipped status when unavailable?
- Which smoke scenarios are required before Codegeist has full runtime behavior:
  `--version`, `--help`, non-interactive startup, shell-disabled startup, basic
  command invocation, binary exit code, and artifact integrity checks?
- Which later task should implement GitHub Actions and release deployment after
  this strategy is solved?

## Verification

```bash
git --no-pager diff --check
```

## Solution Note

- Added `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
  as the durable release strategy. It defines GitHub Releases as the deployment
  target, separates JVM jar and native executable verification, names the
  Windows/Linux/macOS platform matrix, defines release artifact naming and checksum
  posture, records smoke scenarios and provisional startup budgets, and requires
  every platform check to report `passed`, `skipped`, or `failed` with actionable
  reason and ownership.
- Updated `docs/developer/architecture/architecture.md` only with references to the
  new strategy so the current-state architecture remains clear that release CI,
  workflows, scripts, installers, checksums, and platform smoke suites are not
  implemented yet.
- No Java source, tests, Maven files, Taskfile commands, GitHub Actions workflows,
  release scripts, signing/notarization setup, package-manager files, installers,
  runtime behavior, or actual GitHub release deployment were changed.
- Test-first work was not applicable because this is a documentation-only strategy
  task with no executable behavior.
- Targeted verification: `git --no-pager diff --check`.

## Solve Workflow Handoff

- Phase command: `/solve-task t003_04`.
- Source task:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_04_define_build_release_and_binary_smoke_strategy.md`.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Result: solved as a documentation-only build, GitHub release, platform support,
  and binary-smoke strategy task.
- Open decisions or blockers: exact measured startup and smoke durations remain
  owned by future release validation after runnable platform checks exist.
- Next recommended phase: finalization to propagate the completed strategy to the
  parent task and later packaging/release implementation tasks.

## Finalization Result

- Phase command: `/finalize-task t003_04`.
- Context or instructions considered: user requested finalization for `t003_04`
  and provided no extra narrowing instructions.
- Upstream phase dependency: satisfied. The task had `Status: solved`, a current
  successful `/solve-task` result, and targeted verification recorded before
  finalization.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md` and
  `docs/tasks/hints/opencode-solving-guidance.md`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Impacted tasks: parent task
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md` now
  records that `T003_04` is finalized and that future packaging, release
  automation, and platform-smoke work should use the completed strategy. No
  `T003_15` task file exists yet, so no child implementation task was updated.
- Documentation updates reviewed:
  `docs/developer/specification/build-release-and-binary-smoke-strategy.md`,
  `docs/developer/architecture/architecture.md`, and
  `docs/memory-bank/chat.md`.
- Documentation updates made during finalization:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`, this
  task file, and `docs/memory-bank/chat.md`.
- Remaining follow-ups: create later packaging/release implementation tasks when
  T003 reaches that stage, especially the planned `T003_15` packaging, native,
  startup, and executable-smoke validation task.
- Verification: `git --no-pager diff --check`.
- Result: finalized as a documentation-only build, GitHub release, platform
  support, and binary-smoke strategy task.
- Next recommended phase: continue with the next T003 implementation task.

## Specification Check Result

- Phase command: direct task creation from user guidance, equivalent to the
  specification phase for this new child task.
- Context or instructions considered: user requested a build definition with
  GitHub release deployment, Windows/Linux/macOS support, testing strategy for
  that platform support, and detailed smoke tests for built binaries on each
  system.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered:
  `T003_02_define_java_generation_guidance.md` and
  `T003_03_define_testing_strategy_and_agent_test_rules.md`.
- Dependency inputs considered:
  `docs/developer/specification/native-packaging-posture.md`,
  `docs/developer/specification/java-generation-guidance.md`,
  `docs/developer/architecture/architecture.md`, `app/codegeist/cli/Taskfile.yml`,
  and `app/codegeist/cli/pom.xml`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Upstream phase dependency: none; this task records the initial specification for
  the planned `T003_04` child slot.
- Result: task is specified as a documentation-only build, release, platform, and
  binary-smoke strategy task.
- Open decisions or blockers: exact numeric startup budgets, artifact blocking
  policy, and runner matrix remain open for planning and solving.
- Next recommended phase: `/plan-task t003_04` to define the concrete developer
  document structure and verification handoff.

## T003_03 Finalization Impact

- `T003_03` finalized the testing strategy and agent rules. This task should use
  that guide for timing-reporting shape, startup-sensitive check separation,
  individually executable smoke-test expectations, and concise reporting of
  skipped or failed slow checks.
- Hard numeric startup, release, native, platform, and binary-smoke thresholds
  remain owned here; `T003_03` intentionally defined the workflow contract without
  setting those release-specific budgets.

## Creation Note

Created after the user clarified that Codegeist releases must deploy to GitHub,
support Windows, Linux, and macOS, and include detailed platform smoke tests for
built binaries.
