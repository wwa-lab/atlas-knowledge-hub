# Specification: Full-Stack Productization

## Status

Draft for implementation. Source of truth for slice `full-stack-productization`.

## Overview

This slice replaces the prototype-first Vue shell with an API-driven P0 workflow while preserving mock/sample-only safety. The backend remains the Spring Boot metadata/control plane; parser, converter, model, vector, storage, and graph projection behavior stay behind backend service/adapter boundaries.

## Functional Requirements

### Space Navigation

- FR-FSP-001: The frontend loads `GET /api/spaces` on startup and renders API spaces.
- FR-FSP-002: Selecting a space loads `GET /api/spaces/{spaceId}` and sets that space as the context for Documents, Review, Wiki, Graph, and Ask.
- FR-FSP-003: Space list and detail failures show user-safe error messages with retry.

### Metadata-Only Upload And Batch

- FR-FSP-004: The Documents tab exposes a sample upload action that posts safe metadata to `POST /api/spaces/{spaceId}/batches`.
- FR-FSP-005: The created batch uses `sourceKind=folder`, review-required file metadata, safe relative paths, markdown path, confidence, and source chunks.
- FR-FSP-006: The UI refreshes `GET /api/spaces/{spaceId}/batches`, `GET /api/batches/{batchId}/files`, and `GET /api/files/{fileId}/chunks`.

### Review And Publish

- FR-FSP-007: The Review tab loads `GET /api/spaces/{spaceId}/review-queues`.
- FR-FSP-008: Approve action posts `POST /api/files/{fileId}/reviews` with action `APPROVE`, reviewer, comment, and affected chunks.
- FR-FSP-009: Publish action posts `POST /api/files/{fileId}/publish` only after the selected file is approved.
- FR-FSP-010: Wiki tab refreshes `GET /api/spaces/{spaceId}/wiki-pages` and displays published metadata.

### Downstream Evidence Refresh

- FR-FSP-011: After publish, the browser flow may call existing adapter-backed backend endpoints to refresh graph projection and vector index for the published file's chunks.
- FR-FSP-012: Graph projection uses `POST /api/spaces/{spaceId}/graph/projection-runs` with backend graph adapter policy and required graph headers.
- FR-FSP-013: Vector indexing uses `POST /api/spaces/{spaceId}/vector-runs` with `mock-vector`, `APPROVED_ONLY`, and source chunk ids from the reviewed file.

### Graph

- FR-FSP-014: Graph tab calls `GET /api/spaces/{spaceId}/graph` and `GET /api/spaces/{spaceId}/graph/nodes/{nodeId}`.
- FR-FSP-015: Graph evidence detail displays source chunk id, source file, section/page, confidence, and review status.
- FR-FSP-016: Unauthorized or failed graph access is visible and must not silently claim the graph is connected.

### Ask

- FR-FSP-017: Ask tab accepts a question and posts `POST /api/spaces/{spaceId}/ask`.
- FR-FSP-018: After Ask creation, the UI reads `GET /api/ask-runs/{runId}` before rendering final answer state.
- FR-FSP-019: Ask answer displays status, answer text or safe message, confidence, `REVIEW_REQUIRED` answer review status, and evidence rows.
- FR-FSP-020: Ask no-evidence or failure states are visible and safe.

### Disabled / Coming Soon

- FR-FSP-021: Any visible feature not connected to the real P0 API loop is disabled, labeled coming soon, or moved out of the primary usable flow.
- FR-FSP-022: The accepted static prototype may remain as a reference artifact but must not be the primary operational UI for P0.

## State Rules

```text
No space -> loading spaces -> space selected
space selected -> sample batch created -> file/chunks loaded
file REVIEW_REQUIRED -> APPROVE -> file APPROVED -> PUBLISH -> wiki PUBLISHED
wiki PUBLISHED -> graph/vector refresh -> graph evidence ready -> ask SUCCEEDED/NO_EVIDENCE/FAILED
```

## Error And Empty States

- API request failures show endpoint-specific safe copy and a retry path where practical.
- Empty spaces, batches, files, chunks, queues, wiki pages, graph, and ask evidence show distinct empty states.
- Unsafe request text, invalid publish sequencing, or missing prerequisites keep actions disabled.

## Acceptance Matrix

| Requirement | Spec Sections | Observable Check |
|---|---|---|
| REQ-FSP-001 | Space Navigation | Browser shows API space list. |
| REQ-FSP-002 | Space Navigation | Space detail is loaded and tab context updates. |
| REQ-FSP-003 | Metadata-Only Upload And Batch | Browser creates sample batch and loads files/chunks. |
| REQ-FSP-004 | Metadata-Only Upload And Batch | File/chunk source trace, confidence, and review status are visible. |
| REQ-FSP-005 | Review And Publish | Review queues load and approve action posts through API. |
| REQ-FSP-006 | Review And Publish | Publish creates/refreshes `PUBLISHED` Wiki metadata. |
| REQ-FSP-007 | Downstream Evidence Refresh | Post-publish graph/vector refresh succeeds through backend endpoints. |
| REQ-FSP-008 | Graph | Graph shows evidence for the published chunk. |
| REQ-FSP-009 | Ask | Ask shows answer, citations, and `REVIEW_REQUIRED`. |
| REQ-FSP-010 | Disabled / Coming Soon | Unconnected mock controls are disabled/coming soon. |
| REQ-FSP-011 | Verification | Existing E2E commands are preserved. |
| REQ-FSP-012 | Verification | Required commands/scans are run or reported skipped. |

## Out Of Scope

- Production file byte upload and storage.
- Production auth/RBAC.
- Direct frontend/provider/vector/parser/storage/converter calls.
- Real external cloud calls.
- Real company data.

## SDD Quality Notes

This document was generated using the Atlas SDD chain model: `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, and `review-doc-quality` as the review checklist.
