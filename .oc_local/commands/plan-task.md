---
description: Plan one implementation task interactively from an existing task
agent: build
---

Plan one concrete implementation task from an existing repo task together with
the user.

Follow @.opencode/rules/task-workflow.md,
@.opencode/rules/language-policy.md,
@.opencode/rules/software-documentation.md,
@.opencode/rules/software-tests.md, and @.opencode/rules/chat.md.

Also read and apply relevant project-specific rules under `@.oc_local/rules/`
when they exist. Keep project-specific behavior in those rules instead of
hard-coding it into this command.

User input:

```text
$ARGUMENTS
```

Expected syntax:

```text
/plan-task <source-task-ref-or-file> [focus/context]
```

Examples:

```text
/plan-task T001_25
/plan-task docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_25_define-implementation-backlog.md
/plan-task docs/tasks/T001_define-codegeist-opencode-feature-architecture/task.md align build baseline
```

## Purpose

Use this command when an existing architecture, planning, backlog, or solution
task should become one concrete implementation plan. The command is deliberately
interactive: it reads the source, derives implementation options, collaborates
with the user on the chosen slice, then writes or sharpens one self-contained
task file with a detailed plan for what should be implemented.

In the standard task-to-implementation workflow, this command runs after
`/specify-task` and before `/solve-task`. It may be run more than once while the
implementation slice is still being prepared; when a matching implementation
task already exists, sharpen that task instead of creating a duplicate unless the
user explicitly confirms a distinct new task.

This command creates or updates task documentation only. It does not implement
runtime code, change build files, or solve the planned task. It may update an
existing implementation task when new decisions, constraints, classes, files,
verification requirements, or user instructions change the plan.

## Workflow

1. Parse the first argument as the source task reference or task file. Stop if it
   is missing.
2. Resolve the source by exact repo-relative path, exact task filename, exact
   task folder name, or exact task id such as `T001` or `T001_25`. If the source
   is a directory, require and use its canonical `task.md`. Stop and list options
   if the reference is ambiguous, and stop if a referenced task directory has no
   `task.md`.
3. Treat remaining arguments as user-provided implementation focus or context.
   When this command is run repeatedly, use that focus or context as the reason
   for sharpening the existing implementation task; ask for a focused instruction
   when the reason for another pass is unclear.
4. If the resolved source is a task file under a task directory, check whether
   that task directory or its nearest owning task directory has a canonical
   `task.md`. Read that parent `task.md` before deriving implementation options.
   If a referenced task directory has a `tasks/` child directory but no `task.md`,
   stop and report the broken task structure instead of guessing the parent.
5. Read the source task before proposing any task. Also read directly relevant
   task docs, including the parent `task.md`, child tasks when the source is an
   epic, dependency tasks, adjacent open tasks, and task templates or README files
   when present.
6. Scan the source task, its parent `task.md`, child tasks when the source is an
   epic, and adjacent or dependency tasks for hint references, including
   `Default Solve Hints`, `Hints`, `Guidance`, and repo-relative paths under
   `docs/tasks/hints/`. Resolve and read those hints before deriving or
   sharpening implementation plans. Stop and ask for clarification if a referenced
   hint is ambiguous, unreadable, or points outside the workspace.
7. Read central architecture or planning docs named by the source task. For
   Codegeist/OpenCode implementation work, read
   `docs/developer/codegeist-opencode-parity.md` when it exists.
8. Inspect existing top-level tasks under `docs/tasks/` so the new task uses the
   next available `TNNN` id and does not duplicate an existing implementation
   task.
9. Start collaboratively. If the source can produce more than one safe slice,
   present 2-3 concrete implementation plan options before writing files. Each
   option should include:
   - proposed task title
   - goal
   - concrete solution direction
   - target files, packages, classes, interfaces, records, configuration files, or
     tests likely to be needed
   - verification command or strategy
   - dependencies and tradeoffs
10. Ask focused questions only when the implementation slice, public contract,
   target files, verification depth, or boundary with later tasks is materially
   unclear. Otherwise choose the smallest correct details from repo conventions.
11. If the user chooses an option, a variant, or provides a clearer focus, refine
   exactly one implementation task. Do not silently create multiple tasks.
12. If a matching implementation task already exists, offer to sharpen that task
    with the new source information, focus, or user instructions instead of
    creating a duplicate. Create a duplicate only when the user explicitly
    confirms the distinction.
13. Create the next top-level implementation task as
    `docs/tasks/TNNN_<slug>.md` unless the user explicitly asks for a child task
    under a parent. When creating under a parent task directory, require the
    parent directory's canonical `task.md` before writing under `tasks/`. If the
    intended parent still exists as a flat `TNNN_<slug>.md` file, migrate it to
    `TNNN_<slug>/task.md` first according to @.opencode/rules/task-workflow.md.
14. Make the task self-contained and implementation-ready. Include at least:
    - title and source reference
    - goal
    - context
    - concrete solution
    - scope
    - planned classes, interfaces, records, configuration files, packages, tests,
      and documentation files to add or change
    - implementation steps in expected order
    - acceptance criteria
    - verification plan, including expected commands and what they prove
    - dependencies
    - non-goals
    - open questions, or `None` when no decision is pending
    - plan workflow handoff with the resolved source task, source parent
      `task.md`, user focus or context, selected option, duplicate check result,
      discovered hints, related context files read, and recommended next command
    - planning note with the selected option and any user decision
15. Keep durable documentation in English even when the chat is not in English.
16. Update `docs/memory-bank/chat.md` when creating the task changes the active
    project focus, current task set, or durable workflow state.
17. Run targeted verification after writing the task. At minimum, run:

```bash
git --no-pager diff --check
```

18. Report the source task, user focus or context considered, source parent
    `task.md` considered, created or sharpened task file, selected option,
    discovered hints considered, detailed implementation plan, verification
    result, user decisions, and the recommended next command such as
    `/specify-task TNNN <context>` or `/solve-task TNNN`.

## Collaboration Defaults

Prefer narrow implementation slices that can be solved independently. Good first
implementation plans usually validate one architecture boundary, risk, or MVP
capability with explicit class/file targets, ordered implementation steps, and a
verification command that proves the acceptance criteria.

When deriving from a backlog table, do not automatically choose the first row if
the user provided no focus and multiple rows are viable. Present options and wait
for the user's choice.

When deriving from a completed architecture task, translate the documented
architecture into an executable plan rather than copying the architecture text.
The task should tell the next agent what to build first, which classes or files
are expected, where to build them, how to verify them, and what not to include.

## Rules

- Create or update planning documentation only; do not implement source code or
  tests in this command.
- Treat repeated runs as plan refinement. Update the matching task when decisions
  changed, new instructions arrived, or class/file/test expectations became more
  precise.
- Create at most one implementation task unless the user explicitly asks for
  more.
- Do not create broad epic-level follow-ups when one narrow implementation task
  would be enough.
- Do not write task docs outside `docs/tasks/`.
- Do not edit `.opencode/commands/task.md`; this command is project-specific and
  belongs to `.oc_local/commands/`.
- Keep the command generic. Move project-specific constraints, file lists,
  architecture references, and domain decisions into local rules under
  `@.oc_local/rules/`.
