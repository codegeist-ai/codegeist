# T003_11 Define Controlled Shell Tool Source Generation Contract

Parent: `T003_implement-codegeist-opencode-core-application`

Status: finalized

## Goal

Define a documentation-only source-generation contract for the controlled shell
verification tool before any shell or Java source is created.

This task replaces the earlier implementation-oriented `T003_11` slot. The next
safe step is to turn the finalized controlled shell verification blueprint into a
compact handoff for future shell request/result contracts, mode and safety gates,
workspace-cwd validation, exact permission approval, environment and stdin posture,
bounded output, typed failures, executor handoff, event/session projection, and
test contracts.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement Java yet.
- Convert `T003_11` into a documentation-only controlled shell tool
  source-generation contract task before source generation.

## Specification Decision

`T003_11` should be a documentation-only controlled shell tool
source-generation contract task.

The later source-generating task should not start directly from the broad T002
shell verification blueprint. It should first receive a reviewed contract that
names the first shell request/result boundary, descriptor and mode posture,
destructive-command safety classification, permission and workspace-cwd handoff,
environment and stdin policy, timeout and cancellation behavior, bounded output
model, typed failure shape, executor isolation, non-goals, and TDD expectations
while preserving runtime, session, event, CLI, context, provider, tool,
permission, workspace, storage, patch/edit, TUI, and server boundaries.

## Context

`T002_09` finalized a documentation-only controlled shell verification blueprint.
It documented future shell verification requests, argv or explicitly marked shell
snippets, cwd validation, environment policy, timeout, stdin posture, output
limits, exit codes, typed failures, destructive-command posture, permission gates,
OpenCode source evidence, future file maps, illustrative Java snippets, and
future test handoff notes. That task intentionally did not create Java source,
tests, package directories, process executor ports, local process runners, PTY
support, terminal UI, remote execution, JBang execution, sandboxing, broad
allowlists, Graphify, Repomix, or runtime behavior.

`T003_05` is the finalized runtime/session/event source-generation contract slice,
`T003_06` is the CLI prompt command source-generation contract slice, `T003_07` is
the context/workspace loading source-generation contract slice, `T003_08` is the
provider configuration and Spring AI adapter source-generation contract slice,
`T003_09` is the tool, permission, and workspace source-generation contract slice,
and `T003_10` is the patch/edit proposal source-generation contract slice. This
task must consume those boundaries where relevant: runtime owns prompt turns, mode
checks, shell request sequencing, events, and session summaries;
tool/permission/workspace owns descriptor exposure, generic policy checks,
permission decisions, workspace target validation, and bounded result posture;
patch/edit owns reviewable file mutations and apply results; controlled shell owns
only the specialized shell verification request/result and executor handoff
contract.

## Scope

- Define the first source-generation boundary for controlled shell verification
  code.
- Translate the T002 shell verification blueprint into a compact implementation
  handoff for future Java contracts.
- Define planned shell descriptor, request, command shape, command purpose,
  destructive posture, cwd, environment, stdin, timeout, output limit, result,
  output summary, output reference, and failure shapes.
- Define planned gate order for descriptor resolution, mode denial, safety
  classification, workspace-cwd validation, exact permission approval,
  environment resolution, executor handoff, bounded result mapping,
  event/session projection, and cancellation.
- Define planned Plan-mode denial and Build-mode approval-required behavior without
  implementing mode policy, approval UI, workspace policy, or process execution.
- Define planned bounded stdout/stderr summaries and output references so full
  logs, command payloads, environment values, stack traces, credentials, and
  provider payloads do not enter session state.
- Define runtime/session/event integration rules for shell lifecycle outcomes
  without making shell verification own event sequencing or session persistence.
- Define how the later shell implementation must specialize the finalized
  tool/permission/workspace contract instead of redefining generic tool policy.
- Define the required TDD and verification contract for the later implementation
  task that will create controlled shell Java source.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, CLI commands, TUI behavior, runtime services,
  context readers, provider calls, Spring AI tool callbacks, generic tool
  execution, permission approval, workspace policy code, storage adapters,
  patch/edit behavior, process executor ports, local process runners, PTY support,
  terminal UI, remote execution, JBang execution, shell sandboxing, broad
  allowlists, Graphify, Repomix, or native/build behavior in this task.
- Do not implement command parsing, command classification, mode gates, permission
  approval, cwd validation, environment resolution, timeout handling,
  cancellation, process start, output capture, output-reference creation, event
  emission, session projection, storage, provider callbacks, shell commands, file
  reads, file writes, patch application, network tools, plugin tools, or script
  tools.
