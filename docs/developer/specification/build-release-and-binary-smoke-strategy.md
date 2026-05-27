# Build Release And Binary Smoke Strategy

Planned release strategy for Codegeist GitHub Releases, cross-platform artifacts,
and platform-native binary smoke validation.

## Purpose And Status

This document defines how Codegeist should build, package, publish, and verify
release artifacts once release automation is implemented. It is a strategy and
handoff document only. It does not add GitHub Actions workflows, release scripts,
Maven plugins, Taskfile commands, Java source, tests, installers, signing keys,
notarization setup, package-manager manifests, or runtime behavior.

Use this guide before implementing release CI, release scripts, native packaging
checks, startup budgets, or platform smoke suites. Current implementation remains
the single Spring Boot CLI module under `app/codegeist/cli`.

## Current Baseline

The repository currently has local developer build and smoke entrypoints, not a
GitHub release pipeline.

| Area | Current state |
| --- | --- |
| Module | Single Maven module under `app/codegeist/cli` |
| Java | Java `25` through Maven compiler release |
| Spring Boot | `4.0.6` parent |
| Spring Shell | `4.0.2` dependency baseline; `--version` command implemented |
| Spring AI | BOM `2.0.0-M6`; no provider starters or model calls yet |
| Spring AI Agent Utils | BOM and core dependency `0.7.0` |
| JVM package | Spring Boot executable jar named `target/codegeist.jar` |
| Native package | GraalVM native Maven profile using `native-maven-plugin` `0.10.6` |
| Local commands | `task test`, `task build`, `task native`, `task native-smoke`, `task local-linux-smoke`, `task qemu-windows-smoke`, `task final-smoke-suite`, `task run` |

No GitHub release workflow, platform artifact matrix, checksum generation,
artifact signing, notarization, installer generation, or package-manager
publishing exists yet.

The current local smoke suite lives under `scripts/tests/` and verifies the
implemented `--version` behavior on local Linux artifacts and, when configured, a
Windows QEMU VM over SSH.

## Release Target

GitHub Releases are the deployment target for Codegeist release artifacts.

A future release workflow should publish artifacts only through a GitHub Release
draft or published release associated with the release tag. Each release should
include:

- JVM jar artifact.
- Platform-native archive artifacts when the platform build is available.
- SHA-256 checksum file for every published artifact.
- Validation summary that lists every platform check as `passed`, `skipped`, or
  `failed`.
- Release notes that state the supported platforms, known native limitations, and
  any intentionally skipped checks.
- Provenance notes that identify the source tag, workflow run, runner images, Java
  and GraalVM versions, and checksum generation command.

Signing, notarization, SBOM generation, SLSA-style provenance, installer creation,
and package-manager distribution are future hardening items unless a later release
task explicitly makes one of them release-blocking.

## Platform Matrix

Codegeist release strategy supports Linux, Windows, and macOS. macOS support is
split by CPU architecture because native binaries are architecture-specific.

| Platform target | Initial runner posture | Native artifact target | First-wave expectation |
| --- | --- | --- | --- |
| Linux x64 | GitHub-hosted `ubuntu-latest` or pinned Ubuntu image | `codegeist-<version>-linux-x64.tar.gz` | Build and smoke as soon as release CI exists. |
| Windows x64 | GitHub-hosted `windows-latest` or pinned Windows image | `codegeist-<version>-windows-x64.zip` | Build and smoke as soon as release CI exists. |
| macOS x64 | GitHub-hosted Intel macOS image when available | `codegeist-<version>-macos-x64.tar.gz` | Build and smoke when runner capacity is available. |
| macOS arm64 | GitHub-hosted Apple Silicon image when available | `codegeist-<version>-macos-aarch64.tar.gz` | Target support; may start as `skipped` with an explicit runner/toolchain reason. |

Platform support is not proven by compiling on one operating system and copying an
artifact elsewhere. Native archive smoke checks must unpack and run on the same
operating system and architecture family as the artifact they validate.

## Artifact Matrix

The JVM jar and native distribution archives have separate responsibilities.

| Artifact | Example name | Built from | Verification posture |
| --- | --- | --- | --- |
| JVM jar | `codegeist-<version>.jar` | `task build` / Maven package | Release-blocking once runtime behavior exists. |
| Linux native archive | `codegeist-<version>-linux-x64.tar.gz` | Native compile and package on Linux x64 | Release target; blocking once runner, toolchain, package script, and smoke command are stable. |
| Windows native archive | `codegeist-<version>-windows-x64.zip` | Native compile and package on Windows x64 | Release target; blocking once runner, toolchain, package script, and smoke command are stable. |
| macOS Intel native archive | `codegeist-<version>-macos-x64.tar.gz` | Native compile and package on macOS x64 | Release target; skip only with explicit runner/toolchain reason. |
| macOS Apple Silicon native archive | `codegeist-<version>-macos-aarch64.tar.gz` | Native compile and package on macOS arm64 | Compatibility target; skip only with explicit runner/toolchain reason. |
| Checksums | `SHA256SUMS` or per-artifact `.sha256` files | Platform-neutral checksum step | Required for every uploaded artifact. |

