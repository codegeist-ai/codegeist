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
   list options if the reference is ambiguous. When the resolved reference is a
   directory, require `<directory>/task.md` and use that file as the target task;
   stop if the directory has no canonical `task.md`.
3. If the resolved target is a task file under a task directory, check whether
   that task directory or its nearest owning task directory has a canonical
   `task.md`. Read that parent `task.md` before specifying the task. If a
   referenced task directory has a `tasks/` child directory but no `task.md`, stop
   and report the broken task structure instead of guessing the parent.
4. Read the target task first. If the task is only a stub without a meaningful
   description, goal, or scope, stop and ask the user to describe the intended
   task before specifying it further.
5. Read the directly relevant parent task and architecture docs before editing,
   especially:
   - `docs/developer/codegeist-opencode-parity.md`
   - the target task's parent `task.md` when present
   - directly adjacent child tasks when they define dependencies or boundaries
6. If the target task or its parent `task.md` contains a `Default Solve Hints`
   section, treat every listed hint file as default task guidance for this
   specification pass. Resolve and read those hints before deciding whether the
   task needs sharpening. Use them to clarify scope, boundaries, dependencies,
   verification, and implementation-readiness questions only; do not solve or
   implement the task from hint content.
7. Treat this command as a Codegeist-specific specification pass, not an
   implementation pass.
8. Preserve the task's intended scope. Do not solve it, add runtime code, change
   build files, or implement the described behavior.
9. Check whether the task answers the Codegeist/OpenCode migration questions
   relevant to its topic:
   - What OpenCode concept is being translated?
   - What is the Java/Spring/Spring AI/Vaadin/JBang/PF4J equivalent?
   - What belongs to Codegeist Runtime versus CLI, server, Vaadin, provider,
     tool, permission, workspace, storage, plugin, or script boundaries?
   - What is MVP and what is later scope?
   - What must stay independent from OpenCode's Bun/TypeScript/API/storage shape?
   - What decisions are required before implementation can start?
10. Deepen the task where needed:
   - sharpen goal, context, scope, non-goals, deliverable, acceptance criteria,
     verification, dependencies, and open questions
   - add migration questions and answers when useful
   - add Codegeist-specific architecture decisions and boundary rules
   - add implementation-readiness questions, but keep them at specification depth
11. If the task changes a central architecture document, update that document in
   the same pass so task and architecture docs stay consistent.
12. Update `docs/memory-bank/chat.md` only when the specification changes durable
    project state or current task focus.
13. Run a targeted documentation check, at minimum:

```bash
git --no-pager diff --check
```

14. Report updated files, parent `task.md` considered, default hints considered,
    what was clarified, what remains open, and any follow-up task that should be
    specified next.

## Repeatability

This command must be safe to run repeatedly on the same task.

- On each run, re-read the current task and related architecture docs before
  deciding whether changes are needed.
- On each run, re-read any parent-declared `Default Solve Hints` and use them as
  specification guidance when they apply.
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
