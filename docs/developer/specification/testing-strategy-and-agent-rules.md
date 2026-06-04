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
  provider config binding and validation, SpEL preprocessing,
  provider feature tests gated by `CODEGEIST_TEST_PROVIDER_CATEGORY`, and
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

## Provider Feature Tests

Provider feature tests run through `task test` and provider category checks may be
applied at method or class level. `CODEGEIST_TEST_PROVIDER_CATEGORY` defaults to
`none`, so ordinary broad verification does not require local provider calls. Set
`CODEGEIST_TEST_PROVIDER_CATEGORY=local` when the fixed local Ollama service and
model should run.

Provider categories:

| Category | Meaning |
| --- | --- |
| `none` | No annotated provider feature calls; config-only provider checks still run. |
| `local` | Local provider calls, such as Ollama chat. |
| `remote_free` | Remote feature calls that are explicitly selected as no-cost. |
| `remote_paid` | Paid or paid-capable remote feature calls. |

`CODEGEIST_TEST_PROVIDER_CATEGORY` selects the highest provider category to
run. `remote_paid` is the explicit cost and rate-limit opt-in and runs all
provider categories. API-key presence alone never enables a remote provider
method.

## First Provider Workflow

The first provider-backed workflow should use an externally managed local Ollama
instance started through `task ollama-start`.

- Do not use Testcontainers for the first Ollama workflow.
- Do not pull, download, create, or delete local Ollama models in Java tests; the
  Taskfile owns host container startup and selected-model availability.
- Run `OLLAMA_ENTER=false task ollama-start` from `app/codegeist/cli` before
  `task test` when using the default local provider category.
- Keep deterministic model options such as temperature or seed in the runtime
  request or provider feature test method, not in provider config.
- Use a narrow prompt and stable assertion.
- Report Spring startup and first chat-call timings separately; do not add separate
  provider preflight helpers that duplicate the selected call.
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
- Spring startup, first provider call latency, and other meaningful slow setup are
  reported explicitly.
- `docs/developer/architecture/architecture.md` is updated when source,
  configuration, tests, or runtime behavior changes.
