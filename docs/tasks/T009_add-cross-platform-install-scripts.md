# T009 Add Cross-Platform Install Scripts

Status: finalized

## Goal

Add Codegeist install scripts for Linux, macOS, and Windows that can be
downloaded directly from a GitHub Release and executed by users through a small
`curl`-based command.

The scripts should install the current platform-native Codegeist release archive,
verify the downloaded checksum, place the executable and required sidecar files in
a user-local install directory, and print the shell PATH step needed to run
`codegeist`.

## Target Platforms

- Linux x64: install from `codegeist-linux-x64.tar.gz`.
- macOS x64: install from `codegeist-macos-x64.tar.gz`.
- Windows x64: install from `codegeist-windows-x64.zip`.

## Expected Script Shape

- Provide one platform-specific install entrypoint per target platform.
- Support a stable release URL such as GitHub Releases `latest/download`.
- Support a release-asset base URL override so tests can serve local release-shaped
  assets without publishing a real GitHub Release.
- Keep scripts non-interactive by default.
- Download and verify `SHA256SUMS.txt` before installing an archive.
- Install into a user-writable default location, with an override variable or flag.
- Avoid requiring package managers, admin rights, signing, notarization, or native
  OS installer packages in this task.
- Document the exact `curl` command for each platform.

## Acceptance Criteria

- Linux users can download and run the Linux install script with `curl`.
- macOS users can download and run the macOS install script with `curl`.
- Windows users can download the PowerShell install script with `curl.exe` and run
  it.
- A Linux QEMU install smoke boots a fresh Linux x64 guest, downloads the Linux
  install script through `curl`, installs from release-shaped assets served by the
  host, and verifies the installed `codegeist --version` and `codegeist
  --show-config` commands inside the guest.
- The GitHub release native matrix runs each install script against local
  release-shaped assets on its matching native runner, including the macOS script on
  the GitHub macOS runner.
- Each script downloads the matching Codegeist release archive and verifies its
  checksum before installation.
- The scripts install the complete native archive contents, including required
  GraalVM sidecar libraries.
- The scripts do not store secrets, require remote provider credentials, or call
  hosted AI providers.
- Release documentation explains how the install scripts relate to the existing
  release assets.
- Architecture or release docs are updated if script paths, release assets, or user
  installation behavior become current project behavior.

## Implementation Targets

- Add install scripts under a repo-owned path that can be uploaded as GitHub
  Release assets or otherwise downloaded from a stable release URL.
- Add a Linux QEMU install-smoke entrypoint, planned as
  `scripts/tests/qemu-linux-install-smoke.sh`, that owns guest image download,
  cloud-init or equivalent guest setup, host asset serving, SSH execution, status
  reporting, and cleanup/reuse of local VM state.
- Add a Taskfile wrapper, planned as `task qemu-linux-install-smoke`, from
  `app/codegeist/cli/Taskfile.yml`.
- Keep local Linux QEMU VM state under `.local/linux-qemu/`, which is already
  ignored through `/.local/`.
- Prefer Ubuntu 24.04 x64 cloud images for the first Linux QEMU guest because the
  native Linux release archive is glibc-based. Use another Linux guest only if the
  task records why its libc, package, and shell baseline still prove the install
  contract.
- Keep the Linux QEMU install smoke opt-in unless a later task explicitly adds it
  to `final-smoke-suite`; avoid making the current final suite depend on another
  VM image download and boot path by default.

## Implementation Plan

1. Define the install script contract.
   - Add `scripts/install/codegeist-install-linux.sh`,
     `scripts/install/codegeist-install-macos.sh`, and
     `scripts/install/codegeist-install-windows.ps1`.
   - Use release asset filenames that match the script filenames.
   - Add `CODEGEIST_INSTALL_BASE_URL` as the release-asset base URL override;
     default it to the GitHub Releases `latest/download` URL.
   - Use user-writable install locations by default: Linux under
     `${XDG_DATA_HOME:-$HOME/.local/share}/codegeist`, macOS under a user-local
     directory, and Windows under `$env:LOCALAPPDATA\Codegeist`.
   - Keep the extracted platform archive as the runtime unit so the executable and
     required sidecar libraries stay together.

