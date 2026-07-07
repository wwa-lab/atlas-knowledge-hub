# Implementation Tasks: graph-from-wiki-extraction

## Task Details

### T-GRAPH-FROM-WIKI-EXTRACTION-001: Extend graph projection contracts
- Owner: backend
- Scope: Add Wiki page descriptors and Wiki evidence fields to the adapter contract and DTOs.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-003, REQ-GRAPH-FROM-WIKI-EXTRACTION-004
- Verification: backend unit compile and focused tests.

### T-GRAPH-FROM-WIKI-EXTRACTION-002: Implement Wiki eligibility filtering
- Owner: backend
- Scope: Load all space Wiki pages, select `APPROVED`/`PUBLISHED` pages with trace and confidence >= 0.800, record safe skip reasons.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-001, REQ-GRAPH-FROM-WIKI-EXTRACTION-005
- Verification: unit/API tests for approved, published, review-required, low-confidence, and missing-trace pages.

### T-GRAPH-FROM-WIKI-EXTRACTION-003: Implement deterministic Wiki extraction
- Owner: backend
- Scope: Produce stable Wiki page, document, concept, and relationship candidates from eligible Wiki metadata.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-002, REQ-GRAPH-FROM-WIKI-EXTRACTION-003
- Verification: idempotency tests and stable ID assertions.

### T-GRAPH-FROM-WIKI-EXTRACTION-004: Persist Wiki-derived graph evidence
- Owner: backend
- Scope: Save nodes/edges with `evidence_wiki_page_ids`, preserve chunk IDs, and count created vs updated records.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-004, REQ-GRAPH-FROM-WIKI-EXTRACTION-006
- Verification: repository/API contract tests.

### T-GRAPH-FROM-WIKI-EXTRACTION-005: Extend graph detail evidence response
- Owner: backend
- Scope: Return safe mixed `SOURCE_CHUNK` and `WIKI_PAGE` evidence references.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-005, REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- Verification: API contract test asserts safe evidence and no raw unsafe strings.

### T-GRAPH-FROM-WIKI-EXTRACTION-006: Render Wiki-derived evidence in Vue Graph UI
- Owner: frontend
- Scope: Extend TypeScript evidence type and Graph detail panel rendering.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- Verification: `cd frontend && npm run typecheck`, component tests.

### T-GRAPH-FROM-WIKI-EXTRACTION-007: Add E2E coverage
- Owner: QA/frontend
- Scope: Cover Graph tab evidence detail showing Wiki-derived evidence while existing filters still work.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-006, REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- Verification: `cd frontend && npm run test`, `cd frontend && npm run build`, E2E.

### T-GRAPH-FROM-WIKI-EXTRACTION-008: Closeout and docs
- Owner: full-stack
- Scope: Update traceability, roadmaps, status evidence, and run all verification.
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-008
- Verification: `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`; `npm run agent:closeout`; `git diff --check`.

## Sequencing

Critical path: T-GRAPH-FROM-WIKI-EXTRACTION-001 -> T-GRAPH-FROM-WIKI-EXTRACTION-002 -> T-GRAPH-FROM-WIKI-EXTRACTION-003 -> T-GRAPH-FROM-WIKI-EXTRACTION-004 -> T-GRAPH-FROM-WIKI-EXTRACTION-005 -> T-GRAPH-FROM-WIKI-EXTRACTION-006 -> T-GRAPH-FROM-WIKI-EXTRACTION-007 -> T-GRAPH-FROM-WIKI-EXTRACTION-008.
