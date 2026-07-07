# Spring Shell Analysis Report

Static third-party analysis for Codegeist follow-up questions about Spring Shell
commands, Spring Shell testing, and the Spring Shell TerminalUI/TUI stack.

Last local refresh: 2026-07-01 against source commit
`fa298743fcbf7a02d9ca7a8c68d62f2005744094`.

## Executive Summary

Spring Shell is a Spring-powered CLI framework for production-grade Java command
applications. The checked-out `main` branch currently identifies itself as
`4.0.4-SNAPSHOT` and targets Java 17 with Spring Framework `7.0.8`, Spring Boot
`4.0.7`, JLine `3.30.13`, Reactor `3.8.6`, Hibernate Validator `9.0.1.Final`,
JUnit Jupiter `6.1.0`, and Maven as the build system.

For Codegeist, the highest-value areas are the command model, non-interactive
runner behavior, Spring Boot test support, JLine integration, and TerminalUI.
These areas are relevant because Codegeist already uses Spring Shell and has a
TerminalUI-based TUI path. Treat this analysis as behavior and source evidence,
not a migration recommendation.

## Source And Module Shape

The root Maven reactor declares these modules in `source/pom.xml`:

- `spring-shell-core` - command model, parsing, execution, input/output
  contracts, shell runners, and utilities.
- `spring-shell-jline` - JLine prompt/input integration, completion/highlighting,
  and TerminalUI/TUI implementation.
- `spring-shell-test` - programmatic shell test client, screen assertions, and
  input providers.
- `spring-shell-core-autoconfigure` - Spring Boot autoconfiguration for command
  registry, shell runners, TerminalUI builder, components, and related beans.
- `spring-shell-test-autoconfigure` - Spring Boot test autoconfiguration for
  `@ShellTest`.
- `spring-shell-docs` - Antora documentation and doc snippet tests.
- `spring-shell-dependencies` - dependency management/BOM support.
- `spring-shell-samples` - hello world, non-interactive, Spring Boot, secure
  input, and Petclinic sample applications.
- `spring-shell-starters` - starters for core, test, JLine/Jansi/JNA/JNI/FFM
  integration surfaces.

The checkout contains 387 Java files, 78 `*Tests.java` files, one
`*IntegrationTests.java` file, 72 AsciiDoc files, six Markdown files, and 21
`pom.xml` files.

## Graphify Findings

The local `/analyse-project` run generated a Graphify cache under
`docs/third-party/spring-shell/graphify-out/` from a temporary filtered corpus of
source-code and documentation files.

- Corpus: 535 supported files, roughly 167,249 words.
- Graph: 5,217 nodes, 14,299 edges, 263 communities, 25 hyperedges.
- Extraction mix: 82% `EXTRACTED`, 18% `INFERRED`, 0% `AMBIGUOUS`.
- Strong hubs: `View`, `CommandRegistry`, `BoxView`, `Screen`,
  `CommandContext`, `AbstractCommand`, `BaseBuilder`, `ThemeResolver`,
  `ComponentContext`, and `TerminalUI`.
- Useful communities include command registry/parser, command context/exit
  status, TerminalUI builder/view component executor, event loop/view component,
  default event loop, Shell test client, Shell runner autoconfiguration, theme
  resolver, path/string/number inputs, selectors, list/grid/menu/status views,
  and sample applications.

The Graphify run used a temporary Markdown mirror of AsciiDoc files because the
installed detector did not classify `.adoc` files directly. The generated graph
remains a navigation aid; source files and docs should still be inspected before
making implementation decisions.

## Repomix Packed Source

The local refresh regenerated
`docs/third-party/spring-shell/repomix-output.xml` from the source checkout.

- Packed files: 549.
- Output size: 1,729,410 bytes.
- Security scan: Repomix reported no suspicious files.
- Excluded by command: `.git`, Maven `target/`, common build output,
  dependency directories, archives, binary/image files, local environment files,
  and secret-like key/certificate patterns.
- A broad follow-up keyword scan found only public placeholder references such
  as GitHub Actions `${{ secrets.* }}` variables and sample password text, not
  credential values.

## Command Runtime Findings

Spring Shell 4 has two command definition paths documented in
`spring-shell-docs/modules/ROOT/pages/commands/registration.adoc` and backed by
source:

- Annotation-based commands use `@Command` methods on scanned Spring beans.
  Spring Boot auto-configuration handles command enablement for Boot apps.
- Programmatic commands are beans of type `Command` and may use
  `AbstractCommand` or the command builder APIs.
- `CommandRegistry` stores commands, filters hidden commands from lookup,
  resolves command names and aliases, supports prefix lookup, and can register,
  unregister, or clear commands at runtime.
- `CommandRegistryAutoConfiguration` creates a `CommandRegistry`, registers
  programmatic `Command` beans, reflects over non-Boot beans for annotated
  command methods, and registers the quit command.
