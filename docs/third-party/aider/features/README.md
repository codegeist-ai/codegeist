# Aider Feature Map

Feature-oriented map of the analyzed Aider source. This is static-analysis
documentation, not runtime verification.

## Coding Session Core

- `aider.main` builds the session from CLI/config inputs and starts `Coder.run()`.
- `Coder` owns prompt state, file scope, chat history, model calls, edit
  application, git/lint/test loops, and reflection messages.
- `ChatChunks` helps assemble message families for the model.
- `InputOutput` owns terminal input/output, confirmations, history, logging, and
  rendered assistant responses.

## Chat Modes And Coder Strategies

- `Coder.create()` selects a concrete coder by `edit_format` from
  `aider/coders/__init__.py`.
- Main modes include `ask`, `code`, `architect`, `context`, and `help`.
- Edit families include edit-block/search-replace, unified diff, whole-file,
  patch, editor-oriented variants, and function-call style variants.
- `ArchitectCoder` uses an architect model to draft changes and hands the result to
  an editor coder for actual file mutation.

## Repository Context

- `GitRepo` discovers the working tree, filters tracked and ignored files, creates
  commits, and provides diff/undo context.
- `RepoMap` extracts tree-sitter tags, ranks important identifiers/files, caches
  tags, and fits repo context into model token budgets.
- Commands such as `/add`, `/drop`, `/ls`, `/read-only`, `/tokens`, and `/copy`
  reshape the active context.

## Model And Provider Handling

- `Model` and `ModelSettings` combine default model choices, aliases, bundled
  settings, local metadata, LiteLLM metadata, OpenRouter fallback data, edit-format
  defaults, token limits, streaming support, prompt caching, reasoning tags, and
  editor/weak model relationships.
- LiteLLM is the main provider gateway.
- `ModelInfoManager` caches model metadata under the user's home directory.

## Editing, Git, And Feedback Loops

- Model output is parsed by the active coder strategy, then `prepare_to_edit()` and
  `allowed_to_edit()` enforce file-in-chat and confirmation behavior.
- `apply_updates()` mutates files, reflects malformed model output back to the
  model, and reports changed files.
- Auto-commit, dirty pre-commit, lint, test, shell-command suggestions, and
  follow-up reflection can run after edits.

## Side-Effect Features

- Shell commands can be run through `!` or `/run`; users choose whether to add
  output to the chat unless non-zero test/lint paths request it.
- URL scraping can add web content to chat.
- Clipboard paste, voice input, external editor integration, and file watch mode
  are implemented as optional interaction surfaces.
- Streamlit browser UI exists as a separate UI surface over the coder runtime.

## Documentation And Benchmark Surfaces

- `aider/website/docs/` contains user documentation for install, usage,
  configuration, git integration, repo maps, models, scripting, and troubleshooting.
- Benchmark and leaderboard data exist in the repository but were not included in
  the focused Graphify corpus beyond selected docs.
