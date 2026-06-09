# Source Code Documentation Strategy

Codegeist source-code documentation highlights the important implementation
contracts, framework interactions, solved problems, and sharp edges that future
tasks should not rediscover from scratch.

## Purpose

- Explain why a non-trivial implementation exists, not just which files changed.
- Preserve problem analysis and resolution context when a task fixes a bug,
  framework integration issue, packaging issue, validation gap, or testability
  problem.
- Make Spring, Maven, GraalVM, shell, and provider-framework interactions visible
  enough that later implementation tasks can extend them safely.
- Keep current-state docs separate from future specifications.

## Documentation Layers

| Layer | Location | Purpose |
| --- | --- | --- |
| Current architecture map | `docs/developer/architecture/architecture.md` | Compact inventory of implemented packages, runtime behavior, tests, and not-yet-implemented areas. |
| Focused source-code docs | `docs/developer/architecture/<topic>.md` | Deeper explanation for implemented subsystems, Spring integration, solved problems, and diagrams. |
| Editable diagrams | `docs/developer/architecture/diagrams/` | Excalidraw SVGs and other diagram sources used by architecture docs. |
| Planned contracts | `docs/developer/specification/*.md` | Future-facing designs and implementation guidance that are not implemented yet. |
| Task handoff | `docs/tasks/...` | Task-specific scope, acceptance criteria, phase status, solve result, and verification evidence. |
| Project memory | `docs/memory-bank/chat.md` | Compact context for the next coding session. |

## When A Focused Source-Code Doc Is Required

Add or update a focused source-code doc when a change introduces one of these:

- A Spring component boundary, configuration binding path, validation path, bean
  qualifier, auto-configuration interaction, or context-startup constraint.
- A runtime flow that crosses several files, such as CLI command dispatch,
  provider setup, configuration loading, smoke-test orchestration, release
  artifact generation, or native-image resource handling.
- A bug or framework issue whose resolution would be expensive to rediscover.
- A policy-bearing adapter boundary for provider calls, tools, permissions,
  workspace access, storage, sessions, events, UI, plugins, or APIs.
- A non-obvious test setup, external service, Testcontainers use, smoke harness,
  or platform-specific behavior.

Small, local, self-explanatory changes can stay documented only in source names,
tests, and the task file.

## Recommended Focused Doc Shape

Use only the sections that add useful context for the subsystem:

- Scope and current status.
- Source map with concrete file paths and each file's responsibility.
- Problem analysis and resolution, especially when framework behavior drove the
  design.
- Spring component model: annotations, bean names, qualifiers, lifecycle timing,
  validation hooks, and which dependencies are Spring-managed.
- Runtime flow diagrams for startup, request/command handling, loading,
  validation, merge, or error propagation.
- UML-style class or component diagrams for source structure.
- Excalidraw overview sketch when a freeform picture helps future discussion.
- Error and validation behavior.
- Test evidence and the behavior each test proves.
- Sharp edges and future task impact.

## Diagram Policy

- Prefer Mermaid in markdown for class diagrams, sequence diagrams, and flowcharts
  that should stay close to the prose and render on GitHub.
- Use Mermaid `classDiagram` for Java type relationships and Spring stereotypes;
  keep each class body expanded over multiple lines for readability.
- Use Mermaid `sequenceDiagram` or `flowchart` for Spring context startup,
  explicit file loading, validation, command dispatch, and smoke-test flows.
- Use Excalidraw SVGs for editable overview sketches, subsystem maps, and diagrams
  that will likely be discussed or rearranged.
- Store Excalidraw SVGs under `docs/developer/architecture/diagrams/` and follow
  `.opencode/rules/excalidraw.md` so the payload remains editable in compatible
  tools.
- Keep diagrams small enough that a future task can load and update them without
  scanning a large architecture poster.

## Spring Documentation Checklist

When a change interacts with Spring Framework or Spring Boot, document these
points when they are relevant:

- Which class is a component and why: `@SpringBootApplication`, `@Component`,
  `@Service`, `@ConfigurationProperties`, `@Bean`, or test annotations.
- Which bean names or qualifiers are part of the contract.
- How Spring discovers the component: component scan, explicit bean method,
  configuration properties binding, or test context.
- Which lifecycle step owns the behavior: context startup, property binding,
  command execution, explicit service method, or smoke script.
- How validation runs, especially when direct Jackson-loaded config is parsed and
  validated outside Spring property binding.
- Which tests prove the behavior and whether they are Spring integration tests,
  unit tests, CLI tests, or smoke tests.

## Maintenance

- Update the focused source-code doc in the same task when implementation changes
  the documented source map, Spring component model, runtime flow, validation
  behavior, or tests.
- Link focused docs from `architecture.md` when they describe implemented current
  state.
- Rewrite stale analysis instead of appending contradictory notes.
- Keep durable docs in English, even when the implementation discussion happened
  in another language.
