# T006_02 Create Spring AI Provider Matrix

Status: solved

Parent: `../task.md`

## Goal

Create a Spring AI provider matrix for the Codegeist provider feature.

The matrix must identify which chat model providers are supported out of the box by
the pinned Spring AI baseline and what each provider needs before Codegeist can run
connection checks.

## Scope

- Use Spring AI `2.0.0-M6`, because that is the version pinned in
  `app/codegeist/cli/pom.xml`.
- List chat model providers, not embeddings, image, audio, vector stores, or
  moderation unless they are relevant as a capability note.
- Record starter artifact ids and Spring property prefixes where available.
- Record account and credential requirements.
- Record whether each provider can be tested locally, requires a cloud account,
  or can be skipped without credentials.
- Record Spring AI capability evidence for streaming, tool calling, multimodality,
  JSON or structured output, local deployment, and OpenAI API compatibility.
- Map each Spring AI provider to a tentative Codegeist `provider.<id>.type` value.

## Initial Provider Set

Start from the Spring AI `2.0.0-M6` chat provider documentation and source tree.
Known candidates include:

- Amazon Bedrock Converse
- Anthropic
- Azure OpenAI
- DeepSeek
- Docker Model Runner
- Google GenAI
- Groq
- Mistral AI
- MiniMax
- Moonshot AI
- NVIDIA
- Ollama
- OpenAI
- Perplexity AI
- QianFan

Also check whether Vertex AI Gemini or other provider pages are present in the
exact pinned version before marking them supported for Codegeist.

## Matrix Columns

Use these columns unless implementation evidence shows a better compact shape:

| Column | Meaning |
| --- | --- |
| `codegeist-type` | Proposed `provider.<id>.type` value in `codegeist.yml`. |
| `spring-provider` | Spring AI provider name. |
| `starter` | Maven starter artifact id. |
| `property-prefix` | Spring Boot property prefix used by Spring AI. |
| `default-model` | Documented default or recommended smoke model when known. |
| `credential-source` | Env var, API key, OAuth, cloud profile, service account, or none. |
| `account-required` | Whether a provider account, billing setup, or cloud project is needed. |
| `local-test` | Whether it can be tested without remote provider credentials. |
| `streaming` | Supported by Spring AI provider docs. |
| `tool-calling` | Supported by Spring AI provider docs. |
| `structured-output` | Built-in JSON or structured output support. |
| `openai-compatible` | Whether the provider uses or supports OpenAI-compatible APIs. |
| `test-status` | `not-started`, `blocked`, `skipped`, `passed`, or `failed`. |
| `notes` | Sharp edges such as cloud region, deployment names, model access, or cost. |

The matrix below adds two evidence-driven columns:

| Column | Meaning |
| --- | --- |
| `multimodality` | Spring AI comparison or provider-page claim for non-text chat inputs or outputs. |
| `evidence` | Source identifiers from the evidence lists below. |

## Evidence Baseline

- Codegeist pins Spring AI `2.0.0-M6` in `app/codegeist/cli/pom.xml`.
- Spring AI source evidence is tied to the `spring-projects/spring-ai` tag
  `v2.0.0-M6`, especially `spring-ai-bom/pom.xml`,
  `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/*.adoc`,
  `starters/spring-ai-starter-model-*`, and
  `auto-configurations/models/spring-ai-autoconfigure-model-*`.
- Capability columns are documented Spring AI support, not Codegeist runtime
  verification. No Codegeist provider call has run yet, so supported providers
  stay at `not-started`; providers that are not core-shipped stay `skipped`.
- OpenAI-compatible providers reuse Spring AI's OpenAI starter and properties
  unless Spring AI ships a dedicated starter in `2.0.0-M6`.

## Provider Classification

| Class | Providers | Codegeist decision |
| --- | --- | --- |
| Core Spring AI chat starter | Anthropic, Amazon Bedrock Converse, DeepSeek, Google GenAI, MiniMax, Mistral AI, Ollama, OpenAI | Eligible for later Codegeist provider-specific tasks after credential strategy and smoke harness work. |
| OpenAI-compatible via OpenAI starter | Azure OpenAI/Microsoft Foundry, Docker Model Runner, Groq, NVIDIA, Perplexity | Treat as distinct Codegeist `provider.<id>.type` values, but map them through the Spring AI OpenAI integration unless later evidence justifies a dedicated adapter. |
| Not core-shipped in Spring AI `2.0.0-M6` | Moonshot AI, QianFan | Keep as future/community notes, not first-wave Codegeist providers. |

