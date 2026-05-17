# T002_07 Describe Tool Permission Workspace Contracts

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_09`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

status: finalized

## Goal

Describe the first tool descriptor, tool request/result, permission request, and
workspace validation contracts before concrete side-effecting tools exist, without
adding Java source yet.

## Context

The architecture requires tools to flow through runtime-owned descriptors,
mode checks, permission policy, workspace validation, events, and session result
parts. Spring AI tool callbacks, PF4J plugins, and JBang scripts must adapt to
these contracts instead of bypassing them.

## Concrete Solution

1. Create or update `docs/developer/tool-permission-workspace-contracts.md` as
   the future tool, permission, and workspace contract blueprint.
2. Define future `ToolDescriptor`, `ToolRequest`, `ToolResult`, `ToolFailure`,
   capability category, and result summary shapes.
3. Define future `PermissionRequest`, `PermissionDecision`, decision scope, and
   audit metadata shapes.
4. Describe mode compatibility, permission need, and workspace validation flow
   without executing concrete file, shell, or network actions.
5. Document future tests for mode denial, permission-required decisions,
   read-only descriptors, and workspace path validation calls.
6. Include OpenCode source evidence, future file maps, diagrams, and illustrative
   Java snippets in markdown only.

## Scope

- `docs/developer/tool-permission-workspace-contracts.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Tool descriptors, permission decisions, workspace path validation, and event
  metadata are specified as future contracts before execution.
- Permission approval is specified as unable to override mode-denied capabilities.
- Workspace-scoped tool requests are specified to require centralized path
  validation.
