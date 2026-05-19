# T003_06 Define CLI Prompt Command Source Generation Contract

Parent: `T003_implement-codegeist-opencode-core-application`

Status: finalized

## Goal

Define a documentation-only source-generation contract for the first CLI prompt
command slice before any CLI command source or Java source is created.

This task replaces the earlier implementation-oriented `T003_06` slot. The next
safe step is to turn the finalized CLI prompt-mode blueprint into a compact handoff
for future Spring Shell `plan` and `build` commands, their runtime request
boundary, output contract, tests, and exclusions.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement CLI yet.
- Do not implement Java yet.
- Convert `T003_06` into a documentation-only CLI prompt command contract task
  before source generation.

## Specification Decision

`T003_06` should be a documentation-only CLI prompt command source-generation
contract task.

The later CLI source-generating task should not start directly from the broad
T002 prompt-mode handoff. It should first receive a reviewed contract that names
the supported commands, user input shape, runtime delegation boundary,
deterministic output, non-goals, and TDD expectations while preserving runtime,
session, event, provider, tool, permission, workspace, context, storage, patch,
shell, TUI, and server boundaries.

## Context

`T002_04` finalized a documentation-only design for wiring CLI prompt mode. It
documented future `plan` and `build` Spring Shell commands, OpenCode reference
evidence, adapter boundaries, future Java examples, and focused test examples.
That task intentionally did not create Java source, tests, package directories, or
CLI behavior.

`T003_05` finalized the preceding documentation-only runtime/session/event
source-generation contract slice at
`docs/developer/specification/runtime-session-event-source-generation-contract.md`.
`T003_06` must consume that final boundary rather than creating an alternate
runtime request, session, or event model.

## Scope

- Define the first source-generation boundary for CLI prompt commands.
- Translate the T002 CLI prompt-mode blueprint into a compact implementation
  handoff for future Spring Shell `plan` and `build` commands.
- Define how the future CLI adapter will collect prompt text, explicit mode, and
  optional continuation input, then delegate to runtime-owned contracts.
- Define deterministic accepted/submitted output for the first source slice without
  claiming assistant response generation.
- Define the required TDD and verification contract for the later implementation
  task that will create CLI Java source.
- Preserve CLI as a client adapter of runtime/session/event contracts, not the
  owner of runtime behavior.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, CLI commands, TUI behavior, runtime services,
  provider calls, tool execution, storage adapters, shell execution, patch/edit
  behavior, or native/build behavior in this task.
- Do not implement `plan`, `build`, `run`, `continue`, `fork`, command-template,
  async/server, provider/model, permission, tool, context-loading, event-stream,
  storage, patch/edit, shell, or TUI behavior.
- Do not invent a CLI-owned runtime request model; depend on the runtime/session
  and event contracts finalized through `T003_05`.
- Do not copy OpenCode's TypeScript, Bun, Effect, HTTP route, generated SDK,
  command-template, provider, permission, tool, or TUI implementation shape.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_05_define_runtime_session_event_source_generation_contract.md`
- `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_04_wire_cli_prompt_mode_contract.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first CLI prompt command
source-generation handoff. The preferred target is:

- `docs/developer/specification/cli-prompt-command-source-generation-contract.md`

The guidance should include:

- The first CLI prompt command boundary and why it is smaller than broad CLI/TUI or
  OpenCode parity behavior.
- Planned command contract for `plan` and `build`, including prompt input,
  optional continuation input if still valid, mode selection, and stable output.
- Runtime delegation rules that consume the finalized runtime/session/event
  contract instead of defining CLI-owned runtime types.
- Boundary rules that prevent Spring Shell, CLI parsing, provider SDK, Spring AI,
  Agent Utils, storage, tools, permissions, workspace, context, patch/edit, shell,
  TUI, server, Vaadin, PF4J, or JBang concerns from leaking into core contracts.
- TDD handoff for the later CLI implementation task, including the first narrow
  adapter tests and the behavior each should prove.
- Explicit deferrals to later T003 tasks for runtime implementation, provider
  streaming, context/workspace loading, tools, permissions, storage, patch/edit,
  shell, TUI, parity workflows, and packaging/native validation.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, runtime behavior, or CLI/TUI behavior.
- The task converts `T003_06` from CLI prompt implementation into a CLI prompt
  command source-generation contract.
- The handoff documents future `plan` and `build` Spring Shell command behavior
  without implementing those commands.
