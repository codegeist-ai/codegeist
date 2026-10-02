# T014 Bootstrap Go Project And Automated Task Lifecycle

Status: solved
Public Tracking: https://github.com/codegeist-ai/codegeist/issues/12
Tracking Key: 91b650bb-ff2b-42f1-81b2-c7ba768c6c50

## Goal

Establish the minimal Go application scaffold and repository task commands while
making GitHub Issue, task-branch, PR, merge, Gitea synchronization, and cleanup
mandatory and automated.

## Context

Implementation preceded this retrospective task record. The approved save scope
combines the initial greenfield Go bootstrap with the repository-local workflow
needed to publish accepted work through protected GitHub pull requests while
keeping the Gitea source remote synchronized.

## Scope

- Add the minimal Go module and empty application entrypoint under
  `app/codegeist/go/`.
- Add module-local test, build, and run tasks and expose them through the root
  `go:` Taskfile namespace.
- Add and register the Codegeist-specific GitHub task lifecycle rule.
- Make mandatory Issue creation, deterministic task branches, checked squash PR
  merges, dual-host synchronization, and branch cleanup automatic for `/task`
  and `/save`.
- Document the fixed GitHub mirror and retrospective `/save` behavior.
- Include the shared `.devcontainer` release gitlink refreshed by the save
  workflow.

## Files

- `app/codegeist/go/**`
- `Taskfile.yml`
- `.oc_local/opencode.json`
- `.oc_local/rules/codegeist-task-specification.md`
- `.oc_local/rules/github-task-lifecycle.md`
- `docs/tasks/README.md`
- `docs/developer/architecture/architecture.md`
- `.devcontainer`

## Non-Goals

- Do not implement AI-agent, provider, CLI-command, or TUI behavior in Go.
- Do not migrate Java source or runtime contracts into the Go project.
- Do not bypass GitHub branch protection or required checks.
- Do not rewrite either remote `main` branch.

## Acceptance Criteria

- The Go module builds, runs, passes `go test`, and has no external dependencies.
- Root Taskfile commands `go:test`, `go:build`, and `go:run` delegate to the Go
  module.
- Every accepted non-backlog task uses one mandatory Issue, deterministic task
  branch, checked squash PR, and automated branch cleanup.
- `/save` can create a task branch from the current attached branch and asks once
  before creating a retrospective task and its mandatory Issue when no unique
  task exists.
- GitHub and Gitea `main` remain identical after automated publication.

## Verification

Run:

```bash
task go:test
task go:build
task go:run
jq empty .oc_local/opencode.json
git --no-pager diff --check
```

## Verification Result

- `task go:test`, `task go:build`, and `task go:run` passed.
- `go vet ./...` passed from `app/codegeist/go`; `go list -m all` reported only
  the Codegeist Go module and no external dependencies.
- `task cli:check` passed with 202 tests, 0 failures, 0 errors, and 6 expected
  provider-gated skips; its test phase completed in 41.309 seconds and package
  phase in 4.826 seconds.
- Taskfile discovery, `.oc_local/opencode.json` parsing and lifecycle-rule
  registration, and `git --no-pager diff --check` passed.
- `.opencode` and `.devcontainer` were refreshed to their configured clean
  `release` branches before publication.
