# T001_02 Map OpenCode Concepts To Java Stack

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Translate OpenCode concepts into Codegeist concepts using the selected
Java-first technology stack from `T001_01`.

This task creates the shared vocabulary for the remaining architecture tasks. It
does not define detailed module boundaries, Java APIs, or implementation steps.

## Architecture Rule

OpenCode is a feature reference, not a runtime blueprint. Codegeist mappings
must use the Java-first baseline:

- Java `25`, while compatible.
- Maven.
- Spring Boot `3.5.x`.
- Spring Shell.
- Spring AI `1.1.x`.
- GraalVM.
- Vaadin.
- JBang as lightweight user extension runtime.
- PF4J as packaged plugin framework.

Every mapping should preserve these boundaries:

- UI surfaces are clients, not runtime owners.
- Spring AI is the AI integration layer, not the whole agent runtime.
- Extensions must not bypass tool registry, permissions, workspace boundaries,
  or audit events.
- Bun, TypeScript, Hono, Effect, Solid, and Electron are OpenCode implementation
  details unless explicitly re-evaluated later.

## Scope

- Map CLI, TUI, agents, sessions, events, providers, tools, permissions,
  plugins, server, web, storage, and packaging.
- Map commands, skills, user scripts, repository context, SDK/OpenAPI,
  authentication, and development/runtime command equivalence.
- Avoid copying OpenCode's Bun/TypeScript architecture shape.
- Identify unclear mappings that need later validation.

## Out Of Scope

- Changing `app/codegeist/pom.xml`.
- Adding dependencies.
- Implementing CLI, TUI, provider, tool, plugin, server, Vaadin, or JBang
  behavior.
- Finalizing physical Maven modules.
- Defining Java classes, APIs, schemas, or package names.
- Making the final MVP cut; this belongs to `T001_22`.

## Deliverable

Add or refine a concept mapping table in
`docs/developer/codegeist-opencode-parity.md`.

The mapping table must use these columns:

- OpenCode concept
- OpenCode evidence
- Codegeist concept
- Primary owner
- Extension path
- MVP relevance
- Follow-up task
- Open questions

`MVP relevance` should use only broad labels in this task:

- `MVP`
- `Later`
- `Out/Unknown`

The final MVP decision remains in `T001_22`.

The table must cover at least these OpenCode concept groups:

- CLI
- TUI
- Plan agent
- Build agent
- Sessions
- Message parts
- Events
- Provider/model configuration
- Tool execution
- File tools
- Shell tools
- Patch/edit tools
- Web/fetch tools
- LSP/code intelligence
- Subagents/tasks
- Plugins
- Commands
- Skills
- User scripts
- Permissions
- Repository context
- Headless server
- Web UI
- Desktop app
- SDK/OpenAPI
- Storage
- Authentication
- Packaging
- Development/runtime command equivalence

## Acceptance Criteria

- Every mapped OpenCode concept has a Codegeist target concept.
- Every mapped concept names a primary Java/Spring technology or component
  owner.
- Every mapped concept has an extension path: built-in, PF4J, JBang, later, or
  none.
- Every mapped concept has a broad MVP relevance label.
- Every mapped concept points to a follow-up architecture task.
- Open questions are listed for uncertain mappings.
- The mapping uses Spring Boot `3.5.x` and Spring AI `1.1.x` from `T001_01`.
- JBang is represented as a repository-local user extension runtime.
- No OpenCode implementation technology is adopted as a Codegeist runtime
  requirement without an explicit later decision.

## Verification

- Review against `docs/third-party/opencode/features/README.md` and
  `docs/third-party/opencode/ANALYSIS_REPORT.md`.
- Review against `docs/third-party/opencode/user/README.md` for user-facing
  surfaces.
- Confirm `T001_02` does not change runtime files or build configuration.

## Specification Check Result

- Already defines the OpenCode-to-Java vocabulary and requires each concept to
  map to Codegeist ownership, extension path, MVP relevance, follow-up task, and
  open questions.
- No further task reshaping was needed during the `/specify-task` pass.

## Solution Note

Status: completed.

The solution pass used the narrow documentation-first path because the central
parity document already contained the required OpenCode-to-Java mapping table.
The implemented change tightened that table against this task's contract by
making the allowed `Extension path` labels explicit and normalizing mapped rows
to the broad labels `built-in`, `PF4J`, `JBang`, `later`, and `none`.

No user decision is pending. This task intentionally keeps MVP labels broad and
leaves the final MVP cut to `T001_22`; uncertain mappings remain captured as
open questions in the table.

Verification passed with `git --no-pager diff --check`. A final diff review
confirmed that only documentation files changed and no runtime or build
configuration was touched.
