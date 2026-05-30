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
- Root `Dockerfile` is a project-local devcontainer extension fragment. It adds
  NVIDIA user-space libraries and NVIDIA Container Toolkit for nested Docker GPU
  workloads, using `NVIDIA_DRIVER_VERSION=595.71.05-1` by default to match the
  validated host driver. Root `compose.local.yml` passes that build arg and sets
  `gpus: all` for the workspace service.
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
  `native-smoke`, `local-linux-smoke`, `qemu-windows-smoke`,
  `final-smoke-suite`, and `ollama-start`. Local smoke scripts live under
  `scripts/tests/`. `ollama-start` starts a persistent GPU-backed
  `ollama/ollama` container named `codegeist-ollama` with models mounted from
  `${OLLAMA_MODELS_DIR:-$HOME/.ollama/models}`. In an interactive terminal it
  enters `docker exec -it codegeist-ollama ollama run llama3.2:1b` by default;
  set `OLLAMA_ENTER=false` for non-interactive starts. `native-smoke` sources
  `scripts/tests/native-smoke.sh`; each native run recreates
  `target/smoke-test`, packages `target/dist/codegeist-linux-x64.tar.gz`,
  unpacks it into a fresh temp directory, runs packaged `./codegeist --version`,
  and writes `target/smoke-test/codegeist.log`.
- Branch `release/v0.1.0-github-release-build` adds `.github/workflows/release.yml`
  for GitHub-hosted release validation. Pushes to `release/v*` validate without
  publishing, `workflow_dispatch` supports pre-tag validation with
  `release_version=0.1.0`, and pushed `v*` tags publish release assets to a GitHub
  Release. Branch run `26535014716` passed JVM, Linux x64, Windows x64, macOS x64,
  and checksum jobs; the release job was correctly skipped on the branch run.
- Future release workflow iterations may use a multi-commit
  `release/v<version>-...` branch, but `main` should receive only one detailed
  squash-candidate commit. `/codegeist-release --source <release-branch> --rc <n>`
  owns version inference, candidate creation, validation, fast-forward-only `main`
  promotion, final tag publication, downloaded checksum verification, and the
  `latest` GitHub Release mirror.
- Codegeist `v0.1.0` is published on GitHub Releases:
  `https://github.com/codegeist-ai/codegeist/releases/tag/v0.1.0`. Pre-tag
  validation run `26537663964`, tag run `26538176834`, and downloaded asset
  checksum verification all passed.
- `app/codegeist/cli/pom.xml` now uses CI-friendly `${revision}` with local default
  `0.1.0-SNAPSHOT`; release CI passes `-Drevision=0.1.0` so artifact smokes print
  `0.1.0`.
- Future GitHub release assets intentionally omit the version because the release
  URL and immutable `v*` tag carry it. Current workflow asset names are
  `codegeist-jvm.jar`,
  `codegeist-linux-x64.tar.gz`, `codegeist-windows-x64.zip`,
  `codegeist-macos-x64.tar.gz`, and `SHA256SUMS.txt`. The already-published
  `v0.1.0` release used the older versioned asset names.
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
  files: Linux uses `codegeist-linux-x64.tar.gz`, Windows uses
  `codegeist-windows-x64.zip`, and each archive keeps the executable next to
  GraalVM sidecar libraries. This preserves fast first startup by avoiding a
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
  tiny implementation task,
  `docs/tasks/T004_implement-codegeist-version-flag/task.md`, is solved with the
  current Spring Shell `--version` behavior.
- `docs/tasks/T005_add-cross-platform-release-and-qemu-smoke/` is finalized.
  `T005_01` delivered local Linux/Windows build-smoke entrypoints under
  `scripts/tests/`; `T005_02` delivered the GitHub-hosted release workflow, passing
  branch validation, passing pre-tag validation on `main`, and published `v0.1.0`
  release assets with downloaded checksum verification.
- `docs/tasks/T006_build-provider-configuration-feature/` is open as the provider
  configuration feature epic. Start with `T006_01` to design the `codegeist.yml`
  schema, then `T006_02` for the Spring AI provider matrix, `T006_03` for
  credential/account strategy, `T006_04` for config loading, `T006_05` for local
  Ollama verification, and `T006_06` for the provider connection smoke harness.
- The previous T003 source-generation child tasks `T003_05` through `T003_12`
  were removed with their generated specification documents because they
  encouraged placeholder Java instead of tested behavior.

## Durable Decisions

- Future implementation should be iterative, Spring-first, and test-driven.
- `codegeist.yml` provider configuration should use `kebab-case` field names such
  as `small-model`, `enabled-providers`, `base-url`, and `api-key-env`, while
  staying structurally close to OpenCode's provider/model config where that model
  fits Codegeist.
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
- Consumer-specific NVIDIA/Ollama development support belongs in the repo root
  `Dockerfile`, root `compose.local.yml`, and `task ollama-start`; keep the
  shared `.devcontainer` submodule unchanged for that local GPU setup.
- Do not merge multi-commit release iteration branches directly into `main`.
  Promote them through `/codegeist-release --source <release-branch> --rc <n>`;
  the command infers SemVer from the diff between the latest reachable release tag
  and the release branch commit, writes a detailed squash commit message, and
  advances `main` by fast-forward only.
- After a verified GitHub Release, `/codegeist-release` moves the lightweight
  `latest` tag to the same commit as the immutable `v*` release tag and creates or
  updates the `latest` GitHub Release with the same downloaded, checksum-verified
  assets from the `v*` release. Do not move or publish `latest` before downloaded
  checksum verification passes, and do not run another build for `latest`.
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
- For future release work, validate Linux and Windows locally before the release
  path where practical, use GitHub-hosted runners for Linux, Windows, and macOS
  release builds, and use `/codegeist-release --source <release-branch> --rc <n>`
  for release publication. The command handles version inference, candidate
  promotion, pre-tag validation, final `v*` tag creation, and automatic GitHub
  Release publication.
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
- For the next release, run `/codegeist-release --source <release-branch> --rc 1`;
  do not enter the version manually unless checking an inferred-version conflict.
- Work the provider feature from `T006_01` first. Do not start provider account
  setup or remote connection tests until the `codegeist.yml` schema, Spring AI
  provider matrix, and credential/account strategy are defined.
