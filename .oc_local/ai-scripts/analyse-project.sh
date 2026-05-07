#!/usr/bin/env bash
# analyse-project.sh - create a third-party repository analysis workspace
#
# Why this exists:
# - /analyse-project needs one predictable path for local directories and GitHub
#   repositories before deeper OpenCode analysis starts
# - heavy Repomix and Graphify outputs are reproducible and should stay out of
#   git by default
# - follow-up commands such as /ask-project need stable docs, feature, and
#   diagram directories to read and extend
#
# Inputs:
# - $1: local directory or GitHub repository URL
# - ARG_PROJECT_NAME: optional output name under docs/third-party/
# - ARG_OUTPUT_ROOT: optional output root, defaults to docs/third-party
# - ARG_SKIP_REPOMIX: set to 1 to skip Repomix
# - ARG_SKIP_GRAPHIFY: set to 1 to skip Graphify
#
# Related files:
# - .oc_local/commands/analyse-project.md
# - .oc_local/commands/ask-project.md
# - .oc_local/skills/repository-analysis/SKILL.md

set -euo pipefail

input="${1-}"
project_name="${ARG_PROJECT_NAME-}"
output_root="${ARG_OUTPUT_ROOT-docs/third-party}"
skip_repomix="${ARG_SKIP_REPOMIX-0}"
skip_graphify="${ARG_SKIP_GRAPHIFY-0}"

fail() {
  printf 'STATUS=error\n'
  printf 'MESSAGE=%s\n' "$1"
  exit 1
}

slugify() {
  local value="$1"
  value="${value%.git}"
  value="$(basename "$value")"
  value="$(printf '%s' "$value" | tr '[:upper:]' '[:lower:]')"
  value="$(printf '%s' "$value" | tr -cs 'a-z0-9._-' '-')"
  value="${value#-}"
  value="${value%-}"
  printf '%s' "$value"
}

json_escape() {
  local value="$1"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  value="${value//$'\n'/\\n}"
  printf '%s' "$value"
}

write_file() {
  local path="$1"
  local content="$2"
  printf '%s\n' "$content" >"$path"
}

write_if_missing() {
  local path="$1"
  local content="$2"
  if [[ ! -e "$path" ]]; then
    write_file "$path" "$content"
  fi
}

