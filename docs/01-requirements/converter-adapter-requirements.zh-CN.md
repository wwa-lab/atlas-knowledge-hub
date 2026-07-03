# 需求：Converter Adapter

## 状态

草稿。Phase 3 适配器切片。ID 前缀：`CA`，代表 `converter-adapter`。

## 切片契约

- **Goal：** Atlas 能通过产品面 converter adapter 把受支持的 Office 源文件转换为可追溯 PDF 产物，同时保持 `trinity-office` 可替换且不被产品工作流直连。
- **Phase：** 3 adapter。
- **范围：** converter adapter 契约、能力元数据、配置校验、转换运行生命周期、mock engine 测试行为、`trinity-office` 包装边界、PDF 路径/状态/错误写回 metadata，以及用户安全报告。
- **排除：** parser/PDF-to-Markdown、OCR 执行、storage adapter 实现、vector/model adapter、Wiki 发布、Graph/Ask、前端 UI 改动、生产认证/RBAC、真实公司文档、外部云调用。
- **来源：** `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md`、`docs/01-requirements/requirement.md`、`docs/00-context/slice-roadmap.md`、`docs/architecture.md`、`docs/batch-processing-design.md`、`docs/markdown-standard.md`、`docs/03-spec/metadata-api-spec.md`、`docs/04-architecture/metadata-api-data-model.md`、`docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`。
- **验证行：** 针对 **mock engines** 的单元 + 集成测试。硬约束：parser/converter/model/vector/storage **只**走产品面适配器；禁止直连工具；禁止硬编码单一实现。API guide：每个适配器切片需适配器契约。

## 产品需求

| ID | 需求 | 优先级 | 产品来源 |
|---|---|---|---|
| REQ-CA-001 | 产品工作流必须只通过产品面 converter adapter 接口执行 Office-to-PDF 转换，controller、metadata service、repository、UI 代码或脚本不得直接调用 `trinity-office`。 | Must | REQ-PROD-015、REQ-PROD-016 |
| REQ-CA-002 | 首个 converter adapter 契约必须支持把 `trinity-office` 作为能力元数据和配置背后的可替换实现，而非硬编码单例。 | Must | REQ-PROD-016、REQ-PROD-017 |
| REQ-CA-003 | Converter 能力元数据必须声明支持的 source type、输出类型、adapter name/version、默认标记、可用状态和脱敏配置摘要。 | Must | REQ-PROD-017、REQ-PROD-061 |
| REQ-CA-004 | Conversion run 必须接收 batch/file metadata，并输出逐文件结果：source path、source type、file status、可用时的 generated PDF path、可用时的 confidence、用户安全 error、adapter name 和时间戳。 | Must | REQ-PROD-018、REQ-PROD-019 |
| REQ-CA-005 | 受支持的 Office 输入（`pptx`、`docx`、`xlsx`）转换成功后必须进入 `PDF_CONVERTED`，并包含相对 `pdfPath`。 | Must | REQ-PROD-012、REQ-PROD-018 |
| REQ-CA-006 | 不支持、失败、需要 OCR 或低置信度的转换结果必须用现有 file status 和报告条目表达，不得引入未文档化状态。 | Must | REQ-PROD-012、REQ-PROD-019 |
| REQ-CA-007 | 转换结果必须保留 source trace 和 review status；转换不得自动批准生成或派生内容。 | Must | REQ-PROD-018、REQ-PROD-022、REQ-PROD-026 |
| REQ-CA-008 | Converter 输入/输出中的 artifact path 与 source path 必须是相对路径、无目录穿越、无私有绝对路径。 | Must | REQ-PROD-074、REQ-PROD-076、Security and Data Rules |
| REQ-CA-009 | Converter 配置和错误不得暴露原始 secret、私有 endpoint、本机路径、命令行凭据、异常栈或机密文档内容。 | Must | REQ-PROD-077、Adapter Standards |
| REQ-CA-010 | Converter 切片必须包含使用 mock engine/fake command runner 的单元与集成测试；CI 不得要求本机安装 `trinity-office` 或真实文档语料。 | Must | Phase 3 验证行 |
| REQ-CA-011 | Adapter seam guard 必须从“adapter 包为空”演进为“只有 adapter 实现可引用引擎特定名称或命令执行边界”。 | Must | Adapter gate |
| REQ-CA-012 | 本切片必须在产品代码实现前提供 adapter/API implementation guide。 | Must | `docs/00-context/slice-roadmap.md` Phase 3 API guide 规则 |

## 验收标准

| ID | 需求 | 可观察完成标准 |
|---|---|---|
| AC-CA-01 | REQ-CA-001、REQ-CA-011 | 若非 adapter 产品层引用 `trinity-office`、命令执行 API 或转换外联网络客户端，静态守卫测试失败。 |
| AC-CA-02 | REQ-CA-002、REQ-CA-003 | 能力列表暴露 converter adapter metadata，配置已脱敏，且不展示原始命令路径或 secret。 |
| AC-CA-03 | REQ-CA-004、REQ-CA-005 | 对受支持 Office file metadata 执行 mock conversion run 后返回 `PDF_CONVERTED` 和相对 PDF artifact path。 |
| AC-CA-04 | REQ-CA-006 | 不支持/失败文件产生 `UNSUPPORTED`、`PDF_CONVERT_FAILED` 或 `OCR_REQUIRED` 结果，并有用户安全报告条目。 |
| AC-CA-05 | REQ-CA-007、REQ-CA-008 | source path、PDF path、confidence、adapter name 和 review status 被保留，且无绝对/穿越路径。 |
| AC-CA-06 | REQ-CA-009 | 错误载荷与日志不含原始 secret、私有路径、异常栈或机密内容。 |
| AC-CA-07 | REQ-CA-010 | `cd backend && mvn verify` 使用 mock-engine 单元/集成覆盖通过，且不依赖真实 `trinity-office`。 |
| AC-CA-08 | REQ-CA-012 | API/adapter guide 的英文与中文版本存在，并在实现交接前被 traceability 链接。 |

## 假设

- Phase 2 metadata API 已足够满足 Phase 3 准入；当前后端已有 file item metadata、`pdfPath`、status、confidence、review status 和 source chunk 概念。
- 首个实现可在测试中使用 fake command runner 或 mock engine；真实 `trinity-office` 执行是可选且受配置门控的。
- Converter 执行初期可以在 Spring Boot 进程内运行，但产品层只能依赖 converter adapter interface 和 contract。

## 待确认问题

| ID | 问题 | 负责人 |
|---|---|---|
| OQ-CA-001 | 真实 `trinity-office` 执行应由 adapter 实现本地启动进程，还是立即移入外部 worker 进程？ | Architecture |
| OQ-CA-002 | 对已有 PDF 的 pass-through 应复制到 generated PDF 存储，还是在 storage-adapter 存在前引用原始相对路径？ | Product/Architecture |
| OQ-CA-003 | 目标环境可用的 `trinity-office` 命令行契约具体是什么？ | Platform |
