# T003_09 Define Tool Permission Workspace Source Generation Contract

Parent: `T003_define-codegeist-opencode-core-source-contracts`

Status: finalized

## Goal

Define a documentation-only source-generation contract for tool descriptors,
permission decisions, and workspace policy before any tool, permission,
workspace, or Java source is created.

This task replaces the earlier implementation-oriented `T003_09` slot. The next
safe step is to turn the finalized tool/permission/workspace blueprint into a
compact handoff for future descriptor registration, mode gates, permission policy,
workspace validation, bounded results, provider tool-call mediation, and test
contracts.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement Java yet.
- Convert `T003_09` into a documentation-only tool permission and workspace
  source-generation contract task before source generation.

## Specification Decision

`T003_09` should be a documentation-only tool/permission/workspace
source-generation contract task.

The later source-generating task should not start directly from the broad T002
tool/permission/workspace blueprint. It should first receive a reviewed contract
that names the first descriptor registry boundary, mode-policy handoff,
permission request and decision shapes, workspace target validation posture,
bounded result model, provider tool-call mediation rules, non-goals, and TDD
expectations while preserving runtime, session, event, CLI, context, provider,
storage, patch/edit, shell, TUI, and server boundaries.

## Context

`T002_07` finalized a documentation-only tool, permission, and workspace contract
blueprint. It documented future Codegeist-owned tool descriptors, request/result
and failure shapes, permission request/decision/scope metadata, workspace
tool-target validation, bounded output references, runtime event/session
projection, Spring AI tool-call mediation, OpenCode source evidence, future file
maps, illustrative Java snippets, and future test handoff notes. That task
intentionally did not create Java source, tests, package directories, provider
callbacks, shell execution, patch/edit behavior, PF4J, JBang, Graphify, Repomix,
or runtime behavior.

`T003_05` is the finalized runtime/session/event source-generation contract
slice, `T003_06` is the CLI prompt command source-generation contract slice,
`T003_07` is the context/workspace loading source-generation contract slice, and
`T003_08` is the provider configuration and Spring AI adapter source-generation
contract slice. This task must consume those boundaries where relevant: runtime
owns prompt turns, mode checks, tool request sequencing, events, and session
summaries; CLI owns input adaptation; context/workspace owns context manifests;
provider owns model invocation and provider diagnostics; tool/permission/workspace
owns descriptor exposure, policy checks, workspace tool-target validation, and
bounded tool results.

## Scope

- Define the first source-generation boundary for tool, permission, and workspace
  policy code.
- Translate the T002 tool/permission/workspace blueprint into a compact
  implementation handoff for future Java contracts.
- Define planned tool descriptor, registry, source, capability, mode
  compatibility, permission need, workspace need, input redaction, result summary,
  and audit posture shapes.
- Define planned tool request, result, failure, bounded summary, output reference,
  and lifecycle status shapes without implementing executors.
- Define planned permission request, decision, scope, expiry, audit metadata, and
  deny/ask/allow behavior without implementing approval UI or storage.
- Define planned workspace tool-target validation for read targets, write targets,
  command cwd, output references, symlinks, ignored/generated/secret-like posture,
  and external-directory candidates.
- Define provider tool-call mediation rules that keep Spring AI `ToolCallback`,
  provider SDK payloads, MCP tools, PF4J tools, and JBang scripts behind
  Codegeist-owned tool/permission/workspace policy.
- Define the required TDD and verification contract for the later implementation
  task that will create tool, permission, and workspace Java source.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, Spring AI tool callbacks, provider callbacks,
  CLI commands, TUI behavior, runtime services, context readers, provider calls,
  storage adapters, shell execution, patch/edit behavior, approval UI, persistent
  permission caches, PF4J, JBang, MCP execution, Graphify, Repomix, or native/build
  behavior in this task.
- Do not implement descriptor registration, tool exposure, mode gates, permission
  approval, workspace validation, output reference creation, event emission,
  session projection, provider tool-call handling, shell commands, file reads,
  file writes, patch application, network tools, plugin tools, or script tools.
