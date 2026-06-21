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
- `docs/memory-bank/chat.md` is the canonical lightweight project memory. The
  legacy root-level `chat.md` pointer has been removed.
- `.devcontainer` now uses `.codegeist/.local.env` and
  `.codegeist/compose.local.yml` for local runtime overrides. The legacy root
  `compose.local.yml` file is removed; `initialize.sh` can copy older local root
  files into `.codegeist/` when needed.
- Devcontainer image extensions now live in `.codegeist/Dockerfile`. The former
  root `Dockerfile` NVIDIA/GPU extension was moved there so
  `.devcontainer/initialize.sh` can append it to
  `.devcontainer/Dockerfile.merged.gen`; the repository root no longer has a
  devcontainer-specific `Dockerfile`.
- `start.sh` has been removed. Start the devcontainer through VS Code Dev
  Containers or `devcontainer up --workspace-folder .`.
- `app/codegeist/cli` is the only implemented Codegeist application module. It is
  a Spring Boot 4 and Spring Shell 4 CLI bootstrap with Java 25, Maven, Spring AI
  `2.0.0-M6`, Spring AI Agent Utils `0.7.0`, and GraalVM native build posture.
- `app/codegeist/cli` implements `--version` as a Spring Shell command. It writes
  through `CommandOutputService` and prints only the Spring Boot build version,
  currently `0.1.0-SNAPSHOT`.
- `CodegeistCommandExceptionMapper` is the shared Spring Shell command-boundary
  mapper. Commands reference it through `@Command(exitStatusExceptionMapper = ...)`,
  throw domain exceptions directly, and let the mapper log the exception and return
  a user-facing `ExitStatus` description. Corrupt existing session-store JSON maps
  to the user-facing message `No session to continue`.
- `app/codegeist/cli` implements `--show-config` as a Spring Shell command in
  `ai.codegeist.app.config.CodegeistConfigService`. The service resolves the
  current global config policy, including `-Dcodegeist.config=<path>`, and prints
  only direct `codegeist.yml` YAML with no `codegeist:` wrapper and no YAML document
  marker.
- `app/codegeist/cli` implements `ask` as a Spring Shell prompt command in
  `ai.codegeist.app.chat.AskCommands`. Plain `ask <prompt>` accepts a single
  positional prompt parameter, delegates the turn to `ChatHarnessService`, prints
  only the provider response text, and keeps the Spring Shell boundary thin.
  `ChatHarnessService` selects the first provider configured in the active config
  through optional `CodegeistConfig.defaultProvider()`, fails at the command boundary
  when none exists, uses that provider config's `defaultModel()` runtime fallback,
  opens a scoped local tool run, calls `CodegeistChatService` with runtime tool
  context, and saves prompt, bounded tool parts, and assistant response through
  `SessionStoreService`. Plain `ask` creates a new session. `ask -c/--continue
  <prompt>` appends to the newest existing session when one exists, creates a new
  session for missing or empty stores, and refuses corrupt JSON stores instead of
  overwriting them. This is not yet an OpenCode-style coding-agent loop: Codegeist
  now exposes prompt-scoped callbacks to one provider call, but it does not own a
  repeated model/tool/model controller, streaming event loop, permission loop, or
  multi-step agent driver.
- `application.yaml` sets `spring.shell.interactive.enabled=false` so Spring
  Shell's noninteractive runner handles `--version` by default without a custom
  runner class. Interactive shell behavior is deferred. Codegeist-owned
  application defaults such as `codegeist.session.*` live in
  `CodegeistSpringAppProperties`, not in the repo `application.yaml`.
- `CodegeistApplication.APP_NAME` is the shared application name and Spring config
  prefix. Configuration code lives under `ai.codegeist.app.config`.
  `CodegeistConfig` is now a container of generic parsed
  `CodegeistConfigRootElement<? extends CodegeistConfigElement>` instances instead
  of a class with one field per top-level YAML root. `CodegeistConfigElement` is the
  common base for single-object config, typed entries, and keyed list config;
  `CodegeistTypedConfigElement<T>` owns a shared validated `type` field, the central
  `type` property constant, and Lombok getter/setter; `ProviderConfig` and
  `McpClientConfig` extend it.
  `CodegeistConfigService` iterates top-level direct `codegeist.yml` fields and
  delegates root parsing to the central `CodegeistConfigRootParser` Spring service.
  Root element subclasses are plain model objects, not Spring components.
  `CodegeistConfigRootParser` owns shared root-shape validation and `type` dispatch
  for typed root entries. The provider root is
  `provider:`; there is no `providers:` alias. `CodegeistConfigRootParser` supplies
  the explicit provider class registry plus provider type constants to select
  concrete access-only provider config classes. MCP entries dispatch through an
  explicit transport config registry to `StdioMcpClientConfig` or
  `StreamableHttpMcpClientConfig`. `provider:` and `mcp:` roots now store
  `ProvidersConfig` and `McpClientsConfig`, both list-backed
  `CodegeistConfigKeyedListElement` implementations that render provider-style YAML
  objects. The first MCP catalog root is `mcp:` and maps as a YAML object keyed by
  client id; `stdio` clients use required `command` plus optional `args`, while
  `streamable_http` clients use required base server `url` plus optional MCP path
  `endpoint`. Missing `streamable_http` endpoints rely on the MCP Java SDK builder
  default. Multiple clients can share the same transport type such as `stdio`. The
  optional `workspace:` root maps to `WorkspaceRootElement`, which stores
  `WorkspaceConfig` in the inherited generic `CodegeistConfigRootElement<T>` config
  slot with only a nullable `directory` field. Provider dispatch does not branch on
  JVM versus native-image runtime; `reflect-config.json` includes root element, MCP,
  workspace, and provider config POJOs for native images. Supported config-only
  provider classes are currently only `ollama` and `openai`; each concrete provider
  config owns a provider-specific `defaultModel()` without adding YAML model fields.
  The broader provider matrix and OpenCode-only provider types remain unsupported in
  this slice.
