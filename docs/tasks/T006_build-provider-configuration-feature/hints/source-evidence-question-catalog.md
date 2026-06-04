# T006 Source Evidence Question Catalog

Use this catalog with `.oc_local/commands/ask-project.md` before implementing
T006 provider config parsing, Spring SpEL evaluation, provider client wiring,
local Ollama checks, remote smoke gates, or later provider/account follow-up
tasks.

The catalog is intentionally broader than the current `T006_03` SpEL-only
strategy. The current parser slice should stay simple, but later T006 work may
still need OpenCode evidence for provider config behavior, auth separation,
runtime provider selection, sensitive-output handling, and remote-call safety. It may also need
Spring AI Agent Utils evidence for Java/Spring `ChatClient` wiring, structural
tests, tool/event patterns, and future agent workflow boundaries.

Evidence status: these answers are based on static source review of the local
third-party workspaces. OpenCode was reviewed from
`docs/third-party/opencode/source`, and Spring AI Agent Utils was reviewed from
`docs/third-party/spring-ai-agent-utils/source` plus the existing Repomix output.
No third-party runtime tests were executed for this catalog.

Expected command shape:

```text
/ask-project <project-name> "<question>"
```

## OpenCode Config Structure And Merge Behavior

```text
/ask-project opencode "How does OpenCode load, merge, and normalize configuration from global, project, custom, managed, and inline config sources? Include source paths and implementation examples."
```

**Answer:** OpenCode loads multiple JSON or JSONC config sources into one merged
runtime config. The documented order is remote `.well-known/opencode`, global
`~/.config/opencode/opencode.json`, `OPENCODE_CONFIG`, project `opencode.json`,
`.opencode` directories, `OPENCODE_CONFIG_CONTENT`, managed files, and macOS MDM
preferences. The implementation follows that broad shape in `Config.load`: it
fetches remote config, merges global config, custom config, project files,
`.opencode` directory config, inline content, active account config, managed
files, and managed preferences. It normalizes deprecated TUI keys out of loaded
config before merge.

**Evidence:** `packages/web/src/content/docs/config.mdx`,
`packages/opencode/src/config/config.ts`, `packages/opencode/src/config/paths.ts`,
`packages/opencode/src/config/parse.ts`.

**Codegeist implication:** Keep Codegeist's first parser smaller than OpenCode's
full source stack. For T006, the useful lesson is the staged pipeline: discover
sources, parse raw text, evaluate substitutions, validate schema, then merge in a
documented precedence order. Do not copy remote, managed, `.opencode`, or account
config sources into the first `codegeist.yml` SpEL slice.

```text
/ask-project opencode "Which source files implement OpenCode config precedence, and how are conflicting provider fields merged? Summarize behavior relevant to Codegeist codegeist.yml source merging."
```

**Answer:** Config precedence is split between the docs and `config.ts` load
sequence. Generic config uses `mergeDeep`, with a special `instructions` merge
that concatenates and de-duplicates arrays. Provider fields are merged again in
`provider.ts`: configured provider entries extend the Models.dev database,
provider `options` merge recursively with database options, model overrides merge
recursively with existing model options, headers merge recursively, and variants
merge recursively while dropping variants marked `disabled`.

**Evidence:** `packages/opencode/src/config/config.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/web/src/content/docs/config.mdx`.

**Codegeist implication:** Define provider combination behavior explicitly before
there are multiple Codegeist config sources. A simple rule such as later source
wins for scalars and map keys, with provider `options` combined as maps, is easier
to test than inheriting OpenCode's model/database/provider/variant overlay
behavior.

```text
/ask-project opencode "How does OpenCode represent provider configuration in its schema, especially provider id, model id, options, baseURL, models, enabled providers, and disabled providers?"
```

**Answer:** Top-level config has `provider`, `enabled_providers`,
`disabled_providers`, `model`, and `small_model`. `provider` is a map from
provider id to `ConfigProvider.Info`. Provider config supports `api`, `name`,
`env`, `id`, `npm`, `whitelist`, `blacklist`, `options`, and `models`.
`options` has known fields such as `apiKey`, `baseURL`, `enterpriseUrl`,
`timeout`, and `chunkTimeout`, plus arbitrary rest fields. `models` is a map from
model id to model metadata and provider-specific `options`, `headers`, and
variants.

**Evidence:** `packages/opencode/src/config/config.ts`,
`packages/opencode/src/config/provider.ts`,
`packages/web/src/content/docs/models.mdx`,
`packages/web/src/content/docs/providers.mdx`.

**Codegeist implication:** The OpenCode shape supports the chosen Codegeist
direction where providers live under a provider map and provider-specific options
stay nested. T006_03 should still avoid model selection fields. Model ids,
variants, model options, and `small_model` are later provider/model selection
work, not SpEL config evaluation.

```text
/ask-project opencode "Show implementation examples for OpenCode provider config parsing and validation flow. Which parts should Codegeist translate conceptually and which parts should it avoid copying?"
```

**Answer:** OpenCode parses JSONC, substitutes config variables, validates with
Effect Schema/Zod compatibility, rejects unknown top-level keys, and later builds
runtime providers from the validated config plus Models.dev metadata, env, auth,
plugins, and custom loaders. Useful concepts are source-aware parsing,
schema-backed validation, separation of raw config from runtime provider objects,
and provider-specific validation at creation time. OpenCode-specific pieces to
avoid are Effect services, AI SDK provider packages, Models.dev transformation,
plugin hooks, and web/server route shapes.

**Evidence:** `packages/opencode/src/config/parse.ts`,
`packages/opencode/src/config/variable.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/session/llm.ts`.

**Codegeist implication:** Translate the boundary, not the implementation. The
Java path should be raw YAML source -> SpEL-evaluated scalar tree -> Jackson or
Spring mapping -> Bean Validation -> merged `CodegeistConfig` -> Spring AI client
creation only in later tasks.

