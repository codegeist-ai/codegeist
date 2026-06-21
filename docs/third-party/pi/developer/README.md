# Pi Developer Notes

These notes summarize the source layout and implementation flow from the local Pi analysis workspace.

## Source Layout

- `packages/ai` - provider-neutral LLM API, provider registry, model registry data, provider implementations, OAuth helpers, streaming utilities, image APIs, and validation helpers.
- `packages/agent` - reusable agent loop, `Agent`, event types, tool execution, queues, and the newer `AgentHarness`/session architecture.
- `packages/coding-agent` - CLI binary, mode selection, settings, auth, resources, extensions, built-in tools, sessions, compaction, TUI integration, RPC/JSON mode, package manager, and HTML export.
- `packages/tui` - terminal UI framework and components.
- `scripts` - release, smoke, dependency, shrinkwrap, stats, profiling, and maintenance scripts.
- `.pi` - project-local prompts, skills, and extension examples used to dogfood Pi.

## Main Runtime Flow

1. `packages/coding-agent/src/bun/cli.ts` restores sandbox environment details, registers Bedrock for Bun builds, and imports `src/cli.ts`.
2. `src/cli.ts` sets `PI_CODING_AGENT=true`, configures HTTP dispatch, and calls `main(process.argv.slice(2))`.
3. `src/main.ts` resolves CLI mode from flags and TTY state, handles package/config commands, runs migrations, initializes settings, resolves sessions, applies project trust, loads resources, resolves models, and creates an `AgentSessionRuntime`.
4. `AgentSessionRuntime` owns the current `AgentSession` plus cwd-bound services and supports replacing the current session on resume/new/fork flows.
5. `AgentSession` binds extensions and modes, creates built-in or custom tool definitions, expands skills/templates, drives prompts, writes session events, and delegates to `Agent`.
6. `Agent` wraps `agentLoop`, tracks state, queues steering/follow-up messages, awaits subscribers, and controls cancellation.
7. `agentLoop` converts `AgentMessage` values to LLM messages, streams assistant output through `pi-ai`, validates and executes tool calls, appends tool results, and repeats until no tool calls or queued messages remain.
8. The active mode renders or forwards events: interactive TUI, print text, JSON event stream, or RPC JSONL.

For the source-level prompt path from CLI input through `AgentSession`, `Agent`, provider streaming, tools, extension hooks, and JSONL persistence, read `prompt-flow.md`.

## AgentHarness Status

`packages/agent/src/harness/agent-harness.ts` is a newer orchestration layer above the low-level loop. Its docs describe session persistence, runtime config snapshots, resource resolution, operation phases, extension-facing mutation semantics, and future durability work. Several docs explicitly mark parts as planned or provisional, so verify source/tests before treating all described behavior as shipped.

## Extension Architecture

Extensions are TypeScript factories loaded through Jiti. They can register tools, commands, shortcuts, flags, providers, and event handlers. They can also interact with UI facades, modify or block tool calls, inject context, customize rendering, and persist custom session entries.

Important source areas:

- `packages/coding-agent/src/core/extensions/`
- `packages/coding-agent/docs/extensions.md`
- `packages/coding-agent/examples/extensions/`
- `packages/agent/docs/hooks.md`

## Tools

Built-in tool definitions live under `packages/coding-agent/src/core/tools/`:

- `read`
- `bash`
- `edit`
- `write`
- `grep`
- `find`
- `ls`

`createCodingToolDefinitions()` exposes `read`, `bash`, `edit`, and `write`. `createReadOnlyToolDefinitions()` exposes `read`, `grep`, `find`, and `ls`. `createAllToolDefinitions()` exposes all seven.

## Tests And Checks

The root scripts define:

```bash
npm run check
./test.sh
```

`npm run check` covers formatting/linting, pinned dependencies, import compatibility, shrinkwrap verification, TypeScript, and browser-smoke checks. `./test.sh` hides local auth and provider credentials before running `npm test`.

The upstream `AGENTS.md` says not to run `npm run build`, `npm test`, or broad Vitest directly unless requested. It prefers `./test.sh` for non-e2e tests and package-specific Vitest commands for targeted tests.

## Static Analysis Artifacts

- `repomix-output.xml` is the broad packed source corpus for `/ask-project` deep dives.
- `graphify-out/GRAPH_REPORT.md` summarizes graph communities, hubs, surprises, and questions.
- `graphify-out/graph.json` is the machine-readable graph for graph-backed questions.
- `graphify-out/graph.html` is the interactive local graph visualization.

## Follow-Up Questions

- `/ask-project pi "How does AgentSession bind extensions and map extension events into tools, UI, and session persistence?"`
- `/ask-project pi "Which parts of AgentHarness are implemented today and which are still planned?"`
- `/ask-project pi "How does Pi's provider abstraction map OpenAI, Anthropic, Google, Bedrock, and OAuth providers into one stream API?"`
