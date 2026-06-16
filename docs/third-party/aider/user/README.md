# Aider User-Facing Workflows

Static user-surface notes from the analyzed Aider source and docs.

## Start A Session

- Install the `aider-chat` package and run `aider` in a project directory.
- Choose a model through CLI options, config files, environment variables, or
  interactive onboarding.
- Aider prefers running inside a git repository. It can offer to create a repo and
  update `.gitignore` for `.aider*` and `.env` patterns.

## Work With Files

- Files can be added to the chat explicitly or through model/user mentions.
- Read-only files can provide context without being edited.
- Aider warns when too many or too-large files are in chat.
- Repo maps can summarize other files that are not fully in chat.

## Chat Modes

- `/ask` answers questions without editing files.
- `/code` asks for code changes using the selected model's edit format.
- `/architect` separates design from editing by using an architect/editor handoff.
- `/context` focuses on discovering likely relevant files and surrounding context.
- `/help` runs an interactive help mode using Aider's documentation.

## Commands And Side Effects

- Slash commands switch models, manage files, show diffs, run tests/lints, commit,
  undo, paste clipboard content, scrape URLs, open editors, and show settings.
- Bang commands and `/run` execute shell commands in the repository root.
- Shell output is added to chat only after confirmation unless a helper path uses
  command output as failure feedback.
- URL, image, clipboard, voice, and browser UI workflows require optional
  dependencies or environment setup.

## Git Behavior

- Aider can auto-commit model edits.
- It can create pre-edit commits for dirty files so undo flows have a baseline.
- Commit attribution and co-authorship behavior is configurable.
- The user can use normal git tools alongside Aider, but Aider's own git operations
  are significant side effects.

## Evidence Limits

These notes are based on static source and docs. No live Aider session was run, no
provider call was made, and no shell/git edit workflow was executed during this
analysis.
