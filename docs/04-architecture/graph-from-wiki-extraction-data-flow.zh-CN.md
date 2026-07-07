# 数据流：graph-from-wiki-extraction

## Projection Flow

```text
POST /api/spaces/{spaceId}/graph/projection-runs
  -> GraphService validates scope/adapter/space
  -> WikiPageRepository loads all Wiki pages for the space
  -> GraphService splits eligible vs skipped pages
  -> GraphProjectionAdapter projects deterministic graph candidates
  -> GraphService upserts GraphNode / GraphEdge
  -> GraphProjectionItem records created/updated/skipped evidence
  -> GraphProjectionRun returns safe summary
```

## Evidence Flow

| Source | Graph storage | API evidence | UI evidence |
|---|---|---|---|
| Wiki page ID/title/slug/status/confidence | `evidence_wiki_page_ids` | `referenceType=WIKI_PAGE` | Wiki evidence section |
| Source chunk ID/file/page/section/status/confidence | `evidence_chunk_ids` | `referenceType=SOURCE_CHUNK` | Source chunk section |
| Source document ID | `DOCUMENT` node and Wiki evidence | Safe ID/label only | Document relationship |

## Skip Flow

| Condition | Reason |
|---|---|
| Review status 不是 `APPROVED` 或 `PUBLISHED` | `UNAPPROVED_WIKI_PAGE` |
| Confidence 低于 `0.800` | `LOW_CONFIDENCE_WIKI_PAGE` |
| 无 source refs、chunk refs 或 source documents | `MISSING_WIKI_SOURCE_TRACE` |

## IDs

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001
- REQ-GRAPH-FROM-WIKI-EXTRACTION-002
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
