# T002_06 Describe Provider Configuration Adapter

Parent: `T002_define-codegeist-mvp-foundation-blueprints`

Sources: `T001_08`, `T001_20`, `T001_22`, `T001_24`

status: finalized

## Goal

Describe the first Codegeist provider configuration and Spring AI adapter
boundary without adding provider code, broad provider parity, or tool-calling
behavior.

## Context

Spring AI is the default provider integration path, but Codegeist owns provider
selection, capability classification, redaction, events, and tool exposure policy.
The architecture calls for one verified provider path before broad provider work.

## Concrete Solution

1. Create or update `docs/developer/specification/provider-configuration-contracts.md` as the
   future provider configuration and adapter blueprint.
2. Define future provider config records/properties for provider id, model id,
   credential source reference, capabilities, and verification status.
3. Define a Spring AI adapter boundary that can be wired later to one provider
   path without exposing Spring AI types to runtime/session contracts.
4. Define safe validation or dry-run behavior that reports missing configuration
   as typed provider errors, not raw SDK exceptions.
5. Document future tests for config binding, missing credentials, capability
   classification, and runtime-facing typed errors.
6. Include OpenCode source evidence, Spring AI documentation evidence from
   Context7, future file maps, concrete UML class diagrams, and illustrative Java
   snippets in markdown only.
7. Map the proposed Codegeist contracts against the Spring AI counterparts that
   matter for a later adapter: `ChatModel`, `StreamingChatModel`, `Prompt`,
   `ChatOptions`, `ChatResponse`, provider-specific Spring Boot configuration
   properties, `ToolCallback`, and `internal-tool-execution-enabled`.

## Scope

