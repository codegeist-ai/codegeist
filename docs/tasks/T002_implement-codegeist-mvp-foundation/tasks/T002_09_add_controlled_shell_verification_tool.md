# T002_09 Describe Controlled Shell Verification Tool

Parent: `T002_implement-codegeist-mvp-foundation`

Sources: `T001_12`, `T001_10`, `T001_11`, `T001_22`, `T001_24`

status: finalized

## Goal

Describe the first controlled shell verification tool contract for approved,
bounded, non-destructive commands without adding a process executor yet.

## Context

Shell execution is a high-risk tool. The MVP needs verification/build/test command
support, but Plan mode denies shell execution and destructive commands require
explicit user intent.

## Concrete Solution

1. Create or update `docs/developer/specification/shell-verification-contracts.md` as the
   future controlled shell verification blueprint.
2. Define future shell command request/result contracts with argv or snippet, cwd,
   env policy, timeout, stdin posture, output limit, exit code, and failure
   reason.
3. Describe the required Build mode, permission approval, workspace-validated cwd,
   and bounded output before execution.
4. Document future tests for Plan-mode denial, approval-required behavior,
   timeout/failure result shape, output truncation metadata, and cwd validation.
5. Include OpenCode source evidence, future file maps, diagrams, and illustrative
   Java snippets in markdown only.

## Scope

- `docs/developer/specification/shell-verification-contracts.md`
- `docs/developer/README.md` if a new developer document is added
- `docs/developer/architecture/architecture.md` only to keep current-state notes accurate
- this task file

## Acceptance Criteria

- Shell execution is specified as a permission-gated tool request.
- Plan mode shell execution is specified as denied by default.
- Command results include exit code, summary, output reference/truncation, and
  typed failure information in the blueprint.
- Destructive commands are not inferred as safe from generic approval.
- Future non-destructive approved and denied tests are described, but no Java
  source, tests, process runner, or executor port is created by this task.

## Verification

```bash
git --no-pager diff --check
```

`task test` is not required unless Java source or build files change. This task is
a documentation and diagram slice.

## Dependencies

- Depends on `T002_07`.

## Non-Goals

- Do not create Java source files, empty package directories, shell tests, process
  executor ports, or local process runners.
- Do not implement PTY, live terminal UI, remote execution, JBang execution,
  sandboxing, or broad command allowlists.

## Open Questions

- None for this documentation pass. The first implementation should start with
  request, policy, workspace-cwd, environment, result, and fake-executor contracts;
  a real process runner remains a later implementation task.

## Specification Decision

- This task is documentation-only by user decision. It should leave a precise
  handoff for a later shell-tool implementation task instead of creating shell
  execution source packages now.

## Specification Check Result

- Rechecked with the T002 parent default hints and the OpenCode source-solving
  hint.
- The task remains narrowly scoped to bounded verification commands and should not
  implement PTY, remote execution, JBang execution, or broad sandboxing.
- Source research should focus on OpenCode shell/tool permission flow and output
  handling before choosing the first executor shape.

## Dependency Impact Notes

- Finalized `T002_07_add_tool_permission_workspace_contracts.md` defines the
  generic tool request, permission decision, workspace target validation, bounded
  result, output-reference, and event/session projection boundaries. This task
  should specialize those boundaries for controlled shell verification instead of
  redefining generic tool policy.
- Shell execution remains denied in Plan mode and approval-gated in Build mode.
  Permission approval must not override destructive-command posture, workspace cwd
  denial, secret-like target denial, or descriptor capability limits.
- Shell output should use bounded summaries and output references from the
  `T002_07` blueprint; session parts and events must not retain unbounded stdout,
  stderr, environment data, or command payloads.
- Finalized `T002_08_add_patch_edit_proposal_flow.md` reinforces the same
  review-before-side-effect pattern for workspace mutation. This task should keep
  shell execution separate from patch/edit apply behavior, while mirroring the
  bounded result, typed failure, permission, workspace, and event/session summary
  posture where it applies to command execution.

## Phase Status

- Phase: `/specify-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_09_add_controlled_shell_verification_tool.md`.
- Context or instructions considered: user ran `/work-task` by exact task path
  with no additional narrowing instructions.
- Upstream phase dependency: none; `/specify-task` is the entry phase and may be
  repeated when shell, permission, workspace-cwd, destructive-command, or bounded
  output boundaries need to be refreshed.