- Do not let tool/permission/workspace contracts own provider configuration,
  provider invocation, runtime prompt execution, session lifecycle, event
  sequencing, CLI parsing, context manifest construction, storage persistence,
  patch/edit semantics, shell/process execution, TUI rendering, server routes,
  Vaadin, PF4J, or JBang behavior.
- Do not expose Spring AI, provider SDK, OpenCode, MCP, PF4J, JBang, shell, patch,
  or filesystem implementation types through runtime, session, event, CLI,
  context, provider, storage, TUI, server, Vaadin, PF4J, or JBang contracts.
- Do not copy OpenCode's TypeScript, Bun, Effect, tool registry, permission rule,
  MCP, external-directory, shell, patch/edit, file I/O, event bus, or storage
  implementation shape.

## Direct Inputs

- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_05_define_runtime_session_event_source_generation_contract.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_06_define_cli_prompt_command_source_generation_contract.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_07_define_context_workspace_loading_source_generation_contract.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_08_define_provider_configuration_spring_ai_adapter_source_generation_contract.md`
- `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_07_add_tool_permission_workspace_contracts.md`
- `docs/developer/specification/tool-permission-workspace-contracts.md`
- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/specification/provider-configuration-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `docs/developer/spring-ai-agent-utils-adoption.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first tool,
permission, and workspace source-generation handoff. The preferred target is:

- `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`

The guidance should include:

- The first tool/permission/workspace source-generation boundary and why it is
  smaller than broad tool execution, provider tool-calling, shell execution,
  patch/edit application, plugin/script execution, or OpenCode parity behavior.
- Planned package ownership for tool descriptors, permission policy, and workspace
  tool-target validation, clearly labeled as planned source.
- Planned descriptor registry and exposure rules for built-in, provider-signaled,
  MCP, PF4J, JBang, and later extension tools.
- Planned mode compatibility and permission layering rules where mode denial,
  deterministic workspace denial, descriptor capability limits, and result limits
  remain enforceable even after approval.
- Planned `ToolRequest`, `ToolResult`, `ToolFailure`, `OutputRef`, lifecycle
  status, redacted input summary, bounded result summary, and audit metadata
  shapes.
- Planned `PermissionRequest`, `PermissionDecision`, scope, expiry, approval
  source, audit metadata, and denial/ask/allow behavior without approval UI or
  persistent caches.
- Planned `WorkspaceToolTarget` validation for read, write, cwd, output-ref,
  symlink, ignored/generated/secret-like, and external-directory candidates.
- Runtime/session/event integration rules that map tool lifecycle outcomes to
  later runtime events and session summaries without making tool policy own event
  sequencing or session persistence.
- Provider/Spring AI integration rules that keep `ToolCallback` execution disabled
  or externally mediated until Codegeist-owned mode, permission, and workspace
  gates have run.
- Boundary rules that keep Spring Shell, CLI parsing, provider configuration,
  context loading, storage, patch/edit apply, shell/process execution, TUI, server,
  Vaadin, PF4J, JBang, Graphify, Repomix, and external analysis outside the first
  source slice.
- TDD handoff for the later implementation task, including the first narrow
  descriptor classification, mode denial, permission decision, workspace target
  validation, bounded result, provider tool-call disabled/mediated, and Spring AI
  type-isolation tests.
- Explicit deferrals to later T003 tasks for patch/edit, controlled shell,
  storage, end-to-end agent loop, CLI/TUI parity workflows, and packaging/native
  validation.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, Spring beans, tool callbacks, provider callbacks,
  runtime behavior, tool behavior, permission behavior, workspace behavior, or
  CLI/TUI behavior.
- The task converts `T003_09` from tool/permission/workspace implementation into a
  source-generation contract.