## Core Spring AI Chat Provider Matrix

| codegeist-type | spring-provider | starter | property-prefix | default-model | multimodality | credential-source | account-required | local-test | streaming | tool-calling | structured-output | openai-compatible | test-status | notes | evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `anthropic` | Anthropic Claude | `spring-ai-starter-model-anthropic` | `spring.ai.anthropic`, `spring.ai.anthropic.chat` | `claude-haiku-4-5` property default | text, PDF, image | `ANTHROPIC_API_KEY` | yes, Anthropic Console account | no | yes | yes | no built-in JSON in comparison | no | not-started | Requires remote account and API key; do not run without explicit credential setup. | [S1], [S2], [S4], [E1] |
| `bedrock-converse` | Amazon Bedrock Converse | `spring-ai-starter-model-bedrock-converse` | `spring.ai.bedrock.aws`, `spring.ai.bedrock.converse.chat` | none; model ID must be selected | text, image, video, documents | AWS profile, AWS credentials, or temporary credentials | yes, AWS account, region, and Bedrock model access | no | yes | yes | no built-in JSON in comparison | no | not-started | Prefer Converse for Codegeist chat; model access and region are required before any smoke. | [S1], [S2], [S5], [E2] |
| `deepseek` | DeepSeek | `spring-ai-starter-model-deepseek` | `spring.ai.deepseek`, `spring.ai.deepseek.chat` | `deepseek-chat` | text | `DEEPSEEK_API_KEY` | yes, DeepSeek account/API key | no | yes | yes | yes | yes | not-started | Has a dedicated Spring AI starter even though the API is OpenAI-compatible. | [S1], [S2], [S6] |
| `google-genai` | Google GenAI | `spring-ai-starter-model-google-genai` | `spring.ai.google.genai`, `spring.ai.google.genai.chat` | `gemini-2.0-flash` | text, PDF, image, audio, video | `GOOGLE_API_KEY`, `GEMINI_API_KEY`, or Vertex AI credentials | yes, Google AI Studio key or Google Cloud project | no | yes | yes | yes | no | not-started | Supports Gemini Developer API and Vertex AI modes; choose one before smoke design. | [S1], [S2], [S7], [E3] |
| `minimax` | MiniMax | `spring-ai-starter-model-minimax` | `spring.ai.minimax`, `spring.ai.minimax.chat` | docs: `abab6.5g-chat`; source default: `ABAB_5_5_Chat` | text | `MINIMAX_API_KEY` | yes, MiniMax account/API key | no | yes | yes | not in comparison; source exposes response format | yes | not-started | Keep the docs/source default mismatch visible until an implementation task picks a smoke model. | [S1], [S2], [S8] |
| `mistral-ai` | Mistral AI | `spring-ai-starter-model-mistral-ai` | `spring.ai.mistralai`, `spring.ai.mistralai.chat` | source: `MISTRAL_SMALL`; docs list `mistral-small-latest` | text, image, audio | `MISTRALAI_API_KEY` | yes, Mistral account/API key | no | yes | yes | yes | yes | not-started | Mistral also offers an OpenAI-compatible endpoint, but Spring AI ships a dedicated starter. | [S1], [S2], [S9], [E4] |
| `ollama` | Ollama | `spring-ai-starter-model-ollama` | `spring.ai.ollama`, `spring.ai.ollama.chat` | `mistral` | text, image | none for local daemon; optional base URL | no remote account; local model must be present | yes | yes | yes | yes | yes | not-started | First local Codegeist candidate; use an externally managed local daemon with the selected model already downloaded for later implementation. | [S1], [S2], [S10], [E5] |
| `openai` | OpenAI | `spring-ai-starter-model-openai` | `spring.ai.openai`, `spring.ai.openai.chat` | `gpt-5-mini` | input: text, image, audio; output: text, audio | `OPENAI_API_KEY` | yes, OpenAI account and likely billing | no, except compatible local endpoints | yes | yes | yes | yes | not-started | Remote smoke must be opt-in and skipped without credentials. | [S1], [S2], [S11], [E6] |

