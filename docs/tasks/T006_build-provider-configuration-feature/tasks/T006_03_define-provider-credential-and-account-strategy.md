# T006_03 Define Provider SpEL, Account, And Availability Strategy

Status: solved

Parent: `../task.md`

## Goal

Define the smallest useful strategy for evaluating Spring SpEL expressions inside
`codegeist.yml` provider configuration and for classifying provider account,
free-tier, Spring AI starter, and runtime availability requirements before
Codegeist can configure and use providers on demand.

This task deliberately narrows the previous credential/account strategy scope.
Codegeist should first prove a simple local-config evaluation model and record
remote account safety facts before adding credential reference schemas, account
setup flows, OAuth, auth stores, or model selection rules.

## Scope

- Define `codegeist.yml` as trusted local input.
- Define Spring SpEL as the only template/expression mechanism for this slice.
- Define that the first SpEL implementation should not add a Codegeist-specific
  sandbox, whitelist, or expression restriction.
- Define a minimal parser pipeline for evaluating scalar YAML values with SpEL.
- Keep provider examples limited to essential provider connection/config values.
- Call out sensitive-output and secret-in-git risks that later commands must handle.
- Catalog each provider's account setup, API free-tier/no-cost posture, billing or
  credit requirements, safe smoke posture, and Spring AI starter route.
- Distinguish API free tiers from consumer web-chat free plans.
- Define what makes a provider available in Codegeist: build-time dependencies,
  config fields, safety gates, runtime selection, and on-demand Spring AI client
  creation.
- Identify provider-specific blockers that must be solved before a provider can be
  configured and used on demand.
- Compare OpenCode and Codegeist provider support so later provider-specific tasks
  can see which providers are already broad OpenCode behavior versus first-wave
  Codegeist scope.

## SpEL-Only Decision

Use Spring SpEL directly for config expressions. Do not add a separate Codegeist
credential-reference language such as `api-key-env`, `profile-env`, or
`auth-store-ref` in this task.

After SpEL evaluation, values are ordinary config values. If a SpEL expression
reads an environment variable, that secret value is materialized in memory as part
of the normalized config. That is acceptable for this trusted-local-input slice,
but commands that print resolved config must be treated as sensitive output.

The first implementation should not restrict SpEL features. It may use standard
Spring SpEL capabilities such as type references, method calls, property access,
operators, and environment access patterns supported by the chosen evaluation
context.

## Evaluation Contract

Treat `codegeist.yml` as trusted local input, similar to a shell profile or local
developer script. A user who can edit the file can run arbitrary Spring SpEL in
the current JVM process, so this first slice should not pretend that SpEL is a
safe format for untrusted config. Do not load `codegeist.yml` from remote URLs or
from repository templates that may contain real secrets.

Use this expression syntax for the first implementation:

- Evaluate only YAML scalar values. Do not evaluate YAML mapping keys, provider
  ids, list indexes, comments, anchors, aliases, or document structure.
- Evaluate only string scalar values that contain the Spring template prefix
  `#{`. Leave plain strings without `#{` unchanged.
- Use Spring's template expression form with prefix `#{` and suffix `}` for
  embedded expressions. The intended API is `SpelExpressionParser` plus
  `ParserContext.TEMPLATE_EXPRESSION` for strings that mix literal text and SpEL.
- When the whole scalar is one expression such as `"#{1 + 1}"`, evaluate the
  inner expression and keep the returned value as the scalar value when practical.
  This allows later config fields to bind booleans, numbers, or `null` instead of
  only strings.
- When a scalar mixes literal text and expressions, evaluate it as a template and
  keep the result as a string.
- Leave non-string YAML scalars such as booleans, numbers, and `null` unchanged.
  If a user wants SpEL for those values, they must write the value as a string
  expression.

Use a plain Spring `StandardEvaluationContext` for the first implementation.
`SimpleEvaluationContext` is intentionally not the default because it does not
support type references, while this task allows expressions such as
`T(java.lang.System).getenv(...)`. Do not add a Codegeist-specific sandbox,
whitelist, denylist, custom type locator, or expression language. Also do not add
Codegeist helper variables, functions, or an `ApplicationContext` bean resolver in
this first slice; those would be new config contracts and should get a focused
task if they become necessary.

The first implementation should support these local-environment patterns through
ordinary SpEL instead of a custom credential-reference schema:

```yaml
provider:
  anthropic:
    api-key: "#{T(java.lang.System).getenv('ANTHROPIC_API_KEY')}"

  ollama:
    base-url: "#{T(java.lang.System).getenv('OLLAMA_BASE_URL') ?: 'http://localhost:11434'}"
```

Missing values are ordinary SpEL results. For example,
`System.getenv('MISSING')` can return `null`, and config authors can use SpEL's
Elvis operator for defaults. Codegeist should not add a separate missing-env
error layer in this task. Required-field failures belong to Bean Validation after
mapping.

Expression parse or evaluation failures should fail config loading for the
current source. The user-facing failure should include the config source path and
the YAML value path when available, such as `provider.anthropic.api-key`, without
printing the evaluated value.

## Parser Direction

Later implementation work should use a small phased parser instead of treating the
raw YAML file as the final config object immediately.

Planned minimal pipeline for each `codegeist.yml` source:

```text
read YAML source into a YAML tree
-> recursively evaluate Spring SpEL in scalar string values, not keys
-> map evaluated values to CodegeistConfig
-> run Bean Validation
```

Merge should happen only after each source has been evaluated, mapped, and
validated. The source order remains the direction from `T006_01`: Spring-bound
application defaults first, home `~/.config/codegeist/codegeist.yml` next, and an
explicit startup config path last when that source exists. Parsing, evaluating,
and validating config must not create Spring AI clients, start local providers,
or call remote providers.