- Parent considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Dependencies and adjacent tasks considered: finalized dependency
  `T002_07_add_tool_permission_workspace_contracts.md`, finalized adjacent
  `T002_08_add_patch_edit_proposal_flow.md`, and downstream open `T002_10` and
  `T002_12` for storage and extension/client readiness impact.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Documentation considered: `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/tool-permission-workspace-contracts.md`,
  `docs/developer/specification/patch-edit-proposal-contracts.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- Discovered hints considered: the T002 parent `Default Solve Hints` point this
  task toward OpenCode source evidence for shell execution, permission mediation,
  workspace/external-directory checks, output truncation, and session/event
  projection while preserving Codegeist's Java-first runtime-owned boundaries.
- Result: specified. The task remains a documentation-only controlled shell
  verification blueprint. It should define the future shell request/result contract
  for bounded, approved, non-destructive verification commands without creating Java
  source, tests, package directories, process execution, PTY support, terminal UI,
  remote execution, JBang execution, sandboxing, broad command allowlists,
  Graphify, Repomix, or runtime behavior.
- Open decisions or blockers: none for specification. The real process runner is
  deferred; the first future implementation should start with contracts and fake
  executor tests.
- Next recommended phase: run `/plan-task T002_09` as a documentation-only
  architecture plan for `docs/developer/specification/shell-verification-contracts.md`.

## Architecture Plan

This planning pass keeps `T002_09` as one documentation-only architecture task. No
child task is needed because the source task already targets one narrow controlled
shell verification blueprint, and no matching developer document existed yet.

### Selected Option

Create `docs/developer/specification/shell-verification-contracts.md` as the concrete controlled
shell verification contract blueprint, then cross-link it from developer
documentation and current-state architecture notes.

This option follows the active T002 posture: design the complete architecture
handoff first, then derive Java implementation tasks later only when explicitly
requested.

### Concrete Design Direction

Deepen the controlled shell architecture around eight layers:

1. Shell tool descriptor and `SHELL_PROCESS` capability posture.
2. Shell command request shape with `argv` or explicitly marked shell snippet.
3. Plan-mode denial and Build-mode approval for bounded verification commands.
4. Destructive-command posture that generic approval cannot override.
5. Workspace-cwd and referenced-path validation through the existing workspace
   policy boundary.
6. Environment and stdin policy with redaction and non-interactive defaults.
7. Timeout, cancellation, exit-code, non-zero, and typed failure result shapes.
8. Bounded stdout/stderr summaries, output references, events, session parts, and
   future test handoff.

Shell verification should be documented as a specialized `T002_07` tool capability.
It must stay separate from `T002_08` patch/edit apply behavior, while sharing the
same permission, workspace, bounded result, typed failure, and event/session
summary posture where it applies to process execution.

### Planned Documentation Files

- `docs/developer/specification/shell-verification-contracts.md`
- `docs/developer/README.md`
- `docs/developer/architecture/architecture.md` only for current-state cross-references and
  explicit not-implemented notes
- this task file
- directly affected downstream task files if finalization finds durable impact

No Java source, tests, fixtures, Maven files, build files, package directories,
process executor, PTY support, terminal UI, remote execution, JBang execution,
sandboxing, broad allowlists, Graphify, Repomix, or runtime behavior are planned
for this task.

### Planned Blueprint Content

1. State the purpose and non-implementation boundary of the shell verification
   contract blueprint.
2. Summarize OpenCode source evidence for shell command parsing, external-directory
   checks, shell permission prompts, cwd/env/stdin posture, timeout handling,
   output truncation, and session/event behavior.
3. Reuse `T002_07` for generic tool, permission, workspace, bounded-result, and
   event/session boundaries instead of redefining tool policy.
4. Reuse `T002_08` only as a review-before-side-effect comparison; do not merge
   shell execution with patch/edit apply.
5. Define future Codegeist contract names for `ShellVerificationRequest`,
   `ShellCommandShape`, `ShellCommandPurpose`, `DestructivePosture`,
   `ShellEnvPolicy`, `StdinPolicy`, `ShellVerificationResult`, `ShellFailure`, and
   `ShellExecutor`.
6. Define gate order: descriptor, mode, safety/destructive classification,
   workspace-cwd validation, permission, environment resolution, executor handoff,
   bounded result, event emission, and session projection.
7. Define Plan-mode denial and Build-mode approval-required behavior.
8. Define result and failure shapes for exit code, timeout, cancellation,
   non-zero exit, output overflow, invalid cwd, permission denial, workspace denial,
   and executor unavailability.
9. Include Mermaid diagrams for request contracts, gate sequence, and result/failure
   relationships.
10. Include illustrative Java snippets inside markdown only, clearly labeled as
    non-implemented examples.
11. Add future file map and future test handoff notes.

### Acceptance Criteria For Solve

- `docs/developer/specification/shell-verification-contracts.md` exists and describes shell
  execution as a permission-gated tool request.
- Plan mode shell execution is specified as denied by default.
- Command results include exit code, summary, output reference/truncation, and
  typed failure information.
- Destructive commands are not inferred as safe from generic approval.
- Future approved and denied tests are described, but no Java source, tests,
  process runner, package directories, or executor port is created.
- `docs/developer/README.md`, `docs/developer/architecture/architecture.md`, this task file, and
  `docs/memory-bank/chat.md` remain consistent with the new blueprint.

### Verification Plan

```bash
git --no-pager diff --check
```

`task test` is not required because the planned solve changes documentation only
and must not touch Java source, tests, build files, or runtime behavior.

### Open Questions

None for this documentation plan. Real process execution, PTY behavior, shell
sandboxing, command allowlists, and the exact Java process API remain deferred
until a later implementation task.

## Plan Workflow Handoff

- Phase: `/plan-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_09_add_controlled_shell_verification_tool.md`.
- Source task resolved: exact path
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_09_add_controlled_shell_verification_tool.md`.
- Target task: this existing `T002_09` task; no child task was created because the
  task is already the narrow documentation-only controlled shell verification
  blueprint slice.
