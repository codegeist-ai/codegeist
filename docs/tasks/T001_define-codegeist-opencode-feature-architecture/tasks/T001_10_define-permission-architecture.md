# T001_10 Define Permission Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Codegeist's permission model for risky and side-effecting actions.

This task specifies permission policy and approval concepts only. It does not
implement prompts, UI approvals, storage, shell execution, file writes, network
access, or plugin trust.

## Scope

- Define permission categories for read, write, shell, network, plugin, and
  external integrations.
- Define approval request and approval result concepts.
- Define how agent mode affects available permissions.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode permission mediation? | A central permission policy service between tool requests and side effects. |
| Can CLI or Vaadin approve actions directly? | They can collect user decisions, but runtime/permission services own decision scope, cache, and audit events. |
| Can approval override Plan mode restrictions? | No. Mode-denied capabilities cannot be granted by approval; mode switch must be explicit. |
| What is MVP? | Approval requests for write, patch, shell, network, plugin/JBang, and external integration actions. |
| What is later? | Persistent approval cache, revocation, per-session policies, role-based server auth integration, and remote approval workflows. |

## Permission Categories

- Read project files inside workspace.
- Read generated analysis artifacts.
- Write or patch files.
- Execute shell/process commands.
- Access network/web/fetch tools.
- Invoke plugin/PF4J-provided tools.
- Invoke JBang scripts.
- Access external integrations or credentials.

## Boundary Rules

- Permission checks happen after mode checks and before tool side effects.
- Permission service records approval request and decision facts; event model
  publishes user-visible and audit-relevant events.
- Tool implementations cannot decide their own trust level.
- Workspace service still validates paths after approval.
- Server/Vaadin/CLI are approval presentation surfaces, not permission policy
  owners.

## Implementation-Readiness Questions

- Can the runtime ask for approval without knowing which client will render it?
- Can each approval be scoped by tool, capability, path/command/network target,
  session, turn, and expiry?
- Can audit logs later reconstruct who/what approved a side effect?
- Can denied permissions return typed events and session parts?
- Can plugin/JBang tools start untrusted until classified?

## Non-Goals

- Do not implement approval UI, caches, storage, auth, or revocation.
- Do not decide final default allow/deny policy for every tool.
- Do not implement security sandboxing.
- Do not conflate server authentication with tool permission policy.

## Deliverable

Add a permission architecture section to the parity document with categories,
approval request/decision concepts, mode interaction, audit events, and client
boundaries.

## Acceptance Criteria

- Permission checks sit between tool requests and side effects.
- Plan mode defaults to read-only behavior.
- Approval state can be represented in events and audit logs.
- Permission approval cannot override mode-denied capabilities.
- Clients collect approvals but do not own policy.

## Verification

- Check consistency with tool, shell, patch, and web/fetch concepts.

## Verification Result

- Specified permission categories, mode interaction, approval boundaries, and
  implementation-readiness questions.
