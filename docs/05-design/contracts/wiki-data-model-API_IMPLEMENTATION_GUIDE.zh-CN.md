# Wiki 数据模型 — API 实现指南

## 状态

待用户接受的草案。后端/API 实现前必需。

## 基础契约

- Base path: `/api`
- Envelope: 现有 Atlas `ApiEnvelope<T>`，包含 `success`、`data`、`error`、`meta`。
- Auth model: 仅 trusted local/internal contract；生产 auth/RBAC 延后。
- Data safety: metadata-only responses；无 raw documents、prompts、provider payloads、secrets、private paths、SQL 或 stack traces。
- Adapter boundary: 本切片没有 endpoint 直接调用 parser、converter、model、vector、storage 或 search engines。

## Endpoint Summary

| Operation | Method | Endpoint | Status |
|---|---|---|---|
| Publish file | POST | `/api/files/{fileId}/publish` | 现有，response 扩展。 |
| List Wiki pages | GET | `/api/spaces/{spaceId}/wiki-pages` | 现有，response 扩展。 |
| Get Wiki page | GET | `/api/wiki-pages/{wikiPageId}` | 现有，response 扩展。 |
| Get Wiki page by slug | GET | `/api/spaces/{spaceId}/wiki-pages/by-slug/{slug}` | 新增。 |
| List Wiki folders | GET | `/api/spaces/{spaceId}/wiki-folders` | 新增。 |
| List generation runs | GET | `/api/spaces/{spaceId}/wiki-generation-runs` | 新增。 |
| List page logs | GET | `/api/wiki-pages/{wikiPageId}/logs` | 新增。 |
| List page issues | GET | `/api/wiki-pages/{wikiPageId}/issues` | 新增。 |

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

规则：

- `type` 是安全 label，例如 `FILE`、`SOURCE_CHUNK`、`WIKI_PAGE` 或 `GRAPH_NODE`。
- `id` 是稳定安全 identifier。
- `label` 和 `locator` 是可选安全展示字符串。

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

兼容性：

- 读取旧字段的现有 clients 必须继续工作。
- `sourceDocumentIds` 保持存在。
- 新 list fields 必须默认 empty arrays，而非 `null`。
- `version` 默认 `1`。

## 现有 Endpoint 扩展

### `POST /api/files/{fileId}/publish`

Request 保持：

```json
{
  "title": "Migration Boundary",
  "owner": "sme-team"
}
```

Response `201`：

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

验证保持 review-publish 原有逻辑，并为新增字段设置安全默认值。

### `GET /api/spaces/{spaceId}/wiki-pages`

返回 trusted read contract 下 published pages 的 extended `WikiPageResponse` 列表。

### `GET /api/wiki-pages/{wikiPageId}`

返回一个 extended `WikiPageResponse`。非 published pages 保持隐藏，除非未来 accepted slice 修改 trusted read contract。

## 新增 Endpoints

### `GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}`

Purpose: 在 Knowledge Space 范围内按 slug 读取一个 page。

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

- `404 NOT_FOUND`: unknown space、unknown slug，或 slug 只存在于另一个 space。

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

- `VALIDATION_ERROR`: invalid slug/enum/query。
- `NOT_FOUND`: space、page、folder、run、log 或 issue target 不存在。
- `CONFLICT`: future duplicate slug write 或 unsafe state conflict。
- `INTERNAL_ERROR`: 仅 sanitized fallback。

Error bodies 不得包含 stack traces、SQL、raw secrets、private absolute paths、raw source text、provider payloads 或 raw prompts。

## Contract Tests

- Publish approved file 返回 extended `WikiPageResponse` 并保留旧字段。
- List pages 返回 extended fields 和 legacy pages 的 defaults。
- Get page by id 返回 extended fields。
- Get page by slug 按 space scoped，且不泄露 cross-space pages。
- Folder list 只返回 requested space 的 folders。
- Generation run list 返回安全 metadata。
- Page logs list 返回安全 metadata，且没有 raw content。
- Page issues list 返回安全 issue metadata，且不执行 lint rules。
- Error tests 断言用户安全 envelopes。
