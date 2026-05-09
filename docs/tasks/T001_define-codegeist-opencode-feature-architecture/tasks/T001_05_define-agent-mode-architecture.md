# T001_05 Define Agent Mode Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Plan and Build agent modes for Codegeist.

This task defines agent modes as runtime capability profiles. It does not
implement agents, permissions, tools, or provider calls.

## Architecture Decision

Plan and Build are first-class Codegeist Runtime modes, not CLI-only flags and
not separate runtimes. Each mode selects a default capability profile, prompt
policy, allowed tool categories, and permission posture for a session or prompt
run.

Plan mode defaults to read-only analysis and planning. Build mode may request
implementation-capable tools, but risky or side-effecting actions still pass
through the central permission boundary.

## Scope

- Describe Plan mode as read-only analysis and task planning.
- Describe Build mode as implementation-capable work.
- Define which permissions and tools each mode may request.

## OpenCode Reference Behaviors

Use OpenCode as a feature reference for:

- Plan as a read-only analysis/planning agent.
- Build as the default implementation-capable agent.
- General/subagent-style delegation as a later nested runtime feature, not part
  of this task's MVP decision.
- Tool execution mediated by mode and permissions.

Do not copy OpenCode's implementation mechanics. Codegeist modes must be Java
runtime policies that can be used by CLI, future server, and future Vaadin
clients.

## Mode Definitions

| Mode | Primary purpose | Default posture | May request | Must not do by default |
| --- | --- | --- | --- | --- |
| Plan | Understand code, inspect context, propose tasks, explain architecture, draft plans | Read-only | Read workspace files, inspect docs/tasks/memory, inspect generated analysis artifacts, use non-side-effecting local reasoning, optionally ask for more context | Write files, run shell commands, call network tools, mutate sessions beyond planning notes, invoke external integrations |
| Build | Implement approved changes, update docs, run verification, prepare commits when asked | Permission-gated side effects | Everything Plan can request plus file edits, patch application, shell commands, test/build commands, local tool execution, selected network calls when explicitly allowed | Bypass permissions, write outside workspace, run destructive shell commands without explicit approval, treat plugin/JBang capabilities as trusted by default |

## Capability Categories

Use these broad capability categories until `T001_09` and `T001_10` define exact
tool and permission contracts:

| Capability | Plan default | Build default | Notes |
| --- | --- | --- | --- |
| Read project files | Allow | Allow | Must respect workspace boundaries and ignored/secret-like files. |
| Read generated analysis artifacts | Allow | Allow | Includes `docs/third-party/**`, Graphify summaries, and Repomix excerpts through dedicated workflows. |
| Modify files | Deny | Ask/allow by policy | Build still uses patch/write tools through permission checks. |
| Run shell commands | Deny by default | Ask by default | Build may run targeted verification; destructive commands remain explicit user actions. |
| Network access | Deny by default | Ask by default | Provider calls are separate from general web/fetch tools. |
| Provider/model calls | Allow through runtime policy | Allow through runtime policy | Both modes may call the selected LLM provider, subject to provider configuration. |
| Tool execution | Read-only tools only | Permission-gated tools | Exact registry belongs to `T001_09`. |
| Plugin/PF4J tools | Deny until registered and classified | Ask/allow after registration and classification | Plugin architecture belongs to `T001_15`. |
| JBang scripts | Deny by default | Ask by default after classification | JBang role belongs to `T001_16`; scripts must not bypass runtime policy. |
| Approval decisions | May request clarification | May request approval | Permission model belongs to `T001_10`. |

## Runtime Semantics

- Agent mode is part of the runtime request/session context.
- CLI commands such as `codegeist plan` and `codegeist build` select a mode but
  do not implement mode behavior themselves.
- Mode checks happen before tool execution and before permission prompts.
- A denied-by-mode capability should fail with a typed runtime event explaining
  that the current mode cannot request the capability.
- Permission approval cannot grant a capability that the current mode forbids;
  switching modes must be an explicit runtime action.
- Sessions should record the selected mode for auditability and reproducibility.
- Later nested/subagent execution must create a child run or scoped runtime
  context instead of silently inheriting unrestricted Build capabilities.

## Initial Mode Selection Rules

- Interactive shell default mode is Build only after the user explicitly chooses
  or configures it; otherwise default mode remains an MVP decision for `T001_22`.
- `codegeist plan "<prompt>"` always starts in Plan mode.
- `codegeist build "<prompt>"` always starts in Build mode.
- `codegeist run "<prompt>"` must resolve to an explicit configured or displayed
  default mode before executing.
- Server and Vaadin clients must pass mode selection into the runtime instead of
  duplicating CLI logic.

## Non-Goals

- Do not implement agent classes or prompts.
- Do not define exact tool schemas; that belongs to `T001_09`.
- Do not define full permission storage, approval caching, or audit schema; that
  belongs to `T001_10`.
- Do not implement subagents or nested tasks.
- Do not decide final MVP defaults for `codegeist run`; that belongs to
  `T001_22`.

## Deliverable

Add `## Agent Mode Architecture` to
`docs/developer/codegeist-opencode-parity.md` with:

- Plan and Build as runtime modes,
- OpenCode reference behaviors,
- mode definitions,
- capability matrix,
- runtime semantics,
- initial mode selection rules,
- explicit non-goals.

## Acceptance Criteria

- Plan mode cannot perform write, shell, or network side effects by default.
- Build mode still requires permission gates for risky operations.
- Agent modes are runtime concepts, not CLI-only flags.
- Permission approval cannot override a capability denied by the active mode.
- CLI, server, and Vaadin clients pass mode selection into the runtime instead
  of implementing mode behavior themselves.
- Nested/subagent behavior is identified as later scope, not silently included in
  MVP Plan or Build.

## Verification

- Compare with OpenCode feature notes for build and plan agents.
- Check consistency with `T001_04`, `T001_09`, and `T001_10`.

## Specification Check Result

- Already specifies Plan and Build as runtime capability profiles, not CLI flags,
  and preserves mode, permission, tool, and client boundaries.
- No further task reshaping was needed during the `/specify-task` pass.
