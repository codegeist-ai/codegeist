# T007 Third-Party Question Catalog

Question catalog for source-backed `/ask-project` research against OpenCode and
Spring AI Agent Utils before implementing the T007 chat-file tool harness.

Answered in: `third-party-question-answers.md`.

## Purpose

Use these questions to gather evidence for T007 implementation decisions around:

- `ask --chat <chat.json>` create/load/append/save behavior.
- A chat-only `chat.json` contract that excludes provider config, selected
  provider/model, MCP client definitions, enabled tool definitions, and status.
- Direct Codegeist `codegeist.yml` `mcp:` config mapped into Spring AI MCP support.
- MCP and read/write tools.
- Patch/edit and shell tools with bounded results.
- Terminal TUI rendering over the same chat file.
- Focused Java/Spring tests and architecture documentation.

If `/ask-project opencode ...` reports missing or stale artifacts, rerun the
OpenCode analysis first because the current workspace may not have
`docs/third-party/opencode/repomix-output.xml`.

## OpenCode Questions

```text
/ask-project opencode "For Codegeist T007, analyze how OpenCode represents resumable chat/session state. Focus on what belongs in persisted conversation state versus provider config, model selection, tool registry, MCP config, runtime status, or UI-only state. Cite source paths."

/ask-project opencode "For Codegeist T007, identify OpenCode's equivalent of ask --chat <chat.json>: how a conversation is created, loaded, appended to, saved, and resumed. Explain the call flow and key files."

/ask-project opencode "Analyze OpenCode's message model for user, assistant, tool call, and tool result records. Which fields are essential for continuing a chat, rendering a UI, and replaying context? Which fields should Codegeist avoid copying?"

/ask-project opencode "How does OpenCode separate provider configuration from persisted session data? Identify where providers, models, options, credentials, and runtime request settings live, and explain what Codegeist should translate into Java config."

/ask-project opencode "Analyze OpenCode's tool call lifecycle: tool discovery, model-visible tool descriptors, execution, result mapping, error handling, persistence, and UI rendering. Provide a sequence diagram if useful."

/ask-project opencode "Deep dive into OpenCode MCP client support. How are MCP clients configured, started, discovered, connected, exposed as tools, failed, retried, and shut down? Cite implementation files."

/ask-project opencode "For T007 read/write tools, analyze OpenCode's file read, write, list, glob, and grep behavior. Focus on path validation, working directory boundaries, result bounding, ignored files, binary files, mutation safety, and error cases."

/ask-project opencode "For T007 patch/edit tools, analyze OpenCode's file mutation path. How are edits represented, validated, applied, summarized, surfaced to the user, and persisted in session/tool activity?"

/ask-project opencode "For T007 shell tools, analyze OpenCode's command execution behavior. Focus on cwd selection, timeouts, stdout/stderr bounding, exit codes, environment handling, user approval, cancellation, and result persistence."

/ask-project opencode "Analyze OpenCode permission and side-effect gates for file edits and shell commands. Which protections are core user-visible behavior, and which are OpenCode-specific implementation details that Codegeist should defer?"

/ask-project opencode "Analyze OpenCode's terminal UI or TUI rendering of chats and tool activity. What state is read from persisted session data, what is UI-only, and how should Codegeist avoid introducing a second persistence model?"

/ask-project opencode "Identify OpenCode's bounded-output strategy for large tool results, shell output, file content, diffs, and error traces. Recommend Java-side T007 fields and truncation behavior based on source evidence."

/ask-project opencode "Analyze how OpenCode handles corrupt, missing, old-version, or incompatible persisted chat/session data. What migration or validation behavior is essential for Codegeist's first chat.json schema?"

/ask-project opencode "Explain OpenCode's working directory or workspace boundary model for tools. How does it prevent path escape, cwd escape, or mutation outside the intended project root?"

/ask-project opencode "Create a sequence diagram for a full OpenCode flow: user sends prompt, provider receives context, model requests a tool, tool runs, result is stored, assistant continues, UI updates."

/ask-project opencode "Create a feature deep dive for OpenCode session persistence specifically useful for designing Codegeist chat.json. Include field recommendations, non-recommendations, and source citations."

/ask-project opencode "What tests in OpenCode prove chat persistence, tool execution, MCP integration, patch/edit, shell, and UI rendering? Summarize test names, source paths, and behaviors Codegeist should mirror with Spring tests."

/ask-project opencode "List OpenCode behavior that Codegeist T007 should explicitly not copy: storage schemas, server APIs, plugin surfaces, subagents, memory, UI framework assumptions, provider catalog behavior, and runtime architecture."

/ask-project opencode "Produce a migration assessment: which OpenCode concepts map cleanly to Codegeist Java/Spring T007, which require translation, and which should remain deferred?"
```

## Spring AI Agent Utils Questions