## OpenAI-Compatible Provider Matrix

| codegeist-type | spring-provider | starter | property-prefix | default-model | multimodality | credential-source | account-required | local-test | streaming | tool-calling | structured-output | openai-compatible | test-status | notes | evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `azure-openai` | Azure OpenAI/Microsoft Foundry via OpenAI Chat | `spring-ai-starter-model-openai` | `spring.ai.openai`, `spring.ai.openai.chat` plus Microsoft deployment fields | deployment-specific | deployment/model-dependent | Azure/OpenAI key, Microsoft Foundry credential, deployment name | yes, Azure subscription/project and model deployment | no | yes, deployment-dependent | yes, deployment-dependent | yes, deployment-dependent | yes | not-started | Spring AI `2.0.0-M6` says to use OpenAI Chat for Azure/Microsoft Foundry deployments. | [S11], [S12], [E7] |
| `docker-model-runner` | Docker Model Runner via OpenAI Chat | `spring-ai-starter-model-openai` | `spring.ai.openai`, `spring.ai.openai.chat` | example: `ai/gemma3:4B-F16` | model-dependent | any API-key string for local endpoint | no remote account; Docker Desktop or Engine required | yes | yes | model-dependent | model-dependent | yes | not-started | Local OpenAI-compatible candidate after Ollama; first pull can be slow. | [S11], [S13], [E8] |
| `groq` | Groq via OpenAI Chat | `spring-ai-starter-model-openai` | `spring.ai.openai`, `spring.ai.openai.chat` | example: `llama3-70b-8192` | text; Spring comparison lists image, provider page warns about multimodal gaps | `GROQ_API_KEY` | yes, Groq account/API key | no | yes | yes | no built-in JSON in comparison | yes, with compatibility limits | not-started | Verify base URL shape in the implementation task because Spring docs and Groq docs include slightly different path suffixes. | [S2], [S11], [S14], [E9] |
| `nvidia` | NVIDIA LLM API/NIM via OpenAI Chat | `spring-ai-starter-model-openai` | `spring.ai.openai`, `spring.ai.openai.chat` | example: `meta/llama-3.1-70b-instruct` | text, image | `NVIDIA_API_KEY` | yes, NVIDIA account/credits or self-hosted NIM entitlement | no for hosted API; self-hosted NIM is separate work | yes | model-dependent | no built-in JSON in comparison | yes | not-started | Spring docs say `max-tokens` must be set explicitly for NVIDIA. | [S2], [S11], [S15], [E10] |
| `perplexity` | Perplexity via OpenAI Chat | `spring-ai-starter-model-openai` | `spring.ai.openai`, `spring.ai.openai.chat` | example: `llama-3.1-sonar-small-128k-online` | text | `PERPLEXITY_API_KEY` | yes, Perplexity API key | no | yes | no | no built-in JSON in comparison | yes, with compatibility limits | not-started | Requires `/chat/completions`; no explicit tool calls and no multimodal messages in Spring docs. | [S2], [S11], [S16], [E11] |

## Not Core-Shipped In Spring AI 2.0.0-M6

| codegeist-type | spring-provider | starter | property-prefix | default-model | multimodality | credential-source | account-required | local-test | streaming | tool-calling | structured-output | openai-compatible | test-status | notes | evidence |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `moonshot` | Moonshot AI | none in core Spring AI `2.0.0-M6` | n/a | n/a | text in comparison, but not core-shipped | n/a | n/a | no | n/a | n/a | n/a | unspecified | skipped | Spring AI `2.0.0-M6` page says this moved to Spring AI Community. | [S2], [S17] |
| `qianfan` | QianFan | none in core Spring AI `2.0.0-M6` | n/a | n/a | text in comparison, but not core-shipped | n/a | n/a | no | n/a | n/a | n/a | no in comparison | skipped | Spring AI `2.0.0-M6` page says this moved to Spring AI Community. | [S2], [S18] |

## First Local Provider Candidate

