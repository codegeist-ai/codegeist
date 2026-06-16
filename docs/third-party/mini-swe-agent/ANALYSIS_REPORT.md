# mini-SWE-agent Analysis Report

## Scope

This report analyzes `https://github.com/SWE-agent/mini-swe-agent` at revision
`2caffc565474b8856a323ff163ffb7ab98d1ef02` on branch `main`.

Source material used:

- Local source checkout under `docs/third-party/mini-swe-agent/source`.
- Repomix output generated from source, documentation, package metadata, selected
  tests, workflow files, and config files.
- Graphify output generated from a focused source/documentation corpus. The
  temporary corpus was outside the repository and was deleted after generation;
  `graphify-out/` remains the ignored local graph cache used by `/ask-project`.

Runtime evidence is missing. No commands were executed inside mini-SWE-agent
beyond source checkout inspection and reproducible analysis generation.

Related focused documentation:

- `developer/runtime-flow.md` - prompt lifecycle, model boundary, bash action
  execution, trajectory output, CLI assembly, and batch-run notes.

## Project Purpose

mini-SWE-agent is a minimal AI software engineering agent for local coding tasks,
SWE-bench-style benchmarks, and agent research. The README frames the project as
an intentionally small alternative to larger coding agents: bash is the only tool,
message history is linear, each action runs as an independent subprocess, and the
language model rather than the scaffold is the center of attention.

The package metadata exposes console scripts:

- `mini` and `mini-swe-agent` -> `minisweagent.run.mini:app`
- `mini-extra` and `mini-e` -> `minisweagent.run.utilities.mini_extra:main`

The package targets Python `>=3.10` and is published as `mini-swe-agent`.

## Main User Surfaces

- Local CLI: `mini` starts a REPL-style interactive local agent.
- Python bindings: users can instantiate `DefaultAgent`, a model, and an
  environment directly.
- Config CLI: `mini-extra config ...` manages global environment settings and
  default model configuration.
- SWE-bench batch runner: `mini-extra swebench` processes many dataset instances
  with worker threads and writes `preds.json` plus per-instance trajectories.
- SWE-bench single runner: `mini-extra swebench-single` debugs one instance with
  interactivity.
- ProgramBench runner: `mini-extra programbench` supports the documented
  ProgramBench benchmark path.
- Trajectory inspector: `mini-extra inspector` or `mini-e i` opens `.traj.json`
  files in a Textual UI.

## Runtime Architecture

The implemented structure is compact and intentionally duck-typed:

1. `minisweagent.__init__` defines `Model`, `Environment`, and `Agent` protocols,
   the package version, and global config path loading through `.env`.
2. `run/mini.py` uses Typer to parse CLI flags, merges YAML/key-value config specs,
   prompts for a task when needed, and selects a model, environment, and agent.
3. `models.__init__.get_model()` resolves model name from CLI, YAML, or
   `MSWEA_MODEL_NAME`, then chooses a model class. The default is `LitellmModel`.
4. `environments.__init__.get_environment()` maps short names such as `local`,
   `docker`, `singularity`, `swerex_docker`, `swerex_modal`, `bubblewrap`, and
   `contree` to implementation classes.
5. `agents.__init__.get_agent()` maps `default` or `interactive` to the selected
   agent implementation.
6. `DefaultAgent.run()` renders initial system/user messages, loops on
   `step()`, saves after every step, and exits when the final message has
   `role="exit"`.
7. `DefaultAgent.step()` is exactly `execute_actions(query())`.
8. `DefaultAgent.query()` checks cost, call, step, and wall-clock limits, calls
   `model.query(messages)`, adds the assistant message, and records cost.
9. `DefaultAgent.execute_actions()` sends each parsed action to
   `env.execute(action)` and asks the model adapter to format observation
   messages.
10. `LocalEnvironment.execute()` runs one shell command through `subprocess.Popen`,
    captures combined stdout/stderr, kills the process group on timeout, and
    raises `Submitted` when output begins with
    `COMPLETE_TASK_AND_SUBMIT_FINAL_OUTPUT` and the command returns zero.

## Important Architecture Findings

- The central abstraction is a three-part composition: `Agent` owns the loop,
  `Model` owns provider format/action parsing/observation formatting, and
  `Environment` owns command execution.
- The default agent does not keep a persistent shell. Directory and environment
  changes are intentionally non-persistent unless the model encodes them in each
  command.
