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
- `.devcontainer` now uses `.codegeist/.local.env` and
  `.codegeist/compose.local.yml` for local runtime overrides. The legacy root
  `compose.local.yml` file is removed; `initialize.sh` can copy older local root
  files into `.codegeist/` when needed.
- `start.sh` has been removed. Start the devcontainer through VS Code Dev
  Containers or `devcontainer up --workspace-folder .`.
- `app/codegeist/cli` is the only implemented Codegeist application module. It is
  a Spring Boot 4 and Spring Shell 4 CLI bootstrap with Java 25, Maven, Spring AI
  `2.0.0-M6`, Spring AI Agent Utils `0.7.0`, and GraalVM native build posture.
- `app/codegeist/cli` implements `--version` as a Spring Shell command. It writes
  through `CommandContext.outputWriter()` and prints only the Spring Boot build
  version, currently `0.1.0-SNAPSHOT`.
- `app/codegeist/cli` implements `--show-config` as a Spring Shell command in
  `ai.codegeist.app.config.CodegeistConfigService`. The service resolves the
  current primary `CodegeistConfig` and prints only direct `codegeist.yml` YAML
  with no `codegeist:` wrapper and no YAML document marker. Until additional
  source loading is implemented, this is the Spring-bound config.
- `application.yaml` sets `spring.shell.interactive.enabled=false` so Spring
  Shell's noninteractive runner handles `--version` by default without a custom
  runner class. Interactive shell behavior is deferred.
- `CodegeistApplication.APP_NAME` is the shared application name and Spring config
  prefix. Provider configuration code lives under `ai.codegeist.app.config`.
  `CodegeistConfig` is the current typed config model: a `provider` map whose
  values are abstract sealed `ProviderConfig` instances selected from the required
  provider object `type` field through `@Provider` annotations. It is Spring-bound
  with `@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)`,
  where `CONFIGURATION_PREFIX` is defined from `CodegeistApplication.APP_NAME`, and
  uses Jackson YAML metadata for direct `codegeist.yml` mapping. It receives the
  qualified YAML `ObjectMapper` so `setProvider(...)` can normalize raw provider
  maps into typed provider config classes. Supported config-only provider classes
  are currently only `ollama` and `openai`; the broader provider matrix and
  OpenCode-only provider types remain unsupported in this slice.
- `CodegeistYamlConfiguration` exposes the qualified `codegeistYamlObjectMapper`
  bean used by the config service and config model for direct `codegeist.yml`
  parsing, provider normalization, and rendering. The mapper carries Jackson
  injectable values for direct `CodegeistConfig` loads.
- `CodegeistYamlExpressionEvaluator` is a Spring service that receives the YAML
  mapper bean and owns scalar SpEL preprocessing for direct YAML loads.
- `CodegeistConfigService` receives the Spring-bound `CodegeistConfig` through
  field `@Autowired` plus `@Qualifier(CodegeistConfig.SPRING_BOUND_CONFIG_BEAN)`,
  exposes `primaryCodegeistConfig` as a primary `@Bean` that currently returns that
  config, and can load an explicit YAML path with `loadConfig(String configPath)`
  using a phased parser: read YAML into a Jackson tree, evaluate SpEL only in
  string scalar values containing `#{`, map raw provider objects into concrete
  provider config classes, then call `jakarta.validation.Validator`. Normal app
  code injects unqualified `CodegeistConfig` to get the primary config.
  Bean Validation and SpEL failures throw `CodegeistConfigValidationException`;
  Jackson mapping and IO failures surface directly through Lombok `@SneakyThrows`.
  `toYaml(...)` emits public direct `codegeist.yml` shape, leaves configured values
  unchanged, and keeps empty configs visible as `provider: {}`. `--show-config` can
  print API keys or other sensitive config values when they are present.
  `CodegeistConfigServiceTest` owns Spring binding and direct-path loading,
  `CodegeistProviderConfigTest` owns type dispatch and local validation,
  `CodegeistConfigSpelEvaluationTest` owns scalar SpEL preprocessing, and
  `CodegeistConfigCommandTest` owns command stdout and direct-value rendering.
  There is no model-level multi-source combination helper now. Home-path
  discovery, service-level combination beyond returning the Spring-bound config,
  inheritance, provider runtime selection, provider clients, model calls, and
  model catalogs are not implemented yet.
- `app/codegeist/cli/src/main/resources/logback.xml` routes logs only to
  `${LOG_FILE:-logs/codegeist.log}`. Console output is reserved for command
  output. Current Spring `@Service` and `@Component` classes use Lombok `@Slf4j`
  and emit concise debug logs with `log.debug(...)`; enable them with
  `logging.level.root=DEBUG` or `LOGGING_LEVEL_ROOT=DEBUG`.
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
  asserts packaged `./codegeist --show-config` prints exactly `provider: {}`, and
  writes `target/smoke-test/codegeist.log`.
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
- The release workflow's native matrix packages Linux, Windows, and macOS native
  archives, unpacks each archive into a fresh temp directory, and smoke-tests both
  `--version` and the default `--show-config` output before upload. The JVM jar
  smoke remains `--version` only.
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
- `docs/developer/architecture/source-code-documentation.md` is the source-code
  documentation strategy. It asks future tasks to add focused current-state docs
  for non-trivial Spring/framework interactions, solved problems, runtime flows,
  validation behavior, tests, sharp edges, UML-style Mermaid diagrams, and
  editable Excalidraw sketches.
