---
description: Create one implementation task interactively from an existing task
agent: build
---

Create one concrete implementation task from an existing repo task together with
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
/create-implementation-task <source-task-ref-or-file> [focus/context]
```

Examples:

```text
/create-implementation-task T001_25
/create-implementation-task docs/tasks/T001_define-codegeist-opencode-feature-architecture/tasks/T001_25_define-implementation-backlog.md
/create-implementation-task docs/tasks/T001_define-codegeist-opencode-feature-architecture/task.md align build baseline
```

## Purpose

Use this command when an existing architecture, planning, backlog, or solution
task should become one concrete implementation task. The command is deliberately
interactive: it reads the source, derives implementation options, collaborates
with the user on the chosen slice, then writes one self-contained task file with
a concrete solution direction.

This command creates task documentation only. It does not implement runtime code,
change build files, or solve the newly created task.

## Workflow

1. Parse the first argument as the source task reference or task file. Stop if it
   is missing.
2. Resolve the source by exact repo-relative path, exact task filename, exact
   task folder name, or exact task id such as `T001` or `T001_25`. If the source
   is a directory, require and use its canonical `task.md`. Stop and list options
   if the reference is ambiguous, and stop if a referenced task directory has no
   `task.md`.
3. Treat remaining arguments as user-provided implementation focus or context.
4. If the resolved source is a task file under a task directory, check whether
   that task directory or its nearest owning task directory has a canonical
   `task.md`. Read that parent `task.md` before deriving implementation options.
   If a referenced task directory has a `tasks/` child directory but no `task.md`,
   stop and report the broken task structure instead of guessing the parent.
5. Read the source task before proposing any task. Also read directly relevant
   task docs, including the parent `task.md`, child tasks when the source is an
   epic, dependency tasks, adjacent open tasks, and task templates or README files
   when present.
6. Read central architecture or planning docs named by the source task. For
   Codegeist/OpenCode implementation work, read
   `docs/developer/codegeist-opencode-parity.md` when it exists.
7. Inspect existing top-level tasks under `docs/tasks/` so the new task uses the
   next available `TNNN` id and does not duplicate an existing implementation
   task.
8. Start collaboratively. If the source can produce more than one safe slice,
   present 2-3 concrete implementation options before writing files. Each option
   should include:
   - proposed task title
   - goal
   - concrete solution direction
   - target files or packages
   - verification command or strategy
   - dependencies and tradeoffs
9. Ask focused questions only when the implementation slice, public contract,
   target files, verification depth, or boundary with later tasks is materially
   unclear. Otherwise choose the smallest correct details from repo conventions.
10. If the user chooses an option, a variant, or provides a clearer focus, refine
   exactly one implementation task. Do not silently create multiple tasks.
11. If a matching `T002+` implementation task already exists, offer to sharpen
    that task instead of creating a duplicate. Create a duplicate only when the
    user explicitly confirms the distinction.
12. Create the next top-level implementation task as
    `docs/tasks/TNNN_<slug>.md` unless the user explicitly asks for a child task
    under a parent. When creating under a parent task directory, require the
    parent directory's canonical `task.md` before writing under `tasks/`. If the
    intended parent still exists as a flat `TNNN_<slug>.md` file, migrate it to
    `TNNN_<slug>/task.md` first according to @.opencode/rules/task-workflow.md.
13. Make the task self-contained. Include at least:
    - title and source reference
    - goal
    - context
    - concrete solution
    - scope
    - target files or packages
    - acceptance criteria
    - verification
    - dependencies
    - non-goals
    - open questions, or `None` when no decision is pending
    - creation note with the selected option and any user decision
14. Keep durable documentation in English even when the chat is not in English.
15. Update `docs/memory-bank/chat.md` when creating the task changes the active
    project focus, current task set, or durable workflow state.
16. Run targeted verification after writing the task. At minimum, run:

```bash
git --no-pager diff --check
```

17. Report the source task, source parent `task.md` considered, created task file,
    selected option, concrete
    solution, verification result, user decisions, and the recommended next
    command such as `/solve-task TNNN`.

## Collaboration Defaults

Prefer narrow implementation slices that can be solved independently. Good first
implementation tasks usually validate one architecture boundary, risk, or MVP
capability with explicit file targets and a small verification command.

When deriving from a backlog table, do not automatically choose the first row if
the user provided no focus and multiple rows are viable. Present options and wait
for the user's choice.

When deriving from a completed architecture task, translate the documented
architecture into an executable task rather than copying the architecture text.
The task should tell the next agent what to build first, where to build it, how
to verify it, and what not to include.

## Rules

- Create documentation only; do not implement source code or tests in this
  command.
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
