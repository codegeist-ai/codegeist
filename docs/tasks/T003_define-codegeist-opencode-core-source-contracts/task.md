# T003 Define Codegeist OpenCode Core Source Contracts

Status: open

## Goal

Define the source-generation contracts and later implementation slices that let
Codegeist grow into an OpenCode-style core application for the target CLI and TUI
coding-agent workflows while staying Java-first and Codegeist-owned.

The eventual implementation must provide the runtime, session, event, context,
provider, tool, permission, workspace, patch/edit, shell, storage, CLI, and TUI
behavior needed for practical OpenCode-style agent work. The current completed
T003 work through `T003_12` is still documentation-only source-generation
handoff, not generated Java source.

## Naming Note

The directory name now matches the current task set: source-contract definition
before implementation. Later child tasks may introduce Java source only when they
explicitly move from a finalized contract handoff into a source-generating
implementation task.

## Context

`T001_define-codegeist-opencode-feature-architecture` defined the target
OpenCode-parity architecture. `T002_define-codegeist-mvp-foundation-blueprints` completed
the documentation blueprints and readiness gates without implementing runtime
behavior, except for the earlier build/layout baseline.

This parent task is the bridge from architecture blueprints to implementation. It
should derive concrete source-generation handoffs from the finalized blueprints,
then later derive code from those handoffs without reopening broad architecture
decisions unless an implementation task proves a contradiction.

## Deferred Surface Compatibility

The following surfaces are intentionally deferred out of the T003 core
implementation scope:

- JBang extension runtime.
- PF4J packaged plugin runtime.
- Vaadin client.
- Headless web server.
- API and SDK/OpenAPI surface.

T003 implementation tasks must still keep these future surfaces possible. Runtime
contracts, session/event projections, tool descriptors, permission decisions,
workspace policy, storage ports, provider boundaries, and native status reporting
must stay adapter-ready so later JBang, PF4J, Vaadin, server, and API tasks can
attach without reworking the core.

## Child Tasks

- `T003_01_analyze_spring_ai_agent_utils_adoption.md`
- `T003_02_define_java_generation_guidance.md`
- `T003_03_define_testing_strategy_and_agent_test_rules.md`
- `T003_04_define_build_release_and_binary_smoke_strategy.md`
- `T003_05_define_runtime_session_event_source_generation_contract.md`
- `T003_06_define_cli_prompt_command_source_generation_contract.md`
- `T003_07_define_context_workspace_loading_source_generation_contract.md`
- `T003_08_define_provider_configuration_spring_ai_adapter_source_generation_contract.md`
- `T003_09_define_tool_permission_workspace_source_generation_contract.md`
- `T003_10_define_patch_edit_proposal_source_generation_contract.md`
- `T003_11_define_controlled_shell_tool_source_generation_contract.md`
- `T003_12_define_storage_ports_session_continuation_source_generation_contract.md`
- `T003_13_implement_end_to_end_agent_loop.md`
- `T003_14_implement_opencode_parity_cli_workflows.md`
- `T003_15_validate_packaging_native_and_startup_posture.md`
- `T003_16_validate_opencode_core_replacement_readiness.md`

`T003_01`, `T003_02`, `T003_03`, `T003_04`, and `T003_05` are finalized.
`T003_05` created the documentation-only runtime/session/event source-generation
contract before Java implementation. `T003_06` is finalized as a
documentation-only CLI prompt command source-generation contract slice before CLI
or Java source is created. `T003_07` is finalized as a documentation-only
context/workspace loading source-generation contract slice before context,
workspace, or Java source is created. `T003_08` is finalized as a
documentation-only provider configuration and Spring AI adapter source-generation
contract slice before provider or Java source is created. `T003_09` is finalized as
a documentation-only tool, permission, and workspace source-generation contract
slice before tool, permission, workspace, or Java source is created. `T003_10` is
finalized as a documentation-only patch/edit proposal source-generation contract
slice before patch/edit or Java source is created. `T003_11` is finalized as a
documentation-only controlled shell tool
source-generation contract slice before shell or Java source is created. `T003_12`
is finalized as a documentation-only storage ports and session continuation
source-generation contract slice before storage, continuation, or Java source is
created. Later child tasks should be created or adjusted when the preceding
analysis and implementation-guidance tasks clarify the exact boundaries.

## Derivation Map

