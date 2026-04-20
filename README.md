# codegeist.ai

`codegeist.ai` is a customizable coding agent for the CLI, TUI, and web.

It is being built with a strong focus on customization, adaptable workflows,
and project-local control over behavior, prompts, and developer tooling.

## Vision

- Provide one coding agent experience across CLI, TUI, and web surfaces.
- Make workflows, prompts, and behavior easy to adapt per project.
- Keep configuration and automation close to the repository instead of hiding
  them behind fixed defaults.

## Current Scope

The repository now contains the first runnable application bootstrap for that
vision:

- a compose-based devcontainer setup mounted from the `.devcontainer/` submodule
- a Spring Boot application under `app/codegeist` built in the devcontainer with Java 25 and GraalVM Community 25
- a Spring Shell bootstrap application with the built-in shell commands enabled
- a GraalVM native-image Maven profile prepared for the next build step
- repo-local agent workflow rules, commands, and configuration
- lightweight project memory in `chat.md`

## Development Environment

The checked-in devcontainer is the current development workspace.

Key properties:

- custom Docker image and entrypoint
- Docker available inside the workspace container
- Node.js, Python, GitHub CLI, and supporting CLI tooling
- a tracked `.devcontainer/.env` that `./start.sh` refreshes locally so standard
  git worktrees can resolve repository-root metadata inside the container
- local overrides separated from committed defaults via
  `.devcontainer/.local.env`

## Repository Layout

- `.devcontainer/` - development container image and runtime setup from `codegeist-devcontainer-kit`
- `app/codegeist/` - Spring Boot bootstrap application, Maven project files, and local `Taskfile.yml`
- `chat.md` - lightweight project memory for the repository
- `README.md` - project overview

## Application Bootstrap

The first application milestone is an executable Spring Boot jar that can be
built and started inside `app/codegeist/` with:

```bash
task run
```

From the repository root, the equivalent command is:

```bash
task -t app/codegeist/Taskfile.yml run
```

To build a GraalVM native executable instead, use:

```bash
task native
```

From the repository root:

```bash
task -t app/codegeist/Taskfile.yml native
```

What this does:

1. builds `app/codegeist/target/codegeist.jar`
2. starts the Spring Shell application
3. exposes the built-in shell commands such as `help`, `version`, and `quit`

The native build writes the executable to `app/codegeist/target/codegeist`.

Implementation notes:

- build and run happen directly in the devcontainer with the installed Java 25
  GraalVM toolchain and system Maven
- Java 25 is the current project baseline
- the Maven build already includes a `native` profile with the GraalVM native
  build tools for the later native-image step
- the application currently relies on the built-in Spring Shell commands only
- the runtime configuration lives in `app/codegeist/src/main/resources/application.yaml`

## Getting Started

1. Clone the repository with `git clone --recurse-submodules <repo-url>` so the nested `.opencode` and `.devcontainer` checkouts are available from the start.
2. Create `.devcontainer/.local.env` from `.devcontainer/.local.env.example`.
3. Open the repository root with `./start.sh`; it now opens the checkout
   directly in the repository's devcontainer.
4. Verify that `java -version` and `native-image --version` work inside the workspace.
5. Run `task -t app/codegeist/Taskfile.yml run` from the repo root, or `task run` inside `app/codegeist/`.
6. Run `java -jar app/codegeist/target/codegeist.jar help` to verify the built-in shell commands are available.

If the repository was cloned without `--recurse-submodules`, Git does not let the
repository force that clone behavior afterward. Running `./start.sh` in the
repository root or in a managed worktree repairs the nested `.opencode` and
`.devcontainer` checkouts automatically before it opens the selected checkout in
a devcontainer.

## Git Worktrees

This repository uses standard Git worktrees under `.worktrees/<branch>`.

Recommended workflow:

1. Keep `main` checked out in the repository root.
2. Open the repository root with `./start.sh`.
3. Create or open a managed worktree with `./start.sh <branch>`.
4. Keep `.devcontainer/.local.env` in the repository root; managed worktrees
   link to it automatically when `start.sh` prepares them.

`start.sh` also ensures that the nested `.opencode` and `.devcontainer`
submodules are initialized in the selected checkout. New managed worktrees
therefore get their own usable `.opencode/` and `.devcontainer/` checkouts
automatically, and existing worktrees are repaired if either nested submodule
is still missing.

`start.sh` now opens the selected checkout directly as a VS Code devcontainer
workspace instead of opening a plain host-side folder window first.

The launcher can be started from the host or from inside an already running
devcontainer. In the in-container case it first brings up the target checkout
with the devcontainer CLI and only then opens the matching remote workspace.

`start.sh` injects four runtime variables when it opens VS Code:
`CODEGEIST_REPO_ROOT`, `CODEGEIST_REPO_WORKTREE`, `COMPOSE_PROJECT_NAME`, and
`CODEGEIST_HOSTNAME`. In the repository root the two path variables point to
the same path; in a linked worktree they point to the repository root and the
worktree path. The devcontainer opens `CODEGEIST_REPO_WORKTREE` directly as the
workspace folder and working directory while the extra repository-root mount
still lets Git resolve standard worktree metadata correctly. That also gives
each checkout its own container, network, volume, and hostname.

The hostname stays dot-free so the default Bash prompt shows the full branch-
based name instead of truncating at the first `.`.

The `.devcontainer/.env` file from the checked-out `.devcontainer` submodule now
stays static across checkouts; `start.sh` handles the dynamic runtime values
when it launches VS Code.

Each worktree uses the `.devcontainer/` files from its own Git state. If you
change the devcontainer setup in the repository root and want the same setup in
an existing worktree, update that worktree to the newer commit first.

If an older checked-out `.devcontainer` submodule does not yet contain
`compose.local.yml.example`, `start.sh` writes a minimal local
`compose.local.yml` fallback so the worktree still opens.

## Status

The repository is still early, but it now has a real application entrypoint and
an end-to-end local build/run workflow in the devcontainer. The next logical
step is expanding the shell application beyond the built-in commands and
wiring the first native-image task into the repo workflow.
