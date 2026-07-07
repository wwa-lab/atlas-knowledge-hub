# retrieval-quality-metrics API Implementation Guide

Date: 2026-07-07
Base path: `/api`
Backend stack: Spring Boot
Auth model: Existing mock-safe current-user and RBAC boundary

Trace IDs: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-004, REQ-RETRIEVAL-QUALITY-METRICS-005, REQ-RETRIEVAL-QUALITY-METRICS-006, REQ-RETRIEVAL-QUALITY-METRICS-007, REQ-RETRIEVAL-QUALITY-METRICS-008.

## Endpoints

### GET `/api/ask-runs/{runId}/quality-metrics`

Returns safe deterministic metrics for one Ask run.

Response:

```json
{
  "success": true,
  "data": {
    "runId": "ask-sample",
    "spaceId": "ibm-i-modernization",
    "status": "SUCCEEDED",
    "evidenceCoverage": {
      "evidenceCount": 2,
      "citedEvidenceCount": 2,
      "coverageRatio": 1.0,
      "missingEvidence": false
    },
    "citationHealth": {
      "citationCount": 2,
      "healthyCitationCount": 2,
      "uncitedEvidenceCount": 0,
      "healthRatio": 1.0,
      "unhealthy": false
    },
    "confidence": {
      "averageEvidenceConfidence": 0.91,
      "answerConfidence": 0.88,
      "band": "HIGH"
    },
    "reviewEligibility": {
      "reviewEligible": true,
      "status": "ELIGIBLE",
      "reasons": []
    },
    "noEvidenceRefusal": false,
    "safeDiagnostics": []
  },
  "error": null,
  "meta": null
}
```

### GET `/api/spaces/{spaceId}/retrieval-quality-metrics`

Returns safe aggregate metrics for Ask runs in a Knowledge Space.

Response:

```json
{
  "success": true,
  "data": {
    "spaceId": "ibm-i-modernization",
    "totalRuns": 4,
    "succeededRuns": 2,
    "noEvidenceRefusals": 1,
    "failedRuns": 1,
    "reviewEligibleRuns": 1,
    "lowConfidenceRuns": 1,
    "averageEvidenceCoverage": 0.75,
    "averageCitationHealth": 0.75,
    "generatedAt": "2026-07-07T00:00:00Z"
  },
  "error": null,
  "meta": null
}
```

## Error Contract

- Unknown run: safe `NOT_FOUND`.
- Unknown space: safe `NOT_FOUND`.
- Unexpected failure: safe system error through existing handler.
- No raw exception, stack trace, prompt, provider payload, private path, secret, internal endpoint, or raw source content.

## Frontend Contract

Frontend may call the run endpoint after `POST /api/spaces/{spaceId}/ask` returns an Ask run id. Metrics loading failure must not hide the existing answer or citation display.

## Contract Tests

- Successful run-level metrics.
- Successful space-level aggregate metrics.
- No-evidence run is not eligible and increments refusal counts.
- Review-required or low-confidence evidence is not eligible.
- Unknown run/space uses safe errors.

## Non-Goals

No production governance, new provider calls, raw diagnostic storage, billing/cost dashboard, online experiment, or human rating workflow.
