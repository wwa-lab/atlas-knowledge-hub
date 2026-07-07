# 设计：retrieval-quality-metrics

状态：已由本次 goal 预授权接受
最后更新：2026-07-07

Trace IDs：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-004、REQ-RETRIEVAL-QUALITY-METRICS-005、REQ-RETRIEVAL-QUALITY-METRICS-006、REQ-RETRIEVAL-QUALITY-METRICS-007、REQ-RETRIEVAL-QUALITY-METRICS-008。

## 后端设计

- 新增 `RetrievalQualityMetricsCalculator` 作为确定性纯 calculator。
- 新增 `RetrievalQualityMetricsService` 校验 run/space 存在，并组合 calculator inputs。
- 新增 `RetrievalQualityMetricsController`，提供只读 endpoints。
- 新增 DTO records，覆盖 evidence coverage、citation health、confidence、review eligibility、run metrics 和 summary metrics。
- 只在 repositories 中增加列出 Ask runs 与 evidence 所需的读方法。

## 指标规则

- Evidence coverage ratio：`citedEvidenceCount / evidenceCount`，当 evidence count 为零时为 `0.000`。
- Cited evidence：有 source chunk id，且 source file、page、section 至少一个安全 source label 存在的 evidence。
- Citation health ratio：`healthyCitationCount / evidenceCount`，当 evidence count 为零时为 `0.000`。
- Confidence band：
  - `HIGH`：average evidence confidence 和 answer confidence 都至少为 `0.850`。
  - `MEDIUM`：所有已知 confidence values 都至少为 `0.700`。
  - `LOW`：任意已知 confidence value 低于 `0.700`。
  - `UNKNOWN`：不存在 confidence values。
- Review eligibility 遵循 FR-RETRIEVAL-QUALITY-METRICS-004。

## 前端设计

- 新增 TypeScript metric types。
- 为 run 与 space quality metrics 新增 API client functions。
- 在 API-backed Trusted Ask 中，当成功 Ask run response 有 run id 后请求 metrics。
- 渲染紧凑 chips：evidence coverage、citation health、confidence band、review eligibility、no-evidence refusal。
- 如果 metrics 无法加载，渲染安全 unavailable state。

## 测试设计

- 后端单元测试覆盖 calculator determinism、eligibility filtering、confidence bands、no-evidence refusal，以及 safe DTO 不包含 raw fields。
- 后端 integration/API contract tests 覆盖两个 endpoints 和 `NOT_FOUND`。
- 前端测试覆盖 type mapping、quality chips 渲染、安全 unavailable state，以及无 raw diagnostics。
- E2E 使用 mocks 覆盖 API-backed Trusted Ask quality signal display。

## 可访问性与 UX

Quality chips 使用文本标签和稳定 data attributes。它们补充现有 answer/citation 内容，不阻塞现有流程。

## 风险

- Metrics 可能被误解为生产级治理。缓解：使用保守命名，并在文档中说明这些只是本地质量信号。
- 现有 Ask evidence 没有显式 citation objects。缓解：使用安全 evidence trace metadata 作为 citation health proxy。
