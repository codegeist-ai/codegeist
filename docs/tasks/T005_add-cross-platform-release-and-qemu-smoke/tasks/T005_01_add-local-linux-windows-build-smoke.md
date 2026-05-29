# T005_01 Add Local Linux Windows Build Smoke

Status: finalized

Parent: `../task.md`

## Goal

Add local build and smoke-test entrypoints for Linux and Windows so release
artifacts can be exercised before a GitHub release run. Linux runs directly on the
current development environment or inside a Linux guest. Windows runs in a local
Windows QEMU VM over SSH for release-grade local validation.

The local Linux and Windows smoke checks must also be part of one final local
smoke-suite entrypoint so release-readiness checks are easy to run before the
GitHub release workflow exists.

## Scope

- Work from the existing Maven module under `app/codegeist/cli`.
- Keep all local test and smoke scripts under `scripts/tests/`.
- Add local Linux build/smoke commands for the jar and native archive.
- Add local Windows build/smoke commands that create or start a Windows QEMU VM,
  sync the repo subset, and run a Windows-side PowerShell helper over SSH.
- Add a final local smoke-suite command that runs Linux and Windows smoke checks
  and requires both platforms to pass by default.
- Document required guest prerequisites: GraalVM Java 25, `native-image`, Maven,
  Git or a repo-copy mechanism, SSH access, and Windows MSVC Build Tools.
- Document artifact paths, smoke commands, timeout expectations, and `passed`,
  `skipped`, or `failed` status reporting.
- Update current-state architecture and user/developer documentation for the
  implemented local build/smoke path.

## Windows Validation Decision

Windows release-grade local validation must use a real Windows VM or a matching
GitHub-hosted Windows runner. Local compatibility-layer execution is not part of
this task.

The Windows local path owns repeatable VM creation and startup. It downloads the
official Windows Server 2025 Evaluation ISO from Microsoft when no local ISO
exists, while allowing `CODEGEIST_WINDOWS_ISO` and `CODEGEIST_WINDOWS_ISO_URL`
overrides for other official media. Generated VM state, credentials, answer media,
downloaded ISO files, and disk images stay under `.local/windows-qemu` by default.

## Non-Goals

- Do not add GitHub Release upload or platform matrix behavior in this child task.
- Do not add local macOS virtualization.
- Do not add local compatibility-layer smoke checks.
- Do not build Windows artifacts on Linux by cross-compilation unless a later plan
  proves a supported GraalVM and toolchain path.
- Do not commit generated build artifacts from `target/`, VM shared folders, or
  local release output directories.
- Do not hard-code VM credentials, local machine paths, or developer-specific SSH
  configuration in committed files.

## Acceptance Criteria

- Local Linux build/smoke can verify jar `--version` output.
- Local Linux build/smoke can package the native executable, unpack the archive in
  a fresh temp directory, and verify native `--version` output when GraalVM
  `native-image` is available.
- Local Windows build/smoke can run in a Windows QEMU VM and verify jar
  `--version` output.
- Local Windows build/smoke can run in a Windows QEMU VM, package the native
  executable as a zip, unpack it in a fresh temp directory, and verify native
  `--version` output when GraalVM and MSVC Build Tools are available.
- The final local smoke suite runs both Linux and Windows smoke entrypoints.
- The final local smoke suite fails by default unless both Linux and Windows pass.
- Developer-only skip behavior is available only through an explicit
  `--allow-skips` flag.
- Missing VM or toolchain prerequisites produce clear `skipped` or `failed` status
  output rather than a vague error.
- Documentation explains how to prepare the Linux and Windows environments and how
  to run each local smoke command.

## Implementation Targets

- `scripts/tests/native-smoke.sh`
- `scripts/tests/local-linux-smoke.sh`
- `scripts/tests/qemu-windows-vm.sh`
- `scripts/tests/qemu-windows-smoke.sh`
- `scripts/tests/windows-smoke.ps1`
- `scripts/tests/windows-qemu/autounattend.xml`
- `scripts/tests/windows-qemu/setup.ps1`
- `scripts/tests/final-smoke-suite.sh`
- `app/codegeist/cli/Taskfile.yml`
- `docs/developer/release/local-build-smoke.md`
- `docs/developer/release/windows-qemu-smoke.md`
- `README.md`
- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
- `docs/memory-bank/chat.md`

