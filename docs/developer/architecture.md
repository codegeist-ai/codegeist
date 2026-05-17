# Codegeist Architecture

Current-state architecture overview for coding agents and contributors.

## Scope

This document describes the system that exists in this repository now. It is not
the target architecture, the OpenCode parity plan, or an implementation backlog.
Use it as the compact current-state map before changing Codegeist source,
configuration, build, or verification behavior.

For target architecture and future design decisions, see
`docs/developer/codegeist-opencode-parity.md`. For the Codegeist-owned runtime
vocabulary and boundary diagram, see `docs/developer/runtime-vocabulary.md`. For
the future runtime/session/event contract blueprint and diagrams, see
`docs/developer/runtime-session-event-contracts.md`. For the future
context-loading and workspace-manifest blueprint, see
`docs/developer/context-workspace-manifest.md`. For the future provider
configuration and Spring AI adapter blueprint, see
`docs/developer/provider-configuration-contracts.md`. For the future tool,
permission, and workspace execution blueprint, see
`docs/developer/tool-permission-workspace-contracts.md`. For the future patch/edit
proposal blueprint, see `docs/developer/patch-edit-proposal-contracts.md`. For the
future controlled shell verification blueprint, see
`docs/developer/shell-verification-contracts.md`. For the future storage port
posture blueprint, see `docs/developer/storage-port-posture.md`.

## Current System State

Codegeist currently contains one Java/Spring Boot CLI application under
`app/codegeist/cli`. The implemented runtime behavior is limited to Spring Boot
application startup. Spring Shell is present as a dependency and configuration
surface, but no shell commands are implemented yet.

The repository also contains planning documentation for a broader coding-agent
runtime. Those future runtime, session, event, provider, tool, permission,
workspace, storage, extension, server, and UI concepts are not implemented in the
application code yet. Their current vocabulary and ownership boundaries are
documented in `docs/developer/runtime-vocabulary.md`; the future
runtime/session/event contract shape is described in
`docs/developer/runtime-session-event-contracts.md`; and the future
context/workspace manifest shape is described in
`docs/developer/context-workspace-manifest.md`. Future provider configuration,
validation, and Spring AI adapter boundaries are described in
`docs/developer/provider-configuration-contracts.md`; future tool descriptors,
permission decisions, workspace validation, and bounded tool results are described
in `docs/developer/tool-permission-workspace-contracts.md`; future patch/edit
proposal contracts are described in
`docs/developer/patch-edit-proposal-contracts.md`; and future controlled shell
verification contracts are described in
`docs/developer/shell-verification-contracts.md`; and future storage port posture
is described in `docs/developer/storage-port-posture.md`.

## Build Baseline

The current application build is defined by `app/codegeist/cli/pom.xml`.

| Area | Current state |
| --- | --- |
| Module shape | Single Maven module under `app/codegeist/cli` |
| Group/artifact | `ai.codegeist:codegeist` |
| Java | `25` through `java.version` and `maven.compiler.release` |
| Spring Boot | Parent `spring-boot-starter-parent` `3.5.14` |
| Spring Shell | BOM `3.4.2`, dependency `spring-shell-starter` |
| Spring AI | BOM `1.1.6` imported for dependency management only |
| GraalVM | Native Maven profile using `native-maven-plugin` `0.10.6` |
| Packaging | Spring Boot executable jar named `target/codegeist.jar` |
| Tests | Spring Boot context-load test only |

Spring AI provider starters are not present. The BOM import only records the
dependency-management posture for later provider work.

## Implemented File Layout

```text
app/codegeist/cli/
  pom.xml
  Taskfile.yml
  src/main/java/ai/codegeist/app/CodegeistApplication.java
  src/main/resources/application.yaml
  src/test/java/ai/codegeist/app/CodegeistApplicationTests.java
```

Implemented Java package:

| Package | Current responsibility |
| --- | --- |
| `ai.codegeist.app` | Spring Boot application entrypoint and bootstrap wiring |

