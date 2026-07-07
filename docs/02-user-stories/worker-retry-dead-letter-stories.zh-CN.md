# 用户故事：worker-retry-dead-letter

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07

## User Story 1

**Title:** 安全检查失败处理任务

**Story:**
作为 Atlas operator，
我希望查看失败 worker jobs 及其 safe error snapshots，
以便理解处理可靠性问题，同时不看到 secret 或 raw internal 信息。

### Acceptance Criteria

1. **Given** 存在 failed job
   **When** operator 打开 dead-letter 列表
   **Then** job 展示 job type、status、safe error category、source trace、created time 和 operator status。
2. **Given** failure 中包含 unsafe text
   **When** API 和 UI 暴露该 failure
   **Then** raw stack traces、private paths、tokens、cookies、API keys 和 internal endpoints 不出现。

### Notes / Assumptions
- Operator access 使用现有 local/mock-safe API 行为表达；本 slice 不改变 production RBAC。

### Dependencies
- 现有 `ApiEnvelope` 与 safe error patterns。

### Out of Scope
- Production alerting、SIEM、SSO/OIDC、audit policy 改动。

### Open Questions
- 预授权 local v0 范围内无阻塞问题。

## User Story 2

**Title:** 确定性重试 retryable failures

**Story:**
作为 Atlas developer，
我希望 retryable failures 遵循确定性本地 policy，
以便 batch ingest、connector sync 与未来 async processing 可以被稳定测试。

### Acceptance Criteria

1. **Given** max attempts 前发生 retryable failure
   **When** 后端记录 failure
   **Then** job attempt count 增加，并进入 `WAITING_RETRY`，包含 `nextRetryAt` 与 `retryDelaySeconds`。
2. **Given** retry attempts 已耗尽
   **When** 后端记录下一次 failure
   **Then** job 进入 `DEAD_LETTERED`，并创建一条 dead-letter entry。

### Notes / Assumptions
- v0 policy 使用固定本地 delay list，不调度 production worker。

### Dependencies
- Worker job 与 attempt 持久化。

### Out of Scope
- 真实 timers、distributed retry、external queue visibility、exactly-once guarantees。

### Open Questions
- None.

## User Story 3

**Title:** Dead-letter handling 中保留 source trace

**Story:**
作为 SME reviewer，
我希望失败处理记录保留 source trace，
以便把 failure 映射回来源文件、connector item 或 batch context。

### Acceptance Criteria

1. **Given** job 有 source trace metadata
   **When** attempts 与 dead-letter records 被创建
   **Then** records 保留 relative/mock-safe source identifiers 与 review eligibility。
2. **Given** source trace 不完整
   **When** UI 渲染 dead-letter detail
   **Then** 显示安全的 unavailable state，而不是伪造 trusted source。

### Notes / Assumptions
- Source trace 在 local v0 中以 JSON-like text 保存，并保持 safe relative metadata。

### Dependencies
- Markdown standard 与 connector sync source trace conventions。

### Out of Scope
- 真实文档 payload preview 与 raw source content。

### Open Questions
- None.

## User Story 4

**Title:** 本地 retry 或 acknowledge dead-letter entries

**Story:**
作为 Atlas operator，
我希望手动 retry 或 acknowledge dead-letter entry，
以便在生产运维能力前先验证安全本地恢复基础。

### Acceptance Criteria

1. **Given** dead-letter entry 处于 open
   **When** operator 请求 retry
   **Then** 后端创建可预测 retry job 状态，并将 entry 标记为 `RETRIED`，不会重复创建 active retries。
2. **Given** dead-letter entry 处于 open
   **When** operator acknowledge
   **Then** entry 进入 `ACKNOWLEDGED`，后续 acknowledge 请求保持幂等。

### Notes / Assumptions
- Manual actions 只是 local v0 transitions，不 dispatch 真实 worker。

### Dependencies
- Dead-letter API 与状态模型。

### Out of Scope
- Production authorization、audit retention、escalation workflow、incident management。

### Open Questions
- None.

## User Story 5

**Title:** 验证并记录可靠性边界

**Story:**
作为 Atlas maintainer，
我希望 SDD、tests、roadmap 和 traceability 描述 retry/dead-letter 限制，
以便未来 production operations work 从清晰契约开始。

### Acceptance Criteria

1. **Given** slice 完成
   **When** closeout 运行
   **Then** SDD、tasks、traceability、roadmap 与 verification evidence 和实现行为一致。
2. **Given** scans 运行于 changed files
   **When** slice close
   **Then** 不引入 secrets、private paths、real data 或新的 external network dependencies。

### Notes / Assumptions
- 本 slice 可以更新 roadmap 状态，但不得宣称 production readiness。

### Dependencies
- Goal-loop closeout gate。

### Out of Scope
- Production deployment monitoring runbook。

### Open Questions
- None.
