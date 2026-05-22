# Testing Strategy And Agent Rules

Codegeist testing guidance for iterative Java/Spring implementation.

## Baseline

The implemented application is currently small:

- One Maven module: `app/codegeist/cli`.
- Java 25 via Maven compiler release.
- Spring Boot 4, Spring Shell 4, Spring AI BOM, and Spring AI Agent Utils are
  present in the build baseline.
- Implemented behavior is limited to Spring Boot application startup.
- `CodegeistApplicationTests#contextLoads` is the existing Spring context test.

## TDD Rules

- Start with the smallest failing test that proves the behavior.
- Add only the code needed to make that test pass.
- Keep tests individually executable with Maven/JUnit selectors.
- Broaden verification only after the focused test passes.
- Report targeted command, approximate duration, broader command when run, and any
  skipped or slow checks.

## Test Selection

- Use plain JVM tests for logic that does not need Spring.
- Use Spring Boot tests when the behavior is Spring wiring, configuration binding,
  provider selection, Spring AI invocation, command registration, or application
  startup.
- Use Testcontainers when the task intentionally owns an external local service.
- Do not hide provider startup, model pulls, CLI startup, shell execution, native
  builds, or filesystem-heavy checks inside tests that look like fast unit tests.

## First Provider Workflow

The first provider-backed workflow should use a pinned local Ollama Testcontainer
with `llama3`.

- Pin the Ollama image and model tag.
- Pull or prepare the model inside the test setup.
- Configure deterministic options: `temperature=0` and fixed seed when supported.
- Use a narrow prompt and stable assertion.
- Report container startup and model-pull timing separately.
- Do not use remote provider credentials for this workflow.

## Current Commands

Examples from `app/codegeist/cli`:

```bash
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistApplicationTests test
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistApplicationTests#contextLoads test
mvn --batch-mode --no-transfer-progress test
```

Add task-specific commands in the active task file when new tests are added.

## Solve Checklist

- A focused failing test was added or updated first, unless the task records why
  test-first work was not practical.
- The test can run by class or method selector.
- The implementation avoids placeholder types and packages.
- Spring startup, Testcontainers startup, model pulls, and other slow setup are
  reported explicitly.
- `docs/developer/architecture/architecture.md` is updated when source,
  configuration, tests, or runtime behavior changes.
