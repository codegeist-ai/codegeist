# T001_14 Define Context Loading Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist loads repository context for agent sessions.

## Scope

- Define sources for rules, memory, task files, project docs, local overlays,
  source files, and third-party analysis artifacts.
- Define priority and ordering for context sources.
- Identify what should be loaded automatically versus on demand.

## Deliverable

Add a context loading architecture section to the parity document.

## Acceptance Criteria

- Context loading is deterministic and explainable.
- Repo-owned rules and memory are first-class sources.
- Large third-party artifacts are treated as on-demand context.

## Verification

- Review against `docs/memory-bank/chat.md` and `docs/third-party/opencode/`.
