# GitHub Release Build

GitHub-hosted release automation builds, packages, smoke-tests, checksums, and
uploads Codegeist release artifacts.

## Scope

The workflow lives at `.github/workflows/release.yml` and covers the implemented
Spring Boot CLI module under `app/codegeist/cli`. It validates the current
`--version` behavior on generated JVM artifacts and validates both `--version`
and default `--show-config` behavior on native archives. It does not create
installers, signing, notarization, SBOM, SLSA provenance, package-manager
publishing, or runtime behavior beyond the existing no-side-effect commands.

## Triggers

| Trigger | Purpose | Publishes a GitHub Release |
| --- | --- | --- |
| Push to `release/v*` | Branch-based release workflow validation for iteration and release-candidate branches before `main` promotion. | No |
| `workflow_dispatch` | Pre-tag validation or operator rerun from GitHub CLI, API, or UI. | No |
| Push tag `v*` | Release-cycle automation after pre-tag validation passes. | Yes, as a published release |

Branch validation derives the workflow artifact version from the branch name.
Iteration branches may contain multiple commits while the release build is being
fixed and validated. The release command later infers the final release version
from the Git diff between the latest reachable release tag and the source branch
commit, and stops if a versioned release branch prefix conflicts with that
inferred tag. The first release automation iteration branch was:

```text
release/v0.1.0-github-release-build
```

That branch resolves to Maven and artifact version `0.1.0`. Tag runs require the
tag name to match the resolved version, for example `v0.1.0`.

When the iteration branch is ready, create a squashed release-candidate branch
from current `main`, for example:

```text
release/v0.1.0-codegeist-rc-1
```

Candidate branches also resolve to Maven and artifact version `0.1.0` because the
workflow reads the leading `release/v<major>.<minor>.<patch>` segment.

`workflow_dispatch` can pass an explicit SemVer value without the leading `v`:

```bash
gh workflow run release.yml --ref main -f release_version=0.1.0
```

GitHub only exposes `workflow_dispatch` after the workflow file exists on the
default branch. Before the workflow reaches `main`, validate it by pushing a
`release/v*` iteration branch and then a squashed `release/v*` candidate branch.

## Artifact Contract

The workflow produces versionless release assets. The GitHub Release URL and
immutable `v*` tag carry the version, so filenames intentionally omit it:

| Asset | Source job | Smoke command |
| --- | --- | --- |
| `codegeist-jvm.jar` | Ubuntu JVM job | `java -jar codegeist-jvm.jar --version` |
| `codegeist-linux-x64.tar.gz` | Ubuntu native job | unpack and run `./codegeist --version` and `./codegeist --show-config` |
| `codegeist-windows-x64.zip` | Windows native job | unzip and run `codegeist.exe --version` and `codegeist.exe --show-config` |
| `codegeist-macos-x64.tar.gz` | macOS native job | unpack and run `./codegeist --version` and `./codegeist --show-config` |
| `SHA256SUMS.txt` | Checksum job | `sha256sum -c` before upload |

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
   `--version` plus `--show-config` from the extracted directory.
8. Generate and verify `SHA256SUMS.txt`.
9. Upload all assets as workflow artifacts.
10. On `v*` tag runs only, upload the same assets to a published GitHub Release.

## Iteration Branch Validation Flow

