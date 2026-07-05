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

## Product Goal Batch 5 Addendum: Phase I1-I3 API-Backed Cutover

Status: completed for L3 API-backed readiness evidence on 2026-07-05; not final product acceptance.

| Phase | API-backed product surface | Existing API contract |
|---|---|---|
| I1 Knowledge Space metadata | Real Vue home cards and space header consume Knowledge Space metadata and keep safe sample fallbacks only when API data is unavailable. | `GET /api/spaces`; `GET /api/spaces/{spaceId}` |
| I2 Batch/file/chunk metadata | Real Vue Documents tab shows API batch, file, and source chunk metadata, and creates the safe sample batch through Atlas API. | `GET /api/spaces/{spaceId}/batches`; `POST /api/spaces/{spaceId}/batches`; `GET /api/batches/{batchId}/files`; `GET /api/files/{fileId}/chunks` |
| I3 Review queues | Real Vue Processing Center shows API review queues alongside existing processing gates for Wiki/Graph/Ask eligibility. | `GET /api/spaces/{spaceId}/review-queues` |

This addendum does not introduce new backend endpoints, production uploads, production authentication/RBAC, real company data, external providers, or direct frontend calls to parser/converter/storage/vector/model engines. Remaining Phase I work covers Wiki pages, Graph evidence, Ask runs/citations, and model configuration metadata.

## Product Goal Batch 6 Addendum: Phase I4-I7 API-Backed Cutover

Status: completed for L3 API-backed readiness evidence on 2026-07-05; not final product acceptance.

| Phase | API-backed product surface | Existing API contract |
|---|---|---|
| I4 Wiki pages | Real Vue Wiki tab reads published Wiki metadata from Atlas API after API review/publish actions. | `GET /api/spaces/{spaceId}/wiki-pages`; `POST /api/files/{fileId}/publish` |
| I5 Graph evidence | Real Vue Graph tab maps Atlas graph nodes, edges, and source-trace evidence into the product graph surface. | `GET /api/spaces/{spaceId}/graph`; `GET /api/spaces/{spaceId}/graph/nodes/{nodeId}` |
| I6 Ask runs and citations | Real Vue global Ask surface can submit through Atlas Ask API and display answer status, model run id, and citations. | `POST /api/spaces/{spaceId}/ask`; `GET /api/ask-runs/{runId}` |
| I7 Model configuration metadata | Real Vue model settings surface reads masked model adapter capability metadata. | `GET /api/model-adapters` |

This addendum does not add production provider calls, raw secret display, production RBAC, real company data, streaming Ask, or new model administration write contracts. Phase J hardening remains pending.

## Core Knowledge Loop v1 Addendum: Real Upload And Runtime Model Configuration

Status: planned for the next implementation slice on 2026-07-05. This slice targets a user-runnable closed loop, not full L5 production hardening.

### Scope

- FR-FSP-023: A normal user can configure the DeepSeek chat model key through Atlas UI/API without editing shell scripts or process environment variables.
- FR-FSP-024: Atlas stores only masked model configuration state in API responses; raw secrets are never returned to the frontend, logs, or persisted documentation.
- FR-FSP-025: A normal user can upload one or more PDF files, or a ZIP containing PDF files, through the primary product UI.
- FR-FSP-026: Uploaded files are stored in a local artifact area controlled by Atlas backend configuration, and metadata records keep relative source trace paths.
- FR-FSP-027: Atlas parses uploaded PDFs through an adapter boundary into review-required source chunks with page/section/source trace metadata.
- FR-FSP-028: File review updates propagate to the selected source chunks so review queues, publish gates, graph, vector, and Ask use consistent review state.
- FR-FSP-029: Atlas exposes a backend downstream refresh action that rebuilds graph and vector evidence from approved or published chunks without frontend direct engine calls.
- FR-FSP-030: Ask uses configured model capabilities when available, while keeping safe no-key and no-evidence states visible.
- FR-FSP-031: Any visible capability outside this v1 closed loop is disabled or labeled coming soon instead of behaving like a working production feature.

### Acceptance Matrix Addendum

| Requirement | Spec Sections | Observable Check |
|---|---|---|
| REQ-FSP-013 | Runtime Model Configuration | User saves, reads masked, and clears model key through Atlas API/UI. |
| REQ-FSP-014 | Real Upload | User uploads PDF or ZIP-of-PDF through UI and receives a real batch id. |
| REQ-FSP-015 | Parser Adapter | Backend parser run creates review-required chunks from uploaded PDF content. |
| REQ-FSP-016 | Review Consistency | Approving a file updates selected chunks and review queues consistently. |
| REQ-FSP-017 | Downstream Refresh | Backend refresh creates graph/vector evidence without frontend engine calls. |
| REQ-FSP-018 | Ask | Ask can run after publish/refresh and shows citations or a safe actionable state. |
| REQ-FSP-019 | Disabled / Coming Soon | Office/OCR/RBAC/vector-store/admin surfaces are disabled until implemented. |
| REQ-FSP-020 | Verification | Backend tests, frontend checks, safety scans, and manual closed-loop evidence are recorded. |

### Explicitly Disabled For v1

- Office document conversion unless a configured internal converter runtime is present.
- Image OCR, tables-as-structured-data extraction, incremental re-indexing, and streaming Ask.
- Production authentication, RBAC administration, audit retention, rate limiting, multi-tenant isolation, and secret-manager integration.
- Production vector stores such as pgvector, Milvus, or Qdrant.
- Any direct frontend calls to parser, converter, model, vector, or storage engines.

## SDD Quality Notes

This document was generated using the Atlas SDD chain model: `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, and `review-doc-quality` as the review checklist.
