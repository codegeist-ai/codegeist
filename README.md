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
- lightweight project memory in `docs/memory-bank/chat.md`

## Development Environment

The checked-in devcontainer is the current development workspace.

Key properties:

- custom Docker image and entrypoint
- Docker available inside the workspace container
- Node.js, Python, GitHub CLI, and supporting CLI tooling
- an `.opencode/` submodule that tracks the agent kit `release` branch
- a `.devcontainer/` submodule that tracks the devcontainer kit `release` branch
- local runtime values in root `.local.env`, generated from the kit example when
  missing and ignored by Git
- root `compose.local.yml` for local compose overrides included by
  `.devcontainer/devcontainer.json`

## Repository Layout

- `.devcontainer/` - development container image and runtime setup from `codegeist-devcontainer-kit`
- `app/codegeist/` - Spring Boot bootstrap application, Maven project files, and local `Taskfile.yml`
- `docs/memory-bank/chat.md` - lightweight project memory for the repository
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
2. Open the repository root in VS Code and choose `Reopen in Container`, or run
   `devcontainer up --workspace-folder .` from the repository root.
3. Let `.devcontainer/initialize.sh` create root `.local.env`,
   `compose.local.yml`, and the generated compose overlay when they are missing.
4. Verify that `java -version` and `native-image --version` work inside the workspace.
5. Run `task -t app/codegeist/Taskfile.yml run` from the repo root, or `task run` inside `app/codegeist/`.
6. Run `java -jar app/codegeist/target/codegeist.jar help` to verify the built-in shell commands are available.

If the repository was cloned without `--recurse-submodules`, Git does not let the
repository force that clone behavior afterward. Run
`git submodule update --init --recursive` before opening the devcontainer.

## Git Worktrees

This repository uses standard Git worktrees under `.worktrees/<branch>`.

Recommended workflow:

1. Keep `main` checked out in the repository root.
2. Open the repository root directly through VS Code Dev Containers.
3. To open a managed worktree, start VS Code or the Dev Containers CLI with
   `BRANCH=<branch>` in the environment. The kit's `initializeCommand` creates
   or reuses `.worktrees/<branch>` and mounts it as `/workspace`.
4. Keep root `.local.env` in the repository root; managed worktrees link back to
   it automatically when `.devcontainer/initialize.sh` prepares them.

The devcontainer kit generates `.devcontainer/.gen.env` and
`.devcontainer/compose.local.gen.yml` on startup. These files keep the container
hostname, user, UID, and GID aligned with the selected checkout without a
repo-local launcher script.

Each worktree uses the `.devcontainer/` files from its own Git state. If you
change the devcontainer setup in the repository root and want the same setup in
an existing worktree, update that worktree to the newer commit first.

If an older checkout is missing nested submodules, initialize them with
`git submodule update --init --recursive` before opening the devcontainer.

## Status

The repository is still early, but it now has a real application entrypoint and
an end-to-end local build/run workflow in the devcontainer. The next logical
step is expanding the shell application beyond the built-in commands and
wiring the first native-image task into the repo workflow.
