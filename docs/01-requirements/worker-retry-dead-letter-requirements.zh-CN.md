# 需求：worker-retry-dead-letter

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07
Wave：Wave 5 / Connector And Operations

## 目标

为 Atlas 的 batch ingest、connector sync 与未来 async processing 建立本地、确定性、可观测、可追踪的 retry 与 dead letter 可靠性契约；不引入生产队列、分布式 worker、定时 worker、真实 connector、外部 API、真实凭证或公司数据。

## 范围

范围内：

- worker job、job attempt、retry policy、retry schedule metadata、terminal failure classification、dead-letter entry、safe error snapshot、source trace、operator action metadata、review/processing eligibility。
- 可通过后端 service 与 API contract test 验证的本地确定性 retry 状态转换。
- 安全 dead-letter 检查 API 与前端 Processing Center 运维查看入口。
- manual retry 与 acknowledge 仅作为本地 v0 安全状态转换。
- 复用现有 safe error pattern 做脱敏。

范围外：

- Kafka、RabbitMQ、SQS、PubSub、Redis Queue、真实 scheduled worker、distributed worker cluster、exactly-once delivery、saga orchestration、真实 connector/provider 调用、production auth/RBAC/audit/secret-manager 改动、破坏性 migration、raw stack trace、raw exception message、private path、token、cookie、API key、真实公司数据。

## 需求

| ID | Requirement | Priority |
|---|---|---|
| REQ-WORKER-RETRY-DEAD-LETTER-001 | Atlas 必须定义 worker job 模型，可表示 batch ingest、connector sync 与未来 async processing job，且不绑定单一队列引擎。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-002 | 每个 worker job 必须追踪 attempts，包括 attempt number、status、timestamps、safe error snapshot、retryable classification 与 source trace。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-003 | Retry policy 必须是本地确定性的，并记录 max attempt count 与固定 delay schedule，便于测试。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-004 | Retryable failure 必须增加 attempt，并把 job 进入可预测的等待重试状态，包含 next retry metadata。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-005 | Non-retryable failure 或超过最大次数必须进入 terminal failed，并创建一条 dead-letter entry。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-006 | Dead-letter entry 必须保留 safe error code/category、safe message、attempt summary、source trace、created time 和 operator-facing status。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-007 | Error snapshot 必须复用现有 safe error 脱敏模式，不得暴露 raw exception、stack trace、secret、private path、internal endpoint、token、cookie、API key 或真实 source content。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-008 | Source trace 必须从来源 batch、file、connector item 或未来 job metadata 保留，并保持 relative/mock-safe。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-009 | 后端 API 必须使用现有 `ApiEnvelope` pattern 暴露 failed/dead-letter job 列表与详情。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-010 | 仅当行为安全、局部、可预测且有测试时，才实现 manual retry 与 acknowledge endpoint。 | Should |
| REQ-WORKER-RETRY-DEAD-LETTER-011 | 前端 Processing Center 或运维入口必须展示 failed/dead-letter jobs、attempts、safe error、source trace、review/blocked states，且不暴露 raw internals。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-012 | 测试必须覆盖 retry transitions、terminal failure、dead-letter creation、safe redaction、source trace、manual retry 和 acknowledge 行为。 | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-013 | 文档、roadmap、traceability 与 closeout evidence 必须记录范围、排除项、验证和生产化缺口。 | Must |

## 约束

- 仅使用 mock/sample 数据。
- parser、converter、connector、model、vector、storage、search 执行必须保持在 adapter 边界后。
- 不引入外部网络依赖或 provider SDK 调用。
- 不改变 production auth、RBAC、audit、secret-manager 或 provider strategy。
- 仅允许 additive、non-destructive persistence changes。

## 验收

- Retryable failure 生成新 attempt 与 next retry metadata。
- Terminal failure 正好创建一条 dead-letter entry。
- Dead-letter detail 暴露 safe error、attempt summary、source trace、created time 与 operator status。
- 如果实现 manual retry/acknowledge，其行为必须安全、可预测。
- 完成前 backend/frontend verification 与 `npm run agent:closeout` 必须通过。
