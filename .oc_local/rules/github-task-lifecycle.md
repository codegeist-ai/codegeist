# GitHub Task Lifecycle

Use this rule for every Codegeist `/task` and `/save` invocation. It is the
repository-specific mandatory overlay for the shared task and save workflows.

## Purpose

Every accepted Codegeist task must be traceable through one GitHub Issue, one
task branch, and one merged pull request. GitHub is the review and merge target;
the Gitea `origin` remains a synchronized source remote. The local task file
remains the source of truth for scope, acceptance criteria, status, and
verification.

## Fixed Repository Contract

- GitHub repository: `https://github.com/codegeist-ai/codegeist`.
- Base branch: `main`.
- Gitea source remote: `origin`.
- `docs/tasks/README.md` must retain the exact declaration
  `GitHub Mirror: https://github.com/codegeist-ai/codegeist`.
- Use `GH_TOKEN` as the only GitHub credential. Before every GitHub mutation,
  require it to be non-empty and validate it with a read-only `gh api user`
  request using `GH_HOST=github.com` and `GH_PROMPT_DISABLED=1`.
- For GitHub HTTPS fetches and pushes, use `gh auth git-credential` as a
  command-scoped credential helper backed by `GH_TOKEN`; do not persist GitHub
  credentials or reconfigure the user's global helper.
- Use the already configured non-interactive credentials for Gitea. Never print,
  inspect, or persist either host's token.
- Never weaken or bypass GitHub branch protection as part of this lifecycle.

## Authorization And Precedence

- In this repository, public tracking is mandatory rather than optional. This
  rule replaces the shared optional-activation and per-Issue preview gates for
  `/task spec`, `/task impl`, and `/task cancel`.
- Direct invocation of `/task spec`, `/task impl`, or `/task cancel` authorizes
  the Issue and branch mutations explicitly assigned to that action below.
- Direct invocation of `/task impl` authorizes its implementation commit, branch
  pushes, Issue completion, pull-request creation, check wait, squash merge,
  synchronization, and branch cleanup when verification succeeds.
- Direct invocation of `/save` authorizes deterministic task-branch creation for
  an already resolved task and the same publication lifecycle. When no unique
  task exists, `/save` must obtain one focused approval before creating a
  retrospective task and its mandatory Issue; that approval also authorizes the
  matching task branch. Do not ask for additional confirmations afterward.
- `/task backlog` remains local. It creates no Issue, branch, commit, or PR.
- This standing authorization applies only to the exact resolved task and its
  deterministic branch. It does not authorize unrelated repository, Issue, PR,
  branch-protection, release, or history-rewrite changes.

## Identity And Naming

- Every non-backlog task and child task must have one immutable `Tracking Key`
  and one full GitHub Issue URL in `Public Tracking`.
- Use the hidden Issue marker
  `<!-- canonical-task-key: <tracking-key> -->` and the bounded canonical task
  link defined by the shared `/task` workflow. Search all Issues for the marker
  before creating anything; reuse exactly one valid match and block on ambiguity.
- Use Issue title `[<task-id>] <task title>`.
- Use branch `task/<lowercase-task-id>-<task-slug>`. Normalize the existing task
  slug to lowercase ASCII kebab-case and do not invent alternate branches on
  retries.
- The PR title must follow the repository Conventional Commit rule. The PR body
  must link the Issue and canonical task path and list the verification commands.
  Include the canonical task key marker. Use an Issue reference, not an automatic
  `Closes` keyword, because completion closes the Issue before PR creation.

## `/task spec`

1. Inspect the worktree, refuse to start a second specification when unrelated
   changes exist, then create or update the canonical local task file. One
   uncommitted target task document may carry forward into its `impl` branch.
2. Resolve and validate the fixed GitHub repository and `GH_TOKEN` contract.
3. Reconcile by Tracking Key across all Issues, including closed Issues and
   excluding pull requests.
4. Reuse one valid linked Issue or create it immediately and non-interactively
   with the one-sentence Goal plus canonical-link block. Do not show a preview or
   ask for another approval.
5. Read the Issue back, require one exact valid marker match, then persist its full
   URL in `Public Tracking`. Keep the task `blocked` when creation or read-back is
   uncertain; retries must reconcile before creating again.
6. Do not create the task branch during `spec`.

Apply the same behavior to top-level and child tasks. Existing non-backlog tasks
without an Issue must be linked automatically the next time `spec`, `impl`, or
task-aware `save` touches them. Historical completed tasks do not need bulk
backfill unless they are reopened.

## `/task impl`

1. Require a clean worktree except for the resolved task file created or updated
   by the immediately preceding specification step. Stop on unrelated changes.
2. Ensure the Issue exists and passes the exact linkage validation above.
3. Refresh GitHub `main`, Gitea `origin/main`, and local `main`. Continue only when
   both remote base refs are identical and local `main` can be updated by
   fast-forward. Never merge divergent base branches or rewrite either `main`.
4. Create or reuse the deterministic task branch from that synchronized `main`.
   A reused branch must belong to the same task and Tracking Key.
5. Mark the local task `in progress`, then implement and verify only that task.
6. Run the shared learn and submodule-refresh steps, include their relevant task
   changes, and repeat affected verification when they change the task diff.
7. After successful verification, automatically execute the task publication
   lifecycle below. `/task impl` must not stop merely to suggest `/save`.

## Task-Aware `/save`

- Require an attached `HEAD`, inspect the current branch, active chat, changed
  paths, commits since the `main` merge base, task files, and intended save scope,
  then resolve exactly one local task. A matching task branch or one changed
  canonical task file is strong identity; do not guess from a coincidental task
  id or similar title.
