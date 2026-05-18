# T001_15 Define Plugin Architecture With PF4J

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how PF4J should provide Codegeist extension points.

This task specifies packaged plugin boundaries only. It does not implement PF4J,
load plugins, define a plugin API, or make native-image compatibility guarantees.

## Scope

- Define plugin extension points for tools, commands, skills, hooks, and
  integrations.
- Define plugin lifecycle expectations.
- Identify PF4J and GraalVM compatibility risks.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode plugins? | PF4J-managed packaged extensions that contribute to runtime-owned extension points. |
| Does PF4J replace core services? | No. Runtime, tools, permissions, workspace, events, and storage remain core services. |
| What is MVP? | Architecture contract only; built-in functionality must work without plugins. |
| What is later? | Tool/command/skill/hook/integration extension APIs, plugin metadata, isolation, versioning, trust policy, and marketplace-like distribution. |

## Boundary Rules

- Plugins register contributions; runtime decides if and when they are used.
- Plugin tools flow through the same tool, permission, workspace, and event
  policies as built-in tools.
- Plugin commands are adapters over runtime APIs, not independent runtimes.
- Plugin class loading and lifecycle are PF4J concerns, but trust and capability
  classification are Codegeist concerns.
- Native-image support for plugin loading is a risk until proven.

## Implementation-Readiness Questions

- Which extension points need stable interfaces before PF4J is useful?
- Can plugin-provided tools be disabled or denied by policy?
- Can plugin metadata describe capabilities, required permissions, and version
  compatibility?
- Can Codegeist run without plugin loading in native images?
- Can user-visible plugin errors be represented as runtime events?

## Non-Goals

- Do not implement PF4J or plugin APIs.
- Do not decide plugin packaging/distribution.
- Do not allow plugins to bypass core runtime services.
- Do not assume native-image plugin loading works.

## Deliverable

Add a PF4J plugin architecture section to the parity document with extension
points, lifecycle, trust/capability classification, runtime mediation, and
GraalVM constraints.

## Acceptance Criteria

- PF4J is the plugin boundary, not a replacement for core runtime services.
- Built-in functionality can exist without PF4J plugins.
- Native-image limitations are documented as a risk or constraint.
- Plugin tools and commands pass through runtime-owned policies.
- Plugin trust/capability metadata is identified as a future need.

## Verification

- Check consistency with tool, command, and GraalVM tasks.

## Verification Result

- Specified PF4J as packaged extension mediation, not a replacement for runtime
  services.

## Solution Note

Status: completed.

The solution pass added `## Plugin Architecture With PF4J` to
`docs/developer/specification/codegeist-opencode-parity.md`. The section defines PF4J as the
packaged plugin boundary, extension points, runtime mediation, plugin metadata,
trust/capability classification, lifecycle posture, and GraalVM/native-image
risk.

No user decision is pending. Built-in functionality remains independent of PF4J,
and plugin tools/commands must pass through runtime-owned tool, permission,
workspace, event, and storage policies.

Verification passed with `git --no-pager diff --check`. A final review confirmed
PF4J is not assumed native-image compatible and no plugin APIs were implemented.
