# Spring Shell User-Facing Getting Started Notes

Current user-facing behavior summarized from Spring Shell README and docs in the
analyzed checkout.

## What Spring Shell Provides

Spring Shell helps create Spring-powered Java CLI applications. The README frames
it around fast shell development, Spring Initializr setup, executable `java -jar`
applications, and possible GraalVM native packaging.

## Minimal Command Example

The README shows a Spring Boot application with a method annotated by `@Command`:

```java
@Command
public String hi() {
    return "hi";
}
```

After packaging, running the jar starts an interactive shell where built-in
commands such as `help`, `clear`, `exit`, `version`, and `script` appear beside
application commands.

## Samples

The source checkout includes these documented sample applications:

- `spring-shell-sample-hello-world` - simple greeting shell.
- `spring-shell-sample-spring-boot` - Spring Boot shell application.
- `spring-shell-sample-non-interactive` - command execution without staying in a
  shell loop.
- `spring-shell-sample-secure-input` - secure input and password prompt sample.
- `spring-shell-sample-petclinic` - larger Petclinic-style command surface.

`run-sample.sh` provides an interactive menu for launching samples from the
repository root.

## Testing Guidance For Users

The docs describe two testing approaches:

- Spring Shell testing utilities such as `@ShellTest`, `ShellTestClient`,
  `ShellScreen`, `ShellAssertions`, and `ShellInputProvider`.
- Spring Boot end-to-end tests using `@SpringBootTest`, process args, and
  `OutputCaptureExtension` with `spring.shell.interactive.enabled=false`.

## Runtime Evidence Gap

This analysis did not run the samples. Treat sample behavior as documented source
behavior until a follow-up task executes the sample applications.
