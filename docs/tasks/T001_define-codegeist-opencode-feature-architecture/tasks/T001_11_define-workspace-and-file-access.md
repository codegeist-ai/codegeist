# T001_11 Define Workspace And File Access

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define safe workspace and file-system access rules for Codegeist.

This task specifies workspace and file boundary policy only. It does not
implement file tools, patch application, ignored-file scanning, or storage.

## Scope

- Define workspace root, allowed paths, ignored paths, and generated output
  handling.
- Define read versus write access.
- Identify path traversal and symlink risks.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode workspace/project context? | A `Workspace` or `WorkspaceRef` boundary around one repository/worktree root. |
| Who owns path safety? | A central workspace service, not individual tools or clients. |
| How does Plan mode read files safely? | Plan may request read-only file access inside allowed workspace paths while respecting ignored/secret-like files. |
| How do writes happen? | Only through write/patch tools after mode, permission, and workspace validation. |
| What is later? | Multi-workspace server operation, worktree management, snapshots, file status APIs, and remote workspace adapters. |

## Boundary Rules

- Workspace service canonicalizes and validates paths before reads or writes.
- Tools receive validated workspace paths or output refs, not arbitrary unchecked
  user/model paths.
- Ignore/generated policies must protect `target/`, build artifacts, heavy
  analysis output, secrets, and dependency directories.
- Symlink traversal must not escape the workspace unless explicitly allowed by a
  later policy.
- Workspace rules apply equally to built-ins, PF4J plugins, and JBang scripts.

## Implementation-Readiness Questions

- Can a file tool ask workspace service whether a path is readable or writable?
- Can generated or ignored files be skipped with an explainable warning event?
- Can file writes produce reviewable patch/edit proposals instead of silent
  mutation?
- Can session metadata store workspace identity without embedding every raw path?
- Can future server mode isolate multiple projects/worktrees?

## Non-Goals

- Do not implement file reads, writes, patch application, or worktree creation.
- Do not define final ignore syntax beyond current repo conventions.
- Do not implement secret scanning.
- Do not decide remote workspace support.

## Deliverable

Add a workspace and file access section to the parity document with workspace
identity, path validation, read/write policy, ignored/generated handling,
symlink risks, and tool boundaries.

## Acceptance Criteria

- Workspace boundary checks are owned by one conceptual service.
- Read-only behavior is compatible with Plan mode.
- Write access is tied to permission approval.
- Built-in, PF4J, and JBang tools all pass through the same workspace policy.
- Path traversal and symlink escape risks are explicitly listed.

## Verification

- Review against repo rules for generated artifacts and ignored files.

## Verification Result

- Specified workspace ownership, path validation, and file-access boundaries for
  later Java implementation.

## Solution Note

Status: completed.

The solution pass added `## Workspace And File Access` to
`docs/developer/codegeist-opencode-parity.md`. The section defines workspace
identity, canonical path validation, read/write policy, ignored/generated
handling, symlink escape risks, output references, and shared tool boundaries.

No user decision is pending. Built-in tools, PF4J plugins, and JBang scripts all
pass through the same workspace policy, with writes tied to mode, permission,
and patch/edit validation.

Verification passed with `git --no-pager diff --check`. A final review confirmed
the policy is compatible with Plan-mode reads and does not implement file tools
or patch application.