- The handoff makes CLI a client adapter over runtime/session/event contracts, not
  the owner of prompt execution, session lifecycle, event sequencing, provider
  calls, tool policy, permissions, workspace validation, context loading, storage,
  patch/edit, shell execution, or TUI behavior.
- The handoff uses the finalized Java generation and testing strategy documents as
  constraints for future source generation.
- Planned package names, command shapes, Java examples, and tests are clearly
  labeled as planned, not current implementation.

## Implementation Plan

### Selected Option

Create one documentation-only source-generation handoff at
`docs/developer/specification/cli-prompt-command-source-generation-contract.md`.
Do not create a separate Java implementation task yet, because this task's purpose
is to make the later Spring Shell source generation safe and bounded.

### Concrete Solution Direction

The handoff should translate the finalized T002 CLI prompt-mode blueprint and the
finalized T003 runtime/session/event source-generation contract into a compact
future implementation contract for `codegeist plan` and `codegeist build`.

The document should make CLI a thin Spring Shell adapter over runtime-owned
contracts. It should define prompt input, optional session continuation input,
mode mapping, runtime delegation, deterministic accepted/submitted output,
boundary exclusions, illustrative Java examples, and later TDD handoff tests.

### Planned Files And Targets

- Add
  `docs/developer/specification/cli-prompt-command-source-generation-contract.md`.
- Update this task with plan, solve, and finalization results.
- Update the T003 parent task and `docs/memory-bank/chat.md` so future sessions
  know `T003_06` is finalized and future CLI source generation depends on this
  contract.
- Update current-state architecture documentation only to add links to the new
  planned source-generation contract; do not claim CLI packages or commands exist.
- Do not change Java source, Java tests, package directories, Maven files,
  Taskfiles, Spring configuration, runtime behavior, provider calls, tools,
  permissions, workspace reads, storage, patch/edit, shell execution, TUI behavior,
  build behavior, or native behavior.

### Contract Document Structure

Use this structure unless a clearer equivalent preserves the same scope:

1. Purpose and status: planned source-generation guidance, not implementation.
2. Current baseline: one CLI Maven module, only `ai.codegeist.app` implemented,
   and no shell commands yet.
3. First command boundary: `plan` and `build` only, with prompt input, optional
   session id, explicit mode mapping, runtime delegation, and accepted output.
4. Planned command contract: input shape, runtime mapping, output wording, and
   deferred behavior for each command.
5. Runtime delegation rules: sequence diagram and rules that consume
   runtime/session/event contracts rather than inventing CLI-owned runtime types.
6. Planned package ownership: `ai.codegeist.cli` as adapter, runtime/session/event
   as contract owners, and later owners for provider, context, tools, permissions,
   workspace, storage, patch/edit, shell, TUI, and server.
7. Boundary rules: keep Spring Shell at the edge and prevent provider, tool,
   permission, workspace, context, storage, patch/edit, shell, TUI, server, Vaadin,
   PF4J, or JBang concerns from entering the CLI slice.
8. OpenCode translation table: cite CLI/TUI adapter evidence separately from
   runtime-owned prompt/session behavior.
9. Illustrative Java sketches: Spring Shell adapter and direct adapter tests,
   clearly marked as examples only.
10. TDD handoff: planned adapter tests, dependency-leak tests, optional Spring
    Shell registration smoke, and suggested Maven selectors.
11. Deferral table and later implementation checklist.

### Implementation Steps

1. Re-read this task, the T003 parent, finalized `T003_05`, finalized `T002_04`,
   `java-generation-guidance.md`, `testing-strategy-and-agent-rules.md`,
   `runtime-session-event-source-generation-contract.md`, and the project overlays.
2. Create the CLI prompt command source-generation handoff with the structure
   above.
3. Ensure every Java type, package, command, and test name is labeled as planned,
   not current implementation.
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

- Depends on finalized `T003_05` for runtime/session/event source-generation
  boundaries and finalized `T002_04` for CLI prompt-mode OpenCode evidence.
- Keeps source generation delayed by one documentation slice, but prevents the
  future CLI implementation from pulling in provider, context, tool, permission,
  workspace, storage, patch/edit, shell, TUI, or server behavior too early.
- Leaves exact Spring Shell input syntax to the later implementation task when it
  can be verified against Spring Shell 4, while fixing the runtime delegation
  contract now.

### Open Questions

None. The later source task may choose positional versus `--prompt` syntax, as
long as it preserves the runtime request boundary and tests the selected shape.

## Planning-Readiness Questions

