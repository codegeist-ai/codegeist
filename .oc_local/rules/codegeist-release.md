# Codegeist Release Workflow

Use this rule for Codegeist GitHub release work, especially `/codegeist-release`,
`.github/workflows/release.yml`, release tags, release assets, and checksum
verification.

## Release Shape

- Use SemVer tags with a leading `v`, for example `v0.1.0`.
- Pass the Maven release version without the leading `v`, for example
  `-Drevision=0.1.0`.
- Keep the default Maven revision as `0.1.0-SNAPSHOT` between release runs.
- Release work may start on an unversioned work branch. The release command must
  infer the next SemVer tag from the source diff using `.opencode/rules/semver.md`
  before it creates any versioned release branch.
- A release may also start directly from `main` when `main` is clean,
  synchronized with `origin/main`, and already contains the release-ready source
  diff. Direct-main releases infer SemVer from `last-tag..main` and skip
  validation-source and squash-candidate branch creation to avoid an empty commit.
- Use `release/v<version>-github-release-build` as the versioned validation branch
  created from the inferred tag when the original source branch is not already a
  matching `release/v*` branch.
- Use `release/v<version>-codegeist-rc-<n>` for the squashed release-candidate
  branch that is allowed to advance `main`.

## Required Validation Order

1. Infer the release version from the diff between the latest reachable SemVer tag
   and the release source commit, following `.opencode/rules/semver.md`.
2. If the source commit is synchronized `main`, run the direct-main path: skip
   validation-source and squash-candidate creation, then run pre-tag validation
   from `main` before tagging.
3. Otherwise, validate the source commit on a matching `release/v<version>-github-release-build`
   branch. If the original source branch is not already a matching `release/v*`
   branch, create that validation branch from the source commit after version
   inference. Branch runs must build, smoke, checksum, and upload workflow artifacts
   without publishing a GitHub Release.
4. Create a fresh `release/v<version>-codegeist-rc-<n>` branch from current
   `main` and squash the entire validated source diff into exactly one
   candidate commit.
5. Validate the candidate branch with the release workflow. If validation fails,
   fix the source or validation branch and create a new candidate such as `rc-2`; do not
   rewrite the failed candidate.
6. Advance `main` from the passing candidate branch by fast-forward only. Do not
   use a merge commit, GitHub merge button, or force-push.
7. Run pre-tag validation from `main` with `workflow_dispatch` and
   `release_version=<version>`.
8. Create and push the annotated `v*` tag only after pre-tag validation passes.
9. Let the tag-triggered workflow publish the GitHub Release automatically.
10. Verify the published release assets and checksums after the tag run passes.
11. Move the lightweight `latest` tag to the verified `v*` release commit.
12. Create or update the GitHub Release for `latest` by reusing the already
    downloaded and checksum-verified assets from the `v*` release. Do not run
    another build.

## Candidate Promotion Policy

- Direct-main release mode is allowed only when `main` and `origin/main` are
  synchronized before pre-tag validation. Do not create a validation-source branch,
  candidate branch, or empty commit in this mode.
- Original release work branches may be unversioned, but branch validation must run
  on a versioned branch whose leading `release/v<version>` segment matches the
  inferred tag.
- Iteration branches such as `release/v0.1.0-github-release-build` may contain as
  many commits as needed to make the release build pass. If the command creates
  that branch from an unversioned source ref, do not overwrite an existing local or
  remote branch with the same name.
- `main` must receive only one coherent release promotion commit for that release
  workflow slice.
- Build the candidate branch from current `origin/main`, not from the iteration
  branch history.
- Apply the versioned validation branch as a squash merge into the candidate
  branch, then commit the squashed diff with `git commit -F <message-file>`.
- The squash commit message must be detailed. It should include the source branch,
  versioned validation branch, candidate branch, why the squash exists, included
  changes, validation evidence, publication state, and the fast-forward-only rule
  for `main`.
- Keep the squash commit subject in Conventional Commit style, for example
  `ci(release): promote Codegeist v0.1.0 release candidate`.
- Do not use short `git commit -m` messages for the release-candidate squash
  commit.
- If a candidate needs code or workflow changes after validation fails, leave that
  candidate branch intact, update the source or validation branch, and create the next
  candidate branch from current `main`.
- Do not infer the release version from the source branch name. A `release/v*`
  source branch name is only a consistency check after the SemVer decision has been
  made from the actual diff.
- Do not use direct-main release mode to bypass candidate validation for an
  unmerged work branch. Use it only when the release-ready work is already on
  synchronized `main`.

## Publication Policy

- Only pushed `v*` tags may publish GitHub Releases.
- `release/v*` branch runs and `workflow_dispatch` runs must not publish releases.
- Tag runs publish releases automatically; they must not leave the release as a
  draft.
- The `latest` tag is a moving pointer to the current verified `v*` release
  commit. It must not trigger the release workflow.
- The GitHub Release for `latest` is also a moving mirror of the current verified
  `v*` release. It exists so users can download the current release from a stable
  GitHub Release URL.
- The `latest` GitHub Release must reuse the assets from the verified `v*` build;
  it must not run or imply a second release build.
- Do not publish manually uploaded assets that bypass the workflow.
- Do not create or push the final tag if the pre-tag validation run fails,
  remains cancelled, or is skipped without an explicit release decision.
- Do not move `latest` or create/update the `latest` GitHub Release until the
  `v*` tag release has published and downloaded checksum verification has passed.

## Expected Assets

Each release must include exactly the expected Codegeist artifact family for the
selected version:

```text
codegeist-jvm.jar
codegeist-linux-x64.tar.gz
codegeist-windows-x64.zip
codegeist-macos-x64.tar.gz
SHA256SUMS.txt
```

Release asset filenames intentionally omit the version because the GitHub Release
URL and immutable `v*` tag carry the version. Keep `codegeist-jvm.jar` instead of
`codegeist-jvm-any.jar`; the `jvm` suffix already distinguishes the portable JVM
artifact from platform-native archives.

Verify `SHA256SUMS.txt` against the downloaded release assets before reporting the
release as complete.

## Safety Rules

- Run `gh auth status` before using `gh workflow`, `gh run`, or `gh release`.
- Start release execution only from a clean worktree. If uncommitted changes are
  present, stop before fetch, branch creation, tagging, or workflow dispatch.
- Confirm the tag does not already exist locally, remotely, or as a GitHub Release
  before creating it.
- Keep the worktree clean before tagging.
- Prefer annotated tags for human-facing Codegeist releases.
- Keep `v*` release tags immutable. Only `latest` may move, and only to the commit
  already referenced by the verified `v*` release tag.
- Do not upload assets to the `latest` GitHub Release from local build outputs.
  Upload only the assets downloaded from and verified against the current `v*`
  GitHub Release.
- Do not merge a multi-commit release work branch directly into `main`.
- Do not use GitHub squash merge as the source of truth for release promotion;
  prepare and validate the local candidate commit explicitly.
- Do not advance `main` unless the update is a fast-forward to a passing
  candidate commit.
- Never use `git reset`, force-push, or delete tags as part of the normal release
  command. If a bad release tag or release exists, stop and ask for an explicit
  recovery decision.
- Keep release docs and project memory synchronized when workflow behavior changes.
