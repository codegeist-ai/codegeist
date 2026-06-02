# Build Release And Binary Smoke Strategy

Release strategy for Codegeist GitHub Releases, cross-platform artifacts, and
platform-native binary smoke validation.

## Purpose And Status

This document defines how Codegeist builds, packages, publishes, and verifies
release artifacts. The current implementation is `.github/workflows/release.yml`,
which validates release-shaped artifacts on GitHub-hosted runners and publishes
GitHub Releases for `v*` tag runs. It does not add installers, signing keys,
notarization setup, package-manager manifests, or runtime behavior.

Use this guide before changing release CI, release scripts, native packaging
checks, startup budgets, or platform smoke suites. Current application
implementation remains the single Spring Boot CLI module under `app/codegeist/cli`.

## Current Baseline

The repository currently has local developer build and smoke entrypoints and a
GitHub-hosted release workflow.

| Area | Current state |
| --- | --- |
| Module | Single Maven module under `app/codegeist/cli` |
| Java | Java `25` through Maven compiler release |
| Spring Boot | `4.0.6` parent |
| Spring Shell | `4.0.2` dependency baseline; `--version` command implemented |
| Spring AI | BOM `2.0.0-M6`; `spring-ai-ollama` is present for the focused local provider path, with no Spring AI provider starter auto-configuration |
| Spring AI Agent Utils | BOM and core dependency `0.7.0` |
| JVM package | Spring Boot executable jar named `target/codegeist.jar`; release asset `codegeist-jvm.jar` |
| Native package | GraalVM native Maven profile using `native-maven-plugin` `0.10.6` |
| Local commands | `task test`, `task build`, `task native`, `task native-smoke`, `task local-linux-smoke`, `task qemu-windows-smoke`, `task final-smoke-suite`, `task ollama-start`, `task run` |
| GitHub release workflow | `.github/workflows/release.yml` for `release/v*` iteration and candidate branch validation, `workflow_dispatch` pre-tag validation, and `v*` tag release publication |
| Main promotion | Multi-commit release iteration branches are squashed into `release/v<version>-codegeist-rc-<n>` candidate branches; `main` is advanced by fast-forward only after candidate validation passes |

No artifact signing, notarization, installer generation, SBOM, SLSA provenance, or
package-manager publishing exists yet.

The current local smoke suite lives under `scripts/tests/` and verifies the
implemented `--version` behavior on local Linux artifacts and, when configured, a
Windows QEMU VM over SSH.

## Release Target

GitHub Releases are the deployment target for Codegeist release artifacts.

The implemented release workflow publishes artifacts only through a GitHub Release
associated with a `v*` release tag. Each release includes:

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
| Linux x64 | GitHub-hosted `ubuntu-latest` or pinned Ubuntu image | `codegeist-linux-x64.tar.gz` | Build and smoke as soon as release CI exists. |
| Windows x64 | GitHub-hosted `windows-latest` or pinned Windows image | `codegeist-windows-x64.zip` | Build and smoke as soon as release CI exists. |
| macOS x64 | GitHub-hosted Intel macOS image when available | `codegeist-macos-x64.tar.gz` | Build and smoke when runner capacity is available. |
| macOS arm64 | GitHub-hosted Apple Silicon image when available | `codegeist-macos-aarch64.tar.gz` | Target support; may start as `skipped` with an explicit runner/toolchain reason. |

Platform support is not proven by compiling on one operating system and copying an
artifact elsewhere. Native archive smoke checks must unpack and run on the same
operating system and architecture family as the artifact they validate.

## Artifact Matrix

The JVM jar and native distribution archives have separate responsibilities.

| Artifact | Example name | Built from | Verification posture |
| --- | --- | --- | --- |
| JVM jar | `codegeist-jvm.jar` | Maven package on Ubuntu | Release-blocking for the current `--version` artifact contract. |
| Linux native archive | `codegeist-linux-x64.tar.gz` | Native compile and package on Linux x64 | Release-blocking in the implemented workflow. |
| Windows native archive | `codegeist-windows-x64.zip` | Native compile and package on Windows x64 | Release-blocking in the implemented workflow. |
| macOS Intel native archive | `codegeist-macos-x64.tar.gz` | Native compile and package on macOS x64 | Release-blocking in the implemented workflow. |
| macOS Apple Silicon native archive | `codegeist-macos-aarch64.tar.gz` | Native compile and package on macOS arm64 | Compatibility target; skip only with explicit runner/toolchain reason. |
| Checksums | `SHA256SUMS.txt` | Platform-neutral checksum step | Required for every uploaded artifact. |

