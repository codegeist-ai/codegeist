# T001 Define Codegeist OpenCode Feature Architecture

## Goal

Define the feature parity target and architecture concept for evolving
`codegeist.ai` into a coding agent that can eventually replace OpenCode in this
repository's workflow.

This is an architecture and planning epic. It is intentionally
documentation-only and split into small child tasks.

## Context

The repository currently contains a minimal Codegeist bootstrap under
`app/codegeist/cli` using Spring Boot and Spring Shell. OpenCode has been analyzed as
a third-party reference under `docs/third-party/opencode/`, but the analysis is
based on static source inspection and generated analysis artifacts only. Runtime
behavior has not been verified yet.

The architecture work must translate OpenCode concepts into Codegeist's
Java-first stack instead of copying OpenCode's Bun/TypeScript runtime shape.

## Inputs

- `docs/third-party/opencode/README.md`
- `docs/third-party/opencode/ANALYSIS_REPORT.md`
- `docs/third-party/opencode/features/README.md`
- `docs/third-party/opencode/user/README.md`
- `docs/third-party/opencode/developer/README.md`
- `app/codegeist/cli/`
- `docs/memory-bank/chat.md`

## Technology Baseline

- Java as the primary implementation language.
- GraalVM as the native image and runtime optimization target.
- Spring as the application framework baseline.
- Spring AI as the preferred AI integration layer.
- Vaadin as the preferred Java-native web UI option.
- JBang as the lightweight Java scripting and command prototyping option.
- PF4J as the plugin framework for extension points.

## Child Tasks

- `T001_01_define-technology-baseline.md`
- `T001_02_map-opencode-concepts-to-java-stack.md`
- `T001_03_define-codegeist-module-boundaries.md`
- `T001_04_define-cli-and-shell-architecture.md`
- `T001_05_define-agent-mode-architecture.md`
- `T001_06_define-session-model.md`
- `T001_07_define-event-model.md`
- `T001_08_define-provider-architecture.md`
- `T001_09_define-tool-architecture.md`
- `T001_10_define-permission-architecture.md`
- `T001_11_define-workspace-and-file-access.md`
- `T001_12_define-shell-execution-architecture.md`
- `T001_13_define-patch-and-edit-architecture.md`
- `T001_14_define-context-loading-architecture.md`
- `T001_15_define-plugin-architecture-with-pf4j.md`
- `T001_16_define-jbang-role.md`
- `T001_17_define-vaadin-web-client-role.md`
- `T001_18_define-headless-server-architecture.md`
- `T001_19_define-storage-architecture.md`
- `T001_20_define-graalvm-constraints.md`
- `T001_21_define-feature-parity-matrix.md`
- `T001_22_define-mvp-cut.md`
- `T001_23_define-end-to-end-prompt-flow.md`
- `T001_24_define-risk-register.md`
- `T001_25_define-implementation-backlog.md`

## Required Deliverable

The child tasks collectively produce and refine:

```text
docs/developer/specification/codegeist-opencode-parity.md
```

The document should separate current facts, architecture decisions, assumptions,
open questions, MVP scope, later-stage capabilities, and out-of-scope items.

## Out Of Scope

- Implementing CLI commands beyond documentation changes.
- Implementing a TUI.
- Implementing LLM provider calls.
- Implementing tool execution.
- Implementing file edits, shell execution, or patch application.
- Implementing a server API, web UI, desktop app, SDK, or plugin runtime.
- Migrating existing OpenCode workflows to Codegeist.

## Acceptance Criteria

- All child tasks are defined as separate task files.
- Each child task has one narrow architecture outcome.
- The final architecture document includes a feature matrix, component model,
  prompt flow, MVP cut, risk register, and implementation backlog.
- No runtime implementation is included in this epic.

## Verification

- Review all child task files for single-responsibility scope.
- Confirm the old flat `T001` task file no longer exists next to the parent
  directory.
- Confirm the proposed architecture can start from the current `app/codegeist/cli`
  Spring Boot/Spring Shell bootstrap without requiring a full rewrite first.