- Provider-neutral chat runtime code lives under `ai.codegeist.app.chat`.
  `CodegeistChatService` accepts a caller-selected `ProviderConfig` separately from
  `CodegeistChatRequest`; the request now carries only runtime model and prompt.
  Commands without a model selector use the selected provider config's
  `defaultModel()` before creating the request. The service asks the selected
  provider config to create the matching
  `CodegeistChatModel<T extends ProviderConfig>` without storing the runtime model,
  and the model is applied when the selected prompt is called. Tool-aware calls pass
  `CodegeistChatExecutionContext` beside the request, keeping callbacks out of
  `CodegeistChatRequest` and `.codegeist/session.json`. The context-aware
  `CodegeistChatModel` method is the only provider implementation contract; the older
  service-level request-only path supplies an empty context before invoking the model.
  `OllamaChatModel` is the first concrete provider model. It maps
  `OllamaProviderConfig` into Spring AI `OllamaApi`, then maps the request model to
  `OllamaChatOptions` plus prompt-scoped tool callbacks at call time before
  delegating to the Spring AI Ollama model.
- Local session-store runtime code lives under `ai.codegeist.app.session`.
  `SessionStoreService` owns `.codegeist/session.json` paths, JSON I/O, and clock
  input. `SessionStore` owns in-memory store changes: creating a store, adding a
  session, selecting the latest session, and appending prompt/response exchanges.
  The store model has schema version, working directory, store timestamps, multiple
  `CodegeistSession` values, chronological `SessionMessage` values,
  `SessionMessageRole`, ordered `SessionPart` values, and implemented
  `TextSessionPart`, `CompactionSessionPart`, plus additive `ToolSessionPart` for
  bounded completed or failed tool activity. Tool status uses nested
  `ToolSessionPart.ToolSessionPartStatus` values `completed` and `failed`.
  Session, message, part,
  parent-message, and compaction tail references use plain UUID values, not
  prefixed strings. `SessionStore` is a Lombok getter/setter/builder class with a
  default empty session list, while session, message, and existing part model
  entries remain records or Jackson-bound classes as implemented. The store uses an
  ISO-instant JSON mapper and native reflection metadata for the session model types
  and part implementations. It persists chat history, compaction markers, and bounded
  tool activity parts only; it does not store API keys, provider config, selected
  provider/model, MCP client definitions, enabled tools, permission rules, runtime
  status, or TUI state. `CodegeistSpringAppProperties` owns the built-in default
  `.codegeist/session.json` path and binds Spring keys `codegeist.session.directory`
  and `codegeist.session.store-file`; external Spring application properties or
  `CODEGEIST_SESSION_DIRECTORY` and `CODEGEIST_SESSION_STORE_FILE` can override it.
  These values are not direct `codegeist.yml` provider/tool config. Continuing a
  session currently persists the new turn but does not yet rebuild provider-facing
  model context from stored history.
