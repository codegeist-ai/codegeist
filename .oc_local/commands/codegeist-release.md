---
description: Infer, promote, tag, publish, and verify a Codegeist GitHub release
agent: build
---

Release Codegeist from a release iteration branch through one validated
squash-candidate commit on `main`, then tag, publish, and verify the GitHub
Release.

User request:

```text
$ARGUMENTS
```

Expected syntax:

```text
/codegeist-release --source release/v0.1.1-github-release-build [--rc 1]
```

Options:

- `--source <branch>` is the release iteration branch whose commit should be
  compared against the latest reachable release tag.
- `--rc <n>` selects the candidate branch suffix and defaults to `1`.
- `--tag <vMAJOR.MINOR.PATCH>` is an optional compatibility override. Prefer not
  to pass it; if present, it must exactly match the inferred tag.

Apply `.oc_local/rules/codegeist-release.md`, `.opencode/rules/semver.md`, and
the repository commit and command-execution rules.

## Workflow

1. Parse `$ARGUMENTS` for `--source <branch>`, optional `--rc <n>`, and optional
   `--tag <vMAJOR.MINOR.PATCH>`.
2. If `--source` is missing and the current branch name starts with `release/v`,
   use the current branch as the source. Otherwise stop and ask for `--source`.
3. Run `gh auth status`. If GitHub CLI is not authenticated, use the `gh-auth`
   skill and stop unless authentication succeeds.
4. Read `.opencode/rules/semver.md`, `.oc_local/rules/codegeist-release.md`,
   `docs/developer/release/github-release-build.md`, `.github/workflows/release.yml`,
   and `docs/memory-bank/chat.md` for current release context.
5. Verify that the current worktree is clean with
   `git --no-pager status --short --branch`. Stop if any uncommitted changes are
   present.
6. Fetch `origin` and release tags:

```bash
git fetch --tags origin
```

7. Verify that local `main` and `origin/main` point to the same commit, or
   fast-forward local `main` to `origin/main` when it is safe and the worktree is
   clean. Do not create a merge commit.
8. Resolve the release source commit from the source branch. Prefer
   `origin/<source>` after fetch; otherwise use a local source ref only when it is
   explicit and up to date enough for the release decision.
9. Determine the latest reachable SemVer release tag from the source commit:

```bash
git describe --tags --abbrev=0 --match 'v[0-9]*.[0-9]*.[0-9]*' <source-commit>
```

10. Inspect the commits and actual diff from the last release tag to the source
    commit:

```bash
git log <last-release-tag>..<source-commit> --oneline
git diff <last-release-tag>..<source-commit>
```

11. Apply `.opencode/rules/semver.md` to choose the next SemVer tag from that
    diff. Treat Conventional Commits as hints only; the actual CLI, artifact,
    workflow, and documentation impact in the diff decides the bump. Stop if the
    diff has no release-impacting changes.
12. Derive `release_tag` and `release_version` from that SemVer decision, for
    example `v0.1.1` and `0.1.1`. If `--tag` was supplied, it must exactly match
    the inferred `release_tag`; otherwise stop and report the last tag, source
    commit, inferred bump, inferred tag, and supplied tag.
13. If the source branch name contains a leading `release/v<version>` segment,
    verify that segment matches `release_tag`. Stop on mismatch because source
    branch validation would have used a different artifact version.
14. Resolve the candidate branch as `release/<release-tag>-codegeist-rc-<rc>`, for
    example `release/v0.1.1-codegeist-rc-1`.
15. Verify the inferred tag and candidate branch do not already exist:
    - `git --no-pager tag --list '<release-tag>'`
    - `git ls-remote --tags origin '<release-tag>'`
    - `gh release view '<release-tag>'`
    - `git --no-pager branch --list '<candidate>'`
    - `git ls-remote --heads origin '<candidate>'`
    Stop if any tag, release, or candidate branch already exists. Use the next
    `--rc` value instead of overwriting a candidate branch.
16. Verify the source branch has a successful release workflow run for the inferred
    version, or record why candidate promotion is being prepared without one:

```bash
gh run list --workflow release.yml --branch <source> --json databaseId,status,conclusion,url,headSha
```

