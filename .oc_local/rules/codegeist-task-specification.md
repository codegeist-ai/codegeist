# Codegeist Task Workflows

Use the local `/specify-task <task-ref>` command when an existing Codegeist
architecture task under `docs/tasks/` needs another specification pass.

Use the local `/solve-task <task-ref>` command when an existing task should be
solved collaboratively with the user. It may receive zero or more hint files
after the task reference.

## Rules

- Use `/specify-task` for already-described tasks that need Codegeist-specific
  OpenCode-to-Java migration questions, boundary rules, non-goals, or
  implementation-readiness criteria.
- Do not use `/specify-task` to create, solve, or implement a task.
- Re-run `/specify-task` safely when related architecture decisions change; if
  the task is already current, leave it unchanged and report the no-op.
- Keep the central parity document and `docs/memory-bank/chat.md` synchronized
  when the specification changes durable architecture state.
- Start future T001 architecture expansion from the next specified child task,
  currently `T001_09_define-tool-architecture.md`.
- Keep `/solve-task` generic. Put Codegeist-specific task-solving guidance in
  this rule instead of hard-coding architecture paths or domain assumptions into
  the command.
- Treat hint files passed to `/solve-task` as task-solving input. Read them
  before proposing solution options, and let them override stale assumptions in
  older task text when they clearly document a newer decision.
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
- When a Codegeist architecture decision affects later child tasks, update those
  task files in the same solution pass so their dependencies, non-goals,
  acceptance criteria, and implementation-readiness questions stay current.
- Treat `T001` child tasks as documentation-first architecture work unless a
  task explicitly asks for runtime implementation. Keep OpenCode-to-Java mapping
  decisions aligned with the Java, GraalVM, Spring, Spring AI, Vaadin, JBang,
  and PF4J baseline.