- When no task resolves, or existing tasks do not uniquely cover the complete
  intended diff, propose one concise retrospective task title, Goal, and file
  scope and ask whether to create it. This is the only extra approval in the
  automated save path. If declined, stop without a commit, Issue, branch, or
  remote mutation.
- After approval, allocate the next unused task id from files and Git history,
  create the canonical task document with a random immutable Tracking Key, and
  create and validate its GitHub Issue through the mandatory `/task spec` linkage
  rules. Implementation preceding the task record is valid in this retrospective
  path. Never reuse an existing id or combine unrelated changes merely to avoid a
  second task.
- For an existing resolved task without a valid Issue URL, create and validate
  its mandatory Issue automatically before branch creation. If Issue creation or
  read-back is uncertain, mark the task `blocked` and stop before committing.
- Derive the deterministic `task/<id>-<slug>` branch after task and Issue identity
  are valid. When already on that exact branch, continue. Otherwise create it at
  the current `HEAD` and switch to it while preserving the intended uncommitted
  changes. This is allowed from `main` and from another attached branch.
- Reuse an existing local or remote deterministic branch only when its task id,
  Tracking Key, and Issue match exactly. Block on conflicting ownership instead
  of creating an alternate branch name.
- Branching from the current `HEAD` does not make that point the final merge base.
  After committing, refresh both remote `main` refs and rebase the task branch
  onto their synchronized `main` as required by the publication lifecycle.
- Run the shared learn and submodule-refresh steps before committing, but keep all
  changes scoped to the resolved task.
- After verification, use the publication lifecycle below instead of the shared
  direct-push or base-branch save paths. Never push task work directly to `main`.

## Publication Lifecycle

This lifecycle is used by successful `/task impl`, task-aware `/save`, and the
cancel path where explicitly noted.

1. Revalidate the Issue, task path, task id, Tracking Key, branch name, clean
   scope, and both current remote `main` SHAs. Require GitHub and Gitea `main` to
   identify the same commit before completion starts.
2. For solved work, close the Issue with `gh issue close --reason completed` and
   read it back before writing local status `solved`. For cancellation, close it
   with `gh issue close --reason "not planned"`, require API state reason
   `not_planned`, and write status `cancelled` plus the cancellation reason.
3. Record verification in the task file, stage only task-related changes, and
   create a focused Conventional Commit through the repository commit-message
   guard. If commit creation fails after Issue closure, reopen the Issue and keep
   the task blocked.
4. Rebase the task branch onto the still-current synchronized `main` when needed.
   If the rebase changes commits already pushed for this task, fetch first and use
   `--force-with-lease` only for the task branch.
5. Push the exact task branch to Gitea `origin` and to the fixed GitHub repository.
   Verify both branch refs equal local HEAD.
6. Search PRs in all states by exact head branch and canonical task key before
   creation. Reuse the sole matching open PR, accept a matching merged PR as a
   recovery state, or reopen the sole matching closed-unmerged PR when safe.
   Create only when none exists. Block on multiple or conflicting matches. The
   Issue, task path, Tracking Key, head, and base must all match.
7. Poll for a bounded maximum of ten minutes until GitHub reports at least one
   required check, then wait non-interactively with `gh pr checks --required
   --watch --fail-fast`. A failed, cancelled, skipped, missing, or timed-out
   required check blocks merging.
8. Squash-merge the PR with `gh pr merge --squash --delete-branch`. Do not use a
   merge commit, direct protected-branch push, admin bypass, or force push to
   `main`.
9. Read the PR back and require `MERGED` state plus a merge commit on GitHub
   `main`. Confirm the Issue remains closed with the expected reason.
10. Fetch GitHub `main`; require Gitea `origin/main` to be an ancestor, then update
    Gitea `main` by normal fast-forward push only. Stop rather than rewrite Gitea
    `main` if it diverged.
11. Update local `main` by fast-forward in the worktree that owns it, without
    disturbing another dirty worktree. Verify local, GitHub, and Gitea `main` all
    identify the same commit.
12. Delete the exact task branch from GitHub, Gitea, and locally only after merge
    and three-way `main` equality are verified. Squash merge means local deletion
    may require `git branch -D`; this is authorized only for the verified merged
    task branch.

If PR creation, checks, merge, or synchronization fails after an Issue was closed,
reopen the Issue, set the local task to `blocked`, preserve the branch and PR for
retry, and report the exact non-secret blocker. Never create a duplicate Issue,
branch, or PR during recovery.

## `/task cancel`

- Ensure the Issue exists and validate linkage.
- Create or reuse the deterministic task branch from synchronized `main` when no
  implementation branch exists.
- Close the Issue with reason `not_planned`, write `cancelled` plus the reason in
  the task file, and run the publication lifecycle through a small cancellation
  PR and normal squash merge.
- Delete the task branch on both hosts and locally only after the cancellation PR
  is merged and both `main` refs are synchronized.

## Safety And Idempotency

- Before every remote mutation, read current state and compare exact repository,
  task key, branch, Issue, PR, and expected SHA values.
- Never use `--force` and never force-push `main`. `--force-with-lease` is limited
  to a previously pushed task branch after its rebase.
- Never disable branch protection, required checks, conversation resolution,
  linear history, or branch deletion/force-push restrictions.
- Never merge while required checks are pending or unsuccessful.
- Never include secrets, raw Issue bodies from unrelated Issues, tokens, or raw
  mirror API responses in output or durable files.
- Report Issue URL, branch, PR URL, check result, merge commit, synchronization
  result, and branch deletion result at completion.
