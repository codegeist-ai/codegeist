# Developer Documentation

This directory is for contributor and maintainer guides.

Document architecture, source layout, development workflows, and operational
constraints here.

## Architecture Documents

- `architecture/architecture.md` - current implemented system state.

## Specification Documents

- `specification/codegeist-opencode-parity.md` - target architecture and OpenCode parity map.
- `specification/runtime-vocabulary.md` - Codegeist-owned runtime vocabulary and boundary
  diagram for later implementation tasks.
- `specification/runtime-session-event-contracts.md` - architecture blueprint and
  diagrams for future Runtime, Session, Turn, MessagePart, and RuntimeEvent
  contracts.
- `specification/context-workspace-manifest.md` - architecture blueprint for future
  deterministic context loading, workspace path validation, and explainable
  context manifests.
- `specification/provider-configuration-contracts.md` - architecture blueprint for
  future provider configuration, validation, typed provider errors, and Spring AI
  adapter boundaries.
- `specification/tool-permission-workspace-contracts.md` - architecture blueprint
  for future tool descriptors, permission decisions, workspace validation, and
  bounded tool results.
- `specification/patch-edit-proposal-contracts.md` - architecture blueprint for
  future reviewable patch/edit proposals, apply requests, typed apply failures,
  and bounded result summaries.
- `specification/shell-verification-contracts.md` - architecture blueprint for
  future controlled shell verification requests, permission-gated execution, typed
  shell failures, and bounded output summaries.
- `specification/storage-port-posture.md` - architecture blueprint for future
  storage ports, in-memory-first session projections, redaction boundaries, and
  later persistence adapters.
- `specification/native-packaging-posture.md` - architecture blueprint for future
  JVM jar and GraalVM native-image verification posture, status reporting, and
  blocker classification.
- `specification/extension-client-readiness-gates.md` - architecture blueprint for
  readiness gates before deferred PF4J, JBang, server, Vaadin, SDK/OpenAPI, and TUI
  work.
