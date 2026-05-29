# T005 Add Cross-Platform Release And Local Build Smoke

Status: finalized

## Goal

Create a split release-readiness workflow for Codegeist executable artifacts:
GitHub-hosted release builds for Linux, Windows, and macOS, plus local build and
smoke-test paths for Linux and Windows before a release run.

The work should prove the current `--version` behavior on generated artifacts
without expanding Codegeist runtime behavior beyond the existing Spring Shell CLI
bootstrap.

## Child Tasks

- `T005_01_add-local-linux-windows-build-smoke.md` - finalized local Linux and
  Windows build/smoke workflow using direct Linux execution and automated Windows
  QEMU/SSH; verified by `task final-smoke-suite` with Linux and Windows
  jar/native statuses all `passed`.
- `T005_02_add-github-release-build.md` - finalized GitHub Actions release build,
  platform matrix, checksums, and GitHub Release upload; verified by passing
  branch validation, pre-tag validation on `main`, `v0.1.0` tag publication, and
  downloaded asset checksum verification.

## Platform Decisions

- Linux native artifact builds and smokes on `ubuntu-latest` in GitHub Actions.
- Windows native artifact builds and smokes on `windows-latest` in GitHub Actions.
- macOS native artifact builds and smokes only on a GitHub-hosted macOS runner for
  this task group.
- Local validation focuses on Linux and Windows only.
- Local macOS QEMU setup is not part of this task group. Do not add macOS
  bootloader, installer, bypass, or non-Apple virtualization instructions.
- Local Windows validation runs in a real Windows VM over SSH. Compatibility-layer
  smoke paths are out of scope for this task group.

## Non-Goals

- Do not implement provider calls, agent loops, tools, permissions, storage,
  Vaadin, PF4J, JBang, server APIs, or TUI behavior.
- Do not add installers, package-manager publishing, signing, notarization, SBOM,
  or SLSA provenance unless needed as minimal release notes.
- Do not require local macOS virtualization for release validation.
- Do not create placeholder runtime packages or classes for future release
  diagnostics.
- Do not commit generated build artifacts from `target/` or local VM output.

## Parent Acceptance Criteria

- The GitHub build child task can produce and publish release artifacts for Linux,
  Windows, and macOS through GitHub-hosted runners.
- The local build child task can smoke-test Linux and Windows artifacts before a
  release run, or record a precise `skipped` status with a blocker and follow-up.
- Documentation states exactly which platforms were validated locally, which run
  only on GitHub, and what prerequisites local machines or VMs must satisfy.
- `docs/developer/architecture/architecture.md` reflects any implemented release,
  Taskfile, script, or workflow behavior.

## Verification

Each child task owns its narrow verification. Parent-level documentation-only
updates should run:

```bash
git --no-pager diff --check
```

## Planning Notes

- Use `docs/developer/specification/build-release-and-binary-smoke-strategy.md`
  for artifact naming, release gating, and platform status reporting.
- Use `docs/developer/specification/native-packaging-posture.md` for native status
  vocabulary and smoke expectations.
- Prefer minimal scripts that delegate to Maven, Taskfile, and platform-native
  shell commands instead of adding a large release framework.
- Keep release workflow steps visible: tests, jar package, jar smoke, native build,
  native smoke, checksum, artifact upload, and release creation.

## Finalization Notes

- Both child tasks are finalized and satisfy the parent acceptance criteria.
- The implemented local path covers Linux and Windows smoke validation; macOS
  release artifacts are validated on GitHub-hosted macOS runners as intended.
- Documentation impact was reviewed across README, current-state architecture,
  release docs, release strategy, native packaging posture, child task records, and
  project memory. Only task status and memory summary updates were needed in this
  finalization pass.
- Future release work should start from a new focused task or release iteration
  branch and use `/codegeist-release --source <release-branch> --rc 1` for
  publication.
- Verification: `git --no-pager diff --check`.
