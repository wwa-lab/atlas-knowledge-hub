# Wiki Data Model — API Implementation Guide

## Status

Draft for user acceptance. Required before backend/API implementation.

## Base Contract

- Base path: `/api`
- Envelope: existing Atlas `ApiEnvelope<T>` with `success`, `data`, `error`, and `meta`.
- Auth model: trusted local/internal contract only; production auth/RBAC is deferred.
- Data safety: metadata-only responses; no raw documents, prompts, provider payloads, secrets, private paths, SQL, or stack traces.
- Adapter boundary: no endpoint in this slice calls parser, converter, model, vector, storage, or search engines directly.

## Endpoint Summary

| Operation | Method | Endpoint | Status |
|---|---|---|---|
| Publish file | POST | `/api/files/{fileId}/publish` | Existing, response extended. |
| List Wiki pages | GET | `/api/spaces/{spaceId}/wiki-pages` | Existing, response extended. |
| Get Wiki page | GET | `/api/wiki-pages/{wikiPageId}` | Existing, response extended. |
| Get Wiki page by slug | GET | `/api/spaces/{spaceId}/wiki-pages/by-slug/{slug}` | New. |
| List Wiki folders | GET | `/api/spaces/{spaceId}/wiki-folders` | New. |
| List generation runs | GET | `/api/spaces/{spaceId}/wiki-generation-runs` | New. |
| List page logs | GET | `/api/wiki-pages/{wikiPageId}/logs` | New. |
| List page issues | GET | `/api/wiki-pages/{wikiPageId}/issues` | New. |

## Shared DTOs

### `WikiReferenceResponse`

```json
{
  "type": "SOURCE_CHUNK",
  "id": "chunk-file-001-p12-b02",
  "label": "BRD methodology page 12",
  "locator": "page 12 / section 3"
}
```

Rules:

- `type` is a safe label such as `FILE`, `SOURCE_CHUNK`, `WIKI_PAGE`, or `GRAPH_NODE`.
- `id` is a stable safe identifier.
- `label` and `locator` are optional safe display strings.

### Extended `WikiPageResponse`

```json
{
  "id": "wiki-file-003",
  "spaceId": "ibm-i-modernization",
  "folderId": null,
  "title": "Migration Boundary",
  "slug": "migration-boundary",
  "pageType": "SOURCE_SUMMARY",
  "markdownPath": "generated/md/migration-boundary.md",
  "sourceDocumentIds": ["file-003"],
  "aliases": ["Boundary Overview"],
  "sourceRefs": [
    { "type": "FILE", "id": "file-003", "label": "Migration Boundary", "locator": "generated/md/migration-boundary.md" }
  ],
  "chunkRefs": [
    { "type": "SOURCE_CHUNK", "id": "chunk-file-003-p01-b01", "label": "source chunk", "locator": "page 1" }
  ],
  "inLinks": [],
  "outLinks": ["modernization-index"],
  "version": 1,
  "sourceMode": "PUBLISHED_FILE",
  "refreshPolicy": "MANUAL",
  "confidence": 0.96,
  "reviewStatus": "PUBLISHED",
  "owner": "sme-team",
  "lastUpdated": "2026-07-05T00:00:00Z"
}
```

Compatibility:

- Existing clients reading the old fields must continue to work.
- `sourceDocumentIds` remains present.
- New list fields must default to empty arrays, not `null`.
- `version` defaults to `1`.

## Existing Endpoint Extensions

### `POST /api/files/{fileId}/publish`

Request remains:

```json
{
  "title": "Migration Boundary",
  "owner": "sme-team"
}
```

Response `201`:

```json
{
  "success": true,
  "data": {
    "id": "wiki-file-003",
    "spaceId": "ibm-i-modernization",
    "folderId": null,
    "title": "Migration Boundary",
    "slug": "migration-boundary",
    "pageType": "SOURCE_SUMMARY",
    "markdownPath": "generated/md/migration-boundary.md",
    "sourceDocumentIds": ["file-003"],
    "aliases": [],
    "sourceRefs": [{ "type": "FILE", "id": "file-003", "label": "file-003", "locator": "generated/md/migration-boundary.md" }],
    "chunkRefs": [{ "type": "SOURCE_CHUNK", "id": "chunk-file-003-p01-b01", "label": "source chunk", "locator": "page 1" }],
    "inLinks": [],
    "outLinks": [],
    "version": 1,
    "sourceMode": "PUBLISHED_FILE",
    "refreshPolicy": "MANUAL",
    "confidence": 0.96,
    "reviewStatus": "PUBLISHED",
    "owner": "sme-team",
    "lastUpdated": "2026-07-05T00:00:00Z"
  },
  "error": null,
  "meta": null
}
```

