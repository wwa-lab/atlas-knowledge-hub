# Wiki Ingest v0 API 实现指南

## 状态

已由用户接受。Implementation 可严格依据本 API guide、已接受 spec 和任务清单推进。

## 概述

本文定义启动和查看 Auto Wiki ingest v0 runs 的 API contract。该 contract 归 backend 所有，使用 Atlas response envelopes，并保持 generated content review-required。

## Base Path

- Backend stack: Spring Boot。
- Envelope: `ApiEnvelope<T>`。
- Auth model: 当前项目安全 local/API path；production auth/RBAC 范围外。

## Endpoint Summary

| Operation | Method | Endpoint | Purpose |
|---|---|---|---|
| Start ingest run | POST | `/api/spaces/{spaceId}/wiki-ingest-runs` | 从 approved chunks 生成 review-required candidates。 |
| Get ingest run | GET | `/api/spaces/{spaceId}/wiki-generation-runs/{runId}` | 读取单个 safe run summary。 |
| List ingest runs | GET | `/api/spaces/{spaceId}/wiki-generation-runs` | 读取最近 run metadata。 |
| List Wiki pages with drafts | GET | `/api/spaces/{spaceId}/wiki-pages?includeDrafts=true` | 显式包含 generated review-required candidates。 |

## Request / Response Types

### `CreateWikiIngestRunRequest`

| Field | Type | Required | Rule |
|---|---|---:|---|
| `mode` | string | No | 默认 `deterministic`；v0 拒绝或禁用 `model-assisted`。 |
| `sourceFileIds` | string[] | No | 可选 filter；files 必须属于该 space。 |
| `requestedBy` | string | No | Safe actor label。 |
| `dryRun` | boolean | No | 默认 false；true 时返回 candidates 但不写 pages。 |

### `WikiIngestRunResponse`

| Field | Type | Description |
|---|---|---|
| `runId` | string | Generation run id。 |
| `spaceId` | string | Owning space。 |
| `status` | string | Run status。 |
| `mode` | string | v0 中为 `deterministic`。 |
| `createdPageIds` | string[] | Created candidate page ids。 |
| `updatedPageIds` | string[] | Updated candidate page ids。 |
| `issueIds` | string[] | Safe issue ids。 |
| `eligibleChunkCount` | number | 使用的 approved traced chunks。 |
| `excludedChunkCount` | number | 被 safety/review rules 排除的 chunks。 |
| `safeSummary` | string | Safe human-readable summary。 |
| `safeError` | string or null | Sanitized error。 |
| `startedAt` | string or null | ISO timestamp。 |
| `finishedAt` | string or null | ISO timestamp。 |

## Start Ingest Run

`POST /api/spaces/{spaceId}/wiki-ingest-runs`

Example request:

```json
{
  "mode": "deterministic",
  "sourceFileIds": ["file-001"],
  "requestedBy": "knowledge-manager",
  "dryRun": false
}
```

Example response:

```json
{
  "success": true,
  "data": {
    "runId": "wiki-ingest-run-001",
    "spaceId": "space-ibm-i-modernization",
    "status": "SUCCEEDED",
    "mode": "deterministic",
    "createdPageIds": ["wiki-auto-modernization-scope"],
    "updatedPageIds": [],
    "issueIds": [],
    "eligibleChunkCount": 3,
    "excludedChunkCount": 1,
    "safeSummary": "Created 1 review-required Wiki candidate from approved source chunks.",
    "safeError": null,
    "startedAt": "2026-07-05T00:00:00Z",
    "finishedAt": "2026-07-05T00:00:01Z"
  },
  "error": null,
  "meta": null
}
```

Validation:

- `spaceId` 必须存在。
- `mode=model-assisted` 不属于 v0 范围；如被请求，必须返回安全 validation response。
- `sourceFileIds` 如存在，必须属于 selected space。

Error cases:

| Status | Code | When |
|---|---|---|
| 404 | `NOT_FOUND` | Space 或 run 不存在。 |
| 409 | `CONFLICT` | 由于 trusted slug collision policy 阻止写入，run 无法继续。 |
| 422 | `VALIDATION_ERROR` | Invalid mode 或 cross-space source filter。 |
| 500 | `INTERNAL_ERROR` | Unexpected failure，只返回 sanitized message。 |

## Get Ingest Run

`GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}`

- 返回 `WikiIngestRunResponse`。
- 必须校验 run 属于 `spaceId`。
- 不得暴露 raw source text、prompts、provider payloads、secrets、private paths 或 stack traces。

## List Wiki Pages With Drafts

`GET /api/spaces/{spaceId}/wiki-pages?includeDrafts=true`

Decision：

- 现有 `GET /api/spaces/{spaceId}/wiki-pages` 默认保持 published-only。
- `includeDrafts=true` 包含 generated review-required candidates。
- Draft responses 必须包含 `sourceMode=AUTO_GENERATED` 和 `reviewStatus=REVIEW_REQUIRED`。

## Side Effects

- 写入 `wiki_generation_run`。
- 创建或更新 generated `wiki_page` candidates。
- 写入带确定性安全摘要的 generated Markdown artifacts。
- 追加 safe `wiki_log_entry`。
- 必要时为 conflicts 或 missing evidence 创建 `wiki_page_issue`。

## Testing Contracts

- Start run 排除 non-approved chunks。
- Generated candidates 为 review-required。
- Repeated run 按 slug 幂等。
- Trusted published pages 不被覆盖。
- Generated Markdown artifacts 只包含 safe summaries 与 source/chunk labels。
- Safe logs 不含 raw text、prompts、provider payloads、secrets、private paths 或 stack traces。
