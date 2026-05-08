# OpenCode User Notes

## Installation And Entry Points

The upstream README documents install options through the hosted install script, npm-compatible package managers, Homebrew, Scoop/Chocolatey, Arch packages, Mise, and Nix.

The primary command is:

```bash
opencode
```

The README also describes a beta desktop app for macOS, Windows, and Linux.

## Main Workflows

- Interactive terminal use through the default TUI.
- Read-only or analysis-oriented work with the plan agent.
- Full development work with the build agent.
- Headless API server use through `opencode serve`.
- Browser client use through `opencode web`.
- Programmatic usage through generated SDKs.

## Notable User Controls

- OpenCode is provider-agnostic and supports many model providers.
- Built-in LSP support is opt-in according to the upstream README.
- A client/server design allows non-TUI clients to drive the same core runtime.
- Server deployments should review authentication and `OPENCODE_SERVER_PASSWORD`; static analysis found a warning path when the password is missing, but no runtime validation was performed.

## Missing Runtime Evidence

This analysis did not install OpenCode, run a TUI session, start a server, connect a browser client, authenticate providers, or invoke tools.
