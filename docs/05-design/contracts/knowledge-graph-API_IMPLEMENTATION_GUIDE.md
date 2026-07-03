# Knowledge Graph API Implementation Guide

Date: 2026-07-03
Version: Draft
Base path: `/api`
Backend stack: Spring Boot metadata API
Auth model: Phase 4 backend-enforced authentication and authorization

## Envelope

All endpoints return:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {}
}
```

Errors must be user-safe and must not include stack traces, SQL, secrets, private absolute paths, raw prompts, raw vectors, or engine diagnostics.

## Endpoints

### GET `/api/spaces/{spaceId}/graph`

Returns a bounded graph view.

Query parameters:

- `q` optional search text.
- `nodeType` optional enum.
- `edgeType` optional enum.
- `reviewStatus` optional enum.
- `evidenceOnly` optional boolean, default `true`.
- `limit` optional integer, default `200`, max `500`.

Response data:

```json
{
  "spaceId": "ibm-i-modernization",
  "nodes": [
    {
      "id": "node-concept-rpgle",
      "label": "RPGLE",
      "type": "CONCEPT",
      "reviewStatus": "APPROVED",
      "confidence": 0.91,
      "evidenceCount": 4
    }
  ],
  "edges": [
    {
      "id": "edge-page-rpgle-defines",
      "sourceNodeId": "node-page-modernization-overview",
      "targetNodeId": "node-concept-rpgle",
      "type": "DEFINES",
      "reviewStatus": "APPROVED",
      "confidence": 0.88,
      "evidenceCount": 2
    }
  ],
  "counts": {
    "nodes": 172,
    "edges": 248,
    "excluded": 94
  }
}
```

### GET `/api/spaces/{spaceId}/graph/nodes/{nodeId}`

Returns selected node detail and adjacent evidence.

Response data includes node fields, adjacent nodes, adjacent edges, and `evidenceReferences` with `wikiPageId`, `sourceChunkId`, `sourceFile`, `page`, `section`, `confidence`, and `reviewStatus`.

### POST `/api/spaces/{spaceId}/graph/projection-runs`

Starts a deterministic or configured projection run for approved/published evidence.

Request:

```json
{
  "scope": "APPROVED_ONLY",
  "adapterId": "deterministic",
  "dryRun": false
}
```

Response data includes `runId`, `status`, `summary`, and safe message.

### GET `/api/graph/projection-runs/{runId}`

Returns run summary, item outcomes, safe exclusion reason codes, and timestamps.

### POST `/api/spaces/{spaceId}/graph/edges/{edgeId}/review-actions`

Records an SME review action for an edge.

Request:

```json
{
  "action": "APPROVE",
  "comment": "Relationship is supported by cited modernization overview."
}
```

Allowed actions: `APPROVE`, `NEED_FIX`, `OCR_REQUIRED`.

## Validation

- IDs must be non-empty stable identifiers.
- `limit` must be `1..500`.
- Enum values must match the data model.
- Review comments must be bounded and sanitized.
- Projection rejects unapproved, missing-source-trace, unsafe-path, and no-evidence candidates.

## Contract Tests

Run:

```bash
cd backend && mvn verify
```

Tests must cover success envelopes, validation errors, `401`/`403`, `404`, projection exclusions, evidence preservation, audit append-only behavior, and safe error bodies.
