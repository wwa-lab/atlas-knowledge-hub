# 系统架构：Wiki Ingest v0

## 状态

供用户接受的草稿。

## 概述

- **Architecture summary:** `wiki-ingest-v0` 增加 backend-owned Auto Wiki generation workflow，消费 approved source chunks，并将 review-required Wiki candidates 写入现有 Wiki data model。设计围绕 Atlas APIs、domain services、repositories 与 adapter boundaries 分层。
- **Design objective:** 生成可追溯、幂等、需审核的 Wiki candidates，同时不削弱现有 published Wiki、Graph 或 Ask trust boundaries。
- **Architectural style:** Layered Spring Boot control-plane workflow，v0 使用 deterministic generation，并记录 future ModelAdapter boundary。

## Source Specification

- **Feature name:** Wiki Ingest v0
- **Scope summary:** Space-scoped ingest run、approved chunk selection、deterministic candidate generation、slug merge、run/log/issue evidence 和 status display。

## Architectural Drivers

### Key Functional Drivers

- 只从 approved source chunks 生成 candidates。
- 保留 source trace、chunk refs、confidence 和 review status。
- 按 space-scoped slug 幂等合并。
- 保持 generated output review-required。
- 保留当前 published Wiki behavior。

### Key Non-Functional Drivers

- 不暴露 raw secret、prompt、provider payload、stack trace、private path 或 raw source text。
- v0 执行使用确定性本地逻辑，无外部 cloud calls。
- Future model assistance 必须使用 ModelAdapter，且不属于 v0 implementation 范围。

### Constraints and Assumptions

- `wiki-data-model` 已完成，并提供 Wiki page/run/log/issue support。
- 现有 PDF ingestion 生成 review-required chunks；本 slice 从 approved chunks 开始，而不是处理 parser runtime。
- [ASSUMPTION] v0 candidate text 可从 source chunk metadata 与已批准安全处理的 excerpts 中确定性生成。

## System Context

### Primary Actors

| Actor | Role |
|---|---|
| Knowledge manager | 启动 ingest runs。 |
| SME reviewer | 审核 candidates 与 evidence。 |
| Knowledge user | 阅读 published Wiki，并在暴露时看到 generated draft state。 |

### External Systems

| System | Integration Purpose |
|---|---|
| ModelAdapter | 未来 model-assisted candidate text generation；v0 不调用。 |
| Parser/converter adapters | 本 slice 范围外；现有 chunks 是输入。 |

### System Boundary

范围内：API contracts、backend ingest service、candidate generation policy、Wiki page/run/log/issue persistence、tests 与可选 UI status display。范围外：parser runtime、linkify/lint、review approval workflow、production auth/RBAC、connectors、external provider setup 和 operations hardening。

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge Manager · SME Reviewer · Knowledge User            │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue Product Surface                                          │
│ Wiki tab · Processing Center status · generated draft labels │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot API                                              │
│ Wiki ingest run endpoints · Wiki page/run read endpoints     │
├──────────────────────────────────────────────────────────────┤
│ Wiki Ingest Application Service                              │
│ input selection · deterministic candidates · slug merge logs │
├──────────────────────────────────────────────────────────────┤
│ Domain / Repositories                                        │
│ SourceChunk · WikiPage · WikiGenerationRun · WikiLogEntry    │
└──────────────┬──────────────────────────────┬────────────────┘
               │ JDBC / Flyway-managed schema │ future adapter call
               ▼                              ▼
