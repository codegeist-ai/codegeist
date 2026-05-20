# T002 Define Codegeist MVP Foundation Blueprints

## Goal

Define the first MVP foundation handoffs for Codegeist from the completed
OpenCode parity architecture.

This parent task groups the narrow foundation slices that must be specified
before runtime behavior, provider calls, tools, permissions, UI, plugins, or
storage are implemented. `T002_01` is the completed build/layout baseline
exception; the remaining child tasks are documentation and specification handoff
slices, not Java implementation tasks.

## Naming Note

The directory name now matches the current task meaning: blueprint definition
rather than runtime implementation.

## Context

`T001_define-codegeist-opencode-feature-architecture` completed the architecture
and backlog definition for evolving Codegeist toward OpenCode feature parity. The
first implementation work should validate the build baseline and establish
Codegeist-owned runtime vocabulary before deeper runtime/session/event behavior
is added.

## Child Tasks

- `T002_01_align-codegeist-build-baseline.md`
- `T002_02_introduce-runtime-vocabulary-contracts.md`
- `T002_03_introduce-runtime-session-event-contracts.md`
- `T002_04_wire_cli_prompt_mode_contract.md`
- `T002_05_add_context_workspace_manifest_slice.md`
- `T002_06_add_provider_configuration_adapter.md`
- `T002_07_add_tool_permission_workspace_contracts.md`
- `T002_08_add_patch_edit_proposal_flow.md`
- `T002_09_add_controlled_shell_verification_tool.md`
- `T002_10_decide_minimal_storage_ports.md`
- `T002_11_validate_native_packaging_posture.md`
- `T002_12_define_extension_and_client_readiness_gates.md`

## Derivation Map

The child tasks are grouped foundation slices derived from the completed `T001`
architecture tasks. They are intentionally not a one-to-one copy of the
specification task list. After the completed `T002_01` build/layout baseline,
the remaining slices are documentation-first specifications.

| Child task | Primary source tasks | Purpose |
| --- | --- | --- |
| `T002_01` | `T001_01`, `T001_20`, `T001_24`, `T001_25` | Align build/dependency and CLI layout baseline. |
| `T002_02` | `T001_02`, `T001_03` | Document runtime vocabulary before behavior. |
| `T002_03` | `T001_03`, `T001_05`, `T001_06`, `T001_07`, `T001_22`, `T001_23` | Add runtime/session/event contracts. |
| `T002_04` | `T001_04`, `T001_05`, `T001_22`, `T001_23` | Describe CLI command wiring to runtime modes. |
| `T002_05` | `T001_11`, `T001_14`, `T001_22`, `T001_23` | Describe context loading through workspace policy. |
| `T002_06` | `T001_08`, `T001_20`, `T001_22`, `T001_24` | Describe provider configuration and Spring AI adapter boundary. |
| `T002_07` | `T001_09`, `T001_10`, `T001_11`, `T001_22`, `T001_24` | Describe tool/permission/workspace contracts. |
| `T002_08` | `T001_13`, `T001_10`, `T001_11`, `T001_22`, `T001_24` | Describe patch/edit proposal flow. |
| `T002_09` | `T001_12`, `T001_10`, `T001_11`, `T001_22`, `T001_24` | Describe controlled shell verification tool. |
| `T002_10` | `T001_19`, `T001_06`, `T001_07`, `T001_22`, `T001_24` | Decide minimal storage ports. |
| `T002_11` | `T001_20`, `T001_01`, `T001_24`, `T001_25` | Validate JVM/native packaging posture. |
| `T002_12` | `T001_15`, `T001_16`, `T001_17`, `T001_18`, `T001_21`, `T001_22`, `T001_24` | Define later extension/client readiness gates. |

## Default Solve Hints

When solving any `T002_*` child task, read these hints before proposing a
solution:

- `docs/tasks/hints/opencode-solving-guidance.md`
- `docs/tasks/hints/opencode-source-solving-guidance.md`

Use the source-focused hint especially for provider, tool, MCP, permission,
session, event, context, shell, patch/edit, extension, storage, and prompt-flow
tasks that benefit from targeted OpenCode source evidence.

## Scope

- Keep the completed `app/codegeist/cli` build baseline with the selected Spring
  Boot `3.5.x`, Spring AI `1.1.x`, Java `25`, Spring Shell, Maven, and GraalVM
  posture as historical foundation state.
- Document initial Codegeist-owned runtime vocabulary and boundary direction
  after the build baseline is solved.
- Specify core runtime/session/event, CLI mode, context, provider, tool,
  permission, workspace, patch/edit, shell, storage, and packaging foundation
  slices in dependency order through developer documentation, diagrams, future
  file maps, and illustrative Java examples in markdown.
- Keep PF4J, JBang, server, and Vaadin as readiness-gated later surfaces until
  the CLI/runtime foundation is validated.
- Keep each child task independently verifiable and small enough for `/solve-task`.
  For `T002_02` and later, verification should stay documentation-focused unless
  a task is explicitly reopened as an implementation task.

## Non-Goals

- Do not implement provider calls, tools, permissions, storage, server APIs,
  Vaadin views, PF4J plugins, or JBang execution in this parent task.
