# 用户故事：真实 Office 解析 Runtime

## 状态

供用户审阅的草稿。SDD 被接受前 implementation 仍然阻塞。

## Story Set

### US-REAL-OFFICE-PARSER-RUNTIME-001: 从已接受 SDD 启动 Runtime Integration

**Story:**  
作为 knowledge platform owner，  
我希望真实 runtime integration 从已接受的 SDD 开始，  
以便 Atlas 从 mock 走向真实 runtime 时不会绕过 adapter、安全和 trust 边界。

#### Acceptance Criteria

1. **Given** 用户请求本切片  
   **When** 生成 SDD  
   **Then** requirements、stories、spec、architecture、data flow、data model、design、API guide、tasks、traceability 的英文和简体中文文件都存在。

2. **Given** 有人提出产品代码改动  
   **When** SDD 尚未被接受  
   **Then** implementation 保持阻塞，并在 traceability 中说明。

#### Notes / Assumptions

- 用户已明确启动 Wave 2 / `real-office-parser-runtime`。

#### Dependencies

- 已有 converter/parser adapter SDD 和实现。

#### Out of Scope

- 用户接受前的产品 runtime code。

#### Open Questions

- None.

### US-REAL-OFFICE-PARSER-RUNTIME-002: 通过 Adapter 运行 Office Conversion

**Story:**  
作为 knowledge base administrator，  
我希望 Office-to-PDF conversion 通过 converter adapter 使用配置好的内部 runtime，  
以便引入真实 conversion 时不会把产品工作流耦合到 `trinity-office`。

#### Acceptance Criteria

1. **Given** `trinity-office` runtime 已配置且可用  
   **When** conversion run 请求 configured execution  
   **Then** Atlas 解析 converter adapter，在 adapter boundary 内执行，并记录安全 per-file results。

2. **Given** runtime configuration 缺失或禁用  
   **When** conversion run 请求 configured execution  
   **Then** run 以 unavailable 或 misconfigured 安全失败；除非存在安全 per-file results，否则 file metadata 不变。

3. **Given** runtime output 包含 PDF path 或 error text  
   **When** Atlas 持久化或返回 result  
   **Then** paths 是安全相对路径，errors 已脱敏。

#### Notes / Assumptions

- 精确 command contract 尚未批准，必须保留为 open question。

#### Dependencies

- 现有 `ConverterAdapter` 和 conversion run API。

#### Out of Scope

- 外部 cloud conversion。

#### Open Questions

- 已批准的 command 或 worker contract 是什么？

### US-REAL-OFFICE-PARSER-RUNTIME-003: 通过 Adapter 运行 PDF Parsing

**Story:**  
作为 delivery lead，  
我希望 PDF-to-Markdown parsing 通过 parser adapter 使用配置好的内部 runtime，  
以便生成真实 Markdown 和 source chunks，同时保留 review 和 trace metadata。

#### Acceptance Criteria

1. **Given** `document-normalize` runtime 已配置且可用  
   **When** parser run 指向 eligible `PDF_CONVERTED` files  
   **Then** Atlas 解析 parser adapter，在 adapter boundary 内执行，并记录 Markdown path、assets path、confidence、source chunks 和 safe messages。

2. **Given** parser output confidence 低于已接受 threshold  
   **When** result 被持久化  
   **Then** file 映射为 `LOW_CONFIDENCE`，并保持 `REVIEW_REQUIRED`。

3. **Given** parser output invalid 或 unsafe  
   **When** Atlas 验证 result  
   **Then** unsafe output 在持久化前被拒绝，API 返回安全 validation error。

#### Notes / Assumptions

- Markdown byte materialization 可以通过 local artifact storage 或后续 worker/storage boundary 完成，但产品 metadata 必须保持相对且安全。

#### Dependencies

- 现有 `ParserAdapter`、parser run API、source chunk metadata 和 relative path validation。

#### Out of Scope

- Wiki publishing、graph extraction、Ask indexing、OCR execution 和 LLM enrichment。

#### Open Questions

- `document-normalize` 应返回什么 output manifest format？

### US-REAL-OFFICE-PARSER-RUNTIME-004: 安全查看 Runtime Capability

**Story:**  
作为 platform administrator，  
我希望 capability endpoints 展示 runtime adapters 是 configured、available、disabled 还是 misconfigured，  
以便诊断配置，同时不暴露 command paths、endpoints 或 credentials。

#### Acceptance Criteria

1. **Given** runtime settings 已配置  
   **When** 请求 capability endpoints  
   **Then** responses 包含 adapter key、display name、version/status、default marker、supported inputs/outputs 和 masked configuration summary。

2. **Given** runtime settings 包含敏感值  
   **When** 返回 capability metadata  
   **Then** 不暴露 raw paths、endpoints、tokens、passwords、command arguments 或 hostnames。

#### Notes / Assumptions

- 应尽量复用现有 capability endpoint shapes。

#### Dependencies

- 现有 converter 和 parser capability endpoints。

#### Out of Scope

- Secret manager-backed storage 或 credential rotation。

#### Open Questions

- 哪些非 secret runtime fields 可以作为 status-only summary 安全展示？

### US-REAL-OFFICE-PARSER-RUNTIME-005: 保留 CI 与安全门禁

**Story:**  
作为 implementation agent，  
我希望 runtime integration 保持 mock-safe 和 static guarded，  
以便正常验证不需要本地内部 binaries，且产品层不会漂移成直接工具调用。

#### Acceptance Criteria

1. **Given** 未安装真实 runtime binary  
   **When** 运行正常 backend verification  
   **Then** tests 使用 mocks/fakes 通过，不要求内部工具。

2. **Given** 有人在允许的 adapter/runtime package 外新增 direct tool call  
   **When** 运行 seam guard tests  
   **Then** tests 失败。

3. **Given** changed files 被扫描  
   **When** 运行 secret/private-path 和 network/dependency scans  
   **Then** 未发现 raw credentials、private paths、real data 或 new external cloud calls。

#### Notes / Assumptions

- Optional opt-in runtime tests 可以在缺少配置时跳过。

#### Dependencies

- 现有 `AdapterSeamGuardTest` 和 backend verification pipeline。

#### Out of Scope

- 默认让 CI 安装或运行内部 binaries。

#### Open Questions

- 应使用哪个 opt-in environment flag 启用 runtime smoke tests？
