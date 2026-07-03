# 需求：模型适配器

## 状态

草稿。Phase 3 适配器切片。本轮仅产出 SDD，不实现产品代码。

## 切片契约

- **目标：** Atlas 可以通过产品面的模型适配器契约调用 LLM、embedding、rerank、vision、speech 模型能力，并保持配置脱敏、仅 mock 验证、输出默认需审核。
- **切片：** `model-adapter`
- **阶段：** 3 adapter
- **来源：** `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md`、`docs/00-context/slice-roadmap.md`、`docs/01-requirements/requirement.md`、`docs/architecture.md`、`docs/technology-decisions.md`、`docs/03-spec/metadata-api-spec.md`、`docs/04-architecture/metadata-api-data-model.md`、已有 converter/parser adapter SDD、storage adapter SDD，以及 `frontend/public/atlas-prototype.html` / `prototypes/index.html` 中的模型设置基线。
- **验证行：** Phase 3 adapter 需要针对 mock 引擎的单元测试与集成测试。
- **硬约束：** parser/converter/model/vector/storage 只能通过产品面的 adapter；禁止直连工具或供应商；不得硬编码单一实现；secret 与私有 endpoint 只能脱敏/状态化；必须保留 source trace、confidence、review status。

## 范围内

- 面向产品的模型适配器契约与 registry，覆盖 chat、embedding、rerank、vision、speech 能力类别。
- 内置/mock 模型 provider 的能力元数据，包括 adapter key、model key、model type、default marker、status、supported operations、context limits、masked configuration summary。
- 用于 mock 执行模型操作的内部 model-run API 契约，并带 source references 与 review-required 输出元数据。
- 模型运行证据记录：request purpose、input reference、source chunk references、output summary、confidence/evidence、review status、safe message、usage summary。
- Provider 配置和错误的 secret 与 endpoint 脱敏规则。
- Mock model adapter 实现与契约测试；不调用真实模型 provider。
- Adapter seam guard：禁止 controller/service/repository/domain 层出现模型 SDK/client/provider 直连名称。

## 排除项

- 真实 LLM、embedding、rerank、vision、speech、GitHub Models、Copilot、OpenAI-compatible、Ollama 或自定义 provider 执行。
- 生产级 secret manager、凭据轮换、BYOK、成本控制、额度、流式响应、限流、provider 账号管理。
- Ask/RAG 回答生成、向量索引、图谱抽取、Markdown 标准化、Wiki 发布、OCR/转写流水线、前端 UI 变更、生产 auth/RBAC，以及超出运行证据的生产审计策略。
- 原始 prompt/文档存储、原始 provider 日志、明文凭据、私有 endpoint、真实公司内容、外部云调用或外部网络依赖。

## 需求

| ID | 需求 | 优先级 | 来源 / 理由 |
|---|---|---|---|
| REQ-MODA-001 | 产品工作流代码必须通过模型 adapter registry 解析模型行为，不得在 adapter 范围外直接调用模型 SDK、provider HTTP API、本地模型 runtime 或 outbound client。 | Must | Adapter Standards, REQ-PROD-041, REQ-PROD-058 |
| REQ-MODA-002 | 模型 provider 必须表示为可替换 adapter；不得把 Ollama、DeepSeek、GitHub Models、Copilot 或 OpenAI-compatible API 等单一 provider 硬编码为唯一实现。 | Must | REQ-PROD-057, REQ-PROD-058, REQ-PROD-061 |
| REQ-MODA-003 | 能力元数据必须暴露 adapter key、model key、display name、model type、default marker、status、supported operations、context limits 与 masked configuration summary。 | Must | REQ-PROD-055, REQ-PROD-056, REQ-PROD-057, REQ-PROD-061 |
| REQ-MODA-004 | 能力元数据和 API 响应绝不能暴露原始 secret、provider endpoint、组织标识、hostname、本地 runtime path、credential 或私有模型配置。 | Must | REQ-PROD-049, REQ-PROD-058, Security/Data Standards |
| REQ-MODA-005 | Adapter 契约必须以产品概念支持 chat、embedding、rerank、vision、speech 能力类别，并允许不支持的 operation 安全失败。 | Must | REQ-PROD-057 |
| REQ-MODA-006 | Model run 必须接受 purpose、operation type、model selector、input reference 或安全 mock input、可选 source chunk references、requested-by 与 mode；自动化测试使用 `mock` mode。 | Must | Phase 3 verification row |
| REQ-MODA-007 | Model run 输出必须包含 operation-specific 的安全输出描述、可用时的 confidence/evidence、review status、usage summary、adapter/model identity 与 sanitized message。 | Must | Trace/review preservation |
| REQ-MODA-008 | 任何 LLM 或模型生成内容默认必须为 `REVIEW_REQUIRED`，除非后续切片中被已接受的确定性校验或 SME review 明确变更。 | Must | REQ-PROD-026, REQ-PROD-041, Trace And Review |
| REQ-MODA-009 | 当输入来自 file item、source chunk、Wiki page、graph node 或 Ask context 时，model run evidence 必须保留 source trace references。 | Must | REQ-PROD-040, REQ-PROD-042 |
| REQ-MODA-010 | Embedding 输出只能以 metadata 暴露 vector dimension 和 item counts；本切片不得写入向量数据库或创建索引。 | Must | Vector adapter boundary |
| REQ-MODA-011 | Chat/vision/speech/rerank mock 输出必须是安全 summary 或 reference，不得存储原始机密文档文本或原始 prompt payload。 | Must | Security/Data Standards |
| REQ-MODA-012 | Provider 失败和校验错误必须返回用户安全消息，不得包含原始 provider payload、stack trace、secret、私有 endpoint、本地路径或机密输入文本。 | Must | REQ-PROD-077 |
| REQ-MODA-013 | 自动化验证必须只使用 mock/fake model engine，不得需要外部网络访问、模型凭据、本地模型 daemon 或真实 provider 账号。 | Must | Phase 3 verification row |
| REQ-MODA-014 | Model run 记录必须支持 success、partial failure、validation failure、unavailable adapter、安全 adapter fault 等终态与 summary。 | Must | Adapter Standards |
| REQ-MODA-015 | 本 model adapter 切片不得实现 Ask/RAG、向量索引、图谱抽取、Wiki 发布、生产 auth/RBAC 或前端设置变更。 | Must | Phase discipline |

## 验收

- requirements、stories、spec、architecture、data flow、data model、design、API guide、tasks、traceability 均存在完整双语 SDD 产物。
- 英文与简体中文副本中的 REQ/US/T ID 一致。
- 任务映射到 requirement ID 与 spec 章节，并包含确切验证命令。
- API guide 已包含，因为本 Phase 3 adapter 切片定义内部 API/adapter contract。
- 本轮 SDD 不改产品代码。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-MODA-001 | mock 验证后首个真实 provider 应启用本地 runtime、GitHub/Copilot catalog、DeepSeek-compatible API，还是其他内部网关？ | 影响部署细节，不阻塞 mock adapter contract。 |
| OQ-MODA-002 | 是否允许持久化原始 prompt，还是 Atlas 只持久化 prompt reference、source reference 与 safe summary？ | 本 SDD 承诺只保留 reference 与 safe summary；若需要原始 prompt 保留，实施前需确认。 |
| OQ-MODA-003 | Model run usage/cost policy 是否属于本切片，还是推迟到 Phase 4 governance？ | 本 SDD 只记录 usage counts，成本/额度策略延后。 |
