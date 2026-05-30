# T006_05 Verify Local Ollama Provider

Status: open

Parent: `../task.md`

## Goal

Implement and verify the first provider-backed Codegeist workflow with local
Ollama.

This is the first real provider call and should stay local, deterministic, and
small before remote provider account work begins.

## Scope

- Use the schema and loader from `T006_01` and `T006_04`.
- Add the Spring AI Ollama starter or the smallest Spring AI dependency needed for
  the test.
- Use a pinned Ollama image and model tag when using Testcontainers.
- Configure deterministic options: `temperature: 0` and a fixed seed when the
  active Spring AI and Ollama versions support it.
- Use a narrow prompt and stable assertion.
- Report container startup and model-pull timing separately from ordinary Spring
  test timing.
- Update current-state architecture for implemented provider behavior and tests.

## Non-Goals

- Do not use remote provider credentials.
- Do not implement tool calling, permissions, sessions, storage, TUI, Vaadin,
  server APIs, PF4J, or JBang.
- Do not add fake providers in place of the Ollama call.
- Do not make the ordinary unit test suite depend on a long model pull unless the
  task records an explicit test profile or opt-in command.

## Acceptance Criteria

- A focused test proves Codegeist can call a local Ollama model through Spring AI.
- The test command is individually runnable.
- The task reports Spring context startup timing, container startup timing, and
  model pull or preparation timing.
- Provider configuration comes from `codegeist.yml` or a test fixture matching the
  schema.
- The implementation does not introduce public provider architecture beyond what
  the tested workflow needs.

## Verification

Expected focused command from `app/codegeist/cli`:

```bash
mvn --batch-mode --no-transfer-progress -Dtest=<focused-ollama-test> test
```

Then run the relevant broader command after the focused test passes:

```bash
mvn --batch-mode --no-transfer-progress test
git --no-pager diff --check
```

Replace `<focused-ollama-test>` with the actual test class introduced by the task.

## Planning Notes

- Follow `docs/developer/specification/testing-strategy-and-agent-rules.md` for
  first provider workflow constraints.
- Prefer a pinned local Ollama Testcontainer with `llama3` unless the task updates
  that decision with source-backed evidence.
- Keep assertions narrow enough to survive model variance.
