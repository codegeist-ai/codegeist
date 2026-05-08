# T001_04 Define CLI And Shell Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist should expose CLI and interactive shell behavior.

## Scope

- Map OpenCode CLI and TUI behavior to Spring Boot, Spring Shell, and JLine.
- Separate command parsing from runtime orchestration.
- Define interactive and non-interactive entrypoint expectations.

## Deliverable

Add a CLI and shell architecture section to the parity document.

## Acceptance Criteria

- CLI is defined as a client of the runtime.
- TUI/full-screen terminal behavior is not required for the first MVP.
- Streaming output and approval prompts are considered.

## Verification

- Review current `app/codegeist` Spring Shell bootstrap assumptions.
