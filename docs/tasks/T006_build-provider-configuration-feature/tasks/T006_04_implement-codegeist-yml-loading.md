# T006_04 Implement Codegeist YML Loading

Status: open

Parent: `../task.md`

## Goal

Implement focused config access for Codegeist provider configuration, extending
the existing name-only loader with the `T006_03` SpEL, provider availability, and
provider support decisions.

The current slice should support both Spring-bound application configuration and
direct YAML loading for an explicit `codegeist.yml` path, then map provider
entries into typed Java config records/POJOs for every supported provider type
without introducing provider runtime abstractions or provider calls.

## Scope

- Work in `app/codegeist/cli`.
- Use `T006_01` for the base loader contract, `T006_02` for the Spring AI
  provider baseline, and `T006_03` for SpEL, account/free-tier, OpenCode-vs-
  Codegeist provider support, and provider availability decisions.
- Add Lombok support for focused getter/setter boilerplate reduction and `@Slf4j`
  diagnostics on Spring service/component classes.
- Add Jackson YAML support for direct YAML-to-POJO mapping.
- Add Spring Bean Validation support for annotation-based config validation.
- Add `CodegeistApplication.APP_NAME` as the shared application name and Spring
  config prefix constant.
- Add `CodegeistConfig` under `ai.codegeist.app.config` with
  `@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)`, a
  prefix constant backed by `CodegeistApplication.APP_NAME`, Jackson naming
  metadata, the `provider` map, and top-level `enabled-providers` and
  `disabled-providers` fields for config loading and merge behavior only.
- Expand `ProviderConfig` from the current `name` field into the common provider
  envelope used by the typed provider records.
- Add `CodegeistConfigService` that receives the Spring-bound config through
  qualified `@Autowired`, exposes a primary `mergedCodegeistConfig` `@Bean`, and
  exposes `loadConfig(String configPath): CodegeistConfig`.
- Add the first trusted-local-input SpEL evaluation phase before direct YAML maps to
  `CodegeistConfig`: evaluate only string scalar values containing `#{`, leave YAML
  keys and non-string scalars unchanged, and use plain Spring
  `StandardEvaluationContext` without a Codegeist sandbox, helper variables,
  functions, or Spring bean resolver.
- Expand the model from provider `name` only to the supported provider config fields
  needed by `T006_03`: top-level `enabled-providers` and `disabled-providers`, plus
  common provider `type`, `enabled`, `model`, `base-url`, `completions-path`,
  provider credentials as ordinary scalar values that may be populated by SpEL, and
  nested `options` for provider-specific knobs.
- Add typed Java config records/POJOs for every supported provider type listed in
  this task's provider record matrix. These are config data shapes only; they must
  not be Spring AI clients, runtime adapters, factories, or provider registries.
- Add redacted rendering for `--show-config` and diagnostics so evaluated secret
  fields are not printed after SpEL materializes environment values.
- Add non-mutating `merge(...)` methods on each current config model type so later
  source-order work can compose Spring-bound, home, and explicit-path configs.
- Use unqualified `CodegeistConfig` injection for callers that need the primary
  merged config bean.
- Keep `--show-config` as a Spring Shell command that prints only the current
  merged config in direct `codegeist.yml` YAML form, with secret-bearing fields
  redacted.
- Add focused tests for application YAML binding, explicit YAML path loading, SpEL
  scalar evaluation, typed provider config records, merge behavior, redaction, and
  annotation-backed validation.

## Supported Provider Config Records

For this implementation task, "supported provider" means provider types that
`T006_03` classifies as usable through the pinned Spring AI `2.0.0-M6` baseline,
either by a dedicated Spring AI starter or by the OpenAI-compatible starter route.
Also include `openrouter` as a thin OpenAI-compatible profile because `T006_03`
classifies it as technically supportable without a dedicated Spring AI starter.

Do not add `moonshot` or `qianfan` records in this task. `T006_03` marks them as
not core-shipped in the pinned Spring AI baseline, so they need a later
upgrade/community-provider task before becoming supported Codegeist config types.

