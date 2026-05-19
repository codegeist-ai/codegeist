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
  prefer `/ask-project opencode ...` when a phase needs source-backed OpenCode
  behavior details. `/ask-project` owns any Repomix-backed deep dive internally.
- Spring AI Agent Utils may be used directly inside Codegeist implementation code
  when it helps move faster, but Codegeist runtime, provider, tool, permission,
  workspace, event, session, storage, API, and UI contracts must not depend on
  Agent Utils architecture or raw broad provider callbacks. Add an adapter only
  when a concrete boundary needs policy mediation, result mapping, or replacement
  flexibility.
- T003 core implementation scope includes both CLI and TUI behavior. Keep JBang,
  PF4J, Vaadin, headless server, API, and SDK/OpenAPI implementation in the
  backlog while preserving adapter-ready runtime boundaries for those later
  surfaces.
- For T003 Java implementation tasks, use
  `docs/developer/specification/java-generation-guidance.md` as the source
  generation contract and keep planned package boundaries separate from current
  implemented source.
- For Codegeist behavior changes and bug fixes, use
  `docs/developer/specification/testing-strategy-and-agent-rules.md`: TDD is the
  default, tests should stay individually executable, and solve results should
  report targeted commands plus enough timing detail to spot slow tests or slow
  startup.
- For packaging, release, platform, or binary-smoke work, use
  `docs/developer/specification/build-release-and-binary-smoke-strategy.md`:
  GitHub Releases are the release target, Windows/Linux/macOS support must be
  proven explicitly, and each platform check should report `passed`, `skipped`
  with reason, or `failed` with blocker.
- For T003 implementation slots, prefer a documentation-only source-generation
  contract before creating Java source when the boundary still spans runtime,
  CLI, context/workspace, provider, tool/permission/workspace, patch/edit, shell,
  or storage behavior. Use the finalized `*-source-generation-contract.md`
  documents under `docs/developer/specification/` as the handoff for later
  source-generating tasks.
