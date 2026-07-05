# Wiki 自动互链与质量检查 API 实现指南

## 状态

供用户审阅的草稿。后端实现前必需。

## 基础契约

- Backend stack：Spring Boot。
- Envelope：`ApiEnvelope<T>`。
- Auth model：沿用当前 local/API path；生产 auth/RBAC 仍在范围外。
- Data safety：只返回安全 metadata。

## Endpoints

| Operation | Method | Endpoint |
|---|---|---|
| Start linkify/lint run | POST | `/api/spaces/{spaceId}/wiki-linkify-lint-runs` |
| Get run | GET | `/api/spaces/{spaceId}/wiki-generation-runs/{runId}` |
| List space issues | GET | `/api/spaces/{spaceId}/wiki-page-issues` |
| List page issues | GET | `/api/wiki-pages/{wikiPageId}/issues` |
| Review queues | GET | `/api/spaces/{spaceId}/review-queues` |

## `CreateWikiLinkifyLintRunRequest`

| Field | Type | Required | Rule |
|---|---|---:|---|
| `pageIds` | string[] | No | 可选 page scope；每个 page 必须属于 `spaceId`。 |
| `requestedBy` | string | No | 安全 actor label。 |
| `dryRun` | boolean | No | 默认 false；若为 true，只返回 summary，不写 pages 或 issues。 |
| `linkify` | boolean | No | 默认 true。 |
| `lint` | boolean | No | 默认 true。 |

## `WikiLinkifyLintRunResponse`

| Field | Type | Description |
|---|---|---|
| `runId` | string | Run id。 |
| `spaceId` | string | 所属空间。 |
| `status` | string | `SUCCEEDED`、`PARTIAL_FAILED` 或 `FAILED`。 |
| `mode` | string | `linkify-lint`。 |
| `scannedPageCount` | number | 已评估页面数。 |
| `updatedPageIds` | string[] | 已变化页面。 |
| `issueIds` | string[] | 已打开或刷新的 issues。 |
| `insertedLinkCount` | number | 已插入链接数。 |
| `brokenLinkCount` | number | Broken link issue 数。 |
| `orphanPageCount` | number | Orphan issue 数。 |
| `sourceIssueCount` | number | Missing 或 stale source issue 数。 |
| `thinContentCount` | number | Thin content issue 数。 |
| `safeSummary` | string | 只含安全计数的 summary。 |
| `safeError` | string or null | 脱敏 error。 |
| `startedAt` | string or null | ISO timestamp。 |
| `finishedAt` | string or null | ISO timestamp。 |

## 示例请求

```json
{
  "pageIds": ["wiki-auto-modernization-scope"],
  "requestedBy": "knowledge-manager",
  "dryRun": false,
  "linkify": true,
  "lint": true
}
```

## 示例响应

```json
{
  "success": true,
  "data": {
    "runId": "wiki-linkify-lint-run-001",
    "spaceId": "space-ibm-i-modernization",
    "status": "SUCCEEDED",
    "mode": "linkify-lint",
    "scannedPageCount": 4,
    "updatedPageIds": ["wiki-auto-modernization-scope"],
    "issueIds": ["wiki-issue-broken-link-001"],
    "insertedLinkCount": 3,
    "brokenLinkCount": 1,
    "orphanPageCount": 0,
    "sourceIssueCount": 0,
    "thinContentCount": 0,
    "safeSummary": "Scanned 4 Wiki pages, inserted 3 links, and recorded 1 issue.",
    "safeError": null,
    "startedAt": "2026-07-05T00:00:00Z",
    "finishedAt": "2026-07-05T00:00:01Z"
  },
  "error": null,
  "meta": null
}
```

## Validation

- `spaceId` 必须存在。
- `pageIds` 存在时必须全部属于所选空间。
- `linkify` 或 `lint` 至少一个为 true。
- Unknown run ids 和 cross-space run ids 返回安全 not found responses。

## Side Effects

- 变化时更新 `wiki_page.in_links`、`wiki_page.out_links`、`version`、`last_updated`。
- linkify 启用时，可能通过安全 storage 重写 Markdown artifacts。
- 写入 mode 为 `linkify-lint` 的 `wiki_generation_run`。
- 追加安全 `wiki_log_entry` events。
- 打开、刷新、解决或替换安全 `wiki_page_issue` records。

## Testing Contract

- Contract tests 覆盖 start run、cross-space validation、dry run、safe run read。
- Service tests 覆盖 protected Markdown regions、idempotency、broken links、orphan pages、missing refs、stale refs、thin content。
- Response assertions 确认不返回 raw Markdown body、secret、private path、prompt、provider payload 或 stack trace。