17. Create the candidate branch from current `origin/main`:

```bash
git switch -c <candidate> origin/main
```

18. Apply the full source branch diff as a squash merge:

```bash
git merge --squash origin/<source>
```

19. Inspect the staged diff. Stop if the squash introduces unrelated files,
    generated noise, secrets, or a version that does not match the inferred tag.
20. Write a detailed commit message to a temporary file. Prefer `/tmp/opencode`
    when writable; otherwise use an ignored generated path under
    `app/codegeist/cli/target/`.
21. Commit the squash with the message file:

```bash
git commit -F <message-file>
```

22. Verify the candidate is exactly one commit ahead of `origin/main`:

```bash
git --no-pager rev-list --count origin/main..<candidate>
```

23. Push the candidate branch and watch the branch-triggered release workflow:

```bash
git push -u origin <candidate>
gh run list --workflow release.yml --branch <candidate>
gh run watch <run-id> --exit-status
```

24. Stop if candidate validation does not conclude with `success`. Do not amend,
    force-push, or repair the candidate branch. Fix the source branch and rerun
    this command with the next `--rc` value.
25. Advance `main` by fast-forward only from the passing candidate:

```bash
git switch main
git merge --ff-only <candidate>
git push origin main
```

26. Verify `main` and `origin/main` now point to the candidate commit and the
    working tree is clean.
27. Start pre-tag validation on `main`:

```bash
gh workflow run release.yml --ref main -f release_version=<release-version>
```

28. Locate the created run with `gh run list --workflow release.yml --branch main`
    or the run id returned by GitHub CLI, then wait for it:

```bash
gh run watch <run-id> --exit-status
```

29. Stop if pre-tag validation does not conclude with `success`.
30. Create and push an annotated release tag from the validated `main` commit:

```bash
git tag -a <release-tag> -m "Codegeist <release-tag>"
git push origin <release-tag>
```

31. Locate and watch the tag-triggered release run. It must conclude with
    `success`.
32. Verify the GitHub Release exists, is not a draft, and has the expected tag:

```bash
gh release view <release-tag> --json tagName,isDraft,isPrerelease,url,assets
```

33. Verify the expected assets are present:

```text
codegeist-jvm.jar
codegeist-linux-x64.tar.gz
codegeist-windows-x64.zip
codegeist-macos-x64.tar.gz
SHA256SUMS.txt
```

34. Download the release assets into a temporary directory and verify checksums.
    Prefer `/tmp/opencode` when it is writable; otherwise use an ignored generated
    path such as `app/codegeist/cli/target/release-verify-<release-tag>.*`.

```bash
gh release download <release-tag> --dir <tmp-dir>
(cd <tmp-dir> && sha256sum -c SHA256SUMS.txt)
```

35. After the published release assets and checksums verify, move the lightweight
    `latest` tag to the same commit as the immutable `v*` release tag. The `latest`
    tag is the only intentionally moving tag in the normal release workflow, and
    it must not trigger a new build:

```bash
git tag -f latest <release-tag>^{}
remote_latest="$(git ls-remote --tags origin latest | awk '{print $1}')"
if [ -n "$remote_latest" ]; then
  git push --force-with-lease=refs/tags/latest:$remote_latest origin refs/tags/latest
else
  git push origin refs/tags/latest
fi
```

36. Verify `latest` now points to the release commit:

```bash
remote_latest="$(git ls-remote --tags origin latest | awk '{print $1}')"
release_commit="$(git --no-pager rev-parse <release-tag>^{})"
test "$remote_latest" = "$release_commit"
```

37. Create or update the GitHub Release for `latest` from the already downloaded
    and checksum-verified `v*` assets. Do not run a second build and do not upload
    any files that were not downloaded from the verified `v*` release:

```bash
cat > "$tmp_dir/latest-release-notes.md" <<EOF
Codegeist latest release.

This moving release mirrors <release-tag> and reuses the assets built and verified
by the <release-tag> GitHub Actions release workflow. No additional build was run.
EOF

latest_assets=(
  "$tmp_dir/codegeist-jvm.jar"
  "$tmp_dir/codegeist-linux-x64.tar.gz"
  "$tmp_dir/codegeist-windows-x64.zip"
  "$tmp_dir/codegeist-macos-x64.tar.gz"
  "$tmp_dir/SHA256SUMS.txt"
)

if gh release view latest >/dev/null 2>&1; then
  gh release edit latest --title "Codegeist latest" --notes-file "$tmp_dir/latest-release-notes.md" --latest --verify-tag
  gh release upload latest "${latest_assets[@]}" --clobber
else
  gh release create latest "${latest_assets[@]}" --title "Codegeist latest" --notes-file "$tmp_dir/latest-release-notes.md" --latest --verify-tag
fi
```

38. Verify the `latest` GitHub Release exists, is not a draft, has tag `latest`,
    and contains the same expected assets as the `v*` release:

```bash
gh release view latest --json tagName,isDraft,isPrerelease,url,assets
```

39. Report the release URL, `latest` release URL, source branch, source commit,
    last release tag,
    inferred SemVer bump, candidate branch, candidate commit, fast-forwarded
    `main` commit, validated workflow run ids, assets, checksum result, `latest`
    tag update result, and any warnings such as GitHub Actions deprecation notices.

## Squash Commit Message Contract

Use a concise Conventional Commit subject and a detailed body. The body must be
specific enough for a future release audit to understand what was promoted and
why the commit is safe to fast-forward into `main`.

Template:

```text
ci(release): promote Codegeist <release-tag> release candidate

Squash-promote the validated release implementation branch into a single
release-candidate commit for main.

Source branch:
- <source>

Source commit:
- <source-commit>

Version inference:
- Last release tag: <last-release-tag>
- Diff range: <last-release-tag>..<source-commit>
- SemVer bump: <patch|minor|major>
- Inferred release tag: <release-tag>

Candidate branch:
- <candidate>

Why this commit exists:
- The source release branch may contain multiple implementation, validation, and
  fixup commits while the release workflow is iterated.
- main should receive only one coherent release workflow commit for this release
  slice.
- The candidate branch is rebuilt from current main and contains the squashed
  diff only.
- main must later be advanced by fast-forward only; no merge commit is allowed.

Included changes:
- <high-signal summary of release workflow, build, test, docs, and command changes>

Validation evidence and gates:
- Source branch validation run: <run-id or URL>
- Candidate branch validation: required after this commit is pushed and before
  main is fast-forwarded.
- Local checks before candidate push: <commands and results>

Publication state:
- This commit does not create or push the final <release-tag> tag.
- This commit does not publish a GitHub Release.
- Tagging and publication happen only after main is fast-forwarded and pre-tag
  validation passes.

Main promotion rule:
- Advance main only by fast-forward from this candidate commit.
- Do not use a merge commit.
- Do not squash through GitHub's merge button.
- Do not rebase or force-push main.
```

## Rules

- Do not ask for a manual release version during normal release execution. Infer
  it from the diff between the latest reachable release tag and the release source
  commit, using `.opencode/rules/semver.md`.
- Do not split candidate promotion into a separate local command; this command owns
  source-branch version inference, candidate creation, fast-forward promotion,
  tagging, publication, and post-release verification.
- Do not merge the multi-commit source branch directly into `main`.
- Do not use GitHub's merge or squash button for release promotion.
- Do not use a short `git commit -m` message for the candidate squash commit.
- Do not create the tag before pre-tag validation passes.
- Do not move `latest` or create/update the `latest` GitHub Release until the final
  `v*` tag run has published the release and downloaded checksum verification has
  passed.
- Do not run another build for the `latest` GitHub Release. It must reuse the
  already downloaded and verified assets from the immutable `v*` release.
- Do not force-update any `v*` release tag. Only `latest` is allowed to move, and
  it must point to the same commit as the verified release tag.
- Do not publish from branch or `workflow_dispatch` runs.
- Do not use `git reset`, force-push, delete tags, or overwrite a release.
- Do not continue when an expected asset or checksum is missing.
- Do not mark the release complete until the GitHub Release is published and the
  downloaded checksums verify.
