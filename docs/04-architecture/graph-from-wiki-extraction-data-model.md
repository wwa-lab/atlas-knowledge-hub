# Data Model: graph-from-wiki-extraction

## Existing Entities

| Entity | Existing fields used | Slice use |
|---|---|---|
| `wiki_page` | `id`, `space_id`, `title`, `slug`, `source_document_ids`, `source_refs`, `chunk_refs`, `in_links`, `out_links`, `confidence`, `review_status` | Eligibility and extraction input. |
| `source_chunk` | `id`, `source_file`, `page`, `section`, `confidence`, `review_status` | Evidence detail and chunk-backed concept extraction. |
| `graph_node` | `id`, `space_id`, `label`, `type`, `review_status`, `evidence_chunk_ids`, `evidence_wiki_page_ids`, `confidence` | Wiki-derived nodes. |
| `graph_edge` | `id`, `space_id`, `source_node_id`, `target_node_id`, `type`, `review_status`, `evidence_chunk_ids`, `evidence_wiki_page_ids`, `confidence` | Wiki-derived edges. |
| `graph_projection_item` | `source_type`, `source_id`, `target_type`, `target_id`, `status`, `reason_code` | Created/updated/skipped extraction evidence. |

## Additive Data Contract

- REQ-GRAPH-FROM-WIKI-EXTRACTION-004: `graph_node.evidence_wiki_page_ids` and `graph_edge.evidence_wiki_page_ids` are populated for Wiki-derived graph objects.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-002: Stable IDs allow repository `save` to behave as an idempotent upsert by primary key.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005: Evidence DTO fields are safe metadata, not raw Wiki or source document content.

## Logical Evidence DTO

| Field | Meaning |
|---|---|
| `referenceType` | `SOURCE_CHUNK` or `WIKI_PAGE`. |
| `sourceChunkId` | Present only for chunk evidence. |
| `wikiPageId` | Present only for Wiki page evidence. |
| `label` | Safe title or section label. |
| `sourceFile`, `page`, `section` | Safe source trace metadata when available. |
| `confidence`, `reviewStatus` | Trust metadata. |

## IDs

- REQ-GRAPH-FROM-WIKI-EXTRACTION-002
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-003
