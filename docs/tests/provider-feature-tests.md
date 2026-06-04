# Provider Feature Tests

Current provider feature and provider test guidance for Codegeist.

## Purpose

Provider tests prove three different contracts without mixing their risk levels:

- Config-only provider checks prove `codegeist.yml` loading, type dispatch, and
  provider-specific validation without any provider call.
- Local provider checks prove Codegeist can call an externally managed local
  provider, currently Ollama, without remote credentials or billing risk.
- Hosted provider checks prove remote provider features only after an explicit
  category selection and account or cost decision.

All provider verification runs through `task test` from `app/codegeist/cli`. There
is no separate provider-specific Taskfile entrypoint, no JUnit provider tag, and no
Maven group exclusion. Method-level provider categories decide whether provider-call
methods run or are skipped.

## Implemented Feature Shape

The current provider implementation is intentionally small:

- `ProviderConfig` stores access data only, such as `name`, `base-url`, and
  provider-specific credential fields.
- Provider config does not store model names, generation options, enablement,
  provider routing, or completion paths.
- YAML `provider.<id>.type` is a dispatch-only input. Runtime/output type is
  derived from the concrete class `@Provider` annotation.
- `ProviderConfigJacksonConverter` uses an explicit Java registry to map registered
  `@Provider` config classes. It maps the raw YAML object into the matching class
  without branching on JVM versus native-image runtime.
- `OllamaProviderConfig` creates the implemented local Ollama chat model.
- `OpenAiProviderConfig` is currently config-only for runtime chat; the OpenAI
  provider feature tests call selected HTTP endpoints directly.
- `CodegeistChatRequest` carries the runtime model and prompt. The selected
  provider is passed separately to `CodegeistChatService`.

Implemented provider config types:

| Type | Config class | Runtime status |
| --- | --- | --- |
| `ollama` | `OllamaProviderConfig` | Local chat model implemented. |
| `openai` | `OpenAiProviderConfig` | Config binding implemented; provider feature tests cover selected remote endpoints. |

## Category Contract

`CODEGEIST_TEST_PROVIDER_CATEGORY` selects the highest provider-call category that
may run.

Accepted values are exact and case-sensitive:

| Value | Meaning | Runs provider calls? |
| --- | --- | --- |
| `none` | Default when the environment variable is missing. Runs no annotated provider-call methods. | No. |
| `local` | Runs local provider-call methods and all lower categories. | Local only. |
| `remote_free` | Runs hosted no-cost-selected methods plus local methods. | Local and selected hosted no-cost calls. |
| `remote_paid` | Runs all provider-call methods. This is the explicit cost and rate-limit opt-in. | Local, `remote_free`, and paid-capable calls. |

There is no normalization. Uppercase values, hyphenated remote category names,
space-separated values, or blank strings are invalid. This is intentional so
automation uses one visible contract and does not accidentally opt into provider
calls through loose parsing.

The category hierarchy is implemented by enum order in `ProviderTestCategory`:

```text
none < local < remote_free < remote_paid
```

`ProviderTestExtension` reads the environment variable with a missing-value default
of `none`. It skips any class or method annotated with `@ProviderCategory` when the
active category does not allow the required category.

Config-only checks stay unannotated. They should not need a provider category
because they do not call local services or hosted APIs.

## Test Classes

| Test class | Default `none` behavior | Provider-call categories |
| --- | --- | --- |
| `OpenAiProviderTest` | Runs config binding and missing-API-key validation. | `remote_free` for model listing; `remote_paid` for image generation, text-to-speech, and speech-to-text. |
| `OllamaProviderTest` | Runs config binding and missing-base-url validation. | `local` for one local Ollama chat call. |
| `AskCommandsTest` | Skips the whole class. | `local` at class level for one Spring Boot command test backed by local Ollama. |
| `LocalOllamaProviderIT` | Not included by the default Surefire test name patterns; run only by explicit selector. | No category gate; it is an explicit live integration test. |

## Why These Tests Exist

Provider tests are split by risk because provider work has different failure modes:

| Risk | Test policy |
| --- | --- |
| Config mapping can break ordinary startup or `--show-config`. | Config-only checks run by default under `none`. |
| Local Ollama may be unavailable, slow, or missing `llama3.2:1b`. | Local calls require `CODEGEIST_TEST_PROVIDER_CATEGORY=local` or higher. |
| Hosted APIs can consume quota, require account setup, or bill. | Hosted calls require `remote_free` or `remote_paid`; API-key presence alone is never enough. |
| Paid-capable endpoints can create direct cost. | Paid-capable calls require `CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid`. |

This keeps broad development and CI-style verification safe while still making live
provider checks easy to run when their prerequisites are intentionally available.

## When To Run Which Command

Run commands from `app/codegeist/cli`.

### Ordinary Code Or Config Changes

Use this for most implementation work and final JVM verification when live provider
calls are not part of the task:

```bash
task test
```

Why: this runs provider config checks and skips annotated provider-call methods
because the default category is `none`. It does not require Ollama, hosted
credentials, audio fixtures, or paid account confirmation.

### Provider Config Parser Or Validation Changes

Use focused config checks first:

```bash
task test TEST=CodegeistProviderConfigTest,CodegeistConfigServiceTest,CodegeistConfigSpelEvaluationTest
CODEGEIST_TEST_PROVIDER_CATEGORY=none task test TEST=OpenAiProviderTest,OllamaProviderTest
```

Why: this proves typed provider dispatch, direct YAML loading, SpEL preprocessing,
provider-specific validation, and provider test config rows without any provider
calls.

### Local Ollama Provider Feature Work

Use this when the task changes `OllamaProviderConfig`, `OllamaChatModel`,
`CodegeistChatService`, `CodegeistChatRequest`, or local Ollama provider behavior:

```bash
OLLAMA_ENTER=false task ollama-start
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=OllamaProviderTest
```

Why: `ollama-start` starts or reuses the local `codegeist-ollama` container and
ensures the selected model is present, while the focused test proves config loading
plus the provider feature chat method. The Java test uses fixed values and does not
pull models itself:

| Setting | Value |
| --- | --- |
| Ollama base URL | `http://localhost:11434` |
| Ollama model | `llama3.2:1b` |

Use this broader local check only when local provider behavior should be included
in the whole JVM suite:

```bash
OLLAMA_ENTER=false task ollama-start
CODEGEIST_TEST_PROVIDER_CATEGORY=local task test
```

Why: `local` also allows every lower category, so it runs unannotated config checks
and local provider-call methods while still skipping hosted remote methods.

### Provider-Neutral Local Integration Seam

Use this when the task specifically changes the provider-neutral chat seam or Spring
application context path used by local provider calls:

```bash
OLLAMA_ENTER=false task ollama-start
task test TEST=LocalOllamaProviderIT
```

Why: `LocalOllamaProviderIT` starts `CodegeistApplication` through a manual Spring
application builder, loads a temporary `codegeist.yml`, and calls
`CodegeistChatService` with a selected `ProviderConfig` plus runtime model and
prompt. It is intentionally selector-only and not part of broad `task test`.

### Hosted Remote-Free Provider Checks

Use this only after recording or confirming that the selected account, endpoint,
model, and route are no-cost for the current run:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_free task test TEST=OpenAiProviderTest#testListModels
```

Why: `remote_free` is for explicitly selected no-cost hosted calls. It may still
require `CODEGEIST_TEST_OPENAI_APIKEY`, and missing credentials cause the selected
method to be skipped through JUnit assumptions. Do not treat an API key as
permission to run this category; the no-cost decision must be explicit.

### Hosted Paid-Capable Provider Checks

Use this only after an explicit cost and rate-limit decision for the selected
account and endpoint:

```bash
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid task test TEST=OpenAiProviderTest#testImageGeneration
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid task test TEST=OpenAiProviderTest#testTextToSpeech
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid task test TEST=OpenAiProviderTest#testSpeechToText
```

Why: `remote_paid` allows paid-capable calls. Prefer method selectors so the run is
limited to the feature being verified. Running the whole class with `remote_paid`
can call all OpenAI provider feature methods whose assumptions are satisfied.

### Full OpenAI Remote-Paid Verification

Use this when the current account is intentionally allowed to spend quota on every
implemented OpenAI provider feature test:

```bash
espeak-ng -v en-us -s 135 -w target/codegeist-speech-en.wav "Hello world test."

CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_AUDIO=target/codegeist-speech-en.wav \
CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_EXPECTED=test \
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid \
task test TEST=OpenAiProviderTest
```

Why: this runs all six `OpenAiProviderTest` methods: two unannotated config checks,
one `remote_free` model-list check, and three `remote_paid` feature checks. The
speech-to-text test needs a local audio fixture; the generated English `espeak-ng`
fixture is short, cheap to transcribe, and has a stable expected word.

This command is intentionally not the default verification path. It can spend
hosted-provider quota and may fail when the account has no available quota, a hard
billing limit is reached, organization verification is missing for image models, or
the audio fixture is absent.

The last successful full run used the built-in low-cost defaults and this fixture:

```text
CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid
CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_AUDIO=target/codegeist-speech-en.wav
CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_EXPECTED=test
```

Result:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

## Hosted OpenAI Test Inputs

OpenAI provider feature tests use environment variables only for hosted credentials,
fixtures, and remote-provider routing. Local Ollama tests do not use environment
overrides for base URL or model.

| Environment variable | Used by | Meaning |
| --- | --- | --- |
| `CODEGEIST_TEST_OPENAI_APIKEY` | `remote_free`, `remote_paid` | API key for explicitly selected OpenAI checks. |
| `CODEGEIST_TEST_OPENAI_BASE_URL` | `remote_free`, `remote_paid` | OpenAI-compatible base URL; defaults to `https://api.openai.com`. |
| `CODEGEIST_TEST_OPENAI_IMAGE_MODEL` | `remote_paid` | Image model; defaults to `gpt-image-1-mini`. |
| `CODEGEIST_TEST_OPENAI_IMAGE_SIZE` | `remote_paid` | Image size; defaults to `1024x1024`. |
| `CODEGEIST_TEST_OPENAI_SPEECH_MODEL` | `remote_paid` | Text-to-speech model; defaults to `tts-1`. |
| `CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_MODEL` | `remote_paid` | Speech-to-text model; defaults to `gpt-4o-mini-transcribe`. |
| `CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_AUDIO` | `remote_paid` | Local audio fixture path for speech-to-text. |
| `CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_EXPECTED` | `remote_paid` | Expected text fragment; defaults to `codegeist`. |

The default OpenAI feature models are deliberately low-cost working defaults for
the current test suite:

| Feature | Default model | Reason |
| --- | --- | --- |
| Image generation | `gpt-image-1-mini` | Lower-cost image model for a single small generated image. |
| Text-to-speech | `tts-1` | Lower-cost speech model that returns enough audio for the contract check. |
| Speech-to-text | `gpt-4o-mini-transcribe` | Lower-cost transcription model with reliable output for the short fixture. |

Override these only when the task specifically needs a different model behavior.
The override variables are useful for compatibility checks, not for ordinary broad
verification.

## Speech-To-Text Audio Fixture

The speech-to-text feature test requires a local audio file because the repository
does not commit generated prompt, completion, image, or audio artifacts.

Use `espeak-ng` to create a deterministic, cheap fixture:

```bash
espeak-ng -v en-us -s 135 -w target/codegeist-speech-en.wav "Hello world test."
```

Install `espeak-ng` when the tool is missing:

```bash
sudo apt-get update
sudo apt-get install -y espeak-ng
```

Expected fixture shape:

```text
target/codegeist-speech-en.wav: RIFF WAVE audio, Microsoft PCM, 16 bit, mono 22050 Hz
```

Use `CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_EXPECTED=test` with this fixture. A
German fixture such as `Dies ist ein Codegeist Test Satz.` can be misrecognized by
the transcription model and is less stable for the current assertion.

## Expected Skip Behavior

Provider tests intentionally produce skipped methods in safe runs:

| Command | Expected provider-call behavior |
| --- | --- |
| `task test` | Annotated provider-call methods are skipped because the category is `none`. |
| `CODEGEIST_TEST_PROVIDER_CATEGORY=none task test TEST=OpenAiProviderTest,OllamaProviderTest` | Same as broad default, but limited to provider feature classes. |
| `CODEGEIST_TEST_PROVIDER_CATEGORY=local task test TEST=OllamaProviderTest` | Ollama config checks and local chat run; no methods should be skipped in this class when Ollama is ready. |
| `CODEGEIST_TEST_PROVIDER_CATEGORY=remote_free task test TEST=OpenAiProviderTest#testListModels` | The selected method runs only when required hosted inputs are present; otherwise it is skipped by assumptions. |
| `CODEGEIST_TEST_PROVIDER_CATEGORY=remote_paid task test TEST=OpenAiProviderTest#testSpeechToText` | The selected paid-capable method runs only when credentials and fixture inputs are present; otherwise it is skipped by assumptions. |

Skipped provider-call methods under `none` are not failures. They are the safety
contract that keeps ordinary verification free from local-provider prerequisites and
hosted-provider costs.

## Remote Failure Modes

Remote paid checks can fail for account or service reasons even when the Java code is
correct. Treat these failures as provider/account blockers unless the response body
points at a request-shape regression.

| Symptom | Meaning | Action |
| --- | --- | --- |
| `billing_hard_limit_reached` | The account or project hit a configured billing hard limit. | Add budget or lower the limit before re-running `remote_paid`. |
| `insufficient_quota` | The account has no usable quota for the selected endpoint or model. | Add credits, wait for quota refresh, or select an allowed model. |
| `Missing provider test environment variable: CODEGEIST_TEST_OPENAI_APIKEY` | Hosted method selected without an API key. | Set the test-specific API-key env var only when the remote call is intended. |
| `Missing provider test environment variable: CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_AUDIO` | Speech-to-text selected without a local fixture. | Generate the `espeak-ng` fixture or point to another local audio file. |
| Assertion text mismatch in `testSpeechToText` | The model transcribed the fixture differently than expected. | Use the documented English fixture or adjust `CODEGEIST_TEST_OPENAI_SPEECH_TO_TEXT_EXPECTED` for the selected audio. |

## Adding A Provider Feature Test

Use this checklist when adding future provider feature tests:

- Add provider-specific config checks without `@ProviderCategory` when they do not
  call a provider.
- Put provider-call checks in the provider's own test class.
- Annotate every provider-call method, or a whole provider-call test class, with
  exactly one `@ProviderCategory` value.
- Use `local` only for local services with fixed, documented prerequisites.
- Use `remote_free` only after a no-cost account and route decision exists.
- Use `remote_paid` for any endpoint that can consume paid quota, generate media,
  invoke speech APIs, or otherwise create billing risk.
- Keep local provider test values fixed unless a future task explicitly changes the
  contract.
- Use JUnit assumptions for missing remote credentials or fixtures, not API-key
  presence as an automatic opt-in.
- Keep assertions focused on stable observable behavior, not full natural-language
  completions.

## Related Files

| File | Role |
| --- | --- |
| `app/codegeist/cli/src/test/java/ai/codegeist/app/provider/ProviderCategory.java` | Class or method annotation for provider-call categories. |
| `app/codegeist/cli/src/test/java/ai/codegeist/app/provider/ProviderTestCategory.java` | Exact category enum and hierarchy. |
| `app/codegeist/cli/src/test/java/ai/codegeist/app/provider/ProviderTestExtension.java` | JUnit execution condition that reads `CODEGEIST_TEST_PROVIDER_CATEGORY`. |
| `app/codegeist/cli/src/test/java/ai/codegeist/app/provider/OllamaProviderTest.java` | Config checks and local Ollama provider feature test. |
| `app/codegeist/cli/src/test/java/ai/codegeist/app/provider/OpenAiProviderTest.java` | Config checks and hosted OpenAI provider feature tests. |
| `app/codegeist/cli/src/test/java/ai/codegeist/app/provider/AskCommandsTest.java` | Spring Boot command test gated as a local provider-call class. |
| `app/codegeist/cli/src/test/java/ai/codegeist/app/chat/LocalOllamaProviderIT.java` | Explicit local provider-neutral integration seam test. |
| `docs/developer/architecture/provider-configuration.md` | Current-state provider config architecture. |
| `docs/developer/specification/llm-provider-implementation.md` | Provider runtime and future provider implementation guidance. |
