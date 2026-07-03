# API Implementation Guide: Metadata API

## Status

Draft. **Required and must be accepted before backend implementation** (REQ-PROD-073). Phase 2. Slice `metadata-api`. Formalizes the candidate endpoints sketched in `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md`.

## Principles

- One response envelope for every endpoint (success and error).
- Preserve source trace, confidence, and review status in all content responses.
- User-safe errors only — no stack traces, SQL, secrets, hostnames, or absolute paths.
- Relative paths only in all responses.
- Metadata-only: no endpoint triggers conversion, parsing, storage, model, or vector work.
- No auth in this slice (internal-only); do not expose publicly as-is.

## Conventions

- Base path: `/api`. JSON only (`application/json`).
- Pagination: `?page=<0-based>&size=<1..200, default 20>`. List responses include `meta`.
- Filtering: `?status=<enum>` on file lists; `?status=<space_status>` on space lists.
- IDs are opaque strings. Timestamps are ISO-8601 UTC (`timestamptz`).

## Envelope

```json
{ "success": true, "data": {}, "error": null, "meta": { "page": 0, "size": 20, "total": 42 } }
```

Error:

```json
{ "success": false, "data": null,
  "error": { "code": "VALIDATION_ERROR", "message": "Invalid request.",
             "fields": { "name": "must not be blank" },
             "timestamp": 1750000000000, "path": "/api/spaces" },
  "meta": null }
```

