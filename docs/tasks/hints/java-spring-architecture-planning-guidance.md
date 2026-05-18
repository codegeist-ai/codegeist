# Java Spring Architecture Planning Hint

Use this hint during `/plan-task` runs for Codegeist tasks that need Java and
Spring architecture documentation before implementation.

## Guidance

- Plan architecture in durable developer documentation before broad Java source
  changes when the task affects runtime, provider, tool, permission, workspace,
  shell, storage, session, event, CLI, or extension boundaries.
- Include UML or Mermaid diagrams when they make the planned structure easier to
  review, especially class diagrams for contracts, sequence diagrams for runtime
  flows, and component diagrams for adapters or module boundaries.
- Include concise Java and Spring examples when they clarify the intended shape of
  records, interfaces, services, adapters, configuration properties, tests, or
  error types.
- Define the matching test strategy in the plan. Name the contract, unit,
  integration, Spring context, CLI, native/posture, or smoke tests that must exist
  for the implementation slice and explain which behavior each test proves.
- Treat tests as a fixed part of the implementation contract. A later solve phase
  should add or update the planned tests with the code change and must not leave
  known failing tests unresolved unless the task records a concrete blocker.
- Keep examples clearly illustrative during planning. Do not create Java source,
  tests, package directories, build changes, or runtime behavior unless the target
  task is explicitly an implementation task and the solve phase owns those edits.
- Explain how the design should fit Codegeist-owned contracts rather than exposing
  Spring AI, Spring Shell, provider SDK, storage, shell, or third-party utility
  details directly to the runtime boundary.
- Prefer Spring Boot, Spring AI, Spring Shell, Java records, small interfaces,
  typed errors, and focused contract tests where they fit the existing Codegeist
  baseline.
- Record key constraints, non-goals, deferred surfaces, and follow-up task
  candidates so the later solve phase can implement one bounded slice safely.
- Record the narrow verification command set that should pass after the later
  implementation, preferring focused Maven or Taskfile checks over broad suites
  unless the task changes shared build, runtime, or packaging behavior.

## Non-Goals

- Do not use planning to bypass `/specify-task` decisions or reopen settled scope
  unless a concrete contradiction is found.
- Do not turn illustrative code snippets into committed Java source during the
  plan phase.
- Do not copy OpenCode, Spring AI Agent Utils, or other third-party package
  layouts directly when a Codegeist-owned Java/Spring boundary is needed.

## Example Usage

```text
/plan-task T003_01 docs/tasks/hints/java-spring-architecture-planning-guidance.md
```