- `docs/developer/specification/provider-configuration-contracts.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Runtime-facing provider contracts are specified as Codegeist-owned and isolated
  from provider SDK details.
- Provider/model/capability configuration and validation posture are fully
  described.
- Missing or invalid provider config has typed error shapes in the blueprint.
- UML class diagrams show the proposed Codegeist provider configuration,
  capability, credential reference, validation result, adapter port, request,
  response, streaming event, and typed error relationships.
- The blueprint cites Spring AI documentation evidence for the closest module and
  API counterparts, while keeping those types behind adapter boundaries.
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

- None for planning. The user decided that the first provider wave should cover
  OpenAI-compatible/OpenAI and Ollama, while the architecture must stay extensible
  to all Spring AI-supported providers.

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later provider implementation task instead of creating
  `ai.codegeist.provider` source packages now.
- The specification should describe a Codegeist-owned provider boundary that can
  later adapt Spring AI without letting Spring AI, provider SDKs, CLI commands, or
  session/event contracts own provider policy.
- The first provider path remains an implementation-readiness decision, not a
  specification-phase selection. The blueprint should compare OpenAI-compatible,
  Anthropic, and local/Ollama-style paths against MVP fit, streaming, credential
  posture, typed errors, and native-image risk before choosing one later.
- User decision: the first implementation wave should support OpenAI-compatible
  or OpenAI providers and Ollama. The architecture must still remain extensible to
  all Spring AI-supported providers through generic Codegeist descriptors,
  capability classification, credential references, and adapter ports.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- This task should ask targeted OpenCode source questions about provider
  selection, model configuration, streaming, and tool-call mediation before
  choosing the first provider path.
- Live network calls remain out of scope unless a safe local or test provider is
  explicitly selected.
- Rechecked again after `T002_05` finalization and the current task-phase rules.
  Provider configuration must consume runtime request/context metadata from
  upstream boundaries; it must not select context profiles, read workspace files,
  ingest external analysis artifacts, or bypass context manifest redaction and
  skip reasons.
- The future developer document should keep provider config, capability
  classification, credential-source references, verification status, dry-run
  validation, streaming posture, and typed provider errors separate from
  tool/permission execution contracts owned by `T002_07`.
- User clarification: this documentation slice should become concrete enough to
  include UML class diagrams for the future Codegeist provider contract model and
  adapter port. Use Context7 to check Spring AI documentation for matching
  Spring-side concepts before naming adapter seams.
- Context7 Spring AI evidence considered for specification: `ChatModel` handles
  prompt calls, `StreamingChatModel` streams `Prompt` input into `Flux` responses,
  `Prompt` carries messages plus optional `ChatOptions`, `ChatOptions` exposes
  portable model options such as model, max tokens, temperature, top-p/top-k, and
  stop sequences, provider properties live under `spring.ai.*`, and Spring AI
  tool support uses `ToolCallback` plus provider options such as
  `internal-tool-execution-enabled` that must not bypass Codegeist permission
  policy.
- Future tests may be mentioned as handoff guidance. They should stay at the edge
  of the documentation slice and cover config binding, missing credential-source
  references, capability classification, dry-run validation, Spring AI type
  isolation, streaming fallback mapping, disabled/internal tool execution posture,
  and typed provider error mapping.
- Provider support decision: the first concrete provider candidates are
  OpenAI-compatible/OpenAI and Ollama. Anthropic, Bedrock, Vertex AI, Mistral,
  Groq, DeepSeek, Hugging Face, OCI GenAI, QianFan, ZhipuAI, MiniMax, Moonshot,
  Perplexity, Docker Model Runner, NVIDIA/OpenAI-compatible, and other Spring
  AI-supported providers remain later adapters behind the same Codegeist-owned
  provider contracts.
- Spring AI provider starters, Java source files, tests, package directories,
  credentials, and live model calls remain out of scope for this task.

## Implementation-Readiness Questions

- How should the first implementation plan sequence OpenAI-compatible/OpenAI and
  Ollama support while keeping both behind the same provider adapter contract?
- Can provider validation prove configuration binding, credential-source presence,
  capability classification, and typed error mapping without making network calls?
- Which provider errors should be runtime-facing typed errors versus internal
  diagnostics retained behind redaction boundaries?
- How should the future adapter expose streaming capability and fallback behavior
  without leaking Spring AI response or option types into runtime/session/event
  contracts?
- Where should provider tool-call mediation stop until `T002_07` defines tool,
  permission, and workspace contracts?
- Which UML diagrams are necessary for the first blueprint: provider config model,
  adapter-port boundary, validation/error model, and Spring AI mapping should be
  considered the minimum useful set unless the plan narrows them further.
- Which extension contract makes later Spring AI provider adapters addable without
  changing runtime, session, event, CLI, or tool/permission contracts?

## Dependency Impact Notes

- Finalized `T002_05_add_context_workspace_manifest_slice.md` keeps context
  selection, context profiles, workspace path validation, context manifests,
  redaction posture, and external-analysis exclusion outside provider
  configuration. This task should describe provider config and adapter boundaries
  only; it must not make the provider layer choose workspace context sources,
  load Graphify or Repomix artifacts, or bypass context manifest redaction and
  skip reasons.
- Provider validation may later emit typed provider errors or diagnostics that
  reference context/request metadata, but the provider adapter should consume a
  runtime request boundary rather than own context profile creation or workspace
  reads.

## Phase Status

- Phase: `/specify-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_06_add_provider_configuration_adapter.md`.
- Context or instructions considered: user requested a specification pass by exact
  task path with no additional narrowing instructions.
- Upstream phase dependency: none; `/specify-task` is the entry phase and may be
  repeated when provider boundaries, dependencies, or documentation-only scope
  need to be refreshed.
- Parent considered:
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`.
- Adjacent and dependency tasks considered: finalized
  `T002_05_add_context_workspace_manifest_slice.md`, adjacent
  `T002_07_add_tool_permission_workspace_contracts.md`, dependency
  `T002_03_introduce-runtime-session-event-contracts.md`, and source tasks
  `T001_08`, `T001_20`, `T001_22`, and `T001_24`.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Documentation considered: `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- Discovered hints considered: the T002 parent `Default Solve Hints` point this
  provider task toward OpenCode source evidence for provider selection, model
  configuration, streaming, and tool-call mediation, while preserving Codegeist's
  Java-first runtime-owned boundaries.
- Result: specified. The task remains a documentation-only provider configuration
  and Spring AI adapter blueprint. It now explicitly keeps provider policy,
  capability classification, credential references, verification posture,
  streaming fallback, redacted diagnostics, and typed provider errors inside a
  Codegeist-owned boundary without implementing Java source or selecting a final
  first provider path.
- Additional user clarification considered: become concrete in the blueprint with
  UML class diagrams, use Context7 to find Spring AI module/API counterparts, and
  mention future tests around the edges without creating tests in this task.
- Provider support decision considered: OpenAI-compatible/OpenAI and Ollama are
  the first supported provider targets, while the provider architecture must be
  extensible to all Spring AI-supported providers over time.
- Open decisions or blockers: the later implementation plan still needs to choose
  how to sequence OpenAI-compatible/OpenAI versus Ollama verification and decide
  how much validation can run offline before any safe live-provider smoke test
  exists.
- Next recommended phase: run `/plan-task T002_06` as a documentation-only
  architecture plan for `docs/developer/specification/provider-configuration-contracts.md`.

## Architecture Plan

This planning pass keeps `T002_06` as one documentation-only architecture task.
No child task is needed because the source task already targets the provider
configuration and Spring AI adapter blueprint, and no matching provider blueprint
task or document exists yet.

### Selected Option

Create `docs/developer/specification/provider-configuration-contracts.md` as the concrete
provider configuration and adapter blueprint, then cross-link it from developer
documentation and current-state architecture notes.

This option follows the user's decisions:

- Be concrete with UML class diagrams and illustrative Java snippets in markdown.
- Use Context7 and Spring AI reference material for Spring-side counterparts.
- Support OpenAI-compatible/OpenAI and Ollama in the first provider wave.
- Keep the provider architecture extensible to all Spring AI-supported providers.
- Mention future tests as handoff guidance without creating test source now.

### Concrete Design Direction

Deepen the provider architecture around six layers:

1. Codegeist-owned provider descriptors and provider-model references.
2. Credential-source references, configuration source, and verification status.
3. Capability classification for streaming, structured output, tool calling,
   vision, reasoning, local/offline use, context window, and network requirement.
4. Provider validation and dry-run diagnostics that produce typed provider errors.
5. A runtime-facing provider adapter port for prompt calls and streaming output.
6. Spring AI adapter mapping for OpenAI-compatible/OpenAI, Ollama, and later
   Spring AI-supported providers.

The blueprint should treat Spring AI as the integration layer, not the runtime
contract. Spring AI concepts such as `ChatModel`, `StreamingChatModel`, `Prompt`,
`ChatOptions`, `ChatResponse`, provider-specific `spring.ai.*` properties,
`ToolCallback`, and `internal-tool-execution-enabled` should be documented as
adapter-side counterparts only.

### Planned Documentation Files

- `docs/developer/specification/provider-configuration-contracts.md`
- `docs/developer/README.md`
- `docs/developer/architecture/architecture.md` only for current-state cross-references and
  explicit not-implemented notes
- this task file

No Java source, tests, fixtures, Maven files, build files, package directories,
Spring beans, provider starters, credentials, live model calls, tool callbacks,
Graphify, Repomix, or runtime behavior are planned for this task.

### Planned Blueprint Content

1. State the purpose and non-implementation boundary of the provider blueprint.
2. Summarize OpenCode provider behavior as feature evidence: provider/model
   selection, configuration, streaming, capability checks, and tool-call mediation.
3. Summarize Spring AI documentation evidence from Context7 and official
   reference material, including the chat model abstractions and provider
   configuration properties relevant to adapters.
4. Define Codegeist-owned future contract names for provider id, provider type,
   model id, provider-model reference, credential-source reference, capability
   set, verification status, validation result, adapter request, adapter response,
   streaming chunk/event, and provider error.
5. Add UML class diagrams in markdown for the minimum useful set:
   provider configuration model, adapter-port boundary, validation/error model,
   streaming response model, and Spring AI counterpart mapping.
6. Add a provider support matrix with first-wave OpenAI-compatible/OpenAI and
   Ollama rows, followed by later Spring AI-supported providers behind the same
   adapter contract.
7. Define the first-wave provider stance: OpenAI-compatible/OpenAI proves hosted or
   configurable-base-url provider support; Ollama proves local/offline provider
   support and safer non-cloud validation.
8. Define validation and dry-run behavior that detects missing provider selection,
   missing model id, missing credential source, unsupported capability, disabled
   provider, malformed endpoint/base URL, and tool-calling disabled by policy.
9. Define typed provider errors and redacted diagnostics without raw SDK exception
   leakage.
10. Define how provider output should later map to runtime/session/event metadata
    without embedding Spring AI or provider SDK types in those contracts.
11. Define tool-calling posture: Spring AI tool callbacks remain disabled or
    externally mediated until `T002_07` defines tool, permission, and workspace
    contracts.
12. Record future test handoff notes for config binding, missing credentials,
    capability classification, validation, type isolation, streaming fallback,
    disabled/internal tool execution posture, adapter contract tests, and typed
    error mapping.

### Future File Map

The solve pass should include a future file map in markdown only. It can name
future paths such as:

- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderId.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderType.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderModelRef.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderCapability.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderConfig.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/CredentialSourceRef.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderValidationResult.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderError.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/ProviderAdapter.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/springai/SpringAiProviderAdapter.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/springai/OpenAiCompatibleProviderAdapter.java`
- `app/codegeist/cli/src/main/java/ai/codegeist/provider/springai/OllamaProviderAdapter.java`
- `app/codegeist/cli/src/test/java/ai/codegeist/provider/...` for later contract
  tests