| Child task | Primary inputs | Purpose |
| --- | --- | --- |
| `T003_01` | Backlog idea, Spring AI Agent Utils repository, T002 tool/provider/storage/shell docs | Establish Agent Utils as a dependency baseline and document boundary guidance so Codegeist can use it internally without adopting its architecture. |
| `T003_02` | T002 blueprints, existing Java baseline | Define Codegeist Java source generation guidance for coding agents before broad implementation starts. |
| `T003_03` | T002 verification posture, Spring/JUnit/Mockito needs | Define fast, fine-grained, coding-agent-friendly test strategy. |
| `T003_04` | Native packaging posture, testing strategy, release requirements | Define build, GitHub release, platform support, startup, and binary smoke-test strategy. |
| `T003_05` | Runtime/session/event contracts, Java generation guidance, testing strategy | Define the first runtime/session/event source-generation contract before Java implementation. |
| `T003_06` | CLI prompt contract, runtime/session/event contract handoff | Define the documentation-only Spring Shell prompt command contract before CLI or Java source generation. |
| `T003_07` | Context/workspace manifest | Define the documentation-only context/workspace loading source-generation contract before Java implementation. |
| `T003_08` | Provider configuration contracts | Define the documentation-only provider configuration and Spring AI adapter source-generation contract before Java implementation. |
| `T003_09` | Tool/permission/workspace contracts | Define the documentation-only tool, permission, and workspace source-generation contract before Java implementation. |
| `T003_10` | Patch/edit proposal contracts | Define the documentation-only patch/edit proposal and apply-result source-generation contract before Java implementation. |
| `T003_11` | Shell verification contracts | Define the documentation-only controlled shell tool source-generation contract before Java implementation. |
| `T003_12` | Storage port posture, runtime/session/event contracts, shell and patch output references | Define the documentation-only storage ports and session continuation source-generation contract before Java implementation. |
| `T003_13` | Runtime, provider, tools, storage | Implement a complete prompt loop with provider streaming, tools, permissions, and projections. |
| `T003_14` | OpenCode parity architecture | Implement and verify key OpenCode-style CLI workflows. |
| `T003_15` | Native packaging posture, performance budgets | Validate packaging, native status, startup, and executable smoke behavior. |
| `T003_16` | All T003 implementation tasks | Validate that Codegeist can replace OpenCode for the selected core CLI workflows. |

## Scope

- Build on the existing single Maven module under `app/codegeist/cli` until the
  contracts and tests prove that a module split is useful.
- Implement core CLI and TUI coding-agent behavior before deferred JBang, PF4J,
  Vaadin, server, API, or SDK surfaces.
- Keep Spring AI behind Codegeist provider and tool policy.
- Keep tools behind Codegeist mode, permission, workspace, bounded-result, event,
  and session contracts.
- Keep storage behind ports and avoid making persistence own runtime behavior.
- Keep tests fast, fine-grained, and useful for coding agents.

## Non-Goals

- Do not copy OpenCode's Bun, TypeScript, Hono, Effect, or storage architecture.
- Do not implement JBang, PF4J, Vaadin, headless server, API, or SDK/OpenAPI
  behavior in the T003 core implementation.
- Do not expose external utilities directly through runtime, provider, session,
  event, permission, workspace, or storage boundaries in a way that bypasses
  Codegeist policy. Direct internal use is allowed when Codegeist-owned contracts
  stay independent.
- Do not mark Codegeist as OpenCode-replaceable until end-to-end parity checks and
  migration acceptance criteria pass.
- Do not split Maven modules before runtime, provider, tool, permission,
  workspace, and storage contracts are stable enough to justify it.

## Acceptance Criteria

- Codegeist can perform the selected OpenCode-style coding-agent workflows through
  its own CLI and TUI.
- Runtime, session, event, context, provider, tool, permission, workspace,
  patch/edit, shell, storage, CLI, and TUI behavior are implemented behind
  Codegeist contracts.
- Tests cover each subsystem with fast, deterministic unit or contract tests plus
  focused integration and smoke tests where needed.
- The final validation task demonstrates that Codegeist can replace OpenCode for
  the selected core CLI and TUI workflows without implementing the deferred JBang,
  PF4J, Vaadin, server, or API surfaces.
- Deferred surfaces have explicit backlog entries and remain compatible with the
  implemented core boundaries.

