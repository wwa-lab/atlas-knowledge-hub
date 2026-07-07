# Specification: manual-url-knowledge-ingest

## Scope

`manual-url-knowledge-ingest` adds a metadata-only manual URL source registration path. It creates a safe URL source record and review-required metadata artifacts that can be shown in Knowledge Space / Processing Center / Wiki surfaces. It does not fetch remote content.

## Actors

- Knowledge Space contributor registering a URL.
- SME reviewer reviewing URL-derived metadata.
- Atlas maintainer verifying no unsafe data or downstream approval bypass.

## Functional Requirements

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` The system shall expose a manual URL registration action for a selected Knowledge Space.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` The system shall persist URL source metadata including `sourceId`, `spaceId`, `displayUrl`, `host`, `title`, `description`, `fetchIntent`, `fetchPolicy`, `ingestStatus`, `reviewStatus`, `confidence`, `eligibilityStatus`, `sourceTrace`, `batchId`, and `fileItemId`.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003` The system shall validate URLs before persistence and reject unsafe input with field-specific validation errors.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` The system shall not perform external HTTP calls or retrieve page content during registration.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` The system shall create metadata artifacts mapped to existing batch/file/source-chunk models with `REVIEW_REQUIRED` review status.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` The system shall preserve source trace from URL source to metadata artifacts.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` The system shall redact unsafe URL components and never return raw credentials, query strings, fragments, cookies, or private endpoint values.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` The system shall be additive and not change existing upload, review, Wiki, Ask, Graph, auth, rate-limit, or safe-error semantics.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` The frontend shall show registration controls, source status, review-required output, and source trace.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` Backend, frontend, and E2E tests shall cover the accepted behavior.

## URL Validation Rules

The accepted rule is intentionally conservative for `AC-MANUAL-URL-KNOWLEDGE-INGEST-004`.

| Input aspect | Rule |
|---|---|
| Scheme | Only `https` is accepted. |
| Userinfo | Any username/password component is rejected. |
| Query/fragment | Rejected; not stored or echoed. |
| Host | Required; normalized to lower-case. |
| Local/private host | `localhost`, `.local`, `.internal`, `.corp`, private IPv4 ranges, loopback, link-local, and unique-local IPv6 are rejected. |
| Display path | A short path may be stored; query and fragment are always stripped. |

Edge trace:

- `https://example.com/reference/page` -> accepted and displayed as `https://example.com/reference/page`.
- `http://example.com` -> rejected as unsupported scheme.
- `https://user:pass@example.com/a` -> rejected as credential-bearing URL.
- `https://localhost/wiki` -> rejected as private/internal-looking host.

## State Mapping

| Manual URL status | Meaning | Downstream mapping |
|---|---|---|
| `REGISTERED` | Metadata saved and no fetch has occurred. | Batch/file metadata exists with review-required status. |
| `FETCH_INTENT_RECORDED` | User intent to fetch later is recorded. | No real fetch is executed in this slice. |
| `REVIEW_REQUIRED` | URL-derived output requires SME review. | Processing Center can show review-required item. |
| `REJECTED` | Validation failed before persistence. | No source record created. |

## UI Behavior

- Knowledge Space Documents or Processing Center surface includes a compact manual URL ingest form.
- The form accepts URL, optional title, optional description, and fetch intent.
- After successful registration, the UI shows safe display URL, host, ingest status, review status, confidence, eligibility status, and source trace.
- The UI must not render URL query, fragment, credential userinfo, cookies, or private endpoint values.
- Mock-safe fallback data may show sample URLs only.

## API Behavior

- `POST /api/spaces/{spaceId}/manual-url-sources` registers one URL source.
- `GET /api/spaces/{spaceId}/manual-url-sources` lists registered URL sources for a Knowledge Space.
- `GET /api/manual-url-sources/{sourceId}` returns one source.
- Responses use `ApiEnvelope<T>`.
- Validation errors use existing safe error envelopes.

## Acceptance Matrix

| Acceptance | Requirements | Tests |
|---|---|---|
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` | API contract + frontend submit test |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` | API contract field assertions |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` | Service/integration tests |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` | Validation/redaction tests |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-005` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` | Vitest + Playwright |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` | Full verification commands |

## Non-Functional Requirements

- No external network dependency.
- No raw URL secrets or private endpoints in responses, logs, tests, or docs.
- Existing safe errors and rate limits remain active.
- API contract is additive.

## Open Questions

None for this slice. Future connector-sync-v0 will define real fetch, robots/compliance, connector credentials, and scheduled refresh semantics.