Use names that include the project, version, operating system, and architecture.
Do not reuse `target/codegeist.jar`, `target/codegeist`, or `target/codegeist.exe`
as final release asset names because they do not identify version or platform and
do not package required sidecar libraries.

Native release artifacts are archives, not true single executable files. The
archive is the user download. The extracted directory is the runtime unit because
GraalVM native builds currently emit runtime-required sidecar libraries on Linux
and Windows. See `docs/developer/release/native-distribution-packaging.md` for the
full packaging rationale.

## Verification Gates

Future release CI should use ordered gates so a failure explains which part of the
release is unsafe.

1. Source hygiene: checkout tag, verify clean workspace, run `git --no-pager diff
   --check` for generated release changes when applicable.
2. Tests: run the normal Maven test lifecycle through `task test` from
   `app/codegeist/cli`.
3. JVM package: run `task build` and stage the jar under a release name.
4. JVM startup smoke: run the packaged jar with Spring Shell interactivity disabled
   and a bounded timeout.
5. Native compile: run `task native` on each supported native runner when the
   toolchain is available.
6. Native archive package: collect the executable and required sidecar libraries
   into the platform archive.
7. Native package smoke: unpack the archive into a clean temporary directory and
   run the packaged executable on its own platform with the same bounded startup
   policy.
8. Artifact integrity: generate checksums and verify every checksum before upload.
9. Release draft validation: create or update a GitHub Release draft and attach all
   artifacts, checksums, and validation summaries.
10. Publication: publish the GitHub Release only when required gates passed and all
   skips or failures have approved release notes.

The JVM jar should be the first release-blocking artifact. Native archives are
named release targets and should become blocking for each platform as soon as that
platform's runner, GraalVM toolchain, package naming, unpack step, and smoke
command are stable. Until then, missing native checks must be recorded as
`skipped`, not left implicit.

## Binary Smoke Scenarios

Binary smoke tests prove that a built artifact can start and respond on its target
platform. They are not replacements for unit, contract, integration, or end-to-end
agent workflow tests.

Required smoke checks once the corresponding CLI behavior exists:

- Help output: run `--help` or the Spring Shell equivalent and require exit code
  `0` with bounded output.
- Version output: run `--version`, which is the current implemented no-side-effect
  command.
- Noninteractive startup: run the default no-side-effect command path and require
  the process to exit or report readiness within the timeout defined by the
  release task.
- Basic command invocation: run a stable no-side-effect command once CLI commands
  exist.
- Exit-code behavior: assert expected success and failure exit codes for the smoke
  command being tested.
- Artifact integrity: verify the downloaded artifact checksum before execution.
- Timeout handling: fail the smoke if the process hangs beyond the smoke budget.

Planned command shapes for future CI jobs:

```bash
java -jar codegeist-<version>.jar --version
java -jar codegeist-<version>.jar --help
tar -xzf codegeist-<version>-linux-x64.tar.gz -C /tmp/codegeist-smoke
cd /tmp/codegeist-smoke/codegeist-<version>-linux-x64
./codegeist --version

tar -xzf codegeist-<version>-macos-x64.tar.gz -C /tmp/codegeist-smoke
cd /tmp/codegeist-smoke/codegeist-<version>-macos-x64
./codegeist --version
```

```powershell
Expand-Archive -Force codegeist-<version>-windows-x64.zip $env:TEMP\codegeist-smoke
Set-Location $env:TEMP\codegeist-smoke\codegeist-<version>-windows-x64
.\codegeist.exe --version
```

These examples are planned smoke shapes. They must not be reported as executed
until the corresponding release job or local verification really runs them.

## Implemented Local Smoke Suite

The implemented local suite is a pre-release validation aid until GitHub Actions
release jobs exist.

| Script | Current behavior |
| --- | --- |
| `scripts/tests/local-linux-smoke.sh` | Runs Maven tests, builds `target/codegeist.jar`, verifies jar `--version`, and when `native-image` is available builds, packages, unpacks, and verifies `target/dist/codegeist-<version>-linux-x64.tar.gz`. |
| `scripts/tests/qemu-windows-vm.sh` | Downloads the official Windows Server Evaluation ISO when needed, creates or starts the local Windows QEMU VM, syncs the repo subset, and runs Windows smoke through SSH. Download, ISO, or VM failures fail by default. |
| `scripts/tests/qemu-windows-smoke.sh` | Lower-level SSH wrapper that runs `scripts/tests/windows-smoke.ps1` inside an already reachable Windows VM. |
| `scripts/tests/windows-smoke.ps1` | Runs Windows-side Maven tests, jar package, jar `--version`, and when GraalVM and MSVC Build Tools are available builds, packages, unpacks, and verifies `target/dist/codegeist-<version>-windows-x64.zip`. |
| `scripts/tests/final-smoke-suite.sh` | Runs Linux and Windows local smoke checks. Default mode requires both platforms to pass; `--allow-skips` is developer-only. |