```text
/ask-project opencode "How does OpenCode handle unknown, optional, or provider-specific config fields? Explain the schema behavior and implementation tradeoffs."
```

**Answer:** OpenCode rejects unknown top-level keys in `ConfigParse.effectSchema`
when the top-level schema has no index signature. At the provider level, however,
`ConfigProvider.Info.options` uses a rest schema for arbitrary provider-specific
fields, and model variants also accept rest fields. This gives strictness for the
main config contract and flexibility at provider-specific extension points.

**Evidence:** `packages/opencode/src/config/parse.ts`,
`packages/opencode/src/config/provider.ts`.

**Codegeist implication:** Use Bean Validation for known Codegeist fields and keep
provider `options` as an open map until a focused provider task needs stronger
typed properties. Do not make arbitrary top-level YAML keys silently pass.

```text
/ask-project opencode "How does OpenCode expose resolved config for debugging or UI usage, and how does it avoid leaking credential values?"
```

**Answer:** OpenCode exposes resolved instance config through `ConfigRoutes.get`
and exposes configured providers/default models through `ConfigRoutes.providers`.
Provider runtime info includes an optional `key` field, and provider config
options may include materialized `apiKey` values. The reviewed source does not
show a universal redaction layer on these resolved config responses. It relies on
local instance trust boundaries more than on systematic redaction.

**Evidence:** `packages/opencode/src/server/routes/instance/config.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/config/provider.ts`.

**Codegeist implication:** Codegeist follows the trusted-local-output boundary for
the current config slice. `--show-config` prints configured values unchanged, so
that output must be treated as sensitive when config contains credentials.

## OpenCode Credentials, Auth, And Provider Accounts

```text
/ask-project opencode "How does OpenCode separate provider config from provider credentials? Include the auth storage files, auth data model, and provider lookup flow."
```

**Answer:** OpenCode separates provider config from stored credentials. Config
lives in config sources under the `provider` map. Auth v1 stores per-provider
credentials in `auth.json` under the global data path. Auth v2 stores accounts in
`auth-v2.json` with `accounts` and active account ids by service. Provider
runtime loading reads config, env, and auth stores, then merges credential-derived
keys into runtime provider info.

**Evidence:** `packages/opencode/src/auth/index.ts`,
`packages/opencode/src/v2/auth.ts`, `packages/opencode/src/provider/provider.ts`,
`packages/web/src/content/docs/providers.mdx`.

**Codegeist implication:** T006_03 intentionally does not implement this split.
The only current mechanism is SpEL materializing config values. If Codegeist adds
an auth store later, it should be a new task with explicit storage, active account
selection, and sensitive-output behavior.

```text
/ask-project opencode "How does OpenCode's /connect flow store API-key credentials and OAuth credentials? Include source paths and sequence of calls."
```

**Answer:** The UI `/connect` flow selects provider auth methods, starts OAuth
authorization when needed, handles callback completion, or accepts API-key input.
The server side exposes provider auth methods plus OAuth authorize and callback
routes. Control handlers can set or remove auth records. `ProviderAuth.callback`
stores callback results as either `api` or `oauth` records through `Auth.set`,
which writes `auth.json` with mode `0600`.

**Evidence:** `packages/opencode/src/provider/auth.ts`,
`packages/opencode/src/server/routes/instance/provider.ts`,
`packages/opencode/src/server/routes/instance/httpapi/handlers/control.ts`,
`packages/opencode/src/auth/index.ts`,
`packages/app/src/components/dialog-connect-provider.tsx`.

**Codegeist implication:** Do not fold `/connect`, OAuth, or credential storage
into the SpEL parser. A future Codegeist connect flow should be separate from
config parsing and should write a protected auth store rather than rewriting
`codegeist.yml` with secrets.

```text
/ask-project opencode "What auth types does OpenCode support for providers, and how are api, oauth, and wellknown auth records represented in source?"
```

**Answer:** Auth v1 supports three record types. `Api` stores `type: api`, `key`,
and optional string metadata. `Oauth` stores `type: oauth`, refresh token, access
token, expiry, and optional account or enterprise fields. `WellKnown` stores
`type: wellknown`, `key`, and `token` for remote config discovery. Auth v2 narrows
credentials to `ApiKeyCredential` and `OAuthCredential`, then wraps them in
account records with service ids and active account selection.

**Evidence:** `packages/opencode/src/auth/index.ts`,
`packages/opencode/src/v2/auth.ts`.

**Codegeist implication:** These are useful future reference points, but not a
current T006_03 schema. Avoid adding `auth-store-ref`, account ids, OAuth token
records, or well-known credential records until a focused auth-store task exists.

```text
/ask-project opencode "How does OpenCode resolve provider credentials at runtime when starting a model request? Include provider id, model id, config, auth store, and environment behavior."
```

**Answer:** Provider state construction loads the Models.dev provider database,
config providers, env vars, auth records, plugin auth loaders, and custom provider
loaders. Env vars listed by a provider can create a runtime provider with
`source: env` and sometimes a `key`. API auth records create providers with
`source: api` and `key`. Later, `resolveSDK` copies provider options, resolves
`baseURL`, injects `apiKey` from provider `key` if needed, loads the provider SDK,
and caches the SDK by provider package plus options.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/session/llm.ts`.

**Codegeist implication:** Codegeist should separate evaluated config from runtime
client creation. SpEL may produce `api-key` and `base-url`, but no provider call
should occur during parse or validation. Runtime client construction belongs in a
later provider client task.

```text
/ask-project opencode "How does OpenCode handle providers that can authenticate through environment variables instead of stored auth? Give examples from source or docs."
```

**Answer:** OpenCode provider metadata includes an `env` array. During provider
state construction, it checks each provider's env names and creates runtime
provider entries when env values exist. Provider-specific loaders also read env
for fields such as AWS region/profile, Cloudflare account ids, gateway ids, and
tokens. The custom-provider form accepts an API key written as `{env:VAR}` and
turns it into a provider `env` entry instead of a literal `apiKey`.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/app/src/components/dialog-custom-provider-form.ts`,
`packages/web/src/content/docs/providers.mdx`.

