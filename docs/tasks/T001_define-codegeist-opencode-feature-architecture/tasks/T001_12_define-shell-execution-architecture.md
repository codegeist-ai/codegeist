# T001_12 Define Shell Execution Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist should model shell command execution safely.

This task specifies shell execution as a high-risk tool contract only. It does
not implement a process runner, terminal UI, PTY, JBang execution, or command
approval storage.

## Scope

- Define command request, working directory, environment, timeout, output, exit
  code, and failure concepts.
- Define approval requirements for shell execution.
- Define audit and display requirements for command results.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode shell tools? | A controlled process-execution tool mediated by runtime, permission, workspace, and event services. |
| Is shell execution allowed in Plan mode? | Deny by default; Plan may inspect commands conceptually but not run them. |
| What is MVP? | Targeted verification/build/test commands with explicit approval and bounded output. |
| What is later? | PTY/live terminal integration, richer cancellation, remote execution, JBang-specific execution, and command allowlists. |

## Boundary Rules

- Shell execution is a tool request, not a CLI implementation detail.
- Runtime requests permission before starting a process.
- Workspace validates working directory before execution.
- Environment variables must be controlled and redacted in events/logs.
- Output is streamed as events and summarized as session/tool result parts.
- Destructive commands require explicit user intent and must not be inferred from
  generic approval.

## Implementation-Readiness Questions

- Can a command request describe argv/snippet, cwd, env policy, timeout, stdin,
  and output limits without invoking a shell yet?
- Can the permission prompt show enough command context for informed approval?
- Can timeout, cancellation, non-zero exit, and output truncation be represented
  as typed results?
- Can verification commands be distinguished from destructive commands?
- Can command output avoid leaking secrets into durable storage?

## Non-Goals

- Do not implement process execution, PTY, streaming transport, or terminal UI.
- Do not define a full command sandbox.
- Do not allow shell execution to bypass tool/permission/workspace policy.

## Deliverable

Add a shell execution architecture section to the parity document with command
request fields, permission requirements, workspace/env policy, events, output,
timeouts, cancellation, and audit boundaries.

## Acceptance Criteria

- Shell execution is a high-risk tool call.
- Commands require explicit permission before execution.
- Output and exit code can be represented as events.
- Plan mode shell execution is denied by default.
- Destructive commands are called out as requiring explicit user intent.

## Verification

- Check consistency with the permission and event model tasks.

## Verification Result

- Specified shell execution as a controlled high-risk tool with approval,
  workspace, event, and audit boundaries.

## Solution Note

Status: completed.

The solution pass added `## Shell Execution Architecture` to
`docs/developer/codegeist-opencode-parity.md`. The section defines shell
execution as a high-risk tool call, command request fields, permission and
workspace requirements, environment/output redaction, timeouts, cancellation,
typed results, and destructive-command constraints.

No user decision is pending. Plan mode denies shell execution by default, while
Build-mode shell execution remains explicit-approval work with bounded output and
runtime events.

Verification passed with `git --no-pager diff --check`. A final review confirmed
the section is consistent with permission and event models and does not implement
a process runner.