- What is the smallest CLI prompt command contract that future Java source can
  implement without needing provider, tool, permission, workspace, context,
  storage, patch/edit, shell, TUI, or server behavior?
- How should `plan` and `build` differ at the command adapter boundary while
  leaving permission and tool behavior to later runtime tasks?
- Which pieces of the T002 CLI prompt-mode blueprint are still valid after the
  `T003_05` runtime/session/event contract is finalized?
- Which first tests should the later CLI implementation task write before source,
  and how can those tests stay focused on adapter behavior without starting a broad
  Spring context?
- How should the later source task prove that CLI/Spring Shell types do not leak
  into runtime/session/event contracts?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: `/specify-task t003_06`.
- Context or instructions considered: user explicitly requested no CLI or Java
  implementation yet and asked to convert this into a documentation-only CLI prompt
  command contract task before source generation.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered: finalized `T003_02`, `T003_03`, `T003_04`, and
  `T003_05`; the parent listed an implementation-oriented
  `T003_06_implement_cli_prompt_commands.md` slot, but that child task file did
  not exist before this pass.
- Dependency inputs considered: finalized `T002_04`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`, `runtime-session-event-contracts.md`, and
  `codegeist-opencode-parity.md`.
- Project overlay considered: `.oc_local/rules/codegeist-task-specification.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_06` as a documentation-only CLI prompt command
  source-generation contract slice. CLI and Java implementation should wait until
  this contract is planned and solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, command contract details, dependency on the completed `T003_05`
  runtime/session/event handoff, and future TDD handoff.
- Next recommended phase: `/plan-task t003_06` to define the concrete
  documentation plan for
  `docs/developer/specification/cli-prompt-command-source-generation-contract.md`.

## Planning Result

- Phase command: `/plan-task t003_06`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_06`
  result in this task.
- Duplicate check result: no existing
  `docs/developer/specification/cli-prompt-command-source-generation-contract.md`
  existed before this pass.
- Related context files read: T003 parent task, finalized `T003_05`, finalized
  `T002_04`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`,
  `runtime-session-event-source-generation-contract.md`,
  `runtime-session-event-contracts.md`, `codegeist-opencode-parity.md`, and the
  Codegeist task and architecture overlays.
- Result: planned one documentation-only contract slice for
  `docs/developer/specification/cli-prompt-command-source-generation-contract.md`.
- Open decisions or blockers: none.
- Next recommended phase: `/solve-task t003_06`.

## Solution Note

- Phase command: `/solve-task t003_06`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the `/plan-task t003_06` result and
  implementation plan in this task.
- Files changed:
  `docs/developer/specification/cli-prompt-command-source-generation-contract.md`,
  this task, the T003 parent task, current-state architecture links, and
  `docs/memory-bank/chat.md`.
- Result: solved the documentation-only source-generation handoff for the first
  CLI prompt command slice. The new specification defines planned `plan` and
  `build` command boundaries, runtime delegation rules, package ownership,
  boundary exclusions, OpenCode translation notes, illustrative Spring Shell and
  test examples, TDD handoff, deferrals, and later implementation checklist.
- Acceptance criteria status: satisfied. No Java source, tests, package
  directories, build files, Spring beans, CLI commands, runtime behavior, provider
  calls, tools, permissions, workspace reads, storage, patch/edit, shell behavior,
  TUI behavior, or native/build behavior were created.
- Verification: `git --no-pager diff --check` passed after finalization updates.
- Open decisions or blockers: none.
- Next recommended phase: completed by `/finalize-task t003_06`.

## Finalization Result

- Phase command: `/finalize-task t003_06`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Upstream phase dependency: satisfied by the successful solve result above.
- Impacted tasks: updated the T003 parent progress notes so future CLI prompt
  implementation consumes the finalized CLI command handoff and still waits for a
  dedicated source-generating task.
- Documentation updates: created
  `docs/developer/specification/cli-prompt-command-source-generation-contract.md`,
  refreshed current-state architecture links without claiming implementation, and
  refreshed `docs/memory-bank/chat.md` because future sessions need to know
  `T003_06` is finalized.
- Remaining follow-ups: a later dedicated Java implementation task should create
  runtime/session/event source first or together with the smallest missing runtime
  port, then implement `plan` and `build` with the TDD handoff from this
  specification. `T003_07` remains the next documentation-only source-generation
  contract task in the current sequence.
- Verification: `git --no-pager diff --check` passed.
- Result: finalized.

## Creation Note

Created from the T003 parent child slot after the user paused CLI and Java
implementation and requested a documentation-only contract before source
generation.
