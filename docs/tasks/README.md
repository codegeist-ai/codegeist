# Codegeist Task Guide

Repository-local task files preserve implementation detail that does not fit in a
GitHub issue. They are working specifications and historical records, not a
standalone public backlog.

GitHub Mirror: https://github.com/codegeist-ai/codegeist

Read [`CONTRIBUTING.md`](../../CONTRIBUTING.md) before starting implementation.
Codegeist also uses the account-wide
[Code of Conduct](https://github.com/codegeist-ai/.github/blob/main/CODE_OF_CONDUCT.md),
[Security Policy](https://github.com/codegeist-ai/.github/blob/main/SECURITY.md),
and [Support Guide](https://github.com/codegeist-ai/.github/blob/main/SUPPORT.md).
The T010 account rollout is complete. Shared policy, CI, repository metadata,
private reporting, and branch-protection settings are published and verified.

## Statuses

- `open` means the task still has unresolved scope or implementation work. It is
  not automatically ready for an external contributor.
- `in progress` means implementation is actively underway.
- `solved`, `completed`, `implemented`, and `finalized` are historical completion
  terms already used by this repository. They all mean the described work is not
  available as new work.
- `deferred` means the work was intentionally postponed and needs a new readiness
  decision before implementation.
- `cancelled` means the task is closed without implementation.
- `backlog` records an idea that has not yet become an accepted implementation
  task.
- Historical plans, research, and solve notes remain useful context even when a
  parent task still says `open`; inspect child statuses and current source before
  assuming any work remains.

New task files should use the smallest status vocabulary that accurately describes
their state. Rewrite stale status text when work changes state rather than treating
old task files as a list of ready issues.

## Public Tracking

The public workflow is:

```text
Codegeist Roadmap -> repository Issue -> repository task file -> branch -> PR -> merge
```

- The [Codegeist Roadmap](https://github.com/users/codegeist-ai/projects/1) is the
  cross-repository planning view.
- [Codegeist Issues](https://github.com/codegeist-ai/codegeist/issues) own public
  discovery, discussion, priority, assignment, and status for this repository.
- A local task file owns accepted implementation scope, acceptance criteria, file
  targets, non-goals, and verification.
- Every issue marked ready for implementation should link its canonical task path.
- Every publicly tracked task should replace `pending issue creation` with the full
  GitHub issue URL.
- Every accepted non-backlog task and child task receives one GitHub Issue. Its
  implementation uses one task branch and one pull request.
- Automation closes a solved Issue as completed before opening the PR, waits for
  required checks, squash-merges the PR, synchronizes GitHub and Gitea `main`, and
  deletes the task branch from both hosts and locally.
- Cancellation closes the Issue as not planned and records the cancelled task
  state through a small automated PR before the same synchronization and cleanup.
- `/save` may create the deterministic task branch from the current branch. When
  implemented work has no unique task yet, it first asks whether to create a
  retrospective task; accepting creates both the local task and mandatory Issue
  before publication continues.

Backlog ideas remain local until promoted to accepted tasks. Do not mirror an
entire task specification into an issue body, and do not advertise historical or
deferred task records as ready work.

## Current Contributor Foundation Work

- `T010_build-shared-github-contributor-foundation/` is the finalized account-wide
  rollout tracked by
  [codegeist-ai/codegeist#2](https://github.com/codegeist-ai/codegeist/issues/2).
  Shared policy and profile repositories, the Roadmap, source-repository pull
  request checks, licensed kit releases, metadata, private vulnerability reporting,
  branch protection, and the final community-profile audit are complete.

No contributor task is currently advertised as ready. New public tasks should be
created only after maintainers accept concrete repository work, not to populate the
Roadmap or meet an issue-count target.
