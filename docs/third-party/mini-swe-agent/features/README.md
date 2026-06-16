# mini-SWE-agent Feature Map

This file summarizes user-visible and architecture-visible feature areas found in
the analyzed mini-SWE-agent source.

## Agent Runtime

- `DefaultAgent` implements the minimal loop: initialize messages, query a model,
  execute parsed actions, append observations, save trajectory state, and stop on
  `role="exit"`.
- `InteractiveAgent` adds human/confirm/yolo modes, command confirmation,
  KeyboardInterrupt handling, and exit confirmation.
- Exceptions such as `Submitted`, `FormatError`, `LimitsExceeded`, and
  `TimeExceeded` carry messages that are appended to the trajectory.

## Model Support

- `LitellmModel` is the default model class and sends a bash tool to LiteLLM.
- Text-based and Responses API variants support code-block parsing and native
  tool-call/result formats.
- OpenRouter, Portkey, Requesty, and deterministic test models provide provider
  and testing variants.
- Global cost and call limits are available through environment variables.

## Execution Environments

- `local` executes commands directly on the host through subprocesses.
- `docker` and `singularity` execute commands in containers.
- `swerex_docker` and `swerex_modal` use SWE-ReX backends.
- `bubblewrap` and `contree` provide additional sandbox options.

## Configuration

- YAML config uses top-level `agent`, `environment`, `model`, and `run` keys.
- Config specs can be file names, paths, or dotted key-value overrides.
- Multiple specs are recursively merged, with later specs taking precedence.
- Built-in configs cover local `mini`, text-based action parsing, SWE-bench,
  ProgramBench, XML action parsing, and Modal/SWE-ReX variants.

## Benchmarks And Batch Runs

- `mini-extra swebench` runs batches with worker threads, dataset selection,
  filtering, slicing, retry/redo behavior, and `preds.json` updates.
- `mini-extra swebench-single` runs a single SWE-bench instance for debugging.
- `mini-extra programbench` supports ProgramBench workflows.
- Batch runs record per-instance trajectories and progress state.

## Observability And Output

- `.traj.json` files contain `info`, `messages`, and
  `trajectory_format: mini-swe-agent-1.1`.
- `preds.json` aggregates SWE-bench predictions keyed by instance id.
- `mini-extra inspector` is a Textual trajectory browser with step navigation,
  trajectory navigation, reasoning toggle, reload, and optional `jless` handoff.

## Documentation And Quality

- MkDocs material documentation covers quickstart, CLI usage, configuration,
  environments, models, output files, and reference pages.
- CI runs pytest with coverage, docs build, link checks, pylint, release checks,
  dependency updates, and Codecov upload.
