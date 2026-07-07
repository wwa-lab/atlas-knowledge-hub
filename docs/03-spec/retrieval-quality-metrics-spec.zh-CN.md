# 规格：retrieval-quality-metrics

状态：已由本次 goal 预授权接受
最后更新：2026-07-07

## 行为唯一真相源

本规格定义 Atlas retrieval surfaces 的本地确定性质量指标。它不定义生产 retrieval governance、answer approval、provider analytics、billing、A/B evaluation 或 raw diagnostic capture。

## 功能需求

### FR-RETRIEVAL-QUALITY-METRICS-001：Run metric calculation

对于已存储的 Ask run，Atlas 必须从 `AskRun` 与 `AskEvidence` records 推导安全 quality object。计算必须确定性，并且不需要 provider、vector、analytics 或 cloud calls。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-001、AC-RETRIEVAL-QUALITY-METRICS-001。

### FR-RETRIEVAL-QUALITY-METRICS-002：Metric categories

Run metric object 必须包含：

- evidence coverage：evidence count、cited evidence count、coverage ratio、missing evidence flag。
- citation health：citation count、healthy citation count、uncited evidence count、unhealthy citation flag。
- confidence：average evidence confidence、answer confidence、confidence band。
- review eligibility：boolean、status 和 reasons。
- no-evidence refusal：run-level refusal behavior 的 boolean。
- safe diagnostics：有界、非敏感 reason codes。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-002、AC-RETRIEVAL-QUALITY-METRICS-002。

### FR-RETRIEVAL-QUALITY-METRICS-003：Aggregate metrics

对于 Knowledge Space，Atlas 必须将 Ask runs 聚合为安全 counts 和 ratios：

- total runs。
- succeeded runs。
- no-evidence refusals。
- failed or partial runs。
- review-eligible runs。
- average evidence coverage。
- average citation health。
- low confidence runs。

Aggregate response 不得包含 raw questions、answers、prompts、provider payloads 或 raw source text。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-003、AC-RETRIEVAL-QUALITY-METRICS-003。

### FR-RETRIEVAL-QUALITY-METRICS-004：Review eligibility rules

只有同时满足以下条件时，run 才是 review-eligible：

- run status 为 `SUCCEEDED`。
- evidence count 大于零。
- citation health 不是 unhealthy。
- average evidence confidence 至少为 `0.700`。
- answer confidence 缺席或至少为 `0.700`。
- 所有 evidence records 都是 `APPROVED`。

否则 response 必须报告 `reviewEligible=false`，并包含安全 reason codes，例如 `NO_EVIDENCE`、`LOW_CONFIDENCE`、`REVIEW_REQUIRED_EVIDENCE`、`FAILED_RUN` 或 `UNHEALTHY_CITATIONS`。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-004、AC-RETRIEVAL-QUALITY-METRICS-004。

### FR-RETRIEVAL-QUALITY-METRICS-005：API surface

Atlas 必须暴露：

- `GET /api/ask-runs/{runId}/quality-metrics`
- `GET /api/spaces/{spaceId}/retrieval-quality-metrics`

两个 endpoints 都必须使用 `ApiEnvelope` 与现有 safe error handling。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-005、AC-RETRIEVAL-QUALITY-METRICS-005。

### FR-RETRIEVAL-QUALITY-METRICS-006：Frontend display

Vue frontend 必须在现有 API-backed Trusted Ask surface 中展示安全 quality metrics。展示可以使用紧凑 quality chips 和 diagnostic section。不得显示 raw source content、raw prompt text、raw provider response、internal endpoints、private paths、stack traces 或 secrets。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-006、AC-RETRIEVAL-QUALITY-METRICS-006。

### FR-RETRIEVAL-QUALITY-METRICS-007：Non-regression

现有 Ask、Graph、Wiki、review/publish、auth、rate-limit、safe-error 和 adapter 行为必须继续通过当前测试。本切片可以新增 DTO fields 或 endpoints，但不得改变 provider/model adapter 调用语义。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-007、AC-RETRIEVAL-QUALITY-METRICS-007。

### FR-RETRIEVAL-QUALITY-METRICS-008：Closeout evidence

必须记录 traceability、roadmap、task completion、verification evidence、closeout gate result、commit hash 和 push target。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-008、AC-RETRIEVAL-QUALITY-METRICS-008。

## 状态规则

| 条件 | Review eligibility | Safe reasons |
|---|---|---|
| `SUCCEEDED` 且有 approved evidence 与 healthy citations | true | none |
| `NO_EVIDENCE` | false | `NO_EVIDENCE` |
| `FAILED` 或 `PARTIAL_FAILED` | false | `FAILED_RUN` |
| 任意 `REVIEW_REQUIRED` 或 `REJECTED` evidence | false | `REVIEW_REQUIRED_EVIDENCE` |
| 平均 confidence 低于阈值 | false | `LOW_CONFIDENCE` |
| 有 evidence 但没有任何 source trace | false | `UNHEALTHY_CITATIONS` |

## 验收矩阵

| 验收 | 覆盖方式 |
|---|---|
| AC-RETRIEVAL-QUALITY-METRICS-001 | Calculator determinism 单元测试。 |
| AC-RETRIEVAL-QUALITY-METRICS-002 | DTO/API contract tests 和 frontend tests。 |
| AC-RETRIEVAL-QUALITY-METRICS-003 | Safe DTO tests 和 diff scans。 |
| AC-RETRIEVAL-QUALITY-METRICS-004 | Eligibility filtering 单元测试。 |
| AC-RETRIEVAL-QUALITY-METRICS-005 | API contract tests。 |
| AC-RETRIEVAL-QUALITY-METRICS-006 | Frontend component/unit 和 E2E tests。 |
| AC-RETRIEVAL-QUALITY-METRICS-007 | `mvn verify`、frontend typecheck/test/build。 |
| AC-RETRIEVAL-QUALITY-METRICS-008 | Traceability 和 closeout gate。 |

## 约束

- 不进行 external network calls 或新增 dependencies。
- 不声称 production governance。
- API、UI、fixtures、logs 或 docs 中不得包含 raw source content、prompt、provider payload、stack trace、secret、private path 或 internal endpoint。
- 保持 adapter boundaries 与现有安全语义。
