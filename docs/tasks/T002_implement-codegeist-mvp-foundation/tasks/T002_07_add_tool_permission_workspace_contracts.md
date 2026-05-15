# T002_07 Describe Tool Permission Workspace Contracts

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_09`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

## Goal

Describe the first tool descriptor, tool request/result, permission request, and
workspace validation contracts before concrete side-effecting tools exist, without
adding Java source yet.

## Context

The architecture requires tools to flow through runtime-owned descriptors,
mode checks, permission policy, workspace validation, events, and session result
parts. Spring AI tool callbacks, PF4J plugins, and JBang scripts must adapt to
these contracts instead of bypassing them.

## Concrete Solution

1. Create or update `docs/developer/tool-permission-workspace-contracts.md` as
   the future tool, permission, and workspace contract blueprint.
2. Define future `ToolDescriptor`, `ToolRequest`, `ToolResult`, `ToolFailure`,
   capability category, and result summary shapes.
3. Define future `PermissionRequest`, `PermissionDecision`, decision scope, and
   audit metadata shapes.
4. Describe mode compatibility, permission need, and workspace validation flow
   without executing concrete file, shell, or network actions.
5. Document future tests for mode denial, permission-required decisions,
   read-only descriptors, and workspace path validation calls.
6. Include OpenCode source evidence, future file maps, diagrams, and illustrative
   Java snippets in markdown only.

## Scope

- `docs/developer/tool-permission-workspace-contracts.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Tool descriptors, permission decisions, workspace path validation, and event
  metadata are specified as future contracts before execution.
- Permission approval is specified as unable to override mode-denied capabilities.
- Workspace-scoped tool requests are specified to require centralized path
  validation.
- Tool results are specified to summarize output without unbounded session data.
- Future deny/allow/path-validation tests are described, but no Java source or
  tests are created by this task.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_03` and should coordinate with `T002_05`.
- Feeds patch/edit, shell, provider tool-calling, PF4J, and JBang tasks.

## Non-Goals

- Do not create Java source files, empty package directories, or contract tests.
- Do not implement actual file edits, shell commands, network fetches, PF4J,
  JBang, LSP, or subagents.
- Do not implement approval UI or persistent approval caches.

## Open Questions

- Should permission decisions be represented as events directly in this task or
   only through event-ready metadata?

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later implementation task instead of creating `ai.codegeist.tool`,
  `ai.codegeist.permission`, or `ai.codegeist.workspace` source packages now.

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
one contract-first documentation/specification slice.