- User context considered: the user ran `/work-task` with no additional narrowing
  instructions.
- Parent task considered:
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`.
- Dependencies and adjacent tasks considered: finalized dependency `T002_07`,
  finalized adjacent `T002_08`, downstream open `T002_10`, and downstream open
  `T002_12`.
- Duplicate check result: no existing
  `docs/developer/specification/shell-verification-contracts.md` document existed before this
  solve, and no separate implementation task already planned this blueprint; the
  existing `T002_09` task is the correct target to sharpen.
- Selected option: create the controlled shell verification architecture blueprint
  document in the solve pass and cross-link it from developer documentation while
  leaving Java source and build files untouched.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/tool-permission-workspace-contracts.md`,
  `docs/developer/specification/patch-edit-proposal-contracts.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- Upstream phase dependency: satisfied; this task had a current specification
  result and dependency impact notes before planning.
- Result: planned as one documentation-only architecture task.
- Open decisions or blockers: none for planning.
- Next recommended phase: run `/solve-task T002_09` as a documentation-only
  architecture design pass.

## Solution Note

Solved as a documentation-only architecture blueprint in
`docs/developer/specification/shell-verification-contracts.md`. The new document defines
Codegeist-owned shell verification request shape, command posture, Plan-mode
denial, Build-mode approval gates, destructive-command safety posture,
workspace-cwd validation, environment and stdin policy, timeout/cancellation
handling, typed shell result and failure shapes, bounded output references,
runtime event/session projection, future Java file maps, illustrative Java
snippets, and future test handoff notes.

The solve pass also linked the new blueprint from `docs/developer/README.md` and
`docs/developer/architecture/architecture.md`. It did not create Java source, tests, package
directories, process execution, PTY support, terminal UI, remote execution, JBang
execution, shell sandboxing, Graphify, Repomix, or runtime behavior.

## Solve Status

- Phase: `/solve-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_09_add_controlled_shell_verification_tool.md`.
- User instructions considered: run the full `/work-task` workflow for this exact
  task path. Prior user decisions remain in effect: this slice is documentation
  only and should create `docs/developer/specification/shell-verification-contracts.md` without
  Java source.
- Upstream phase dependency: satisfied. The target task had a current plan handoff
  before this solve pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Related context files read: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`, finalized
  `T002_07_add_tool_permission_workspace_contracts.md`, finalized
  `T002_08_add_patch_edit_proposal_flow.md`,
  `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/tool-permission-workspace-contracts.md`,
  `docs/developer/specification/patch-edit-proposal-contracts.md`,
  `docs/developer/architecture/architecture.md`, `docs/developer/README.md`, and
  `docs/memory-bank/chat.md`.
