# T012 Add Workspace And Local Tool Configuration Example

Status: open

Public Tracking: https://github.com/codegeist-ai/codegeist/issues/6

Contribution Level: beginner (`good first issue` candidate)

Effort: small

Confirmed Unmet: on 2026-08-02, `examples/` has parser-checked Ollama, OpenAI,
and MCP files but no example for the implemented `workspace:` and `tools:` roots.

## Goal

Add one credential-free example for the implemented workspace, edit-preview, and
shell-timeout settings and keep it on the same production-parser test path as the
existing contributor examples.

## Acceptance Criteria

- `examples/codegeist.tools.yml` contains only implemented `workspace:` and
  `tools:` fields.
- The example uses a relative workspace, keeps the directory guard enabled, and
  demonstrates bounded edit-preview and positive shell-timeout settings.
- The example contains no credentials, provider, MCP process, command prefix, or
  executable command.
- `examples/README.md` explains that parsing the file has no tool side effects and
  that running Codegeist tools can still read, mutate, or execute on the host.
- `CodegeistExamplesTest` includes the new file and loads it through
  `CodegeistConfigService` without provider, MCP, Docker, or tool calls.

## Files

- `examples/codegeist.tools.yml`
- `examples/README.md`
- `app/codegeist/cli/src/test/java/ai/codegeist/app/config/CodegeistExamplesTest.java`

## Non-Goals

- Do not change production configuration classes or tool behavior.
- Do not disable the workspace directory guard or present any setting as a
  sandbox, permission system, or secret-redaction mechanism.
- Do not add a provider or MCP client to this example.

## Verification

```bash
task cli:test-jvm TEST=CodegeistExamplesTest
DOCKER_HOST=tcp://127.0.0.1:1 task cli:check
git --no-pager diff --check
```