Use Java records for immutable provider-specific normalized config where that stays
simple with Jackson and validation. Keep mutable POJOs only where Spring
`@ConfigurationProperties`, Jackson YAML, or merge behavior needs setters.

| Config record | `type` value | Spring AI route | Minimum fields to bind and validate now |
| --- | --- | --- | --- |
| `OllamaProviderConfig` | `ollama` | Dedicated `spring-ai-starter-model-ollama`. | `name`, `type`, `enabled`, `model`, `base-url`, nested deterministic `options` such as `temperature` or `seed` when present. |
| `DockerModelRunnerProviderConfig` | `docker-model-runner` | OpenAI-compatible through `spring-ai-starter-model-openai`. | `name`, `type`, `enabled`, `model`, `base-url`, optional local/dummy API key scalar, OpenAI-compatible `options`. |
| `OpenAiProviderConfig` | `openai` | Dedicated `spring-ai-starter-model-openai`. | `name`, `type`, `enabled`, `model`, API key scalar, optional `base-url`, organization/project fields, token and request `options`. |
| `AzureOpenAiProviderConfig` | `azure-openai` | OpenAI-compatible through `spring-ai-starter-model-openai`. | `name`, `type`, `enabled`, deployment/model selector, `base-url`, API key scalar, `options.deployment-name`, `options.api-version`, optional region. |
| `AnthropicProviderConfig` | `anthropic` | Dedicated `spring-ai-starter-model-anthropic`. | `name`, `type`, `enabled`, `model`, API key scalar, token cap and service-tier `options`. |
| `BedrockConverseProviderConfig` | `bedrock-converse` | Dedicated `spring-ai-starter-model-bedrock-converse`. | `name`, `type`, `enabled`, `model`, AWS credential/profile scalar fields, `options.region`, timeout and token `options`. |
| `GoogleGenAiProviderConfig` | `google-genai` | Dedicated `spring-ai-starter-model-google-genai`. | `name`, `type`, `enabled`, `model`, API-key mode fields or Vertex project/location fields, provider `options`. |
| `DeepSeekProviderConfig` | `deepseek` | Dedicated `spring-ai-starter-model-deepseek`. | `name`, `type`, `enabled`, `model`, API key scalar, optional endpoint/path `options`. |
| `MiniMaxProviderConfig` | `minimax` | Dedicated `spring-ai-starter-model-minimax`. | `name`, `type`, `enabled`, `model`, API key scalar, response-format and seed `options`. |
| `MistralAiProviderConfig` | `mistral-ai` | Dedicated `spring-ai-starter-model-mistral-ai`. | `name`, `type`, `enabled`, `model`, API key scalar, response-format and random-seed `options`. |
| `GroqProviderConfig` | `groq` | OpenAI-compatible through `spring-ai-starter-model-openai`. | `name`, `type`, `enabled`, `model`, `base-url`, API key scalar, conservative OpenAI-compatible `options`. |
| `NvidiaProviderConfig` | `nvidia` | OpenAI-compatible through `spring-ai-starter-model-openai`. | `name`, `type`, `enabled`, `model`, `base-url`, API key scalar, required `options.max-tokens` when a call is later attempted. |
| `PerplexityProviderConfig` | `perplexity` | OpenAI-compatible through `spring-ai-starter-model-openai`. | `name`, `type`, `enabled`, `model`, `base-url`, `completions-path`, API key scalar, token/search `options`. |
| `OpenRouterProviderConfig` | `openrouter` | OpenAI-compatible through `spring-ai-starter-model-openai`. | `name`, `type`, `enabled`, `model`, `base-url`, API key scalar, optional `options.http-referer`, `options.x-openrouter-title`, routing options, and `:free` gating metadata. |

Credential fields in this task are ordinary config scalar values, not a new
credential-reference schema. If a value should come from the environment, the YAML
should use the `T006_03` SpEL approach, for example:

```yaml
provider:
  openai:
    type: openai
    api-key: "#{T(java.lang.System).getenv('OPENAI_API_KEY')}"
```

This task may validate that required fields for each provider-specific record are
present and non-blank after SpEL evaluation. It must not treat the presence of an
API key as permission to create a provider client or make a remote call.

