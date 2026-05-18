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
- Treat `T002_01_align-codegeist-build-baseline.md` as the completed build/layout
  baseline exception. For `T002_02` and later, solve child tasks as
  documentation/specification handoffs unless the user explicitly reopens a task
  as implementation work.
- For Codegeist architecture and implementation tasks, read
  `docs/developer/specification/codegeist-opencode-parity.md` when the target task references
  OpenCode parity, runtime boundaries, provider behavior, tools, permissions,
  workspace policy, storage, UI, plugin surfaces, or packaging.
- Use OpenCode as a feature and behavior reference, not as an implementation
  blueprint. Map decisions onto Codegeist's Java-first architecture: Java,
  GraalVM, Spring, Spring AI, Spring Shell, Vaadin, JBang, and PF4J.
- For `T002` foundation tasks, the parent task declares default solve hints for
  OpenCode translation and source-evidence workflows. Use those parent hints
  automatically through the shared hint discovery behavior.
- For vocabulary or boundary slices, do not create empty Java package directories
  only to reserve names. Git does not version empty directories, and premature
  classes can imply unstable contracts. Prefer a focused developer document or
  diagram until a later task is ready to define behavior-free Java contracts.
- When a Codegeist architecture decision affects later child tasks, update those
  task files in the same phase pass so dependencies, non-goals, acceptance
  criteria, and implementation-readiness questions stay current.
- Keep repo-specific context paths such as task docs, memory docs, local rules, and
  developer docs as context-profile data owned by repo commands and rules. Do not
  hard-code this repository's `docs/` layout or external analysis artifacts as
  Codegeist core runtime context sources.
- Keep Codegeist-specific source evidence under `docs/third-party/opencode/` and
  prefer `/ask-project opencode ...` or `/ask-project-repomix opencode ...` when
  a phase needs source-backed OpenCode behavior details.
