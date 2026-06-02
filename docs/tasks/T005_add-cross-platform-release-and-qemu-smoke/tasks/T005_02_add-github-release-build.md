# T005_02 Add GitHub Release Build

Status: finalized

Parent: `../task.md`

## Goal

Add the GitHub-hosted release build path for Codegeist. The workflow should build,
package, smoke-test, checksum, and upload release artifacts for Linux,
Windows, and macOS using GitHub-hosted runners.

## Scope

- Work from the existing Maven module under `app/codegeist/cli`.
- Add a GitHub Actions release workflow for `workflow_dispatch` and `v*` tag
  triggers.
- Add a release-cycle automation path that starts the GitHub release workflow
  without requiring a manual click in the GitHub UI.
- Add a pre-tag development validation path using GitHub CLI so the workflow
  configuration and all platform smoke tests can be exercised before a release tag
  is created.
- Support the normal automated release path through a pushed `v*` tag.
- Document any optional command-based trigger, such as `gh workflow run`, only as
  an operator convenience for pre-release or rerun scenarios.
- Run the Maven test suite before release packaging.
- Build and smoke-test the JVM jar on CI.
- Build native executables on GitHub-hosted Linux, Windows, and macOS runners,
  package them with required sidecar libraries, and smoke-test the unpacked
  archives.
- Publish release assets to GitHub Releases with stable versionless artifact names;
  the GitHub Release URL and immutable `v*` tag carry the version.
- Generate and publish SHA-256 checksums for all release assets.
- Publish the GitHub Release automatically from the tag-triggered workflow after
  pre-tag validation passes.
- Update current-state architecture and user/developer documentation for the
  implemented GitHub release path.

## Non-Goals

- Do not add local QEMU VM management in this child task.
- Do not add local compatibility-layer checks in this child task.
- Do not implement installers, signing, notarization, package-manager publishing,
  SBOM, or SLSA provenance.
- Do not change runtime behavior beyond what release smoke checks need to execute
  the existing `--version` command.
- Do not create or push the final release tag until the pre-tag GitHub Actions
  validation run has passed or the release decision explicitly records why it was
  skipped.

## Acceptance Criteria

- A GitHub Actions workflow can be triggered manually and by a `v*` tag.
- During development, `gh` can trigger a pre-tag validation run against the current
  branch or selected ref.
- The pre-tag validation run executes the same Linux, Windows, and macOS build and
  smoke jobs required for the tagged release path.
- The pre-tag validation path does not publish a GitHub Release or require a final
  `v*` tag.
- The release cycle can start the workflow automatically by creating and pushing
  the release tag.
- If a command-based trigger is added, it reports the workflow run URL or run id
  and waits for or documents how to check completion.
- The workflow runs tests before packaging release artifacts.
- The workflow builds a versioned JVM jar asset.
- The workflow builds, packages, and smoke-tests native Linux, Windows, and macOS
  artifacts on their matching GitHub runner platforms.
- Linux native release output is a `tar.gz` archive, Windows native release output
  is a `zip` archive, and each native archive keeps the executable beside required
  GraalVM sidecar libraries.
- Windows native compilation activates the MSVC build tools environment before
  running GraalVM `native-image` through Maven.
- Release asset names include project, artifact family, platform, and architecture
  when needed, but omit the version.
- A checksum file is generated, verified, and uploaded with the release assets.
- The workflow uploads all expected artifacts to a published GitHub Release.
- Release documentation describes how to run the workflow and what artifacts it
  produces.

## Suggested Implementation Targets

Likely files, subject to the plan phase:

- `.github/workflows/release.yml`
- `README.md`
- Optional release orchestration script or Taskfile entrypoint, if the plan phase
  chooses a local command to create/push the tag and trigger the workflow
- `docs/developer/release/`
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
```

Expected CI verification:

```text
Manual workflow run or v* tag release run completes Linux, Windows, and macOS
build, native archive packaging, unpacked smoke, checksum, artifact upload, and
release publication jobs.
```

Expected pre-tag development verification:

```text
gh auth status passes, then gh workflow run starts the release validation workflow
on the current branch or selected ref. gh run watch reports success for Linux,
Windows, and macOS smoke jobs before any final v* release tag is created.
```

Expected release-cycle automation verification:

```text
A release tag push or documented release command starts the GitHub Actions
release workflow without manually opening the GitHub Actions UI.
```

If a GitHub platform smoke is skipped, record `skipped` with the concrete reason,
platform, artifact, command, and follow-up owner.

## Implementation Notes

- Created the implementation on branch `release/v0.1.0-github-release-build` so
  `main` stays unchanged while the workflow is tested.
- Added `.github/workflows/release.yml` for `release/v*` branch validation,
  `workflow_dispatch` pre-tag validation, and `v*` tag release publication.
- The workflow derives version `0.1.0` from branch
  `release/v0.1.0-github-release-build`, accepts `release_version=0.1.0` for
  `workflow_dispatch`, and passes Maven `-Drevision=0.1.0` so `--version` prints
  the release version instead of `0.1.0-SNAPSHOT`.
- Added CI-friendly Maven revision support in `app/codegeist/cli/pom.xml` while
  keeping the local default version `0.1.0-SNAPSHOT`.
- The workflow builds and smokes `codegeist-jvm.jar`,
  `codegeist-linux-x64.tar.gz`, `codegeist-windows-x64.zip`, and
  `codegeist-macos-x64.tar.gz`, then generates and verifies `SHA256SUMS.txt`.
- GitHub Release upload is guarded to `v*` tag runs only and publishes the release
  automatically. Branch and `workflow_dispatch` runs validate artifacts without
  publishing.
- Added `docs/developer/release/github-release-build.md` and updated current-state
  architecture, release strategy, native packaging notes, developer docs, README,
  and project memory for the implemented workflow.

## Verification Notes

- `git --no-pager diff --check` passed.
- `.github/workflows/release.yml` parsed successfully with Python `yaml.safe_load`.
- `actionlint` was not installed in the local environment, so actionlint validation
  was skipped locally.
- From `app/codegeist/cli`, the following local release-version path passed:

```bash
mvn --batch-mode --no-transfer-progress test
mvn --batch-mode --no-transfer-progress -Drevision=0.1.0 -DskipTests package
java -jar target/codegeist.jar --version
```

- The jar smoke printed `0.1.0`, proving the release workflow can override the
  default `0.1.0-SNAPSHOT` project version with Maven `-Drevision=0.1.0`.
- GitHub branch validation passed on
  `release/v0.1.0-github-release-build`:
  `https://github.com/codegeist-ai/codegeist/actions/runs/26534205715`.
