# T010_01 Add Shared 0BSD And Community Policies

Parent: `T010_build-shared-github-contributor-foundation`

Status: finalized

Roadmap: https://github.com/users/codegeist-ai/projects/1

## Goal

Establish one legal and community-policy baseline across every current public
Codegeist GitHub repository and provide inherited defaults for future repositories.

## Decisions

- Use the OSI-approved Zero Clause BSD License with SPDX identifier `0BSD` for all
  Codegeist-owned source and documentation.
- Accept inbound contributions under the same license without a separate CLA or DCO
  sign-off requirement.
- Preserve all third-party licenses and notices.
- Create `codegeist-ai/.github` for shared community health files.
- Create `codegeist-ai/codegeist-ai` for the personal account profile rendered
  from its root `README.md`.
- Keep a root `LICENSE` in every repository containing Codegeist-owned material;
  GitHub's inherited community files do not replace local license detection.
- Adapt Contributor Covenant 3.0 for all official Codegeist GitHub and Discord
  community spaces.
- Use GitHub private vulnerability reporting where available and publish an email
  address only after confirming that it is monitored.

## Scope

- Create the public `codegeist-ai/.github` and `codegeist-ai/codegeist-ai`
  repositories.
- Add shared `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, and `SUPPORT.md`
  defaults to `codegeist-ai/.github`.
- Add the personal ecosystem map to the root `README.md` of
  `codegeist-ai/codegeist-ai`; personal account profile content belongs only in
  that repository.
- Add the canonical `0BSD` license to `.github`, `codegeist-ai`, `codegeist`,
  `codegeist-agent-kit`, and `codegeist-devcontainer-kit`.
- Add `0BSD` to package metadata where a repository publishes package metadata,
  including the Codegeist CLI Maven POM.
- Ensure generated shared-kit `release` branches and release bundles include the
  applicable license when they distribute Codegeist-owned files.
- Link shared policies from every source README and add local policy overrides only
  for real repository-specific details.
- Enable private vulnerability reporting independently in each public source
  repository when GitHub supports it.
- Document the process for applying the same baseline to a future public repository.

## Acceptance Criteria

- `codegeist-ai/.github` is public and GitHub recognizes its default community
  health files.
- `codegeist-ai/codegeist-ai` is public and GitHub renders its root `README.md` as
  the personal account profile.
- Both account infrastructure repositories have root canonical `0BSD` licenses.
- Every current public Codegeist repository has a root `LICENSE` matching the
  canonical OSI/SPDX `0BSD` text.
- `codegeist/app/codegeist/cli/pom.xml` declares `0BSD` with a stable license URL.
- Shared policy files contain no template placeholders and name a confirmed private
  reporting route.
- Local repository policy files exist only when they add accurate repository-specific
  build, test, security, or ownership guidance.
- Contributor terms state that submitted Codegeist-owned changes are accepted under
  `0BSD` without a CLA or DCO requirement.
- Security guidance directs vulnerability reports away from public issues and warns
  against posting credentials, tokens, or sensitive configuration output.
- Each source README links the effective contribution, conduct, security, support,
  and license information.
- Generated `release` branches do not omit the license from distributed
  Codegeist-owned runtime content.
- The JVM jar contains the canonical license at `META-INF/LICENSE`, every native
  archive contains an exact root `LICENSE`, and the standalone release `LICENSE`
  asset is covered by `SHA256SUMS.txt`.
- No third-party license, notice, vendored source, or submodule content is
  relicensed.
- GitHub detects `0BSD` for every current source and account infrastructure
  repository after publication.

## Repository Targets

- `codegeist-ai/.github`
- `codegeist-ai/codegeist-ai`
- `codegeist-ai/codegeist`
- `codegeist-ai/codegeist-agent-kit`
- `codegeist-ai/codegeist-devcontainer-kit`
- Any additional non-archived public repository created before task completion

## Common File Targets

- `LICENSE`
- `codegeist-ai/codegeist-ai/README.md` for the personal profile
- Source and account-infrastructure repository `README.md` files
- `CONTRIBUTING.md` when a local override is necessary
- `CODE_OF_CONDUCT.md` when a local override is necessary
- `SECURITY.md` when a local override is necessary
- `SUPPORT.md` when a local override is necessary
- Package and release manifest files that must carry license metadata
- `codegeist/.github/workflows/release.yml` and
  `codegeist/scripts/tests/artifact-smoke.ps1`
- Repository-local memory or contributor documentation when present

## Non-Goals

- Do not add a CLA service, DCO bot, copyright assignment, dual licensing, or
  commercial license.
- Do not copy identical policy files into every repository when GitHub inheritance
  works and no local difference exists.
- Do not alter third-party files to make them appear covered by `0BSD`.
- Do not claim that Codegeist runtime or development environments are sandboxed.

## Verification

- Compare every root license with the canonical OSI/SPDX `0BSD` text.
- Parse package metadata and run each repository's focused documentation or release
  manifest checks.
- Build each shared kit's test release and confirm the license is present.
- Query each GitHub community profile and license endpoint after publication.
- Verify the public personal profile renders from
  `codegeist-ai/codegeist-ai/README.md`, not from the `.github` repository.
- Verify private vulnerability reporting and every public policy link manually.

## Open Input

`dev@codegeist.ai` was confirmed as the monitored private address before publication.