- Tool results are specified to summarize output without unbounded session data.
- Future deny/allow/path-validation tests are described, but no Java source or
  tests are created by this task.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_03` and the finalized workspace boundary in `T002_05`.
- Should follow the finalized provider boundary in `T002_06`, which keeps Spring
  AI tool callbacks disabled or externally mediated until this contract exists.
- Feeds patch/edit, shell, provider tool-calling, PF4J, and JBang tasks.

## Non-Goals

- Do not create Java source files, empty package directories, or contract tests.
- Do not implement actual file edits, shell commands, network fetches, PF4J,
  JBang, LSP, or subagents.
- Do not implement approval UI or persistent approval caches.

## Open Questions

- None for this specification pass. The blueprint should define event-ready
  permission and tool metadata, plus future runtime event families when useful,
  but it must not implement event publication or decide storage/event-sourcing
  mechanics.

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later implementation task instead of creating `ai.codegeist.tool`,
  `ai.codegeist.permission`, or `ai.codegeist.workspace` source packages now.
- The future tool contract belongs above provider adapters and below runtime mode
  policy. Provider-emitted tool-call signals and Spring AI `ToolCallback` hooks are
  adapter-side inputs until Codegeist maps them into `ToolRequest` contracts and
  runs mode, permission, and workspace checks.
- Permission approval is not a universal override. Mode-denied capabilities,
  deterministic workspace denials, protected or ignored path posture, and
  descriptor-declared capability limits must remain enforceable even when a user
  approves a narrower request.
- Workspace path validation should reuse the deterministic boundary from
  `T002_05`: canonical path classification happens centrally before any tool reads
  or writes workspace files, while approval policy decides whether an otherwise
  eligible capability may proceed.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task should use targeted OpenCode source questions for tool registration,
  MCP tool exposure, permission mediation, and workspace boundaries before
  finalizing contract names.
- Scope remains contract-first; concrete file, shell, network, PF4J, JBang, LSP,
  and subagent execution stays out of this task.

## Dependency Impact Notes

- Finalized `T002_05_add_context_workspace_manifest_slice.md` defines workspace
  path classification as deterministic validation that runs before context reads
  and records denied or skipped candidates in a manifest. This task should reuse
  that boundary when describing tool-scoped path validation instead of redefining
  workspace identity from scratch.
- Permission approval belongs above deterministic workspace validation. A later
  permission policy may ask users to approve tool behavior or external-directory
  access, but it must not silently override mode-denied capabilities or default
  context loader denials.
- Finalized `T002_06_add_provider_configuration_adapter.md` defines provider
  configuration and Spring AI adapters as Codegeist-owned boundaries. This task
  should treat Spring AI `ToolCallback` and provider-emitted tool-call signals as
  adapter-side inputs that remain disabled or externally mediated until tool,
  permission, and workspace contracts are defined here.
- Tool availability must be the intersection of provider/model capability,
  Codegeist tool descriptors, active agent mode, permission policy, and workspace
  validation. A model's tool-calling capability must not directly execute tools or
  bypass Codegeist permission and workspace checks.

## Implementation-Readiness Questions

- Which minimal descriptor fields are required to distinguish read-only tools,
  workspace-mutating tools, shell/process tools, network tools, provider-mediated
  tools, PF4J tools, and JBang script tools without overfitting the first Java
  package shape?
- Which `ToolRequest` fields should carry session id, turn id, runtime mode,
  correlation id, provider/model origin, workspace path intent, and redacted input
  summaries for audit and event handoff?
- Which permission scopes are necessary for the first MVP: one request, one turn,
  one session, workspace-scoped path access, or trusted built-in descriptor scope?
- Which result limits should be specified so tool output can be summarized into
  session message parts without unbounded stdout, patch content, file content, or
  provider payloads?
- Which future event families are needed for requested, denied, approved, started,
  completed, failed, and summarized tool activity, and which fields are only audit
  metadata for now?
- Which handoff tests should later prove that provider tool-calling remains
  disabled or externally mediated until Codegeist-owned permission and workspace
  gates have run?

## Phase Status

- Phase: `/specify-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_07_add_tool_permission_workspace_contracts.md`.
- Context or instructions considered: user requested a specification pass by exact
  task path with no additional narrowing instructions.
- Upstream phase dependency: none; `/specify-task` is the entry phase and may be
  repeated when tool, permission, provider, or workspace boundaries need to be
  refreshed.
- Parent considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Adjacent and dependency tasks considered: dependency
  `T002_03_introduce-runtime-session-event-contracts.md`, finalized
  `T002_05_add_context_workspace_manifest_slice.md`, finalized
  `T002_06_add_provider_configuration_adapter.md`, adjacent
  `T002_08_add_patch_edit_proposal_flow.md`, and adjacent
  `T002_09_add_controlled_shell_verification_tool.md`.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Documentation considered: `docs/developer/architecture.md`,
  `docs/developer/README.md`, `docs/developer/context-workspace-manifest.md`,
  `docs/developer/provider-configuration-contracts.md`, and
  `docs/memory-bank/chat.md`.
- Discovered hints considered: the T002 parent `Default Solve Hints` point this
  task toward OpenCode source evidence for tool registration, MCP tool exposure,
  permission mediation, workspace boundaries, provider tool-call signals, shell
  safety, and patch/edit gates while preserving Codegeist's Java-first
  runtime-owned boundaries.
- Result: specified. The task remains a documentation-only tool, permission, and
  workspace contract blueprint. It now explicitly layers provider tool-call signals
  under Codegeist-owned descriptors, mode policy, permission decisions,
  deterministic workspace validation, bounded results, and event-ready audit
  metadata without creating Java source, tests, package directories, tool
  execution, provider callbacks, shell execution, patch/edit behavior, PF4J, JBang,
  Graphify, Repomix, or runtime behavior.
- Open decisions or blockers: none for specification. Later planning should choose
  the document structure, concrete diagrams, source-evidence questions, and future
  file map for `docs/developer/tool-permission-workspace-contracts.md`.
- Next recommended phase: run `/plan-task T002_07` as a documentation-only
  architecture plan for `docs/developer/tool-permission-workspace-contracts.md`.

## Architecture Plan

This planning pass keeps `T002_07` as one documentation-only architecture task. No
child task is needed because the source task already targets the narrow tool,
permission, and workspace contract blueprint, and no matching developer document
or implementation task exists yet.

### Selected Option

Create `docs/developer/tool-permission-workspace-contracts.md` as the concrete
tool, permission, and workspace contract blueprint, then cross-link it from
developer documentation and current-state architecture notes.

This option follows the active T002 posture: design the complete architecture
handoff first, then derive Java implementation tasks later only when explicitly
requested.

### Concrete Design Direction

Deepen the tool, permission, and workspace architecture around seven layers:

1. Tool descriptor and registry posture.
2. Runtime tool request shape and provider tool-call adaptation boundary.
3. Mode compatibility and capability classification.
4. Permission request, decision, scope, expiry, and audit metadata.
5. Workspace validation for tool paths, command working directories, and output
   references.
6. Tool result, failure, output-summary, and artifact-reference limits.
7. Runtime events and session message-part summaries for requested, denied,
   approved, started, completed, failed, and summarized tool activity.

Spring AI `ToolCallback` and provider-emitted tool calls should be documented as
adapter-side inputs only. They must become Codegeist-owned `ToolRequest` values
before any mode, permission, workspace, execution, event, or session-result
behavior can occur.

### Planned Documentation Files

- `docs/developer/tool-permission-workspace-contracts.md`
- `docs/developer/README.md`
- `docs/developer/architecture.md` only for current-state cross-references and
  explicit not-implemented notes
- this task file

No Java source, tests, fixtures, Maven files, build files, package directories,
Spring beans, provider callbacks, tool execution, permission UI, patch/edit,
shell execution, PF4J, JBang, Graphify, Repomix, or runtime behavior are planned
for this task.

### Planned Blueprint Content

1. State the purpose and non-implementation boundary of the tool, permission, and
   workspace blueprint.
2. Summarize OpenCode source evidence for tool registration, MCP tool exposure,
   permission prompts, external-directory approval, workspace path checks, shell
   tool behavior, and session tool events.
3. Summarize Spring AI counterpart posture for `ToolCallback` and internal tool
   execution controls, using `T002_06` as the provider-side boundary.
4. Define future Codegeist contract names for `ToolDescriptor`, `ToolId`,
   `ToolCapability`, `ToolRequest`, `ToolResult`, `ToolFailure`,
   `ToolResultSummary`, `PermissionRequest`, `PermissionDecision`,
   `PermissionScope`, `WorkspaceToolTarget`, and `OutputRef`.
5. Define a descriptor model that records source, capability category, mode
   compatibility, permission need, workspace need, input redaction policy, result
   summary policy, and audit/event classification.
6. Define the request flow: provider or runtime signal, descriptor lookup, mode
   check, permission decision, workspace validation, execution handoff, bounded
   result, runtime event emission, and session message-part summary.
7. Define the permission model, including allow/deny, decision scope, expiry,
   source client, deciding actor when known, redacted request summary, audit
   relevance, and the rule that approval cannot override mode denial or
   deterministic workspace denial.
8. Define workspace validation reuse from `T002_05`: file targets, write targets,
   command working directories, output references, outside-root paths, symlink
   escapes, generated/ignored paths, secret-like paths, and external-directory
   posture.
9. Define result and failure shapes that summarize output and refer to bounded
   artifacts instead of embedding unbounded stdout, file content, patch content, or
   provider-native payloads in sessions.
10. Add UML class diagrams for descriptors, requests/results/failures,
    permission decisions, workspace-target validation, event/session projection,
    and provider/Spring AI tool-call mediation.
11. Add a sequence diagram for tool execution from provider/runtime request through
    mode, permission, workspace, execution handoff, result, event, and session
    summary.
12. Add future test handoff notes for mode denial, approval-required decisions,
    approval-not-override behavior, read-only descriptors, workspace path denial,
    disabled/internal provider tool execution, output summary limits, and typed
    failures.

### Future File Map

The solve pass should include a future file map in markdown only. It can name
future paths such as:

- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolId.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolDescriptor.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolCapability.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolSource.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolRequest.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolResult.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolFailure.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/tool/ToolResultSummary.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/permission/PermissionRequest.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/permission/PermissionDecision.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/permission/PermissionScope.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/workspace/WorkspaceToolTarget.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/workspace/WorkspaceToolVerdict.java`
- `app/codegeist/cli/src/test/java/ai/codegeist/tool/...` for later contract tests
- `app/codegeist/cli/src/test/java/ai/codegeist/permission/...` for later contract
  tests

