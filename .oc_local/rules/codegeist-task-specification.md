# Codegeist Task Workflows

Use the local `/specify-task <task-ref> [context/instructions]` command when an
existing Codegeist architecture task under `docs/tasks/` needs another
specification pass.

Use the local `/solve-task <task-ref> [context/instructions]` command when an
existing task should be solved collaboratively with the user.

Use the local `/plan-task <task-ref> [context/instructions]` command when an
architecture, planning, backlog, or solution task should become one concrete
implementation task. The first argument may be a task id, repo-relative task
file, task filename, or task folder.

Use the local `/work-task <task-ref> [context/instructions]` command when the
user wants one command to orchestrate the full specify, plan, specify, and solve
workflow for a task.

## Standard Task-To-Implementation Workflow

Use this workflow when an architecture, planning, backlog, or solution task needs
to become implementation work. The preparation steps are intentionally iterative:
run `/specify-task` and `/plan-task` as often as needed while
new information, user instructions, or scope decisions are still arriving.

1. Run `/specify-task <source-task-ref> [context/instructions]` to sharpen the
   source task without creating or implementing anything. This phase has no prior
   dependency and records the specification status in the task.
2. Run `/plan-task <task-ref> [context/instructions]`
   only when no suitable implementation task already exists. This creates one
   concrete implementation task and stops before code changes. This phase depends
   on a current `/specify-task` status and records the planning status in the
   task.
3. Re-run `/specify-task <task-ref> [context/instructions]` when new information
   should sharpen the source or implementation task before solving.
4. Re-run `/plan-task <task-ref> [context/instructions]` when the implementation
   slice needs to be refined from the source context. If a matching
   implementation task already exists, update that task instead of creating a
   duplicate unless the user explicitly asks for a new distinct task.
5. Run `/solve-task <task-ref> [context/instructions]` only after
   the implementation task is specified enough to build. This implements the
   existing implementation task, updates tests and docs, and records the solution
   state. This phase depends on a current `/plan-task` status and records the
   solve status in the task.

Skip new task creation when a suitable implementation task already exists. In
that case, use repeated `/specify-task` or `/plan-task` passes to
sharpen that existing task, then proceed to `/solve-task`.

## Full Workflow Orchestration

Use `/work-task <task-ref> [context/instructions]` when the user asks to run the
whole workflow with one command. It forwards the same task reference and context
through these phases:

1. `/specify-task <task-ref> [context/instructions]`
2. `/plan-task <task-ref> [context/instructions]`
3. `/specify-task <task-ref> [context/instructions]` on the concrete
   implementation task selected or sharpened by planning
4. `/solve-task <task-ref> [context/instructions]` on that concrete
   implementation task

`/work-task` must stop before solving when planning leaves multiple possible
implementation tasks, unresolved material decisions, or a task that lacks the
plan detail needed by `/solve-task`.

## Rules

- Use `/specify-task` for already-described tasks that need Codegeist-specific
  OpenCode-to-Java migration questions, boundary rules, non-goals, or
  implementation-readiness criteria.
- Do not use `/specify-task` to create, solve, or implement a task.
- Repeated `/specify-task` passes should include context or instructions that say
  what changed or what should be sharpened. Ask for a focused instruction when a
  repeated pass has no clear reason.
- Use `/plan-task` to derive one concrete implementation task
  interactively with the user before code changes start when no suitable
  implementation task already exists.
- Re-running `/plan-task` with new information may sharpen an
  existing matching implementation task. It must not create a duplicate unless
  the user explicitly confirms the duplicate is distinct.
- Every workflow step accepts context or instructions after the task reference.
  Use those arguments to focus the pass, record relevant context in the task when
  it affects durable handoff, and avoid generic churn.
- `/work-task` uses the same `<task-ref> [context/instructions]` contract and
  passes that context through every phase. It may switch from the initial source
  task to the concrete implementation task selected by `/plan-task`, but it must
  report that switch.
- Every workflow step may update the target task or directly affected task files
  when decisions change, new instructions arrive, or acceptance criteria,
  non-goals, implementation plans, dependencies, or follow-up boundaries need to
  stay current.
