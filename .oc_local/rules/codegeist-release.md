# Codegeist Release Workflow

Use this rule for Codegeist GitHub release work, especially `/codegeist-release`,
`.github/workflows/release.yml`, release tags, release assets, and checksum
verification.

## Release Shape

- Use SemVer tags with a leading `v`, for example `v0.1.0`.
- Pass the Maven release version without the leading `v`, for example
  `-Drevision=0.1.0`.
- Keep the default Maven revision as `0.1.0-SNAPSHOT` between release runs.
- Use a `release/v*` branch for workflow development and branch validation before
  merging release automation to `main`.

## Required Validation Order

1. Validate workflow changes on a `release/v*` branch. Branch runs must build,
   smoke, checksum, and upload workflow artifacts without publishing a GitHub
   Release.
2. Merge the validated branch to `main`.
3. Run pre-tag validation from `main` with `workflow_dispatch` and
   `release_version=<version>`.
4. Create and push the annotated `v*` tag only after pre-tag validation passes.
5. Let the tag-triggered workflow publish the GitHub Release automatically.
6. Verify the published release assets and checksums after the tag run passes.

## Publication Policy

- Only pushed `v*` tags may publish GitHub Releases.
- `release/v*` branch runs and `workflow_dispatch` runs must not publish releases.
- Tag runs publish releases automatically; they must not leave the release as a
  draft.
- Do not publish manually uploaded assets that bypass the workflow.
- Do not create or push the final tag if the pre-tag validation run fails,
  remains cancelled, or is skipped without an explicit release decision.

## Expected Assets

Each release must include exactly the expected Codegeist artifact family for the
selected version:

```text
codegeist-<version>-jvm-any.jar
codegeist-<version>-linux-x64.tar.gz
codegeist-<version>-windows-x64.zip
codegeist-<version>-macos-x64.tar.gz
codegeist-<version>-SHA256SUMS.txt
```

Verify `codegeist-<version>-SHA256SUMS.txt` against the downloaded release assets
before reporting the release as complete.

## Safety Rules

- Run `gh auth status` before using `gh workflow`, `gh run`, or `gh release`.
- Confirm the tag does not already exist locally, remotely, or as a GitHub Release
  before creating it.
- Keep the worktree clean before tagging.
- Prefer annotated tags for human-facing Codegeist releases.
- Never use `git reset`, force-push, or delete tags as part of the normal release
  command. If a bad release tag or release exists, stop and ask for an explicit
  recovery decision.
- Keep release docs and project memory synchronized when workflow behavior changes.