These paths are illustrative future implementation targets only. The solve pass
must not create them.

### Planned Diagrams

Use Mermaid `classDiagram` and `sequenceDiagram` unless PlantUML is clearly more
useful for a specific view.

1. Tool descriptor class diagram: `ToolDescriptor`, `ToolId`, `ToolCapability`,
   `ToolSource`, mode compatibility, permission need, workspace need, input
   redaction, result summary policy, and audit classification.
2. Tool request/result class diagram: `ToolRequest`, `ToolResult`, `ToolFailure`,
   `ToolResultSummary`, `OutputRef`, correlation ids, session ids, turn ids, and
   provider/model origin.
3. Permission class diagram: `PermissionRequest`, `PermissionDecision`,
   `PermissionScope`, expiry, deciding actor, source client, redacted summary, and
   audit metadata.
4. Workspace validation class diagram: `WorkspaceToolTarget`, validation purpose,
   file target, command cwd, output reference, and verdict mappings from `T002_05`.
5. Runtime flow sequence diagram: provider/runtime signal, registry lookup, mode
   check, permission decision, workspace validation, execution handoff, result,
   events, and session message-part summary.
6. Spring AI mediation diagram: provider `ToolCallback` or tool-call signal enters
   the adapter boundary and is converted into a Codegeist request or denied without
   internal execution.

