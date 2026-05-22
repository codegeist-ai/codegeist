# T003 Define Codegeist Implementation Readiness

Status: open

## Goal

Keep the useful implementation-readiness guidance from the former source-contract
epic while removing the placeholder-first source-generation path.

Future implementation work should start from the smallest focused workflow, write
or update a failing test first when practical, and add only the Java/Spring code
that the test requires. The previous contract-first handoff documents and child
tasks are obsolete because they encouraged broad placeholder packages, records,
ports, ids, enums, and validation hierarchies before behavior existed.

## Current Scope

- Use `docs/developer/specification/java-generation-guidance.md` for iterative
  Java/Spring implementation rules.
- Use `docs/developer/specification/testing-strategy-and-agent-rules.md` for TDD,
  individually executable tests, timing reports, and startup-heavy test handling.
- Use `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
  for packaging, release, platform, and binary-smoke work.
- Use `docs/developer/spring-ai-agent-utils-adoption.md` and
  `/ask-project spring-ai-agent-utils ...` when a task needs source-backed Agent
  Utils guidance.
- Use `/ask-project opencode ...` when behavior is not already present in Java and
  OpenCode source evidence is needed before translating a workflow.

## Child Tasks

- `T003_01_analyze_spring_ai_agent_utils_adoption.md`
- `T003_02_define_java_generation_guidance.md`
- `T003_03_define_testing_strategy_and_agent_test_rules.md`
- `T003_04_define_build_release_and_binary_smoke_strategy.md`

The removed `T003_05` through `T003_12` source-generation child tasks and their
generated specification documents are obsolete. Do not resurrect them as inputs
for implementation planning.

## Implementation Direction

- Keep the current single Maven module under `app/codegeist/cli` until real tests
  prove that another module or package boundary is useful.
- Core implementation scope includes CLI and TUI behavior.
- Keep JBang, PF4J, Vaadin, headless server, API, and SDK/OpenAPI surfaces in the
  backlog while preserving adapter-ready runtime boundaries when behavior exists.
- Prefer Spring and Spring AI integration from the beginning instead of building
  fake provider abstractions first.
- For the first provider-backed workflow, prefer a pinned local Ollama
  Testcontainer with `llama3`, `temperature=0`, and a fixed seed when supported.
- Keep Spring AI Agent Utils as a private implementation aid or behind a thin
  Codegeist wrapper only when policy, workspace, permission, session/event,
  output, or result mapping requires it.

## Non-Goals

- Do not create placeholder classes, ids, ports, records, enums, package layers,
  validation hierarchies, or empty package directories.
- Do not copy OpenCode's Bun, TypeScript, Hono, Effect, or storage architecture.
- Do not implement JBang, PF4J, Vaadin, headless server, API, or SDK/OpenAPI
  behavior in the next core implementation slice.
- Do not recreate broad implementation handoff documents under
  `docs/developer/implementation/`.

## Verification

Documentation-only changes should run:

```bash
git --no-pager diff --check
```

Implementation tasks should add the narrowest relevant Maven or Taskfile checks,
report targeted commands and timing, and update
`docs/developer/architecture/architecture.md` when implemented packages, classes,
configuration, or tests change.

## Progress Notes

- `T003_01` is finalized. Codegeist keeps Spring AI Agent Utils available while
  maintaining Codegeist-owned runtime, provider, tool, permission, workspace,
  event, session, storage, and native-readiness boundaries.
- `T003_02` is finalized and now points future implementation toward iterative
  Java/Spring work instead of source-generation handoffs.
- `T003_03` is finalized as the testing and development strategy.
- `T003_04` is finalized as the packaging, release, platform, and binary-smoke
  strategy.
- The next recommended step is to create a fresh, small implementation epic or
  task for the first tested Spring workflow.
