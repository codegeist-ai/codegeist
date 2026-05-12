# T002_09 Add Controlled Shell Verification Tool

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_12`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

## Goal

Implement the first controlled shell verification tool contract for approved,
bounded, non-destructive commands.

## Context

Shell execution is a high-risk tool. The MVP needs verification/build/test command
support, but Plan mode denies shell execution and destructive commands require
explicit user intent.

## Concrete Solution

1. Add shell command request/result contracts with argv or snippet, cwd, env
   policy, timeout, stdin posture, output limit, exit code, and failure reason.
2. Require Build mode, permission approval, workspace-validated cwd, and bounded
   output before execution.
3. Implement only a safe executor port or a minimal local executor for tests with
   non-destructive commands.
4. Add tests for Plan-mode denial, approval-required behavior, timeout/failure
   result shape, output truncation metadata, and cwd validation.

## Scope

- `ai.codegeist.tool` shell tool contracts
- `ai.codegeist.permission`
- `ai.codegeist.workspace`
- optional process executor adapter if kept small and testable
- focused tests

## Acceptance Criteria

- Shell execution is represented as a permission-gated tool request.
- Plan mode shell execution is denied by default.
- Command results include exit code, summary, output reference/truncation, and
  typed failure information.
- Destructive commands are not inferred as safe from generic approval.
- Tests cover non-destructive approved and denied paths.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_07`.

## Non-Goals

- Do not implement PTY, live terminal UI, remote execution, JBang execution,
  sandboxing, or broad command allowlists.

## Open Questions

- Should the first implementation include a real process runner or only executor
  contracts plus a fake test executor?

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task remains narrowly scoped to bounded verification commands and should not
  implement PTY, remote execution, JBang execution, or broad sandboxing.
- Source research should focus on OpenCode shell/tool permission flow and output
  handling before choosing the first executor shape.

## Creation Note

Status: open.

Derived from shell-execution architecture as its own task because shell safety is
a high blocking risk and should stay separate from patch/edit work.
