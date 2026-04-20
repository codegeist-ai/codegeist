#!/usr/bin/env bash
# start.sh - open the repository root or a repo-managed worktree in a VS Code devcontainer
#
# Why this exists:
# - Keeps standard Git worktrees under `.worktrees/` with one simple entrypoint.
# - Injects the runtime Compose variables needed to mount the real repository
#   root and current checkout when a worktree is opened directly.
# - Computes the host numeric UID/GID explicitly so Docker Compose does not rely
#   on optional exported shell variables.
# - Reuses the repository root's `.devcontainer/.local.env` from managed
#   worktrees via a symlink.
# - Creates a local `compose.local.yml` from the tracked example when the
#   devcontainer workflow needs that optional overlay file.
# - Repairs the required `.opencode` and `.devcontainer` submodule checkouts for
#   the selected repository checkout so worktrees stay runnable.
# - Allows targeted submodule repair without changing global Git config.
# - Waits for the opened VS Code window and removes the matching Compose
#   project again when that window closes.
# - Opens the selected checkout directly through the repo's devcontainer
#   definition instead of a plain host-side folder window.
#
# Usage:
# - ./start.sh           Open the repository root in a new VS Code devcontainer window.
# - ./start.sh <branch>  Create or open `.worktrees/<branch>` in a new VS Code devcontainer window.
#
# Related files:
# - .devcontainer/.env
# - .devcontainer/.local.env.example
# - .devcontainer/docker-compose.yml
# - .gitignore

set -euo pipefail

if [ "$#" -gt 1 ]; then
  printf 'Usage: %s [branch]\n' "$0" >&2
  exit 1
fi

script_dir="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
repo_root="$(git -C "$script_dir" rev-parse --show-toplevel)"
branch="${1:-}"
target="$repo_root"
runtime_repo_root="$repo_root"
runtime_repo_worktree="$repo_root"
runtime_project_name="codegeist-ai-root"
runtime_hostname="codegeist"
runtime_uid="$(id -u)"
runtime_gid="$(id -g)"

slugify_branch() {
  local branch_name="${1:-detached}"

  branch_name="$(printf '%s' "$branch_name" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9-' '-')"
  branch_name="${branch_name#-}"
  branch_name="${branch_name%-}"

  if [ -z "$branch_name" ]; then
    branch_name="detached"
  fi

  printf '%s\n' "$branch_name"
}

ensure_worktree() {
  local branch_name="$1"
  local worktree_path="$repo_root/.worktrees/$branch_name"

  git check-ref-format --branch "$branch_name" >/dev/null
  mkdir -p "$(dirname "$worktree_path")"

  if [ -e "$worktree_path" ]; then
    [ "$(git -C "$worktree_path" rev-parse --show-toplevel)" = "$worktree_path" ]
  elif git show-ref --verify --quiet "refs/heads/$branch_name"; then
    git worktree add "$worktree_path" "$branch_name" >&2
  else
    git worktree add -b "$branch_name" "$worktree_path" >&2
  fi

  printf '%s\n' "$worktree_path"
}

init_submodule() {
  local checkout="$1"
  local submodule_name="$2"

  git -C "$checkout" -c protocol.file.allow=always \
    submodule update --init --recursive "$submodule_name" >&2
}

ensure_submodule() {
  local checkout="$1"
  local submodule_name="$2"
  local submodule_path="$checkout/$submodule_name"
  local submodule_status=""

  if [ ! -f "$checkout/.gitmodules" ]; then
    return 0
  fi

  if ! git -C "$checkout" config --file .gitmodules --get "submodule.${submodule_name}.path" >/dev/null 2>&1; then
    return 0
  fi

  if [ -e "$submodule_path/.git" ]; then
    return 0
  fi

  submodule_status="$(git -C "$checkout" submodule status -- "$submodule_name" 2>/dev/null || true)"

  if [ -n "$submodule_status" ] && [ "${submodule_status#-}" != "$submodule_status" ]; then
    printf 'Initializing %s submodule in %s\n' "$submodule_name" "$checkout" >&2
    init_submodule "$checkout" "$submodule_name"
    return 0
  fi

  if [ -e "$submodule_path" ]; then
    printf 'Cannot initialize %s submodule in %s\n' "$submodule_name" "$checkout" >&2
    printf 'The path %s already exists and is not an initialized Git submodule.\n' "$submodule_path" >&2
    printf 'Move or remove that directory, then run %s again.\n' "$0" >&2
    return 1
  fi

  printf 'Initializing %s submodule in %s\n' "$submodule_name" "$checkout" >&2
  init_submodule "$checkout" "$submodule_name"
}

devcontainer_folder_uri() {
  local checkout="$1"
  local workspace_hex=""

  workspace_hex="$(printf '%s' "$checkout" | od -An -tx1 -v | tr -d '[:space:]')"
  printf 'vscode-remote://dev-container+%s%s\n' "$workspace_hex" "$checkout"
}

