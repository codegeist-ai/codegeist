# T003_10 Define Patch Edit Proposal Source Generation Contract

Parent: `T003_define-codegeist-opencode-core-source-contracts`

Status: finalized

## Goal

Define a documentation-only source-generation contract for patch/edit proposals
and apply results before any patch/edit or Java source is created.

This task replaces the earlier implementation-oriented `T003_10` slot. The next
safe step is to turn the finalized patch/edit proposal blueprint into a compact
handoff for future proposal identity, target summaries, freshness checks, exact
approval binding, Build-mode apply flow, Plan-mode apply denial, typed apply
failures, bounded summaries, output references, event/session projection, and test
contracts.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement Java yet.
- Convert `T003_10` into a documentation-only patch edit proposal
  source-generation contract task before source generation.

## Specification Decision

`T003_10` should be a documentation-only patch/edit proposal source-generation
contract task.

The later source-generating task should not start directly from the broad T002
patch/edit proposal blueprint. It should first receive a reviewed contract that
names the first proposal and apply-result boundary, proposal freshness posture,
workspace and permission handoff, mode gates, bounded result model, typed failure
shape, non-goals, and TDD expectations while preserving runtime, session, event,
CLI, context, provider, tool, permission, workspace, storage, shell, TUI, and
server boundaries.

## Context

`T002_08` finalized a documentation-only patch/edit proposal and apply-result
blueprint. It documented future edit proposal, target file, patch hunk or text
replacement, apply request, apply result, typed apply failure, permission and
workspace gate, proposal freshness, bounded summary, output reference, OpenCode
source evidence, future file map, and illustrative Java snippets. That task
intentionally did not create Java source, tests, package directories, patch parser
code, apply logic, direct-write behavior, rollback, formatter integration, or
runtime behavior.

`T003_05` is the finalized runtime/session/event source-generation contract slice,
`T003_06` is the CLI prompt command source-generation contract slice, `T003_07` is
the context/workspace loading source-generation contract slice, `T003_08` is the
provider configuration and Spring AI adapter source-generation contract slice, and
`T003_09` is the tool, permission, and workspace source-generation contract slice.
This task must consume those boundaries where relevant: runtime owns prompt turns,
mode checks, edit sequencing, events, and session summaries; tool/permission/
workspace owns descriptor exposure, policy checks, permission decisions, and
workspace target validation; patch/edit owns reviewable proposals and apply-result
contracts only.

## Scope

- Define the first source-generation boundary for patch/edit proposal and
  apply-result code.
- Translate the T002 patch/edit proposal blueprint into a compact implementation
  handoff for future Java contracts.
- Define planned proposal identity, target metadata, hunk or replacement summary,
  content freshness, approval binding, apply request, apply result, and apply
  failure shapes.
- Define planned Build-mode apply and Plan-mode apply denial behavior without
  implementing mode policy, approval UI, workspace policy, or file mutation.
- Define planned bounded result summaries and output references so full file
  contents, large diffs, provider payloads, stack traces, and secrets do not enter
  session state.
- Define runtime/session/event integration rules for proposal and apply lifecycle
  outcomes without making patch/edit own event sequencing or session persistence.
- Define how the later patch/edit implementation must specialize the finalized
  tool/permission/workspace contract instead of redefining generic tool policy.
- Define the required TDD and verification contract for the later implementation
  task that will create patch/edit Java source.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, CLI commands, TUI behavior, runtime services,
  context readers, provider calls, Spring AI tool callbacks, tool execution,
  permission approval, workspace policy code, storage adapters, shell execution,
  patch parsers, apply executors, file reads, file writes, direct-write behavior,
  formatter integration, rollback, Graphify, Repomix, or native/build behavior in
  this task.
- Do not implement proposal creation, diff generation, patch parsing, target
  validation, permission approval, freshness checks, apply behavior, conflict
  handling, partial apply behavior, event emission, session projection, storage, or
  UI rendering.
- Do not let patch/edit contracts own provider configuration, provider invocation,
  runtime prompt execution, session lifecycle, event sequencing, CLI parsing,
  context manifest construction, generic tool policy, generic permission policy,
  generic workspace validation, storage persistence, shell/process execution, TUI
  rendering, server routes, Vaadin, PF4J, or JBang behavior.