## OpenCode And Spring AI Implementation Notes

These notes are for later Codegeist implementation. They summarize the useful
OpenCode behavior checked in this task and the Spring AI `2.0.0-M6` guidance
checked with Context7. Do not copy OpenCode's TypeScript, AI SDK package loading,
plugin system, Models.dev catalog, or auth-store architecture into Codegeist.

OpenCode source paths checked:

- `docs/third-party/opencode/source/packages/opencode/src/provider/provider.ts`
- `docs/third-party/opencode/source/packages/opencode/src/config/provider.ts`
- `docs/third-party/opencode/source/packages/opencode/src/provider/transform.ts`
- `docs/third-party/opencode/source/packages/opencode/src/provider/models.ts`
- `docs/third-party/opencode/source/packages/web/src/content/docs/providers.mdx`
- `docs/third-party/opencode/source/packages/web/src/content/docs/models.mdx`
- `docs/third-party/opencode/source/packages/web/src/content/docs/config.mdx`

Cross-cutting OpenCode behavior to translate into Codegeist carefully:

- OpenCode builds provider state from Models.dev, user config, environment keys,
  `/connect` auth storage, and plugins. Codegeist should start smaller: merge
  Spring-bound config and `codegeist.yml`, then validate typed provider config.
- OpenCode treats `provider.<id>.options` as the main extension point and lets model
  rows add `options`, `headers`, `limit`, `cost`, capabilities, variants, and an
  overriding `provider.npm`/`provider.api`. Codegeist should keep `options` nested
  and flexible, but defer model catalogs, costs, limits, and variants until runtime
  model selection needs them.
- OpenCode filters providers with `enabled_providers` and `disabled_providers`, with
  disabled winning. Codegeist's `enabled-providers` and `disabled-providers` should
  follow that behavior when a later runtime task selects a provider.
- OpenCode provider ids and env names do not always match Codegeist's Spring-first
  names. Examples: OpenCode uses `amazon-bedrock`, `azure`, `google` or
  `google-vertex`, and `mistral`, while Codegeist uses `bedrock-converse`,
  `azure-openai`, `google-genai`, and `mistral-ai`. Do not copy OpenCode ids or env
  names blindly; map them through the T006 provider matrix.
- OpenCode resolves providers lazily in `getLanguage(...)`: SDK creation happens for
  the selected model, then results are cached by provider, package, and resolved
  options. Codegeist should likewise create only the selected Spring AI `ChatModel`
  or `ChatClient` later; config loading must not instantiate clients.
- OpenCode resolves API keys from auth storage, env vars, or config `options.apiKey`.
  Codegeist intentionally has no auth store in this slice. Use SpEL-evaluated scalar
  fields for local trusted config and redact those fields before display.
- OpenCode's custom provider docs use `@ai-sdk/openai-compatible`, `options.baseURL`,
  optional `options.apiKey`, `options.headers`, and a model map. Codegeist's
  OpenAI-compatible provider records should expose `base-url`, optional
  `completions-path`, ordinary scalar API key fields, and `options` without adding a
  dynamic package loader.
- OpenCode performs provider-specific message transforms at runtime: Anthropic and
  Bedrock reject empty content, Anthropic tool-call ordering is fragile, Mistral
  needs short sanitized tool call ids, DeepSeek reasoning messages need special
  handling, and unsupported media parts are converted to user-visible text errors.
  These are runtime notes for later provider-call tasks, not T006_04 config logic.
- OpenCode adds request wrappers for timeouts, streamed SSE chunk timeouts,
  provider-specific headers, and OpenAI/Azure request-body cleanup. Codegeist should
  keep timeout/chunk-timeout fields under `options` for now and implement request
  handling only when a provider call exists.
- Context7 confirms Spring AI can create `ChatClient` programmatically from a
  selected `ChatModel`, and OpenAI can be configured programmatically with
  `OpenAiApi.builder()`, a custom `ApiKey`, and `OpenAiChatModel.builder()`. This
  supports the Codegeist plan to map one selected provider config into Spring AI
  objects later instead of relying only on global `spring.ai.*` properties.