This task does not require `vars`, dotenv loading, includes, `.dist` files,
remote includes, or source-generation helpers. Those can be considered only after
the minimal SpEL path is implemented and tested.

## Minimal Example

Illustrative only; these fields do not all exist in `CodegeistConfig` today.

```yaml
provider:
  anthropic:
    type: anthropic
    api-key: "#{T(java.lang.System).getenv('ANTHROPIC_API_KEY')}"

  ollama:
    type: ollama
    base-url: "#{T(java.lang.System).getenv('OLLAMA_BASE_URL') ?: 'http://localhost:11434'}"
```

The example intentionally omits model selection. Model choice belongs to a later
provider/model task and may depend on runtime context, user command selection,
task type, model capabilities, or future policy.

The example also keeps expressions in values, not keys. Provider ids such as
`anthropic` and `ollama` should remain literal YAML keys in the first slice so the
parser does not need to rewrite map structure before validation.

## Safety Notes

- Do not commit real API keys, OAuth tokens, cloud credentials, service account
  files, or generated secret material.
- SpEL may materialize secrets in normalized config values. Treat normalized config
  as sensitive when it contains provider secrets.
- Future `--show-config`, diagnostic, and smoke output must avoid printing
  materialized secret values.
- Redaction should run after SpEL evaluation and before any diagnostic or
  user-visible config display. Sensitive paths include names containing
  `api-key`, `token`, `secret`, `authorization`, `password`, or `credentials`.
- Parsing config must not by itself call a local or remote provider.
- Remote provider smokes remain a separate `T006_06` concern and must not run only
  because SpEL produced an API key or endpoint value.

## Spring AI Starter Baseline

Use the pinned Spring AI `2.0.0-M6` baseline from `app/codegeist/cli/pom.xml`.
These are Spring AI starters, not arbitrary provider-owned Spring Boot starters.
Many providers have dedicated Spring AI model starters, while several
OpenAI-compatible providers should reuse Spring AI's OpenAI starter.

| Provider group | Codegeist provider types | Spring AI dependency direction |
| --- | --- | --- |
| Dedicated Spring AI chat starters | `anthropic`, `bedrock-converse`, `deepseek`, `google-genai`, `minimax`, `mistral-ai`, `ollama`, `openai` | Use each provider's `spring-ai-starter-model-*` artifact from the pinned Spring AI baseline. |
| OpenAI-compatible through Spring AI OpenAI | `azure-openai`, `docker-model-runner`, `groq`, `nvidia`, `perplexity` | Use `spring-ai-starter-model-openai` first unless later source evidence proves a dedicated starter is available and better in the pinned baseline. |
| Not core-shipped in Spring AI `2.0.0-M6` | `moonshot`, `qianfan` | Keep out of first-wave implementation until an upgrade or community-starter task adopts them explicitly. |

## OpenCode And Codegeist Provider Support Comparison

This table compares current OpenCode support with the current Codegeist plan.
OpenCode support is broad because OpenCode uses the AI SDK and Models.dev for
`75+` providers, bundles several AI SDK provider packages, supports `/connect`,
and can configure custom OpenAI-compatible providers. Codegeist support is still a
planned T006 feature: no provider call is implemented yet, and provider rows below
describe the intended first-wave route or out-of-scope status.

