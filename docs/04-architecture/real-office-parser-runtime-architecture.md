# Architecture: Real Office Parser Runtime

## Status

Draft for user review. Implementation is blocked until user acceptance.

## Overview

- **Architecture summary:** This slice enables configured internal converter/parser runtime execution while keeping Atlas product logic dependent on converter and parser adapter contracts. Runtime-specific execution is isolated inside adapter/runtime implementation scope; the metadata control plane continues to own validation, run state, safe summaries, and trace/review persistence.
- **Design objective:** Move from mock-only adapter execution toward controlled internal runtime execution without weakening adapter neutrality, data safety, or review boundaries.
- **Architectural style:** Layered Spring Boot control plane with adapter-isolated runtime execution.

## Architectural Drivers

### Functional Drivers

- Configured conversion through converter adapter only.
- Configured parsing through parser adapter only.
- Safe capability metadata and run reports.
- Existing file, run, result, and source chunk metadata remain the product control plane.
- Runtime failure must be safe, bounded, and non-leaky.

### Non-Functional Drivers

- Default CI must not require real internal binaries.
- Runtime output must not leak secrets, hosts, private paths, or raw logs.
- Runtime integration must preserve source trace, confidence, and review status.
- The architecture must support later worker/queue topology without product-layer rewrites.

## System Context

| Actor / System | Role |
|---|---|
| Knowledge administrator | Starts conversion/parser runs. |
| Platform administrator | Configures runtime availability and checks safe capability status. |
| Atlas metadata API | Validates requests, resolves adapters, records run evidence. |
| Converter runtime | Internal Office-to-PDF tool behind converter adapter. |
| Parser runtime | Internal PDF-to-Markdown/assets tool behind parser adapter. |
| Artifact root | Safe local or storage-backed relative artifact location. |

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge admin · Platform admin · Delivery lead             │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Atlas API Control Plane                                      │
│ capability endpoints · conversion runs · parser runs          │
├──────────────────────────────────────────────────────────────┤
│ Metadata Services                                             │
│ target validation · status mapping · safe summaries           │
├──────────────────────────────────────────────────────────────┤
│ Adapter Registries                                            │
│ default resolution · masked capabilities · safe statuses       │
└──────────────┬───────────────────────────────┬───────────────┘
               │ Adapter contract              │ Adapter contract
               ▼                               ▼
┌──────────────────────────────┐  ┌────────────────────────────┐
│ Converter Adapter Runtime    │  │ Parser Adapter Runtime     │
│ trinity-office isolated      │  │ document-normalize isolated│
└──────────────┬───────────────┘  └──────────────┬─────────────┘
               │ safe relative artifacts          │ safe relative artifacts
               ▼                                  ▼
┌──────────────────────────────────────────────────────────────┐
│ Metadata + Artifact Evidence                                  │
│ file status · run/result rows · source chunks · safe paths     │
└──────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

### API Control Plane

- Exposes existing converter/parser capability and run endpoints.
- Accepts configured runtime requests only after SDD acceptance and implementation.
- Returns the standard Atlas API envelope and safe error responses.

### Metadata Services

- Validate batch and file scope.
- Resolve adapter via registry.
- Enforce eligible target rules.
- Map adapter results to existing file statuses.
- Validate safe relative paths and confidence.
- Persist run/result/file/chunk evidence.

### Adapter Registries

- Own default adapter resolution.
- Expose safe capability status.
- Return masked configuration summaries only.
- Avoid raw command paths, endpoints, hostnames, and credentials in capability responses.

### Converter Adapter Runtime

- Translates Atlas converter requests into the approved internal conversion runtime contract.
- Produces product-facing converter results.
- Sanitizes runtime output before returning safe messages.
- May use local process or worker topology behind the adapter boundary.

### Parser Adapter Runtime

- Translates Atlas parser requests into the approved internal parser runtime contract.
- Produces product-facing parser results and source chunk descriptors.
- Validates or normalizes runtime manifest output before returning safe messages.
- May use local process or worker topology behind the adapter boundary.

### Artifact Evidence Boundary

- Stores or references generated PDFs, Markdown, and assets through safe relative paths.
- Rejects traversal, absolute, host-prefixed, or URI-prefixed paths.
- Does not expose raw storage root paths.

## Data Architecture

| Entity | Ownership | Role |
|---|---|---|
| File item | Metadata control plane | Latest product-visible file status and artifact path metadata. |
| Conversion run/result | Metadata control plane | Conversion execution evidence and per-file status. |
| Parser run/result | Metadata control plane | Parser execution evidence and per-file status. |
| Source chunk | Metadata control plane | Parser-emitted trace evidence for review, Wiki, graph, and Ask. |
| Runtime configuration | Server-side configuration | Selects available runtime adapter behavior without raw exposure. |

## Integration Architecture

### Runtime Interaction Pattern

- **Default path:** mock/fake adapters for CI and deterministic local verification.
- **Configured path:** opt-in runtime adapter execution when approved configuration is present.
- **Failure path:** missing config, timeout, invalid output, or adapter fault maps to safe failed/partial-failed evidence.

### Adapter Boundary Rules

- Runtime command/process/worker code is allowed only inside adapter/runtime implementation scope and tests.
- Product layers do not import runtime libraries, execute commands, or call runtime endpoints directly.
- Runtime names may appear in docs and adapter packages only where seam guards allow them.

## State Model

```text
Capability:
  DISABLED -> MISCONFIGURED -> AVAILABLE

Run:
  REQUESTED -> RUNNING -> SUCCEEDED
                     \--> PARTIAL_FAILED
                     \--> FAILED
```

## Security, Reliability, And Observability

- Safe messages are bounded and sanitized before persistence.
- Capability metadata is status-only and masked.
- Default verification stays mock-safe.
- Optional runtime smoke tests self-skip when runtime config is absent.
- Run/result rows remain the primary observable evidence for troubleshooting.

## Risks And Tradeoffs

| ID | Risk / Tradeoff | Architectural response |
|---|---|---|
| R-ARCH-001 | Local process topology may change later. | Keep process/worker concerns behind adapter runtime executor. |
| R-ARCH-002 | Runtime manifests may contain unsafe paths. | Validate all paths in product services before persistence. |
| R-ARCH-003 | Runtime logs may leak internals. | Truncate and sanitize before returning safe messages. |
| R-ARCH-004 | Default CI cannot depend on internal binaries. | Keep mock/fake path as default; add opt-in smoke tests only. |

## Open Questions

1. Local process, sidecar worker, or internal worker endpoint for first configured implementation?
2. Exact approved command/manifest contracts for `trinity-office` and `document-normalize`.
3. Timeout, max-output, and artifact materialization policy for runtime execution.
