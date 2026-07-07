# 用户故事：connector-sync-v0

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07

## US-CONNECTOR-SYNC-V0-001：发现可用 connector definitions

作为知识空间操作员，我希望看到 mock-safe 的 connector definitions，以便了解 Atlas 可以在不暴露真实凭证或供应商细节的情况下同步哪些来源类型。

验收：
- AC-CONNECTOR-SYNC-V0-001：给定 Connector Sync surface 已打开，当 connector definitions 加载时，每个 connector 展示 type、status、capability summary 和 mock-safe configuration state。
- AC-CONNECTOR-SYNC-V0-006：给定 connector 内部存在不安全实现细节，当 API 响应时，UI 只能收到安全状态和摘要字段。

依赖：REQ-CONNECTOR-SYNC-V0-001、REQ-CONNECTOR-SYNC-V0-002、REQ-CONNECTOR-SYNC-V0-007。

## US-CONNECTOR-SYNC-V0-002：启动 mock/local fixture sync run

作为知识空间操作员，我希望从 local fixture connector 启动 connector sync run，以便 Atlas 在不访问外部系统的情况下演示 connector workflow。

验收：
- AC-CONNECTOR-SYNC-V0-002：给定 mock/local connector 可用，当用户启动 sync 时，Atlas 创建 sync job 和 sync run。
- AC-CONNECTOR-SYNC-V0-003：给定 run 执行，当它完成时，状态确定性地从 QUEUED 到 RUNNING 再到 REVIEW_REQUIRED 或 COMPLETED。

依赖：REQ-CONNECTOR-SYNC-V0-003、REQ-CONNECTOR-SYNC-V0-004、REQ-CONNECTOR-SYNC-V0-009。

## US-CONNECTOR-SYNC-V0-003：检查 source trace 和 provenance

作为 SME reviewer，我希望检查每个 synced item 的 source reference、source trace、provenance、confidence 和 review eligibility，以便 connector-derived content 在可信使用前可以被审核。

验收：
- AC-CONNECTOR-SYNC-V0-004：给定 sync item 存在，当它被展示时，source trace 和 provenance 可见。
- AC-CONNECTOR-SYNC-V0-005：给定 connector output 已生成，当它交接时，output artifact 默认保持 review-required。

依赖：REQ-CONNECTOR-SYNC-V0-005、REQ-CONNECTOR-SYNC-V0-006。

## US-CONNECTOR-SYNC-V0-004：查看安全失败

作为操作员，我希望 connector sync failure 使用安全 category 和脱敏 message，以便在不泄露凭证、endpoint、private path 或 raw payload 的情况下排查状态。

验收：
- AC-CONNECTOR-SYNC-V0-006：给定 connector adapter 失败，当 API 响应或 UI 渲染 item 时，只展示 safe category 和 safe message。

依赖：REQ-CONNECTOR-SYNC-V0-007、REQ-CONNECTOR-SYNC-V0-010。

## US-CONNECTOR-SYNC-V0-005：保留现有可信流程

作为 Atlas 用户，我希望 connector sync v0 与 upload、wiki、review、ask、graph 和 safe-error flows 共存，以便新增 connector foundation 不回归当前产品路径。

验收：
- AC-CONNECTOR-SYNC-V0-007：给定验证执行，当后端和前端 gates 完成时，现有 flows 继续通过。

依赖：REQ-CONNECTOR-SYNC-V0-011、REQ-CONNECTOR-SYNC-V0-012。

## 说明 / 假设

- “knowledge space operator” 对应现有 mock 用户流程，不引入 production RBAC 语义。
- 所有故事都是 v0 mock/local-fixture stories；真实 provider onboarding 不在范围内。

## 开放问题

在当前 goal 的预授权边界内没有阻塞问题。
