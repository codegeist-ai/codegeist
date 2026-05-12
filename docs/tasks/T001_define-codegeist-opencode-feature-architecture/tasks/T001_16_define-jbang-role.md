# T001_16 Define JBang Role

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define the limited role of JBang in the Codegeist architecture.

This task specifies where repository-local Java scripts fit. It does not
implement a script runner, remote script trust policy, command API, or tool
integration.

## Scope

- Decide where lightweight Java scripts are useful.
- Separate JBang automation from long-running runtime state.
- Identify possible developer workflows or migration helpers.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is JBang's Codegeist-specific role? | Lightweight Java scripting for repo-local helper commands, migration aids, or simple user extensions. |
| Is JBang a plugin system? | No. PF4J owns packaged plugins; JBang is for lightweight scripts mediated by the runtime. |
| Can JBang scripts bypass permissions? | No. Script execution is a classified tool/extension action subject to mode, permission, workspace, and audit policy. |
| What is MVP? | Define the role and constraints only; no script execution required. |
| What is later? | Script metadata, allowed locations, dependency trust, caching, remote script policy, and native-image behavior. |

## Boundary Rules

- JBang scripts must not own sessions, provider calls, permissions, storage, or
  long-running runtime state.
- Runtime mediates any script invoked as a command or tool.
- Script working directories and file access pass through workspace policy.
- Script dependencies and remote loading are untrusted until a policy exists.
- PF4J remains the boundary for packaged, versioned extension points.

## Implementation-Readiness Questions

- Where should repo-local scripts live?
- What metadata is required before a script can be exposed as a command or tool?
- How are arguments, output, exit status, timeout, and errors represented?
- How does JBang behave in JVM mode versus native-image distributions?
- Which simple workflows are worth keeping as scripts instead of Spring services?

## Non-Goals

- Do not implement script discovery or execution.
- Do not allow remote scripts by default.
- Do not use JBang for core runtime services.
- Do not replace PF4J with ad hoc scripts.

## Deliverable

Add a JBang role section to the parity document with use cases, constraints,
runtime mediation, PF4J boundaries, security questions, and native-image risks.

## Acceptance Criteria

- JBang is not used as the core runtime mechanism.
- JBang use cases are specific and optional.
- Boundaries with PF4J commands and Spring services are clear.
- JBang execution cannot bypass mode, permission, workspace, or audit policy.
- Remote script and dependency trust are listed as open questions.

## Verification

- Review whether each proposed JBang use case could be a plain Java script.

## Verification Result

- Specified JBang as a lightweight, mediated, optional scripting extension path.

## Solution Note

Status: completed.

The solution pass added `## JBang Role` to
`docs/developer/codegeist-opencode-parity.md`. The section defines JBang as an
optional lightweight Java scripting path, identifies fitting and non-fitting use
cases, preserves PF4J as the packaged plugin boundary, and lists trust,
dependency, metadata, caching, and native-image questions.

No user decision is pending. JBang does not own runtime state, sessions,
provider calls, permissions, storage, or workspace policy, and script execution
cannot bypass runtime mediation.

Verification passed with `git --no-pager diff --check`. A final review confirmed
the task remains role definition only and does not implement script discovery or
execution.
