# T006_03 Define Provider SpEL Config Evaluation Strategy

Status: open

Parent: `../task.md`

## Goal

Define the smallest useful strategy for evaluating Spring SpEL expressions inside
`codegeist.yml` provider configuration.

This task deliberately narrows the previous credential/account strategy scope.
Codegeist should first prove a simple local-config evaluation model before adding
credential reference schemas, account setup flows, OAuth, auth stores, or model
selection rules.

## Scope

- Define `codegeist.yml` as trusted local input.
- Define Spring SpEL as the only template/expression mechanism for this slice.
- Define that the first SpEL implementation should not add a Codegeist-specific
  sandbox, whitelist, or expression restriction.
- Define a minimal parser pipeline for evaluating scalar YAML values with SpEL.
- Keep provider examples limited to essential provider connection/config values.
- Call out output-redaction and secret-in-git risks that later commands must handle.

## SpEL-Only Decision

Use Spring SpEL directly for config expressions. Do not add a separate Codegeist
credential-reference language such as `api-key-env`, `profile-env`, or
`auth-store-ref` in this task.

After SpEL evaluation, values are ordinary config values. If a SpEL expression
reads an environment variable, that secret value is materialized in memory as part
of the normalized config. That is acceptable for this trusted-local-input slice,
but later output commands and logs must not print those values accidentally.

The first implementation should not restrict SpEL features. It may use standard
Spring SpEL capabilities such as type references, method calls, property access,
operators, and environment access patterns supported by the chosen evaluation
context.

## Parser Direction

Later implementation work should use a small phased parser instead of treating the
raw YAML file as the final config object immediately.

Planned minimal pipeline:

```text
read YAML source
-> evaluate Spring SpEL in scalar values
-> map evaluated values to CodegeistConfig
-> run Bean Validation
-> merge config sources in precedence order
```

This task does not require `vars`, dotenv loading, includes, `.dist` files,
remote includes, or source-generation helpers. Those can be considered only after
the minimal SpEL path is implemented and tested.

## Minimal Example

Illustrative only; these fields do not all exist in `CodegeistConfig` today.

```yaml
provider:
  anthropic:
    type: anthropic
    api-key: "#{T(java.lang.System).getenv('ANTHROPIC_API_KEY')}"

  ollama:
    type: ollama
    base-url: "#{T(java.lang.System).getenv('OLLAMA_BASE_URL') ?: 'http://localhost:11434'}"
```

The example intentionally omits model selection. Model choice belongs to a later
provider/model task and may depend on runtime context, user command selection,
task type, model capabilities, or future policy.

## Safety Notes

- Do not commit real API keys, OAuth tokens, cloud credentials, service account
  files, or generated secret material.
- SpEL may materialize secrets in normalized config values. Treat normalized config
  as sensitive when it contains provider secrets.
- Future `--show-config`, diagnostic, and smoke output must avoid printing
  materialized secret values.
- Parsing config must not by itself call a local or remote provider.
- Remote provider smokes remain a separate `T006_06` concern and must not run only
  because SpEL produced an API key or endpoint value.

## Non-Goals

- Do not define credential reference forms such as `api-key-env`, `profile-env`,
  `credentials-path`, or `auth-store-ref`.
- Do not design provider account setup records.
- Do not design OAuth authorization, refresh, logout, or token storage.
- Do not design a Codegeist credential vault or auth store.
- Do not define model selection fields or model fallback behavior.
- Do not create provider accounts or use remote provider resources.
- Do not add Spring AI provider starters or call provider APIs.
- Do not implement the SpEL parser in this documentation-only child task.

## Acceptance Criteria

- The task defines Spring SpEL as the only expression mechanism for the current
  provider-config evaluation slice.
- The task explicitly rejects a separate Codegeist credential-reference schema for
  this slice.
- The task states that the first SpEL implementation should not add a
  Codegeist-specific sandbox, whitelist, or expression restriction.
- The task defines a minimal read/evaluate/map/validate/merge parser direction.
- The task examples avoid provider model selection.
- The task keeps provider calls, remote smokes, account setup, OAuth, and auth-store
  design out of scope.
- The task records the output-redaction risk caused by materialized SpEL values.

## Verification

Documentation-only verification:

```bash
git --no-pager diff --check
```

## Planning Notes

- `T006_04` should implement the minimal SpEL evaluation pipeline before broader
  source merge features are added.
- `T006_05` should still keep the first real provider call local through Ollama.
- `T006_06` owns provider smoke status, local/remote gating, and remote-call safety.
- Use `../hints/source-evidence-question-catalog.md` when an implementation task
  needs source-backed OpenCode or Spring AI Agent Utils evidence.
- If future work needs account setup, OAuth, or auth-store behavior, create a
  focused child task instead of expanding this SpEL parser strategy retroactively.