| Provider or group | OpenCode support | Codegeist support status | Codegeist route or decision |
| --- | --- | --- | --- |
| OpenCode Zen / OpenCode Go | Supported as OpenCode-managed providers with `/connect`, billing, and recommended model lists. | Not a Codegeist target. | Codegeist should not copy OpenCode-managed provider products; use direct providers or local providers first. |
| OpenAI | Supported through the bundled `@ai-sdk/openai` provider and Models.dev model metadata. | First-wave planned. | Dedicated `spring-ai-starter-model-openai`; remote calls blocked unless credits/no-cost posture is explicitly confirmed. |
| Anthropic | Supported through the bundled `@ai-sdk/anthropic` provider and `/connect`. | First-wave planned. | Dedicated `spring-ai-starter-model-anthropic`; hosted calls blocked by default. |
| Amazon Bedrock | Supported and documented as `amazon-bedrock` with AWS env/profile/token options. | First-wave planned. | Dedicated `spring-ai-starter-model-bedrock-converse`; requires region, model access, and AWS credential source. |
| Azure OpenAI / Microsoft Foundry | Supported through the bundled `@ai-sdk/azure` provider and Azure resource/deployment config. | First-wave OpenAI-compatible planned. | Reuse `spring-ai-starter-model-openai` with Azure-shaped endpoint, deployment, and API-version options. |
| Google Gemini / Google Vertex | Supported through bundled `@ai-sdk/google`, `@ai-sdk/google-vertex`, and Vertex Anthropic providers. | First-wave planned as `google-genai`. | Dedicated `spring-ai-starter-model-google-genai`; keep Gemini Developer API and Vertex/cloud billing paths separate. |
| Mistral AI | Supported through the bundled `@ai-sdk/mistral` provider. | First-wave planned. | Dedicated `spring-ai-starter-model-mistral-ai`; `remote_free` only after Studio Free-mode confirmation. |
| DeepSeek | Supported through OpenCode provider directory and Models.dev/OpenAI-compatible behavior. | First-wave planned. | Dedicated `spring-ai-starter-model-deepseek`; blocked unless granted balance/no-cost confirmation exists. |
| MiniMax | Supported through OpenCode provider directory and OpenCode Zen/Go model families. | First-wave planned. | Dedicated `spring-ai-starter-model-minimax`; blocked by default because no durable free chat API tier was confirmed. |
| Groq | Supported through the bundled `@ai-sdk/groq` provider. | First-wave OpenAI-compatible planned. | Reuse `spring-ai-starter-model-openai`; block until console confirms no-cost plan or credits. |
| Perplexity | Supported through the bundled `@ai-sdk/perplexity` provider. | First-wave OpenAI-compatible planned. | Reuse `spring-ai-starter-model-openai`; blocked by default because official pricing is request/token/tool based. |
| NVIDIA hosted NIM | Supported indirectly by OpenCode model/provider catalogs and OpenAI-compatible routes; no dedicated bundled NVIDIA SDK was found in the checked `BUNDLED_PROVIDERS` map. | First-wave OpenAI-compatible planned. | Reuse `spring-ai-starter-model-openai`; require `options.max-tokens` and block until hosted credit/self-hosted posture is confirmed. |
| Ollama and local OpenAI-compatible runtimes | Supported through local/custom OpenAI-compatible provider config and local-provider docs. | First real provider path planned. | Dedicated `spring-ai-starter-model-ollama`; `T006_05` should prove local on-demand use first. |
| Docker Model Runner | Supported by OpenCode's custom OpenAI-compatible pattern when pointed at a local OpenAI-compatible API. | Second local OpenAI-compatible candidate. | Reuse `spring-ai-starter-model-openai`; local safety gate, model pull/cache lifecycle required. |
| OpenRouter | Supported directly through bundled `@openrouter/ai-sdk-provider`; OpenRouter also exposes an OpenAI-compatible `/api/v1/chat/completions` API and `:free` model variants. | Not currently in the first-wave matrix, but technically supportable. | No dedicated Spring AI starter needed; add as a thin `openrouter` profile over `spring-ai-starter-model-openai` if Codegeist wants OpenRouter-specific headers, routing, or `:free` safety gates. |
| Moonshot AI / Kimi | Supported in OpenCode provider docs and model catalogs. | Out of first-wave Codegeist scope. | Not core-shipped in Spring AI `2.0.0-M6`; future task may adopt a community or OpenAI-compatible route. |
| QianFan | Potentially reachable through OpenCode/Models.dev-style provider catalogs or custom configuration, but not a checked bundled OpenCode SDK row. | Out of first-wave Codegeist scope. | Not core-shipped in Spring AI `2.0.0-M6`; future community/provider task required. |
| Other OpenCode providers such as xAI, Cerebras, Cohere, Together AI, Vercel, Alibaba, GitLab Duo, GitHub Copilot, Venice, Cloudflare, Hugging Face, DeepInfra, and Helicone | OpenCode supports many of these through bundled SDK packages, provider-directory entries, Models.dev, plugins, subscriptions, or custom OpenAI-compatible config. | Not T006 first-wave. | Add only when a focused Codegeist task needs the provider and can identify the Spring AI route, config fields, account posture, and safety gate. |

OpenRouter does not need a special low-level Codegeist client because its chat API
is OpenAI-compatible. A dedicated Codegeist `type: openrouter` is still useful as a
thin profile when Codegeist wants OpenRouter-specific UX or policy, especially
`HTTP-Referer`, `X-OpenRouter-Title`, `provider` routing options, and `:free`
model gating.

## Provider Account And Free-Tier Catalog

Last checked: 2026-06-01.

The catalog below is deliberately conservative. A provider can be listed as having
an official free API tier only when the checked official docs clearly expose an API
free path. Consumer web-chat free plans, starter credits, vouchers, and trial
balances do not make a provider safe for default remote smokes. To keep the table
readable, the provider rows use official source references instead of repeating
raw URL columns; the references resolve to official account, pricing, quota, or
starter sources in `Official Source References`.

