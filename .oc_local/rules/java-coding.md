# Codegeist Java Coding Rule

Use this rule when adding or changing Java source in Codegeist.

## Spring Wiring

- Prefer annotation-based Spring wiring and metadata.
- For Spring-managed dependencies, use field injection with `@Autowired` on the
  field.
- Do not use constructor injection for Spring beans.
- Do not use Lombok constructor annotations such as `@RequiredArgsConstructor` or
  `@AllArgsConstructor` to inject Spring dependencies.
- Use constructors only for non-Spring value objects, records, test fixtures, or
  local helper types where Spring dependency injection is not involved.
- Prefer Spring annotations such as `@Component`, `@ConfigurationProperties`,
  `@EnableConfigurationProperties`, `@Autowired`, and test annotations when they
  make ownership and runtime behavior explicit.
- For the primary config bean, inject unqualified `CodegeistConfig` and let
  Spring `@Primary` select it. Only `CodegeistConfigService` should qualify
  `CodegeistConfig.SPRING_BOUND_CONFIG_BEAN` to access the Spring-bound source.
- Prefer Bean Validation annotations on configuration POJOs for config validation.
  When config is loaded directly with Jackson rather than Spring binding, call
  `jakarta.validation.Validator` explicitly after mapping.

## Lombok

- Use Lombok to reduce simple Java boilerplate when it keeps the source easier to
  read.
- Prefer focused Lombok annotations such as `@Getter` and `@Setter` over broad
  annotations such as `@Data`.
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
- When adding Lombok to the Maven build on Java 25 or newer, configure Lombok as an
  explicit annotation processor instead of relying on implicit processor discovery.

## Configuration Properties

- Use `@ConfigurationProperties` for Spring-bound configuration classes.
- For Codegeist config, use `CodegeistConfig.CONFIGURATION_PREFIX` in
  `@ConfigurationProperties`; define that constant from
  `CodegeistApplication.APP_NAME` so the app name stays the source of truth.
- Keep configuration classes small and shaped by current binding tests.
- Use `kebab-case` in YAML and `camelCase` in Java fields.
- Use maps for provider ids, model ids, and open-ended options until a focused
  provider task needs stronger typed properties.

## String Constants

- Prefer named `static final String` constants over inline string literals in Java
  code.
- Use constants for contract-bearing strings, especially application names,
  configuration prefixes, property keys, CLI command names, environment variable
  names, file names, path segments, provider ids, and test selectors.
- Keep constants close to the class that owns the concept.
- For shared application-wide values, prefer `CodegeistApplication` as the owner.
- Annotation values must reference compile-time constants, for example
  `CodegeistConfig.CONFIGURATION_PREFIX`.
- When tests assert class-owned error messages or prefixes, reuse the owning
  constant, for example `CodegeistConfigService.VALIDATION_ERROR_PREFIX`, instead
  of duplicating message fragments such as `Invalid Codegeist config file`.
- Do not duplicate the same string literal across classes or tests when it
  represents one shared contract.

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
