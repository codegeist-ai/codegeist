# T003_01 Analyze Spring AI Agent Utils Adoption

Parent: `T003_implement-codegeist-opencode-core-application`

Status: planned

## Goal

Analyze `https://github.com/spring-ai-community/spring-ai-agent-utils` and decide
which parts can be used directly, wrapped behind Codegeist contracts, copied
conceptually, deferred, or rejected for the Codegeist OpenCode-core
implementation.

## Context

Codegeist should not reimplement useful Spring AI agent utilities unnecessarily.
At the same time, external tools must not bypass Codegeist-owned runtime, session,
event, permission, workspace, storage, and native-readiness boundaries.

The user explicitly wants this analysis to happen before the other T003 guidance
and implementation tasks. The prior backlog idea to analyze this repository is now
promoted into this tracked T003 child task.

## Initial Repository Findings

A read-only Repomix packaging of the remote repository found these relevant
modules and utilities:

- `spring-ai-agent-utils` core library with tools, advisors, skills, memory, and
  Claude-style subagents.
- `spring-ai-agent-utils-common` shared subagent SPI.
- `spring-ai-agent-utils-a2a` A2A protocol subagent implementation.
- `spring-ai-agent-utils-bom` Maven BOM.
- Tools and utilities including `FileSystemTools`, `GlobTool`, `GrepTool`,
  `ShellTools`, `TodoWriteTool`, `AskUserQuestionTool`, `SkillsTool`, `TaskTool`,
  `AutoMemoryTools`, `AutoMemoryToolsAdvisor`, `SmartWebFetchTool`, and
  `BraveWebSearchTool`.
- Test coverage with JUnit, Mockito, AssertJ, temporary directories, compatibility
  tests, and tool-specific behavior tests.

The public README advertises Maven coordinates such as
`org.springaicommunity:spring-ai-agent-utils:0.7.0`, while the packed source also
contains `0.8.0-SNAPSHOT` development modules. Compatibility with Codegeist's
Spring Boot `3.5.14`, Spring AI `1.1.6`, Java `25`, and GraalVM posture must be
verified instead of assumed.

## Scope

- Analyze Maven artifacts, versions, modules, and dependency risk.
- Check Spring Boot and Spring AI compatibility with Codegeist's current baseline.
- Check Java baseline and GraalVM/native-image implications.
- Review tool APIs and Spring AI `ToolCallback` integration shape.
- Review file, grep, glob, shell, todo, question, web, skills, memory, task, and
  subagent utilities.
- Review test strategy and reusable test patterns.
- Decide whether Codegeist should depend on the library directly, wrap selected
  utilities, copy concepts, defer adoption, or reject utilities.

## Specification Boundaries

- Treat this as an evidence and decision-report task, not an implementation task.
- Use the external repository as a candidate utility source and behavior reference;
  do not let its runtime, storage, permission, workspace, shell, skill, or subagent
  model become Codegeist's architecture by default.
- Any `use-directly` decision must explain why the utility can operate inside
  Codegeist-owned provider, tool, permission, workspace, event, session, storage,
  and native-readiness boundaries without bypassing them.
- Any utility with side effects, process execution, workspace access, network use,
  storage, memory, skills, or subagent orchestration should default to
  `wrap-behind-codegeist-contract`, `copy-concept-only`, `defer`, or `reject`
  unless the report can prove it is safe to use directly.
- Compatibility findings should distinguish released Maven artifacts from
  snapshot/source-only modules. Snapshot behavior may inform concepts, but it must
  not be treated as a stable dependency choice.
- The report may recommend follow-up implementation tasks, but this task should not
  create those task files unless a later planning or finalization phase explicitly
  chooses that handoff work.

## Deferred Surface Compatibility

This analysis must account for future JBang, Vaadin, headless server, and
API/SDK work, but it must not implement those surfaces. Adoption decisions should
record whether a utility helps or constrains those future surfaces.

## Direct Inputs And Dependencies

- `app/codegeist/cli/pom.xml` is the current build baseline: Spring Boot `3.5.14`,
  Spring AI BOM `1.1.6`, Spring Shell `3.4.2`, Java `25`, and GraalVM build tools
  `0.10.6`.
- `docs/developer/specification/provider-configuration-contracts.md` defines the Spring AI
  adapter boundary. Spring AI `ToolCallback` use must remain mediated by
  Codegeist-owned provider and tool policy.
- `docs/developer/specification/tool-permission-workspace-contracts.md` defines the tool,
  permission, workspace, bounded-result, event, and session gates that any adopted
  tool utility must satisfy.
- `docs/developer/specification/shell-verification-contracts.md` defines shell execution as a
  high-risk verification tool, not a generic process-execution shortcut.
