# Regenerate Spring Shell Analysis Artifacts

This workspace separates durable documentation from reproducible heavy outputs.

## Source Checkout

The source checkout is a git submodule:

```bash
git submodule update --init --recursive -- docs/third-party/spring-shell/source
```

To update it to the remote `main` branch, update the submodule deliberately and
review the parent gitlink diff before committing.

## Full Analysis Cache

Regenerate Graphify, Repomix, manifest, verification, and durable handoff docs
through the local command workflow:

```text
/analyse-project https://github.com/spring-projects/spring-shell --project spring-shell
```

Do not use a separate local analysis script. Repomix belongs to the command
workflow; Graphify must run through the shared `graphify` skill.

## Repomix

The latest run used this effective command shape from the repository root:

```bash
repomix "docs/third-party/spring-shell/source" \
  --output "docs/third-party/spring-shell/repomix-output.xml" \
  --style xml \
  --include "**/*.{java,xml,md,adoc,properties,yml,yaml,json,sh,bat,cmd,ps1,txt},pom.xml,mvnw,mvnw.cmd" \
  --ignore "**/.git/**,**/target/**,**/build/**,**/out/**,**/.gradle/**,**/.mvn/wrapper/maven-wrapper.jar,**/*.class,**/*.jar,**/*.war,**/*.ear,**/*.zip,**/*.tar,**/*.gz,**/*.tgz,**/*.png,**/*.jpg,**/*.jpeg,**/*.gif,**/*.svg,**/*.ico,**/node_modules/**,**/dist/**,**/coverage/**,**/*.log,**/.env*,**/*secret*,**/*credential*,**/*token*,**/*key*,**/*.pem,**/*.key,**/*.p12,**/*.jks,**/*.lock"
```

The output is ignored at `docs/third-party/spring-shell/repomix-output.xml` and
is used by `/ask-project` for broad source-level follow-up questions.

## Graphify

Graph generation must be run through the shared `graphify` skill, not by
duplicating its workflow in this command.

The latest run used a temporary focused input directory under this workspace
named `graphify-input-focus`, copied from source-code and documentation files
only, then deleted after output generation. The run also mirrored upstream
`.adoc` files to temporary `.md` files under
`graphify-input-focus/__adoc_markdown_mirror__/` because the installed Graphify
detector did not classify `.adoc` directly.

The focused corpus included Java, Maven XML, Markdown, AsciiDoc mirrors,
properties/YAML/JSON config, and shell/PowerShell/batch script files. It excluded
`.git`, Maven `target/`, common build output, dependency folders, binary/image
files, archives, local environment files, and secret-like key/certificate files.

Expected key files:

```text
docs/third-party/spring-shell/graphify-out/GRAPH_REPORT.md
docs/third-party/spring-shell/graphify-out/graph.json
docs/third-party/spring-shell/graphify-out/graph.html
```

The latest graph had 5,217 nodes, 14,299 edges, 263 communities, and 25
hyperedges. HTML export required `GRAPHIFY_VIZ_NODE_LIMIT=6000` because the
default visualization safety limit is 5,000 nodes.

## Mermaid Rendering

Render diagrams only when SVG handoff artifacts are needed:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "docs/third-party/spring-shell/diagrams"
```

Rendered SVGs are ignored and should be regenerated from
`diagrams/source/*.mmd`.

## Documentation To Keep In Git

Commit only durable handoff docs, editable diagram sources, and the source
gitlink by default:

```text
docs/third-party/spring-shell/README.md
docs/third-party/spring-shell/ANALYSIS_REPORT.md
docs/third-party/spring-shell/REGENERATE.md
docs/third-party/spring-shell/features/*.md
docs/third-party/spring-shell/user/*.md
docs/third-party/spring-shell/developer/*.md
docs/third-party/spring-shell/diagrams/source/*.mmd
docs/third-party/spring-shell/source
```

Do not commit `graphify-out/`, `repomix-output.xml`, `VERIFY_REPORT.md`,
`analysis-manifest.json`, or rendered diagrams unless a future handoff explicitly
asks for reproducible generated artifacts.
