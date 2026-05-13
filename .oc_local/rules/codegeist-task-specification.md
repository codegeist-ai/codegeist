# Codegeist Task Workflow Overlay

Use the shared task phase commands from `.opencode` for task workflow phases:

- `/specify-task <task-ref> [context/instructions]`
- `/plan-task <task-ref> [context/instructions]`
- `/solve-task <task-ref> [context/instructions]`
- `/work-task <task-ref> [context/instructions]`

This overlay adds only Codegeist-specific guidance. Keep generic phase behavior in
`.opencode/rules/task-phases.md` and `.opencode/rules/task-workflow.md`.

## Codegeist Guidance

- Treat `T001` child tasks as documentation-first Codegeist/OpenCode parity
  architecture work unless a task explicitly asks for runtime implementation.
- Start current MVP implementation work from
  `T002_01_align-codegeist-build-baseline.md`, then continue through the `T002`
  child tasks in dependency order.
- For Codegeist architecture and implementation tasks, read
  `docs/developer/codegeist-opencode-parity.md` when the target task references
  OpenCode parity, runtime boundaries, provider behavior, tools, permissions,
  workspace policy, storage, UI, plugin surfaces, or packaging.
- Use OpenCode as a feature and behavior reference, not as an implementation
  blueprint. Map decisions onto Codegeist's Java-first architecture: Java,
  GraalVM, Spring, Spring AI, Spring Shell, Vaadin, JBang, and PF4J.
- For `T002` implementation tasks, the parent task declares default solve hints
  for OpenCode translation and source-evidence workflows. Use those parent hints
  automatically through the shared hint discovery behavior.
- When a Codegeist architecture decision affects later child tasks, update those
  task files in the same phase pass so dependencies, non-goals, acceptance
  criteria, and implementation-readiness questions stay current.
- Keep Codegeist-specific source evidence under `docs/third-party/opencode/` and
  prefer `/ask-project opencode ...` or `/ask-project-repomix opencode ...` when
  a phase needs source-backed OpenCode behavior details.
