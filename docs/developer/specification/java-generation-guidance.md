# Iterative Java Implementation Guidance

Guidance for adding Codegeist Java and Spring source without creating placeholder
architecture.

## Purpose

Use this document before adding Java source. It replaces the previous
source-generation posture: broad package maps, planned type catalogs, and
implementation handoffs are no longer source-authoritative.

## Core Rule

Create code only when a focused test requires it.

- Start from the next smallest observable behavior.
- Write the failing test first when practical.
- Prefer a Spring Boot test when the behavior depends on Spring wiring,
  configuration binding, provider selection, or model invocation.
- Add only the source, configuration, and dependencies needed to pass that test.
- Do not create ids, records, enums, interfaces, ports, adapters, packages, or
  validation hierarchies only to reserve future architecture.
- Do not create files under `docs/developer/implementation/`; that handoff layer
  is obsolete.

## First Provider Workflow

For the first provider-backed workflow, use an externally managed local Ollama
instance with a `llama3`-family model already downloaded before the focused test
starts instead of a fake provider.

Implementation constraints:

- Do not use Testcontainers for the first Ollama workflow.
- Do not pull, download, create, or delete local Ollama models in the test.
- Use `temperature=0`.
- Use a fixed seed when the active Spring AI and Ollama versions support it.
- Keep prompts small and assertions narrow enough to be stable.
- Report Ollama readiness/model-availability and first chat-call timing separately
  from ordinary Spring test timing.
- Do not use remote provider credentials for the first workflow.

## Type Introduction Rules

- Add a record when the current test needs immutable data crossing a real boundary.
- Add an enum only when the current behavior has a closed set of real values.
- Add an interface only when the current test needs a seam or there is more than
  one immediate implementation/caller.
- Add a service when the current behavior coordinates real work.
- Add configuration properties only for values that Spring Boot must bind now.
- Add typed failures only when a current test asserts recoverable user-facing or
  policy-facing failure behavior.

## Boundaries

Spring Boot may own startup, dependency injection, configuration binding, and test
context setup. Spring AI may own local model invocation behind the current tested
workflow. Codegeist should avoid making Spring, provider SDK, CLI, storage,
workspace, tool, permission, session, or event abstractions public before a tested
workflow needs them.

## What Not To Copy

- Do not copy OpenCode TypeScript schemas, Effect layers, Hono routes, storage
  tables, or generated SDK models into Java.
- Do not copy Spring AI Agent Utils architecture as Codegeist public architecture.
- Do not turn specification vocabulary into Java packages unless behavior requires
  it.

## Documentation Updates

When source changes, update `docs/developer/architecture/architecture.md` in the
same task so it describes what exists now, not what is planned.