```text
/ask-project spring-ai-agent-utils "For Codegeist T007, identify Java/Spring equivalents for a resumable chat-file harness. Which existing Agent Utils classes can support chat state, tool calls, tool results, and provider interaction?"

/ask-project spring-ai-agent-utils "Analyze how Agent Utils models chat messages, assistant responses, tool calls, and tool results. Which records/classes can Codegeist reuse or wrap, and which should remain Codegeist-owned?"

/ask-project spring-ai-agent-utils "Deep dive into Agent Utils tool registration and invocation. How are tools described, exposed to Spring AI, executed, and mapped back into model-visible results?"

/ask-project spring-ai-agent-utils "Analyze Agent Utils support for MCP or Spring AI tool callbacks. What is already available for bridging configured MCP tools into ChatClient or ChatModel calls?"

/ask-project spring-ai-agent-utils "For Codegeist's direct codegeist.yml mcp: map, explain how to map a Codegeist-owned MCP config into Spring AI MCP client support without exposing spring.ai.mcp.client.* as the public config contract."

/ask-project spring-ai-agent-utils "Analyze recommended Spring bean boundaries for a Java chat-file service, tool service, MCP adapter, and command/TUI clients. Identify existing Agent Utils patterns that fit Spring Boot."

/ask-project spring-ai-agent-utils "For T007 read/list/glob/grep/write tools, identify whether Agent Utils already provides file tools or workspace tools. If yes, explain behavior and boundaries. If no, recommend a minimal Codegeist wrapper design."

/ask-project spring-ai-agent-utils "For T007 patch/edit tools, identify any existing Agent Utils edit/apply-patch abstractions. Explain whether Codegeist should reuse them directly, wrap them, or implement a focused local tool."

/ask-project spring-ai-agent-utils "For T007 shell execution, identify any Agent Utils command execution abstractions. Focus on cwd, timeout, stdout/stderr bounding, exit code, cancellation, and safe result mapping."

/ask-project spring-ai-agent-utils "Analyze Agent Utils handling of bounded tool output and large content. What Java types or services should Codegeist use to truncate, summarize, or preserve tool results?"

/ask-project spring-ai-agent-utils "Analyze error handling patterns in Agent Utils for tools, provider calls, MCP failures, and invalid input. Recommend how Codegeist should persist failed tool activity in chat.json."

/ask-project spring-ai-agent-utils "Identify Agent Utils test patterns for Spring Boot integration tests involving chat models, tool callbacks, file tools, shell tools, MCP, agent loops, and TUI rendering. Recommend focused tests for T007_02 through T007_07."

/ask-project spring-ai-agent-utils "How should Codegeist integrate Agent Utils while keeping CodegeistChatRequest focused on model and prompt and keeping chat.json separate from provider configuration?"

/ask-project spring-ai-agent-utils "Create a proposed Java package and bean dependency diagram for Codegeist T007 using Agent Utils where useful, but preserving Codegeist-owned policy, config, chat file, and tool-result mapping."

/ask-project spring-ai-agent-utils "Compare direct use versus wrapper use for Agent Utils in Codegeist T007. Which classes are safe private implementation details, and where should Codegeist add thin wrappers for policy or persistence?"

/ask-project spring-ai-agent-utils "Analyze whether Agent Utils assumes server, database, memory, subagent, or long-running runtime concepts that conflict with Codegeist T007's file-only chat harness."

/ask-project spring-ai-agent-utils "Create a migration assessment from OpenCode-style tool/chat behavior to Spring AI Agent Utils and Codegeist Java/Spring implementation. Focus on minimal T007 scope."
```

## Comparison Questions

```text
/ask-project opencode "For each T007 feature, list the source-backed OpenCode behavior Codegeist should preserve: ask --chat, chat-only persisted state, MCP config, read/write tools, patch/edit, shell, TUI, bounded outputs, and tests."

/ask-project spring-ai-agent-utils "For each T007 feature, list the Java/Spring implementation building blocks Codegeist can use: chat file, ChatModel/ChatClient calls, ToolCallbackProvider, MCP client, read/write tools, patch/edit, shell, TUI integration, and tests."

/ask-project opencode "Produce an evidence table with columns: T007 feature, OpenCode source files, implemented behavior, persisted fields, runtime-only fields, risks, and Codegeist translation notes."

/ask-project spring-ai-agent-utils "Produce an evidence table with columns: T007 feature, Agent Utils source files, reusable Java types, Spring beans, missing pieces, risks, and Codegeist wrapper recommendations."
```

## Suggested Order

1. Run the OpenCode evidence table question.
2. Run the Spring AI Agent Utils evidence table question.
3. Research `T007_02`: chat file and `ask --chat` behavior.
4. Research `T007_03`: MCP config and read/write tools.
5. Research `T007_04`: patch/edit and shell tools.
6. Research `T007_05`: Codegeist-owned model/tool/model agent control loop.
7. Research `T007_06`: terminal TUI over `chat.json`.
8. Research tests and architecture documentation before `T007_07` verification.
