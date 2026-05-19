# T003_08 Define Provider Configuration Spring AI Adapter Source Generation Contract

Parent: `T003_implement-codegeist-opencode-core-application`

Status: finalized

## Goal

Define a documentation-only source-generation contract for provider configuration
and the Spring AI adapter boundary before any provider or Java source is created.

This task replaces the earlier implementation-oriented `T003_08` slot. The next
safe step is to turn the finalized provider configuration blueprint into a compact
handoff for future provider configuration, model references, credential-source
references, validation, typed provider errors, Spring AI adapter mapping, and test
contracts.

## User Direction

The user explicitly narrowed this specification pass:

- Do not implement Java yet.
- Convert `T003_08` into a documentation-only provider configuration and Spring AI
  adapter source-generation contract task before source generation.

## Specification Decision

`T003_08` should be a documentation-only provider configuration and Spring AI
adapter source-generation contract task.

The later source-generating task should not start directly from the broad T002
provider blueprint. It should first receive a reviewed contract that names the
first provider configuration boundary, first-wave OpenAI-compatible/OpenAI and
Ollama posture, credential-source and validation rules, runtime-facing adapter
port, Spring AI type-isolation rules, typed error mapping, streaming fallback
expectations, non-goals, and TDD expectations while preserving runtime, session,
event, CLI, context, workspace, tool, permission, storage, patch/edit, shell, TUI,
and server boundaries.

## Context

`T002_06` finalized a documentation-only provider configuration and Spring AI
adapter blueprint. It documented future Codegeist-owned provider descriptors,
model references, capability classification, credential-source references,
verification status, typed provider errors, Spring AI mapping, and first-wave
OpenAI-compatible/OpenAI plus Ollama support. That task intentionally did not
create Java source, tests, package directories, provider starters, credentials, or
live model calls.

`T003_05` is the finalized runtime/session/event source-generation contract slice,
`T003_06` is the CLI prompt command source-generation contract slice, and
`T003_07` is the context/workspace loading source-generation contract slice. This
task must consume those boundaries where relevant: runtime owns prompt turns,
session references, events, and provider request handoff; CLI owns input
adaptation; context/workspace owns context manifest construction; provider owns
only provider configuration, validation, Spring AI invocation, response mapping,
and provider diagnostics.

## Scope

- Define the first source-generation boundary for provider configuration and
  Spring AI adapter code.
- Translate the T002 provider configuration blueprint into a compact
  implementation handoff for future Java contracts.
- Define planned provider identifiers, model references, provider capability
  metadata, option profiles, credential-source references, verification status,
  and redaction posture.
- Define planned provider validation and dry-run behavior that can report missing
  or invalid configuration as typed provider errors without requiring live network
  calls by default.
- Define the planned runtime-facing provider adapter port, request/response shape,
  streaming chunk shape, fallback behavior, diagnostics, and typed error mapping.
- Define Spring AI adapter isolation rules for `ChatModel`, `StreamingChatModel`,
  `Prompt`, `ChatOptions`, `ChatResponse`, provider `spring.ai.*` properties, and
  tool-callback controls.
- Define first-wave OpenAI-compatible/OpenAI and Ollama support as planned source
  targets while keeping the extension point generic for later Spring AI-supported
  providers.
- Define the required TDD and verification contract for the later implementation
  task that will create provider configuration and Spring AI adapter Java source.

## Non-Goals

- Do not create Java source, Java tests, package directories, Maven files,
  Taskfile commands, Spring beans, Spring Boot configuration properties, provider
  starters, credentials, live model calls, CLI commands, TUI behavior, runtime
  services, context readers, workspace reads, tool execution, permission approval,
  storage adapters, shell execution, patch/edit behavior, or native/build behavior
  in this task.
- Do not implement OpenAI-compatible/OpenAI, Ollama, provider discovery, model
  listing, credential loading, validation, streaming, response mapping, or error
  mapping behavior.
