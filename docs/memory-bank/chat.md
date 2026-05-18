# Chat Memory

## Target State

- `codegeist.ai` should grow into a customizable coding agent for CLI, TUI, and
  web use.
- The development environment remains repo-local: devcontainer configuration,
  rules, commands, and project memory live in the repository.

## Current State

- `main` contains the current project state. `.opencode` is configured as a git
  submodule that tracks the `release` branch of `codegeist-agent-kit` and points
  to `c79edf1`.
- `.devcontainer/` is configured as a git submodule that tracks the `release`
  branch of `codegeist-devcontainer-kit` in `.gitmodules` and points to
  `35f46d9`.
- `start.sh` has been removed; the devcontainer is started directly through VS
  Code Dev Containers or `devcontainer up --workspace-folder .`.
- `.devcontainer/initialize.sh` from the kit creates root `.local.env`,
  `compose.local.yml`, root `.oc_local/`, `.devcontainer/.env`, and
  `.devcontainer/compose.local.gen.yml` when needed, and can select a worktree
  under `.worktrees/<branch>` as `/workspace` via `BRANCH`.
- `app/codegeist/cli` is a Spring Boot and Spring Shell CLI bootstrap with Java
  25, Maven, and a prepared GraalVM native build.
- `app/codegeist/cli/Taskfile.yml` provides `test`, `build`, `run`, and `native`.
- `.devcontainer/Dockerfile` installs `nix` in addition to the existing
  apt-based toolchain without migrating the devcontainer workflow to Nix.
- `.devcontainer/Dockerfile` also installs `@devcontainers/cli` in the existing
  global npm tools block.
- `.devcontainer/tests.sh` now lives in the extracted `.devcontainer` submodule
  and remains the devcontainer self-test.
- The Nix profile hook is wired globally through `/etc/profile.d/nix.sh` so
  `nix` is available on `PATH` in container login shells.
- The local OpenCode overlay `.oc_local/` contains the commands
  `/analyse-project` and `/ask-project`, the `@repomix` subagent, and the AI
  script `render-mermaid.sh` for Mermaid SVG rendering. The task phase commands
  now come from the shared `.opencode` agent kit.
- `docs/third-party/opencode/source` is a submodule for
  `https://github.com/anomalyco/opencode.git` on branch `dev` and points to
  `22e64ca`.
- `docs/third-party/opencode/` contains the initial third-party documentation
  workspace for OpenCode: `README.md`, `ANALYSIS_REPORT.md`, `REGENERATE.md`,
  feature, user, and developer notes, plus Mermaid sources.
- `docs/tasks/T001_define-codegeist-opencode-feature-architecture/` is the active
  architecture epic for Codegeist/OpenCode parity. The epic is split into 25
  granular documentation tasks under `tasks/`.
- `docs/developer/specification/codegeist-opencode-parity.md` records the Codegeist/OpenCode
  parity architecture. Codegeist is mapped onto the Java-first stack instead of
  being planned as an OpenCode/Bun/TypeScript copy. The document now covers the
  technology baseline, OpenCode-to-Java mapping, module boundaries, CLI/Shell
  architecture, agent modes, session model, event model, provider architecture,
  tool/permission/workspace boundaries, shell and patch/edit architecture,
  context loading, PF4J/JBang/Vaadin/server/storage roles, GraalVM constraints,
  feature matrix, MVP cut, prompt flow, risk register, and implementation
  backlog.
- `docs/developer/architecture/architecture.md` is the current-state architecture
  map for coding agents. It describes only what exists now in `app/codegeist/cli`,
  including the single Maven module, Spring Boot entrypoint, configuration,
  context-load test, Taskfile commands, current build baseline, and
  not-yet-implemented boundaries.
- All 25 child tasks under
  `docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/` have
  been checked with `/specify-task` and solved with `/solve-task`. Each child
  task now has a `Solution Note` that points to the corresponding section in
  `docs/developer/specification/codegeist-opencode-parity.md`.
- `docs/tasks/T002_implement-codegeist-mvp-foundation/` is the active foundation
  specification parent for the first Codegeist MVP work. `T002_01` remains the
  completed build/layout baseline exception. `T002_02` and later are
  documentation/specification slices, not Java implementation tasks: they should
  define developer docs, diagrams, future file maps, contract sketches, and
  handoff checklists before later implementation tasks are created.
- The T002 parent and all `T002_*` child tasks have been rechecked with
  `/specify-task` semantics after adding parent default hints. Each task now has a
  `Specification Check Result` clarifying scope, dependencies, OpenCode source
  research expectations, and non-implementation boundaries.