**Codegeist implication:** Codegeist's SpEL can cover env lookup directly, but the
result becomes a materialized value. If later work wants OpenCode-like late env
resolution, it should introduce a separate runtime credential source concept
rather than expanding this SpEL-only task.

```text
/ask-project opencode "How does OpenCode model OAuth provider authorization, callback handling, pending auth state, and storing refresh/access tokens?"
```

**Answer:** Provider auth plugins expose auth methods with `oauth` or `api` type.
`ProviderAuth.authorize` validates prompt inputs, invokes the plugin's OAuth
authorize function, stores the pending result in an in-memory map keyed by
provider id, and returns URL/method/instructions. `ProviderAuth.callback` looks up
the pending result, requires a code for `code`-mode callbacks, invokes the
callback, and persists either an API key or OAuth refresh/access tokens.

**Evidence:** `packages/opencode/src/provider/auth.ts`,
`packages/opencode/src/server/routes/instance/provider.ts`.

**Codegeist implication:** OAuth is a separate workflow with pending state,
callbacks, error handling, and protected persistence. It should not be modeled as
extra YAML fields in the first provider config parser.

```text
/ask-project opencode "What output redaction or logging patterns does OpenCode use around auth, provider config, and session requests? Identify implementation examples and gaps."
```

**Answer:** OpenCode often logs identifiers rather than secret values, such as
provider id, model id, session id, and package name. The provider SDK cache hashes
options but does not log the serialized options. However, the reviewed source did
not reveal a universal secret redaction utility for resolved config, and config
routes can return resolved config/provider objects. The gap matters when config
or provider runtime objects include materialized `apiKey` or `key` fields.

**Evidence:** `packages/opencode/src/session/llm.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/server/routes/instance/config.ts`,
`packages/opencode/src/config/provider.ts`.

**Codegeist implication:** Do not add automatic masking as an implicit property of
provider config. T006_03 should document the sensitive-output risk, and config
commands that intentionally print resolved config should be tested as direct output.

## OpenCode Provider Runtime And Model Selection

```text
/ask-project opencode "How does OpenCode turn a provider/model selection like provider-id/model-id into a runtime model object used for chat requests?"
```

**Answer:** OpenCode parses model strings with `parseModel`, splitting on `/` into
provider id and model id. `Provider.defaultModel` prefers explicit config,
recent selections, then sorted available providers and models. `Provider.getModel`
validates the selected provider and model and returns a runtime `Model` object.
`Provider.getLanguage` resolves the underlying AI SDK language model, using
custom model loaders when registered.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/web/src/content/docs/models.mdx`.

**Codegeist implication:** This is later provider/model selection work. The T006_03
SpEL parser should not add `model` fields or default model rules.

```text
/ask-project opencode "How does OpenCode distinguish provider metadata from user provider config and auth data? Include source paths for Models.dev/provider database integration if present."
```

**Answer:** OpenCode starts with Models.dev metadata converted by
`fromModelsDevProvider` and `fromModelsDevModel`. User config then extends or
overrides provider and model metadata. Auth and env data are loaded after config
and attached to runtime provider state as sources and keys. The final runtime
provider object therefore blends metadata, user config, env, and auth, but the
source files keep those concerns in separate services.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/provider/models.ts`,
`packages/opencode/src/auth/index.ts`, `packages/opencode/src/config/config.ts`.

**Codegeist implication:** Codegeist's provider matrix should stay separate from
user `codegeist.yml`. Use config to select or parameterize providers, not as the
only source of provider capability metadata.

```text
/ask-project opencode "How does OpenCode filter providers through enabled_providers and disabled_providers? Include behavior when a provider has credentials but is disabled."
```

**Answer:** Provider state builds sets from `enabled_providers` and
`disabled_providers`. A provider is allowed only when it is included by the
enabled set, if that set exists, and not present in the disabled set. Disabled
providers are skipped during plugin loading, env loading, auth loading, custom
loader application, and final provider cleanup. The provider list route also
filters Models.dev providers before merging connected providers.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/server/routes/instance/provider.ts`.

**Codegeist implication:** Keep Codegeist provider selection simple until a
focused runtime-selection task exists. A provider blocked by a future selection
policy must not become callable just because SpEL produced a value.

```text
/ask-project opencode "How does OpenCode support custom OpenAI-compatible providers with baseURL, model maps, and provider options?"
```

**Answer:** Custom providers can be configured with an arbitrary provider id,
`npm: @ai-sdk/openai-compatible`, display name, `options.baseURL`, optional
headers, and a `models` map. The custom-provider UI validates provider id, name,
base URL, unique model ids, and headers, then emits provider config. Runtime
provider loading defaults unknown custom model packages to OpenAI-compatible when
needed.

**Evidence:** `packages/web/src/content/docs/providers.mdx`,
`packages/app/src/components/dialog-custom-provider-form.ts`,
`packages/opencode/src/config/provider.ts`,
`packages/opencode/src/provider/provider.ts`.

**Codegeist implication:** Codegeist can copy the conceptual shape for local
Ollama/OpenAI-compatible endpoints later: provider id, type or package mapping,
`base-url`, and nested options. Do not add a model map to T006_03.

```text
/ask-project opencode "How does OpenCode handle provider-specific options and model-specific options during request creation?"
```

**Answer:** Provider options are merged into runtime provider info. Model options
are attached to each runtime model. During chat, `LLM.run` builds base options
from model/provider transforms, then merges model options, agent options, and
variant options. It passes transformed provider options to AI SDK `streamText`.
Headers are assembled from OpenCode defaults, model headers, and plugin-provided
headers.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/session/llm.ts`,
`packages/opencode/src/provider/transform.ts`.