These paths are illustrative future implementation targets only. The solve pass
must not create them.

### Planned Diagrams

Use Mermaid `classDiagram` unless PlantUML is clearly more useful for a specific
view.

1. Provider configuration class diagram: `ProviderConfig`, `ProviderModelRef`,
   `ProviderCapabilitySet`, `CredentialSourceRef`, `VerificationStatus`, and
   `ConfigurationSource`.
2. Adapter boundary class diagram: runtime-facing `ProviderAdapter`,
   `ProviderRequest`, `ProviderResponse`, `ProviderStreamChunk`, `ProviderError`,
   plus Spring AI adapter implementation classes.
3. Validation/error class diagram: `ProviderValidator`, `ProviderValidationResult`,
   `ProviderDiagnostic`, `ProviderErrorCode`, recoverability, and redaction
   posture.
4. Spring AI counterpart mapping class diagram: Codegeist adapter side mapped to
   `ChatModel`, `StreamingChatModel`, `Prompt`, `ChatOptions`, `ChatResponse`, and
   tool-calling controls.
5. Optional sequence diagram: future runtime provider call from runtime request to
   provider adapter, validation, Spring AI call/stream, typed event handoff, and
   failure mapping.

### Scope

- Architecture and documentation only.
- Keep Codegeist runtime, session, event, CLI, tool, permission, and workspace
  contracts free of OpenAI, Ollama, Spring AI concrete classes, and provider SDK
  types.
