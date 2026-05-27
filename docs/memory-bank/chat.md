# Chat Memory

## Target State

- `codegeist.ai` should grow into a customizable coding agent for CLI, TUI, and
  web use.
- Codegeist is Java-first: Java 25, Spring Boot, Spring Shell, Spring AI,
  GraalVM, and later Vaadin, JBang, and PF4J where they fit.
- OpenCode is a feature and behavior reference, not an implementation blueprint.

## Current State

- `main` contains the current project state.
- `.opencode` is a git submodule tracking the `release` branch of
  `codegeist-agent-kit`; `.devcontainer/` is a git submodule tracking the
  `release` branch of `codegeist-devcontainer-kit`.
- `start.sh` has been removed. Start the devcontainer through VS Code Dev
  Containers or `devcontainer up --workspace-folder .`.
- `app/codegeist/cli` is the only implemented Codegeist application module. It is
  a Spring Boot 4 and Spring Shell 4 CLI bootstrap with Java 25, Maven, Spring AI
  `2.0.0-M6`, Spring AI Agent Utils `0.7.0`, and GraalVM native build posture.
- `app/codegeist/cli` implements `--version` as a Spring Shell command. It writes
  through `CommandContext.outputWriter()` and prints only the Spring Boot build
  version, currently `0.1.0-SNAPSHOT`.
- `application.yaml` sets `spring.shell.interactive.enabled=false` so Spring
  Shell's noninteractive runner handles `--version` by default without a custom
  runner class. Interactive shell behavior is deferred.
- `app/codegeist/cli/src/main/resources/logback.xml` routes logs only to
  `${LOG_FILE:-logs/codegeist.log}`. Console output is reserved for command
  output.
- `app/codegeist/cli/Taskfile.yml` provides `test`, `build`, `run`, `native`,
  `native-smoke`, `local-linux-smoke`, `qemu-windows-smoke`, and
  `final-smoke-suite`. Local smoke scripts live under `scripts/tests/`.
  `native-smoke` sources `scripts/tests/native-smoke.sh`; each native run
  recreates `target/smoke-test`, packages
  `target/dist/codegeist-<version>-linux-x64.tar.gz`, unpacks it into a fresh temp
  directory, runs packaged `./codegeist --version`, and writes
  `target/smoke-test/codegeist.log`.
- Branch `release/v0.1.0-github-release-build` adds `.github/workflows/release.yml`
  for GitHub-hosted release validation. Pushes to `release/v*` validate without
  publishing, `workflow_dispatch` supports pre-tag validation with
  `release_version=0.1.0`, and pushed `v*` tags upload versioned assets to a draft
  GitHub Release.
- `app/codegeist/cli/pom.xml` now uses CI-friendly `${revision}` with local default
  `0.1.0-SNAPSHOT`; release CI passes `-Drevision=0.1.0` so artifact smokes print
  `0.1.0`.
- GitHub release assets are `codegeist-<version>-jvm-any.jar`,
  `codegeist-<version>-linux-x64.tar.gz`,
  `codegeist-<version>-windows-x64.zip`,
  `codegeist-<version>-macos-x64.tar.gz`, and
  `codegeist-<version>-SHA256SUMS.txt`.
- `scripts/tests/final-smoke-suite.sh` is the local final smoke entrypoint. It
  runs Linux direct smoke and automated Windows QEMU/SSH smoke. Default mode
  requires both platforms to pass; `--allow-skips` is developer-only. The suite
  has passed locally with Linux and Windows jar/native statuses all `passed`.
- `scripts/tests/qemu-windows-vm.sh` downloads the official Windows Server 2025
  Evaluation ISO with `curl` when no local ISO exists, stores VM state under
  `.local/windows-qemu`, provisions OpenSSH/GraalVM/Maven/MSVC in the guest, syncs
  the repo subset, and runs Windows smoke over SSH. It uses `-cpu host` with KVM
  and `-cpu max` without KVM unless `CODEGEIST_WINDOWS_CPU` overrides the model.
- Native release artifacts should be platform archives, not true single executable
  files: Linux uses `codegeist-<version>-linux-x64.tar.gz`, Windows uses
  `codegeist-<version>-windows-x64.zip`, and each archive keeps the executable next
  to GraalVM sidecar libraries. This preserves fast first startup by avoiding a
  self-extracting runtime wrapper. The local Linux and Windows native smokes now
  package these archives, unpack them into fresh temp directories, and test the
  packaged executable rather than raw `target/` binaries.
- `docs/developer/release/native-distribution-packaging.md` documents the archive
  layout, sidecar libraries, and why Codegeist does not ship true single-file
  native executables by default.
- `docs/developer/release/windows-qemu-smoke.md` is the detailed operational guide
  for the Windows QEMU smoke lifecycle, configuration, artifacts, and
  troubleshooting.
- `docs/developer/architecture/architecture.md` is the current-state architecture
  map. It must describe only implemented repository state and explicitly mark
  not-yet-implemented boundaries.
- `docs/developer/specification/` now contains only the surviving high-level
  specifications and guidance:
  - `codegeist-opencode-parity.md`
  - `java-generation-guidance.md`
  - `testing-strategy-and-agent-rules.md`
  - `build-release-and-binary-smoke-strategy.md`
  - `native-packaging-posture.md`
  - `runtime-vocabulary.md`
- `docs/developer/implementation/` was removed. Do not recreate it as a broad
  handoff layer.
