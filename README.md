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
- a Spring Boot CLI application under `app/codegeist/cli` built in the devcontainer with Java 25 and GraalVM Community 25
- a Spring Shell `--version` command backed by Spring Boot build metadata
- a GraalVM native-image Maven profile and local native smoke check
- local Linux and Windows smoke scripts under `scripts/tests/`
- a GitHub Actions release workflow for branch validation, pre-tag validation,
  tag-triggered published releases, checksums, and Linux/Windows/macOS native
  smokes
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
- local runtime values in `.codegeist/.local.env`, generated from the kit example
  when missing and ignored by Git
- local Compose overrides in `.codegeist/compose.local.yml`, generated from the kit
  example when missing and ignored by Git

## Repository Layout

- `.devcontainer/` - development container image and runtime setup from `codegeist-devcontainer-kit`
- `app/codegeist/cli/` - Spring Boot CLI bootstrap application, Maven project files, and local `Taskfile.yml`
- `scripts/tests/` - local Linux, Windows QEMU, native, and final smoke-suite scripts
- `docs/memory-bank/chat.md` - lightweight project memory for the repository
- `README.md` - project overview

## Application Bootstrap

The first application milestone is an executable Spring Boot jar that can be
built and started inside `app/codegeist/cli/` with:

```bash
task run
```

From the repository root, the equivalent command is:

```bash
task -t app/codegeist/cli/Taskfile.yml run
```

To build a GraalVM native executable instead, use:

```bash
task native
```

From the repository root:

```bash
task -t app/codegeist/cli/Taskfile.yml native
```

What this does:

1. builds `app/codegeist/cli/target/codegeist.jar`
2. starts the Spring Shell application
3. runs the current noninteractive command path

The native build writes the executable to `app/codegeist/cli/target/codegeist`.

Implementation notes:

- build and run happen directly in the devcontainer with the installed Java 25
  GraalVM toolchain and system Maven
- Java 25 is the current project baseline
- the Maven build includes a `native` profile with the GraalVM native build tools
- the application currently implements `--version` as the only Codegeist-owned
  Spring Shell command
- the runtime configuration lives in `app/codegeist/cli/src/main/resources/application.yaml`

## Local Smoke Tests

Local smoke scripts live under `scripts/tests/`.

Run the local Linux smoke from the repository root:

```bash
scripts/tests/local-linux-smoke.sh
```

Run the final local smoke suite:

```bash
scripts/tests/final-smoke-suite.sh
```

The final suite requires Linux and Windows to pass by default. It downloads the
official Windows Server Evaluation ISO when needed, creates or starts the local
Windows QEMU VM, and fails if download, VM, or smoke prerequisites fail.

For developer-only runs that may skip missing platform prerequisites, use:

```bash
scripts/tests/final-smoke-suite.sh --allow-skips
```

The Windows smoke path uses a local Windows QEMU VM over SSH. See
`docs/developer/release/windows-qemu-smoke.md` for the detailed VM lifecycle,
ISO, toolchain, artifact, and troubleshooting guide.

Native release downloads are planned as platform archives, not true single-file
executables. See `docs/developer/release/native-distribution-packaging.md` for the
Linux `tar.gz`, Windows `zip`, sidecar-library, and no-single-executable rationale.

## GitHub Release Build

The GitHub release workflow lives at `.github/workflows/release.yml`.

It validates release artifacts on GitHub-hosted runners:

- `codegeist-jvm.jar`
- `codegeist-linux-x64.tar.gz`
- `codegeist-windows-x64.zip`
- `codegeist-macos-x64.tar.gz`
- `SHA256SUMS.txt`

Push a versioned iteration branch such as
`release/v0.1.0-github-release-build` to test the workflow without publishing.
When the iteration branch is ready, run
`/codegeist-release --source <release-branch> --rc 1`. The command infers the
next SemVer release from the diff between the latest reachable release tag and the
release branch commit, creates one detailed squash-candidate commit, validates the
candidate branch, advances `main` by fast-forward only, runs pre-tag validation,
pushes the final `v*` tag that publishes the GitHub Release, verifies the
downloaded checksums, moves `latest` to the verified release commit, and creates
or updates the `latest` GitHub Release with the same verified assets without
running another build.

See `docs/developer/release/github-release-build.md` for the full operator flow.

## Getting Started

1. Clone the repository with `git clone --recurse-submodules <repo-url>` so the nested `.opencode` and `.devcontainer` checkouts are available from the start.
2. Open the repository root in VS Code and choose `Reopen in Container`, or run
   `devcontainer up --workspace-folder .` from the repository root.
3. Let `.devcontainer/initialize.sh` create `.codegeist/.local.env`,
   `.codegeist/compose.local.yml`, and the generated compose overlay when they are
   missing.
4. Verify that `java -version` and `native-image --version` work inside the workspace.
5. Run `task -t app/codegeist/cli/Taskfile.yml run` from the repo root, or `task run` inside `app/codegeist/cli/`.
6. Run `java -jar app/codegeist/cli/target/codegeist.jar --version` to verify the current command path.

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

The repository is still early, but it now has a real application entrypoint, an
end-to-end local build/run workflow in the devcontainer, local Linux and Windows
smoke-test entrypoints, and GitHub-hosted release automation for the current
`--version` artifact contract.
