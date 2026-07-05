# User Stories: Real Office Parser Runtime

## Status

Draft for user review. Implementation remains blocked until the SDD is accepted.

## Story Set

### US-REAL-OFFICE-PARSER-RUNTIME-001: Start Runtime Integration From An Accepted SDD

**Story:**
As a knowledge platform owner,
I want real runtime integration to start from an accepted SDD,
so that Atlas does not bypass adapter, safety, and trust boundaries while moving beyond mocks.

#### Acceptance Criteria

1. **Given** the slice is requested
   **When** the SDD is generated
   **Then** requirements, stories, spec, architecture, data flow, data model, design, API guide, tasks, and traceability exist in English and Simplified Chinese.

2. **Given** product code changes are proposed
   **When** the SDD has not been accepted
   **Then** implementation remains blocked and the traceability file says so.

#### Notes / Assumptions

- The user has explicitly started Wave 2 / `real-office-parser-runtime`.

#### Dependencies

- Existing converter/parser adapter SDD and implementation.

#### Out of Scope

- Product runtime code before user acceptance.

#### Open Questions

- None.

### US-REAL-OFFICE-PARSER-RUNTIME-002: Run Office Conversion Through The Adapter

**Story:**
As a knowledge base administrator,
I want Office-to-PDF conversion to use a configured internal runtime through the converter adapter,
so that real conversion can be introduced without coupling product workflows to `trinity-office`.

#### Acceptance Criteria

1. **Given** `trinity-office` runtime is configured and available
   **When** a conversion run requests configured execution
   **Then** Atlas resolves the converter adapter, executes within the adapter boundary, and records safe per-file results.

2. **Given** runtime configuration is missing or disabled
   **When** a conversion run requests configured execution
   **Then** the run fails safely as unavailable or misconfigured and file metadata remains unchanged unless safe per-file results exist.

3. **Given** runtime output includes a PDF path or error text
   **When** Atlas persists or returns the result
   **Then** paths are safe relative paths and errors are sanitized.

#### Notes / Assumptions

- The exact command contract is not yet approved and must remain an open question.

#### Dependencies

- Existing `ConverterAdapter` and conversion run API.

#### Out of Scope

- External cloud conversion.

#### Open Questions

- What is the approved command or worker contract?

### US-REAL-OFFICE-PARSER-RUNTIME-003: Run PDF Parsing Through The Adapter

**Story:**
As a delivery lead,
I want PDF-to-Markdown parsing to use a configured internal runtime through the parser adapter,
so that real Markdown and source chunks can be generated while preserving review and trace metadata.

#### Acceptance Criteria

1. **Given** `document-normalize` runtime is configured and available
   **When** a parser run targets eligible `PDF_CONVERTED` files
   **Then** Atlas resolves the parser adapter, executes within the adapter boundary, and records Markdown path, assets path, confidence, source chunks, and safe messages.

2. **Given** parser output confidence is below the accepted threshold
   **When** the result is persisted
   **Then** the file maps to `LOW_CONFIDENCE` and remains `REVIEW_REQUIRED`.

3. **Given** parser output is invalid or unsafe
   **When** Atlas validates the result
   **Then** unsafe output is rejected before persistence and the API returns a safe validation error.

#### Notes / Assumptions

- Markdown byte materialization may occur through local artifact storage or a later worker/storage boundary, but product metadata must stay relative and safe.

#### Dependencies

- Existing `ParserAdapter`, parser run API, source chunk metadata, and relative path validation.

#### Out of Scope

- Wiki publishing, graph extraction, Ask indexing, OCR execution, and LLM enrichment.

#### Open Questions

- What output manifest format should `document-normalize` return?

### US-REAL-OFFICE-PARSER-RUNTIME-004: Inspect Runtime Capability Safely

**Story:**
As a platform administrator,
I want capability endpoints to show whether runtime adapters are configured, available, disabled, or misconfigured,
so that I can diagnose setup without exposing command paths, endpoints, or credentials.

#### Acceptance Criteria

1. **Given** runtime settings are configured
   **When** capability endpoints are requested
   **Then** responses include adapter key, display name, version/status, default marker, supported inputs/outputs, and masked configuration summary.

2. **Given** runtime settings contain sensitive values
   **When** capability metadata is returned
   **Then** raw paths, endpoints, tokens, passwords, command arguments, and hostnames are not exposed.

#### Notes / Assumptions

- Existing capability endpoint shapes should be reused where possible.

#### Dependencies

- Existing converter and parser capability endpoints.

#### Out of Scope

- Secret manager-backed storage or credential rotation.

#### Open Questions

- Which non-secret runtime fields are safe enough to expose as status-only summaries?

### US-REAL-OFFICE-PARSER-RUNTIME-005: Preserve CI And Safety Gates

**Story:**
As an implementation agent,
I want runtime integration to remain mock-safe and statically guarded,
so that normal verification does not require local internal binaries and product layers cannot drift into direct tool calls.

#### Acceptance Criteria

1. **Given** no real runtime binary is installed
   **When** normal backend verification runs
   **Then** tests pass using mocks/fakes and do not require internal tools.

2. **Given** a direct tool call is added outside an allowed adapter/runtime package
   **When** seam guard tests run
   **Then** the tests fail.

3. **Given** changed files are scanned
   **When** secret/private-path and network/dependency scans run
   **Then** no raw credentials, private paths, real data, or new external cloud calls are found.

#### Notes / Assumptions

- Optional opt-in runtime tests may be skipped unless configuration is present.

#### Dependencies

- Existing `AdapterSeamGuardTest` and backend verification pipeline.

#### Out of Scope

- Making CI install or run internal binaries by default.

#### Open Questions

- What opt-in environment flag should enable runtime smoke tests?
