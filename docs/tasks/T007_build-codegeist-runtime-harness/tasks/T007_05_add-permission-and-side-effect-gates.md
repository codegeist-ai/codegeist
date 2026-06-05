# T007_05 Add Permission And Side-Effect Gates

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add Codegeist mode, permission, approval, and workspace gates before any
side-effecting tool can run.

This child prepares the harness for patch/edit, shell, network, plugin, MCP, and
external integration tools. It should make denial and approval observable through
runtime events without requiring a full persistent permission cache in the first
slice.

## Dependencies

- Prefer starting after `T007_02` so approval and denial can be runtime events.
- Prefer starting after `T007_04` so there is a concrete tool registry and workspace
  boundary to extend.

## OpenCode Evidence To Translate

- `docs/third-party/opencode/source/packages/opencode/src/permission/index.ts` for
  permission ask/reply lifecycle and persisted approvals.
- `docs/third-party/opencode/source/packages/opencode/src/permission/evaluate.ts`
  for rule matching and default ask behavior.
- `docs/third-party/opencode/source/packages/opencode/src/agent/agent.ts` and
  `config/agent.ts` for mode/tool availability.
- `docs/third-party/opencode/source/packages/opencode/src/tool/tool.ts` and
  `tool/registry.ts` for tool execution context.
- TUI permission rendering paths from `routes/session/index.tsx` and state
  projection from `context/sync.tsx` for client-facing approval behavior.

## Scope

- Add the smallest mode policy needed for current tests, likely plan/read-only and
  build/side-effect-capable.
- Define permission request data only for real side-effecting categories that will
  be tested soon: write/patch, shell/process, network, plugin/JBang/MCP, and
  external credentials.
- Emit runtime events for approval requested, approval granted, approval denied,
  and mode-denied requests.
- Let clients collect approval decisions, but keep decision validation, scope, and
  audit/event emission in runtime/permission services.
- Ensure approval cannot override a mode-denied capability. Switching mode must be a
  separate explicit action.
- Keep workspace validation after approval and before side effects.

## Acceptance Criteria

- A focused test proves plan mode denies a side-effecting tool before user approval
  is requested.
- A focused test proves build mode can request approval for a side-effecting tool
  and then continue or deny based on the decision.
- Permission decisions produce typed events suitable for CLI/TUI rendering.
- Tool implementations cannot classify their own trust level or bypass permission
  policy.
- No persistent approval cache is required unless the focused test introduces one.
- Architecture docs are updated with the implemented permission/mode behavior.

## Non-Goals

- Do not implement long-lived wildcard approval persistence until a storage task
  needs it.
- Do not implement server-side auth, roles, users, or remote approval workflows.
- Do not implement shell or patch tools in this child unless a tiny fake/test tool
  is needed to prove policy.
- Do not let approval override workspace rejection.
- Do not add security sandboxing claims beyond explicit mode/permission/workspace
  checks.

## Suggested Tests

- Mode-denied request produces a denial result and event without asking approval.
- Approval-required request emits an approval event with tool id, capability, target
  summary, and scope.
- Denied approval produces a user-visible failure and audit-style event.
- Approved request still passes workspace validation before execution.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<permission-test-selector>
task test
```
