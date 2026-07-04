# Requirements: Full-Stack Productization

## Status

Draft for implementation. Slice `full-stack-productization`. Phase 4 hardening / P0 productization.

## Goal

Move Atlas Knowledge Hub from static/mock UI surfaces to a real API-driven browser loop for:

```text
Knowledge Space list -> Space detail -> metadata-only sample upload/batch -> file/chunk source trace
-> SME review -> publish Wiki -> graph projection -> trusted Ask
```

## Scope

### In Scope

- Vue UI calls real Atlas backend APIs for P0 workflow surfaces.
- Metadata-only sample upload creates safe mock/sample batch inventory through the existing batch API.
- Review, publish, Wiki, graph, vector indexing, and Ask use backend services and adapter boundaries.
- Loading, empty, error, and disabled/coming-soon states are visible.
- New UI-driven Playwright E2E proves a browser user completes the loop.

### Out Of Scope

- Production file byte upload, real company documents, external cloud calls, production auth/RBAC, production storage, or a production vector/model provider.
- Copying WeKnora code, structure, assets, or proprietary-looking UI details.
- Replacing the existing first-layer, second-layer, or provider-backed test strategy.

## Requirements

| ID | Requirement | Priority |
|---|---|---|
| REQ-FSP-001 | The home view must list Knowledge Spaces from `GET /api/spaces`, with loading, empty, and error states. | Must |
| REQ-FSP-002 | Selecting a Knowledge Space must load detail from `GET /api/spaces/{spaceId}` and show the selected space context across the P0 tabs. | Must |
| REQ-FSP-003 | The Documents tab must create a metadata-only sample batch through `POST /api/spaces/{spaceId}/batches` and then load batches, files, and source chunks from backend APIs. | Must |
| REQ-FSP-004 | File and chunk views must preserve source trace, confidence, status, and review status from backend responses. | Must |
| REQ-FSP-005 | The Review tab must load review queues from `GET /api/spaces/{spaceId}/review-queues` and submit SME review through `POST /api/files/{fileId}/reviews`. | Must |
| REQ-FSP-006 | The Publish flow must call `POST /api/files/{fileId}/publish`, refresh `GET /api/spaces/{spaceId}/wiki-pages`, and show `PUBLISHED` Wiki metadata. | Must |
| REQ-FSP-007 | After publish, the browser flow must refresh trusted downstream evidence through existing graph/vector APIs so the Graph tab and Ask tab can reference the published evidence. | Must |
| REQ-FSP-008 | The Graph tab must remain API-backed and show graph evidence that includes the published file/chunk after projection. | Must |
| REQ-FSP-009 | The Ask tab must call `POST /api/spaces/{spaceId}/ask` and `GET /api/ask-runs/{runId}`, then display answer, citations/evidence, and `REVIEW_REQUIRED` answer status. | Must |
| REQ-FSP-010 | UI controls that are not truly connected to backend behavior must be disabled and labeled coming soon instead of appearing operational. | Must |
| REQ-FSP-011 | The implementation must preserve existing first-layer, second-layer, and provider-backed E2E commands and semantics. | Must |
| REQ-FSP-012 | Verification must include frontend typecheck/unit/build/E2E, backend `mvn verify`, `git diff --check`, secret/private-path scan, and new-network/dependency scan where feasible. | Must |

## Constraints

- Mock/sample data only.
- No real company documents, screenshots, credentials, logs, private paths, or confidential content.
- Parser, converter, model, vector, storage, and graph projection logic stay behind product-facing adapter/service boundaries.
- Frontend must not call provider, parser, vector DB, storage, or converter engines directly.
- Generated Ask answers remain `REVIEW_REQUIRED`.

## Assumptions

- Existing Spring Boot endpoints are the P0 API contract unless this slice finds a blocking gap.
- Metadata-only sample batch creation is acceptable as the P0 browser-triggered upload substitute.
- Graph/vector refresh may call existing adapter-backed backend endpoints after publish to make the same evidence available to downstream panels.

## Open Questions

- Future P1 file-byte upload UX and storage policy are deferred.
- Production auth/RBAC and organization-specific permissions are deferred.