- Do not expose Spring AI, provider SDK, OpenCode, MCP, PF4J, JBang, shell,
  filesystem, or patch-library implementation types through runtime, session,
  event, CLI, context, provider, tool, permission, workspace, storage, TUI, server,
  Vaadin, PF4J, or JBang contracts.
- Do not copy OpenCode's TypeScript, Bun, Effect, write/apply-patch tools,
  permission rule, external-directory, output-truncation, event bus, file watcher,
  formatter, or storage implementation shape.

## Direct Inputs

- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_05_define_runtime_session_event_source_generation_contract.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_06_define_cli_prompt_command_source_generation_contract.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_07_define_context_workspace_loading_source_generation_contract.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_08_define_provider_configuration_spring_ai_adapter_source_generation_contract.md`
- `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/tasks/T003_09_define_tool_permission_workspace_source_generation_contract.md`
- `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_08_add_patch_edit_proposal_flow.md`
- `docs/developer/specification/patch-edit-proposal-contracts.md`
- `docs/developer/specification/tool-permission-workspace-contracts.md`
- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first patch/edit
proposal source-generation handoff. The preferred target is:

- `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`

The guidance should include:

- The first patch/edit source-generation boundary and why it is smaller than broad
  file editing, generic tool execution, shell execution, storage, UI review,
  provider tool-calling, or OpenCode parity behavior.
- Planned package ownership for patch/edit proposal and apply-result contracts,
  clearly labeled as planned source.
- Planned `EditProposal`, `EditProposalId`, `EditTarget`, `PatchHunk`,
  `TextReplacement`, `ProposalFreshness`, `ApplyEditRequest`, `ApplyEditResult`,
  `ApplyEditFailure`, `ApplyFailureKind`, `ProposalSummary`, and `OutputRef`
  shapes without implementing them.
- Planned target metadata for existing file updates, new files, deletes,
  rename-like later behavior, generated/ignored posture, secret-like posture,
  symlink escapes, outside-workspace candidates, and external-directory candidates.
- Planned exact approval binding between a reviewed proposal, permission decision,
  apply request, workspace revalidation, and freshness check.
- Planned mode behavior where proposal creation can be review-oriented but apply is
  denied in Plan mode and permission/workspace gated in Build mode.
- Planned result and failure taxonomy for applied, partially applied, mode denied,
  permission denied, workspace denied, missing target, stale input, conflict,
  invalid patch, output overflow, and unexpected I/O failure.
- Runtime/session/event integration rules that map proposal and apply lifecycle
  outcomes to later runtime events and session summaries without making patch/edit
  policy own event sequencing or persistence.
- Boundary rules that keep Spring Shell, CLI parsing, provider configuration,
  context loading, generic tool registry, generic permission policy, generic
  workspace validation, storage, shell/process execution, TUI, server, Vaadin,
  PF4J, JBang, Graphify, Repomix, and external analysis outside the first source
  slice.
- TDD handoff for the later implementation task, including the first narrow
  proposal construction, Plan-mode apply denial, Build-mode approval requirement,
  workspace-denied target, stale input, conflict/failure representation, bounded
  summary, output-reference, event/session projection, and implementation-type
  isolation tests.
- Explicit deferrals to later T003 tasks for controlled shell, storage,
  end-to-end agent loop, CLI/TUI parity workflows, packaging/native validation,
  direct-write exceptions, formatter integration, rollback, rich diff UI, PF4J,
  JBang, Vaadin, server, and API behavior.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, Spring beans, patch parsers, apply executors, file
  reads, file writes, patch/edit behavior, runtime behavior, tool behavior,
  permission behavior, workspace behavior, or CLI/TUI behavior.
- The task converts `T003_10` from patch/edit proposal implementation into a
  source-generation contract.
- The handoff documents future proposal identity, edit targets, patch hunk or text
  replacement summaries, proposal freshness, exact approval binding, apply request,
  apply result, typed failures, bounded summaries, output references, diagnostics,
  and event/session projection without implementing those contracts.
- The handoff specializes the finalized tool/permission/workspace source-generation
  contract and does not redefine generic tool descriptors, generic permission
  decisions, or generic workspace validation.
- The handoff keeps patch/edit separate from runtime prompt execution, session
  lifecycle, event sequencing, CLI parsing, context loading, provider invocation,
  generic tool execution, storage, shell execution, and UI behavior.
- The handoff keeps Spring AI, provider SDK, MCP, PF4J, JBang, shell, filesystem,
  formatter, and patch-library implementation types inside planned adapter or
  executor boundaries and exposes only Codegeist-owned patch/edit contracts to
  runtime/session/event code.
- The handoff uses the finalized Java generation, testing strategy, patch/edit,
  tool/permission/workspace, runtime/session/event, context/workspace, provider,
  and CLI documents as constraints for future source generation.
- Planned package names, Java shapes, source maps, apply flow, failure taxonomy,
  output-reference rules, and tests are clearly labeled as planned, not current
  implementation.

## Planning-Readiness Questions

- What is the smallest patch/edit proposal contract a later Java task can
  implement without requiring provider orchestration, shell execution, storage,
  TUI, server, PF4J, JBang, direct-write exceptions, formatter integration, or
  end-to-end agent-loop behavior?
- Which proposal fields are required first to bind a review summary, workspace
  target verdicts, original content identity, permission decision, and apply
  request to the exact same proposal?
- Which target metadata belongs in generic workspace validation from `T003_09`, and
  which metadata is specialized for patch/edit proposals and apply results?
- Which freshness signal should the source-generation contract require without
  forcing a specific implementation: content hash, file timestamp, workspace
  revision, or an implementation-defined equivalent?
- How should the first implementation prove that Plan mode cannot apply edits while
  Build mode still cannot bypass permission approval, workspace validation,
  descriptor limits, or bounded result policies?
- Which typed failures must be distinguishable in the first implementation handoff
  to make conflict, stale input, partial apply, and unexpected I/O behavior safe to
  display and retry?
- How should proposal and apply summaries use `OutputRef` values without storing
  full file contents, full patches, provider payloads, stack traces, or secrets in
  session parts?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: `/specify-task t003_10`.
- Context or instructions considered: user explicitly requested no Java
  implementation yet and asked to convert this into a documentation-only patch
  edit proposal source-generation contract task before source generation.
- Parent task considered:
  `docs/tasks/T003_define-codegeist-opencode-core-source-contracts/task.md`.
- Adjacent child tasks considered: finalized `T003_05`, specified `T003_06`,
  specified `T003_07`, specified `T003_08`, finalized `T003_09`, and finalized
  `T003_02`, `T003_03`, and `T003_04`; the parent listed an
  implementation-oriented `T003_10_implement_patch_edit_proposal_flow.md` slot,
  but that child task file did not exist before this pass.
- Dependency inputs considered: finalized `T002_08`,
  `patch-edit-proposal-contracts.md`, `tool-permission-workspace-contracts.md`,
  `context-workspace-manifest.md`, `runtime-session-event-contracts.md`,
  `java-generation-guidance.md`, `testing-strategy-and-agent-rules.md`, and
  `codegeist-opencode-parity.md`.
- Project overlay considered: `.oc_local/rules/codegeist-task-specification.md`.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_10` as a documentation-only patch/edit proposal
  source-generation contract slice. Patch/edit and Java implementation should wait
  until this contract is planned and solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, first proposal boundary, target metadata split from generic workspace
  policy, freshness posture, exact approval binding, failure taxonomy, bounded
  result model, event/session projection, and future TDD handoff.
