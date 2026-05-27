# T005_02 Add GitHub Release Build

Status: open

Parent: `../task.md`

## Goal

Add the GitHub-hosted release build path for Codegeist. The workflow should build,
package, smoke-test, checksum, and upload versioned release artifacts for Linux,
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
- Publish release assets to GitHub Releases with versioned artifact names.
- Generate and publish SHA-256 checksums for all release assets.
- Create the GitHub Release as a draft unless the solve phase intentionally
  chooses and documents a different publication policy.
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
- Release asset names include project, version, platform, and architecture.
- A checksum file is generated, verified, and uploaded with the release assets.
- The workflow uploads all expected artifacts to a draft GitHub Release.
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
draft release creation jobs.
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
- Prefer GitHub-hosted runners as the source of truth for macOS release artifacts.
- Keep platform-specific command differences small and explicit rather than hiding
  them behind a large release wrapper.