The local suite is intentionally not a release publisher. It does not upload
artifacts, generate checksums, create GitHub Releases, sign binaries, or replace
the future GitHub-hosted Linux, Windows, and macOS release matrix.

## Timing And Budgets

Startup time and smoke duration are release-validation signals. Record them in a
concise summary instead of pasting raw logs.

Each release validation should report:

- Platform and architecture.
- Artifact name and checksum status.
- Command used for JVM startup smoke and native startup smoke.
- Approximate startup duration for each smoke command.
- Total smoke duration per platform.
- Timeout budget used by the job.
- Result: `passed`, `skipped`, or `failed` with reason.

Initial provisional budgets until baseline measurements exist:

| Check | Provisional budget | Notes |
| --- | --- | --- |
| JVM jar non-interactive startup | 15 seconds | Conservative for Spring Boot startup in CI. Refine after measurements. |
| Native non-interactive startup | 5 seconds | Native should start faster, but early GraalVM or runner variance may require adjustment. |
| Help or version command | 5 seconds | Applies once the CLI owns those commands. |
| Platform smoke suite | 60 seconds per artifact | Excludes native compilation time. |

Treat these as release smoke budgets, not ordinary unit-test budgets. A later task
should replace them with measured values after the first stable release matrix is
implemented.

## Skip And Failure Policy

Every platform and native check must end as `passed`, `skipped`, or `failed`.
Do not use vague `unknown` status in release notes, task solve results, or release
validation summaries.

Skipped checks must include:

- Platform and artifact.
- Skipped command or gate.
- Concrete reason, such as unavailable runner, unavailable GraalVM toolchain,
  missing signing/notarization prerequisite, not-yet-implemented command behavior,
  or documentation-only task.
- Owner or follow-up task.
- Whether the skip blocks publication.

Failed checks must include:

- Platform and artifact.
- Failing command.
- Exit code or timeout status.
- Shortest useful error summary.
- Checksum status when artifact execution was attempted.
- Owner or follow-up task.
- Release decision: block, rerun, or publish with explicit exception.

Example status shape:

```text
Platform smoke status: skipped
Platform: macOS arm64
Artifact: codegeist-0.1.0-macos-aarch64.tar.gz
Command: tar -xzf codegeist-0.1.0-macos-aarch64.tar.gz && ./codegeist-0.1.0-macos-aarch64/codegeist --version
Reason: no Apple Silicon release runner configured yet
Owner: future release automation task
Release decision: non-blocking for this pre-matrix release; must be listed in release notes
```

## GitHub Actions Handoff

A later task should implement the workflow. This document only defines the shape.

Illustrative matrix outline:

```yaml
strategy:
  matrix:
    include:
      - os: ubuntu-latest
        artifact_suffix: linux-x64.tar.gz
        native: true
      - os: windows-latest
        artifact_suffix: windows-x64.zip
        native: true
      - os: macos-latest
        artifact_suffix: macos-x64.tar.gz
        native: true
      - os: macos-latest
        artifact_suffix: macos-aarch64.tar.gz
        native: skip-until-runner-confirmed
```

The future workflow should keep build, package, unpacked smoke, checksum,
release-draft, and publish steps visible as separate stages. It should upload logs
as CI artifacts when useful, but release notes should contain only concise
validation summaries.

## Release Candidate Checklist

Before publishing or approving a release candidate, verify:

- Release tag is selected and matches artifact version names.
- `task test` passed from `app/codegeist/cli`.
- JVM jar was built, renamed for release, checksumed, and smoke tested.
- Each platform-native archive is either unpacked and smoke tested on its own
  platform or recorded as `skipped` or `failed` with required details.
- Checksum verification passed before artifact upload.
- Release notes list supported platforms and skipped or failed platform checks.
- GitHub Release draft contains the expected artifacts and checksum files.
- Signing, notarization, SBOM, provenance, installer, or package-manager gaps are
  listed as non-blocking or blocking according to the current release task.
- Startup and smoke durations are summarized for every executed smoke check.
- Publication decision is explicit: publish, block, or publish with approved
  exception.

## Future Implementation Handoff

Likely follow-up owners:

- A future packaging task should validate the JVM package, native archive shape,
  sidecar-library collection, startup behavior, and unpacked executable smoke
  behavior when the implementation reaches packaging-readiness.
- A later release automation task should create GitHub Actions workflows, artifact
  naming scripts, native archive packaging, checksum generation, release
  draft/upload behavior, and platform smoke jobs.
- A later CLI task should add stable `--help` and broader no-side-effect command
  behavior. Release smoke checks can already assert the implemented `--version`
  command.

When any of those tasks implements real behavior, update
`docs/developer/architecture/architecture.md` so it continues to describe current
state rather than planned release strategy.
