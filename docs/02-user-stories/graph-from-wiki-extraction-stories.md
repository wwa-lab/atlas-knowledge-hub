# User Stories: graph-from-wiki-extraction

## User Story US-GRAPH-FROM-WIKI-EXTRACTION-001

**Title:** Extract trusted graph from reviewed Wiki pages

As a knowledge user, I want Graph nodes and edges to come from reviewed Wiki pages, so that graph exploration reflects trusted Atlas knowledge.

### Acceptance Criteria

1. **Given** Wiki pages with `APPROVED` or `PUBLISHED` status and source trace, **when** a graph projection run executes, **then** graph nodes and edges are extracted from those pages.
2. **Given** Wiki pages with `REVIEW_REQUIRED`, `NEED_FIX`, low confidence, or missing source trace, **when** extraction runs, **then** those pages are skipped with safe reason codes.
3. **Given** the same eligible Wiki input, **when** extraction runs repeatedly, **then** graph IDs and persisted records remain stable.

## User Story US-GRAPH-FROM-WIKI-EXTRACTION-002

**Title:** Inspect Wiki-derived evidence

As an SME reviewer, I want each graph object to show Wiki page and source chunk evidence, so that I can audit why a relationship exists.

### Acceptance Criteria

1. **Given** a selected graph node, **when** the detail API is loaded, **then** it returns safe source chunk and Wiki page evidence metadata.
2. **Given** a selected graph object in the UI, **when** evidence is displayed, **then** the UI distinguishes Wiki page evidence from chunk evidence.
3. **Given** evidence metadata, **when** API/UI responses are inspected, **then** no raw source content, private path, secret, endpoint, or provider payload is exposed.

## User Story US-GRAPH-FROM-WIKI-EXTRACTION-003

**Title:** Preserve existing graph product behavior

As a delivery lead, I want existing Graph and downstream refresh behavior to keep working, so that this slice upgrades evidence quality without regressing current product flows.

### Acceptance Criteria

1. **Given** current Graph APIs and frontend filters, **when** this slice is implemented, **then** existing graph query, detail, projection, and edge review contracts remain compatible.
2. **Given** downstream refresh, **when** it invokes graph projection, **then** projection remains deterministic and local.
3. **Given** verification commands, **when** they run, **then** backend, frontend, E2E, and closeout gates pass.

## Trace IDs

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001
- REQ-GRAPH-FROM-WIKI-EXTRACTION-002
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- REQ-GRAPH-FROM-WIKI-EXTRACTION-008
