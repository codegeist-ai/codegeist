---
description: Validate main, tag, publish, and verify a Codegeist GitHub release
agent: build
---

Release Codegeist from `main` through the GitHub-hosted release workflow.

User request:

```text
$ARGUMENTS
```

Expected syntax:

```text
/codegeist-release v0.1.0
```

Apply `.oc_local/rules/codegeist-release.md`, `.opencode/rules/semver.md`, and
the repository commit and command-execution rules.

## Workflow

1. Parse the requested tag from `$ARGUMENTS`. It must match `vMAJOR.MINOR.PATCH`
   or an explicit SemVer prerelease such as `v0.1.0-rc.1`.
2. Derive `release_version` by removing the leading `v`.
3. Run `gh auth status`. If GitHub CLI is not authenticated, use the `gh-auth`
   skill and stop unless authentication succeeds.
4. Read `docs/developer/release/github-release-build.md`,
   `.github/workflows/release.yml`, and `docs/memory-bank/chat.md` for current
   release context.
5. Verify that the current worktree is clean with
   `git --no-pager status --short --branch`. Stop if any uncommitted changes are
   present.
6. Verify the requested tag does not already exist:
   - `git --no-pager tag --list '<tag>'`
   - `git ls-remote --tags origin '<tag>'`
   - `gh release view '<tag>'`
   Stop if any of these show an existing tag or release.
7. Fetch `origin` and verify that local `main` and `origin/main` point to the same
   commit, or fast-forward local `main` to `origin/main` when it is safe and the
   worktree is clean. Do not create a merge commit.
8. Verify `.github/workflows/release.yml` exists on `main`.
9. Start pre-tag validation on `main`:

```bash
gh workflow run release.yml --ref main -f release_version=<version>
```

10. Locate the created run with `gh run list --workflow release.yml --branch main`
    or the run id returned by GitHub CLI, then wait for it:

```bash
gh run watch <run-id> --exit-status
```

11. Stop if pre-tag validation does not conclude with `success`.
12. Create and push an annotated release tag from the validated `main` commit:

```bash
git tag -a <tag> -m "Codegeist <tag>"
git push origin <tag>
```

13. Locate and watch the tag-triggered release run. It must conclude with
    `success`.
14. Verify the GitHub Release exists, is not a draft, and has the expected tag:

```bash
gh release view <tag> --json tagName,isDraft,isPrerelease,url,assets
```

15. Verify the expected assets are present:

```text
codegeist-<version>-jvm-any.jar
codegeist-<version>-linux-x64.tar.gz
codegeist-<version>-windows-x64.zip
codegeist-<version>-macos-x64.tar.gz
codegeist-<version>-SHA256SUMS.txt
```

16. Download the release assets into a temporary directory and verify checksums.
    Prefer `/tmp/opencode` when it is writable; otherwise use an ignored generated
    path such as `app/codegeist/cli/target/release-verify-<tag>.*`.

```bash
gh release download <tag> --dir <tmp-dir>
(cd <tmp-dir> && sha256sum -c codegeist-<version>-SHA256SUMS.txt)
```

17. Report the release URL, tag, validated workflow run ids, assets, checksum
    result, and any warnings such as GitHub Actions deprecation notices.

## Rules

- Do not create the tag before pre-tag validation passes.
- Do not publish from branch or `workflow_dispatch` runs.
- Do not use `git reset`, force-push, delete tags, or overwrite a release.
- Do not continue when an expected asset or checksum is missing.
- Do not mark the release complete until the GitHub Release is published and the
  downloaded checksums verify.
