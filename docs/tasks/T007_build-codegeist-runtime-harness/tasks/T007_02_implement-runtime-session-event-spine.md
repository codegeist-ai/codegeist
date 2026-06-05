# T007_02 Implement Runtime Session Event Spine

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Implement the smallest Codegeist runtime/session/event spine around the existing
provider-backed prompt path.

The first spine should let a client submit one prompt request, create a runtime
turn, emit typed events, call the selected provider through the existing
`CodegeistChatService`, and return a final assistant result. It should be useful to
CLI and future TUI clients without implementing full storage, tool calling, or
permission approval yet.

## Current Starting Point

- `AskCommands` directly selects provider config and calls `CodegeistChatService`.
- `CodegeistChatService` validates one `CodegeistChatRequest`, asks the selected
  `ProviderConfig` for a concrete `CodegeistChatModel`, calls the model, and returns
  `CodegeistChatResponse`.
- There is no runtime package, session identity, turn identity, event publisher,
  stream/event projection, cancellation, storage, or prompt loop.
- `application.yaml` keeps `spring.shell.interactive.enabled=false`; this task must
  not break noninteractive Spring Shell command handling.

## OpenCode Evidence To Translate

Use these source paths from `T007_01` as behavior references:

- `docs/third-party/opencode/source/packages/opencode/src/session/prompt.ts` for
  prompt orchestration.
- `docs/third-party/opencode/source/packages/opencode/src/session/processor.ts` for
  stream processing and message part updates.
- `docs/third-party/opencode/source/packages/opencode/src/session/llm.ts` for model
  invocation and plugin hooks.
- `docs/third-party/opencode/source/packages/opencode/src/session/message-v2.ts` for
  message and part vocabulary.
- `docs/third-party/opencode/source/packages/opencode/src/bus/index.ts` and
  `sync/index.ts` for event publication and projections.
- `docs/third-party/opencode/source/packages/opencode/src/agent/agent.ts` for basic
  agent/mode expectations.

Translate only the needed behavior. Do not copy storage schemas, Effect layers, or
TypeScript data models.

## Scope

- Introduce the minimal Java/Spring runtime service needed by a testable one-prompt
  run.
- Add session/turn identifiers only if the test needs them for observable behavior
  or event correlation.
- Add typed events for request start, provider request start, assistant output,
  completion, and failure. Use the smallest closed set needed by the current test.
- Reuse `CodegeistConfig.defaultProvider()`, `ProviderConfig.defaultModel()`, and
  `CodegeistChatService` instead of adding a second provider path.
- Make the existing `ask` command delegate through the runtime only if the focused
  test proves the same user-visible output and no regression.
- Keep event rendering separate from event creation so TUI and CLI can subscribe or
  consume later.

## Acceptance Criteria

- A focused test proves a prompt request produces a runtime result and ordered
  typed events.
- Provider selection still flows through the existing config and chat service.
- The runtime does not know about Spring Shell command details.
- `ask` remains a one-shot command with the same stdout contract if it is migrated
  to the runtime path in this task.
- No tool execution, storage persistence, permission prompts, or TUI rendering is
  introduced in this child.
- `docs/developer/architecture/architecture.md` documents any new packages,
  classes, runtime flow, and tests added by the task.

## Non-Goals

- Do not add a full session database or replay model.
- Do not add streaming token deltas unless the current provider path and test need
  them.
- Do not add Spring AI tool callbacks or Agent Utils tools.
- Do not implement plan/build agent modes beyond minimal labels or mode values
  needed by the runtime test.
- Do not create placeholder event hierarchies for every future OpenCode event.

## Suggested Tests

- Plain JVM tests for small value objects if any are introduced.
- Spring Boot test when proving runtime wiring, config-backed provider selection, or
  `ask` command delegation.
- Keep provider-call tests behind the existing local provider category when they hit
  real Ollama.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<new-runtime-test-selector>
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=<local-provider-runtime-selector>
task test
```
