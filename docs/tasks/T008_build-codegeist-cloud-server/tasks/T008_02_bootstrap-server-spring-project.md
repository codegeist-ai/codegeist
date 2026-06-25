# T008_02 Bootstrap Server Spring Project

Status: solved

Parent: `../task.md`

## Goal

Add the first implemented Codegeist Cloud server module beside the existing CLI
module and introduce the shared Maven parent/aggregator at `app/codegeist/pom.xml`.

## Implemented Scope

- Added `app/codegeist/pom.xml` as the shared Maven parent and aggregator.
- Kept `app/codegeist/cli` as a child module with its existing executable jar name
  `target/codegeist.jar`.
- Added `app/codegeist/server` as a child module with executable jar name
  `target/codegeist-server.jar`.
- Added `CodegeistServerApplication` as the server Spring Boot entrypoint.
- Added a minimal unauthenticated `GET /health` endpoint returning
  `{"status":"ok"}`.
- Added server context-load and health endpoint tests.
- Added server-local and parent-level Taskfile entrypoints.
- Added the server GraalVM native profile plus `server:native-smoke` startup timing
  for the bootstrap `/health` endpoint.

## Acceptance Criteria

- `app/codegeist/pom.xml` lists `cli` and `server` modules.
- Shared Java, Spring Boot, Spring AI, Spring Shell, Lombok, and GraalVM build-tool
  versions live in the parent POM.
- `app/codegeist/server` builds independently through its own `Taskfile.yml`.
- `task server:native-smoke` from `app/codegeist` builds the native server and
  verifies the bootstrap `/health` endpoint with startup timing.
- The first server endpoint is local and deterministic.
- No auth, storage, database, OpenRouter, hosted LLM, quota, or billing behavior is
  implemented in this bootstrap.

## Verification

```bash
task test
```

Run the command from `app/codegeist/server` for the focused server suite. Run
`task test` from `app/codegeist` when both CLI and server suites should run.

```bash
task server:native-smoke
```

Run the native smoke from `app/codegeist` to build the GraalVM native server,
start it on a temporary localhost port, and measure `/health` startup time.
