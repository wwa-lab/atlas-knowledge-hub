# 功能规格：connector-sync-v0

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07
来源故事：US-CONNECTOR-SYNC-V0-001 through US-CONNECTOR-SYNC-V0-005

## 概览

connector-sync-v0 将 connector sync 从概念推进为本地、可审计、review-required 的 Atlas workflow。它新增 connector definitions、sync jobs、确定性 sync runs、sync items、source trace/provenance、review-required output artifacts、safe error categories 和前端检查能力，并且只使用 mock/local fixture connector。

## 范围

范围内：
- Connector registry 和 connector definition listing。
- 位于 Atlas connector adapter interface 后的 mock/local fixture connector adapter。
- 针对一个 Knowledge Space 创建 sync job 和执行 sync run。
- 持久化带 source trace 和 provenance 的 sync item。
- Review-required output artifact handoff metadata。
- Safe status/error categories 和 redacted messages。
- 前端 connector sync 入口、run status、item details 和 source trace inspection。

范围外：
- 真实外部 connectors、真实凭证、外部网络调用、provider SDKs、OAuth、crawlers、scheduled/background workers、incremental sync、webhooks、production marketplace、production auth/RBAC/audit/secret-manager 变更，或直接调用 parser/converter。

## 功能需求

| ID | 规格需求 |
|---|---|
| FR-CONNECTOR-SYNC-V0-001 | REQ-CONNECTOR-SYNC-V0-001：Connector execution 必须通过 Atlas connector adapter interface。 |
| FR-CONNECTOR-SYNC-V0-002 | REQ-CONNECTOR-SYNC-V0-002：API 必须用安全 metadata 和 mock-safe configuration state 列出 connector definitions。 |
| FR-CONNECTOR-SYNC-V0-003 | REQ-CONNECTOR-SYNC-V0-003：API 必须为 Knowledge Space 和选定 connector definition 创建 sync job/run。 |
| FR-CONNECTOR-SYNC-V0-004 | REQ-CONNECTOR-SYNC-V0-004：Sync runs 必须在请求内确定性地 QUEUED -> RUNNING -> COMPLETED、REVIEW_REQUIRED 或 FAILED。 |
| FR-CONNECTOR-SYNC-V0-005 | REQ-CONNECTOR-SYNC-V0-005：Sync items 必须保存 source reference、source trace、provenance、confidence、review eligibility、status 和 safe error category。 |
| FR-CONNECTOR-SYNC-V0-006 | REQ-CONNECTOR-SYNC-V0-006：由 connector item 生成的 output artifact 默认必须是 REVIEW_REQUIRED。 |
| FR-CONNECTOR-SYNC-V0-007 | REQ-CONNECTOR-SYNC-V0-007：API errors 和 item errors 只能暴露 safe categories/messages。 |
| FR-CONNECTOR-SYNC-V0-008 | REQ-CONNECTOR-SYNC-V0-008：前端必须渲染 connector definitions、sync run status、item list、selected item trace、provenance 和 review-required artifact status。 |
| FR-CONNECTOR-SYNC-V0-009 | REQ-CONNECTOR-SYNC-V0-009：任何代码路径都不得调用真实外部 provider 或要求真实凭证。 |
| FR-CONNECTOR-SYNC-V0-010 | REQ-CONNECTOR-SYNC-V0-010：后端测试必须覆盖 adapter boundary、state transitions、safe redaction、trace preservation 和 review-required handoff。 |
| FR-CONNECTOR-SYNC-V0-011 | REQ-CONNECTOR-SYNC-V0-011：前端验证必须包含 typecheck、tests 和 build。 |
| FR-CONNECTOR-SYNC-V0-012 | REQ-CONNECTOR-SYNC-V0-012：Traceability 和 roadmap evidence 必须记录最终状态和 residual risk。 |

## 状态模型

Run status：
- QUEUED：adapter execution 前已持久化。
- RUNNING：执行已开始。
- REVIEW_REQUIRED：adapter 产生至少一个 review-required output artifact，且没有 blocking failure。
- COMPLETED：所有 items 完成且没有 review-required output。模型允许，但默认 fixture 预计不会出现。
- FAILED：adapter unavailable、request invalid 或所有 items 安全失败。

Item status：
- DISCOVERED：adapter 发现 source metadata。
- FETCHED：local fixture content 从内存 fixture data 读取。
- OUTPUT_CREATED：output artifact metadata 已创建。
- REVIEW_REQUIRED：item output 在可信使用前需要 review。
- FAILED：item 使用 safe category/message 失败。

Safe error categories：
- NONE、VALIDATION、CONNECTOR_UNAVAILABLE、UNSUPPORTED_SOURCE、SOURCE_UNREADABLE、SAFE_SYSTEM。

## 主流程

1. 用户打开 Knowledge Space 并看到 Connector Sync 入口。
2. 前端从 `/api/connector-definitions` 获取 connector definitions。
3. 用户通过 `/api/spaces/{spaceId}/connector-sync-jobs` 为 mock/local fixture connector 启动 sync。
4. 后端校验 space 和 connector definition，创建 sync job 和 run，然后将 run 标记为 RUNNING。
5. 后端通过 connector adapter interface 执行 mock/local fixture adapter。
6. 后端持久化带 source trace/provenance 的 sync items 和 review-required output artifacts。
7. 当存在 review-required output 时，后端将 run 标记为 REVIEW_REQUIRED。
8. 前端展示 run summary、确定性状态、item details、safe errors、source trace、provenance 和 review-required handoff。

## 数据 / 配置需求

核心实体：
- ConnectorDefinition：稳定 connector metadata 和 capability summary。
- ConnectorSyncJob：Knowledge Space 的用户可见 sync request。
- ConnectorSyncRun：job 的确定性执行记录。
- ConnectorSyncItem：每个 source item 的 status、trace、provenance、confidence 和 safe error category。
- ConnectorOutputArtifact：由 sync items 创建的 review-required handoff metadata。

配置：
- v0 唯一 connector definition 是 `mock-local-fixture`。
- Configuration state 是 `MOCK_CONFIGURED`；不接受或返回 raw credentials。

## 非功能需求

- 安全：committed data 或 API errors 中不得出现 secrets、private paths、raw connector payloads、internal endpoints 或 external URLs。
- 可审计：run 和 item records 保留 trace/provenance，支持 review 和未来 audit integration。
- 可靠性：v0 execution 是同步且确定性的，适合本地测试。
- 环境：无外部网络依赖或 provider credential。

## 边界情况

- 未知 connector definition 返回安全 `NOT_FOUND`。
- 无效 connector key 返回 `VALIDATION_FAILED`。
- Adapter unavailable 产生 FAILED run 和 safe category。
- Unsafe adapter text 在存储和 API 响应前被 sanitization。
- 缺少足够 source trace 的 fixture item 标记为 FAILED 或 REVIEW_REQUIRED，绝不直接可信。

## 任务映射

T-CONNECTOR-SYNC-V0-001 through T-CONNECTOR-SYNC-V0-009 实现 FR-CONNECTOR-SYNC-V0-001 through FR-CONNECTOR-SYNC-V0-012。

## 开放问题

在当前 goal 的预授权边界内没有阻塞问题。
