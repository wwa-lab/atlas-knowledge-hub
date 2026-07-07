# Requirements: manual-url-knowledge-ingest

## Slice Contract

- **Slice:** `manual-url-knowledge-ingest`
- **Wave:** Wave 5 / Connector And Operations
- **Goal:** Allow a user to manually register a URL as a knowledge source and route safe URL metadata, fetch intent, source trace, ingest status, and review-required output into existing Knowledge Space, Wiki, Processing Center, and review flows.
- **Maturity:** Prototype metadata/API-backed foundation. This is not connector sync, crawling, headless rendering, browser automation, production compliance, or production connector readiness.

## In Scope

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` A Knowledge Space user can register one manual URL source from a product UI entry point.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` The backend persists safe URL metadata, normalized display URL, source trace, fetch intent, fetch policy, ingest status, review status, confidence, and eligibility metadata.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003` URL validation rejects unsafe schemes, credentials in URL userinfo, query/fragment storage, localhost, private IPs, and internal-looking hostnames.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` Manual URL ingest is metadata-only in this slice: no real external HTTP fetch, crawl, scheduled sync, browser automation, or connector provider call is performed.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` Registered URL sources create or expose review-required artifacts that can appear in Processing Center and Wiki generation metadata without becoming approved Wiki, Ask, or Graph knowledge.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` Source trace is preserved from URL source to batch/file/chunk metadata and any review-required generated artifact.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` API responses and frontend states never expose credentials, cookies, tokens, private endpoints, raw stack traces, raw provider payloads, or confidential source content.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` Existing file/folder ingest, Wiki, Ask, Graph, review/publish, auth guard, safe error, and rate limit behavior must not regress.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` The frontend shows a manual URL ingest entry point, status summary, source trace, and review-required output using API-backed data with mock-safe fallback patterns.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` Tests prove URL validation, status mapping, source trace preservation, redaction, and review-required behavior.

## Out of Scope

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-011` Connector sync v0, recursive crawl, scheduled crawl, sitemap crawl, browser automation, headless rendering, and full web crawling are excluded.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-012` Production legal/compliance review, robots policy engine, copyright policy, DLP scan, malware scan, connector secret management, and production connector authentication are excluded.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-013` Auth/RBAC/audit/secret/rate-limit/provider/migration semantics must not be changed beyond additive metadata and API behavior described by this slice.

## Acceptance Criteria

- `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` A safe public HTTPS sample URL can be registered for a Knowledge Space.
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` The source record contains safe URL metadata, source trace, ingest status, review status, confidence, and eligibility metadata.
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` Any derived batch/file/chunk or Wiki ingest metadata remains `REVIEW_REQUIRED` until explicitly reviewed through existing flows.
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` Unsafe URLs produce user-safe validation errors without leaking the raw unsafe value.
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-005` Processing Center or Knowledge Space UI can show manual URL ingest status and source trace.
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` Existing verification commands pass.

## Constraints

- No external network calls.
- Mock/sample URLs only.
- No raw credentials, cookies, query strings, fragments, private endpoints, private paths, or confidential content.
- Adapter boundaries remain intact.
- Review-required status is mandatory for URL-derived artifacts in this slice.