**Codegeist implication:** Provider config parsing should preserve provider
options without interpreting all of them. Request-time mapping to Spring AI
`ChatOptions` or provider-specific properties belongs to provider client wiring,
not the SpEL parser.

```text
/ask-project opencode "Which OpenCode provider runtime concepts are useful for Codegeist's Spring AI provider mapping, and which are too TypeScript/AI-SDK-specific to reuse?"
```

**Answer:** Useful concepts are provider metadata vs user config separation,
disabled-wins filtering, runtime provider objects distinct from raw config,
source-aware credential resolution, custom endpoint support, and post-request
usage accounting. TypeScript/AI-SDK-specific pieces include dynamic NPM provider
loading, `LanguageModelV3`, AI SDK `streamText`, Effect layers, Models.dev
transforms, and OpenCode plugin hooks.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/session/llm.ts`,
`packages/opencode/src/provider/transform.ts`.

**Codegeist implication:** Implement a Java/Spring boundary that maps evaluated
`CodegeistConfig` and provider matrix entries to Spring AI clients. Do not chase
OpenCode's dynamic TypeScript provider loading model.

## OpenCode Cost Safety And Remote Calls

```text
/ask-project opencode "Does OpenCode have any explicit safety gates before making remote provider calls, or does credential/model selection imply permission to call? Cite source behavior."
```

**Answer:** The reviewed request path does not show a no-cost or remote_free gate
before chat. Once a provider/model is selected and `Provider.getLanguage` can
resolve a language model, `LLM.run` calls AI SDK `streamText`. OpenCode has
provider availability logic, permission logic for tools, and some provider-specific
missing-credential checks, but no general user confirmation that a hosted provider
call may cost money.

**Evidence:** `packages/opencode/src/session/llm.ts`,
`packages/opencode/src/provider/provider.ts`.

**Codegeist implication:** Codegeist should intentionally differ for T006_06.
Remote smoke tests must require explicit remote_free selection and account/cost
confirmation; API-key presence alone is not permission to call a hosted provider.

```text
/ask-project opencode "How does OpenCode calculate, record, or expose token usage and cost metadata after a provider request?"
```

**Answer:** `Session.getUsage` normalizes AI SDK usage fields into input, output,
reasoning, cache-read, and cache-write token counts. It calculates cost from the
runtime model's per-million-token cost metadata, including separate cache and
reasoning treatment. `session/processor.ts` records usage cost and tokens on
step-finish parts, emits v2 step-ended events, and updates the assistant message.

**Evidence:** `packages/opencode/src/session/session.ts`,
`packages/opencode/src/session/processor.ts`,
`packages/opencode/src/session/message-v2.ts`.

**Codegeist implication:** Usage/cost capture is post-call telemetry. It is not a
substitute for pre-call smoke gates. Later Codegeist provider work should record
usage where Spring AI exposes it, but T006_03 should not include cost handling.

```text
/ask-project opencode "What provider errors does OpenCode surface when credentials, model access, region, or account setup are missing?"
```

**Answer:** OpenCode surfaces missing provider/model selections through
`ProviderModelNotFoundError` with suggestions. Some provider loaders return
`getModel` functions that throw targeted setup errors, such as missing
`AZURE_RESOURCE_NAME`, `CLOUDFLARE_ACCOUNT_ID`, `CLOUDFLARE_GATEWAY_ID`, or
`CLOUDFLARE_API_TOKEN`. Request-time AI SDK errors are logged by `streamText`
error handling and propagate through the session stream path.

**Evidence:** `packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/session/llm.ts`.

**Codegeist implication:** Codegeist should classify provider smoke results as
`passed`, `skipped`, `blocked`, or `failed` with a reason. Missing account setup
should usually be `skipped` or `blocked`, not an accidental remote failure.

```text
/ask-project opencode "How should Codegeist differ from OpenCode if it wants remote_free and no-cost gates before hosted provider smoke tests?"
```

**Answer:** OpenCode optimizes for interactive provider use once configured.
Codegeist should make provider smokes a separate, explicit verification mode.
Parsing config should never call a provider. Local Ollama smoke can be allowed by
local-provider selection. Hosted provider smoke should require an explicit mode,
selected provider, account readiness, and no-cost confirmation before making a
network call.

**Evidence:** `packages/opencode/src/session/llm.ts`,
`packages/opencode/src/provider/provider.ts`,
`docs/tasks/T006_build-provider-configuration-feature/tasks/T006_06_add-provider-connection-smoke-harness.md`.

**Codegeist implication:** Keep this difference in T006_06. Do not weaken it by
treating SpEL-produced secrets as automatic permission to call hosted providers.

## OpenCode Config Variables And Templating Comparison

```text
/ask-project opencode "Does OpenCode support variable substitution in config files, such as env or file references? Explain syntax, implementation, and security implications."
```

**Answer:** OpenCode supports simple textual config substitution with `{env:VAR}`
and `{file:path}`. Env variables are replaced by `process.env[varName]` or an
empty string. File references are resolved relative to the config source, support
`~/`, trim file contents, JSON-escape them into the text stream, and ignore
tokens in JSONC comment lines starting with `//`.

**Evidence:** `packages/opencode/src/config/variable.ts`,
`packages/opencode/src/config/config.ts`.

**Codegeist implication:** This is intentionally much narrower than Spring SpEL.
Codegeist can use it as a comparison point for documentation, but the current
decision is to use unrestricted SpEL for trusted local config.

```text
/ask-project opencode "How does OpenCode substitute environment variables or file contents in config, and what happens when variables are missing?"
```