- OpenCode evidence considered: `tool/shell.ts` for command parsing, external
  directory and shell permission prompts, cwd/env/stdin posture, timeout/abort
  behavior, streaming output, exit code, and truncation metadata; `tool/tool.ts`
  for tool context, permission hooks, metadata updates, and output truncation;
  `tool/truncate.ts` for output caps and out-of-band output paths;
  `permission/index.ts` for permission request/reply shape and decision scope;
  `tool/external-directory.ts` for external-directory gates; and
  `v2/session-event.ts` plus `v2/session-message.ts` for shell/tool lifecycle
  events and bounded message parts.
- Documentation updates: created
  `docs/developer/specification/shell-verification-contracts.md`, updated
  `docs/developer/README.md`, updated `docs/developer/architecture/architecture.md`, and
  prepared finalization updates for affected tasks and memory.
- Acceptance criteria status: satisfied. Shell execution is specified as a
  permission-gated tool request; Plan mode is denied by default; command results
  include exit code, summary, output reference/truncation, and typed failure
  information; destructive commands are not inferred as safe from generic approval;
  future approved and denied tests are described; and no Java source, tests,
  process runner, package directories, or executor port were created.
- Verification: `git --no-pager diff --check` and
  `git --no-pager diff --check --no-index /dev/null docs/developer/specification/shell-verification-contracts.md`.
- Open decisions or blockers: none for this architecture pass. Future Java
  implementation tasks still need to choose exact APIs, fake-executor test shape,
  process executor behavior, stream handling, and any explicit destructive-command
  workflow.
- Next recommended phase: run `/finalize-task T002_09`.

## Finalization Status

- Phase: `/finalize-task` for
  `docs/tasks/T002_implement-codegeist-mvp-foundation/tasks/T002_09_add_controlled_shell_verification_tool.md`.
- User instructions considered: finalize after solving by checking task impact and
  refreshing affected documentation through update-documentation semantics.
- Upstream phase dependency: satisfied. The target task had top-level
  `status: solved` and a current successful `/solve-task` status before this
  finalization pass.
- Hints and overlays considered: `docs/tasks/hints/opencode-solving-guidance.md`,
  `docs/tasks/hints/opencode-source-solving-guidance.md`,
  `.oc_local/rules/codegeist-task-specification.md`, and
  `.oc_local/rules/architecture-doc.md`.
- Impacted tasks: parent
  `docs/tasks/T002_implement-codegeist-mvp-foundation/task.md`, downstream open
  `T002_10_decide_minimal_storage_ports.md`, and downstream open
  `T002_12_define_extension_and_client_readiness_gates.md`.
- Documentation reviewed through update-documentation semantics:
  `docs/developer/specification/shell-verification-contracts.md`, `docs/developer/README.md`,
  `docs/developer/architecture/architecture.md`, affected T002 task files, and
  `docs/memory-bank/chat.md`.
- Documentation updates: the parent task now records that `T002_09` is finalized;
  `T002_10` records that shell logs and output refs are sensitive or large storage
  artifacts rather than ordinary session text; `T002_12` records that later PF4J,
  JBang, server-triggered verification, and client approval surfaces must satisfy
  this shell readiness posture; and `docs/memory-bank/chat.md` was refreshed to
  make `T002_10` the next useful T002 slice.
- Remaining follow-ups: continue with `T002_10_decide_minimal_storage_ports.md` as
  the next documentation-first T002 slice. Future Java implementation tasks may be
  derived later from `docs/developer/specification/shell-verification-contracts.md` only when
  explicitly requested.
- Verification: `git --no-pager diff --check` and
  `git --no-pager diff --check --no-index /dev/null docs/developer/specification/shell-verification-contracts.md`.
- Result: finalized. No implementation gaps, blockers, Java source, tests, package
  directories, process execution, PTY support, terminal UI, remote execution, JBang
  execution, shell sandboxing, Graphify, Repomix, or runtime behavior were
  introduced.
- Next recommended phase: start `/specify-task T002_10` or `/work-task T002_10`
  when ready to continue the storage posture slice.

## Creation Note

Status: open.

Derived from shell-execution architecture as its own task because shell safety is
a high blocking risk and should stay separate from patch/edit work.
