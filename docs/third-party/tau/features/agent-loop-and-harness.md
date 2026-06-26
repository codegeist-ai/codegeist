# Tau Agent Loop And Harness

Tau separates the pure provider/tool loop from the reusable stateful harness.

## Source Files

- `source/src/tau_agent/loop.py`
- `source/src/tau_agent/harness.py`
- `source/src/tau_agent/events.py`
- `source/src/tau_agent/messages.py`
- `source/src/tau_agent/tools.py`
- `source/tests/test_agent_loop.py`
- `source/tests/test_agent_harness.py`

## Current Behavior

`run_agent_loop` receives a provider, model, system prompt, caller-owned transcript, tools, cancellation token, and optional queued-message callbacks. It streams provider-neutral agent events while appending completed assistant and tool-result messages to the transcript.

The loop emits lifecycle, turn, message, thinking, retry, tool, queue, error, and end events. Provider-specific event shapes are translated before renderers or TUIs see them.

`AgentHarness` owns transcript state, event subscribers, cancellation, active-run guarding, steering messages, and follow-up messages. It delegates each prompt or continuation to `run_agent_loop`.

## Design Notes

- The pure loop is stateless except for mutations to the caller-owned transcript list.
- The harness stays independent of CLI, Rich, Textual, local paths, session files, and resource discovery.
- Steering messages are injected after the current turn or tool batch.
- Follow-up messages are injected when a run would otherwise stop.
- Cancellation produces recoverable error events and records interrupted tool-call results.

## Codegeist Relevance

This is useful evidence for a Java event-stream agent loop where a small stateful service owns transcript and queues, while frontends only consume events. Translate the behavior into Codegeist's Spring service boundaries instead of copying Tau's Python classes.
