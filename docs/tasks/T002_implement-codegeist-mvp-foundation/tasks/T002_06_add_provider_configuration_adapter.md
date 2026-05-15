# T002_06 Describe Provider Configuration Adapter

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_08`, `T001_20`, `T001_22`, `T001_24`

## Goal

Describe the first Codegeist provider configuration and Spring AI adapter
boundary without adding provider code, broad provider parity, or tool-calling
behavior.

## Context

Spring AI is the default provider integration path, but Codegeist owns provider
selection, capability classification, redaction, events, and tool exposure policy.
The architecture calls for one verified provider path before broad provider work.

## Concrete Solution

1. Create or update `docs/developer/provider-configuration-contracts.md` as the
   future provider configuration and adapter blueprint.
2. Define future provider config records/properties for provider id, model id,
   credential source reference, capabilities, and verification status.
3. Define a Spring AI adapter boundary that can be wired later to one provider
   path without exposing Spring AI types to runtime/session contracts.
4. Define safe validation or dry-run behavior that reports missing configuration
   as typed provider errors, not raw SDK exceptions.
5. Document future tests for config binding, missing credentials, capability
   classification, and runtime-facing typed errors.
6. Include OpenCode source evidence, future file maps, diagrams, and illustrative
   Java snippets in markdown only.

## Scope

- `docs/developer/provider-configuration-contracts.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Runtime-facing provider contracts are specified as Codegeist-owned and isolated
  from provider SDK details.
- Provider/model/capability configuration and validation posture are fully
  described.
- Missing or invalid provider config has typed error shapes in the blueprint.
- No live model calls are required unless explicitly safe in the test setup.
- Tool-calling exposure remains disabled until tool/permission contracts exist.
- No Java source files, tests, provider starters, credentials, or package
  directories are created by this task.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_01` for dependency alignment and should follow `T002_03` so
  provider errors and streaming can map to runtime/session/event contracts.
- Feeds end-to-end prompt flow and provider streaming work.

## Non-Goals

- Do not create Java source files, empty package directories, provider tests, or
  provider configuration classes.
- Do not implement broad provider ecosystems, model listing, persistent secrets,
  or Spring AI tool callbacks.
- Do not require network-dependent tests.

## Open Questions

- Which provider path should be verified first: OpenAI-compatible, Anthropic, or
   local/Ollama-style?

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later provider implementation task instead of creating
  `ai.codegeist.provider` source packages now.

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
register tasks as one provider-boundary documentation/specification slice.
