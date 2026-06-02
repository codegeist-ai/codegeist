# Codegeist Test Documentation

Test guidance for Codegeist contributors and coding agents.

## When To Read This

- Read before adding or changing Java tests, smoke scripts, or verification docs.
- Read before reporting task results that include test, smoke, or startup timing.
- Read before changing `app/codegeist/cli/Taskfile.yml` test entrypoints or files
  under `scripts/tests/`.

## Core Rules

- Use the Taskfile from `app/codegeist/cli` for implementation verification.
- Prefer `task test TEST=<selector>` for focused checks and `task test` for the
  broader JVM suite.
- Do not document new direct `mvn test` commands for Codegeist implementation
  tasks unless a task explicitly needs Maven behavior that the Taskfile cannot
  express.
- Keep ordinary `task test` free from live provider prerequisites. Live provider
  tests must stay behind explicit selectors.
- Report command duration when a check is slow, platform-specific, or part of a
  smoke-test workflow.
- Keep smoke scripts non-interactive and make their status plus duration easy to
  scan in terminal output.

## Documents

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
