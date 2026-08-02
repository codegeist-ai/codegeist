# T010 Build Shared GitHub Contributor Foundation

Status: open

Public tracking: [codegeist-ai/codegeist#2](https://github.com/codegeist-ai/codegeist/issues/2)

Roadmap: [Codegeist Roadmap](https://github.com/users/codegeist-ai/projects/1)

## Goal

Make every non-archived public repository owned by the `codegeist-ai` GitHub
account legally, technically, and organizationally ready for external open source
contributors.

GitHub Issues and one account-level Codegeist Roadmap project are the primary
public sources for work discovery, priority, discussion, and status. Each source
repository keeps its own canonical task file under `docs/tasks/` as the
authoritative implementation specification for accepted work, whether that task
uses the flat `TNNN_slug.md` form or nested `TNNN_slug/task.md` form.

The completed foundation should let a contributor understand how the repositories
fit together, select the correct repository, find ready work, run that repository's
fast validation command, and submit a pull request with automatic feedback.

## Repository Scope

The current source repository inventory on 2026-08-02 is:

- `codegeist-ai/codegeist` - Java CLI/TUI coding-agent runtime and user-facing
  releases.
- `codegeist-ai/codegeist-agent-kit` - shared OpenCode rules, commands, skills,
  scripts, plugins, and generated `release` branch content.
- `codegeist-ai/codegeist-devcontainer-kit` - shared devcontainer source and
  generated `release` branch content.

This task also creates two account infrastructure repositories:

- `codegeist-ai/.github` for default community health files inherited by public
  repositories that do not provide local overrides.
- `codegeist-ai/codegeist-ai` for the personal account profile rendered from its
  root `README.md`.

These account repositories provide community and discovery infrastructure. They do
not expand the set of source repositories that require implementation tasks,
normal source checks, ready issue backlogs, or protected source branches.

The rollout must include public repositories added before T010 completes. Future
non-archived public repositories must follow the checklist established by this task.
Forks and vendored mirrors retain their upstream license and contribution posture
unless Codegeist owns original changes that require a clearly separated policy.

## Current Baseline

The 2026-08-02 public GitHub audit found:

- `codegeist` has a 14 percent community profile, no detected license, one
  release-only workflow, no open issues, and an unprotected `main` branch.
- `codegeist-agent-kit` has a 14 percent community profile, no detected license, no
  GitHub Actions workflow, no open issues, and an unprotected `main` branch.
- `codegeist-devcontainer-kit` has a 28 percent community profile, no detected
  license, no GitHub Actions workflow, no open issues, and an unprotected `main`
  branch.
- No public `codegeist-ai/.github` default-community repository exists.
- No public `codegeist-ai/codegeist-ai` personal profile repository exists.
- Repository descriptions, homepages, topics, contribution entrypoints, issue
  intake, and public roadmap coverage are incomplete or inconsistent.

The source repositories already provide useful implementation foundations:

- `codegeist` has detailed architecture, tests, a devcontainer, native releases,
  and install scripts.
- `codegeist-agent-kit` exposes `task test` for its release bundle contract.
- Both shared kits use source `main` and generated `release` branches consumed as
  submodules.

## Decisions

- Apply the contributor foundation to all current and future non-archived public
  repositories under `codegeist-ai`, not only the core Java repository.
- Create a public `codegeist-ai/.github` repository for shared community policies,
  issue forms, and pull-request defaults.
- Create a separate public `codegeist-ai/codegeist-ai` repository whose root
  `README.md` is the personal account profile and public ecosystem map.
- Add a root `LICENSE` to every repository containing Codegeist-owned material;
  do not rely on inherited community files for license detection.
- License Codegeist-owned source and documentation under the OSI-approved Zero
  Clause BSD License, SPDX identifier `0BSD`.
- Accept external contributions under the same `0BSD` terms without a CLA or DCO
  requirement in this task.
- Preserve all third-party licenses, notices, submodules, and vendored content.
- Use shared community defaults where the policy is genuinely account-wide and
  repository-local overrides where build, test, release, security, or ownership
  details differ.
- Use one account-level GitHub Project for public planning across repositories.
- Keep each ready issue's canonical repository-local task file as the primary
  implementation contract.
- Require a repository-appropriate fast pull-request check in every source
  repository; do not force unrelated repositories to use the same build command.
- Protect each public source repository's `main` branch after its required check is
  proven.
- Treat generated `release` branches as distribution outputs, not contributor
  implementation branches.
- Keep GitHub Discussions disabled initially. Use Issues for tracked work and
  Discord for informal help.

## Child Tasks

- `tasks/T010_01_add-0bsd-and-community-policies.md` - create shared community
  defaults and apply the license and required local policy details to every public
  repository.
- `tasks/T010_02_define-public-planning-and-task-linkage.md` - define one
  account-level Issue, Project, repository-task, and pull-request relationship.
- `tasks/T010_03_add-repository-checks-and-pull-request-ci.md` - add a fast local
  check and pull-request CI to each source repository.
- `tasks/T010_04_fix-onboarding-and-add-repository-examples.md` - explain the
  repository map, correct onboarding, and add repository-specific examples.
- `tasks/T010_05_publish-contributor-backlogs-and-protect-main.md` - publish work
  across repositories, complete metadata, and protect each `main` branch.

## Parent Acceptance Criteria

- The public `codegeist-ai/.github` repository exists and provides default
  contribution, conduct, security, support, issue, and pull-request files.
- The public `codegeist-ai/codegeist-ai` repository exists and its root `README.md`
  renders the personal account profile and ecosystem map.
- Both account infrastructure repositories have a root canonical `0BSD` license.
- Every current non-archived public repository containing Codegeist-owned material
  has a root canonical `0BSD` license.
- Every distributable source or generated release bundle carries the applicable
  license without deleting third-party notices.
- Each repository either inherits the shared community files or provides a justified
  repository-specific override linked from its README.
- Security and conduct policies use one confirmed private reporting channel and
  explain any repository-specific trust boundary.
- One public Codegeist Roadmap project spans all source repositories and includes a
  repository field plus `Backlog`, `Ready`, `In Progress`, `In Review`, and `Done`.
- Every repository that exposes ready implementation work has a local task guide and
  bidirectional links between ready issues and canonical task files.
- `codegeist` provides `task cli:check` without Docker, Ollama, credentials, model
  downloads, or hosted provider calls.
- `codegeist-agent-kit`, `codegeist-devcontainer-kit`, and future source
  repositories expose and document their own fast normal check.
- Pull requests and pushes to `main` run repository-appropriate CI in every current
  source repository.
- Each current public source repository's `main` requires its proven CI check and
  rejects force pushes and branch deletion.
- Contributor-facing documentation explains repository ownership, source versus
  generated release branches, cross-repository changes, and where tasks belong.
- At least two ready public issues exist in each current source repository, with at
  least eight total and at least one realistic `good first issue` per source
  repository.
- Repository descriptions, homepages, and topics are complete and consistent
  across the three source and two account infrastructure repositories.
- All current repository community profiles reach the highest practical completion
  level.
- A reusable checklist defines the contributor baseline for future public Codegeist
  repositories.

## Implementation Order

1. Complete `T010_01` and create the shared `.github` plus personal profile
   repositories before actively inviting contributions anywhere in the account.
2. Complete `T010_02` so all repositories use one public planning and local task
   contract.
3. Complete `T010_03` and prove each source repository's normal check before
   protecting its branch.
4. Complete `T010_04` and verify each source repository from a fresh checkout.
5. Complete `T010_05`, publish ready work across repositories, protect default
   branches, and perform the final account-wide audit.

## Non-Goals

- Do not add new Codegeist runtime, provider, TUI, plugin, server, or agent-loop
  features.
- Do not make every repository use Java, Maven, the same Taskfile target, or the
  same release implementation.
- Do not implement source changes directly on generated `release` branches or in
  consuming submodule checkouts.
- Do not add package-manager publishing, artifact signing, notarization, SBOM, or
  provenance work.
- Do not require QEMU, native-image, container image builds, hosted providers, or
  paid services for normal pull requests unless a repository's focused change truly
  requires one and the task documents it.
- Do not rewrite every historical task or specification in one pass.

## Verification

Each child task owns focused checks in the repository it changes. Parent completion
additionally requires an account-wide audit that confirms:

- the public `.github` defaults are inherited where expected;
- the root `README.md` in `codegeist-ai/codegeist-ai` renders the account profile;
- each repository has local `0BSD` license detection;
- issue and task links include the repository and path and work in both directions;
- each source repository's fast check passes from a fresh source checkout;
- a test pull request receives the required CI check in each source repository;
- the Codegeist Roadmap project contains work from all current source repositories;
- all current `main` branches have the intended protection without impossible
  self-approval requirements;
- private vulnerability and conduct-reporting routes are usable; and
- community profiles and repository metadata are complete.

## Planning Notes

- The candidate private contact is `dev@codegeist.ai`; confirm that the mailbox is
  monitored before publishing it.
- The `.github` repository reduces policy duplication but does not render this
  personal account's profile. Profile content belongs in the root `README.md` of
  `codegeist-ai/codegeist-ai`.
- Neither account repository can provide another repository's license. Both account
  infrastructure repositories and every Codegeist-owned source repository need
  their own root `LICENSE`.
- Shared kit changes must start on each kit's source `main`, pass that repository's
  tests, build its generated `release` branch through the existing workflow, and
  update consuming gitlinks separately.
- Prefer small public issues with observable acceptance criteria and explicit
  repository ownership.
- Treat permission, workspace-containment, secret-redaction, release mutation, and
  other security-sensitive work as maintainer-led rather than beginner work.
