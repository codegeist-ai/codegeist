# OpenCode Analysis Report

## Scope

This report analyzes `https://github.com/anomalyco/opencode.git` at revision `d46af9cf1e7168d519377044f2412dea08ead5f8` on branch `dev`.

Source material used:

- Local source checkout under `docs/third-party/opencode/source`.
- Repomix output generated from source, documentation, package, and config files.
- Graphify output generated from a focused runtime corpus. The temporary corpus
  directory is not required after generation; `graphify-out/` remains the
  ignored local graph cache used by `/ask-project`.

Runtime evidence is missing. No commands were executed inside the upstream checkout beyond source inspection and reproducible analysis generation.

## Project Purpose

OpenCode is an open-source AI coding agent. The upstream README describes it as provider-agnostic, terminal-focused, and built around a client/server architecture. The repository contains more than the terminal app: it includes the core runtime, server, Solid web app, Electron desktop app, plugin package, generated SDK, docs/marketing site, console/cloud-adjacent code, and release/development automation.

## Main User Surfaces

- CLI: package binary `opencode` from `packages/opencode/package.json`.
- TUI: default terminal interface launched through the CLI.
- Headless server: `opencode serve`, defaulting to port `4096` in the source-level command implementation.
- Web UI: `opencode web` starts or attaches to a server and opens a browser client.
- Desktop app: Electron-based desktop distribution described in the upstream README.
- SDK: generated JavaScript SDK under `packages/sdk/js`.
- Plugin API: package under `packages/plugin` for extensions, hooks, tools, and TUI integration.

## Repository Shape

- `packages/opencode` contains the core product runtime: CLI, server, sessions, providers, permissions, tools, config, storage, plugin loading, and event handling.
- `packages/core` contains shared utilities and Effect runtime support.
- `packages/app` contains the Solid web UI.
- `packages/desktop` contains the Electron shell.
- `packages/plugin` contains plugin-facing package code.
- `packages/sdk/js` contains generated SDK code.
- `packages/web` contains the public website and documentation surface.
- `packages/console`, `packages/enterprise`, and `packages/slack` are adjacent operational or integration surfaces.

## Runtime Architecture

The central runtime path is session oriented:

1. A user enters through the CLI, TUI, web UI, desktop app, or SDK.
2. The client talks to the server or an in-process server adapter.
3. Server routes dispatch requests into workspace and instance contexts.
4. Session processing builds prompts, selects agents, resolves provider/model configuration, and streams LLM output.
5. Tool execution is mediated by tool registries, agent mode, configuration, plugins, provider/model support, and permission decisions.
6. Message parts, events, costs, and state are persisted or projected for clients.

Graphify highlighted package-manifest dependency hubs plus these runtime and API
hubs. Treat `catalog` and `scripts` as package-manifest navigation nodes rather
than architecture abstractions:

- `SessionID`
- `Authorization`
- `InstanceHttpApi`
- `InstanceContextMiddleware`
- `WorkspaceRoutingQuery`
- `WorkspaceRoutingMiddleware`
- `matchLegacyOpenApi()`

## Important Architecture Findings

- The project has two server API implementation tracks: legacy Hono routes and Effect `HttpApi` route/schema work. Graphify found an explicit `Instance Route Parity System` community, which indicates route parity is an important maintenance axis.
- Provider support is intentionally broad. The core package depends on many AI SDK provider packages and additional provider integrations.
- Tool execution is a broad capability surface and should be analyzed with permissions, agent modes, plugins, and model support together rather than as independent modules.
- OpenCode treats package-directory boundaries as important for development. The root `test` script intentionally fails, and upstream guidance says tests and type checks should run from package directories.
- The server warns when no `OPENCODE_SERVER_PASSWORD` is configured. Static analysis cannot confirm deployment assumptions, so exposed server use needs runtime and operational review.

## Graphify Summary

- Focused corpus: 148 files, about 96,767 words.
- Graph: 1,340 nodes, 2,011 edges, 93 communities.
- Extraction confidence: 99% extracted and 1% inferred, with 27 inferred edges.
- Key grouped relationships included user interfaces, agent modes, core packages,
  prompt families, package-directory development workflow, provider/model
  ecosystem, instance route parity, and HttpApi handler patterns.

## Repomix Summary

Repomix was generated from a broad source/document/config corpus with heavy generated or unusually large files excluded where practical. The artifact is intentionally ignored at `repomix-output.xml` and can be regenerated from `REGENERATE.md`.

- Current packed output: 3,033 files, 28,647,149 bytes.
- Security scan: Repomix reported no suspicious files.
- The broad corpus still contains large generated, fixture, SDK, and OpenAPI
  materials; use targeted `/ask-project` questions or Graphify for focused
  runtime navigation.

## Gaps And Risks

- No runtime verification was performed.
- Full Graphify semantic extraction was not run over the entire repository; the graph is focused on core runtime architecture.
- Desktop, web, PTY, OAuth/provider auth, plugin loading, and remote/server workflows need separate runtime checks.
- Generated SDK/OpenAPI details are large and were not deeply reviewed in this first pass.
- Route parity between Hono and Effect `HttpApi` deserves dedicated follow-up analysis.

## Suggested Follow-Up

Ask:

```text
/ask-project opencode "How does a user prompt flow from the CLI or TUI through session processing, provider streaming, tool execution, permissions, and event rendering?"
```