## Verification

Expected local checks include at least:

```bash
git --no-pager diff --check
```

From `app/codegeist/cli`:

```bash
mvn --batch-mode --no-transfer-progress test
mvn --batch-mode --no-transfer-progress -DskipTests package
java -jar target/codegeist.jar --version
task native-smoke
task local-linux-smoke
task final-smoke-suite
```

Expected local QEMU check after the ISO is downloaded or `CODEGEIST_WINDOWS_ISO`
points to an official Windows Server Evaluation ISO:

```bash
scripts/tests/qemu-windows-vm.sh smoke
```

Expected final-suite check after both Linux native-image and the Windows ISO or
existing VM are configured:

```bash
scripts/tests/final-smoke-suite.sh
```

If a local platform smoke is skipped in developer-only mode, record `skipped` with
the concrete reason, platform, artifact, command, and follow-up owner. The default
final suite must turn skipped platforms into a failed suite result.

## Actual Verification

Executed locally after implementation:

```bash
bash -n scripts/tests/native-smoke.sh scripts/tests/local-linux-smoke.sh scripts/tests/qemu-windows-smoke.sh scripts/tests/qemu-windows-vm.sh scripts/tests/final-smoke-suite.sh
task --list-all
CODEGEIST_WINDOWS_ALLOW_SKIP=1 CODEGEIST_SMOKE_STATUS_FILE=app/codegeist/cli/target/smoke-test/qemu-windows-skip.status scripts/tests/qemu-windows-vm.sh smoke
CODEGEIST_WINDOWS_VM_DIR=.local/codegeist-win-download-test/vm CODEGEIST_WINDOWS_ISO_URL=file://.../fake.iso scripts/tests/qemu-windows-vm.sh download
CODEGEIST_SMOKE_STATUS_FILE=app/codegeist/cli/target/smoke-test/qemu-windows-vm-fail.status scripts/tests/qemu-windows-vm.sh smoke
CODEGEIST_WINDOWS_INSTALL_TIMEOUT_SECONDS=2400 scripts/tests/qemu-windows-vm.sh create
CODEGEIST_WINDOWS_NATIVE_MODE=skip scripts/tests/qemu-windows-vm.sh smoke
scripts/tests/qemu-windows-vm.sh smoke
task -t app/codegeist/cli/Taskfile.yml local-linux-smoke
scripts/tests/qemu-windows-vm.sh smoke
task -t app/codegeist/cli/Taskfile.yml final-smoke-suite
mvn --batch-mode --no-transfer-progress test
mvn --batch-mode --no-transfer-progress -DskipTests package
java -jar target/codegeist.jar --version
task native-smoke
task local-linux-smoke
task final-smoke-suite
task qemu-windows-smoke
git --no-pager diff --check
```

Results:

- Shell syntax checks passed.
- `task --list-all` showed `local-linux-smoke`, `qemu-windows-smoke`, and
  `final-smoke-suite`.
- Maven tests passed: 2 tests, 0 failures.
- Jar package succeeded and `java -jar target/codegeist.jar --version` printed
  `0.1.0-SNAPSHOT`.
- `task native-smoke` passed; local native compilation completed in about 47
  seconds and the native archive `--version` smoke passed.
- `task -t app/codegeist/cli/Taskfile.yml local-linux-smoke` passed after the
  archive-smoke update; Linux native-image completed in about 42 seconds,
  `target/dist/codegeist-0.1.0-SNAPSHOT-linux-x64.tar.gz` was packaged, unpacked
  into a fresh temp directory, and the extracted `./codegeist --version` printed
  `0.1.0-SNAPSHOT`.
- `scripts/tests/qemu-windows-vm.sh smoke` reports `skipped` only when
  `CODEGEIST_WINDOWS_ALLOW_SKIP=1` is set.
- `scripts/tests/qemu-windows-vm.sh download` downloaded a fake local `file://`
  ISO into an ignored VM directory, proving the missing-file download path without
  transferring the real multi-GB Windows ISO.
- `scripts/tests/qemu-windows-vm.sh smoke` downloads the default official Windows
  Server Evaluation ISO when no VM disk or local ISO exists during a real default
  run.
- `scripts/tests/qemu-windows-vm.sh create` used the official Windows Server 2025
  Evaluation ISO, completed unattended QEMU installation, provisioned the guest,
  and reached the SSH readiness marker.
