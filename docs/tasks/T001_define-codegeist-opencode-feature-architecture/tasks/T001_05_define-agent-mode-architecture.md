# T001_05 Define Agent Mode Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Plan and Build agent modes for Codegeist.

## Scope

- Describe Plan mode as read-only analysis and task planning.
- Describe Build mode as implementation-capable work.
- Define which permissions and tools each mode may request.

## Deliverable

Add an agent mode section to `docs/developer/codegeist-opencode-parity.md`.

## Acceptance Criteria

- Plan mode cannot perform write, shell, or network side effects by default.
- Build mode still requires permission gates for risky operations.
- Agent modes are runtime concepts, not CLI-only flags.

## Verification

- Compare with OpenCode feature notes for build and plan agents.
