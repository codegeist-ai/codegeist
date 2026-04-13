# Chat Memory

## Goal

- Build `codegeist.ai` as a customizable coding agent for the CLI, TUI, and
  web with strong control over workflows, prompts, and project-local behavior.
- Keep the repository runnable from a clean checkout with minimal host setup.

## Current State

- The repo keeps its compose-based devcontainer under `.devcontainer/` and
  stores lightweight project memory in `chat.md`.
- `start.sh` is now the launcher for opening the repository root or a managed
  worktree directly in the repository devcontainer.
- The devcontainer now opens `CODEGEIST_REPO_WORKTREE` directly as the VS Code
  workspace folder and container working directory while still mounting the
  repository root so linked-worktree Git metadata keeps working.
- The first real application now lives under `app/codegeist` as a Spring Boot
  4.0.3 and Spring Shell 4.0.1 project with system Maven and a prepared
  GraalVM native build profile.
- `app/codegeist/Taskfile.yml` exposes local `test`, `build`, `run`, and
  `native` tasks.
- `.devcontainer/Dockerfile` now installs GraalVM Community 25, `native-image`,
  system Maven, and the local Linux prerequisites needed for future native
  builds.
- The application build and run workflow now executes directly inside the
  devcontainer through the app-local `Taskfile.yml`; the app no longer uses
  Docker-wrapped helper scripts.
- `start.sh` now also repairs a missing nested `.opencode` checkout before it
  opens the selected root or worktree as a devcontainer workspace.
- Builds and tests should be executed directly in the active devcontainer when
  the local toolchain is already available.
- Java 25 is now the fixed project baseline for the application and devcontainer.
- Software documentation is now split by audience: user docs live under
  `docs/user/` and developer docs live under `docs/developer/`, with
  `docs/README.md` as the documentation entry point.
- Commit guidance now prefers one commit per task: include the full
  task-related diff in a single commit instead of split commits.
- Commit format and workflow guidance currently live in
  `@.opencode/rules/commit.md` and
  `@.opencode/rules/commit-conventions.md`.
- The `@.opencode/commands/save.md` workflow now also applies
  `@.opencode/rules/software-documentation.md` and includes required
  `docs/user/` and `docs/developer/` updates in the same task commit.
- Running `task build` creates `app/codegeist/target/codegeist.jar` without
  running tests first; `task test` runs the test suite separately.
- Running `task run` inside `app/codegeist` or
  `task -t app/codegeist/Taskfile.yml run` from the repo root builds the jar
  and starts the Spring Shell application.
- Running `task native` builds a GraalVM executable as
  `app/codegeist/target/codegeist`.
- The app currently relies on the built-in Spring Shell commands only; no
  custom shell commands are implemented yet.
- The project no longer uses PF4J, Maven Wrapper files, Eclipse `.prefs`, or
  a REST bootstrap controller.
- `.gitignore` ignores the generated application output under
  `/app/codegeist/target/` and Eclipse metadata under
  `/app/codegeist/.settings/`.
- Versioned repository files remain English-only, but the direct OpenCode chat
  follows the user's language.

## Open Items

- Add the first project-specific Spring Shell commands beyond the built-in
  defaults when the shell behavior is ready to grow.
