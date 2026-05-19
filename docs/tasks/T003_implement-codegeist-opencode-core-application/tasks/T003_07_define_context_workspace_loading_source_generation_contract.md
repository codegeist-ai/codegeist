# T003_07 Define Context Workspace Loading Source Generation Contract

Parent: `T003_implement-codegeist-opencode-core-application`

Status: finalized

## Goal

Define a documentation-only source-generation contract for context and workspace
loading before any context, workspace, or Java source is created.

This task replaces the earlier implementation-oriented `T003_07` slot. The next
safe step is to turn the finalized context/workspace manifest blueprint into a
compact handoff for future workspace identity, path classification, context
profile loading, deterministic source selection, manifest assembly, and test
contracts.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement Java yet.
- Convert `T003_07` into a documentation-only context and workspace loading
  source-generation contract task before source generation.

## Specification Decision

`T003_07` should be a documentation-only context/workspace loading
source-generation contract task.

The later source-generating task should not start directly from the broad T002
context/workspace blueprint. It should first receive a reviewed contract that
names the first workspace and context boundaries, profile data responsibilities,
path-validation posture, deterministic ordering, manifest shape, non-goals, and
TDD expectations while preserving runtime, session, event, provider, tool,
permission, storage, patch/edit, shell, CLI, TUI, and server boundaries.

## Context

`T002_05` finalized a documentation-only design for deterministic context loading
through a workspace boundary and repo-owned context profile. It documented future
workspace validation, context source selection, skip reasons, source ordering,
manifest fields, external-analysis exclusions, and illustrative Java examples.
That task intentionally did not create Java source, tests, package directories,
context readers, or runtime behavior.

`T003_05` is the finalized runtime/session/event source-generation contract slice,
and `T003_06` is the CLI prompt command source-generation contract slice. This
task must consume those boundaries where relevant: runtime owns prompt turns and
events, CLI owns input adaptation, and context/workspace owns only explicit
profile-selected source loading and explainable manifests.

## Scope

- Define the first source-generation boundary for context and workspace loading.
- Translate the T002 context/workspace manifest blueprint into a compact
  implementation handoff for future Java contracts.
- Define planned workspace root identity, canonical path classification,
  generated/ignored/secret-like posture, and skip reasons.
- Define planned repo-owned context profile inputs without hard-coding this
  repository's `docs/` layout into Codegeist core.
- Define deterministic context source ordering and context manifest assembly for
  selected sources.
- Define how the future context loader will attach manifest diagnostics to runtime
  prompt handling without owning runtime, session, or event contracts.
- Define the required TDD and verification contract for the later implementation
  task that will create context/workspace Java source.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, CLI commands, TUI behavior, runtime services,
  provider calls, tool execution, permission approval, storage adapters, shell
  execution, patch/edit behavior, file watchers, embeddings, indexes, RAG,
  Graphify/Repomix runs, or native/build behavior in this task.
- Do not implement workspace reads, context profile discovery, context source
  expansion, manifest creation, active-task selection, file mutation, external
  directory access, or permission overrides.
- Do not hard-code repository-specific paths such as `docs/`, memory-bank files,
  local rules, third-party analysis artifacts, or task files as Codegeist core
  context sources.
- Do not invent a context-owned runtime request, session, event, provider, tool,
  permission, storage, patch/edit, shell, CLI, TUI, server, Vaadin, PF4J, or JBang
  model.
- Do not copy OpenCode's TypeScript, Bun, Effect, config, instruction-loading,
  file-tool, permission, watcher, storage, or package layout.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_05_define_runtime_session_event_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_06_define_cli_prompt_command_source_generation_contract.md`
- `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_05_add_context_workspace_manifest_slice.md`
- `docs/developer/specification/context-workspace-manifest.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first context and
workspace loading source-generation handoff. The preferred target is:

- `docs/developer/specification/context-workspace-loading-source-generation-contract.md`

The guidance should include:

