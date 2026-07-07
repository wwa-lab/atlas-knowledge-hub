# 功能规格：worker-retry-dead-letter

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07
Source stories: US-WORKER-RETRY-DEAD-LETTER-001 through US-WORKER-RETRY-DEAD-LETTER-005

## 概览

Atlas 需要一个本地可靠性契约，用于 batch ingest、connector sync 或未来 async execution 启动后可能失败的处理工作。本 slice 增加确定性的 job attempt tracking、retry 状态转换、terminal dead-letter handling、safe error snapshot、source trace preservation、inspection APIs 和 Processing Center operations UI，但不引入生产队列或 scheduled worker。

## Actors

- Operator：检查 failed jobs，并执行安全本地 retry/acknowledge actions。
- Developer：验证确定性 retry 和 dead-letter 状态转换。
- SME reviewer：使用 source trace 与 review eligibility 理解失败输出是否可恢复。
- Platform administrator：理解 retry policy 限制，且不误认为已经生产运维就绪。

## Functional Requirements

| ID | Spec Requirement |
|---|---|
| FR-WORKER-RETRY-DEAD-LETTER-001 | REQ-WORKER-RETRY-DEAD-LETTER-001：表示 batch ingest、connector sync 与未来 async processing 的 worker jobs，且不绑定 Atlas 到某个 queue engine。 |
| FR-WORKER-RETRY-DEAD-LETTER-002 | REQ-WORKER-RETRY-DEAD-LETTER-002：每个 job attempt 追踪 status、attempt number、timestamps、safe error snapshot、retryable classification 与 source trace。 |
| FR-WORKER-RETRY-DEAD-LETTER-003 | REQ-WORKER-RETRY-DEAD-LETTER-003：应用具有 max attempts 与固定 delay metadata 的本地确定性 retry policy。 |
| FR-WORKER-RETRY-DEAD-LETTER-004 | REQ-WORKER-RETRY-DEAD-LETTER-004：未耗尽前的 retryable failures 进入 `WAITING_RETRY`。 |
| FR-WORKER-RETRY-DEAD-LETTER-005 | REQ-WORKER-RETRY-DEAD-LETTER-005：non-retryable failures 或耗尽 attempts 后进入 `DEAD_LETTERED` 并创建一条 dead-letter entry。 |
| FR-WORKER-RETRY-DEAD-LETTER-006 | REQ-WORKER-RETRY-DEAD-LETTER-006：Dead-letter entries 保留 safe error、attempt summary、source trace、created time 与 operator status。 |
| FR-WORKER-RETRY-DEAD-LETTER-007 | REQ-WORKER-RETRY-DEAD-LETTER-007：Safe error snapshots 通过现有 safe error patterns 脱敏。 |
| FR-WORKER-RETRY-DEAD-LETTER-008 | REQ-WORKER-RETRY-DEAD-LETTER-008：Source trace 保持 relative/mock-safe，并在 job、attempt 与 dead-letter records 中保留。 |
| FR-WORKER-RETRY-DEAD-LETTER-009 | REQ-WORKER-RETRY-DEAD-LETTER-009：APIs 通过 `ApiEnvelope` 暴露 failed/dead-letter list 与 detail。 |
| FR-WORKER-RETRY-DEAD-LETTER-010 | REQ-WORKER-RETRY-DEAD-LETTER-010：Manual retry 与 acknowledge 使用安全本地 v0 transitions。 |
| FR-WORKER-RETRY-DEAD-LETTER-011 | REQ-WORKER-RETRY-DEAD-LETTER-011：前端渲染 failed/dead-letter jobs、attempts、safe error、source trace 与 blocked/review states。 |
| FR-WORKER-RETRY-DEAD-LETTER-012 | REQ-WORKER-RETRY-DEAD-LETTER-012：测试覆盖 retry、terminal failure、redaction、source trace、retry action 与 acknowledge action。 |
| FR-WORKER-RETRY-DEAD-LETTER-013 | REQ-WORKER-RETRY-DEAD-LETTER-013：Roadmap、traceability、tasks 与 verification evidence 和实现保持一致。 |

## 状态模型

Worker job status:

- `QUEUED`：job record 已存在，可进入工作。
- `RUNNING`：attempt 正在执行或记录中。
- `WAITING_RETRY`：上一 attempt retryable failure，且存在 next retry metadata。
- `SUCCEEDED`：终态成功。
- `DEAD_LETTERED`：终态失败，并有 dead-letter entry。
- `ACKNOWLEDGED`：operator 已确认 terminal failure。

Attempt status:

- `RUNNING`
- `FAILED_RETRYABLE`
- `FAILED_TERMINAL`
- `SUCCEEDED`

Dead-letter status:

- `OPEN`
- `RETRIED`
- `ACKNOWLEDGED`

Safe error category:

- `NONE`
- `VALIDATION`
- `CONNECTOR_UNAVAILABLE`
- `UNSUPPORTED_SOURCE`
- `SOURCE_UNREADABLE`
- `RATE_LIMITED`
- `SAFE_SYSTEM`

## Retry Policy

- `maxAttempts`: 3。
- Delay schedule in seconds：attempt 1 failure -> 30，attempt 2 failure -> 120。
- Retryable failure 且仍有剩余 attempts 时进入 `WAITING_RETRY`，并设置 `nextRetryAt`。
- Non-retryable failure 直接进入 `DEAD_LETTERED`。
- Retryable failure 达到 max attempts 后进入 `DEAD_LETTERED`。
- Policy 只记录 schedule metadata；不运行 scheduler，也不 dispatch 真实 worker。

## 主流程

1. 本地 workflow 为 batch ingest、connector sync 或未来 async processing 创建 worker job。
2. 后端将 attempt 记录为 `RUNNING`。
3. 调用方记录 success、retryable failure 或 non-retryable failure。
4. 对未耗尽的 retryable failure，job 进入 `WAITING_RETRY`。
5. 对 terminal failure，job 进入 `DEAD_LETTERED`，并创建一条 dead-letter entry。
6. Operations API 返回 dead-letter entries、attempts 和 source trace。
7. 前端 Processing Center 展示 failed/dead-letter jobs 与安全详情。
8. Operators 可以对 `OPEN` entries 请求本地 retry 或 acknowledge。

## Manual Actions

Manual retry:

- 仅允许 `OPEN` dead-letter entries。
- 将 entry 标记为 `RETRIED`。
- 将关联 job 创建或重置为 `WAITING_RETRY`，包含确定性 retry metadata。
- 同一 entry 重复 retry 请求返回相同可预测状态，不创建重复 open entries。

Manual acknowledge:

- 允许 `OPEN` 或已 `ACKNOWLEDGED` entries。
- 将 entry 与 job 标记为 `ACKNOWLEDGED`。
- 重复 acknowledge 请求幂等。

## API Acceptance Matrix

| Check | Requirement | Observable result |
|---|---|---|
| AC-WORKER-RETRY-DEAD-LETTER-001 | REQ-WORKER-RETRY-DEAD-LETTER-001, 002 | Job detail 包含 job type、status、attempts、source trace 与 created/updated timestamps。 |
| AC-WORKER-RETRY-DEAD-LETTER-002 | REQ-WORKER-RETRY-DEAD-LETTER-003, 004 | Max attempts 前 retryable failure 返回 `WAITING_RETRY` 与 next retry metadata。 |
| AC-WORKER-RETRY-DEAD-LETTER-003 | REQ-WORKER-RETRY-DEAD-LETTER-005, 006 | Terminal failure 创建正好一条 `OPEN` dead-letter entry。 |
| AC-WORKER-RETRY-DEAD-LETTER-004 | REQ-WORKER-RETRY-DEAD-LETTER-007 | Unsafe error strings 从 persisted snapshots 与 API responses 中脱敏。 |
| AC-WORKER-RETRY-DEAD-LETTER-005 | REQ-WORKER-RETRY-DEAD-LETTER-008 | Source trace 在 job、attempt、dead-letter 与 UI detail 中保持 relative/mock-safe。 |
| AC-WORKER-RETRY-DEAD-LETTER-006 | REQ-WORKER-RETRY-DEAD-LETTER-009 | List/detail APIs 使用 `ApiEnvelope` 和 safe error semantics。 |
| AC-WORKER-RETRY-DEAD-LETTER-007 | REQ-WORKER-RETRY-DEAD-LETTER-010 | Manual retry 与 acknowledge transitions 确定且在规定处幂等。 |
| AC-WORKER-RETRY-DEAD-LETTER-008 | REQ-WORKER-RETRY-DEAD-LETTER-011 | UI 展示 failed/dead-letter list 与 detail，且不暴露 raw internals。 |
| AC-WORKER-RETRY-DEAD-LETTER-009 | REQ-WORKER-RETRY-DEAD-LETTER-012, 013 | Backend/frontend tests、scans、SDD gate 与 closeout gate 通过。 |

## Edge States

- 未知 dead-letter 或 job ID 返回 safe `NOT_FOUND`。
- 无效 action state 返回 safe `CONFLICT`。
- 空 source trace 渲染为 unavailable，不视为 trusted。
- Acknowledgement 后 retry 返回 safe `CONFLICT`。
- Unsafe error input 在持久化与 response mapping 前脱敏。

## Non-Functional Requirements

- Security：API 或 UI 不出现 raw exception、stack trace、secret、token、cookie、API key、private path、internal endpoint 或 real source content。
- Reliability：本地确定性状态转换可重复测试。
- Auditability foundation：本地记录 operator action metadata，但不宣称 production audit readiness。
- Environment：无 external queue、无 scheduler、无 external network、无 real provider credential。

## Open Questions

预授权 local v0 范围内无阻塞问题。
