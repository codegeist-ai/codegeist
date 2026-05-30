# T006_04 Implement Codegeist YML Loading

Status: open

Parent: `../task.md`

## Goal

Implement focused loading and validation for `codegeist.yml` in the existing
Spring Boot CLI module.

This child task should make Codegeist understand provider configuration structure
without performing provider calls.

## Scope

- Work in `app/codegeist/cli`.
- Add only the dependencies needed for Spring Boot YAML/config binding if current
  dependencies are insufficient.
- Load a project-level `codegeist.yml` from the working directory or an explicitly
  documented location.
- Bind the schema designed in `T006_01` with `kebab-case` keys.
- Validate model reference shape such as `provider-id/model-id`.
- Validate that referenced providers and models exist in the loaded config.
- Produce clear user-facing errors for invalid config.
- Add focused tests for successful binding and validation failures.
- Update current-state architecture if source, config, or tests change.

## Non-Goals

- Do not add Spring AI provider starters here unless a binding test needs a Spring
  AI type, which should be avoided.
- Do not call Ollama or remote provider APIs.
- Do not create an encrypted credential store.
- Do not add CLI commands beyond the smallest diagnostic needed to verify config
  loading, if any.
- Do not create broad runtime provider abstractions before the loader tests need
  them.

## Acceptance Criteria

- A focused test proves a valid `codegeist.yml` provider config loads.
- A focused test proves invalid model references are rejected with a clear error.
- A focused test proves raw secret values are not required for binding.
- The implementation uses the smallest Java/Spring shape needed by the tests.
- `docs/developer/architecture/architecture.md` reflects the implemented config
  loader behavior.

## Verification

Start with a focused Maven selector from `app/codegeist/cli`, then run the broader
test suite:

```bash
mvn --batch-mode --no-transfer-progress -Dtest=<focused-config-test> test
mvn --batch-mode --no-transfer-progress test
git --no-pager diff --check
```

Replace `<focused-config-test>` with the actual test class introduced by the task.

## Planning Notes

- Read `docs/developer/specification/java-generation-guidance.md` before adding
  Java source.
- Read `docs/developer/specification/testing-strategy-and-agent-rules.md` before
  adding tests.
- Keep any diagnostic command machine-readable and log-free on stdout, following
  the current `--version` precedent.
