# T001_04 Define CLI And Shell Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist should expose CLI and interactive shell behavior.

This task defines the CLI contract and shell boundary only. It does not add
commands or implement runtime behavior.

## Architecture Decision

The MVP should be Spring Shell-first. The CLI and interactive shell are clients
of the Codegeist Runtime API; they parse user input, render runtime events, and
collect approval prompts, but they do not own sessions, agent mode execution,
provider calls, tool execution, permission decisions, workspace boundaries, or
audit events.

Full-screen TUI behavior is deferred. JLine may be used through Spring Shell or
later shell ergonomics for streaming output, prompts, completion, and terminal
interaction, but it must remain a presentation/detail layer over the runtime.

## Scope

- Map OpenCode CLI and TUI behavior to Spring Boot, Spring Shell, and JLine.
- Separate command parsing from runtime orchestration.
- Define interactive and non-interactive entrypoint expectations.

## OpenCode Reference Behaviors

Use OpenCode as a feature reference for these user-facing surfaces:

- `opencode` as the main CLI entrypoint.
- The default terminal UI as the primary interactive product surface.
- Plan and Build agent modes exposed through the CLI/TUI experience.
- `serve` and `web` commands as later server and browser-client entrypoints.
- Streaming model/tool output and user approval prompts.

Do not copy OpenCode's Bun, TypeScript, Hono, or full-screen TUI implementation
shape into Codegeist.

## Codegeist Entrypoint Model

Define these entrypoint classes:

| Entrypoint | MVP relevance | Owner | Runtime relationship |
| --- | --- | --- | --- |
| `codegeist` interactive shell | MVP | Spring Shell adapter | Starts a shell session and calls runtime services |
| `codegeist run "<prompt>"` | Candidate MVP | Spring Shell/CLI adapter | Creates or continues a runtime session with default mode |
| `codegeist plan "<prompt>"` | Candidate MVP | Spring Shell/CLI adapter | Calls runtime with Plan mode and read-only capability profile |
| `codegeist build "<prompt>"` | Candidate MVP | Spring Shell/CLI adapter | Calls runtime with Build mode and permission-gated side effects |
| `codegeist session ...` | Later/MVP candidate | Spring Shell/CLI adapter | Lists, continues, exports, or inspects runtime sessions |
| `codegeist context ...` | Later/MVP candidate | Spring Shell/CLI adapter | Inspects deterministic context selected by runtime/context services |
| `codegeist provider ...` | Later | Spring Shell/CLI adapter | Lists or validates provider configuration without owning provider calls |
| `codegeist serve` | Later | Server adapter | Exposes the same runtime over HTTP |
| `codegeist web` | Later | Vaadin/server adapter | Presents runtime sessions through a browser client |
| Full-screen TUI | Later | To decide | Must remain a runtime client, not a second runtime |

## Command Categories To Specify

- Session commands: start, continue, list, inspect, export.
- Prompt commands: run, plan, build.
- Approval commands: approve, deny, explain pending approval.
- Context commands: show loaded rules, memory, tasks, docs, source snippets, and
  third-party analysis artifacts.
- Configuration/provider commands: list, select, validate, doctor.
- Diagnostics commands: version, status, environment summary, native-image
  capability check.

## Runtime Boundary Rules

- CLI commands may translate terminal input into runtime requests.
- CLI commands may subscribe to runtime events and render text, progress, tool
  activity, errors, and completion.
- CLI commands may ask the user approval questions, but the permission decision,
  decision scope, caching, and audit event belong to the runtime/permission
  boundary.
- Plan and Build are runtime agent modes, not separate CLI implementations.
- Interactive and one-shot commands must exercise the same runtime path whenever
  they perform the same user-visible action.
- Server, Vaadin, and future TUI adapters must reuse the same runtime contracts
  instead of reimplementing CLI behavior.

## Streaming And Approval Expectations

- Runtime emits typed events for session lifecycle, text deltas, tool requests,
  approval requests, tool results, errors, and completion.
- CLI renders these events in terminal-friendly form.
- Approval prompts are presentation; approval policy remains centralized.
- If the terminal cannot support rich rendering, the CLI must degrade to plain
  line-oriented output without changing runtime semantics.

## Non-Goals

- Do not implement CLI commands.
- Do not implement full-screen TUI behavior.
- Do not implement provider calls, tool execution, permission storage, or event
  streaming.
- Do not add dependencies or change `app/codegeist` code in this task.
- Do not decide final MVP command list; that belongs to `T001_22` and backlog
  tasks.

## Deliverable

Add `## CLI And Shell Architecture` to
`docs/developer/codegeist-opencode-parity.md` with:

- the Spring Shell-first decision,
- the OpenCode reference behaviors,
- the Codegeist entrypoint matrix,
- command categories,
- runtime boundary rules,
- streaming and approval expectations,
- explicit non-goals.

## Acceptance Criteria

- CLI is defined as a client of the runtime.
- TUI/full-screen terminal behavior is not required for the first MVP.
- Streaming output and approval prompts are considered.
- Plan and Build are runtime modes, not separate CLI implementations.
- Interactive and non-interactive commands share runtime paths.
- Approval prompts are UI concerns, but permission decisions and audit events
  stay in the runtime/permission boundary.

## Verification

- Review current `app/codegeist` Spring Shell bootstrap assumptions.
- Confirm the parity document keeps this task documentation-only and does not
  require immediate command implementation.

## Specification Check Result

- Already specifies Spring Shell as the MVP client, keeps runtime orchestration
  out of CLI commands, and defers full-screen TUI behavior.
- No further task reshaping was needed during the `/specify-task` pass.