Use this while developing release workflow changes. This branch may contain
multiple commits and should not be merged directly to `main`:

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
gh run download <run-id> -n codegeist-linux-x64 -D downloads/linux
gh run download <run-id> -n codegeist-windows-x64 -D downloads/windows
gh run download <run-id> -n codegeist-jvm -D downloads/jvm
```

The Linux artifact contains `codegeist-linux-x64.tar.gz`; the Windows artifact
contains `codegeist-windows-x64.zip`. Extract the archive and keep
the executable beside its sidecar libraries.

## Release Command Flow

After an iteration branch has passed validation, use the repo-local OpenCode
command as the normal release entrypoint:

```text
/codegeist-release --source release/v0.1.1-github-release-build --rc 1
```

The command uses `.opencode/rules/semver.md` to infer the next release version
from the diff between the latest reachable release tag and the source branch
commit:

```bash
git describe --tags --abbrev=0 --match 'v[0-9]*.[0-9]*.[0-9]*' <source-commit>
git log <last-release-tag>..<source-commit> --oneline
git diff <last-release-tag>..<source-commit>
```

The inferred version defines the release tag, Maven `release_version`, and the
candidate branch name. Promote the source diff through a fresh candidate branch
instead of merging the iteration branch directly to `main`.

```bash
git fetch origin
git switch -c release/v0.1.1-codegeist-rc-1 origin/main
git merge --squash origin/release/v0.1.1-github-release-build
```

Inspect the staged diff, then create one detailed release-candidate commit with a
message file:

```bash
git commit -F <message-file>
```

The squash commit message must describe the source branch, candidate branch, why
the squash exists, included changes, validation evidence, publication state, and
the rule that `main` may only be advanced by fast-forward.

Push and validate the candidate branch:

```bash
git push -u origin release/v0.1.1-codegeist-rc-1
gh run list --workflow release.yml --branch release/v0.1.1-codegeist-rc-1
gh run watch <run-id> --exit-status
```

If validation fails, leave the candidate branch intact, fix the iteration branch,
and create the next candidate, for example `release/v0.1.1-codegeist-rc-2`, from
current `main`. Do not amend or force-push a failed candidate branch.

When candidate validation passes, advance `main` by fast-forward only:

```bash
git switch main
git merge --ff-only release/v0.1.1-codegeist-rc-1
git push origin main
```

Do not use a merge commit, GitHub merge button, GitHub squash button, or
force-push for this promotion.

## Pre-Tag Validation Flow

After the validated candidate has fast-forwarded `main`, the release command runs
the same build and smoke matrix without a release tag:

```bash
gh auth status
gh workflow run release.yml --ref main -f release_version=0.1.1
gh run watch <run-id> --exit-status
```

The pre-tag run must pass before creating the final release tag unless a release
decision explicitly records why it was skipped. Pre-tag validation does not publish
a GitHub Release.

## Tag Release Flow

When candidate promotion and pre-tag validation have passed, the release command
creates and pushes the inferred release tag:

```bash
git tag -a v0.1.1 -m "Codegeist v0.1.1"
git push origin v0.1.1
```

The tag push starts the release workflow automatically. The release job creates or
updates a published GitHub Release and uploads the jar, native archives, and
checksum file.

The same repo-local OpenCode command owns the full release sequence:

```text
/codegeist-release --source release/v0.1.1-github-release-build --rc 1
```

It infers the version from Git history, creates and validates the candidate,
fast-forwards `main`, runs pre-tag validation, creates and pushes the annotated
tag after validation passes, waits for the tag-triggered workflow, verifies the
published release assets and checksums, then moves the lightweight `latest` tag to
the verified `v*` release commit. It also creates or updates the GitHub Release
for `latest` using the same downloaded and checksum-verified assets from the `v*`
release; no second build is run for `latest`.

For local post-release verification downloads, prefer a temporary directory outside
the repo when it is writable. In this workspace, using an ignored generated path
under `app/codegeist/cli/target/` is also acceptable:

```bash
gh release download v0.1.0 --dir app/codegeist/cli/target/release-verify-v0.1.0
```

Verify `SHA256SUMS.txt` in the download directory. Do not move `latest` or update
the `latest` GitHub Release until the published `v*` assets verify successfully.
Upload only those verified downloaded files to the `latest` release.

## Status And Skips

Normal release validation expects Linux x64, Windows x64, and macOS x64 jobs to
pass. If a GitHub-hosted platform smoke is skipped or fails, record the platform,
artifact, command, concrete reason, and follow-up owner in the task or release
decision before publishing.