- The first context/workspace source-generation boundary and why it is smaller
  than broad context, workspace, permission, tool, runtime, or OpenCode parity
  behavior.
- Planned package ownership for context and workspace contracts, clearly labeled
  as planned source.
- Planned workspace identity and path-classification contracts, including
  canonicalization, root containment, symlink escape, generated/ignored,
  secret-like, unsupported-source, and optional-missing outcomes.
- Planned context profile contract that treats repository-specific paths as profile
  data owned by repo commands or configuration, not Codegeist core constants.
- Planned context load request, candidate expansion, deterministic ordering,
  bounded read posture, included/skipped source records, manifest warnings, and
  redaction status.
- Runtime/session/event integration rules that attach manifests to future prompt
  diagnostics without making context loading own prompt execution or event
  sequencing.
- Boundary rules that keep Spring Shell, Spring AI, Agent Utils, provider SDK,
  storage, tools, permissions, patch/edit, shell, TUI, server, Vaadin, PF4J, JBang,
  Graphify, Repomix, embeddings, RAG, and external analysis outside the first
  source slice.
- TDD handoff for the later implementation task, including the first narrow
  workspace classification, profile interpretation, ordering, manifest, and
  boundary tests.
- Explicit deferrals to later T003 tasks for provider streaming, tools,
  permission approval, storage, patch/edit, shell, end-to-end agent loop, CLI/TUI
  parity workflows, and packaging/native validation.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, runtime behavior, context readers, workspace reads, or
  CLI/TUI behavior.
- The task converts `T003_07` from context/workspace implementation into a
  context and workspace loading source-generation contract.
- The handoff documents future workspace identity, path validation, context
  profile, deterministic source ordering, and manifest behavior without
  implementing those contracts.
- The handoff keeps repository-specific paths as context-profile data, not
  Codegeist core constants.
- The handoff separates workspace validation from later permission approval and
  separates context loading from runtime prompt execution, provider calls, tool
  policy, storage, patch/edit, shell execution, and UI behavior.
- The handoff uses the finalized Java generation and testing strategy documents as
  constraints for future source generation.
- Planned package names, Java shapes, source maps, and tests are clearly labeled as
  planned, not current implementation.

## Planning-Readiness Questions

- What is the smallest context/workspace contract a later Java task can implement
  without requiring provider, tool, permission, storage, patch/edit, shell, CLI,
  TUI, or server behavior?
- Which workspace classification outcomes are required for the first source slice,
  and which outcomes should remain later expansions?
- How should context profiles represent repo-owned instruction, state, work-item,
  knowledge, and source-snippet selections without hard-coding this repository's
  paths?
- What deterministic ordering and manifest fields are necessary for the first
  source slice to be testable and explainable?
- How should the future source task prove that context/workspace contracts do not
  leak Spring Shell, Spring AI, Agent Utils, provider, permission, tool, storage,
  patch/edit, shell, UI, Graphify, Repomix, or repo-specific path concerns?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: `/specify-task t003_07`.
- Context or instructions considered: user explicitly requested no Java
  implementation yet and asked to convert this into a documentation-only context
  and workspace loading source-generation contract task before source generation.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered: finalized `T003_05`, specified `T003_06`, and
  finalized `T003_02`, `T003_03`, and `T003_04`; the parent listed an
  implementation-oriented `T003_07_implement_context_workspace_loading.md` slot,
  but that child task file did not exist before this pass.
