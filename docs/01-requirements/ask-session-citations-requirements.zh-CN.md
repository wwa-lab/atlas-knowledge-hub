# 需求：Ask Session Citations

## 状态

草案；当 SDD gate 通过且内容仍在本次自主目标预授权边界内时，视为已接受。

## 切片契约

| 字段 | 值 |
|---|---|
| Slice | `ask-session-citations` |
| Wave | Wave 4 / Ask And Graph Productization |
| Goal | 为 Trusted Ask 保留按 session 组织的 question、answer、citation snapshot、source trace、review eligibility 与安全 evidence metadata，方便用户回看依据，并为后续治理切片打基础。 |
| Phase | 基于现有 API-backed Ask 行为的 Phase 4 产品化 |
| 范围内 | Ask sessions、session-scoped Ask answer records、安全 citation snapshots、evidence labels、source locators、review eligibility、API 读写契约、Trusted Ask session history/detail UI、测试、traceability 与 roadmap 证据。 |
| 范围外 | Answer review governance、retrieval quality metrics、graph extraction、provider/model 策略变化、真实公司数据、外部云调用、auth/RBAC/audit/secret/rate-limit 语义变化、生产 prompt/cost/quota 控制。 |

## 需求

| ID | 优先级 | 需求 | 验收 |
|---|---|---|---|
| REQ-ASK-SESSION-CITATIONS-001 | Must | Atlas 必须将 Trusted Ask runs 归入 Knowledge Space 范围内的 Ask session。 | 用户可以用已有 `sessionId` 创建 Ask run；未提供时系统生成 session。 |
| REQ-ASK-SESSION-CITATIONS-002 | Must | 现有 `POST /api/spaces/{spaceId}/ask` 与 `GET /api/ask-runs/{runId}` 行为必须保持向后兼容。 | 现有前端和 API 测试无需 session 字段仍可创建和读取 Ask run。 |
| REQ-ASK-SESSION-CITATIONS-003 | Must | 每个 answer response 必须暴露 session metadata。 | Ask run response 包含 `sessionId`、session title 和 session timestamps。 |
| REQ-ASK-SESSION-CITATIONS-004 | Must | 每个 answer citation 必须保留安全的 source trace snapshot。 | Citation DTO 包含 source chunk id、file item id、source file label、page、section、locator、confidence、review status 和 score。 |
| REQ-ASK-SESSION-CITATIONS-005 | Must | Citation 必须暴露 review eligibility，且不能把未审核 evidence 当作可信。 | Citation 包含 `reviewEligible`、`citationStatus` 和 `excludedReason`；review-required evidence 清楚标记。 |
| REQ-ASK-SESSION-CITATIONS-006 | Must | Citation 不得暴露 raw secrets、private paths、internal endpoints、raw source documents、raw provider payloads 或 raw stack traces。 | 对 session title、evidence labels、source labels 和 safe messages 应用安全文本校验或脱敏。 |
| REQ-ASK-SESSION-CITATIONS-007 | Must | 用户必须能列出某个 Knowledge Space 的最近 Ask sessions。 | `GET /api/spaces/{spaceId}/ask-sessions` 返回安全 session summaries，包括 answer counts 和 latest run state。 |
| REQ-ASK-SESSION-CITATIONS-008 | Must | 用户必须能读取一个 session 及其 answer history 和 citations。 | `GET /api/ask-sessions/{sessionId}` 返回 session metadata 和按顺序排列的 Ask runs/citation snapshots。 |
| REQ-ASK-SESSION-CITATIONS-009 | Must | 低置信、缺失 source trace 或 review-required evidence 必须按 Ask policy 被排除或清楚标记。 | Approved-only 模式只返回可信 citation；include-review-required 模式将 citation 标为 `REVIEW_REQUIRED`。 |
| REQ-ASK-SESSION-CITATIONS-010 | Should | Trusted Ask UI 应展示 session-aware answer history 和当前 answer citation detail。 | Ask 界面展示最近 session、所选 session answer history，以及带 label/review state 的 citation detail。 |
| REQ-ASK-SESSION-CITATIONS-011 | Must | API contract tests 必须覆盖 session creation、session reuse、citation metadata 和 session read 行为。 | 后端 integration tests 验证 create/read/list contract 和安全 citation 字段。 |
| REQ-ASK-SESSION-CITATIONS-012 | Must | Traceability 与 roadmap docs 必须记录实现证据和残留风险。 | closeout 前更新 slice traceability 与 roadmap status 文件。 |

## 假设

- 现有 `AskRun` 和 `AskEvidence` 是 answer 与 citation snapshot 的正确基础。
- Ask session 持久化是增量能力，不改变 provider、vector、auth、RBAC、audit、secret 或 rate-limit 行为。
- 请求未提供 title 时，session title 可以由首个安全 question 生成。

## 约束

- 仅使用 mock/sample-safe 数据。
- 不新增外部网络调用或 provider 依赖。
- Citation label 与 source locator 必须是安全展示字符串，不是 raw document content。
- LLM-generated answer 在后续治理切片批准前保持 `REVIEW_REQUIRED`。
