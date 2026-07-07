# User Stories: manual-url-knowledge-ingest

## Story Map

### `US-MANUAL-URL-KNOWLEDGE-INGEST-001` Register A Manual URL Source

As a Knowledge Space contributor, I want to register a public URL manually, so that Atlas can track it as a potential knowledge source without starting an uncontrolled crawl.

**Acceptance Criteria**

1. Given a Knowledge Space and a safe HTTPS sample URL, when I submit the manual URL form, then Atlas creates a URL source record and returns `AC-MANUAL-URL-KNOWLEDGE-INGEST-001`.
2. Given a URL with credentials, unsafe scheme, private host, query string, or fragment, when I submit it, then Atlas rejects it with `AC-MANUAL-URL-KNOWLEDGE-INGEST-004`.
3. Given a registered URL source, when I view the source list, then I can see safe display URL, host, status, review status, and source trace per `AC-MANUAL-URL-KNOWLEDGE-INGEST-002`.

**Dependencies:** Existing Knowledge Space API, safe error envelopes, auth guard, rate-limit guard.
**Out of Scope:** Real URL fetch, connector sync, crawl, browser automation.

### `US-MANUAL-URL-KNOWLEDGE-INGEST-002` Preserve Trace And Review Governance

As an SME reviewer, I want URL-derived metadata to remain review-required and traceable, so that unverified URL material cannot become trusted knowledge.

**Acceptance Criteria**

1. Given a registered URL source, when Atlas creates supporting batch/file/chunk metadata, then those artifacts preserve the URL source trace per `AC-MANUAL-URL-KNOWLEDGE-INGEST-003`.
2. Given any URL-derived artifact, when it reaches Processing Center, then review status remains `REVIEW_REQUIRED`.
3. Given Wiki/Ask/Graph flows, when URL-derived metadata has not been approved, then it is not exposed as approved downstream knowledge.

**Dependencies:** Existing batch, file item, source chunk, review queue, Wiki ingest and publish contracts.
**Out of Scope:** New review state machine or production legal/compliance review.

### `US-MANUAL-URL-KNOWLEDGE-INGEST-003` Show Safe URL Ingest Status

As a Knowledge Space operator, I want to see manual URL ingest status in the product surface, so that I can decide whether review work is pending.

**Acceptance Criteria**

1. Given the frontend loads a Knowledge Space, when manual URL sources exist, then the UI shows their ingest status, review status, eligibility, and source trace per `AC-MANUAL-URL-KNOWLEDGE-INGEST-005`.
2. Given the API is unavailable, when the frontend falls back to mock data, then it does not invent approved URL content.
3. Given unsafe URL validation fails, when the UI shows feedback, then it uses safe error text without raw secrets or private endpoint values.

**Dependencies:** Existing `frontend/src/api.ts`, `frontend/src/types.ts`, product shell state, and E2E fixture patterns.
**Out of Scope:** Styling overhaul, new navigation model, production connector admin.

### `US-MANUAL-URL-KNOWLEDGE-INGEST-004` Verify No Regressions

As an Atlas maintainer, I want focused backend, frontend, and E2E tests, so that URL source registration does not regress existing ingest and review flows.

**Acceptance Criteria**

1. Given backend tests run, when URL validation and registration paths are exercised, then validation, redaction, source trace, status mapping, and review-required behavior are covered per `AC-MANUAL-URL-KNOWLEDGE-INGEST-006`.
2. Given frontend tests run, when manual URL state renders, then status and trace are visible without exposing unsafe values.
3. Given full verification runs, when existing commands complete, then file/folder ingest, Wiki, Ask, Graph, and review/publish flows do not regress.

**Dependencies:** Maven, Vite/Vitest, Playwright fixtures, workflow closeout gate.
**Out of Scope:** Provider-backed tests or real network calls.
