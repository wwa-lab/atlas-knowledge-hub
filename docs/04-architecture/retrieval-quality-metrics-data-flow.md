# Data Flow: retrieval-quality-metrics

Status: Accepted by goal preauthorization
Last updated: 2026-07-07

Trace IDs: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-004, REQ-RETRIEVAL-QUALITY-METRICS-005, REQ-RETRIEVAL-QUALITY-METRICS-006, REQ-RETRIEVAL-QUALITY-METRICS-007, REQ-RETRIEVAL-QUALITY-METRICS-008.

## Run-Level Flow

```text
GET /api/ask-runs/{runId}/quality-metrics
  -> validate run exists
  -> load AskRun
  -> load AskEvidence by run id
  -> compute evidence coverage, citation health, confidence, review eligibility
  -> map safe DTO
  -> return ApiEnvelope<RetrievalRunQualityMetricsResponse>
```

## Space-Level Flow

```text
GET /api/spaces/{spaceId}/retrieval-quality-metrics
  -> validate space exists
  -> load AskRun records by space
  -> load AskEvidence records for each run
  -> compute run metrics
  -> aggregate safe counts and ratios
  -> return ApiEnvelope<RetrievalQualityMetricsSummaryResponse>
```

## Frontend Flow

```text
Ask response loaded
  -> optional metrics request for returned run id
  -> quality chips rendered when metrics exist
  -> safe unavailable state rendered when metrics are absent or fail safely
```

## Data Safety Flow

- Repository layer reads persisted metadata only.
- Calculator receives domain objects and emits numeric counts, ratios, bands, and reason codes.
- Mapper strips raw questions, answers, source content, prompts, provider payloads, and private diagnostics from metrics DTOs.
- UI renders labels from safe enum-like fields only.

## Failure Flow

- Unknown run or space uses existing safe `NOT_FOUND`.
- Unexpected backend errors use existing safe system error behavior.
- Frontend preserves existing Ask display when metrics fetch fails and shows a safe unavailable quality state.
