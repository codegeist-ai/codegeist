# T003_02 Define Java Generation Guidance

Parent: `T003_implement-codegeist-opencode-core-application`

Status: open

## Goal

Define Codegeist-specific Java and Spring generation guidance for coding agents
before the broad T003 implementation tasks create runtime, provider, tool,
workspace, CLI, or TUI source code.

The guidance should make later generated Java code small, testable, and aligned
with Codegeist-owned architecture instead of copying OpenCode, Spring AI Agent
Utils, or generic framework package layouts.

## Context

`T003_01` finalized the Spring AI Agent Utils boundary decision. Agent Utils may
be used directly inside implementation code when useful, but Codegeist contracts
must stay independent and adapters should appear only for concrete boundary
needs.

The T003 parent now targets both CLI and TUI core behavior while keeping JBang,
PF4J, Vaadin, headless server, and API/SDK surfaces in the backlog. Java source
generation guidance should reflect that scope so later implementation tasks do
not accidentally create deferred extension, plugin, web, or API behavior.

## Scope

- Define package and dependency direction guidance for the single current Maven
  module under `app/codegeist/cli`.
- Define when to create Java records, interfaces, services, adapters,
  configuration properties, typed errors, and tests.
- Define how generated code should preserve runtime-owned boundaries for
  sessions, events, context, provider calls, tools, permissions, workspace,
  patch/edit, shell, storage, CLI, and TUI.
- Define how Spring Boot, Spring AI, Spring Shell, and Spring AI Agent Utils may
  be used without leaking framework or utility types into Codegeist domain
  contracts.
- Define test expectations that later T003 implementation tasks must satisfy when
  they generate Java source.
- Define non-goals and deferred surfaces so generated code does not implement
  JBang, PF4J, Vaadin, headless server, or API/SDK behavior in T003 core tasks.

## Non-Goals

- Do not create Java source files, Java tests, packages, Spring beans, runtime
  behavior, provider callbacks, storage adapters, shell executors, CLI commands,
  or TUI implementation in this guidance task.
- Do not introduce new Maven modules or move source directories.
- Do not add dependencies or change the build baseline.
- Do not reopen the finalized Agent Utils boundary decision from `T003_01`.
- Do not implement deferred JBang, PF4J, Vaadin, server, API, or SDK surfaces.

## Direct Inputs

- `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`
- `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_01_analyze_spring_ai_agent_utils_adoption.md`
- `docs/developer/spring-ai-agent-utils-adoption.md`
- `docs/developer/specification/codegeist-opencode-parity.md`
- `docs/developer/architecture/architecture.md`
- `.oc_local/rules/codegeist-task-specification.md`
- `docs/tasks/hints/java-spring-architecture-planning-guidance.md`

## Deliverables

Create or update a durable developer guidance document for Java generation. The
preferred target is:

- `docs/developer/specification/java-generation-guidance.md`

The guidance should include:

- Package ownership and dependency direction rules.
- Code shape rules for records, interfaces, services, adapters, configuration,
  errors, and event/result types.
- Spring Boot, Spring AI, Spring Shell, and Agent Utils boundary rules.
- CLI and TUI adapter expectations.
- Test-generation expectations for unit, contract, Spring context, CLI/TUI, and
  smoke tests.
- Examples that are clearly illustrative and not committed as source code.
- A short checklist for later T003 implementation tasks.

## Acceptance Criteria

- The guidance is written in English and stored under `docs/developer/`.
- The guidance is specific enough for later coding agents to generate Java source
  without inventing package structure or bypassing Codegeist boundaries.
- The guidance keeps Codegeist domain contracts independent from Spring AI Agent
  Utils architecture and raw broad provider callbacks.
- The guidance treats CLI and TUI as required T003 core client surfaces while
  keeping JBang, PF4J, Vaadin, server, and API/SDK work deferred.
- The guidance defines test expectations for generated code instead of leaving
  tests as optional follow-up work.
- No runtime code, tests, package directories, Maven changes, or behavior are
  created by this task.

## Verification

```bash
git --no-pager diff --check
```

## Creation Note

Created as the next T003 child task after `T003_01` was finalized and the parent
scope was adjusted so T003 includes CLI and TUI core behavior while moving JBang
and PF4J implementation to the backlog.
