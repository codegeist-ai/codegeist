---
description: Run the full task workflow through specify, plan, and solve
agent: build
---

Run the complete Codegeist task-to-implementation workflow for one task together
with the user.

Follow @.opencode/rules/task-workflow.md,
@.opencode/rules/language-policy.md,
@.opencode/rules/software-documentation.md,
@.opencode/rules/software-tests.md, @.opencode/rules/chat.md, and
@.oc_local/rules/codegeist-task-specification.md.

This command orchestrates the local phase commands. Keep phase-specific behavior
in `/specify-task`, `/plan-task`, `/solve-task`, and
`.oc_local/rules/codegeist-task-specification.md` instead of duplicating detailed
phase rules here.

User input:

```text
$ARGUMENTS
```

Expected syntax:

```text
/work-task <task-ref> [context/instructions]
```

Examples:

```text
/work-task T002_01
/work-task T001_25 plan only the Maven baseline before solving
/work-task docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_01_align-codegeist-build-baseline.md keep package moves out of this pass
```

## Purpose

Use this command when the user wants the complete workflow to run from task
clarification through detailed planning and verified implementation.

The command runs these phases in order with the same argument contract:

```text
/specify-task <task-ref> [context/instructions]
/plan-task <task-ref> [context/instructions]
/specify-task <task-ref> [context/instructions]
/solve-task <task-ref> [context/instructions]
```

The second `/specify-task` pass is intentional. It checks the planned
implementation task after `/plan-task` has created or sharpened it, before
runtime code changes begin.

## Workflow

1. Parse the first argument as the initial task reference and treat remaining
   arguments as user-provided context or instructions for every phase. Stop if the
   task reference is missing.
2. Resolve the initial task reference by exact repo-relative path, exact task
   filename, exact task folder name, or exact task id such as `T001` or `T002_01`.
   Stop and list options if the reference is ambiguous. When the resolved
   reference is a directory, require `<directory>/task.md`.
3. Read the initial task, its parent `task.md` when present, directly relevant
   child tasks, dependencies, and local task workflow rules before starting.
4. Run the `/specify-task <task-ref> [context/instructions]` phase semantics on
   the resolved task. Record that phase status in the task according to the phase
   command.
5. Run the `/plan-task <task-ref> [context/instructions]` phase semantics with the
   same task reference and context. If this phase creates or identifies a
   different concrete implementation task, switch the workflow target to that task
   for the remaining phases.
6. Stop and report if planning leaves multiple possible implementation tasks,
   unresolved material decisions, or no safe concrete implementation task.
7. Run the `/specify-task <task-ref> [context/instructions]` phase semantics again
   on the concrete implementation task selected by planning. Use the same context
   or instructions and record the new specification status in that task.
8. Stop and report if the implementation task is still missing required planning
   details, open decisions block implementation, or the task status recommends
   another `/plan-task` pass before solving.
9. Run the `/solve-task <task-ref> [context/instructions]` phase semantics on the
   concrete implementation task. This phase depends on the current `/plan-task`
   status and must record solve status, verification results, and acceptance
   criteria status in the task.
10. Update directly affected task files when decisions change during any phase so
    dependencies, non-goals, acceptance criteria, implementation plans, and
    follow-up boundaries stay current.
11. Update `docs/memory-bank/chat.md` only when the workflow changes durable
    project state or future sessions would otherwise miss important context.
12. Run the verification required by the final solve phase. At minimum, run:

```bash
git --no-pager diff --check
```

13. Report the initial task, final implementation task, context or instructions
    used, phase statuses written, discovered hints considered, files changed,
    verification commands and results, acceptance criteria status, open decisions,
    and remaining follow-ups.

## Stop Conditions

Stop before implementation and report the exact blocker when:

- the task reference is ambiguous or points to a non-canonical task directory
- `/specify-task` determines the task is too vague to plan safely
- `/plan-task` finds multiple viable implementation tasks and needs a user choice
- `/plan-task` cannot identify or create a concrete implementation task
- the planned task is missing classes, files, implementation steps, acceptance
  criteria, or verification details needed by `/solve-task`
- the provided context changes scope enough that another `/specify-task` or
  `/plan-task` pass is required before solving

## Rules

- Use the same `<task-ref> [context/instructions]` argument contract for every
  phase.
- Do not bypass the phase commands' requirements. Each phase must discover hints,
  honor dependencies, and write its own phase status in the target task.
- Do not silently solve a source task when `/plan-task` created or selected a
  different concrete implementation task. Switch to the concrete task and report
  the switch.
- Do not continue into `/solve-task` when `/plan-task` or the second
  `/specify-task` leaves material implementation decisions open.
- Keep durable documentation in English even when the chat is not in English.
