# OpenCode Source Solving Hint

Use this hint when a Codegeist task needs evidence from the OpenCode source
checkout under `docs/third-party/opencode/source/`.

This hint is source-focused. It complements
`docs/tasks/hints/opencode-solving-guidance.md`, which covers the broader
OpenCode-to-Codegeist translation posture.

## Guidance

- Use OpenCode source as evidence for behavior, boundaries, and data flow, not as
  a Codegeist implementation blueprint.
- For OpenCode implementation analysis, use the local `/ask-project opencode ...`
  workflow from `.oc_local/commands/ask-project.md`. It reads the prepared
  analysis workspace under `docs/third-party/opencode/` and uses the current
  source checkout at `docs/third-party/opencode/source/` when source-level
  behavior must be verified.
- Prefer `/ask-project opencode "<question>"` for targeted source questions that
  can benefit from analysis artifacts, source paths, and Mermaid diagrams.
- Prefer `/ask-project-repomix opencode "<question>"` for broad source-level
  questions that require many files or packed-output context.
- Ask source questions before designing Codegeist tasks for provider, tool, MCP,
  permission, session, event, context, shell, patch/edit, extension, server, or
  storage behavior.
- Require source-path citations for important claims. Do not treat memory or
  generated summaries as enough evidence when the task depends on exact OpenCode
  behavior.
- Translate findings into Codegeist's Java-first runtime boundaries: Spring,
  Spring AI, Spring Shell, Java domain contracts, workspace policy, permissions,
  events, PF4J, and JBang.
- For CLI, prompt-flow, or client-adapter tasks, separate OpenCode adapter evidence
  from runtime evidence: CLI/TUI/HTTP route files show input collection and
  delegation, while session, agent, provider, tool, permission, and event files
  show runtime-owned behavior that should usually stay out of the client slice.
- Do not copy Bun, TypeScript, Hono, Effect, Solid, storage schemas, API routes,
  or package layout unless a later Codegeist decision explicitly chooses an
  equivalent concept.

## High-Value Source Questions

Use focused questions like these during `/solve-task` runs:

```text
/ask-project opencode "Which source files implement provider selection, model configuration, streaming, and tool-call mediation? Cite files and create a Mermaid sequence diagram if useful."
/ask-project opencode "How are tools registered and executed, including MCP tools? Cite source files and create a component diagram."
/ask-project opencode "How does permission approval flow from tool request to user decision to execution? Cite source files and diagram the sequence if useful."
/ask-project opencode "How are sessions, message parts, and runtime events connected during a prompt run? Cite source files and create a sequence diagram if useful."
/ask-project opencode "Where are MCP servers configured, connected, and exposed as tools? Cite source files and identify policy boundaries."
/ask-project opencode "How does CLI, TUI, or HTTP input collect prompt, agent, model, session, and command data before delegating to session prompt handling? Cite adapter and runtime files separately."
```

## Diagram Guidance

- For provider, tool, MCP, permission, session, event, and prompt-flow tasks,
  prefer small Mermaid diagrams when source relationships span several files.
- Use `/ask-project` diagram output rules so editable Mermaid files are written
  under `docs/third-party/opencode/diagrams/source/` and rendered SVGs under
  `docs/third-party/opencode/diagrams/rendered/` when rendering is available.
- Prefer `sequenceDiagram` for runtime flows and approvals, `flowchart TD` for
  component paths, and `classDiagram` for type/contract relationships.
- Keep diagrams evidence-backed and focused. Do not create giant whole-system
  diagrams when one provider, tool, or MCP flow is the task-relevant target.

## Non-Goals

- Do not run Graphify directly from a solve task. Use `/ask-project` or
  `/analyse-project` ownership when graph artifacts need refresh.
- Do not load the full `repomix-output.xml` into the parent solve context.
- Do not add source-derived implementation details to Codegeist unless they fit
  the current task's scope and Java-first architecture boundaries.

## Example Usage

This hint is usually inherited from a parent task's `Default Solve Hints` section.
When needed explicitly:

```text
/solve-task T002_06 docs/tasks/hints/opencode-source-solving-guidance.md
```