- Tool runtime code now lives under `ai.codegeist.app.tool`.
  `WorkspaceResolver` resolves the active workspace from optional direct
  `codegeist.yml` `workspace.directory` or `${user.dir}`; absolute values are
  normalized, relative values resolve against the process working directory, and
  filesystem roots/traversal-normalized paths are intentionally allowed because no
  workspace policy exists yet. `ToolOutputBounds` centralizes deterministic string
  and limit caps for local and future MCP tool output: preview, line preview, result
  limits, read limits, and normalized error previews. `CodegeistLocalTools` assembles
  Codegeist-owned local Spring AI callbacks and currently discovers the file-backed
  `codegeist_read`, `codegeist_list`, `codegeist_glob`, `codegeist_grep`, and
  `codegeist_write` components through a `List<CodegeistLocalTool>`. Callback order
  is non-semantic because tools are selected by name. `CodegeistToolInput` wraps raw
  Spring AI JSON at the local-tool boundary. File tools use `CodegeistFileToolSupport`
  and the injected `CodegeistToolJsonMapper` to parse tool input, resolve the active
  workspace, return bounded model-visible text, and record the same bounded preview
  in `ToolSessionPart`. `CodegeistFileEncoding` resolves the global file-tool charset
  from optional direct `codegeist.yml` `workspace.encoding`, defaulting to UTF-8.
  Local file-tool multi-line output joins with `CodegeistFileToolSupport.LINE_SEPARATOR`,
  a shared constant backed by `System.lineSeparator()`, instead of inline newline
  literals. `CodegeistToolService` opens one closeable `CodegeistToolRun` per chat
  turn, exposes local and MCP callbacks through `CodegeistChatExecutionContext`, wraps
  MCP callbacks with `RecordingToolCallback`, and returns defensive copies of
  recorded `ToolSessionPart` values for session persistence. The MCP runtime lives
  under `ai.codegeist.app.mcp`: `CodegeistMcpAdapter` lazily maps direct `mcp:` config
  into prompt-scoped Spring AI callbacks for `stdio` and `streamable_http`, and
  `SpringAiMcpClientFactory` owns the Spring AI/MCP Java SDK transport details. MCP
  resources close when the tool run closes. `CodegeistMcpRun` exposes MCP callbacks
  through the Lombok-backed JavaBean accessor `getToolCallbacks()`; do not use
  Lombok `@Accessors` for this path. `docs/developer/architecture/local-file-tools.md`
  is the detailed current-state source-code guide for this subsystem and includes the
  first-provider-call to `McpSyncClient.callTool(...)` sequence.
- `T007_03_add-mcp-and-read-write-tools` is completed. The slice includes direct
  `mcp:` config, `stdio` and `streamable_http` MCP callbacks, local
  `codegeist_read`, `codegeist_list`, `codegeist_glob`, `codegeist_grep`, and
  `codegeist_write` tools, bounded `ToolSessionPart` recording, prompt-scoped MCP
  cleanup, and final focused plus broad JVM verification. The remote MCP path is
  verified by explicit `task mcp-remote-smoke`, which packages a deterministic local
  MCP server fixture, runs it in Docker, proves the real `streamable_http` callback
  path directly, then starts local Ollama and proves Spring Boot `ask` can make the
  model invoke the remote MCP tool and persist a completed `ToolSessionPart`. SSE,
  OAuth, server management, resources, prompts, public timeout config, and
  external-network-dependent MCP tests remain out of scope.
- `docs/tasks/T007_build-codegeist-runtime-harness/tasks/T007_05_add-agent-control-loop.md`
  is the planned follow-up for the actual OpenCode-style loop translation. It should
  add Codegeist-owned model/tool/model continuation on top of the current one-turn
  callback harness instead of reopening `T007_03_04`.
- `CodegeistConfigYamlMapper` is the concrete Spring service and Jackson mapper
  used by the config service for direct `codegeist.yml` parsing, empty-safe source
  tree loading, list-backed keyed element rendering, and direct YAML output.
- `CodegeistYamlExpressionEvaluator` is a Spring service that receives the config
  mapper and owns scalar SpEL preprocessing for direct YAML loads.
- `CodegeistConfigRootParser` receives the config mapper, owns supported root-name
  dispatch, explicit `provider:`, `mcp:`, and `workspace:` parser methods, provider
  `type` dispatch, MCP YAML-key id copying, shape checks, and unsupported-root errors.
- `CodegeistConfigService` receives the root parser, YAML mapper, SpEL evaluator,
  validator, and `codegeist.config` property. It exposes
  `loadCurrentConfig` as a primary `@Bean` parsed from `codegeist.config` when
  that property is set, and can load an explicit YAML path with
  `loadConfig(String configPath)` using a phased parser: read YAML into a Jackson
  tree, evaluate SpEL only in string scalar values containing `#{`, parse each
  top-level root through `CodegeistConfigRootParser`, then call
  `jakarta.validation.Validator`. Normal app code injects unqualified
  `CodegeistConfig` to get the primary config.
  `loadCurrentConfig()` uses the same global `codegeist.config` config path when
  that Spring property is set, commonly through `-Dcodegeist.config=<path>` at
  startup; otherwise it returns the empty default config. `--show-config` calls
  this central policy directly; `ask` receives the resulting primary
  `CodegeistConfig` bean by injection.
  Bean Validation and SpEL failures throw `CodegeistConfigValidationException`;
  Jackson mapping and IO failures surface directly through Lombok `@SneakyThrows`.
  `toYaml(...)` emits public direct `codegeist.yml` shape, leaves configured values
  unchanged, and renders empty configs as `{}` without creating synthetic roots.
  `--show-config` can
  print API keys or other sensitive config values when they are present.
  `CodegeistConfigServiceTest` owns empty-primary-config and direct-path loading,
  `CodegeistProviderConfigTest` owns type dispatch and local validation,
  `CodegeistConfigSpelEvaluationTest` owns scalar SpEL preprocessing, and
  `CodegeistConfigCommandTest` owns command stdout and direct-value rendering.
  There is no model-level multi-source combination helper now. Home-path
  discovery, service-level combination beyond the explicit config path,
  inheritance, provider runtime selection beyond a caller-supplied config,
  provider clients beyond the selected local Ollama `ChatModel`, model catalogs,
  provider-facing multi-turn prompt reconstruction, streaming, provider flags, and
  model flags are not implemented yet.
