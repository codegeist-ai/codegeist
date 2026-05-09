# T001_14 Define Context Loading Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist loads repository context for agent sessions.

This task specifies deterministic context selection only. It does not implement
retrieval, embeddings, indexing, file scanning, prompt construction, or provider
calls.

## Scope

- Define sources for rules, memory, task files, project docs, local overlays,
  source files, and third-party analysis artifacts.
- Define priority and ordering for context sources.
- Identify what should be loaded automatically versus on demand.

## OpenCode-To-Java Migration Questions

| Question | Codegeist direction |
| --- | --- |
| What is the Java equivalent of OpenCode context/project/instance loading? | A deterministic context loader that gathers repo rules, memory, tasks, docs, source snippets, and analysis artifacts for a session/turn. |
| What is Codegeist-specific? | `.opencode`, `.oc_local`, `docs/memory-bank`, `docs/tasks`, and third-party analysis artifacts are first-class context sources. |
| What is MVP? | Load rules, memory, active task docs, architecture docs, and focused source snippets on demand. |
| What is later? | Indexing, embeddings/RAG, LSP-derived context, user profiles, server-side project cache, and remote context providers. |

## Boundary Rules

- Context loader reads through workspace policy and must not bypass ignored or
  secret-like file rules.
- Context selection is explainable: the runtime can report which sources were
  used and why.
- Large artifacts such as Repomix output and Graphify output are on-demand, not
  blindly loaded into every prompt.
- Context loading does not call providers or execute tools.
- Plugins/JBang may contribute context sources later through runtime mediation.

## Implementation-Readiness Questions

- Can the runtime produce a context manifest for a turn?
- Can Plan and Build modes share deterministic context behavior?
- Can context loading avoid large `repomix-output.xml` in the parent context?
- Can future server/Vaadin clients inspect context without owning selection
  policy?
- Can stale or missing analysis artifacts be reported without blocking basic MVP?

## Non-Goals

- Do not implement embeddings, vector search, RAG, LSP indexing, or source graph
  generation.
- Do not run Graphify or Repomix from this task.
- Do not define final token budgeting policy.
- Do not let context loading mutate workspace state.

## Deliverable

Add a context loading architecture section to the parity document with source
priority, auto/on-demand rules, explainability, large artifact handling,
workspace boundaries, and later retrieval options.

## Acceptance Criteria

- Context loading is deterministic and explainable.
- Repo-owned rules and memory are first-class sources.
- Large third-party artifacts are treated as on-demand context.
- Context loading does not execute tools or provider calls.
- Loaded context can be summarized as events/session metadata later.

## Verification

- Review against `docs/memory-bank/chat.md` and `docs/third-party/opencode/`.

## Verification Result

- Specified deterministic context loading sources, boundaries, on-demand artifact
  handling, and implementation-readiness questions.
