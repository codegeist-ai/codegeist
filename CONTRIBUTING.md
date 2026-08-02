# Contributing To Codegeist

This guide provides the Java/core ownership, setup, and verification details for
`codegeist-ai/codegeist`. Account-wide contribution policy is maintained in the
[Codegeist community repository](https://github.com/codegeist-ai/.github/blob/main/CONTRIBUTING.md).
The complete ownership, policy, planning, release, and account-repository model is
documented in
[`GITHUB_ACCOUNT_MODEL.md`](https://github.com/codegeist-ai/.github/blob/main/GITHUB_ACCOUNT_MODEL.md).

## Repository Ownership

This repository owns the Java 25 Codegeist core runtime, CLI/TUI application,
provider and MCP integration, local tools, packaging, installers, and user-facing
releases. Core implementation belongs under `app/codegeist/cli` and starts from
this repository's source `main` branch.

Use the other source repositories for shared workspace infrastructure:

- [`codegeist-agent-kit`](https://github.com/codegeist-ai/codegeist-agent-kit)
  owns shared OpenCode rules, commands, skills, plugins, and generated agent-kit
  `release` content.
- [`codegeist-devcontainer-kit`](https://github.com/codegeist-ai/codegeist-devcontainer-kit)
  owns the shared development image, initialization, Compose runtime, and
  generated devcontainer `release` content.

Do not implement shared-kit changes inside this repository's `.opencode` or
`.devcontainer` submodule checkouts. Make the source change and pull request in
the owning kit first; a later Codegeist change can update the pinned gitlink.

## Contributor Setup

Host prerequisites are Git, Docker, and either VS Code with Dev Containers or
the Dev Containers CLI. The normal clone initializes only the two shared
workspace submodules; the large `docs/third-party/*/source` research references
are not needed for ordinary development.

```bash
git clone https://github.com/codegeist-ai/codegeist.git
cd codegeist
git submodule update --init .devcontainer .opencode
devcontainer up --workspace-folder .
devcontainer exec --workspace-folder . task cli:check
devcontainer exec --workspace-folder . task cli:run -- --version
```

VS Code users can choose `Reopen in Container` and run `task cli:check` plus
`task cli:run -- --version` from the container terminal instead.

The devcontainer initialization creates ignored local/generated files such as
`.codegeist/.local.env`, `.devcontainer/.env`,
`.devcontainer/Dockerfile.merged.gen`, and generated Compose bridges. Put
machine-local values in `.codegeist/.local.env`; do not edit generated files.
Create `.codegeist/compose.local.yml` or `.codegeist/Dockerfile` only when an
intentional repository-specific override is needed.

## Checks

The canonical contributor and pull-request check is:

```bash
task cli:check
```

It runs the JVM tests with provider category `none`, packages
`app/codegeist/cli/target/codegeist.jar`, and runs that artifact's real
`--version` command with a non-empty-output assertion. The jar includes the root
license at `META-INF/LICENSE`. It ignores ambient `TEST` and provider-category
values, is deterministic and noninteractive, and requires no Docker daemon,
Ollama service, model download, credentials, or provider call.

Use stronger checks only when the change needs them:

- `task cli:test-jvm TEST=<selector>` runs a focused deterministic JVM test.
- `CODEGEIST_TEST_PROVIDER_CATEGORY=local task cli:test TEST=<selector>` opts
  into local-provider tests and the Taskfile-managed Ollama setup.
- `task cli:mcp-remote-smoke` opts into the Docker/Ollama MCP smoke.
- `task cli:native-smoke` builds and smokes the GraalVM native artifact.
- `task cli:final-smoke-suite` runs the environment-heavy Linux and Windows/QEMU
  suite.

Hosted provider, paid-capable provider, release, and publication checks require
their explicit documented opt-ins. Credentials alone never authorize a provider
call. Report every command run, relevant skips, and concrete blockers in the
pull request.

## Issues, Tasks, And Pull Requests

Use [GitHub Issues](https://github.com/codegeist-ai/codegeist/issues) for public
discovery, discussion, priority, assignment, and status. The
[Codegeist Roadmap](https://github.com/users/codegeist-ai/projects/1) provides the
account-wide view. Once work is accepted and sufficiently scoped, its matching
file under `docs/tasks/` is the primary implementation specification for goal,
acceptance criteria, file targets, non-goals, and verification.

Ready issues link their canonical task file, and publicly tracked task files link
back to the full issue URL. Pull requests should link both, use a closing keyword
for the owning issue when appropriate, describe the implementation, and report
verification. See [`docs/tasks/README.md`](docs/tasks/README.md) before selecting
work; historical task records and an `open` task status do not by themselves mean
the work is ready for an external contributor.

A small unplanned fix may proceed without creating a new local task when a
maintainer confirms that the Issue and pull request provide enough durable scope.
The pull request must state `No local task needed:` and the reason. Always link an
existing task when one already defines the work.

## Contribution Terms

Codegeist-owned source and documentation are licensed under
[0BSD](LICENSE). By submitting a contribution, you agree that your contribution
is provided under the same `0BSD` terms. This project does not require a CLA or
DCO sign-off. Preserve third-party licenses, notices, vendored material, and
submodule ownership.

The effective shared policies are the
[Code of Conduct](https://github.com/codegeist-ai/.github/blob/main/CODE_OF_CONDUCT.md),
[Security Policy](https://github.com/codegeist-ai/.github/blob/main/SECURITY.md),
and [Support Guide](https://github.com/codegeist-ai/.github/blob/main/SUPPORT.md).
Do not report vulnerabilities or credentials in public issues.
