# Pi Analysis Report

## Scope

This report analyzes `https://github.com/earendil-works/pi.git` at revision `bc0db643502ba0bf1b227a97d9d5885cefc2b909` on branch `main`.

Source material used:

- Local source checkout under `docs/third-party/pi/source`.
- Repomix output generated from TypeScript, JavaScript, Markdown, package/config metadata, and shell/PowerShell files with generated, vendored, build, lock, binary, and environment-like files excluded where practical.
- Graphify output generated from a temporary focused source/document corpus under `graphify-input-focus/` and written to ignored `graphify-out/`.
- Direct inspection of key upstream docs and source files including `README.md`, `packages/coding-agent/README.md`, `packages/agent/README.md`, `packages/ai/README.md`, `packages/tui/README.md`, `packages/coding-agent/src/main.ts`, `packages/coding-agent/src/core/agent-session.ts`, `packages/agent/src/agent-loop.ts`, and `packages/agent/src/harness/agent-harness.ts`.

Runtime evidence is missing. No commands were executed inside the upstream checkout beyond source inspection and reproducible analysis generation.

## Project Purpose

Pi is a terminal-first coding-agent harness. Its root README describes four packages:

- `@earendil-works/pi-ai` - unified provider/model API for tool-capable LLMs.
- `@earendil-works/pi-agent-core` - stateful agent loop, tool execution, event streaming, and harness work.
- `@earendil-works/pi-coding-agent` - interactive coding-agent CLI.
- `@earendil-works/pi-tui` - terminal UI framework with differential rendering.

The product philosophy is deliberately minimal core plus high extensibility. Pi ships with a usable coding-agent loop and built-in tools, but the docs explicitly move optional workflows such as MCP, subagents, plan mode, permission popups, todos, background bash, and alternate UI behaviors into extensions, skills, Pi packages, or local environment patterns.

## Repository Shape

- Root `package.json` defines an npm workspace monorepo, Node `>=22.19.0`, TypeScript/native-preview tooling, Biome, release scripts, dependency-pin checks, shrinkwrap checks, and a local release smoke path.
- `packages/ai` owns provider-neutral LLM types, registries, streaming/completion entrypoints, provider implementations, OAuth helpers, generated model metadata, image APIs, environment-key resolution, and test utilities such as the faux provider.
- `packages/agent` owns the low-level `Agent`, `agentLoop`, tool-call execution, queueing, event streams, and the newer `AgentHarness`/session architecture.
- `packages/coding-agent` owns the `pi` binary, CLI argument parsing, interactive/print/JSON/RPC modes, sessions, compaction, built-in local tools, model registry, auth storage, settings, project trust, resources, extensions, package manager, export-to-HTML, and startup UI.
- `packages/tui` owns the reusable terminal rendering library used by interactive mode: TUI container, terminal abstraction, editor/input components, overlays, Markdown rendering, selection widgets, key matching, terminal images, and width-aware text handling.
- `scripts/` owns release, dependency, shrinkwrap, smoke, profiling, stats, and source-import maintenance utilities.
- `.github/` contains contribution-gate, CI, npm audit, binary build, and issue/PR automation workflows.
- `.pi/` contains project-local prompts, skills, and example extensions for the repository's own dogfooding.

## Runtime Architecture

The main coding-agent path is layered:

1. `packages/coding-agent/src/bun/cli.ts` and `src/cli.ts` bootstrap process metadata, restore sandbox environment details, configure HTTP dispatch, and delegate to `main()`.
2. `packages/coding-agent/src/main.ts` parses CLI flags, resolves the app mode, manages sessions, applies project trust, creates cwd-bound services, loads resources/settings/extensions, resolves models and tools, and launches interactive, print, JSON, or RPC mode.
3. `AgentSession` is the coding-agent runtime facade shared by modes. It owns model/thinking management, built-in tool definitions, extension binding, prompt/template/skill expansion, compaction, session tree operations, event subscription, and persistence coordination.
4. `Agent` from `packages/agent` wraps the low-level agent loop. It holds mutable state, awaited event listeners, steering and follow-up queues, current model/tool configuration, and run cancellation.
5. `agentLoop` converts Pi's broader `AgentMessage` history to provider-facing LLM messages at the LLM boundary, streams assistant responses, executes tool batches, appends tool results, drains steering/follow-up queues, and optionally prepares the next turn snapshot.
6. `pi-ai` dispatches provider calls through registered provider implementations. `streamSimple()` and `stream()` resolve the provider by model `api`, attach environment API keys when needed, and return event streams whose final result is an assistant message.
7. Built-in tools live under `packages/coding-agent/src/core/tools/` and expose `read`, `bash`, `edit`, `write`, `grep`, `find`, and `ls`. The default coding-tool set is `read`, `bash`, `edit`, and `write`; read-only definitions are available for narrower contexts.
8. Interactive mode renders events through `pi-tui`, while print, JSON, and RPC modes adapt the same `AgentSession` events to stdout or JSONL protocols.

## Main User Surfaces

- Interactive terminal mode: default `pi` invocation with TUI, editor, slash commands, model selector, sessions, tree navigation, settings, compaction, extension UI, and tool rendering.
- Print mode: `pi -p` or non-TTY input/output for a single prompt and final text response.
- JSON event stream mode: `pi --mode json` outputs session and agent events as newline-delimited JSON.
- RPC mode: `pi --mode rpc` exposes commands, responses, events, and extension UI requests over strict LF-delimited JSONL.
- SDK usage: consumers can import `createAgentSession`, `AgentSession`, `Agent`, and lower-level Pi packages directly.
- Package manager commands: `pi install`, `pi remove`, `pi update`, `pi list`, and `pi config` manage Pi packages and resources.
- Session export: session files can be exported to HTML.

## Features And Capabilities

- Provider/model support spans subscription auth and API-key providers: Anthropic, OpenAI/Codex, GitHub Copilot, Google, Vertex, Bedrock, Mistral, Groq, Cerebras, xAI, OpenRouter, Cloudflare, Vercel AI Gateway, ZAI, MiniMax, Together, Fireworks, Kimi, Xiaomi MiMo, and OpenAI-compatible APIs.
- `pi-ai` supports streaming text/thinking/tool-call events, image input, image generation, thinking/reasoning options, cross-provider handoffs through serializable context, provider-scoped environment overrides, and OAuth/device flows for selected providers.
- Sessions are JSONL files with tree-shaped entries, branch navigation, labels, compaction summaries, model/thinking changes, custom messages, and migration from earlier versions.
- Interactive mode supports file references, path completion, image paste/drop, shell commands, keyboard shortcuts, queued steering/follow-up messages, tree navigation, collapse controls, model cycling, and custom UI from extensions.
- Extensions can register tools, commands, shortcuts, flags, providers, event handlers, custom renderers, custom TUI components, widgets, status/footer/header UI, and compaction behavior.
- Skills follow the Agent Skills shape and can be invoked with `/skill:name` or loaded automatically.
- Prompt templates, themes, extensions, and skills can live globally, project-locally, or inside Pi packages distributed through npm/git.

## Security And Trust Findings

- Pi has no built-in sandbox. Built-in tools can read, write, edit, and run shell commands with the permissions of the process.
- Project trust is an input-loading guard, not a permission boundary. It controls whether project-local `.pi` settings/resources/packages/extensions load before approval.
- `AGENTS.md` and `CLAUDE.md` context files are loaded regardless of project trust unless context loading is disabled.
- Extensions and Pi packages run as ordinary TypeScript/Node code with full process permissions. The upstream docs explicitly tell users to review third-party package source before installing.
- Upstream docs recommend container, VM, micro-VM, or policy sandbox boundaries for untrusted or unattended work.
- Containerization patterns include Gondolin tool routing, plain Docker for the whole process, and OpenShell for policy-controlled sandboxing.
- Startup network behavior includes version checks and install/update telemetry unless disabled with settings or `PI_OFFLINE=1`/`--offline`.

