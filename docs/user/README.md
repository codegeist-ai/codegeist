# User Documentation

User-facing usage notes for Codegeist.

## Guides

- `install-from-github-releases.md` - install Codegeist from GitHub Release assets
  on Linux, macOS, or Windows.

## Ask Command Sessions

`codegeist ask <prompt>` sends one prompt to the configured provider and prints only
the provider response to stdout. Each successful plain `ask` call starts a new local
session and stores the turn in a session store.

Use `-c` or `--continue` to append to the newest existing session:

```bash
codegeist ask "Explain this project"
codegeist ask -c "Continue with the next step"
codegeist ask --continue "Summarize the current session"
```

By default, Codegeist stores sessions in the current working directory at:

```text
.codegeist/session.json
```

The file contains chat/session history only. It must not contain provider config,
selected provider or model, API keys, MCP definitions, enabled tools, permission
rules, runtime status, or TUI state.

## Session Store Path

The session store path is application runtime configuration, not `codegeist.yml`
provider/tool configuration. Defaults are built into Codegeist:

| Setting | Default | Purpose |
| --- | --- | --- |
| `codegeist.session.directory` | `.codegeist` | Directory under the current working directory. |
| `codegeist.session.store-file` | `session.json` | Session store file name inside that directory. |

You can override them with Spring application properties, for example in an external
`application.yaml`:

```yaml
codegeist:
  session:
    directory: .codegeist-dev
    store-file: session-dev.json
```

Environment variables also work when you need local workflow isolation or
test-specific storage:

| Environment variable | Maps to |
| --- | --- |
| `CODEGEIST_SESSION_DIRECTORY` | `codegeist.session.directory` |
| `CODEGEIST_SESSION_STORE_FILE` | `codegeist.session.store-file` |

If those environment variables are unset, the mapped properties keep these
defaults:

| Setting | Default |
| --- | --- |
| `CODEGEIST_SESSION_DIRECTORY` | `.codegeist` |
| `CODEGEIST_SESSION_STORE_FILE` | `session.json` |

Example:

```bash
CODEGEIST_SESSION_DIRECTORY=.codegeist-dev \
CODEGEIST_SESSION_STORE_FILE=session-dev.json \
codegeist ask "Use an isolated session store"
```

Blank values fall back to the defaults. The store schema stays the same regardless
of the chosen directory or file name.
