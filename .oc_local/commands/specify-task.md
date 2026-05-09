---
description: Specify an existing Codegeist architecture task
agent: build
---

Review the current repository state and existing task docs.

Follow @.opencode/rules/task-workflow.md,
@.opencode/rules/language-policy.md,
@.opencode/rules/software-documentation.md,
@.opencode/rules/chat.md, and the `specify` behavior from
@.opencode/commands/task.md.

User input:

```text
$ARGUMENTS
```

Expected syntax:

```text
/specify-task <task-ref>
```

Examples:

```text
/specify-task T001_07
/specify-task docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_07_define-event-model.md
```

## Purpose

Use this command to run a repeatable Codegeist-specific specification pass over
an existing task. The target task must already have a meaningful description,
goal, or scope. This command deepens and checks the task against current
Codegeist/OpenCode migration decisions; it does not create, solve, or implement
the task.

## Workflow

1. Parse the first argument as the target task reference. Stop if it is missing.
2. Resolve the task reference by exact repo-relative path, exact task filename,
   exact task folder name, or exact task id such as `T001` or `T001_07`. Stop and
   list options if the reference is ambiguous.
3. Read the target task first. If the task is only a stub without a meaningful
   description, goal, or scope, stop and ask the user to describe the intended
   task before specifying it further.
4. Read the directly relevant parent task and architecture docs before editing,
   especially:
   - `docs/developer/codegeist-opencode-parity.md`
   - the target task's parent `task.md` when present
   - directly adjacent child tasks when they define dependencies or boundaries
5. Treat this command as a Codegeist-specific specification pass, not an
   implementation pass.
6. Preserve the task's intended scope. Do not solve it, add runtime code, change
   build files, or implement the described behavior.
7. Check whether the task answers the Codegeist/OpenCode migration questions
   relevant to its topic:
   - What OpenCode concept is being translated?
   - What is the Java/Spring/Spring AI/Vaadin/JBang/PF4J equivalent?
   - What belongs to Codegeist Runtime versus CLI, server, Vaadin, provider,
     tool, permission, workspace, storage, plugin, or script boundaries?
   - What is MVP and what is later scope?
   - What must stay independent from OpenCode's Bun/TypeScript/API/storage shape?
   - What decisions are required before implementation can start?
8. Deepen the task where needed:
   - sharpen goal, context, scope, non-goals, deliverable, acceptance criteria,
     verification, dependencies, and open questions
   - add migration questions and answers when useful
   - add Codegeist-specific architecture decisions and boundary rules
   - add implementation-readiness questions, but keep them at specification depth
9. If the task changes a central architecture document, update that document in
   the same pass so task and architecture docs stay consistent.
10. Update `docs/memory-bank/chat.md` only when the specification changes durable
    project state or current task focus.
11. Run a targeted documentation check, at minimum:

```bash
git --no-pager diff --check
```

12. Report updated files, what was clarified, what remains open, and any
    follow-up task that should be specified next.

## Repeatability

This command must be safe to run repeatedly on the same task.

- On each run, re-read the current task and related architecture docs before
  deciding whether changes are needed.
- If the task already answers the Codegeist-specific specification questions, do
  not rewrite it just for style or churn. Report that no changes were needed.
- If repository context, architecture decisions, parent task scope, or related
  tasks changed since the last run, update only the stale or missing parts.
- Prefer additive clarification or precise replacement of stale sections over
  duplicating existing sections.
- Keep existing correct decisions intact. Rewrite only obsolete, vague,
  contradictory, or incomplete content.
- Do not append repeated `Verification Result` or completion notes on every run.
  Update an existing verification/status note only when the verification result
  materially changed.

## Rules

- Do not create a new task.
- Do not solve the task.
- Do not split the task unless the current task is clearly too broad to remain
  safe or executable.
- Do not write task docs outside `docs/tasks/`.
- Do not edit `.opencode/commands/task.md`; this command is project-specific and
  belongs to `.oc_local/commands/`.
- Keep durable documentation in English unless the repository records a specific
  language exception for the target documentation tree.