- `CommandParser` parses raw input into `ParsedInput`, where a command can be
  a word sequence such as `command subcommand1 subcommand2` followed by
  arguments and options.
- `InteractiveShellRunner` reads commands from an `InputProvider`, parses,
  executes through `CommandExecutor`, prints user-facing parse/execution errors,
  and exits on `quit`, `exit`, null input, or interruption.
- `NonInteractiveShellRunner` executes a single command from process args or a
  script file referenced with `@file`, and throws `CommandExecutionException` on
  non-OK command exit status in command and script paths.

One source-level sharp edge is visible in `CommandRegistry`: a TODO notes alias
conflict checks are not implemented when registering commands.

## TerminalUI Findings

TerminalUI is implemented in `spring-shell-jline` and documented under
`spring-shell-docs/modules/ROOT/pages/tui/`.

- `TerminalUI` orchestrates a JLine `Terminal`, `BindingReader`, key map,
  virtual display, root view, optional modal view, focus, theme resolver, and
  `DefaultEventLoop`.
- `TerminalUIBuilder` is the recommended construction path. Spring Boot
  auto-configuration contributes it as a prototype bean when TerminalUI is on
  the classpath.
- `TerminalUI.configure(View)` initializes a view and injects the event loop,
  theme resolver, theme name, and view service.
- `run()` is blocking. It binds keys, enters terminal raw mode, optionally enters
  full-screen alternate screen mode, registers event subscriptions, renders,
  reads key/mouse bindings, and restores terminal state in `finally`.
- Root view rendering is layered with one optional modal view. A modal view sits
  above root content and receives mouse input in preference to the root.
- Key input is offered to root hotkeys first, then to the focused root view.
  Mouse input is routed to the modal view when present, otherwise the root view.
- `DefaultEventLoop` uses Reactor sinks and processors to expose typed key,
  mouse, system, signal, and view event streams.

For Codegeist TUI work, the most relevant source areas are the event/focus
contract, blocking loop cleanup, view wrappers, theme injection, and Spring Boot
prototype construction path.

## Testing Findings

Spring Shell provides both framework-owned test utilities and normal Spring Boot
application tests:

- `ShellTestClient` parses a command, creates `CommandContext`, writes output to
  a captured `StringWriter`, and returns a `ShellScreen`.
- `ShellInputProvider` supports simulated clear-text input and password input.
- `ShellAssertions` and `ShellScreen` support output assertions.
- `@ShellTest` from test autoconfiguration sets up Spring Shell command testing
  for Boot applications.
- The docs also show `@SpringBootTest` with
  `spring.shell.interactive.enabled=false` and `OutputCaptureExtension` for
  end-to-end command output checks.

These test utilities are relevant to Codegeist because they prove command
behavior through the Spring Shell boundary instead of direct command class
instantiation.

## Dependency And Build Findings

The root POM configures Java 17 compilation, Spring Java Format validation,
Error Prone, NullAway, Maven Surefire, Maven Failsafe, Javadocs, Spring Boot
autoconfigure metadata processing, and Antora documentation generation. The
README documents `./mvnw install` for local build/test and `./mvnw antora -pl
spring-shell-docs` for documentation site generation.

The samples are first-class source evidence for framework behavior:

- Hello world and Spring Boot samples demonstrate basic command registration.
- The non-interactive sample demonstrates argument-dispatched command execution.
- The secure-input sample demonstrates `InputReader.readPassword` and command
  availability based on authentication state.
- Petclinic demonstrates a larger command surface.

## Runtime Evidence

No Spring Shell commands, Maven builds, sample applications, or upstream tests
were run during this refresh. The current conclusions are based on source files,
docs, Repomix, and Graphify only.

## Risks And Sharp Edges

- Graphify HTML required raising `GRAPHIFY_VIZ_NODE_LIMIT` from 5,000 to 6,000
  for this graph size.
- Semantic Graphify extraction was chunked through coding-agent subagents, so
  token accounting in `GRAPH_REPORT.md` is recorded as zero.
- `.adoc` files were mirrored to temporary `.md` files for Graphify detection;
  check original AsciiDoc source before quoting docs.
- The source branch targets Spring Boot 4 and Spring Framework 7. Confirm version
  compatibility before using upstream internals as direct dependencies.
- TerminalUI owns terminal raw-mode and screen cleanup behavior. Any Codegeist
  TUI change should preserve cleanup and blocking-loop failure handling.
- Do not infer runtime behavior beyond source and docs until a focused follow-up
  runs upstream tests or sample applications.

## Recommended Follow-Up Questions

- `/ask-project spring-shell "Trace the non-interactive command execution path from ApplicationRunner to CommandExecutor."`
- `/ask-project spring-shell "Explain TerminalUI focus, modal, key, and mouse routing with source citations."`
- `/ask-project spring-shell "Which Spring Shell test utilities should Codegeist use for command and secure input tests?"`
- `/ask-project spring-shell "What source files define the command registration contract and its extension points?"`
