# Codegeist OpenCode Parity Reference

Behavior reference for evolving Codegeist toward selected OpenCode-style coding
agent workflows without copying OpenCode internals or pre-generating Java
contracts.

## Purpose

Use OpenCode as evidence for user-facing behavior, not as a Java implementation
blueprint. This document intentionally avoids Java type catalogs, package maps,
ports, ids, and source-generation plans.

## Technology Baseline

- Java 25 remains the preferred language baseline while it works with the current
  Spring stack.
- Spring Boot owns application startup, dependency injection, and configuration.
- Spring Shell is the first CLI surface.
- Spring AI is the first model integration layer.
- Spring AI Agent Utils may be used as an internal implementation aid when a
  focused test and Codegeist policy boundary justify it.
- GraalVM native posture remains a later verification concern.

## Parity Principles

- Translate behavior, not implementation shape.
- Prefer the smallest Spring-tested workflow over broad contract generation.
- Avoid placeholder ids, ports, records, enums, and package layers.
- Keep OpenCode's TypeScript schemas, Effect layers, Hono routes, storage tables,
  and generated SDK models out of Codegeist source.
- Keep future Vaadin, server, PF4J, and JBang surfaces deferred until a tested
  workflow needs them.

## First Implementation Direction

The first provider-backed workflow should be local and testable:

```text
Spring Boot test
  -> Spring-managed prompt workflow
  -> pinned Ollama Testcontainer with llama3
  -> observable response
```

Use `temperature=0`, a fixed seed when supported, pinned image/model tags, and
narrow assertions. Do not start with fake provider contracts or remote provider
credentials.

## Behavior Areas To Grow Iteratively

- Prompt workflow through Spring and Spring AI.
- CLI command delegation through Spring Shell.
- Context loading only when prompt behavior needs context.
- Tool and permission behavior only when a model/tool workflow needs it.
- Patch/edit and shell behavior only after permission and workspace policy are
  proven by tests.
- Storage only when a workflow needs continuation or persistence.
- Packaging and native checks only after a meaningful workflow exists to smoke
  test.

## Open Questions

- Which prompt workflow is the first user-visible slice after the Ollama-backed
  Spring test passes?
- Which `llama3` tag and Ollama image tag should be pinned for the first test?
- Which deterministic prompt/response assertion is stable enough across local
  machines?
- When should Spring AI Agent Utils be used directly instead of plain Spring AI?