2. Implement Linux and macOS install scripts.
   - Use small Bash or POSIX-style shell scripts with repo-standard headers.
   - Require `curl`, `tar`, and either `sha256sum` or `shasum`.
   - Download `SHA256SUMS.txt` plus the matching platform archive.
   - Verify only the matching archive checksum before extraction.
   - Extract into a temporary directory, then atomically publish the install under a
     stable `current/` directory.
   - Create a user-local `codegeist` wrapper that calls the real executable while
     preserving the caller working directory.
   - Run `codegeist --version` after installation as a quick self-check.

3. Implement the Windows install script.
   - Use PowerShell with `Invoke-WebRequest` or `curl.exe` for downloads.
   - Use `Get-FileHash` for SHA-256 verification.
   - Use `Expand-Archive` for `codegeist-windows-x64.zip`.
   - Install into a user-local directory without requiring administrator rights.
   - Create a `codegeist.cmd` or PowerShell shim under the install tree.
   - Print the PATH update instruction instead of silently changing global user or
     machine PATH state.

4. Extend the release workflow after file names stabilize.
    - Update `.github/workflows/release.yml` to upload the install scripts as
      release assets.
    - Run each install script against the matching native archive on the matching
      release runner before upload.
    - Include the install scripts in checksum generation when that is the chosen
      release contract, or document why only archives are checksum-verified by the
     install scripts.
   - Update release notes with the supported `curl` commands.
   - Keep package-manager publishing, signing, notarization, SBOM, and OS-native
     installers out of this task.

5. Add the Linux QEMU install smoke.
   - Add `scripts/tests/qemu-linux-install-smoke.sh` as the host-side smoke
     entrypoint.
   - Add `task qemu-linux-install-smoke` in `app/codegeist/cli/Taskfile.yml`.
   - Cache the Ubuntu 24.04 x64 cloud image under `.local/linux-qemu/` and verify
     its checksum when the image source publishes one in a script-consumable form.
   - Create a fresh qcow2 overlay per smoke run so the install starts from a clean
     guest state while allowing the base image to be reused.
   - Use `cloud-localds` or equivalent cloud-init seed media for the guest user,
     SSH key, and minimal package prerequisites.
   - Start QEMU with user networking and host SSH port forwarding.
   - Stage local release-shaped assets from the current build output, including
     `codegeist-linux-x64.tar.gz`, `SHA256SUMS.txt`, and the Linux install script.
   - Serve the staged assets from a temporary host HTTP server reachable from the
     guest as `http://10.0.2.2:<port>/`.
   - In the guest, download the Linux install script with `curl`, run it with
     `CODEGEIST_INSTALL_BASE_URL` set to the host asset server, then verify
     `codegeist --version` and `codegeist --show-config` from a separate working
     directory.

6. Keep smoke status and duration output consistent.
   - Report scan-friendly status lines using the existing smoke vocabulary:
     `Platform smoke status`, `Platform`, `Transport`, `Install status`, and
     `Reason` when needed.
   - Print stable duration lines such as
     `Duration: linux qemu install smoke total: <seconds>s`.
   - Fail by default when QEMU, image download, SSH readiness, asset serving, curl
     install, checksum verification, or command verification fails.
   - Allow developer-only skips only through an explicit opt-in such as
     `CODEGEIST_LINUX_QEMU_ALLOW_SKIP=1`.