- `T002_01_align-codegeist-build-baseline.md` was expanded into a build and CLI
  layout baseline: the tracked Spring Boot CLI Maven project moved from
  `app/codegeist` to `app/codegeist/cli`, while future `server`, customization
  storage, model access, and deployment-artifact boundaries remain documented but
  not implemented. `task test`, `task build`, and `task native` pass from
  `app/codegeist/cli`.
- `T002_02_introduce-runtime-vocabulary-contracts.md` is finalized as a
  documentation and diagram slice. It created
  `docs/developer/specification/runtime-vocabulary.md` for Codegeist-owned runtime terms,
  boundary direction, deferred surfaces, and non-ownership rules without creating
  Java packages, classes, or empty directories.
- `T002_03_introduce-runtime-session-event-contracts.md` is specified as a
  documentation and diagram slice after the user narrowed it away from Java
  implementation. It created
  `docs/developer/specification/runtime-session-event-contracts.md` as the future Java contract
  blueprint for prompt requests, agent modes, sessions, turns, message parts,
  runtime event envelopes, event families, sequencing, and event-to-session
  projection. It used OpenCode source evidence from `session/schema.ts`,
  `v2/session.ts`, `v2/session-event.ts`, `v2/session-message.ts`,
  `sync/index.ts`, `bus/index.ts`, and `session/projectors-next.ts`, but did not
  create Java source files or empty packages. The blueprint now includes
  OpenCode-focused class diagrams for identity, session service, session events,
  session messages, sync/bus/projectors, and a concept reference table mapping
  each OpenCode implementation idea to the Codegeist blueprint.
- `T002_04_wire_cli_prompt_mode_contract.md` is finalized as a documentation-only
  solution-design slice with OpenCode reference
  evidence for CLI/TUI prompt input, agent/mode selection, session prompt
  delegation, and command-template boundaries. The relevant OpenCode files are
  `cli/cmd/tui/thread.ts`, `config/config.ts`, `config/agent.ts`,
  `agent/agent.ts`, `server/routes/instance/httpapi/groups/session.ts`,
  `server/routes/instance/httpapi/handlers/session.ts`, and `session/prompt.ts`.
  The Codegeist translation is a minimal Spring Shell adapter that maps explicit
  Plan/Build input to runtime request contracts without owning provider, tool,
  permission, storage, session lifecycle, or event behavior. The task now includes
  a future file map, command-flow sequence, boundary rules, deep Java examples for
  runtime records/ports, Spring Shell adapter examples, adapter test examples, and
  a future acceptance checklist. It intentionally created no Java source, tests,
  packages, or build changes. Finalization confirmed that `T002_05` still owns
  active-task path, context-source, and workspace-policy decisions.
- `T002_05_add_context_workspace_manifest_slice.md` is finalized as a
  documentation-only context/workspace architecture task, with no child task and no
  Java implementation work. It created and then deepened
  `docs/developer/specification/context-workspace-manifest.md`, which defines future workspace
  path-validation responsibilities, explicit context loader request fields,
  deterministic source ordering, context manifest fields, skip reasons,
  external-analysis exclusion posture, OpenCode lessons, permission boundaries,
  deferred implementation slices, future Java file maps, illustrative Java
  snippets, and future test handoff notes. It created no Java source, tests,
  package directories, provider calls, embeddings, Graphify/Repomix runs, or
  runtime behavior. Later corrections clarified that the whole `docs/` layout is
  repository-specific context-profile input maintained by repo rules and commands,
  not a Codegeist hard-coded constant, and that third-party analysis artifacts are
  not a core context source kind.
- By user decision, the remaining open `T002_06` through `T002_12` tasks have been
  reframed as documentation/specification slices. They must not create Java source
  files, tests, empty package directories, provider calls, tool execution,
  Graphify/Repomix runs, storage adapters, process executors, or runtime behavior
  unless explicitly reopened as implementation work.
- A follow-up audit confirmed the T002 task set matches that posture: `T002_01` is
  the only completed build/layout implementation exception, while `T002_02` through
  `T002_12` remain documentation/design/specification slices.
- Finalization propagated `T002_05` impact notes to the parent task, `T002_06`, and
  `T002_07`: provider configuration must not own context profiles, workspace
  reads, or external analysis ingestion, and tool/permission contracts should layer
  permission approval above deterministic workspace validation.