**Answer:** Missing env variables become an empty string. Missing files raise an
`InvalidError` by default, with a mode for empty substitution when requested.
File content is trimmed before insertion. Because substitution happens before
schema parsing, substituted secrets become ordinary parsed config values.

**Evidence:** `packages/opencode/src/config/variable.ts`,
`packages/opencode/src/config/parse.ts`.

**Codegeist implication:** Codegeist should define missing-value behavior in
tests. For SpEL, `System.getenv('MISSING')` can return null and expressions may
use Elvis defaults. Validation should catch required values after evaluation.

```text
/ask-project opencode "Compare OpenCode's config variable substitution with the planned Codegeist parser using unrestricted Spring SpEL. What behavior should Codegeist copy, adapt, or avoid?"
```

**Answer:** OpenCode substitution is token-based, string-only, and limited to env
and file reads. SpEL is expression-based and can call types, methods, property
accessors, operators, and environment APIs depending on the evaluation context.
Codegeist should copy the phased substitution-before-validation concept, adapt the
source-aware error reporting, and avoid pretending SpEL is only a credential
reference language.

**Evidence:** `packages/opencode/src/config/variable.ts`,
`docs/tasks/T006_build-provider-configuration-feature/tasks/T006_03_define-provider-credential-and-account-strategy.md`.

**Codegeist implication:** Document the trust boundary plainly. `codegeist.yml` is
trusted local input for the first slice, and unrestricted SpEL should not be
marketed as safe for untrusted config.

```text
/ask-project opencode "Where can OpenCode resolved config output leak materialized secrets, and how should Codegeist document sensitive output after SpEL evaluation?"
```

**Answer:** Resolved OpenCode config can expose provider options, and runtime
provider routes can expose provider objects that may contain `key` or `apiKey`.
Env or auth values can therefore become visible through debug or UI routes when
they are part of resolved runtime state. The reviewed source does not show a
single redaction pass that all config outputs use.

**Evidence:** `packages/opencode/src/server/routes/instance/config.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/config/provider.ts`.

**Codegeist implication:** Treat resolved config display as sensitive output.
`--show-config` should print configured values unchanged and callers should avoid
persisting that output when it contains credentials.

## Spring AI Agent Utils Provider Integration Examples

```text
/ask-project spring-ai-agent-utils "Which classes or tests show the simplest Spring Boot integration with Spring AI chat models? Provide source paths and implementation examples relevant to Codegeist."
```

**Answer:** The simplest examples are the demo `Application.java` files. The
code-agent demo is a Spring Boot app that injects `ChatClient.Builder` into a
`CommandLineRunner`, builds a `ChatClient` with system prompt parameters,
tool callbacks, default tools, advisors, and then calls
`chatClient.prompt(...).call().content()`. This is the smallest useful Spring
Boot pattern: let Spring AI auto-configure the model and inject the builder.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
`examples/*/src/main/resources/application.properties`.

**Codegeist implication:** For Codegeist provider wiring, prefer Spring Boot and
Spring AI autoconfiguration where possible. Use `ChatClient.Builder` as the first
Java integration seam rather than creating a custom HTTP provider layer.

```text
/ask-project spring-ai-agent-utils "Does Spring AI Agent Utils provide abstractions for provider configuration, model selection, or chat client creation that Codegeist can reuse privately?"
```

**Answer:** It does not provide a general provider configuration system. It does
provide `ClaudeSubagentType` and `ClaudeSubagentExecutor`, which route subagents
through a map of named `ChatClient.Builder` instances. The executor can parse a
subagent `model` as either `model` or `provider:model`, choose a named builder,
and apply `ChatOptions.builder().model(modelName)`.

**Evidence:** `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentType.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentExecutor.java`,
`spring-ai-agent-utils/docs/TaskTools.md`.

**Codegeist implication:** Reuse this idea privately only if it helps later agent
workflow tasks. Do not use it as the T006 provider config schema. Codegeist should
own its config binding and provider matrix boundaries.

```text
/ask-project spring-ai-agent-utils "Find examples of local Ollama integration, deterministic chat calls, or testcontainer-based model tests. Summarize the smallest pattern Codegeist should implement."
```

**Answer:** The reviewed Spring AI Agent Utils source did not contain Java
matches for `Ollama`, `ollama`, `Testcontainers`, `llama`, or model `seed` usage.
The examples focus on Anthropic, OpenAI SDK, and Google GenAI properties. There
is no source-backed local Ollama Testcontainer pattern in this project.

**Evidence:** Static search in `docs/third-party/spring-ai-agent-utils/source`,
`examples/*/src/main/resources/application.properties`.

**Codegeist implication:** For T006_05, use Spring AI's Ollama support and an
externally managed local Ollama instance with the selected model already
downloaded before the focused test starts. Keep calls deterministic with
temperature `0` and a fixed seed only when the selected Spring AI and Ollama
versions support it.

```text
/ask-project spring-ai-agent-utils "How does Spring AI Agent Utils configure ChatClient or ChatModel beans in Spring? Include annotations, properties, and test examples."
```

**Answer:** The examples rely on Spring Boot autoconfiguration to provide
`ChatClient.Builder`. Provider settings live in `application.properties` under
Spring AI keys such as `spring.ai.anthropic.api-key`,
`spring.ai.anthropic.chat.options.model`, `spring.ai.openai-sdk.api-key`, and
`spring.ai.google.genai.project-id`. Tests that need builders often use
`ChatClient.builder(mock(ChatModel.class))` rather than starting a real provider.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
`examples/*/src/main/resources/application.properties`,
`spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentTypeTest.java`.

**Codegeist implication:** Codegeist can map evaluated `codegeist.yml` values into
Spring properties or provider-specific client configuration. Tests for config
mapping can use mocked `ChatModel` or application context assertions before real
provider smokes exist.

