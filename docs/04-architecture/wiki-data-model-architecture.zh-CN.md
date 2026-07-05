# 系统架构：Wiki 数据模型

## 状态

待用户接受的草案。由 `docs/03-spec/wiki-data-model-spec.zh-CN.md` 派生。

## 概述

- **架构摘要：** 本切片用 Wiki Foundation 实体和读取契约扩展现有 Atlas metadata control plane。Wiki metadata 仍位于 Spring Boot/PostgreSQL 路径；parser、converter、model、vector、storage、search 执行不属于本切片，并继续位于现有 adapters 后面。
- **设计目标：** 为未来 Auto Wiki 提供稳定数据底座，同时保留现有 review-publish、graph、Ask 和 Vue Wiki 行为。
- **架构风格：** 分层 Spring Boot metadata service、Vue API-backed UI、Flyway 管理 PostgreSQL schema、DTO 边界和 adapter 隔离。

## 来源规格

- **Feature name:** Wiki Data Model
- **Scope summary:** 增加 Wiki page metadata 字段和最小 folder/run/log/issue records；暴露读取 APIs；Vue 展示可见 metadata；不实现 ingest、linkify、lint、生产 RBAC 或 provider 执行。

## 架构驱动

### 关键功能驱动

- 为未来 generated Wiki pages 提供 space-scoped slug lookup。
- Additive `wiki_page` extension，同时保留当前 publish/list/id-detail APIs。
- 最小 folders、generation runs、logs、issues，作为未来 Auto Wiki slices 的持久基础。
- Vue Wiki tab 展示新的 API-backed metadata，并保留 sample-safe fallback。

### 关键非功能驱动

- 保留 source trace、confidence、review status。
- Migration 兼容 V1-V9 seeded data 和现有 E2E。
- 避免 raw secrets、raw document content、provider payloads、private paths 和 external cloud calls。
- 基础设施 engines 继续位于 Atlas adapter boundaries 后面。

### 约束和假设

- 已验证现有基线：`ReviewPublishController` 已暴露 `POST /api/files/{fileId}/publish`、`GET /api/spaces/{spaceId}/wiki-pages`、`GET /api/wiki-pages/{wikiPageId}`。
- 已验证现有基线：`WikiPage` 当前存储 published metadata，但缺少 slug、page type、更丰富 refs、link refs、version、source mode、refresh policy。
- [ASSUMPTION] 初始 folder、generation run、log、issue 写行为可以限制在 repository/service 支持和 sample-safe seed/tests；公开 API 范围为最小读取。
- 本次 SDD 期间 `WEKNORA_ANALYSIS_DIR` 未设置，因此未使用 WeKnora 分析文件。

## 系统上下文

| Actor/System | Role |
|---|---|
| Vue Wiki tab | 从 Atlas API 消费 Wiki page/folder/run/log/issue metadata。 |
| Review Publish service | 从 approved files 创建或重新发布 `wiki_page` records。 |
| Graph and Ask surfaces | 继续消费 trusted source trace 和 published/approved evidence。 |
| PostgreSQL/Flyway | 负责 Wiki metadata 的 additive schema evolution。 |
| Adapter layer | 仍是 parser/converter/model/vector/storage/search engines 的边界；本切片不调用。 |

## 高层架构

```text
┌──────────────────────────────────────────────────────────────┐
│  Users                                                       │
│  Knowledge User · SME Reviewer · Delivery Lead               │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / local dev
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Vue Product UI                                              │
│  Wiki tab · Processing Center · Graph · Ask fallback safety   │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON ApiEnvelope
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Atlas Metadata API                                          │
│  Review Publish API · Wiki Metadata API                      │
├──────────────────────────────────────────────────────────────┤
│  Wiki Domain Services                                        │
│  Page metadata · Folder reads · Run/log/issue reads          │
├──────────────────────────────────────────────────────────────┤
│  JPA Repositories · DTO Mappers · User-safe errors           │
└──────────────┬───────────────────────────────────────────────┘
               │ JDBC
               ▼
┌──────────────────────────────────────────────────────────────┐
│  PostgreSQL / Flyway                                         │
│  wiki_page extension · wiki_folder · run/log/issue tables    │
└──────────────────────────────────────────────────────────────┘

Parser / converter / model / vector / storage / search engines remain behind
existing Atlas adapters and are not called by this slice.
```