- Keep first-wave provider detail limited to OpenAI-compatible/OpenAI and Ollama.
- Keep the extension contract broad enough for every Spring AI-supported provider
  to become a later adapter.
- Keep future Java names and snippets illustrative.

### Non-Goals

- Do not create Java source files, tests, fixtures, package directories, Maven
  changes, build files, Spring beans, or Spring AI provider starters.
- Do not implement provider calls, provider validation, model listing, credential
  loading, secret storage, streaming, tool callbacks, or runtime events.
- Do not perform live model calls or network-dependent verification.
- Do not run Graphify, Repomix, OpenCode analysis generation, or broad source
  indexing.
- Do not make Anthropic, Bedrock, Vertex AI, Mistral, Groq, DeepSeek, Hugging
  Face, OCI GenAI, QianFan, ZhipuAI, MiniMax, Moonshot, Perplexity, Docker Model
  Runner, NVIDIA/OpenAI-compatible, or other Spring AI-supported providers
  first-wave implementation targets.

### Acceptance Criteria

- `docs/developer/specification/provider-configuration-contracts.md` exists and clearly
  describes Codegeist-owned provider configuration and adapter boundaries.
- The document includes concrete UML class diagrams for provider configuration,
  adapter boundary, validation/error, streaming response, and Spring AI mapping.
- Spring AI reference evidence is cited or summarized for `ChatModel`,
  `StreamingChatModel`, `Prompt`, `ChatOptions`, `ChatResponse`, provider
  properties, and tool-calling controls.
- The first provider wave is explicitly OpenAI-compatible/OpenAI and Ollama.
- The extension model explains how later Spring AI-supported providers can be
  added without changing runtime, session, event, CLI, or tool/permission
  contracts.
- Provider validation and typed error shapes are described, including missing
  provider/model/credential cases and disabled tool-calling posture.
- Future tests are described as handoff guidance only; no test source is created.
- `docs/developer/README.md` links the new provider blueprint.
- `docs/developer/architecture/architecture.md` remains current-state focused and only gains
  not-implemented or related-document references if needed.
- No Java source, tests, packages, provider starters, credentials, live calls,
  runtime behavior, Graphify, or Repomix artifacts are added.

### Verification Plan

Run from the repository root:

```bash
git --no-pager diff --check
```

