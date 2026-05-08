# OpenCode Developer Notes

## Toolchain

- Package manager: Bun `1.3.13` from root `package.json`.
- Root development command: `bun dev`, which dispatches to `packages/opencode` with browser conditions.
- Root test script intentionally fails; run tests from package directories.
- `packages/opencode` exposes `typecheck`, `test`, `test:ci`, `build`, `fix-node-pty`, `dev`, and `db` scripts.

## Package Map

- `packages/opencode`: core runtime, CLI, server, sessions, providers, tools, permissions, config, storage, and plugins.
- `packages/core`: shared utilities and Effect support.
- `packages/app`: Solid web UI.
- `packages/desktop`: Electron desktop app.
- `packages/plugin`: plugin package.
- `packages/sdk/js`: generated JavaScript SDK.
- `packages/web`: website and public docs.

## Runtime Areas To Study First

- `packages/opencode/src/index.ts` for CLI bootstrap and command registration.
- `packages/opencode/src/server/**` for route groups, middleware, and API backend selection.
- `packages/opencode/src/session/**` for prompt/message/event processing.
- `packages/opencode/src/provider/**` for model/provider resolution.
- `packages/opencode/src/tool/**` for built-in and plugin tool handling.
- `packages/opencode/src/config/**` for configuration and agent behavior.

## Development Constraints From Source Docs

- Use package-level test and typecheck commands.
- Regenerate JavaScript SDK after API changes.
- Follow the upstream module-shape guidance in `packages/opencode/AGENTS.md`: avoid `export namespace`, prefer flat exports with self reexports, and avoid multi-sibling directory barrels.
- Effect code has strict patterns around `Effect.gen`, `Effect.fn`, `InstanceState`, and service layering.

## Missing Runtime Evidence

No package tests, typechecks, builds, server starts, SDK generation, migrations, or desktop/web development commands were run during this analysis.
