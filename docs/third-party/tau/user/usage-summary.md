# Tau Usage Summary

Compact user-facing notes extracted from Tau's README and docs.

## Install And Run

Tau uses `uv` and targets Python `>=3.14`.

```bash
uv tool install git+https://github.com/alejandro-ao/tau.git
tau --version
```

For local development:

```bash
uv sync --dev --group docs
uv run tau --version
```

## Main Commands

| Command | Purpose |
| --- | --- |
| `tau` | Start the interactive Textual TUI. |
| `tau "prompt"` | Start the TUI and submit the first prompt. |
| `tau -p "prompt"` | Run a one-shot non-interactive prompt. |
| `tau --provider <name> --model <model>` | Select a configured provider/model. |
| `tau --cwd <path>` | Run tools against another working directory. |
| `tau setup ...` | Add or update an OpenAI-compatible provider entry. |
| `tau providers` | List configured providers. |
| `tau sessions` | List sessions. |
| `tau export ...` | Export a session. |

## TUI Commands

| Command | Purpose |
| --- | --- |
| `/login [provider]` | Save or refresh provider credentials. |
| `/logout [provider]` | Remove Tau-saved credentials. |
| `/model` | Choose active provider/model. |
| `/scoped-models` | Pick models available for quick cycling. |
| `/session` | Show session and context information. |
| `/resume [session-id]` | Resume a previous session. |
| `/tree` | Branch from a previous session entry. |
| `/name <new name>` | Rename the current session. |
| `/compact <summary>` | Replace active context with a manual summary. |
| `/export [--format html|jsonl] [destination]` | Export the current session. |
| `/reload` | Reload resources and project context. |
| `/theme [name]` | Show or set the TUI theme. |
| `/hotkeys` | Show keyboard shortcuts. |
| `/quit` | Exit the session. |

## State Locations

Tau stores durable app state under the user home directory:

```text
~/.tau/providers.json
~/.tau/credentials.json
~/.tau/tui.json
~/.tau/sessions/
~/.tau/skills/
~/.tau/prompts/
~/.tau/AGENTS.md
```

It also reads project-local resources from the active working directory, including `AGENTS.md`, `.tau/`, and `.agents/` locations.