- The previous T004 implementation epic was discarded and removed. Its replacement
  tiny implementation task, `docs/tasks/T004_implement-codegeist-version-flag.md`,
  is solved with the current Spring Shell `--version` behavior.
- `docs/tasks/T005_add-cross-platform-release-and-qemu-smoke/` is the active
  release-readiness task group. `T005_01` is solved with local Linux/Windows
  build-smoke entrypoints under `scripts/tests/`; `T005_02` is implemented locally
  on `release/v0.1.0-github-release-build` and still needs GitHub branch workflow
  validation after `gh` authentication is available.
- The previous T003 source-generation child tasks `T003_05` through `T003_12`
  were removed with their generated specification documents because they
  encouraged placeholder Java instead of tested behavior.

## Durable Decisions

- Future implementation should be iterative, Spring-first, and test-driven.
- Do not create placeholder classes, ids, ports, enums, records, package layers,
  validation hierarchies, or empty package directories before a focused test or
  workflow needs them.
- Keep the active task file small enough to revise during implementation instead
  of creating broad implementation handoff documents.
- First provider-backed workflow should prefer a pinned local Ollama
  Testcontainer with `llama3` over fake providers. Pin the Ollama image and model
  tag, set `temperature=0`, use a fixed seed when the active Spring AI/Ollama
  versions support it, and keep assertions constrained enough to be stable.
- Spring AI Agent Utils may be used directly as a private implementation detail
  when useful, but Codegeist runtime, provider, tool, permission, workspace,
  event, session, storage, API, and UI contracts must remain Codegeist-owned.
- Add a thin Codegeist adapter only when a concrete boundary needs policy
  mediation, workspace validation, permission handling, session/event mapping,
  output mapping, or replacement flexibility.
- Core implementation scope includes CLI and TUI behavior. Keep JBang, PF4J,
  Vaadin, headless server, API, and SDK/OpenAPI in the backlog while preserving
  adapter-ready boundaries when real behavior exists.
- Build artifacts such as `target/`, `bin/`, `.class`, and `.jar` stay out of
  git.
- Durable repo-owned docs, rules, code comments, test names, and commit messages
  stay in English.

## Workflow Notes

- Use `/specify-task`, `/plan-task`, `/solve-task`, and `/work-task` from the
  shared `.opencode` agent kit for phased task work when a tracked task benefits
  from that workflow.
- Use `docs/developer/specification/java-generation-guidance.md` before writing
  Java implementation code.
- Use `docs/developer/specification/testing-strategy-and-agent-rules.md` for
  behavior changes and bug fixes. TDD is the default; tests should remain
  individually executable; solve results should report targeted commands and
  timing.
- Use `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
  for packaging, release, platform, or binary-smoke work.
- For the active T005 release work, validate Linux and Windows locally before the
  release path where practical, use GitHub-hosted runners for Linux, Windows, and
  macOS release builds, and use `gh` pre-tag validation before creating the final
  `v*` release tag.
- Keep test and smoke helper scripts under `scripts/tests/`. Local Windows release
  validation uses a real Windows QEMU VM over SSH or a matching GitHub Windows
  runner; do not add local compatibility-layer smoke paths.
- For Codegeist smoke scripts, treat expected devcontainer tools such as
  `timeout` and `curl` as part of the script contract and call them directly.
  Keep command-existence checks only when they drive real skip, status, or guest
  installation behavior.
- For Codegeist architecture or implementation tasks, read
  `docs/developer/specification/codegeist-opencode-parity.md` when the target
  touches OpenCode parity, runtime boundaries, provider behavior, tools,
  permissions, workspace policy, storage, UI, plugin surfaces, or packaging.
- For Java implementation phases, use
  `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md`: ask
  `/ask-project spring-ai-agent-utils ...` for Java/Spring-side equivalents first.
- When behavior is not already present in Java or covered by Spring AI Agent
  Utils, use `/ask-project opencode ...` to inspect OpenCode behavior before
  translating it into Codegeist's Java-first architecture.
- Source-close third-party questions should use
  `/ask-project <project> "<question>"`. `/ask-project` consumes the analyzed
  project workspace and delegates broad packed-source questions to the `@repomix`
  subagent.

## Third-Party Analysis

- `docs/third-party/opencode/source` is a submodule for OpenCode on branch `dev`.
- `docs/third-party/opencode/` contains the OpenCode analysis workspace:
  `README.md`, `ANALYSIS_REPORT.md`, `REGENERATE.md`, feature/user/developer
  notes, and Mermaid sources. Heavy Graphify, Repomix, manifest, verification,
  and rendered artifacts are regenerable and ignored.
- `docs/third-party/spring-ai-agent-utils/` contains the Spring AI Agent Utils
  analysis workspace with source submodule, durable docs, and ignored local
  Repomix/Graphify/manifest/verification artifacts. Use it for navigation, then
  inspect source/tests before adoption decisions.

## Open Points

- Keep `docs/developer/architecture/architecture.md` synchronized whenever
  implemented packages, classes, configuration, runtime flows, or tests change.
- Revisit `docs/developer/specification/native-packaging-posture.md` and
  `build-release-and-binary-smoke-strategy.md` when release automation or binary
  smoke work starts.
- Finish `T005_02`: authenticate `gh`, push
  `release/v0.1.0-github-release-build`, watch the branch workflow, fix any CI
  issues, then record the branch-validation result before merging or tagging.