### Scope

- Architecture and documentation only.
- Keep Codegeist runtime, provider, tool, permission, workspace, event, and session
  contracts free of Spring AI concrete classes, provider SDK payloads, PF4J plugin
  classes, and JBang runner details.
- Reuse `T002_05` workspace validation concepts instead of redefining workspace
  identity.
- Reuse `T002_06` provider boundary so provider tool-calling remains disabled or
  externally mediated until these contracts exist.
- Keep future Java names and snippets illustrative.

### Non-Goals

- Do not create Java source files, tests, fixtures, package directories, Maven
  changes, build files, Spring beans, provider callbacks, or Spring AI tool
  configuration.
- Do not implement tool registry behavior, descriptor loading, tool execution,
  permission prompts, approval cache, approval UI, workspace path classification,
  patch/edit, shell execution, network fetch, PF4J, JBang, LSP, subagents, storage,
  or runtime events.
- Do not run Graphify, Repomix, OpenCode analysis generation, or broad source
  indexing.
- Do not make plugin or script contributed tools trusted by default.

### Acceptance Criteria

- `docs/developer/tool-permission-workspace-contracts.md` exists and clearly
  describes Codegeist-owned tool, permission, and workspace execution boundaries.
- The document includes concrete UML diagrams for descriptors, requests/results,
  permission decisions, workspace validation, runtime event/session projection, and
  Spring AI tool-call mediation.
- OpenCode source evidence is cited or summarized for tool registration, MCP tool
  exposure, permission flow, workspace/external-directory posture, shell/tool
  events, and bounded result handling.
- Spring AI tool-calling is documented as disabled or externally mediated until
  Codegeist mode, permission, and workspace gates run.
- Permission approval is explicitly unable to override mode-denied capabilities,
  deterministic workspace denials, protected/ignored path posture, or descriptor
  capability limits.
- Tool results are bounded by summary and output-reference rules so session parts
  do not store unbounded stdout, file contents, patch contents, or provider-native
  payloads.
- Future tests are described as handoff guidance only; no test source is created.
- `docs/developer/README.md` links the new blueprint.
- `docs/developer/architecture.md` remains current-state focused and only gains
  not-implemented or related-document references if needed.
- No Java source, tests, packages, provider callbacks, runtime behavior, Graphify,
  or Repomix artifacts are added.

### Verification Plan

Run from the repository root:

```bash
git --no-pager diff --check
```

This proves the documentation diff has no whitespace errors. `task test` is not
required because this task must not change Java source, tests, Maven files, build
files, provider callbacks, Spring beans, or runtime behavior.

### Dependencies

