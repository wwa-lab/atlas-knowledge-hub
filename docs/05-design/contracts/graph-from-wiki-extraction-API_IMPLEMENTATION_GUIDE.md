# graph-from-wiki-extraction API Implementation Guide

## Existing Endpoints

| Operation | Method | Path | Change |
|---|---|---|---|
| Create projection run | POST | `/api/spaces/{spaceId}/graph/projection-runs` | Same request; extraction source becomes eligible Wiki pages. |
| Query graph | GET | `/api/spaces/{spaceId}/graph` | Same response shape plus Wiki-derived nodes/edges. |
| Node detail | GET | `/api/spaces/{spaceId}/graph/nodes/{nodeId}` | Evidence references add `referenceType`, `wikiPageId`, and `label`. |

## Request

```json
{
  "scope": "APPROVED_ONLY",
  "adapterId": "deterministic",
  "dryRun": false
}
```

## Evidence Response

```json
{
  "referenceType": "WIKI_PAGE",
  "sourceChunkId": null,
  "wikiPageId": "wiki-modernization-index",
  "label": "Modernization Index",
  "sourceFile": null,
  "page": null,
  "section": "source trace",
  "confidence": 0.93,
  "reviewStatus": "PUBLISHED"
}
```

## Error Rules

- REQ-GRAPH-FROM-WIKI-EXTRACTION-005: Errors use the existing safe envelope.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006: Authorization and rate-limit behavior are unchanged.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-001: Skipped pages are reported through projection items, not raw errors.

## Verification

- T-GRAPH-FROM-WIKI-EXTRACTION-005: API contract tests assert mixed evidence.
- T-GRAPH-FROM-WIKI-EXTRACTION-008: `cd backend && mvn verify`.
