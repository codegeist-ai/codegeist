# Spring AI Agent Utils Phase Guidance

Use this hint during specify, plan, and solve phases for Codegeist Java
implementation tasks that touch Spring AI-facing agent behavior.

## Guidance

- Before specifying, planning, designing, or writing Java implementation code, ask
  the prepared third-party workspace what Spring AI Agent Utils already
  implements:
  `/ask-project spring-ai-agent-utils "<specific implementation question>"`.
- Check for matching Agent Utils classes, tools, builders, tests, prompts,
  advisors, repositories, or subagent boundaries before creating a Codegeist
  implementation from scratch.
- If an Agent Utils implementation already matches the task and Codegeist policy
  can validate the request first, prefer using that implementation as a private
  implementation detail.
- If Codegeist must enforce workspace validation, permissions, mode gates,
  session/event projection, output bounding, error mapping, or result shaping,
  build a thin Codegeist wrapper around the Agent Utils behavior instead of
  duplicating the full utility.
- If the existing Agent Utils behavior does not fit the Codegeist contract, use it
  only as a concept or test-pattern reference and keep the Codegeist-owned
  contract independent.
- Keep raw Agent Utils APIs out of Codegeist public runtime, CLI, session, event,
  tool, permission, provider, and storage contracts.
- Cite the Agent Utils source files or docs that informed the decision in the
  task result or implementation handoff when the choice affects architecture.

## High-Value Source Questions

Use focused questions like these during implementation specify, plan, and solve
phases:

```text
/ask-project spring-ai-agent-utils "Which classes already implement grep, glob, list-directory, and file read behavior, and what validation or output bounding do they apply? Cite source files and tests."
/ask-project spring-ai-agent-utils "How do ShellTools model command execution, background processes, timeouts, and output filtering? Cite source files and tests."
/ask-project spring-ai-agent-utils "How are ToolCallback and @Tool-based utilities built and registered, and where would a Codegeist wrapper need to map descriptors, permissions, and results? Cite source files."
/ask-project spring-ai-agent-utils "Which tests cover filesystem traversal, path safety, grep output modes, shell execution, task repositories, and question handling? Cite reusable patterns."
```

## Non-Goals

- Do not skip Codegeist policy boundaries because Agent Utils already exposes a
  convenient Spring AI tool.
- Do not expose Agent Utils types as Codegeist public contracts unless a later
  task explicitly chooses that dependency boundary.
- Do not re-run Graphify or reload the packed source directly from task phases;
  use `/ask-project spring-ai-agent-utils ...` so source deep dives stay isolated.

## Example Usage

```text
/specify-task <task-ref> docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md
/plan-task <task-ref> docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md
/solve-task <task-ref> docs/tasks/hints/spring-ai-agent-utils-phase-guidance.md
```
