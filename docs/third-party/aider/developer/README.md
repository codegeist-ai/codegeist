# Aider Developer Notes

Developer-oriented source map for follow-up `/ask-project aider ...` questions.

## Focused Docs

- `prompt-flow.md` - prompt lifecycle from CLI startup through `Coder`, message
  formatting, LiteLLM calls, edit parsing, git commits, lint/test, shell feedback,
  and reflection.

## High-Value Source Files

- `aider/main.py` - CLI/config bootstrap, git setup, model selection, coder
  creation, one-shot commands, and main loop.
- `aider/coders/base_coder.py` - central session runtime, message formatting,
  provider calls, edit application, git/lint/test loops, reflection, and state
  cloning.
- `aider/commands.py` - slash/bang command dispatcher and command implementations.
- `aider/models.py` - model aliases, model settings, LiteLLM/OpenRouter metadata,
  token/cost behavior, and request option mapping.
- `aider/repo.py` - git repository wrapper, ignore handling, commits, diffing, and
  attribution policy.
- `aider/repomap.py` - tree-sitter tag extraction, repo-map ranking, token fitting,
  and disk cache behavior.
- `aider/sendchat.py` - message alternation validation helpers.
- `aider/coders/*.py` - concrete coder strategies and prompt contracts.
- `tests/basic/` - focused contract tests for core command, model, repo, repomap,
  sendchat, unified diff, watch, and coder behavior.

## Key Runtime Data Objects

- `Coder.cur_messages` holds the active turn state.
- `Coder.done_messages` holds completed prior conversation state and can be
  summarized.
- `Coder.abs_fnames` and `Coder.abs_read_only_fnames` are the active editable and
  read-only file scopes.
- `Coder.main_model`, weak model, and editor model drive provider calls,
  summarization, commit messages, and architect/editor workflows.
- `GitRepo` wraps GitPython and centralizes most repository mutation.
- `RepoMap` owns a local tag cache and token-budgeted context generation.

## Important Control Flows

- Mode switching uses exceptions: commands raise `SwitchCoder`, and `main.py`
  recreates a `Coder` with state copied from the previous coder.
- Edit formats are selected by `edit_format` class attributes rather than external
  plugin discovery.
- Malformed model edits become `reflected_message` text and can trigger another
  model turn up to `max_reflections`.
- Lint/test failures can be reflected back to the model after user confirmation.
- Shell command suggestions are gathered during editing and can be run with user
  confirmation.

## Test And Verification Notes

- The upstream repository has a broad pytest suite, but this analysis did not run
  it.
- Focused tests included in the analysis corpus cover behavior that is relevant to
  command dispatch, main startup, model settings, repo handling, repo maps,
  `sendchat`, unified diff parsing, watch mode, and coder edits.
- Runtime-sensitive follow-up should run targeted upstream tests before relying on
  behavior for implementation decisions.

## Sharp Edges For Translation

- `Coder` is a large stateful object; avoid assuming every responsibility should be
  translated into one class in another runtime.
- File mutation, git commits, shell execution, web scraping, clipboard access,
  Streamlit launch, and model calls all require explicit side-effect review.
- Import cycles around coder registration and prompt modules are visible in the
  Graphify report; inspect Python import timing before refactoring those areas.
- Aider's session model is in-memory plus chat-history files, not a structured
  event/session store like Codegeist's planned `.codegeist/session.json`.
