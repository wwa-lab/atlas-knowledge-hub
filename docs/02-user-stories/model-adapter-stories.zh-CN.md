# 用户故事：模型适配器

## 状态

草稿。由 `docs/01-requirements/model-adapter-requirements.md` 派生。ID 在英文与中文副本中保持稳定。

## US-MODA-001：通过适配器解析模型能力

**故事：**
作为平台管理员，
我希望 Atlas 通过可替换的 adapter registry 展示模型能力，
以便模型 provider 可以更换，而产品工作流不依赖单一 SDK 或 provider。

## 验收标准

1. **Given** 已配置模型 adapter
   **When** 请求能力元数据
   **Then** Atlas 返回 adapter/model key、model type、status、supported operations、default marker、context limits 与 masked configuration。

2. **Given** 某个 provider 有敏感配置
   **When** 返回能力元数据
   **Then** 不暴露原始 secret、provider endpoint、hostname、组织标识或本地路径。

3. **Given** 扫描 adapter package 之外的产品代码
   **When** seam guard 执行
   **Then** 直接模型 SDK、provider、本地 runtime 与 outbound client 引用会导致验证失败。

## 说明 / 假设

- 能力元数据可包含 `configured`、`not_configured`、`mock` 等安全状态词。
- 真实 provider 执行延后；mock adapter 用于证明契约。

## 依赖

- Phase 2 metadata API 与现有 `ApiEnvelope`。
- converter/parser 切片中的 adapter registry 模式。

## 排除项

- 真实 provider 执行、凭据轮换、生产 auth/RBAC、前端设置编辑。

## 待确认问题

- OQ-MODA-001：mock 验证后首个真实 provider 是哪一个？

## US-MODA-002：以需审核输出执行 mock 模型操作

**故事：**
作为交付负责人，
我希望 Atlas 能为 chat、embedding、rerank、vision 或 speech 操作创建 mock model run，
以便后续 Ask、vector、graph、review 工作流可集成稳定契约。

## 验收标准

1. **Given** 一个有效的 `mock` mode model run request
   **When** Atlas 执行该 run
   **Then** 它持久化包含 adapter/model identity、operation、status、source references、safe output summary、usage summary 与 timestamps 的运行记录。

2. **Given** 生成了模型输出
   **When** run 完成
   **Then** 输出默认是 `REVIEW_REQUIRED`，且不会被视为已批准知识。

3. **Given** 请求 embedding 操作
   **When** mock adapter 返回 embedding metadata
   **Then** Atlas 只记录 dimension 和 item counts，不写入向量数据库。

## 说明 / 假设

- Mock input 可以使用安全示例文本；真实文档内容只引用，不持久化。
- Usage summary 只记录 counts；成本/额度策略延后。

## 依赖

- 使用 source references 时依赖 file item/source chunk 元数据实体。

## 排除项

- Ask 回答生成、向量索引、图谱抽取、Wiki 发布、真实 speech/vision 处理。

## 待确认问题

- OQ-MODA-002：是否允许保留原始 prompt。

## US-MODA-003：保留溯源与安全证据

**故事：**
作为 SME reviewer，
我希望模型输出保留 source references、confidence/evidence 与 review status，
以便生成内容在影响可信 Wiki、Graph 或 Ask 体验前可被评估。

## 验收标准

1. **Given** 模型输入引用 source chunks
   **When** Atlas 返回 model run report
   **Then** report 包含 source chunk references，且不嵌入机密原文。

2. **Given** 产出了 mock chat、vision、speech 或 rerank 输出
   **When** 输出被持久化或返回
   **Then** Atlas 包含 safe summary、可用时的 confidence/evidence，以及 `REVIEW_REQUIRED`。

3. **Given** 后续工作流消费模型输出
   **When** 该输出尚未审核
   **Then** 契约让 review-required 状态可观察。

## 说明 / 假设

- 模型输出 reference 故意比最终 Wiki 发布记录更轻量。

## 依赖

- Metadata API 的 review status 词汇。

## 排除项

- SME review UI 变更和发布状态转换。

## 待确认问题

- 无。

## US-MODA-004：安全失败且不泄漏 provider 内部信息

**故事：**
作为知识库管理员，
我希望模型 adapter 失败返回安全且有界的错误，
以便排障时不暴露 secret、私有路径、prompt 或 provider 内部细节。

## 验收标准

1. **Given** 请求了未知 adapter 或 model key
   **When** Atlas 校验请求
   **Then** 返回带用户安全字段消息的 `VALIDATION_ERROR`。

2. **Given** 配置的 adapter 不可用或配置错误
   **When** 请求 run
   **Then** Atlas 记录 failed run 或拒绝请求，且不暴露原始 endpoint、credential、stack trace、本地路径或 provider payload。

3. **Given** mock adapter 抛出未预期异常
   **When** service 映射错误
   **Then** API 与持久化消息均已脱敏且有界。

## 说明 / 假设

- 如后续需要详细运维诊断，应进入受保护的服务端日志和 Phase 4 governance，而不是用户可见响应。

## 依赖

- 现有安全 error envelope 与 validation 模式。

## 排除项

- 生产观测、限流、成本告警或 provider 事故工作流。

## 待确认问题

- OQ-MODA-003：成本/额度策略是否属于后续 governance 切片。

## US-MODA-005：用 mock 引擎验证模型适配器契约

**故事：**
作为 Codex 实现 agent，
我希望获得带确切 mock-engine 验证命令的可执行 model-adapter 任务，
以便实现时不需要猜测范围或调用真实 provider。

## 验收标准

1. **Given** SDD 已接受
   **When** 开始实现
   **Then** 每个任务都映射到 REQ ID、spec 章节、constraints、dependencies 与确切命令。

2. **Given** 自动化验证运行
   **When** adapter contract 与 API tests 执行
   **Then** 它们只使用 mock/fake model engine，不需要网络、凭据、本地 daemon 或 provider 账号。

3. **Given** 最终 guard checks 运行
   **When** 扫描 non-adapter 产品层
   **Then** 不存在直接 model provider/client 引用和原始 secret pattern。

## 说明 / 假设

- 若实现会偏离已接受的 spec/tasks，必须停止并报告。

## 依赖

- 已接受的 `model-adapter` SDD 集。

## 排除项

- 本轮 SDD 生成不实现产品代码。

## 待确认问题

- 无。
