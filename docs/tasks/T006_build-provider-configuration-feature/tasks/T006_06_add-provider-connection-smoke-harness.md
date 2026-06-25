# T006_06 Add Provider Feature Tests And Ask Smoke

Status: solved

Parent: `../task.md`

## Goal

Add configurable provider-specific feature tests and an end-to-end one-shot `ask`
smoke path for Codegeist.

The tests must run at method level because one provider can expose features with
different cost and safety profiles. The default `none` category runs config-only
checks without local or hosted provider calls. Local Ollama calls require
`CODEGEIST_TEST_PROVIDER_CATEGORY=local`. OpenAI image generation or speech-to-text
can require a paid remote request or consume request limits, so hosted calls stay
behind explicit remote categories.

The `ask` command provides the first CLI path that uses a configured provider. It
uses the active global config, selects the first configured provider, uses the
selected provider config's default runtime model, and is covered by real
Ollama-backed Linux and Windows smokes. The current Ollama provider default is
`llama3.2:1b`.

## Scope

- Remove `model`, generic `options`, `enabled`, and `completions-path` from
  `ProviderConfig`; provider config describes connection and credential data only.
- Pass model names at runtime through the chat/test request path because each
  coding agent, session, command, or provider-feature test may select a different
  model.
- Run provider tests through `task test`; provider categories are the only provider
  call gate and may be applied at method or class level.
- Add one provider test class per implemented provider family, starting with
  `OpenAiProviderTest` and `OllamaProviderTest`.
- Leave config-only checks unannotated so they run without provider calls.
- Add `ask` as a one-shot Spring Shell command with a single positional prompt
  parameter, no command-specific config flag, no provider flag, and no model flag.
- Let `ask` use `ProviderConfig.defaultModel()` instead of a command-owned hardcoded
  model default.
- Use the global `-Dcodegeist.config=<path>` policy for `ask` and `--show-config`.
- Extend Linux and Windows QEMU native smokes to write a temporary
  Ollama config and run real `ask` calls.
- Keep Windows QEMU on host Ollama through `http://10.0.2.2:11434`; do not start
  or install Ollama in the Windows guest.
- Keep provider dispatch free of JVM/native runtime branching and register provider
  config POJOs in `ProvidersRootElement` plus native reflection metadata
  for Jackson binding.
- Classify every provider-call test method by category:
  - `local` for local providers such as Ollama.
  - `remote_free` for explicitly allowed no-cost remote feature calls.
  - `remote_paid` for remote feature calls that can cost money.
- Treat `CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid` as the explicit cost and
  rate-limit opt-in. No separate paid confirmation flag exists.
- Keep provider config output and ordinary test diagnostics free of credential
  values.

## Category Contract

Provider tests read one environment variable:

| Environment variable | Meaning |
| --- | --- |
| `CODEGEIST_TEST_PROVIDER_CATEGORY` | Highest provider category to run. Defaults to `none`; accepts `none`, `local`, `remote_free`, or `remote_paid`. |

Hosted provider live-test settings use environment variables because they need
external credentials, fixture paths, or explicit safety gates. Local Ollama tests
use fixed test values instead: base URL `http://localhost:11434` and model
`llama3.2:1b`.

| Environment variable | Meaning |
| --- | --- |
| `CODEGEIST_TEST_OPENAI_APIKEY` | OpenAI API key used only by explicitly selected remote OpenAI checks. |
| `CODEGEIST_TEST_OPENAI_BASE_URL` | OpenAI-compatible base URL; defaults to `https://api.openai.com`. |
| `CODEGEIST_TEST_OPENAI_IMAGE_MODEL` | Image generation model for the paid-capable OpenAI check; defaults to `gpt-image-1-mini`. |
| `CODEGEIST_TEST_OPENAI_IMAGE_SIZE` | Image size for the paid-capable OpenAI image check. |
| `CODEGEIST_TEST_OPENAI_SPEECH_MODEL` | Text-to-speech model for the paid-capable OpenAI check; defaults to `tts-1`. |
| `CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_MODEL` | Speech-to-text model for the paid-capable OpenAI check; defaults to `gpt-4o-mini-transcribe`. |
| `CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_EXPECTED` | Expected text fragment in the OpenAI speech-to-text response. |

## ProviderConfig Decision

`ProviderConfig` is free of stored YAML model fields, generic options, enabled
flags, and completions paths after this task. The provider map stores access data:

```yaml
provider:
  openai:
    type: openai
    api-key: "#{T(java.lang.System).getenv('CODEGEIST_TEST_OPENAI_APIKEY')}"

  ollama:
    type: ollama
    base-url: http://localhost:11434
```