- Depends on the current `T002_07` specification status and user decision that the
  slice remains documentation-only.
- Uses the T002 parent default hints for OpenCode translation and source-evidence
  posture.
- Uses `docs/developer/codegeist-opencode-parity.md` tool, permission, workspace,
  mode, PF4J, JBang, MVP-cut, and risk-register direction.
- Uses `docs/developer/runtime-session-event-contracts.md` for runtime event,
  session, turn, message-part, and correlation-id boundaries.
- Uses finalized `T002_05` context/workspace boundary for deterministic path
  validation and generated/ignored/secret-like path posture.
- Uses finalized `T002_06` provider boundary so Spring AI tool callbacks and
  provider-emitted tool-call signals remain adapter-side inputs until mediated by
  Codegeist contracts.
- Feeds `T002_08` patch/edit proposal flow, `T002_09` controlled shell verification
  tool, provider tool-calling implementation, PF4J tools, and JBang tools.

### Open Questions

None for this planning pass. The solve pass should record any newly discovered
OpenCode or Spring AI evidence directly in the blueprint and keep unanswered
implementation choices as future handoff notes.

## Plan Workflow Handoff

- Phase: `/plan-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_07_add_tool_permission_workspace_contracts.md`.
- Source task resolved from user input: `t002_07`, resolved to this existing task
  file under the T002 parent.
- Target task: this existing `T002_07` task; no child task was created because the
  task is already the narrow documentation-only tool, permission, and workspace
  blueprint slice.
- User context considered: the user asked what the task result should be, then ran
  `/plan-task t002_07` with no additional narrowing instructions. The answer in
  the conversation confirmed that the result should be
  `docs/developer/tool-permission-workspace-contracts.md` and no Java source.
- Parent task considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Adjacent and dependency tasks considered: dependency
  `T002_03_introduce-runtime-session-event-contracts.md`, finalized
  `T002_05_add_context_workspace_manifest_slice.md`, finalized
  `T002_06_add_provider_configuration_adapter.md`, adjacent
  `T002_08_add_patch_edit_proposal_flow.md`, and adjacent
  `T002_09_add_controlled_shell_verification_tool.md`.
- Duplicate check result: no existing
  `docs/developer/tool-permission-workspace-contracts.md` document exists, and no
  separate implementation task already covers this blueprint; the existing
  `T002_07` task is the correct target to sharpen.
- Selected option: create the tool, permission, and workspace architecture
  blueprint document in the solve pass and cross-link it from developer docs while
  leaving Java source and build files untouched.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: `docs/developer/codegeist-opencode-parity.md`,
  `docs/developer/runtime-session-event-contracts.md`,
  `docs/developer/context-workspace-manifest.md`,
  `docs/developer/provider-configuration-contracts.md`,
  `docs/developer/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- Upstream phase dependency: satisfied; this task had top-level
  `status: specified` and a current `/specify-task` phase status before this
  planning pass.
- Result: planned as one documentation-only architecture task.
- Open decisions or blockers: none for planning.
- Next recommended phase: run `/solve-task T002_07` as a documentation-only
  architecture design pass.

## Solution Note

Solved as a documentation-only architecture blueprint in
`docs/developer/tool-permission-workspace-contracts.md`. The new document defines
Codegeist-owned tool descriptor, tool request/result, permission request/decision,
workspace tool-target validation, bounded result, output-reference, runtime event,
session projection, and Spring AI tool-call mediation boundaries.

The solve pass also linked the new blueprint from `docs/developer/README.md` and
`docs/developer/architecture.md`. It did not create Java source, tests, package
directories, provider callbacks, tool execution, permission UI, workspace policy
code, patch/edit behavior, shell execution, PF4J, JBang, Graphify, Repomix, or
runtime behavior.

## Solve Status

- Phase: `/solve-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_07_add_tool_permission_workspace_contracts.md`.
- User instructions considered: solve `t002_07` using the existing plan. Prior
  user decisions remain in effect: this slice is documentation-only and should
  create `docs/developer/tool-permission-workspace-contracts.md` without Java
  source.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: planned` and a current `/plan-task` handoff before this solve pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`, finalized
  `T002_05_add_context_workspace_manifest_slice.md`, finalized
  `T002_06_add_provider_configuration_adapter.md`,
  `docs/developer/codegeist-opencode-parity.md`,
  `docs/developer/runtime-session-event-contracts.md`,
  `docs/developer/context-workspace-manifest.md`,
  `docs/developer/provider-configuration-contracts.md`,
  `docs/developer/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- OpenCode evidence considered: `tool/tool.ts` for tool definitions, execution
  context, permission ask hooks, and output truncation; `tool/registry.ts` for
  built-in, plugin, dynamic, and model-exposed tool registration; `permission/index.ts`
  and `permission/evaluate.ts` for request/reply, rule evaluation, and ask/allow/deny
  policy; `tool/external-directory.ts` for external-directory mediation;
  `tool/read.ts`, `tool/write.ts`, `tool/apply_patch.ts`, and `tool/shell.ts` for
  read, edit, patch, and shell safety posture; `tool/truncate.ts` for bounded
  output; `v2/session-event.ts` and `v2/session-message.ts` for tool lifecycle
  events and assistant tool message parts; and `mcp/index.ts` for MCP tool
  exposure.
