# Spring Shell Command Model

Source-backed summary of command registration, parsing, execution, and testing
surfaces in the analyzed Spring Shell checkout.

## Why This Matters

Codegeist uses Spring Shell for command boundaries. This feature note points
future `/ask-project` follow-up work at the smallest relevant upstream source
set instead of requiring a broad scan.

## Command Definition Paths

Spring Shell documents two command definition models in
`source/spring-shell-docs/modules/ROOT/pages/commands/registration.adoc`:

- Annotation model: methods annotated with `@Command` on scanned Spring beans.
- Programmatic model: beans of type `Command`, often built through
  `AbstractCommand` or builder APIs.

In Spring Boot applications, command enablement for annotation commands is handled
by auto-configuration. Non-Boot use can opt in with `@EnableCommand`.

## Registry And Discovery

`source/spring-shell-core/src/main/java/org/springframework/shell/core/command/CommandRegistry.java`
is the in-memory command catalog. It stores `Command` values, hides hidden
commands from lookups, resolves by name or alias, supports prefix lookup, and can
register or unregister commands at runtime.

`source/spring-shell-core-autoconfigure/src/main/java/org/springframework/shell/core/autoconfigure/CommandRegistryAutoConfiguration.java`
creates the default registry, registers programmatic command beans, scans
non-Spring-Boot beans for annotated command methods, and registers the quit
command.

Known sharp edge: `CommandRegistry.registerCommand` contains a source TODO for
alias conflict checks.

## Parsing And Execution

`source/spring-shell-core/src/main/java/org/springframework/shell/core/command/CommandParser.java`
defines the parser contract: raw input becomes `ParsedInput`, where command names
can be multiple words followed by arguments and options.

The runner split is important:

- `InteractiveShellRunner` reads from `InputProvider`, loops until `quit`,
  `exit`, null input, interruption, or read failure, and prints user-facing parse
  and execution errors.
- `NonInteractiveShellRunner` executes process args as one command, or executes
  an `@script` file line by line. In non-OK exit paths it logs and can throw
  `CommandExecutionException`.

Spring Boot autoconfiguration contributes an `ApplicationRunner` that delegates
process args to the selected `ShellRunner`.

## Output And Input Contract

Commands operate through `CommandContext`, which carries parsed input, command
registry, output writer, and input reader. Docs and tests show commands reading
plain text and passwords through the input reader and writing user-visible output
through the command context writer.

## Testing Surface

`source/spring-shell-test/src/main/java/org/springframework/shell/test/ShellTestClient.java`
executes commands through the same parser, registry, and executor path used by
runtime command dispatch. It captures output into a `ShellScreen`.

`source/spring-shell-docs/modules/ROOT/pages/testing.adoc` documents three useful
testing forms:

- `ShellTestClient`, `ShellScreen`, and `ShellAssertions` for command-level tests.
- `ShellInputProvider` for simulated input and password prompts.
- `@SpringBootTest` plus `OutputCaptureExtension` for end-to-end command output.

## Follow-Up Pointers

- Ask for the exact `ApplicationRunner -> ShellRunner -> CommandParser -> CommandExecutor` path.
- Ask how `@Command` methods are converted into `Command` objects by
  `CommandFactoryBean`.
- Ask how availability, validation, completion, built-in commands, and exception
  mapping fit into this command model.