Release asset filenames intentionally omit the version because the GitHub Release
URL and immutable `v*` tag carry the version. Use names that include the project,
artifact family, platform, and architecture where needed. The JVM jar uses
`codegeist-jvm.jar`, not `codegeist-jvm-any.jar`, because `jvm` already
distinguishes the portable JVM artifact from platform-native archives. Do not
reuse `target/codegeist.jar`, `target/codegeist`, or `target/codegeist.exe` as
final release asset names because they do not identify the artifact family or
platform and do not package required sidecar libraries.

Native release artifacts are archives, not true single executable files. The
archive is the user download. The extracted directory is the runtime unit because
GraalVM native builds currently emit runtime-required sidecar libraries on Linux
and Windows. See `docs/developer/release/native-distribution-packaging.md` for the
full packaging rationale.

## Verification Gates

Release CI uses ordered gates so a failure explains which part of the release is
unsafe.

1. Source hygiene: checkout tag or release candidate, verify clean workspace, run
   `git --no-pager diff --check` for generated release changes when applicable,
   and verify that any multi-commit iteration branch was promoted through a single
   detailed squash-candidate commit.
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
9. Main promotion: after candidate branch validation, advance `main` by
   fast-forward only from the candidate commit.
10. Release publication: on `v*` tag runs only, create or update a published GitHub
    Release and attach all artifacts and checksums.
11. Post-release verification: download the published assets and verify
     `SHA256SUMS.txt` before reporting the release complete.
12. Latest mirror: after downloaded checksum verification passes, move the
    lightweight `latest` tag to the verified `v*` release commit and create or
    update the `latest` GitHub Release with those same verified downloaded assets.
    Do not run another build for `latest`.

The JVM jar is a release-blocking artifact. Linux x64, Windows x64, and macOS x64
native archives are release-blocking in the implemented GitHub workflow. Missing
native checks must be recorded as `skipped`, not left implicit.

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

Implemented CI smoke command shapes:

```bash
java -jar codegeist-jvm.jar --version
tar -xzf codegeist-linux-x64.tar.gz -C /tmp/codegeist-smoke
cd /tmp/codegeist-smoke/codegeist-linux-x64
./codegeist --version
./codegeist --show-config

tar -xzf codegeist-macos-x64.tar.gz -C /tmp/codegeist-smoke
cd /tmp/codegeist-smoke/codegeist-macos-x64
./codegeist --version
./codegeist --show-config
```

```powershell
Expand-Archive -Force codegeist-windows-x64.zip $env:TEMP\codegeist-smoke
Set-Location $env:TEMP\codegeist-smoke\codegeist-windows-x64
.\codegeist.exe --version
.\codegeist.exe --show-config
```

These examples reflect the implemented release workflow's `--version` and
`--show-config` smoke shape. Do not report broader commands such as `--help` as
executed until the CLI owns that behavior and the corresponding release job really
runs it.

## Implemented Local Smoke Suite

The implemented local suite is a pre-release validation aid alongside GitHub
Actions release jobs.

| Script | Current behavior |
| --- | --- |
| `scripts/tests/local-linux-smoke.sh` | Runs Maven tests, builds `target/codegeist.jar`, verifies jar `--version`, and when `native-image` is available builds, packages, unpacks, and verifies native `--version` plus `--show-config` from `target/dist/codegeist-linux-x64.tar.gz`. |
| `scripts/tests/qemu-windows-vm.sh` | Downloads the official Windows Server Evaluation ISO when needed, creates or starts the local Windows QEMU VM, syncs the repo subset, and runs Windows smoke through SSH. Download, ISO, or VM failures fail by default. |
| `scripts/tests/qemu-windows-smoke.sh` | Lower-level SSH wrapper that runs `scripts/tests/windows-smoke.ps1` inside an already reachable Windows VM. |
| `scripts/tests/windows-smoke.ps1` | Runs Windows-side Maven tests, jar package, jar `--version`, and when GraalVM and MSVC Build Tools are available builds, packages, unpacks, and verifies native `--version` plus `--show-config` from `target/dist/codegeist-windows-x64.zip`. |
| `scripts/tests/final-smoke-suite.sh` | Runs Linux and Windows local smoke checks. Default mode requires both platforms to pass; `--allow-skips` is developer-only. |