## Verification

Each child task must define its own targeted verification. At minimum, task file
and documentation-only changes should run:

```bash
git --no-pager diff --check
```

Implementation tasks should add the narrowest relevant Maven/Taskfile checks and
keep `task test` fast enough for coding-agent iteration.

## Progress Notes

- `T003_01` is finalized. Codegeist now keeps Spring AI Agent Utils `0.7.0` on
  the CLI classpath with Spring Boot 4 and Spring AI `2.0.0-M6`. The durable
  handoff is `docs/developer/spring-ai-agent-utils-adoption.md`, now an Agent
  Utils boundary guide rather than an adoption report. Later T003 tasks may use
  Agent Utils directly inside implementation code, but Codegeist runtime,
  provider, tool, permission, workspace, event, session, and storage contracts
  must not depend on Agent Utils architecture or broad raw provider callbacks.
- `T003_02` is finalized as a documentation-only Java/Spring generation guidance
  task. It created `docs/developer/specification/java-generation-guidance.md`,
  which defines package ownership, dependency direction, code-shape rules,
  framework and Agent Utils boundaries, CLI/TUI adapter expectations, future
  test-generation expectations, illustrative examples, and a later-task checklist
  without creating Java source, tests, packages, build changes, or runtime
  behavior.
- `T003_03` is finalized as a documentation-only testing and development strategy
  task. It created
  `docs/developer/specification/testing-strategy-and-agent-rules.md`, which makes
  TDD the default for behavior changes and bug fixes, separates fast
  unit/contract tests from startup-heavy and external categories, requires tests
  to stay individually executable or document blockers, and requires solve results
  to report targeted commands and enough timing information to spot slow tests or
  slow startup. It created no Java source, tests, Maven changes, Taskfile changes,
  runtime behavior, or build behavior.
- `T003_04` is finalized as a documentation-only build, release, platform, and
  binary-smoke strategy task. It created
  `docs/developer/specification/build-release-and-binary-smoke-strategy.md`, which
  defines GitHub Releases as the deployment target, Windows/Linux/macOS artifact
  and smoke expectations, JVM jar versus native executable verification,
  provisional startup/smoke budgets, checksum and release-note posture, and
  explicit `passed`/`skipped`/`failed` reporting for every platform check. It
  created no CI workflow, release script, Java source, tests, Maven changes,
  Taskfile changes, or release deployment behavior. Future packaging, release
  automation, and platform-smoke implementation tasks, especially the planned
  `T003_15`, should use this strategy as their release-readiness contract.
- `T003_05` is finalized as a documentation-only runtime/session/event
  source-generation contract slice. It created
  `docs/developer/specification/runtime-session-event-source-generation-contract.md`,
  which narrows the broad T002 runtime/session/event blueprint into planned
  `ai.codegeist.runtime`, `ai.codegeist.session`, and `ai.codegeist.event`
  package ownership, minimum first-wave records and ports, event-family cuts,
  sequencing and projection rules, boundary exclusions, TDD handoff tests, and
  deferrals. It created no Java source, tests, package directories, build files,
  runtime behavior, CLI/TUI behavior, provider calls, tools, permissions,
  workspace reads, storage, patch/edit, or shell behavior. The planned later Java
  source task should implement runtime/session/event core contracts with TDD
  before CLI prompt command implementation.
- `T003_06` is finalized as a documentation-only CLI prompt command
  source-generation contract slice. It created
  `docs/developer/specification/cli-prompt-command-source-generation-contract.md`,
  which defines planned `plan` and `build` Spring Shell command boundaries,
  prompt/session input, runtime delegation over the finalized runtime/session/event
  handoff, stable accepted/submitted output, OpenCode translation notes,
  illustrative adapter and test examples, TDD handoff, deferrals, and later
  implementation checklist. It created no Java source, tests, packages, Spring
  beans, CLI commands, runtime behavior, provider calls, tools, permissions,
  workspace reads, storage, patch/edit, shell behavior, TUI behavior, or
  native/build behavior.