- Context7 also confirms OpenAI-compatible providers such as Groq and Perplexity use
  `OpenAiChatOptions` at request time, while local OpenAI-compatible endpoints can
  be reached by configuring OpenAI with a custom base URL. Codegeist should keep
  provider-specific request options mapped explicitly and gate unsupported OpenAI
  fields per provider.

Provider-specific notes for later Codegeist implementation:

| Codegeist type | OpenCode implementation evidence | Spring AI / Codegeist note |
| --- | --- | --- |
| `ollama` | OpenCode documents Ollama as a custom OpenAI-compatible provider with `npm: @ai-sdk/openai-compatible`, `options.baseURL: http://localhost:11434/v1`, and an explicit model map. No special bundled Ollama provider appears in `BUNDLED_PROVIDERS`. | Codegeist should not copy this OpenAI-compatible shortcut first. Use Spring AI's dedicated `spring-ai-starter-model-ollama` later, keep `base-url` configurable, and preserve `options.temperature`, `options.seed`, and local model lifecycle fields for `T006_05`. |
| `docker-model-runner` | OpenCode has no checked special Docker Model Runner adapter; the useful pattern is the custom OpenAI-compatible provider contract with `baseURL`, model map, optional API key, and headers. | Use Spring AI OpenAI for Docker Model Runner later. Config should require `type`, `model`, and `base-url`; runtime smoke owns model pull/cache and any dummy API key needed by Spring's OpenAI client. |
| `openai` | OpenCode bundles `@ai-sdk/openai` and its custom loader returns `sdk.responses(modelID)` for OpenAI models. Runtime code strips OpenAI item ids from request bodies unless store is enabled. | Spring AI `OpenAiApi.builder()` and `OpenAiChatModel.builder()` support programmatic construction. Keep `model`, scalar API key, optional `base-url`, organization/project, and token fields; defer Responses API vs chat-completions choice until a real OpenAI runtime task. |
| `azure-openai` | OpenCode bundles `@ai-sdk/azure`. Its custom loader resolves resource name from config, auth metadata, or `AZURE_RESOURCE_NAME`, selects chat/responses/messages based on model options, and supports a Cognitive Services base URL variant. Docs require Azure resource name, API key, and deployment whose name matches the model. | Codegeist uses Spring AI OpenAI route for Azure in this baseline. Config should keep `base-url`, scalar API key, `options.deployment-name`, `options.api-version`, and optional Microsoft Foundry fields. Do not rely on global Spring properties when later runtime needs provider-id selection. |
| `anthropic` | OpenCode bundles `@ai-sdk/anthropic` and adds `anthropic-beta` headers for interleaved thinking and fine-grained tool streaming. Runtime transforms remove empty messages, sanitize Claude tool ids, reorder problematic assistant tool-call content, and add cache-control options. | Spring AI has a dedicated Anthropic starter. Config records should capture API key, model, token/service-tier/geo/custom-header options, but runtime tasks must handle empty messages, tool ordering, thinking/cache options, and redaction explicitly. |
| `bedrock-converse` | OpenCode bundles `@ai-sdk/amazon-bedrock`. Its loader resolves region from config/env/default, profile from config/env, bearer token from env/auth with precedence over AWS credential chain, optional endpoint over baseURL, and prefixes model ids for region/cross-region inference profiles. | Spring AI Bedrock Converse supports programmatic region and credentials-provider configuration. Config should capture region, profile/default-chain/static credential fields, optional endpoint/base-url, and model access requirements. Later runtime must not call Bedrock unless account, region, model access, and safety gate are satisfied. |
| `google-genai` | OpenCode has a bundled Google AI SDK provider from Models.dev and separate Google Vertex custom loaders that resolve project/location env vars and can inject a Google auth token through custom fetch. Docs emphasize Vertex project, location, and service-account/application-default auth. | Codegeist should use Spring AI Google GenAI and keep Developer API and Vertex-style modes separate. Config should allow scalar API key for Gemini Developer API or project/location/credentials URI for Vertex mode, but no Google client creation in T006_04. |
| `deepseek` | Current Models.dev metadata routes DeepSeek through `@ai-sdk/openai-compatible` with API URL `https://api.deepseek.com` and `DEEPSEEK_API_KEY`; OpenCode runtime adds DeepSeek reasoning-message handling for assistant turns. | Spring AI has a dedicated DeepSeek starter in `2.0.0-M6`, so Codegeist should prefer it over the OpenCode OpenAI-compatible route. Config should include `base-url` and `completions-path` only as overrides, and runtime should treat reasoning model behavior as provider-specific. |
| `minimax` | Current Models.dev metadata routes MiniMax through `@ai-sdk/anthropic` with API URL `https://api.minimax.io/anthropic/v1` and `MINIMAX_API_KEY`; OpenCode transform sets MiniMax temperature/top-p/top-k defaults for some model ids. | Spring AI has a dedicated MiniMax starter. Codegeist should not mirror OpenCode's Anthropic-compatible path unless a later task proves it is better. Require explicit `model` because Spring docs/source defaults differ, and keep seed/response-format/max-token options provider-specific. |
| `mistral-ai` | OpenCode bundles `@ai-sdk/mistral` through Models.dev provider metadata. Runtime transforms sanitize Mistral tool-call ids to a short alphanumeric form and insert an assistant acknowledgement when tool messages are followed by user messages. | Spring AI has a dedicated Mistral AI starter. Config should use `mistral-ai` even though OpenCode's provider id is `mistral`; include scalar API key, `base-url`, response-format, safe-prompt, random-seed, and max-token options. Runtime tool-call quirks belong to provider-call tasks. |
| `groq` | OpenCode bundles `@ai-sdk/groq`; docs use `/connect` and API key. Models.dev supplies `GROQ_API_KEY`. | T006 keeps Groq on Spring AI OpenAI-compatible route. Config should include `base-url`, scalar API key, model, and only conservative OpenAI options; later runtime should block unsupported OpenAI request fields. |
| `nvidia` | OpenCode routes NVIDIA through `@ai-sdk/openai-compatible` with API URL `https://integrate.api.nvidia.com/v1` and `NVIDIA_API_KEY`; custom loader adds `HTTP-Referer` and `X-Title` headers. Docs also support on-prem NIM via custom base URL. | Use Spring AI OpenAI route. Config should require `options.max-tokens` before later calls because Spring AI docs call this out, and should distinguish hosted NIM from self-hosted NIM with safety gates. |
| `perplexity` | OpenCode bundles `@ai-sdk/perplexity`; Models.dev supplies `PERPLEXITY_API_KEY`. Spring AI Context7 examples show Perplexity uses OpenAI starter and `OpenAiChatOptions` for model/temperature overrides. | Codegeist should use Spring AI OpenAI route with `base-url`, `completions-path`, scalar API key, and conservative token/search options. Later runtime should not assume tool calling or multimodal support. |
| `openrouter` | OpenCode bundles `@openrouter/ai-sdk-provider`, Models.dev uses `OPENROUTER_API_KEY` and API URL `https://openrouter.ai/api/v1`, and the custom loader adds `HTTP-Referer` and `X-Title`. Docs show per-model `options.provider.order` and `allow_fallbacks`. Runtime variants use OpenRouter-specific `reasoning.effort` shape. | Codegeist can support this as a thin OpenAI-compatible profile over Spring AI OpenAI. Config should capture `base-url`, scalar API key, `options.http-referer`, `options.x-openrouter-title`, routing options, fallback control, and `:free` gating metadata. No dedicated starter or low-level client is needed. |

