#!/usr/bin/env bash
# render-mermaid.sh - render repository-analysis Mermaid sources to SVG
#
# Why this exists:
# - repository-analysis and /ask-project create editable .mmd diagram sources
# - generated documentation should reference reproducible SVG artifacts
# - verification needs predictable, parseable rendering results
#
# Inputs:
# - $1: diagram directory containing source/ and optionally rendered/
# - PUPPETEER_EXECUTABLE_PATH: optional Chrome/Chromium path for mmdc
# - ARG_FAIL_FAST: set to 1 to stop on the first failed Mermaid render
#
# Related files:
# - .oc_local/skills/repository-analysis/SKILL.md
# - .oc_local/commands/ask-project.md

set -euo pipefail

diagram_dir="${1-}"
fail_fast="${ARG_FAIL_FAST-0}"

fail() {
  printf 'STATUS=error\n'
  printf 'MESSAGE=%s\n' "$1"
  exit 1
}

detect_browser() {
  local candidate

  if [[ -n "${PUPPETEER_EXECUTABLE_PATH-}" ]]; then
    printf '%s' "$PUPPETEER_EXECUTABLE_PATH"
    return 0
  fi

  for candidate in chromium chromium-browser google-chrome chrome; do
    if command -v "$candidate" >/dev/null 2>&1; then
      command -v "$candidate"
      return 0
    fi
  done

  return 1
}

if [[ -z "$diagram_dir" ]]; then
  fail 'diagram directory argument is required'
fi

source_dir="$diagram_dir/source"
rendered_dir="$diagram_dir/rendered"

if [[ ! -d "$source_dir" ]]; then
  fail "source directory not found: $source_dir"
fi

if ! command -v mmdc >/dev/null 2>&1; then
  fail 'mmdc is not available in PATH'
fi

mkdir -p "$rendered_dir"

if browser_path="$(detect_browser)"; then
  export PUPPETEER_EXECUTABLE_PATH="$browser_path"
  printf 'BROWSER=%s\n' "$browser_path"
else
  printf 'BROWSER=unresolved\n'
  printf 'WARNING=%s\n' 'mmdc may fail unless Chrome/Chromium is installed or PUPPETEER_EXECUTABLE_PATH is set'
fi

printf 'STATUS=ok\n'
printf 'MMDC_VERSION=%s\n' "$(mmdc --version)"
printf 'SOURCE_DIR=%s\n' "$source_dir"
printf 'RENDERED_DIR=%s\n' "$rendered_dir"

found=0
rendered=0
failed=0
log_file="$(mktemp)"

while IFS= read -r -d '' source_file; do
  found=$((found + 1))
  base_name="$(basename "$source_file" .mmd)"
  output_file="$rendered_dir/$base_name.svg"

  if mmdc -i "$source_file" -o "$output_file" >"$log_file" 2>&1; then
    rendered=$((rendered + 1))
    printf 'RENDERED=%s\n' "$output_file"
  else
    failed=$((failed + 1))
    printf 'FAILED=%s\n' "$source_file"
    while IFS= read -r line; do
      printf 'ERROR=%s\n' "$line"
    done <"$log_file"
    if [[ "$fail_fast" == "1" ]]; then
      rm -f "$log_file"
      exit 1
    fi
  fi
done < <(find "$source_dir" -type f -name '*.mmd' -print0 | sort -z)

rm -f "$log_file"

printf 'FOUND=%s\n' "$found"
printf 'RENDERED_COUNT=%s\n' "$rendered"
printf 'FAILED_COUNT=%s\n' "$failed"

if [[ "$found" -eq 0 ]]; then
  fail 'no .mmd files found'
fi

if [[ "$failed" -gt 0 ]]; then
  exit 1
fi