| codegeist-type | spring-ai-route | account steps | credential source | free/no-cost-state | remote_free eligibility | safe-default-smoke | last-checked | sources and notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `anthropic` | Dedicated `spring-ai-starter-model-anthropic`. | Create an Anthropic Console account, create an API key, and confirm API billing or credits before a call. | `ANTHROPIC_API_KEY`. | `paid-only` for API use checked here; Anthropic's public `Free` plan evidence is the Claude chat product, not API permission. | No by default. Only allow if the console explicitly shows non-billable credits for the selected API/model. | `blocked` for remote calls; `config-only` before credentials. | 2026-06-01 | [SA1], [A1], [A2]. Keep Claude.ai free-plan evidence out of API free-tier decisions. |
| `bedrock-converse` | Dedicated `spring-ai-starter-model-bedrock-converse`. | Create or use an AWS account, configure IAM/API-key credentials, select region, request/confirm Bedrock model access, and confirm billing posture. | AWS profile, temporary credentials, Bedrock API key, or environment-backed AWS credentials. | `paid-only` for model inference in checked pricing; AWS account/free-credit promotions are not a durable Bedrock API free tier. | No by default. Only allow if an explicit AWS account credit/sandbox decision exists for the selected region/model. | `blocked` for remote calls; `config-only` before AWS setup. | 2026-06-01 | [SA1], [B1], [B2]. Region and model access are prerequisites, not just credentials. |
| `deepseek` | Dedicated `spring-ai-starter-model-deepseek`. | Create a DeepSeek platform account, create an API key, and confirm topped-up or granted balance. | `DEEPSEEK_API_KEY`. | `trial-credit-only`; docs describe charges deducted from topped-up or granted balance. | Only with explicit no-cost confirmation that granted balance covers the selected smoke request. | `blocked` for remote calls; `config-only` before balance confirmation. | 2026-06-01 | [SA1], [D1], [D2]. DeepSeek is OpenAI/Anthropic-format compatible but has a dedicated Spring AI starter in the pinned baseline. |
| `google-genai` | Dedicated `spring-ai-starter-model-google-genai`. | Use Google AI Studio, accept terms, create or import a Google Cloud project, create an API key, and pick Developer API or Vertex-style project/location mode. | `GOOGLE_API_KEY`, `GEMINI_API_KEY`, or Vertex/Google Cloud credentials. | `official-free-api-tier` for Gemini Developer API routes only; Vertex-style cloud mode remains cloud/billing governed. | Yes, only for an explicitly selected Gemini API free-tier model/route with current quota confirmation. | `remote_free` only after explicit confirmation; otherwise `config-only` or `blocked` for Vertex/cloud mode. | 2026-06-01 | [SA1], [G1], [G2], [G3]. Keep Developer API and Vertex AI billing paths separate. |
| `minimax` | Dedicated `spring-ai-starter-model-minimax`. | Create a MiniMax platform account, generate an API key, and confirm recharge/package or account balance. | `MINIMAX_API_KEY`. | `paid-only` from checked price/package docs; no durable free chat API tier was confirmed. | No by default. | `blocked` for remote calls; `config-only` before account balance confirmation. | 2026-06-01 | [SA1], [MM1], [MM2]. Checked docs are official but some pricing/account pages are Chinese-language platform docs. |
| `mistral-ai` | Dedicated `spring-ai-starter-model-mistral-ai`. | Activate Mistral Studio, generate an API key, and confirm whether the selected account remains in Free mode or needs billing. | `MISTRALAI_API_KEY`. | `official-free-api-tier` for Studio Free mode as documented by the quickstart entrypoint, subject to current console limits. | Yes, only after confirming the account is in Free mode and the selected model/route is no-cost. | `remote_free` after explicit confirmation; otherwise `config-only`. | 2026-06-01 | [SA1], [MI1], [MI2]. Keep Studio Free-mode evidence separate from enterprise or paid deployment modes. |
| `ollama` | Dedicated `spring-ai-starter-model-ollama`. | Install/start Ollama outside the test, download the selected local model before the test starts, and confirm local resource availability. | None for local daemon; optional base URL. | `local-free`; costs are local compute, disk, and download time. | Not a hosted remote provider. | `local`. | 2026-06-01 | [SA1], [O1]. First Codegeist real-provider candidate. |
| `openai` | Dedicated `spring-ai-starter-model-openai`. | Create an OpenAI platform account, create an API key, and confirm billing or available free test/credits before a call. | `OPENAI_API_KEY`. | `trial-credit-only`; official quickstart mentions a free test API request, but durable API use should assume billing/credits. | Only with explicit confirmation that current account credits cover the selected smoke. | `blocked` for remote calls; `config-only` before billing/credit confirmation. | 2026-06-01 | [SA1], [OA1], [OA2]. ChatGPT free plans are not API free-tier permission. |
| `azure-openai` | Reuse `spring-ai-starter-model-openai` for Azure OpenAI/Microsoft Foundry-shaped OpenAI-compatible calls in this baseline. | Create/use Azure subscription, create Foundry/OpenAI resource, deploy/select model, choose region/deployment type, create credentials, and confirm subscription billing or credits. | Azure/OpenAI API key, endpoint/deployment config, or future Azure credential path. | `trial-credit-only`; Azure has free-account entrypoints, but Azure OpenAI pricing is pay-as-you-go/provisioned/batch by deployment. | Only with explicit subscription credit/sandbox confirmation for selected deployment. | `blocked` for remote calls; `config-only` before Azure setup. | 2026-06-01 | [SA1], [AZ1], [AZ2]. Deployment name, endpoint, API version, and region are required. |
| `docker-model-runner` | Reuse `spring-ai-starter-model-openai` against Docker Model Runner's local OpenAI-compatible API. | Install supported Docker Desktop or Docker Engine, enable/use Model Runner, pull selected model from Docker Hub, Hugging Face, or OCI registry. | Usually none or dummy API key if the OpenAI client requires one. | `local-free`; costs are local compute, storage, downloads, Docker licensing/availability, and model-license constraints. | Not a hosted remote provider. | `local`. | 2026-06-01 | [SA1], [DMR1]. Treat model download/setup time like other local smoke setup. |
| `groq` | Reuse `spring-ai-starter-model-openai` for Groq's OpenAI-compatible endpoint. | Create GroqCloud account, generate API key, select supported model, inspect account limits and billing plan in the console. | `GROQ_API_KEY`. | `unknown` from public docs checked here: docs expose rate limits and model pricing, but account billing plan details require console/settings confirmation. | Only after the Groq console confirms a no-cost plan or credits for the selected model. | `blocked` for remote calls; `config-only` before console confirmation. | 2026-06-01 | [SA1], [GR1], [GR2], [GR3]. Agent fetch could not read Groq billing settings because robots.txt blocks `/settings/`. |
| `nvidia` | Reuse `spring-ai-starter-model-openai` for NVIDIA NIM-hosted OpenAI-compatible LLM API. | Create/use NVIDIA developer/API access, create API key for hosted NIM endpoint or set up self-hosted NIM with entitlement, and confirm any free/prototype credits. | `NVIDIA_API_KEY` for hosted endpoint; entitlement/config for self-hosted NIM. | `unknown`; public docs checked expose hosted prototype endpoints but no durable free API tier in the fetched pages. | Only with explicit account/credit or self-hosted no-cost confirmation. | `blocked` for hosted remote calls; local/self-hosted NIM should get its own task. | 2026-06-01 | [SA1], [NV1], [NV2]. Self-hosted NIM has separate entitlement/runtime concerns. |
| `perplexity` | Reuse `spring-ai-starter-model-openai` for Perplexity/Sonar OpenAI-compatible calls. | Create Perplexity API access, generate auth token/API key, select Sonar/Search/Agent API route, and confirm billing. | Perplexity API token/key. | `paid-only`; official pricing lists token/request/tool prices and no durable free API tier was confirmed. | No by default. | `blocked` for remote calls; `config-only` before billing confirmation. | 2026-06-01 | [SA1], [P1], [P2], [P3]. The consumer Perplexity product is not API free-tier permission. |
| `moonshot` | `not-core-spring-ai` in pinned `2.0.0-M6`; future/community or OpenAI-compatible task only. | Create Kimi/Moonshot platform organization, generate API key, check balance/vouchers, and confirm model pricing if a future task adopts it. | `MOONSHOT_API_KEY` or future chosen env var. | `paid-only` for chat completion inference in checked pricing; file extraction/storage may be temporarily free but chat inference is billed. | No for first-wave Codegeist because it is not core-shipped in pinned Spring AI. | `out-of-scope`. | 2026-06-01 | [SA1], [K1], [K2], [K3]. Do not add until a focused upgrade/community-provider task exists. |
| `qianfan` | `not-core-spring-ai` in pinned `2.0.0-M6`; future/community task only. | Create Baidu Cloud/QianFan account, complete real-name verification when required, receive any voucher, configure model service, and confirm billing. | Baidu/QianFan API key/secret or future chosen env vars. | `trial-credit-only`; official docs mention a new-user voucher after registration and real-name verification, not a durable free API tier. | No for first-wave Codegeist because it is not core-shipped in pinned Spring AI. | `out-of-scope`. | 2026-06-01 | [SA1], [Q1], [Q2]. Keep China-region cloud/account requirements explicit if adopted later. |