- `docs/developer/specification/storage-port-posture.md` keeps storage in-memory-first and
  replaceable behind ports; external memory utilities must not own Codegeist
  persistence behavior.
- `docs/developer/specification/native-packaging-posture.md` requires native status to be
  reported as `passed`, `skipped`, or `failed` with a concrete reason when native
  validation matters.

## Planning Guidance

- `docs/tasks/hints/java-spring-architecture-planning-guidance.md`
  - The plan should also document fixed test expectations for any recommended
    adoption path, including which later tests must prove wrapper boundaries,
    compatibility, permission/workspace mediation, native posture, or rejection
    rationale.

## Key Questions

- Can `spring-ai-agent-utils` `0.7.0` be used with Codegeist's Spring Boot
  `3.5.14` and Spring AI `1.1.6` baseline?
- Which utilities can be useful for Codegeist immediately?
- Which utilities are unsafe unless wrapped behind Codegeist permission and
  workspace gates?
- Does `ShellTools` satisfy Codegeist controlled-shell requirements, or only
  provide implementation ideas?
- Can `GrepTool` and `GlobTool` replace or inform Codegeist source-search tools?
- Can `FileSystemTools` be used without violating Codegeist workspace policy?
- Can `SkillsTool` map to Codegeist skills without conflicting with `.opencode`
  skills and repo-local guidance?
- Can `AutoMemoryTools` inform Codegeist memory, or does it conflict with the
  storage-port posture?
- Can `TaskTool` and subagent support inform later Codegeist subagent behavior?
- Are there tests, fixtures, or performance patterns worth adopting?
- What are the native-image and startup-time risks?

## Deliverables

Create:

- `docs/developer/spring-ai-agent-utils-adoption.md`

The report should classify each candidate with one of these adoption decisions:

- `use-directly`
- `wrap-behind-codegeist-contract`
- `copy-concept-only`
- `defer`
- `reject`

## Acceptance Criteria

- The report identifies all relevant modules and utilities.
- The report records Maven coordinates and version choices or open compatibility
  checks.
- Compatibility with Codegeist's Spring Boot, Spring AI, Java, and GraalVM baseline
  is assessed.
- Every useful utility has an adoption decision and reason.
- Tool, permission, workspace, shell, storage, skill, subagent, performance, and
  native risks are explicitly noted.
- The report defines follow-up implementation tasks for selected adoption paths.
- No production dependency is added by this analysis task unless a later task
  explicitly chooses it.
- No Codegeist runtime behavior changes in this task.
- The report separates stable released artifacts, snapshot/source-only findings,
  and conceptual lessons.
- Each direct-use recommendation includes a boundary-safety explanation; otherwise
  the utility is classified as wrapped, copied conceptually, deferred, or rejected.
- Follow-up recommendations are phrased as implementation-task candidates, not as
  changes performed by this task.

## Implementation Plan

### Selected Option

Create one evidence-backed adoption report for the whole Spring AI Agent Utils
candidate set. Do not split this into separate Maven, tool, memory, skills, or
subagent reports yet, because the first Codegeist implementation decisions need a
single cross-boundary recommendation before later T003 child tasks are created or
sharpened.

### Concrete Solution Direction

Write `docs/developer/spring-ai-agent-utils-adoption.md` as a decision report that
compares Spring AI Agent Utils against Codegeist's current Java/Spring baseline and
the finalized T002 contract blueprints. The report should separate released Maven
artifact evidence from snapshot/source-only findings, then classify every relevant
module and utility as `use-directly`, `wrap-behind-codegeist-contract`,
`copy-concept-only`, `defer`, or `reject`.

The report should default side-effecting utilities to mediated or conceptual use
unless the analysis proves they can run fully inside Codegeist-owned runtime,
provider, tool, permission, workspace, storage, event, session, and native-readiness
boundaries. Direct use should be rare and must include a boundary-safety argument.

### Planned Files And Targets

- Add `docs/developer/spring-ai-agent-utils-adoption.md`.
- Update this task file with solve/finalization notes when the report is written.
- Update `docs/memory-bank/chat.md` only if the solve phase changes the active T003
  focus, selected adoption posture, or follow-up task set.
- Do not edit `app/codegeist/cli/pom.xml` during this task.
- Do not create Java source, Java tests, package directories, Spring beans,
  provider callbacks, storage adapters, shell executors, skill adapters, subagent
  implementations, or build configuration during this task.

### Report Structure

- Purpose and executive recommendation.
- Evidence sources and versions reviewed, including released artifacts versus
  source snapshots.
- Codegeist baseline summary from `app/codegeist/cli/pom.xml`: Spring Boot
  `3.5.14`, Spring AI BOM `1.1.6`, Spring Shell `3.4.2`, Java `25`, and GraalVM
  build tools `0.10.6`.
