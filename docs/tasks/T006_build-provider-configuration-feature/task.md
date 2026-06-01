# T006 Build Provider Configuration Feature

Status: open

## Goal

Build Codegeist provider configuration as a feature area instead of a single broad
implementation task.

The feature starts by designing a `codegeist.yml` provider schema that follows the
useful shape of OpenCode's provider configuration while using YAML-friendly
`kebab-case` field names. It then builds a Spring AI provider matrix, defines a
minimal Spring SpEL config-evaluation strategy plus provider availability analysis,
and only then implements validated provider loading and connection smoke checks.

## Feature Decision

Use `kebab-case` for committed `codegeist.yml` keys, for example
`enabled-providers`, `disabled-providers`, and `base-url`.

Keep Codegeist structurally close to OpenCode where the model applies:

- `provider.<provider-id>` contains provider configuration;
- `provider.<provider-id>.options` contains provider-specific runtime options;
- provider enablement is controlled through `enabled-providers` and
  `disabled-providers`;
- provider config values may be computed with Spring SpEL in the minimal parser
  slice;
- model selection and credential-store design are deferred until focused later
  tasks need them.

## OpenCode Source Evidence

Before implementing Codegeist behavior, use the analyzed OpenCode workspace and
prefer `/ask-project opencode ...` when the required Repomix artifacts are
available. The current source paths to inspect are:

- `docs/third-party/opencode/source/packages/opencode/src/config/config.ts`
- `docs/third-party/opencode/source/packages/opencode/src/config/provider.ts`
- `docs/third-party/opencode/source/packages/opencode/src/provider/provider.ts`
- `docs/third-party/opencode/source/packages/opencode/src/provider/auth.ts`
- `docs/third-party/opencode/source/packages/opencode/src/auth/index.ts`
- `docs/third-party/opencode/source/packages/web/src/content/docs/config.mdx`
- `docs/third-party/opencode/source/packages/web/src/content/docs/providers.mdx`
- `docs/third-party/opencode/source/packages/web/src/content/docs/models.mdx`

OpenCode behavior to translate, not copy:

- Config files merge global, custom, project, and managed sources.
- Provider credentials are stored outside project config in an auth store.
- Provider metadata and model lists can come from a provider database, config, env,
  auth, or plugins.
- Custom OpenAI-compatible providers can be configured with a provider id,
  `baseURL`, and explicit model map.
- Runtime provider filtering supports disabled and enabled provider lists.

## Spring AI Baseline

Current Codegeist state:

- `app/codegeist/cli` imports the Spring AI BOM `2.0.0-M6`.
- No Spring AI provider starters are present yet.
- No `codegeist.yml` file is loaded yet.
- No provider call, model selection, account flow, credential store, or connection
  smoke harness is implemented yet.

## Child Tasks

- `T006_01_design-codegeist-provider-config-schema.md` - design the
  `codegeist.yml` provider schema with `kebab-case` field names and OpenCode
  source evidence.
- `T006_02_create-spring-ai-provider-matrix.md` - list the Spring AI `2.0.0-M6`
  chat providers, starters, property prefixes, capabilities, credential needs,
  account requirements, and verification status.
- `T006_03_define-provider-credential-and-account-strategy.md` - define the
  minimal Spring SpEL config-evaluation strategy for provider config and catalog
  provider account/free-tier, starter, and on-demand availability requirements.
- `T006_04_implement-codegeist-yml-loading.md` - implement focused loading,
  SpEL evaluation, redacted rendering, and validation of `codegeist.yml`, including
  typed config records/POJOs for supported providers, without provider calls.
- `T006_05_verify-local-ollama-provider.md` - prove the first local provider path
  through Ollama with deterministic options and narrow assertions.
- `T006_06_add-provider-connection-smoke-harness.md` - add a repeatable connection
  smoke harness that can report `passed`, `skipped`, or `failed` for local and
  remote providers.

