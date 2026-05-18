# T001_08 Define Provider Architecture

Parent: `T001_define-codegeist-opencode-feature-architecture`

## Goal

Define how Codegeist uses Spring AI and any Codegeist-specific provider policy.

This task defines provider boundaries and selection policy only. It does not add
dependencies, credentials, provider implementations, model calls, or tool
binding code.

## Architecture Decision

Spring AI is the default provider integration path for Codegeist. Codegeist
should wrap Spring AI behind runtime-owned provider contracts so sessions,
agent modes, permissions, tool policy, event emission, and provider selection do
not leak into CLI or provider-specific SDK code.

Codegeist owns provider policy. Spring AI owns portable chat/model integration,
provider-specific options, streaming APIs, and tool-calling integration points
where they fit. Codegeist must still classify model capabilities and mediate tool
execution through its own tool and permission boundaries.

## Scope

- Define provider, model, credentials, model capabilities, and selection.
- Decide where Spring AI is sufficient and where Codegeist adapters may be
  needed.
- Identify first provider candidates for later verification.

## OpenCode Reference Behaviors

Use OpenCode as a feature reference for:

- Provider-agnostic model access.
- User or project configuration selecting providers and models.
- Provider authentication and model listing as runtime/server-visible concerns.
- Model capability checks affecting tool availability and execution.
- Streaming model output into session events.

Do not copy OpenCode's broad AI SDK provider package set. Codegeist starts with
Spring AI `1.1.x` and adds Codegeist-specific adapters only where Spring AI does
not expose the runtime policy Codegeist needs.

## Core Concepts

| Concept | Definition | Owner |
| --- | --- | --- |
| Provider | A configured model backend such as OpenAI-compatible, Anthropic, Ollama, or another Spring AI-supported backend. | Codegeist provider policy plus Spring AI adapter |
| Model | A concrete model id and option set usable for chat generation. | Codegeist model selection policy, implemented through Spring AI options |
| Credentials | Secrets or auth flows needed by a provider. | Configuration/secret handling, never CLI command logic |
| Capability | Runtime-relevant model feature such as streaming, tool calling, vision, reasoning, JSON/structured output, context window, or local/offline use. | Codegeist capability registry |
| Selection | Resolution of provider/model for a session, turn, or command. | Runtime/provider policy |
| Provider call | Execution of a prompt against a selected model. | Spring AI adapter invoked by runtime |

## Spring AI Boundary

Spring AI should be used for:

- Chat model integration and provider-specific client setup.
- Portable chat options where the common `ChatOptions` contract is sufficient.
- Streaming responses when available, mapped into Codegeist `assistant.delta` and
  `assistant.message` events.
- Provider-specific options when a selected model requires them, isolated behind
  provider adapter code.
- Tool-calling integration only after Codegeist has classified tools and
  permission policy permits exposing them.

Codegeist should wrap or extend Spring AI behavior for:

- Provider/model selection across sessions, turns, project config, and defaults.
- Capability classification used by agent modes, tools, and permissions.
- Consistent provider events and typed errors.
- Redaction of prompts, outputs, options, and errors before events or storage.
- Fallback behavior when a provider lacks streaming or tool calling.
- Any provider auth or model-listing workflow exposed to CLI/server/Vaadin.

## First Model Shape

The first provider model should include:

- `providerId`, `providerType`, display name, and enabled/disabled status.
- `modelId`, display name, context window if known, and default options profile.
- Capability flags for `streaming`, `toolCalling`, `structuredOutput`, `vision`,
  `reasoning`, `local`, and `networkRequired`.
- Credential source reference, never raw credential values.
- Configuration source such as default config, project config, environment, or
  interactive selection.
- Verification status such as `unknown`, `configured`, `validated`, or `failed`.

## Selection Rules

- Runtime resolves provider/model before a provider call and records the selected
  provider/model reference on the turn or assistant response metadata.
- CLI commands may request a provider/model override, but selection rules live in
  provider policy.
- Agent mode may constrain capability use, for example Plan can use streaming
  text but cannot gain write-capable tools only because a model supports tool
  calling.
- Tool availability is the intersection of model capability, Codegeist tool
  registry classification, active mode, and permission policy.
- A missing provider, missing credentials, unsupported capability, or failed
  provider call must produce typed provider/runtime events instead of raw SDK
  exceptions leaking to clients.

## First Provider Candidates

Verify these in later implementation/configuration work:

- OpenAI-compatible provider path, because it is commonly supported and useful
  for hosted and local-compatible endpoints.
- Anthropic provider path, because OpenCode-style coding workflows often depend
  on Claude-family models and streaming.
- Ollama or another local provider path, because local/offline use changes
  network and credential assumptions.

The first verified provider should prove chat completion, streaming, basic
options, typed errors, and whether tool-calling can be mediated through
Codegeist policy without exposing raw Spring AI tool execution directly.

## Open Questions

- Which Spring AI `1.1.x` provider starter should be verified first in this
  repository?
- Does Spring AI tool calling expose enough control for Codegeist permissions, or
  should Codegeist run tools itself after model tool-call requests are parsed?
- How should model capability metadata be discovered when providers do not expose
  reliable machine-readable capability data?
- Should provider credentials be environment-only for MVP, or should project/user
  config support credential references early?
- Is model listing required for MVP, or can the first CLI workflow use explicit
  configured model ids?

## Non-Goals

- Do not add Spring AI dependencies or change `app/codegeist/cli/pom.xml` here.
- Do not implement provider calls, credentials, model listing, or tool calling.
- Do not define final config file syntax.
- Do not expose provider behavior in CLI/server/Vaadin clients yet.
- Do not decide the final MVP provider; this task only identifies candidates.

## Deliverable

Add `## Provider Architecture` to
`docs/developer/specification/codegeist-opencode-parity.md` with:

- Spring AI as the default integration path,
- Codegeist-owned provider policy boundaries,
- core provider/model/capability concepts,
- selection rules,
- first provider candidates,
- open questions and non-goals.

## Acceptance Criteria

- Spring AI is the default provider integration path.
- Provider configuration is kept separate from CLI behavior.
- Open questions about tool calling and streaming are listed.
- Model capabilities are represented separately from provider SDK details.
- Provider selection is owned by runtime/provider policy, not by clients.
- First provider candidates for later verification are identified.

## Verification

- Review Spring AI assumptions before implementation in a later task.

## Verification Result

- Checked Spring AI reference material for `ChatOptions`, `ChatClient` streaming,
  and tool integration concepts before documenting assumptions.
- Added a provider architecture section that keeps Spring AI integration behind
  Codegeist runtime/provider policy.
- Listed tool-calling and streaming as explicit open questions for later
  implementation verification.

## Solution Note

Status: completed.

The solution pass used the narrow documentation-first path because
`docs/developer/specification/codegeist-opencode-parity.md` already contains the required
`Provider Architecture` section. That section records Spring AI as the default
integration path, Codegeist-owned provider policy, provider/model/capability
concepts, selection rules, first provider candidates, open questions, and
non-goals.

No user decision is pending. The first verified provider remains a later
implementation choice, and Spring AI tool-calling control remains an explicit
verification question before tool-capable provider work starts.

Verification passed with `git --no-pager diff --check`. A final review confirmed
provider selection stays in runtime/provider policy and does not leak into CLI,
server, Vaadin, sessions, or raw provider SDK code.
