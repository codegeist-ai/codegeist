# OpenCode Task Solving Hint

Use OpenCode as a feature reference, not as an implementation blueprint.

Keep this hint current when solving tasks reveals reusable OpenCode-to-Codegeist
lessons. Add broadly useful guidance only; avoid task-specific notes,
implementation logs, or one-off decisions.

## Guidance

- Identify the OpenCode concept behind the task before designing the Codegeist
  equivalent.
- Translate behavior into the Codegeist Java-first stack instead of copying
  Bun/TypeScript structure.
- Prefer Codegeist runtime-owned boundaries for sessions, events, tools,
  permissions, workspace access, providers, storage, and extensions.
- Preserve high customizability as a core Codegeist quality. Architecture
  decisions should leave room for both user-level configuration and
  developer-level extension points without letting either bypass runtime-owned
  safety boundaries.
- Check existing third-party OpenCode docs under `docs/third-party/opencode/`
  before making architecture claims.
- For source-level questions, prefer `/ask-project opencode "<question>"`.
  `/ask-project` uses the analyzed project's Repomix artifact internally when
  broad packed-output context is needed.
- Keep task updates aligned with
  `docs/developer/specification/codegeist-opencode-parity.md`.
- If a decision affects later `T001` child tasks, update those tasks in the same
  pass.
- For `T002_02` and later foundation slices, solve them as detailed
  documentation/specification handoffs unless the user explicitly reopens a slice
  as implementation work: include concrete future file maps, boundary rules,
  sequence or class diagrams, and realistic Java examples inside markdown, but do
  not add source files, tests, empty packages, or build changes.
- For documentation-only foundation slices, diagrams may be concrete and detailed.
  UML class diagrams are appropriate when they make future Java contract
  relationships, adapter ports, typed errors, and value objects clear before code
  exists.
- Documentation-only slices may mention future tests as handoff guidance. Keep the
  tests descriptive and contract-focused; do not create test source, fixtures, or
  build changes unless the task is explicitly reopened as implementation work.
- For documentation-only solve passes, keep current-state docs such as
  `docs/developer/architecture/architecture.md` accurate by not claiming planned packages or
  commands already exist.

## Non-Goals

- Do not copy OpenCode package or module layout.
- Do not treat OpenCode storage, SSE, Bus, or TypeScript schemas as mandatory
  Codegeist architecture.
- Do not add runtime implementation unless the task explicitly asks for it.
- Do not run `task test` for documentation-only solves unless Java, test, build,
  or runtime files changed; `git --no-pager diff --check` is the required minimum.

## Example Usage

```text
/solve-task T001_09 docs/tasks/hints/opencode-solving-guidance.md
```