- `T003_07` is finalized as a documentation-only context/workspace loading
  source-generation contract slice. It created
  `docs/developer/specification/context-workspace-loading-source-generation-contract.md`,
  which defines planned workspace identity, path classification, repo-owned context
  profile data, explicit source selection, deterministic source ordering, manifest
  records, runtime/session/event diagnostics integration, boundary exclusions, TDD
  handoff tests, deferrals, and a later implementation checklist. It created no
  Java source, tests, packages, context readers, workspace reads, runtime behavior,
  provider calls, tools, permissions, storage, patch/edit, shell behavior, TUI
  behavior, or native/build behavior.
- `T003_08` is finalized as a documentation-only provider configuration and Spring
  AI adapter source-generation contract slice. It created
  `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`,
  which defines planned provider ids, model refs, capabilities, option profiles,
  credential-source references, offline validation, runtime-facing adapter records,
  typed provider errors, Spring AI mapping isolation, OpenAI-compatible/OpenAI and
  Ollama first-wave posture, streaming fallback, disabled tool-callback posture,
  TDD handoff tests, deferrals, and a later implementation checklist. It created
  no Java source, tests, packages, provider starters, credentials, live model
  calls, Spring beans, runtime behavior, provider behavior, CLI/TUI behavior,
  context loading, workspace reads, tools, permissions, storage, patch/edit, shell
  behavior, or native/build behavior.
- `T003_09` is finalized as a documentation-only tool, permission, and workspace
  source-generation contract slice. It created
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`,
  which defines planned descriptor classification, registry exposure, mode gates,
  permission requests and decisions, workspace tool-target validation, bounded
  results, output references, runtime/session/event integration, Spring AI provider
  tool-call mediation, typed failures, TDD handoff tests, and deferrals. It created
  no Java source, tests, packages, Spring AI tool callbacks, provider callbacks,
  tool execution, permission approval, workspace policy code, shell execution,
  patch/edit behavior, storage, or runtime behavior. `T003_10`, `T003_11`, and
  `T003_12` should consume this generic policy handoff instead of redefining it.
- `T003_10` is finalized as a documentation-only patch/edit proposal
  source-generation contract slice. It created
  `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`,
  which defines planned proposal identity, target summaries, patch hunk and text
  replacement summaries, freshness metadata, exact approval binding, Build-mode
  apply flow, Plan-mode apply denial, typed apply failures, bounded summaries,
  output references, runtime/session/event projection, TDD handoff tests,
  deferrals, and a later implementation checklist. It created no Java source,
  tests, packages, patch parsers, apply executors, file reads, file writes,
  provider callbacks, shell execution, storage, or runtime behavior. `T003_11` and
  `T003_12` should consume patch/edit output-reference and bounded-result posture
  without redefining patch/edit apply behavior.
- `T003_11` is finalized as a documentation-only controlled shell tool
  source-generation contract slice. It created
  `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`,
  which defines planned shell request identity, command shapes, command purpose,
  destructive posture, Plan-mode denial, Build-mode exact approval,
  workspace-cwd validation, env/stdin policy, timeout and cancellation, result
  statuses, typed failures, bounded stdout/stderr summaries, output references,
  runtime/session/event projection, fake-executor posture, TDD handoff tests,
  deferrals, and a later implementation checklist. It created no Java source,
  tests, packages, shell commands, process executors, process execution, provider
  callbacks, patch/edit behavior, storage, or runtime behavior. `T003_12` should
  consume shell bounded-output and output-reference decisions where storage
  artifact references and session continuation interact with shell results.
- `T003_12` is finalized as a documentation-only storage ports and session
  continuation source-generation contract slice. It created
  `docs/developer/specification/storage-session-continuation-source-generation-contract.md`,
  which defines planned in-memory-first storage ports, session continuation
  identity, create/continue/list/update/delete behavior, message and event
  projection stores, artifact-reference metadata, redaction and retention posture,
  file-backed persistence deferral criteria, typed storage failures, storage health
  reporting, runtime/session/event integration rules, TDD handoff tests, and
  deferrals. It created no Java source, tests, packages, storage adapters,
  file-backed persistence, event sourcing, database schemas, migrations, CLI/TUI
  behavior, provider behavior, tool behavior, permission behavior, workspace
  behavior, patch/edit behavior, shell behavior, or runtime behavior.

## Creation Note

Created after `T002_12` finalized the MVP foundation documentation sequence. The
user selected `T003_01` to decide how Spring AI Agent Utils should inform
Codegeist, and selected JBang, PF4J, Vaadin, web server, and API/SDK as deferred
backlog surfaces that must still shape T003's core contracts.
