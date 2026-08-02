# T010_03 Add Repository Checks And Pull Request CI

Parent: `T010_build-shared-github-contributor-foundation`

Status: finalized

Roadmap: https://github.com/users/codegeist-ai/projects/1

## Goal

Give contributors one fast local validation command per source repository and run
the same repository-appropriate contract automatically for pull requests and normal
`main` pushes.

## Current Problems

- `codegeist` has only release CI. Its existing module `task test` runs
  `ollama-start` before Maven even when live provider tests are skipped.
- `codegeist-agent-kit` has a focused `task test` release-bundle check but no GitHub
  Actions workflow.
- `codegeist-devcontainer-kit` has no public GitHub Actions workflow and needs an
  explicitly documented fast source/release validation entrypoint.
- All three current `main` branches are unprotected.
- `codegeist-ai/.github` and `codegeist-ai/codegeist-ai` are account
  community/profile infrastructure, not source repositories; this task does not
  invent build checks or ready implementation backlogs for them.

## Scope

- Audit each source repository's existing test and release entrypoints before adding
  wrappers.
- Add or document one normal fast check in each source repository.
- Add `.github/workflows/ci.yml` to `codegeist`, `codegeist-agent-kit`, and
  `codegeist-devcontainer-kit`.
- Configure every workflow with minimal permissions, dependency caching where
  useful, no project secrets, and repository-specific failure stages.
- Run CI for pull requests and pushes to `main`.
- Keep release workflows separate from normal contributor CI.
- Validate generated release bundle contracts without publishing or pushing a
  release branch.
- Document when a change needs a stronger native, container, release, or provider
  check beyond the normal gate.
- Link the three source repositories and their normal checks from the account
  profile in `codegeist-ai/codegeist-ai/README.md`; keep default policy ownership
  discoverable from `codegeist-ai/.github/README.md` when that overview exists.

## Codegeist Check Contract

- Add module-local `test-jvm` and `check` tasks.
- Expose `task cli:test-jvm` and `task cli:check` from the root.
- Run provider category `none` as a command-local override without Ollama setup,
  Docker, model downloads, or hosted calls.
- Ignore ambient `TEST` in the normal check so it always runs the complete suite.
- Package the JVM JAR, assert its license resource, and require non-empty output
  from the real artifact's `--version` command.
- Keep explicit local/remote provider, native, QEMU, and release checks opt-in.

## Agent Kit Check Contract

- Use the existing `task test` release-copy smoke as the normal repository gate.
- Validate shared rule, command, skill, plugin, config, and release-path references
  without building or pushing the generated `release` branch.
- Keep `task release-build` outside pull-request CI because it creates and pushes a
  release commit.

## Devcontainer Kit Check Contract

- Define or confirm a fast repository-owned test entrypoint.
- Validate shell syntax, required release paths, generated release copy behavior,
  and focused deterministic smoke checks without building the full heavyweight
  devcontainer image unless the changed contract requires it.
- Keep release publication and broad image/runtime smoke separate from normal PR CI.

## Acceptance Criteria

- Every current source repository documents one canonical normal check.
- Each normal check is deterministic, non-interactive, and does not mutate Git
  history or publish releases.
- `codegeist` normal CI makes no local or hosted model call.
- Shared-kit normal CI never pushes `release` or updates consuming gitlinks.
- CI runs for every pull request and push to `main` in all current source
  repositories.
- Workflows use read-only contents permission unless one documented check requires
  more.
- Failure output identifies the local stage a contributor can reproduce.
- Repository docs distinguish normal, focused, broad, release, and environment-heavy
  checks.
- A test pull request proves each workflow before branch protection is enabled.
- Future repository guidance requires a normal check and pull-request CI before
  advertising ready work.
- Account infrastructure repositories are explicitly excluded from source-build CI
  unless they later gain repository-owned executable validation that justifies a
  focused workflow.
- Both `codegeist-ai/.github` and `codegeist-ai/codegeist-ai` retain root canonical
  `0BSD` licenses even though this child task does not add source-build CI to them.

## Common File Targets

- Each source repository's `Taskfile.yml` or existing test entrypoint
- Each source repository's `.github/workflows/ci.yml`
- Each source repository's `README.md` and `CONTRIBUTING.md`
- `codegeist-ai/codegeist-ai/README.md` for source-check discovery
- `codegeist-ai/.github/README.md` for default-community ownership when present
- Relevant test, release, architecture, and memory documentation

## Non-Goals

- Do not force all repositories to use the same language or exact command.
- Do not run native-image, QEMU, paid providers, hosted providers, model downloads,
  full devcontainer image builds, or release pushes in normal CI.
- Do not remove stronger release validation.
- Do not add an unrelated formatter, static-analysis, coverage, or architecture
  stack to every repository in this first gate.

## Verification

- Run each repository's normal check from a clean source checkout.
- Run `codegeist` checks with an invalid `DOCKER_HOST` to prove the normal path does
  not require Docker.
- Build each shared kit's non-publishing test release and inspect its manifest.
- Parse all workflow YAML.
- Open a disposable test pull request in every current source repository and verify
  the workflow reports reproducible stages.
- Run `git diff --check` in every changed repository.
