# 任务：retrieval-quality-metrics

状态：已完成并验证
最后更新：2026-07-07

## 工作流

- SDD 和 execution manifest。
- 后端确定性 metric calculation 与 API contract。
- 前端安全 quality signal display。
- Tests、traceability、roadmap、closeout、commit 和 push。

## 任务详情

### T-RETRIEVAL-QUALITY-METRICS-001：确认 SDD 和 execution manifest
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-008
- Spec：FR-RETRIEVAL-QUALITY-METRICS-008
- Owner：docs
- Priority：Must
- Verification：`npm run agent:check-sdd -- --slice retrieval-quality-metrics --require-api-guide --report docs/00-context/retrieval-quality-metrics-sdd-completion-report.md`

### T-RETRIEVAL-QUALITY-METRICS-002：实现后端 DTOs 和 calculator
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-004
- Spec：FR-RETRIEVAL-QUALITY-METRICS-001、FR-RETRIEVAL-QUALITY-METRICS-002、FR-RETRIEVAL-QUALITY-METRICS-004
- Owner：backend
- Priority：Must
- Verification：后端单元测试覆盖 deterministic calculation、confidence bands、no-evidence refusal、citation health 和 conservative review eligibility。

### T-RETRIEVAL-QUALITY-METRICS-003：增加 repository reads 和 service orchestration
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-004
- Spec：FR-RETRIEVAL-QUALITY-METRICS-001、FR-RETRIEVAL-QUALITY-METRICS-003、FR-RETRIEVAL-QUALITY-METRICS-004
- Owner：backend
- Priority：Must
- Verification：service tests 证明 run 与 space metrics 使用持久化 metadata，并排除 raw content fields。

### T-RETRIEVAL-QUALITY-METRICS-004：增加只读 metrics controller
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-005、REQ-RETRIEVAL-QUALITY-METRICS-007
- Spec：FR-RETRIEVAL-QUALITY-METRICS-005、FR-RETRIEVAL-QUALITY-METRICS-007
- Owner：backend
- Priority：Must
- Verification：API contract tests 覆盖 run metrics、space metrics、no-evidence behavior、low-confidence behavior 和 safe `NOT_FOUND`。

### T-RETRIEVAL-QUALITY-METRICS-005：增加前端 types、API client calls 和 UI display
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-006、REQ-RETRIEVAL-QUALITY-METRICS-007
- Spec：FR-RETRIEVAL-QUALITY-METRICS-002、FR-RETRIEVAL-QUALITY-METRICS-006、FR-RETRIEVAL-QUALITY-METRICS-007
- Owner：frontend
- Priority：Must
- Verification：`cd frontend && npm run typecheck && npm run test && npm run build`

### T-RETRIEVAL-QUALITY-METRICS-006：增加安全质量信号的 E2E 覆盖
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-006、REQ-RETRIEVAL-QUALITY-METRICS-007
- Spec：FR-RETRIEVAL-QUALITY-METRICS-006、FR-RETRIEVAL-QUALITY-METRICS-007
- Owner：QA
- Priority：Must
- Verification：E2E 使用 mock-safe data 覆盖 API-backed Trusted Ask quality signals。

### T-RETRIEVAL-QUALITY-METRICS-007：运行后端、前端、scan 和 closeout gates
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-007、REQ-RETRIEVAL-QUALITY-METRICS-008
- Spec：FR-RETRIEVAL-QUALITY-METRICS-007、FR-RETRIEVAL-QUALITY-METRICS-008
- Owner：docs
- Priority：Must
- Verification：`cd backend && mvn verify`；`cd frontend && npm run typecheck`；`cd frontend && npm run test`；`cd frontend && npm run build`；`git diff --check`；focused secret/private-path/real-data scan；focused network/dependency scan；`npm run agent:closeout`。

### T-RETRIEVAL-QUALITY-METRICS-008：更新 traceability、roadmaps、commit 和 push
- Requirements：REQ-RETRIEVAL-QUALITY-METRICS-008
- Spec：FR-RETRIEVAL-QUALITY-METRICS-008
- Owner：docs
- Priority：Must
- Verification：traceability 与 roadmaps 展示 completed implementation evidence；commit message 为 `feat: add retrieval quality metrics`；push target 为 `origin develop-leo`。

## 依赖计划

T-RETRIEVAL-QUALITY-METRICS-001 -> T-RETRIEVAL-QUALITY-METRICS-002 -> T-RETRIEVAL-QUALITY-METRICS-003 -> T-RETRIEVAL-QUALITY-METRICS-004 -> T-RETRIEVAL-QUALITY-METRICS-005 -> T-RETRIEVAL-QUALITY-METRICS-006 -> T-RETRIEVAL-QUALITY-METRICS-007 -> T-RETRIEVAL-QUALITY-METRICS-008。

## 风险

- Metrics 是本地确定性质量信号，不是 production retrieval governance。
- 因为 explicit ask-session-citations 不在范围内，现有 Ask evidence 被用作 citation proxy。

## 开放问题

在本次 goal 的预授权边界内没有阻塞问题。

## 完成证据

- T-RETRIEVAL-QUALITY-METRICS-001：已创建 SDD artifact set、execution manifest 与 SDD completion report；SDD gate 已通过。
- T-RETRIEVAL-QUALITY-METRICS-002：后端 DTOs 与 `RetrievalQualityMetricsCalculator` 已计算 evidence coverage、citation health、confidence band、no-evidence refusal 和 conservative review eligibility。
- T-RETRIEVAL-QUALITY-METRICS-003：`RetrievalQualityMetricsService` 读取持久化 Ask run/evidence metadata，并从 metrics DTOs 排除 raw question、answer、source content、prompt 和 provider payload。
- T-RETRIEVAL-QUALITY-METRICS-004：`RetrievalQualityMetricsController` 已暴露只读 run 与 space metrics endpoints，并使用安全 Atlas envelopes。
- T-RETRIEVAL-QUALITY-METRICS-005：已增加前端 TypeScript、API client、Trusted Ask UI 和安全 quality chips。
- T-RETRIEVAL-QUALITY-METRICS-006：E2E mock 与 API-backed Trusted Ask test coverage 已验证可见 quality signals。
- T-RETRIEVAL-QUALITY-METRICS-007：已运行 backend、frontend、E2E、SDD、diff、secret/private-path、network/dependency 与 closeout gates。
- T-RETRIEVAL-QUALITY-METRICS-008：Traceability、slice roadmap、repo status roadmap、commit 与 push evidence 在 closeout 中跟踪。
