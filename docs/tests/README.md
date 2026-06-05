# Codegeist Test Documentation

Test guidance for Codegeist contributors and coding agents.

## When To Read This

- Read before adding or changing Java tests, smoke scripts, or verification docs.
- Read before reporting task results that include test, smoke, or startup timing.
- Read before changing `app/codegeist/cli/Taskfile.yml` test entrypoints or files
  under `scripts/tests/`.
- Read before adding provider feature tests, especially any method that can call a
  local service or remote provider API.

## Core Rules

- Use the Taskfile from `app/codegeist/cli` for implementation verification.
- Prefer `task test TEST=<selector>` for focused checks and `task test` for the
  broader JVM suite.
- Do not document new direct `mvn test` commands for Codegeist implementation
  tasks unless a task explicitly needs Maven behavior that the Taskfile cannot
  express.
- Provider feature tests run through `task test` and method- or class-level
  provider categories. `CODEGEIST_TEST_PROVIDER_CATEGORY` defaults to `none`, so
  broad verification skips annotated provider calls. `task test` always starts the
  fixed local Ollama service first with `OLLAMA_ENTER=false`; set the category to
  `local` when local provider-call methods should run.
- Hosted provider calls require explicit `remote_free` or `remote_paid` category
  selection. API-key presence alone never enables hosted provider calls.
- Report command duration when a check is slow, platform-specific, or part of a
  smoke-test workflow.
- Keep smoke scripts non-interactive and make their status plus duration easy to
  scan in terminal output.

## Documents

- `provider-feature-tests.md` - provider feature, category, safety, and command
  guidance for config-only, local, `remote_free`, and paid-capable provider checks.
- `codegeist-test-guidelines.md` - Java, Spring, provider, and task-verification
  test conventions.
- `smoke-tests.md` - Linux and Windows smoke-test status and duration-output
  contract.

## Related Files

- `app/codegeist/cli/Taskfile.yml`
- `scripts/tests/native-smoke.sh`
- `scripts/tests/local-linux-smoke.sh`
- `scripts/tests/qemu-windows-vm.sh`
- `scripts/tests/qemu-windows-smoke.sh`
- `scripts/tests/windows-smoke.ps1`
- `docs/developer/specification/testing-strategy-and-agent-rules.md`
