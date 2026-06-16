# Codegeist Java Coding Rule

Use this rule when adding or changing Java source in Codegeist.

## Spring Wiring

- Prefer annotation-based Spring wiring and metadata.
- For Spring-managed dependencies in production beans, use Lombok
  `@RequiredArgsConstructor` on the bean and `private final` collaborator fields.
  Do not add new `@Autowired` fields for normal Spring bean injection.
- Prefer Spring dependency injection for Spring-managed collaborators and shared
  framework objects instead of passing those dependencies through method parameters.
  Method parameters should represent per-call input data; inject dependencies such
  as an owned `ObjectMapper`, service, repository, or validator into the receiving
  Spring bean.
- Prefer Lombok-generated constructor injection over hand-written constructors for
  Spring beans. Keep explicit constructors only when annotations, superclass
  constants, or custom initialization make the generated constructor unclear.
- Do not use Lombok `@AllArgsConstructor` to inject Spring dependencies; prefer
  `@RequiredArgsConstructor` so optional or non-final fields such as `@Value`
  properties are not constructor parameters.
- Prefer Spring annotations such as `@Component`, `@ConfigurationProperties`,
  `@EnableConfigurationProperties`, `@Qualifier`, `@Value`, and test annotations when they
  make ownership and runtime behavior explicit.
- For the primary config bean, inject unqualified `CodegeistConfig` and let
  Spring `@Primary` select it. `CodegeistConfigService` owns conversion from
  explicit `codegeist.yml` files into root elements; `application.yaml` is not a
  Codegeist config source.
- `CodegeistConfigService` is the reference shape for constructor-injected Spring
  services: use Lombok `@RequiredArgsConstructor` with final collaborator fields,
  keep `configPath` as a non-final `@Value` field, and inject the concrete
  `CodegeistConfigYamlMapper` type instead of a qualified generic `ObjectMapper`.
- When a Spring dependency would need `@Qualifier` only to distinguish generic
  infrastructure beans such as Jackson mappers, prefer a domain-specific bean type
  such as `CodegeistConfigYamlMapper` over Lombok compiler configuration or
  qualifier-copy workarounds.
- Prefer Bean Validation annotations on configuration POJOs for config validation.
  When config is loaded directly with Jackson rather than Spring binding, call
  `jakarta.validation.Validator` explicitly after mapping.

## Lombok

- Use Lombok to reduce simple Java boilerplate when it keeps the source easier to
  read.
- Prefer focused Lombok annotations such as `@Getter` and `@Setter` over broad
  annotations such as `@Data`.
- For trivial enum or value-object constructor and accessor boilerplate, prefer
  focused Lombok annotations such as `@RequiredArgsConstructor` and `@Getter` with
  the narrowest useful access level over hand-written boilerplate.
- For non-Spring abstract base classes with only required final fields, prefer
  Lombok `@RequiredArgsConstructor(access = AccessLevel.PROTECTED)` plus `@NonNull`
  fields over hand-written protected constructors.
- Keep explicit constructors when Jackson annotations, superclass constants, or
  custom initialization make generated constructors unclear.
- Use Lombok `@NonNull` for simple required constructor or method parameters instead
  of hand-written `Assert.notNull(...)` checks. If a method parameter must not be
  Java `null`, annotate that parameter with Lombok `@NonNull` instead of relying on
  an undocumented caller contract. Keep explicit assertions when they validate richer
  contracts such as non-blank text or add domain-specific messages. Do not add
  `@NonNull` where Java `null` is valid input by contract; handle that case
  explicitly.
- Use Lombok `@Slf4j` on Spring `@Service` and `@Component` classes, and prefer
  concise `log.debug(...)` messages around non-obvious lifecycle, command,
  loading, validation, or bean-creation behavior. Spring Boot's default logging
  stack routes SLF4J to Logback.
- Do not add constructors or lifecycle methods only to log trivial object creation,
  such as `log.debug("Creating ...")`; those logs add noise without explaining
  behavior.
- Avoid generating `toString`, `equals`, or `hashCode` for configuration objects
  that may later hold credential metadata or provider details unless a current test
  requires that behavior.
- Do not use Lombok to introduce hidden architecture, builders, constructors, or
  inheritance patterns before a focused task needs them.
- Use Lombok `@SneakyThrows` to remove checked-exception boilerplate when a catch
  block only wraps or rethrows a checked exception. Keep explicit catches when they
  add domain context, recovery, cleanup, or intentionally translate runtime errors.
