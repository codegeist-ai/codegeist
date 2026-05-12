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
/solve-task <task-ref> [hint-file ...] [additional context]
```

Examples:

```text
/solve-task T001_07
/solve-task docs/tasks/T001_example/tasks/T001_07_define-event-model.md
/solve-task T001_07 docs/notes/event-model-review.md
/solve-task T001_07 focus on the smallest documentation change first
```

## Purpose

Use this command when a task already exists and should move from specification
to solution. The command is intentionally generic: it resolves the task, reads
the relevant repo context, proposes concrete starting options, collaborates with
the user on important decisions, implements the chosen path, and updates task
state and related docs when needed.

## Workflow

1. Parse the first argument as the target task reference. Treat any remaining
   arguments that resolve to existing files as optional hint files for the
   solution pass, and treat the rest as user-provided context. Stop if the task
   reference is missing.
2. Resolve the task reference by exact repo-relative path, exact task filename,
   exact task folder name, or exact task id such as `T001` or `T001_07`. Stop and
   list options if the reference is ambiguous.
3. Read the target task before making changes. Also read directly relevant task
   docs, including the parent task, child tasks, dependency tasks, adjacent open
   tasks, and task templates or README files when present.
4. Read every hint file after resolving the task and before proposing solution
   options. Hint files are living guidance and can contain user notes, design
   sketches, research, review feedback, or examples for how similar tasks should
   be solved. Stop and ask for clarification if a hint-file argument is
   ambiguous, unreadable, or appears to point outside the workspace.
5. Read relevant repo and project-specific rules before implementation,
   especially local overlays under `@.oc_local/rules/`.
6. Prefer solving leaf tasks. If the target has open child tasks that should stay
   separate, stop and report the next child task instead of silently collapsing
   that structure.
7. Inspect the files named by the task, hint files, acceptance criteria,
   dependencies, and project-specific rules. Do not assume the task text is fully
   current.
8. Maintain a precise working note inside the target task throughout the
   solution pass. In Plan Mode, document exactly what should be done, which
   options were proposed, which user decisions are needed, and what should happen
   once Build Mode starts. In Build Mode, document what was done, why decisions
   were made, what changed, what remains open, and what the next agent or user
   must do. Keep this note current after each meaningful interactive decision.
9. Start collaboratively unless the task is trivial and already has a single
   obvious implementation path. Offer 2-3 concrete starting options derived from
   the current task, hint files, and repo context. Include tradeoffs when they
   matter.
10. Ask focused questions only for decisions that materially affect scope,
   behavior, public contracts, task boundaries, or other open tasks. Otherwise,
   choose the smallest correct path and continue.
11. Implement the chosen solution with the smallest reasonable change. Update
   tests, documentation, task status, and implementation notes according to the
   task's acceptance criteria and the repo rules.
12. When a decision affects other open tasks, update those task files in the same
    pass so their scope, dependencies, acceptance criteria, or non-goals remain
    accurate. Do not rewrite unrelated tasks for style.
13. If solving the task reveals a reusable insight that would help solve future
    tasks, update the relevant hint file in the same pass. Keep hint updates
    concise and broadly applicable; avoid task-specific conclusions, temporary
    details, or duplicating task status.
14. If solving the task reveals a reusable workflow gap, minimally update this
    command or the most relevant project-specific rule. Keep generic workflow in
    this command and project-specific guidance in `@.oc_local/rules/`.
15. Update `docs/memory-bank/chat.md` only when the solution changes durable
    project state or future sessions would otherwise miss important context.
16. Run targeted verification that matches the task. At minimum, run:

```bash
git --no-pager diff --check
```

17. Report the updated files, hint files considered or updated, implemented
    solution, verification result,
    decisions made with the user, affected tasks, and remaining follow-ups.

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
- Keep durable documentation in English unless the repository records a specific
  language exception.
- Keep the command generic. Move project-specific constraints, file lists,
  architecture references, and domain decisions into local rules under
  `@.oc_local/rules/`.
- Keep hint files dynamic but generic. They should capture durable solving
  guidance, not a transcript of one task or a narrow implementation checklist.