- Do not let shell verification contracts own provider configuration, provider
  invocation, runtime prompt execution, session lifecycle, event sequencing, CLI
  parsing, context manifest construction, generic tool policy, generic permission
  policy, generic workspace validation, patch/edit semantics, storage persistence,
  TUI rendering, server routes, Vaadin, PF4J, or JBang behavior.
- Do not expose Spring AI, provider SDK, OpenCode, MCP, PF4J, JBang, shell,
  process API, terminal, filesystem, patch/edit, or sandbox implementation types
  through runtime, session, event, CLI, context, provider, tool, permission,
  workspace, storage, TUI, server, Vaadin, PF4J, or JBang contracts.
- Do not copy OpenCode's TypeScript, Bun, Effect, shell tool, permission rule,
  external-directory, output truncation, event bus, process execution, file
  watcher, terminal, or storage implementation shape.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_05_define_runtime_session_event_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_06_define_cli_prompt_command_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_07_define_context_workspace_loading_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_08_define_provider_configuration_spring_ai_adapter_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_09_define_tool_permission_workspace_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_10_define_patch_edit_proposal_source_generation_contract.md`
- `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_09_add_controlled_shell_verification_tool.md`
- `docs/developer/specification/shell-verification-contracts.md`
- `docs/developer/specification/tool-permission-workspace-contracts.md`
- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/specification/patch-edit-proposal-contracts.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first controlled shell
tool source-generation handoff. The preferred target is:

- `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`

The guidance should include:

- The first controlled shell source-generation boundary and why it is smaller than
  broad terminal support, generic tool execution, patch/edit apply behavior,
  provider tool-calling, storage, TUI, process sandboxing, or OpenCode parity
  behavior.
- Planned package ownership for shell verification request/result contracts,
  policy adapters, and executor handoff, clearly labeled as planned source.
- Planned `ShellVerificationRequest`, `ShellRequestId`, `ShellCommandShape`,
  `ArgvCommand`, `ShellSnippet`, `ShellCommandPurpose`, `DestructivePosture`,
  `WorkspaceCommandCwd`, `ShellEnvPolicy`, `StdinPolicy`, `ShellOutputLimit`,
  `ShellVerificationResult`, `ShellResultStatus`, `ShellOutputSummary`,
  `ShellFailure`, `ShellFailureKind`, `ApprovedShellExecution`, `ShellExecutor`,
  and `OutputRef` shapes without implementing them.
- Planned command-shape rules where `argv` is preferred for structured
  verification commands and shell snippets require explicit marking, redacted
  summaries, and stricter safety classification.
- Planned gate order for descriptor resolution, Plan-mode denial, Build-mode
  approval candidacy, destructive or unknown posture denial, workspace-cwd and
  referenced-path validation, exact permission approval, environment resolution,
  executor handoff, timeout/cancellation, result mapping, output reference
  creation, event emission, and session projection.
- Planned controlled command posture for verification, build, test, read-only
  inspection, workspace mutation, destructive, deploy, network, and unknown
  command categories.
- Planned result and failure taxonomy for mode denied, safety denied, permission
  denied, workspace denied, invalid command shape, invalid cwd, timeout,
  cancellation, non-zero exit, output overflow, executor unavailable, and
  unexpected process failure.
- Runtime/session/event integration rules that map shell lifecycle outcomes to
  later runtime events and session summaries without making shell policy own event
  sequencing or persistence.
- Boundary rules that keep Spring Shell, CLI parsing, provider configuration,
  context loading, generic tool registry, generic permission policy, generic
  workspace validation, patch/edit apply, storage, TUI, server, Vaadin, PF4J,
  JBang, Graphify, Repomix, and external analysis outside the first source slice.
- TDD handoff for the later implementation task, including the first narrow
  Plan-mode denial, Build-mode approval requirement, destructive posture denial,
  workspace-cwd denial, env redaction, stdin denial, timeout shape, non-zero exit,
  bounded stdout/stderr summary, output-reference, event/session projection, fake
  executor, and implementation-type isolation tests.
- Explicit deferrals to later T003 tasks for storage, end-to-end agent loop,
  CLI/TUI parity workflows, packaging/native validation, real process execution,
  PTY or terminal UI, remote execution, sandboxing, broad allowlists, destructive
  workflows, network/deploy commands, PF4J, JBang, Vaadin, server, and API
  behavior.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, Spring beans, shell commands, process executors,
  process execution, runtime behavior, tool behavior, permission behavior,
  workspace behavior, patch/edit behavior, storage behavior, or CLI/TUI behavior.
- The task converts `T003_11` from controlled shell tool implementation into a
  source-generation contract.
- The handoff documents future shell request identity, command shapes, command
  purpose, destructive posture, cwd validation handoff, permission approval,
  environment and stdin policy, timeout and cancellation, result statuses, typed
  failures, bounded summaries, output references, diagnostics, and event/session
  projection without implementing those contracts.
- The handoff specializes the finalized tool/permission/workspace
  source-generation contract and does not redefine generic tool descriptors,
  generic permission decisions, or generic workspace validation.
- The handoff keeps shell verification separate from runtime prompt execution,
  session lifecycle, event sequencing, CLI parsing, context loading, provider
  invocation, generic tool execution, patch/edit apply behavior, storage, TUI,
  server, Vaadin, PF4J, and JBang behavior.
- The handoff keeps Spring AI, provider SDK, MCP, PF4J, JBang, shell, process,
  terminal, filesystem, sandbox, and patch/edit implementation types inside
  planned adapter or executor boundaries and exposes only Codegeist-owned shell
  verification contracts to runtime/session/event code.
- The handoff uses the finalized Java generation, testing strategy, shell
  verification, tool/permission/workspace, patch/edit, runtime/session/event,
  context/workspace, provider, and CLI documents as constraints for future source
  generation.
- Planned package names, Java shapes, source maps, gate order, failure taxonomy,
  output-reference rules, fake-executor posture, and tests are clearly labeled as
  planned, not current implementation.

## Planning-Readiness Questions

- What is the smallest controlled shell verification contract a later Java task can
  implement without requiring real process execution, storage, TUI, server, PF4J,
  JBang, terminal UI, sandboxing, network/deploy commands, or end-to-end
  agent-loop behavior?
- Which request fields are required first to bind a redacted command summary, cwd,
  purpose, timeout, environment posture, workspace verdict, permission decision,
  executor handoff, and result to the exact same shell request?
- Which command-shape constraints belong in shell verification, and which generic
  tool descriptor or workspace validations should be consumed from `T003_09`?
- How should the first implementation prove that Plan mode cannot run shell
  commands while Build mode still cannot bypass safety classification, permission
  approval, workspace-cwd validation, descriptor limits, or bounded output
  policies?
- Which typed failures must be distinguishable in the first implementation handoff
  to make timeout, cancellation, non-zero exit, invalid cwd, output overflow, and
  executor unavailability safe to display and retry?
- How should shell summaries use `OutputRef` values without storing full stdout,
  stderr, raw snippets, environment maps, provider payloads, stack traces, or
  secrets in session parts?

## Verification

```bash
git --no-pager diff --check
```

## Plan Result

- Phase command: `/plan-task t003_11` as part of `/work-task t003_11`.
- Context or instructions considered: no extra user context beyond the work-task
  request; the existing specification keeps this task documentation-only and
  forbids Java source generation.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_11`
  result in this task.