Runtime model selection and generation options belong to commands, requests, and
tests. Each concrete provider config also owns a `defaultModel()` fallback for
callers that intentionally do not expose a model flag. Local Ollama provider tests
use the fixed runtime model `llama3.2:1b`; hosted provider tests may use environment
variables for external credentials, fixtures, and explicit remote-provider safety
gates.

## Acceptance Criteria

- `ProviderConfig` no longer contains `model`, `options`, `enabled`, or
  `completions-path` fields.
- The existing Ollama chat path receives the selected model through
  `CodegeistChatRequest` instead of provider config, and the request does not carry
  the selected provider.
- `task test` runs provider feature tests, with non-config methods gated by
  `CODEGEIST_TEST_PROVIDER_CATEGORY`.
- `OpenAiProviderTest` contains unannotated config checks and method-level
  `remote_free` and `remote_paid` checks.
- `OllamaProviderTest` contains unannotated config checks and method-level `local`
  checks.
- Local provider methods run only when `CODEGEIST_TEST_PROVIDER_CATEGORY=local` or a
  higher category is selected.
- `remote_paid` methods are skipped unless
  `CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid` is set.
- Config-only runs can leave `CODEGEIST_TEST_PROVIDER_CATEGORY` unset or set it to
  `none` to avoid local provider calls.
- `ask` uses the first provider in the active config and that provider config's
  default runtime model; it does not add `--provider`, `--model`, or `--config`
  options.
- Linux and Windows QEMU native smokes include stable `ask` duration
  labels and pass against real host Ollama.
- Provider config dispatch does not branch on JVM/native runtime state.

## Verification

Expected focused commands from `app/codegeist/cli`:

```bash
task test TEST=CodegeistProviderConfigTest,CodegeistConfigServiceTest,CodegeistConfigSpelEvaluationTest
CODEGEIST_TEST_PROVIDER_CATEGORY=none task test TEST=OpenAiProviderTest,OllamaProviderTest
```

For local Ollama provider execution:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=OllamaProviderTest
```

For local release runs that intentionally allow paid OpenAI feature checks:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid \
task test TEST=OpenAiProviderTest
```

For end-to-end `ask` smoke coverage:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=AskCommandsTest,OllamaProviderTest
task native-smoke
task qemu-windows-smoke
```

Then run:

```bash
task test
git --no-pager diff --check
```

## Verification Result

- `task test TEST=CodegeistProviderConfigTest,CodegeistConfigServiceTest,CodegeistConfigSpelEvaluationTest` passed.
- `CODEGEIST_TEST_PROVIDER_CATEGORY=none task test TEST=OpenAiProviderTest,OllamaProviderTest`
  passed with only unannotated config checks running after the Env-only gate cleanup.
- `task test TEST=LocalOllamaProviderIT` passed after removing provider options.
  The live test no longer runs a separate model-list preflight.
- `CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=OllamaProviderTest` passed
  with `task test` starting Ollama first and the local chat method running after the
  Env-only gate cleanup.
- `CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=AskCommandsTest,OllamaProviderTest`
  passed with the real local Ollama command test and provider test running.
- `task test` passed with provider feature tests included and the default `none`
  category active after the Env-only gate cleanup.
- `task native-smoke` passed after explicit provider registry dispatch replaced
  runtime scanning. The run included
  `Duration: linux native ask smoke: 3.567s`.
- `task qemu-windows-smoke` passed after the same provider registry fix. The run
  included `Duration: windows jar ask smoke: 9.049s` and
  `Duration: windows native ask smoke: 1.850s`.
- The full OpenAI `remote_paid` run is explicit opt-in and runs all six provider
  feature tests when selected:

  ```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid \
task test TEST=OpenAiProviderTest
  ```

  A historical run with the low-cost defaults `gpt-image-1-mini`, `tts-1`, and
  `gpt-4o-mini-transcribe` was blocked by the configured API key returning
  `401 invalid_api_key`. A later `AskCommandsOpenAiToolSmokeIT` remote-paid run
  proved OpenAI chat tool calling with `codegeist_write`; rerun the full
  `OpenAiProviderTest` class separately when the current account is intentionally
  allowed to spend quota on every implemented OpenAI provider feature test. The
  speech-to-text test generates its default audio fixture with `espeak-ng` under
  `target/provider-tests/` when needed.

## Planning Notes

- Do not add provider models, generation options, enablement, or completion path
  routing back into `ProviderConfig` until a focused runtime task needs them; they
  vary per coding agent, command, provider feature, or provider-specific route.
- Add future provider feature methods in the provider's own test class and guard
  each method with the right category.
- API-key presence is never permission to make a remote call.
- Remote paid feature tests should keep prompts and fixtures small, but token or
  output limits do not make a paid feature free.
- Speech-to-text uses a caller-provided local audio fixture path so the repository
  does not commit generated prompt/completion/audio artifacts.