This proves the documentation diff has no whitespace errors. `task test` is not
required because this task must not change Java source, tests, Maven files, build
files, provider starters, or runtime behavior.

### Dependencies

- Depends on the current `T002_06` specification status and user decisions in this
  task.
- Uses the T002 parent default hints for OpenCode translation and source-evidence
  posture.
- Uses `docs/developer/specification/codegeist-opencode-parity.md` provider architecture,
  GraalVM constraints, MVP cut, and risk-register direction.
- Uses `docs/developer/specification/runtime-vocabulary.md` and
  `docs/developer/specification/runtime-session-event-contracts.md` for runtime/session/event
  type-isolation boundaries.
- Uses finalized `T002_05` context/workspace boundary so provider configuration
  does not own context profiles, workspace reads, or external analysis ingestion.
- Feeds `T002_07` by keeping Spring AI tool-calling disabled or externally
  mediated until tool, permission, and workspace contracts exist.

### Open Questions

None for this planning pass. The solve pass should record any newly discovered
Spring AI or OpenCode evidence directly in the blueprint and keep unanswered
implementation choices as future handoff notes.

## Plan Workflow Handoff

- Phase: `/plan-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_06_add_provider_configuration_adapter.md`.
- Source task resolved from user input: `t002_06`, resolved to this existing task
  file under the T002 parent.
- Target task: this existing `T002_06` task; no child task was created because the
  task is already the narrow documentation-only provider blueprint slice.
- User context considered: the user asked for concrete UML class diagrams, Spring
  AI documentation lookup through Context7, future test mentions at the edge, and
  first-wave OpenAI-compatible/OpenAI plus Ollama support with long-term support
  for all Spring AI-supported providers.
- Parent task considered:
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`.
- Adjacent and dependency tasks considered: finalized
  `T002_05_add_context_workspace_manifest_slice.md`, adjacent
  `T002_07_add_tool_permission_workspace_contracts.md`, dependency
  `T002_03_introduce-runtime-session-event-contracts.md`, and source tasks
  `T001_08`, `T001_20`, `T001_22`, and `T001_24`.
- Duplicate check result: no existing `docs/developer/specification/provider-configuration-contracts.md`
  document exists, and no separate provider implementation task already covers
  this blueprint; the existing `T002_06` task is the correct target to sharpen.
- Selected option: create the provider architecture blueprint document in the
  solve pass and cross-link it from developer docs while leaving Java source and
  build files untouched.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/runtime-vocabulary.md`, `docs/developer/architecture/architecture.md`,
  `docs/developer/README.md`, and `docs/memory-bank/chat.md`.
- Spring AI evidence considered from prior Context7 lookup: `ChatModel`,
  `StreamingChatModel`, `Prompt`, `ChatOptions`, `ChatResponse`, provider
  `spring.ai.*` properties, `ToolCallback`, and `internal-tool-execution-enabled`.
- Upstream phase dependency: satisfied; this task had top-level
  `status: specified` and a current `/specify-task` phase status before this
  planning pass.
- Result: planned as one documentation-only architecture task.
- Open decisions or blockers: none for planning.
- Next recommended phase: run `/solve-task T002_06` as a documentation-only
  architecture design pass.

## Solution Note

Solved as a documentation-only architecture blueprint in
`docs/developer/specification/provider-configuration-contracts.md`. The new document defines
Codegeist-owned provider configuration, capability classification, credential
source references, validation and dry-run diagnostics, typed provider errors,
runtime-facing adapter ports, streaming response boundaries, Spring AI counterpart
mapping, first-wave OpenAI-compatible/OpenAI and Ollama support, and extension
rules for later Spring AI-supported providers.

The solve pass also linked the new blueprint from `docs/developer/README.md` and
`docs/developer/architecture/architecture.md`. It did not create Java source, tests, package
directories, provider starters, credentials, live model calls, tool callbacks,
Graphify, Repomix, or runtime behavior.

## Solve Status

