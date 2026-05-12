# T002_02 Introduce Runtime Vocabulary Contracts

Parent: `T002_implement-codegeist-mvp-foundation`

Source: `docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_02_map-opencode-concepts-to-java-stack.md`

## Goal

Turn the OpenCode-to-Java concept mapping into the first Codegeist-owned Java
vocabulary and package contract skeletons for the MVP runtime foundation.

## Context

`T001_02` established that OpenCode is a feature reference, not a runtime
blueprint. Its concept mapping names Codegeist-owned concepts such as Runtime,
Session, Agent mode, Context, Provider, Tool, Permission, Workspace, Event,
Storage, and Extension mediation.

`T002_01_align-codegeist-build-baseline.md` should establish the compatible
Spring Boot, Spring AI, Spring Shell, Java, Maven, and GraalVM build baseline
first. After that baseline is available, this task creates a small Java
vocabulary layer so later implementation tasks can target stable package
boundaries instead of inventing names ad hoc.

## Concrete Solution

Add minimal package-level contracts and type names that reflect the architecture
mapping without implementing prompt execution, provider calls, tools, storage, or
UI behavior:

1. Create package placeholders or minimal contracts for the first MVP boundary
   names under `app/codegeist/src/main/java/ai/codegeist/`.
2. Prefer small Java records, enums, interfaces, or package documentation only
   when they express a real boundary from the concept mapping.
3. Start with runtime vocabulary that later tasks need immediately:
   `runtime`, `session`, `agent`, `context`, `provider`, `tool`, `permission`,
   `workspace`, and `event`.
4. Keep `storage`, `extension`, `server`, and `ui.vaadin` documented as later or
   placeholder boundaries unless a minimal marker is needed to prevent naming
   drift.
5. Add focused tests that prove the initial vocabulary can be compiled and used
   together without Spring Shell or provider integration.
6. Avoid OpenCode implementation names, Bun/TypeScript concepts, transport
   schemas, or database assumptions unless the parity architecture explicitly
   renamed them as Codegeist concepts.

## Scope

- Minimal Java source files under `app/codegeist/src/main/java/ai/codegeist/` for
  concept vocabulary and package ownership.
- Minimal tests under `app/codegeist/src/test/java/ai/codegeist/` that compile and
  exercise the contracts at a shallow level.
- Documentation updates only when a package name or boundary differs from
  `docs/developer/codegeist-opencode-parity.md`.

## Target Files And Packages

- `app/codegeist/src/main/java/ai/codegeist/runtime/`
- `app/codegeist/src/main/java/ai/codegeist/session/`
- `app/codegeist/src/main/java/ai/codegeist/agent/`
- `app/codegeist/src/main/java/ai/codegeist/context/`
- `app/codegeist/src/main/java/ai/codegeist/provider/`
- `app/codegeist/src/main/java/ai/codegeist/tool/`
- `app/codegeist/src/main/java/ai/codegeist/permission/`
- `app/codegeist/src/main/java/ai/codegeist/workspace/`
- `app/codegeist/src/main/java/ai/codegeist/event/`
- `app/codegeist/src/test/java/ai/codegeist/`
- `docs/developer/codegeist-opencode-parity.md` only if the implemented package
  names intentionally differ from the current architecture map

## Acceptance Criteria

- Codegeist has explicit Java package boundaries for the first mapped runtime
  concepts instead of only architecture prose.
- The initial types use Codegeist names and ownership from `T001_02`, not
  OpenCode implementation technology names.
- Runtime-facing vocabulary can reference session, agent mode, context, provider,
  tool, permission, workspace, and event concepts without circular adapter
  ownership.
- No provider calls, tool execution, file edits, shell execution, persistence,
  server endpoints, Vaadin views, PF4J plugin loading, or JBang execution is
  implemented.
- Focused tests compile and exercise the new contracts enough to catch naming or
  dependency-direction drift.

## Verification

Run after `T002_01` is solved and the build baseline is available:

```bash
task test
```

Run from the repository root before finishing:

```bash
git --no-pager diff --check
```

If `task test` cannot run because `T002_01` is not solved yet, stop and solve
`T002_01` first instead of weakening this task.

## Dependencies

- Depends on `T001_02` for the concept mapping and allowed Codegeist vocabulary.
- Depends on `T002_01_align-codegeist-build-baseline.md` for a compatible build
  and test baseline.
- Feeds later runtime/session/event, context-loading, provider, tool,
  permission, workspace, and event implementation tasks.

## Non-Goals

- Do not implement prompt orchestration or agent execution.
- Do not implement Spring AI provider integration or model calls.
- Do not implement tool execution, permission prompts, workspace file access,
  shell commands, patch application, or storage.
- Do not introduce server, Vaadin, PF4J, or JBang runtime behavior.
- Do not split Maven modules.
- Do not copy OpenCode's TypeScript package structure, event schema, or storage
  model.

## Open Questions

- Should the first runtime vocabulary use Java records and enums only, or should
  a few service interfaces be introduced where later tasks need injection seams?
- Which later boundaries, if any, deserve package-level documentation now without
  concrete types?

## Specification Check Result

- Rechecked with the T002 parent default hints.
- The task remains a vocabulary/package-boundary slice, not behavior
  implementation.
- OpenCode source evidence is optional here; source lookups become more valuable
  in later session, event, provider, tool, and MCP-oriented tasks.

## Creation Note

Status: open.

Created interactively from `T001_02_map-opencode-concepts-to-java-stack.md`. The
user selected the `Runtime vocabulary` option over command-mapping and
extension-mapping slices. This task was migrated under the `T002` MVP foundation
parent and should be solved after `T002_01` so the first Java concept contracts
compile on the selected build baseline.