## Official Source References

| ID | URL |
| --- | --- |
| [SA1] | Spring AI `2.0.0-M6` provider matrix evidence in `T006_02_create-spring-ai-provider-matrix.md`, especially the Spring AI starter/source citations. |
| [A1] | `https://docs.anthropic.com/en/api/getting-started` |
| [A2] | `https://www.anthropic.com/pricing#api` |
| [B1] | `https://docs.aws.amazon.com/bedrock/latest/userguide/getting-started.html` |
| [B2] | `https://aws.amazon.com/bedrock/pricing/` |
| [D1] | `https://api-docs.deepseek.com/` |
| [D2] | `https://api-docs.deepseek.com/quick_start/pricing` |
| [G1] | `https://ai.google.dev/gemini-api/docs/api-key` |
| [G2] | `https://ai.google.dev/gemini-api/docs/pricing` |
| [G3] | `https://ai.google.dev/gemini-api/docs/rate-limits` |
| [MM1] | `https://platform.minimaxi.com/docs/llms.txt` |
| [MM2] | `https://platform.minimaxi.com/document/Price` |
| [MI1] | `https://docs.mistral.ai/getting-started/quickstart/` |
| [MI2] | `https://mistral.ai/products/la-plateforme#pricing` |
| [O1] | `https://ollama.com/download` |
| [OA1] | `https://platform.openai.com/docs/quickstart` |
| [OA2] | `https://platform.openai.com/docs/pricing` |
| [AZ1] | `https://learn.microsoft.com/en-us/azure/ai-foundry/openai/overview` |
| [AZ2] | `https://azure.microsoft.com/en-us/pricing/details/cognitive-services/openai-service/` |
| [DMR1] | `https://docs.docker.com/desktop/features/model-runner/` |
| [GR1] | `https://console.groq.com/docs/quickstart` |
| [GR2] | `https://console.groq.com/docs/rate-limits` |
| [GR3] | `https://console.groq.com/docs/models` |
| [NV1] | `https://docs.api.nvidia.com/quick-start/` |
| [NV2] | `https://docs.api.nvidia.com/nim/reference/llm-apis` |
| [P1] | `https://docs.perplexity.ai/guides/getting-started` |
| [P2] | `https://docs.perplexity.ai/guides/pricing` |
| [P3] | `https://docs.perplexity.ai/llms.txt` |
| [K1] | `https://platform.kimi.ai/docs/llms.txt` |
| [K2] | `https://platform.moonshot.ai/docs/pricing/chat` |
| [K3] | `https://platform.moonshot.cn/docs/pricing/chat` |
| [Q1] | `https://cloud.baidu.com/doc/WENXINWORKSHOP/s/hlrk4akp7` |
| [Q2] | `https://cloud.baidu.com/doc/qianfan/s/wmh4sv6ya` |
| [OC1] | `docs/third-party/opencode/source/packages/web/src/content/docs/providers.mdx` |
| [OC2] | `docs/third-party/opencode/source/packages/web/src/content/docs/models.mdx` |
| [OC3] | `docs/third-party/opencode/source/packages/opencode/src/provider/provider.ts` |
| [OR1] | `https://openrouter.ai/docs/quickstart.md` |
| [OR2] | `https://openrouter.ai/docs/guides/routing/model-variants/free.md` |

## Provider Availability And On-Demand Use Analysis

Update from `T006_06` and the follow-up options removal: `ProviderConfig` no
longer owns model selection or generic provider options. Models and generation
options vary by coding agent, session, command, request, or provider feature test
method, so provider config only stores access, endpoint, enablement, and
credential data. Rows below that mention `model`, `enabled`, `completions-path`,
or `options.*` now describe runtime/provider-feature requirements rather than
fields that belong in `ProviderConfig`.

Making a provider "available" in Codegeist is more than adding a provider id to
`codegeist.yml`. A provider becomes usable only when these layers are satisfied:

| Layer | Required work | Why it matters |
| --- | --- | --- |
| Build availability | The needed Spring AI starter or compatible integration is on the application classpath and covered by native-image posture when native builds are in scope. | Spring Boot cannot create provider clients from config if the provider classes are not packaged. |
| Config shape | `ProviderConfig` can bind access fields needed by that provider, such as endpoint fields and credential values or references. YAML `type` is a dispatch-only discriminator, provider YAML keys are parsed and validated, and the current provider map view is keyed by provider type. Runtime enablement, model, deployment, path, and generation-option selectors belong to the calling agent, command, request, or provider feature test method. | The current config-only implementation binds `ollama` and `openai`; later provider-use tasks must add provider-specific runtime requirements only when a call path needs them. |
| Source loading | `codegeist.yml` sources are discovered, SpEL-evaluated, mapped, and validated before diagnostics. | Provider use must consume normalized config, not raw YAML with unevaluated expressions. |
| Safety gate | Local, config-only, and hosted `remote_free` modes are decided before a provider call. API-key presence is never permission to call a hosted provider. | Prevents accidental paid remote calls and keeps provider smokes repeatable. |
| Runtime selection | A command, session, smoke allowlist, or later model-selection policy chooses one provider id and model/deployment for the current request. | On-demand use means Codegeist should not initialize or call every configured provider at startup. |
| Client creation | Codegeist maps the selected provider config to the matching Spring AI `ChatModel` or `ChatClient` path and creates it only when selected. | Keeps provider work lazy, testable, and isolated to the requested provider. |
| Result handling | Calls return an observable response, status, duration, and diagnostics. Failures identify missing config, missing dependency, blocked cost posture, provider setup gaps, or provider API failure. | Users need actionable feedback and must treat any config-bearing output as sensitive. |

The first implementation should stay lazy. Loading config, running `--show-config`,
or starting the CLI must not instantiate all provider clients, pull local models,
or call remote APIs. Provider client creation should happen inside the command,
workflow, or smoke path that explicitly selects one provider.

## Minimal Runtime Contract For One Provider

Before Codegeist can use any provider on demand, a focused implementation task must
define and test this smallest contract:

1. A configured provider id exists under `provider.<provider-id>`.
2. The provider entry has a supported `type` from the T006 matrix.
3. The provider type is build-available through either a dedicated Spring AI
   starter or the OpenAI-compatible starter route.
4. The provider entry contains the minimum provider-specific access fields for a
   call. Runtime model, deployment, region, token cap, and other generation options
   must come from the caller, command, request, or provider feature test method.
5. Config loading has evaluated SpEL values and run Bean Validation. If future
   work adds multi-source combination, it must define that policy explicitly.
6. The selected execution mode permits the call: `local` for local providers,
   `remote_free` only with explicit no-cost confirmation for eligible hosted
   providers, and `config` for no-call configuration checks.
7. Codegeist can map the normalized provider config into the matching Spring AI
   properties/options or builder objects without mutating global Spring properties.
8. Codegeist creates the selected `ChatModel` or `ChatClient` lazily for the current
   request or smoke, then reports `passed`, `skipped`, `blocked`, or `failed` with
   a reason and duration.

This contract is intentionally per-provider. Do not introduce a broad provider
registry, adapter hierarchy, dynamic plugin layer, factory layer, or
model-selection policy before a focused provider task needs it. The first provider
task should map the selected provider config to its concrete chat model in the chat
service from access config only, then pass the runtime model at prompt-call time.

## Provider Access Fields Needed For On-Demand Use

The current Java implementation supports config-only `ollama` and `openai`
provider entries with typed fields. Provider use still requires later tasks to
connect one selected provider config to Spring AI in small tested steps.

Minimum access fields for on-demand provider use:

| Field | Applies to | Purpose |
| --- | --- | --- |
| `provider.<id>.type` | all real providers | Dispatch-only discriminator that selects the provider integration, for example `ollama`, `anthropic`, or `openai`; it is not stored as mutable `ProviderConfig` state. |
| `provider.<id>.base-url` | local and OpenAI-compatible providers, optional for many dedicated providers | Overrides local daemons, proxies, hosted endpoints, and compatibility APIs. |
| `provider.<id>.credentials.*` | hosted providers and cloud providers | References env vars, profiles, or files without storing raw secret values in YAML. T006_03 still rejects a separate credential-reference language for the first SpEL slice. |

Keep the first model deliberately minimal. Add provider config fields only when a
focused test needs persistent access data. Model names and generation options stay
outside `ProviderConfig`.

## Provider Availability Matrix

This matrix analyzes what each provider still needs before it can be configured
and used on demand in Codegeist. It is not an implementation checklist for one
single task; provider-specific tasks should pick one row and prove the smallest
usable path.

