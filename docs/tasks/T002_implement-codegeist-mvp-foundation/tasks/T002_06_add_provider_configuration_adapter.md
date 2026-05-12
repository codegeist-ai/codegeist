# T002_06 Add Provider Configuration Adapter

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_08`, `T001_20`, `T001_22`, `T001_24`

## Goal

Add the first Codegeist provider configuration and Spring AI adapter boundary
without broad provider parity or tool-calling behavior.

## Context

Spring AI is the default provider integration path, but Codegeist owns provider
selection, capability classification, redaction, events, and tool exposure policy.
The architecture calls for one verified provider path before broad provider work.

## Concrete Solution

1. Add provider config records/properties for provider id, model id, credential
   source reference, capabilities, and verification status.
2. Add a Spring AI adapter boundary that can be wired later to one provider path
   without exposing Spring AI types to runtime/session contracts.
3. Add a safe validation or dry-run path that reports missing configuration as a
   typed provider error, not a raw SDK exception.
4. Add tests for config binding, missing credentials, capability classification,
   and runtime-facing typed errors.
5. Defer live network calls unless a safe local/test provider path exists.

## Scope

- `ai.codegeist.provider`
- application configuration classes/properties
- provider-focused tests
- documentation only if the chosen first provider differs from the architecture

## Acceptance Criteria

- Runtime-facing code depends on Codegeist provider contracts, not provider SDK
  details.
- Provider/model/capability configuration can be represented and validated.
- Missing or invalid provider config produces typed errors.
- No live model calls are required unless explicitly safe in the test setup.
- Tool-calling exposure remains disabled until tool/permission contracts exist.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_01` for dependency alignment and should follow `T002_03` so
  provider errors and streaming can map to runtime/session/event contracts.
- Feeds end-to-end prompt flow and provider streaming work.

## Non-Goals

- Do not implement broad provider ecosystems, model listing, persistent secrets,
  or Spring AI tool callbacks.
- Do not require network-dependent tests.

## Open Questions

- Which provider path should be verified first: OpenAI-compatible, Anthropic, or
  local/Ollama-style?

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- This task should ask targeted OpenCode source questions about provider
  selection, model configuration, streaming, and tool-call mediation before
  choosing the first provider path.
- Live network calls remain out of scope unless a safe local or test provider is
  explicitly selected.

## Creation Note

Status: open.

Derived from provider architecture, GraalVM constraints, MVP cut, and risk
register tasks as one provider-boundary implementation slice.
