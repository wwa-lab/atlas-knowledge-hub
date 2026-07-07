# 设计：worker-retry-dead-letter

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07

## 概览

本设计增加一个小型后端 domain，用于 local retry/dead-letter state，并在 Processing Center UI 中扩展 operations panel。它遵循现有 Spring Boot API envelope pattern 与 Vue mock/API-backed data pattern。

## Backend Design

### Domain

- `WorkerJob`：拥有 job identity、job type、subject、status、retry metadata、safe latest error 和 source trace。
- `WorkerJobAttempt`：job 的 append-only attempt history。
- `DeadLetterEntry`：terminal failure inspection 与 operator action record。
- `LocalRetryPolicy`：确定性的 max attempts 与 delay calculation。
- `WorkerJobService`：transition coordinator；controllers 将 workflow logic 委托给它。

### API

- `GET /api/worker-jobs/failed`
- `GET /api/worker-jobs/{jobId}`
- `GET /api/dead-letter-entries`
- `GET /api/dead-letter-entries/{entryId}`
- `POST /api/dead-letter-entries/{entryId}/retry`
- `POST /api/dead-letter-entries/{entryId}/acknowledge`

### Error Handling

- 所有 stored 与 returned safe messages 都使用现有 `SafeErrorSanitizer`。
- 可复用现有 safe codes：`VALIDATION_FAILED`、`NOT_FOUND`、`CONFLICT`、`SAFE_SYSTEM_ERROR`。
- Worker-specific safe categories 是 domain fields，不引入竞争性的 API error envelope。

### Seed / Fixture Behavior

如现有产品测试需要，后端可以 seed 一个 mock dead-letter scenario 供本地 UI/API inspection 使用。Seed data 必须只使用 sample-safe IDs 与 relative source traces。

## Frontend Design

### Processing Center Surface

UI 增加 operations section 或 panel，包含：

- Failed/dead-letter job list。
- Status 与 safe category badges。
- Attempt timeline。
- Source trace panel。
- Safe error snapshot panel。
- Review eligibility / blocked state indicator。
- 后端支持时展示 Retry 和 Acknowledge controls。

### UI Rules

- 不展示 raw stack traces、private paths、raw exception names、internal endpoints、tokens、cookies 或 API keys。
- 空 source trace 展示 safe unavailable state。
- Acknowledged entries 作为 resolved operations records 继续可见。
- Retry action 可预测地更新 selected detail。

## Test Design

Backend:

- Max attempts 前 retryable failure。
- Retry exhaustion 创建一条 dead-letter entry。
- Non-retryable failure 创建一条 dead-letter entry。
- Unsafe error text 被脱敏。
- Manual retry 与 acknowledge transitions 安全。
- API contract tests 覆盖 list/detail/actions 与 safe errors。

Frontend:

- Worker/dead-letter models 通过 type checks。
- Component/data tests 验证 list/detail mapping 与 safe display。
- Build 验证 runtime type 不漂移。

## Risks / Tradeoffs

- Local metadata 不证明 production worker recovery。
- Manual retry 不 dispatch 真实工作；它只记录安全本地恢复基础。
- Operator action metadata 不是 production audit log。

## Open Questions

预授权 local v0 范围内无阻塞问题。