```text
/ask-project spring-ai-agent-utils "Which Spring AI Agent Utils APIs are stable enough to use as private implementation details, and which should Codegeist wrap behind its own boundary?"
```

**Answer:** Spring AI APIs such as `ChatClient.Builder`, `ChatModel`,
`ChatOptions`, `ToolCallback`, `ToolCallAdvisor`, and `MessageChatMemoryAdvisor`
are the more reusable layer. Agent Utils classes such as `TaskTool`,
`ClaudeSubagentType`, `ClaudeSubagentExecutor`, skill loading, and default tool
bundles are useful implementation references but are opinionated around Claude
subagents and coding-agent tools.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/TaskTool.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentExecutor.java`.

**Codegeist implication:** Wrap Agent Utils behind Codegeist-owned boundaries if
used. Provider config, permissions, sessions, events, and smoke status should not
depend directly on Agent Utils package shapes.

## Spring AI Agent Utils Config And Binding

```text
/ask-project spring-ai-agent-utils "Does this project define configuration properties classes for agent or provider behavior? Show examples of @ConfigurationProperties, validation, and binding tests."
```

**Answer:** The reviewed source did not show project-owned
`@ConfigurationProperties` classes or binding tests for provider behavior. The
examples use Spring Boot property files consumed by Spring AI provider starters,
and Agent Utils builders use explicit Java builder methods plus `Assert` checks.

**Evidence:** Static search for `@ConfigurationProperties` and
`ConfigurationProperties` under `spring-ai-agent-utils/src/main/java`,
`examples/*/src/main/resources/application.properties`.

**Codegeist implication:** Codegeist cannot borrow a provider config binding model
from Agent Utils. It should continue with its own `CodegeistConfig`, Bean
Validation, and YAML mapping tests.

```text
/ask-project spring-ai-agent-utils "How does Spring AI Agent Utils structure Spring configuration classes, conditional beans, and auto-configuration around Spring AI components?"
```

**Answer:** The core library does not appear to ship broad Spring Boot
autoconfiguration for its tools. The examples configure behavior in application
classes using `@SpringBootApplication` and `@Bean` methods, while Spring AI itself
supplies the chat model/builder beans based on properties and dependencies.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
static search for `AutoConfiguration`, `ConditionalOn`, and Spring configuration
annotations under `spring-ai-agent-utils/src/main/java`.

**Codegeist implication:** Keep Codegeist Spring wiring explicit and small in the
first provider slices. Add autoconfiguration only when there is a real extension
or starter need.

```text
/ask-project spring-ai-agent-utils "Find examples where YAML or application properties are mapped into Spring model/provider configuration. Explain how Codegeist can adapt the pattern."
```

**Answer:** The examples use `.properties`, not YAML. They set Spring AI provider
properties for Anthropic, OpenAI SDK, and Google GenAI, including API keys, model
names, temperature, max tokens, project id, and location. These are consumed by
Spring AI, not by Agent Utils-specific binding code.

**Evidence:** `examples/todo-demo/src/main/resources/application.properties`,
`examples/subagent-demo/src/main/resources/application.properties`,
`examples/code-agent-demo/src/main/resources/application.properties`.

**Codegeist implication:** Codegeist can adapt the pattern by translating
evaluated `codegeist.yml` into Spring AI's expected property/client settings, but
that translation should be tested as Codegeist code rather than assumed from
Agent Utils.

```text
/ask-project spring-ai-agent-utils "Are there examples of validating provider config before creating Spring AI clients? Include error behavior and tests."
```

**Answer:** There are builder-level validations for Agent Utils constructs, such
as requiring a default `ChatClient.Builder` for `ClaudeSubagentType` and rejecting
null builder arguments. There is not a general provider-config validation layer
before Spring AI clients are created. Provider property validation is left to
Spring AI or to runtime failures.

**Evidence:** `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentType.java`,
`spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentTypeTest.java`.

**Codegeist implication:** Codegeist should validate evaluated config before
client creation using Bean Validation and focused error messages. Runtime provider
smoke failures should not be the first feedback for missing required config.

## Spring AI Agent Utils Prompt, Tools, And Agent Workflow

```text
/ask-project spring-ai-agent-utils "Which source files implement the core agent prompt workflow from user input to model response? Create a concise implementation map for Codegeist."
```

**Answer:** The code-agent demo maps user input to model response directly:
Spring Boot starts, a `CommandLineRunner` builds `ChatClient`, a scanner reads a
line, and `chatClient.prompt(scanner.nextLine()).call().content()` prints the
assistant response. The builder configures system prompt parameters, MCP tool
callbacks, skills, common tools, `ToolCallAdvisor`, and memory advisor before the
loop starts.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`.

**Codegeist implication:** This is useful for later CLI/TUI chat flow, but T006
only needs enough provider wiring to build or smoke a client. Keep prompt loop,
memory, and tool orchestration out of T006_03.

```text
/ask-project spring-ai-agent-utils "How are tool calls, tool permissions, or tool registries modeled in Spring AI Agent Utils? Identify what is relevant later for Codegeist and what is out of scope for T006."
```

**Answer:** Tools are Spring AI `ToolCallback` objects or method-backed tool
callbacks. `ClaudeSubagentType` builds a default callback list with tools such as
Grep, Glob, Shell, FileSystem, SmartWebFetch, and TodoWrite. `ClaudeSubagentExecutor`
filters tools by subagent `tools` and `disallowedTools`. `permissionMode` is
recognized but not implemented beyond a warning when not `default`.

**Evidence:** `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentType.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentExecutor.java`,
`spring-ai-agent-utils/docs/TaskTools.md`.

**Codegeist implication:** Tool filtering is later agent-runtime work. T006 should
not introduce tool permission contracts while defining provider config parsing.

```text
/ask-project spring-ai-agent-utils "How does Spring AI Agent Utils represent agent sessions, messages, events, and response streaming? Summarize reusable implementation ideas for later Codegeist tasks."
```

**Answer:** Agent Utils has background task support through `TaskRepository` and
`BackgroundTask`, plus synchronous subagent execution through `SubagentExecutor`.
The reviewed examples use blocking `call().content()` rather than streaming.
There is no OpenCode-like session/event store in the reviewed core workflow.

**Evidence:** `spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/TaskTool.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/repository/DefaultTaskRepository.java`,
`examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`.

**Codegeist implication:** Reuse only the idea of a small task repository or
background execution boundary later. Codegeist sessions, event streams, and TUI
updates should be Codegeist-owned contracts.

```text
/ask-project spring-ai-agent-utils "Find tests that prove agent workflow behavior without mocking the model provider. What testing patterns are useful for Codegeist?"
```

**Answer:** The reviewed tests largely avoid real model providers. They use
Mockito for `ChatClient`, `ChatModel`, and tool collaborators, and use temporary
directories for file/tool behavior. No source-backed test was found that proves a
real model workflow without mocking the provider.

**Evidence:** `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/SmartWebFetchToolTest.java`,
`spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentTypeTest.java`,
`spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolTest.java`.

**Codegeist implication:** Use structural tests for config, wiring, and validation,
but add Codegeist-owned local Ollama integration coverage for the first real model
workflow. Agent Utils does not supply that pattern.

## Spring AI Agent Utils Testing And Smokes

```text
/ask-project spring-ai-agent-utils "What are the project's best examples of Spring integration tests for AI workflows? Include test setup, assertions, and runtime prerequisites."
```

**Answer:** The reviewed core tests are mostly unit and compatibility tests rather
than Spring integration tests for live AI workflows. They assert builder failures,
tool behavior, repository behavior, markdown parsing, and compatibility against
external tools such as ripgrep. Runtime prerequisites are usually local binaries
or temp files, not AI provider accounts.

**Evidence:** `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentTypeTest.java`,
`spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolCompatibilityTest.java`,
`spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/task/repository/DefaultTaskRepositoryTest.java`.

**Codegeist implication:** For Codegeist, use Spring integration tests where they
prove Codegeist Spring binding or CLI behavior. Do not expect Agent Utils to cover
provider-account integration testing.

```text
/ask-project spring-ai-agent-utils "Does Spring AI Agent Utils use local providers, Testcontainers, or fake providers in tests? Compare tradeoffs for Codegeist's first Ollama workflow."
```

**Answer:** No reviewed Java source uses local AI providers, Ollama, or
Testcontainers. The tests use mocks for chat clients/models and real local files
or processes for tool behavior. This keeps tests fast and deterministic but does
not prove provider wiring against a real model server.

**Evidence:** Static source search for `Ollama`, `Testcontainers`, and related
terms under `docs/third-party/spring-ai-agent-utils/source`; tests under
`spring-ai-agent-utils/src/test/java`.

**Codegeist implication:** Codegeist's first Ollama workflow should explicitly
exercise one local integration path against an externally managed ready Ollama
instance, while keeping most config tests provider-free and fast.

```text
/ask-project spring-ai-agent-utils "Find examples of deterministic model calls or constrained assertions that avoid flaky AI tests."
```

**Answer:** No deterministic live model calls were found. Determinism is achieved
by avoiding live model calls: tests mock `ChatClient` responses, assert exception
messages, assert exact tool output, or compare pure Java tool behavior against a
local binary.

**Evidence:** `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/SmartWebFetchToolTest.java`,
`spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolCompatibilityTest.java`.

**Codegeist implication:** For live Ollama tests, constrain assertions to stable
observable behavior such as non-empty response, expected marker when possible,
successful Spring client wiring, and explicit skipped/blocked statuses. Avoid
semantic assertions that require a specific prose answer.

```text
/ask-project spring-ai-agent-utils "How are skipped, blocked, or environment-dependent tests represented? Give implementation guidance for Codegeist provider smoke statuses."
```

**Answer:** The clearest pattern is JUnit `@EnabledIf` in compatibility tests that
depend on `rg`. Tests are enabled only when the prerequisite is present. Other
tests fail fast on invalid inputs using assertions. There is no project-specific
`blocked` status model for AI providers.

**Evidence:** `spring-ai-agent-utils/src/test/java/org/springaicommunity/agent/tools/GrepToolCompatibilityTest.java`.

**Codegeist implication:** T006_06 should model smoke outcomes explicitly instead
of relying only on JUnit enablement. Report `passed`, `skipped` with reason,
`blocked` with missing prerequisite, or `failed` with error details.

```text
/ask-project spring-ai-agent-utils "What Maven or Spring test selectors are used for narrow AI integration tests? Recommend a Codegeist test command shape."
```

**Answer:** The parent Maven build uses Surefire and Failsafe plugins, and normal
narrow test selection can use Maven module and `-Dtest` selectors. The reviewed
source does not define special AI integration selectors. Codegeist should prefer
its repo-local Taskfile entries when available, then use Maven's narrow selectors
for focused Spring tests.

**Evidence:** `pom.xml`, `spring-ai-agent-utils/src/test/java`.

**Codegeist implication:** Recommended Codegeist command shape should stay aligned
with local conventions, such as a Taskfile smoke target for Ollama and a narrow
Maven command for config tests. Each solve result should report the exact command
and timing.

## Comparative Questions For Both Projects

```text
/ask-project opencode "For Codegeist T006, what implementation lessons should be taken from OpenCode's provider config, auth store, and provider runtime boundaries?"
```

**Answer:** The main OpenCode lesson is boundary separation. Raw config is parsed
and validated, auth is stored separately, provider metadata comes from a provider
database, runtime provider objects are built later, and chat requests use those
runtime objects. OpenCode also proves that enabled/disabled provider filters and
provider-specific options need clear precedence.

**Evidence:** `packages/opencode/src/config/config.ts`,
`packages/opencode/src/auth/index.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/session/llm.ts`.

**Codegeist implication:** Keep T006_03 narrow. Implement SpEL config evaluation
first, provider matrix next, local Ollama smoke after that, and auth/account
storage only as later focused work.

```text
/ask-project spring-ai-agent-utils "For Codegeist T006, what implementation lessons should be taken from Spring AI Agent Utils' Spring Boot integration and AI workflow tests?"
```

**Answer:** The main Spring AI Agent Utils lesson is to lean on Spring AI's
`ChatClient.Builder` and Spring Boot property-driven provider setup. Its tests
show fast structural validation with Mockito, AssertJ, temp directories, and
conditional prerequisites. It does not provide a ready local Ollama smoke pattern.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
`examples/*/src/main/resources/application.properties`,
`spring-ai-agent-utils/src/test/java`.

**Codegeist implication:** Use Spring-first implementation and tests. Add real
local provider smoke coverage in Codegeist because the third-party project does
not cover that gap.

```text
/ask-project opencode "Create a migration assessment: OpenCode provider config/auth behavior -> Codegeist Java/Spring design. Focus on concepts, not TypeScript implementation."
```

**Answer:** OpenCode concepts map to Codegeist as follows: `provider` map maps to
`CodegeistConfig.provider`, `enabled_providers` and `disabled_providers` map to
kebab-case Codegeist fields, provider options map to nested option maps or typed
provider properties, auth store maps to future protected credential storage, and
runtime provider state maps to Spring beans or factory services. Dynamic NPM
loading, AI SDK models, and Effect services do not map directly.

**Evidence:** `packages/opencode/src/config/provider.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/auth/index.ts`.

**Codegeist implication:** Build Java records/classes and Spring services around
Codegeist contracts, not around OpenCode's TypeScript runtime object graph.

```text
/ask-project spring-ai-agent-utils "Create a migration assessment: Spring AI Agent Utils provider/chat workflow -> Codegeist Java/Spring design. Focus on reusable APIs and wrapper boundaries."
```

**Answer:** Spring AI Agent Utils maps well at the API level: inject
`ChatClient.Builder`, configure default tools and advisors, apply `ChatOptions`,
and use `ToolCallback` when tools arrive. Its subagent and task abstractions are
agent-runtime concepts, not provider config concepts. Its provider selection is a
builder-map convenience, not a full config system.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentExecutor.java`,
`spring-ai-agent-utils/docs/TaskTools.md`.

