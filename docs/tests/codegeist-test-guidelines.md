# Codegeist Test Guidelines

How Codegeist tests should be shaped and reported.

## Test Shape

- Start with the smallest focused test that proves the behavior.
- Prefer Spring Boot tests when behavior depends on Spring wiring, configuration
  binding, Spring Shell command dispatch, provider selection, or Spring AI model
  invocation.
- Use plain JVM tests for logic that does not need Spring context startup.
- Keep tests deterministic and individually executable by class or method selector.
- Do not introduce placeholder packages, interfaces, records, ids, enums, or
  validation layers only to satisfy planned architecture.

## Commands

Run normal contributor commands from the repository root:

```bash
task cli:test-jvm TEST=CodegeistApplicationTests
task cli:test-jvm TEST=CodegeistApplicationTests#contextLoads
task cli:check
```

Use task-specific selectors in active task docs and final reports. Keep
`task cli:check` as the final normal verification once focused tests pass.

## Provider Tests

- Use `provider-feature-tests.md` as the detailed provider feature test reference.
- Keep live provider tests individually executable with
  `task cli:test TEST=<selector>` so local or hosted prerequisites remain easy to
  isolate.
- `task cli:test-jvm` and `task cli:check` use command-local provider category
  `none`, overriding ambient values, and do not start Ollama. `task cli:check`
  also ignores ambient `TEST` so it always runs the complete suite. `task cli:test`
  is the explicit provider-capable path and starts the Taskfile-managed local
  Ollama service before Maven.
- Provider feature tests that can call providers must use method-level categories:
  `local`, `remote_free`, or `remote_paid`. Config-only checks stay unannotated.
- `CODEGEIST_TEST_PROVIDER_CATEGORY` selects the highest provider category to
  run. The default is `none`; `remote_paid` is the explicit cost and rate-limit
  opt-in and runs all provider categories.
- Local Ollama verification uses the Taskfile-managed local Ollama instance;
  `task cli:test` starts it before Maven with `OLLAMA_ENTER=false`.
- Live local Ollama tests must not pull, download, create, or delete models.
- Every provider-calling integration test, including selector-only `*IT` classes,
  must carry a provider category so `test-jvm` can safely skip it under `none`.
- Hosted provider calls require explicit no-cost confirmation and an opt-in task
  or selector. API-key presence alone is not permission to call a hosted provider.

## Assertions

- Assert observable behavior: stdout, stderr, return values, exit codes, rendered
  config, loaded config type, or provider response contract.
- Prefer narrow assertions for LLM responses, such as checking for one expected
  token or phrase, rather than asserting a whole natural-language answer.
- Keep command stdout clean when tests prove CLI output. Logs should remain routed
  to files through the current Logback setup.

## Reporting

Every task result should name:

- The focused command that proved the changed behavior.
- Any broader command that was run afterward.
- Failed or skipped commands with the concrete blocker.
- Timing for provider readiness, first live provider call, native builds, smoke
  tests, and platform startup checks when those are in scope.
