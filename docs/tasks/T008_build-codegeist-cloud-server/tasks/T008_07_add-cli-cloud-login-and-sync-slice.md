# T008_07 Add CLI Cloud Login And Sync Slice

Status: open

Parent: `../task.md`

## Goal

Add the first local client flow that logs into Codegeist Cloud and syncs one small
artifact family after the server exposes a safe authenticated API.

## Scope

- Choose exactly one artifact family to sync first.
- Add the smallest CLI command or flow needed to authenticate and sync that family.
- Define conflict handling only as needed for the selected slice.
- Keep local CLI file tools, shell tools, session state, and TUI behavior local.

## Acceptance Criteria

- CLI authentication uses the server auth contract instead of ad hoc credentials.
- Sync transfers one artifact family through the server API.
- Secrets are not stored in synced artifact bytes.
- Server and CLI tests cover the selected contract without live hosted-provider calls.

## Verification

```bash
task test
```

Run focused suites from the affected module directories.
