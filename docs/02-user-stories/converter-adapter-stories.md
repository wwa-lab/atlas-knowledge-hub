# User Stories: Converter Adapter

## Status

Draft. Derived from `docs/01-requirements/converter-adapter-requirements.md`.

## Story Index

| Story ID | Title | Requirement IDs |
|---|---|---|
| US-CA-001 | Convert Office files behind an adapter boundary | REQ-CA-001, REQ-CA-002, REQ-CA-005 |
| US-CA-002 | Inspect converter capabilities safely | REQ-CA-003, REQ-CA-009 |
| US-CA-003 | Preserve metadata and trace on conversion results | REQ-CA-004, REQ-CA-007, REQ-CA-008 |
| US-CA-004 | Report unsupported and failed conversion outcomes | REQ-CA-006, REQ-CA-009 |
| US-CA-005 | Verify adapter isolation with mock engines | REQ-CA-010, REQ-CA-011, REQ-CA-012 |

## US-CA-001: Convert Office Files Behind An Adapter Boundary

**Story**
As a **knowledge base administrator**,
I want Atlas to convert supported Office files to PDFs through a converter adapter,
so that batches can progress toward parsing without binding product workflows directly to one tool.

### Acceptance Criteria

1. **Given** a batch contains `pptx`, `docx`, or `xlsx` file metadata
   **When** a conversion run is requested
   **Then** Atlas routes the work through a configured converter adapter interface, not through direct `trinity-office` calls in controller/service/repository code.

2. **Given** the configured adapter succeeds for a supported Office file
   **When** the run completes
   **Then** the file result is `PDF_CONVERTED` with a relative `pdfPath`.

3. **Given** no real `trinity-office` binary is installed in CI
   **When** tests run
   **Then** the mock engine/fake command runner covers the conversion behavior without requiring the real binary.

### Notes / Assumptions

- `trinity-office` is the first concrete target, but adapter contracts must allow later replacement.
- PDF pass-through behavior is documented as an open question until storage-adapter policy is accepted.

### Dependencies

- Phase 2 metadata API and file item status model.
- Adapter/API guide for this slice.

### Out Of Scope

- PDF-to-Markdown parsing.
- OCR execution.
- Frontend upload UI.

### Open Questions

- OQ-CA-001, OQ-CA-002, OQ-CA-003.

## US-CA-002: Inspect Converter Capabilities Safely

**Story**
As a **platform administrator**,
I want to see which converter adapter is configured and what it supports,
so that I can validate readiness without exposing local paths, command details, or secrets.

### Acceptance Criteria

1. **Given** a converter adapter is registered
   **When** capability metadata is requested
   **Then** the response includes adapter key, display name, version, supported source types, output type, default marker, and availability status.

2. **Given** adapter configuration contains command paths or credentials
   **When** metadata is returned or logged
   **Then** only masked/status-only configuration summaries are visible.

3. **Given** no converter adapter is available
   **When** capability metadata is requested
   **Then** Atlas returns a user-safe unavailable status rather than leaking runtime details.

### Notes / Assumptions

- Capability metadata is internal/admin-facing and has no frontend work in this slice.

### Dependencies

- Configuration properties and adapter registry.

### Out Of Scope

- Secret manager integration.
- Production RBAC enforcement.

### Open Questions

- None.

## US-CA-003: Preserve Metadata And Trace On Conversion Results

**Story**
As a **delivery lead**,
I want converted files to retain source path, adapter, status, confidence, and review metadata,
so that downstream parser, review, and Wiki work remains traceable.

### Acceptance Criteria

1. **Given** a conversion run produces a PDF result
   **When** Atlas records the result
   **Then** it stores adapter name, source path, source type, file status, `pdfPath`, confidence if available, and timestamps.

2. **Given** a converter result includes generated or derived content
   **When** the metadata is written
   **Then** review status remains `REVIEW_REQUIRED` unless a later explicit review action changes it.

3. **Given** any path value is absolute, contains traversal, or reveals a private machine path
   **When** Atlas validates the result
   **Then** the result is rejected or sanitized according to the API guide, and no unsafe path is persisted.

### Notes / Assumptions

- Conversion result persistence updates existing file item metadata and may also create conversion-run audit rows.

### Dependencies

- Metadata API file item model.

### Out Of Scope

- Source chunk generation; that belongs to parser-adapter.

### Open Questions

- None.

## US-CA-004: Report Unsupported And Failed Conversion Outcomes

**Story**
As an **SME reviewer**,
I want unsupported and failed files to be reported with clear safe statuses,
so that I know which documents need another source, OCR, or manual handling.

### Acceptance Criteria

1. **Given** an unsupported source type is included
   **When** conversion runs
   **Then** Atlas records `UNSUPPORTED` with a user-safe reason.

2. **Given** a supported Office file fails conversion
   **When** the adapter returns an error
   **Then** Atlas records `PDF_CONVERT_FAILED` or `FAILED` according to the API guide and includes a safe summary.

3. **Given** a file likely needs OCR or image-specific processing
   **When** converter logic classifies it as outside Office-to-PDF conversion
   **Then** Atlas records `OCR_REQUIRED` where appropriate without invoking OCR.

### Notes / Assumptions

- Error messages are summaries, not raw engine output.

### Dependencies

- Existing `FileStatus` enum.

### Out Of Scope

- Retry scheduling UI.
- OCR processing.

### Open Questions

- None.

## US-CA-005: Verify Adapter Isolation With Mock Engines

**Story**
As an **engineer implementing Atlas**,
I want tests and static guards proving conversion stays behind adapters,
so that future adapter additions do not couple the product to one engine.

### Acceptance Criteria

1. **Given** converter adapter code is added
   **When** `cd backend && mvn verify` runs
   **Then** unit tests, mock-engine integration tests, and static seam guards pass.

2. **Given** a controller/service/repository references concrete engine names or command execution APIs
   **When** seam guard tests run
   **Then** the build fails.

3. **Given** docs are handed to Codex for implementation
   **When** tasks are read
   **Then** every task maps to requirement IDs, spec sections, exact verification commands, and adapter/no-network/secret-masked constraints.

### Notes / Assumptions

- Existing `AdapterSeamGuardTest` must be updated rather than removed.

### Dependencies

- SDD task list and API/adapter guide.

### Out Of Scope

- Code implementation in this SDD generation pass.

### Open Questions

- None.