- Do not use experimental Lombok APIs such as `lombok.experimental.Accessors`.
  Prefer standard Lombok annotations and JavaBean-style accessors such as
  `getText()` or `isAuto()` when records are not a fit.
- When adding Lombok to the Maven build on Java 25 or newer, configure Lombok as an
  explicit annotation processor instead of relying on implicit processor discovery.

## Configuration Properties

- Use `@ConfigurationProperties` for focused Spring configuration classes when a
  Spring-only config type needs binding, but do not annotate `CodegeistConfig`
  with it. Codegeist root config is parsed by `CodegeistConfigService` through
  injected `CodegeistConfigRootElement` parser components.
- For Codegeist-owned Spring application settings such as `codegeist.session.*`,
  keep built-in defaults in `CodegeistSpringAppProperties` instead of repo
  `application.yaml`; use external Spring application properties or environment
  variables as overrides.
- For Codegeist config, keep `CodegeistConfig.CONFIGURATION_PREFIX` defined from
  `CodegeistApplication.APP_NAME` so the app name stays the source of truth.
- Keep configuration classes small and shaped by current binding tests.
- Use `kebab-case` in YAML and `camelCase` in Java fields.
- Use maps for provider ids, model ids, and open-ended options until a focused
  provider task needs stronger typed properties.

## Provider Config And Chat Models

- Keep `ProviderConfig` access-only. Do not add stored YAML model fields,
  generation options, enablement flags, or completions-path routing to provider
  config unless a focused runtime task explicitly changes that contract.
- Let each concrete `ProviderConfig` implement `defaultModel()` for the
  provider-owned runtime fallback, but do not add stored YAML model fields or
  command-owned hardcoded model defaults.
- Put optional first-provider selection in `CodegeistConfig.defaultProvider()`.
  Callers that require a provider should choose the failure behavior at their
  boundary instead of duplicating provider-map iteration in command classes.
- Keep selected provider config separate from runtime request data. Pass the
  validated `ProviderConfig` to `CodegeistChatService`, and keep
  `CodegeistChatRequest` focused on runtime model and prompt.
- Let each concrete `ProviderConfig` implement `createChatModel()` and return the
  matching `CodegeistChatModel<T extends ProviderConfig>`. Do not reintroduce
  factory or strategy layers unless a focused task intentionally replaces this
  contract.

## String Constants

- Prefer named `static final String` constants over inline string literals in Java
  code.
- Use constants for contract-bearing strings, especially application names,
  configuration prefixes, property keys, CLI command names, environment variable
  names, file names, path segments, provider ids, and test selectors.
- Use class-owned constants for exception messages and exception message prefixes
  instead of inline string literals in `throw` statements.
- Keep constants close to the class that owns the concept.
- For shared application-wide values, prefer `CodegeistApplication` as the owner.
- Annotation values must reference compile-time constants, for example
  `CodegeistConfig.CONFIGURATION_PREFIX`.
- Use named constants for Jackson contract strings such as `@JsonProperty(...)`
  names and `@JsonTypeInfo.property` values, especially when the same persisted
  field name appears on fields and constructor parameters. Keep the constant on
  the class that owns the persisted field.
- When tests assert class-owned error messages or prefixes, reuse the owning
  constant, for example `CodegeistConfigService.VALIDATION_ERROR_PREFIX`, instead
  of duplicating message fragments such as `Invalid Codegeist config file`.
- Do not duplicate the same string literal across classes or tests when it
  represents one shared contract.

## Imports And Type Names

- Do not use fully qualified Java type names in code when a normal import works.
  Prefer `new LinkedHashMap<>()` with `import java.util.LinkedHashMap;` over
  `new java.util.LinkedHashMap<>()`.
- Use fully qualified type names only when Java syntax or framework contracts make
  them necessary, such as avoiding an unavoidable name collision or inside strings
  that require a fully qualified class reference.
- If an avoidable fully qualified type appears while changing nearby code, replace
  it with an import in the same task.

## Framework Utilities

- Prefer existing utility methods from frameworks already present in the project
  over hand-written helper logic. For example, use Spring `StringUtils.hasText(...)`
  for null-or-blank text checks instead of `value == null || value.isBlank()`.
- Use the smallest fitting existing utility, such as Spring `CollectionUtils`,
  `ObjectUtils`, or `StringUtils`, before adding custom helper methods or local
  normalization loops.
