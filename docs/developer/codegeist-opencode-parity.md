# Codegeist OpenCode Parity Architecture

This document defines the first architecture decisions for evolving
`codegeist.ai` toward OpenCode feature parity. It starts with the baseline
technology stack and will grow into the feature matrix and component model for
the implementation tasks that follow.

## Technology Baseline

Codegeist should be designed around a Java-first stack:

- Java is the primary implementation language.
- GraalVM is the native-image and runtime optimization target.
- Spring is the main application framework baseline.
- Spring AI is the preferred integration layer for model providers and AI
  runtime concerns.
- Vaadin is the preferred Java-native web UI option for a future browser client.
- JBang is the lightweight Java scripting and command prototyping option.
- PF4J is the plugin framework for extension points.

## Initial Architecture Implications

- The runtime should stay Java-native instead of copying OpenCode's Bun and
  TypeScript runtime shape.
- The current Spring Boot and Spring Shell bootstrap under `app/codegeist` is a
  valid starting point for the CLI and runtime foundation.
- GraalVM support should influence dependency choices early, especially for
  reflection-heavy libraries, dynamic loading, and plugin boundaries.
- Spring AI should be evaluated as the default provider abstraction before
  introducing custom provider contracts.
- Vaadin should be treated as a later client surface over the same runtime, not
  as the owner of agent orchestration.
- JBang should be considered for lightweight developer commands, experiments,
  and migration helpers, not for core long-running runtime state.
- PF4J should own the plugin boundary for extension points such as tools,
  commands, skills, hooks, and integrations.
- PF4J plugin loading must be evaluated against GraalVM constraints before it is
  treated as native-image compatible.

## Concept Mapping To The Java Stack

OpenCode should be used as a feature reference, not as an implementation shape
to copy. Codegeist concepts must be translated into the selected Java-first
technology stack.

| OpenCode concept | Codegeist mapping | Primary technology | Architecture direction |
| --- | --- | --- | --- |
| CLI entrypoint | Java application command with interactive and non-interactive modes | Spring Boot, Spring Shell | Keep command parsing and user interaction thin; delegate agent work to runtime services. |
| Terminal UI | Terminal-oriented UX over the runtime | Spring Shell, JLine | Start with shell commands and streaming output before committing to a full TUI. |
| Plan agent | Read-only agent mode | Spring AI, Spring services | Enforce read-only tool access through permissions, not by UI convention. |
| Build agent | Implementation-capable agent mode | Spring AI, Spring services | Allow edits and shell commands only through explicit tool contracts and permission checks. |
| Sessions | Long-lived conversation and task state | Spring beans, persistence layer | Model sessions independently from CLI so future server and Vaadin clients can reuse them. |
| Messages and events | Typed Java records or classes for prompts, responses, tool calls, approvals, and results | Java, Spring events or application services | Prefer explicit event types before introducing streaming infrastructure. |
| Provider/model layer | AI provider abstraction with Codegeist-specific policy on top | Spring AI | Use Spring AI first, add custom adapters only where Spring AI does not cover a required provider or behavior. |
| Tool registry | Registry of Java tool contracts and implementations | Spring beans, Spring AI tool support | Built-in tools should be Spring-managed; plugin tools should enter through PF4J extension points. |
| Permission system | Central policy gate for file, shell, network, and write actions | Spring services | Permission checks must sit between agent/tool requests and side effects. |
| File access | Workspace-scoped read and write services | Java NIO, Spring services | Keep path normalization and workspace boundary checks in one service. |
| Shell execution | Controlled process runner | Java Process API, Spring services | Treat shell commands as high-risk tool calls requiring explicit approval. |
| Patch/edit mechanism | Structured file modification tool | Java NIO, diff/patch library to be chosen | Prefer patch-shaped changes over ad hoc writes so approvals and audit logs stay readable. |
| Web/fetch access | Network tool with policy controls | Spring WebClient or Java HTTP client | Keep network access permissioned and independently testable. |
| LSP/code intelligence | Later code-intelligence service | LSP4J or dedicated adapter to be evaluated | Keep out of the first MVP unless needed for tool correctness. |
| Subagents/tasks | Nested agent runs or task workflows | Spring services, Spring AI | Model as runtime orchestration first; avoid coupling to plugin loading too early. |
| Plugins | External extension mechanism | PF4J | Use PF4J for tools, commands, hooks, integrations, and future skills. |
| Commands and skills | Repo-local or packaged extensions | PF4J, JBang where lightweight scripting fits | Commands that need runtime state should be PF4J extensions; small Java scripts may use JBang. |
| Repository context | Loader for rules, memory, tasks, docs, and local overlays | Java NIO, Markdown parsing if needed | Keep context loading deterministic and explainable before adding retrieval complexity. |
| Headless server/API | HTTP client surface over the same runtime | Spring Boot Web | Server should be a client adapter, not a second implementation of agent behavior. |
| Web UI | Browser client over runtime/server APIs | Vaadin | Vaadin should present sessions, approvals, events, and tool results without owning orchestration. |
| Desktop app | Later packaging/client option | To be decided | Do not choose desktop technology until CLI/server/web boundaries are stable. |
| SDK/OpenAPI | Generated client contract for headless API | Springdoc/OpenAPI to be evaluated | Only relevant after the server API is real. |
| Storage | Session, event, config, and audit persistence | Spring Data or lightweight file storage to be decided | Start simple, but keep event/audit data model explicit. |
| Auth/server security | Protection for remote/headless access | Spring Security | Required before exposing server mode beyond localhost. |
| Packaging | JVM jar and native executable | Maven, GraalVM | Keep native-image constraints visible while allowing JVM-first prototyping. |

## Technology Ownership

- Java owns core domain types, tool contracts, event models, and workspace-safe
  file/process abstractions.
- Spring owns dependency injection, lifecycle, configuration, service wiring,
  application events, and future HTTP boundaries.
- Spring AI owns the first provider/model integration path and should be the
  default place to evaluate chat, tool-calling, prompt, and model abstractions.
- PF4J owns plugin boundaries for external tools, commands, hooks, skills, and
  integrations.
- Vaadin owns the future Java-native web client, but not runtime orchestration.
- JBang owns lightweight Java scripts, experiments, and developer automation
  that should not become long-running runtime services.
- GraalVM owns native packaging constraints and should influence dependency and
  plugin-loading decisions from the start.

## Open Questions

- Should the first implementation stay in one Maven module until runtime
  boundaries stabilize, or should the baseline architecture start as a
  multi-module Maven project?
- Which Spring AI provider path should be verified first?
- Which parts of the architecture must be GraalVM-native compatible from day
  one, and which can remain JVM-only during early prototyping?
- Should PF4J plugins be loaded only in JVM mode at first, with native images
  limited to built-in extensions until plugin loading is verified?