open_checkout() {
  local checkout="$1"

  if [ "${REMOTE_CONTAINERS:-false}" = "true" ] && [ -n "${VSCODE_IPC_HOOK_CLI:-}" ]; then
    ensure_devcontainer_running "$checkout"
    code --new-window --folder-uri "$(devcontainer_folder_uri "$checkout")"
    return 0
  fi

  env \
    PWD="$runtime_repo_worktree" \
    CODEGEIST_REPO_ROOT="$runtime_repo_root" \
    CODEGEIST_REPO_WORKTREE="$runtime_repo_worktree" \
    COMPOSE_PROJECT_NAME="$runtime_project_name" \
    PROJECT_NAME="$runtime_project_name" \
    CODEGEIST_HOSTNAME="$runtime_hostname" \
    UID="$runtime_uid" \
    GID="$runtime_gid" \
    code --new-window --wait --folder-uri "$(devcontainer_folder_uri "$checkout")"
}

cleanup_devcontainer_project() {
  local checkout="$1"

  env \
    PWD="$runtime_repo_worktree" \
    CODEGEIST_REPO_ROOT="$runtime_repo_root" \
    CODEGEIST_REPO_WORKTREE="$runtime_repo_worktree" \
    COMPOSE_PROJECT_NAME="$runtime_project_name" \
    PROJECT_NAME="$runtime_project_name" \
    CODEGEIST_HOSTNAME="$runtime_hostname" \
    UID="$runtime_uid" \
    GID="$runtime_gid" \
    docker compose \
      --project-name "$runtime_project_name" \
      -f "$checkout/.devcontainer/docker-compose.yml" \
      down --remove-orphans >&2
}

ensure_devcontainer_running() {
  local checkout="$1"

  env \
    PWD="$runtime_repo_worktree" \
    CODEGEIST_REPO_ROOT="$runtime_repo_root" \
    CODEGEIST_REPO_WORKTREE="$runtime_repo_worktree" \
    COMPOSE_PROJECT_NAME="$runtime_project_name" \
    PROJECT_NAME="$runtime_project_name" \
    CODEGEIST_HOSTNAME="$runtime_hostname" \
    UID="$runtime_uid" \
    GID="$runtime_gid" \
    bash -lc '
      set -euo pipefail
      if command -v devcontainer >/dev/null 2>&1; then
        devcontainer up --workspace-folder "$1" --log-level info
      else
        npx --yes @devcontainers/cli up --workspace-folder "$1" --log-level info
      fi
    ' bash "$checkout" >&2
}

set_runtime_env() {
  local checkout="$1"
  local branch_name="$(git -C "$checkout" branch --show-current || true)"
  local branch_slug=""

  if [ -z "$branch_name" ]; then
    branch_name="detached"
  fi

  branch_slug="$(slugify_branch "$branch_name")"

  runtime_repo_root="$repo_root"
  runtime_repo_worktree="$checkout"
  runtime_project_name="codegeist-ai-$branch_slug"
  runtime_hostname="codegeist-$branch_slug"
}

ensure_worktree_local_env_link() {
  local checkout="$1"
  local link_path="$checkout/.devcontainer/.local.env"
  local link_target="../../../.devcontainer/.local.env"

  if [ "$checkout" = "$repo_root" ]; then
    return 0
  fi

  if [ -e "$link_path" ] || [ -L "$link_path" ]; then
    return 0
  fi

  ln -s "$link_target" "$link_path"
}

ensure_compose_local_file() {
  local checkout="$1"
  local compose_local_path="$checkout/.devcontainer/compose.local.yml"
  local compose_local_example_path="$checkout/.devcontainer/compose.local.yml.example"

  if [ -f "$compose_local_path" ]; then
    return 0
  fi

  if [ ! -f "$compose_local_example_path" ]; then
    printf '%s\n' 'services:' > "$compose_local_path"
    printf '%s\n' '  workspace: {}' >> "$compose_local_path"
    return 0
  fi

  cp "$compose_local_example_path" "$compose_local_path"
}

if [ -n "$branch" ]; then
  target="$(ensure_worktree "$branch")"
fi

ensure_submodule "$target" .opencode
ensure_submodule "$target" .devcontainer

ensure_worktree_local_env_link "$target"
ensure_compose_local_file "$target"

if [ ! -f "$target/.devcontainer/.local.env" ]; then
  printf 'Missing %s\n' "$target/.devcontainer/.local.env" >&2
  printf 'Create it manually from .devcontainer/.local.env.example in the repository root before rebuilding the devcontainer.\n' >&2
fi

set_runtime_env "$target"

if [ "${W_NO_OPEN:-0}" = "1" ]; then
  printf 'CODEGEIST_REPO_ROOT=%s\n' "$runtime_repo_root"
  printf 'CODEGEIST_REPO_WORKTREE=%s\n' "$runtime_repo_worktree"
  printf 'COMPOSE_PROJECT_NAME=%s\n' "$runtime_project_name"
  printf 'PROJECT_NAME=%s\n' "$runtime_project_name"
  printf 'CODEGEIST_HOSTNAME=%s\n' "$runtime_hostname"
  printf 'UID=%s\n' "$runtime_uid"
  printf 'GID=%s\n' "$runtime_gid"
  exit 0
fi

open_checkout "$target"

if [ "${REMOTE_CONTAINERS:-false}" != "true" ]; then
  cleanup_devcontainer_project "$target"
fi