- Dependency inputs considered: finalized `T002_05`,
  `context-workspace-manifest.md`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`, `runtime-session-event-contracts.md`, and
  `codegeist-opencode-parity.md`.
- Project overlay considered: `.oc_local/rules/codegeist-task-specification.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_07` as a documentation-only context/workspace loading
  source-generation contract slice. Context, workspace, and Java implementation
  should wait until this contract is planned and solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, first-wave workspace/context contract cut, profile fields, manifest
  fields, dependency on completed runtime/CLI handoffs, and future TDD handoff.
- Next recommended phase: `/plan-task t003_07` to define the concrete
  documentation plan for
  `docs/developer/specification/context-workspace-loading-source-generation-contract.md`.

## Implementation Plan

### Selected Option

Create one documentation-only source-generation handoff at
`docs/developer/specification/context-workspace-loading-source-generation-contract.md`.
Do not create Java source, tests, package directories, context readers, workspace
reads, or runtime behavior in this task, because the task exists to make the later
source-generating context/workspace pass safe and bounded.

### Concrete Solution Direction

The handoff should translate `context-workspace-manifest.md`, the finalized
runtime/session/event source-generation contract, and the finalized CLI prompt
command source-generation contract into a compact future implementation contract
for workspace identity, path classification, context profile data, explicit source
selection, deterministic ordering, manifest assembly, runtime diagnostics
attachment, boundary exclusions, and TDD expectations.

Every Java package, type, record, enum, port, and test name must be clearly labeled
as planned source. Repository-specific paths must appear only as profile data or
examples, not as Codegeist core constants.

### Planned Files And Targets

- Add
  `docs/developer/specification/context-workspace-loading-source-generation-contract.md`.
- Update this task with plan, solve, and finalization results.
- Update the T003 parent task and `docs/memory-bank/chat.md` so future sessions
  know `T003_07` is finalized and future context/workspace source generation
  depends on this contract.
- Update current-state architecture documentation only to add links to the new
  planned source-generation contract; do not claim context/workspace packages or
  behavior exist.
- Do not change Java source, Java tests, package directories, Maven files,
  Taskfiles, Spring configuration, runtime behavior, provider calls, tools,
  permissions, context readers, workspace reads, storage, patch/edit, shell
  execution, TUI behavior, build behavior, or native behavior.

### Contract Document Structure

Use this structure unless a clearer equivalent preserves the same scope:

1. Purpose and status: planned source-generation guidance, not implementation.
2. Current baseline: one CLI Maven module, only `ai.codegeist.app` implemented,
   and no context/workspace source yet.
3. First-wave boundary: workspace identity, path classification, profile data,
   explicit context load request, candidate ordering, manifest records, and typed
   failures only.
4. Planned package ownership: `ai.codegeist.workspace` and `ai.codegeist.context`
   as first owners; runtime/session/event as integration boundaries; all provider,
   tool, permission, storage, patch/edit, shell, UI, and external-analysis surfaces
   deferred.
5. Planned workspace contracts: `WorkspaceRef`, `WorkspacePath`,
   `WorkspacePathVerdict`, `WorkspacePathClassification`, and
   `WorkspacePathPolicy`.
6. Planned context profile contract: profile id/source, instruction, state,
   work-item, knowledge, and source-snippet policy fields, with provenance.
7. Planned request and candidate shapes: explicit selections and no repository-wide
   scan.
8. Deterministic ordering rules and post-classification deduplication.
9. Planned manifest contract: included sources, skipped sources, warnings, limits,
   redaction status, and optional bounded content refs.
10. Runtime/session/event integration sequence and rules.
11. Boundary rules that exclude Spring Shell, Spring AI, Agent Utils, provider SDK,
    storage, tools, permissions, patch/edit, shell, TUI/server/Vaadin, PF4J, JBang,
    Graphify, Repomix, embeddings, RAG, and repository-specific path constants.
12. Illustrative Java sketches, marked as examples only.
13. TDD handoff with focused workspace classification, profile, ordering,
    manifest, and dependency-leak tests.
14. Deferral table and later implementation checklist.

### Implementation Steps

1. Re-read this task, the T003 parent, finalized `T003_05`, finalized `T003_06`,
   finalized `T002_05`, `context-workspace-manifest.md`,
   `java-generation-guidance.md`, and `testing-strategy-and-agent-rules.md`.
2. Create the context/workspace loading source-generation handoff with the
   structure above.
3. Ensure every planned source name is labeled as planned, not current
   implementation.
4. Update this task with solve and finalization notes.
5. Update parent task, memory, and current-state architecture links only where the
   completed documentation changes durable context.

### Verification Strategy

Required for this documentation-only plan and solve:

```bash
git --no-pager diff --check
```

Do not run `task test`, `task build`, `task native`, Maven, or Spring Shell
commands unless Java source, tests, build files, Taskfiles, or runtime
configuration change unexpectedly.

### Dependencies And Tradeoffs

- Depends on finalized `T002_05` for broad context/workspace manifest design,
  finalized `T003_05` for runtime/session/event source-generation boundaries, and
  finalized `T003_06` for CLI prompt adapter exclusions.
- Keeps source generation delayed by one documentation slice, but prevents the
  future context/workspace implementation from pulling in provider, tool,
  permission, storage, patch/edit, shell, UI, or external-analysis behavior too
  early.
- Leaves the exact profile persistence source to a later implementation task while
  fixing the rule that profile data owns repository-specific paths.

### Open Questions

None. The later source task may choose whether profile data comes from repo config,
generated workspace state, command output, or explicit user selection, as long as
the paths stay profile-owned and the core contracts remain repository-agnostic.

## Planning Result

- Phase command: `/plan-task t003_07`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_07`
  result in this task.