| codegeist-type | Build availability | Access config and runtime inputs for on-demand use | Safety gate | Main blockers before use |
| --- | --- | --- | --- | --- |
| `ollama` | Add `spring-ai-starter-model-ollama`. | Discriminator: `type`; access config: `base-url`; runtime input: selected model and deterministic options as needed. | `local`; no hosted account. | Ready local daemon, already downloaded model, readiness timing, stable assertion, and first private client-creation path. |
| `docker-model-runner` | Add/reuse `spring-ai-starter-model-openai`. | Discriminator: `type`; access config: `base-url`, optional dummy key; runtime input: selected model and OpenAI-compatible options. | `local`; no hosted account. | Docker Model Runner availability, model pull/cache lifecycle, and OpenAI-compatible base URL behavior. |
| `openai` | Add `spring-ai-starter-model-openai`. | Discriminator: `type`; access config: API key, optional `base-url`, org/project; runtime input: selected model and token caps. | `blocked` unless explicit credits/no-cost confirmation exists. | Billing/credits, sensitive config output, and no default remote call from API-key presence. |
| `anthropic` | Add `spring-ai-starter-model-anthropic`. | Discriminator: `type`; access config: API key; runtime input: selected model and provider options. | `blocked` unless explicit no-cost account state exists. | API billing confirmation and provider-specific options such as max tokens, service tier, and geo. |
| `bedrock-converse` | Add `spring-ai-starter-model-bedrock-converse`. | Discriminator: `type`; access config: AWS credential source; runtime input: model id, region, timeout, and token options. | `blocked` unless explicit AWS credit/sandbox decision exists. | AWS account, region, Bedrock model access, profile/credentials chain, and deployment permissions. |
| `google-genai` | Add `spring-ai-starter-model-google-genai`. | Discriminator: `type`; access config: API-key or Vertex access fields; runtime input: selected model and provider options. | `remote_free` only for confirmed Gemini Developer API free-tier route; Vertex/cloud mode is blocked until confirmed. | Keeping Developer API and Vertex-style billing paths separate. |
| `deepseek` | Add `spring-ai-starter-model-deepseek`. | Discriminator: `type`; access config: API key and endpoint fields; runtime input: selected model and request options. | `blocked` unless granted balance/no-cost confirmation exists. | Balance state, model deprecations, and reasoning-mode differences. |
| `minimax` | Add `spring-ai-starter-model-minimax`. | Discriminator: `type`; access config: API key; runtime input: selected model, response format, and seed options. | `blocked` by default. | Paid-package/account-balance posture and docs/source default-model mismatch. |
| `mistral-ai` | Add `spring-ai-starter-model-mistral-ai`. | Discriminator: `type`; access config: API key; runtime input: selected model, response format, and random seed. | `remote_free` only after Studio Free-mode confirmation. | Current console mode, model access, and provider-specific safe-prompt/random-seed options. |
| `azure-openai` | Reuse `spring-ai-starter-model-openai`. | Discriminator: `type`; access config: `base-url`, credentials, API version/path as needed; runtime input: deployment/model selector. | `blocked` unless subscription credits/sandbox are confirmed. | Azure subscription, resource deployment, endpoint/API-version shape, region, and deployment name mapping. |
| `groq` | Reuse `spring-ai-starter-model-openai`. | Discriminator: `type`; access config: `base-url`, API key; runtime input: selected model and conservative supported options. | `blocked` until console confirms no-cost plan/credits. | Account billing plan is not public through fetched docs; unsupported OpenAI request fields must be gated. |
| `nvidia` | Reuse `spring-ai-starter-model-openai`. | Discriminator: `type`; access config: `base-url`, API key; runtime input: selected model and required max-token option. | `blocked` unless hosted credits or self-hosted no-cost posture is confirmed. | Hosted NIM account state, self-hosted entitlement/runtime, and provider-required token cap. |
| `perplexity` | Reuse `spring-ai-starter-model-openai`. | Discriminator: `type`; access config: `base-url`, API key, completions path when required; runtime input: selected model and token/search options. | `blocked` by default. | Paid token/request/tool pricing and limited OpenAI compatibility for tools/multimodal messages. |
| `moonshot` | Not core-shipped in Spring AI `2.0.0-M6`. | None for first-wave Codegeist; future task may choose community or OpenAI-compatible path. | `out-of-scope`. | Requires Spring AI upgrade/community adoption and pricing/account review before use. |
| `qianfan` | Not core-shipped in Spring AI `2.0.0-M6`. | None for first-wave Codegeist; future task may choose community starter. | `out-of-scope`. | Requires community/provider adoption, China-region account setup, real-name/billing posture, and credential shape. |

## On-Demand Provider Flow

The first on-demand flow should be observable and narrow:

```text
command or smoke selects provider id
-> load/evaluate/merge CodegeistConfig
-> resolve provider entry and type
-> check build availability for that type
-> validate provider-specific required fields
-> apply safety gate and runtime provider selection
-> map Codegeist config to provider-specific Spring AI options
-> create ChatModel or ChatClient for the selected provider only
-> execute one local or explicitly allowed remote request
-> report status, duration, and blocker/failure reason
```

Do not bind all provider config into Spring Boot's global `spring.ai.*` properties
as the only runtime mechanism. Global properties are useful for simple Boot
autoconfiguration, but Codegeist needs on-demand provider selection by provider id.
Provider-specific tasks should prefer an explicit mapping from Codegeist's merged
provider entry into the selected Spring AI builder/options path. If a provider's
Spring AI integration only works cleanly through Boot autoconfiguration, document
that constraint in the provider-specific task before adding the starter.

## Task Split For Implementation

Use this order so provider availability grows without a broad placeholder layer:

1. `T006_04`: finish config source loading, SpEL evaluation, direct rendering, and
   config behavior. No provider client creation.
2. `T006_05`: add the Ollama starter and one local on-demand provider call through
   a focused test. The chat service maps the selected provider config to the
   concrete chat model from access config only; the runtime model is passed at
   prompt-call time.
3. `T006_06`: add the generic smoke harness and status vocabulary around the first
   local provider path.
4. Later provider-specific tasks: add one hosted or OpenAI-compatible provider at a
   time, using this task's account/free-tier catalog and availability matrix before
   allowing `remote_free`.

## Non-Goals

- Do not define credential reference forms such as `api-key-env`, `profile-env`,
  `credentials-path`, or `auth-store-ref`.
- Do not implement provider account setup flows or runtime account records.
- Do not design OAuth authorization, refresh, logout, or token storage.
- Do not design a Codegeist credential vault or auth store.
- Do not define model selection fields or model fallback behavior.
- Do not create provider accounts or use remote provider resources.
- Do not add Spring AI provider starters or call provider APIs.
- Do not implement the SpEL parser in this documentation-only child task.
- Do not implement provider registries, factories, adapters, `ChatClient` creation,
  or provider smoke commands in this documentation-only child task.