Use `ollama` as the first local provider candidate for later implementation tasks.
It has a dedicated Spring AI starter, can run without remote provider credentials,
supports local model deployment, supports streaming/tool/structured-output
capabilities in the Spring AI comparison, and matches the existing Codegeist
direction to use a ready local Ollama daemon before remote provider account work.

Docker Model Runner is a second local candidate, but it should follow Ollama
because Spring AI reaches it through the generic OpenAI integration rather than a
dedicated provider starter.

## No-Cost Integration Test Posture

The desired long-term test shape is to cover as many provider configurations as
possible in integration tests while preventing accidental provider charges.
Provider accounts and API keys may exist locally, but their presence must not be
enough to trigger billable remote chat calls.

Use three test levels for later implementation tasks:

| Level | Remote chat call? | Default? | Purpose |
| --- | --- | --- | --- |
| `config` | no | yes | Load `codegeist.yml`, validate provider fields, resolve credential references, and build provider-specific Spring configuration without contacting hosted providers. |
| `local` | local only | yes when local service is available | Run real calls against local providers such as Ollama and, later, Docker Model Runner. |
| `remote_free` | yes, only with explicit no-cost confirmation | no | Run the smallest possible remote request only when a developer explicitly confirms that the account/key/model path will not create charges. |

Do not add a `remote_paid` mode to the ordinary integration test workflow. If a
future provider-specific task needs a paid smoke, require an explicit user
decision and keep it outside the default Maven lifecycle.

The later smoke harness should therefore distinguish these outcomes:

| Status | Meaning |
| --- | --- |
| `passed` | A no-cost local or explicitly confirmed no-cost remote check completed. |
| `skipped` | Credentials, account setup, local daemon, model access, or no-cost confirmation is absent. |
| `blocked` | A provider can only be tested by a potentially billable request and no explicit paid-test decision exists. |
| `failed` | A required provider was selected and a non-cost prerequisite was present, but the check failed. |

For remote providers, `remote_free` should use a tiny deterministic prompt,
`options.max-tokens` or provider equivalent set to the smallest useful value, and
one request per selected provider. This minimizes usage but does not by itself
prove that a provider will not bill. The no-cost guarantee must come from the
user's local account setup, free-tier state, sandbox mode, or provider-specific
no-charge endpoint.

## Codegeist Config Property Requirements

The rows below translate Spring AI and provider documentation into candidate
`codegeist.yml` fields. They are schema guidance for later T006 implementation
tasks, not fields that exist in `CodegeistConfig` today. Credentials remain
references to environment variables, profiles, or local files; secret values must
not be stored in `codegeist.yml`.

Update from `T006_06` and follow-up field removals: model selection, generic
provider options, enablement, completion-path routing, and the YAML `type`
discriminator are no longer `ProviderConfig` fields. Any `model`, `options.*`,
`enabled`, or `completions-path` examples in this solved matrix should be read as
runtime, selection-policy, or provider-feature test inputs, not provider config
fields. Provider config now stores provider access, endpoint, and credentials only;
`type` remains dispatch-only YAML input and read-only derived output.

### Candidate Generic Provider Access Fields

These fields appear across enough providers to shape the public YAML contract. Only
access fields belong as stored `ProviderConfig` fields:

| codegeist.yml field | Purpose |
| --- | --- |
| `type` | Dispatch-only discriminator that selects the provider adapter, matching the `codegeist-type` values above. |
| `base-url` | Overrides the provider endpoint for local runtimes, proxies, and OpenAI-compatible APIs. |
| `credentials.api-key-env` | References an API key environment variable for the common remote-provider case. |

Provider-specific credential shapes and cloud metadata should start as focused
access fields only when a later task proves they are needed. Runtime model,
generation knobs, enablement, and route/path selection belong outside
`ProviderConfig`.

### Provider-Specific Field Matrix

