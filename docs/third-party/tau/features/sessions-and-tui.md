# Tau Sessions And TUI

Tau combines append-only session storage with a Textual frontend over provider-neutral agent events.

## Source Files

- `source/src/tau_agent/session/`
- `source/src/tau_coding/session.py`
- `source/src/tau_coding/session_manager.py`
- `source/src/tau_coding/tui/app.py`
- `source/src/tau_coding/tui/adapter.py`
- `source/src/tau_coding/tui/state.py`
- `source/src/tau_coding/tui/widgets.py`
- `source/docs/04-sessions.md`
- `source/docs/custom-tui.md`
- `source/tests/test_session.py`
- `source/tests/test_coding_session.py`
- `source/tests/test_tui_adapter.py`
- `source/tests/test_tui_app.py`

## Session Behavior

Tau stores sessions as append-only JSONL entry trees. It reconstructs state by replaying entries, not by mutating previous snapshots.

`CodingSession` treats `MessageEndEvent` as the durable-message boundary. This means completed user, assistant, steering, follow-up, and tool-result messages can be persisted before the whole run has ended.

Session entries include messages, model changes, thinking-level changes, compactions, branch summaries, labels, leaves, session info, and custom entries.

## TUI Behavior

The Textual TUI consumes agent events through adapter/state objects and renders prompt input, transcript, sidebar, completions, command surfaces, model/provider controls, thinking controls, queued follow-ups, and tool output.

Important user-facing behaviors include multiline prompt entry, `Alt+Enter` follow-up queuing, cancellation, slash-command completions, session picker, scoped model cycling, theme persistence, and provider login flows.

## Codegeist Relevance

This is the strongest Tau evidence for Codegeist's terminal UI planning. The useful concept is event projection into frontend state over a durable session store. The non-transferable detail is Textual's widget runtime; Codegeist should map the behavior to JLine and Spring Shell constraints.
