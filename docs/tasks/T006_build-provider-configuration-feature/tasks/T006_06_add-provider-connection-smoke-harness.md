# T006_06 Add Provider Connection Smoke Harness

Status: open

Parent: `../task.md`

## Goal

Add a repeatable provider connection smoke harness for Codegeist.

The harness should let local and remote providers be checked one by one while
keeping credentials optional, preventing accidental costs, and reporting precise
status.

## Scope

- Define a smoke entrypoint for checking configured providers.
- Use evaluated `codegeist.yml` provider entries.
- Support an allowlist so one provider can be checked without touching every
  configured provider.
- Report `passed`, `skipped`, or `failed` for each provider.
- Treat missing credentials, missing account setup, missing cloud project, missing
  model deployment, or missing local service as `skipped` when the provider is not
  explicitly required.
- Treat a provider that would require a potentially billable remote chat call as
  `blocked` unless the caller explicitly selected a no-cost remote test mode and
  the selected account and route are confirmed as no-cost.
- Treat an explicitly required provider as failed when prerequisites are absent.
- Record enough timing to see slow startup, network calls, model pulls, or cloud
  latency.
- Keep secret values redacted from output and logs.
- Support integration-test levels that can exercise many providers safely:
  `config` for no-network provider configuration checks, `local` for real local
  calls, and `remote-free` for explicitly confirmed no-cost remote calls.

## Non-Goals

- Do not create accounts inside the smoke harness.
- Do not run all paid providers by default.
- Do not make Maven's ordinary test lifecycle depend on remote provider accounts.
- Do not make API-key presence sufficient to trigger a hosted provider call.
- Do not add a `remote-paid` path to the default integration test command.
- Do not store smoke output containing prompts, completions, or credentials in
  committed files.
- Do not implement provider-specific retries beyond what the first smoke needs.

## Acceptance Criteria

- The harness can check at least the already implemented local Ollama provider.
- The harness can skip a remote provider with a precise reason when required
  evaluated config values are missing.
- The harness redacts sensitive config values from output.
- The harness output is easy to scan and includes provider id, status, duration,
  and blocker when skipped or failed.
- The harness can run a default no-cost integration mode that checks all configured
  provider definitions without calling hosted providers.
- The harness can run a local integration mode that performs real no-cost calls for
  local providers such as Ollama.
- The harness requires an explicit `remote-free` selection before any hosted
  provider call and reports potentially billable providers as `blocked` when that
  selection or no-cost confirmation is absent.
- The harness provides a path for later provider-specific child tasks to add
  OpenAI, Anthropic, cloud, and OpenAI-compatible endpoint checks.

## Verification

Expected commands will be finalized by this child task. At minimum, run the focused
smoke command and a documentation/format check:

```bash
<provider-smoke-command>
git --no-pager diff --check
```

If Java source changes, also run the relevant Maven selector from
`app/codegeist/cli`.

## Planning Notes

- Keep the first harness output text-oriented and script-friendly.
- Reuse the repository's existing smoke status vocabulary from release smoke work
  where it fits.
- Use the smallest deterministic prompt and the smallest useful output cap for
  any `remote-free` check, but do not claim that token limits alone make a remote
  provider no-cost.
- Provider-specific follow-up tasks should add one remote provider at a time after
  this harness exists.
- Hosted-provider smoke rows must consume the account/free-tier catalog in
  `T006_03_define-provider-credential-and-account-strategy.md` before deciding
  whether a provider is eligible for `remote-free`, `blocked`, or config-only
  behavior.
- Provider-specific smoke rows should also consume the T006_03 availability matrix
  so the harness reports missing starter, missing required config, blocked safety
  posture, local service setup, and provider API failures distinctly.
