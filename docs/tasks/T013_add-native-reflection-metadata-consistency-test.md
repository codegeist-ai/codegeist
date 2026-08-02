# T013 Add Native Reflection Metadata Consistency Test

Status: open

Public Tracking: pending issue creation.

Roadmap: https://github.com/users/codegeist-ai/projects/1

Contribution Level: intermediate (`help wanted` candidate, not `good first issue`)

Effort: medium

Confirmed Unmet: on 2026-08-02,
`app/codegeist/cli/src/main/resources/META-INF/native-image/reflect-config.json`
manually lists config models, local-tool input records, and session-store types,
but no JVM test parses that file or detects duplicate, missing, or stale class
registrations.

## Goal

Add deterministic JVM coverage that keeps Codegeist-owned native reflection
metadata aligned with the classes reached through Jackson config mapping, local
tool input parsing, and session-store serialization.

## Acceptance Criteria

- A focused JVM test parses `reflect-config.json` through a real JSON parser.
- The test rejects duplicate `name` entries and reports the duplicate class names.
- Every configured Codegeist class name resolves through the test runtime
  classpath, so renamed or removed classes fail with the stale metadata entry.
- The test defines and checks the current required Codegeist-owned reflection set:
  concrete config root/payload and provider/MCP dispatch types, local-tool input
  records passed to Jackson, and session aggregate/part types serialized by the
  session store.
- Missing required entries fail with a message that names every missing class.
- Assertions cover required constructor, field, and public-method access flags
  where the current Jackson/native contract depends on them.
- The focused test and `task cli:check` pass with provider category `none`, no
  Docker access, and no provider or MCP calls.

## Files

- `app/codegeist/cli/src/main/resources/META-INF/native-image/reflect-config.json`
- A focused test under `app/codegeist/cli/src/test/java/ai/codegeist/app/`
- Native/reflection documentation only if the test establishes a maintenance
  contract not already documented

## Non-Goals

- Do not build a native executable; static JVM consistency is sufficient for this
  task.
- Do not replace GraalVM metadata generation or prove third-party dependency
  reachability.
- Do not add broad classpath scanning, architecture-test frameworks, or production
  reflection registries.
- Do not add or remove runtime features merely to satisfy the metadata test.

## Verification

```bash
task cli:test-jvm TEST=<reflection-metadata-test-class>
DOCKER_HOST=tcp://127.0.0.1:1 task cli:check
git --no-pager diff --check
```
