# T002_09 Describe Controlled Shell Verification Tool

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_12`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

## Goal

Describe the first controlled shell verification tool contract for approved,
bounded, non-destructive commands without adding a process executor yet.

## Context

Shell execution is a high-risk tool. The MVP needs verification/build/test command
support, but Plan mode denies shell execution and destructive commands require
explicit user intent.

## Concrete Solution

1. Create or update `docs/developer/shell-verification-contracts.md` as the
   future controlled shell verification blueprint.
2. Define future shell command request/result contracts with argv or snippet, cwd,
   env policy, timeout, stdin posture, output limit, exit code, and failure
   reason.
3. Describe the required Build mode, permission approval, workspace-validated cwd,
   and bounded output before execution.
4. Document future tests for Plan-mode denial, approval-required behavior,
   timeout/failure result shape, output truncation metadata, and cwd validation.
5. Include OpenCode source evidence, future file maps, diagrams, and illustrative
   Java snippets in markdown only.

## Scope

- `docs/developer/shell-verification-contracts.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Shell execution is specified as a permission-gated tool request.
- Plan mode shell execution is specified as denied by default.
- Command results include exit code, summary, output reference/truncation, and
  typed failure information in the blueprint.
- Destructive commands are not inferred as safe from generic approval.
- Future non-destructive approved and denied tests are described, but no Java
  source, tests, process runner, or executor port is created by this task.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_07`.

## Non-Goals

- Do not create Java source files, empty package directories, shell tests, process
  executor ports, or local process runners.
- Do not implement PTY, live terminal UI, remote execution, JBang execution,
  sandboxing, or broad command allowlists.

## Open Questions

- Should the first implementation include a real process runner or only executor
   contracts plus a fake test executor?

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later shell-tool implementation task instead of creating shell
  execution source packages now.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task remains narrowly scoped to bounded verification commands and should not
  implement PTY, remote execution, JBang execution, or broad sandboxing.
- Source research should focus on OpenCode shell/tool permission flow and output
  handling before choosing the first executor shape.

## Dependency Impact Notes

- Finalized `T002_07_add_tool_permission_workspace_contracts.md` defines the
  generic tool request, permission decision, workspace target validation, bounded
  result, output-reference, and event/session projection boundaries. This task
  should specialize those boundaries for controlled shell verification instead of
  redefining generic tool policy.
- Shell execution remains denied in Plan mode and approval-gated in Build mode.
  Permission approval must not override destructive-command posture, workspace cwd
  denial, secret-like target denial, or descriptor capability limits.
- Shell output should use bounded summaries and output references from the
  `T002_07` blueprint; session parts and events must not retain unbounded stdout,
  stderr, environment data, or command payloads.

## Creation Note

Status: open.

Derived from shell-execution architecture as its own task because shell safety is
a high blocking risk and should stay separate from patch/edit work.
