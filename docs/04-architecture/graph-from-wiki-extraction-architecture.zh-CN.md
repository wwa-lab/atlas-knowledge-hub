# 架构：graph-from-wiki-extraction

## 摘要

本 slice 扩展现有分层 Graph 实现。`GraphService` 继续作为 orchestrator，`GraphProjectionAdapter` 继续作为 deterministic extraction seam，Wiki metadata 成为 trusted input set。

## 分层架构

```text
Users
  |
Vue Graph tab
  |
GraphController REST API
  |
GraphService
  |-- WikiPageRepository / SourceChunkRepository
  |-- GraphProjectionAdapter (deterministic local extraction)
  |-- GraphNodeRepository / GraphEdgeRepository / GraphProjectionItemRepository
  |
PostgreSQL via Flyway-managed tables
```

## Components

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001：`WikiPageRepository` 提供 extraction 和 skip evidence 所需的 eligible/all space Wiki pages。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-002：`GraphProjectionAdapter` 接收 Wiki descriptors 并返回稳定 node/edge candidates。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004：`GraphService` 持久化带 chunk 与 Wiki page evidence arrays 的 graph objects。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005：`GraphEvidenceReferenceResponse` 成为安全 mixed evidence DTO。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007：Vue `App.vue` 渲染 mixed graph evidence，不改变布局架构。

## Boundaries

- Product logic 不调用 parser、model、vector 或外部 graph engine。
- 复用现有 authorization interceptors 和 path policy，不改变语义。
- Audit 行为保持现有 graph projection/review audit behavior。
- Flyway migration 仅做 additive index 或安全 schema support。

## Risks

- Wiki pages 当前可能没有丰富 entity metadata；deterministic extraction 有意只使用当前 metadata。
- Natural-language relationship extraction 延后，因为它需要 model-assisted behavior。

## Review Result

Architecture-review result: ready for implementation within Tier 1，因为它扩展现有 API/persistence contracts，且不改变 auth、audit、secrets、providers 或 production readiness 语义。

## Task IDs

- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
- T-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-006
- T-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-008