- The passing branch run validated metadata resolution, Maven tests,
  JVM jar package and smoke, Linux x64 native package and smoke, Windows x64 native
  package and smoke, macOS x64 native package and smoke, checksum generation, and
  checksum verification.
- The GitHub Release job was correctly skipped because the validation run was
  a `release/v*` branch push, not a `v*` tag push.
- The first attempted branch run,
  `https://github.com/codegeist-ai/codegeist/actions/runs/26532524977`, proved JVM,
  Linux, and Windows behavior but was cancelled after the old `macos-13` runner
  label left the macOS job queued. The workflow now uses `macos-15-intel` for the
  macOS x64 job.
- Full pre-tag validation with `workflow_dispatch` and automatic release
  publication on a `v0.1.0` tag remain release-cycle steps after this branch is
  merged to `main`.
- The release workflow was merged to `main` through PR
  `https://github.com/codegeist-ai/codegeist/pull/1`.
- Pre-tag validation on `main` passed:
  `https://github.com/codegeist-ai/codegeist/actions/runs/26537663964`.
- The annotated tag `v0.1.0` was pushed and the tag-triggered workflow published
  the GitHub Release:
  `https://github.com/codegeist-ai/codegeist/actions/runs/26538176834`.
- The published release is available at
  `https://github.com/codegeist-ai/codegeist/releases/tag/v0.1.0` and is not a
  draft.
- Downloaded release assets verified with
  `sha256sum -c codegeist-0.1.0-SHA256SUMS.txt` under
  `app/codegeist/cli/target/release-verify-v0.1.0.0zZZH3`.

## Finalization Notes

- Reviewed the parent `T005` task, sibling `T005_01`, GitHub release docs,
  architecture notes, README coverage, release workflow rules, and project memory
  for stale release-build claims.
- The GitHub release path is complete for this task group: the workflow is merged
  to `main`, pre-tag validation passed, `v0.1.0` was published, and downloaded
  release assets passed checksum verification.
- The stable future release entrypoint is `/codegeist-release --source
  <release-work-branch> --rc 1`, or `/codegeist-release` from synchronized `main`;
  manual version entry should only be used to resolve an inferred-version conflict.
- Parent impact: `T005` closes with both release-readiness children finalized.
- Verification: `git --no-pager diff --check`.

## Planning Notes

- Keep GitHub workflow steps visible: tests, jar package, jar smoke, native build,
  native archive packaging, unpacked native smoke, checksum, artifact upload, and
  release creation.
- Follow `docs/developer/release/native-distribution-packaging.md`: do not try to
  publish true single executable native artifacts unless a later task proves a
  supported, smoke-tested static or wrapper path without hurting first-start
  latency.
- Keep pre-tag validation and tagged release publication separated. The pre-tag
  `gh workflow run` path should prove build and smoke behavior, while tag push
  remains the source of truth for creating the release.
- Prefer tag-push automation as the release-cycle source of truth. Use
  `workflow_dispatch` or `gh workflow run` as explicit operator controls, not as
  the only automated release path.
- Release workflow work branches may contain multiple commits, but they must be
  promoted through `/codegeist-release --source <release-work-branch> --rc <n>`.
  The command infers SemVer from the diff between the latest reachable release tag
  and the source commit, creates a matching
  `release/v<version>-github-release-build` validation branch when needed, creates
  a fresh `release/v<version>-codegeist-rc-<n>` branch from current `main`, writes
  one detailed squash commit, validates the candidate branch, and advances `main`
  by fast-forward only.
- If synchronized `main` already contains the release-ready work, `/codegeist-release`
  may release directly from `main`, infer SemVer from `last-tag..main`, skip
  validation-source and candidate branches, and start at pre-tag validation.
- Future releases should move the lightweight `latest` tag to the verified `v*`
  release commit after published asset checksum verification passes, then create
  or update the `latest` GitHub Release with the same verified downloaded assets.
  Do not run another build for `latest`.
- Prefer GitHub-hosted runners as the source of truth for macOS release artifacts.
- Keep platform-specific command differences small and explicit rather than hiding
  them behind a large release wrapper.
