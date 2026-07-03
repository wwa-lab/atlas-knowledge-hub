# User Stories: Parser Adapter

## Status

Draft. Derived from `docs/01-requirements/parser-adapter-requirements.md`.

## Story Map

| Story ID | Title | Requirements |
|---|---|---|
| US-PA-001 | Parse converted PDFs through an adapter boundary | REQ-PA-001, REQ-PA-002, REQ-PA-004, REQ-PA-012 |
| US-PA-002 | Inspect parser capabilities safely | REQ-PA-002, REQ-PA-003 |
| US-PA-003 | Generate traceable Markdown and assets | REQ-PA-005, REQ-PA-009, REQ-PA-010, REQ-PA-014 |
| US-PA-004 | Route low-confidence, OCR, and failed results to reviewable states | REQ-PA-006, REQ-PA-007, REQ-PA-008, REQ-PA-013 |
| US-PA-005 | Protect parser metadata writes and adapter seams | REQ-PA-001, REQ-PA-008, REQ-PA-011, REQ-PA-012 |

## US-PA-001: Parse converted PDFs through an adapter boundary

**Story**
As a knowledge base administrator,
I want parser runs to process converted PDFs through a parser adapter,
so that Atlas can produce Markdown without hard-coupling product workflows to one parser engine.

### Acceptance Criteria

1. **Given** a batch contains eligible `PDF_CONVERTED` files
   **When** a parser run is requested
   **Then** Atlas resolves a registered parser adapter before parsing begins.
2. **Given** non-adapter product layers are scanned
   **When** direct parser engine references or outbound command/network calls appear outside the adapter package
   **Then** seam guard verification fails.
3. **Given** tests run in CI
   **When** parser behavior is verified
   **Then** mock/fake parser engines are used and no real `document-normalize` runtime is required.

### Notes / Assumptions

- The existing converter adapter slice is the pattern for registry/capability/run behavior.
- Parser implementation starts after Phase 2 metadata API, which the current roadmap marks as implemented.

### Dependencies

- Metadata API file, batch, and source chunk records.
- Converter adapter output that marks files as `PDF_CONVERTED`.

### Out of Scope

- Real parser process execution, OCR execution, frontend UI, graph, Ask, and publish behavior.

### Open Questions

- OQ-PA-001: Real parser worker/process topology remains deferred beyond mock-engine verification.

## US-PA-002: Inspect parser capabilities safely

**Story**
As a platform administrator,
I want to see configured parser adapter capabilities and status,
so that I can understand which parser is available without exposing secrets or private runtime details.

### Acceptance Criteria

1. **Given** a parser adapter is configured
   **When** capabilities are listed
   **Then** the response includes adapter key, display name, version/status, supported input/output types, default marker, and masked configuration.
2. **Given** the adapter is disabled or misconfigured
   **When** capabilities are listed or selected
   **Then** Atlas exposes only a safe status and does not leak raw command paths, environment values, tokens, hostnames, or absolute paths.

### Notes / Assumptions

- `document-normalize` is the first named adapter, but future adapters such as MinerU, Docling, PaddleOCR, or internal OCR must remain replaceable.

### Dependencies

- Adapter registry and capability metadata contract.

### Out of Scope

- Production secret-manager integration.

### Open Questions

- None.

## US-PA-003: Generate traceable Markdown and assets

**Story**
As an SME reviewer,
I want parser output to include Markdown, assets, and source trace,
so that I can review generated knowledge against the original PDF evidence.

### Acceptance Criteria

1. **Given** a parser result succeeds
   **When** metadata is written back
   **Then** the file item records relative Markdown/assets paths, parser identity, confidence, and `MARKDOWN_GENERATED`.
2. **Given** Markdown is generated
   **When** the artifact is inspected
   **Then** it includes required front matter and source trace blocks with source file, PDF, page/section/chunk id, confidence, and review status.
3. **Given** source chunks are returned
   **When** they are persisted
   **Then** each chunk stores file item id, source file, page, section, confidence, and `REVIEW_REQUIRED` unless an explicit review status is supplied safely.

### Notes / Assumptions

- Parser-adapter writes source chunks but does not publish Wiki pages.
- Generated content remains review-required until a later review/publish flow changes it.

### Dependencies

- Markdown standard.
- Existing `file_item` and `source_chunk` metadata tables.

### Out of Scope

- Wiki publication and graph projection.

### Open Questions

- OQ-PA-002: Whether a later slice creates `wiki_page` rows from parser output or from approved publish output.

## US-PA-004: Route low-confidence, OCR, and failed results to reviewable states

**Story**
As a delivery lead,
I want parser reports to classify low-confidence, OCR-required, and failed files,
so that follow-up review work is visible and no generated content is treated as trusted too early.

### Acceptance Criteria

1. **Given** parser confidence is below `0.80`
   **When** the parser result is mapped
   **Then** the file item status is `LOW_CONFIDENCE` and review status remains `REVIEW_REQUIRED`.
2. **Given** parser output indicates OCR is required
   **When** the result is mapped
   **Then** the file item status is `OCR_REQUIRED`, OCR is not executed, and the report includes the reason.
3. **Given** parsing fails
   **When** the result is persisted
   **Then** the file item status is `FAILED` with a sanitized safe error.
4. **Given** a parser run completes
   **When** the report is viewed
   **Then** totals include Markdown generated, low-confidence, OCR-required, failed, skipped/ineligible, and unsupported outcomes.

### Notes / Assumptions

- The low-confidence threshold is committed as `< 0.80` for this slice.

### Dependencies

- Existing `FileStatus` values and review status rules.

### Out of Scope

- Automatic approval or publication.

### Open Questions

- OQ-PA-003: Confirm threshold before implementation if product wants a different value.

## US-PA-005: Protect parser metadata writes and adapter seams

**Story**
As an implementation owner,
I want parser result writes and adapter seams to be guarded,
so that malformed parser output cannot corrupt metadata or leak sensitive information.

### Acceptance Criteria

1. **Given** a parser result contains an absolute path, URI path, traversal path, unknown file id, or out-of-range confidence
   **When** Atlas validates the result
   **Then** the run fails safely with field-level validation and no unsafe metadata is persisted.
2. **Given** parser errors contain secrets, stack traces, or private paths
   **When** the error is returned or stored
   **Then** only a bounded sanitized summary is kept.
3. **Given** automated verification runs
   **When** seam guard and secret scans execute
   **Then** non-adapter product layers contain no direct parser engine calls and generated SDD/code contain no raw secrets or private paths.

### Notes / Assumptions

- Relative path validation should reuse the existing metadata API safety rule where possible.

### Dependencies

- Existing relative-path validator and API error envelope.

### Out of Scope

- Full production credential management.

### Open Questions

- None.
