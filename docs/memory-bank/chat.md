# Chat Memory

## Target State

- `codegeist.ai` should grow into a customizable coding agent for CLI, TUI, and
  web use.
- The development environment remains repo-local: devcontainer configuration,
  rules, commands, and project memory live in the repository.

## Current State

- `main` contains the current project state. `.opencode` is configured as a git
  submodule that tracks the `release` branch of `codegeist-agent-kit` and points
  to `aa5f258`.
- `.devcontainer/` is configured as a git submodule that tracks the `release`
  branch of `codegeist-devcontainer-kit` in `.gitmodules` and points to
  `35f46d9`.
- `start.sh` has been removed; the devcontainer is started directly through VS
  Code Dev Containers or `devcontainer up --workspace-folder .`.
- `.devcontainer/initialize.sh` from the kit creates root `.local.env`,
  `compose.local.yml`, root `.oc_local/`, `.devcontainer/.env`, and
  `.devcontainer/compose.local.gen.yml` when needed, and can select a worktree
  under `.worktrees/<branch>` as `/workspace` via `BRANCH`.
- `app/codegeist` is a Spring Boot and Spring Shell bootstrap with Java 25,
  Maven, and a prepared GraalVM native build.
- `app/codegeist/Taskfile.yml` provides `test`, `build`, `run`, and `native`.
- `.devcontainer/Dockerfile` installs `nix` in addition to the existing
  apt-based toolchain without migrating the devcontainer workflow to Nix.
- `.devcontainer/Dockerfile` also installs `@devcontainers/cli` in the existing
  global npm tools block.
- `.devcontainer/tests.sh` now lives in the extracted `.devcontainer` submodule
  and remains the devcontainer self-test.
- The Nix profile hook is wired globally through `/etc/profile.d/nix.sh` so
  `nix` is available on `PATH` in container login shells.
- The local OpenCode overlay `.oc_local/` contains the commands
  `/analyse-project`, `/ask-project`, `/ask-project-repomix`,
  `/create-implementation-task`, `/specify-task`, and `/solve-task`, the
  `/repository-analysis` skill, the `@repomix` subagent, and the AI script
  `render-mermaid.sh` for Mermaid SVG rendering.
- `docs/third-party/opencode/source` is a submodule for
  `https://github.com/anomalyco/opencode.git` on branch `dev` and points to
  `22e64ca`.
- `docs/third-party/opencode/` contains the initial third-party documentation
  workspace for OpenCode: `README.md`, `ANALYSIS_REPORT.md`, `REGENERATE.md`,
  feature, user, and developer notes, plus Mermaid sources.
- `docs/tasks/T001_define-codegeist-opencode-feature-architecture/` is the active
  architecture epic for Codegeist/OpenCode parity. The epic is split into 25
  granular documentation tasks under `tasks/`.
- `docs/developer/codegeist-opencode-parity.md` records the Codegeist/OpenCode
  parity architecture. Codegeist is mapped onto the Java-first stack instead of
  being planned as an OpenCode/Bun/TypeScript copy. The document now covers the
  technology baseline, OpenCode-to-Java mapping, module boundaries, CLI/Shell
  architecture, agent modes, session model, event model, provider architecture,
  tool/permission/workspace boundaries, shell and patch/edit architecture,
  context loading, PF4J/JBang/Vaadin/server/storage roles, GraalVM constraints,
  feature matrix, MVP cut, prompt flow, risk register, and implementation
  backlog.
- All 25 child tasks under
  `docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/` have
  been checked with `/specify-task` and solved with `/solve-task`. Each child
  task now has a `Solution Note` that points to the corresponding section in
  `docs/developer/codegeist-opencode-parity.md`.
- The `opencode` analysis uses a focused runtime corpus for Graphify instead of
  the whole repository. The last Graphify run produced 1,247 nodes, 2,008 edges,
  and 78 communities; Graphify, Repomix, and verify outputs remain regenerable
  and ignored.
- Source-close questions about third-party projects should use
  `/ask-project-repomix <project> "<question>"` or the `@repomix` subagent
  directly. The subagent loads `docs/third-party/<project>/repomix-output.xml`
  into its own context through Repomix tools so the main context stays small.

## Important Decisions

- Build artifacts such as `target/`, `bin/`, `.class`, and `.jar` stay out of
  git.
