# Codegeist Task Workflow Overlay

Use the shared task phase commands from `.opencode` for task workflow phases:

- `/specify-task <task-ref> [context/instructions]`
- `/plan-task <task-ref> [context/instructions]`
- `/solve-task <task-ref> [context/instructions]`
- `/work-task <task-ref> [context/instructions]`

This overlay adds only Codegeist-specific guidance. Keep generic phase behavior in
`.opencode/rules/task-phases.md` and `.opencode/rules/task-workflow.md`.

## Codegeist Guidance

- Treat `T001` child tasks as documentation-first Codegeist/OpenCode parity
  architecture work unless a task explicitly asks for runtime implementation.
- Treat `T002_01_align-codegeist-build-baseline.md` as the completed build/layout
  baseline exception. For `T002_02` and later, solve child tasks as
  documentation/specification handoffs unless the user explicitly reopens a task
  as implementation work.
- For Codegeist architecture and implementation tasks, read
  `docs/developer/specification/codegeist-opencode-parity.md` when the target task references
  OpenCode parity, runtime boundaries, provider behavior, tools, permissions,
  workspace policy, storage, UI, plugin surfaces, or packaging.
- Use OpenCode as a feature and behavior reference, not as an implementation
  blueprint. Map decisions onto Codegeist's Java-first architecture: Java,
  GraalVM, Spring, Spring AI, Spring Shell, Vaadin, JBang, and PF4J.
- For `T002` foundation tasks, the parent task declares default solve hints for
  OpenCode translation and source-evidence workflows. Use those parent hints
  automatically through the shared hint discovery behavior.
- For vocabulary or boundary slices, do not create empty Java package directories
  only to reserve names. Git does not version empty directories, and premature
  classes can imply unstable contracts. Prefer a focused developer document or
  diagram until a later task is ready to define behavior-free Java contracts.
- When a Codegeist architecture decision affects later child tasks, update those
  task files in the same phase pass so dependencies, non-goals, acceptance
  criteria, and implementation-readiness questions stay current.
- Keep repo-specific context paths such as task docs, memory docs, local rules, and
  developer docs as context-profile data owned by repo commands and rules. Do not
  hard-code this repository's `docs/` layout or external analysis artifacts as
  Codegeist core runtime context sources.
- Keep Codegeist-specific source evidence under `docs/third-party/opencode/` and
  prefer `/ask-project opencode ...` when a phase needs source-backed OpenCode
  behavior details. `/ask-project` owns any Repomix-backed deep dive internally.
- Spring AI Agent Utils may be used directly inside Codegeist implementation code
  when it helps move faster, but Codegeist runtime, provider, tool, permission,
  workspace, event, session, storage, API, and UI contracts must not depend on
  Agent Utils architecture or raw broad provider callbacks. Add an adapter only
  when a concrete boundary needs policy mediation, result mapping, or replacement
  flexibility.
- T003 core implementation scope includes both CLI and TUI behavior. Keep JBang,
  PF4J, Vaadin, headless server, API, and SDK/OpenAPI implementation in the
  backlog while preserving adapter-ready runtime boundaries for those later
  surfaces.
- For T003 Java implementation tasks, use
  `docs/developer/specification/java-generation-guidance.md` as the source
  generation contract and keep planned package boundaries separate from current
  implemented source.
- For Codegeist behavior changes and bug fixes, use
  `docs/developer/specification/testing-strategy-and-agent-rules.md`: TDD is the
  default, tests should stay individually executable, and solve results should
  report targeted commands plus enough timing detail to spot slow tests or slow
  startup.
- For Spring Boot tests that need to assert stdout or stderr, prefer Spring
  Boot's `OutputCaptureExtension` and `CapturedOutput` over custom
  `System.setOut` or `System.setErr` helpers.
- For Spring Shell CLI command behavior, prefer Spring integration tests that run
  through `@SpringBootTest` and command arguments over unit tests that instantiate
  command classes manually.
- For Codegeist application version output, prefer Spring Boot's
  `BuildProperties` bean over manually parsing `META-INF/build-info.properties`.
  Keep `META-INF/build-info.properties` available as the generated build-info
  source and include it in GraalVM resource metadata when the native image needs
  it.
- For Spring Shell command output, prefer the command `CommandContext` and
  `CommandContext.outputWriter()` over direct `System.out`/`System.err` usage or
  a custom stdout service bean. Flush the writer after no-newline command output
  such as `--version`.
- For packaging, release, platform, or binary-smoke work, use
  `docs/developer/specification/build-release-and-binary-smoke-strategy.md`:
  GitHub Releases are the release target, Windows/Linux/macOS support must be
  proven explicitly, and each platform check should report `passed`, `skipped`
  with reason, or `failed` with blocker.
- For cross-platform release implementation, split local validation from GitHub
  release automation when practical: solve local Linux/Windows build-smoke work
  before GitHub release publication, use GitHub-hosted macOS runners for macOS
  release artifacts, and require a `gh`-triggered pre-tag workflow validation
  before creating the final `v*` release tag. Keep local Windows validation on a
  real Windows VM over SSH or a matching GitHub Windows runner; do not add local
  compatibility-layer smoke paths for Windows release validation.
