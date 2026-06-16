# mini-SWE-agent Runtime Flow

This note maps the core static runtime flow from the analyzed source. It is not a
runtime trace.

## CLI Assembly

`src/minisweagent/run/mini.py` owns the default CLI path. Typer parses flags such
as `--model`, `--agent-class`, `--environment-class`, `--task`, `--yolo`,
`--cost-limit`, `--config`, `--output`, and `--exit-immediately`. The command
loads config specs through `get_config_from_spec()`, appends CLI-derived override
values, recursively merges the dictionaries, prompts for a task when missing,
then builds model, environment, and agent objects.

## Agent Loop

`DefaultAgent.run()` resets the message list, renders system and instance
templates through Jinja2, and then loops until the newest message has
`role="exit"`. Each loop calls `step()`, catches control-flow exceptions that
carry trajectory messages, saves the current trajectory, and returns the final
message `extra` data.

`DefaultAgent.step()` is intentionally small:

```python
return self.execute_actions(self.query())
```

This makes the runtime easy to reason about: model response first, environment
actions second, observation formatting third.

## Model Boundary

`LitellmModel.query()` prepares messages by stripping `extra` fields, reordering
Anthropic thinking blocks, and applying optional cache-control markers. It calls
`litellm.completion()` with a single bash tool definition, calculates cost, parses
actions, stores raw response data in `message["extra"]`, and returns the assistant
message.

When parsing fails, model implementations are expected to raise `FormatError` and
persist the raw response on the format-error message so the trajectory retains the
failure evidence.

## Environment Boundary

`LocalEnvironment.execute()` extracts `action["command"]`, chooses a working
directory, merges environment variables, and calls a subprocess helper. The helper
starts a new process group on POSIX, captures stdout and stderr together, and
kills the process group on timeout.

After each command, `_check_finished()` looks for the sentinel
`COMPLETE_TASK_AND_SUBMIT_FINAL_OUTPUT` as the first non-leading-whitespace output
line with return code `0`. When present, it raises `Submitted` with an exit
message and the submission body.

## Interactive Layer

`InteractiveAgent` subclasses `DefaultAgent`. It prints messages, supports
`human`, `confirm`, and `yolo` modes, prompts before commands when required,
handles `/c`, `/y`, `/u`, `/m`, and `/h`, and lets `Ctrl+C` inject user
interruptions or mode changes. It preserves the base query/execute/save loop.

## Trajectory Output

`DefaultAgent.serialize()` writes:

- `info.model_stats.instance_cost` and `api_calls`
- `info.config.agent`, `model`, and `environment` from collaborator serializers
- `info.mini_version`, `exit_status`, and `submission`
- `messages`, including system, user, assistant, observation, and exit messages
- `trajectory_format: mini-swe-agent-1.1`

`DefaultAgent.save()` writes this JSON when `output_path` is configured. The
default `mini` CLI stores the last run under the global config directory.

## Batch Flow

`run/benchmarks/swebench.py` loads a Hugging Face dataset, filters or slices
instances, skips completed entries from `preds.json` unless `--redo-existing` is
set, creates one model per instance, builds an environment from the selected
backend, runs `ProgressTrackingAgent`, writes per-instance `.traj.json` files, and
updates `preds.json` under a lock.

## Static Evidence Gaps

This analysis did not execute a model call, run a command through any
mini-SWE-agent environment, load a real SWE-bench dataset, run the Textual
inspector, or execute upstream pytest.