- `T002_06_add_provider_configuration_adapter.md` is finalized as a
  documentation-only provider configuration and Spring AI adapter blueprint. It
  created `docs/developer/specification/provider-configuration-contracts.md` with UML class
  diagrams, Spring AI counterpart mapping, future file maps, first-wave
  OpenAI-compatible/OpenAI and Ollama support, later Spring AI provider extension
  rules, typed provider errors, validation posture, and future test handoff notes.
  It created no Java source, tests, provider starters, credentials, or live model
  calls.
- `T002_07_add_tool_permission_workspace_contracts.md` is finalized as a
  documentation-only tool, permission, and workspace contract blueprint. It created
  `docs/developer/specification/tool-permission-workspace-contracts.md`, which defines
  Codegeist-owned tool descriptors, request/result and failure shapes, permission
  request/decision/scope metadata, workspace tool-target validation, bounded
  output references, runtime event/session projection, Spring AI tool-call
  mediation, OpenCode source evidence, future file maps, illustrative Java
  snippets, and future test handoff notes. It created no Java source, tests,
  package directories, provider callbacks, shell execution, patch/edit behavior,
  PF4J, JBang, Graphify, Repomix, or runtime behavior.
- Finalization propagated `T002_07` impact notes to the parent task, `T002_08`,
  `T002_09`, and `T002_12`: patch/edit, shell, and extension/client readiness
  work should specialize the generic tool/permission/workspace blueprint rather
  than redefining tool policy, and must keep side effects documentation-only until
  explicitly reopened as implementation work.
- `T002_08_add_patch_edit_proposal_flow.md` is finalized as a documentation-only
  patch/edit proposal and apply-result blueprint. It created
  `docs/developer/specification/patch-edit-proposal-contracts.md`, which specializes finalized
  `T002_07` tool/permission/workspace contracts for exact proposal review,
  Build-mode apply, permission approval, workspace validation, proposal freshness,
  typed failures, bounded summaries, output references, and future test handoff.
  It created no Java source, tests, package directories, patch parser, apply
  executor, file writes, or runtime behavior. Finalization propagated the bounded
  result, typed failure, permission, workspace, and event/session summary posture
  to `T002_09` while keeping shell execution separate from patch/edit apply
  behavior.
- `T002_09_add_controlled_shell_verification_tool.md` is finalized as a
  documentation-only controlled shell verification blueprint. It created
  `docs/developer/specification/shell-verification-contracts.md`, which specializes finalized
  `T002_07` tool/permission/workspace contracts for Plan-mode shell denial,
  Build-mode approval-gated verification commands, destructive-command safety
  posture, workspace-cwd validation, env/stdin policy, timeout/cancellation,
  exit-code and typed failure results, bounded stdout/stderr summaries,
  `OutputRef` values, and event/session projection. It created no Java source,
  tests, package directories, process executor, PTY support, terminal UI, remote
  execution, JBang execution, shell sandboxing, Graphify, Repomix, or runtime
  behavior. Finalization propagated shell-output and process-execution readiness
  notes to `T002_10` and `T002_12`.
- `T002_10_decide_minimal_storage_ports.md` is finalized as a documentation-only
  minimal storage posture blueprint. It created
  `docs/developer/specification/storage-port-posture.md`, which selects in-memory storage first
  behind replaceable ports and defers file-backed restart/continue/list persistence
  until a concrete CLI workflow requires it. The blueprint keeps event sourcing
  optional, excludes credentials and sensitive tool/shell/provider artifacts from
  ordinary session storage, defines future session/message projection/event
  projection/artifact-reference/health ports, and records future in-memory,
  redaction, projection, artifact-reference, file-backed continuation, and
  event-sourcing optionality tests. It created no Java source, tests, package
  directories, storage ports, storage adapters, database schemas, migrations,
  encryption, durable audit logs, compaction, event replay, Graphify, Repomix, or
  runtime behavior. Finalization propagated storage readiness notes to `T002_12`.
- `T002_11_validate_native_packaging_posture.md` is finalized as a
  documentation-only native packaging posture blueprint. It created
  `docs/developer/specification/native-packaging-posture.md`, which keeps the MVP foundation
  JVM-first and native-aware, defines future JVM jar and GraalVM native-image
  verification ladders, requires native status to be reported as `passed`,
  `skipped` with reason, or `failed` with a concrete blocker, and treats PF4J,
  JBang, Vaadin, server, broad providers, shell/process, and storage surfaces as
  JVM-first until their own tasks prove native compatibility. It created no Java
  source, tests, package directories, Maven changes, Taskfile commands, provider
  dependencies, PF4J, JBang, Vaadin, server, or runtime behavior.