## Dependency And Release Posture

- The root README describes supply-chain hardening: exact direct dependency pinning, `save-exact=true`, `min-release-age=2`, reviewed lockfile changes, shrinkwrap generation for the published CLI, `--ignore-scripts` installs, and scheduled npm audits.
- `packages/coding-agent/npm-shrinkwrap.json` is generated from the root lockfile and shipped with the published CLI package.
- Release scripts are lockstep across packages. The upstream agent rules say all packages share one version and releases are patch or minor, with no major releases.
- The local release path builds and packs isolated Node and Bun installs before tagging.

## Test And Verification Posture

- Root `npm run check` runs Biome, pinned-dependency checks, TypeScript import checks, shrinkwrap verification, `tsgo --noEmit`, and browser-smoke verification.
- Root `./test.sh` temporarily moves `~/.pi/agent/auth.json`, unsets many provider credentials, sets `PI_NO_LOCAL_LLM=1`, and runs `npm test` to avoid accidental provider calls.
- Package tests use Vitest, with package-specific guidance to avoid broad e2e activation when credentials are present.
- The agent harness docs recommend the `pi-ai` faux provider for deterministic harness/provider tests.
- This analysis did not execute `npm install`, `npm run check`, `./test.sh`, package tests, smoke tests, TUI runs, or provider calls.

## Graphify Summary

- Focused corpus: 810 files, about 719,195 words.
- Structural extraction: 7,709 AST nodes and 22,681 AST edges.
- Semantic extraction: 125 documentation nodes, 172 documentation edges, and 21 hyperedges from four documentation chunks.
- Final graph: 7,833 nodes, 18,263 graph edges, 349 communities.
- Extraction summary from `GRAPH_REPORT.md`: 99% extracted, 1% inferred, 0% ambiguous; 115 inferred edges with average confidence around 0.82.
- HTML export required `GRAPHIFY_VIZ_NODE_LIMIT=10000` because the graph exceeded Graphify's default 5,000-node visualization limit.
- Token benchmark estimated a 22.2x average reduction compared with reading the full corpus for each question.

## Repomix Summary

Repomix was generated at `docs/third-party/pi/repomix-output.xml`.

- Total files: 815.
- Total tokens reported by Repomix: 1,796,199.
- Total characters reported by Repomix: 6,962,536.
- Security check: Repomix reported no suspicious files.
- Additional pattern scan found only test/documentation placeholders such as `test-fireworks-key`, `OPENROUTER_API_KEY`, and example API-key strings.

## Gaps And Risks

- Runtime evidence is missing; no upstream command was executed.
- Graphify semantic extraction covered documentation chunks, while code structure was covered by AST. It should be enough for navigation, but not for source-level behavioral proof in every subsystem.
- Generated model metadata, changelogs, vendored JS, lockfiles, shrinkwrap data, binary/image assets, build output, and environment-like files were intentionally excluded from Repomix/Graphify filters where practical.
- The source repo includes planned or provisional AgentHarness docs. Treat claims marked planned or future in upstream docs as not fully implemented until confirmed in source/tests.
- Pi's no-sandbox design is intentional but security-relevant for users expecting OpenCode/Claude-Code-style permission prompts.
- Broad provider support means hosted-provider runtime behavior can vary by credentials, transport, endpoint, and provider-specific API details.

## Suggested Follow-Up

The prompt-flow follow-up is documented in `developer/prompt-flow.md`, with an editable sequence diagram at `diagrams/source/prompt-flow-sequence.mmd`.

Other useful questions:

- `/ask-project pi "What exactly does project trust load or block, and where are the remaining security boundaries?"`
- `/ask-project pi "How do Pi extensions intercept tool calls, register providers, and add custom TUI or RPC UI behavior?"`
- `/ask-project pi "How does Pi's JSONL session tree represent branching, compaction, model changes, and custom extension messages?"`
