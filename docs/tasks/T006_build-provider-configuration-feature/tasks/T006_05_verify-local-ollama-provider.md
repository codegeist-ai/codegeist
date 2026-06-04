# T006_05 Verify Local Ollama Provider

Status: solved

Parent: `../task.md`

## Goal

Implement and verify the first provider-backed Codegeist workflow with local
Ollama.

This is the first real provider call and should stay local, deterministic, and
small before remote provider account work begins.

## Scope

- Use the schema and loader from `T006_01` and `T006_04`.
- Follow `docs/developer/specification/llm-provider-implementation.md` for the
  provider-neutral chat pattern. T006_05 should prove Ollama as the first concrete
  provider without making Codegeist's chat path Ollama-specific.
- Add `org.springframework.ai:spring-ai-ollama` as the smallest Spring AI Ollama
  dependency needed for programmatic on-demand use. Do not add
  `spring-ai-starter-model-ollama` in this task unless implementation proves the
  starter is required; the starter could auto-create Spring AI beans from global
  `spring.ai.*` properties, while this slice should map evaluated Codegeist config
  to one selected provider call without mutating global Spring properties.
- Do not add Testcontainers for this task. The focused live local test should use
  an externally managed local Ollama instance that is already running when the test
  starts.
- Use the fixed local Ollama base URL `http://localhost:11434` and fixed model
  `llama3.2:1b` in the focused test. The model must be present before the test
  starts; the test should not pull, download, or create it.
- Before running `task test` for this task, run `OLLAMA_ENTER=false task
  ollama-start` from `app/codegeist/cli` so the externally managed local Ollama
  service is ready in non-interactive automation.
- Do not read generation options from `codegeist.yml`; later runtime request or
  provider feature test work should own temperature, seed, and similar knobs.
- Use a narrow prompt and stable assertion.
- Report Spring context startup and first chat-call timing separately from ordinary
  Spring test timing. Do not add a separate model-list preflight before the chat
  call.
- Update current-state architecture for implemented provider behavior and tests.

## Implementation Notes

- Implement the minimal provider-neutral chat seam from
  `llm-provider-implementation.md`: `CodegeistChatService`, `CodegeistChatRequest`,
  `CodegeistChatResponse`, `CodegeistChatModel<T extends ProviderConfig>`, and
  `OllamaChatModel` when the focused test needs those classes.
- `CodegeistChatService` should receive the selected raw `ProviderConfig` separately
  from `CodegeistChatRequest`; the request carries only runtime model and prompt.
  The service asks that provider config to create the matching `CodegeistChatModel`.
- Keep provider-specific imports isolated in `OllamaChatModel`. The generic chat
  service should depend only on `ProviderConfig` and `CodegeistChatModel`.
- `CodegeistChatService` should create the selected provider's Codegeist chat model lazily
  inside the call path. Loading config, running `--show-config`, or starting the
  Spring context must not pull models, create every configured provider client, or
  call providers.
- Do not introduce a public provider plugin API, model catalog, provider ranking,
  fallback policy, smoke command, CLI command, or cross-provider status model in
  this task.
- Programmatic Spring AI `2.0.0-M6` construction should use the documented
  `OllamaApi.builder().baseUrl(...).build()` and
  `OllamaChatModel.builder().ollamaApi(...).build()` path. Build request options
  with `OllamaChatOptions.builder().model(...).build()` when the selected prompt is
  called.
- The focused test should let the selected chat call prove local Ollama and model
  availability. It must not run a separate model-list preflight and must not pull,
  prepare, download, or delete models.
- The focused test should load provider config from a temp `codegeist.yml` through
  `CodegeistConfigService.loadConfig(String)` so the real T006 loader, SpEL phase,
  provider dispatch, and Bean Validation are exercised before the provider call.
- Use a stable one-turn prompt such as `Respond with exactly the lowercase word
  codegeist.` Assert on a narrow, case-insensitive response containing `codegeist`
  rather than asserting a full natural-language answer.
- Use `task test` for verification, never a direct `mvn test` command in this
  task. When a focused selector is needed, use the Taskfile selector form such as
  `task test TEST=LocalOllamaProviderIT`.
