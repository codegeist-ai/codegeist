---
name: repository-analysis
description: analyze a repository statically and dynamically for architecture, features, documentation, diagrams, and migration readiness
trigger: /repository-analysis
---

# /repository-analysis

Use this skill when a repository must be understood deeply before migration,
partial rewrite, modernization, onboarding, or documentation work.

The goal is to understand the system before proposing technology changes:

- what the system does for users
- how important features behave at runtime
- how developers build, test, operate, and extend it
- how modules, services, data, and external systems interact
- which boundaries are stable enough for migration or extraction
- which risks need tests, documentation, or instrumentation first

Do not treat this as a source-code translation workflow.

## Usage

```text
/repository-analysis
/repository-analysis <path>
/repository-analysis <path> --project <project-name>
/repository-analysis <path> --mode deep
/repository-analysis <path> --skip-graphify
/repository-analysis <path> --skip-repomix
/repository-analysis <path> --output docs/third-party/<project-name>
```

If no path is given, analyze the current repository root. If no project name is
given, derive a lowercase kebab-case name from the repository directory.

## Output Contract

Write all outputs under one third-party project directory:

```text
docs/third-party/<project-name>/
├── analysis-manifest.json
├── ANALYSIS_REPORT.md
├── VERIFY_REPORT.md
├── repomix-output.xml
├── graphify-out/
│   ├── GRAPH_REPORT.md
│   ├── graph.json
│   └── graph.html
├── diagrams/
│   ├── source/
│   │   ├── system-context.mmd
│   │   ├── module-dependencies.mmd
│   │   ├── feature-<slug>-flow.mmd
│   │   ├── feature-<slug>-sequence.mmd
│   │   └── feature-<slug>-state.mmd
│   └── rendered/
│       ├── system-context.svg
│       ├── module-dependencies.svg
│       ├── feature-<slug>-flow.svg
│       └── feature-<slug>-sequence.svg
├── features/
│   ├── README.md
│   └── <feature-slug>.md
├── user/
│   ├── README.md
│   ├── getting-started.md
│   ├── workflows.md
│   ├── features.md
│   └── troubleshooting.md
└── developer/
    ├── README.md
    ├── architecture.md
    ├── runtime-behavior.md
    ├── feature-map.md
    ├── testing.md
    ├── operations.md
    └── migration-readiness.md
```

Do not overwrite existing manually refined documentation unless the user
explicitly asks for regeneration. Heavy tool outputs such as `repomix-output.*`,
`graphify-out/`, `analysis-manifest.json`, `VERIFY_REPORT.md`, and rendered SVGs
are reproducible and ignored by git by default.

## Manifest

`analysis-manifest.json` is the source of truth for reproducibility. It must
record:

- project name
- input path
- output directory
- timestamp
- selected mode and options
- exact Repomix command or MCP parameters
- exact Graphify command or skill invocation
- Mermaid CLI version from `mmdc --version` when available
- build, test, and runtime commands attempted
- generated files
- skipped phases
- warnings, failures, and assumptions

## Workflow

### 1. Resolve Inputs

1. Resolve the repository path.
2. Resolve project name.
3. Resolve the output directory.
4. Record options such as `standard`, `deep`, `skip-repomix`, and
   `skip-graphify`.
5. Stop if the input path does not exist.

Ask one short clarification question only when the project name, output
directory, or overwrite behavior is ambiguous.

### 2. Inventory Repository

Inspect the repository before running heavier tools.

Collect:

- git branch and status
- submodule status
- top-level directory layout
- languages and frameworks
- build files and test entrypoints
- runtime entrypoints
- configuration files
- dependency files
- CI/CD and deployment files
- database, schema, or migration files
- public APIs, UI routes, CLI commands, and background jobs
- existing user and developer documentation
- generated, vendored, or secret-like paths that must be excluded

### 3. Run Repomix

Use Repomix to create a consolidated source artifact for AI review.

Default output:

```text
<output-dir>/repomix-output.xml
```

Use conservative excludes for generated, dependency-heavy, and secret-like
paths:

```text
.git/**
node_modules/**
target/**
build/**
dist/**
out/**
coverage/**
tmp/**
.tmp/**
.env
.env.*
*.pem
*.key
```

After Repomix finishes, verify that the output exists, is non-empty, contains
file boundaries, and did not intentionally include obvious secret files.

### 4. Run Graphify

Use Graphify to build a persistent knowledge graph and cluster view.

Default output:

```text
<output-dir>/graphify-out/
```

Graphify should produce at least:

```text
graphify-out/GRAPH_REPORT.md
graphify-out/graph.json
graphify-out/graph.html
```

Use `--mode deep` when migration depends on latent coupling, architecture
rationale, or cross-module behavior. Read `GRAPH_REPORT.md` before writing
architecture or migration conclusions.

Record:

- graph node count
- graph edge count
- community count
- god nodes
- surprising connections
- suggested questions
- feature or cluster candidates

### 5. Runtime Behavior Analysis

Static analysis is not enough. Identify safe commands that show how the system
behaves, then run the narrowest useful checks.

Examples:

```bash
task test
task run
mvn test
npm test
npm run dev
docker compose up
```

Only use commands appropriate for the repository. Do not run destructive,
production, deployment, or credential-requiring commands.

Capture:

- startup sequence
- runtime logs relevant to behavior
- exposed ports or CLI commands
- API and UI routes
- background jobs and scheduled tasks
- event handlers
- database access paths
- external service calls
- configuration loading
- error handling paths

If the system cannot be run, document the exact blocker and distinguish it from
source-level conclusions.

