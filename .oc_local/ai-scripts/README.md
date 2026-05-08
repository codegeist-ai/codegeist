# Local AI Scripts

Project-specific helper scripts for repeatable OpenCode workflows.

## Scripts

- `render-mermaid.sh` - renders `.mmd` files from a diagram `source/`
  directory into sibling `rendered/*.svg` files using `mmdc`.

## Usage

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/<project>/diagrams"
```

Set `ARG_FAIL_FAST=1` to stop on the first Mermaid rendering failure.

If `mmdc` is installed but Puppeteer cannot find a browser, install
Chrome/Chromium or set `PUPPETEER_EXECUTABLE_PATH` before running the script:

```bash
PUPPETEER_EXECUTABLE_PATH=/path/to/chromium \
  bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/<project>/diagrams"
```