## 层级摘要

- **Presentation layer:** Vue 渲染 API-backed Wiki metadata，并保留确定性 sample fallback。
- **API layer:** Spring controllers 使用 `ApiEnvelope` 暴露 page list/detail/slug 和最小 folder/run/log/issue read contracts。
- **Domain layer:** Wiki services 校验 scope、保留兼容默认值，并将 entities 映射为 DTOs。
- **Persistence layer:** Flyway 添加 columns 和 tables；repositories 查询 space-scoped Wiki records。
- **Adapter boundary:** 不引入 parser、converter、model、vector、storage 或 search 执行。

## 组件拆分

### 前端组件

- **Wiki tab metadata view:** 展示 slug、type、aliases、refs、links、version、source mode、refresh policy。
- **Fallback Wiki sample provider:** API data 为空或不可用时，保持确定性和 sample-safe。
- **API client/types:** 增加 typed Wiki page、folder、run、log、issue shapes。

### 后端服务

- **Review Publish service:** 继续发布 approved files；为新增 `wiki_page` 字段提供默认值。
- **Wiki metadata service:** 按 id/list/slug 读取 pages，并读取 folders、generation runs、logs、issues。
- **DTO mappers:** 将 entities 转为安全 response records，不泄露 raw content。

### 持久化

- **`wiki_page`:** 扩展页面 metadata 和兼容字段。
- **`wiki_folder`:** 最小层级。
- **`wiki_generation_run`:** 未来 generation/refresh run metadata。
- **`wiki_log_entry`:** 安全生命周期事件日志。
- **`wiki_page_issue`:** 未来 issue/lint finding records。

## 数据架构

| Entity | Description | Key Attributes |
|---|---|---|
| WikiPage | Published 或未来 generated Wiki page metadata。 | slug, pageType, refs, links, version, sourceMode, refreshPolicy, reviewStatus |
| WikiFolder | Space-scoped Wiki grouping。 | spaceId, parentFolderId, slug, name |
| WikiGenerationRun | 未来 generation/refresh run metadata。 | status, sourceMode, refreshPolicy, counts, timestamps |
| WikiLogEntry | 安全生命周期事件。 | eventType, actor, message, safe metadata |
| WikiPageIssue | 未来维护 issue。 | issueType, severity, status, evidence refs |

## API / Interface 边界

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/spaces/{spaceId}/wiki-pages` | Vue, tests | 带新增字段的现有 page list。 |
| `GET /api/wiki-pages/{wikiPageId}` | Vue, tests | 带新增字段的现有 page detail。 |
| `GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}` | Future Wiki navigation | Space-scoped lookup。 |
| `GET /api/spaces/{spaceId}/wiki-folders` | Vue/future navigation | 最小 folder tree/list。 |
| `GET /api/spaces/{spaceId}/wiki-generation-runs` | Delivery lead/admin UI | 安全 run inspection。 |
| `GET /api/wiki-pages/{wikiPageId}/logs` | SME/admin UI | 安全 page lifecycle events。 |
| `GET /api/wiki-pages/{wikiPageId}/issues` | SME/admin UI | 安全 issue inspection。 |

## 安全 / 可靠性 / 可观测性

- 不实现生产 auth/RBAC；保持现有 trusted internal/local API 姿态，直到 security slice。
- 用户安全错误使用现有 global exception conventions。
- Logs/issues 只包含安全 metadata。
- Migration 必须 additive，并保留 seeded mock data。
- 完成报告不得声称 Auto Wiki ingest 或生产就绪。

## 风险 / 权衡

| # | Risk / Tradeoff | Notes |
|---|---|---|
| 1 | refs 使用 structured JSON 还是 arrays | JSON 提供未来灵活性；DTOs 必须保持 shape 稳定。 |
| 2 | 在完整 workflows 之前新增支撑表 | 有利于 foundation，但 docs 必须清楚说明 generation/lint engine 还不存在。 |
| 3 | Legacy pages 的 slug defaults | Defaults 必须 deterministic，并在每个 space 内 collision-safe。 |

## 待确认问题

- OQ-WIKI-DATA-MODEL-001: 未来 generated-page initial state。
- OQ-WIKI-DATA-MODEL-002: 未来 source freshness checksum shape。
- OQ-WIKI-DATA-MODEL-003: 未来 folder ordering model。
