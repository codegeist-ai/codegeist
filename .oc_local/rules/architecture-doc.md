# Architecture Documentation Rule

Use this rule when creating, reading, or updating Codegeist architecture and
specification documentation under `docs/developer/architecture/` and
`docs/developer/specification/`.

## Purpose

- Treat `docs/developer/architecture/` as the home for current-state project
  architecture documents that describe implemented Codegeist structure,
  boundaries, runtime flows, and architecture-relevant tests.
- Treat `docs/developer/architecture/architecture.md` as the current-state
  architecture map for Codegeist.
- Treat focused docs such as `docs/developer/architecture/<topic>.md` as the place
  for deeper source-code documentation when a subsystem needs problem analysis,
  Spring component detail, runtime flow diagrams, UML-style class diagrams, or
  task handoff context beyond the compact architecture map.
- Treat `docs/developer/specification/` as the home for planned architecture,
  contract blueprints, implementation guidance, and future-facing specification
  documents.
- Use it to give coding agents a compact, accurate view of what exists in the
  repository now.
- Keep target architecture, parity planning, and future design details in
  specification documents only when they are clearly marked as planned or not
  implemented. Broad OpenCode parity planning can live in
  `docs/developer/specification/codegeist-opencode-parity.md` when it is not current
  implementation architecture.

## What Belongs In Architecture Docs

- Current implemented system structure.
- Current Java/Spring Boot CLI application layout under `app/codegeist/cli`.
- Current Maven, Spring Boot, Spring Shell, Spring AI BOM, Java, GraalVM, and
  Taskfile build posture.
- Implemented packages, classes, configuration files, tests, and entrypoints.
- Current runtime behavior and startup flow.
- Mermaid or UML diagrams that summarize components, package layout, class
  relationships, runtime flows, startup, adapter boundaries, and verification
  flows.
- Focused source-code documentation for implemented subsystems that explains
  important source files, solved problems, Spring annotations and bean contracts,
  validation flows, error behavior, tests, sharp edges, and future task impact.
- Excalidraw overview sketches under `docs/developer/architecture/diagrams/` when
  a subsystem map benefits from an editable visual explanation.
- Java and Spring examples that clarify intended contracts, services, adapters,
  records, configuration properties, errors, and tests. Examples must be labeled
  as illustrative unless they reflect existing source.
- Architecture-relevant test expectations: contract tests, unit tests,
  integration tests, Spring context tests, CLI tests, smoke tests, native posture
  checks, and the behavior each test proves.
- Explicit "not implemented yet" sections when they prevent a coding agent from
  assuming planned architecture already exists.

## What Does Not Belong

- Aspirational architecture that has not been implemented.
- Broad OpenCode parity analysis.
- Future module/package plans unless clearly labeled as planned or not
  implemented.
- Task-level implementation logs.
- Duplicated long-form content from `docs/developer/specification/codegeist-opencode-parity.md`.

## What Belongs In Specification Docs

- Planned architecture for upcoming implementation slices, clearly marked as
  planned and kept separate from current-state claims.
- Contract blueprints, boundary rules, Java/Spring examples, class diagrams,
  sequence diagrams, and implementation guidance that should exist before or
  alongside later runtime code.
- Architecture-relevant test plans and verification expectations for planned
  contracts.
- Deferred surface notes for JBang, Vaadin, server, API/SDK, PF4J, storage,
  provider, tool, shell, patch/edit, and similar future slices.

## Usage

- Read the relevant docs under `docs/developer/architecture/` early when working
  on Codegeist runtime, Spring Boot, CLI, build, package-boundary, or
  verification tasks.
- Before changing architecture-relevant code, compare the intended change against
  the current-state architecture.
- It is valid to plan architecture first in `docs/developer/specification/` and
  implement it later, as long as planned content is labeled as planned and not
  confused with current implementation.
- It is also valid to implement first and document immediately afterward, as long
  as the architecture docs are synchronized in the same task.
- After changing architecture-relevant code, update the relevant architecture doc
  in the same task so it remains accurate.
- When a change solves a non-obvious problem or interacts with Spring Framework,
  prefer a focused source-code doc over burying the analysis only in a task solve
  result. Link that doc from `architecture.md` when it describes current state.
- If architecture docs describe new contracts or behavior, the same plan or task
  must name the tests or verification that prove those contracts.
- If a change only affects broad parity planning, update the relevant task file or
  `docs/developer/specification/codegeist-opencode-parity.md` instead.

## Accuracy Requirements

- Describe the current repository state, not intended future state.
- Keep implemented and planned concepts visibly separate.
- Prefer concrete file paths and class names over vague descriptions.
- Keep diagrams small enough for a coding agent to scan quickly.
- Keep Mermaid class diagrams readable on a single DIN A4 landscape page when
  rendered or printed. Split large diagrams into focused views instead of forcing
  many classes onto one page.
- In Mermaid `classDiagram` blocks, format class attributes and methods on their
  own lines inside the class body instead of compressing them onto one line. Use
  this shape so records, enums, and method lists stay readable:

  ```mermaid
  classDiagram
      class MessagePartProjection {
        <<record>>
        PartId id;
        long sequence;
        MessagePartType type;
        String summary
      }
  ```
- Update diagrams when the described structure changes.
- Keep focused source-code docs synchronized with their diagrams, including
  Excalidraw SVGs and Mermaid diagrams.
- Keep architecture docs, implementation, and tests synchronized. The order may be
  docs-first or implementation-first, but the final task state must not leave them
  inconsistent.