- Every workflow step must record its own phase status in the target task. Status
  entries should name the phase, context or instructions considered, discovered
  hints, upstream phase dependency, outcome, open decisions, and next recommended
  phase.
- `/specify-task` has no prior phase dependency. `/plan-task` depends on a
  current `/specify-task` status. `/solve-task` depends on a current `/plan-task`
  status.
- Every workflow step must discover applicable hint files from the task docs it
  reads, including parent tasks, child tasks, dependencies, `Default Solve Hints`,
  `Hints`, `Guidance`, and repo-relative `docs/tasks/hints/` references. Do not
  rely only on explicit command arguments.
- `/plan-task` must propose concrete implementation options
  when the source allows multiple slices, ask for user decisions on material
  scope or contract choices, and create at most one task unless the user
  explicitly asks for more.
- Implementation tasks created or sharpened by `/plan-task`
  must include a concrete solution direction, planned classes, interfaces,
  records, configuration files, packages, tests, documentation targets, ordered
  implementation steps, acceptance criteria, verification plan, dependencies,
  non-goals, and a planning note that records the selected option or user
  decision.
- Implementation tasks created by `/plan-task` must also keep
  the plan workflow handoff inside the task file: resolved source task,
  source parent `task.md`, user focus or context, selected option, duplicate
  check result, discovered hints, related context files read, and the recommended
  next command.
- `/plan-task` creates task documentation only. Runtime code,
  tests, build-file changes, and task solution work belong in a later
  `/solve-task <task-ref> [context/instructions]` pass.
- `/specify-task`, `/plan-task`, and `/solve-task` must treat
  task directories as canonical only when they contain `task.md`. When a
  referenced task directory has no `task.md`, stop and report the broken task
  structure instead of guessing.
- When a task belongs to a parent task directory, these commands should read the
  parent `task.md` before proposing specifications, solutions, or
  implementation-task options.
- `/specify-task` should use discovered hints as specification guidance, using
  those hints to sharpen scope, boundaries, dependencies, verification, and
  implementation-readiness questions while staying non-implementation.
- `/solve-task` should use discovered hints as implicit hint files, reading those
  hints before proposing solution options.
- Re-run `/specify-task` safely when related architecture decisions change; if
  the task is already current, leave it unchanged and report the no-op.
- Keep the central parity document and `docs/memory-bank/chat.md` synchronized
  when the specification changes durable architecture state.
- Start current MVP implementation work from
  `T002_01_align-codegeist-build-baseline.md`, then continue through the `T002`
  child tasks in dependency order.
- Keep `/solve-task` generic. Put Codegeist-specific task-solving guidance in
  this rule instead of hard-coding architecture paths or domain assumptions into
  the command.
- During every `/solve-task` run, keep the target task itself as the precise
  handoff record. In Plan Mode, record what should be done and which decisions
  are pending. In Build Mode, record what was done, what changed, what remains
  open, and what the next step is.
- Keep hint files dynamic. When solving a task reveals a reusable lesson for
  future tasks, update the relevant hint file immediately, but keep the wording
  generic enough to apply beyond the current task.
- For OpenCode-related Codegeist architecture tasks, pass
  `docs/tasks/hints/opencode-solving-guidance.md` to `/solve-task` when the task
  needs an explicit reminder to use OpenCode as a feature reference rather than
  an implementation blueprint.
- For Codegeist architecture tasks, `/solve-task` should read the target task,
  its parent epic, directly adjacent open tasks, and
  `docs/developer/codegeist-opencode-parity.md` before proposing solution
  options.
- For `T002` implementation tasks, the parent task declares default solve hints
  for OpenCode translation and source-evidence workflows. Use those parent hints
  automatically instead of requiring the user to pass them on every solve call.
- When a Codegeist architecture decision affects later child tasks, update those
  task files in the same solution pass so their dependencies, non-goals,
  acceptance criteria, and implementation-readiness questions stay current.
- Treat `T001` child tasks as documentation-first architecture work unless a
  task explicitly asks for runtime implementation. Keep OpenCode-to-Java mapping
  decisions aligned with the Java, GraalVM, Spring, Spring AI, Vaadin, JBang,
  and PF4J baseline.
