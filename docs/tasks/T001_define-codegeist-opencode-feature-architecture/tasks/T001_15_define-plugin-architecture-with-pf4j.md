# T001_15 Define Plugin Architecture With PF4J

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how PF4J should provide Codegeist extension points.

## Scope

- Define plugin extension points for tools, commands, skills, hooks, and
  integrations.
- Define plugin lifecycle expectations.
- Identify PF4J and GraalVM compatibility risks.

## Deliverable

Add a PF4J plugin architecture section to the parity document.

## Acceptance Criteria

- PF4J is the plugin boundary, not a replacement for core runtime services.
- Built-in functionality can exist without PF4J plugins.
- Native-image limitations are documented as a risk or constraint.

## Verification

- Check consistency with tool, command, and GraalVM tasks.
