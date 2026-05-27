# Local Build Smoke

Local Linux and Windows smoke checks verify the current Codegeist jar and native
executable before GitHub release automation runs.

## Scope

This workflow covers the implemented Spring Boot CLI module under
`app/codegeist/cli`. It proves the existing `--version` command on generated jar
and native artifacts. It does not create release uploads, installers, signing,
notarization, or GitHub Actions release jobs.

All local test and smoke scripts live under `scripts/tests/`.

See `windows-qemu-smoke.md` for the detailed Windows QEMU VM lifecycle,
configuration, artifacts, status semantics, and troubleshooting guide.

See `native-distribution-packaging.md` for the release artifact decision: Linux
native builds should ship as `tar.gz` archives and Windows native builds should
ship as `zip` archives because both platforms currently produce runtime-required
sidecar libraries next to the GraalVM executable.

## Entrypoints

| Entrypoint | Purpose |
| --- | --- |
| `scripts/tests/local-linux-smoke.sh` | Runs Maven tests, builds the jar, verifies jar `--version`, then packages, unpacks, and smokes the Linux native archive when `native-image` is available. |
| `scripts/tests/qemu-windows-vm.sh` | Creates or starts the local Windows QEMU VM, syncs the repo subset, and runs the Windows smoke helper. |
| `scripts/tests/qemu-windows-smoke.sh` | Lower-level SSH wrapper for an already reachable Windows VM. |
| `scripts/tests/windows-smoke.ps1` | Runs inside the Windows VM to build and smoke the jar, then optionally package, unpack, and smoke the Windows native zip. |
| `scripts/tests/final-smoke-suite.sh` | Runs Linux and Windows smoke entrypoints as the final local suite. |
| `scripts/tests/native-smoke.sh` | Reusable Linux native archive smoke helper used by Taskfile and Linux smoke runs. |

The same checks are exposed through `app/codegeist/cli/Taskfile.yml`:

```bash
task local-linux-smoke
task qemu-windows-smoke
task final-smoke-suite
```

From the repository root, use the explicit Taskfile path:

```bash
task -t app/codegeist/cli/Taskfile.yml final-smoke-suite
```

## Linux Smoke

Run directly from the repository root:

```bash
scripts/tests/local-linux-smoke.sh
```

The Linux smoke command runs:

- `mvn --batch-mode --no-transfer-progress test`
- `mvn --batch-mode --no-transfer-progress -DskipTests clean package`
- `java -jar target/codegeist.jar --version`
- native compile, `target/dist/codegeist-<version>-linux-x64.tar.gz` packaging,
  archive extraction into a fresh temp directory, and `./codegeist --version`
  from the extracted package when `native-image` is on `PATH`

If `native-image` is missing, the native subcheck is reported as `skipped` unless
`CODEGEIST_SMOKE_REQUIRE_NATIVE=1` is set.

Linux version smokes are bounded by `CODEGEIST_JAR_SMOKE_TIMEOUT`, default `15s`,
and `CODEGEIST_NATIVE_SMOKE_TIMEOUT`, default `5s`.

## Windows QEMU Smoke

The automated Windows path downloads the official Windows Server 2025 Evaluation
ISO from Microsoft when no local ISO exists. Set `CODEGEIST_WINDOWS_ISO` to reuse
or choose a local ISO path. Set `CODEGEIST_WINDOWS_ISO_URL` to override the
official Microsoft URL when using another official evaluation ISO.

Optional host environment for first VM creation:

```bash
export CODEGEIST_WINDOWS_ISO='.local/windows-qemu/downloads/windows-server-2025-eval.iso'
export CODEGEIST_WINDOWS_ISO_URL='https://go.microsoft.com/fwlink/?linkid=2345730&clcid=0x409&culture=en-us&country=us'
export CODEGEIST_WINDOWS_ISO_SHA256='<optional expected sha256>'
```

Optional host environment:

```bash
export CODEGEIST_WINDOWS_VM_DIR='.local/windows-qemu'
export CODEGEIST_WINDOWS_SSH_PORT='2222'
export CODEGEIST_WINDOWS_MEMORY='8192'
export CODEGEIST_WINDOWS_CPUS='4'
export CODEGEIST_WINDOWS_CPU='host'
export CODEGEIST_WINDOWS_DISK_SIZE='80G'
export CODEGEIST_WINDOWS_IMAGE_INDEX='4'
```

The QEMU launcher defaults to `-cpu host` when KVM is available and `-cpu max`
without KVM. Override `CODEGEIST_WINDOWS_CPU` only when the host requires a
different CPU model.

Download the ISO without starting QEMU:

```bash
scripts/tests/qemu-windows-vm.sh download
```

Run the automated Windows smoke command:

```bash
scripts/tests/qemu-windows-vm.sh smoke
```

The VM state, generated password, SSH key, answer ISO, and disk image are written
under `.local/windows-qemu` by default and are ignored by Git.

The lower-level SSH wrapper can still target an already running VM by setting
connection details manually.

Required VM prerequisites:

- GraalVM Java 25 on `PATH`.
- Maven on `PATH`.
- Git or another repeatable repo-copy mechanism.
- OpenSSH server reachable from the host.
- MSVC Build Tools for native-image validation.

The automated VM provisioning installs Visual Studio Build Tools under
`C:\BuildTools`. The smoke helper checks that path, the standard Visual Studio
2022 Program Files paths, and the explicit `CODEGEIST_WINDOWS_MSVC_CMD` override.

Required host environment for the lower-level SSH wrapper:

```bash
export CODEGEIST_WINDOWS_SSH_TARGET='codegeist@127.0.0.1'
export CODEGEIST_WINDOWS_REPO_DIR='C:\codegeist'
```

Optional host environment:

```bash
export CODEGEIST_WINDOWS_SSH_PORT='2222'
export CODEGEIST_WINDOWS_SSH_KEY='/path/to/windows-vm-key'
export CODEGEIST_WINDOWS_NATIVE_MODE='auto'
export CODEGEIST_WINDOWS_MSVC_CMD='"C:\Program Files\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat" -arch=x64'
export CODEGEIST_WINDOWS_JAR_TIMEOUT_SECONDS='15'
export CODEGEIST_WINDOWS_NATIVE_TIMEOUT_SECONDS='5'
```

Run the Windows smoke command:

```bash
scripts/tests/qemu-windows-smoke.sh
```

`CODEGEIST_WINDOWS_NATIVE_MODE` accepts:

- `auto` - run native smoke only when `native-image` and MSVC Build Tools are
  available.
- `skip` - skip Windows native smoke and still verify the jar.
- `required` - fail when native prerequisites are missing.

## Final Suite

Run the normal final local suite:

```bash
scripts/tests/final-smoke-suite.sh
```

The final suite runs Linux and Windows entrypoints. Windows is required by
default. A download failure, failed VM startup, missing SSH readiness, or failed
Windows smoke check fails the suite.

Use developer-only skip mode when platform prerequisites are intentionally absent:

```bash
scripts/tests/final-smoke-suite.sh --allow-skips
```

`--allow-skips` is not release-grade validation. It only keeps ordinary developer
machines usable when the Windows ISO download or VM prerequisites are unavailable.

## Status Output

Each platform reports a concise result:

```text
Platform smoke status: passed
Platform: linux
Jar status: passed
Native status: passed
```

Skipped checks must include the concrete reason, such as an unset Windows SSH
target or missing native-image. Failed checks report the failing command or gate.

Smoke logs are written under `app/codegeist/cli/target/smoke-test/`, which is a
generated build directory and must not be committed.