**Codegeist implication:** Use Spring AI APIs directly or behind thin Codegeist
wrappers. Keep Codegeist provider config, policy, smoke statuses, and
sensitive-output policy owned by Codegeist.

```text
/ask-project opencode "What should Codegeist ask from OpenCode before implementing unrestricted SpEL-backed codegeist.yml parsing, sensitive config output, and remote_free provider smoke gates?"
```

**Answer:** Ask OpenCode for config source precedence, config variable
substitution timing, strict vs open schema boundaries, provider env/auth merge
behavior, resolved config output surfaces, and whether remote provider calls have
pre-call cost gates. The source-backed answers above show the key findings:
substitution before validation, strict top-level schema with open provider
options, separate auth store, possible resolved-config secret exposure, and no
general remote_free gate.

**Evidence:** `packages/opencode/src/config/config.ts`,
`packages/opencode/src/config/variable.ts`,
`packages/opencode/src/config/parse.ts`,
`packages/opencode/src/provider/provider.ts`,
`packages/opencode/src/server/routes/instance/config.ts`,
`packages/opencode/src/session/llm.ts`.

**Codegeist implication:** These answers are sufficient for the current T006_03
documentation slice. Re-ask only when implementing a concrete provider/auth/smoke
behavior that needs line-level confirmation.