- Do not require network-dependent tests or live provider credentials for the
  first source-generation handoff.
- Do not let provider configuration choose context profiles, read workspace files,
  ingest external analysis artifacts, run tools, approve permissions, mutate
  session state, sequence events, or own prompt execution.
- Do not expose Spring AI, provider SDK, `ToolCallback`, or provider-specific
  payload types through runtime, session, event, CLI, context, workspace, tool,
  permission, storage, patch/edit, shell, TUI, server, Vaadin, PF4J, or JBang
  contracts.
- Do not copy OpenCode's TypeScript, Bun, provider SDK matrix, configuration
  loader, generated SDK, server route, tool-calling, or storage implementation
  shape.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_05_define_runtime_session_event_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_06_define_cli_prompt_command_source_generation_contract.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_07_define_context_workspace_loading_source_generation_contract.md`
- `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_06_add_provider_configuration_adapter.md`
- `docs/developer/specification/provider-configuration-contracts.md`
- `docs/developer/specification/java-generation-guidance.md`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `docs/developer/spring-ai-agent-utils-adoption.md`
- `.oc_local/rules/codegeist-task-specification.md`

## Deliverables

Create or update a durable developer specification for the first provider
configuration and Spring AI adapter source-generation handoff. The preferred
target is:

- `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`

The guidance should include:

- The first provider source-generation boundary and why it is smaller than broad
  provider ecosystems, end-to-end agent loops, tool-calling, runtime orchestration,
  or OpenCode parity behavior.
- Planned package ownership for provider contracts and Spring AI adapter code,
  clearly labeled as planned source.
- Planned provider configuration records for provider id, model id, provider type,
  capabilities, option profiles, credential-source references, verification
  status, redaction status, and diagnostics.
- Planned first-wave OpenAI-compatible/OpenAI and Ollama adapter posture, including
  which behavior can be validated offline and which live-provider checks remain
  later explicit smoke tests.
- Planned adapter port, request, response, stream chunk, usage, diagnostics,
  fallback, and typed error shapes that runtime can consume without seeing Spring
  AI or provider SDK types.
- Spring AI mapping rules for adapter-internal `ChatModel`, `StreamingChatModel`,
  `Prompt`, `ChatOptions`, `ChatResponse`, provider properties, and disabled or
  externally mediated tool-callback execution.
- Runtime/session/event integration rules that map provider output and failures to
  later runtime events without making provider configuration own event sequencing,
  session persistence, or prompt execution.
- Boundary rules that keep Spring Shell, CLI parsing, Agent Utils architecture,
  context loading, workspace reads, tools, permissions, storage, patch/edit, shell,
  TUI, server, Vaadin, PF4J, JBang, Graphify, Repomix, and external analysis
  outside the first provider source slice.
- TDD handoff for the later implementation task, including the first narrow
  configuration binding, credential-source presence, capability classification,
  offline validation, Spring AI type-isolation, disabled tool-callback posture,
  streaming fallback, response mapping, and typed error tests.
- Explicit deferrals to later T003 tasks for runtime implementation, CLI command
  implementation, context/workspace loading, tool/permission mediation,
  patch/edit, shell, storage, end-to-end agent loop, CLI/TUI parity workflows, and
  packaging/native validation.

## Acceptance Criteria

- The task remains documentation-only and creates no Java source, tests, package
  directories, build files, Spring beans, provider starters, credentials, live
  model calls, runtime behavior, provider behavior, or CLI/TUI behavior.
- The task converts `T003_08` from provider configuration and Spring AI adapter
  implementation into a source-generation contract.
- The handoff documents future provider configuration, capability classification,
  credential-source references, validation, adapter request/response/streaming,
  diagnostics, and typed provider errors without implementing those contracts.
- The handoff keeps provider configuration separate from runtime prompt execution,
  session lifecycle, event sequencing, CLI parsing, context loading, workspace
  reads, tool execution, permission approval, storage, patch/edit, shell execution,
  and UI behavior.
- The handoff keeps Spring AI and provider SDK types inside planned adapter
  implementation boundaries and exposes only Codegeist-owned provider contracts to
  runtime/session/event code.
- The handoff uses the finalized Java generation, testing strategy, provider
  configuration, runtime/session/event, CLI, and context/workspace documents as
  constraints for future source generation.
- Planned package names, Java shapes, Spring AI mappings, source maps, and tests
  are clearly labeled as planned, not current implementation.

## Planning-Readiness Questions

- What is the smallest provider configuration and Spring AI adapter contract a
  later Java task can implement without requiring runtime orchestration, CLI
  commands, context loading, workspace reads, tools, permissions, storage,
  patch/edit, shell, TUI, or server behavior?
- How should the first implementation sequence OpenAI-compatible/OpenAI and Ollama
  while keeping both behind the same Codegeist-owned provider adapter port?
- Which provider configuration checks can run offline, and which live-provider
  checks should remain explicit later smoke tests with credentials or local Ollama
  availability?
- Which Spring AI types and provider properties are adapter-internal only, and how
  should the later source task prove they do not leak into runtime/session/event
  contracts?
- How should provider streaming fallback and typed provider errors map to planned
  runtime events without making provider code own event sequencing?
- How should tool-callback controls be disabled or externally mediated until the
  later tool/permission/workspace task defines Codegeist policy?

## Verification

```bash
git --no-pager diff --check
```

## Specification Check Result

- Phase command: `/specify-task t003_08`.
- Context or instructions considered: user explicitly requested no Java
  implementation yet and asked to convert this into a documentation-only provider
  configuration and Spring AI adapter source-generation contract task before source
  generation.
- Parent task considered:
  `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered: finalized `T003_05`, specified `T003_06`,
  specified `T003_07`, and finalized `T003_02`, `T003_03`, and `T003_04`; the
  parent listed an implementation-oriented
  `T003_08_implement_provider_configuration_and_spring_ai_adapter.md` slot, but
  that child task file did not exist before this pass.
