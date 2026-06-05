# T007_07 Add Patch Edit And Shell Tools

Parent: `T007_build-codegeist-runtime-harness`

Status: open

## Goal

Add the first side-effecting Codegeist tools for controlled patch/edit and shell
execution.

This child should land only after the runtime, tool registry, permission gates,
workspace validation, and Spring AI tool-calling boundary are stable enough to
prevent side effects from bypassing policy.

## Dependencies

- Requires `T007_04` tool registry and workspace-read boundary.
- Requires `T007_05` permission and side-effect gates.
- Prefer starting after `T007_06` if model-driven tool calls should be covered in
  the same behavior; otherwise start with CLI/runtime-internal tool invocation.

## OpenCode Evidence To Translate

- OpenCode patch/edit and shell tool files should be inspected before this task is
  implemented. Start from `docs/third-party/opencode/source/packages/opencode/src/tool/`
  and use `/ask-project opencode` for exact source-path citations.
- Use `docs/third-party/opencode/source/packages/opencode/src/permission/index.ts`
  and `permission/evaluate.ts` for approval behavior.
- Use `docs/third-party/opencode/source/packages/opencode/src/cli/cmd/tui/routes/session/index.tsx`
  for how side-effecting tool calls are rendered to users.

## Spring AI Agent Utils Evidence To Consider

- `docs/third-party/spring-ai-agent-utils/source/spring-ai-agent-utils/src/main/java/org/springaicommunity/agent/tools/ShellTools.java`
  and `AgentEnvironment.java` are concept references for command execution,
  environment capture, and output handling.
- `ShellTools` should not be used directly until Codegeist owns command
  classification, cwd validation, timeout, output bounds, and permission policy.
- `FileSystemTools.write` or edit behavior should not be exposed directly; Codegeist
  patch/edit should produce reviewable proposals or controlled mutations based on
  task requirements.

## Scope

- Implement either patch/edit or shell first if doing both would make the task too
  large. Split this child further if needed.
- For patch/edit, validate workspace path, produce reviewable diff or patch output,
  and apply only after approval when the active mode permits writes.
- For shell, validate cwd under workspace, classify command capability, enforce
  timeout and output bounds, and require approval for side effects.
- Emit runtime events for request, approval, execution start, output summary,
  success, failure, and denied execution.
- Keep actual command output bounded and store large output through an output
  reference only when a storage/output-ref task provides that mechanism.

## Acceptance Criteria

- A focused test proves patch/edit or shell is denied in plan mode.
- A focused test proves approval is required before write or shell side effects.
- Workspace validation prevents outside-root file mutation or cwd escape.
- Tool output is bounded and represented as Codegeist tool results/events.
- Existing read-only tools and commands remain unaffected.
- Architecture docs describe the implemented side-effecting tool behavior.

## Non-Goals

- Do not implement arbitrary unrestricted shell execution.
- Do not implement broad destructive-command detection beyond current tested policy.
- Do not implement network, MCP, plugin, JBang, PF4J, LSP, or subagent tools here.
- Do not implement a full patch review UI unless the terminal/TUI event model
  already supports it.
- Do not claim sandboxing beyond the explicit tested checks.

## Suggested Tests And Verification

- Temporary workspace fixtures for patch/edit.
- Shell tests should avoid relying on external network, provider credentials, or
  platform-specific commands unless the task explicitly owns platform behavior.
- Prefer simple cross-platform commands or Java-level fakes for policy tests.

Candidate commands from `app/codegeist/cli`:

```bash
task test TEST=<patch-or-shell-test-selector>
task test
```

Run smoke checks only when command runtime or packaged behavior changes:

```bash
task native-smoke
```
