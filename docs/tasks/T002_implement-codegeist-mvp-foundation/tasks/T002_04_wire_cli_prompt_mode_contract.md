# T002_04 Wire CLI Prompt Mode Contract

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_04`, `T001_05`, `T001_22`, `T001_23`

## Goal

Expose the first Spring Shell command contract that translates CLI input into a
runtime prompt request with an explicit agent mode.

## Context

The architecture makes Spring Shell the first client surface. CLI commands parse
input and render output, but runtime owns sessions, modes, provider calls, tools,
permissions, workspace policy, and events.

`T002_03` now provides a documentation blueprint for runtime/session/event
contracts rather than implemented Java contracts. This task must therefore treat
that blueprint as the naming and boundary reference. If command wiring needs real
Java contracts such as `PromptRequest` or `AgentMode`, the implementation plan for
this task must introduce only the smallest concrete types required by the CLI
adapter tests.

## Concrete Solution

1. Add minimal `plan`, `build`, and possibly `run` command stubs that construct a
   runtime request contract and delegate to the runtime boundary.
2. Make `plan` select Plan mode and `build` select Build mode explicitly.
3. Keep `run` either absent or clearly mapped to a configured/default mode; do not
   hide mode selection.
4. Render only a deterministic placeholder or summary until provider/runtime
   behavior exists.
5. Add Spring Shell tests or focused command-unit tests that verify command input
   maps to the expected runtime mode and prompt text.

## Scope

- `ai.codegeist.cli`
- `ai.codegeist.runtime` boundary usage
- Spring Shell command tests where practical
- `app/codegeist/cli/Taskfile.yml` only if a new smoke command is needed

## Acceptance Criteria

- CLI command code remains an adapter over runtime contracts.
- Plan and Build modes are explicit in the command-to-runtime request mapping.
- No provider call, tool execution, permission prompt, storage, or event streaming
  is implemented.
- Tests prove CLI input maps to runtime request contracts.

## Verification

```bash
task test
git --no-pager diff --check
```

## Dependencies

- Depends on `T002_02` for package vocabulary and `T002_03` for the documented
  runtime/session/event contract blueprint.
- Must not assume `T002_03` created Java source files, packages, services, or
  tests.

## Non-Goals

- Do not implement a full-screen TUI.
- Do not implement provider calls, context loading, tools, approvals, or durable
  sessions.

## Open Questions

- Should `codegeist run` be deferred until a default mode policy is available?

## Specification Check Result

- Rechecked with the T002 parent default hints.
- The command adapter boundary is clear: CLI maps input to runtime requests and
  must not own session, mode, provider, tool, permission, or event behavior.
- `codegeist run` should stay explicit or deferred until a default mode policy is
  chosen during implementation.
- Rechecked after `T002_03` was narrowed to a documentation blueprint. This task
  remains the likely first slice that may need concrete Java request/mode types,
  but only if its implementation plan keeps them minimal and adapter-focused.

## Creation Note

Status: open.

Derived from the CLI/shell, agent mode, MVP cut, and prompt-flow architecture as
one CLI adapter implementation slice.