No other `ai.codegeist.*` application packages currently exist in source code.

## Application Entrypoint

`CodegeistApplication` is the only implemented application class. It is annotated
with `@SpringBootApplication` and delegates startup to `SpringApplication.run`.

```mermaid
flowchart TD
    Main[CodegeistApplication.main]
    Boot[SpringApplication.run]
    Context[Spring application context]

    Main --> Boot --> Context
```

## Runtime Components

The current implemented component graph is intentionally small.

```mermaid
flowchart LR
    User[User or task command]
    Taskfile[app/codegeist/cli/Taskfile.yml]
    Maven[Maven]
    App[Spring Boot app]
    Test[Context-load test]
    Native[GraalVM native profile]

    User --> Taskfile
    Taskfile --> Maven
    Maven --> App
    Maven --> Test
    Maven -. optional .-> Native
```

Current behavior:

- `task run` builds the jar and runs `java -jar target/codegeist.jar`.
- The app starts a Spring Boot context using `application.yaml`.
- `application.yaml` sets `spring.application.name` to `codegeist` and enables
  interactive Spring Shell.
- There are no implemented shell commands, runtime services, provider calls, tool
  executions, permission prompts, workspace policies, storage adapters, server
  endpoints, Vaadin views, PF4J plugins, or JBang execution paths.

## Current Package Boundary

```mermaid
flowchart TD
    AppPackage[ai.codegeist.app]

    AppPackage --> Entry[CodegeistApplication]

    Runtime[ai.codegeist.runtime<br/>not implemented]
    Session[ai.codegeist.session<br/>not implemented]
    Event[ai.codegeist.event<br/>not implemented]
    Provider[ai.codegeist.provider<br/>not implemented]
    Tool[ai.codegeist.tool<br/>not implemented]
    Permission[ai.codegeist.permission<br/>not implemented]
    Workspace[ai.codegeist.workspace<br/>not implemented]
    Storage[ai.codegeist.storage<br/>not implemented]

    Entry -. future architecture references .-> Runtime
    Runtime -. planned boundary .-> Session
    Runtime -. planned boundary .-> Event
    Runtime -. planned boundary .-> Provider
    Runtime -. planned boundary .-> Tool
    Runtime -. planned boundary .-> Permission
    Runtime -. planned boundary .-> Workspace
    Runtime -. planned boundary .-> Storage
```

The dotted nodes are planned architecture concepts documented elsewhere. They are
included here only to prevent coding agents from assuming those packages already
exist. The vocabulary and dependency direction for those concepts are documented
in `docs/developer/runtime-vocabulary.md`, and their future contract blueprint is
documented in `docs/developer/runtime-session-event-contracts.md`.

## Test Architecture

`CodegeistApplicationTests` is a Spring Boot context-load test. It disables
Spring Shell interactive and noninteractive modes for the test context so the
bootstrap can be verified without starting an interactive shell.

```mermaid
sequenceDiagram
    participant Test as CodegeistApplicationTests
    participant Spring as Spring Boot TestContext
    participant App as CodegeistApplication

    Test->>Spring: Load context with shell modes disabled
    Spring->>App: Start application context
    App-->>Spring: Context starts or fails
    Spring-->>Test: contextLoads passes on successful startup
```

The test does not verify CLI commands, runtime behavior, provider integration,
native-image compatibility, or packaging output.

## Taskfile Verification Flow

`app/codegeist/cli/Taskfile.yml` provides the current developer entrypoints.

| Task | Command | Proves |
| --- | --- | --- |
| `task test` | `mvn --batch-mode --no-transfer-progress test` | Maven test lifecycle and Spring context-load test |
| `task build` | `mvn --batch-mode --no-transfer-progress -DskipTests clean package` | Executable jar packaging |
| `task native` | `mvn --batch-mode --no-transfer-progress -DskipTests -Pnative clean native:compile` | GraalVM native posture when practical |
| `task run` | `java -jar target/codegeist.jar` after `build` | Starts the packaged Spring Boot application |

