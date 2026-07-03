# 数据模型：知识图谱

## 概述

知识图谱数据在现有 `wiki_page`、`source_chunk`、`graph_node`、`graph_edge` 元数据上扩展 projection run 与 audit 概念。模型必须保留 source trace、confidence 和 review status。

## 实体

### GraphNode

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | String | 稳定 node id。 |
| `spaceId` | String | 所属知识空间。 |
| `label` | String | 展示名称。 |
| `type` | GraphNodeType | `KNOWLEDGE_SPACE`、`DOCUMENT`、`WIKI_PAGE`、`CONCEPT`、`ENTITY`、`SOURCE_CHUNK`。 |
| `reviewStatus` | ReviewStatus | 可信/审核状态。 |
| `confidence` | Decimal | 可选聚合置信度。 |
| `evidenceChunkIds` | String[] | 支撑节点的 source chunks。 |
| `evidenceWikiPageIds` | String[] | 支撑节点的 Wiki pages。 |
| `updatedAt` | Timestamp | 最近投影或审核更新时间。 |

### GraphEdge

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | String | 稳定 edge id。 |
| `spaceId` | String | 所属知识空间。 |
| `sourceNodeId` | String | 来源节点。 |
| `targetNodeId` | String | 目标节点。 |
| `type` | GraphEdgeType | 批准的边类型。 |
| `reviewStatus` | ReviewStatus | 关系审核状态。 |
| `confidence` | Decimal | 可选关系置信度。 |
| `evidenceChunkIds` | String[] | 可信边必需，除非是结构性 `CONTAINS`。 |
| `evidenceWikiPageIds` | String[] | 关系的 Wiki 证据。 |
| `updatedAt` | Timestamp | 最近投影或审核更新时间。 |

### GraphProjectionRun

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | String | 稳定 run id。 |
| `spaceId` | String | 投影范围。 |
| `status` | GraphProjectionStatus | `REQUESTED`、`RUNNING`、`SUCCEEDED`、`PARTIAL_FAILED`、`FAILED`。 |
| `requestedBy` | String | 用户或系统 actor。 |
| `startedAt` | Timestamp | Run 开始时间。 |
| `completedAt` | Timestamp | Run 完成时间。 |
| `summary` | JSON | created/updated/skipped/failed 统计。 |
| `safeMessage` | String | 用户安全状态详情。 |

### GraphProjectionItem

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | String | Item outcome id。 |
| `runId` | String | 父 projection run。 |
| `sourceType` | String | `wiki_page`、`source_chunk` 或 candidate relationship。 |
| `sourceId` | String | 来源标识。 |
| `targetType` | String | `node`、`edge` 或 `skip`。 |
| `targetId` | String | 创建/更新的 graph record。 |
| `status` | String | `CREATED`、`UPDATED`、`SKIPPED`、`FAILED`。 |
| `reasonCode` | String | 安全排除/失败代码。 |

### GraphAuditRecord

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | String | Audit id。 |
| `spaceId` | String | 范围。 |
| `actor` | String | 用户/系统 actor。 |
| `action` | String | Projection/review/access action。 |
| `targetType` | String | `projection_run`、`graph_node`、`graph_edge` 或 `endpoint`。 |
| `targetId` | String | 目标 id。 |
| `safeSummary` | String | 脱敏 summary。 |
| `createdAt` | Timestamp | 追加写时间。 |

## 关系

```text
KnowledgeSpace 1:N GraphNode
KnowledgeSpace 1:N GraphEdge
GraphNode 1:N GraphEdge as source
GraphNode 1:N GraphEdge as target
GraphProjectionRun 1:N GraphProjectionItem
WikiPage N:M GraphNode through evidenceWikiPageIds
SourceChunk N:M GraphNode/GraphEdge through evidenceChunkIds
```

## 枚举集合

- GraphNodeType：`KNOWLEDGE_SPACE`、`DOCUMENT`、`WIKI_PAGE`、`CONCEPT`、`ENTITY`、`SOURCE_CHUNK`。
- GraphEdgeType：`CONTAINS`、`DERIVED_FROM`、`MENTIONS`、`DEFINES`、`RELATED_TO`、`BELONGS_TO`、`USES`、`DEPENDS_ON`、`REVIEWED_BY`。
- GraphProjectionStatus：`REQUESTED`、`RUNNING`、`SUCCEEDED`、`PARTIAL_FAILED`、`FAILED`。

## Migration 说明

- 现有 `graph_node` 与 `graph_edge` 表可能需要增加 confidence、Wiki evidence、timestamps 和 indexes。
- 仅使用 additive Flyway migration；不得运行时变更 schema。
- Seed 与测试只能使用 mock/sample data。