is_remote() {
  [[ "$1" =~ ^https://github\.com/[^/]+/[^/]+/?$ || "$1" =~ ^git@github\.com:[^/]+/[^/]+\.git$ || "$1" =~ ^[^/[:space:]]+/[^/[:space:]]+$ ]]
}

if [[ -z "$input" ]]; then
  fail 'input GitHub repository URL or local directory is required'
fi

if [[ -z "$project_name" ]]; then
  project_name="$(slugify "$input")"
fi

if [[ -z "$project_name" ]]; then
  fail 'could not derive project name; set ARG_PROJECT_NAME'
fi

repo_source="$input"
source_kind="local"
clone_dir=""

if [[ -d "$input" ]]; then
  repo_source="$(realpath "$input")"
elif is_remote "$input"; then
  source_kind="remote"
  clone_input="$input"
  if [[ ! "$clone_input" =~ ^https:// && ! "$clone_input" =~ ^git@ ]]; then
    clone_input="https://github.com/$clone_input.git"
  fi
  clone_parent="/tmp/opencode/analyse-project"
  mkdir -p "$clone_parent"
  clone_dir="$clone_parent/${project_name}-$(date -u +%Y%m%dT%H%M%SZ)"
  git clone --depth 1 "$clone_input" "$clone_dir"
  repo_source="$clone_dir"
else
  fail "input is neither an existing directory nor a recognized GitHub repository: $input"
fi

output_dir="$output_root/$project_name"
mkdir -p "$output_dir" "$output_dir/features" "$output_dir/user" "$output_dir/developer" "$output_dir/diagrams/source" "$output_dir/diagrams/rendered"

timestamp="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
repomix_status="skipped"
graphify_status="skipped"
repomix_command=""
graphify_command=""
git_head="unknown"
git_branch="unknown"
git_remote=""

if [[ -d "$repo_source/.git" ]]; then
  git_head="$(git -C "$repo_source" rev-parse --short=12 HEAD 2>/dev/null || printf unknown)"
  git_branch="$(git -C "$repo_source" branch --show-current 2>/dev/null || printf unknown)"
  git_remote="$(git -C "$repo_source" config --get remote.origin.url 2>/dev/null || true)"
fi

if [[ "$skip_repomix" != "1" ]]; then
  repomix_command="npx -y repomix \"$repo_source\" --output \"$output_dir/repomix-output.xml\" --style xml --ignore \".git/**,node_modules/**,target/**,build/**,dist/**,out/**,coverage/**,tmp/**,.tmp/**,.env,.env.*,*.pem,*.key\""
  if npx -y repomix "$repo_source" --output "$output_dir/repomix-output.xml" --style xml --ignore ".git/**,node_modules/**,target/**,build/**,dist/**,out/**,coverage/**,tmp/**,.tmp/**,.env,.env.*,*.pem,*.key" >"$output_dir/repomix.log" 2>&1; then
    repomix_status="ok"
  else
    repomix_status="failed"
  fi
fi

if [[ "$skip_graphify" != "1" ]]; then
  graphify_command="graphify extract \"$repo_source\" --out \"$output_dir\""
  if command -v graphify >/dev/null 2>&1; then
    if graphify extract "$repo_source" --out "$output_dir" >"$output_dir/graphify.log" 2>&1; then
      graphify_status="ok"
    else
      graphify_status="failed"
    fi
  else
    graphify_status="missing"
    write_file "$output_dir/graphify.log" 'graphify command not found'
  fi
fi

mmdc_version="missing"
if command -v mmdc >/dev/null 2>&1; then
  mmdc_version="$(mmdc --version)"
fi

write_file "$output_dir/analysis-manifest.json" "{
  \"project_name\": \"$(json_escape "$project_name")\",
  \"created_at\": \"$timestamp\",
  \"source_kind\": \"$source_kind\",
  \"input\": \"$(json_escape "$input")\",
  \"source_path\": \"$(json_escape "$repo_source")\",
  \"git_remote\": \"$(json_escape "$git_remote")\",
  \"git_branch\": \"$(json_escape "$git_branch")\",
  \"git_head\": \"$(json_escape "$git_head")\",
  \"output_dir\": \"$(json_escape "$output_dir")\",
  \"repomix_status\": \"$repomix_status\",
  \"repomix_command\": \"$(json_escape "$repomix_command")\",
  \"graphify_status\": \"$graphify_status\",
  \"graphify_command\": \"$(json_escape "$graphify_command")\",
  \"mmdc_version\": \"$(json_escape "$mmdc_version")\",
  \"regenerate_command\": \"ARG_PROJECT_NAME=$(json_escape "$project_name") bash .oc_local/ai-scripts/analyse-project.sh $(json_escape "$input")\"
}"

write_file "$output_dir/README.md" "# $project_name

Third-party repository analysis workspace for \`$project_name\`.

## Source

- Input: \`$input\`
- Source kind: \`$source_kind\`
- Git remote: \`${git_remote:-unknown}\`
- Git branch: \`$git_branch\`
- Git head: \`$git_head\`

## Durable Documentation

- \`ANALYSIS_REPORT.md\` - initial analysis summary and next-step checklist.
- \`features/README.md\` - feature and cluster index to deepen manually.
- \`user/README.md\` - user documentation entrypoint.
- \`developer/README.md\` - developer documentation entrypoint.
- \`diagrams/source/\` - editable Mermaid diagram sources.
- \`REGENERATE.md\` - how to rebuild ignored tool artifacts.

## Regenerable Artifacts

These files are intentionally ignored by git and can be rebuilt:

- \`repomix-output.xml\`
- \`repomix.log\`
- \`graphify-out/\`
- \`graphify.log\`
- \`diagrams/rendered/\`
- \`analysis-manifest.json\`
- \`VERIFY_REPORT.md\`

Use \`/ask-project $project_name \"<question>\"\` for follow-up analysis and diagram requests.
"

write_file "$output_dir/ANALYSIS_REPORT.md" "# Repository Analysis: $project_name

## Summary

This is the initial analysis workspace for \`$project_name\`. It records tool
execution status and provides a stable place for deeper feature, user, developer,
diagram, and migration-readiness documentation.

## Tool Status

- Repomix: \`$repomix_status\`
- Graphify: \`$graphify_status\`
- Mermaid CLI: \`$mmdc_version\`

## Source

- Input: \`$input\`
- Source path: \`$repo_source\`
- Git remote: \`${git_remote:-unknown}\`
- Git branch: \`$git_branch\`
- Git head: \`$git_head\`

## Required Deep-Dive Work

- Review \`repomix-output.xml\` when source-level context is needed.
- Review \`graphify-out/GRAPH_REPORT.md\` when available.
- Identify important feature clusters and document them under \`features/\`.
- Create focused Mermaid workflow and sequence diagrams under \`diagrams/source/\`.
- Expand \`user/\` and \`developer/\` documentation from observed behavior.
- Mark runtime assumptions and missing test coverage explicitly.

## Open Questions

- Which features are user-critical?
- Which runtime commands can safely verify behavior?
- Which clusters are migration candidates?
- Which behavior lacks tests or documentation?
"

write_file "$output_dir/REGENERATE.md" "# Regenerate $project_name Analysis Artifacts

Run from the repository root:

\`\`\`bash
ARG_PROJECT_NAME='$project_name' bash '.oc_local/ai-scripts/analyse-project.sh' '$input'
\`\`\`

To skip expensive or credential-dependent phases:

\`\`\`bash
ARG_PROJECT_NAME='$project_name' ARG_SKIP_GRAPHIFY=1 bash '.oc_local/ai-scripts/analyse-project.sh' '$input'
ARG_PROJECT_NAME='$project_name' ARG_SKIP_REPOMIX=1 bash '.oc_local/ai-scripts/analyse-project.sh' '$input'
\`\`\`

Render Mermaid diagrams when a browser is available for \`mmdc\`:

\`\`\`bash
bash '.oc_local/ai-scripts/render-mermaid.sh' 'docs/third-party/$project_name/diagrams'
\`\`\`

Ignored artifacts are reproducible and should not be committed by default:

- \`repomix-output.*\`
- \`repomix.log\`
- \`graphify-out/\`
- \`graphify.log\`
- \`diagrams/rendered/\`
- \`analysis-manifest.json\`
- \`VERIFY_REPORT.md\`
"

write_if_missing "$output_dir/features/README.md" "# Feature Index

Use this file to group features and Graphify clusters for \`$project_name\`.

## Candidate Features

- To be identified from source, tests, routes, commands, docs, and Graphify communities.
"

write_if_missing "$output_dir/user/README.md" "# User Documentation

User-facing documentation for \`$project_name\`.

Document installation, startup, workflows, feature usage, configuration, and troubleshooting here.
"

write_if_missing "$output_dir/developer/README.md" "# Developer Documentation

Developer-facing documentation for \`$project_name\`.

Document architecture, runtime behavior, feature implementation, tests, operations, and migration readiness here.
"

verify_status="ok"
{
  printf '# Verify Report: %s\n\n' "$project_name"
  printf -- '- Manifest: `%s`\n' "$([[ -s "$output_dir/analysis-manifest.json" ]] && printf ok || printf missing)"
  printf -- '- Repomix: `%s`\n' "$repomix_status"
  printf -- '- Graphify: `%s`\n' "$graphify_status"
  printf -- '- README: `%s`\n' "$([[ -s "$output_dir/README.md" ]] && printf ok || printf missing)"
  printf -- '- Analysis report: `%s`\n' "$([[ -s "$output_dir/ANALYSIS_REPORT.md" ]] && printf ok || printf missing)"
  printf -- '- Mermaid CLI: `%s`\n' "$mmdc_version"
  printf '\n## Notes\n\n'
  if [[ "$repomix_status" == "failed" ]]; then
    verify_status="failed"
    printf -- '- Repomix failed. See `repomix.log`.\n'
  fi
  if [[ "$graphify_status" == "failed" || "$graphify_status" == "missing" ]]; then
    printf -- '- Graphify did not complete. See `graphify.log`. This can be normal when no LLM backend credentials are configured.\n'
  fi
} >"$output_dir/VERIFY_REPORT.md"

printf 'STATUS=%s\n' "$verify_status"
printf 'PROJECT=%s\n' "$project_name"
printf 'SOURCE=%s\n' "$repo_source"
printf 'OUTPUT_DIR=%s\n' "$output_dir"
printf 'REPOMIX=%s\n' "$repomix_status"
printf 'GRAPHIFY=%s\n' "$graphify_status"
printf 'MMDC=%s\n' "$mmdc_version"
