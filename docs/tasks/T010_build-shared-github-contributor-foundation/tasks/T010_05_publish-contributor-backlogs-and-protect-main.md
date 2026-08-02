# T010_05 Publish Contributor Backlogs And Protect Main

Parent: `T010_build-shared-github-contributor-foundation`

Status: finalized

Roadmap: https://github.com/users/codegeist-ai/projects/1

## Goal

Finish the account-wide contributor rollout by publishing real scoped work in every
current source repository, completing discovery metadata, and protecting each
`main` branch with its proven CI gate.

## Scope

- Set accurate descriptions, homepages, and topics for `.github`, `codegeist-ai`,
  `codegeist`, `codegeist-agent-kit`, and `codegeist-devcontainer-kit`.
- Populate the account-level Codegeist Roadmap Project with work from every current
  source repository.
- Add repository, status, area, effort, and contribution-level Project fields.
- Create at least two ready public issues in each current source repository, at
  least eight meaningful issues overall across the three source repositories, and
  at least one realistic `good first issue` in each source repository.
- Create or link the matching repository-local task specification for every issue
  marked `Ready`.
- Use `help wanted` only for sufficiently specified intermediate work.
- Keep security, permission, secret handling, generated-release mutation, and broad
  architecture decisions maintainer-led.
- Protect `main` in every current public source repository after its contributor CI
  passes on a test pull request.
- Require the repository's normal CI check, reject force pushes and branch deletion,
  and avoid impossible self-approval while Codegeist has one maintainer.
- Add a checklist for repeating the full baseline whenever a new public repository
  is created.
- Run a final account-wide public-state and community-profile review.

## Candidate Issue Areas

Confirm each candidate is still unmet before publishing it.

### Codegeist

- Split `CodegeistLocalToolsTest` into focused per-tool test classes.
- Add native-image reflection metadata consistency coverage.
- Add architecture-boundary tests for provider, MCP, chat, and tool packages.
- Add a Maven Wrapper and enforce supported Java and Maven versions.
- Refresh stale provider implementation specifications against current architecture.
- Design maintainer-led permission and workspace-containment behavior.

### Codegeist Agent Kit

- Validate cross-references between commands, rules, skills, plugins, and config.
- Expand release-bundle manifest checks for required and forbidden paths.
- Add focused schema or structural validation for shared OpenCode configuration.
- Improve contributor documentation for generic versus repository-local behavior.
- Audit mutable tool/version references and propose reproducible pins where useful.

### Codegeist Devcontainer Kit

- Add focused shell syntax and generated-release manifest checks.
- Document image-extension, Compose-override, and consuming-repository boundaries.
- Add deterministic tests for release-copy path completeness.
- Audit mutable installer and package inputs and propose reproducibility improvements.
- Improve contributor guidance for browser, Docker, worktree, and environment setup.

The first `good first issue` in each repository should come from documentation,
small deterministic tests, or safe structural validation. Do not label runtime
security policy, release publication, broad image builds, or provider work as
beginner work.

## Metadata Targets

- `.github`: shared Codegeist default community health files and contribution
  intake; it does not render the personal account profile.
- `codegeist-ai`: personal account profile and ecosystem map rendered from the
  repository root `README.md`.
- `codegeist`: open source Java coding agent with CLI/TUI, MCP, local models, and
  native cross-platform releases; homepage `https://codegeist.ai`.
- `codegeist-agent-kit`: shared OpenCode rules, commands, skills, integrations, and
  generated workspace release content.
- `codegeist-devcontainer-kit`: shared reproducible development-container source and
  generated consumer release content.

## Acceptance Criteria

- Every current public repository has a non-empty accurate description, homepage
  where applicable, and focused topics.
- The account Roadmap contains ready work from all current source repositories.
- Each current source repository has at least two ready scoped issues and one
  realistic `good first issue`.
- At least eight issues are open across the three source repositories and no issue
  exists only to meet the count.
- Every ready issue links a canonical local task file, and the task links back.
- Project views can group work by repository and status.
- Each current source repository's `main` requires its proven CI check and rejects
  force pushes and deletion.
- Branch protection does not require self-approval from the sole maintainer.
- Generated `release` branches keep their existing controlled publication workflow
  and are not used for contributor implementation.
- Every source README links its Issue list, the exact account Roadmap, effective
  contribution guide, local task guide, and license.
- `codegeist-ai/codegeist-ai/README.md` renders the personal account profile and
  links the source repositories, exact Roadmap, and default-community repository.
- Both `codegeist-ai/.github` and `codegeist-ai/codegeist-ai` have root canonical
  `0BSD` licenses.
- Community profiles reach the highest practical score for all current repositories.
- The shared future-repository checklist is complete and linked from `.github`.
- T010 parent and child statuses plus relevant project memories match the public
  final state.

## File Targets

- `codegeist-ai/codegeist-ai/README.md`
- `codegeist-ai/.github/README.md` when documenting default-community ownership
- Each source repository's `README.md`
- Each source repository's local task guide
- New repository-local task specifications for ready issues
- Relevant repository memory files
- `docs/tasks/T010_build-shared-github-contributor-foundation/`

## GitHub Targets

- Account-level Codegeist Roadmap Project and views
- Descriptions, homepages, and topics for all three source plus both account
  infrastructure repositories
- Source-repository Issues and labels
- Each current source repository's `main` ruleset
- Community profiles

## Non-Goals

- Do not manufacture broad placeholder issues only to increase issue count.
- Do not centralize every repository's tasks in the core `codegeist` repository.
- Do not mark release, QEMU, hosted-provider, permission, secret, or security-policy
  work as `good first issue`.
- Do not require pull-request approval until a second active reviewer exists.
- Do not enable GitHub Discussions.

## Verification

- Use GitHub UI or authenticated `gh` queries to verify metadata, issue labels,
  Project membership, task links, and branch rules in every current repository.
- Open a disposable test pull request in each source repository before enabling its
  required check.
- Verify that failing CI blocks merge and successful CI permits the sole-maintainer
  workflow.
- Query every community profile and license endpoint.
- Verify the personal profile renders from `codegeist-ai/codegeist-ai/README.md`,
  inherited defaults come from `codegeist-ai/.github`, and both have detected
  `0BSD` licenses.
- Run each source repository's normal check and `git diff --check`.
