# Testing Strategy And Agent Rules

Codegeist testing guidance for iterative Java/Spring implementation.

## Baseline

The implemented application is currently small:

- One Maven module: `app/codegeist/cli`.
- Java 25 via Maven compiler release.
- Spring Boot 4, Spring Shell 4, Spring AI BOM, Spring AI Agent Utils, and
  `spring-ai-ollama` are present in the build baseline.
- Implemented behavior includes Spring Boot startup, `--version`, `--show-config`,
  typed provider config loading, trusted-local SpEL preprocessing, and the
  provider-neutral chat seam for a selected local Ollama provider.
- Current focused tests include Spring context startup, command stdout behavior,
  provider config binding and validation, SpEL preprocessing, and
  `LocalOllamaProviderIT` behind the explicit selector
  `task test TEST=LocalOllamaProviderIT`.

## TDD Rules

- Start with the smallest failing test that proves the behavior.
- Add only the code needed to make that test pass.
- Keep tests individually executable with Taskfile-backed Maven/JUnit selectors.
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

The first provider-backed workflow should use an externally managed local Ollama
instance with the selected `llama3`-family model already downloaded before the
focused test starts.

- Do not use Testcontainers for the first Ollama workflow.
- Do not pull, download, create, or delete local Ollama models in the test.
- Run `OLLAMA_ENTER=false task ollama-start` from `app/codegeist/cli` before
  `task test` when verifying the local Ollama provider workflow.
- Configure deterministic options: `temperature=0` and fixed seed when supported.
- Use a narrow prompt and stable assertion.
- Report Spring startup, Ollama readiness/model-availability, and first chat-call
  timings separately.
- Do not use remote provider credentials for this workflow.

## Current Commands

Examples from `app/codegeist/cli`:

```bash
task test TEST=CodegeistApplicationTests
task test TEST=CodegeistApplicationTests#contextLoads
task test
```

Add task-specific `task test` commands in the active task file when new tests are
added. Do not document direct `mvn test` commands for new Codegeist implementation
tasks.

## Solve Checklist

- A focused failing test was added or updated first, unless the task records why
  test-first work was not practical.
- The test can run by class or method selector.
- The implementation avoids placeholder types and packages.
- Spring startup, provider readiness, model availability, and other slow setup are
  reported explicitly.
- `docs/developer/architecture/architecture.md` is updated when source,
  configuration, tests, or runtime behavior changes.