- `T002_12_define_extension_and_client_readiness_gates.md` is finalized as a
  documentation-only extension/client readiness blueprint. It created
  `docs/developer/specification/extension-client-readiness-gates.md`, which gates later PF4J,
  JBang, headless server, Vaadin, SDK/OpenAPI, and TUI work behind runtime API,
  session/event, tool/permission/workspace, storage, auth/security, native posture,
  and test-readiness decisions. It created no Java source, tests, dependencies,
  adapters, server routes, Vaadin views, PF4J plugins, JBang scripts, SDK
  generation, TUI behavior, or runtime behavior.
- The `opencode` analysis uses a focused runtime corpus for Graphify instead of
  the whole repository. The last Graphify run produced 1,247 nodes, 2,008 edges,
  and 78 communities; Graphify, Repomix, and verify outputs remain regenerable
  and ignored.
- Source-close questions about third-party projects should use
  `/ask-project <project> "<question>"`. The command uses the analyzed project's
  `docs/third-party/<project>/repomix-output.xml` through the `@repomix` subagent
  when broad packed-output context is needed, so the main context stays small.

## Important Decisions

- Build artifacts such as `target/`, `bin/`, `.class`, and `.jar` stay out of
  git.
- Local devcontainer environment values stay in root `.local.env`; root
  `compose.local.yml` is the versioned local Compose override that
  `devcontainer.json` always includes.
- Repo memory is maintained in `docs/memory-bank/chat.md`.
- Nix is only installed as an additional package manager for now; there are no
  flakes and no migration of the toolchain to Nix packages yet.
- Checkouts without recursive submodules are repaired with
  `git submodule update --init --recursive`, not through a repo-local launcher.
- `.opencode` and `.devcontainer` should be updated in the parent repo through
  their `release` branches, not through the previously used `main` branches or
  repo-local launcher scripts.
- Shared kit updates use the OpenCode workflow `/update-submodules`, which sets
  `.opencode` and `.devcontainer` to the branches configured in `.gitmodules`.
- Project-specific analysis workflows belong in the local overlay `.oc_local/`,
  not in the shared `.opencode` submodule. Third-party analysis artifacts are
  stored under `docs/third-party/<project-name>/`; Mermaid sources live under
  `diagrams/source/` and rendered SVGs under `diagrams/rendered/`. Regenerable
  heavy artifacts such as `repomix-output.*`, `graphify-out/`, logs, manifests,
  verify reports, and rendered SVGs stay out of git via `.gitignore` and are
  regenerated through `REGENERATE.md`.
- `/analyse-project` no longer uses its own analysis shell script. The old
  `.oc_local/ai-scripts/analyse-project.sh` orchestration was removed; graph
  generation runs through the shared `graphify` skill on a filtered code and
  documentation corpus.
- The selected Codegeist technology baseline is Java, GraalVM, Spring,
  Spring AI, Vaadin, JBang, and PF4J. Architecture docs should map OpenCode
  concepts explicitly onto this stack.
- Codegeist sessions are runtime-owned aggregates. CLI, server, Vaadin, and
  future TUI clients may create, continue, inspect, and render sessions, but they
  must not own session state transitions. `T001_06` answers the OpenCode-to-Java
  migration questions for session, turn, message part, lifecycle, streaming
  boundaries, and later storage projections.
- Codegeist events are typed runtime events for CLI output and later server,
  Vaadin, and TUI streams. They separate user-visible rendering from
  audit-relevant events; transport, event sourcing, and storage schema remain
  later decisions. `T001_07` answers the OpenCode-to-Java migration questions for
  `RuntimeEvent`, envelope, ordering, correlation, event families, audit
  relevance, and later projections.
- Spring AI is the default provider integration path, but it remains behind
  Codegeist-owned runtime/provider policies. Provider selection, model
  capabilities, tool exposure, events, and errors remain Codegeist decisions, not
  CLI or SDK details.
- The task phase workflow has moved upstream into the shared `.opencode` agent
  kit. Use `/specify-task`, `/plan-task`, `/solve-task`, and `/work-task` from
  `.opencode/commands/`; shared behavior lives in `.opencode/rules/task-phases.md`
  and `.opencode/rules/task-workflow.md`.
- `.oc_local/rules/codegeist-task-specification.md` is now a thin Codegeist
  overlay. It only records Codegeist/OpenCode parity guidance, T002 dependency
  order, Java-first architecture mapping, context-profile ownership guidance, and
  OpenCode source-evidence hints.