- `app/codegeist/cli/src/main/resources/logback.xml` routes logs only to
  `${LOG_FILE:-logs/codegeist.log}`. Console output is reserved for command
  output. Current Spring `@Service` and `@Component` classes use Lombok `@Slf4j`
  and emit `log.info(...)` for important system events, `log.debug(...)` for
  actionable diagnostics, and `log.trace(...)` for high-volume routine details;
  enable them with
  `logging.level.root=DEBUG` or `LOGGING_LEVEL_ROOT=DEBUG`.
- `app/codegeist/cli/Taskfile.yml` provides `test`, `build`, `run`, `native`,
  `native-smoke`, `local-linux-smoke`, `mcp-remote-smoke`, `qemu-windows-smoke`,
  `final-smoke-suite`, and `ollama-start`. Local smoke scripts live under
  `scripts/tests/`. `task test` delegates to Maven and accepts a focused selector
  as `task test TEST=<test-selector>`; new implementation tasks should document
  `task test` instead of direct `mvn test` commands. `task test` always runs
  `ollama-start` with `OLLAMA_ENTER=false` before Maven. `ollama-start` starts a
  persistent `ollama/ollama` container named `codegeist-ollama` with models
  mounted from `${OLLAMA_MODELS_DIR:-$HOME/.ollama/models}`. It uses NVIDIA GPU
  Docker flags only when `OLLAMA_GPU=true` or `OLLAMA_GPU=auto` detects NVIDIA
  support, so CPU-only Docker hosts can still start the local service. It now
  ensures the selected model, default `llama3.2:1b`, exists before reporting
  readiness. In an interactive terminal it enters the default model session; set
  `OLLAMA_ENTER=false` for non-interactive starts.
  `native-smoke` sources
  `scripts/tests/native-smoke.sh`; each native run recreates
  `target/smoke-test`, packages `target/dist/codegeist-linux-x64.tar.gz`,
  unpacks it into a fresh temp directory, starts host Ollama through
  `task ollama-start`, runs packaged `./codegeist --version`, asserts packaged
  `./codegeist --show-config` prints exactly `{}`, verifies packaged
  `./codegeist -Dcodegeist.config=<smoke-yml> ask <prompt>`, and writes
  `target/smoke-test/codegeist.log`. Linux jar and Windows QEMU smokes also run
  real Ollama-backed `ask`; `mcp-remote-smoke` builds the local MCP fixture Docker
  image, verifies the direct `streamable_http` callback path, then verifies an
  Ollama-backed `ask` call that invokes the remote MCP tool; Windows native archive
  `--show-config` expects `{}` for
  empty config. The Windows guest reaches host Ollama at `http://10.0.2.2:11434`
  by default. Smoke scripts now emit stable `Duration: <label>: <seconds>s` lines
  for Maven, package, jar, native compile, archive smoke, platform total, SSH, and
  QEMU wrapper timings. The latest full `task test` passed with 129 tests, 0
  failures, 0 errors, and 6 skips. The latest `task final-smoke-suite` passed with
  `linux platform smoke total: 89.644s`, `windows qemu smoke total: 212.295s`,
  `linux native ask smoke: 1.303s`, `windows jar ask smoke: 5.131s`, and
  `windows native ask smoke: 1.223s`.
- Branch `release/v0.1.0-github-release-build` adds `.github/workflows/release.yml`
  for GitHub-hosted release validation. Pushes to `release/v*` validate without
  publishing, `workflow_dispatch` supports pre-tag validation with
  `release_version=0.1.0`, and pushed `v*` tags publish release assets to a GitHub
  Release. Branch run `26535014716` passed JVM, Linux x64, Windows x64, macOS x64,
  and checksum jobs; the release job was correctly skipped on the branch run.
