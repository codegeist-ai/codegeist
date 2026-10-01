# T010 Start Codegeist Go Project

Status: open
Public Tracking: not requested
Tracking Key: 3b7b5077-5cc3-478b-a127-d5da8cb5a359

## Goal

Start a completely new Codegeist project in Go under `app/codegeist/go`, next to
the existing `app/codegeist/cli` project. This task establishes only a minimal,
buildable Go project foundation. It does not migrate or reuse the existing Java
application, its architecture, behavior, or command-line contract.

The later product direction is a general AI agent, analogous to a coding agent
but not limited to coding, with CLI and TUI as its first user interfaces. Small
application size and fast startup/runtime are core goals. These are context for
future tasks, not features to implement in this initial project setup.

## Scope

- Create the Go project in `app/codegeist/go/`.
- Add only the minimal Go module and source needed for a clean build and a runnable
  empty application entrypoint.
- Choose and document the Go module path for the new project.
- Keep all new Go project files within `app/codegeist/go/` unless a narrowly
  required repository-level integration is identified and explained.
- Leave `app/codegeist/cli/` and the current Java/Spring implementation unchanged.
- Keep the initial module free of agent behavior, provider integrations, tools,
  configuration contracts, session storage, and user-facing CLI/TUI features.

## Non-Goals

- Do not migrate, port, copy, or adapt code, architecture, dependencies, tests,
  configuration, documentation, or command-line options from the Java project.
- Do not remove or modify the existing `app/codegeist/cli/` project.
- Do not implement an AI agent, model/provider integration, coding tools, or
  general-purpose tools.
- Do not implement CLI commands, flags, or a TUI in this initial task.
- Do not add speculative packages, abstractions, frameworks, or future feature
  placeholders.
- Do not add build/release automation or change repository-wide workflows.

## Acceptance Criteria

- `app/codegeist/go/` contains an independent, minimal Go module.
- The new Go application builds and runs using the documented project-local
  commands.
- The initial program has no agent or user-facing CLI/TUI behavior.
- The project documentation states that this is a greenfield Go project and
  records the module path and minimal build/run commands.
- Files under `app/codegeist/cli/` are not changed by this task.

## Verification

Run from `app/codegeist/go/`:

```bash
go test ./...
go build ./...
go run .
```

Also run from the repository root:

```bash
git --no-pager diff --check
```

## Future Direction

Follow-up tasks will define and implement the general AI agent from first
principles. CLI/TUI-first interaction, application size, and speed should guide
those tasks. No existing Codegeist command-line contract or implementation is
inherited by this new project.
