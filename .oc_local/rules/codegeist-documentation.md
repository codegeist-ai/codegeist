# Codegeist Documentation Workflow

Use this rule when creating or updating Codegeist user documentation, developer
documentation, documentation preview artifacts, or docs-related Taskfile entries.

## User Documentation

- Keep committed user documentation under `docs/user/` in English.
- Keep current behavior separate from future publishing plans.
- Link new user guides from `docs/user/README.md` in the same task.

## Documentation Preview Tasks

- From the repository root, use `task cli:docs` as the current local documentation
  preview gate. From `app/codegeist/cli`, use `task docs`.
- The current docs task runs the native TUI capture smoke because the TUI user guide
  depends on generated local preview artifacts.
- There is no static documentation-site build in the repository yet; do not imply one
  unless a future task adds it.

## TUI Capture Artifacts

- Use `task cli:tui-capture-smoke` from the repository root, or `task
  tui-capture-smoke` from `app/codegeist/cli`, when TUI documentation needs fresh
  preview captures.
- The capture path must use the native `codegeist tui` command, not the JVM jar.
- The capture script needs Charmbracelet VHS plus its runtime tools: `vhs`,
  `ffmpeg`, and `ttyd` must be on `PATH`; the shared `.devcontainer` release kit
  provides them after a rebuild.
- Generated TUI preview artifacts stay under
  `app/codegeist/cli/target/smoke-test/tui-capture/` until a future task explicitly
  promotes selected assets into committed documentation paths.
- Do not add or resurrect a generic `task tui-smoke`; use the documentation-specific
  `tui-capture-smoke` name for this workflow.
- Do not create `docs/user/assets/` or copy generated images into committed docs
  unless the user explicitly asks for published documentation assets.

## Verification

- For TUI documentation changes, run `task cli:docs` when native build and VHS
  prerequisites are available. If native build or VHS prerequisites are missing,
  report the concrete blocker and leave generated artifacts out of the commit.
- Keep `docs/tests/README.md`, `docs/tests/smoke-tests.md`, and
  `docs/developer/architecture/architecture.md` synchronized when docs-related smoke
  entrypoints change.
