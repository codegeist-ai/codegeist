# T003 Implement Codegeist OpenCode Core Application

Status: open

## Goal

Implement the Codegeist core application so it can replace OpenCode for the target
CLI-based coding-agent workflows while staying Java-first and Codegeist-owned.

The implementation must provide the runtime, session, event, context, provider,
tool, permission, workspace, patch/edit, shell, storage, and CLI behavior needed
for practical OpenCode-style agent work.

## Context

`T001_define-codegeist-opencode-feature-architecture` defined the target
OpenCode-parity architecture. `T002_implement-codegeist-mvp-foundation` completed
the documentation blueprints and readiness gates without implementing runtime
behavior, except for the earlier build/layout baseline.

This parent task starts the implementation phase. It should derive concrete code
from the finalized blueprints, not reopen broad architecture decisions unless an
implementation task proves a contradiction.

## Deferred Surface Compatibility

The following surfaces are intentionally deferred out of the T003 core
implementation scope:

- JBang extension runtime.
- Vaadin client.
- Headless web server.
- API and SDK/OpenAPI surface.

T003 implementation tasks must still keep these future surfaces possible. Runtime
contracts, session/event projections, tool descriptors, permission decisions,
workspace policy, storage ports, provider boundaries, and native status reporting
must stay adapter-ready so later JBang, Vaadin, server, and API tasks can attach
without reworking the core.

## Child Tasks

- `T003_01_analyze_spring_ai_agent_utils_adoption.md`
- `T003_02_define_java_generation_guidance.md`
- `T003_03_define_testing_strategy_and_agent_test_rules.md`
- `T003_04_define_performance_startup_and_native_smoke_budgets.md`
- `T003_05_implement_runtime_session_event_core.md`
- `T003_06_implement_cli_prompt_commands.md`
- `T003_07_implement_context_workspace_loading.md`
- `T003_08_implement_provider_configuration_and_spring_ai_adapter.md`
- `T003_09_implement_tool_permission_workspace_core.md`
- `T003_10_implement_patch_edit_proposal_flow.md`
- `T003_11_implement_controlled_shell_tool.md`
- `T003_12_implement_storage_ports_and_session_continuation.md`
- `T003_13_implement_end_to_end_agent_loop.md`
- `T003_14_implement_opencode_parity_cli_workflows.md`
- `T003_15_validate_packaging_native_and_startup_posture.md`
- `T003_16_validate_opencode_core_replacement_readiness.md`

Only `T003_01` is created initially. Later child tasks should be created when the
preceding analysis and implementation-guidance tasks clarify the exact boundaries.

## Derivation Map

| Child task | Primary inputs | Purpose |
| --- | --- | --- |
| `T003_01` | Backlog idea, Spring AI Agent Utils repository, T002 tool/provider/storage/shell docs | Decide whether Spring AI Agent Utils can be used directly, wrapped, copied conceptually, deferred, or rejected. |
| `T003_02` | T002 blueprints, existing Java baseline | Define Codegeist Java source generation guidance for coding agents before broad implementation starts. |
| `T003_03` | T002 verification posture, Spring/JUnit/Mockito needs | Define fast, fine-grained, coding-agent-friendly test strategy. |
| `T003_04` | Native packaging posture, testing strategy | Define test runtime, startup, and native executable smoke budgets. |
| `T003_05` | Runtime/session/event contracts | Implement core runtime request, mode, session, turn, message part, and event contracts. |
| `T003_06` | CLI prompt contract | Implement Spring Shell prompt commands over runtime APIs. |
| `T003_07` | Context/workspace manifest | Implement deterministic context and workspace loading. |
| `T003_08` | Provider configuration contracts | Implement provider configuration and Spring AI adapter boundaries. |
| `T003_09` | Tool/permission/workspace contracts | Implement descriptors, registry, mode gates, permissions, and workspace validation. |
| `T003_10` | Patch/edit proposal contracts | Implement reviewable patch/edit proposal and apply flow. |
| `T003_11` | Shell verification contracts | Implement controlled shell verification tool. |
| `T003_12` | Storage port posture | Implement storage ports and session continuation behavior needed for CLI workflows. |
| `T003_13` | Runtime, provider, tools, storage | Implement a complete prompt loop with provider streaming, tools, permissions, and projections. |
| `T003_14` | OpenCode parity architecture | Implement and verify key OpenCode-style CLI workflows. |
| `T003_15` | Native packaging posture, performance budgets | Validate packaging, native status, startup, and executable smoke behavior. |
| `T003_16` | All T003 implementation tasks | Validate that Codegeist can replace OpenCode for the selected core CLI workflows. |

## Scope

- Build on the existing single Maven module under `app/codegeist/cli` until the
  contracts and tests prove that a module split is useful.
- Implement core CLI coding-agent behavior before deferred server, Vaadin, JBang,
  API, or SDK surfaces.
- Keep Spring AI behind Codegeist provider and tool policy.
- Keep tools behind Codegeist mode, permission, workspace, bounded-result, event,
  and session contracts.
- Keep storage behind ports and avoid making persistence own runtime behavior.
- Keep tests fast, fine-grained, and useful for coding agents.

## Non-Goals

- Do not copy OpenCode's Bun, TypeScript, Hono, Effect, or storage architecture.
- Do not implement JBang, Vaadin, headless server, API, or SDK/OpenAPI behavior in
  the T003 core implementation.
- Do not bypass Codegeist runtime-owned sessions, events, permissions, workspace
  validation, or storage ports to use an external utility directly.
- Do not mark Codegeist as OpenCode-replaceable until end-to-end parity checks and
  migration acceptance criteria pass.
- Do not split Maven modules before runtime, provider, tool, permission,
  workspace, and storage contracts are stable enough to justify it.

## Acceptance Criteria

- Codegeist can perform the selected OpenCode-style coding-agent workflows through
  its own CLI.
- Runtime, session, event, context, provider, tool, permission, workspace,
  patch/edit, shell, storage, and CLI behavior are implemented behind Codegeist
  contracts.
- Tests cover each subsystem with fast, deterministic unit or contract tests plus
  focused integration and smoke tests where needed.
- The final validation task demonstrates that Codegeist can replace OpenCode for
  the selected core CLI workflows without implementing the deferred JBang, Vaadin,
  server, or API surfaces.
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

## Creation Note

Created after `T002_12` finalized the MVP foundation documentation sequence. The
user selected `T003_01` as a Spring AI Agent Utils adoption analysis, and selected
JBang, Vaadin, web server, and API/SDK as deferred backlog surfaces that must still
shape T003's core contracts.
