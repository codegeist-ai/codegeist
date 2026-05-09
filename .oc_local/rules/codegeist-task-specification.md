# Codegeist Task Specification

Use the local `/specify-task <task-ref>` command when an existing Codegeist
architecture task under `docs/tasks/` needs another specification pass.

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
