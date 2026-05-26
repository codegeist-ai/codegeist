# T005_01 Add Local Linux Windows Build Smoke

Status: open

Parent: `../task.md`

## Goal

Add local build and smoke-test entrypoints for Linux and Windows so release
artifacts can be exercised before a GitHub release run. Linux can run directly on
the current development environment or a Linux QEMU guest. Windows should run in a
Windows QEMU VM for release-grade local validation.

## Scope

- Work from the existing Maven module under `app/codegeist/cli`.
- Add local Linux build/smoke commands for the jar and native executable.
- Add local Windows build/smoke commands that run inside a pre-provisioned Windows
  QEMU VM over SSH or another documented non-interactive transport.
- Add optional Linux QEMU smoke support if the user wants the same guest-based
  flow as Windows.
- Document required guest prerequisites: GraalVM Java 25, `native-image`, Maven,
  Git or a repo-copy mechanism, SSH access, and Windows MSVC Build Tools.
- Document artifact paths, smoke commands, timeout expectations, and `passed`,
  `skipped`, or `failed` status reporting.
- Evaluate Wine only as an optional convenience check for running a prebuilt
  Windows executable on Linux, not as the supported Windows build environment.
- Update current-state architecture and user/developer documentation for the
  implemented local build/smoke path.

## Wine Decision

Wine is not the recommended Windows native build path for this task.

GraalVM native-image on Windows expects a real Windows toolchain, especially MSVC
Build Tools and the Visual Studio developer environment. Running that build stack
under Wine is not a supported or release-grade path and would make failures hard
to interpret.

Wine may still be useful as a fast, optional smoke check for an already-built
`codegeist-<version>-windows-x64.exe`, for example to catch a completely broken
binary invocation on Linux. That result must be labeled non-authoritative and
must not replace a Windows QEMU VM or GitHub `windows-latest` smoke for release
validation.

## Non-Goals

- Do not add GitHub Release upload or platform matrix behavior in this child task.
- Do not add local macOS virtualization.
- Do not rely on Wine as the source of truth for Windows builds or Windows release
  validation.
- Do not build Windows artifacts on Linux by cross-compilation unless a later plan
  proves a supported GraalVM and toolchain path.
- Do not commit generated build artifacts from `target/`, VM shared folders, or
  local release output directories.

## Acceptance Criteria

- Local Linux build/smoke can verify jar `--version` output.
- Local Linux build/smoke can verify native `--version` output when GraalVM
  `native-image` is available.
- Local Windows build/smoke can run in a Windows QEMU VM and verify jar
  `--version` output.
- Local Windows build/smoke can run in a Windows QEMU VM and verify native
  `--version` output when GraalVM and MSVC Build Tools are available.
- Missing VM or toolchain prerequisites produce clear `skipped` or `failed` status
  output rather than a vague error.
- Documentation explains how to prepare the Linux and Windows guests and how to
  run each local smoke command.
- Documentation clearly states that Wine checks are optional and non-authoritative.

## Suggested Implementation Targets

Likely files, subject to the plan phase:

- `app/codegeist/cli/Taskfile.yml`
- `app/codegeist/cli/scripts/`
- `scripts/release/` or another small repo-root release-smoke location chosen in
  the plan phase
- `docs/developer/release/`
- `README.md`
- `docs/developer/architecture/architecture.md`
- `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
- `docs/memory-bank/chat.md`

## Verification

Plan the narrowest checks before solving. Expected local checks include at least:

```bash
git --no-pager diff --check
```

From `app/codegeist/cli`:

```bash
mvn --batch-mode --no-transfer-progress test
mvn --batch-mode --no-transfer-progress -DskipTests package
java -jar target/codegeist.jar --version
task native-smoke
```

Expected local QEMU checks after the harness exists:

```bash
# Exact command names are defined by the implementation.
scripts/release/qemu-linux-smoke.sh
scripts/release/qemu-windows-smoke.sh
```

Optional non-authoritative Wine smoke, only if implemented:

```bash
wine codegeist-<version>-windows-x64.exe --version
```

If a local platform smoke is skipped, record `skipped` with the concrete reason,
platform, artifact, command, and follow-up owner.

## Planning Notes

- Prefer QEMU guest execution for Windows because it validates the same operating
  system family as the release artifact.
- Keep host scripts thin: prepare or copy the repo, invoke guest-side Maven or
  Taskfile commands, collect status, and avoid owning VM provisioning unless the
  plan explicitly requires it.
- Use explicit environment variables for VM connection details instead of hard-
  coded local paths or credentials.