- Local devcontainer environment values stay in root `.local.env`; root
  `compose.local.yml` is the versioned local Compose override that
  `devcontainer.json` always includes.
- Repo memory is maintained in `docs/memory-bank/chat.md`.
- Nix is only installed as an additional package manager for now; there are no
  flakes and no migration of the toolchain to Nix packages yet.
- Checkouts without recursive submodules are repaired with
  `git submodule update --init --recursive`, not through a repo-local launcher.
- `.opencode` and `.devcontainer` should be updated in the parent repo through
  their `release` branches, not through the previously used `main` branches or
  repo-local launcher scripts.
- Shared kit updates use the OpenCode workflow `/update-submodules`, which sets
  `.opencode` and `.devcontainer` to the branches configured in `.gitmodules`.
- Project-specific analysis workflows belong in the local overlay `.oc_local/`,
  not in the shared `.opencode` submodule. Third-party analysis artifacts are
  stored under `docs/third-party/<project-name>/`; Mermaid sources live under
  `diagrams/source/` and rendered SVGs under `diagrams/rendered/`. Regenerable
  heavy artifacts such as `repomix-output.*`, `graphify-out/`, logs, manifests,
  verify reports, and rendered SVGs stay out of git via `.gitignore` and are
  regenerated through `REGENERATE.md`.
- `/analyse-project` no longer uses its own analysis shell script. The old
  `.oc_local/ai-scripts/analyse-project.sh` orchestration was removed; graph
  generation runs through the shared `graphify` skill on a filtered code and
  documentation corpus.
- The selected Codegeist technology baseline is Java, GraalVM, Spring,
  Spring AI, Vaadin, JBang, and PF4J. Architecture docs should map OpenCode
  concepts explicitly onto this stack.
- Codegeist sessions are runtime-owned aggregates. CLI, server, Vaadin, and
  future TUI clients may create, continue, inspect, and render sessions, but they
  must not own session state transitions. `T001_06` answers the OpenCode-to-Java
  migration questions for session, turn, message part, lifecycle, streaming
  boundaries, and later storage projections.
- Codegeist events are typed runtime events for CLI output and later server,
  Vaadin, and TUI streams. They separate user-visible rendering from
  audit-relevant events; transport, event sourcing, and storage schema remain
  later decisions. `T001_07` answers the OpenCode-to-Java migration questions for
  `RuntimeEvent`, envelope, ordering, correlation, event families, audit
  relevance, and later projections.
- Spring AI is the default provider integration path, but it remains behind
  Codegeist-owned runtime/provider policies. Provider selection, model
  capabilities, tool exposure, events, and errors remain Codegeist decisions, not
  CLI or SDK details.
- `/specify-task <task-ref>` is the local repeatable workflow for checking
  existing Codegeist architecture tasks against OpenCode-to-Java migration
  questions and sharpening them when needed without implementation. The local
  rule `.oc_local/rules/codegeist-task-specification.md` records this
  convention.
- `/solve-task <task-ref> [hint-file ...]` is the local generic workflow for
  solving an existing task collaboratively with the user. After the task
  reference, zero or more hint files may be passed. Every run must record in the
  target task what should happen in Plan Mode, which decisions are open, what was
  implemented in Build Mode, what remains, and the next step. Project-specific
  solution guidance stays in `.oc_local/rules/`, while the command only records
  the generic flow for task resolution, options, implementation, affected tasks,
  and verification.
- `/create-implementation-task <source-task-ref-or-file> [focus/context]` is the
  local interactive workflow for deriving one concrete `T002+` implementation
  task from an existing architecture, planning, backlog, or solution task. It
  may take a task id, repo-relative task file, task filename, or task folder as
  its first argument. It collaborates with the user before writing the new task,
  records a concrete solution direction, and leaves implementation for a later
  `/solve-task <new-task>` pass.
- `docs/tasks/hints/opencode-solving-guidance.md` is the reusable hint for
  OpenCode-related `/solve-task` runs. It reminds solvers to use OpenCode as a
  feature reference rather than an implementation blueprint. Hint files are
  dynamic: when solving a task reveals reusable lessons, update them generically
  without task-specific logs or narrow implementation details.

## Open Points

- The next useful step is to derive explicit `T002+` implementation tasks from
  the `Implementation Backlog` in `docs/developer/codegeist-opencode-parity.md`,
  starting with the build baseline and the runtime/session/event contracts.