- `CODEGEIST_WINDOWS_NATIVE_MODE=skip scripts/tests/qemu-windows-vm.sh smoke`
  passed the Windows jar path and verified host exit status propagation.
- `scripts/tests/qemu-windows-vm.sh smoke` passed with Windows jar and native
  statuses both `passed`; after the archive-smoke update, Windows native-image
  completed in about 2 minutes 6 seconds,
  `target/dist/codegeist-0.1.0-SNAPSHOT-windows-x64.zip` was packaged, unpacked
  into a fresh temp directory, and the extracted `codegeist.exe --version` printed
  `0.1.0-SNAPSHOT`.
- `task -t app/codegeist/cli/Taskfile.yml final-smoke-suite` passed in strict mode;
  Linux and Windows jar/native statuses were all `passed`. In that final run,
  Linux native-image completed in about 48 seconds and Windows native-image
  completed in about 2 minutes 13 seconds before each archive was unpacked and
  smoked.
- `task qemu-windows-smoke` now goes through `scripts/tests/qemu-windows-vm.sh
  smoke` and fails by default unless the Windows VM can be created or started.
- `git --no-pager diff --check` passed.
- PowerShell parser validation was skipped locally because `pwsh` is not installed
  in this environment.

## Solve Notes

- Moved native smoke assertions to `scripts/tests/native-smoke.sh` and updated
  `task native-smoke` to source the new path.
- Added `scripts/tests/local-linux-smoke.sh` for Linux Maven test, jar smoke, and
  native archive smoke when `native-image` is available.
- Added `scripts/tests/qemu-windows-vm.sh` as the Windows QEMU VM automation
  wrapper, `scripts/tests/qemu-windows-smoke.sh` as the lower-level SSH wrapper,
  and `scripts/tests/windows-smoke.ps1` as the Windows-side Maven, jar, and native
  smoke helper.
- Added automatic official Windows Server Evaluation ISO download when no local ISO
  exists, with optional `CODEGEIST_WINDOWS_ISO`, `CODEGEIST_WINDOWS_ISO_URL`, and
  `CODEGEIST_WINDOWS_ISO_SHA256` overrides.
- Added `scripts/tests/windows-qemu/autounattend.xml` and
  `scripts/tests/windows-qemu/setup.ps1` for unattended Windows VM provisioning.
- Added `scripts/tests/final-smoke-suite.sh` to run Linux and Windows smoke checks
  together. Default mode requires both platforms to pass; `--allow-skips` is
  developer-only.
- Added timeout controls for local version smoke execution:
  `CODEGEIST_JAR_SMOKE_TIMEOUT`, `CODEGEIST_NATIVE_SMOKE_TIMEOUT`,
  `CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS`, and
  `CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS`.
- Updated Linux and Windows native smokes to package release-shaped archives under
  `target/dist/`, unpack them into fresh temp directories, and run the packaged
  executable instead of the raw native build output.
- Updated architecture, release strategy, README, developer release docs, project
  memory, and relevant task docs to describe the implemented local smoke path.
- Added detailed Windows QEMU smoke documentation with lifecycle diagrams,
  configuration, artifact paths, status semantics, and troubleshooting notes.
- Kept local compatibility-layer Windows checks out of the implementation.

## Finalization Notes

- Reviewed the parent `T005` task, sibling `T005_02`, release documentation,
  architecture notes, README coverage, and project memory for stale local-smoke
  claims.
- The local Linux and Windows smoke documentation already records the implemented
  strict final suite, artifact paths, status semantics, and Windows QEMU
  troubleshooting guidance.
- No extra child task is needed for local macOS virtualization; it remains outside
  the task group's scope.
- Parent impact: `T005` closes with both release-readiness children finalized.
- Verification: `git --no-pager diff --check`.

## Planning Notes

- Prefer QEMU guest execution for Windows because it validates the same operating
  system family as the release artifact.
- Keep host scripts thin: prepare or copy the repo, invoke guest-side Maven or
  Taskfile commands, collect status, and avoid owning VM provisioning unless the
  plan explicitly requires it.
- Use explicit environment variables for VM connection details instead of hard-
  coded local paths or credentials.
- Keep all local test and smoke scripts under `scripts/tests/`.