- Phase: `/solve-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_06_add_provider_configuration_adapter.md`.
- User instructions considered: solve `t002_06` using the existing plan. Prior
  user decisions remain in effect: include concrete UML class diagrams, use Spring
  AI documentation evidence from Context7, mention future tests only as handoff
  guidance, support OpenAI-compatible/OpenAI and Ollama first, and keep the
  architecture extensible to all Spring AI-supported providers.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: planned` and a current `/plan-task` handoff before this solve pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: parent
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md`, finalized
  `T002_05_add_context_workspace_manifest_slice.md`, adjacent
  `T002_07_add_tool_permission_workspace_contracts.md`, dependency
  `T002_03_introduce-runtime-session-event-contracts.md`,
  `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/runtime-session-event-contracts.md`,
  `docs/developer/specification/context-workspace-manifest.md`, `docs/developer/architecture/architecture.md`,
  `docs/developer/README.md`, and `docs/memory-bank/chat.md`.
- OpenCode evidence considered: `v2/model.ts` for provider/model refs,
  capabilities, endpoints, options, and status; `config/provider.ts` for provider
  config, env keys, base URL, model metadata, modalities, reasoning, tool-call
  flags, cost, limits, headers, and variants; `provider/provider.ts` for broad SDK
  loading and provider-specific behavior; provider HTTP handlers for provider
  listing and authorization; and `v2/session-event.ts` for model switches,
  provider metadata, text/reasoning deltas, step failures, and retry errors.
- Spring AI evidence considered through Context7 and the Spring AI reference:
  `ChatModel`, `StreamingChatModel`, `Prompt`, `ChatOptions`, `ChatResponse`,
  provider `spring.ai.*` properties, OpenAI API key/base URL/model properties,
  Ollama base URL/model properties, `ToolCallback`, and
  `internal-tool-execution-enabled`.
- Documentation updates: created
  `docs/developer/specification/provider-configuration-contracts.md`, updated
  `docs/developer/README.md`, updated `docs/developer/architecture/architecture.md`, and
  refreshed `docs/memory-bank/chat.md`.
- Acceptance criteria status: satisfied. Runtime-facing provider contracts are
  specified as Codegeist-owned and isolated from provider SDK details; provider,
  model, capability, credential-source, validation, typed error, adapter,
  streaming, and Spring AI mapping shapes are described with UML diagrams; no live
  model calls are required; tool-calling remains disabled or externally mediated;
  and no Java source, tests, provider starters, credentials, or package
  directories were created.
- Verification: `git --no-pager diff --check`.
- Open decisions or blockers: none for this architecture pass. Future
  implementation tasks still need to choose exact Spring AI starter dependencies,
  bind real configuration properties, write Java contracts, and add deterministic
  tests before any optional live smoke checks.
- Next recommended phase: run `/finalize-task T002_06`.

## Finalization Status

- Phase: `/finalize-task` for
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/tasks/T002_06_add_provider_configuration_adapter.md`.
- User instructions considered: finalize `T002_06` by checking task impact and
  refreshing affected documentation after the successful solve phase.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: solved` and a current successful `/solve-task` status before this
  finalization pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Impacted tasks: parent
  `docs/tasks/T002_define-codegeist-mvp-foundation-blueprints/task.md` and adjacent open
  `T002_07_add_tool_permission_workspace_contracts.md`.
- Documentation reviewed through update-documentation semantics:
  `docs/developer/specification/provider-configuration-contracts.md`, `docs/developer/README.md`,
  `docs/developer/architecture/architecture.md`, and `docs/memory-bank/chat.md`.
- Documentation updates: the parent task now records that `T002_06` is finalized;
  `T002_07` records that Spring AI tool callbacks and provider-emitted tool-call
  signals must remain disabled or externally mediated until tool, permission, and
  workspace contracts exist; and `docs/memory-bank/chat.md` was refreshed to make
  `T002_07` the next useful T002 slice.
- Remaining follow-ups: continue with
  `T002_07_add_tool_permission_workspace_contracts.md` as the next
  documentation-first T002 slice. Future Java implementation tasks for provider
  config can be derived later from `docs/developer/specification/provider-configuration-contracts.md`
  only when explicitly requested.
- Verification: `git --no-pager diff --check`.
- Result: finalized. No implementation gaps, blockers, Java source, tests,
  package directories, provider starters, credentials, live model calls, tool
  callbacks, Graphify, Repomix, or runtime behavior were introduced.
- Next recommended phase: start `/specify-task T002_07` or `/work-task T002_07`
  when ready to continue the next documentation/specification slice.

## Creation Note

Created as an open task and later specified through the shared task-phase
workflow.

Derived from provider architecture, GraalVM constraints, MVP cut, and risk
register tasks as one provider-boundary documentation/specification slice.