Validation remains from review-publish and adds safe defaults for new fields.

### `GET /api/spaces/{spaceId}/wiki-pages`

Returns a list of extended `WikiPageResponse` records for published pages under the trusted read contract.

### `GET /api/wiki-pages/{wikiPageId}`

Returns one extended `WikiPageResponse`. Non-published pages remain hidden unless a future accepted slice changes the trusted read contract.

## New Endpoints

### `GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}`

Purpose: read one page by slug scoped to a Knowledge Space.

Response:

```json
{
  "success": true,
  "data": { "...": "Extended WikiPageResponse" },
  "error": null,
  "meta": null
}
```

Errors:

- `404 NOT_FOUND`: unknown space, unknown slug, or slug exists only in another space.

### `GET /api/spaces/{spaceId}/wiki-folders`

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "wiki-folder-foundation",
      "spaceId": "ibm-i-modernization",
      "parentFolderId": null,
      "slug": "foundation",
      "name": "Foundation",
      "description": "Sample-safe Wiki foundation pages",
      "sortOrder": 10
    }
  ],
  "error": null,
  "meta": null
}
```

### `GET /api/spaces/{spaceId}/wiki-generation-runs`

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "wiki-run-sample-001",
      "spaceId": "ibm-i-modernization",
      "pageId": "wiki-modernization-overview",
      "status": "SUCCEEDED",
      "sourceMode": "PUBLISHED_FILE",
      "refreshPolicy": "MANUAL",
      "requestedBy": "system-sample",
      "createdPageIds": [],
      "updatedPageIds": ["wiki-modernization-overview"],
      "issueIds": [],
      "safeSummary": "Sample-safe metadata refresh recorded.",
      "safeError": null,
      "startedAt": "2026-07-05T00:00:00Z",
      "finishedAt": "2026-07-05T00:00:03Z"
    }
  ],
  "error": null,
  "meta": null
}
```

### `GET /api/wiki-pages/{wikiPageId}/logs`

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "wiki-log-001",
      "spaceId": "ibm-i-modernization",
      "pageId": "wiki-modernization-overview",
      "runId": null,
      "eventType": "PUBLISHED",
      "actor": "sme-team",
      "message": "Published safe Wiki metadata.",
      "metadata": { "sourceMode": "PUBLISHED_FILE" },
      "createdAt": "2026-07-05T00:00:00Z"
    }
  ],
  "error": null,
  "meta": null
}
```

### `GET /api/wiki-pages/{wikiPageId}/issues`

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": "wiki-issue-001",
      "spaceId": "ibm-i-modernization",
      "pageId": "wiki-modernization-overview",
      "issueType": "MISSING_SOURCE_REF",
      "severity": "LOW",
      "status": "OPEN",
      "evidenceRefs": [{ "type": "WIKI_PAGE", "id": "wiki-modernization-overview", "label": "page", "locator": null }],
      "message": "Sample-safe issue placeholder for future lint workflows.",
      "createdAt": "2026-07-05T00:00:00Z",
      "resolvedAt": null
    }
  ],
  "error": null,
  "meta": null
}
```

## Error Response Expectations

- `VALIDATION_ERROR`: invalid slug/enum/query.
- `NOT_FOUND`: space, page, folder, run, log, or issue target not found.
- `CONFLICT`: future duplicate slug write or unsafe state conflict.
- `INTERNAL_ERROR`: sanitized fallback only.

Error bodies must not include stack traces, SQL, raw secrets, private absolute paths, raw source text, provider payloads, or raw prompts.

## Contract Tests

- Publish approved file returns extended `WikiPageResponse` and preserves old fields.
- List pages returns extended fields and defaults for legacy pages.
- Get page by id returns extended fields.
- Get page by slug is space-scoped and does not leak cross-space pages.
- Folder list returns only folders for the requested space.
- Generation run list returns safe metadata.
- Page logs list returns safe metadata and no raw content.
- Page issues list returns safe issue metadata and does not execute lint rules.
- Error tests assert user-safe envelopes.