- For publishing a Codegeist GitHub Release, prefer
  `/codegeist-release --source <release-branch> --rc <n>`. The command uses
  `.opencode/rules/semver.md` to infer the release version from the diff between
  the latest reachable release tag and the release branch commit, creates
  `release/v<version>-codegeist-rc-<n>` from current `main`, squashes the
  iteration diff into one detailed commit, validates that candidate branch,
  advances `main` by fast-forward only, then owns pre-tag validation, annotated tag
  creation, automatic tag-run publication, published asset checksum verification,
  and the `latest` tag plus `latest` GitHub Release mirror using the already
  verified `v*` assets.
- For Spring Shell command-line arguments such as `--version`, keep the current
  default command path noninteractive with
  `spring.shell.interactive.enabled=false` until a task intentionally implements
  interactive shell behavior. Spring Shell's interactive runner ignores process
  arguments; do not add a custom runner class unless the task explicitly needs one
  binary to support both REPL startup and argument dispatch.
- For GraalVM native-image work, remember that Spring AOT fixes conditional bean
  choices such as the Spring Shell runner during native compilation. Configure
  the intended mode before the native build and prove it with `task native-smoke`.
- For Codegeist native smoke scripts, keep smoke artifacts under
  `target/smoke-test`, delete and recreate that directory at the start of each
  smoke run, route `LOG_FILE` to `target/smoke-test/codegeist.log`, and keep the
  Taskfile path as a sourced function call such as
  `source ../../../scripts/tests/native-smoke.sh; run-native-smoke-tests`.
- For Codegeist smoke scripts, treat expected devcontainer tools such as `timeout`
  and `curl` as part of the script contract and call them directly. Use
  command-existence checks only when the result drives real `passed`, `skipped`,
  `failed`, or guest installation behavior.
- For local GPU-backed Ollama development, keep NVIDIA runtime customizations in
  local `.codegeist/` devcontainer overrides such as `.codegeist/Dockerfile` and
  `.codegeist/compose.local.yml`; do not edit the shared `.devcontainer` submodule
  for consumer-specific GPU setup. Keep Ollama models under
  `${OLLAMA_MODELS_DIR:-$HOME/.ollama/models}` and use `OLLAMA_ENTER=false task
  ollama-start` for non-interactive automation.
- For GraalVM resource inclusion, prefer metadata under
  `src/main/resources/META-INF/native-image/resource-config.json` for simple
  resource patterns such as `logback.xml` and `META-INF/build-info.properties`.
  Avoid Java `RuntimeHints` when the user wants native configuration out of code,
  and do not use nonstandard filenames such as
  `META-INF/native-image-resource-config.json` unless the native-image command is
  explicitly configured to load them.
- For T003 and later implementation slots, do not create source-generation
  handoff documents before writing code. Keep the active task small, write or
  update the focused test first when practical, and let real Spring behavior drive
  any new Java type or package boundary.
- The previous T004 implementation epic was discarded. When a replacement
  implementation epic is created, keep it iterative and Spring-first. Do not create
  broad implementation handoff documents under `docs/developer/implementation/`;
  that directory's previous handoffs are obsolete. Before solving implementation
  tasks, keep the active task file scoped to the next smallest tested Spring
  workflow and avoid placeholder classes, ids, ports, enums, package layers, or
  validation hierarchies.
- For the first provider-backed workflow in the replacement epic, use a pinned
  local Ollama Testcontainer with `llama3` instead of a fake provider. Pin the
  Ollama image and model tag, set `temperature=0`, use a fixed seed when the active
  Spring AI and Ollama versions support it, and keep assertions constrained enough
  to be stable.
- During implementation specify, plan, and solve phases, use
  `docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md`: first ask
  `/ask-project spring-ai-agent-utils ...` for Java/Spring-side equivalents. When
  Agent Utils already fits, prefer it as a private implementation detail or behind
  a thin Codegeist wrapper for policy, workspace, permission, session/event,
  output, and result mapping.
- During the same implementation phases, when behavior is not already present in Java or
  covered by a suitable Spring AI Agent Utils equivalent, use `/ask-project
  opencode ...` to inspect how OpenCode implements the behavior before translating
  the relevant contract and flow into Codegeist's Java-first architecture.
- Implementation solve phases may write Java source and tests, but they must follow
  the current plan, start from the planned failing test where practical, run the
  planned narrow test commands, report timing, and update
  `docs/developer/architecture/architecture.md` when implemented packages, classes,
  configuration, or tests change.
- For `T006_build-provider-configuration-feature`, work provider support in order:
  first design the `codegeist.yml` schema with `kebab-case` keys such as
  `small-model`, `enabled-providers`, `base-url`, and `api-key-env`; then create
  the Spring AI provider matrix; then define credential and account strategy;
  only then implement config loading, local Ollama verification, and remote
  connection smokes. Do not create provider accounts, use paid provider resources,
  or run remote provider tests before the schema, matrix, and credential strategy
  tasks are complete.
- For Codegeist provider integration tests, cover as many configured providers as
  possible without causing charges: default tests may validate config, credential
  references, and client wiring without hosted provider calls; local tests may run
  real local providers such as Ollama; hosted provider calls require an explicit
  `remote-free` selection plus local confirmation that the selected account, key,
  and model path will not bill. API-key presence alone is never permission to call
  a remote provider.
- For Codegeist provider config, follow OpenCode's useful shape only as behavior
  evidence: root `model` uses `provider-id/model-id`, providers live under the
  `provider` map, provider and model options remain nested, and credentials are
  referenced rather than stored in committed project config.