- `docs/developer/architecture/provider-configuration.md` is the focused current
  source-code doc for the T006 config slice. It explains the Spring component
  model, direct Jackson YAML loading, SpEL preprocessing, provider type dispatch,
  Bean Validation flow, command rendering, tests, and sharp edges.
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
  configuration feature epic. `T006_01` is solved with the minimal provider config
  model and dual loading approach: Spring `@ConfigurationProperties` for
  application config plus Jackson YAML for explicit `codegeist.yml` paths.
  `T006_02` is solved with a Spring AI `2.0.0-M6` provider matrix, source evidence,
  candidate `codegeist.yml` and future `ProviderConfig` fields, Ollama as the first
  local provider candidate, and a no-cost integration-test posture that keeps
  hosted provider calls behind explicit `remote-free` selection plus local no-cost
  confirmation. `T006_03` is solved as a documentation-only SpEL, account, and
  provider-availability strategy: `codegeist.yml` is trusted local input, the first
  parser slice uses Spring template expressions in YAML string scalar values only,
  map keys stay literal, the first evaluation context is a plain
  `StandardEvaluationContext` without Codegeist helper variables/functions or a
  Spring bean resolver, there is no separate credential-reference schema, and
  model selection is deferred. Its source-backed
  question catalog now lives at
  `docs/tasks/T006_build-provider-configuration-feature/hints/source-evidence-question-catalog.md`
  with OpenCode and Spring AI Agent Utils answers for config merge, auth/runtime
  boundaries, sensitive-output handling, remote-call safety, `ChatClient` wiring, and testing
  gaps. `T006_03` also includes the provider account/free-tier catalog: each
  provider records account setup, official API free-tier/no-cost posture, billing
  or credit requirements, safe default smoke posture, and whether Spring AI
  `2.0.0-M6` uses a dedicated starter or the OpenAI-compatible starter path before
  hosted-provider remote smokes use real accounts. It now defines provider
  availability as layered readiness: Spring AI dependency route, config fields,
  evaluated/validated config, safety gate, runtime provider selection, lazy
  `ChatModel`/`ChatClient` creation for the selected provider only, and
  result handling. It also contains an OpenCode-vs-Codegeist provider comparison:
  OpenCode already supports broad AI SDK/Models.dev/provider-directory coverage,
  while Codegeist's first wave stays limited to pinned Spring AI routes; OpenRouter
  is classified as an OpenAI-compatible candidate that needs no dedicated Spring AI
  starter unless Codegeist wants a thin profile for headers, routing, and `:free`
  gating. `T006_04` is now implemented as the config-only typed YAML loading
  slice under `ai.codegeist.app.config`: trusted-local SpEL runs before direct YAML
  mapping, provider object `type` selects concrete Java config classes for only
  `ollama` and `openai`, provider fields validate locally, `--show-config` leaves
  configured values unchanged, and empty configs render as `provider: {}`. The config
  classes are data contracts only; all other provider types are deferred, and no
  Spring AI starters, provider clients, smoke commands, local model pulls, or
  remote calls were added. The implementation was verified with the full Maven
  suite, Linux jar/native smokes, and Windows QEMU jar/native smokes. Next are
  `T006_05` for local Ollama verification and `T006_06` for the provider
  connection smoke harness.
- The previous T003 source-generation child tasks `T003_05` through `T003_12`
  were removed with their generated specification documents because they
  encouraged placeholder Java instead of tested behavior.

## Durable Decisions

- Future implementation should be iterative, Spring-first, and test-driven.
- Source comments should stay sparse and useful for coding agents: explain why,
  non-obvious framework behavior, cross-file contracts, or sharp edges, and include
  related file/doc references when they help recover context quickly.
- `.oc_local/rules/java-coding.md` records the Codegeist Java coding convention:
  prefer annotation-based Spring wiring, use field `@Autowired` for Spring-managed
  dependencies instead of constructor injection, use Lombok `@Slf4j` on Spring
  `@Service` and `@Component` classes, and use focused Lombok annotations such as
  `@Getter` and `@Setter` to reduce boilerplate. Prefer named
  `static final String` constants for contract-bearing strings; shared app-wide
  values belong in `CodegeistApplication`, for example `APP_NAME`. Tests should
  reuse owning constants for class-owned error messages and prefixes, such as
  `CodegeistConfigService.VALIDATION_ERROR_PREFIX`, instead of duplicating string
  fragments.
- Codegeist provider configuration currently has config-only typed `ollama` and
  `openai` provider entries. The config properties POJO should keep
  `@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)` so
  `application.yaml` can configure it, with the prefix constant backed by
  `CodegeistApplication.APP_NAME`, while direct `codegeist.yml` loading uses
  Jackson YAML against the same POJO.
- Use Jackson YAML (`jackson-dataformat-yaml`) as the current direct YAML-to-POJO
  mapping framework for `codegeist.yml`. Current direct YAML loading evaluates a
  minimal Spring SpEL phase before mapping string scalar values containing `#{`.
  `T006_04` validates configured `model`, `enabled`, `base-url`,
  `completions-path`, ordinary scalar credential fields, and nested provider
  `options`; runtime model selection policy, provider capabilities, inheritance,
  runtime source orchestration beyond the task scope, provider starters, and
  provider calls stay deferred to later T006 child tasks.
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
- Consumer-specific NVIDIA/Ollama development support belongs in local
  `.codegeist/` devcontainer overrides and `task ollama-start`; keep the shared
  `.devcontainer` submodule unchanged for that local GPU setup.
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
- Next provider work should continue with `T006_05` local Ollama verification and
  `T006_06` provider smoke harness work. Use the solved `T006_03` account/free-tier
  catalog before adding hosted provider-specific smoke rows or treating any hosted
  provider as `remote-free`; use its availability matrix before adding a provider
  starter or client code.
