---
description: Solve an existing repo task collaboratively
agent: build
---

Solve an existing task from the repo's task documentation together with the
user.

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
/solve-task <task-ref> [hint-file ...] [context/instructions]
```

Examples:

```text
/solve-task T001_07
/solve-task docs/tasks/T001_example/tasks/T001_07_define-event-model.md
/solve-task T001_07 docs/notes/event-model-review.md
/solve-task T001_07 focus on the smallest documentation change first
/solve-task T002_01 implement only the Maven baseline and defer package moves
```

## Purpose

Use this command when a task already exists and should move from implementation
plan to verified solution. The command is intentionally generic: it resolves the
task, reads the relevant repo context, proposes concrete starting options,
collaborates with the user on important decisions, implements the chosen path,
runs enough verification to prove the acceptance criteria, and updates task state
and related docs when needed.

In the standard task-to-implementation workflow, this is the implementation step
after any needed iterative `/specify-task` and `/plan-task`
preparation passes.

## Workflow

1. Parse the first argument as the target task reference. Treat any remaining
   arguments that resolve to existing files as optional hint files for the
   solution pass, and treat the rest as user-provided context or implementation
   instructions. Stop if the task reference is missing.
2. Resolve the task reference by exact repo-relative path, exact task filename,
   exact task folder name, or exact task id such as `T001` or `T001_07`. Stop and
   list options if the reference is ambiguous. When the resolved reference is a
   directory, require `<directory>/task.md` and use that file as the target task;
   stop if the directory has no canonical `task.md`.
3. If the resolved target is a task file under a task directory, check whether
   that task directory or its nearest owning task directory has a canonical
   `task.md`. Read that parent `task.md` before proposing solution options. If a
   referenced task directory has a `tasks/` child directory but no `task.md`, stop
   and report the broken task structure instead of guessing the parent.
4. Read the target task before making changes. Also read directly relevant task
   docs, including the parent `task.md`, child tasks, dependency tasks, adjacent
   open tasks, and task templates or README files when present.
5. Scan the target task, its parent `task.md`, child tasks when relevant, and
   adjacent or dependency tasks for hint references, including `Default Solve
   Hints`, `Hints`, `Guidance`, and repo-relative paths under
   `docs/tasks/hints/`. Treat every discovered hint file as an implicit hint file
   for this solve pass, together with any explicit hint-file arguments.
6. Read every discovered or explicit hint file after resolving the task and
   before proposing solution options. Hint files are living guidance and can
   contain user notes, design sketches, research, review feedback, or examples
   for how similar tasks should be solved. Stop and ask for clarification if a
   hint-file argument or discovered hint is ambiguous, unreadable, appears to
   point outside the workspace, or does not exist.
7. Read relevant repo and project-specific rules before implementation,
   especially local overlays under `@.oc_local/rules/`.
8. Prefer solving leaf tasks. If the target has open child tasks that should stay
   separate, stop and report the next child task instead of silently collapsing
   that structure.
9. Inspect the files named by the task, hint files, acceptance criteria,
   dependencies, and project-specific rules. Do not assume the task text is fully
   current.
10. Apply any user-provided context or implementation instructions as the main
   focus for this solve pass. If the instruction changes scope materially,
   update the task first or recommend another `/specify-task` pass before
   implementation.
11. Maintain a precise working note inside the target task throughout the
   solution pass. In Plan Mode, document exactly what should be done, which
   options were proposed, which user decisions are needed, and what should happen
   once Build Mode starts. In Build Mode, document what was done, why decisions
   were made, what changed, what remains open, and what the next agent or user
   must do. Keep this note current after each meaningful interactive decision.
12. Start collaboratively unless the task is trivial and already has a single
   obvious implementation path. Offer 2-3 concrete starting options derived from
   the current task, hint files, and repo context. Include tradeoffs when they
   matter.
13. Ask focused questions only for decisions that materially affect scope,
   behavior, public contracts, task boundaries, or other open tasks. Otherwise,
   choose the smallest correct path and continue.
14. Implement the chosen solution with the smallest reasonable change. Update
   tests, documentation, task status, and implementation notes according to the
   task's acceptance criteria and the repo rules. If decisions change while
   solving, update the task plan, acceptance criteria, non-goals, or affected
   follow-up tasks before continuing.
15. When a decision affects other open tasks, update those task files in the same
    pass so their scope, dependencies, acceptance criteria, or non-goals remain
    accurate. Do not rewrite unrelated tasks for style.
16. If solving the task reveals a reusable insight that would help solve future
    tasks, update the relevant hint file in the same pass. Keep hint updates
    concise and broadly applicable; avoid task-specific conclusions, temporary
    details, or duplicating task status.
17. If solving the task reveals a reusable workflow gap, minimally update this
    command or the most relevant project-specific rule. Keep generic workflow in
    this command and project-specific guidance in `@.oc_local/rules/`.
18. Update `docs/memory-bank/chat.md` only when the solution changes durable
    project state or future sessions would otherwise miss important context.
19. Run comprehensive verification for the task, including every relevant command
    named by the task and enough additional checks to prove all acceptance
    criteria are satisfied. At minimum, run:

```bash
git --no-pager diff --check
```

20. Report the updated files, explicit and discovered hint files considered or
    updated, user context or implementation instructions considered, implemented
    solution, verification commands and results,
    acceptance criteria status, decisions made with the user, affected tasks, and
    remaining follow-ups.

## Collaboration Defaults

Offer options that are specific enough for the user to choose quickly. Good
starting options include:

- Solve the narrow acceptance criteria first and defer broader cleanup.
- Clarify or update related open tasks before editing implementation files.
- Update the central documentation or contract first, then align tests or task
  notes.

Do not ask the user to restate information already present in the task docs.
Do not stop for minor choices that can be handled by existing repo conventions.
When a new lesson applies beyond the current task, prefer updating an existing
hint file over scattering the same guidance across several task files.
Treat solving as an interactive process that must be resumable from the target
task file itself, not only from chat history.

## Rules

- Do not create a new task unless the current task is too broad to solve safely
  and the split is necessary for resumability.
- Do not collapse unresolved child tasks into the parent solution unless the user
  explicitly chooses that direction.
- Do not write task docs outside `docs/tasks/`.
- Do not leave the target task without an up-to-date solution note. The task
  must say whether work is planned, in progress, completed, blocked, or awaiting
  user decision.
- Update the target task when solving changes decisions, scope, acceptance
  criteria, implementation plan, or follow-up work. The task file is the durable
  handoff, not just a static input.
- Keep durable documentation in English unless the repository records a specific
  language exception.
- Keep the command generic. Move project-specific constraints, file lists,
  architecture references, and domain decisions into local rules under
  `@.oc_local/rules/`.
- Keep hint files dynamic but generic. They should capture durable solving
  guidance, not a transcript of one task or a narrow implementation checklist.
- Discover applicable hints from the task files themselves on every run instead
  of relying only on explicit command arguments.
