# Pi User Notes

Pi is a local terminal coding agent. It expects users to run it from the project they want to work on, configure a provider/model, and decide whether project-local resources should be trusted.

## Install And Start

The upstream quick-start path is:

```bash
npm install -g --ignore-scripts @earendil-works/pi-coding-agent
pi
```

The docs also describe an installer script:

```bash
curl -fsSL https://pi.dev/install.sh | sh
```

`--ignore-scripts` is intentional in upstream docs; Pi does not require install lifecycle scripts for normal npm installs.

## Authentication

Pi supports API-key and subscription/OAuth-style authentication.

Typical paths:

```bash
export ANTHROPIC_API_KEY=sk-ant-...
pi
```

or inside interactive mode:

```text
/login
```

Model switching happens through `/model`, `Ctrl+L`, or CLI flags such as `--provider`, `--model`, and `--thinking`.

## Main Modes

- `pi` - interactive TUI mode.
- `pi -p "prompt"` - print final response and exit.
- `pi --mode json "prompt"` - stream session and agent events as JSON lines.
- `pi --mode rpc` - run a headless JSONL protocol for host applications.
- `pi --list-models [search]` - list available models.
- `pi --offline` or `PI_OFFLINE=1` - disable startup network operations described by upstream docs.

## Interactive Concepts

- `@` references files through fuzzy search.
- Tab completes paths.
- `!command` runs shell and sends output to the model; `!!command` runs shell without sending output to the model.
- `/settings` adjusts common options.
- `/resume`, `/new`, `/session`, `/tree`, `/fork`, and `/clone` manage sessions and branches.
- `/compact` manually compacts long context.
- `/reload` reloads keybindings, extensions, skills, prompts, context files, and themes.
- `/trust` saves a project trust decision for future sessions.

## Session Storage

Sessions are JSONL files under:

```text
~/.pi/agent/sessions/--<path>--/<timestamp>_<uuid>.jsonl
```

Entries form a tree through `id` and `parentId`, so branching and navigation stay in one session file. Existing session formats are migrated on load to the current version described in `packages/coding-agent/docs/session-format.md`.

## Project Trust

Project trust controls whether Pi loads project-local `.pi` settings, resources, packages, and extensions. It is not a sandbox.

Resources that require trust include:

- `.pi/settings.json`
- `.pi/extensions`, `.pi/skills`, `.pi/prompts`, `.pi/themes`
- `.pi/SYSTEM.md` and `.pi/APPEND_SYSTEM.md`
- project `.agents/skills`

`AGENTS.md` and `CLAUDE.md` context files are loaded regardless of project trust unless context loading is disabled.

## Security Warning

Pi runs with the permissions of the launching process. Built-in tools can read files, write files, edit files, and run shell commands. Extensions and Pi packages are executable TypeScript/Node code with the same process permissions.

For untrusted repositories, unattended automation, or generated code you do not plan to watch closely, use a container, VM, micro-VM, or policy sandbox. Upstream docs describe Gondolin, Docker, and OpenShell patterns in `packages/coding-agent/docs/containerization.md`.

## Follow-Up Questions

- `/ask-project pi "How do project trust and package loading interact in interactive and non-interactive modes?"`
- `/ask-project pi "What events does JSON mode emit for one prompt and a tool call?"`
- `/ask-project pi "How do session tree navigation, fork, clone, and compaction affect the JSONL session file?"`