| codegeist-type | Minimum fields for connection check | Additional credential fields | Additional provider/options fields | Spring AI equivalents | Notes |
| --- | --- | --- | --- | --- | --- |
| `anthropic` | `type`, `model`, `credentials.api-key-env` | none beyond `api-key-env` | `base-url`, `options.max-tokens`, `options.timeout`, `options.max-retries`, `options.inference-geo`, `options.service-tier`, `options.custom-headers` | `spring.ai.anthropic.api-key`, `.base-url`, `.timeout`, `.max-retries`, `.custom-headers.*`, `.chat.model`, `.chat.max-tokens`, `.chat.inference-geo`, `.chat.service-tier` | Keep `top-p`, `top-k`, thinking, and cache controls provider-specific because model support changes across Claude releases. |
| `bedrock-converse` | `type`, `model`, `options.region`, one AWS credential source | `credentials.profile-env`, `credentials.profile-name`, `credentials.access-key-env`, `credentials.secret-key-env`, `credentials.session-token-env`, `credentials.credentials-path`, `credentials.configuration-path` | `options.timeout`, `options.connection-timeout`, `options.connection-acquisition-timeout`, `options.async-read-timeout`, `options.socket-timeout`, `options.max-tokens` | `spring.ai.bedrock.aws.region`, `.access-key`, `.secret-key`, `.session-token`, `.profile.*`, `.timeout`, `.connectionTimeout`, `.connectionAcquisitionTimeout`, `.asyncReadTimeout`, `.socketTimeout`, `spring.ai.bedrock.converse.chat.model`, `.max-tokens` | Account setup also requires Bedrock model access in the selected region. Prefer profile/default-chain references over static access keys. |
| `deepseek` | `type`, `model`, `credentials.api-key-env` | none beyond `api-key-env` | `base-url`, `completions-path`, `options.beta-prefix-path`, `options.max-tokens`, `options.response-format` | `spring.ai.deepseek.api-key`, `.base-url`, `.chat.api-key`, `.chat.base-url`, `.chat.completions-path`, `.chat.beta-prefix-path`, `.chat.model`, `.chat.maxTokens`, `.chat.response-format` | `deepseek-reasoner` has reasoning output behavior; keep reasoning-specific handling out of the generic model. |
| `google-genai` | `type`, `model`, one auth mode: `credentials.api-key-env` or `options.project-id` plus `options.location` | `credentials.api-key-env`, `credentials.credentials-uri-env`, future `credentials.auth-store-ref` | `options.auth-mode`, `options.project-id`, `options.location`, `options.response-mime-type`, `options.google-search-retrieval`, `options.include-server-side-tool-invocations`, cache options | `spring.ai.google.genai.api-key`, `.project-id`, `.location`, `.credentials-uri`, `.chat.model`, `.chat.response-mime-type`, `.chat.google-search-retrieval`, `.chat.include-server-side-tool-invocations` | `api-key` mode uses Gemini Developer API; project/location mode targets Vertex AI. Keep `auth-mode` provider-specific until implementation proves the shape. |
| `minimax` | `type`, `model`, `credentials.api-key-env` | none beyond `api-key-env` | `base-url`, `options.max-tokens`, `options.response-format`, `options.seed`, `options.mask-sensitive-info` | `spring.ai.minimax.api-key`, `.base-url`, `.chat.api-key`, `.chat.base-url`, `.chat.model`, `.chat.maxTokens`, `.chat.response-format`, `.chat.seed` | Spring docs and source disagree on the default model; require explicit `model` in Codegeist config before remote smoke. |
| `mistral-ai` | `type`, `model`, `credentials.api-key-env` | none beyond `api-key-env` | `base-url`, `options.max-tokens`, `options.safe-prompt`, `options.random-seed`, `options.response-format` | `spring.ai.mistralai.api-key`, `.base-url`, `.chat.api-key`, `.chat.base-url`, `.chat.model`, `.chat.maxTokens`, `.chat.safePrompt`, `.chat.randomSeed`, `.chat.responseFormat` | Use the dedicated Spring AI starter first; use OpenAI-compatible mode only if a later task chooses that path explicitly. |
| `ollama` | `type`, `model`, `base-url` | none | `options.keep-alive`, `options.format`, `options.seed`, `options.temperature`, `options.num-ctx`, `options.num-gpu`, `options.num-thread`, `options.pull-model-strategy`, `options.pull-timeout` | `spring.ai.ollama.base-url`, `.chat.model`, `.chat.keep_alive`, `.chat.format`, `.chat.seed`, `.chat.temperature`, `.init.pull-model-strategy`, `.init.timeout` | `base-url` can default to `http://localhost:11434`, but explicit config makes smokes easier to reproduce. |
| `openai` | `type`, `model`, `credentials.api-key-env` | none beyond `api-key-env` | `base-url`, `completions-path`, `options.organization-id`, `options.project-id`, `options.max-tokens`, `options.max-completion-tokens`, `options.service-tier`, `options.extra-body` | `spring.ai.openai.api-key`, `.base-url`, `.organization-id`, `.chat.api-key`, `.chat.base-url`, `.chat.model`, `.chat.maxTokens`, `.chat.maxCompletionTokens`, `.chat.service-tier`, `.chat.extra-body` | `max-tokens` and `max-completion-tokens` are mutually exclusive for some OpenAI model families. |
| `azure-openai` | `type`, `model`, `base-url`, `credentials.api-key-env` or future token credential | `credentials.api-key-env`, future managed identity/auth-store reference | `completions-path`, `options.deployment-name`, `options.api-version`, `options.microsoft-foundry`, `options.microsoft-foundry-service-version` | Spring AI `2.0.0-M6` Azure page redirects to OpenAI Chat; OpenAI source exposes Microsoft Foundry/deployment fields through OpenAI properties/options. | Endpoint, deployment, and API-version handling are Azure-specific; keep them under `options.*` until tested. |
| `docker-model-runner` | `type`, `model`, `base-url` | none; adapter may supply a dummy API key if Spring's OpenAI client requires one | `options.extra-body`, `options.max-tokens`, `options.temperature` | `spring.ai.openai.base-url`, `.api-key`, `.chat.model`, `.chat.extra-body` | Local runtime via OpenAI-compatible API; model pull/cache lifecycle belongs to smoke setup. |
| `groq` | `type`, `model`, `base-url`, `credentials.api-key-env` | none beyond `api-key-env` | `options.max-tokens`, `options.temperature`, `options.top-p` | `spring.ai.openai.api-key`, `.base-url`, `.chat.model`, `.chat.maxTokens` | Groq is mostly OpenAI-compatible but has unsupported OpenAI request fields; keep unsupported fields provider-gated. |
| `nvidia` | `type`, `model`, `base-url`, `credentials.api-key-env`, `options.max-tokens` | none beyond `api-key-env` | OpenAI-compatible options that the selected NVIDIA model supports | `spring.ai.openai.api-key`, `.base-url`, `.chat.model`, `.chat.maxTokens` | Spring AI docs say NVIDIA requires `max-tokens`; make it required for this provider type. |
| `perplexity` | `type`, `model`, `base-url`, `completions-path`, `credentials.api-key-env` | none beyond `api-key-env` | `options.max-tokens`, `options.temperature`, `options.top-p` | `spring.ai.openai.api-key`, `.base-url`, `.chat.completions-path`, `.chat.model`, `.chat.maxTokens` | Perplexity does not support explicit function/tool calling or multimodal messages in Spring AI docs. |
| `moonshot` | none for core Spring AI `2.0.0-M6` | n/a | n/a | Spring AI `2.0.0-M6` page says Moonshot moved to Spring AI Community. | Do not add Codegeist model fields until a later task adopts and verifies the community provider. |
| `qianfan` | none for core Spring AI `2.0.0-M6` | future community shape includes `credentials.api-key-env` plus `credentials.secret-key-env` | future community shape includes `base-url`, `options.max-tokens`, `options.temperature` | Spring AI `2.0.0-M6` page says QianFan moved to Spring AI Community; community source uses `spring.ai.qianfan.api-key`, `.secret-key`, `.base-url`, and `.chat.options.model`. | Keep out of first-wave Codegeist model because it is no longer core-shipped in the pinned Spring AI baseline. See [S19]. |

