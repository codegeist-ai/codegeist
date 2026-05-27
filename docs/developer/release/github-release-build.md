# GitHub Release Build

GitHub-hosted release automation builds, packages, smoke-tests, checksums, and
uploads Codegeist release artifacts.

## Scope

The workflow lives at `.github/workflows/release.yml` and covers the implemented
Spring Boot CLI module under `app/codegeist/cli`. It validates the current
`--version` behavior on generated JVM and native artifacts. It does not create
installers, signing, notarization, SBOM, SLSA provenance, package-manager
publishing, or runtime behavior beyond the existing command.

## Triggers

| Trigger | Purpose | Publishes a GitHub Release |
| --- | --- | --- |
| Push to `release/v*` | Branch-based release workflow validation before merging into `main`. | No |
| `workflow_dispatch` | Pre-tag validation or operator rerun from GitHub CLI, API, or UI. | No |
| Push tag `v*` | Release-cycle automation after pre-tag validation passes. | Yes, as a published release |

Branch validation derives the release version from the branch name. The first
release automation branch is:

```text
release/v0.1.0-github-release-build
```

That branch resolves to Maven and artifact version `0.1.0`. Tag runs require the
tag name to match the resolved version, for example `v0.1.0`.

`workflow_dispatch` can pass an explicit SemVer value without the leading `v`:

```bash
gh workflow run release.yml --ref main -f release_version=0.1.0
```

GitHub only exposes `workflow_dispatch` after the workflow file exists on the
default branch. Before the workflow is merged to `main`, validate it by pushing a
`release/v*` branch.

## Artifact Contract

The workflow produces versioned release assets:

| Asset | Source job | Smoke command |
| --- | --- | --- |
| `codegeist-<version>-jvm-any.jar` | Ubuntu JVM job | `java -jar codegeist-<version>-jvm-any.jar --version` |
| `codegeist-<version>-linux-x64.tar.gz` | Ubuntu native job | unpack and run `./codegeist --version` |
| `codegeist-<version>-windows-x64.zip` | Windows native job | unzip and run `codegeist.exe --version` |
| `codegeist-<version>-macos-x64.tar.gz` | macOS native job | unpack and run `./codegeist --version` |
| `codegeist-<version>-SHA256SUMS.txt` | Checksum job | `sha256sum -c` before upload |

Native archives keep the executable and required GraalVM sidecar libraries in one
directory. See `native-distribution-packaging.md` for the archive layout and
sidecar-library rationale.

## Workflow Gates

The implemented jobs run these gates in order:

1. Resolve and validate the release version.
2. Run the Maven test suite with `-Drevision=<version>`.
3. Build the executable JVM jar and smoke `--version`.
4. Build native executables on GitHub-hosted Linux, Windows, and macOS runners.
5. Activate the MSVC tools environment on Windows before Maven native compile.
6. Package native archives with sidecar libraries.
7. Unpack each native archive into a fresh temporary directory and smoke
   `--version` from the extracted directory.
8. Generate and verify `codegeist-<version>-SHA256SUMS.txt`.
9. Upload all assets as workflow artifacts.
10. On `v*` tag runs only, upload the same assets to a published GitHub Release.

## Branch Validation Flow

Use this before merging release workflow changes to `main`:

```bash
git checkout -b release/v0.1.0-github-release-build
git push -u origin release/v0.1.0-github-release-build
```

The push starts the workflow without creating a GitHub Release. Inspect the run:

```bash
gh run list --workflow release.yml --branch release/v0.1.0-github-release-build
gh run watch <run-id> --exit-status
```

Branch-run artifacts are downloaded from the workflow run, not from GitHub
Releases. Use the run page's `Artifacts` section or GitHub CLI:

```bash
gh run download <run-id> -n codegeist-0.1.0-linux-x64 -D downloads/linux
gh run download <run-id> -n codegeist-0.1.0-windows-x64 -D downloads/windows
gh run download <run-id> -n codegeist-0.1.0-jvm-any -D downloads/jvm
```

The Linux artifact contains `codegeist-0.1.0-linux-x64.tar.gz`; the Windows
artifact contains `codegeist-0.1.0-windows-x64.zip`. Extract the archive and keep
the executable beside its sidecar libraries.

## Pre-Tag Validation Flow

After the workflow exists on `main`, run the same build and smoke matrix without a
release tag:

```bash
gh auth status
gh workflow run release.yml --ref main -f release_version=0.1.0
gh run watch <run-id> --exit-status
```

The pre-tag run must pass before creating the final release tag unless a release
decision explicitly records why it was skipped. Pre-tag validation does not publish
a GitHub Release.

## Tag Release Flow

When branch validation and pre-tag validation have passed, create and push the
release tag:

```bash
git tag -a v0.1.0 -m "Codegeist v0.1.0"
git push origin v0.1.0
```

The tag push starts the release workflow automatically. The release job creates or
updates a published GitHub Release and uploads the jar, native archives, and
checksum file.

The repo-local OpenCode command wraps the full release sequence:

```text
/codegeist-release v0.1.0
```

It runs pre-tag validation, creates and pushes the annotated tag after validation
passes, waits for the tag-triggered workflow, then verifies the published release
assets and checksums.

## Status And Skips

Normal release validation expects Linux x64, Windows x64, and macOS x64 jobs to
pass. If a GitHub-hosted platform smoke is skipped or fails, record the platform,
artifact, command, concrete reason, and follow-up owner in the task or release
decision before publishing.