- Future release workflow iterations may start on an unversioned work branch, but
  branch validation still runs on a versioned
  `release/v<version>-github-release-build` branch created from the SemVer-inferred
  tag when needed. `main` should receive only one detailed squash-candidate commit.
  `/codegeist-release --source <release-work-branch> --rc <n>` owns version
  inference, validation-source branch creation, candidate creation, validation,
  fast-forward-only `main` promotion, final tag publication, downloaded checksum
  verification, and the `latest` GitHub Release mirror. `/codegeist-release` may
  also release directly from synchronized `main`; in that mode it infers SemVer
  from `last-tag..main`, skips validation-source and candidate branch creation, and
  starts at pre-tag validation.
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
  has passed locally with Linux and Windows jar/native statuses all `passed`; the
  latest run reported `linux platform smoke total: 96.305s` and
  `windows platform smoke total: 233.946s`.
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
- `docs/tests/` is the coding-agent and contributor test guidance directory.
  `docs/tests/README.md` is loaded through `.oc_local/opencode.json`, and links
  to focused test rules, provider feature test policy, and the smoke-test
  duration-output contract.
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
  Bean Validation flow, native metadata, command rendering, tests, and sharp edges,
  with compact class and flow diagrams for the implemented config slice.
- `docs/developer/specification/` now contains only the surviving high-level
  specifications and guidance:
  - `codegeist-opencode-parity.md`
  - `llm-provider-implementation.md`
  - `java-generation-guidance.md`
  - `testing-strategy-and-agent-rules.md`
  - `build-release-and-binary-smoke-strategy.md`
  - `native-packaging-posture.md`
  - `runtime-vocabulary.md`
  - `runtime-harness-implementation.md`
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
  model; the later current implementation now loads only explicit `codegeist.yml`
  paths through the central root parser instead of direct
  `@ConfigurationProperties` binding on `CodegeistConfig` or Codegeist roots in
  `application.yaml`.
  `T006_02` is solved with a Spring AI `2.0.0-M6` provider matrix, source evidence,
  candidate `codegeist.yml` and future `ProviderConfig` fields, Ollama as the first
  local provider candidate, and a no-cost integration-test posture that keeps
  hosted provider calls behind explicit `remote_free` selection plus local no-cost
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
  configured values unchanged, and empty configs render as `{}`. The config
  classes are data contracts only; all other provider types are deferred, and no
  Spring AI starters, provider clients, smoke commands, local model pulls, or
  remote calls were added. The implementation was verified with the full Maven
  suite, Linux jar/native smokes, and Windows QEMU jar/native smokes. `T006_05`
  now adds the provider-neutral chat seam, `spring-ai-ollama`, the local Ollama
  chat model, and `LocalOllamaProviderIT`. Ordinary startup was rechecked with
  `task test TEST=CodegeistApplicationTests`, and `OLLAMA_ENTER=false task
  ollama-start` now works on the current CPU-only Docker host through GPU auto
  fallback. The local `llama3.2:1b` model is downloaded in the Ollama model store,
  and `task test TEST=LocalOllamaProviderIT` passes. The live Ollama tests use fixed
  values, `http://localhost:11434` and `llama3.2:1b`, and no longer run a separate
  model-list preflight before the selected chat call. `T006_06` is solved as
  provider feature-test categories plus the one-shot `ask` command and real
  Ollama-backed smoke coverage:
  `ProviderConfig` is free of stored YAML model fields, options, enablement, and
  completions-path routing; runtime model names now flow through
  `CodegeistChatRequest`, and provider
  feature tests run through `task test` with method- or class-level
  `CODEGEIST_TEST_PROVIDER_CATEGORY` gating. The category default is `none`, so
  broad `task test` skips annotated provider calls; set
  `CODEGEIST_TEST_PROVIDER_CATEGORY=local` to run methods that call the fixed local
  Ollama service and `llama3.2:1b` model.
  Implemented provider test classes are `OpenAiProviderTest`, `OllamaProviderTest`,
  and class-gated `AskCommandsTest`; hosted methods require explicit `remote_free`
  or `remote_paid` category selection, and API-key presence alone never triggers
  remote calls. Verification passed for config-only provider tests, default
  non-provider `task test`, and local Ollama chat through both
  `LocalOllamaProviderIT` and `OllamaProviderTest`. The explicit OpenAI
  `remote_paid` run is currently blocked by the configured API key returning
  `401 invalid_api_key`; rerun it only after `CODEGEIST_TEST_OPENAI_APIKEY` is
  valid for the account being tested. `ask` uses the first configured provider and
  its provider-owned default model; Linux native smoke, Windows QEMU jar smoke, and
  Windows QEMU native smoke now pass against real host Ollama. The final smoke suite
  passed after the provider-owned default-model change. The test defaults now use
  low-cost OpenAI models `gpt-image-1-mini`, `tts-1`, and
  `gpt-4o-mini-transcribe`; the speech-to-text test generates its default English
  `espeak-ng` fixture under `target/provider-tests/` when needed.
