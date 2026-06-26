# Install From GitHub Releases

Install Codegeist from GitHub Release assets without a package manager or
OS-native installer.

These commands apply to releases produced by the current release workflow. Older
releases may not include the install-script assets yet.

## Current Install Shape

Codegeist publishes platform-native archives plus small install scripts:

| Platform | Install script | Native archive |
| --- | --- | --- |
| Linux x64 | `codegeist-install-linux.sh` | `codegeist-linux-x64.tar.gz` |
| macOS x64 | `codegeist-install-macos.sh` | `codegeist-macos-x64.tar.gz` |
| Windows x64 | `codegeist-install-windows.ps1` | `codegeist-windows-x64.zip` |

The scripts download `SHA256SUMS.txt`, verify the matching archive, install the
complete native archive contents in a user-local directory, and create a stable
`codegeist` command wrapper.

Codegeist does not currently publish Homebrew formulae, `.deb` or `.rpm` packages,
MSI installers, notarized macOS apps, or signed installers.

## Linux

Run:

```bash
curl -fsSL https://github.com/codegeist-ai/codegeist/releases/latest/download/codegeist-install-linux.sh | bash
```

Default locations:

| Path | Default |
| --- | --- |
| Install root | `${XDG_DATA_HOME:-$HOME/.local/share}/codegeist` |
| Command wrapper | `$HOME/.local/bin/codegeist` |

If the script prints a PATH hint, add `$HOME/.local/bin` to your shell profile.
Then verify:

```bash
codegeist --version
codegeist --show-config
```

## macOS

Run:

```bash
curl -fsSL https://github.com/codegeist-ai/codegeist/releases/latest/download/codegeist-install-macos.sh | bash
```

Default locations:

| Path | Default |
| --- | --- |
| Install root | `$HOME/Library/Application Support/Codegeist` |
| Command wrapper | `$HOME/.local/bin/codegeist` |

If the script prints a PATH hint, add `$HOME/.local/bin` to your shell profile.
Then verify:

```bash
codegeist --version
codegeist --show-config
```

## Windows

Download and run the PowerShell install script:

```powershell
curl.exe -fsSL -o codegeist-install-windows.ps1 https://github.com/codegeist-ai/codegeist/releases/latest/download/codegeist-install-windows.ps1
pwsh -NoProfile -ExecutionPolicy Bypass -File .\codegeist-install-windows.ps1
```

Default locations:

| Path | Default |
| --- | --- |
| Install root | `%LOCALAPPDATA%\Codegeist` |
| Command shim | `%LOCALAPPDATA%\Codegeist\bin\codegeist.cmd` |

If the script prints a PATH hint, add the printed `bin` directory to your user
`PATH`. Then verify from a new terminal:

```powershell
codegeist.cmd --version
codegeist.cmd --show-config
```

## Custom Asset Or Install Locations

Use environment variables to install from a different release asset location or to
choose custom user-local paths.

| Variable | Purpose |
| --- | --- |
| `CODEGEIST_INSTALL_BASE_URL` | Release asset base URL. Defaults to GitHub Releases `latest/download`. |
| `CODEGEIST_INSTALL_DIR` | Install root for extracted release archives. |
| `CODEGEIST_BIN_DIR` | Directory where the `codegeist` wrapper or `codegeist.cmd` shim is written. |

Example for Linux after downloading `codegeist-install-linux.sh`:

```bash
CODEGEIST_INSTALL_DIR="$HOME/tools/codegeist" \
CODEGEIST_BIN_DIR="$HOME/bin" \
bash codegeist-install-linux.sh
```

Use `bash codegeist-install-macos.sh` with the same variables on macOS.

Example for Windows:

```powershell
$env:CODEGEIST_INSTALL_DIR = "$env:USERPROFILE\tools\codegeist"
$env:CODEGEIST_BIN_DIR = "$env:USERPROFILE\bin"
pwsh -NoProfile -ExecutionPolicy Bypass -File .\codegeist-install-windows.ps1
```

## Update Or Reinstall

Run the same install command again. The installer downloads the current release
assets, verifies the checksum, replaces the platform release directory, and rewrites
the command wrapper.

## Verification Status

The Linux installer path is covered by an opt-in QEMU smoke test that boots a fresh
Linux x64 guest, downloads the install script with `curl`, installs from
release-shaped assets, and verifies installed `codegeist --version` plus
`codegeist --show-config`.

The Windows installer path is covered by the Windows QEMU smoke test. It builds the
Windows native archive inside a real Windows VM, runs `codegeist-install-windows.ps1`
against local release-shaped assets, and verifies the installed `codegeist.cmd`
shim with `--version` and `--show-config`.

The macOS installer uses the same release-asset contract and is run by the GitHub
release workflow on a GitHub-hosted macOS x64 runner.
