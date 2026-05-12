# T002_07 Add Tool Permission Workspace Contracts

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_09`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

## Goal

Implement the first tool descriptor, tool request/result, permission request, and
workspace validation contracts before concrete side-effecting tools exist.

## Context

The architecture requires tools to flow through runtime-owned descriptors,
mode checks, permission policy, workspace validation, events, and session result
parts. Spring AI tool callbacks, PF4J plugins, and JBang scripts must adapt to
these contracts instead of bypassing them.

## Concrete Solution

1. Add `ToolDescriptor`, `ToolRequest`, `ToolResult`, `ToolFailure`, capability
   category, and result summary contracts.
2. Add `PermissionRequest`, `PermissionDecision`, decision scope, and audit
   metadata contracts.
3. Connect tool request evaluation to mode compatibility and permission need, but
   do not execute concrete file/shell/network actions.
4. Use workspace validation contracts for path-scoped requests.
5. Add tests for mode denial, permission-required decisions, allowed read-only
   descriptors, and workspace path validation calls.

## Scope

- `ai.codegeist.tool`
- `ai.codegeist.permission`
- `ai.codegeist.workspace`
- `ai.codegeist.event` references only through contracts
- focused tests

## Acceptance Criteria

- Tool descriptors can be classified before execution.
- Permission approval cannot override mode-denied capabilities.
- Workspace-scoped tool requests require centralized path validation.
- Tool results can be summarized without unbounded output in session contracts.
- Tests cover deny/allow/path-validation contract behavior.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_03` and should coordinate with `T002_05`.
- Feeds patch/edit, shell, provider tool-calling, PF4J, and JBang tasks.

## Non-Goals

- Do not implement actual file edits, shell commands, network fetches, PF4J,
  JBang, LSP, or subagents.
- Do not implement approval UI or persistent approval caches.

## Open Questions

- Should permission decisions be represented as events directly in this task or
  only through event-ready metadata?

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task should use targeted OpenCode source questions for tool registration,
  MCP tool exposure, permission mediation, and workspace boundaries before
  finalizing contract names.
- Scope remains contract-first; concrete file, shell, network, PF4J, JBang, LSP,
  and subagent execution stays out of this task.

## Creation Note

Status: open.

Derived by grouping tool, permission, workspace, MVP, and risk-register tasks into
one contract-first implementation slice.
