# T007_05 Agent Control Loop Question Catalog

Repeatable `/ask-project` question set for the T007_05 source-evidence pass.
This catalog targets all currently analyzed third-party coding-agent projects and
keeps the research prompts local to the task that uses the answers.

## Scope

- Task: `task.md`.
- Research answer file: `ask-project-research.md`.
- Focused OpenCode translation: `opencode-agent-loop.md`.
- Projects: `aider`, `mini-swe-agent`, `opencode`, `pi`, and
  `spring-ai-agent-utils`.

Use `/ask-project <project> "..."` only after the matching workspace exists under
`docs/third-party/<project>/` with current analysis artifacts.

## Base Questions For Every Project

Run these questions for each project when refreshing the T007_05 evidence pass.

```text
/ask-project <project> "For Codegeist T007_05, how does this project implement the agent control loop from user prompt to model request, tool-call request, tool dispatch, tool result, and model continuation? Cite source files and tests."
```

```text
/ask-project <project> "For Codegeist T007_05, which runtime data structures represent model turns, assistant tool calls, tool results, and final assistant responses? Which state is persisted and which remains runtime-only? Cite source files."
```

```text
/ask-project <project> "For Codegeist T007_05, how are tools exposed to the model while keeping actual tool execution owned by the agent application instead of hidden provider or framework behavior? Cite source files."
```

```text
/ask-project <project> "For Codegeist T007_05, how does the loop feed tool results back into the next model request? Show the exact message/history shape or equivalent continuation payload and cite source files or tests."
```

```text
/ask-project <project> "For Codegeist T007_05, how does the loop decide whether to continue, stop, handle multiple tool calls, handle failed tools, and prevent infinite loops? Cite source files and tests."
```

```text
/ask-project <project> "For Codegeist T007_05, how are tool outputs bounded, summarized, recorded, or rendered before being returned to the model, saved, or shown to users? Cite source files."
```

```text
/ask-project <project> "For Codegeist T007_05, which focused tests prove a two-turn model/tool/model loop, tool-result continuation, and ordered tool activity before final assistant text? Cite test files."
```

```text
/ask-project <project> "For Codegeist T007_05, what should Codegeist copy conceptually, adapt for Java/Spring, reject, or defer? Keep the answer scoped to a synchronous first loop, not TUI, permissions, subagents, streaming, or server runtime."
```

## Project-Specific Follow-Ups

### OpenCode

```text
/ask-project opencode "For Codegeist T007_05, explain OpenCode's model/tool/model loop boundaries around session processing, provider calls, tool execution, permission checks, event emission, and persistence. What is essential for Codegeist's first synchronous loop and what should be deferred? Cite source files and tests."
```

### Spring AI Agent Utils

```text
/ask-project spring-ai-agent-utils "For Codegeist T007_05, identify any reusable Java/Spring tool-calling or agent-loop utilities. Should Codegeist reuse Spring AI ToolCallingManager-style behavior directly, wrap it, or implement a smaller loop? Cite source files and tests."
```

### Pi

```text
/ask-project pi "For Codegeist T007_05, analyze Pi's stateful agent runtime loop, tool execution boundary, message history updates, and extension points. Which ideas fit a minimal Codegeist-owned Java control loop? Cite source files and tests."
```

### Aider

```text
/ask-project aider "For Codegeist T007_05, analyze Aider's prompt-to-model-to-tool/edit/test/reflection loop. How are tool results or command observations fed into later model turns, and what should Codegeist avoid copying? Cite source files and tests."
```

### mini-SWE-agent

```text
/ask-project mini-swe-agent "For Codegeist T007_05, analyze mini-SWE-agent's minimal model-plus-environment loop, linear message history, action execution, observations, and stop conditions. What is the smallest equivalent design for Codegeist? Cite source files and tests."
```

## Refresh Order

1. Run the first base question for every project to rebuild the broad loop map.
2. Run base questions 2 through 5 for `opencode`, `pi`, and `mini-swe-agent`.
3. Run the Spring AI Agent Utils follow-up to check Java/Spring reuse options.
4. Run base questions 6 through 8 for all projects as implementation alignment.
5. Update `ask-project-research.md` and `opencode-agent-loop.md` with current
   source-backed answers, replacing stale findings instead of appending conflicts.

## Answer Contract

Each answer should record:

- Source files and tests used as evidence.
- The project's loop shape and ownership boundaries.
- Tool-call and tool-result data structures.
- Persistence versus runtime-only state.
- Continue, stop, failure, and loop-guard behavior.
- Output bounds and rendering behavior.
- Concrete Codegeist takeaways for T007_05.
- Caveats when evidence is static analysis only.
