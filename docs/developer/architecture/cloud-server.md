# Codegeist Cloud Server

Current-state source guide for the Codegeist Cloud server module.

## Purpose

`app/codegeist/server` is the second Codegeist Spring Boot application. It is the
starting point for the hosted SaaS control plane where authenticated users will
later access Codegeist-managed models, cloud-stored agent assets, and sync
workflows.

The server is intentionally separate from the local CLI runtime under
`app/codegeist/cli`. It is not a local `opencode serve` clone and does not expose
local file tools, shell tools, terminal TUI behavior, or local session-store state.

## Build Layout

The Java workspace now uses a Maven parent/aggregator under `app/codegeist`:

```text
app/codegeist/
  pom.xml
  Taskfile.yml
  cli/
    pom.xml
    Taskfile.yml
  server/
    pom.xml
    Taskfile.yml
```

The parent POM owns shared versions for Java 25, Spring Boot 4.0.6, Lombok,
Spring AI, Spring Shell, Spring AI Agent Utils, and GraalVM build tools. The CLI
and server keep module-specific dependencies, executable jar names, and native
image names.

## Implemented Server Slice

Implemented files:

- `app/codegeist/server/pom.xml` - standalone server module under the shared Maven
  parent, including the server `native` profile.
- `app/codegeist/server/Taskfile.yml` - server-local `test`, `build`, `native`,
  `native-smoke`, and `run` entrypoints.
- `scripts/tests/server-native-smoke.ps1` - native server smoke harness that starts
  the native executable on a temporary localhost port, polls `/health`, reports
  startup timing, and stops the process.
- `CodegeistServerApplication` - Spring Boot server entrypoint with application
  name `codegeist-server`.
- `HealthController` - minimal unauthenticated `GET /health` endpoint returning
  `{"status":"ok"}`.
- `CodegeistServerApplicationTests` - proves the Spring context loads.
- `HealthControllerTest` - proves the health endpoint contract through MockMvc.

The first slice uses `spring-boot-starter-webmvc`, validation, and Spring Boot
test dependencies. It does not add Actuator yet; the `/health` endpoint is a small
bootstrap contract, not a final operational readiness API.

## Non-Goals In Current Source

- No authentication or identity-provider integration.
- No users, organizations, tenancy model, entitlements, quotas, or billing.
- No database, metadata store, S3 client, MinIO/AWS test harness, or object-store
  side effects.
- No OpenRouter, OpenAI-compatible proxy, model routing, streaming, usage
  accounting, hosted-provider credentials, or live remote provider calls.
- No shared Java module between CLI and server yet.

## Verification

Run the server tests from `app/codegeist/server`:

```bash
task test
```

Run both Java application test suites from `app/codegeist`:

```bash
task test
```

Build both GraalVM native executables from `app/codegeist`:

```bash
task native
```

Build and smoke-test the native server executable while measuring `/health`
startup time:

```bash
task server:native-smoke
```

Future server tasks should keep using `app/codegeist/server/Taskfile.yml` for
focused verification and update this document when authentication, storage,
metadata, or model-proxy behavior becomes implemented source.
