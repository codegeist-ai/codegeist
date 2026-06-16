# mini-SWE-agent Developer Notes

This file is a compact implementation map for future `/ask-project` questions.

## Package Map

- `src/minisweagent/__init__.py` - package version, global config path, startup
  dotenv load, and duck-typed `Model`, `Environment`, and `Agent` protocols.
- `src/minisweagent/agents/` - default and interactive agent loops.
- `src/minisweagent/environments/` - local, Docker, Singularity, and extra
  sandbox/cloud backends.
- `src/minisweagent/models/` - LiteLLM, text-based, Responses API, OpenRouter,
  Portkey, Requesty, and deterministic test models.
- `src/minisweagent/config/` - built-in YAML configs and config lookup/override
  helpers.
- `src/minisweagent/run/` - CLI entrypoints, batch benchmark runners, utilities,
  and inspector.
- `tests/` - pytest coverage for agents, environments, models, config, CLI, runs,
  serialization, and inspector behavior.

## Extension Pattern

mini-SWE-agent favors class substitution over a plugin framework. `get_agent`,
`get_environment`, and `get_model_class` accept either short names from mapping
tables or full import paths. Custom classes can therefore be supplied by YAML or
CLI config when they follow the duck-typed protocol methods.

## Runtime Flow

See `developer/runtime-flow.md` and `diagrams/source/runtime-flow.mmd`.

Key calls:

- `run/mini.py main()` -> `get_model()` -> `get_environment()` -> `get_agent()`
  -> `agent.run(task)`.
- `DefaultAgent.run()` -> `DefaultAgent.step()` -> `DefaultAgent.query()` ->
  `model.query(messages)`.
- `DefaultAgent.step()` -> `DefaultAgent.execute_actions()` ->
  `env.execute(action)` -> `model.format_observation_messages(...)`.
- `DefaultAgent.save()` writes the serialized trajectory when an output path is
  configured.

## Test And CI Evidence

- The repository uses pytest rather than unittest.
- `.github/workflows/pytest.yaml` installs `.[full]`, Podman, Bubblewrap,
  Apptainer/Singularity, and runs `pytest -v --cov --cov-branch --cov-report=xml
  -n auto`.
- Tests cover deterministic agent runs, CLI integration, local and sandboxed
  environments, model format handling, benchmark runners, and trajectory
  inspector behavior.

## Sharp Edges For Follow-Up Questions

- `minisweagent.__init__` creates the global config directory and loads `.env` at
  import time.
- `LocalEnvironment` is deliberately unsafe for untrusted tasks because it runs
  shell commands on the host.
- Cost calculation can raise unless `cost_tracking: ignore_errors` or
  `MSWEA_COST_TRACKING=ignore_errors` is used.
- The graph and this documentation are static. Provider behavior, sandbox
  behavior, and CI pass/fail state were not verified at runtime in this analysis.
