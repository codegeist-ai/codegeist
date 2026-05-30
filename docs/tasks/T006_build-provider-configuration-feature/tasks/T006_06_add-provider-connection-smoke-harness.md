# T006_06 Add Provider Connection Smoke Harness

Status: open

Parent: `../task.md`

## Goal

Add a repeatable provider connection smoke harness for Codegeist.

The harness should let local and remote providers be checked one by one while
keeping credentials optional and reporting precise status.

## Scope

- Define a smoke entrypoint for checking configured providers.
- Use `codegeist.yml` provider entries and credential references.
- Support an allowlist so one provider can be checked without touching every
  configured provider.
- Report `passed`, `skipped`, or `failed` for each provider.
- Treat missing credentials, missing account setup, missing cloud project, missing
  model deployment, or missing local service as `skipped` when the provider is not
  explicitly required.
- Treat an explicitly required provider as failed when prerequisites are absent.
- Record enough timing to see slow startup, network calls, model pulls, or cloud
  latency.
- Keep secret values redacted from output and logs.

## Non-Goals

- Do not create accounts inside the smoke harness.
- Do not run all paid providers by default.
- Do not make Maven's ordinary test lifecycle depend on remote provider accounts.
- Do not store smoke output containing prompts, completions, or credentials in
  committed files.
- Do not implement provider-specific retries beyond what the first smoke needs.

## Acceptance Criteria

- The harness can check at least the already implemented local Ollama provider.
- The harness can skip a remote provider with a precise reason when credentials are
  missing.
- The harness redacts credential sources and values from output.
- The harness output is easy to scan and includes provider id, model id, status,
  duration, and blocker when skipped or failed.
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
- Provider-specific follow-up tasks should add one remote provider at a time after
  this harness exists.
