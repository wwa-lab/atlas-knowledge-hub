# 数据模型：graph-from-wiki-extraction

## 现有实体

| Entity | Existing fields used | Slice use |
|---|---|---|
| `wiki_page` | `id`, `space_id`, `title`, `slug`, `source_document_ids`, `source_refs`, `chunk_refs`, `in_links`, `out_links`, `confidence`, `review_status` | Eligibility 与 extraction input。 |
| `source_chunk` | `id`, `source_file`, `page`, `section`, `confidence`, `review_status` | Evidence detail 与 chunk-backed concept extraction。 |
| `graph_node` | `id`, `space_id`, `label`, `type`, `review_status`, `evidence_chunk_ids`, `evidence_wiki_page_ids`, `confidence` | Wiki-derived nodes。 |
| `graph_edge` | `id`, `space_id`, `source_node_id`, `target_node_id`, `type`, `review_status`, `evidence_chunk_ids`, `evidence_wiki_page_ids`, `confidence` | Wiki-derived edges。 |
| `graph_projection_item` | `source_type`, `source_id`, `target_type`, `target_id`, `status`, `reason_code` | Created/updated/skipped extraction evidence。 |

## Additive Data Contract

- REQ-GRAPH-FROM-WIKI-EXTRACTION-004：`graph_node.evidence_wiki_page_ids` 和 `graph_edge.evidence_wiki_page_ids` 为 Wiki-derived graph objects 填充。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-002：Stable IDs 允许 repository `save` 通过 primary key 表现为 idempotent upsert。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005：Evidence DTO fields 是安全 metadata，不是 raw Wiki 或 source document content。

## Logical Evidence DTO

| Field | Meaning |
|---|---|
| `referenceType` | `SOURCE_CHUNK` 或 `WIKI_PAGE`。 |
| `sourceChunkId` | 仅 chunk evidence 存在。 |
| `wikiPageId` | 仅 Wiki page evidence 存在。 |
| `label` | 安全 title 或 section label。 |
| `sourceFile`, `page`, `section` | 可用时的安全 source trace metadata。 |
| `confidence`, `reviewStatus` | Trust metadata。 |

## IDs

- REQ-GRAPH-FROM-WIKI-EXTRACTION-002
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-003
