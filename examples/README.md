# Codegeist Configuration Examples

These examples are small parser-checked starting points for the currently
implemented direct `codegeist.yml` roots. Copy only the settings you need into a
local `codegeist.yml`; Codegeist does not load files from `examples/`
automatically.

- `codegeist.ollama.yml` configures a local Ollama endpoint and model.
- `codegeist.openai.yml` uses the literal non-credential placeholder
  `not-a-real-openai-api-key` so production validation accepts the tracked example
  without reading the environment.
- `codegeist.mcp.yml` defines a local stdio MCP filesystem server. Using it can
  start the configured child process and may let `npx` download the package.

The files contain no usable credentials and parser tests make no provider or MCP
calls. Replace placeholders only in ignored local configuration, never in these
tracked examples.

## Trust Boundaries

Treat `codegeist.yml` as trusted local code: string values containing `#{...}`
use an unrestricted Spring SpEL evaluation context. `--show-config` prints
configured values without redaction. Codegeist tools can read, write, edit, and
run host processes, and current workspace controls are not a sandbox or a
permission-prompt system. Review configuration and tool access before running
`ask` or `tui` in a sensitive workspace.

To prove the examples still match the production parser without starting
providers, run from the repository root:

```bash
task cli:test-jvm TEST=CodegeistExamplesTest
```