- The handoff documents future tool descriptors, descriptor registry, capability
  classification, mode checks, permission requests/decisions, workspace tool-target
  validation, bounded results, output references, lifecycle statuses, diagnostics,
  and typed tool failures without implementing those contracts.
- The handoff keeps tool/permission/workspace policy separate from runtime prompt
  execution, session lifecycle, event sequencing, CLI parsing, context loading,
  provider invocation, storage, patch/edit apply, shell execution, and UI behavior.
- The handoff keeps Spring AI, provider SDK, MCP, PF4J, JBang, shell, patch/edit,
  and filesystem implementation types inside planned adapter/executor boundaries
  and exposes only Codegeist-owned tool, permission, and workspace contracts to
  runtime/session/event code.
- The handoff uses the finalized Java generation, testing strategy,
  tool/permission/workspace, runtime/session/event, context/workspace, provider,
  and CLI documents as constraints for future source generation.
- Planned package names, Java shapes, provider mediation, workspace validation,
  source maps, and tests are clearly labeled as planned, not current
  implementation.

## Planning-Readiness Questions

- What is the smallest tool/permission/workspace contract a later Java task can
  implement without requiring provider orchestration, shell execution, patch/edit
  apply, storage, TUI, server, PF4J, JBang, MCP execution, or end-to-end agent-loop
  behavior?
- Which descriptor fields are required first to distinguish read-only,
  workspace-mutating, shell/process, network, provider-mediated, patch/edit,
  plugin, script, LSP, and subagent capabilities without overfitting later tools?
- How should the first implementation prove that mode denial, workspace denial,
  descriptor limits, and bounded result policies cannot be bypassed by permission
  approval?
- Which permission scopes are necessary for the first MVP: one request, one turn,
  one session, workspace path scoped approval, trusted built-in descriptor scope,
  or explicit external-directory approval?
- Which workspace target fields and skip/denial reasons should be shared with the
  context/workspace loading contract, and which are specialized for side-effecting
  tool requests?
- How should provider tool-call signals and Spring AI `ToolCallback` integration be
  represented while remaining disabled or externally mediated until this policy
  layer exists?
- Which result limits and `OutputRef` rules should prevent unbounded tool, shell,
  patch, file, provider, or plugin output from entering session summaries?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: `/specify-task t003_09`.
- Context or instructions considered: user explicitly requested no Java
  implementation yet and asked to convert this into a documentation-only tool
  permission and workspace source-generation contract task before source
  generation.
- Parent task considered:
  `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`.
- Adjacent child tasks considered: finalized `T003_05`, specified `T003_06`,
  specified `T003_07`, specified `T003_08`, and finalized `T003_02`, `T003_03`,
  and `T003_04`; the parent listed an implementation-oriented
  `T003_09_implement_tool_permission_workspace_core.md` slot, but that child task
  file did not exist before this pass.
- Dependency inputs considered: finalized `T002_07`,
  `tool-permission-workspace-contracts.md`, `context-workspace-manifest.md`,
  `provider-configuration-contracts.md`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`, `runtime-session-event-contracts.md`,
  `codegeist-opencode-parity.md`, and `spring-ai-agent-utils-adoption.md`.
- Project overlay considered: `.oc_local/rules/codegeist-task-specification.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_09` as a documentation-only tool, permission, and
  workspace source-generation contract slice. Tool/permission/workspace and Java
  implementation should wait until this contract is planned and solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, first descriptor registry cut, mode-policy handoff, permission scope
  shape, workspace target validation split, provider tool-call mediation posture,
  bounded result model, typed failure shape, and future TDD handoff.
- Next recommended phase: `/plan-task t003_09` to define the concrete
  documentation plan for
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`.

## Plan Result

