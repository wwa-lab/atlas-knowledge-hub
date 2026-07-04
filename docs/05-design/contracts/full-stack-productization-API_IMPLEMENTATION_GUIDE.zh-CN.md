# 全栈产品化 — API 实现指南

## 状态

实现草案。本指南将现有 Atlas APIs 组合为一个 P0 浏览器工作流。

## 基础契约

- Base path: `/api`。
- Envelope: `ApiEnvelope<T>`，包含 `success`、`data`、`error`、`meta`。
- Auth: 生产 auth/RBAC 不在范围内。Graph endpoints 使用现有本地 headers。
- Data: 仅 mock/sample metadata。

## Endpoint Sequence

| Step | Method | Endpoint | Purpose |
|---|---|---|---|
| 1 | GET | `/api/spaces` | 列出 Knowledge Spaces。 |
| 2 | GET | `/api/spaces/{spaceId}` | 加载 selected space detail。 |
| 3 | GET | `/api/spaces/{spaceId}/batches` | 加载 existing batches。 |
| 4 | POST | `/api/spaces/{spaceId}/batches` | 创建 metadata-only sample batch。 |
| 5 | GET | `/api/batches/{batchId}/files` | 加载 batch files。 |
| 6 | GET | `/api/files/{fileId}/chunks` | 加载 source trace chunks。 |
| 7 | GET | `/api/spaces/{spaceId}/review-queues` | 加载 review queues。 |
| 8 | POST | `/api/files/{fileId}/reviews` | 批准 selected file。 |
| 9 | POST | `/api/files/{fileId}/publish` | 将 approved file 发布到 Wiki。 |
| 10 | GET | `/api/spaces/{spaceId}/wiki-pages` | 加载 published Wiki pages。 |
| 11 | POST | `/api/spaces/{spaceId}/graph/projection-runs` | 刷新 graph evidence。 |
| 12 | POST | `/api/spaces/{spaceId}/vector-runs` | 为 Ask 索引 approved chunks。 |
| 13 | GET | `/api/spaces/{spaceId}/graph` | 加载 graph view。 |
| 14 | GET | `/api/spaces/{spaceId}/graph/nodes/{nodeId}` | 加载 graph detail evidence。 |
| 15 | POST | `/api/spaces/{spaceId}/ask` | 创建 trusted Ask run。 |
| 16 | GET | `/api/ask-runs/{runId}` | 读取最终 Ask answer/evidence。 |

## Sample Batch Request

```json
{
  "name": "P0 Productization Sample 20260703",
  "sourceKind": "folder",
  "owner": "P0 Browser E2E",
  "files": [
    {
      "sourcePath": "samples/p0/productization-sample.md",
      "sourceType": "pdf",
      "status": "MARKDOWN_GENERATED",
      "confidence": 0.93,
      "reviewStatus": "REVIEW_REQUIRED",
      "markdownPath": "generated/md/productization-sample.md",
      "chunks": [
        {
          "sourceFile": "productization-sample.md",
          "page": 1,
          "section": "P0 Browser Evidence",
          "confidence": 0.93,
          "reviewStatus": "APPROVED"
        }
      ]
    }
  ]
}
```

## Review Request

```json
{
  "action": "APPROVE",
  "reviewer": "p0-browser-e2e",
  "comment": "Approved for P0 browser loop.",
  "affectedChunks": ["chunk-id"]
}
```

## Publish Request

```json
{
  "title": "P0 Productization Wiki",
  "owner": "p0-browser-e2e"
}
```

## Graph Headers

Graph read:

```text
X-Atlas-User: frontend-demo
X-Atlas-Role: VIEWER
```

Graph projection:

```text
X-Atlas-User: frontend-demo
X-Atlas-Role: ADMIN
```

## Ask Request

```json
{
  "question": "What evidence was published for the P0 browser flow?",
  "requestedBy": "p0-browser-e2e",
  "reviewPolicy": "APPROVED_ONLY",
  "limit": 3,
  "mode": "mock",
  "filters": {
    "fileItemIds": ["file-id"],
    "sourceTypes": ["pdf"]
  }
}
```

## Error Handling

- `400 VALIDATION_ERROR`：request body 或 unsafe fields 无效。
- `404 NOT_FOUND`：space、batch、file、wiki page、graph node 或 Ask run 不存在。
- `409 CONFLICT`：publish 早于 review approval 或 source trace readiness。
- `401/403`：graph headers 缺失或权限不足。
- `500 INTERNAL_ERROR`：仅安全 server fault；无 stack traces 或 private details。

## Contract Tests

- 现有 backend contract tests 必须继续在 `mvn verify` 下通过。
- 新 frontend E2E 必须通过浏览器控件调用 endpoint sequence。
- 前端代码不得直接调用 provider/vector/parser/storage/converter engines。