- Maven coordinates, modules, dependency graph risk, Java baseline, Spring Boot and
  Spring AI compatibility, and native-image posture.
- Candidate classification table covering at least `FileSystemTools`, `GlobTool`,
  `GrepTool`, `ShellTools`, `TodoWriteTool`, `AskUserQuestionTool`, `SkillsTool`,
  `TaskTool`, `AutoMemoryTools`, `AutoMemoryToolsAdvisor`, `SmartWebFetchTool`,
  `BraveWebSearchTool`, common subagent SPI, A2A subagent support, and the BOM.
- Boundary analysis sections for provider/tool callback mediation, permission and
  workspace safety, shell execution, storage/memory, skills, subagents, network
  tools, native readiness, performance/startup, and future JBang/Vaadin/server/API
  compatibility.
- Reusable test patterns from the external repository and fixed expectations for
  later Codegeist tests.
- Follow-up implementation-task candidates mapped to existing or planned T003 child
  slots.

### Evidence Collection Steps

1. Confirm released Maven coordinates and versions through Maven metadata or
   upstream release documentation. Record whether `0.7.0`, `0.8.0-SNAPSHOT`, or
   another version is the stable adoption candidate.
2. Inspect the upstream repository modules and POM files for Spring Boot, Spring AI,
   Java, optional dependency, and test-library versions. Keep snapshot/source-only
   facts separate from released artifact facts.
3. Inspect upstream tool implementations and tests for API shape, Spring AI
   `ToolCallback` integration, side effects, path handling, process execution,
   network use, memory/storage behavior, skills, task/subagent orchestration, output
   bounding, and error handling.
4. Compare each candidate against the Codegeist provider, tool/permission/workspace,
   shell verification, storage, and native packaging contract docs named by this
   task.
5. Record a decision and reason for each candidate. For `wrap-behind-codegeist-contract`,
   name the Codegeist boundary that must mediate the utility. For
   `copy-concept-only`, name the concept worth reusing without copying package or
   runtime ownership. For `defer` or `reject`, name the blocker.
6. Define later test expectations before any adoption implementation begins,
   including compatibility checks, wrapper-boundary tests, permission/workspace
   mediation tests, shell denial/approval tests, storage redaction tests, native
   status reporting, and rejection rationale coverage.

### Verification Strategy

- Required for this documentation-only task:

```bash
git --no-pager diff --check
```

- Do not run `task test`, `task build`, or `task native` unless the solve phase
  unexpectedly changes Java, Maven, Taskfile, build, or runtime files. If native
  validation is discussed in the report but not run, record native status as
  `skipped` with a documentation-only reason.

### Dependencies And Tradeoffs

- Depends on the existing T003 parent scope and the T002 provider, tool, shell,
  storage, and native posture documents.
- Depends on accurate upstream Spring AI Agent Utils evidence. Missing released
  artifact metadata should be recorded as an open compatibility check instead of
  guessed.
- The single-report approach gives Codegeist one coherent adoption posture before
  implementation starts, but it may defer detailed wrapper design to later T003
  implementation tasks.
- A narrower compatibility-only report would be faster, but it would not answer
  the user's main question about which utilities are safe to reuse or adapt.

### Open Questions

None for planning. The solve phase still needs to answer the compatibility,
artifact availability, utility classification, native risk, and follow-up task
questions through evidence.

## Plan Workflow Handoff

- Phase command: `/plan-task`.
- Source task: `docs/tasks/T003_implement-codegeist-opencode-core-application/tasks/T003_01_analyze_spring_ai_agent_utils_adoption.md`.
- Parent task considered: `docs/tasks/T003_implement-codegeist-opencode-core-application/task.md`.
- User context or instructions considered: only the task reference was provided;
  no extra narrowing instructions were supplied.
- Selected option: sharpen the existing `T003_01` analysis/report task as one
  comprehensive adoption report.
- Duplicate check result: no existing duplicate Spring AI Agent Utils adoption task
  was found under `docs/tasks/`; the existing `T003_01` task is the target.
- Discovered hints considered:
  `docs/tasks/hints/java-spring-architecture-planning-guidance.md`,
  `docs/tasks/hints/opencode-solving-guidance.md`, and
  `docs/tasks/hints/opencode-source-solving-guidance.md`.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Related context files read: `app/codegeist/cli/pom.xml`,
  `docs/developer/architecture/architecture.md`,
  `docs/developer/specification/codegeist-opencode-parity.md`,
  `docs/developer/specification/provider-configuration-contracts.md`,
  `docs/developer/specification/tool-permission-workspace-contracts.md`,
  `docs/developer/specification/shell-verification-contracts.md`,
  `docs/developer/specification/storage-port-posture.md`, and
  `docs/developer/specification/native-packaging-posture.md`.