- Selected plan: update this same task as the concrete implementation task and
  create
  `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`.
- Target documentation files:
  `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`,
  this task file, the T003 parent task, `docs/developer/architecture/architecture.md`,
  and `docs/memory-bank/chat.md` if finalization changes durable project state.
- Implementation steps:
  1. Create the planned source-generation handoff with current baseline,
     first-wave boundary, package ownership, OpenCode evidence translation,
     request contracts, gate order, command posture, approval/executor handoff,
     result/failure taxonomy, event/session integration, boundary rules, future
     file map, Java sketches, TDD handoff, deferrals, and later implementation
     checklist sections.
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
- Next recommended phase: `/solve-task t003_11`.

## Solve Result

- Phase command: `/solve-task t003_11` as part of `/work-task t003_11`.
- Context or instructions considered: the task specification, the T003 parent,
  finalized `T003_05` through `T003_10` contracts, finalized T002 shell
  verification blueprint, Java generation guidance, testing strategy, local
  Codegeist task overlay, and architecture documentation rule.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- OpenCode source evidence considered:
  `docs/third-party/opencode/source/packages/opencode/src/tool/shell.ts`,
  `docs/third-party/opencode/source/packages/opencode/src/tool/tool.ts`,
  `docs/third-party/opencode/source/packages/opencode/src/tool/truncate.ts`, and
  `docs/third-party/opencode/source/packages/opencode/src/permission/index.ts`.
- Upstream phase dependency: satisfied by the plan result above.
- What changed: created
  `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`
  as the durable source-generation handoff for the first planned controlled shell
  verification Java contract slice.
