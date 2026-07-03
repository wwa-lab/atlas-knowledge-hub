# Detailed Design: Converter Adapter

## Status

Draft. Derived from `docs/03-spec/converter-adapter-spec.md` and `docs/04-architecture/converter-adapter-architecture.md`.

## Design Scope

In scope:

- Converter adapter interface and registry.
- Masked capability metadata.
- Conversion run creation, state transitions, report retrieval, and result persistence.
- File item status/PDF/error update through metadata boundary.
- Mock/fake adapter implementation for tests.
- Optional `trinity-office` wrapper behind configuration, not required for CI.
- Static seam guard update.

Out of scope:

- Parser, OCR, storage bytes, vector/model, Wiki publish, graph, Ask, frontend UI, production auth/RBAC.

## Module Design

### Converter API Module

Responsibilities:

- `GET /api/converter-adapters`: return registered converter capabilities.
- `POST /api/batches/{batchId}/conversion-runs`: create and execute a conversion run through adapter contract.
- `GET /api/conversion-runs/{runId}`: return run summary and per-file report.
- Return the existing `ApiEnvelope` shape and user-safe errors.

No controller may reference `trinity-office`, command execution APIs, file bytes, or outbound HTTP clients.

### Converter Application Module

Responsibilities:

- Validate batch existence and target file ids.
- Resolve adapter by requested key or default adapter.
- Create `conversion_run` in `REQUESTED`, transition to `RUNNING`, then terminal state.
- Call only the `ConverterAdapter` interface.
- Map adapter results to existing `FileStatus` values.
- Sanitize errors and validate relative artifact paths before persistence.
- Update file item conversion metadata and persist conversion result records.

### Converter Adapter Registry

Responsibilities:

- Register converter adapters from configuration.
- Return capability metadata in stable shape.
- Mark adapter status as `AVAILABLE`, `DISABLED`, or `MISCONFIGURED`.
- Provide masked configuration summaries only.

### Converter Adapter Interface

The adapter interface is product-facing and uses Atlas domain terms.

Required concepts:

- **Capability:** adapter key, display name, version, supported source types, output type, default marker, status, masked config.
- **Request:** run id, batch id, file item descriptors, artifact root hint when available, and correlation metadata.
- **Result:** per-file status, relative PDF path when available, confidence when available, safe error, and adapter identity.

The interface must not expose vendor-specific command flags to product layers.

### Trinity-office Adapter Boundary

The concrete `trinity-office` adapter may translate Atlas requests into command invocation, but only inside adapter implementation code. It must:

- Be disabled unless required configuration is present.
- Return `MISCONFIGURED` capability status when required configuration is missing.
- Sanitize command output into bounded safe errors.
- Never log raw command args containing secrets or private paths.
- Be replaceable by another adapter without changing controllers or metadata services.

### Mock/Fake Converter Adapter

The mock/fake adapter is required for tests. It must deterministically support:

- Office success to `PDF_CONVERTED`.
- Office failure to `PDF_CONVERT_FAILED`.
- PDF pass-through to `PDF_CONVERTED`.
- Image to `OCR_REQUIRED`.
- Unsupported to `UNSUPPORTED`.
- Misconfigured adapter state.
- Unsafe path rejection at service boundary.

## API / Interface Design

Full endpoint and payload shapes are in `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`.

Implementation rules:

- Use existing envelope conventions.
- Use existing `FileStatus`, `SourceType`, and `ReviewStatus` values.
- Do not add frontend-facing routes.
- Do not expose raw adapter config.

## Data Design

Use the logical data model in `docs/04-architecture/converter-adapter-data-model.md`.

Implementation notes:

- Add conversion run and per-file result persistence before enabling report retrieval.
- Keep conversion results as evidence records; do not rely only on mutable `file_item` latest fields.
- Keep `conversion_run.summary` recomputable from result rows.
- Existing `file_item` remains the product-visible latest status holder.

## Workflow / Execution Design

1. Request arrives with `batchId`, optional `adapterKey`, optional target `fileIds`, and `requestedBy`.
2. Service validates batch and target file metadata.
3. Service resolves adapter.
4. If adapter is unavailable/misconfigured, create a failed run and return a safe error; file statuses remain unchanged.
5. Service transitions run to `RUNNING`.
6. Service calls `ConverterAdapter.convert(request)`.
7. Service validates each result.
8. Service persists result rows.
9. Service updates file item status, `pdfPath`, confidence, and safe `errorMessage`.
10. Service transitions run to `SUCCEEDED`, `PARTIAL_FAILED`, or `FAILED`.
11. API returns run report.

## Validation And Error Handling

| Case | Behavior |
|---|---|
| Unknown batch | `404 NOT_FOUND` envelope. |
| Unknown file target | `400 VALIDATION_ERROR` or `404 NOT_FOUND` according to existing API pattern; do not partially start run. |
| Adapter key unknown | `400 VALIDATION_ERROR` with safe adapter unavailable message. |
| Adapter disabled/misconfigured | Create failed run when possible; return user-safe error. |
| Unsafe `pdfPath` | Reject result; record safe validation failure; do not persist unsafe path. |
| Raw/private error content | Sanitize before persistence/response. |
| Adapter throws unexpected exception | Run becomes `FAILED`; response hides internals. |

## Security / Audit / Reliability Design

- Configuration: use externalized properties; no raw command path or secrets in capability responses.
- Audit: persist run id, adapter key, timestamps, result counts, target file ids, and safe messages.
- Reliability: tests must cover adapter unavailable and partial failure.
- No-network: converter adapter must not introduce external cloud/network calls in this slice.
- Review safety: conversion does not set `APPROVED` or `PUBLISHED`.

## Testing Considerations

Required checks for implementation:

- Unit tests for result mapping, summary calculation, path validation, error sanitization, and run state transitions.
- Integration tests using mock/fake converter adapter over seeded PostgreSQL metadata.
- API contract tests for capability, create run, and get run endpoints.
- Static seam guard proving concrete engine references are limited to adapter implementation packages.
- Secret/path scan over source, config, docs, and tests.

## Risks / Design Tradeoffs

| Risk | Design Response |
|---|---|
| Real command contract unknown. | Define interface and fake runner now; keep real wrapper config-gated. |
| In-process execution may not be the final runtime. | Keep adapter boundary runtime-neutral. |
| Existing metadata API has no conversion-run records. | Add explicit run/result model in this slice for evidence and reports. |

## Open Questions

- OQ-CA-001: local process wrapper vs external worker.
- OQ-CA-002: PDF pass-through copy/reference policy.
- OQ-CA-003: exact `trinity-office` command-line contract.
