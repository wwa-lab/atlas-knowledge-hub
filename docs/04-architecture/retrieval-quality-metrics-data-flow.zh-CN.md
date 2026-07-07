# 数据流：retrieval-quality-metrics

状态：已由本次 goal 预授权接受
最后更新：2026-07-07

Trace IDs：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-004、REQ-RETRIEVAL-QUALITY-METRICS-005、REQ-RETRIEVAL-QUALITY-METRICS-006、REQ-RETRIEVAL-QUALITY-METRICS-007、REQ-RETRIEVAL-QUALITY-METRICS-008。

## Run 级流程

```text
GET /api/ask-runs/{runId}/quality-metrics
  -> validate run exists
  -> load AskRun
  -> load AskEvidence by run id
  -> compute evidence coverage, citation health, confidence, review eligibility
  -> map safe DTO
  -> return ApiEnvelope<RetrievalRunQualityMetricsResponse>
```

## Space 级流程

```text
GET /api/spaces/{spaceId}/retrieval-quality-metrics
  -> validate space exists
  -> load AskRun records by space
  -> load AskEvidence records for each run
  -> compute run metrics
  -> aggregate safe counts and ratios
  -> return ApiEnvelope<RetrievalQualityMetricsSummaryResponse>
```

## 前端流程

```text
Ask response loaded
  -> optional metrics request for returned run id
  -> quality chips rendered when metrics exist
  -> safe unavailable state rendered when metrics are absent or fail safely
```

## 数据安全流程

- Repository layer 只读取持久化 metadata。
- Calculator 接收 domain objects，输出 numeric counts、ratios、bands 和 reason codes。
- Mapper 从 metrics DTOs 中剥离 raw questions、answers、source content、prompts、provider payloads 和 private diagnostics。
- UI 只从安全 enum-like fields 渲染 labels。

## 失败流程

- 未知 run 或 space 使用现有安全 `NOT_FOUND`。
- 未预期后端错误使用现有 safe system error 行为。
- Metrics fetch 失败时，前端保留既有 Ask display，并显示安全 unavailable quality state。