- Upstream phase dependency: satisfied by the recorded `/specify-task` pass in
  this task.
- Result: the task is now implementation-ready as a documentation-only analysis
  and adoption-decision report.
- Open decisions or blockers: none blocking planning; evidence collection during
  solve owns the actual adoption decisions.
- Next recommended phase: `/solve-task` for this same task to write
  `docs/developer/spring-ai-agent-utils-adoption.md` without changing runtime code.

## Third-Party Analysis Workspace

The local `/analyse-project` workflow has been applied to
`https://github.com/spring-ai-community/spring-ai-agent-utils` with project name
`spring-ai-agent-utils`.

- Source checkout: `docs/third-party/spring-ai-agent-utils/source`.
- Durable analysis docs: `docs/third-party/spring-ai-agent-utils/README.md`,
  `docs/third-party/spring-ai-agent-utils/ANALYSIS_REPORT.md`, and
  `docs/third-party/spring-ai-agent-utils/REGENERATE.md`.
- Local ignored Graphify cache: `docs/third-party/spring-ai-agent-utils/graphify-out/`.
- Current Graphify cache shape: 1,030 nodes, 1,604 edges, and 47 communities from
  156 supported files. The run is structural/AST-focused; use it for navigation,
  then inspect source files directly before final adoption decisions.

The solve phase should use this workspace as reusable evidence while writing the
Codegeist-owned report at `docs/developer/spring-ai-agent-utils-adoption.md`.

## Verification

```bash
git --no-pager diff --check
```

## Non-Goals

- Do not add `spring-ai-agent-utils` as a Codegeist dependency in this analysis
  task.
- Do not implement or modify Codegeist runtime, provider, tools, permissions,
  storage, shell, skills, or subagents.
- Do not execute live provider calls or network-dependent tests.
- Do not treat external utilities as trusted tool execution paths unless a later
  implementation task wraps them behind Codegeist contracts.
- Do not create Java source, tests, Maven dependency changes, package directories,
  storage adapters, shell executors, skills, subagents, or provider callbacks.

## Planning-Readiness Questions

- Which released Maven artifact versions are available, and do they align with the
  current Spring Boot, Spring AI, Java, and GraalVM baseline?
- Which candidate utilities are pure enough to consider direct use, and which have
  side effects that require Codegeist mediation?
- Which utilities overlap with already documented T002 boundaries strongly enough
  that wrapping would be safer than direct dependency exposure?
- What source or documentation evidence is needed from the external repository to
  make each adoption decision defensible?
- What follow-up implementation-task candidates should be created after the report,
  if any, and which existing T003 child slots would they influence?

## Specification Check Result

- Phase command: `/specify-task`.
- Context or instructions considered: user requested a specification pass for this
  task and provided no extra narrowing beyond the task reference.
- Parent task considered: `T003_implement-codegeist-opencode-core-application/task.md`.
- Adjacent child tasks considered: none exist yet; the parent lists planned child
  slots, but only `T003_01` is currently created.
- Dependency inputs considered: the current Maven baseline in `app/codegeist/cli`,
  provider configuration, tool/permission/workspace, shell verification, storage
  posture, and native packaging developer docs.
- Discovered hints considered: `docs/tasks/hints/opencode-solving-guidance.md` and
  `docs/tasks/hints/opencode-source-solving-guidance.md` exist under the optional
  hints directory. They are solve-oriented, so this pass used them only for scope
  and evidence-boundary posture, not implementation planning. The later planning
  hint `docs/tasks/hints/java-spring-architecture-planning-guidance.md` was added
  after this specification pass so `/plan-task` can document Java/Spring
  architecture with UML diagrams, class diagrams, explanatory text, and
  illustrative code examples before implementation. It now also requires fixed
  test expectations and narrow verification commands for later implementation
  slices.
- Project overlays considered: `.oc_local/rules/codegeist-task-specification.md`
  and `.oc_local/rules/architecture-doc.md`.
- Upstream phase dependency: none; `/specify-task` is the entry phase.
- Result: task is specified as an analysis/report deliverable with explicit
  adoption-classification boundaries, dependency inputs, and non-implementation
  constraints.
- Open decisions or blockers: actual compatibility, artifact availability, utility
  classification, and follow-up implementation candidates remain open for the
  planning and solve phases.
- Next recommended phase: `/plan-task` to define the concrete research/report plan
  without changing runtime code.

## Creation Note

Created as the first T003 child task by user direction. The user wants to see how
much of Spring AI Agent Utils can be reused directly or adapted before Codegeist
starts implementing the core OpenCode-replacement application.