- Dependency inputs considered: finalized `T002_06`,
  `provider-configuration-contracts.md`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`, `runtime-session-event-contracts.md`,
  `codegeist-opencode-parity.md`, and `spring-ai-agent-utils-adoption.md`.
- Project overlay considered: `.oc_local/rules/codegeist-task-specification.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: specified `T003_08` as a documentation-only provider configuration and
  Spring AI adapter source-generation contract slice. Provider and Java
  implementation should wait until this contract is planned and solved.
- Open decisions or blockers: the next phase must choose the exact guide
  structure, first-wave provider contract cut, offline validation boundary,
  OpenAI-compatible/OpenAI and Ollama sequencing, Spring AI adapter mapping, typed
  provider error shape, streaming fallback posture, disabled tool-callback posture,
  and future TDD handoff.
- Next recommended phase: `/plan-task t003_08` to define the concrete
  documentation plan for
  `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`.

## Implementation Plan

### Selected Option

Create one documentation-only source-generation handoff at
`docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`.
Do not create Java source, tests, package directories, provider starters,
credentials, live model calls, Spring beans, runtime behavior, or CLI/TUI behavior
in this task, because the task exists to make the later provider implementation
safe and bounded.

### Concrete Solution Direction

The handoff should translate `provider-configuration-contracts.md`, the finalized
runtime/session/event source-generation contract, the finalized CLI prompt command
source-generation contract, and the finalized context/workspace loading contract
into a compact future implementation contract for provider identifiers, model
references, capabilities, option profiles, credential-source references, offline
validation, typed provider errors, Spring AI adapter isolation, first-wave
OpenAI-compatible/OpenAI and Ollama posture, streaming fallback, disabled
tool-callback posture, and TDD expectations.

Every Java package, type, record, enum, port, adapter class, and test name must be
clearly labeled as planned source. Spring AI types must appear only inside planned
adapter-internal packages or illustrative snippets.