- Duplicate check result: no existing
  `docs/developer/specification/context-workspace-loading-source-generation-contract.md`
  existed before this pass.
- Related context files read: T003 parent task, finalized `T003_05`, finalized
  `T003_06`, finalized `T002_05`, `context-workspace-manifest.md`,
  `java-generation-guidance.md`, `testing-strategy-and-agent-rules.md`, and
  current-state architecture and memory docs.
- Result: planned one documentation-only contract slice for
  `docs/developer/specification/context-workspace-loading-source-generation-contract.md`.
- Open decisions or blockers: none.
- Next recommended phase: `/solve-task t003_07`.

## Solution Note

- Phase command: `/solve-task t003_07`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the `/plan-task t003_07` result and
  implementation plan in this task.
- Files changed:
  `docs/developer/specification/context-workspace-loading-source-generation-contract.md`,
  this task, the T003 parent task, current-state architecture links, and
  `docs/memory-bank/chat.md`.
- Result: solved the documentation-only source-generation handoff for the first
  context/workspace loading slice. The new specification defines planned workspace
  identity, path classification, context profile data, explicit source selection,
  deterministic ordering, manifest records, runtime/session/event diagnostics
  integration, boundary exclusions, illustrative Java examples, TDD handoff,
  deferrals, and a later implementation checklist.
- Acceptance criteria status: satisfied. No Java source, tests, package
  directories, build files, Spring beans, CLI commands, runtime behavior, context
  readers, workspace reads, provider calls, tools, permissions, storage,
  patch/edit, shell behavior, TUI behavior, or native/build behavior were created.
- Verification: `git --no-pager diff --check` passed after finalization updates.
- Open decisions or blockers: none.
- Next recommended phase: completed by `/finalize-task t003_07`.

## Finalization Result

- Phase command: `/finalize-task t003_07`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Upstream phase dependency: satisfied by the successful solve result above.
- Impacted tasks: updated the T003 parent progress notes so future
  context/workspace implementation consumes the finalized handoff and still waits
  for a dedicated source-generating task.
- Documentation updates: created
  `docs/developer/specification/context-workspace-loading-source-generation-contract.md`,
  refreshed current-state architecture links without claiming implementation, and
  refreshed `docs/memory-bank/chat.md` because future sessions need to know
  `T003_07` is finalized.
- Remaining follow-ups: a later dedicated Java implementation task should create
  context/workspace contracts with TDD after or alongside the minimum
  runtime/session/event contracts, then let provider, tool, permission, patch/edit,
  shell, storage, and end-to-end loop tasks consume the manifest boundary.
- Verification: `git --no-pager diff --check` passed.
- Result: finalized.

## Creation Note

Created from the T003 parent child slot after the user paused Java implementation
and requested a documentation-only context/workspace loading contract before
source generation.
