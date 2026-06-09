# T006_01 Specify Codegeist Provider Config Loading

Status: solved

Parent: `../task.md`

## Goal

Specify the smallest useful Codegeist provider configuration model and loading
direction.

The first specification targeted a plain Codegeist-owned POJO that could be used
in two ways:

- Spring could bind it from `application.yaml` with `@ConfigurationProperties`.
- Jackson YAML could load the same model directly from a `codegeist.yml` file.

The current implementation has evolved into root-element parsing. `CodegeistConfig`
now stores `List<CodegeistConfigRootElement>`, `provider:` is the provider root,
and `CodegeistConfigService` dispatches direct `codegeist.yml` roots through
injected `CodegeistConfigRootElement` parser components.

## Decisions

- Define `CodegeistApplication.APP_NAME = "codegeist"` as the shared application
  name and configuration prefix constant.
- Keep `CodegeistConfig.CONFIGURATION_PREFIX` defined from
  `CodegeistApplication.APP_NAME` so `codegeist.config` stays aligned with the app
  name.
- Use `CodegeistConfigRootElement` parser components with explicit `rootName()`
  constants for direct YAML roots.
- Keep Jackson annotations on config POJOs so direct YAML loading remains possible.
- Use Jackson YAML via `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`
  for direct file loading.
- Keep Lombok focused: `@Getter` and `@Setter` are acceptable for the config POJOs.
- The provider root is top-level `provider`; there is no `providers` alias.
- Current provider entries are typed by `type` and stay access-only; model and
  generation-option selection remain outside provider config.
- Do not add top-level provider filter lists, `models`, capabilities, limits, or
  runtime selection fields to provider config yet.

## Java Shape

The current model is equivalent to this shape:

```java
@Getter
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodegeistConfig {
    private final List<CodegeistConfigRootElement> rootElements = new ArrayList<>();
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

`CodegeistConfigService` owns access to this config in `ai.codegeist.app.config`.
It exposes a direct `loadConfig(String configPath)` method for loading a specific
`codegeist.yml` path with Jackson YAML. `application.yaml` is not a Codegeist root
config source.

## File Loading Direction

- The default `codegeist.yml` location is the user's home config path:
  `~/.config/codegeist/codegeist.yml`.
- `codegeist.yml` is a dedicated file and should map directly to top-level root
  elements such as `provider:` and `mcp:`. It does not need a `codegeist` wrapper.
- `application.yaml` must not configure Codegeist roots such as `provider:` or
  `mcp:`. Keep application YAML limited to Spring Boot and Spring Shell settings.
- Do not use Spring `EnvironmentPostProcessor`, `spring.factories`, or
  `application.yaml` import tricks to load `codegeist.yml`.
- Do not use Spring `Binder` to load `codegeist.yml`; direct file loading belongs
  to Jackson YAML.

## Primary Config Direction

This first task only defined the initial source order idea. The current
implementation has no model-level merge or combination helper:

- The primary config bean currently parses `codegeist.config` when that property is
  set and otherwise returns an empty `CodegeistConfig`.
- Explicit `codegeist.yml` paths are loaded as single files through
  `CodegeistConfigService.loadConfig(String configPath)`.
- Later home-path or startup-file work must define combination semantics before
  adding additional sources.

Historical candidate source order, if future work defines combination semantics,
from lowest to highest precedence:

1. Home config file at `~/.config/codegeist/codegeist.yml`.
2. An explicit startup config path when one is provided later.

Do not implement broader inheritance or delete semantics in the first loader.

## Validation Direction

With the current minimal model, validation should stay annotation-first and small:

- Add Bean Validation through `spring-boot-starter-validation`.
- Put constraints on the config POJOs instead of starting with a broad custom
  validation framework.
- Validate provider YAML keys during root parsing and keep Bean Validation on mapped
  provider config values.
- Keep provider `name` optional, but reject it with a Bean Validation annotation
  when it is present and blank.
- Run `jakarta.validation.Validator` after direct Jackson YAML loads because
  Jackson does not evaluate Bean Validation annotations by itself.
- Validate the parsed root-element `CodegeistConfig` through the explicit
  `jakarta.validation.Validator` call in `CodegeistConfigService`.
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

- The config model uses root elements and exposes derived provider accessors.
- `CodegeistConfig.CONFIGURATION_PREFIX` remains backed by
  `CodegeistApplication.APP_NAME` for the `codegeist.config` property.
- Jackson YAML can map a direct `codegeist.yml` file into root elements.
- `CodegeistConfigService` can load an explicit YAML path through
  `loadConfig(String configPath)`.
- Tests cover parsed primary config service access and explicit YAML path loading.
- No Spring environment post-processor or `spring.factories` loader is introduced.

## Verification

Specification-only verification for this task:

```bash
git --no-pager diff --check
```

## Solve Result

- The provider configuration specification is complete for the current minimal
  slice.
- The accepted model is `ai.codegeist.app.config.CodegeistConfig` with root
  elements. The provider YAML root is `provider:`.
- The primary config bean is selected by unqualified `CodegeistConfig` injection
  through Spring `@Primary`.
- `CodegeistConfigService` owns direct explicit-path YAML loading.
- Runtime home-path loading, service-level multi-source orchestration, provider
  options, credentials, model selection, and provider calls remain in later T006
  child tasks. Annotation-based validation belongs to `T006_04` with the loader
  implementation.

## Verification Result

- `git --no-pager diff --check` passed.
