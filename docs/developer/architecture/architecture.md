# Codegeist Architecture

Current-state architecture overview for coding agents and contributors.

## Scope

This document describes what exists in the repository now. It is not an
implementation backlog and must not be used as a source-generation checklist.

For future direction, use only the compact, current specification set under
`docs/developer/specification/`:

- `codegeist-opencode-parity.md` - behavior reference and OpenCode parity posture.
- `java-generation-guidance.md` - iterative Java/Spring implementation rules.
- `testing-strategy-and-agent-rules.md` - test-first workflow and timing rules.
- `runtime-vocabulary.md` - vocabulary only, not package or class requirements.
- `build-release-and-binary-smoke-strategy.md` and `native-packaging-posture.md` -
  packaging strategy for later implemented workflows.

## Current System State

Codegeist currently contains one Java/Spring Boot CLI application under
`app/codegeist/cli`. Implemented runtime behavior is Spring Boot application
startup plus a Spring Shell `--version` command that prints the build version.

The previous source-generation contracts and T004 implementation epic were removed
because they encouraged placeholder classes. Future implementation should start
from focused tests and add only the source needed by the current behavior.

## Build Baseline

The current application build is defined by `app/codegeist/cli/pom.xml`.

| Area | Current state |
| --- | --- |
| Module shape | Single Maven module under `app/codegeist/cli` |
| Group/artifact | `ai.codegeist:codegeist` |
| Java | `25` through `java.version` and `maven.compiler.release` |
| Spring Boot | Parent `spring-boot-starter-parent` `4.0.6` |
| Spring Shell | BOM `4.0.2`, dependency `spring-shell-starter` |
| Spring AI | BOM `2.0.0-M6` imported for dependency management |
| Spring AI Agent Utils | BOM and core artifact `0.7.0` |
| GraalVM | Native Maven profile using `native-maven-plugin` `0.10.6` |
| Packaging | Spring Boot executable jar named `target/codegeist.jar` |
| Tests | Spring Boot context-load test, Spring-context command test, focused version output tests, and native version smoke |

Spring AI provider starters are not present. Spring AI Agent Utils is present as a
dependency baseline, but no Agent Utils runtime utility is wired into the app yet.

## Implemented File Layout

```text
app/codegeist/cli/
  pom.xml
  Taskfile.yml
  src/main/java/ai/codegeist/app/CodegeistApplication.java
  src/main/java/ai/codegeist/app/VersionCommands.java
  src/main/resources/META-INF/native-image/resource-config.json
  src/main/resources/application.yaml
  src/main/resources/logback.xml
  src/test/java/ai/codegeist/app/CodegeistApplicationTests.java
  src/test/java/ai/codegeist/app/VersionCommandsTests.java
```

Implemented Java package:

| Package | Current responsibility |
| --- | --- |
| `ai.codegeist.app` | Spring Boot application entrypoint and version command |

No other `ai.codegeist.*` application packages currently exist in source code.

## Application Entrypoint

`CodegeistApplication` is annotated with `@SpringBootApplication` and delegates
startup to `SpringApplication.run`. GraalVM resource inclusion is kept out of
Java code in `src/main/resources/META-INF/native-image/resource-config.json`,
which includes `logback.xml` and `META-INF/build-info.properties` for the native
binary.

```mermaid
flowchart TD
    Main[CodegeistApplication.main]
    Boot[SpringApplication.run]
    Context[Spring application context]

    Main --> Boot --> Context
```

## Runtime Components

Current behavior:

- `task run` builds the jar and runs `java -jar target/codegeist.jar`.
- The app starts a Spring Boot context using `application.yaml`.
- `application.yaml` sets `spring.application.name` to `codegeist`, disables the
  Spring banner, and sets `spring.shell.interactive.enabled=false` so command
  arguments such as `--version` run through Spring Shell's noninteractive runner.
- `--version` is implemented as a Spring Shell command in `VersionCommands`. It
  uses Spring Boot's `BuildProperties` bean, backed by the generated
  `META-INF/build-info.properties`, and writes through Spring Shell's
  `CommandContext.outputWriter()` so output is only the version string, for
  example `0.1.0-SNAPSHOT`.
- `logback.xml` writes logs only to `${LOG_FILE:-logs/codegeist.log}`. Console
  output is reserved for command output, so plain `./target/codegeist --version`
  prints only the version.
- There are no implemented prompt workflows, model calls, shell commands beyond
  `--version`, runtime services, provider adapters, tool executions, permission
  prompts, workspace policies, storage adapters, server endpoints, Vaadin views,
  PF4J plugins, or JBang execution paths.

## Test Architecture

`CodegeistApplicationTests` is a Spring Boot context-load test. It excludes
Spring Shell auto-configuration so bootstrap can be verified without starting an
interactive runner.

`VersionCommandsTests` starts the Spring context with
`VersionCommands.VERSION_COMMAND` as an argument and verifies that stdout equals
the generated build version while stderr stays empty.

```mermaid
sequenceDiagram
    participant Test as CodegeistApplicationTests
    participant Spring as Spring Boot TestContext
    participant App as CodegeistApplication
    participant VersionTest as VersionCommandsTests
    participant Version as VersionCommands

    Test->>Spring: Load context without Spring Shell auto-configuration
    Spring->>App: Start application context
    App-->>Spring: Context starts or fails
    Spring-->>Test: contextLoads passes on successful startup
    VersionTest->>Spring: Start context with --version argument
    Spring->>Version: Execute Spring Shell command
    Version-->>VersionTest: stdout equals build-info version
```

## Taskfile Verification Flow

`app/codegeist/cli/Taskfile.yml` provides the current developer entrypoints.
`scripts/native-smoke.sh` defines `run-native-smoke-tests`, which owns the
native command smoke assertions used by `task native-smoke`. The function deletes
and recreates `target/smoke-test` for each run, then writes the smoke log to
`target/smoke-test/codegeist.log`.

| Task | Command | Proves |
| --- | --- | --- |
| `task test` | `mvn --batch-mode --no-transfer-progress test` | Maven test lifecycle, Spring context-load test, and version output test |
| `task build` | `mvn --batch-mode --no-transfer-progress -DskipTests clean package` | Executable jar packaging |
| `task native` | `mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile` | GraalVM command-mode native posture when practical |
| `task native-smoke` | Builds native, then sources `scripts/native-smoke.sh` and calls `run-native-smoke-tests` | Native command console output equals generated build version and native smoke log file works |
| `task run` | `java -jar target/codegeist.jar` after `build` | Starts the packaged Spring Boot application |

## Not Implemented Yet

The following concepts are discussed in strategy docs but are not implemented in
Java source:

- Prompt workflows.
- Spring AI Ollama provider calls.
- Runtime orchestration.
- Session or event models.
- Context loading.
- Tool registry or tool execution.
- Permission approval flow.
- Workspace and file-access policy.
- Patch/edit proposal flow.
- Controlled shell execution.
- Storage ports or adapters.
- CLI/Spring Shell commands beyond `--version`.
- Headless server endpoints.
- Vaadin client.
- PF4J plugin loading.
- JBang extension execution.

When implementing any of these concepts, update this document in the same task so
future coding agents can distinguish current code from future direction.
