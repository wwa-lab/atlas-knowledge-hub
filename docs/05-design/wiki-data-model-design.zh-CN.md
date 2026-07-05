# 详细设计：Wiki 数据模型

## 状态

待用户接受的草案。接受前产品代码仍被阻塞。

## 概述

本设计将 `wiki-data-model` 规格转化为可实现的模块、API、数据、UI、验证和测试决策。它保持 metadata foundation 范围，不实现 Auto Wiki ingest、linkify/lint、生产 RBAC 或 provider 执行。

## 来源架构

- `docs/03-spec/wiki-data-model-spec.zh-CN.md`
- `docs/04-architecture/wiki-data-model-architecture.zh-CN.md`
- `docs/04-architecture/wiki-data-model-data-flow.zh-CN.md`
- `docs/04-architecture/wiki-data-model-data-model.zh-CN.md`

## 设计假设

- 现有 review-publish endpoints 和 Vue API-backed Wiki flow 是兼容性基线。
- Flyway migration 是唯一 schema mutation path。
- DTOs 使用 immutable response records 和安全 collection copies。
- JSON reference fields 在 API responses 中表示为小型 DTO record 列表，而不是 raw JSON strings。
- 初始实现可以只暴露 read-only folder/run/log/issue APIs；Auto Wiki 写 workflows 延后。

## 设计范围

范围内：

- Additive migration 和 domain/repository/DTO/API changes。
- 向后兼容的 publish/list/id-detail 行为。
- 新 slug lookup、folder list、generation run list、page logs、page issues 读取。
- Vue type/client 更新和 Wiki tab metadata 展示。
- Contract/unit/E2E 回归覆盖和安全扫描。

范围外：

- Ingest pipeline、linkify/lint engine、refresh worker、folder editing UI、生产 auth/RBAC、external providers、真实公司文档。

## 模块设计

### Backend Wiki Domain

- 用 data model 中的字段扩展 `WikiPage`。
- 新增最小实体 `WikiFolder`、`WikiGenerationRun`、`WikiLogEntry`、`WikiPageIssue`。
- Entities 均按 `spaceId` scoped。
- 对 publish-created Wiki pages 使用 factory methods/defaulting helpers：
  - slug 从 `markdownPath`、title 或 id 派生。
  - `pageType=SOURCE_SUMMARY`
  - `sourceMode=PUBLISHED_FILE`
  - `refreshPolicy=MANUAL`
  - `version=1`
  - aliases 和 links 为空 arrays/lists。

### Backend Repositories

- 扩展 `WikiPageRepository`，增加 space-scoped slug lookup。
- 增加 folders、generation runs、log entries、issues repositories。
- Queries 必须按 `spaceId` scoped，或先验证 page 属于请求的 space。

### Backend Service

- 扩展现有 review-publish service，或引入窄范围 Wiki metadata service 负责 read paths。
- 保留现有 publish behavior 和 response compatibility。
- Log/issue/run read methods 返回 bounded results 和 safe DTOs。
- Slug lookup 校验 space 存在；当 slug 属于另一个 space 时返回 not found。

### Backend DTOs And Mapping

- 扩展 `WikiPageResponse`：
  - `slug`
  - `pageType`
  - `aliases`
  - `sourceRefs`
  - `chunkRefs`
  - `inLinks`
  - `outLinks`
  - `version`
  - `sourceMode`
  - `refreshPolicy`
  - optional `folderId`
- 新增 response records：
  - `WikiReferenceResponse(type, id, label, locator)`
  - `WikiFolderResponse(id, spaceId, parentFolderId, slug, name, description, sortOrder)`
  - `WikiGenerationRunResponse(...)`
  - `WikiLogEntryResponse(...)`
  - `WikiPageIssueResponse(...)`
- Public response bodies 不暴露 raw JSON strings。

### Frontend API And UI

