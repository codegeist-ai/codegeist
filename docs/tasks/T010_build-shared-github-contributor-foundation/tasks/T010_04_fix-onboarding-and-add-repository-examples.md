# T010_04 Fix Onboarding And Add Repository Examples

Parent: `T010_build-shared-github-contributor-foundation`

Status: open

Roadmap: https://github.com/users/codegeist-ai/projects/1

## Goal

Provide an accurate account-level repository map and a short, repository-specific
path from a fresh clone to a successful normal check and meaningful local change.

## Shared Scope

- Add `codegeist-ai/codegeist-ai/README.md` as the personal account profile and
  public ecosystem map.
- Document `codegeist-ai/.github` separately as the default-community repository;
  it owns inherited community files rather than personal profile content.
- Explain what belongs in `codegeist`, `codegeist-agent-kit`, and
  `codegeist-devcontainer-kit` and where a contributor should open an issue.
- Explain source `main` versus generated `release` branches for both shared kits.
- State that implementation changes begin in the owning source repository, not in a
  consuming submodule checkout or generated release branch.
- Document the cross-repository flow: source change, local tests, source PR, release
  build when applicable, then a separate consuming gitlink update.
- Add a compact first-contribution path and link the account Roadmap.
- Distinguish inherited community guidance from repository-local instructions.

## Codegeist Scope

- Correct stale devcontainer file names, generated-file behavior, workspace paths,
  and environment-file locations.
- List actual host prerequisites and make selective initialization of
  `.devcontainer` plus `.opencode` the normal path.
- Keep recursive third-party source initialization research-only.
- Add safe Ollama, OpenAI, and MCP configuration examples with parser tests.
- Add a short security/trust section covering side-effecting tools, workspace policy,
  trusted local expressions, and sensitive config output.
- Improve current-versus-historical documentation navigation.

## Shared Kit Scope

- Document each kit's source checkout, normal check, release-copy test, generated
  release workflow, consumer installation/update path, and local extension boundary.
- Add small non-destructive usage examples for adding or updating each submodule.
- Make clear which files are source-only and which are shipped on `release`.
- Document how contributors verify a change without publishing a release.

## Expected Codegeist Contributor Path

```bash
git clone https://github.com/codegeist-ai/codegeist.git
cd codegeist
git submodule update --init .devcontainer .opencode
devcontainer up --workspace-folder .
devcontainer exec --workspace-folder . task cli:check
devcontainer exec --workspace-folder . task cli:run -- --version
```

## Codegeist Example Targets

```text
examples/
|-- README.md
|-- codegeist.ollama.yml
|-- codegeist.openai.yml
`-- codegeist.mcp.yml
```

## Acceptance Criteria

- The root `README.md` in `codegeist-ai/codegeist-ai` renders the account profile
  and maps both account infrastructure repositories plus every current source
  repository and contributor role.
- Every source README explains purpose, source branch, normal check, contribution
  guide, Issues, Roadmap, and effective license/policies.
- Shared kit docs clearly prohibit implementation work directly on generated
  `release` branches and consuming submodule checkouts.
- Each source repository's documented normal check works from a fresh clone.
- Codegeist setup matches current devcontainer behavior and does not recursively
  clone third-party references by default.
- Codegeist examples contain no usable credentials and parse through the production
  config parser without making provider calls.
- A contributor can identify which repository owns a core runtime, OpenCode
  workspace, or devcontainer change without private clarification.
- Documentation indexes route contributors to current behavior before historical
  planning records.
- Future-repository guidance requires a purpose statement, ownership boundary,
  normal check, Issues link, Roadmap link, license, and contribution path.
- Both `codegeist-ai/.github` and `codegeist-ai/codegeist-ai` retain root canonical
  `0BSD` licenses and are identified as account infrastructure rather than source
  repositories.

## Common File Targets

- `codegeist-ai/codegeist-ai/README.md`
- `codegeist-ai/.github/README.md` when documenting default-community ownership
- Each source repository's `README.md`
- Each source repository's `CONTRIBUTING.md` override when needed
- Each source repository's developer or release documentation
- `codegeist/examples/`
- `codegeist` example parser tests
- Relevant repository memory files

## Non-Goals

- Do not make every repository use the Codegeist Java devcontainer workflow.
- Do not initialize third-party research submodules by default.
- Do not make real provider calls in examples or docs verification.
- Do not promise sandboxing, permission prompts, or secret redaction that is not
  implemented.
- Do not duplicate an entire shared guide locally when a short repository-specific
  override and link is sufficient.

## Verification

- Follow each source repository's onboarding from a disposable fresh clone.
- Confirm both shared kits can test their release bundle without pushing it.
- Parse every tracked Codegeist example.
- Check every account-profile and README repository link.
- Confirm the profile renders from `codegeist-ai/codegeist-ai/README.md` and the
  `.github` repository remains limited to default-community ownership.
- Run each source repository's normal check and `git diff --check`.
