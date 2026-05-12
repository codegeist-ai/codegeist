# T001_03 Define Codegeist Module Boundaries

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the logical Codegeist architecture components and their responsibilities.

This task turns the broad runtime definition from `T001_02` into a concrete
component model, package-boundary proposal, and physical module timing decision.
It does not implement those packages yet.

## Runtime Definition

The Codegeist Runtime is the central orchestration layer that turns user input
into agent work. It owns sessions, agent modes, context loading, provider calls,
tool request handling, permission checks, runtime events, extension mediation,
and later storage coordination.

The runtime is not the JVM, Spring Boot itself, Spring Shell, Vaadin, an HTTP
controller, a PF4J plugin, or a JBang script. Those are clients, adapters, or
extensions that must call into the runtime instead of owning agent behavior.

## Scope

- Define boundaries for CLI, runtime, context, provider, tools, permissions,
  storage, server, plugins, and UI.
- Define runtime ownership explicitly so CLI, Vaadin, server adapters, PF4J
  plugins, and JBang scripts cannot become duplicate agent runtimes.
- Decide whether the first implementation should stay in one Maven module or
  start as a multi-module project.
- Keep boundaries logical even if physical modules are delayed.

## Architecture Decision

The MVP implementation should start as one Maven module under `app/codegeist`.
Strict logical boundaries must be defined now through package ownership,
interfaces, dependency direction, tests, and documentation. Physical Maven
modules are deferred until the core runtime contracts are stable enough to split
without structural churn.

Decision status: accepted for MVP architecture planning.

## Component Model To Specify

Add a component table with these responsibility boundaries:

| Component | Primary responsibility | Must not own |
| --- | --- | --- |
| App bootstrap | Spring Boot startup, configuration, process lifecycle | Agent behavior or user workflow decisions |
| CLI/Shell adapter | Spring Shell command parsing, terminal prompts, command output | Sessions, provider calls, permissions, tool execution |
| Runtime | Central orchestration for user input, sessions, agent modes, context, provider flow, tool requests, permissions, and events | UI rendering, Spring Shell command parsing, plugin class loading, provider SDK details |
| Session | Session aggregate, message model, continuation state | Provider SDK calls, tool side effects, UI rendering |
| Agent | Agent mode policy, especially Plan and Build behavior | CLI commands, provider integration, persistence adapters |
| Context | Deterministic context loading from rules, memory, tasks, docs, source snippets, and analysis artifacts | LLM calls or tool execution |
| Provider | Spring AI-backed model integration and streaming adapter boundary | Prompt ownership, sessions, permissions, tool policy |
| Tool | Tool registry, tool contracts, built-in tool execution surfaces | Permission decisions or workspace escape policy |
| Permission | Approval policy, decision scope, audit-relevant permission decisions | Tool implementation or UI prompts as the source of truth |
| Workspace | Repository roots, allowed paths, file identity, symlink/ignore policy | Edits or shell execution without tool and permission mediation |
| Event | Runtime event types, audit events, user-visible output events | Storage implementation or UI rendering |
| Storage | Persistence ports and later adapters for sessions, events, config, audit, and cache data | Runtime orchestration |
| Extension mediation | PF4J and JBang contribution registration and isolation | Core runtime state, workspace boundaries, permissions |
| Server adapter | Future HTTP/API request-response mapping | Runtime behavior |
| Vaadin client | Future Java web UI for sessions, approvals, and event display | Runtime behavior or permission source of truth |

## Proposed Java Package Map

Define logical package targets without creating them yet:

```text
ai.codegeist.app              Spring Boot entrypoint and application wiring
ai.codegeist.cli              Spring Shell adapter
ai.codegeist.runtime          orchestration API and services
ai.codegeist.session          session domain model
ai.codegeist.agent            agent mode policy
ai.codegeist.context          context loading
ai.codegeist.provider         Spring AI adapter boundary
ai.codegeist.tool             tool registry and contracts
ai.codegeist.permission       permission policy
ai.codegeist.workspace        file and workspace boundary
ai.codegeist.event            runtime and audit events
ai.codegeist.storage          persistence ports and later adapters
ai.codegeist.extension        PF4J and JBang mediation
ai.codegeist.server           HTTP adapter later
ai.codegeist.ui.vaadin        Vaadin client later
```

## Dependency Direction Rules

- `cli`, `server`, and `ui.vaadin` may depend on `runtime`, but `runtime` must
  not depend on those adapters.
- `provider` adapts Spring AI to Codegeist runtime contracts; it must not own
  sessions, prompt policy, permissions, or tool decisions.
- Tool implementations must pass through `permission` and `workspace` boundaries
  before side effects.
- PF4J plugins and JBang scripts may register commands, tools, skills, hooks, or
  integrations, but execution must flow through runtime-owned registries and
  permission checks.
- Storage is a runtime port. Runtime may use storage contracts, but storage
  adapters must not orchestrate agent execution.
- Package boundaries should be testable before they become physical Maven
  modules.

## Physical Module Split Triggers

Keep one Maven module until at least two of these are true:

- Runtime APIs are stable enough for both CLI and server adapters.
- Tool, permission, and workspace contracts have focused tests.
- Provider integration has at least one verified Spring AI backend.
- Storage ports are needed by more than one adapter.
- PF4J or Vaadin introduces dependency weight that should stay outside the core
  runtime classpath.
- GraalVM native-image configuration needs separate adapter boundaries.

## Non-Goals

- Do not create Maven modules.
- Do not move Java packages.
- Do not add dependencies.
- Do not implement runtime, provider, tool, permission, server, Vaadin, PF4J, or
  JBang behavior.
- Do not decide final MVP feature scope; that belongs to `T001_22`.

## Deliverable

Add `## Component Model And Module Boundaries` to
`docs/developer/codegeist-opencode-parity.md` with:

- the one-Maven-module-for-now decision,
- the component responsibility table,
- the proposed Java package map,
- dependency direction rules,
- physical Maven module split triggers,
- explicit non-goals.

## Acceptance Criteria

- Each component has one primary responsibility.
- Runtime logic is not owned by CLI, Vaadin, or server adapters.
- PF4J plugins and JBang scripts may extend behavior, but they do not own
  permissions, workspace boundaries, or audit events.
- Physical module timing is explicitly decided: one Maven module for MVP
  planning, physical module split deferred until runtime contracts stabilize.
- Dependency direction rules prevent adapter-to-runtime inversion.
- Proposed package boundaries can start from current `app/codegeist` without a
  full rewrite.

## Verification

- Confirm the component model can start from current `app/codegeist` without a
  full rewrite.
- Confirm `docs/developer/codegeist-opencode-parity.md` keeps the task
  documentation-only and does not require immediate Maven or package moves.

## Specification Check Result

- Already specifies Codegeist runtime ownership, Java package targets,
  dependency direction, physical module split triggers, and explicit non-goals.
- No further task reshaping was needed during the `/specify-task` pass.

## Solution Note

Status: completed.

The solution pass used the narrow documentation-first path because
`docs/developer/codegeist-opencode-parity.md` already contains the required
`Component Model And Module Boundaries` section. That section records the
one-Maven-module MVP decision, the component responsibility table, the proposed
Java package map, dependency direction rules, module split triggers, and explicit
non-goals.

No user decision is pending. The current `app/codegeist` layout has a single
Maven module, so the documented logical package boundaries can start there
without requiring immediate Maven module creation, package moves, dependency
changes, or runtime implementation.

Verification passed with `git --no-pager diff --check`. A final review confirmed
the parity document keeps this task documentation-only and preserves runtime
ownership outside CLI, server, Vaadin, PF4J, and JBang adapters.