- The focused test should print or otherwise report Spring context startup and the
  first chat call timing in its Maven output. Approximate durations are sufficient;
  do not hide live provider latency inside an ordinary-looking unit test.

## Non-Goals

- Do not use remote provider credentials.
- Do not implement tool calling, permissions, sessions, storage, TUI, Vaadin,
  server APIs, PF4J, or JBang.
- Do not add fake providers in place of the Ollama call.
- Do not add Testcontainers, container lifecycle management, model pulls, or model
  downloads.
- Do not make the ordinary unit test suite depend on a ready local Ollama instance;
  keep the live local check behind the explicit focused test selector.

## Acceptance Criteria

- A focused test proves Codegeist can call a local Ollama model through Spring AI.
- The test command is individually runnable.
- The task reports Spring context startup timing and first chat-call timing.
- Provider configuration comes from `codegeist.yml` or a test fixture matching the
  schema.
- The implementation does not introduce public provider architecture beyond what
  the tested workflow needs.
- Verification uses `task test` rather than direct Maven. The workflow starts local
  Ollama first with `OLLAMA_ENTER=false task ollama-start`; it does not pull or
  download models and does not use Testcontainers.
- The focused local-provider test uses no remote provider credentials and performs
  no hosted provider calls.
- The Ollama call path is lazy and selected-provider only; normal config loading
  and `--show-config` remain provider-call-free.
- Codegeist chat execution is provider-neutral: the chat service is not named after
  Ollama and does not import Ollama-specific Spring AI classes.

## Current Status

- Implemented source now exists under `ai.codegeist.app.chat` with
  `CodegeistChatService`, `CodegeistChatRequest`, `CodegeistChatResponse`,
  `CodegeistChatModel<T extends ProviderConfig>`, and `OllamaChatModel`.
- `app/codegeist/cli/pom.xml` includes `org.springframework.ai:spring-ai-ollama`
  and does not include `spring-ai-starter-model-ollama`.
- `LocalOllamaProviderIT` loads a temporary `codegeist.yml` through
  `CodegeistConfigService.loadConfig(String)` and calls the provider-neutral chat
  service with the selected provider config plus a request carrying the runtime
  model.
- `T006_06` later removed model selection from `ProviderConfig`; the temporary
  `codegeist.yml` now contains only provider access data while the test passes the
  model through `CodegeistChatRequest`.
- `OLLAMA_ENTER=false task ollama-start` starts the local service in this
  environment, and `task test TEST=LocalOllamaProviderIT` passes with the fixed
  runtime model `llama3.2:1b`.
- Focused live verification should report Spring context startup and first chat-call
  timing only; separate model-list preflight timing is no longer part of the test.

## Verification

Expected focused commands from `app/codegeist/cli`:

```bash
OLLAMA_ENTER=false task ollama-start
task test TEST=LocalOllamaProviderIT
```

Then run the relevant broader command after the focused test passes:

```bash
OLLAMA_ENTER=false task ollama-start
task test
git --no-pager diff --check
```

If the focused test uses a different final class name, update this section with the
actual selector before marking the task solved.

## Planning Notes

- Follow `docs/developer/specification/testing-strategy-and-agent-rules.md` for
  first provider workflow constraints.
- Follow `docs/developer/specification/llm-provider-implementation.md` for the
  `CodegeistChatModel<T extends ProviderConfig>` pattern and the provider
  implementation checklist.
- Use an externally managed local Ollama instance for the focused live test. The
  instance and selected model are prerequisites supplied before the test starts.
- Keep assertions narrow enough to survive model variance.
- Spring AI docs for `2.0.0-M6` confirm the Ollama dependency coordinates,
  programmatic `OllamaChatModel` builder, `OllamaApi` base URL builder, and
  `OllamaChatOptions` fields for `model`, `temperature`, and `seed`.
- Do not use Spring AI's Testcontainers-based Ollama integration test as the
  implementation shape for Codegeist. It remains source evidence for Spring AI's
  Ollama API only.
- Prefer direct Codegeist-config-to-Spring-AI object mapping for this task. Do not
  bind all provider config into global `spring.ai.ollama.*` properties as the only
  runtime mechanism.
