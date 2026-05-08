# OpenCode Feature Notes

## Core Feature Clusters

- AI coding agent: interactive coding assistance with build and plan agent modes.
- Terminal UI: terminal-first interface and default product experience.
- Headless server: API server for remote, SDK, web, and automation clients.
- Web and desktop clients: alternative clients over the same runtime concepts.
- Provider-agnostic model access: bundled AI SDK providers plus custom configuration and auth flows.
- Session management: prompts, message parts, events, cost/tokens, snapshots, and state projection.
- Tool execution: file, shell, patch, web, LSP, task/subagent, and plugin-provided tools mediated by permissions.
- Plugins: extension points for hooks, tools, shell/TUI behavior, and external integration.
- SDK/OpenAPI: generated client surface for programmatic access.

## Feature Relationships

Graphify grouped the project around these cross-cutting features:

- OpenCode user interfaces: terminal UI, web app, and desktop app.
- Agent modes: build, plan, and general subagent.
- Core packages: runtime/server, TUI code, shared UI components, desktop app, and plugin package.
- Development and production CLI equivalence: `bun dev` mirrors `opencode` commands for local development.
- Provider and model ecosystem: bundled providers, model discovery, and provider configuration.
- Instance route parity: legacy Hono routes and Effect `HttpApi` routes need to stay aligned.

## Missing Evidence

No feature was exercised at runtime. The current notes come from source, docs, Repomix, and Graphify only.
