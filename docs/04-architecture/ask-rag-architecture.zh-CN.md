# 架构：Ask RAG

## 状态

草稿。由 `docs/03-spec/ask-rag-spec.md` 通过 `spec-to-architecture` 推导。

## 概览

Ask RAG 是 full-stack Phase 4 hardening 切片。前端渲染 Trusted Ask 状态；后端校验限定范围的问题，通过 vector adapter 边界检索 evidence，通过 model adapter 边界生成 mock answer，并持久化安全 Ask run evidence。source trace、confidence 与 review status 是可信主线。

## 架构驱动

| Driver | 影响 |
|---|---|
| Source-grounded answers | API 和 UI 必须为每个回答暴露 evidence references。 |
| Review-aware retrieval | 默认 approved-only，避免未审核内容被当作可信答案。 |
| Adapter neutrality | Ask 编排依赖 vector/model service contract，而不是 provider implementation。 |
| Phase 4 hardening | 必须有 audit、safe errors、seam guards 和 E2E verification。 |
| Data safety | 测试中无真实公司文档、raw prompt、private path、provider payload 或 external call。 |

## 系统上下文

| 边界 | 职责 |
|---|---|
| Trusted Ask UI | 收集问题，展示 answer/evidence/policy/errors，并保持 FE 基线行为。 |
| Ask API | 校验请求，执行 review policy，返回 envelope，并暴露 Ask run detail。 |
| Ask application service | 编排 retrieval、no-evidence refusal、model run、evidence persistence 和 safe errors。 |
| Vector adapter/service | 返回带 trace、confidence、review status 的有界 similarity evidence。 |
| Model adapter/service | 基于 evidence references 生成 mock safe answer，并标记 output review-required。 |
| Metadata persistence | 存储 Ask run 和 evidence，不改变 source/Wiki/graph/review 状态。 |

## 高层架构

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge user · SME reviewer · Delivery lead                │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Atlas Frontend                                               │
│ Trusted Ask tab · evidence list · review warnings            │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / ApiEnvelope
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                    │
│ AskController · validation · safe envelope errors            │
├──────────────────────────────────────────────────────────────┤
│ Ask Application Service                                      │
│ scope policy · retrieval orchestration · model run link       │
├─────────────────────────┬────────────────────────────────────┤
│ Vector Service Contract  │ Model Service Contract             │
│ mock vector adapter      │ mock model adapter                 │
└──────────────┬───────────┴──────────────┬─────────────────────┘
               │                          │
               ▼                          ▼
┌──────────────────────────┐   ┌───────────────────────────────┐
│ PostgreSQL metadata      │   │ Adapter implementations        │
│ ask_run · ask_evidence   │   │ replaceable, mock-only in CI   │
└──────────────────────────┘   └───────────────────────────────┘
```

## 组件拆分

### 前端组件

- **Trusted Ask tab:** space-scoped question input、answer、evidence、confidence、policy note、review warning、loading、empty 和 safe error state。
- **Evidence list:** 展示 source file/page 或 section、score/confidence 和 review status。
- **Ask state mapper:** 将 API status 映射为 UI state，且不暗示 review-required output 已可信。

### 后端服务

- **Ask controller:** 暴露 `POST /api/spaces/{spaceId}/ask` 和 `GET /api/ask-runs/{runId}`。
- **Ask service:** 负责 validation、scope checks、retrieval policy、no-evidence refusal、model run orchestration、persistence 和 response assembly。
- **Ask summary/evidence mapper:** 构造 safe DTO，避免 raw source 或 prompt 泄漏。

### 集成适配器

- **Vector boundary:** Ask 通过产品侧 service/adapter contract 调用 vector query 行为。
- **Model boundary:** Ask 通过产品侧 service/adapter contract 调用 model run 行为。
- **禁止的直接依赖:** adapter package 外不得依赖 model provider、vector database、SDK、CLI、outbound HTTP client 或 provider-specific payload。

### 持久化

- `ask_run` 记录请求与回答生命周期。
- `ask_evidence` 记录可追溯 evidence rows。
- 现有 `source_chunk`、`file_item`、`wiki_page`、`graph_node` 与 review records 只引用不修改。

## 安全与可靠性

- Request validation 在 adapter execution 前完成。
- No-evidence response 不生成答案。
- Safe message 有边界且 sanitized。
- Adapter failure 不泄漏 provider detail。
- Seam guard tests 保护 adapter boundary。
- E2E tests 验证 UI 展示 evidence 与 refusal states。

## 风险与取舍

| ID | 风险 | 缓解 |
|---|---|---|
| R-ASKRAG-001 | 用户可能过度信任生成回答。 | 保持 answer review status 可见且为 `REVIEW_REQUIRED`。 |
| R-ASKRAG-002 | review-required evidence 可能与 trusted evidence 混合。 | 默认 approved-only；显式包含时必须有 warning。 |
| R-ASKRAG-003 | 未来 model prompt policy 可能要求留存。 | 当前 SDD 只存 safe summaries 与 references。 |

## 待确认问题

- OQ-ASKRAG-001：review-required evidence 可见性的角色策略。
- OQ-ASKRAG-002：Ask answer 未来接入 review queue。
- OQ-ASKRAG-003：reranking 时机。
