# T002_03 Describe Runtime Session Event Contracts

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_03`, `T001_05`, `T001_06`, `T001_07`, `T001_22`, `T001_23`

Status: specified

## Goal

Describe the first runtime-owned contracts for prompt requests, agent modes,
sessions, turns, message parts, and runtime events as architecture documentation
and diagrams, without adding Java source files yet.

## Context

The architecture defines Runtime as the central orchestrator, Plan and Build as
runtime modes, Session as the user-work aggregate, and RuntimeEvent as the shared
observation contract for CLI now and later server/Vaadin clients.

This task follows `T002_02`, which documents the Codegeist-owned runtime
vocabulary and boundary direction in `docs/developer/specification/runtime-vocabulary.md`. It
adds a concrete contract blueprint needed by later prompt-flow, provider, tool,
permission, and storage tasks.

The task must use OpenCode as source evidence before finalizing the Codegeist
shape. OpenCode is a feature reference, not an implementation blueprint: translate
its session, message, event, sync, bus, and projection ideas into Codegeist-owned
Java-first terminology instead of copying TypeScript, Effect, Drizzle, storage,
or route schemas.

## Concrete Solution

1. Create `docs/developer/specification/runtime-session-event-contracts.md` as the contract
   blueprint for prompt requests, agent modes, sessions, turns, message parts,
   runtime event envelopes, event families, sequencing, and projection rules.
2. Include source-evidence notes from OpenCode files under
   `docs/third-party/opencode/source/packages/opencode/src/`, especially
   `session/schema.ts`, `v2/session.ts`, `v2/session-event.ts`,
   `v2/session-message.ts`, `sync/index.ts`, `bus/index.ts`, and
   `session/projectors-next.ts`.
3. Add diagrams that make the intended shape visible before code exists:
   boundary, domain/class, prompt sequence, event projection, and event taxonomy.
4. Add OpenCode implementation concept diagrams for the comparable identity,
   session service, session event, session message, sync, bus, and projector
   shapes so future tasks can see how OpenCode implemented the ideas before
   translating them to Codegeist.
5. Include small Java sketch snippets only inside markdown, clearly marked as
   illustrative examples that are not implemented source.
6. Update developer documentation cross-references and current-state notes so
   future agents understand that the runtime/session/event contracts are specified
   but not implemented.

## Scope

- `docs/developer/specification/runtime-session-event-contracts.md`
- `docs/developer/README.md`
- `docs/developer/architecture/architecture.md`
- this task file

## Acceptance Criteria

- Runtime contracts are specified well enough to represent Plan and Build
  requests later.
- Session and turn contract diagrams include stable typed ids, status, mode, and
  ordered message/event relationships.
- Event contract diagrams include envelope, session sequence, visibility, audit
  relevance, and correlation fields.
- OpenCode source evidence is cited and translated into Codegeist-owned terms.
- Every core concept in this slice has a description and at least one class or
  relationship diagram covering either the OpenCode implementation evidence or the
  Codegeist blueprint: request, agent mode, session identity, session info, turn,
  message identity, message part, runtime event, event sequence, publication,
  projection, and deferred storage.
- Java examples are illustrative markdown snippets only and do not create source
  files, packages, tests, or runtime behavior.
- CLI, server, Vaadin, Spring AI, PF4J, JBang, and storage types do not leak into
  the described core contracts.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java or build files change. This task is a
documentation and diagram slice.

## Dependencies

- Depends on `T002_01` and `T002_02`.
- Use `docs/developer/specification/runtime-vocabulary.md` as the naming and ownership
  reference before describing Java packages or contracts.
- Use the local `/ask-project opencode` workflow or equivalent targeted source
  inspection against `docs/third-party/opencode/` before finalizing the contract
  blueprint.
- Feeds context, provider, tool, permission, storage, and CLI prompt-flow tasks.

## Non-Goals

- Do not call Spring AI or any provider.
- Do not execute tools, file edits, shell commands, or permissions.
- Do not implement storage, server endpoints, Vaadin views, or a full CLI prompt
  loop.
- Do not create Java source files, empty package directories, or contract tests.
- Do not copy OpenCode TypeScript, Effect, Drizzle, route, sync, bus, or storage
  schemas as Codegeist implementation.

## Open Questions

- None for this documentation slice. Later implementation tasks should decide
  whether the first runtime service returns a result object, publishes events
  through an in-process publisher, or exposes both contracts for tests.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task should use targeted OpenCode source questions for session, message
  part, and event flow before finalizing contract names.
- The user explicitly narrowed the task away from concrete Java implementation.
  The scope is now architecture documentation and diagrams, with optional
  illustrative Java snippets in markdown only.
- The `/ask-project opencode` workflow was applied read-only before solving. The
  relevant source references are `session/schema.ts`, `v2/session.ts`,
  `v2/session-event.ts`, `v2/session-message.ts`, `sync/index.ts`, `bus/index.ts`,
  and `session/projectors-next.ts`.
- The scope stays contract-blueprint level and should not implement provider,
  tool, permission, storage, or CLI orchestration behavior.

## Phase Status

- Phase: `/specify-task` repeated for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_03_introduce-runtime-session-event-contracts.md`.
- Context or instructions considered: the user requested a specification pass over
  the existing task after narrowing it away from concrete Java implementation and
  after asking to use `/ask-project opencode` source evidence.
- Parent considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Adjacent tasks considered:
  `T002_02_introduce-runtime-vocabulary-contracts.md` and
  `T002_04_wire_cli_prompt_mode_contract.md`.
- Hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Result: specified. The durable scope remains a documentation and diagram
  blueprint in `docs/developer/specification/runtime-session-event-contracts.md`; this task must
  not create Java source files, empty packages, service implementations, or tests.
- Repeat result: specified again after the user requested descriptions and class
  diagrams for all concepts and explicit documentation of how comparable concepts
  are implemented in `docs/third-party/opencode/`. The blueprint document now
  includes OpenCode concept diagrams for identity, session service, events,
  messages, sync/bus/projectors, plus a concept reference table that maps each
  OpenCode implementation idea to the Codegeist blueprint.
- Open decisions or blockers: none for the specification pass. Later
  implementation tasks must decide when the first Java contracts are actually
  needed.
- Next recommended phase: run `/plan-task` for the next dependency slice, likely
  `T002_04_wire_cli_prompt_mode_contract.md`, with the explicit understanding that
  `T002_03` produced a blueprint rather than implemented Java contracts.

## Solution Note

Status: solved.

Created `docs/developer/specification/runtime-session-event-contracts.md` as the architecture
contract blueprint for future runtime/session/event Java work. The document uses
OpenCode source evidence, Codegeist-owned naming, diagrams, and illustrative Java
sketches while leaving Java packages, source files, services, and tests
unimplemented.

## Creation Note

Status: solved.

Derived from the runtime, mode, session, event, MVP, and prompt-flow architecture
tasks as one grouped implementation slice.
