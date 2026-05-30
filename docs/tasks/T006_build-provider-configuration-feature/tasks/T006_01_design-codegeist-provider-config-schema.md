# T006_01 Design Codegeist Provider Config Schema

Status: open

Parent: `../task.md`

## Goal

Design the initial `codegeist.yml` provider configuration schema for Codegeist.

The schema must be structurally inspired by OpenCode's `opencode.json` provider
model while using YAML-friendly `kebab-case` keys and Spring/Java implementation
constraints.

## Scope

- Define root-level model selection keys.
- Define provider ids and provider entries under `provider`.
- Define provider-specific `options` and model-specific `options`.
- Define model metadata fields that Codegeist needs before a provider call.
- Define credential reference fields without storing secret values.
- Define `enabled-providers` and `disabled-providers` behavior.
- Define local OpenAI-compatible provider examples.
- Define Spring AI mapping rules where a Codegeist provider maps to one Spring AI
  starter and property namespace.
- Document which parts are schema-only and which are deferred runtime behavior.

## Required OpenCode Checks

Use `/ask-project opencode "How does OpenCode model provider and model config in opencode.json, including credentials, custom OpenAI-compatible providers, and enabled or disabled providers?"` when the analyzed project artifacts are available.

If `/ask-project` cannot run because Repomix or Graphify artifacts are missing or
stale, inspect these source files directly and record that limitation in the task
result:

- `docs/third-party/opencode/source/packages/opencode/src/config/config.ts`
- `docs/third-party/opencode/source/packages/opencode/src/config/provider.ts`
- `docs/third-party/opencode/source/packages/opencode/src/provider/provider.ts`
- `docs/third-party/opencode/source/packages/opencode/src/auth/index.ts`
- `docs/third-party/opencode/source/packages/web/src/content/docs/config.mdx`
- `docs/third-party/opencode/source/packages/web/src/content/docs/providers.mdx`
- `docs/third-party/opencode/source/packages/web/src/content/docs/models.mdx`

## Proposed Shape To Refine

```yaml
model: ollama/llama3.2:1b
small-model: ollama/llama3.2:1b

enabled-providers:
  - ollama

provider:
  ollama:
    type: ollama
    options:
      base-url: http://localhost:11434
    models:
      llama3.2:1b:
        name: Llama 3.2 1B
        capabilities:
          streaming: true
          tool-calling: false
          structured-output: false
          local: true
          network-required: false
        options:
          temperature: 0
```

Credential references should use explicit source fields instead of raw secrets:

```yaml
provider:
  openai:
    type: openai
    credentials:
      api-key-env: OPENAI_API_KEY
    models:
      gpt-4o-mini:
        options:
          temperature: 0
```

## Non-Goals

- Do not implement Java config loading in this child task.
- Do not add Spring AI provider starters.
- Do not create provider accounts or run remote calls.
- Do not define final storage for encrypted or OAuth credentials.
- Do not create placeholder Java types only to mirror the schema.

## Acceptance Criteria

- The task result documents the `codegeist.yml` schema in `kebab-case`.
- The schema includes root model selection, provider entries, provider options,
  model entries, model options, capabilities, and credential references.
- The schema states how `provider-id/model-id` is parsed, including model ids that
  may contain additional `/` characters.
- The schema defines precedence and merge expectations only as far as needed for
  the first implementation slice.
- The schema distinguishes committed project config from local user secrets.
- The schema identifies which OpenCode ideas were adopted, translated, or rejected.
- A later implementer can write focused `codegeist.yml` binding tests from the
  documented examples.

## Verification

Documentation-only verification:

```bash
git --no-pager diff --check
```

## Planning Notes

- Prefer a compact developer specification under `docs/developer/specification/`
  only if the schema is too large to keep in the task file.
- Keep the schema compatible with Spring Boot binding and clear Java property names.
- Do not add broad schema validation hierarchies until `T006_04` needs them.