`code` ∈ `VALIDATION_ERROR` (400) · `NOT_FOUND` (404) · `CONFLICT` (409) · `INTERNAL_ERROR` (500). `timestamp` (epoch millis) and `path` are present on error responses only (mirrors Spring's `DefaultErrorAttributes`); success responses omit them.

## Endpoints

### Knowledge Spaces

#### `GET /api/spaces`
List space cards. Query: `page`, `size`, `status?`.
```json
{ "success": true,
  "data": [ { "id": "ibm-i-modernization", "name": "IBM i Modernization",
              "description": "…", "owner": "Platform Team", "status": "HEALTHY",
              "documentCount": 42, "wikiPageCount": 12, "reviewCount": 3,
              "createdAt": "2026-06-01T09:00:00Z", "updatedAt": "2026-06-20T14:30:00Z" } ],
  "error": null, "meta": { "page": 0, "size": 20, "total": 5 } }
```

#### `GET /api/spaces/{spaceId}`
Space detail. `404 NOT_FOUND` if unknown. `data` is a single space object (as above).

#### `POST /api/spaces`
Create a space.
Request:
```json
{ "name": "IBM i Modernization", "description": "…",
  "type": "document", "indexStrategy": "rag", "owner": "Platform Team" }
```
Validation: `name` non-blank ≤200; `type` ∈ {`document`,`faq`}; `indexStrategy` ∈ {`rag`,`wiki`}.
Response `201`: envelope with the created space (server-set `id`, `status=HEALTHY`, `createdAt`, `updatedAt`).
Errors: `400 VALIDATION_ERROR`.

### Batches

#### `GET /api/spaces/{spaceId}/batches`
List a space's batches with **derived** metrics. Query: `page`, `size`.
```json
{ "success": true,
  "data": [ { "id": "batch-2026-06-20-001", "spaceId": "ibm-i-modernization",
              "name": "Discovery Package", "sourceKind": "folder",
              "owner": "Delivery Lead", "uploadedAt": "2026-06-20T14:30:00Z",
              "metrics": { "total": 24, "pdfConverted": 20, "markdownGenerated": 18,
                           "reviewRequired": 6, "failed": 1, "unsupported": 2 } } ],
  "error": null, "meta": { "page": 0, "size": 20, "total": 3 } }
```

#### `GET /api/batches/{batchId}`
Batch detail with derived metrics. `404` if unknown.

#### `POST /api/spaces/{spaceId}/batches`
Create a batch + file items from **pre-computed inventory metadata**. No bytes/engines touched.
Request:
```json
{ "name": "Discovery Package", "sourceKind": "folder", "owner": "Delivery Lead",
  "files": [
    { "sourcePath": "Discovery/BRD/BRD.docx", "sourceType": "docx",
      "status": "MARKDOWN_GENERATED", "confidence": 0.82, "reviewStatus": "REVIEW_REQUIRED",
      "chunks": [ { "sourceFile": "BRD.docx", "page": 12, "section": "Scope",
                    "confidence": 0.82, "reviewStatus": "REVIEW_REQUIRED" } ] },
    { "sourcePath": "Discovery/notes.txt", "sourceType": "unsupported",
      "status": "UNSUPPORTED", "confidence": 0, "reviewStatus": "REVIEW_REQUIRED" }
  ] }
```
Validation: `sourceKind` ∈ {`folder`,`zip`}; each file `sourcePath` relative & traversal-free; `status` ∈ `FileStatus`; `confidence` ∈ [0,1]. Generated/low-confidence files default `reviewStatus=REVIEW_REQUIRED`; the server rejects `APPROVED` on create.
Response `201`: created batch with derived metrics. Errors: `400 VALIDATION_ERROR`, `404` (unknown space).

### File Items

#### `GET /api/batches/{batchId}/files`
List file items. Query: `page`, `size`, `status?`.
```json
{ "success": true,
  "data": [ { "id": "file-003", "batchId": "batch-2026-06-20-001",
              "sourcePath": "Discovery/BRD/BRD.docx", "sourceType": "docx",
              "status": "MARKDOWN_GENERATED", "confidence": 0.82,
              "reviewStatus": "REVIEW_REQUIRED",
              "pdfPath": "generated/pdf/BRD.pdf", "markdownPath": "generated/md/BRD.md",
              "assetsPath": "generated/assets/BRD/", "errorMessage": null } ],
  "error": null, "meta": { "page": 0, "size": 20, "total": 24 } }
```

#### `GET /api/files/{fileId}`
File-item detail. `404` if unknown.

#### `GET /api/files/{fileId}/chunks`
Source chunks (trace records) for a file.
```json
{ "success": true,
  "data": [ { "id": "chunk-file-003-p12-b02", "fileItemId": "file-003",
              "sourceFile": "BRD.docx", "page": 12, "section": "Scope",
              "confidence": 0.82, "reviewStatus": "REVIEW_REQUIRED" } ],
  "error": null, "meta": null }
```

### Reviews

#### `POST /api/files/{fileId}/reviews`
Append an immutable review record; update the target's `reviewStatus`.
Request:
```json
{ "action": "NEED_FIX", "reviewer": "sme.alex",
  "comment": "Terminology mismatch in section 3.",
  "affectedChunks": ["chunk-file-003-p12-b02"] }
```
Validation: `action` ∈ {`APPROVE`,`NEED_FIX`,`OCR_REQUIRED`}; `reviewer` non-blank.
Effect: `APPROVE→APPROVED`, `NEED_FIX→NEED_FIX`, `OCR_REQUIRED→OCR_REQUIRED`. `PUBLISHED` is never set here.
Response `201`: the created review record with server `createdAt`.

#### `GET /api/files/{fileId}/reviews`
Append-only history, chronological.
```json
{ "success": true,
  "data": [ { "id": 1001, "targetType": "file", "targetId": "file-003",
              "action": "NEED_FIX", "reviewer": "sme.alex",
              "comment": "…", "affectedChunks": ["chunk-file-003-p12-b02"],
              "createdAt": "2026-06-21T10:15:00Z" } ],
  "error": null, "meta": null }
```

## Deferred Endpoints (out of scope — REQ-MA-014)

Tables exist and are seeded, but **no endpoints** are exposed this slice:

- Wiki pages (`review-publish` slice) — `GET /api/spaces/{id}/wiki-pages`, publish transitions.
- Graph (`knowledge-graph` slice) — `GET /api/spaces/{id}/graph`.
- Ask (`ask-rag` slice) — `POST /api/spaces/{id}/ask`.
- Model/engine/member/registration management — their own later slices.

## Contract Test Expectations

Every endpoint has a contract test asserting: correct status code; envelope shape (`success`/`data`/`error`/`meta`); pagination `meta` on lists; `400` field-level validation for bad input; `404` for unknown ids; user-safe error bodies (no stack/SQL/secret/absolute path); relative paths in all responses; generated content never returned as `APPROVED` by default. Run under `mvn verify`.

## Security & Secret Handling

- Datasource credentials come from externalized config (`${ATLAS_DB_*}`), never committed literals.
- No endpoint returns raw secrets; any future key/config field is status-only (`configured`/`not_configured`).
- No auth in this slice — internal-only; RBAC deferred to Phase 4. Do not deploy publicly as-is.