Implementation notes for T006_04 only:

- Prefer a common mutable `ProviderConfig` envelope for Spring/Jackson binding and
  merge behavior, then expose provider-specific normalized records from it when the
  `type` is known.
- Keep provider-specific records under `ai.codegeist.app.config` or a focused child
  package under it; do not create runtime/provider packages yet.
- Treat `api-key`, `authorization`, `token`, `secret`, `password`, `credentials`,
  and provider-specific key fields as redaction-sensitive after SpEL evaluation.
- Validation may prove config completeness for a selected provider type, but it
  must not check network availability, model existence, account balance, or billing
  status.
- Keep unknown `options` entries allowed unless a provider-specific field creates a
  safety risk. OpenCode's value here is flexibility; Codegeist should retain that
  flexibility while still validating required scalar fields.

## Non-Goals

- Do not add Spring AI provider starters here.
- Do not call Ollama or remote provider APIs.
- Do not create an encrypted credential store.
- Do not add runtime provider clients, `ChatModel`/`ChatClient` creation, provider
  factories, provider registries, adapter hierarchies, or smoke commands.
- Do not add credential-reference fields such as `api-key-env`, `profile-env`,
  `credentials-path`, or `auth-store-ref`; use ordinary scalar fields plus SpEL for
  this slice.
