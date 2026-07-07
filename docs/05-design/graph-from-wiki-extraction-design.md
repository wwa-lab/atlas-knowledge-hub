# Detailed Design: graph-from-wiki-extraction

## Backend Design

- T-GRAPH-FROM-WIKI-EXTRACTION-001: Extend graph projection descriptors to include Wiki page metadata, chunk references, source document IDs, links, review status, and confidence.
- T-GRAPH-FROM-WIKI-EXTRACTION-002: `GraphService` loads all Wiki pages in a space, filters eligible pages, records skipped pages, and passes eligible descriptors to the deterministic adapter.
- T-GRAPH-FROM-WIKI-EXTRACTION-003: `DeterministicGraphProjectionAdapter` emits stable Wiki page, document, concept, and relationship candidates.
- T-GRAPH-FROM-WIKI-EXTRACTION-004: `GraphService` persists candidates with both chunk evidence and Wiki page evidence arrays.
- T-GRAPH-FROM-WIKI-EXTRACTION-005: Node detail maps evidence arrays into safe mixed evidence DTOs.

## Frontend Design

- T-GRAPH-FROM-WIKI-EXTRACTION-006: `ApiGraphEvidenceReference` gains `referenceType`, optional `wikiPageId`, and `label`.
- T-GRAPH-FROM-WIKI-EXTRACTION-006: The Graph detail panel renders Wiki evidence and source chunk evidence in the same detail area with clear labels.
- T-GRAPH-FROM-WIKI-EXTRACTION-007: Component/unit and E2E tests verify Wiki-derived evidence is visible and existing graph filters still work.

## Validation And Error Handling

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001: Unsupported `scope` or `adapterId` remains validation failure.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005: Evidence mapping must use safe metadata only.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006: Existing safe error envelope behavior remains unchanged.

## Review-Doc-Quality Result

Verdict: Ready. The SDD set is complete, bilingual, traceable, and within the prompt preauthorization boundary. No SDD change introduces external providers, real data, production auth/RBAC/audit/secret/rate-limit semantics, or destructive migration.

## IDs

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
- T-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-006
- T-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-008
