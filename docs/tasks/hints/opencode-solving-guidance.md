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
- For source-level questions, prefer
  `/ask-project-repomix opencode "<question>"` when `repomix-output.xml` is
  available.
- Keep task updates aligned with
  `docs/developer/codegeist-opencode-parity.md`.
- If a decision affects later `T001` child tasks, update those tasks in the same
  pass.
- When a T002 slice is intentionally narrowed to documentation-only, solve it as
  a detailed handoff: include concrete future file maps, boundary rules, sequence
  or class diagrams, and realistic Java examples inside markdown, but do not add
  source files, tests, empty packages, or build changes.
- For documentation-only solve passes, keep current-state docs such as
  `docs/developer/architecture.md` accurate by not claiming planned packages or
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
