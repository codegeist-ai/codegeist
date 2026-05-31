# T006_03 Define Provider Credential And Account Strategy

Status: open

Parent: `../task.md`

## Goal

Define how Codegeist handles provider accounts, local credentials, environment
references, and remote connection testing without committing secrets or requiring
every developer to own every provider account.

## Scope

- Define which credential forms `codegeist.yml` may reference.
- Define where local secret values may live outside committed project config.
- Define how provider account setup is recorded without storing personal account
  details.
- Define remote test gating and skip behavior.
- Define safety rules for paid providers, free-tier limits, and cloud resources.
- Define how provider test status should be recorded in task files and smoke
  output.
- Define how OAuth-style providers differ from simple API-key providers.
- Define a no-cost integration-test posture: account credentials may be present,
  but remote chat calls still need an explicit no-cost confirmation before they
  run.

## Credential Reference Candidates

Evaluate and refine these shapes:

```yaml
provider:
  anthropic:
    type: anthropic
    credentials:
      api-key-env: ANTHROPIC_API_KEY
```

```yaml
provider:
  google-genai:
    type: google-genai
    credentials:
      api-key-env: GOOGLE_API_KEY
```

```yaml
provider:
  amazon-bedrock:
    type: bedrock-converse
    credentials:
      profile-env: AWS_PROFILE
    options:
      region: us-east-1
```

## Non-Goals

- Do not create real provider accounts in this child task unless the user
  explicitly turns it into account setup work.
- Do not store API keys in `codegeist.yml`, task files, docs, tests, scripts, logs,
  or Git history.
- Do not design a full encrypted credential vault before a focused runtime task
  needs it.
- Do not require remote credentials for the ordinary Maven test suite.
- Do not treat an available API key as permission to spend credits or money.
- Do not run a remote chat completion in default integration tests unless the test
  mode and provider config both declare that the check is intended to be no-cost.

## Acceptance Criteria

- The strategy defines allowed credential reference forms, for example
  `api-key-env`, cloud profile env vars, service account file references, or future
  local auth-store references.
- The strategy defines disallowed secret storage locations.
- The strategy defines when a provider connection test reports `skipped` instead
  of failing.
- The strategy defines how to handle providers that require billing, regional model
  access, deployment names, cloud projects, or service accounts.
- The strategy identifies which provider accounts should be created first after the
  local Ollama path is working.
- The strategy is compatible with `T006_06` connection smoke output.
- The strategy defines how `config`, `local`, and `remote-free` integration-test
  levels are selected and how potentially billable providers are reported as
  `blocked` or `skipped` instead of being called accidentally.

## Verification

Documentation-only verification:

```bash
git --no-pager diff --check
```

## Planning Notes

- Keep account setup instructions generic and security-conscious.
- Prefer one provider account per provider-specific child task instead of a large
  account-creation batch.
- If a provider needs paid resources, require an explicit user decision before
  creating or using them.
- Prefer a two-gate rule for remote provider tests: credentials must resolve, and
  the developer must explicitly opt into a no-cost remote test mode for the
  selected provider. Missing either gate should skip or block instead of calling
  the provider.