7. Update documentation with implemented behavior.
   - Update `README.md` with the supported install commands.
   - Update `docs/developer/release/local-build-smoke.md` with the Linux QEMU
     install-smoke workflow.
   - Update `docs/tests/smoke-tests.md` with the new status and duration labels.
   - Update `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
     with install-script assets and validation posture.
   - Update `docs/developer/architecture/architecture.md` once the install scripts,
     release upload path, or QEMU smoke become current implemented behavior.

8. Verify the implementation.
   - Run shell syntax checks for the Linux, macOS, and Linux-QEMU shell scripts.
   - Run a PowerShell parser check for the Windows install script.
   - Run `git --no-pager diff --check`.
   - From `app/codegeist/cli`, run `task test`, `task native-smoke`, and
     `task qemu-linux-install-smoke`.

## Verification

Use the narrowest checks that prove the scripts are syntactically valid and safe to
publish, then run the relevant release/package smoke checks.

Suggested verification:

```bash
git --no-pager diff --check
```

From `app/codegeist/cli`, run the relevant Taskfile checks after script behavior is
implemented:

```bash
task test
task qemu-linux-install-smoke
```

If the scripts package or consume release archives, also run the appropriate native
smoke or release-asset checks for the changed platform behavior.

## Planning Notes

- Use `docs/developer/specification/build-release-and-binary-smoke-strategy.md` for
  artifact names, checksum expectations, and release validation posture.
- Use `docs/developer/release/github-release-build.md` for the current GitHub
  Release asset contract.
- The current release workflow lives at `.github/workflows/release.yml`; update it
  only after the install-script file names, asset names, and local install smoke
  contract are stable.
- Keep this task focused on downloadable bootstrap scripts, not package-manager
  publishing or OS-native installers.

## Actual Implementation

- Added `scripts/install/codegeist-install-linux.sh`,
  `scripts/install/codegeist-install-macos.sh`, and
  `scripts/install/codegeist-install-windows.ps1`.
- The Linux and macOS scripts download `SHA256SUMS.txt` plus the matching native
  archive, verify the archive checksum, install the complete extracted archive
  under a user-local install root, create a `codegeist` wrapper, and run a
  `--version` self-check.
- The Windows script downloads and verifies `codegeist-windows-x64.zip`, installs
  under a user-local root, creates `codegeist.cmd`, and prints the PATH directory.
- All install scripts support `CODEGEIST_INSTALL_BASE_URL` so tests can install
  from locally served release-shaped assets instead of a published GitHub Release.
- Added `scripts/tests/qemu-linux-install-smoke.sh` for the opt-in Linux install
  smoke. It stages local Linux release-shaped assets, serves them from a temporary
  host HTTP server, boots a fresh Ubuntu 24.04 x64 QEMU guest, downloads the Linux
  install script with guest `curl`, runs the installer, and verifies installed
  `codegeist --version` plus `codegeist --show-config`.
- Added `task qemu-linux-install-smoke`, which runs the existing `native` task first
  and then executes the Linux QEMU install smoke.
- Added `scripts/tests/install-script-smoke.ps1`, used by the GitHub native matrix
  to run the Linux, macOS, and Windows install scripts against local release-shaped
  assets on their matching native runners.
- Extended the Windows QEMU smoke path to sync `scripts/install/` into the guest and
  run `scripts/tests/install-script-smoke.ps1` after the Windows native archive
  smoke, proving `codegeist-install-windows.ps1` in the VM as well.
- Updated `.github/workflows/release.yml` to syntax-check and stage the Linux,
  macOS, and Windows install scripts, run install-script smoke after native archive
  smoke, include the scripts in `SHA256SUMS.txt`, and upload them as release assets.
- Updated the Codegeist release command/rule asset lists, README, smoke-test docs,
  release docs, release strategy, and architecture map.

## Actual Verification

Executed locally after implementation:

```bash
bash -n scripts/install/codegeist-install-linux.sh scripts/install/codegeist-install-macos.sh scripts/tests/qemu-linux-install-smoke.sh
pwsh -NoProfile -Command '"scripts/install/codegeist-install-windows.ps1", "scripts/tests/install-script-smoke.ps1", "scripts/tests/artifact-smoke.ps1" | ForEach-Object { $tokens = $null; $errors = $null; [System.Management.Automation.Language.Parser]::ParseFile($_, [ref]$tokens, [ref]$errors) > $null; if ($errors.Count -gt 0) { throw $errors[0] } }'
python3 - <<'PY'
import yaml
from pathlib import Path
with Path('.github/workflows/release.yml').open() as handle:
    yaml.safe_load(handle)