- Next recommended phase: `/plan-task t003_10` to define the concrete
  documentation plan for
  `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`.

## Plan Result

- Phase command: `/plan-task t003_10` as part of `/work-task t003_10`.
- Context or instructions considered: no extra user context beyond the work-task
  request; the existing specification keeps this task documentation-only and
  forbids Java source generation.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_10`
  result in this task.
- Selected plan: update this same task as the concrete implementation task and
  create
  `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`.
- Target documentation files:
  `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`,
  this task file, the T003 parent task, `docs/developer/architecture/architecture.md`,
  and `docs/memory-bank/chat.md` if finalization changes durable project state.
- Implementation steps:
  1. Create the planned source-generation handoff with current baseline, first-wave
     boundary, package ownership, proposal contracts, target metadata, approval
     binding, apply contracts, runtime/session/event integration, boundary rules,
     future file map, Java sketches, TDD handoff, deferrals, and later
     implementation checklist sections.
  2. Keep every package, class, record, port, failure kind, and test name clearly
     labeled as planned source rather than current implementation.
  3. Update task phase status, parent progress notes, architecture references, and
     memory only where the solved documentation changes future-session context.
  4. Run `git --no-pager diff --check` as the documentation-only verification.
- Verification plan: `git --no-pager diff --check` proves the markdown/task edits
  do not introduce whitespace errors. Java tests are intentionally not run because
  this task changes no Java, Maven, build, or runtime files.
- Open questions: None.
- Result: planned. The task has one safe documentation-only implementation path and
  no material blockers.
- Next recommended phase: `/solve-task t003_10`.

## Solve Result

- Phase command: `/solve-task t003_10` as part of `/work-task t003_10`.
- Context or instructions considered: the task specification, the T003 parent,
  finalized `T003_05` through `T003_09` contracts, finalized T002 patch/edit
  blueprint, Java generation guidance, testing strategy, local Codegeist task
  overlay, and architecture documentation rule.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the plan result above.
- What changed: created
  `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`
  as the durable source-generation handoff for the first planned patch/edit
  proposal and apply-result Java contract slice.
- Key decisions:
  - The first source-generation boundary is contract-level only: reviewable
    proposals, target summaries, freshness metadata, exact approval binding, apply
    request/result records, typed failures, bounded summaries, output references,
    and TDD handoff.
  - Patch/edit specializes the finalized generic tool/permission/workspace handoff
    instead of redefining descriptors, permission decisions, workspace target
    validation, bounded results, or output-reference policy.
  - Plan mode may describe proposals but cannot apply edits; Build-mode apply still
    requires exact permission approval, workspace revalidation, and freshness
    checks.
  - Spring AI, provider SDK, MCP, PF4J, JBang, shell/process, filesystem,
    patch-library, formatter, storage, UI, and OpenCode implementation types stay
    outside Codegeist patch/edit contracts.
- Verification: `git --no-pager diff --check` is the required command for this
  documentation-only task. It is also run after finalization.
- Acceptance criteria status: satisfied. The handoff documents future proposal
  identity, edit targets, patch hunk and replacement summaries, proposal freshness,
  exact approval binding, apply request/result records, typed failures, bounded
  summaries, output references, event/session projection, package maps, Java
  sketches, and tests without creating Java source, tests, packages, build files,
  Spring beans, patch parsers, apply executors, file reads, file writes, tool
  behavior, permission behavior, workspace behavior, runtime behavior, storage,
  shell behavior, or CLI/TUI behavior.
- Open decisions or blockers: None.
- Result: solved.
- Next recommended phase: `/finalize-task t003_10`.

## Finalization Result

- Phase command: `/finalize-task t003_10` as part of `/work-task t003_10`.
- Context or instructions considered: solved documentation changes from this task,
  finalized `T003_09`, and adjacent tasks `T003_11` and `T003_12`.
- Upstream phase dependency: satisfied by the solve result above.
- Impacted tasks:
  - T003 parent task now records `T003_10` as finalized and names the new handoff.
  - `T003_11` and `T003_12` should consume patch/edit bounded result and
    output-reference decisions where shell output or storage artifact references
    interact with edit results.
- Documentation updates: refreshed `docs/developer/architecture/architecture.md` so
  the current-state doc links the new planned handoff, and refreshed
  `docs/memory-bank/chat.md` so future sessions know the `T003_10` handoff exists
  and remains documentation-only.
- Verification: `git --no-pager diff --check`.
- Remaining follow-ups: continue with `/work-task t003_11` or `/work-task t003_12`
  before Java source generation, unless the user changes scope.
- Result: finalized.

## Dependency Update

- `T003_09` is now finalized and created
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`.
- The `T003_10` plan should specialize that generic descriptor, permission,
  workspace target, bounded-result, output-reference, provider mediation, and TDD
  handoff instead of redefining generic tool policy.

## Creation Note

Created during `/specify-task t003_10` after the user paused Java implementation
and requested a documentation-only patch/edit proposal source-generation contract
before source generation.
