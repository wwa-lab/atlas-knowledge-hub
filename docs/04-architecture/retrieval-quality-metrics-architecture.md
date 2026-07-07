# Architecture: retrieval-quality-metrics

Status: Accepted by goal preauthorization
Last updated: 2026-07-07

## Architecture Summary

`retrieval-quality-metrics` adds an additive metrics layer over existing Ask run and evidence records. It does not change model, vector, graph, wiki, auth, rate-limit, audit, or provider semantics.

Trace IDs: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-004, REQ-RETRIEVAL-QUALITY-METRICS-005, REQ-RETRIEVAL-QUALITY-METRICS-006, REQ-RETRIEVAL-QUALITY-METRICS-007, REQ-RETRIEVAL-QUALITY-METRICS-008.

## Components

```text
Vue Trusted Ask / diagnostics
        |
        v
RetrievalQualityMetricsController
        |
        v
RetrievalQualityMetricsService
        |
        +--> RetrievalQualityMetricsCalculator
        |
        +--> AskRunRepository / AskEvidenceRepository
        |
        +--> SpaceRepository
```

## Backend Boundaries

- Controller returns `ApiEnvelope` DTOs only.
- Service validates space/run existence and gathers persisted metadata.
- Calculator is pure and deterministic.
- Repository reads existing Ask records; no provider, vector, analytics, or cloud calls.
- Safe errors are handled by the existing `GlobalExceptionHandler` and safe error factory.

## Frontend Boundaries

- TypeScript types mirror backend safe DTOs.
- API client adds read-only metrics calls.
- `App.vue` displays compact quality signals on existing Trusted Ask states.
- UI labels must remain conservative and avoid production governance claims.

## Persistence Decision

No new table is required for MVP quality metrics because all metrics are derived from stored Ask run and evidence metadata. This avoids storing redundant diagnostics and avoids raw-content capture. A Flyway migration is not required unless implementation discovers a missing safe metadata field.

## Security And Data Safety

- No raw questions or answers in aggregate metrics.
- Run-level metrics may identify a run and safe evidence metadata, but must not include raw source text, prompt, provider payload, stack trace, secret, private path, or internal endpoint.
- Review eligibility is conservative and cannot mark low-confidence, rejected, review-required, missing-trace, failed, or no-evidence content as trusted.

## Architecture Review Result

Architecture review is required because this slice adds API contracts and data flow. Result: ready with additive API-only architecture, no semantic security/provider changes, no production governance claim.
