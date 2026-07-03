# Requirements: Converter Adapter

## Status

Draft. Phase 3 adapter slice. ID prefix: `CA` for `converter-adapter`.

## Slice Contract

- **Goal:** Atlas can convert supported Office source files into traceable PDF artifacts through a product-facing converter adapter, while keeping `trinity-office` replaceable and outside direct product workflow calls.
- **Phase:** 3 adapter.
- **Scope:** converter adapter contract, capability metadata, configuration validation, conversion run lifecycle, mock-engine test behavior, `trinity-office` wrapper boundary, metadata write-back for PDF paths/status/errors, and user-safe reporting.
- **Exclusions:** parser/PDF-to-Markdown behavior, OCR execution, storage adapter implementation, vector/model adapters, Wiki publishing, graph/Ask, frontend UI changes, production auth/RBAC, real company documents, and external cloud calls.
- **Sources:** `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/00-context/sdd-profile.md`, `docs/01-requirements/requirement.md`, `docs/00-context/slice-roadmap.md`, `docs/architecture.md`, `docs/batch-processing-design.md`, `docs/markdown-standard.md`, `docs/03-spec/metadata-api-spec.md`, `docs/04-architecture/metadata-api-data-model.md`, `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`.
- **Verification row:** Unit + integration tests against **mock engines**. Hard constraints: parser/converter/model/vector/storage go **only** through product-facing adapters; never call a tool directly; never hardcode one implementation. API guide: adapter contract required per adapter slice.

## Product Requirements

| ID | Requirement | Priority | Product Source |
|---|---|---|---|
| REQ-CA-001 | Product workflows must invoke Office-to-PDF conversion only through a product-facing converter adapter interface, never by calling `trinity-office` directly from controllers, metadata services, repositories, UI code, or scripts. | Must | REQ-PROD-015, REQ-PROD-016 |
| REQ-CA-002 | The first converter adapter contract must support `trinity-office` as a replaceable implementation behind capability metadata and configuration, not as a hardcoded singleton. | Must | REQ-PROD-016, REQ-PROD-017 |
| REQ-CA-003 | Converter capability metadata must declare supported source types, output type, adapter name/version, default marker, availability status, and masked configuration summary. | Must | REQ-PROD-017, REQ-PROD-061 |
| REQ-CA-004 | Conversion runs must accept batch/file metadata and emit per-file results with source path, source type, file status, generated PDF path when available, confidence when available, user-safe error message, adapter name, and timestamps. | Must | REQ-PROD-018, REQ-PROD-019 |
| REQ-CA-005 | Supported Office inputs (`pptx`, `docx`, `xlsx`) must move to `PDF_CONVERTED` on successful conversion and include a relative `pdfPath`. | Must | REQ-PROD-012, REQ-PROD-018 |
| REQ-CA-006 | Unsupported, failed, OCR-needed, or low-confidence conversion outcomes must be represented using existing file statuses and report entries without introducing undocumented statuses. | Must | REQ-PROD-012, REQ-PROD-019 |
| REQ-CA-007 | Conversion results must preserve source trace and review status; conversion must not auto-approve generated or derived content. | Must | REQ-PROD-018, REQ-PROD-022, REQ-PROD-026 |
| REQ-CA-008 | All artifact paths and source paths in converter input/output must be relative, traversal-free, and free of private absolute paths. | Must | REQ-PROD-074, REQ-PROD-076, Security and Data Rules |
| REQ-CA-009 | Converter configuration and errors must never expose raw secrets, private endpoints, local machine paths, command-line credentials, stack traces, or confidential document content. | Must | REQ-PROD-077, Adapter Standards |
| REQ-CA-010 | The converter slice must include unit and integration tests using mock engines/fake command runners; CI must not require a local `trinity-office` binary or real document corpus. | Must | Phase 3 verification row |
| REQ-CA-011 | The adapter seam guard must evolve from "adapter package empty" to "only adapter implementations may reference engine-specific names or command execution boundaries." | Must | Adapter gate |
| REQ-CA-012 | This slice must provide an adapter/API implementation guide before product-code implementation starts. | Must | `docs/00-context/slice-roadmap.md` Phase 3 API guide rule |

## Acceptance Criteria

| ID | Requirement | Observable Completion Standard |
|---|---|---|
| AC-CA-01 | REQ-CA-001, REQ-CA-011 | Static guard tests fail if non-adapter product layers reference `trinity-office`, command execution APIs, or outbound network clients for conversion. |
| AC-CA-02 | REQ-CA-002, REQ-CA-003 | A capability listing exposes converter adapter metadata with masked config and no raw command path or secret values. |
| AC-CA-03 | REQ-CA-004, REQ-CA-005 | A mock conversion run over supported Office file metadata returns `PDF_CONVERTED` and relative PDF artifact paths. |
| AC-CA-04 | REQ-CA-006 | Unsupported/failing files produce `UNSUPPORTED`, `PDF_CONVERT_FAILED`, or `OCR_REQUIRED` outcomes with user-safe report entries. |
| AC-CA-05 | REQ-CA-007, REQ-CA-008 | Source path, PDF path, confidence, adapter name, and review status are preserved without absolute/traversal paths. |
| AC-CA-06 | REQ-CA-009 | Error payloads and logs contain no raw secrets, private paths, stack traces, or confidential content. |
| AC-CA-07 | REQ-CA-010 | `cd backend && mvn verify` passes with mock-engine unit/integration coverage and without requiring real `trinity-office`. |
| AC-CA-08 | REQ-CA-012 | The API/adapter guide exists in English and Chinese and is linked from traceability before implementation handoff. |

## Assumptions

- Phase 2 metadata API is complete enough for Phase 3 entry; current backend already has file item metadata, `pdfPath`, status, confidence, review status, and source chunk concepts.
- The first implementation may use a fake command runner or mock engine in tests; real `trinity-office` execution is optional and configuration-gated.
- Converter execution may run inside the Spring Boot process initially, but product layers must depend only on converter adapter interfaces and contracts.

## Open Questions

| ID | Question | Owner |
|---|---|---|
| OQ-CA-001 | Should real `trinity-office` execution be launched as a local process from the adapter implementation or moved immediately to an external worker process? | Architecture |
| OQ-CA-002 | Should PDF pass-through for existing PDFs copy the file into generated PDF storage or reference the original relative path until storage-adapter exists? | Product/Architecture |
| OQ-CA-003 | What exact `trinity-office` command-line contract will be available in the target environment? | Platform |