- Phase command: `/plan-task t003_09` as part of `/work-task t003_09`.
- Context or instructions considered: no extra user context beyond the work-task
  request; the existing specification keeps this task documentation-only and
  forbids Java source generation.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_09`
  result in this task.
- Selected plan: update this same task as the concrete implementation task and
  create
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`.
- Target documentation files:
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`,
  this task file, the T003 parent task, and `docs/memory-bank/chat.md` if
  finalization changes durable project state.
- Implementation steps:
  1. Create the planned source-generation handoff with current baseline, first-wave
     boundary, package ownership, descriptor registry, request/result, permission,
     workspace target, runtime/session/event, provider mediation, boundary,
     future file map, Java sketch, TDD handoff, deferral, and later implementation
     checklist sections.
  2. Keep every package, class, record, port, and test name clearly labeled as
     planned source rather than current implementation.
  3. Update task phase status, parent progress notes, adjacent task impact, and
     memory only where the solved documentation changes future-session context.
  4. Run `git --no-pager diff --check` as the documentation-only verification.
- Verification plan: `git --no-pager diff --check` proves the markdown/task edits
  do not introduce whitespace errors. Java tests are intentionally not run because
  this task changes no Java, Maven, build, or runtime files.
- Open questions: None.
- Result: planned. The task has one safe documentation-only implementation path and
  no material blockers.
- Next recommended phase: `/solve-task t003_09`.

## Solve Result

- Phase command: `/solve-task t003_09` as part of `/work-task t003_09`.
- Context or instructions considered: the task specification, the T003 parent,
  finalized `T003_05` through `T003_08` contracts, finalized T002
  tool/permission/workspace blueprint, Java generation guidance, testing strategy,
  local Codegeist task overlay, and architecture documentation rule.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the plan result above.
- What changed: created
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`
  as the durable source-generation handoff for the first planned
  tool/permission/workspace Java contract slice.
- Key decisions:
  - The first source-generation boundary is contract-level only: descriptors,
    request/result records, permission requests/decisions, workspace tool targets,
    provider mediation posture, and TDD handoff.
  - Descriptor classification, mode denial, permission decisions, deterministic
    workspace denial, and bounded-result limits must remain enforceable as separate
    gates.
  - Spring AI internal tool execution stays disabled or externally mediated until
    Codegeist can construct and gate `ToolRequest` values.
  - Provider SDK, Spring AI, MCP, PF4J, JBang, shell, patch, process, terminal,
    filesystem, storage, and OpenCode implementation types stay outside
    runtime/session/event, permission, and workspace contracts.
- Verification: `git --no-pager diff --check` is the required command for this
  documentation-only task. It is also run after finalization.
- Acceptance criteria status: satisfied. The handoff documents future descriptors,
  registry, capability classification, mode checks, permission requests/decisions,
  workspace target validation, bounded results, output references, lifecycle
  statuses, provider mediation, typed failures, and tests without creating Java
  source, tests, packages, build files, Spring beans, callbacks, tool execution,
  permission behavior, workspace behavior, runtime behavior, or CLI/TUI behavior.
- Open decisions or blockers: None.
- Result: solved.
- Next recommended phase: `/finalize-task t003_09`.

## Finalization Result

- Phase command: `/finalize-task t003_09` as part of `/work-task t003_09`.
- Context or instructions considered: solved documentation changes from this task
  and adjacent tasks `T003_10`, `T003_11`, and `T003_12`.
- Upstream phase dependency: satisfied by the solve result above.
- Impacted tasks:
  - T003 parent task now records `T003_09` as finalized and points later tasks to
    the new handoff.
  - `T003_10`, `T003_11`, and `T003_12` should consume the finalized generic
    tool/permission/workspace handoff instead of redefining it.
- Documentation updates: refreshed `docs/memory-bank/chat.md` so future sessions
  know the `T003_09` source-generation handoff exists and remains
  documentation-only.
- Verification: `git --no-pager diff --check`.
- Remaining follow-ups: continue with `/work-task t003_10`, `/work-task t003_11`,
  or `/work-task t003_12` before Java source generation, unless the user changes
  scope.
- Result: finalized.

## Creation Note

Created from the T003 parent child slot after the user paused Java implementation
and requested a documentation-only tool, permission, and workspace contract before
source generation.
