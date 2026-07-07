# Design: manual-url-knowledge-ingest

## Overview

The implementation adds a manual URL source domain around existing Atlas metadata flows. Registration validates and sanitizes a URL, stores a `manual_url_source`, and creates review-required batch/file/chunk metadata so the URL appears in existing operational surfaces without becoming trusted content.

## Backend Design

| Module | Design |
|---|---|
| `ManualUrlSource` domain | Immutable-style JPA entity with factory method and no public setters. |
| `ManualUrlSourceRepository` | Space-scoped list and id lookup. |
| `ManualUrlSourceService` | Owns validation, sanitization, duplicate prevention, metadata creation, and response mapping. |
| `ManualUrlSourceController` | Exposes create/list/get endpoints under `/api`. |
| DTOs | Request and response records use existing `ApiEnvelope<T>`. |

## Validation Design

- Parse with `java.net.URI`.
- Require `https`.
- Reject userinfo, query, and fragment.
- Reject missing host, `localhost`, internal suffixes, private IPv4 ranges, loopback, link-local, and unique-local IPv6.
- Build `displayUrl` from scheme, host, optional port, and bounded path only.
- Return `RequestValidationException` with field-level messages that do not echo raw unsafe URLs.

## Metadata Creation Design

On successful registration:

1. Create `manual_url_source`.
2. Create `Batch` with `SourceKind.url`.
3. Create `FileItem` with `SourceType.url`, `FileStatus.REVIEW_REQUIRED`, `ReviewStatus.REVIEW_REQUIRED`, and confidence `0.300`.
4. Create a `SourceChunk` with source file equal to safe display URL, section `Manual URL metadata`, and `ReviewStatus.REVIEW_REQUIRED`.
5. Link the manual source to `batchId` and `fileItemId`.

This design satisfies `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` and `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006`.

## Frontend Design

| Area | Design |
|---|---|
| Types | Add `ManualUrlSource`, status unions, and create request types. |
| API client | Add list/create/get helpers using existing envelope and safe error handling. |
| Mock fallback | Add sample-safe URL source with `REVIEW_REQUIRED` status only. |
| UI | Add compact form and status list in the Documents/Processing Center area of `App.vue`. |
| Tests | Cover rendering, submit path, validation-safe error state, and E2E smoke. |

## Error Handling

Backend validation failures use existing safe error infrastructure. The frontend shows generic safe messages and field summaries, not raw rejected URLs.

## Testing Considerations

- `T-MANUAL-URL-KNOWLEDGE-INGEST-001` through `T-MANUAL-URL-KNOWLEDGE-INGEST-008` define executable implementation order.
- Backend tests cover service validation and API contract behavior.
- Frontend tests cover rendering and state mapping.
- E2E covers a manual URL registration happy path with a sample-safe URL.

## Risks And Tradeoffs

- The slice stores metadata only, so users may expect content fetch. UI copy and `fetchPolicy` must state metadata-only.
- The existing review queue is file-item based; URL sources enter it through review-required file metadata rather than a new queue type.

## Acceptance IDs

`AC-MANUAL-URL-KNOWLEDGE-INGEST-001`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-002`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-003`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-004`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-005`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-006`.