- Do not add a new utility dependency only to replace a trivial check; this rule
  applies to utilities already available through the current framework stack.

## Exception Handling

- Do not add null branches only to choose between exception constructor overloads
  when the overload already accepts `null` with the intended behavior. Prefer one
  direct call such as `new DomainException(MESSAGE, cause)` instead of branching to
  `new DomainException(MESSAGE)` when `cause == null`.

## Runtime Environment Boundaries

- Do not branch application, configuration, provider, domain, or command logic on
  whether Codegeist is running on the JVM or as a GraalVM native image.
- Do not use runtime checks such as
  `System.getProperty("org.graalvm.nativeimage.imagecode")` in Codegeist app code
  to select different behavior.
- Solve native-image compatibility with uniform Java contracts, Spring/GraalVM
  metadata under `META-INF/native-image/`, build configuration, tests, or a
  dedicated adapter boundary when a real platform-specific concern exists.
- Keep platform-specific decisions in packaging, smoke scripts, release workflows,
  or clearly named adapter layers, not inside provider dispatch or config parsing.

## GraalVM Reflection And Discovery

- Prefer explicit discovery mechanisms that GraalVM can see at build time, such as
  Java registries, Spring configuration, or generated registries, over runtime
  classpath scanning.
- For Codegeist provider config dispatch, use the explicit provider class registry
  in `ProvidersRootElement` unless a future task intentionally replaces it with a
  generated registry. Do not add ServiceLoader back for this path.
- When adding reflective config or domain types that Jackson or another framework
  must instantiate in native images, register the type in
  `src/main/resources/META-INF/native-image/reflect-config.json` or a centralized
  generated equivalent, then prove it with `task native-smoke`.
- Avoid broad `.class` resource includes only to make runtime scanners work. Add
  resource patterns only when the resource itself is part of the runtime contract,
  such as `logback.xml` or `META-INF/build-info.properties`.

## Helper Methods

- Do not add dead code. New constructors, methods, setters, fields, overloads,
  helpers, annotations, or constants must have a current production call site,
  framework binding contract, serialization contract, or test-visible behavior.
  Remove speculative convenience APIs instead of keeping them for possible future
  use.
- When a Jackson-bound type intentionally uses a no-argument constructor plus
  mutable properties, do not keep a duplicate property constructor only for
  convenience. Use setters at the current call site unless a required production
  or framework contract needs constructor binding.
- Do not add one-line pass-through helper methods that only call another helper with
  a constant or parameter, such as `apiKey()` returning `requireEnv(API_KEY_ENV)`.
  Prefer the direct call at the use site when it stays readable.
- Do not add public or private pass-through wrappers that only delegate to another
  method with the same inputs, such as `load(path)` returning `read(path)`. Put the
  real implementation on the method callers should use, unless the wrapper adds a
  distinct contract such as interface adaptation, error translation, access control,
  or framework binding.
- If two methods have the same signature and behavior and differ only by name, keep
  the one that callers should use and remove the other instead of adding a wrapper.
- Add a helper only when it centralizes non-trivial behavior, improves repeated
  call sites, or names a real domain operation that would otherwise be unclear.
- Do not add public production methods only to support tests. When tests need to
  set internal state, prefer package-private fields or methods in the same package,
  or a focused test utility that can set private fields, instead of expanding the
  runtime API.

## Comments For Coding Agents

- Keep source comments sparse and useful for later coding agents.
- Comment why, invariants, cross-file contracts, framework behavior, or sharp
  edges that are not obvious from names, annotations, and tests.
- Include nearby file or documentation references when they help recover context,
  such as related tests, smoke scripts, or focused architecture docs.
- Do not narrate obvious Java, Spring, Lombok, or shell syntax.
- Update or delete comments when the referenced behavior, file path, or contract
  changes.

## Tests And Documentation

- Add or update focused tests when Java wiring, configuration binding, validation,
  or Lombok-generated accessors affect behavior.
- When a Spring test must prove YAML-backed configuration loading, prefer a
  profile-specific test resource such as `application-<profile>.yml` plus
  `@ActiveProfiles("<profile>")` so the fixture is isolated to that test.
- Use inline `@SpringBootTest(properties = ...)` only when the test does not need
  to prove YAML file loading.
- Update `docs/developer/architecture/architecture.md` when Java packages,
  classes, configuration behavior, or tests change.
