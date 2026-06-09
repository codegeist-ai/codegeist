# T006_04 Implement Codegeist YML Loading

Status: solved

Parent: `../task.md`

## Goal

Implement focused config access for Codegeist provider configuration using only
the first two supported provider config classes: `ollama` and `openai`.

The slice supports both Spring-bound application configuration and direct YAML
loading for an explicit `codegeist.yml` path. Provider entries map into normal Java
config classes selected from the required YAML `type` field. This task remains
config-only and must not introduce provider runtime abstractions, Spring AI
provider starters, provider clients, local daemon startup, model pulls, smoke
commands, or remote provider calls.

## Scope

- Work in `app/codegeist/cli`.
- Use `T006_01` for the base loader contract and `T006_03` for the trusted-local
  SpEL decisions.
- Keep the first implementation limited to `OllamaProviderConfig` and
  `OpenAiProviderConfig`.
- Keep all other provider types unsupported in this task, including
  `docker-model-runner`, `azure-openai`, `anthropic`, `bedrock-converse`,
  `google-genai`, `deepseek`, `minimax`, `mistral-ai`, `groq`, `nvidia`,
  `perplexity`, `openrouter`, `moonshot`, `qianfan`, OpenCode Zen, and
  OpenCode Go.
- Keep `ProviderConfig` as an abstract config-data base class, not a runtime
  provider adapter.
- Keep provider credentials as ordinary scalar values that may be populated by
  SpEL. Do not add a credential-reference schema such as `api-key-env`.

## Supported Provider Config Classes

| Config class | Provider type | Minimum fields to bind and validate now |
| --- | --- | --- |
| `OllamaProviderConfig` | `ollama` | Stored fields: `name` and `base-url`; YAML `type` is dispatch-only and read back from `getType()`. |
| `OpenAiProviderConfig` | `openai` | Stored fields: `name`, `api-key`, optional `base-url`, `organization-id`, and `project-id`; YAML `type` is dispatch-only and read back from `getType()`. |

## Implemented Behavior

- `CodegeistApplication.APP_NAME` remains the shared application name and Spring
  config prefix source.
- `CodegeistConfig` stores parsed root elements under `ai.codegeist.app.config`,
  and `ProvidersRootElement` parses the raw provider map into typed provider config
  values.
- `ProviderConfig` is an abstract base class for provider map values and
  currently permits only `OllamaProviderConfig` and `OpenAiProviderConfig`.
- Provider map values are selected from the required YAML `type` field through
  registered provider type constants. There is no fallback to the provider map key.
- `CodegeistConfigService.loadConfig(String)` reads direct YAML into a Jackson
  tree, evaluates Spring SpEL only in string scalar values containing `#{`, maps
  raw provider objects into concrete provider config classes, and runs explicit
  Bean Validation after mapping.
- YAML keys, provider ids, list indexes, comments, aliases, maps, and non-string
  scalars are not evaluated by SpEL.
- Missing provider `type` and unsupported provider types are rejected during typed
  dispatch and may surface as Jackson mapping errors. SpEL failures use
  `CodegeistConfigValidationException` with source and path context, and Bean
  Validation failures include the config path.
- `--show-config` prints direct `codegeist.yml` YAML without a `codegeist:` wrapper
  or YAML document marker, leaves configured values unchanged, and preserves empty
  default config output as `{}` without creating synthetic roots.
- The current implementation has no model-level multi-source combination helper;
  each load returns one mapped `CodegeistConfig` instance.

## Non-Goals

- Do not add support classes for providers beyond `ollama` and `openai` in this
  task.
- Do not add Spring AI provider starters here.
- Do not call Ollama or remote provider APIs.
- Do not create an encrypted credential store.
- Do not add runtime provider clients, `ChatModel`/`ChatClient` creation, provider
  factories, runtime provider registries, adapter hierarchies, smoke commands,
  local daemon startup, model pulls, or remote API calls.
- Do not implement home-path discovery, service-level source-combination orchestration,
  inheritance, provider selection, model catalogs, fallback policy, or delete
  semantics in this slice.
- Do not introduce Spring `EnvironmentPostProcessor` or `spring.factories` loading
  for `codegeist.yml`.

## Acceptance Criteria

- A focused test proves `CodegeistConfigService` receives typed `ollama` and
  `openai` config from Spring application YAML.
- A focused test proves `CodegeistConfigService.loadConfig(String)` maps an
  explicit YAML file to `CodegeistConfig`.
- Focused tests prove explicit YAML loading rejects blank provider ids and blank
  provider names when `name` is present.
- Focused tests prove provider `name` remains optional.
- Focused tests prove string scalar values containing `#{` are evaluated with
  Spring SpEL before mapping, while YAML keys and non-string scalars stay literal.
- Focused tests prove SpEL parse/evaluation failures include source and YAML path
  context without printing evaluated secret values.
- The config model supports provider `type` as a dispatch-only discriminator,
  `base-url`, and ordinary scalar OpenAI credentials. `T006_06` later removed model
  selection from `ProviderConfig`, and follow-up removals kept `enabled`,
  `completions-path`, and generation options out of provider config too; those
  choices now belong to runtime agent, session, command, request, or provider
  feature test selection.
- Missing provider `type` is rejected.
- Unsupported provider `type` values are rejected, including the broader provider
  matrix types that are intentionally deferred from this task.
- Validation rejects missing or blank required fields for `ollama` and `openai`
  after SpEL evaluation, without calling the provider.
- `--show-config` writes only direct YAML to stdout, omits the Spring `codegeist:`
  wrapper, omits YAML document markers, leaves configured values unchanged, and
  keeps stderr empty.
- Empty config output is `{}` and does not create top-level root elements.
- Current-state architecture docs reflect the implemented two-provider config
  service behavior.

## Verification

```bash
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistConfigCommandTest,CodegeistConfigServiceTest test
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistProviderConfigTest test
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistConfigSpelEvaluationTest test
mvn --batch-mode --no-transfer-progress test
git --no-pager diff --check
```

## Result

- Implemented the focused `ollama` and `openai` config-only YAML loading slice.
- Deferred all other provider config classes to later provider-specific tasks.
- Added focused tests for parsed primary config, explicit path loading, SpEL
  evaluation, explicit provider registry dispatch, unsupported provider rejection,
  validation, and command output.
- Updated `docs/developer/architecture/architecture.md`,
  `docs/developer/architecture/provider-configuration.md`, and
  `docs/memory-bank/chat.md` to describe the current two-provider state.
