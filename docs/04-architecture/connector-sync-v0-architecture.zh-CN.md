# 架构：connector-sync-v0

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07

## 概览

connector-sync-v0 使用现有 Atlas 分层 Spring Boot 和 Vue 产品架构。它新增 connector adapter boundary、mock/local fixture adapter、connector sync application service、增量 persistence tables、API endpoints 和紧凑的前端检查 surface。

## 架构驱动

- REQ-CONNECTOR-SYNC-V0-001：adapter-first connector boundary。
- REQ-CONNECTOR-SYNC-V0-004：确定性 sync run 状态转换。
- REQ-CONNECTOR-SYNC-V0-005：持久 source trace 和 provenance。
- REQ-CONNECTOR-SYNC-V0-006：review-required output handoff。
- REQ-CONNECTOR-SYNC-V0-007：safe errors 且不泄露不安全信息。
- REQ-CONNECTOR-SYNC-V0-009：仅 mock/local fixture，无外部网络。

## 高层架构

```text
Users
  |
  v
Vue Product Shell
  Connector Sync panel: definitions, run status, item trace, review handoff
  |
  | REST / JSON using ApiEnvelope
  v
Spring Boot Metadata API
  ConnectorSyncController
  |
  v
Connector Sync Service
  validates space + connector definition
  owns QUEUED -> RUNNING -> terminal transitions
  sanitizes safe status/error details
  |
  +--> Connector Adapter Registry
  |      MockLocalFixtureConnectorAdapter only in v0
  |
  v
PostgreSQL / Flyway additive metadata
  connector_definition
  connector_sync_job
  connector_sync_run
  connector_sync_item
  connector_output_artifact
```

## 组件职责

- Frontend Connector Sync panel：提供用户入口，调用 typed API helpers，渲染 run/item status，并在不接受凭证的情况下展示 source trace/provenance。
- ConnectorSyncController：通过 `ApiEnvelope` 暴露 connector definition listing、sync job creation、run lookup 和 run item listing。
- ConnectorSyncService：编排 job/run creation、adapter resolution、deterministic transitions、item persistence、review-required artifact creation 和 safe messages。
- ConnectorAdapterRegistry：返回安全 connector capabilities，并按 key 解析 adapters。
- MockLocalFixtureConnectorAdapter：返回确定性的 local fixture items，包含 source references、trace、provenance、confidence 和 review-required output hints。
- Persistence layer：通过 additive Flyway migration 和 JPA repositories 保存 connector definitions、jobs、runs、items 和 output artifacts。

## 边界

- Connector adapters 暴露 Atlas 概念：connector key、source references、source trace、provenance、confidence 和 review-required output。它们不暴露 provider SDKs 或供应商 request/response payloads。
- Connector sync 不直接调用 parser、converter、model、vector、storage 或 search adapters。未来切片可以通过文档化契约把 review-required artifacts 交给 review/Wiki pipelines。
- 现有 safe error infrastructure 继续作为 API error boundary。

## 安全 / 数据安全

- v0 requests 不接受凭证。
- Connector configuration status 仅为 mock/status-only。
- Unsafe strings 在持久化和 API response 前被 sanitize。
- Fixture data 只使用 sample-safe relative identifiers。
- Raw connector payloads 不持久化、不返回。

## 风险 / 取舍

| 风险 | 缓解 |
|---|---|
| 用户可能从 connector 命名误解为已有真实 provider 支持。 | 使用 `mock-local-fixture`，并在 UI copy/status 标明 v0 仅为 local fixture。 |
| 同步执行不是生产 worker 架构。 | 记录为 v0 prototype foundation；scheduled/background worker 仍排除。 |
| Connector output 可能被误认为 trusted Wiki knowledge。 | output artifacts 持久化为 REVIEW_REQUIRED，并渲染明确 review-required 标记。 |

## 相关任务

T-CONNECTOR-SYNC-V0-001 through T-CONNECTOR-SYNC-V0-009。
