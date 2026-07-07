# 功能规格：Ask Session Citations

> **Source stories:** US-ASK-SESSION-CITATIONS-001 through US-ASK-SESSION-CITATIONS-004  
> **Spec status:** SDD gate 通过后由自主预授权接受  
> **Last updated:** 2026-07-07

## 概述

Ask Session Citations 为现有 Trusted Ask API 增加持久 session 分组和更丰富的 answer citation snapshots。该切片保持现有 Ask create/read 行为，同时新增 session list/detail APIs 与 UI history/detail 展示。

## Actors

- **知识用户:** 提问并查看 answer evidence。
- **SME reviewer:** 在信任答案前检查 citation safety、review status 和 source trace。
- **Atlas maintainer:** 验证 contract compatibility 和安全约束。

## 范围

### 范围内

- 按 Knowledge Space 归属的 Ask session persistence。
- 带可选 `sessionId` 和 `sessionTitle` 的向后兼容 Ask run 创建。
- Session summaries 与 session detail APIs。
- Ask evidence response 上的 citation snapshot 字段。
- 最近 sessions、所选 session answer history 和 citation detail 的 UI 展示。
- Backend unit/integration/API contract tests 与 frontend type/unit/build tests。

### 范围外

- Answer review governance workflow。
- Retrieval quality metrics。
- Graph extraction from Wiki。
- Provider/model adapter 策略变化。
- Auth/RBAC/audit/secret/rate-limit 语义变化。
- 生产 prompt tuning、cost guards、quotas 或 billing controls。

## 功能需求

### Session Lifecycle

- **FR-01:** 当请求省略 `sessionId` 时，Ask API 必须创建 `ask_session`。
- **FR-02:** 当 `sessionId` 属于请求 Knowledge Space 时，Ask API 必须把 run 附加到该 session。
- **FR-03:** 对跨 space 或未知 session id，Ask API 必须返回安全 validation/not-found response。
- **FR-04:** Session title 必须是来自 request `sessionTitle` 或首个 question 的安全展示字符串。

### Answer And Citation Contract

- **FR-05:** `AskRunResponse` 必须包含 `sessionId`、`sessionTitle` 和现有 answer 字段。
- **FR-06:** `AskEvidenceResponse` 必须保留现有 evidence 字段，并新增 `citationId`、`evidenceLabel`、`sourceLocator`、`citationStatus`、`reviewEligible` 与 `excludedReason`。
- **FR-07:** Approved 或 published evidence 必须为 `reviewEligible=true` 且 `citationStatus=ELIGIBLE`。
- **FR-08:** 由显式 policy 包含的 review-required evidence 必须为 `reviewEligible=false` 且 `citationStatus=REVIEW_REQUIRED`。
- **FR-09:** 缺失 source trace 的 evidence 不得被视为 eligible citation。

### Session Read APIs

- **FR-10:** `GET /api/spaces/{spaceId}/ask-sessions` 必须按最近更新时间返回 recent session summaries。
- **FR-11:** `GET /api/ask-sessions/{sessionId}` 必须返回 session metadata 以及带 citations 的有序 Ask runs。
- **FR-12:** Session APIs 必须使用现有 `ApiEnvelope` 和 safe error handling patterns。

### Frontend Behavior

- **FR-13:** Trusted Ask UI 必须为所选 Knowledge Space 展示 recent session summaries。
- **FR-14:** Trusted Ask UI 必须允许选择 session 并展示有序 answer history。
- **FR-15:** Citation detail 必须展示 safe labels、locator、confidence、review status 与 review eligibility。
- **FR-16:** UI error states 必须使用安全 fallback messages，不展示 raw internal errors。

## 非功能需求

- **Security:** Session 或 citation responses 不得返回 raw secrets、tokens、private paths、internal endpoints、raw source content 或 raw stack traces。
- **Compatibility:** 现有 Ask create/read clients 必须在没有新增 request fields 时继续工作。
- **Traceability:** Citation snapshots 必须在可用时保留 source chunk id、file item id、source label、page/section locator、confidence、review status、score 和 creation time。
- **Review safety:** Generated answers 保持 `REVIEW_REQUIRED`；未审核 evidence 被标记或排除。
- **No external calls:** 本切片不得在现有 adapter boundaries 之外新增 provider、vector、model、parser、storage 或 search 调用。

## API Surface

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/spaces/{spaceId}/ask` | 创建 Ask run，并创建或复用 session。 |
| GET | `/api/ask-runs/{runId}` | 读取一个带 session 和 citations 的 Ask run。 |
| GET | `/api/spaces/{spaceId}/ask-sessions` | 列出某个 Knowledge Space 的安全 session summaries。 |
| GET | `/api/ask-sessions/{sessionId}` | 读取一个带 answer history 和 citations 的 session。 |

## 验收矩阵

| Requirement | Observable Check |
|---|---|
| REQ-ASK-SESSION-CITATIONS-001 | API contract test 在无 `sessionId` 时创建 run，并收到生成的 session metadata。 |
| REQ-ASK-SESSION-CITATIONS-002 | 现有 Ask API 和 frontend tests 继续通过。 |
| REQ-ASK-SESSION-CITATIONS-004 | Contract tests 断言 citation source chunk、page/section、label、locator、confidence 和 review status。 |
| REQ-ASK-SESSION-CITATIONS-005 | Contract tests 断言 approved 与 review-required evidence 的 `reviewEligible` 和 `citationStatus`。 |
| REQ-ASK-SESSION-CITATIONS-007 | Contract tests 断言 session list response ordering 和安全 summary shape。 |
| REQ-ASK-SESSION-CITATIONS-010 | Frontend tests 断言 session/citation labels 渲染。 |

## 风险

- 现有 Ask test fixtures 可能需要增量字段；TypeScript 中必要字段应保持向后兼容。
- Session title generation 必须保持安全，不能把 unsafe question text 作为 raw display label 存储。

## Open Questions

- None。
