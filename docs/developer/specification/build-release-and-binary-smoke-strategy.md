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

The repository currently has local developer build entrypoints, not a release
pipeline.

| Area | Current state |
| --- | --- |
| Module | Single Maven module under `app/codegeist/cli` |
| Java | Java `25` through Maven compiler release |
| Spring Boot | `4.0.6` parent |
| Spring Shell | `4.0.2` dependency baseline; no shell commands yet |
| Spring AI | BOM `2.0.0-M6`; no provider starters or model calls yet |
| Spring AI Agent Utils | BOM and core dependency `0.7.0` |
| JVM package | Spring Boot executable jar named `target/codegeist.jar` |
| Native package | GraalVM native Maven profile using `native-maven-plugin` `0.10.6` |
| Local commands | `task test`, `task build`, `task native`, `task run` |

No GitHub release workflow, platform artifact matrix, checksum generation,
artifact signing, notarization, installer generation, package-manager publishing,
or binary smoke suite exists yet.

## Release Target

GitHub Releases are the deployment target for Codegeist release artifacts.

A future release workflow should publish artifacts only through a GitHub Release
draft or published release associated with the release tag. Each release should
include:

- JVM jar artifact.
- Platform-native executable artifacts when the platform build is available.
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
| Linux x64 | GitHub-hosted `ubuntu-latest` or pinned Ubuntu image | `codegeist-<version>-linux-x64` | Build and smoke as soon as release CI exists. |
| Windows x64 | GitHub-hosted `windows-latest` or pinned Windows image | `codegeist-<version>-windows-x64.exe` | Build and smoke as soon as release CI exists. |
| macOS x64 | GitHub-hosted Intel macOS image when available | `codegeist-<version>-macos-x64` | Build and smoke when runner capacity is available. |
| macOS arm64 | GitHub-hosted Apple Silicon image when available | `codegeist-<version>-macos-aarch64` | Target support; may start as `skipped` with an explicit runner/toolchain reason. |

Platform support is not proven by compiling on one operating system and copying an
artifact elsewhere. Native executable smoke checks must run on the same operating
system and architecture family as the artifact they validate.

## Artifact Matrix

The JVM jar and native executables have separate responsibilities.

| Artifact | Example name | Built from | Verification posture |
| --- | --- | --- | --- |
| JVM jar | `codegeist-<version>.jar` | `task build` / Maven package | Release-blocking once runtime behavior exists. |
| Linux native | `codegeist-<version>-linux-x64` | Native compile on Linux x64 | Release target; blocking once runner, toolchain, and smoke command are stable. |
| Windows native | `codegeist-<version>-windows-x64.exe` | Native compile on Windows x64 | Release target; blocking once runner, toolchain, and smoke command are stable. |
| macOS Intel native | `codegeist-<version>-macos-x64` | Native compile on macOS x64 | Release target; skip only with explicit runner/toolchain reason. |
| macOS Apple Silicon native | `codegeist-<version>-macos-aarch64` | Native compile on macOS arm64 | Compatibility target; skip only with explicit runner/toolchain reason. |
| Checksums | `SHA256SUMS` or per-artifact `.sha256` files | Platform-neutral checksum step | Required for every uploaded artifact. |

Use names that include the project, version, operating system, and architecture.
Do not reuse `target/codegeist.jar` or `target/codegeist` as final release asset
names because they do not identify version or platform.

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
6. Native startup smoke: run the built executable on its own platform with the same
   bounded startup policy.
7. Artifact integrity: generate checksums and verify every checksum before upload.
8. Release draft validation: create or update a GitHub Release draft and attach all
   artifacts, checksums, and validation summaries.
9. Publication: publish the GitHub Release only when required gates passed and all
   skips or failures have approved release notes.

The JVM jar should be the first release-blocking artifact. Native binaries are
named release targets and should become blocking for each platform as soon as that
platform's runner, GraalVM toolchain, artifact naming, and smoke command are
stable. Until then, missing native checks must be recorded as `skipped`, not left
implicit.

## Binary Smoke Scenarios

Binary smoke tests prove that a built artifact can start and respond on its target
platform. They are not replacements for unit, contract, integration, or end-to-end
agent workflow tests.

Required smoke checks once the corresponding CLI behavior exists:

- Help output: run `--help` or the Spring Shell equivalent and require exit code
  `0` with bounded output.
- Version output: run `--version` once Codegeist owns version reporting; before
  that exists, record the check as `skipped` with a feature-follow-up reason.
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
./codegeist-<version>-linux-x64 --version
./codegeist-<version>-macos-x64 --version
./codegeist-<version>-macos-aarch64 --version
```

```powershell
.\codegeist-<version>-windows-x64.exe --version
.\codegeist-<version>-windows-x64.exe --help
```

These examples are planned smoke shapes. They must not be reported as executed
until the corresponding release job or local verification really runs them.

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
  missing signing/notarization prerequisite, not-yet-implemented `--version`, or
  documentation-only task.
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
Artifact: codegeist-0.1.0-macos-aarch64
Command: ./codegeist-0.1.0-macos-aarch64 --version
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
        artifact_suffix: linux-x64
        native: true
      - os: windows-latest
        artifact_suffix: windows-x64.exe
        native: true
      - os: macos-latest
        artifact_suffix: macos-x64
        native: true
      - os: macos-latest
        artifact_suffix: macos-aarch64
        native: skip-until-runner-confirmed
```

The future workflow should keep build, smoke, checksum, release-draft, and publish
steps visible as separate stages. It should upload logs as CI artifacts when useful,
but release notes should contain only concise validation summaries.

## Release Candidate Checklist

Before publishing or approving a release candidate, verify:

- Release tag is selected and matches artifact version names.
- `task test` passed from `app/codegeist/cli`.
- JVM jar was built, renamed for release, checksumed, and smoke tested.
- Each platform-native artifact is either smoke tested on its own platform or
  recorded as `skipped` or `failed` with required details.
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

- A future packaging task should validate the JVM package, native posture, startup
  behavior, and executable smoke behavior when the implementation reaches
  packaging-readiness.
- A later release automation task should create GitHub Actions workflows, artifact
  naming scripts, checksum generation, release draft/upload behavior, and platform
  smoke jobs.
- A later CLI task should add stable `--help`, `--version`, and no-side-effect
  command behavior that release smoke checks can assert directly.

When any of those tasks implements real behavior, update
`docs/developer/architecture/architecture.md` so it continues to describe current
state rather than planned release strategy.