- Key decisions:
  - The first source-generation boundary is contract-level only: shell request
    identity, command shapes, command purpose, destructive posture, cwd, env/stdin
    policy, timeout, output limits, approved executor handoff, typed results,
    bounded stdout/stderr summaries, output references, and TDD handoff.
  - Controlled shell specializes the finalized generic tool/permission/workspace
    handoff instead of redefining descriptors, permission decisions, workspace
    target validation, bounded results, or output-reference policy.
  - Plan mode denies every shell request; Build mode still requires safety
    classification, workspace-cwd validation, exact approval, env/stdin policy,
    timeout, cancellation, and bounded output controls.
  - Spring Shell, Spring AI, provider SDK, MCP, PF4J, JBang, process, terminal,
    filesystem, storage, patch/edit, UI, and OpenCode implementation types stay
    outside Codegeist shell verification contracts.
- Verification: `git --no-pager diff --check` is the required command for this
  documentation-only task. It is also run after finalization.
- Acceptance criteria status: satisfied. The handoff documents future request
  identity, command shapes, command purpose, destructive posture, cwd validation
  handoff, permission approval, environment and stdin policy, timeout and
  cancellation, result statuses, typed failures, bounded summaries, output
  references, diagnostics, event/session projection, package maps, Java sketches,
  fake-executor posture, and tests without creating Java source, tests, packages,
  build files, Spring beans, shell commands, process executors, process execution,
  tool behavior, permission behavior, workspace behavior, patch/edit behavior,
  storage, runtime behavior, or CLI/TUI behavior.
- Open decisions or blockers: None.
- Result: solved.
- Next recommended phase: `/finalize-task t003_11`.

## Finalization Result

- Phase command: `/finalize-task t003_11` as part of `/work-task t003_11`.
- Context or instructions considered: solved documentation changes from this task,
  finalized `T003_09` and `T003_10`, and adjacent specified task `T003_12`.
- Upstream phase dependency: satisfied by the solve result above.
- Impacted tasks:
  - T003 parent task now records `T003_11` as finalized and names the new handoff.
  - `T003_12` remains specified and should consume shell bounded-output and
    output-reference decisions where storage artifact references and session
    continuation interact with shell results.
- Documentation updates: refreshed `docs/developer/architecture/architecture.md` so
  the current-state doc links the new planned handoff, and refreshed
  `docs/memory-bank/chat.md` so future sessions know the `T003_11` handoff exists
  and remains documentation-only.
- Verification: `git --no-pager diff --check`.
- Remaining follow-ups: continue with `/work-task t003_12` before Java source
  generation, unless the user changes scope.
- Result: finalized.

## Specification Check Result

- Phase command: `/specify-task t003_11`.
- Context or instructions considered: user explicitly requested no Java
  implementation yet and asked to convert this into a documentation-only
  controlled shell tool source-generation contract task before source generation.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered: finalized `T003_05`, specified `T003_06`,
  specified `T003_07`, specified `T003_08`, finalized `T003_09`, specified
  `T003_10`, and finalized `T003_02`, `T003_03`, and `T003_04`; the parent listed
  an implementation-oriented `T003_11_implement_controlled_shell_tool.md` slot,
  but that child task file did not exist before this pass.
- Dependency inputs considered: finalized `T002_09`,
  `shell-verification-contracts.md`, `tool-permission-workspace-contracts.md`,
  `context-workspace-manifest.md`, `patch-edit-proposal-contracts.md`,
  `runtime-session-event-contracts.md`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`, and `codegeist-opencode-parity.md`.
- Project overlay considered: `.oc_local/rules/codegeist-task-specification.md`.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_11` as a documentation-only controlled shell tool
  source-generation contract slice. Shell and Java implementation should wait
  until this contract is planned and solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, first shell boundary, command-shape rules, gate order, destructive
  posture, permission/workspace-cwd handoff, env/stdin policy, timeout and
  cancellation shape, failure taxonomy, bounded output model, event/session
  projection, fake-executor posture, and future TDD handoff.
- Next recommended phase: `/plan-task t003_11` to define the concrete
  documentation plan for
  `docs/developer/specification/controlled-shell-tool-source-generation-contract.md`.

## Dependency Update

- `T003_09` is now finalized and created
  `docs/developer/specification/tool-permission-workspace-source-generation-contract.md`.
- The `T003_11` plan should specialize that generic descriptor, permission,
  workspace target, bounded-result, output-reference, provider mediation, and TDD
  handoff instead of redefining generic tool policy.
- `T003_10` is now finalized and created
  `docs/developer/specification/patch-edit-proposal-source-generation-contract.md`.
- The `T003_11` solution consumes patch/edit bounded-result and output-reference
  posture without redefining patch/edit proposal or apply behavior.

## Creation Note

Created during `/specify-task t003_11` after the user paused Java implementation
and requested a documentation-only controlled shell tool source-generation
contract before source generation.
