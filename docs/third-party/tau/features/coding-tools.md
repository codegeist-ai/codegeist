# Tau Coding Tools

Tau provides local coding tools as provider-neutral `AgentTool` instances.

## Source Files

- `source/src/tau_coding/tools.py`
- `source/docs/03-tools.md`
- `source/tests/test_coding_tools.py`

## Tool Set

| Tool | Purpose |
| --- | --- |
| `read` | Read UTF-8 text files with optional line slicing, or return metadata for supported images. |
| `write` | Create or overwrite a complete UTF-8 text file. |
| `edit` | Apply exact text replacements to one UTF-8 file after validating all edits. |
| `bash` | Execute one shell command in the configured working directory. |

## Shared Contracts

- Relative paths resolve against the configured `cwd`.
- Tool definitions include prompt metadata and JSON schemas.
- Executors return structured `AgentToolResult` values.
- Large returned text is truncated to 2,000 lines or 50 KB.
- Mutating file tools share per-path async locks within the process.

## Notable Behavior

The `edit` tool validates every replacement before writing, requires exact and unique `oldText` matches, rejects overlapping edits, restores dominant line endings, and preserves UTF-8 byte-order marks.

The `bash` tool has no default timeout. Callers must pass a timeout when they need bounded execution. On POSIX systems, timeout handling kills the whole subprocess group.

## Codegeist Relevance

Tau's tools are useful behavioral evidence for Codegeist `codegeist_read`, `codegeist_write`, `codegeist_edit`, and shell tool contracts. Codegeist should keep its own workspace policy, permission handling, output preview, and `ToolSessionPart` persistence contracts.