┌─────────────────────────┐      ┌─────────────────────────────┐
│ PostgreSQL metadata     │      │ ModelAdapter boundary        │
│ atlas wiki/source tables│      │ future only; not used in v0  │
└─────────────────────────┘      └─────────────────────────────┘
```

## Layer Summary

- **Presentation layer:** 展示 generated/review-required status 与现有 published Wiki pages。
- **API layer:** 启动 ingest runs 并暴露 safe run/candidate metadata。
- **Application layer:** 选择 inputs、创建 candidates、按 slug 合并，并记录 run evidence。
- **Domain/persistence layer:** 存储 Wiki pages、runs、logs、issues 与 source chunk references。
- **Adapter layer:** 记录 future model assistance 必须使用 ModelAdapter；v0 不调用 model provider。

## Component Breakdown

### Frontend Components

- **Wiki tab status display:** 如果实现暴露 drafts，则展示 generated candidates 为 review-required。
- **Processing Center ingest status:** 加入 UI 时展示 safe run status/counts。

### Backend Services

- **Wiki ingest service:** 管理 run lifecycle、input selection、candidate generation 与 idempotent merge。
- **Review-publish service compatibility path:** 现有 published-page behavior 保持不变。
- **Model adapter client boundary:** 未来边界；deterministic mode 是 v0 唯一行为。

### Persistence Modules

- **WikiPage:** 存储 generated candidates，并包含 source refs、chunk refs、review status 和 source mode。
- **WikiGenerationRun:** 存储 safe run summary。
- **WikiLogEntry:** 存储 safe lifecycle events。
- **WikiPageIssue:** 存储 conflicts 和 missing-evidence issues。

## Data Architecture

| Entity | Description | Key Attributes |
|---|---|---|
| SourceChunk | Approved evidence input。 | id, file item id, page/section, confidence, review status |
| WikiPage | Generated candidate 或 published page。 | slug, page type, source refs, chunk refs, source mode, review status |
| WikiGenerationRun | Ingest execution record。 | status, input refs, created/updated page ids, issue ids, safe summary |
| WikiLogEntry | Safe lifecycle log。 | event type, page id, run id, safe message |
| WikiPageIssue | Conflict 或 data quality issue。 | issue type, severity, status, evidence refs |

## Workflow / Runtime Architecture

### Request Flow

1. 用户启动 space-scoped ingest run。
2. API 校验 space 与 requested mode。
3. Ingest service 加载该 space 的 approved、traceable source chunks。
4. Service 将 chunks 分组为 deterministic `TOPIC` candidates，并派生 slugs。
5. Service 按 slug 创建或合并 review-required candidates。
6. Service 记录 run/log/issue metadata。
7. API 返回 safe run summary。
8. UI 展示 generated/review-required status，且不宣称 trusted-publish。

### Failure and Retry Handling

- No eligible chunks 返回 successful safe run，candidate 数为 0。
- Partial candidate failures 产生 `PARTIAL_FAILED` 与 safe summaries。
- 重复 retries 使用 slug-based idempotency。

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `POST /api/spaces/{spaceId}/wiki-ingest-runs` | Vue 或 API client | 启动 v0 ingest run。 |
| `GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}` | Vue 或 API client | 读取 safe run summary。 |
| Existing Wiki reads | Vue product | 保留 published-page behavior。 |
| ModelAdapter | Backend service | 未来 configured model assistance only；v0 不使用。 |

## Security / Reliability / Observability

- Generated pages 为 `REVIEW_REQUIRED`。
- Logs 与 errors 被清洗。
- 默认不新增外部调用。
- Idempotency 基于 `(spaceId, slug)`。
- Existing trusted pages 不被 generated candidates 覆盖。

## Risks / Tradeoffs

| Risk | Notes |
|---|---|
| Deterministic mode 的 candidate quality 偏薄 | 因为 review-required 状态明确，v0 可接受。 |
| Draft pages 被误认为 trusted Wiki | UI 与 API 必须清晰标注 generated/review-required。 |
| Slug collision with trusted pages | 保留 trusted page 并记录 safe issue。 |

## 已解决决策

1. v0 只创建 `TOPIC` pages。
2. v0 写入带确定性安全摘要的 generated Markdown artifacts。
3. v0 使用 included chunk confidence 的最低值聚合 confidence。
