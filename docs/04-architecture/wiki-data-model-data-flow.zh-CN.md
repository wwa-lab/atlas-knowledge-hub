# 数据流：Wiki 数据模型

## 状态

待用户接受的草案。

## 概述

本文描述 `wiki-data-model` 切片中的 Wiki metadata 流转。所有流程均为 metadata-only 且 mock/sample-safe；本切片不执行 raw document ingestion、linkification、linting、model generation 或 external provider call。

## Flow 1: Existing Publish To Extended Wiki Page

```text
Approved file metadata
  │
  │ POST /api/files/{fileId}/publish
  ▼
Review Publish Service
  │ validates approved status, markdown path, confidence, source chunks
  │ preserves source_document_ids compatibility
  ▼
WikiPage entity
  │ sets defaults:
  │ slug, pageType, aliases, refs, links, version, sourceMode, refreshPolicy
  ▼
wiki_page table
  │
  ▼
WikiPageResponse with legacy + new fields
```

字段映射：

| Source | Target | Rule |
|---|---|---|
| `FileItem.id` | `sourceDocumentIds[0]` | 保留现有兼容行为。 |
| `FileItem.markdownPath` | `markdownPath`, default slug source | 必须保持安全相对路径。 |
| `SourceChunk.id` | `chunkRefs[]` | 只存安全 chunk IDs。 |
| Publish title | `title` | 现有 request field。 |
| Publish owner | `owner` | 现有 request field。 |
| Existing confidence | `confidence` | 保留值；不重新计算。 |
| Publish operation | `sourceMode=PUBLISHED_FILE` | review-publish 创建页面的默认值。 |

## Flow 2: Space-Scoped Wiki Reads

```text
Vue Wiki tab or API test
  │ GET /api/spaces/{spaceId}/wiki-pages
  │ GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}
  ▼
Wiki Metadata API
  │ validates space scope
  ▼
Wiki Metadata Service
  │ queries by space/status or space/slug
  ▼
DTO Mapper
  │ returns safe refs, links, version, source mode, refresh policy
  ▼
ApiEnvelope response
```

错误路径：

| Case | Result |
|---|---|
| Unknown space | `NOT_FOUND` safe envelope。 |
| Known space 中 slug 不存在 | `NOT_FOUND` safe envelope。 |
| Slug 存在于另一个 space | 对当前 space 返回 `NOT_FOUND`。 |
| Trusted read contract 下的非 published page | 隐藏，除非未来 accepted slice 显式支持。 |

## Flow 3: Folder Reads

```text
GET /api/spaces/{spaceId}/wiki-folders
  ▼
WikiFolderRepository by space
  ▼
Service builds parent-child DTO ordering
  ▼
Folder list/tree response
```

规则：

- Folder read 按 `spaceId` scoped。
- `parentFolderId` 可为 null。
- 没有 folder 的 pages 仍可读取。
- 不引入 permission inheritance 或 folder write workflow。

## Flow 4: Generation Runs, Logs, And Issues

```text
Delivery lead / tests
  │
  ├─ GET /api/spaces/{spaceId}/wiki-generation-runs
  ├─ GET /api/wiki-pages/{wikiPageId}/logs
  └─ GET /api/wiki-pages/{wikiPageId}/issues
        ▼
Wiki Metadata Service
        │ reads bounded safe records
        ▼
ApiEnvelope response
```

安全过滤：

- 返回 IDs、enum values、counts、timestamps、safe summaries、evidence IDs。
- 不返回 raw source text、raw prompts、provider payloads、stack traces、private paths 或 secrets。
- 读取 issues 时不执行 rule engine。

## Flow 5: Vue Display With Fallback

```text
Space detail load
  │ calls listWikiPages(spaceId)
  ▼
wikiPages state
  ├─ non-empty API pages -> map to product Wiki pages with new metadata
  └─ empty/failure -> use deterministic sample-safe fallback pages
  ▼
Wiki tab rendering
  │ shows slug, type, aliases, refs, link counts, version, source mode, policy
  ▼
Graph/Ask flows continue using existing approved/published evidence
```

回归敏感行为：

- 现有 `data-testid="wiki-pages"` 和 API-backed Wiki/Graph/Ask E2E 期望必须继续通过，或按 accepted tasks 更新。
- Fallback data 不得包含真实公司内容、private paths、raw secrets 或 provider logs。

## 状态转换

Generation runs 为未来 workflows 存储：

```text
REQUESTED -> RUNNING -> SUCCEEDED
REQUESTED -> RUNNING -> PARTIAL_FAILED
REQUESTED -> RUNNING -> FAILED
REQUESTED -> CANCELLED
```

Issue lifecycle：

```text
OPEN -> ACKNOWLEDGED -> RESOLVED
OPEN -> IGNORED
ACKNOWLEDGED -> RESOLVED
```

本切片读取并存储这些状态；不通过后台 workflow 评估或转换它们。