### Candidate YAML Shape

This is illustrative only; do not treat it as implemented parser behavior yet.

```yaml
provider:
  ollama:
    type: ollama
    model: llama3.2:1b
    base-url: http://localhost:11434
    options:
      temperature: 0
      seed: 42

  anthropic:
    type: anthropic
    model: claude-haiku-4-5
    credentials:
      api-key-env: ANTHROPIC_API_KEY
    options:
      max-tokens: 256

  bedrock:
    type: bedrock-converse
    model: us.anthropic.claude-haiku-4-5-20251001-v1:0
    credentials:
      profile-env: AWS_PROFILE
    options:
      region: us-east-1

  perplexity:
    type: perplexity
    model: llama-3.1-sonar-small-128k-online
    base-url: https://api.perplexity.ai
    # Runtime route/path selection; not a ProviderConfig field.
    completions-path: /chat/completions
    credentials:
      api-key-env: PERPLEXITY_API_KEY
```

## Source Evidence

| ID | Evidence |
| --- | --- |
| [S1] | Spring AI `v2.0.0-M6` BOM and starter source: `https://github.com/spring-projects/spring-ai/blob/v2.0.0-M6/spring-ai-bom/pom.xml` and `https://github.com/spring-projects/spring-ai/tree/v2.0.0-M6/starters`. |
| [S2] | Spring AI chat model comparison at `v2.0.0-M6`: `https://github.com/spring-projects/spring-ai/blob/v2.0.0-M6/spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/comparison.adoc`. |
| [S4] | Spring AI Anthropic chat docs and properties: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/anthropic-chat.adoc`, `auto-configurations/models/spring-ai-autoconfigure-model-anthropic/.../AnthropicChatProperties.java`. |
| [S5] | Spring AI Bedrock Converse docs and AWS properties: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/bedrock-converse.adoc`, `auto-configurations/models/spring-ai-autoconfigure-model-bedrock-ai/.../BedrockAwsConnectionProperties.java`. |
| [S6] | Spring AI DeepSeek docs and properties: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/deepseek-chat.adoc`, `auto-configurations/models/spring-ai-autoconfigure-model-deepseek/.../DeepSeekChatProperties.java`. |
| [S7] | Spring AI Google GenAI docs and starter: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/google-genai-chat.adoc`, `starters/spring-ai-starter-model-google-genai`. |
| [S8] | Spring AI MiniMax docs and properties: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/minimax-chat.adoc`, `auto-configurations/models/spring-ai-autoconfigure-model-minimax/.../MiniMaxChatProperties.java`. |
| [S9] | Spring AI Mistral AI docs and properties: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/mistralai-chat.adoc`, `auto-configurations/models/spring-ai-autoconfigure-model-mistral-ai/.../MistralAiChatProperties.java`. |
| [S10] | Spring AI Ollama docs and properties: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/ollama-chat.adoc`, `auto-configurations/models/spring-ai-autoconfigure-model-ollama/.../OllamaChatProperties.java`. |
| [S11] | Spring AI OpenAI docs and properties: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/openai-chat.adoc`, `auto-configurations/models/spring-ai-autoconfigure-model-openai/.../OpenAiChatProperties.java`. |
| [S12] | Spring AI Azure OpenAI note: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/azure-openai-chat.adoc`. |
| [S13] | Spring AI Docker Model Runner docs: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/dmr-chat.adoc`. |
| [S14] | Spring AI Groq docs: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/groq-chat.adoc`. |
| [S15] | Spring AI NVIDIA docs: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/nvidia-chat.adoc`. |
| [S16] | Spring AI Perplexity docs: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/perplexity-chat.adoc`. |
| [S17] | Spring AI Moonshot page: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/moonshot-chat.adoc`. |
| [S18] | Spring AI QianFan page: `spring-ai-docs/src/main/antora/modules/ROOT/pages/api/chat/qianfan-chat.adoc`. |
| [S19] | Spring AI Community QianFan source, outside the pinned core baseline: `https://github.com/spring-ai-community/qianfan/blob/main/spring-ai-autoconfigure-model-qianfan/src/main/java/org/springaicommunity/qianfan/autoconfigure/QianFanChatProperties.java` and `https://github.com/spring-ai-community/qianfan/blob/main/spring-ai-autoconfigure-model-qianfan/src/main/java/org/springaicommunity/qianfan/autoconfigure/QianFanConnectionProperties.java`. |

