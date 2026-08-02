# T010_02 Define Account-Wide Planning And Task Linkage

Parent: `T010_build-shared-github-contributor-foundation`

Status: finalized

Roadmap: https://github.com/users/codegeist-ai/projects/1

## Goal

Define one public contribution workflow across Codegeist GitHub repositories while
keeping repository-local task files as the primary implementation specifications.

## Workflow Contract

```text
Account Project -> repository Issue -> canonical local task file -> branch -> PR -> merge
```

- The account-level Codegeist Roadmap Project owns the public cross-repository view.
- GitHub Issues own public discovery, discussion, priority, assignment, and status
  inside the repository that owns the change.
- The matching repository's canonical task file under `docs/tasks/` owns
  implementation scope, acceptance criteria, file targets, non-goals, and
  verification. Flat `TNNN_slug.md` and nested `TNNN_slug/task.md` forms are both
  supported.
- Every ready implementation issue links the repository and task path.
- Every publicly tracked task records the full issue URL or repository plus number.
- Pull requests link the issue and task and report relevant verification.
- Cross-repository work uses one coordinating Project item and explicitly linked
  repository issues/tasks; it does not hide implementation work in another repo.
- Merging a pull request closes its issue and updates the local task status in the
  same implementation unit when practical.

## Scope

- Add default bug and feature issue forms, issue configuration, and pull-request
  template to `codegeist-ai/.github`.
- Add repository-specific issue or PR templates only when the shared default cannot
  describe a real local requirement.
- Add or update `docs/tasks/README.md` in every repository that publishes ready
  implementation work.
- Explain active, solved/finalized, deferred, cancelled, backlog, and historical task
  states.
- Define a small shared label taxonomy for area, readiness, effort, security,
  `good first issue`, and `help wanted`; apply labels independently in each repo.
- Create one account-level public `Codegeist Roadmap` Project with repository,
  status, effort, contribution level, and area fields.
- Use Project iterations or repository-specific milestones when useful; do not treat
  milestones as account-wide because GitHub milestones belong to one repository.
- Link the Roadmap, repository Issues, effective contribution guide, and local task
  guide from each source README.
- Record the future-repository onboarding checklist in the shared `.github`
  repository.
- Link the exact Codegeist Roadmap, the three source repositories, and the
  `codegeist-ai/.github` community-policy repository from the root profile
  `README.md` in `codegeist-ai/codegeist-ai`.
- Treat `.github` and `codegeist-ai` as account community/profile infrastructure;
  they need implementation issues or local task guides only when work belongs to
  those repositories themselves.

## Acceptance Criteria

- Shared issue forms and the pull-request template are inherited by repositories
  without local overrides.
- Security reports are directed away from public issues.
- Each source repository documents that GitHub is public source number one for
  priority/status and its canonical local task file is the primary implementation
  source.
- The task guide defines when an idea needs an issue, when an issue needs a task, and
  how Project, issue, task, and PR states stay synchronized.
- Existing historical task records are not presented as ready public work.
- The account-level Project can filter and group work by repository.
- A sample ready issue/task pair in each current source repository proves
  bidirectional links before public backlog publication.
- A cross-repository sample proves that coordinating links do not replace local
  implementation tasks.
- Shared labels have the same meaning across repositories without requiring every
  repository to use irrelevant area labels.
- The personal account profile renders from `codegeist-ai/codegeist-ai/README.md`
  and links the exact Roadmap plus all current source repositories.
- Both `codegeist-ai/.github` and `codegeist-ai/codegeist-ai` retain root canonical
  `0BSD` licenses as account infrastructure repositories.
- Durable workflow decisions are recorded in the relevant repository memory or
  contributor documentation.

## Repository File Targets

- `.github/ISSUE_TEMPLATE/bug.yml`
- `.github/ISSUE_TEMPLATE/feature.yml`
- `.github/ISSUE_TEMPLATE/config.yml`
- `.github/PULL_REQUEST_TEMPLATE.md`
- `codegeist-ai/.github/README.md` when a repository overview is useful
- `codegeist-ai/codegeist-ai/README.md` for the personal profile and ecosystem map
- Each source repository's `README.md`
- Each source repository's `CONTRIBUTING.md` override when needed
- Each source repository's `docs/tasks/README.md`
- Relevant repository memory files

## GitHub Targets

- Account-level Codegeist Roadmap Project
- `codegeist-ai/.github` default-community repository
- `codegeist-ai/codegeist-ai` personal profile repository
- Common repository labels
- Repository-specific milestones only where useful
- One sample issue/task pair per source repository
- One sample cross-repository relationship

## Non-Goals

- Do not mirror full task specifications into issue bodies.
- Do not store every repository's implementation tasks centrally in `codegeist`.
- Do not require task IDs to be globally unique; links must include repository and
  path.
- Do not add a custom synchronization bot in the first rollout.
- Do not enable GitHub Discussions.
- Do not make Discord the source of truth for roadmap or implementation decisions.

## Verification

- Open the new-issue and pull-request flows in each current source repository.
- Verify shared inheritance and every justified local override.
- Inspect Project repository/status fields and sample relationships.
- Confirm all sample issue/task links in both directions.
- Run each changed repository's documentation checks and `git diff --check`.
