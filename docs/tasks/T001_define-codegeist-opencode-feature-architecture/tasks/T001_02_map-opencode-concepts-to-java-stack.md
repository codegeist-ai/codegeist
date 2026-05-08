# T001_02 Map OpenCode Concepts To Java Stack

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Translate OpenCode concepts into Codegeist concepts using the selected Java-first
technology stack.

## Scope

- Map CLI, TUI, agents, sessions, events, providers, tools, permissions,
  plugins, server, web, storage, and packaging.
- Avoid copying OpenCode's Bun/TypeScript architecture shape.
- Identify unclear mappings that need later validation.

## Deliverable

Add or refine a concept mapping table in
`docs/developer/codegeist-opencode-parity.md`.

## Acceptance Criteria

- Every mapped OpenCode concept has a Codegeist target concept.
- Every mapped concept names a primary Java/Spring technology owner.
- Open questions are listed for uncertain mappings.

## Verification

- Review against `docs/third-party/opencode/features/README.md` and
  `docs/third-party/opencode/ANALYSIS_REPORT.md`.
