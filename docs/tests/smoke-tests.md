# Smoke Test Contract

Smoke-test expectations for Codegeist local and platform checks.

## Scope

Smoke tests prove packaged artifacts work in their target environment. They are
not a replacement for focused unit or Spring tests.

Current smoke entrypoints:

- `task native-smoke` - build Linux native executable, package it, and smoke the
  extracted native archive.
- `task local-linux-smoke` - run JVM tests, build the jar, smoke the jar, and run
  native checks when required or available.
- `task qemu-windows-smoke` - sync the repo into the Windows QEMU VM, run Windows
  JVM tests, build the jar, smoke the jar, build Windows native, package it, and
  smoke the extracted native archive.
- `task final-smoke-suite` - run Linux and Windows platform smokes and require
  both to pass by default.

## Output Contract

Smoke output must include scan-friendly status lines:

```text
Platform smoke status: passed|failed|skipped
Platform: linux|windows-x64
Jar status: passed|failed|skipped
Native status: passed|failed|skipped
Native reason: none|<reason>
```

Smoke output must also include duration lines for every meaningful subcheck. Use
the shape:

```text
Duration: <label>: <seconds>s
```

Labels should be stable and specific, for example:

- `linux maven tests`
- `linux jar package`
- `linux jar version smoke`
- `linux native compile`
- `linux native archive smoke`
- `linux native version smoke`
- `linux native show-config smoke`
- `linux platform smoke total`
- `windows maven tests`
- `windows jar package`
- `windows jar version smoke`
- `windows native compile`
- `windows native archive smoke`
- `windows native version smoke`
- `windows native show-config smoke`
- `windows platform smoke total`

## Timing Rules

- Measure command execution time, not only build-tool-reported time.
- Print duration even when a subcheck fails whenever the script can do so without
  hiding the failure.
- Keep duration output human-readable with seconds and three decimal places when
  practical.
- Keep startup checks separate from compile/package time. For example, native
  `--version` startup duration must not be buried inside native compile duration.
- Do not add interactive prompts to capture timing.

## Artifacts And Logs

- Linux native smoke packages `target/dist/codegeist-linux-x64.tar.gz` and tests
  the extracted `./codegeist` binary.
- Windows native smoke packages `target/dist/codegeist-windows-x64.zip` and tests
  the extracted `codegeist.exe` binary.
- Smoke logs stay under `app/codegeist/cli/target/smoke-test`.
- Generated smoke artifacts remain ignored build output unless a task explicitly
  asks for a handoff snapshot.
