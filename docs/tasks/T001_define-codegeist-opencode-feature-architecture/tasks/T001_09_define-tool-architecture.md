# T001_09 Define Tool Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Codegeist's tool registry, tool contracts, and tool result model.

This task specifies the tool architecture only. It does not implement tools,
Spring AI bindings, PF4J extensions, JBang execution, permissions, or workspace
side effects.

## Scope

- Define built-in tools versus plugin-provided tools.
- Define tool request, input schema, result, failure, and audit metadata.
- Decide how Spring-managed tools and Spring AI tool support fit together.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode tools? | A runtime-owned `ToolRegistry` of typed tool descriptors and executors. |
| Does Spring AI own tool execution? | No. Spring AI may expose model tool-call integration, but Codegeist owns classification, permission, workspace checks, events, and execution policy. |
| How do built-in, PF4J, and JBang tools differ? | Built-ins are Spring services, PF4J contributes packaged extensions, and JBang contributes lightweight user scripts through runtime mediation. |
| What is MVP? | Read-only context/file inspection plus patch/write/shell tool contracts as permission-gated architecture concepts. |
| What is later? | Plugin tools, JBang tools, LSP/code-intelligence tools, network fetch, subagent/task tools, and rich marketplace-style extension metadata. |

## Architecture Decisions To Capture

- Tool execution flows through Runtime -> Tool Registry -> Permission ->
  Workspace/Executor -> Events -> Session result parts.
- Tools are runtime services, not CLI callbacks or Vaadin actions.
- Tool descriptors must expose capability category, mode compatibility,
  permission requirements, input summary/redaction policy, and result type.
- Tool results must be representable as session parts and runtime events without
  embedding unbounded output directly in the session aggregate.
- Spring AI tool callbacks must be adapters around Codegeist tool descriptors, not
  the primary source of policy.

## Boundary Rules

- Tool registry owns registration and lookup; permission owns approval policy.
- Workspace owns path validation before file side effects.
- Shell execution, patch/edit, and network access are specialized tools covered
  by later child tasks.
- PF4J and JBang may contribute tools only through runtime-owned registration.
- Tools must not emit user-visible events directly; runtime translates tool
  lifecycle into event model events.

## Implementation-Readiness Questions

- Can a Java developer describe a tool without importing CLI, Vaadin, HTTP, or
  provider SDK types?
- Can the runtime classify a tool as read-only, write, shell, network, plugin, or
  extension-provided before execution?
- Can a tool request be denied by mode before asking for permission?
- Can a tool result produce a small summary plus artifact references?
- Can Spring AI tool-calling be wired later without bypassing Codegeist policy?

## Non-Goals

- Do not implement concrete tools.
- Do not decide exact JSON schemas or Java APIs for every tool.
- Do not implement permission storage, shell execution, patch application, PF4J,
  JBang, LSP, network fetch, or subagents.
- Do not make provider/model support the only source of tool availability.

## Deliverable

Add a tool architecture section to the parity document with tool registry,
descriptor, request/result, permission, event, Spring AI, PF4J, and JBang
boundaries.

## Acceptance Criteria

- Tool execution is mediated by permissions.
- Tools are runtime services, not UI callbacks.
- PF4J plugin tools are represented without requiring implementation.
- Spring AI tool integration cannot bypass Codegeist tool policy.
- Tool results can feed the session and event models.

## Verification

- Check consistency with permission, plugin, and event model tasks.

## Verification Result

- Specified the Codegeist tool architecture questions and boundaries for later
  Java implementation.

## Solution Note

Status: completed.

The solution pass added `## Tool Architecture` to
`docs/developer/codegeist-opencode-parity.md`. The section defines the
runtime-owned tool registry, descriptors, request/result model, Spring AI adapter
boundary, built-in/PF4J/JBang tool sources, initial tool categories, and
documentation-only non-goals.

No user decision is pending. Spring AI tool callbacks are explicitly adapters
around Codegeist tool descriptors and cannot bypass mode, permission, workspace,
event, or session policies.

Verification passed with `git --no-pager diff --check`. A final review confirmed
tool results can feed the session and event models without implementing concrete
tools.