### 6. Feature And Cluster Discovery

Group behavior by feature, not only by files. Feature candidates can come from:

- Graphify communities and god nodes
- routes, controllers, handlers, and UI pages
- CLI commands and shell entrypoints
- services, domain entities, tests, and README examples
- API endpoints, database tables, and event names

Write `features/README.md` as the feature index. Include each feature's source
signals and whether it needs a deep dive.

Each feature file should use this structure:

```markdown
# Feature: <Name>

## Purpose

## User-Facing Behavior

## Runtime Flow

## Main Code Paths

## Inputs And Outputs

## Data And State

## External Dependencies

## Important Edge Cases

## Tests Covering This Feature

## Missing Tests

## Migration Notes

## Diagrams
```

### 7. Important Feature Deep Dives

Analyze important features more intensively. A feature is important when it is:

- central to user value
- high-risk, stateful, or security-sensitive
- externally integrated
- heavily coupled or repeatedly referenced by Graphify
- poorly tested
- a likely migration or extraction candidate

For each important feature, produce:

- a text explanation
- source file references
- runtime evidence when available
- at least one focused Mermaid diagram
- a sequence diagram when interactions cross components
- a state diagram when lifecycle or workflow state matters
- explicit assumptions and missing evidence
- migration risk notes

### 8. Mermaid Diagram Sources And SVG Rendering

Write Mermaid sources under:

```text
<output-dir>/diagrams/source/
```

Render SVGs under:

```text
<output-dir>/diagrams/rendered/
```

Use the repo-local helper when available:

```bash
bash ".oc_local/ai-scripts/render-mermaid.sh" "<output-dir>/diagrams"
```

Otherwise render directly with Mermaid CLI:

```bash
mmdc -i diagrams/source/<name>.mmd -o diagrams/rendered/<name>.svg
```

If `mmdc` is installed but cannot find Chrome/Chromium, record the failure in
`VERIFY_REPORT.md` and ask whether to install a browser or set
`PUPPETEER_EXECUTABLE_PATH`. Keep the `.mmd` sources as valid analysis output
even when SVG rendering is blocked by the environment.

Documentation should reference SVGs when they exist:

```markdown
![Feature flow](../diagrams/rendered/feature-example-flow.svg)
```

Keep `.mmd` source files next to rendered artifacts so diagrams remain
editable and reproducible.

Use diagram types intentionally:

- `flowchart TD` for feature flows, decisions, and pipelines
- `sequenceDiagram` for request/response, CLI, service, or async interactions
- `stateDiagram-v2` for lifecycle, jobs, sessions, tasks, and state machines
- `classDiagram` for domain models when useful
- `erDiagram` for persistent data models when schemas exist

Do not create decorative diagrams. Prefer focused feature or cluster diagrams
over one unreadable system-wide diagram.

### 9. User Documentation

Create user-facing documentation from observed behavior.

User docs should answer:

- what the system is
- who it is for
- how to install or start it
- which workflows matter
- which features are available
- which inputs are expected
- which outputs or side effects happen
- which configuration is required
- which errors users can see

### 10. Developer Documentation

Create developer-facing documentation.

Developer docs should answer:

- how the system is structured
- where the entrypoints are
- how startup works
- how important features are implemented
- how modules depend on each other
- how data flows
- how tests are organized
- how configuration works
- how the system is built and operated
- which sharp edges matter before migration

### 11. Migration Readiness

Do not recommend migration before behavior is understood.

Classify:

- stable boundaries
- unstable boundaries
- domain logic
- framework glue
- infrastructure glue
- external contracts
- stateful components
- hidden runtime assumptions
- missing tests
- risky dependencies
- migration blockers

Then propose concrete strategies only when supported by evidence:

- no migration yet
- documentation first
- test harness first
- feature-by-feature extraction
- strangler migration
- API-compatible replacement
- domain core extraction
- runtime adapter approach
- full rewrite with explicit risks

### 12. Verification

Create `VERIFY_REPORT.md`. Check:

- manifest exists and is valid JSON
- Repomix output exists unless skipped
- Graphify output exists unless skipped
- Graphify report was read before conclusions
- user docs exist
- developer docs exist
- feature index exists
- important features have deep-dive docs
- Mermaid `.mmd` sources exist for required diagrams
- rendered SVGs exist when `mmdc` is available
- each important feature has at least one diagram
- runtime behavior is documented or explicitly marked as not runnable
- skipped and failed commands are recorded
- migration conclusions distinguish facts from assumptions

If verification fails, do not present the analysis as complete.

## Final Response

Return a concise summary with:

- project name and output directory
- generated artifacts
- skipped phases
- verification status
- top architecture or migration risks
- recommended next analysis question

Then be interactive. Offer targeted follow-up options such as:

- create a deeper feature sequence diagram
- trace a Graphify suggested question
- add user documentation for one workflow
- add developer documentation for one cluster
- run more runtime verification
- compare two migration strategies

Do not paste full generated reports into chat. Point to the files.

## Rules

- Do not rely only on static source analysis.
- Do not hide missing runtime evidence.
- Do not invent behavior that source, graph, docs, tests, or runtime evidence do
  not support.
- Mark assumptions explicitly.
- Do not include secrets or local environment files in analysis artifacts.
- Do not create diagrams without textual explanation.
- Do not create one giant diagram when feature-level diagrams are clearer.
- Do not treat Graphify communities as final feature boundaries without review.
- Do not overwrite existing analysis runs unless the user explicitly confirms.
- Keep durable generated documentation in English.
