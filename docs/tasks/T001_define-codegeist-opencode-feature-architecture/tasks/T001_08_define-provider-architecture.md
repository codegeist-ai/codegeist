# T001_08 Define Provider Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist uses Spring AI and any Codegeist-specific provider policy.

## Scope

- Define provider, model, credentials, model capabilities, and selection.
- Decide where Spring AI is sufficient and where Codegeist adapters may be
  needed.
- Identify first provider candidates for later verification.

## Deliverable

Add a provider architecture section to the parity document.

## Acceptance Criteria

- Spring AI is the default provider integration path.
- Provider configuration is kept separate from CLI behavior.
- Open questions about tool calling and streaming are listed.

## Verification

- Review Spring AI assumptions before implementation in a later task.
