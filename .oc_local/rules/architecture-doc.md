# Architecture Documentation Rule

Use this rule when creating, reading, or updating
`docs/developer/architecture.md`.

## Purpose

- Treat `docs/developer/architecture.md` as the current-state architecture map for
  Codegeist.
- Use it to give coding agents a compact, accurate view of what exists in the
  repository now.
- Keep target architecture, parity planning, and future design details in
  `docs/developer/codegeist-opencode-parity.md` unless they are clearly marked as
  not implemented.

## What Belongs In `architecture.md`

- Current implemented system structure.
- Current Java/Spring Boot application layout under `app/codegeist`.
- Current Maven, Spring Boot, Spring Shell, Spring AI BOM, Java, GraalVM, and
  Taskfile build posture.
- Implemented packages, classes, configuration files, tests, and entrypoints.
- Current runtime behavior and startup flow.
- Mermaid diagrams that summarize current components, package layout, startup,
  and verification flows.
- Explicit "not implemented yet" sections when they prevent a coding agent from
  assuming planned architecture already exists.

## What Does Not Belong

- Aspirational architecture that has not been implemented.
- Broad OpenCode parity analysis.
- Future module/package plans unless clearly labeled as planned or not
  implemented.
- Task-level implementation logs.
- Duplicated long-form content from `docs/developer/codegeist-opencode-parity.md`.

## Usage

- Read `docs/developer/architecture.md` early when working on Codegeist runtime,
  Spring Boot, CLI, build, package-boundary, or verification tasks.
- Before changing architecture-relevant code, compare the intended change against
  the current-state architecture.
- After changing architecture-relevant code, update `architecture.md` in the same
  task so it remains accurate.
- If a change only affects future planning, update the relevant task file or
  `docs/developer/codegeist-opencode-parity.md` instead.

## Accuracy Requirements

- Describe the current repository state, not intended future state.
- Keep implemented and planned concepts visibly separate.
- Prefer concrete file paths and class names over vague descriptions.
- Keep diagrams small enough for a coding agent to scan quickly.
- Update diagrams when the described structure changes.
