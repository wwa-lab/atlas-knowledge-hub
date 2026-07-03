# Architecture: Converter Adapter

## Status

Draft. Phase 3 adapter slice. Source spec: `docs/03-spec/converter-adapter-spec.md`.

## Overview

The converter adapter slice introduces a product-facing Office-to-PDF conversion boundary between Atlas metadata workflows and concrete conversion engines. `trinity-office` is treated as the first replaceable adapter implementation target; controller, service, repository, and UI code interact only with adapter registry and conversion contracts.

## Architectural Drivers

| Driver | Effect |
|---|---|
| Adapter boundary | Product workflow resolves a converter through a registry and does not call engines directly. |
| Metadata continuity | Conversion writes file status, PDF path, confidence, review status, and safe errors back through metadata contracts. |
| Engine replaceability | Adapter capability metadata and configuration shape allow later converter implementations. |
| Mock-engine verification | CI uses mock/fake adapters and does not depend on a real `trinity-office` binary. |
| Secret/path safety | Configuration and errors are masked; persisted paths are relative only. |

## Grounded Existing Context

- Existing batch creation accepts inventory file metadata with `sourcePath`, `sourceType`, `status`, `confidence`, `reviewStatus`, `pdfPath`, `markdownPath`, `assetsPath`, `errorMessage`, and chunks in `backend/src/main/java/com/atlas/metadata/dto/CreateBatchRequest.java:18`.
- Existing file item response exposes `pdfPath`, status, confidence, and review status in `backend/src/main/java/com/atlas/metadata/dto/FileItemResponse.java:9`.
- Existing file statuses include `PDF_CONVERTED`, `PDF_CONVERT_FAILED`, `OCR_REQUIRED`, `FAILED`, and `UNSUPPORTED` in `backend/src/main/java/com/atlas/metadata/enums/FileStatus.java:6`.
- Existing adapter guard currently keeps the Phase 2 adapter package empty and blocks engine references in `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:11`; this slice changes that rule to permit engine-specific references only under adapter implementation boundaries.

## System Context

```text
+-------------------------+
| Atlas user / workflow   |
| batch conversion action |
+-----------+-------------+
            |
            v
+-------------------------+
| Spring Boot API         |
| conversion run endpoints|
+-----------+-------------+
            |
            v
+-------------------------+
| Conversion Service      |
| validation + state      |
+-----------+-------------+
            |
            v
+-------------------------+       +-------------------------+
| Converter Adapter       |------>| trinity-office wrapper  |
| Registry + Interface    |       | or mock/fake engine     |
+-----------+-------------+       +-------------------------+
            |
            v
+-------------------------+
| Metadata Persistence    |
| file status + reports   |
+-------------------------+
```

## Component Breakdown

| Component | Responsibility |
|---|---|
| Converter API boundary | Exposes internal endpoints for adapter capabilities, conversion run creation, and run reporting. |
| Converter application service | Validates batch/file targets, resolves adapter, manages run state, maps adapter results, and writes metadata. |
| Converter adapter registry | Holds configured converter adapters and returns capability metadata with masked config. |
| Converter adapter interface | Product-facing contract for converting source file metadata into PDF conversion results. |
| Trinity-office adapter implementation | Optional concrete adapter behind the interface; no product layer depends on it directly. |
| Mock/fake converter engine | Deterministic test adapter used by unit/integration tests and CI. |
| Conversion persistence | Records run summary and per-file results; updates existing file item metadata. |
| Seam guard tests | Prove concrete engine names and command execution boundaries stay in adapter implementation packages only. |

## Layer Boundaries

- **Controller layer:** accepts JSON requests and returns envelopes. It must not execute commands, inspect local file bytes, or reference `trinity-office`.
- **Application/service layer:** owns validation, state transitions, and adapter registry use. It depends on converter interfaces, not concrete engines.
- **Adapter layer:** owns engine-specific invocation, including fake/mock runner and optional `trinity-office` wrapper.
- **Repository layer:** persists conversion run/result metadata and file item updates. It must not execute converters.
- **Worker/execution boundary:** may remain in-process for the first slice but must be swappable to an external worker later.

## Integration Architecture

| Integration | Pattern | Boundary |
|---|---|---|
| Metadata API file item model | Internal service/repository update | Preserve existing statuses and relative artifact paths. |
| `trinity-office` | Adapter implementation only | Config-gated, replaceable, not required in CI. |
| Mock converter | Adapter implementation for tests | Deterministic success/failure/OCR/unsupported behavior. |
| Future storage adapter | Deferred | This slice records relative artifact paths but does not store bytes. |
| Future parser adapter | Deferred | Consumes converted PDFs in a later slice. |

## State Strategy

Conversion run state is separate from file status. File status remains the product-visible lifecycle from `docs/batch-processing-design.md`; conversion run status records execution lifecycle and summary.

```text
REQUESTED -> RUNNING -> SUCCEEDED
                      -> PARTIAL_FAILED
                      -> FAILED
```

## Security And Data Safety

- No real company documents or private paths in tests or seed data.
- No raw command output returned to users.
- Config summaries are masked/status-only.
- Paths are relative and traversal-free before persistence.
- Logs may include run id, batch id, adapter key, and safe status; logs must not include raw source content, full local paths, or credentials.

## Architecture Review Notes

Applied architecture-review criteria for this SDD pass:

- **Decoupling:** product layers depend on adapter contracts, not concrete engine calls.
- **Configuration externalization:** adapter enablement and command details are config-driven and masked.
- **Error handling:** errors cross boundaries as user-safe summaries.
- **Immutability/audit:** conversion run/result records are append-oriented evidence; file item status updates are explicit state transitions.

## Risks And Tradeoffs

| Risk | Mitigation |
|---|---|
| Real `trinity-office` command contract is not yet confirmed. | Tests use fake command runner; real wrapper remains configuration-gated until OQ-CA-003 is answered. |
| In-process adapter may later need external worker isolation. | Keep adapter interface and run contract independent from runtime topology. |
| PDF pass-through policy depends on storage-adapter. | Record OQ-CA-002 and keep path semantics explicit in the API guide. |

## Open Questions

- OQ-CA-001: local process wrapper vs external worker.
- OQ-CA-002: PDF pass-through copy vs reference policy.
- OQ-CA-003: exact `trinity-office` command-line contract.
