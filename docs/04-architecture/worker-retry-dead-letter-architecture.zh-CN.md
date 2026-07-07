# 架构：worker-retry-dead-letter

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07

## 概览

本 slice 在 Atlas metadata control plane 上增加本地 worker reliability read/write model。它是基于现有 Spring Boot + Vue、PostgreSQL/Flyway、API envelope/safe error pattern 的分层功能。不增加 queue engine、scheduler、distributed worker runtime 或 external integration。

## 架构驱动

- 在 failure handling 中保留 source trace 与 review eligibility。
- 让 retry/dead-letter 对本地测试与 operator inspection 保持确定性。
- 复用现有 `ApiEnvelope`、`SafeErrorSanitizer` 与 safe code/category conventions。
- connector、parser、converter、model、vector、storage、search 执行继续留在 adapter 边界后。
- 不改变 production auth/RBAC/audit/secret-manager。

## 高层架构

```text
┌──────────────────────────────────────────────────────────────┐
│ Operators and reviewers                                      │
│ Processing Center · Failed jobs · Dead-letter detail          │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON ApiEnvelope
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Atlas Metadata API                                           │
│ WorkerRecoveryController · DTO mapping · safe API responses   │
├──────────────────────────────────────────────────────────────┤
│ Worker Reliability Domain                                    │
│ WorkerJobService · RetryPolicy · DeadLetter transitions       │
├──────────────────────────────────────────────────────────────┤
│ Persistence                                                  │
│ WorkerJob · WorkerJobAttempt · DeadLetterEntry repositories   │
└─────────────────────────┬────────────────────────────────────┘
                          │ JDBC / JPA
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ PostgreSQL / Flyway                                          │
│ Additive local reliability tables; mock/sample-safe seed only │
└──────────────────────────────────────────────────────────────┘
```

## 组件职责

### Frontend

- Processing Center 渲染 dead-letter list、selected detail、attempts、safe error、source trace 与 local action controls。
- Frontend API types 表达 safe worker recovery responses。
- UI 不渲染 raw exception text 或 raw source content。

### Backend API

- Worker recovery controller 暴露 list/detail/action endpoints。
- Responses 使用 `ApiEnvelope`。
- 已知无效状态返回 safe `CONFLICT`；记录不存在返回 safe `NOT_FOUND`。

### Domain Service

- Worker job service 负责 job creation、attempt recording、retry transition、dead-letter creation、manual retry 和 acknowledge。
- Retry policy 是确定性的：max attempts 3，delay seconds 30 和 120。
- Dead-letter creation 对每个 terminal job 幂等。

### Persistence

- Additive Flyway migration 创建 worker job、job attempt、dead-letter tables。
- Source trace 和 error snapshots 使用 safe JSON/text fields。
- Operator action metadata 是本地 foundation data，不等于 production audit。

## 边界

- 本 slice 内：local reliability metadata、deterministic state transitions、safe inspection UI/API、tests。
- 本 slice 外：production queue、worker scheduler、distributed cluster、exactly-once、saga、真实 connector/API 调用、production RBAC/audit/secret-manager 改动。

## Security And Data Safety

- Safe error snapshots 在 persistence 和 response serialization 前脱敏。
- Source trace 只存储 relative/mock-safe identifiers。
- 不提交或暴露 secrets、private paths、raw stack traces、tokens、cookies、API keys、internal endpoints 或真实公司数据。

## Open Questions

预授权 local v0 范围内无阻塞问题。