- Do not add provider capability discovery, model catalogs, fallback policy, or
  model-selection logic beyond validating the configured `model` scalar.
- Do not implement home-path discovery, service-level source merge orchestration,
  inheritance, or delete semantics in this slice.
- Do not introduce Spring `EnvironmentPostProcessor` or `spring.factories` loading
  for `codegeist.yml`.
- Do not create config records for `moonshot`, `qianfan`, OpenCode Zen/OpenCode Go,
  or other OpenCode-only providers in this task.

## Acceptance Criteria

- A focused test proves `CodegeistConfigService` receives config from Spring
  application YAML.
- A focused test proves `CodegeistConfigService.loadConfig(String)` maps an
  explicit YAML file to `CodegeistConfig`.
- Focused tests prove explicit YAML loading rejects blank provider ids and blank
  provider names when `name` is present.
- Focused tests prove provider `name` remains optional.
- Focused tests prove string scalar values containing `#{` are evaluated with
  Spring SpEL before mapping, while YAML keys and non-string scalars stay literal.
- Focused tests prove SpEL parse/evaluation failures include source and YAML path
  context without printing evaluated secret values.
- The config model supports common provider fields from `T006_03`: top-level
  `enabled-providers` and `disabled-providers`, provider `type`, `enabled`,
  `model`, `base-url`, `completions-path`, ordinary scalar credential fields, and
  nested `options`.
- Every provider in the supported provider config record matrix has a Java config
  record/POJO and a focused binding or normalization test.
- Validation rejects unsupported provider `type` values and refuses `moonshot`,
  `qianfan`, OpenCode Zen/OpenCode Go, and other OpenCode-only providers in this
  task.
- Validation rejects missing or blank required fields for the selected provider
  record after SpEL evaluation, without calling the provider.
- `openrouter` is accepted only as an OpenAI-compatible config profile and does not
  add a dedicated Spring AI starter or client path.
- The implementation uses field `@Autowired` for Spring injection.
- The implementation uses focused Lombok annotations only where they reduce simple
  boilerplate.
- Spring `@Service` and `@Component` classes in this slice use Lombok `@Slf4j` and
  emit concise debug messages for merged-bean creation, explicit YAML loading, and
  validation outcomes.
- `--show-config` writes only direct YAML to stdout, omits the Spring `codegeist:`
  wrapper, omits YAML document markers, redacts secret-bearing evaluated values, and
  keeps stderr empty.
- Empty config output keeps the top-level shape visible as `provider: {}`.
- Each current config model type exposes a non-mutating `merge(...)` method.
- Config merge keeps the `provider.<id>` shape, adds new providers, and lets later
  non-null provider fields replace earlier values, including typed provider record
  fields and nested `options`.
- Validation is primarily annotation-based on config POJOs, with an explicit
  `Validator` call after direct Jackson YAML loading.
- No Spring AI provider starter, provider client, provider registry, provider smoke,
  local daemon startup, model pull, or remote API call is added by this task.
- `docs/developer/architecture/architecture.md` reflects the implemented config
  service behavior.

## Verification

Start with a focused Maven selector from `app/codegeist/cli`, then run the broader
test suite:

```bash
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistConfigCommandTest,CodegeistConfigServiceTest test
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistConfigMergeTest,CodegeistProviderConfigTest test
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistConfigSpelEvaluationTest test
mvn --batch-mode --no-transfer-progress test
git --no-pager diff --check
```

## Planning Notes