print('workflow yaml parsed')
PY
task -t app/codegeist/cli/Taskfile.yml --list-all
CODEGEIST_INSTALL_BASE_URL=file://... CODEGEIST_INSTALL_DIR=... CODEGEIST_BIN_DIR=... scripts/install/codegeist-install-linux.sh
pwsh -NoProfile -File scripts/tests/install-script-smoke.ps1 -Platform linux-x64 -CliDir app/codegeist/cli -SmokeRoot app/codegeist/cli/target/smoke-test/install-script-local-linux
CODEGEIST_LINUX_QEMU_ALLOW_SKIP=1 CODEGEIST_SMOKE_STATUS_FILE=app/codegeist/cli/target/smoke-test/qemu-linux-install-skip.status CODEGEIST_LINUX_QEMU_IMAGE_URL=file:///does/not/exist scripts/tests/qemu-linux-install-smoke.sh smoke
OLLAMA_CONTAINER_NAME=codegeist-ollama-test OLLAMA_PORT=11435 task test
task qemu-linux-install-smoke
scripts/tests/qemu-linux-install-smoke.sh smoke
scripts/tests/qemu-linux-install-smoke.sh status
OLLAMA_CONTAINER_NAME=codegeist-ollama-test OLLAMA_PORT=11435 CODEGEIST_WINDOWS_OLLAMA_BASE_URL=http://10.0.2.2:11435 task -t app/codegeist/cli/Taskfile.yml qemu-windows-smoke
scripts/tests/qemu-windows-vm.sh status
git --no-pager diff --check
```

Results:

- Shell and PowerShell parser checks passed.
- The release workflow YAML parsed successfully.
- `task --list-all` exposes `qemu-linux-install-smoke`.
- The local fake-archive installer smoke passed and proved file-URL downloads,
  checksum verification, installation, wrapper execution, and `--show-config` from
  a different working directory.
- The new `scripts/tests/install-script-smoke.ps1` local Linux run passed against an
  existing `target/dist/codegeist-linux-x64.tar.gz`, reporting
  `Duration: linux-x64 install script smoke: 2.049s`.
- The developer-only Linux QEMU skip path reported `skipped` with a concrete
  missing-native reason.
- `task test` passed with 167 tests, 0 failures, 0 errors, and 6 skips. The default
  Ollama port `11434` was already occupied by an existing host Ollama service in
  this environment, so verification used `OLLAMA_CONTAINER_NAME=codegeist-ollama-test`
  and `OLLAMA_PORT=11435`; the temporary container was removed afterward.
- `task qemu-linux-install-smoke` passed. It built the Linux native executable,
  downloaded and checksum-verified the Ubuntu 24.04 cloud image, booted the QEMU
  guest, installed from locally served assets, and reported
  `Duration: linux qemu install smoke total: 86.785s`.
- A direct rerun of `scripts/tests/qemu-linux-install-smoke.sh smoke` passed using
  the cached image and existing native output, reporting
  `Duration: linux qemu install smoke total: 32.091s`.
- `scripts/tests/qemu-linux-install-smoke.sh status` reported the Linux VM stopped
  after the smoke.
- `task qemu-windows-smoke` passed after the Windows VM finished first-time
  provisioning. The Windows guest built `codegeist-windows-x64.zip`, passed the
  native archive smoke, ran `codegeist-install-windows.ps1` through
  `install-script-smoke.ps1`, and reported `Native status: passed`,
  `Install status: passed`, `Duration: windows-x64 install script smoke: 3.205s`,
  `Duration: windows platform smoke total: 255.819s`, and
  `Duration: windows qemu smoke total: 262.229s`.
- `scripts/tests/qemu-windows-vm.sh status` reported the Windows VM stopped after
  cleanup.
- `git --no-pager diff --check` passed.
