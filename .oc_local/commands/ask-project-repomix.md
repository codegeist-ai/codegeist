---
description: Ask a third-party project question using its Repomix packed output
agent: build
---

Use this command for source-level questions that should be answered from a
third-party project's Repomix packed output without polluting the parent session
context.

User request:

```text
$ARGUMENTS
```

Expected syntax:

```text
/ask-project-repomix <project-name> "<question>"
/ask-project-repomix opencode "How does session processing call tools?"
```

## Workflow

1. Parse `<project-name>` and the quoted question from `$ARGUMENTS`.
2. Resolve the packed output path:

```text
docs/third-party/<project-name>/repomix-output.xml
```

3. If the packed output is missing, stop and tell the user to run:

```text
/analyse-project <source-or-url> --project <project-name>
```

4. Delegate the question to the `@repomix` subagent. Include:
   - `project=<project-name>`
   - `repomix_path=docs/third-party/<project-name>/repomix-output.xml`
   - `question=<question>`
5. The `@repomix` subagent must attach the packed output with Repomix tools,
   search/read only relevant sections, and answer with source-path citations.
6. Return the subagent's answer to the user. Keep any generated context inside
   the child agent session; do not paste raw XML into the parent context.

## Rules

- Do not answer source-level implementation questions from memory when the
  Repomix artifact is available.
- Do not read the full XML into the parent session.
- Do not modify files from this command.
- If the Repomix artifact is stale or missing relevant files, state that and
  recommend rerunning `/analyse-project`.
