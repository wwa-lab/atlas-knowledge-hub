# 设计：connector-sync-v0

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07

## 设计范围

将 connector sync v0 实现为一个小型全栈 vertical slice：adapter contracts、mock/local fixture adapter、persistence、API、frontend bindings、UI inspection 和 tests。本设计实现 REQ-CONNECTOR-SYNC-V0-001 through REQ-CONNECTOR-SYNC-V0-012。

## 后端模块设计

- Connector adapter contract：`ConnectorAdapter`、`ConnectorCapability`、`ConnectorSyncRequest` 和 `ConnectorSyncResult` 建模 Atlas-level connector 行为。
- Mock adapter：`MockLocalFixtureConnectorAdapter` 返回确定性的 sample-safe items。它不读取文件、不调用 URL、不接受凭证。
- Registry：`ConnectorAdapterRegistry` 列出 capabilities 并按 adapter keys 解析。
- Service：`ConnectorSyncService` 创建 jobs/runs，驱动状态转换，持久化 items/artifacts，并 sanitize safe messages。
- Controller：`ConnectorSyncController` 在 `/api` 下暴露 REST endpoints。
- Mapper/DTO layer：immutable response records 只暴露安全字段。

## 前端设计

Vue 产品外壳在 Knowledge Space product surface 中新增 Connector Sync panel。该 panel 包含：

- Connector definitions list，展示 type、status、capability summary 和 configuration state。
- 针对 `mock-local-fixture` 的 Start sync action。
- Latest run status summary 和 counts。
- Sync item table。
- Selected item detail，展示 source reference、source trace、provenance、confidence、review eligibility、safe error category 和 review-required output artifacts。

## API / Interface Design

Endpoints：
- GET `/api/connector-definitions`
- POST `/api/spaces/{spaceId}/connector-sync-jobs`
- GET `/api/connector-sync-runs/{runId}`
- GET `/api/connector-sync-runs/{runId}/items`

所有 endpoints 使用 `ApiEnvelope`。错误响应使用现有 safe error mapping。

## 校验与错误处理

- 未知 connector keys 返回安全 validation 或 not-found errors。
- 未知 spaces 返回安全 not-found errors。
- 当 run 可以安全创建时，adapter unavailable 创建 FAILED run。
- Adapter exceptions 在持久化前被 sanitize。
- Raw fixture content 和 raw payloads 永不返回。

## UI 状态

- Loading：definitions 或 latest run request 正在进行。
- Empty：尚未启动 run。
- Running：run status 为 QUEUED 或 RUNNING。
- Review required：run status 为 REVIEW_REQUIRED 且 artifact badges 可见。
- Failed：run status FAILED 或 API error safe message。

## 测试考虑

- Adapter output 和 service status transitions 的单元测试。
- Connector definitions、sync creation、run lookup、item listing、safe redaction 和 source trace preservation 的 integration/API contract tests。
- Connector panel rendering 和 review-required indication 的前端测试。
- 现有回归命令：backend verify、frontend typecheck/test/build、closeout gate、diff/secret/network scans。

## 风险 / 取舍

- v0 synchronous execution 简单且确定性强，但不是 production worker model。
- Mock/local fixture adapter 证明边界，但不验证真实 provider 行为。
- Review-required handoff 在未来切片接入 review/Wiki generation 前是 metadata-only。

## 相关任务

T-CONNECTOR-SYNC-V0-001 through T-CONNECTOR-SYNC-V0-009。