## External Provider Evidence

These official provider sources were checked to validate account, credential,
local-runtime, or OpenAI-compatibility notes. They supplement Spring AI source
evidence and do not mark any Codegeist provider as locally verified.

| ID | Evidence |
| --- | --- |
| [E1] | Anthropic API getting-started docs: `https://docs.anthropic.com/en/api/getting-started`; Spring AI also links to Anthropic Console API key pages. |
| [E2] | Amazon Bedrock getting-started docs: `https://docs.aws.amazon.com/bedrock/latest/userguide/getting-started.html`. |
| [E3] | Google Gemini API key docs: `https://ai.google.dev/gemini-api/docs/api-key`. |
| [E4] | Mistral quickstart and account/API key entrypoints: `https://docs.mistral.ai/getting-started/quickstart/`. |
| [E5] | Ollama download/install docs: `https://ollama.com/download`. |
| [E6] | OpenAI quickstart/API key docs: `https://platform.openai.com/docs/quickstart`. |
| [E7] | Microsoft Foundry/Azure OpenAI overview: `https://learn.microsoft.com/en-us/azure/ai-foundry/openai/overview`. |
| [E8] | Docker Model Runner docs: `https://docs.docker.com/desktop/features/model-runner/`. |
| [E9] | Groq OpenAI compatibility docs: `https://console.groq.com/docs/openai`. |
| [E10] | NVIDIA NIM LLM API docs: `https://docs.api.nvidia.com/nim/reference/llm-apis`. |
| [E11] | Perplexity getting-started docs: `https://docs.perplexity.ai/guides/getting-started`. |