- `docs/tasks/T007_build-codegeist-runtime-harness/` is open as the session-store
  tool harness epic. T007 now means `.codegeist/session.json`, `ask -c/--continue`,
  resumable file-based session state with multiple sessions per working directory,
  Codegeist-owned `codegeist.yml` `mcp:` client config, MCP/read/write tools where
  `write` is focused create/overwrite behavior, patch/edit, shell, and a minimum
  usable terminal coding-agent TUI over the same session store. `.codegeist/session.json`
  is the source of truth for saving and resuming chat history and tool activity
  only; it must not store provider config, selected provider/model, MCP client
  definitions, enabled tool definitions, permission rules, runtime status, or TUI
  state. `T007_02` is completed with `ai.codegeist.app.session`,
  `SessionStoreService`, the typed `SessionStoreObjectMapper`, `ask -c/--continue`,
  `TextSessionPart`, and `CompactionSessionPart`; it does not generate compaction
  summaries, perform runtime context pruning, or rebuild provider-facing context from
  stored history.
  Planned tool parts should stay minimal and omit optional title, metadata, timing,
  compaction-marker, and extra lifecycle-state fields until a focused task needs
  them. `T007_01` is completed as the scope-definition child at
  `docs/tasks/T007_build-codegeist-runtime-harness/tasks/T007_01_define-chat-file-tool-harness-scope.md`.
  Third-party research prompts and synthesized answers live in
  `docs/tasks/T007_build-codegeist-runtime-harness/third-party-question-catalog.md`
  and `docs/tasks/T007_build-codegeist-runtime-harness/third-party-question-answers.md`.
  Current T007 planning docs also include
  `runtime-harness-spec.md` for planned grouped class diagrams,
  `opencode-workflow-analysis.md` for source-backed OpenCode workflow evidence,
  `java-workflow-implementation.md` for the Java/Spring implementation guide, and
  `mcp-and-readwrite-tools-spec.md` as the specification-first handoff for
  `T007_03` MCP callbacks plus read/list/glob/grep/write tools.
  `mcp-and-readwrite-tools-question-catalog.md` is the focused `/ask-project`
  research catalog for OpenCode and Spring AI Agent Utils evidence before that
  implementation, and `mcp-and-readwrite-tools-research.md` now answers that
  catalog. `coding-agent-harness-implementations.md` documents the coding-agent
  harness comparison table, while `aider-mini-swe-harness-research.md` applies the
  smaller-harness catalog to the local Aider and mini-SWE-agent workspaces. The
  research recommends implementing `T007_03` as a narrow `ChatHarnessService` plus
  scoped `CodegeistToolRun`, keeping Agent Utils as source inspiration rather than
  directly exposing its file tools. It also analyzes Aider, SWE-agent, and
  mini-SWE-agent as smaller harness references: use them for scope discipline, not as
  new T007_03 requirements. `mcp-and-readwrite-tools-implementation-plan.md` is the
  concrete implementation plan with focused class views and a child-task split for
  `T007_03`; implement that work through the child tasks under
  `T007_03_add-mcp-and-read-write-tools/tasks/`, not as one large runtime change.
  The required OpenCode-style TUI elements are mapped to JLine implementation primitives in
  `docs/tasks/T007_build-codegeist-runtime-harness/tui-opencode-jline-mapping.md`.
  `T007_03_add-mcp-and-read-write-tools/task.md` is completed: direct `mcp:` config,
  workspace resolution, local read/list/glob/grep/write callbacks, the one-turn
  `ChatHarnessService`, MCP callback integration, Docker-backed remote MCP smoke,
  and final docs/verification are done. `T007_04_add-patch-edit-and-shell-tools/`
  is now a task directory. Its `task.md` is the canonical task file,
  `ask-project-question-catalog.md` is the source-backed question catalog for
  Aider, OpenCode, Pi, mini-SWE-agent, and Spring AI Agent Utils,
  `ask-project-research.md` answers that catalog with implementation guidance, and
  `opencode-shell-tool-comparison.md` is the focused OpenCode shell evidence. Use
  the T007_04 research before implementing patch/edit and shell tools; the local
  task-specification rule records the same T007_04 implementation boundaries.
  T007_04 now has focused implementation children for the working-directory guard,
  exact edit, structured patch, shell, and final documentation/verification slices.
  The working-directory guard child has a concrete implementation plan at
  `T007_04_add-patch-edit-and-shell-tools/working-directory-guard-implementation-plan.md`:
  add a package-private `CodegeistWorkingDirectoryGuard`, wire it into
  `codegeist_write` before mutation, keep read/list/glob/grep permissive, and shape
  the guard for later shell cwd checks before `ProcessBuilder.start()`.
  Remaining parent-level T007 children are `T007_04_add-patch-edit-and-shell-tools/task.md`,
  `T007_05_add-agent-control-loop.md`, `T007_06_add-terminal-tui-over-chat-file.md`,
  and `T007_07_verify-chat-file-tool-harness.md`. T007 still avoids a database,
  server runtime, remote sync, API/SDK, Vaadin, PF4J, JBang, LSP, skills, memory,
  and subagents.
