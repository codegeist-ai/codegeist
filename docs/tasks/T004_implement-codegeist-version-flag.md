# T004 Implement Codegeist 0.0.1 Version Flag

Status: specified

## Goal

Implement the first tiny Codegeist `0.0.1` CLI behavior: `--version` prints the
current application version and exits.

This is the smallest useful implementation step after removing the
placeholder-first plans. It creates real product behavior without introducing an
agent loop, provider integration, tool registration, or future-facing
architecture placeholders.

## Scope

- Work in the existing Maven module under `app/codegeist/cli`.
- Add support for `--version`.
- Print a concise version string, for example `codegeist 0.0.1`.
- Source the version from the existing project/build or Spring configuration where
  practical so it is not duplicated in multiple code locations.
- Exit cleanly after printing the version.
- Add or update focused tests for the version behavior.
- Keep ordinary Spring context tests passing.

## Non-Goals

- Do not add Ollama, Spring AI provider calls, model configuration, or
  Testcontainers.
- Do not add an interactive agent loop.
- Do not register Spring AI Agent Utils tools.
- Do not add prompt handling, chat memory, system prompts, sessions, events, or
  workspace behavior.
- Do not create placeholder classes, ids, ports, records, enums, package layers,
  validation hierarchies, or empty package directories.
- Do not add release automation, CI workflows, native-image changes, or binary
  smoke checks.
- Do not recreate broad implementation handoff documents under
  `docs/developer/implementation/`.

## Acceptance Criteria

- Running the application with `--version` prints the Codegeist version and exits.
- The version output is deterministic and easy to assert in tests.
- The implementation does not start an interactive prompt or block stdin for the
  `--version` path.
- A focused test covers the version behavior.
- The existing Spring context test still passes.
- `docs/developer/architecture/architecture.md` is updated if source,
  configuration, or test behavior changes.

## Verification

Planned checks from `app/codegeist/cli`:

```bash
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistApplicationTests test
mvn --batch-mode --no-transfer-progress test
```

If a more focused test class or method is added for `--version`, run that selector
first and then the broader test command.

## Planning Notes

- Read `docs/developer/specification/java-generation-guidance.md` before writing
  Java source.
- Read `docs/developer/specification/testing-strategy-and-agent-rules.md` before
  adding or changing tests.
- Prefer direct implementation in the existing application entrypoint unless a
  tiny separate class makes the version behavior easier to test without adding
  architecture weight.
