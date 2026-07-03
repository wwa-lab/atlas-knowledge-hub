# User Stories: Review Publish

## Status

Draft. Derived from `review-publish` requirements through `req-to-user-story`.

## US-REVIEW-PUBLISH-001: Triage Publish-Blocking Review Queues

**Story:** As a Delivery Lead, I want Processing Center queues to distinguish missing trace, low confidence, OCR, parser failure, and LLM-generated review items, so that I can prioritize work before publishing.

### Acceptance Criteria

1. **Given** a batch has mixed issue types, **when** the Processing Center loads, **then** each issue type is counted separately and publish-ready content is shown separately.
2. **Given** an item lacks `source_trace`, **when** downstream eligibility is evaluated, **then** it is blocked from Wiki, Graph, and Ask.
3. **Given** the UI shows blocked queues, **when** a user views actions, **then** actions remain mock/contract-driven until the real endpoint is implemented.

### Trace

- Requirements: REQ-REVIEW-PUBLISH-001, REQ-REVIEW-PUBLISH-008, REQ-REVIEW-PUBLISH-009

## US-REVIEW-PUBLISH-002: Record SME Review Decisions

**Story:** As an SME Reviewer, I want to approve, mark need-fix, or require OCR with comments and affected chunks, so that knowledge quality decisions are auditable.

### Acceptance Criteria

1. **Given** a file or chunk is review-required, **when** I submit `APPROVE`, **then** the target becomes `APPROVED` and an append-only review record is created.
2. **Given** I submit `NEED_FIX` or `OCR_REQUIRED`, **when** the request succeeds, **then** the target state reflects the action and publish remains blocked.
3. **Given** I enter comments and affected chunks, **when** history is fetched, **then** reviewer, action, timestamp, comment, and affected chunks are visible in chronological order.

### Trace

- Requirements: REQ-REVIEW-PUBLISH-002, REQ-REVIEW-PUBLISH-003

## US-REVIEW-PUBLISH-003: Publish Approved Markdown To Wiki Metadata

**Story:** As a Knowledge Space Admin, I want to publish only approved Markdown into Wiki page metadata, so that trusted downstream surfaces consume reviewed content.

### Acceptance Criteria

1. **Given** a candidate has `APPROVED` status, relative Markdown path, confidence, source document IDs, and source trace coverage, **when** publish is requested, **then** a Wiki page is created or updated with `PUBLISHED` status.
2. **Given** a candidate is unreviewed, low confidence, missing trace, or missing Markdown path, **when** publish is requested, **then** the API returns a user-safe validation error and no Wiki page changes.
3. **Given** publish succeeds, **when** the Wiki page is read, **then** source document IDs, Markdown path, confidence, owner, and last updated timestamp are preserved.

### Trace

- Requirements: REQ-REVIEW-PUBLISH-004, REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-007

## US-REVIEW-PUBLISH-004: Preserve Raw Evidence And Adapter Boundaries

**Story:** As a Platform Engineer, I want publish to update metadata without mutating raw artifacts or directly calling adapters, so that Atlas remains traceable and parser-neutral.

### Acceptance Criteria

1. **Given** a publish operation succeeds, **when** raw source chunks and file artifact paths are inspected, **then** original parser output and source trace remain unchanged.
2. **Given** implementation is reviewed, **when** dependency boundaries are checked, **then** publish logic uses metadata services/repositories and does not directly invoke parser, converter, model, vector, storage, or search engines.
3. **Given** errors occur, **when** responses and logs are reviewed, **then** no raw secrets, private absolute paths, or confidential content are exposed.

### Trace

- Requirements: REQ-REVIEW-PUBLISH-006, REQ-REVIEW-PUBLISH-010, REQ-REVIEW-PUBLISH-011

## Dependencies

- Metadata API entities and review history from Phase 2.
- Adapter slices from Phase 3 remain behind seams and are not invoked by publish.
- Accepted FE baseline for Processing Center, Wiki, and Global Chat trust messaging.

## Out Of Scope

- Graph node/edge extraction.
- Ask/RAG query execution.
- Production authentication/SSO/RBAC enforcement.
- Real external model, vector, search, parser, converter, or storage execution.

## Open Questions

- Whether publish should initially support bulk operations.
- Whether chunk-level review endpoints should be first-class in the first implementation pass.
