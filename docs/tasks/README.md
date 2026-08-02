# Codegeist Task Guide

Repository-local task files preserve implementation detail that does not fit in a
GitHub issue. They are working specifications and historical records, not a
standalone public backlog.

Read [`CONTRIBUTING.md`](../../CONTRIBUTING.md) before starting implementation.
Codegeist also uses the account-wide
[Code of Conduct](https://github.com/codegeist-ai/.github/blob/main/CODE_OF_CONDUCT.md),
[Security Policy](https://github.com/codegeist-ai/.github/blob/main/SECURITY.md),
and [Support Guide](https://github.com/codegeist-ai/.github/blob/main/SUPPORT.md).
The T010 account rollout is still open, so do not assume a shared policy or GitHub
setting has been published merely because its target link is recorded here.

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
- A pull request should link the issue and task, report verification, and close the
  issue when the implementation is complete. Update the task status in the same
  implementation unit when practical.

Ideas do not need a task and issue immediately. Create both when maintainers accept
the work for implementation and need a durable contract. Do not mirror an entire
task specification into an issue body, and do not advertise historical, deferred,
or merely open task records as ready work.

## Current Contributor Foundation Work

- `T010_build-shared-github-contributor-foundation/` is the open account-wide
  rollout. This repository contains its local contributor baseline, but shared
  policy publication, personal account-profile publication, public issues, Roadmap
  items, test pull requests, and branch protection remain outside the completed
  local work.
- `T011_refresh-provider-implementation-specification.md` is a confirmed unmet
  intermediate documentation candidate pending issue creation.
- `T012_add-workspace-tools-configuration-example.md` is a confirmed unmet,
  beginner-safe example/test candidate pending issue creation.
- `T013_add-native-reflection-metadata-consistency-test.md` is a confirmed unmet
  intermediate static-test candidate pending issue creation.

None of these candidates is publicly ready until a maintainer creates and links its
issue and marks it ready in the repository and Roadmap.