The local suite is intentionally not a release publisher. It does not upload
artifacts, generate checksums, create GitHub Releases, sign binaries, or replace
the GitHub-hosted Linux, Windows, and macOS release matrix.

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
can replace them with measured values after several stable release matrix runs.

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
Artifact: codegeist-macos-aarch64.tar.gz
Command: tar -xzf codegeist-macos-aarch64.tar.gz && ./codegeist-macos-aarch64/codegeist --version
Reason: no Apple Silicon release runner configured yet
Owner: future release automation task
Release decision: non-blocking for this pre-matrix release; must be listed in release notes
```

## Implemented GitHub Actions Matrix

`.github/workflows/release.yml` implements the first release matrix:

```yaml
strategy:
  matrix:
    include:
      - platform: linux-x64
        os: ubuntu-latest
        extension: tar.gz
      - platform: windows-x64
        os: windows-latest
        extension: zip
      - platform: macos-x64
        os: macos-15-intel
        extension: tar.gz
```

The workflow keeps build, package, unpacked smoke, checksum, and release publish
steps visible as separate stages. Release notes should contain concise validation
summaries instead of raw logs.

## Release Candidate Checklist

Before publishing or approving a release candidate, verify:

- Release tag is selected and matches artifact version names.
- Release tag was inferred with `.opencode/rules/semver.md` from the diff between
  the latest reachable release tag and the source commit, or any manual override
  was checked against that inferred tag.
- If the source branch was not already a matching `release/v*` branch, a matching
  `release/v<version>-github-release-build` validation branch was created from the
  inferred tag and source commit, unless the release is intentionally running from
  synchronized `main`.
- If the release work was iterated on a multi-commit branch, a fresh
  `release/v<version>-codegeist-rc-<n>` branch exists from current `main` and
  contains exactly one detailed squash commit.
- If the release is running directly from synchronized `main`, no validation-source
  or candidate branch exists and no empty squash commit was created.
- The candidate branch validation run passed before `main` was advanced.
- `main` was advanced by fast-forward only; no merge commit, GitHub merge button,
  GitHub squash button, or force-push was used.
- Maven tests passed from `app/codegeist/cli` with the selected `-Drevision`.
- JVM jar was built, renamed for release, checksumed, and smoke tested.
- Each platform-native archive is either unpacked and smoke tested on its own
  platform or recorded as `skipped` or `failed` with required details.
- Checksum verification passed before artifact upload.
- Release notes list supported platforms and skipped or failed platform checks.
- Published GitHub Release contains the expected artifacts and checksum files.
- The `latest` GitHub Release reuses the assets downloaded from and verified
  against the current `v*` release; it does not use local build outputs or run a
  second build.
- Signing, notarization, SBOM, provenance, installer, or package-manager gaps are
  listed as non-blocking or blocking according to the current release task.
- Startup and smoke durations are summarized for every executed smoke check.
- Publication decision is explicit: publish, block, or publish with approved
  exception.

## Future Implementation Handoff

Likely follow-up owners:

- A future packaging hardening task can add macOS arm64 once runner capacity and
  toolchain behavior are confirmed.
- A future release hardening task can add signing, notarization, SBOM, SLSA
  provenance, and installer/package-manager artifacts when they become release
  goals.
- A later CLI task should add stable `--help` and broader no-side-effect command
  behavior. Release smoke checks can already assert the implemented `--version`
  command.

When any of those tasks implements real behavior, update
`docs/developer/architecture/architecture.md` so it continues to describe current
state rather than stale release strategy.
