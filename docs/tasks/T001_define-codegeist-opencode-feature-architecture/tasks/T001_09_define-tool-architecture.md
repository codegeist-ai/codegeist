# T001_09 Define Tool Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define Codegeist's tool registry, tool contracts, and tool result model.

## Scope

- Define built-in tools versus plugin-provided tools.
- Define tool request, input schema, result, failure, and audit metadata.
- Decide how Spring-managed tools and Spring AI tool support fit together.

## Deliverable

Add a tool architecture section to the parity document.

## Acceptance Criteria

- Tool execution is mediated by permissions.
- Tools are runtime services, not UI callbacks.
- PF4J plugin tools are represented without requiring implementation.

## Verification

- Check consistency with permission, plugin, and event model tasks.