- `.oc_local/rules/architecture-doc.md` defines how to use and maintain Codegeist
  architecture and specification docs. Current-state architecture lives under
  `docs/developer/architecture/`, with
  `docs/developer/architecture/architecture.md` as the current-state map. Planned
  architecture, contract blueprints, and future-facing specs live under
  `docs/developer/specification/`. Architecture may be planned before
  implementation or documented immediately after implementation, but docs, code,
  and architecture-relevant tests must be synchronized in the same task.
- Shared task phases use one argument contract,
  `<task-ref> [context/instructions]`, discover hints from task docs, optionally
  use `docs/tasks/hints/` when it exists, record phase status in the task, and
  keep phase dependencies explicit: `specify` has no prior dependency, `plan`
  depends on `specify`, and `solve` depends on `plan`.
- `/work-task <task-ref> [context/instructions]` from the shared kit orchestrates
  specify, plan, solve, and finalize. It may switch from a source task to the
  concrete implementation task selected by planning, and stops before solving or
  finalizing when a phase leaves multiple choices, material open decisions, failed
  verification, or unresolved documentation impact.
- `docs/tasks/hints/opencode-solving-guidance.md` is the reusable hint for
  OpenCode-related `/solve-task` runs. It reminds solvers to use OpenCode as a
  feature reference rather than an implementation blueprint. Hint files are
  dynamic: when solving a task reveals reusable lessons, update them generically
  without task-specific logs or narrow implementation details. It now records the
  documentation-only T002 handoff pattern: future file maps, boundary rules,
  diagrams, and realistic Java examples in markdown without source/build changes.
- `docs/tasks/hints/opencode-source-solving-guidance.md` is the source-focused
  hint for using `/ask-project opencode ...` during solve passes that need
  evidence from
  `docs/third-party/opencode/source/`, especially provider, tool, MCP,
  permission, session, event, context, shell, patch/edit, extension, and storage
  tasks. It now tells CLI/prompt-flow tasks to separate adapter evidence from
  runtime-owned behavior evidence.
- Codegeist vocabulary or boundary slices should not create empty Java package
  directories just to reserve names. Use focused developer documentation or
  diagrams until a later task is ready for behavior-free Java contracts.
- `docs/tasks/hints/java-spring-architecture-planning-guidance.md` is the reusable
  planning hint for Java/Spring architecture tasks. It tells `/plan-task` passes to
  document architecture in markdown with UML/Mermaid diagrams, class diagrams,
  explanatory text, illustrative Java/Spring examples, and fixed test expectations
  before implementation. Later solve phases should add or update the planned tests
  with the code change and not leave known failing tests unresolved without a
  concrete blocker.

## Open Points

- The T002 MVP foundation documentation/specification sequence is complete through
  `T002_12`.
- `T003_implement-codegeist-opencode-core-application/` is the new implementation
  epic. It targets OpenCode-replaceable CLI-core behavior while keeping JBang,
  Vaadin, headless web server, and API/SDK surfaces deferred to the backlog.
- `T003_01_analyze_spring_ai_agent_utils_adoption.md` is planned as the first
  active child task. It will create
  `docs/developer/spring-ai-agent-utils-adoption.md` as one evidence-backed
  adoption report for `spring-ai-community/spring-ai-agent-utils`, classifying
  released and source-only utilities as direct-use, wrapper, conceptual-copy,
  defer, or reject candidates before Codegeist starts runtime implementation. The
  task remains analysis-only: no runtime behavior, Maven dependency, Java source,
  tests, provider callbacks, storage adapters, shell executors, skills, or
  subagents are added until a later task explicitly chooses them.
- The local `/analyse-project` workflow has created
  `docs/third-party/spring-ai-agent-utils/` with a source submodule, durable
  `README.md`, `ANALYSIS_REPORT.md`, and `REGENERATE.md`, plus an ignored
  Graphify cache. The current graph is structural/AST-focused with 1,030 nodes,
  1,604 edges, and 47 communities; use it for navigation, then inspect source
  files directly before making final T003 adoption decisions.
- Local third-party analysis workflows now use only two commands under the shared
  contract in `.oc_local/rules/third-party-analysis-workflow.md`:
  `/analyse-project` owns the complete deep analysis workspace with source,
  Graphify, Repomix, manifest, verification, and durable docs; `/ask-project`
  consumes that workspace for focused answers, diagrams, and Repomix-backed
  source deep dives via the `@repomix` subagent when needed.
