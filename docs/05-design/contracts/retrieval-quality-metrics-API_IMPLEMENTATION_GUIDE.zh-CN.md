# retrieval-quality-metrics API 实现指南

日期：2026-07-07
Base path：`/api`
Backend stack：Spring Boot
Auth model：现有 mock-safe current-user 与 RBAC boundary

Trace IDs：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-004、REQ-RETRIEVAL-QUALITY-METRICS-005、REQ-RETRIEVAL-QUALITY-METRICS-006、REQ-RETRIEVAL-QUALITY-METRICS-007、REQ-RETRIEVAL-QUALITY-METRICS-008。

## Endpoints

### GET `/api/ask-runs/{runId}/quality-metrics`

返回单个 Ask run 的安全确定性 metrics。

Response：

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

返回 Knowledge Space 中 Ask runs 的安全 aggregate metrics。

Response：

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

- Unknown run：安全 `NOT_FOUND`。
- Unknown space：安全 `NOT_FOUND`。
- Unexpected failure：通过现有 handler 返回 safe system error。
- 不包含 raw exception、stack trace、prompt、provider payload、private path、secret、internal endpoint 或 raw source content。

## Frontend Contract

Frontend 可以在 `POST /api/spaces/{spaceId}/ask` 返回 Ask run id 后调用 run endpoint。Metrics loading failure 不得隐藏现有 answer 或 citation display。

## Contract Tests

- 成功 run-level metrics。
- 成功 space-level aggregate metrics。
- No-evidence run 不是 eligible，并增加 refusal counts。
- Review-required 或 low-confidence evidence 不是 eligible。
- Unknown run/space 使用 safe errors。

## 非目标

不实现 production governance、新 provider calls、raw diagnostic storage、billing/cost dashboard、online experiment 或 human rating workflow。
