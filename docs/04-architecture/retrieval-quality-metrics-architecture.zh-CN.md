# 架构：retrieval-quality-metrics

状态：已由本次 goal 预授权接受
最后更新：2026-07-07

## 架构摘要

`retrieval-quality-metrics` 在现有 Ask run 和 evidence records 之上增加 additive metrics layer。它不改变 model、vector、graph、wiki、auth、rate-limit、audit 或 provider 语义。

Trace IDs：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-004、REQ-RETRIEVAL-QUALITY-METRICS-005、REQ-RETRIEVAL-QUALITY-METRICS-006、REQ-RETRIEVAL-QUALITY-METRICS-007、REQ-RETRIEVAL-QUALITY-METRICS-008。

## 组件

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

## 后端边界

- Controller 只返回 `ApiEnvelope` DTOs。
- Service 校验 space/run 是否存在，并收集持久化 metadata。
- Calculator 是纯函数且确定性。
- Repository 读取现有 Ask records；不调用 provider、vector、analytics 或 cloud。
- Safe errors 由现有 `GlobalExceptionHandler` 与 safe error factory 处理。

## 前端边界

- TypeScript types 镜像后端安全 DTO。
- API client 增加只读 metrics calls。
- `App.vue` 在现有 Trusted Ask states 上展示紧凑 quality signals。
- UI labels 必须保持保守，不暗示 production governance。

## 持久化决策

MVP quality metrics 不需要新表，因为所有 metrics 都从已存储的 Ask run 与 evidence metadata 推导。这避免冗余 diagnostics 存储，也避免 raw-content capture。除非实现发现缺失安全 metadata field，否则不需要 Flyway migration。

## 安全与数据保护

- Aggregate metrics 不包含 raw questions 或 answers。
- Run-level metrics 可以标识 run 和安全 evidence metadata，但不得包含 raw source text、prompt、provider payload、stack trace、secret、private path 或 internal endpoint。
- Review eligibility 必须保守，不能将 low-confidence、rejected、review-required、missing-trace、failed 或 no-evidence 内容标记为 trusted。

## 架构评审结果

本切片新增 API contracts 与 data flow，因此需要 architecture review。结果：additive API-only architecture 可继续，未改变安全/provider 语义，未声称 production governance。
