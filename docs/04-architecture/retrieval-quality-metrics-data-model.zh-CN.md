# 数据模型：retrieval-quality-metrics

状态：已由本次 goal 预授权接受
最后更新：2026-07-07

Trace IDs：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-004、REQ-RETRIEVAL-QUALITY-METRICS-005、REQ-RETRIEVAL-QUALITY-METRICS-006、REQ-RETRIEVAL-QUALITY-METRICS-007、REQ-RETRIEVAL-QUALITY-METRICS-008。

## 持久化

本切片不需要新增持久化表。Metrics 从以下记录推导：

- `AskRun`：run id、space id、status、review policy、answer confidence、answer review status、safe message、timestamps。
- `AskEvidence`：run id、source chunk id、file item id、source file label、page、section、review status、confidence、vector item key、score、created timestamp。

## DTO 模型

### RetrievalRunQualityMetricsResponse

| Field | Type | Notes |
|---|---|---|
| `runId` | string | Ask run id。 |
| `spaceId` | string | Knowledge Space id。 |
| `status` | string | 现有 Ask status。 |
| `evidenceCoverage` | object | Counts and ratio。 |
| `citationHealth` | object | Citation counts and health flag。 |
| `confidence` | object | Evidence average、answer confidence、band。 |
| `reviewEligibility` | object | Boolean、status、安全 reason codes。 |
| `noEvidenceRefusal` | boolean | no-evidence refusal behavior 为 true。 |
| `safeDiagnostics` | string[] | 有界非敏感 reason codes。 |

### RetrievalQualityMetricsSummaryResponse

| Field | Type | Notes |
|---|---|---|
| `spaceId` | string | Knowledge Space id。 |
| `totalRuns` | number | Space 中 Ask runs 总数。 |
| `succeededRuns` | number | Run status `SUCCEEDED`。 |
| `noEvidenceRefusals` | number | Run status `NO_EVIDENCE`。 |
| `failedRuns` | number | `FAILED` 加 `PARTIAL_FAILED`。 |
| `reviewEligibleRuns` | number | 保守 eligible count。 |
| `lowConfidenceRuns` | number | Confidence band low。 |
| `averageEvidenceCoverage` | number | 四舍五入后的确定性 ratio。 |
| `averageCitationHealth` | number | 四舍五入后的确定性 ratio。 |
| `generatedAt` | string | Response 创建时的 server time，不是 provider timestamp。 |

## 安全枚举

- Confidence band：`HIGH`、`MEDIUM`、`LOW`、`UNKNOWN`。
- Review eligibility status：`ELIGIBLE`、`NEEDS_REVIEW`、`NOT_ELIGIBLE`。
- Safe reason codes：`NO_EVIDENCE`、`LOW_CONFIDENCE`、`REVIEW_REQUIRED_EVIDENCE`、`FAILED_RUN`、`UNHEALTHY_CITATIONS`、`MISSING_SOURCE_TRACE`。

## 数据安全

DTOs 不得暴露 raw question text、answer text、source document body、prompt、provider payload、private path、stack trace、secret 或 internal endpoint。
