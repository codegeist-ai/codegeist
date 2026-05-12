# T001_25 Define Implementation Backlog

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Derive the next implementation tasks from the completed architecture concept.

This task converts architecture into a backlog only after the MVP cut is clear.
It does not implement backlog items.

## Scope

- Convert the MVP cut into implementation tasks.
- Keep each follow-up task independently testable.
- Identify task ordering and dependencies.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What should implementation start with? | The smallest runtime/session/provider/tool/permission/context slice needed for the MVP CLI workflow. |
| How should tasks be shaped? | Narrow, independently verifiable Java implementation tasks that preserve architecture boundaries. |
| What should be deferred? | Later-stage parity features, broad UI/server/plugin work, and unverified risk-heavy areas unless they block MVP. |

## Backlog Derivation Rules

- Start from `T001_22` MVP cut and `T001_24` risk register.
- Prefer tasks that validate architecture assumptions early.
- Each implementation task must list target files/packages, acceptance criteria,
  and verification command or strategy.
- Keep tasks small enough to finish independently.
- Do not mix dependency migration, runtime contracts, tool implementation, and UI
  surfaces in one task unless there is a concrete reason.
- Create follow-up `T002+` task files only when explicitly desired for handoff.

## Likely First Backlog Themes

- Align `app/codegeist` build baseline with the chosen Spring Boot/Spring AI
  target.
- Introduce runtime/session/event domain contracts.
- Add provider configuration and one verified Spring AI provider path.
- Add deterministic context loading for rules/memory/tasks/docs.
- Add first permission-gated tool contracts and narrow verification flow.

## Non-Goals

- Do not implement tasks.
- Do not create broad epic-level follow-ups without concrete verification.
- Do not generate `T002+` task files unless the user or final T001 workflow asks
  for tracked follow-up files.
- Do not include later-stage parity features in the first backlog without MVP
  justification.

## Deliverable

Add an implementation backlog section to the parity document and, if desired,
create follow-up `T002+` task files.

## Acceptance Criteria

- The next three to five implementation tasks are clearly defined.
- Each task has a narrow outcome and verification idea.
- No implementation is performed as part of this architecture task.
- Backlog items map to MVP features and blocking risks.
- Follow-up task creation is explicit, not automatic.

## Verification

- Confirm every follow-up task maps back to the MVP cut.

## Verification Result

- Specified backlog derivation rules, likely first themes, and constraints for
  creating implementation follow-up tasks.

## Solution Note

Status: completed.

The solution pass added `## Implementation Backlog` to
`docs/developer/codegeist-opencode-parity.md`. The section derives narrow
implementation candidates from the MVP cut and risk register, covering build
baseline alignment, runtime/session/event contracts, context loading, provider
configuration, tool/permission/workspace contracts, patch/edit proposal flow,
controlled shell verification, and minimal storage decisions.

No user decision is pending. No `T002+` task files were created because this
architecture task only defines backlog candidates; tracked follow-up task
creation remains explicit.

Verification passed with `git --no-pager diff --check`. A final review confirmed
the backlog candidates map back to MVP features and blocking risks and no
implementation was performed.