- Do not add Java source files, empty package directories, contract tests, or
  runtime behavior for `T002_02` and later child tasks unless the user explicitly
  reopens a child task as implementation work.
- Do not collapse child tasks into one broad implementation pass.
- Do not split Maven modules before the runtime boundaries are stable.

## Acceptance Criteria

- The implementation child tasks cover the remaining `T001` architecture topics
  through grouped, independently verifiable slices.
- The build baseline task exists as `T002_01`, and `T002_02` depends on
  `T002_01`.
- Future `T002` child tasks can be added under `tasks/` without changing the
  completed `T001` architecture epic.
- No runtime implementation is performed by creating this parent task.

## Verification

- Confirm there is one canonical `T002` representation at
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`.
- Confirm child tasks live under
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/`.
- Run `git --no-pager diff --check`.

## Specification Check Result

- Rechecked the parent task with the default solve hints from this file and the
  OpenCode source-solving hint.
- The child-task grouping is intentionally implementation-oriented rather than a
  one-to-one copy of the T001 architecture task list.
- Dependency order is explicit enough to start with `T002_01`, continue through
  vocabulary and runtime contracts, and defer extension/client surfaces behind
  readiness gates.
- `T002_01` now includes the narrow CLI layout baseline: the current Spring Boot
  CLI Maven project lives under `app/codegeist/cli` before follow-up tasks add
  runtime vocabulary or later server/deployment surfaces.

## Design And Specification Audit

- `T002_01` is the completed build/layout baseline exception and may reference
  real Maven, Taskfile, source-layout, and verification changes.
- `T002_02` through `T002_12` match the current design/specification approach:
  they create or deepen developer documentation, diagrams, future file maps,
  illustrative Java snippets, and handoff checklists instead of implementing Java
  source, tests, package directories, runtime behavior, providers, tools,
  permissions, storage, Graphify, or Repomix workflows.
- `T002_05` is finalized as a single documentation-only context/workspace
  architecture task; it has no child tasks and should not spawn Java
  implementation work until the user explicitly requests that next phase.
- `T002_06` is finalized as a documentation-only provider configuration and
  Spring AI adapter blueprint. It created
  `docs/developer/specification/provider-configuration-contracts.md` and did not add Java
  source, tests, provider starters, credentials, live model calls, tool callbacks,
  Graphify, Repomix, or runtime behavior.
- `T002_07` is finalized as a documentation-only tool, permission, and workspace
  contract blueprint. It created
  `docs/developer/specification/tool-permission-workspace-contracts.md` and did not add Java
  source, tests, package directories, provider callbacks, tool execution,
  permission UI, workspace policy code, patch/edit behavior, shell execution,
  PF4J, JBang, Graphify, Repomix, or runtime behavior.
- `T002_08` is finalized as a documentation-only patch/edit proposal and
  apply-result blueprint. It created
  `docs/developer/specification/patch-edit-proposal-contracts.md` and did not add Java source,
  tests, package directories, patch parser code, apply logic, file writes,
  rollback, formatter integration, Graphify, Repomix, or runtime behavior.
- `T002_09` is finalized as a documentation-only controlled shell verification
  blueprint. It created `docs/developer/specification/shell-verification-contracts.md` and did
  not add Java source, tests, package directories, process execution, PTY support,
  terminal UI, remote execution, JBang execution, shell sandboxing, Graphify,
  Repomix, or runtime behavior.
- `T002_10` is finalized as a documentation-only minimal storage posture
  blueprint. It created `docs/developer/specification/storage-port-posture.md`, selected
  in-memory storage first behind replaceable ports, and did not add Java source,
  tests, package directories, storage ports, adapters, database schemas,
  migrations, encryption, durable audit logs, compaction, event replay, Graphify,
  Repomix, or runtime behavior.
- `T002_11` is finalized as a documentation-only native packaging posture
  blueprint. It created `docs/developer/specification/native-packaging-posture.md`, kept the MVP
  foundation JVM-first and native-aware, defined future JVM jar and native-image
  verification ladders, and required native status to be reported as `passed`,
  `skipped` with reason, or `failed` with a concrete blocker. It did not add Java
  source, tests, package directories, Maven changes, Taskfile commands, provider
  dependencies, PF4J, JBang, Vaadin, server, or runtime behavior.
- `T002_12` is finalized as a documentation-only extension and client readiness
  gate blueprint. It created
  `docs/developer/specification/extension-client-readiness-gates.md` and kept PF4J, JBang,
  headless server, Vaadin, SDK/OpenAPI, and future TUI surfaces deferred until
  their runtime, tool/permission/workspace, storage/auth, and native gates are
  satisfied. It did not add Java source, tests, dependencies, adapters, server
  routes, Vaadin views, PF4J plugins, JBang integrations, SDK generation, TUI
  behavior, or runtime behavior.

## Creation Note

Status: open.

Created as the implementation parent category for the first `T002` MVP
foundation tasks. The previously top-level build-baseline and runtime-vocabulary
tasks were migrated under this parent as `T002_01` and `T002_02`.

The remaining `T001` architecture topics were iterated through with the local
implementation-task workflow and grouped into `T002_03` through `T002_12` by
implementation dependency and verification boundary instead of one child task per
architecture specification.
