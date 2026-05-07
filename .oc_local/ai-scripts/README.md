# Local AI Scripts

Project-specific helper scripts for repeatable OpenCode workflows.

## Scripts

- `render-mermaid.sh` - renders `.mmd` files from a diagram `source/`
  directory into sibling `rendered/*.svg` files using `mmdc`.
- `analyse-project.sh` - prepares `docs/third-party/<project-name>/` from a
  local directory or GitHub repository URL, runs Repomix and Graphify when
  available, and writes regeneration instructions.

## Usage

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/<project>/diagrams"
bash ".oc_local/ai-scripts/analyse-project.sh" "https://github.com/owner/repo"
```

Set `ARG_FAIL_FAST=1` to stop on the first Mermaid rendering failure.

If `mmdc` is installed but Puppeteer cannot find a browser, install
Chrome/Chromium or set `PUPPETEER_EXECUTABLE_PATH` before running the script:

```bash
PUPPETEER_EXECUTABLE_PATH=/path/to/chromium \
  bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/<project>/diagrams"
```
