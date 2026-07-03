# 用户故事：Parser Adapter

## 状态

草稿。由 `docs/01-requirements/parser-adapter-requirements.md` 派生。

## 故事地图

| Story ID | 标题 | 需求 |
|---|---|---|
| US-PA-001 | 通过 adapter 边界解析已转换 PDF | REQ-PA-001, REQ-PA-002, REQ-PA-004, REQ-PA-012 |
| US-PA-002 | 安全查看 parser capability | REQ-PA-002, REQ-PA-003 |
| US-PA-003 | 生成可追溯 Markdown 与 assets | REQ-PA-005, REQ-PA-009, REQ-PA-010, REQ-PA-014 |
| US-PA-004 | 将低置信度、OCR、失败结果路由到可 review 状态 | REQ-PA-006, REQ-PA-007, REQ-PA-008, REQ-PA-013 |
| US-PA-005 | 保护 parser metadata write 与 adapter seam | REQ-PA-001, REQ-PA-008, REQ-PA-011, REQ-PA-012 |

## US-PA-001：通过 adapter 边界解析已转换 PDF

**故事**
作为知识库管理员，
我希望 parser run 通过 parser adapter 处理已转换 PDF，
以便 Atlas 能生成 Markdown 且产品 workflow 不硬耦合到单一 parser engine。

### 验收标准

1. **Given** 一个 batch 包含符合条件的 `PDF_CONVERTED` 文件
   **When** 请求 parser run
   **Then** Atlas 在解析前解析到已注册 parser adapter。
2. **Given** 扫描 non-adapter product layers
   **When** adapter package 外出现直接 parser engine 引用或命令/网络调用
   **Then** seam guard 验证失败。
3. **Given** CI 中运行测试
   **When** 验证 parser 行为
   **Then** 使用 mock/fake parser engine，且不要求真实 `document-normalize` runtime。

### 说明 / 假设

- 已有 converter adapter 切片作为 registry/capability/run 行为模式。
- Parser 实现开始于 Phase 2 metadata API 之后；当前 roadmap 已标记 Phase 2 已实现。

### 依赖

- Metadata API 的 file、batch、source chunk 记录。
- Converter adapter 输出将文件标记为 `PDF_CONVERTED`。

### 排除项

- 真实 parser 进程执行、OCR 执行、前端 UI、图谱、Ask、发布行为。

### 待确认问题

- OQ-PA-001：真实 parser worker/process 拓扑延后到 mock-engine 验证之后。

## US-PA-002：安全查看 parser capability

**故事**
作为平台管理员，
我希望查看已配置 parser adapter 的 capability 与状态，
以便了解哪个 parser 可用，同时不暴露 secret 或私有 runtime 细节。

### 验收标准

1. **Given** 已配置 parser adapter
   **When** 列出 capabilities
   **Then** response 包含 adapter key、display name、version/status、支持的 input/output types、default marker、masked configuration。
2. **Given** adapter disabled 或 misconfigured
   **When** 列出或选择 capabilities
   **Then** Atlas 只暴露安全状态，不泄露原始命令路径、环境变量、token、hostname 或绝对路径。

### 说明 / 假设

- `document-normalize` 是首个具名 adapter，但 MinerU、Docling、PaddleOCR、内部 OCR 等未来 adapter 必须保持可替换。

### 依赖

- Adapter registry 与 capability metadata contract。

### 排除项

- 生产 secret-manager 集成。

### 待确认问题

- 无。

## US-PA-003：生成可追溯 Markdown 与 assets

**故事**
作为 SME reviewer，
我希望 parser output 包含 Markdown、assets 与 source trace，
以便对照原 PDF 证据 review 生成知识。

### 验收标准

1. **Given** parser result 成功
   **When** 写回 metadata
   **Then** file item 记录相对 Markdown/assets paths、parser identity、confidence 与 `MARKDOWN_GENERATED`。
2. **Given** Markdown 已生成
   **When** 检查 artifact
   **Then** 它包含必需 front matter 与 source trace blocks，含 source file、PDF、page/section/chunk id、confidence、review status。
3. **Given** 返回 source chunks
   **When** 持久化 chunks
   **Then** 每个 chunk 存储 file item id、source file、page、section、confidence，并默认 `REVIEW_REQUIRED`，除非安全地提供显式 review status。

### 说明 / 假设

- Parser-adapter 写 source chunks，但不发布 Wiki pages。
- 生成内容保持 review-required，直到后续 review/publish flow 改变。

### 依赖

- Markdown standard。
- 现有 `file_item` 与 `source_chunk` metadata tables。

### 排除项

- Wiki 发布与图谱投影。

### 待确认问题

- OQ-PA-002：后续切片从 parser output 创建 `wiki_page`，还是从 approved publish output 创建。

## US-PA-004：将低置信度、OCR、失败结果路由到可 review 状态

**故事**
作为 delivery lead，
我希望 parser report 分类 low-confidence、OCR-required、failed files，
以便后续 review 工作可见，且生成内容不会过早被视为可信。

### 验收标准

1. **Given** parser confidence 低于 `0.80`
   **When** 映射 parser result
   **Then** file item status 为 `LOW_CONFIDENCE`，review status 保持 `REVIEW_REQUIRED`。
2. **Given** parser output 表示需要 OCR
   **When** 映射 result
   **Then** file item status 为 `OCR_REQUIRED`，不执行 OCR，report 包含原因。
3. **Given** parsing 失败
   **When** 持久化 result
   **Then** file item status 为 `FAILED`，且只有脱敏 safe error。
4. **Given** parser run 完成
   **When** 查看 report
   **Then** totals 包含 Markdown generated、low-confidence、OCR-required、failed、skipped/ineligible、unsupported outcomes。

### 说明 / 假设

- 本切片提交 low-confidence 阈值为 `< 0.80`。

### 依赖

- 现有 `FileStatus` 值与 review status 规则。

### 排除项

- 自动 approval 或 publication。

### 待确认问题

- OQ-PA-003：如产品希望不同阈值，应在实现前确认。

## US-PA-005：保护 parser metadata write 与 adapter seam

**故事**
作为实现负责人，
我希望 parser result writes 与 adapter seam 有保护，
以便畸形 parser output 不会污染 metadata 或泄露敏感信息。

### 验收标准

1. **Given** parser result 包含 absolute path、URI path、traversal path、unknown file id 或越界 confidence
   **When** Atlas 验证 result
   **Then** run 安全失败并返回 field-level validation，且不持久化 unsafe metadata。
2. **Given** parser errors 包含 secrets、stack traces 或 private paths
   **When** 返回或存储 error
   **Then** 只保留有界脱敏摘要。
3. **Given** 自动化验证运行
   **When** seam guard 与 secret scans 执行
   **Then** non-adapter product layers 无 direct parser engine calls，生成的 SDD/code 无 raw secrets 或 private paths。

### 说明 / 假设

- Relative path validation 应尽量复用现有 metadata API 安全规则。

### 依赖

- 现有 relative-path validator 与 API error envelope。

### 排除项

- 完整生产 credential 管理。

### 待确认问题

- 无。