- Do not treat consumer web-chat free plans, starter credits, vouchers, or trial
  balances as permission for default remote smokes.

## Acceptance Criteria

- The task defines Spring SpEL as the only expression mechanism for the current
  provider-config evaluation slice.
- The task explicitly rejects a separate Codegeist credential-reference schema for
  this slice.
- The task states that the first SpEL implementation should not add a
  Codegeist-specific sandbox, whitelist, or expression restriction.
- The task defines value-only evaluation: string scalar values may contain SpEL,
  while YAML keys and non-string scalar values stay literal.
- The task defines the first evaluation context as plain Spring
  `StandardEvaluationContext` without Codegeist helper variables, functions, or a
  Spring bean resolver.
- The task defines missing environment variables as ordinary SpEL results, with
  required-field checks left to Bean Validation.
- The task defines source-aware parse and evaluation failures without printing
  evaluated secret values.
- The task defines a minimal read/evaluate/map/validate/merge parser direction.
- The task examples avoid provider model selection.
- The task keeps provider calls, remote smokes, runtime account setup flows, OAuth,
  and auth-store design out of scope.
- The task records the sensitive-output risk caused by materialized SpEL values.
- Every provider from `T006_02` has an account/free-tier row with official source
  references and a `last-checked` date.
- Every provider row states its Spring AI route for the pinned `2.0.0-M6` baseline.
- The catalog distinguishes dedicated Spring AI starters from OpenAI-compatible
  providers that should reuse `spring-ai-starter-model-openai`.
- Every hosted provider row states account creation requirements, billing or credit
  requirements, credential source, and model/region/deployment prerequisites when
  applicable.
- Every row has a conservative `safe-default-smoke` value that prevents accidental
  paid remote calls.
- The catalog distinguishes official API free tiers from trial credits, consumer
  chat-product free plans, local-only runtimes, and unknown pricing evidence.
- The task compares OpenCode and Codegeist provider support, including OpenRouter
  as an OpenAI-compatible provider that does not need a dedicated Spring AI starter.
- The task defines the layered requirements for making a provider available,
  configurable, and usable on demand in Codegeist.
- The task defines the minimal runtime contract for selecting one configured
  provider and creating only that provider's Spring AI client lazily.
- The task identifies provider-specific blockers before on-demand use.
- The task keeps provider registry, client-creation abstraction, starter additions, provider
  calls, and smoke command implementation out of this documentation-only child
  task.

## Solve Result

- The provider config evaluation strategy is complete for the current slice.
- `codegeist.yml` is trusted local input, not a remote or untrusted config source.
- Spring SpEL template expressions are the only expression mechanism for this
  slice.
- The first parser should evaluate only YAML string scalar values, not map keys or
  non-string scalars, before mapping to `CodegeistConfig`.
- The first evaluation context is a plain Spring `StandardEvaluationContext`
  without Codegeist-specific sandboxing, helper functions, variables, or bean
  resolution.
- Missing environment variables are ordinary SpEL results; Bean Validation owns
  required-field failures after mapping.
- Parse and evaluation failures should include source/path context without
  printing evaluated values.
- This slice intentionally excludes credential-reference schemas, runtime account
  setup flows, OAuth, auth-store behavior, model selection, provider calls, and
  remote smokes.
- Evaluated config values are sensitive because SpEL can materialize secrets from
  environment variables or other local sources.
- The provider account/free-tier catalog is complete for every provider from
  `T006_02` and uses official source references with a `last-checked` date.
- The catalog distinguishes dedicated Spring AI starters from OpenAI-compatible
  providers that should reuse `spring-ai-starter-model-openai` in the pinned
  baseline.
- Hosted provider calls remain blocked unless the row explicitly allows
  `remote_free` and the developer confirms the selected account/model/route is
  no-cost at execution time.
- The OpenCode/Codegeist comparison is documented: OpenCode already has broad
  AI-SDK/Models.dev/provider-directory support, while Codegeist's first wave is
  constrained to the pinned Spring AI routes and one provider at a time.
- OpenRouter is classified as technically supportable through the OpenAI-compatible
  Spring AI route; a dedicated Codegeist `openrouter` type would be a thin profile
  for headers, routing options, and `:free` gating rather than a separate client.
- Provider availability is defined as layered readiness: build dependency,
  config shape, evaluated/validated config, safety gate, runtime selection,
  lazy client creation, and result handling.
- On-demand use should select one provider id, create only that provider's Spring
  AI client for the current request or smoke, and never instantiate or call every
  configured provider at startup.
- The availability matrix identifies the minimum config, Spring AI route, safety
  gate, and blockers for each provider before provider-specific implementation
  tasks begin.

## Verification

Documentation-only verification:

```bash
git --no-pager diff --check
```

## Verification Result

- `git --no-pager diff --check` passed after the concrete SpEL,
  account/free-tier, and provider-availability strategy was defined.

## Planning Notes

- `T006_04` should implement the minimal SpEL evaluation pipeline before broader
  source-combination features are added.
- `T006_05` should still keep the first real provider call local through Ollama.
- `T006_06` owns provider smoke status, local/remote gating, and remote-call safety.
- `T006_06` hosted-provider smoke rows must consume this account/free-tier catalog
  before deciding whether a provider is eligible for `remote_free`, `blocked`, or
  config-only behavior.
- Provider-specific implementation tasks should consume the availability matrix in
  this task and add one provider at a time instead of adding broad placeholder
  registries or all starters at once.
- Use `../hints/source-evidence-question-catalog.md` when an implementation task
  needs source-backed OpenCode or Spring AI Agent Utils evidence.
- If future work needs runtime account setup, OAuth, or auth-store behavior, create
  a focused child task instead of expanding this strategy retroactively.
