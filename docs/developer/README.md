# Developer Documentation

This directory is for contributor and maintainer guides.

Document architecture, source layout, development workflows, and operational
constraints here.

## Architecture Documents

- `architecture/architecture.md` - current implemented system state.
- `architecture/provider-configuration.md` - current direct `codegeist.yml`
  provider, MCP, workspace, SpEL, validation, and native-reflection config map.
- `architecture/local-file-tools.md` - current local file-tool and MCP callback
  bridge architecture, including the first-provider-call to MCP client invocation
  sequence.

## Release Documents

- `release/github-release-build.md` - GitHub-hosted release workflow triggers,
  artifacts, branch validation, pre-tag validation, and published release behavior.
- `release/local-build-smoke.md` - local Linux and Windows build-smoke entrypoints
  and final local smoke-suite usage.
- `release/native-distribution-packaging.md` - native archive layout, sidecar
  libraries, and why Codegeist does not ship true single executable files.
- `release/windows-qemu-smoke.md` - detailed Windows QEMU smoke lifecycle,
  configuration, artifacts, and troubleshooting guide.

## Specification Documents

- `specification/codegeist-opencode-parity.md` - target architecture and OpenCode parity map.
- `specification/java-generation-guidance.md` - iterative Java/Spring implementation
  guidance for future source tasks.
- `specification/testing-strategy-and-agent-rules.md` - TDD, verification, and
  agent test workflow guidance.
- `specification/build-release-and-binary-smoke-strategy.md` - packaging,
  release, platform, and binary-smoke strategy.
- `specification/runtime-vocabulary.md` - Codegeist-owned runtime vocabulary and boundary
  diagram for later implementation tasks.
- `specification/native-packaging-posture.md` - architecture blueprint for future
  JVM jar and GraalVM native-image verification posture, status reporting, and
  blocker classification.
