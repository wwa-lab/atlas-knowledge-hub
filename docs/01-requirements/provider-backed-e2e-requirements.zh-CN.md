# 需求：Provider-Backed E2E

## 状态

草案。轻量 SDD-only slice。本轮不包含任何实现代码。

## Slice 契约

| 字段 | 内容 |
|---|---|
| 目标 | 本地开发者可以通过环境变量显式提供 DeepSeek API key，一条命令启动本地 Atlas stack，并在浏览器页面完成真实 provider-backed Ask E2E，且只使用 mock/sample knowledge data。 |
| Slice | `provider-backed-e2e` |
| 阶段 | 第三层验收 / Phase 4+ provider 集成验证 |
| 范围 | Opt-in 第三层 E2E 命令、本地服务编排、页面里的 provider-backed Ask、ModelAdapter 边界内的 DeepSeek 调用路径、安全失败行为，以及 secret/log/artifact hygiene gate。 |
| 不包含 | 本轮文档 pass 的代码实现、默认 first-layer 或 second-layer E2E 变更、真实公司文档、真实 provider key 进入源码/文档/日志/截图/trace/git、生产 auth/RBAC、provider 成本治理、非 DeepSeek provider 选择。 |
| 行为源 | 本需求文档、`docs/03-spec/provider-backed-e2e-spec.md`、`docs/06-tasks/provider-backed-e2e-tasks.md`。 |

## 范围内

- 定义第三层 provider-backed E2E gate，并与现有 first-layer mock E2E、second-layer local full-stack E2E 分离。
- 要求任何真实 provider 调用都必须显式 opt-in。
- 使用现有本地 frontend、Spring Boot API、PostgreSQL、vector/Ask flow 和 mock/sample knowledge data。
- DeepSeek 凭据只能来自开发者 shell 或未跟踪本地环境文件中的环境变量。
- 所有 provider 调用必须经过 ModelAdapter 边界。
- 通过浏览器可见的 Ask journey 验证 provider 生成结果和 source citations。
- 缺 key、网络不可用、provider 429、provider 5xx 时必须安全失败。
- 为日志、截图、trace、报告和 git diff hygiene 增加验证任务。

## 范围外

- 本轮实现代码或脚本。
- 把 provider-backed 检查加入 `npm run e2e:first-layer`、`npm run e2e:second-layer`、`npm run e2e` 或默认 CI。
- 替换 first-layer 或 second-layer 测试中的 mock Ask 行为。
- 使用真实公司文档、截图、导出文件或私有 endpoint。
- 持久化 raw prompts、raw provider responses、raw auth headers 或 raw secrets。
- 增加生产 secret manager、quota、billing controls 或 provider 管理 UI。

## 需求

| ID | 需求 | 优先级 | 来源 / 理由 |
|---|---|---|---|
| REQ-PBE2E-001 | Atlas 必须把 provider-backed E2E 定义为第三层验收 gate；它必须 opt-in，且永远不能进入 first-layer、second-layer、默认 frontend E2E 或默认 backend verification。 | Must | 验收分层、数据安全 |
| REQ-PBE2E-002 | 后续实现必须提供一条命令的本地路径，启动所需本地服务并运行带 provider-backed generation 的浏览器 Ask journey。 | Must | 用户目标 |
| REQ-PBE2E-003 | 第三层 journey 必须继续只使用 mock/sample knowledge data，不读取真实公司文档或私有本地文件。 | Must | 项目数据规则 |
| REQ-PBE2E-004 | DeepSeek API 凭据只能来自环境变量，且不得被 commit、echo、记录到日志、截图、trace，或通过 API/UI response 返回。 | Must | 安全与数据标准 |
| REQ-PBE2E-005 | 所有 DeepSeek/provider 执行必须发生在 product-facing ModelAdapter 边界之后；Ask、controller、frontend、vector、graph、repository 和通用 service layer 不得直接调用 provider API。 | Must | Adapter 边界规则 |
| REQ-PBE2E-006 | Provider-backed Ask 必须保留 Ask/RAG 信任模型：限定 Knowledge Space、approved/sample evidence、citations、confidence 或 safe status，并且 generated output 保持 review-required，除非未来策略明确改变。 | Must | Ask RAG spec |
| REQ-PBE2E-007 | 缺 key、配置无效、网络失败、provider 429、provider 5xx、timeout、provider 输出格式异常时，必须以安全消息失败，且不能泄漏不安全 artifact。 | Must | 可靠性与 secret 安全 |
| REQ-PBE2E-008 | 第三层命令必须包含 preflight checks；当缺少必需 opt-in flag 或必需环境变量时，必须在 provider 执行前停止。 | Must | 防止意外 provider 调用 |
| REQ-PBE2E-009 | 测试报告、Playwright 截图、视频、trace、backend logs、frontend console output 和 generated samples 必须被扫描或约束，确保 secrets、raw provider responses、auth headers、private paths、真实公司数据不会进入 git。 | Must | Artifact hygiene |
| REQ-PBE2E-010 | 实现第三层后，first-layer 和 second-layer E2E 行为必须保持不变且继续通过。 | Must | 回归保护 |
| REQ-PBE2E-011 | Provider-backed E2E 必须被记录为 local/developer acceptance，而不是默认 CI 要求，因为它依赖网络和开发者自有 provider credentials。 | Must | 可重复性与成本控制 |
| REQ-PBE2E-012 | 验证任务必须包含明确的 diff hygiene、secret/private-path scan，以及 provider-specific code 只隔离在 adapter implementation 和 provider-backed E2E setup 中的检查。 | Must | 质量门禁 |

## 验收

- 本 SDD pass 只创建双语 requirements、spec、tasks 和 traceability 文档。
- 英文与中文副本中的 requirement IDs 和 task IDs 保持一致。
- Spec 清晰分离 first-layer、second-layer 和 third-layer acceptance。
- Tasks 被限定为未来实现工作，并保留 ModelAdapter、secret、mock/sample data 和 safe-failure 约束。
- 本轮不修改实现代码、脚本或 package commands。

## 未决问题

| ID | 问题 | 本 SDD 默认处理 |
|---|---|---|
| OQ-PBE2E-001 | 实现时 DeepSeek key 应使用哪个精确环境变量名？ | 使用清晰的项目变量名，例如 `ATLAS_DEEPSEEK_API_KEY`；实现时再确认。 |
| OQ-PBE2E-002 | Provider-backed E2E 默认应关闭 Playwright trace/video，还是允许失败时保留已清洗 trace？ | 默认关闭或强清洗；实现不得保留 secrets。 |
| OQ-PBE2E-003 | 未来是否应在受保护分支 CI 中用托管 secrets 运行第三层 E2E？ | 延后。本 SDD 将其视为本地 opt-in。 |
