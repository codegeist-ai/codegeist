# T007_04_05 Document And Verify Side Effect Tools

Parent: `T007_04_add-patch-edit-and-shell-tools`

Status: open

## Goal

Close the T007_04 side-effecting tool slice with synchronized architecture docs and
focused verification.

## Scope

- Update architecture docs to describe the implemented exact edit tool, deferred
  structured patch decision, and shell tool behavior.
- Keep smoke documentation aligned with artifact-level side-effect checks, including
  the shared ask-driven file-edit harness used for native encoding validation.
- Verify the parent T007_04 acceptance criteria after implementation children are
  complete.
- Preserve the documented non-goals: no sandbox claim beyond path/cwd/timeout
  checks, no permission loop, no TUI patch review, no background shell, no plugin
  hooks, and no git automation.
- Update `docs/memory-bank/chat.md` if the completed implementation changes future
  session context.

## Acceptance Criteria

- `docs/developer/architecture/architecture.md` and focused architecture docs describe
  current implemented exact edit, deferred patch, and shell behavior.
- Focused tests for edit, shell, and session persistence pass.
- A broad JVM verification command is run or a blocker is documented.
- Parent `T007_04_add-patch-edit-and-shell-tools/task.md` accurately reflects the
  implementation state.

## File Targets

- `docs/developer/architecture/architecture.md`
- `docs/developer/architecture/local-file-tools.md`
- `docs/developer/architecture/edit-tool.md`
- `docs/tests/smoke-tests.md`
- `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
- `docs/tasks/T007_build-codegeist-runtime-harness/tasks/T007_04_add-patch-edit-and-shell-tools/task.md`
- `docs/memory-bank/chat.md`

## Verification

Run from `app/codegeist/cli` after focused checks:

```bash
task test
```

## Source Notes

- Depends on `T007_04_02_add-exact-edit-tool.md`, the deferred decision recorded in
  `T007_04_03_add-structured-patch-tool.md`, and
  `T007_04_04_add-shell-tool.md`.
- Keep final docs current-state only; move future permission, TUI, background shell,
  and plugin plans to later tasks if needed.
