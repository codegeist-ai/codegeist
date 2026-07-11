# T008_07 Add CLI Cloud Login And Sync Slice

Status: open

Parent: `../task.md`

## Goal

Add the first local client flow that logs into Codegeist Cloud and syncs one small
artifact family after the server exposes a safe authenticated API.

The primary login command is:

```bash
codegeist login
```

With no local Codegeist server configured, the command targets
`https://codegeist.cloud`. A later optional form, `codegeist login <server-id>`,
selects one configured Codegeist server URL. This is not an LLM-provider login and
must not be modeled as `provider: codegeist`; it is a login to a Codegeist server
implemented by `app/codegeist/server`.

The local configuration for additional targets should store only the Codegeist
server URL and any server-selection metadata needed by the CLI. The server owns
the external OAuth2/OIDC provider configuration and decides whether authentik,
Google, Keycloak, GitHub through an adapter, or another provider is offered in the
browser flow.

The CLI must always begin login with the selected Codegeist server. authentik,
Google, Keycloak, GitHub, and other external identity providers are never direct
`codegeist login` targets; they are browser redirect destinations chosen by the
Codegeist server.

## Scope

- Choose exactly one artifact family to sync first.
- Add the smallest CLI command or flow needed to authenticate against a Codegeist
  server and sync that family.
- Implement `codegeist login` with the default target `https://codegeist.cloud`
  when no local server target is selected or configured.
- Support a path for `codegeist login <server-id>` only when this slice also adds
  local Codegeist server-url configuration.
- Store the returned Codegeist-owned API token as a credential for the selected
  Codegeist server, not as an upstream LLM-provider credential.
- Do not store or configure external identity-provider URLs as CLI login targets.
- Define conflict handling only as needed for the selected slice.
- Keep local CLI file tools, shell tools, session state, and TUI behavior local.

## Acceptance Criteria

- CLI authentication uses the server auth contract instead of ad hoc credentials.
- `codegeist login` defaults to `https://codegeist.cloud` when no local server
  target is configured.
- Any configured login target stores a Codegeist server URL, not an LLM provider
  configuration.
- The browser flow authenticates against the Codegeist server, which then uses its
  own configured external OAuth2/OIDC providers.
- authentik, Google, Keycloak, GitHub, or similar IdPs are only server-selected
  redirect targets, not direct CLI login targets.
- Sync transfers one artifact family through the server API.
- Secrets are not stored in synced artifact bytes.
- Server and CLI tests cover the selected contract without live hosted-provider calls.

## Verification

```bash
task test
```

Run focused suites from the affected module directories.
