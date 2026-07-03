# Architecture: Review Publish

## Status

Draft. Derived through `spec-to-architecture` and `architecture-to-design`.

## System Context

Review Publish sits between metadata ingestion/review and trusted downstream knowledge consumption. It hardens existing metadata API capabilities and adds publish-specific read/write boundaries for Wiki metadata.

```text
┌──────────────┐
│ SME / Admin  │
└──────┬───────┘
       ▼
┌──────────────────────┐
│ FE Processing Center │
│ Wiki publish states  │
└──────┬───────────────┘
       ▼
┌──────────────────────┐
│ Metadata API         │
│ Review + Publish     │
└──────┬───────────────┘
       ▼
┌──────────────────────┐
│ Domain Services      │
│ Eligibility + Audit  │
└──────┬───────────────┘
       ▼
┌──────────────────────┐
│ PostgreSQL Metadata  │
│ files/chunks/wiki    │
└──────────────────────┘
```

## Component Boundaries

| Component | Responsibility | Boundary |
|---|---|---|
| Frontend Processing Center | Displays blocked queues, publish-ready counts, and publish outcomes. | Consumes API metadata only; no direct engine calls. |
| Review API | Appends review records and updates review status. | Existing file review behavior remains append-only. |
| Publish API | Validates eligibility and creates/updates Wiki page metadata. | Does not mutate raw parser output or source chunks. |
| Review/Publish Domain Service | Owns state transitions, eligibility rules, and user-safe failures. | Depends on repositories and metadata DTOs, not adapter engines. |
| Metadata Persistence | Stores file items, source chunks, review records, and wiki pages. | Relative paths only; no secrets or real document content. |

## Existing Grounding

- `ReviewStatus` already contains `REVIEW_REQUIRED`, `APPROVED`, `NEED_FIX`, `OCR_REQUIRED`, `PUBLISHED` in `backend/src/main/java/com/atlas/metadata/enums/ReviewStatus.java:4`.
- `ReviewAction.resultingStatus()` already maps review actions to non-published review states in `backend/src/main/java/com/atlas/metadata/enums/ReviewAction.java:10`.
- `ReviewService` already appends file review records and updates file review status in `backend/src/main/java/com/atlas/metadata/service/ReviewService.java:48`.
- `WikiPage` exists as deferred metadata with `markdownPath`, `sourceDocumentIds`, `confidence`, and `reviewStatus` in `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java:15`.

## Architecture Decisions

| Decision | Rationale |
|---|---|
| Publish is a metadata transition, not parser/converter execution. | Preserves adapter boundaries and keeps Phase 4 hardening focused. |
| `PUBLISHED` is only set by publish. | Prevents review action from silently publishing content. |
| Wiki page metadata is separate from raw source chunks. | Preserves source trace and original evidence. |
| Graph and Ask are downstream consumers. | Avoids scope collision with `knowledge-graph` and `ask-rag` slices. |
| API guide is required. | Phase 4 introduces new endpoints. |

## Security And Data Safety

- No external cloud calls or new network dependencies.
- No raw secrets, private absolute paths, logs, or real company documents.
- Error responses must be user-safe and envelope-shaped.
- Production RBAC is not implemented here; endpoints must be documented as internal/trusted until a security slice accepts real enforcement.

## Risks

- Bulk publish could enlarge the implementation beyond one reviewable pass.
- Chunk-level publication may be needed if SME review happens below file level.
- Auth/RBAC must be completed before production exposure.
