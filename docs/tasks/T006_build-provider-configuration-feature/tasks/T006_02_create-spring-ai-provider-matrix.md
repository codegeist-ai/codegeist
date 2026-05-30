# T006_02 Create Spring AI Provider Matrix

Status: open

Parent: `../task.md`

## Goal

Create a Spring AI provider matrix for the Codegeist provider feature.

The matrix must identify which chat model providers are supported out of the box by
the pinned Spring AI baseline and what each provider needs before Codegeist can run
connection checks.

## Scope

- Use Spring AI `2.0.0-M6`, because that is the version pinned in
  `app/codegeist/cli/pom.xml`.
- List chat model providers, not embeddings, image, audio, vector stores, or
  moderation unless they are relevant as a capability note.
- Record starter artifact ids and Spring property prefixes where available.
- Record account and credential requirements.
- Record whether each provider can be tested locally, requires a cloud account,
  or can be skipped without credentials.
- Record Spring AI capability evidence for streaming, tool calling, multimodality,
  JSON or structured output, local deployment, and OpenAI API compatibility.
- Map each Spring AI provider to a tentative Codegeist `provider.<id>.type` value.

## Initial Provider Set

Start from the Spring AI `2.0.0-M6` chat provider documentation and source tree.
Known candidates include:

- Amazon Bedrock Converse
- Anthropic
- Azure OpenAI
- DeepSeek
- Docker Model Runner
- Google GenAI
- Groq
- Mistral AI
- MiniMax
- Moonshot AI
- NVIDIA
- Ollama
- OpenAI
- Perplexity AI
- QianFan

Also check whether Vertex AI Gemini or other provider pages are present in the
exact pinned version before marking them supported for Codegeist.

## Matrix Columns

Use these columns unless implementation evidence shows a better compact shape:

| Column | Meaning |
| --- | --- |
| `codegeist-type` | Proposed `provider.<id>.type` value in `codegeist.yml`. |
| `spring-provider` | Spring AI provider name. |
| `starter` | Maven starter artifact id. |
| `property-prefix` | Spring Boot property prefix used by Spring AI. |
| `default-model` | Documented default or recommended smoke model when known. |
| `credential-source` | Env var, API key, OAuth, cloud profile, service account, or none. |
| `account-required` | Whether a provider account, billing setup, or cloud project is needed. |
| `local-test` | Whether it can be tested without remote provider credentials. |
| `streaming` | Supported by Spring AI provider docs. |
| `tool-calling` | Supported by Spring AI provider docs. |
| `structured-output` | Built-in JSON or structured output support. |
| `openai-compatible` | Whether the provider uses or supports OpenAI-compatible APIs. |
| `test-status` | `not-started`, `blocked`, `skipped`, `passed`, or `failed`. |
| `notes` | Sharp edges such as cloud region, deployment names, model access, or cost. |

## Non-Goals

- Do not implement provider calls in this child task.
- Do not add provider dependencies to `pom.xml` in this child task.
- Do not create provider accounts yet.
- Do not treat non-chat model support as ready for Codegeist chat workflows.
- Do not mark a provider verified only because the documentation says it exists.

## Acceptance Criteria

- The matrix is tied explicitly to Spring AI `2.0.0-M6`.
- Every listed provider has a source link or source-file citation.
- The matrix distinguishes documented support from locally verified support.
- The matrix identifies which providers require account creation and which need
  billing, cloud projects, model deployment, or region configuration.
- The matrix identifies the first local provider candidate for implementation.
- The matrix can drive later provider-specific child tasks without redoing the
  basic discovery work.

## Verification

Documentation-only verification:

```bash
git --no-pager diff --check
```

## Planning Notes

- Prefer official Spring AI reference docs and the `spring-projects/spring-ai`
  `v2.0.0-M6` source tree over blog posts or examples.
- If a newer Spring AI version documents a provider that `2.0.0-M6` does not ship,
  list it only as a future upgrade note.
