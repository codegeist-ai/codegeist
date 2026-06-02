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
- Use `CODEGEIST_TEST_OLLAMA_BASE_URL` to point the focused test at the ready
  Ollama instance, defaulting to `http://localhost:11434` when the variable is not
  set.
- Use `CODEGEIST_TEST_OLLAMA_MODEL` to name the already downloaded local model,
  defaulting to `llama3.2:1b`. The model must be present before the focused test
  starts; the test should not pull, download, or create it.
- Before running `task test` for this task, run `OLLAMA_ENTER=false task
  ollama-start` from `app/codegeist/cli` so the externally managed local Ollama
  service is ready in non-interactive automation.
- Configure deterministic options from `codegeist.yml`: `temperature: 0` and fixed
  `seed: 7` or another explicit integer. Spring AI `2.0.0-M6`
  `OllamaChatOptions` supports both `temperature(...)` and `seed(...)`.
- Use a narrow prompt and stable assertion.
- Report external Ollama readiness/model-availability timing and first chat-call
  timing separately from ordinary Spring test timing.
- Update current-state architecture for implemented provider behavior and tests.

## Implementation Notes

- Implement the minimal provider-neutral chat seam from
  `llm-provider-implementation.md`: `CodegeistChatService`, `CodegeistChatRequest`,
  `CodegeistChatResponse`, `ChatModelFactory`, `ProviderChatModelFactory`, and
  `OllamaChatModelFactory` when the focused test needs those classes.
- Make `ProviderChatModelFactory` generic as
  `ProviderChatModelFactory<T extends ProviderConfig>`. `ChatModelFactory` should
  receive the selected raw `ProviderConfig`, find the matching provider factory,
  and delegate through a common `createFrom(ProviderConfig)` method. Concrete
  factories such as `OllamaChatModelFactory` should implement
  `ProviderChatModelFactory<OllamaProviderConfig>` and receive
  `OllamaProviderConfig` directly in `create(...)`.
- Keep provider-specific imports isolated in `OllamaChatModelFactory`. The generic
  chat service and model factory should depend only on `ProviderConfig` and Spring
  AI's provider-neutral `ChatModel`/`Prompt` APIs.
- `ChatModelFactory` should accept an already normalized `ProviderConfig` and create
  the selected provider's `ChatModel` lazily inside the call path. Loading config,
  running `--show-config`, or starting the Spring context must not pull models,
  create every configured provider client, or call providers.
- Do not introduce a public provider plugin API, model catalog, provider ranking,
  fallback policy, smoke command, CLI command, or cross-provider status model in
  this task.
- Programmatic Spring AI `2.0.0-M6` construction should use the documented
  `OllamaApi.builder().baseUrl(...).build()` and
  `OllamaChatModel.builder().ollamaApi(...).defaultOptions(...).build()` path.
  Build default options with `OllamaChatOptions.builder().model(...)
  .temperature(...).seed(...).build()`.
- The focused test may perform a cheap readiness check against the configured
  Ollama base URL and may verify the selected model is listed locally. It must not
  pull, prepare, download, or delete models.
- The focused test should load provider config from a temp `codegeist.yml` through
  `CodegeistConfigService.loadConfig(String)` so the real T006 loader, SpEL phase,
  provider dispatch, and Bean Validation are exercised before the provider call.
- Use a stable one-turn prompt such as `Respond with exactly the lowercase word
  codegeist.` Assert on a narrow, case-insensitive response containing `codegeist`
  rather than asserting a full natural-language answer.
- Use `task test` for verification, never a direct `mvn test` command in this
  task. When a focused selector is needed, use the Taskfile selector form such as
  `task test TEST=LocalOllamaProviderIT`.
- The focused test should print or otherwise report three timings in its Maven
  output: Spring context startup, Ollama readiness/model-availability check, and
  the first chat call. Approximate durations are sufficient; do not hide live
  provider latency inside an ordinary-looking unit test.

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
- The task reports Spring context startup timing, Ollama readiness/model-availability
  timing, and first chat-call timing.
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
  `ChatModelFactory`, generic `ProviderChatModelFactory<T extends ProviderConfig>`,
  and `OllamaChatModelFactory`.
- `app/codegeist/cli/pom.xml` includes `org.springframework.ai:spring-ai-ollama`
  and does not include `spring-ai-starter-model-ollama`.
- `LocalOllamaProviderIT` loads a temporary `codegeist.yml` through
  `CodegeistConfigService.loadConfig(String)`, verifies the selected model is
  already listed by local Ollama, and calls the provider-neutral chat service.
- `OLLAMA_ENTER=false task ollama-start` starts the local service in this
  environment, `llama3.2:1b` is listed locally, and
  `task test TEST=LocalOllamaProviderIT` passes.
- Focused live verification reported Spring context startup `PT0.740494333S`,
  Ollama readiness/model availability `PT0.326404818S`, and first chat call
  `PT1.687754898S`.

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
  Strategy plus Factory pattern and the provider implementation checklist.
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
