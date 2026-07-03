# Review Publish — API 实现指南

## 状态

草稿。本切片引入新 endpoints，因此实现前必须接受该指南。

## 基础契约

- Base path：`/api`
- Envelope：沿用现有 `ApiEnvelope` shape。
- Auth model：本切片仅 internal/trusted；production RBAC 延后。
- Errors：用户安全的 `VALIDATION_ERROR`、`NOT_FOUND`、`CONFLICT`、`INTERNAL_ERROR`。
- 任何 endpoint 都不调用 parser、converter、model、vector、storage 或 search engines。

## Endpoint 摘要

| Operation | Method | Endpoint | Purpose |
|---|---|---|---|
| Review queues | GET | `/api/spaces/{spaceId}/review-queues` | 返回 blocked queues 和 publish-ready candidates。 |
| Append file review | POST | `/api/files/{fileId}/reviews` | 已有 review history endpoint；纳入本契约。 |
| List file reviews | GET | `/api/files/{fileId}/reviews` | 已有 chronological history endpoint。 |
| Publish file | POST | `/api/files/{fileId}/publish` | 将合格 approved Markdown 发布到 Wiki metadata。 |
| List Wiki pages | GET | `/api/spaces/{spaceId}/wiki-pages` | 返回 space 的 published Wiki metadata。 |
| Get Wiki page | GET | `/api/wiki-pages/{wikiPageId}` | 返回单个 published Wiki metadata record。 |

## `GET /api/spaces/{spaceId}/review-queues`

Response：

```json
{
  "success": true,
  "data": {
    "spaceId": "ibm-i-modernization",
    "queues": [
      {
        "type": "MISSING_SOURCE_TRACE",
        "count": 94,
        "publishBlocked": true,
        "representativeItems": [
          {
            "fileId": "file-missing-trace-001",
            "status": "MARKDOWN_GENERATED",
            "reviewStatus": "APPROVED",
            "confidence": 0.88,
            "hasSourceTrace": false
          }
        ]
      },
      {
        "type": "READY_TO_PUBLISH",
        "count": 12579,
        "publishBlocked": false,
        "representativeItems": [
          {
            "fileId": "file-ready-001",
            "status": "MARKDOWN_GENERATED",
            "reviewStatus": "APPROVED",
            "confidence": 0.96,
            "hasSourceTrace": true
          }
        ]
      }
    ]
  },
  "error": null,
  "meta": null
}
```

Representative items 是用于分诊的有界示例。它们只能使用安全 metadata fields，不得包含 raw source paths、raw document content、private absolute paths、stack traces 或 secrets。

## `POST /api/files/{fileId}/publish`

Request：

```json
{
  "title": "Migration Boundary",
  "owner": "sme-team"
}
```

Validation：

- File 存在。
- File review status 为 `APPROVED`。
- File 有相对 `markdownPath`。
- File 有 confidence。
- 可派生至少一个 source document id。
- 至少存在一个 source trace/source chunk。

Response `201` 或 idempotent `200`：

```json
{
  "success": true,
  "data": {
    "id": "wiki-file-003",
    "spaceId": "ibm-i-modernization",
    "title": "Migration Boundary",
    "markdownPath": "generated/md/Migration_Boundary.md",
    "sourceDocumentIds": ["file-003"],
    "confidence": 0.96,
    "reviewStatus": "PUBLISHED",
    "owner": "sme-team",
    "lastUpdated": "2026-07-03T00:00:00Z"
  },
  "error": null,
  "meta": null
}
```

Errors：

- `400 VALIDATION_ERROR`：缺少 title/owner 或 path 非法。
- `404 NOT_FOUND`：file 或 space 不存在。
- `409 CONFLICT`：候选项未批准、缺少 trace、缺少 confidence、OCR required、need fix、parser failed 或 unsupported。

## `GET /api/spaces/{spaceId}/wiki-pages`

仅返回 published 或本切片 trusted read contract 明确允许的 Wiki pages。

## `GET /api/wiki-pages/{wikiPageId}`

返回单个 Wiki page metadata record。不得返回 raw document content 或 raw secrets。

## Contract Tests

- `mvn verify` 必须包含 publish API contract tests。
- Review queue tests 断言 category counts 和 representative item shape。
- 成功测试断言 envelope、status、持久化 `PUBLISHED` 和保留字段。
- 失败测试断言 missing trace、not approved、missing markdown path、missing confidence、failed/unsupported file、unknown id 时无 mutation。
- 安全测试断言 errors 不出现 stack trace、SQL、secret、private absolute path 或 raw confidential content。
