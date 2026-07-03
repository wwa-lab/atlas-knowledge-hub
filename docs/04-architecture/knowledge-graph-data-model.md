# Data Model: Knowledge Graph

## Overview

Knowledge graph data extends existing `wiki_page`, `source_chunk`, `graph_node`, and `graph_edge` metadata with projection run and audit concepts. The model preserves source trace, confidence, and review status.

## Entities

### GraphNode

| Field | Type | Notes |
|---|---|---|
| `id` | String | Stable node id. |
| `spaceId` | String | Owning knowledge space. |
| `label` | String | Display label. |
| `type` | GraphNodeType | `KNOWLEDGE_SPACE`, `DOCUMENT`, `WIKI_PAGE`, `CONCEPT`, `ENTITY`, `SOURCE_CHUNK`. |
| `reviewStatus` | ReviewStatus | Trust/review state. |
| `confidence` | Decimal | Optional aggregate confidence. |
| `evidenceChunkIds` | String[] | Source chunks backing the node. |
| `evidenceWikiPageIds` | String[] | Wiki pages backing the node. |
| `updatedAt` | Timestamp | Last projection or review update. |

### GraphEdge

| Field | Type | Notes |
|---|---|---|
| `id` | String | Stable edge id. |
| `spaceId` | String | Owning knowledge space. |
| `sourceNodeId` | String | Source node. |
| `targetNodeId` | String | Target node. |
| `type` | GraphEdgeType | Approved edge type. |
| `reviewStatus` | ReviewStatus | Relationship review state. |
| `confidence` | Decimal | Optional relationship confidence. |
| `evidenceChunkIds` | String[] | Required for trusted edges unless edge is structural `CONTAINS`. |
| `evidenceWikiPageIds` | String[] | Wiki evidence for the relationship. |
| `updatedAt` | Timestamp | Last projection or review update. |

### GraphProjectionRun

| Field | Type | Notes |
|---|---|---|
| `id` | String | Stable run id. |
| `spaceId` | String | Projection scope. |
| `status` | GraphProjectionStatus | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`. |
| `requestedBy` | String | User or system actor. |
| `startedAt` | Timestamp | Run start. |
| `completedAt` | Timestamp | Run completion. |
| `summary` | JSON | Counts by created/updated/skipped/failed. |
| `safeMessage` | String | User-safe status detail. |

### GraphProjectionItem

| Field | Type | Notes |
|---|---|---|
| `id` | String | Item outcome id. |
| `runId` | String | Parent projection run. |
| `sourceType` | String | `wiki_page`, `source_chunk`, or candidate relationship. |
| `sourceId` | String | Source identifier. |
| `targetType` | String | `node`, `edge`, or `skip`. |
| `targetId` | String | Created/updated graph record when available. |
| `status` | String | `CREATED`, `UPDATED`, `SKIPPED`, `FAILED`. |
| `reasonCode` | String | Safe exclusion/failure code. |

### GraphAuditRecord

| Field | Type | Notes |
|---|---|---|
| `id` | String | Audit id. |
| `spaceId` | String | Scope. |
| `actor` | String | User/system actor. |
| `action` | String | Projection/review/access action. |
| `targetType` | String | `projection_run`, `graph_node`, `graph_edge`, or `endpoint`. |
| `targetId` | String | Target id. |
| `safeSummary` | String | Sanitized summary. |
| `createdAt` | Timestamp | Append-only timestamp. |

## Relationships

```text
KnowledgeSpace 1:N GraphNode
KnowledgeSpace 1:N GraphEdge
GraphNode 1:N GraphEdge as source
GraphNode 1:N GraphEdge as target
GraphProjectionRun 1:N GraphProjectionItem
WikiPage N:M GraphNode through evidenceWikiPageIds
SourceChunk N:M GraphNode/GraphEdge through evidenceChunkIds
```

## Enum Sets

- GraphNodeType: `KNOWLEDGE_SPACE`, `DOCUMENT`, `WIKI_PAGE`, `CONCEPT`, `ENTITY`, `SOURCE_CHUNK`.
- GraphEdgeType: `CONTAINS`, `DERIVED_FROM`, `MENTIONS`, `DEFINES`, `RELATED_TO`, `BELONGS_TO`, `USES`, `DEPENDS_ON`, `REVIEWED_BY`.
- GraphProjectionStatus: `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`.

## Migration Notes

- Existing `graph_node` and `graph_edge` tables may need additive columns for confidence, Wiki evidence, timestamps, and indexes.
- Additive Flyway migrations only; do not mutate schema at runtime.
- Seed and tests must use mock/sample data only.