### Planned Files And Targets

- Add
  `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`.
- Update this task with plan, solve, and finalization results.
- Update the T003 parent task and `docs/memory-bank/chat.md` so future sessions
  know `T003_08` is finalized and future provider source generation depends on
  this contract.
- Update current-state architecture links only to reference the new planned
  source-generation contract; do not claim provider packages, Spring AI adapters,
  provider starters, or provider behavior exist.
- Do not change Java source, Java tests, package directories, Maven files,
  Taskfiles, Spring configuration, provider starters, credentials, live model
  calls, runtime behavior, CLI/TUI behavior, context loading, workspace reads,
  tools, permissions, storage, patch/edit, shell execution, build behavior, or
  native behavior.

### Contract Document Structure

Use this structure unless a clearer equivalent preserves the same scope:

1. Purpose and status: planned source-generation guidance, not implementation.
2. Current baseline: one CLI Maven module, only `ai.codegeist.app` implemented,
   Spring AI BOM present, and no provider source yet.
3. First-wave boundary: provider config records, capability metadata,
   credential-source references, offline validation, adapter port, request,
   response, stream chunks, diagnostics, fallback, and typed errors only.
4. Planned package ownership: `ai.codegeist.provider` and
   `ai.codegeist.provider.springai` as first owners; runtime/session/event as
   consumers; all CLI, context, tool, permission, storage, patch/edit, shell, UI,
   and extension surfaces deferred.
5. Planned provider configuration contracts for ids, model refs, provider type,
   capabilities, option profiles, credential sources, and verification status.
6. Planned adapter port and runtime-facing request/response/chunk/error shapes.
7. Planned offline validation and typed error model.
8. Spring AI mapping rules for `ChatModel`, streaming model behavior, `Prompt`,
   `ChatOptions`, `ChatResponse`, provider `spring.ai.*` properties, and disabled
   or externally mediated `ToolCallback` behavior.
9. First-wave OpenAI-compatible/OpenAI and Ollama provider posture.
10. Runtime/session/event integration rules.
11. Boundary rules excluding Spring Shell, CLI parsing, Agent Utils architecture,
    context loading, workspace reads, tools, permissions, storage, patch/edit,
    shell, TUI/server/Vaadin, PF4J, JBang, Graphify, Repomix, and live provider
    smoke behavior.
12. Illustrative Java sketches, marked as examples only.
13. TDD handoff with focused config binding, validation, adapter contract,
    streaming fallback, disabled tool-callback, and dependency-leak tests.
14. Deferral table and later implementation checklist.

### Implementation Steps

1. Re-read this task, the T003 parent, finalized `T003_05`, finalized `T003_06`,
   finalized `T003_07`, finalized `T002_06`, `provider-configuration-contracts.md`,
   `java-generation-guidance.md`, and `testing-strategy-and-agent-rules.md`.
2. Check current Spring AI reference material before naming Spring-side mapping
   concepts.
3. Create the provider Spring AI adapter source-generation handoff with the
   structure above.
4. Ensure every planned source name is labeled as planned, not current
   implementation.
5. Update this task with solve and finalization notes.
6. Update parent task, memory, and current-state architecture links only where the
   completed documentation changes durable context.

### Verification Strategy

Required for this documentation-only plan and solve:

```bash
git --no-pager diff --check
```

Do not run `task test`, `task build`, `task native`, Maven, Spring Shell, or live
provider commands unless Java source, tests, build files, Taskfiles, runtime
configuration, or provider configuration change unexpectedly.

### Dependencies And Tradeoffs

- Depends on finalized `T002_06` for broad provider configuration and Spring AI
  adapter design, finalized `T003_05` for runtime/session/event source-generation
  boundaries, finalized `T003_06` for CLI prompt adapter exclusions, and finalized
  `T003_07` for context/workspace handoff boundaries.
