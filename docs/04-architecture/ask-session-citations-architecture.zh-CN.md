# 系统架构：Ask Session Citations

## 概述

Ask Session Citations 通过增量 session aggregate 和更丰富的 citation metadata 扩展现有分层 Ask 实现。架构仍位于 Spring Boot metadata API、PostgreSQL/Flyway persistence、现有 vector/model adapter boundaries 和 Vue Trusted Ask surface 之内。

## 高层架构

```text
Users
  |
  v
Vue Trusted Ask UI
  |
  v
AskController REST / ApiEnvelope
  |
  v
AskService
  |-- AskSession aggregate
  |-- AskRun answer record
  |-- AskEvidence citation snapshot
  |
  v
Spring Data repositories
  |
  v
PostgreSQL / Flyway

现有 adapter 调用保持不变：
AskService -> VectorService -> VectorAdapter
AskService -> ModelService -> ModelAdapter
```

## 架构驱动

- 每个 citation 保留 source trace、confidence 和 review status。
- 保持现有 Ask create/read API 兼容。
- Provider/model/vector 行为继续位于现有 services 和 adapters 之后。
- 增加 session read models，但不改变 production auth/RBAC/audit 语义。
- 使用 safe labels，而不是 raw source content。

## 组件职责

| Component | Responsibility |
|---|---|
| `AskSession` domain | 持久化 session id、space id、title、creator、created/updated timestamps。 |
| `AskRun` domain | 继续拥有单次 question/answer lifecycle；新增 session ownership。 |
| `AskEvidence` domain | 继续拥有 citation snapshot；新增 safe citation label/status fields。 |
| `AskService` | 创建/复用 sessions、创建 runs、持久化 citations、列出/读取 sessions、强制 safe fields。 |
| `AskController` | 通过 `ApiEnvelope` 暴露 Ask run 和 session APIs。 |
| Frontend API client | 调用 Ask run 与 session endpoints，并使用 typed responses。 |
| Trusted Ask UI | 渲染 session summaries、answer history 和 citation detail。 |

## 边界

- UI 不直接调用 vector/model providers。
- Backend 不暴露 raw source snippets、provider payloads、private paths 或 stack traces。
- Session citations 不批准 answer；answer review governance 保持后续切片。
- Persistence changes 使用增量 Flyway migrations。

## Architecture Review Result

| Check | Result |
|---|---|
| Feature boundaries | Pass: session/citation code 保持在 Ask domain。 |
| API envelope | Pass: 新 endpoints 使用现有 `ApiEnvelope`。 |
| Adapter boundaries | Pass: 不新增 direct provider/vector/model calls。 |
| Schema management | Pass: 规划增量 Flyway migration `V15__ask_session_citations.sql`。 |
| Safe evidence | Pass: safe display labels 与 review eligibility 明确。 |

## 风险

- Session list response 未来可能增长；本切片使用有界 recent list contract。
- Citation status names 必须保持前后端一致。
