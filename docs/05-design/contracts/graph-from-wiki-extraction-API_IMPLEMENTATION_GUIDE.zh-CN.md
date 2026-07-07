# graph-from-wiki-extraction API 实现指南

## Existing Endpoints

| Operation | Method | Path | Change |
|---|---|---|---|
| Create projection run | POST | `/api/spaces/{spaceId}/graph/projection-runs` | Request 不变；extraction source 变为 eligible Wiki pages。 |
| Query graph | GET | `/api/spaces/{spaceId}/graph` | Response shape 不变，新增 Wiki-derived nodes/edges。 |
| Node detail | GET | `/api/spaces/{spaceId}/graph/nodes/{nodeId}` | Evidence references 增加 `referenceType`、`wikiPageId` 和 `label`。 |

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

- REQ-GRAPH-FROM-WIKI-EXTRACTION-005：Errors 使用现有 safe envelope。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006：Authorization 和 rate-limit behavior 不变。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-001：Skipped pages 通过 projection items 报告，不作为 raw errors。

## Verification

- T-GRAPH-FROM-WIKI-EXTRACTION-005：API contract tests 断言 mixed evidence。
- T-GRAPH-FROM-WIKI-EXTRACTION-008：`cd backend && mvn verify`。