```mermaid
flowchart TD
    Test[task test] --> MavenTest[mvn test]
    Build[task build] --> MavenPackage[mvn clean package -DskipTests]
    Native[task native] --> NativeCompile[mvn -Pnative clean native:compile -DskipTests]
    Run[task run] --> Build
    Build --> Jar[target/codegeist.jar]
    Run --> JavaJar[java -jar target/codegeist.jar]
```

## Configuration

`app/codegeist/cli/src/main/resources/application.yaml` currently contains only the
application name and Spring Shell interactive setting:

```yaml
spring:
  application:
    name: codegeist
  shell:
    interactive:
      enabled: true
```

No provider credentials, model configuration, workspace roots, permission policy,
storage locations, extension configuration, server configuration, or Vaadin
configuration are implemented.

## Not Implemented Yet

The following concepts are planned or discussed in architecture tasks, but they
are not implemented in the current Java application:

- Runtime orchestration.
- Session domain model.
- Runtime event model.
- Agent modes such as Plan and Build.
- Context loading from profile-selected instructions, state, work items,
  repository knowledge, or bounded source snippets.
- Spring AI provider adapter or model calls.
- Tool registry or tool execution.
- Permission approval flow.
- Workspace and file-access policy.
- Patch/edit proposal flow.
- Controlled shell execution.
- Storage ports or adapters.
- CLI/Spring Shell commands.
- Headless server endpoints.
- Vaadin client.
- PF4J plugin loading.
- JBang extension execution.

When implementing any of these concepts, update this document in the same task so
future coding agents can distinguish current code from future architecture.

## Related Documents

- `docs/developer/codegeist-opencode-parity.md` records target architecture,
  OpenCode parity mapping, planned boundaries, MVP cut, risks, and backlog.
- `docs/developer/runtime-vocabulary.md` records the Codegeist-owned runtime
  vocabulary and boundary diagram without requiring Java packages or classes to
  exist.
- `docs/developer/runtime-session-event-contracts.md` records the planned
  runtime/session/event contract shape, OpenCode source evidence, diagrams, and
  illustrative Java snippets without implementing Java source.
- `docs/developer/context-workspace-manifest.md` records the planned deterministic
  context-loading and workspace-validation manifest contract without implementing
  Java source, readers, provider calls, embeddings, Graphify, or Repomix.
- `docs/developer/provider-configuration-contracts.md` records the planned
  provider configuration, validation, typed provider error, and Spring AI adapter
  boundary without implementing Java source, provider starters, credentials, live
  model calls, or tests.
- `docs/developer/tool-permission-workspace-contracts.md` records the planned tool
  descriptor, permission decision, workspace validation, and bounded result
  boundary without implementing Java source, tool execution, permission UI,
  provider callbacks, shell execution, patch/edit behavior, or tests.
- `docs/developer/patch-edit-proposal-contracts.md` records the planned
  reviewable patch/edit proposal, apply request, typed apply failure, workspace
  validation, and bounded result boundary without implementing Java source, patch
  parsing, apply logic, file writes, rollback, formatter integration, or tests.
- `docs/developer/shell-verification-contracts.md` records the planned controlled
  shell verification request, permission gate, workspace-cwd validation, typed
  shell failure, and bounded output boundary without implementing Java source,
  tests, process execution, PTY support, terminal UI, remote execution, JBang
  execution, or shell sandboxing.
- `docs/developer/storage-port-posture.md` records the planned in-memory-first
  storage port posture, session/message projection boundaries, redaction rules,
  and later file/database adapter gates without implementing Java source, tests,
  storage ports, storage adapters, database schemas, migrations, or event sourcing.
- `docs/tasks/T002_implement-codegeist-mvp-foundation/` contains active
  implementation tasks for moving from the current bootstrap toward the MVP
  foundation.
- `.oc_local/rules/architecture-doc.md` defines how this current-state
  architecture document should be used and maintained.