- Keeps source generation delayed by one documentation slice, but prevents the
  future provider implementation from leaking Spring AI, provider SDK, tool,
  permission, context, storage, CLI/TUI, or live-provider behavior into the wrong
  boundary.
- Leaves the exact Spring Boot configuration binding shape, stream abstraction,
  provider starter dependencies, and live smoke setup to later implementation
  tasks while fixing the Codegeist-owned adapter contract.

### Open Questions

None. The later source task may choose the concrete Spring Boot binding classes,
stream abstraction, and provider starter dependencies as long as public contracts
remain Codegeist-owned and offline tests prove the boundary first.

## Planning Result

- Phase command: `/plan-task t003_08`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the existing `/specify-task t003_08`
  result in this task.
- Duplicate check result: no existing
  `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`
  existed before this pass.
- Related context files read: T003 parent task, finalized `T003_05`, finalized
  `T003_06`, finalized `T003_07`, finalized `T002_06`,
  `provider-configuration-contracts.md`, `java-generation-guidance.md`,
  `testing-strategy-and-agent-rules.md`, current-state architecture and memory
  docs, and Spring AI `2.0.0-M6` reference snippets for chat model, prompt,
  options, response, OpenAI/Ollama configuration, streaming, and tool-callback
  concepts.
- Result: planned one documentation-only contract slice for
  `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`.
- Open decisions or blockers: none.
- Next recommended phase: `/solve-task t003_08`.

## Solution Note

- Phase command: `/solve-task t003_08`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Upstream phase dependency: satisfied by the `/plan-task t003_08` result and
  implementation plan in this task.
- Files changed:
  `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`,
  this task, the T003 parent task, current-state architecture links, and
  `docs/memory-bank/chat.md`.
- Result: solved the documentation-only source-generation handoff for the first
  provider configuration and Spring AI adapter slice. The new specification defines
  planned provider ids, model refs, capabilities, option profiles,
  credential-source references, offline validation, runtime-facing adapter records,
  streaming fallback, typed provider errors, Spring AI mapping isolation,
  OpenAI-compatible/OpenAI and Ollama first-wave posture, disabled tool-callback
  posture, boundary exclusions, illustrative Java examples, TDD handoff, deferrals,
  and a later implementation checklist.
- Acceptance criteria status: satisfied. No Java source, tests, package
  directories, build files, Spring beans, provider starters, credentials, live model
  calls, runtime behavior, provider behavior, CLI/TUI behavior, context loading,
  workspace reads, tools, permissions, storage, patch/edit, shell behavior, or
  native/build behavior were created.
- Verification: `git --no-pager diff --check` passed after finalization updates.
- Open decisions or blockers: none.
- Next recommended phase: completed by `/finalize-task t003_08`.

## Finalization Result

- Phase command: `/finalize-task t003_08`.
- Context or instructions considered: no extra user instructions beyond the task
  reference.
- Upstream phase dependency: satisfied by the successful solve result above.
- Impacted tasks: updated the T003 parent progress notes so future provider
  implementation consumes the finalized handoff and still waits for a dedicated
  source-generating task. Adjacent `T003_09` already references `T003_08` as the
  provider handoff boundary and needs no scope change.
- Documentation updates: created
  `docs/developer/specification/provider-spring-ai-adapter-source-generation-contract.md`,
  refreshed current-state architecture links without claiming implementation, and
  refreshed `docs/memory-bank/chat.md` because future sessions need to know
  `T003_08` is finalized.
- Remaining follow-ups: a later dedicated Java implementation task should create
  provider configuration and Spring AI adapter contracts with TDD after or
  alongside the minimum runtime/session/event contracts, then let tool/permission,
  storage, end-to-end loop, CLI/TUI parity, and provider smoke tasks consume the
  provider boundary.
- Verification: `git --no-pager diff --check` passed.
- Result: finalized.

## Creation Note

Created from the T003 parent child slot after the user paused Java implementation
and requested a documentation-only provider configuration and Spring AI adapter
contract before source generation.
