# T002_02 Describe Runtime Vocabulary Contracts

Parent: `T002_define-codegeist-mvp-foundation-blueprints`

Source: `docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_02_map-opencode-concepts-to-java-stack.md`

Status: finalized

## Goal

Turn the OpenCode-to-Java concept mapping into a Codegeist-owned runtime
vocabulary and boundary diagram for the MVP runtime foundation.

## Context

`T001_02` established that OpenCode is a feature reference, not a runtime
blueprint. Its concept mapping names Codegeist-owned concepts such as Runtime,
Session, Agent mode, Context, Provider, Tool, Permission, Workspace, Event,
Storage, and Extension mediation.

`T002_01_align-codegeist-build-baseline.md` established the compatible Spring
Boot, Spring AI, Spring Shell, Java, Maven, and GraalVM build baseline. This
task records the first Runtime vocabulary as documentation rather than Java
source so later implementation tasks can target stable names without creating
empty packages or premature behavior contracts.

## Concrete Solution

Add a versioned developer document that describes the runtime vocabulary and
ownership boundaries with a diagram:

1. Create `docs/developer/specification/runtime-vocabulary.md` as the compact reference for
   Codegeist-owned Runtime concepts.
2. Use a Mermaid diagram to show the Runtime as the orchestration boundary and
   the relationship to Session, Agent mode, Context, Provider, Tool,
   Permission, Workspace, and Event concepts.
3. Document `storage`, `extension`, `server`, and `ui.vaadin` as deferred
   boundaries, not implemented code.
4. Add a short vocabulary table and ownership rules that explain dependency
   direction and what each boundary must not own.
5. Update `docs/developer/architecture/architecture.md` so it still records the current
   implementation truth: no Runtime Java packages or classes exist yet.
6. Update `docs/developer/specification/codegeist-opencode-parity.md` and
   `docs/developer/README.md` to link to the vocabulary document.

Do not create empty Java packages or placeholder directories. Git does not
version empty directories, and this task intentionally avoids creating Java
classes or service interfaces before the runtime contracts are ready.

## Scope

- `docs/developer/specification/runtime-vocabulary.md` for the diagram, vocabulary table, and
  ownership rules.
- `docs/developer/architecture/architecture.md` for current-state cross-reference only.
- `docs/developer/specification/codegeist-opencode-parity.md` for target-architecture
  cross-reference only.
- `docs/developer/README.md` for discoverability.

## Target Files

- `docs/developer/specification/runtime-vocabulary.md`
- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `docs/developer/README.md`

## Acceptance Criteria

- Codegeist has an explicit, versioned Runtime vocabulary diagram under
  `docs/developer/`.
- The diagram names Runtime, Session, Agent mode, Context, Provider, Tool,
  Permission, Workspace, Event, and the deferred Storage, Extension, Server, and
  Vaadin boundaries.
- The vocabulary uses Codegeist names and ownership from `T001_02`, not OpenCode
  implementation technology names.
- The document explains dependency direction without introducing behavior-bearing
  Java service interfaces.
- `docs/developer/architecture/architecture.md` remains accurate as current-state
  documentation and still says the runtime packages/classes are not implemented.
- No Java source files, empty package directories, provider calls, tool
  execution, file edits, shell execution, persistence, server endpoints, Vaadin
  views, PF4J plugin loading, or JBang execution are introduced.

## Verification

Run from the repository root before finishing:

```bash
git --no-pager diff --check
```

`task test` is not required for this task unless Java source or build files are
changed. This slice is documentation-only.

## Dependencies

- Depends on `T001_02` for the concept mapping and allowed Codegeist vocabulary.
- Depends on `T002_01_align-codegeist-build-baseline.md` for the completed build
  baseline context.
- Feeds later runtime/session/event, context-loading, provider, tool,
  permission, workspace, event, storage, extension, server, and UI tasks.

## Non-Goals

- Do not create Java packages, Java classes, or placeholder directories.
- Do not implement prompt orchestration or agent execution.
- Do not implement Spring AI provider integration or model calls.
- Do not implement tool execution, permission prompts, workspace file access,
  shell commands, patch application, or storage.
- Do not introduce server, Vaadin, PF4J, or JBang runtime behavior.
- Do not split Maven modules.
- Do not copy OpenCode's TypeScript package structure, event schema, or storage
  model.

## Phase Status

- Phase: `/solve-task`-style documentation solution for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_02_introduce-runtime-vocabulary-contracts.md`.
- Result: solved as a documentation and diagram slice instead of a Java contract
  slice.
- Durable outcome: Runtime vocabulary lives in
  `docs/developer/specification/runtime-vocabulary.md`, with current-state and target
  architecture documents linking to it.
- Verification: `git --no-pager diff --check`.
- `/finalize-task` dependency: satisfied by the successful documentation solve
  status and `Status: solved` task note.
- `/finalize-task` impact review: updated the parent task language away from Java
  package vocabulary, updated `T002_03` to depend on the documented vocabulary,
  and refreshed repo memory so it no longer asks to plan or solve this task.
- `/finalize-task` documentation review: `docs/developer/README.md`,
  `docs/developer/architecture/architecture.md`, and
  `docs/developer/specification/codegeist-opencode-parity.md` already point to the new runtime
  vocabulary document; no additional README or rule update was needed.
- `/finalize-task` verification: `git --no-pager diff --check`.
- `/finalize-task` result: finalized. The next recommended phase is to continue
  with `T002_03_introduce-runtime-session-event-contracts.md`.

## Creation Note

Status: finalized.

Created interactively from `T001_02_map-opencode-concepts-to-java-stack.md`. The
user selected the `Runtime vocabulary` option over command-mapping and
extension-mapping slices. The task was later narrowed to a versioned diagram and
developer document because empty package directories are not versioned by Git and
premature Java classes would imply contracts that are not ready yet.