- Control flow uses exceptions that carry messages, including `Submitted`,
  `LimitsExceeded`, `TimeExceeded`, `FormatError`, and user interruption cases.
- `InteractiveAgent` subclasses `DefaultAgent` and adds confirmation/yolo/human
  modes, slash commands, KeyboardInterrupt handling, and optional exit
  confirmation without replacing the base loop.
- YAML config is recursive-merged from file specs and key-value overrides. Top
  level keys are `agent`, `environment`, `model`, and `run`.
- Class selection accepts short names and full import paths, so extension is
  in-process Python class substitution rather than a plugin framework.
- The trajectory output is the durable state contract: `info`, `messages`, and
  `trajectory_format: mini-swe-agent-1.1`.
- The default local environment is not sandboxed. Isolation depends on selecting
  Docker, Singularity/Apptainer, SWE-ReX Docker, SWE-ReX Modal, Bubblewrap, or
  ConTree backends.

## Dependencies And Runtime Surfaces

Architecture-relevant dependencies from `pyproject.toml` include:

- `litellm` for default provider access and cost calculation.
- `openai`, OpenRouter, Portkey, and Requesty-related model classes for provider
  variants.
- `pydantic` for config objects.
- `jinja2` for prompt and observation templates.
- `typer`, `rich`, `prompt_toolkit`, and `textual` for CLI and inspector UIs.
- `datasets` for SWE-bench dataset loading.
- `swe-rex`, `modal`, `boto3`, and `contree-sdk` as optional environment/back-end
  dependencies.

## Test Evidence

The source contains pytest coverage for core agent flow, interactive behavior,
configuration parsing, local/Docker/Singularity/extra environments, model action
parsing, provider wrapper behavior, cache-control helpers, CLI integration,
SWE-bench runners, ProgramBench, output saving, and the trajectory inspector.

The GitHub `Pytest` workflow installs Python 3.11, Podman, Bubblewrap,
Apptainer/Singularity, `uv`, and the package with `.[full]`, then runs
`pytest -v --cov --cov-branch --cov-report=xml -n auto` and uploads coverage to
Codecov.

No upstream tests were executed in this pass. Treat tests as static evidence of
intended contracts, not as a passing result for this checkout.

## Graphify Summary

- Focused corpus: 203 files, about 75,058 words.
- Graph: 1,874 nodes, 2,955 edges, 131 communities.
- Extraction confidence: 74% extracted and 26% inferred.
- Token benchmark: about 35.0x fewer tokens per average query than rereading the
  focused corpus.
- High-value graph hubs include `LocalEnvironment`, `InteractiveAgent`,
  `FormatError`, `Submitted`, `LitellmModel`, `DefaultAgent`,
  `DeterministicModel`, and `TrajectoryInspector`.

## Repomix Summary

Repomix was generated from a broad source/document/config/test corpus with heavy
generated, dependency, binary, lockfile, and secret-like paths excluded.

- Current packed output: 209 files.
- Total tokens reported by Repomix: 259,849.
- Total chars reported by Repomix: 1,007,842.
- Security scan: Repomix reported no suspicious files.
- Excluded by command: `.git`, Python caches, virtualenvs, build and coverage
  output, lockfiles, binary/media/archive files, logs, environment files,
  secret-like filenames, and key material extensions.

## Gaps And Risks

- Runtime behavior was not verified with a real `mini` session.
- No provider API calls were made, so authentication, rate limits, streaming,
  tool-call variants, cost calculation, and provider-specific exceptions remain
  static findings.
- No command was executed through a mini-SWE-agent environment. Shell side effects,
  timeout behavior, process cleanup, sandbox backends, and submission handling need
  live verification before operational conclusions.
- The local environment executes shell commands directly on the host with no
  isolation. This is a deliberate default for local use but unsafe for untrusted
  tasks.
- The package imports global config and prints startup text from
  `minisweagent.__init__` unless `MSWEA_SILENT_STARTUP` is set; tools embedding it
  may need to account for that side effect.
- Graphify semantic extraction used documentation/config files plus AST extraction
  for code. It did not ask subagents to semantically re-read every Python test or
  source file because AST covered structural code relationships.

## Suggested Follow-Up

Ask:

```text
/ask-project mini-swe-agent "For Codegeist T007_03, analyze mini-SWE-agent's minimal model-plus-environment loop, linear message history, bash-only execution, and trajectory output. What does it imply for keeping Codegeist's first harness narrow?"
```
