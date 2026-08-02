# T011 Refresh Provider Implementation Specification

Status: open

Public Tracking: https://github.com/codegeist-ai/codegeist/issues/4

Contribution Level: intermediate (`help wanted` candidate, not `good first issue`)

Effort: medium

Confirmed Unmet: on 2026-08-02,
`docs/developer/specification/llm-provider-implementation.md` still models
`CodegeistConfig` as `@ConfigurationProperties`, puts provider config in the
planned `CodegeistChatRequest`, treats Ollama as the only implemented adapter, and
describes implemented tool/session behavior as future work.

## Goal

Refresh the provider implementation specification so it clearly separates current
Ollama/OpenAI runtime behavior from future provider guidance and no longer teaches
obsolete pre-implementation contracts.

## Acceptance Criteria

- The specification no longer describes `CodegeistConfig` as a Spring
  `@ConfigurationProperties` provider map.
- The documented `CodegeistChatRequest` excludes provider configuration and matches
  the current request/turn split.
- Current Ollama and OpenAI adapters, provider-owned default models, tool-aware
  message flow, agent-loop dispatch, and session/harness ownership are described
  accurately.
- Statements that only applied before OpenAI, tools, and resumable sessions were
  implemented are removed or explicitly labeled historical/future.
- Provider categories and the no-provider-call default remain aligned with
  `docs/tests/provider-feature-tests.md` and the canonical `task cli:check` gate.
- Diagrams render and distinguish implemented classes from illustrative future
  provider classes.

## Files

- `docs/developer/specification/llm-provider-implementation.md`
- `docs/developer/architecture/provider-configuration.md` only if a cross-link or
  current-state correction is required
- `docs/tests/provider-feature-tests.md` only if a stale shared statement is found

## Non-Goals

- Do not add or change Java runtime behavior, providers, models, dependencies, or
  configuration fields.
- Do not make provider calls or require credentials.
- Do not redesign the provider architecture.
- Do not expand this into a general documentation rewrite; the contributor must
  understand the current config, chat, agent-loop, and session boundaries, so this
  is not beginner-safe work.

## Verification

```bash
task cli:check
git --no-pager diff --check
```

Review the final document against the current classes under
`app/codegeist/cli/src/main/java/ai/codegeist/app/chat` and
`app/codegeist/cli/src/main/java/ai/codegeist/app/config`.
