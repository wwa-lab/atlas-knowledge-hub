# 需求：Parser Adapter

## 状态

草稿。Phase 3 adapter 切片。本轮仅产出 SDD，不实现产品代码。

## 切片契约

- **目标：** Atlas 可以通过产品侧 parser adapter，将已转换的 PDF 解析为可追溯 Markdown、抽取图片资产和 source chunk，且产品 workflow 不耦合到 `document-normalize`。
- **切片：** `parser-adapter`
- **阶段：** 3 adapter
- **来源：** `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md`、`docs/00-context/slice-roadmap.md`、`docs/01-requirements/requirement.md`、`docs/markdown-standard.md`、`docs/batch-processing-design.md`、`docs/architecture.md`、`docs/03-spec/converter-adapter-spec.md`、`docs/04-architecture/converter-adapter-data-model.md`，以及 grounding 时核对过的现有 backend metadata 实体。
- **验证行：** Phase 3 adapter 需要针对 mock engines 的单元测试 + 集成测试。
- **硬约束：** Parser/converter/model/vector/storage 只能走产品侧 adapter；禁止直连工具；不得硬编码单一实现；secret 和私有路径必须脱敏；必须保留 source trace、confidence、review status。

## 范围内

- PDF 到 Markdown/图片抽取的产品侧 parser adapter 契约。
- 默认 `document-normalize` parser adapter 的 capability metadata，并只暴露脱敏配置。
- 针对 `PDF_CONVERTED` 文件的 parser run 与逐文件结果行为。
- 写回 `markdownPath`、`assetsPath`、confidence、安全错误，以及 `MARKDOWN_GENERATED` / `LOW_CONFIDENCE` / `OCR_REQUIRED` / `FAILED` 状态映射。
- 创建 source chunk，包含 page、section、confidence、review status。
- 与 `docs/markdown-standard.md` 对齐的 Markdown front matter 与 source trace block 要求。
- mock-engine 单元/集成测试与 seam guard 期望。
- 面向实现的内部 API 与 adapter contract guide。

## 排除项

- 超出 adapter seam contract 的真实 `document-normalize` 二进制/进程执行。
- OCR 执行、LLM enrichment、模型改写 Markdown、图谱抽取、Ask/RAG、发布到 Wiki、storage adapter 对象操作、前端界面、生产认证/RBAC、生产 secret manager 集成。
- 真实公司文档、私有路径、原始日志、凭证、外部云调用或外部网络依赖。

## 需求

| ID | 需求 | 优先级 | 来源 / 理由 |
|---|---|---|---|
| REQ-PA-001 | 产品 workflow code 必须通过 parser adapter registry 解析 parser；adapter 范围外不得直连 `document-normalize`、OCR 引擎、命令执行器或 outbound HTTP client。 | Must | REQ-PROD-015、REQ-PROD-017 |
| REQ-PA-002 | `document-normalize` 必须作为一个可替换 parser adapter 表示，而不是唯一 parser 实现。 | Must | REQ-PROD-016、REQ-PROD-017 |
| REQ-PA-003 | Parser capability metadata 必须暴露 adapter key、display name、version/status、支持输入类型 `pdf`、输出类型 `markdown` 与 `assets`、default marker、脱敏配置摘要。 | Must | Adapter Standards |
| REQ-PA-004 | Parser run 必须接受现有 batch、可选 file selection、可选 adapter key，且仅处理符合条件的 `PDF_CONVERTED` file item。 | Must | Batch workflow |
| REQ-PA-005 | 成功解析必须产出相对 Markdown path、可选相对 assets path、parser adapter identity、confidence，并进入 `MARKDOWN_GENERATED` 状态。 | Must | REQ-PROD-018、REQ-PROD-021 |
| REQ-PA-006 | 低置信度 parser output 必须表示为 `LOW_CONFIDENCE`，并保持 `reviewStatus=REVIEW_REQUIRED`。 | Must | REQ-PROD-019、REQ-PROD-032 |
| REQ-PA-007 | 需要 OCR 的 parser output 必须表示为 `OCR_REQUIRED`；本切片不得执行 OCR。 | Must | REQ-PROD-019 |
| REQ-PA-008 | Parser failure 必须表示为 `FAILED`，并携带脱敏的 user-safe error，不得包含原始 stderr/stdout、stack trace、secret、私有 endpoint 或绝对/私有路径。 | Must | Security/Data Standards |
| REQ-PA-009 | 生成的 Markdown 必须包含 `docs/markdown-standard.md` 要求的 front matter 字段与 page/chunk 级 source trace block。 | Must | REQ-PROD-021、REQ-PROD-023 |
| REQ-PA-010 | Parser result 必须创建或更新 source chunk metadata，包含 source file、page、section、confidence、review status。 | Must | REQ-PROD-018、REQ-PROD-023 |
| REQ-PA-011 | Parser result write 必须拒绝 unsafe path、越界 confidence、未知 file ID，以及不属于请求 batch 的文件结果。 | Must | Metadata/API safety |
| REQ-PA-012 | 自动化验证必须使用 mock/fake parser engine，不得要求真实文档、真实 `document-normalize`、外部网络调用或外部云服务。 | Must | Phase 3 verification row |
| REQ-PA-013 | Parser report 必须汇总 targeted total、Markdown generated、low-confidence、OCR-required、failed、skipped/ineligible、unsupported outcomes。 | Should | REQ-PROD-013 |
| REQ-PA-014 | Parser slice 必须保留现有 converter metadata、source path、review status、confidence；除非 parser result 安全地更新 file-level confidence。 | Must | Trace/review preservation |

## 验收

- Requirements、stories、spec、architecture、data flow、data model、design、API guide、tasks、traceability 均有完整双语 SDD 产物。
- 英文与简体中文副本的 REQ/US/T ID 完全一致。
- Tasks 映射到 requirement IDs 和 spec sections，并包含确切验证命令。
- 本 Phase 3 adapter 切片定义内部 API/adapter contracts，因此包含 API guide。
- 本轮 SDD pass 不改产品代码。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-PA-001 | 进入 mock engine 之外的真实实现时，`document-normalize` 应作为进程包装执行，还是作为外部 worker 执行？ | 部署细节；不阻塞 mock adapter contract。 |
| OQ-PA-002 | Parser output 是否应立即创建 `wiki_page` 行，还是 Wiki page publication 完全留给后续 publish 切片？ | 本 SDD 默认 parser-adapter 不创建 `wiki_page`。 |
| OQ-PA-003 | 将 output 分类为 `LOW_CONFIDENCE` 的 confidence 阈值应是多少？ | 本 SDD 为实现一致性提交 `< 0.80`；若产品希望调整，应在编码前修改。 |
