# Spring Shell Developer Source Map

High-value source paths for follow-up `/ask-project spring-shell "..."`
questions.

## Command Core

- `source/spring-shell-core/src/main/java/org/springframework/shell/core/command/Command.java` - command contract and builder surface.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/command/AbstractCommand.java` - base class for programmatic commands.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/command/CommandContext.java` - per-command parser, registry, output writer, and input reader context.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/command/CommandExecutor.java` - command invocation path.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/command/CommandParser.java` - parser interface.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/command/DefaultCommandParser.java` - default command parser implementation.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/command/CommandRegistry.java` - command catalog, lookup, prefix lookup, and dynamic registration.

## Runners And Input

- `source/spring-shell-core/src/main/java/org/springframework/shell/core/ShellRunner.java` - runner SPI.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/InteractiveShellRunner.java` - interactive loop and user-facing error handling.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/NonInteractiveShellRunner.java` - command args and `@script` execution path.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/InputProvider.java` - input source contract.
- `source/spring-shell-core/src/main/java/org/springframework/shell/core/InputReader.java` - command input and password reader contract.

## Spring Boot Autoconfiguration

- `source/spring-shell-core-autoconfigure/src/main/java/org/springframework/shell/core/autoconfigure/CommandRegistryAutoConfiguration.java` - default command registry population.
- `source/spring-shell-core-autoconfigure/src/main/java/org/springframework/shell/core/autoconfigure/ShellRunnerAutoConfiguration.java` - `ApplicationRunner`, interactive runner, non-interactive runner, parser, and console input defaults.
- `source/spring-shell-core-autoconfigure/src/main/java/org/springframework/shell/core/autoconfigure/TerminalUIAutoConfiguration.java` - TerminalUI builder and component builder beans.
- `source/spring-shell-core-autoconfigure/src/main/java/org/springframework/shell/core/autoconfigure/StandardCommandsAutoConfiguration.java` - built-in command registration surface.

## JLine And TerminalUI

- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/DefaultJLineShellConfiguration.java` - JLine shell configuration.
- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/JLineInputProvider.java` - JLine input provider.
- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/CommandCompleter.java` - completion integration.
- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/CommandHighlighter.java` - command highlighting.
- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/tui/component/view/TerminalUI.java` - blocking TUI loop, render, key/mouse dispatch, focus, modal, cleanup.
- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/tui/component/view/TerminalUIBuilder.java` - TUI builder.
- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/tui/component/view/event/DefaultEventLoop.java` - Reactor-backed event loop.
- `source/spring-shell-jline/src/main/java/org/springframework/shell/jline/tui/component/view/control/` - view/control abstractions.

## Testing

- `source/spring-shell-test/src/main/java/org/springframework/shell/test/ShellTestClient.java` - programmatic command execution client.
- `source/spring-shell-test/src/main/java/org/springframework/shell/test/ShellInputProvider.java` - simulated user input and passwords.
- `source/spring-shell-test/src/main/java/org/springframework/shell/test/ShellScreen.java` - captured screen model.
- `source/spring-shell-test/src/main/java/org/springframework/shell/test/ShellAssertions.java` - assertion entrypoint.
- `source/spring-shell-test-autoconfigure/src/main/java/` - `@ShellTest` autoconfiguration support.

## Docs And Samples

- `source/README.adoc` - project overview, getting started, samples, build docs.
- `source/spring-shell-docs/modules/ROOT/pages/commands/` - command docs.
- `source/spring-shell-docs/modules/ROOT/pages/tui/` - TUI docs.
- `source/spring-shell-docs/modules/ROOT/pages/components/` - component docs.
- `source/spring-shell-docs/modules/ROOT/pages/testing.adoc` - testing docs.
- `source/spring-shell-samples/` - sample applications.

## Graph Navigation Hints

Graphify hubs worth asking about first:

- `View`
- `CommandRegistry`
- `CommandContext`
- `AbstractCommand`
- `TerminalUI`
- `ThemeResolver`
- `DefaultEventLoop`
- `ShellTestClient`

Use `docs/third-party/spring-shell/repomix-output.xml` for broad source dives
and `graphify-out/GRAPH_REPORT.md` plus `graphify-out/graph.json` for graph
navigation.