- Read `docs/developer/specification/java-generation-guidance.md` before adding
  Java source.
- Read `docs/developer/specification/testing-strategy-and-agent-rules.md` before
  adding tests.
- Read `.oc_local/rules/java-coding.md` before writing Java source.
- Read `T006_03` before changing provider fields. Its provider availability matrix
  is the source for supported provider types, starter route decisions, safety gates,
  and OpenRouter's OpenAI-compatible profile.
- Keep any diagnostic command machine-readable and log-free on stdout, following
  the current `--version` precedent.
- Keep provider-specific records in the config package as data contracts only. Do
  not add Spring AI starters or client wiring until `T006_05` or a later
  provider-specific task.

## Remaining Work After Task Expansion

- The already-implemented loader still only understands provider ids and provider
  `name`.
- Implement SpEL evaluation before mapping direct YAML to `CodegeistConfig`.
- Expand `ProviderConfig` and supporting records so the supported provider matrix in
  this task can bind, validate, merge, and render provider-specific config.
- Add redaction before `--show-config` prints evaluated values that may contain API
  keys, tokens, secrets, passwords, or authorization values.
- Preserve existing empty-config output as `provider: {}` for smokes.
- Keep all provider usage lazy and out of scope; this task ends at validated config
  data, not provider availability at runtime.

## Partial Result

- `CodegeistApplication.APP_NAME` is defined as the shared application name and
  configuration prefix.
- `CodegeistConfig`, `ProviderConfig`, and `CodegeistConfigService` exist under
  `ai.codegeist.app.config`.
- `CodegeistConfigService` receives the Spring-bound `CodegeistConfig` from Spring
  with a qualifier and can load an explicit YAML path with Jackson YAML through
  `loadConfig(String configPath)`.
- `CodegeistConfig` uses Bean Validation annotations for provider map
  keys and nested provider validation.
- `ProviderConfig.name` remains optional but is rejected when it is present and
  blank.
- `CodegeistConfigService.loadConfig(String)` runs `jakarta.validation.Validator`
  after direct Jackson YAML loading and throws `CodegeistConfigValidationException`
  with source-path context when constraints fail.
- `CodegeistConfigService.mergedCodegeistConfig` currently returns the
  Spring-bound config as the primary merged config bean selected by unqualified
  `CodegeistConfig` injection.
- `CodegeistConfig.merge(...)` and `ProviderConfig.merge(...)` implement
  non-mutating model-level merge behavior. Provider entries merge by id, later
  non-null provider fields replace earlier values, and source config instances are
  not mutated or reused as merged provider instances.
- `CodegeistConfigService.toYaml(...)` renders a config object in direct
  `codegeist.yml` YAML shape without a `codegeist:` wrapper or YAML document
  marker. Empty config renders as `provider: {}`.
- `CodegeistConfigService.showConfig(...)` owns `--show-config` output writing and
  the Spring Shell command entrypoint. It prints only the current merged config
  YAML to stdout.
- `CodegeistConfigServiceTest` covers Spring application YAML binding and explicit
  YAML path loading, unqualified primary merged-config injection, YAML rendering,
  and the current annotation-backed validation rules.
- `CodegeistConfigMergeTest` covers the current non-mutating model-level merge
  rules.
- `CodegeistConfigCommandTest` covers `--show-config` command output, proving
  stdout is parseable YAML with expected merged config content and stderr is empty.
  `CodegeistConfigServiceTest` owns the no-wrapper and no-document-marker YAML
  shape assertions.
- Local Linux, Windows QEMU, and GitHub native archive smokes now run packaged
  `--show-config` and assert the current empty default config output is exactly
  `provider: {}`.
- `CodegeistConfigService` and `CodegeistConfig` use Lombok `@Slf4j` for
  debug diagnostics. `VersionCommands` also uses `@Slf4j` for command-level debug
  logging.
- `docs/developer/architecture/provider-configuration.md` documents the current
  Spring component model, direct YAML loading flow, validation strategy, tests,
  sharp edges, and future task impact. Its editable Excalidraw overview lives at
  `docs/developer/architecture/diagrams/provider-config-spring-flow.excalidraw.svg`.