```text
/ask-project spring-ai-agent-utils "What should Codegeist ask from Spring AI Agent Utils before implementing SpEL-backed codegeist.yml parsing and Spring AI provider creation?"
```

**Answer:** Ask for Spring Boot `ChatClient.Builder` integration examples,
provider properties used by examples, whether Agent Utils has config binding or
provider abstractions, whether local Ollama/Testcontainers examples exist, and
which tests demonstrate stable workflow behavior. The source-backed answers show
that useful evidence exists for `ChatClient.Builder` and structural tests, but
not for provider config binding or Ollama smokes.

**Evidence:** `examples/code-agent-demo/src/main/java/org/springaicommunity/agent/Application.java`,
`examples/*/src/main/resources/application.properties`,
`spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/task/claude/ClaudeSubagentExecutor.java`,
`spring-ai-agent-utils/src/test/java`.

**Codegeist implication:** Use this project as a Spring AI usage reference, not as
the source of Codegeist's config parser or smoke harness design.

## Recommended Question Order

1. OpenCode config merge and provider map behavior.
2. OpenCode config variable substitution and resolved-config sensitive-output risks.
3. OpenCode provider runtime and remote-call safety behavior.
4. Spring AI Agent Utils `ChatClient` and `ChatModel` wiring.
5. Spring AI Agent Utils Ollama and testing examples.
6. Comparative migration assessments for Codegeist T006.

## Current T006 Takeaways

- Keep `T006_03` as a SpEL-only config evaluation strategy.
- Treat `codegeist.yml` as trusted local input for the first SpEL slice.
- Evaluate scalar values before mapping into `CodegeistConfig` and Bean Validation.
- Do not add `api-key-env`, `profile-env`, `auth-store-ref`, OAuth, account setup,
  or model selection to `T006_03`.
- Treat evaluated config as sensitive because SpEL may materialize secrets.
- Keep provider calls out of parsing and validation.
- Put local Ollama verification in `T006_05` and remote-safe smoke gating in
  `T006_06`.