- The previous T003 source-generation child tasks `T003_05` through `T003_12`
  were removed with their generated specification documents because they
  encouraged placeholder Java instead of tested behavior.

## Durable Decisions

- Future implementation should be iterative, Spring-first, and test-driven.
- Source comments should stay useful for coding agents: add or update class-level
  comments or Javadocs for new or substantially changed non-trivial Java classes,
  explain why, non-obvious framework behavior, cross-file contracts, or sharp
  edges, and include related file/doc references when they help recover context
  quickly.
- `.oc_local/rules/java-coding.md` records the Codegeist Java coding convention:
  prefer annotation-based Spring wiring, use Lombok `@RequiredArgsConstructor` with
  `private final` collaborators for Spring-managed dependencies, use Lombok `@Slf4j`
  on Spring `@Service` and `@Component` classes, and use focused Lombok annotations
  such as `@Getter` and `@Setter` to reduce boilerplate. Prefer named
  `static final String` constants for contract-bearing strings; shared app-wide
  values belong in `CodegeistApplication`, for example `APP_NAME`; common SDK-owned
  values such as line separators should be exposed through shared class-owned
  constants backed by Java SDK calls such as `System.lineSeparator()`. Tests should
  reuse owning constants for class-owned error messages and prefixes, such as
  `CodegeistConfigService.VALIDATION_ERROR_PREFIX`, instead of duplicating string
  fragments. Use existing framework utilities before hand-written helper logic,
  for example Spring `StringUtils.hasText(...)` for null-or-blank text checks. Avoid
  one-line pass-through helper methods when a direct call remains readable. The
  rule now also prefers dependency injection over passing Spring-managed
  collaborators such as `ObjectMapper` through method parameters, uses Lombok
  `@NonNull` for method parameters that must not be Java `null`, avoids avoidable
  fully qualified Java type names in code, forbids dead code and speculative
  convenience APIs without a current call site or framework/serialization contract,
  emits meaningful SLF4J diagnostics for non-obvious lifecycle, parsing,
  validation, fallback, skipped-candidate, external-call, and failure-translation
  behavior with `info` for important system events, `debug` for actionable skips
  such as malformed files, and `trace` for high-volume routine details, avoids
  duplicate Jackson property constructors when no-arg mutable binding is the
  chosen contract, forbids pass-through wrappers that only delegate to another
  method with the same inputs, requires a small refactoring check for touched Java
  classes and packages, requires focused developer documentation for each new
  implemented feature,
  keeps provider config access-only, centralizes optional default
  provider selection in `CodegeistConfig.defaultProvider()`, uses
  `ProviderConfig.defaultModel()` for provider-owned runtime fallbacks, keeps
  provider config separate from `CodegeistChatRequest`, and prevents reintroducing
  chat model factory or strategy layers unless a focused task changes that contract.
- Codegeist provider configuration currently has config-only typed `ollama` and
  `openai` provider entries. Provider config has no stored YAML model fields;
  explicit model selection belongs to the coding agent, session, command, request,
  or provider feature test method, while concrete provider configs own their default
  runtime model fallback.
  `CodegeistConfig` should stay a root-element container; new top-level config
  sections should extend the central `CodegeistConfigRootParser` dispatch and return
  plain `CodegeistConfigRootElement` model objects instead of adding one field per
  root to `CodegeistConfig`.
- Use Jackson YAML (`jackson-dataformat-yaml`) as the current direct YAML-to-POJO
  mapping framework for `codegeist.yml`. Current direct YAML loading evaluates a
  minimal Spring SpEL phase before mapping string scalar values containing `#{`.
  current direct YAML loading validates configured `base-url` and ordinary scalar
  credential fields. Provider enablement, completion-path routing, models,
  generation options, feature capabilities, inheritance, runtime source
  orchestration beyond the task scope, provider starters, and additional provider
  calls stay deferred to later T006 child tasks.
- Do not create placeholder classes, ids, ports, enums, records, package layers,
  validation hierarchies, or empty package directories before a focused test or
  workflow needs them.
- Keep the active task file small enough to revise during implementation instead
  of creating broad implementation handoff documents.
- First provider-backed workflow should use an externally managed local Ollama
  instance started through `task ollama-start`. Do not use Testcontainers or pull
  local models from Java tests; the Taskfile owns host container startup and
  selected-model availability. Keep runtime request options deterministic when the
  active path supports them, and keep assertions constrained enough to be stable.
- Provider chat runtime work should follow
  `docs/developer/specification/llm-provider-implementation.md`: Codegeist chat
  stays provider-neutral through `CodegeistChatService` and
  `CodegeistChatModel<T extends ProviderConfig>`; provider-specific Spring AI
  imports belong only in the concrete provider model such as `OllamaChatModel`,
  which should receive `OllamaProviderConfig` directly and map the runtime model at
  call time.
