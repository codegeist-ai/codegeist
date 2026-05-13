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
  `/plan-task`, `/specify-task`, `/solve-task`, and `/work-task`, the
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
- `docs/tasks/T002_implement-codegeist-mvp-foundation/` is the active
  implementation parent for the first Codegeist MVP foundation work. It contains
  `T002_01_align-codegeist-build-baseline.md` from `T001_01` and
  `T002_02_introduce-runtime-vocabulary-contracts.md` from `T001_02`, plus
  grouped implementation slices through `T002_12` for runtime/session/events,
  CLI mode wiring, context/workspace loading, provider configuration,
  tool/permission/workspace contracts, patch/edit, shell verification, storage,
  native packaging, and later extension/client readiness gates.
- The T002 parent and all `T002_*` child tasks have been rechecked with
  `/specify-task` semantics after adding parent default hints. Each task now has a
  `Specification Check Result` clarifying scope, dependencies, OpenCode source
  research expectations, and non-implementation boundaries.
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
- The local task-to-implementation workflow is iterative: run
  `/specify-task <source-task-ref> [context/instructions]` to sharpen a source
  task, run
  `/plan-task <task-ref> [context/instructions]` when no suitable implementation
  task exists, then repeat `specify` and `plan-task` passes as new information or
  user instructions arrive. Repeated implementation-task planning passes should
  sharpen the matching existing task instead of duplicating it unless the user
  explicitly asks for a distinct new task. Run
  `/solve-task <task-ref> [context/instructions]` only after the
  implementation task is ready to build.
- Repeated workflow passes should receive context or instructions that say what
  changed or what the pass should focus on. The command should ask for a focused
  instruction instead of doing generic churn when a repeated pass has no clear
  reason.
- Every workflow step now discovers applicable hints from task descriptions,
  parent tasks, child tasks, dependencies, `Default Solve Hints`, `Hints`,
  `Guidance`, and `docs/tasks/hints/` references instead of relying only on
  explicit command arguments.
- Every workflow step may update the target task or directly affected task files
  when decisions change, new instructions arrive, or acceptance criteria,
  non-goals, implementation plans, dependencies, or follow-up boundaries need to
  stay current.
- Every workflow step records its own phase status in the target task. The phase
  status should name the phase, context or instructions considered, discovered
  hints, upstream phase dependency, outcome, open decisions, and next recommended
  phase.
- Phase dependencies are explicit: `/specify-task` has no prior dependency,
  `/plan-task` depends on a current `/specify-task` status, and `/solve-task`
  depends on a current `/plan-task` status.
- `/work-task <task-ref> [context/instructions]` orchestrates the complete flow
  when the user wants one command: specify the initial task, plan the concrete
  implementation task, specify the planned task again, then solve it. It passes
  the same context through every phase, may switch from the source task to the
  concrete implementation task selected by `/plan-task`, and stops before solving
  when planning leaves multiple choices or open material decisions.
- `/specify-task <task-ref> [context/instructions]` is the local repeatable
  workflow for checking existing Codegeist architecture tasks against
  OpenCode-to-Java migration questions and sharpening them when needed without
  implementation. The local rule `.oc_local/rules/codegeist-task-specification.md`
  records this convention.
- `/solve-task <task-ref> [context/instructions]` is the local generic workflow
  for solving an existing task collaboratively with the user. Every run must
  record in the target task what should happen in Plan Mode, which decisions are
  open, what was implemented in Build Mode, what remains, and the next step.
  Project-specific solution guidance stays in `.oc_local/rules/`, while the
  command only records the generic flow for task resolution, options,
  implementation, affected tasks, and verification.
- `/plan-task <task-ref> [context/instructions]` is the local interactive
  workflow for planning one concrete implementation task from an existing
  architecture, planning, backlog, or solution task. It may take a task id,
  repo-relative task file, task filename, or task folder as its first argument.
  It collaborates with the user before writing or sharpening the task, records a
  concrete solution direction, stores the plan workflow handoff in the task file,
  and leaves implementation for a later
  `/solve-task <task-ref> [context/instructions]` pass. This command is the
  detailed planning phase: it should identify expected classes, interfaces,
  records, files, packages, tests, implementation order, acceptance criteria, and
  verification before runtime code changes start.
- `/specify-task`, `/plan-task`, and `/solve-task` now require canonical
  `task.md` files for referenced task directories, read parent `task.md` files
  when working with child tasks, and discover applicable hints from task docs
  instead of relying only on explicit hint arguments.
- `docs/tasks/hints/opencode-solving-guidance.md` is the reusable hint for
  OpenCode-related `/solve-task` runs. It reminds solvers to use OpenCode as a
  feature reference rather than an implementation blueprint. Hint files are
  dynamic: when solving a task reveals reusable lessons, update them generically
  without task-specific logs or narrow implementation details.
- `docs/tasks/hints/opencode-source-solving-guidance.md` is the source-focused
  hint for using `/ask-project opencode ...` and `/ask-project-repomix opencode
  ...` during solve passes that need evidence from
  `docs/third-party/opencode/source/`, especially provider, tool, MCP,
  permission, session, event, context, shell, patch/edit, extension, and storage
  tasks.

## Open Points

- The next useful step is to solve `T002_01_align-codegeist-build-baseline.md`,
  then solve `T002_02_introduce-runtime-vocabulary-contracts.md` and continue
  through the `T002` child tasks in dependency order.
