# mini-SWE-agent User Surface

This file summarizes how a user interacts with mini-SWE-agent based on static
source and documentation evidence.

## Install And Start

- Try the package without permanent installation through `uvx mini-swe-agent` or
  `pipx run mini-swe-agent`.
- Install with `pip install mini-swe-agent` for CLI and Python bindings.
- Run `mini` or `mini-swe-agent` to start the local interactive CLI.

## Configure Models

- Run `mini-extra config setup` for first-time model and API-key setup.
- Set defaults with `mini-extra config set MSWEA_MODEL_NAME <model-name>` or an
  environment variable.
- Pass a model at the CLI with `mini -m <provider/model>`.
- Use YAML `model.model_name`, `model.model_class`, and `model.model_kwargs` for
  provider-specific settings.

## Run The Local Agent

- `mini -t "task"` runs a task without prompting for the task text.
- `mini -c <config>` selects a config; the default is `mini.yaml` or
  `MSWEA_MINI_CONFIG_PATH`.
- `mini -y` starts in yolo mode, executing model commands without confirmation.
- `mini -o <path>` controls the trajectory output path.

## Interaction Modes

- `confirm` mode asks before executing model commands.
- `yolo` mode executes model commands immediately.
- `human` mode lets the user type commands directly.
- `/c`, `/y`, and `/u` switch modes while the agent is waiting for input.
- `Ctrl+C` interrupts the agent and allows a user comment, mode switch, or new
  task.

## Run Benchmarks

- `mini-extra swebench` runs SWE-bench instances in batch mode and writes
  `preds.json` plus trajectories.
- `mini-extra swebench-single` runs one SWE-bench instance with interactivity for
  debugging.
- `mini-extra programbench` supports ProgramBench execution.

## Inspect Outputs

- `.traj.json` files contain full messages, config, model stats, exit status, and
  submission text.
- `mini-extra inspector` or `mini-e i` opens a Textual trajectory browser.
- `mini-e i <path>` opens a specific trajectory file or recursively scans a
  directory for trajectories.

## Safety Notes

- The default `local` environment executes shell commands directly on the host.
- Use Docker, Singularity/Apptainer, SWE-ReX, Bubblewrap, or ConTree backends when
  isolation is required.
- A task is considered submitted when command output begins with
  `COMPLETE_TASK_AND_SUBMIT_FINAL_OUTPUT` and the command exits successfully.
