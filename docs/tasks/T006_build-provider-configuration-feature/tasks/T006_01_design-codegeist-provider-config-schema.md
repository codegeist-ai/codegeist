# T006_01 Specify Codegeist Provider Config Loading

Status: solved

Parent: `../task.md`

## Goal

Specify the smallest useful Codegeist provider configuration model and loading
direction.

The config model should stay a plain Codegeist-owned POJO that can be used in two
ways:

- Spring can bind it from `application.yaml` with `@ConfigurationProperties`.
- Jackson YAML can load the same model directly from a `codegeist.yml` file.

## Decisions

- Define `CodegeistApplication.APP_NAME = "codegeist"` as the shared application
  name and configuration prefix constant.
- Use `@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)` on
  the config model so Spring can bind the same POJO from application configuration.
  `CodegeistConfig.CONFIGURATION_PREFIX` should be defined from
  `CodegeistApplication.APP_NAME`.
- Use Jackson annotations on the same POJO so direct YAML loading remains possible.
- Use Jackson YAML via `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`
  for direct file loading.
- Keep Lombok focused: `@Getter` and `@Setter` are acceptable for the config POJOs.
- Keep the first config model intentionally tiny: top-level `provider` map only.
- Keep each provider entry limited to `name` for now.
- Do not add `model`, `small-model`, top-level provider filter lists, `type`,
  `credentials`, `options`, `models`, capabilities, or limits yet.

## Java Shape

The initial model should be equivalent to this shape:

```java
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)
public class CodegeistConfig {
    private Map<String, ProviderConfig> provider = new LinkedHashMap<>();
}
```

Use `CodegeistConfig` for the model name. The primary config is exposed as the
primary `CodegeistConfig` bean, so normal unqualified injection receives that
config.

```java
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderConfig {
    private String name;
}
```

`CodegeistConfigService` should own access to this config in
`ai.codegeist.app.config`. It may receive the Spring-bound config properties from
`application.yaml` and should expose a direct `loadConfig(String configPath)`
method for loading a specific `codegeist.yml` path with Jackson YAML.

## File Loading Direction

- The default `codegeist.yml` location is the user's home config path:
  `~/.config/codegeist/codegeist.yml`.
- `codegeist.yml` is a dedicated file and should map directly to the config
  properties POJO. It does not need a `codegeist` wrapper.
- `application.yaml` may still configure the same POJO through the Spring prefix
  from `CodegeistApplication.APP_NAME`.
- Do not use Spring `EnvironmentPostProcessor`, `spring.factories`, or
  `application.yaml` import tricks to load `codegeist.yml`.
- Do not use Spring `Binder` to load `codegeist.yml`; direct file loading belongs
  to Jackson YAML.

## Primary Config Direction

This first task only defined the initial source order idea. The current
implementation has no model-level merge or combination helper:

- The primary config bean currently returns the Spring-bound config.
- Explicit `codegeist.yml` paths are loaded as single files through
  `CodegeistConfigService.loadConfig(String configPath)`.
- Later home-path or startup-file work must define combination semantics before
  adding additional sources.

Historical candidate source order, if future work defines combination semantics,
from lowest to highest precedence:

1. Spring-bound defaults from `application.yaml`.
2. Home config file at `~/.config/codegeist/codegeist.yml`.
3. An explicit startup config path when one is provided later.

Do not implement broader inheritance or delete semantics in the first loader.

## Validation Direction

With the current minimal model, validation should stay annotation-first and small:

- Add Bean Validation through `spring-boot-starter-validation`.
- Put constraints on the config POJOs instead of starting with a broad custom
  validation framework.
- Validate provider map keys with `Map<@NotBlank String, @Valid ProviderConfig>`.
- Keep provider `name` optional, but reject it with a Bean Validation annotation
  when it is present and blank.
- Run `jakarta.validation.Validator` after direct Jackson YAML loads because
  Jackson does not evaluate Bean Validation annotations by itself.
- Let Spring validate the Spring-bound `@ConfigurationProperties` model through
  `@Validated`.
- Do not validate model references because the model has no model fields yet.
- Keep unknown YAML keys ignored for now; strict unknown-key handling is not part
  of the annotation-first validation slice.

## Non-Goals

- Do not implement provider calls.
- Do not add Spring AI provider starters.
- Do not add credential fields or account handling.
- Do not add model selection fields.
- Do not add provider options or typed provider configuration.
- Do not implement inheritance or `extends`/`imports` behavior.
- Do not introduce broad runtime provider abstractions.

## Acceptance Criteria

- The config model has only a provider map and provider names.
- `@ConfigurationProperties(prefix = CodegeistConfig.CONFIGURATION_PREFIX)` is used
  for Spring application configuration, with the prefix constant backed by
  `CodegeistApplication.APP_NAME`.
- Jackson YAML can map a direct `codegeist.yml` file into the same POJO.
- `CodegeistConfigService` can receive the Spring-bound config and can load an
  explicit YAML path through `loadConfig(String configPath)`.
- Tests cover Spring-bound config service access and explicit YAML path loading.
- No Spring environment post-processor or `spring.factories` loader is introduced.

## Verification

Specification-only verification for this task:

```bash
git --no-pager diff --check
```

## Solve Result

- The provider configuration specification is complete for the current minimal
  slice.
- The accepted model is `ai.codegeist.app.config.CodegeistConfig` with a
  top-level `provider` map whose `ProviderConfig` entries currently contain only
  `name`.
- The primary config bean is selected by unqualified `CodegeistConfig` injection
  through Spring `@Primary`.
- `CodegeistConfigService` owns access to the Spring-bound config properties and
  direct explicit-path YAML loading.
- Runtime home-path loading, service-level multi-source orchestration, provider
  options, credentials, model selection, and provider calls remain in later T006
  child tasks. Annotation-based validation belongs to `T006_04` with the loader
  implementation.

## Verification Result

- `git --no-pager diff --check` passed.