## Future Upgrade Notes

- Vertex AI Gemini is not a separate core chat starter in the Spring AI
  `2.0.0-M6` source tree checked for this task. Google GenAI is the core Spring AI
  chat path that can use Gemini Developer API or Vertex AI mode.
- `spring-ai-starter-model-bedrock` also exists in the source tree, but Codegeist
  should target `bedrock-converse` for chat first because the Spring AI Bedrock
  Converse docs recommend Converse for chat conversation use cases.
- If a later Spring AI version brings Moonshot, QianFan, Vertex AI Gemini chat, or
  another provider back into core starters, add them as upgrade-driven rows rather
  than treating them as `2.0.0-M6` baseline support.

## Non-Goals

- Do not implement provider calls in this child task.
- Do not add provider dependencies to `pom.xml` in this child task.
- Do not create provider accounts yet.
- Do not treat non-chat model support as ready for Codegeist chat workflows.
- Do not mark a provider verified only because the documentation says it exists.

## Acceptance Criteria

- The matrix is tied explicitly to Spring AI `2.0.0-M6`.
- Every listed provider has a source link or source-file citation.
- The matrix distinguishes documented support from locally verified support.
- The matrix identifies which providers require account creation and which need
  billing, cloud projects, model deployment, or region configuration.
- The matrix identifies the first local provider candidate for implementation.
- The matrix identifies candidate `codegeist.yml` and `ProviderConfig` fields
  needed before later connection-check implementation.
- The matrix can drive later provider-specific child tasks without redoing the
  basic discovery work.

## Verification

Documentation-only verification:

```bash
git --no-pager diff --check
```

## Solve Result

- The Spring AI provider matrix is complete for the pinned `2.0.0-M6` baseline.
- The matrix distinguishes core Spring AI chat starters, OpenAI-compatible
  providers that reuse the Spring AI OpenAI starter, and providers that are not
  core-shipped in the pinned baseline.
- Each listed provider has source evidence, account/credential requirements,
  local-test status, documented capability flags, and a tentative Codegeist
  `provider.<id>.type` value.
- Ollama is the first local provider candidate for later implementation tasks;
  Docker Model Runner is a later local OpenAI-compatible candidate.
- Candidate `codegeist.yml` and future `ProviderConfig` fields are documented so
  later loading and connection-smoke tasks can model provider-specific
  credentials, endpoints, cloud metadata, and option fields without redoing the
  provider discovery.
- The no-cost integration-test posture is documented: default tests should cover
  config and local providers, while hosted provider calls require explicit
  `remote_free` selection and local no-cost confirmation.

## Verification Result

- `git --no-pager diff --check` passed.

## Planning Notes

- Prefer official Spring AI reference docs and the `spring-projects/spring-ai`
  `v2.0.0-M6` source tree over blog posts or examples.
- If a newer Spring AI version documents a provider that `2.0.0-M6` does not ship,
  list it only as a future upgrade note.