- 扩展 `ApiWikiPage`，加入全部新增 page metadata fields。
- 在 UI 需要时增加 folder/run/log/issue 读取 helpers。
- 更新 Wiki tab，渲染：
  - page title 附近的 slug 和 page type。
  - aliases 小标签或 fallback "none"。
  - source refs 和 chunk refs 的安全 IDs/labels。
  - in/out links 的 counts，以及集合较小时的 sample IDs。
  - version、source mode、refresh policy metadata badges。
- API pages 为空时保留 fallback sample pages。

## API / Interface Design

`docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.zh-CN.md` 是实现契约。摘要：

- 现有：
  - `POST /api/files/{fileId}/publish`
  - `GET /api/spaces/{spaceId}/wiki-pages`
  - `GET /api/wiki-pages/{wikiPageId}`
- 新增：
  - `GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}`
  - `GET /api/spaces/{spaceId}/wiki-folders`
  - `GET /api/spaces/{spaceId}/wiki-generation-runs`
  - `GET /api/wiki-pages/{wikiPageId}/logs`
  - `GET /api/wiki-pages/{wikiPageId}/issues`

## 数据设计

- 用户接受后使用 V9 之后的一个 additive migration，预期为 `V10__wiki_data_model.sql`，除非实现时已存在更新迁移。
- `wiki_page` 新 columns 必须为现有 rows 提供安全 defaults。
- JSON columns 存安全 metadata，并按需要默认 empty arrays/objects。
- 添加 `(space_id, slug)`、folder by space、run by space/time、log by page/time、issue by page/status indexes。

## UI / 用户流程设计

1. 用户进入 Knowledge Space。
2. Vue 通过现有流程加载 API Wiki pages。
3. 如果 API pages 存在，Wiki page list 和 detail 渲染新增 metadata。
4. 如果 API pages 为空或不可用，fallback sample-safe pages 渲染。
5. Graph 和 Ask tabs 继续使用现有 API-backed evidence flows。

空状态：

- 无 API Wiki pages：展示现有安全 empty/fallback state。
- 无 aliases/links/refs：展示简短 "none" 或 count zero，而不是缺失 UI。

## 验证和错误处理

| Input/Field | Rule |
|---|---|
| `slug` | 小写 URL-safe segment；每个 space 内唯一；不能 slash traversal。 |
| `pageType` | 必须是 spec enum values 之一。 |
| `sourceMode` | 必须是 spec enum values 之一。 |
| `refreshPolicy` | 必须是 spec enum values 之一。 |
| `version` | Integer >= 1。 |
| JSON refs | 只允许安全 IDs/labels；不含 raw document text 或 private paths。 |
| Folder parent | Parent 必须在同一 space。 |

错误行为：

- Unknown resource -> safe `NOT_FOUND`。
- Duplicate slug -> future writes 使用 safe `CONFLICT`；migration 必须避免 duplicates。
- Invalid enum/path -> safe `VALIDATION_ERROR`。

## 测试考虑

- 后端 API contract tests：
  - page response new fields。
  - slug lookup scoped by space。
  - folder list。
  - generation run list。
  - page logs。
  - page issues。
  - existing publish/list/id-detail compatibility。
- 后端 repository/migration tests 覆盖 V1-V10 compatibility 和 defaults。
- 前端 unit tests 覆盖 API page mapping 和 fallback。
- Playwright 检查 Wiki metadata display 和现有 review-publish/graph/Ask flows。
- Safety scans 覆盖 secrets/private paths 和 external network/provider calls。

## 风险 / 设计权衡

| Risk | Mitigation |
|---|---|
| 大 JSON 字段成为不清晰契约 | Public DTOs 使用 typed reference records。 |
| 支撑表让人误以为 workflows 已完成 | UI/docs 标注这是 foundation metadata only。 |
| Slug backfill collisions | Migration 必须按 space 使用 deterministic conflict suffixing。 |

## 待确认问题

- OQ-WIKI-DATA-MODEL-001: 未来 generated-page initial status。
- OQ-WIKI-DATA-MODEL-002: 未来 checksum/freshness metadata。
- OQ-WIKI-DATA-MODEL-003: 未来 folder ordering behavior。
