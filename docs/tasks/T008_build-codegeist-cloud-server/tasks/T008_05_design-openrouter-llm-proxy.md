# T008_05 Design OpenRouter LLM Proxy

Status: open

Parent: `../task.md`

## Goal

Define the first hosted LLM proxy contract for Codegeist Cloud, with OpenRouter or
another OpenAI-compatible upstream as the likely first provider profile.

## Scope

- Define whether the public API should mimic OpenAI chat completions, expose a
  Codegeist-specific API, or support both separately.
- Define entitlement checks, quota checks, model allowlists, request-size limits,
  streaming behavior, and usage accounting boundaries.
- Define where Codegeist-owned upstream credentials live and how bring-your-own-key
  is deferred or introduced later.
- Define safe test posture for no-cost or paid-provider approval before any live
  hosted call exists.

## Acceptance Criteria

- The proxy contract is documented before implementation.
- OpenRouter is treated as OpenAI-compatible upstream evidence, not as permission to
  make live calls.
- Remote calls remain blocked until credentials, costs, usage controls, and safety
  gates are explicit.

## Verification

```bash
git --no-pager diff --check
```