Later provider-specific child tasks should be created from the matrix only after
`T006_06` defines the shared smoke contract. Candidate providers include OpenAI,
Anthropic, Azure OpenAI, Amazon Bedrock Converse, Google GenAI, DeepSeek, Groq,
Mistral AI, MiniMax, Moonshot AI, NVIDIA, Perplexity AI, QianFan, Docker Model
Runner, Ollama, and supported OpenAI-compatible local endpoints.

## Non-Goals

- Do not create accounts or spend provider credits in the schema or matrix tasks.
- Do not commit API keys, OAuth tokens, cloud credentials, service account files, or
  generated secret material.
- Do not implement all provider SDK paths in one task.
- Do not create placeholder provider packages, ports, ids, enums, runtime adapters,
  validation hierarchies, or empty directories before a focused test needs them.
  `T006_04` may add provider-specific config records/POJOs only because its binding
  and validation tests need concrete config data shapes.
- Do not copy OpenCode's TypeScript, AI SDK, plugin, or Models.dev architecture into
  Codegeist source.
- Do not expose provider behavior through Vaadin, server APIs, PF4J, JBang, or SDKs
  in this feature slice.

## Parent Acceptance Criteria

- `codegeist.yml` provider schema is specified with `kebab-case` keys and examples.
- Spring AI provider support is captured in a matrix tied to the pinned
  `2.0.0-M6` baseline.
- The provider config strategy defines Spring SpEL evaluation without introducing a
  separate credential-reference schema.
- The first implemented provider path is local and testable before remote account
  work starts.
- Remote provider smokes are opt-in and report `skipped` with a concrete reason
  when required evaluated config values or setup are absent.
- Hosted provider account setup, billing or credit requirements, API free-tier
  claims, and Spring AI starter routes are cataloged from official sources before
  provider-specific remote smoke tasks use real accounts.
- Provider availability is analyzed before implementation: tasks know the required
  starter route, config fields, safety gate, runtime selection path, lazy Spring AI
  client creation point, and blockers before making a provider callable.
- `T006_04` maps the `T006_03` supported-provider decisions into typed Java config
  records/POJOs for config binding and validation only; no provider starter or
  provider client is added there.
- Integration tests should cover as many provider definitions as possible without
  causing charges: default tests validate config and local providers only, while
  hosted provider calls require an explicit no-cost remote test selection.
- OpenCode provider/config behavior is used as source evidence but translated into
  Codegeist's Java/Spring design.
- `docs/developer/architecture/architecture.md` is updated in the same task when
  implemented source, configuration, runtime behavior, or tests change.

## Verification

Documentation-only child tasks should run:

```bash
git --no-pager diff --check
```

Implementation child tasks should run the narrow Maven or Taskfile selector named
in that child task, then broaden to the relevant `app/codegeist/cli` verification.
Provider smokes must report timing and `passed`, `skipped`, or `failed` status.

## Planning Notes

- Use `docs/developer/specification/java-generation-guidance.md` before adding Java
  source.
- Use `docs/developer/specification/testing-strategy-and-agent-rules.md` before
  adding provider tests.
- Use `docs/developer/specification/codegeist-opencode-parity.md` for OpenCode
  behavior posture and provider boundary guidance.
- Use `/ask-project opencode ...` for source-backed OpenCode questions when its
  required analysis artifacts are present. If the command reports stale or missing
  artifacts, inspect the checked-out OpenCode source directly or rerun the analysis
  workflow before relying on broad source claims.
- Keep the first runtime slice small: load config, validate it, then call one local
  provider path.
- Use the `T006_03` account/free-tier catalog before adding hosted-provider-specific
  smoke rows or treating any hosted provider as eligible for `remote-free` checks.
- Use the `T006_03` availability matrix before provider-specific implementation;
  add one provider at a time instead of adding all starters or a broad placeholder
  registry up front.
- Config record coverage is the exception to one-provider-at-a-time runtime work:
  `T006_04` may cover all supported config shapes at once, but provider client
  creation and starter additions remain one provider at a time.