- Documentation updates: created
  `docs/developer/tool-permission-workspace-contracts.md`, updated
  `docs/developer/README.md`, updated `docs/developer/architecture.md`, and
  refreshed `docs/memory-bank/chat.md`.
- Acceptance criteria status: satisfied. Tool descriptors, permission decisions,
  workspace path validation, event metadata, approval-not-override rules, bounded
  tool results, and future deny/allow/path-validation tests are specified as
  future contracts before execution, with no Java source or tests created.
- Verification: `git --no-pager diff --check`.
- Open decisions or blockers: none for this architecture pass. Future
  implementation tasks still need to choose exact Java APIs, implement policy
  services, and add deterministic contract tests before any side-effecting tool
  execution exists.
- Next recommended phase: run `/finalize-task T002_07`.

## Finalization Status

- Phase: `/finalize-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_07_add_tool_permission_workspace_contracts.md`.
- User instructions considered: finalize `T002_07` by checking task impact and
  refreshing affected documentation after the successful solve phase.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: solved` and a current successful `/solve-task` status before this
  finalization pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Impacted tasks: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`, dependent open
  `T002_08_add_patch_edit_proposal_flow.md`, dependent open
  `T002_09_add_controlled_shell_verification_tool.md`, and later dependent open
  `T002_12_define_extension_and_client_readiness_gates.md`.
- Documentation reviewed through update-documentation semantics:
  `docs/developer/tool-permission-workspace-contracts.md`,
  `docs/developer/README.md`, `docs/developer/architecture.md`, and
  `docs/memory-bank/chat.md`.
- Documentation updates: the parent task now records that `T002_07` is finalized;
  `T002_08`, `T002_09`, and `T002_12` record how the new blueprint constrains
  their later documentation slices; `docs/memory-bank/chat.md` was refreshed to
  make `T002_08` the next useful T002 slice. `docs/developer/README.md` and
  `docs/developer/architecture.md` were already updated during solve and remained
  accurate during finalization.
- Remaining follow-ups: continue with
  `T002_08_add_patch_edit_proposal_flow.md` as the next documentation-first T002
  slice. Future Java implementation tasks may be derived later from
  `docs/developer/tool-permission-workspace-contracts.md` only when explicitly
  requested.
- Verification: `git --no-pager diff --check`.
- Result: finalized. No implementation gaps, blockers, Java source, tests, package
  directories, provider callbacks, tool execution, permission UI, workspace policy
  code, patch/edit behavior, shell execution, PF4J, JBang, Graphify, Repomix, or
  runtime behavior were introduced.
- Next recommended phase: start `/specify-task T002_08` or `/work-task T002_08`
  when ready to continue the patch/edit proposal slice.

## Creation Note

Created as an open task and later specified through the shared task-phase
workflow.

Derived by grouping tool, permission, workspace, MVP, and risk-register tasks into
one contract-first documentation/specification slice.
