# T006_04 Implement Codegeist YML Loading

Status: open

Parent: `../task.md`

## Goal

Implement focused config access for the smallest Codegeist provider configuration
model.

The current slice should support both Spring-bound application configuration and
direct YAML loading for an explicit `codegeist.yml` path without introducing broad
provider runtime abstractions.

## Scope

- Work in `app/codegeist/cli`.
- Use the `T006_01` specification as the source of truth.
- Add Lombok support for focused getter/setter boilerplate reduction and `@Slf4j`
  diagnostics on Spring service/component classes.
- Add Jackson YAML support for direct YAML-to-POJO mapping.
- Add Spring Bean Validation support for annotation-based config validation.
- Add `CodegeistApplication.APP_NAME` as the shared application name and Spring
  config prefix constant.
- Add `CodegeistConfig` under `ai.codegeist.app.config` with
  `@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)`, a
  prefix constant backed by `CodegeistApplication.APP_NAME`, Jackson naming
  metadata, and only a provider map.
- Add `ProviderConfig` with only `name`.
- Add `CodegeistConfigService` that receives the Spring-bound config through
  qualified `@Autowired`, exposes a primary `mergedCodegeistConfig` `@Bean`, and
  exposes `loadConfig(String configPath): CodegeistConfig`.
- Add non-mutating `merge(...)` methods on each current config model type so later
  source-order work can compose Spring-bound, home, and explicit-path configs.
- Use unqualified `CodegeistConfig` injection for callers that need the primary
  merged config bean.
- Add `--show-config` as a Spring Shell command that prints only the current
  merged config in direct `codegeist.yml` YAML form.
- Add focused tests for application YAML binding, explicit YAML path loading, and
  annotation-backed validation.

## Non-Goals

- Do not add Spring AI provider starters here.
- Do not call Ollama or remote provider APIs.
- Do not create an encrypted credential store.
- Do not add model fields, provider options, credentials, capabilities, or limits.
- Do not implement home-path discovery, service-level source merge orchestration,
  inheritance, or delete semantics in this slice.
- Do not introduce Spring `EnvironmentPostProcessor` or `spring.factories` loading
  for `codegeist.yml`.
- Do not create broad runtime provider abstractions before tests need them.

## Acceptance Criteria

- A focused test proves `CodegeistConfigService` receives config from Spring
  application YAML.
- A focused test proves `CodegeistConfigService.loadConfig(String)` maps an
  explicit YAML file to `CodegeistConfig`.
- Focused tests prove explicit YAML loading rejects blank provider ids and blank
  provider names when `name` is present.
- Focused tests prove provider `name` remains optional.
- The model stays limited to provider ids and provider names.
- The implementation uses field `@Autowired` for Spring injection.
- The implementation uses focused Lombok annotations only where they reduce simple
  boilerplate.
- Spring `@Service` and `@Component` classes in this slice use Lombok `@Slf4j` and
  emit concise debug messages for merged-bean creation, explicit YAML loading, and
  validation outcomes.
- `--show-config` writes only direct YAML to stdout, omits the Spring `codegeist:`
  wrapper, omits YAML document markers, and keeps stderr empty.
- Empty config output keeps the top-level shape visible as `provider: {}`.
- Each current config model type exposes a non-mutating `merge(...)` method.
- Config merge keeps the `provider.<id>` shape, adds new providers, and lets later
  non-null provider fields replace earlier values.
- Validation is primarily annotation-based on config POJOs, with an explicit
  `Validator` call after direct Jackson YAML loading.
- `docs/developer/architecture/architecture.md` reflects the implemented config
  service behavior.

## Verification

Start with a focused Maven selector from `app/codegeist/cli`, then run the broader
test suite:

```bash
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistConfigCommandTest,CodegeistConfigServiceTest test
mvn --batch-mode --no-transfer-progress -Dtest=CodegeistConfigMergeTest test
mvn --batch-mode --no-transfer-progress test
git --no-pager diff --check
```

## Planning Notes

- Read `docs/developer/specification/java-generation-guidance.md` before adding
  Java source.
- Read `docs/developer/specification/testing-strategy-and-agent-rules.md` before
  adding tests.
- Read `.oc_local/rules/java-coding.md` before writing Java source.
- Keep any diagnostic command machine-readable and log-free on stdout, following
  the current `--version` precedent.

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
