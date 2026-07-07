# 需求：connector-sync-v0

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07
Wave：Wave 5 / Connector And Operations

## 切片契约

Atlas 必须在不绑定真实外部供应商的前提下提供 connector sync v0 的产品与工程骨架。本切片引入 connector definition、sync job、sync run、sync item、source trace/provenance、review-required handoff artifact，以及安全的状态和错误展示，且只使用 mock 或 local fixture 数据。

## 需求

| ID | 需求 |
|---|---|
| REQ-CONNECTOR-SYNC-V0-001 | Atlas 必须暴露 adapter-first 的 connector 边界，让产品逻辑依赖 Atlas connector 概念，而不是供应商 API。 |
| REQ-CONNECTOR-SYNC-V0-002 | Atlas 必须提供 connector registry，包含 connector definition metadata、type、capability summary、mock-safe configuration status 和 review policy。 |
| REQ-CONNECTOR-SYNC-V0-003 | Atlas 必须允许用户在 Knowledge Space 中基于已注册的 mock/local fixture connector 创建或查看 sync job。 |
| REQ-CONNECTOR-SYNC-V0-004 | Atlas 必须创建确定性的 sync run，并使用 QUEUED、RUNNING、COMPLETED、FAILED、REVIEW_REQUIRED 状态。 |
| REQ-CONNECTOR-SYNC-V0-005 | Atlas 必须持久化 sync item，并保留 item status、safe error category、source reference、source trace、provenance、confidence 和 review eligibility。 |
| REQ-CONNECTOR-SYNC-V0-006 | connector-derived output artifact 默认必须是 review-required，未经 SME review 或确定性校验不得成为 approved Wiki、Ask 或 Graph knowledge。 |
| REQ-CONNECTOR-SYNC-V0-007 | API 响应必须使用现有 `ApiEnvelope` 和 safe error pattern；secrets、private paths、raw payloads、internal endpoints 等不安全值必须被脱敏或省略。 |
| REQ-CONNECTOR-SYNC-V0-008 | 前端必须提供 connector sync 入口、connector list、sync run status、sync item inspection、source trace/provenance view 和 review-required 提示。 |
| REQ-CONNECTOR-SYNC-V0-009 | 实现只能使用 mock/local fixture connector data，不得调用 Confluence、SharePoint、Google Drive、Lark、Notion、Slack、GitHub、web crawler、database 或外部 API。 |
| REQ-CONNECTOR-SYNC-V0-010 | 后端测试必须覆盖 adapter boundary、确定性状态转换、safe error redaction、source trace preservation 和 review-required output handoff。 |
| REQ-CONNECTOR-SYNC-V0-011 | 前端 typecheck、tests 和 build 必须覆盖 connector sync v0 UI，且不回归 upload、wiki、review、ask、graph 和 safe-error flows。 |
| REQ-CONNECTOR-SYNC-V0-012 | SDD、tasks、traceability、roadmap 和 closeout evidence 必须与实际实现保持一致。 |

## 明确排除

- 不接入真实外部 connector provider、网络调用、OAuth、API key、cookie、service account、crawler 或 provider SDK。
- 不使用真实公司文档、private URLs、internal endpoints、raw connector payloads 或 production credentials。
- 不实现 production connector marketplace、scheduled sync、incremental sync、webhook sync、distributed worker、production auth/RBAC/audit/secret-manager 变更或破坏性 migration。
- connector UI 或 service logic 不得直接依赖 parser/converter/model/vector/storage。

## 验收标准

- AC-CONNECTOR-SYNC-V0-001：用户可以在产品 UI 中看到 connector sync v0 入口。
- AC-CONNECTOR-SYNC-V0-002：用户可以启动并查看一个 mock/local fixture sync run。
- AC-CONNECTOR-SYNC-V0-003：sync run 和 sync item 状态是确定性且可检查的。
- AC-CONNECTOR-SYNC-V0-004：每个 sync item 保留 source trace 和 provenance。
- AC-CONNECTOR-SYNC-V0-005：connector-derived output artifact 默认进入 review-required。
- AC-CONNECTOR-SYNC-V0-006：API 错误安全，不泄露 secrets、private paths、raw payloads 或 internal endpoints。
- AC-CONNECTOR-SYNC-V0-007：现有 upload、wiki、review、ask、graph 和 rate-limit-safe-errors flows 继续通过验证。

## 假设

- connector sync v0 是 Wave 5 prototype foundation，使用真实后端持久化，但 adapter execution 仅为 mock-only。
- 当前 Spring Boot + PostgreSQL/Flyway 后端模式已经启用，因此需要 additive Flyway migration。
- 当前 Vue 产品外壳仍是单文件 surface；本切片在现有 space 体验中添加最小范围的 connector UI 区域。

## 开放问题

在当前 goal 的预授权边界内没有阻塞问题。
