# Developer Documentation

This directory is for contributor and maintainer guides.

Document architecture, source layout, development workflows, and operational
constraints here.

## Architecture Documents

- `architecture.md` - current implemented system state.
- `codegeist-opencode-parity.md` - target architecture and OpenCode parity map.
- `runtime-vocabulary.md` - Codegeist-owned runtime vocabulary and boundary
  diagram for later implementation tasks.
- `runtime-session-event-contracts.md` - architecture blueprint and diagrams for
  future Runtime, Session, Turn, MessagePart, and RuntimeEvent contracts.
- `context-workspace-manifest.md` - architecture blueprint for future deterministic
  context loading, workspace path validation, and explainable context manifests.
- `provider-configuration-contracts.md` - architecture blueprint for future
  provider configuration, validation, typed provider errors, and Spring AI adapter
  boundaries.
- `tool-permission-workspace-contracts.md` - architecture blueprint for future
  tool descriptors, permission decisions, workspace validation, and bounded tool
  results.
- `patch-edit-proposal-contracts.md` - architecture blueprint for future
  reviewable patch/edit proposals, apply requests, typed apply failures, and
  bounded result summaries.
- `shell-verification-contracts.md` - architecture blueprint for future controlled
  shell verification requests, permission-gated execution, typed shell failures,
  and bounded output summaries.
- `storage-port-posture.md` - architecture blueprint for future storage ports,
  in-memory-first session projections, redaction boundaries, and later persistence
  adapters.