- For local Ollama verification, use one Taskfile command such as
  `CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=<test-selector>`; the
  `test` task starts Ollama before Maven for every test run.
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
- Do not merge multi-commit release work branches directly into `main`. Promote
  them through `/codegeist-release --source <release-work-branch> --rc <n>`; the
  command infers SemVer from the diff between the latest reachable release tag and
  the source commit, creates the matching versioned validation branch when needed,
  writes a detailed squash commit message, and advances `main` by fast-forward
  only. Direct releases from synchronized `main` are allowed and skip the squash
  path because there is no candidate diff to commit.
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
  release builds, and use `/codegeist-release --source <release-work-branch> --rc <n>`
  or `/codegeist-release` from synchronized `main` for release publication. The
  command handles version inference, optional versioned validation branch creation,
  candidate promotion when needed, pre-tag validation, final `v*` tag creation, and
  automatic GitHub Release publication.
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
- Continue the next T007 implementation from `T007_03_03`, adding the first
  read/list/glob/grep/write tool path on top of the implemented workspace resolver,
  output bounds, and `.codegeist/session.json` store. Use
  `docs/tasks/T007_build-codegeist-runtime-harness/session-store-model-specification.md`
  for the current session-store model.
- Source-close third-party questions should use
  `/ask-project <project> "<question>"`. `/ask-project` consumes the analyzed
  project workspace and delegates broad packed-source questions to the `@repomix`
  subagent.

## Third-Party Analysis

- `docs/third-party/opencode/source` is a submodule for OpenCode on branch `dev`,
  currently at `d46af9cf1e7168d519377044f2412dea08ead5f8`.
- `docs/third-party/opencode/` contains the OpenCode analysis workspace:
  `README.md`, `ANALYSIS_REPORT.md`, `REGENERATE.md`, feature/user/developer
  notes, and Mermaid sources. Heavy Graphify, Repomix, manifest, verification,
  and rendered artifacts are regenerable and ignored. The latest Repomix run
  packed 3,033 files; the focused Graphify run used 148 files and generated
  1,340 nodes, 2,011 edges, and 93 communities. Use a temporary non-ignored
  `graphify-input-focus` input directory for regeneration, not ignored
  `graphify-corpus*` paths, because Graphify skips ignored directories.
- `docs/third-party/spring-ai-agent-utils/` contains the Spring AI Agent Utils
  analysis workspace with source submodule, durable docs, and ignored local
  Repomix/Graphify/manifest/verification artifacts. The latest structural
  Graphify cache has 1,586 nodes, 3,537 edges, and 69 communities. Use it for
  navigation, then inspect source/tests before adoption decisions.
- `docs/third-party/aider/` contains the Aider analysis workspace with source
  submodule on `main` at `5dc9490bb35f9729ef2c95d00a19ccd30c26339c`, durable
  handoff docs, an editable Mermaid runtime-flow source, and ignored local
  Repomix/Graphify/manifest/verification artifacts. Use it for harness, repo-map,
  git/edit/lint/test loop, and terminal coding-agent comparisons, not as an MCP
  lifecycle reference.
- `docs/third-party/mini-swe-agent/` contains the mini-SWE-agent analysis
  workspace with source submodule on `main` at
  `2caffc565474b8856a323ff163ffb7ab98d1ef02`, durable handoff docs, an editable
  Mermaid runtime-flow source, and ignored local Repomix/Graphify/manifest/
  verification artifacts. The latest focused Graphify run used 203 filtered
  source/doc files and generated 1,874 nodes, 2,955 edges, and 131 communities;
  use `/ask-project mini-swe-agent ...` for follow-up source-backed questions.
- `docs/tasks/T007_build-codegeist-runtime-harness/aider-mini-swe-harness-research.md`
  applies the T007_03 smaller-harness catalog to the local Aider and
  mini-SWE-agent workspaces. It confirms that neither project implements MCP, but
  Aider is useful for broad repository-tooling hazards and mini-SWE-agent is useful
  for keeping Codegeist's first `ChatHarnessService` plus `CodegeistToolRun` narrow.

## Open Points

- Keep `docs/developer/architecture/architecture.md` synchronized whenever
  implemented packages, classes, configuration, runtime flows, or tests change.
- For the next release, run `/codegeist-release --source <release-work-branch> --rc 1`
  for a work branch, or `/codegeist-release` from synchronized `main` when the
  release-ready work is already there. Do not enter the version manually unless
  checking an inferred-version conflict.
- Next provider work should add one provider feature method or runtime integration at
  a time. Use the solved `T006_03` account/free-tier catalog before adding hosted
  provider-specific methods or treating any hosted provider feature as
  `remote_free`; use its availability matrix before adding a provider starter or
  client code.
