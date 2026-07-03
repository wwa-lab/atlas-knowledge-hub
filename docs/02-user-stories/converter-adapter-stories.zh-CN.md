# 用户故事：Converter Adapter

## 状态

草稿。来源于 `docs/01-requirements/converter-adapter-requirements.md`。

## 故事索引

| Story ID | 标题 | 需求 ID |
|---|---|---|
| US-CA-001 | 在适配器边界之后转换 Office 文件 | REQ-CA-001, REQ-CA-002, REQ-CA-005 |
| US-CA-002 | 安全查看 converter 能力 | REQ-CA-003, REQ-CA-009 |
| US-CA-003 | 在转换结果中保留 metadata 与 trace | REQ-CA-004, REQ-CA-007, REQ-CA-008 |
| US-CA-004 | 报告不支持和失败的转换结果 | REQ-CA-006, REQ-CA-009 |
| US-CA-005 | 用 mock engine 验证 adapter 隔离 | REQ-CA-010, REQ-CA-011, REQ-CA-012 |

## US-CA-001：在适配器边界之后转换 Office 文件

**故事**
作为**知识库管理员**，
我希望 Atlas 通过 converter adapter 把受支持 Office 文件转换为 PDF，
以便 batch 能继续进入 parser 阶段，而产品工作流不直接绑定单一工具。

### 验收标准

1. **Given** 一个 batch 包含 `pptx`、`docx` 或 `xlsx` file metadata
   **When** 请求 conversion run
   **Then** Atlas 通过配置的 converter adapter interface 路由工作，而不是在 controller/service/repository 代码中直接调用 `trinity-office`。

2. **Given** 配置的 adapter 对受支持 Office 文件转换成功
   **When** run 完成
   **Then** file result 为 `PDF_CONVERTED`，并带有相对 `pdfPath`。

3. **Given** CI 中未安装真实 `trinity-office` binary
   **When** 测试运行
   **Then** mock engine/fake command runner 覆盖转换行为，且不要求真实 binary。

### 说明 / 假设

- `trinity-office` 是首个具体目标，但 adapter contract 必须允许未来替换。
- PDF pass-through 行为在 storage-adapter 策略接受前记录为开放问题。

### 依赖

- Phase 2 metadata API 和 file item status model。
- 本切片的 adapter/API guide。

### 范围外

- PDF-to-Markdown 解析。
- OCR 执行。
- 前端上传 UI。

### 待确认问题

- OQ-CA-001、OQ-CA-002、OQ-CA-003。

## US-CA-002：安全查看 converter 能力

**故事**
作为**平台管理员**，
我希望看到配置了哪个 converter adapter 以及它支持什么，
以便在不暴露本地路径、命令细节或 secret 的情况下验证可用性。

### 验收标准

1. **Given** 一个 converter adapter 已注册
   **When** 请求 capability metadata
   **Then** 响应包含 adapter key、display name、version、supported source types、output type、default marker 和 availability status。

2. **Given** adapter configuration 包含命令路径或凭据
   **When** metadata 被返回或记录日志
   **Then** 只展示脱敏/状态化配置摘要。

3. **Given** 没有可用 converter adapter
   **When** 请求 capability metadata
   **Then** Atlas 返回用户安全的 unavailable 状态，而不是泄漏运行时细节。

### 说明 / 假设

- Capability metadata 面向内部/admin，本切片不做前端工作。

### 依赖

- Configuration properties 和 adapter registry。

### 范围外

- Secret manager 集成。
- 生产 RBAC enforcement。

### 待确认问题

- 无。

## US-CA-003：在转换结果中保留 metadata 与 trace

**故事**
作为**交付负责人**，
我希望转换后的文件保留 source path、adapter、status、confidence 和 review metadata，
以便下游 parser、review 和 Wiki 工作保持可追溯。

### 验收标准

1. **Given** conversion run 产生 PDF result
   **When** Atlas 记录结果
   **Then** 它保存 adapter name、source path、source type、file status、`pdfPath`、可用时的 confidence 和时间戳。

2. **Given** converter result 包含生成或派生内容
   **When** metadata 被写入
   **Then** review status 保持 `REVIEW_REQUIRED`，除非后续明确 review action 改变它。

3. **Given** 任一路径值为绝对路径、包含目录穿越或暴露私有机器路径
   **When** Atlas 校验结果
   **Then** 结果按 API guide 被拒绝或清洗，且不持久化不安全路径。

### 说明 / 假设

- Conversion result persistence 会更新现有 file item metadata，也可以创建 conversion-run audit rows。

### 依赖

- Metadata API file item model。

### 范围外

- Source chunk 生成；这属于 parser-adapter。

### 待确认问题

- 无。

## US-CA-004：报告不支持和失败的转换结果

**故事**
作为**SME reviewer**，
我希望不支持和失败的文件以明确安全状态被报告，
以便知道哪些文档需要其他来源、OCR 或人工处理。

### 验收标准

1. **Given** 包含不支持 source type
   **When** conversion 运行
   **Then** Atlas 记录 `UNSUPPORTED`，并带有用户安全原因。

2. **Given** 受支持 Office 文件转换失败
   **When** adapter 返回错误
   **Then** Atlas 按 API guide 记录 `PDF_CONVERT_FAILED` 或 `FAILED`，并包含安全摘要。

3. **Given** 文件可能需要 OCR 或图像特定处理
   **When** converter logic 将其分类为 Office-to-PDF 范围外
   **Then** Atlas 在适用处记录 `OCR_REQUIRED`，但不执行 OCR。

### 说明 / 假设

- Error message 是摘要，不是原始 engine output。

### 依赖

- 现有 `FileStatus` enum。

### 范围外

- 重试调度 UI。
- OCR processing。

### 待确认问题

- 无。

## US-CA-005：用 mock engine 验证 adapter 隔离

**故事**
作为**实现 Atlas 的工程师**，
我希望测试和静态守卫能证明 conversion 始终位于 adapter 后方，
以便未来新增 adapter 时不会把产品耦合到单一 engine。

### 验收标准

1. **Given** converter adapter 代码被添加
   **When** `cd backend && mvn verify` 运行
   **Then** 单元测试、mock-engine 集成测试和静态 seam guard 通过。

2. **Given** controller/service/repository 引用具体 engine 名称或命令执行 API
   **When** seam guard tests 运行
   **Then** build 失败。

3. **Given** docs 被交接给 Codex 实现
   **When** 读取 tasks
   **Then** 每条 task 都映射到 requirement ID、spec 章节、确切验证命令，以及 adapter/no-network/secret-masked 约束。

### 说明 / 假设

- 现有 `AdapterSeamGuardTest` 必须更新，而不是删除。

### 依赖

- SDD task list 和 API/adapter guide。

### 范围外

- 本轮 SDD 生成不实现代码。

### 待确认问题

- 无。
