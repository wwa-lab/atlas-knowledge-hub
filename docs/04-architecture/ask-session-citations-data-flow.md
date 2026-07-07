# Data Flow: Ask Session Citations

## Create Ask Run With Session

```mermaid
sequenceDiagram
    participant UI as Trusted Ask UI
    participant API as AskController
    participant SVC as AskService
    participant DB as PostgreSQL
    participant V as VectorService
    participant M as ModelService

    UI->>API: POST /api/spaces/{spaceId}/ask
    API->>SVC: createRun(spaceId, request)
    SVC->>DB: create or reuse ask_session
    SVC->>DB: create ask_run with session_id
    SVC->>V: query approved/review-aware evidence
    SVC->>DB: persist ask_evidence citation snapshots
    SVC->>M: create model run with safe source references
    SVC->>DB: complete ask_run and touch ask_session
    SVC-->>API: AskRunResponse with session + citations
    API-->>UI: ApiEnvelope<AskRunResponse>
```

## Read Session History

```mermaid
sequenceDiagram
    participant UI as Trusted Ask UI
    participant API as AskController
    participant SVC as AskService
    participant DB as PostgreSQL

    UI->>API: GET /api/spaces/{spaceId}/ask-sessions
    API->>SVC: listSessions(spaceId)
    SVC->>DB: read recent sessions + latest run counts
    API-->>UI: session summaries
    UI->>API: GET /api/ask-sessions/{sessionId}
    API->>SVC: getSession(sessionId)
    SVC->>DB: read session, runs, citations
    API-->>UI: session detail with ordered answers
```

## Citation Safety Flow

1. Vector results provide source chunk id, file item id, source file, page, section, confidence, review status, vector key, and score.
2. `AskService` filters by review policy.
3. `AskService` derives safe `evidenceLabel` and `sourceLocator`.
4. `AskService` sets `citationStatus`, `reviewEligible`, and `excludedReason`.
5. `AskEvidence` stores the citation snapshot.
6. DTO mapping returns safe fields only.

## Error Flow

- Unknown session: safe not-found response.
- Cross-space session: safe validation response.
- Unsafe session title or requestedBy: safe validation response.
- Model/vector failure: existing Ask failure behavior stores a safe message and returns `FAILED`.

## Verification Points

- API contract tests assert create/reuse/list/detail paths.
- Unit tests assert citation status derivation and safe labels.
- Frontend tests assert session and citation rendering.
