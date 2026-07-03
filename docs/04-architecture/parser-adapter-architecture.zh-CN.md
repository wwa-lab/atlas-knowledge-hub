# 架构：Parser Adapter

## 状态

草稿。Phase 3 adapter 切片。由 `docs/03-spec/parser-adapter-spec.md` 派生。

## 概览

Parser-adapter 在现有 metadata control plane 上增加一个可替换 parser integration seam。后端拥有 parser run 状态、校验、报告与 metadata 写回；parser 执行隔离在产品侧 adapter contract 后。`document-normalize` 是首个具名 adapter，但架构保留 MinerU、Docling、PaddleOCR、内部 OCR 与未来 parser engines 的可替换性。

## 架构驱动

| 驱动 | 影响 |
|---|---|
| Adapter neutral | Product services 通过 registry/capability contracts 解析 parser adapters。 |
| Trace preservation | Markdown、file metadata 与 source chunks 携带 source path、PDF path、confidence、review status。 |
| Mock-only verification | 测试使用 mock/fake parser engines，不要求真实文档或 parser binaries。 |
| Secret/path safety | 路径为相对路径；errors 与 capability summaries 脱敏。 |
| Phase discipline | Parser-adapter 产出 Markdown/assets/chunks，但不发布 Wiki pages，也不执行 OCR/LLM/graph/Ask。 |

## 现有 Metadata 上下文

当前 metadata control plane 已拥有 file items、source chunks、review status、confidence、artifact path 字段、path safety rules 与 adapter seam guarding。Parser-adapter 应复用这些产品概念，而不是创建独立的 parser-owned metadata island。代码级 grounding anchors 记录在 design 与 traceability 产物中，因为那里才属于 implementation-facing detail。

## 系统上下文

| 边界 | 职责 |
|---|---|
| Frontend | 本切片范围外。现有 UI 未来可消费 parser run APIs，但 parser-adapter 不改前端。 |
| Backend API / metadata control plane | 拥有 parser capability endpoints、parser run lifecycle、validation、metadata write-back、reports。 |
| Parser adapter seam | 封装 parser engine 特定执行，返回 Atlas product concepts。 |
| Parser engine / worker | 位于 product workflow 外部。`document-normalize` 只能在 adapter contract 后。 |
| PostgreSQL metadata | 存储 parser run evidence、逐文件结果、file path/status updates、source chunks。 |

## 高层架构

```text
+------------------------------------------------------------+
| Users / agents                                             |
| Admin, delivery lead, SME reviewer, Codex implementation    |
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| Parser capability endpoint, parser run endpoint, reports    |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Parser application service                                 |
| Target validation, adapter resolution, status mapping,       |
| safe error handling, metadata write-back                    |
+------------------------------+-----------------------------+
                               |
                   product-facing adapter interface
                               v
+------------------------------+        +---------------------+
| Parser adapter registry      |        | Parser adapters      |
| capability + default policy  |------->| document-normalize   |
+------------------------------+        | mock parser in tests |
                                        +----------+----------+
                                                   |
                                                   | engine/worker boundary
                                                   v
                                        +---------------------+
                                        | Parser engine        |
                                        | outside product flow |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                         |
| file_item, source_chunk, parser_run, parser_file_result      |
+------------------------------------------------------------+
```

## 组件拆分

### Backend API

- **Parser adapter capability API：** 列出 parser capabilities 与脱敏配置。
- **Parser run API：** 为 batch 创建 parser run 并返回 run report。
- **File/source chunk API 复用：** 现有 file 与 chunk APIs 仍是 file/chunk metadata 的读取面。

### Application Services

- **Parser run service：** 校验目标 batch/files、解析 adapter、执行 mock/configured mode、映射 parser results、持久化 run evidence、构建 reports。
- **Parser summary calculator：** 从 parser file results 派生 totals。
- **Parser adapter registry：** 拥有 default adapter resolution 与 unavailable/misconfigured behavior。
- **Safety helpers：** 复用 relative path validation 与 safe error masking patterns。

### Integration Adapters

- **ParserAdapter contract：** 接收 Atlas file metadata，返回 Atlas parser results。
- **MockDocumentNormalizeParserAdapter：** CI 与集成测试使用的确定性 fake implementation。
- **DocumentNormalizeParserAdapter：** 真实 adapter boundary placeholder 或 configured implementation。它可以知道 engine 细节，但 product layers 不得知道。

### Persistence

- 复用 `file_item` 存储 status、`markdown_path`、`assets_path`、confidence、sanitized errors。
- 复用 `source_chunk` 存储 parser trace chunks。
- 新增 `parser_run` 与 `parser_file_result` 逻辑实体，记录 execution evidence 与 reports。
- 本切片不创建 `wiki_page` 行。

## 状态策略

Parser run status：

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

File status mapping：

- `PDF_CONVERTED` + success confidence `>= 0.80` -> `MARKDOWN_GENERATED`
- `PDF_CONVERTED` + success confidence `< 0.80` -> `LOW_CONFIDENCE`
- parser 表示需要 OCR -> `OCR_REQUIRED`
- parser failure -> `FAILED`
- 不符合条件 -> unchanged，并在 report 中记为 skipped/ineligible

生成内容与低置信度内容保持 `REVIEW_REQUIRED`。

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/parser-adapters` | Admin / implementation tests | 列出脱敏 parser capabilities。 |
| `POST /api/batches/{batchId}/parser-runs` | Internal workflow / future UI | 为符合条件的 PDF file metadata 启动 parser run。 |
| `GET /api/parser-runs/{runId}` | Delivery lead / future UI | 读取 parser report 与逐文件 outcome。 |
| `ParserAdapter` | Parser service | 在产品侧 interface 后执行 parser work。 |

## Security / Reliability / Observability

- Capability responses 只暴露 masked/status-only 信息。
- Parser errors 在持久化或响应前做有界脱敏。
- Parser result paths 必须校验为安全相对路径。
- Source chunks 与 Markdown trace 保留证据，但不在日志中嵌入原始机密文档内容。
- 自动化验证必须使用 mock engines。
- Seam guard 扫描 non-adapter product layers，检查 direct parser engine、command runner 与 outbound client references。

## 风险 / 取舍

| ID | 风险 / 取舍 | 缓解 |
|---|---|---|
| R-PA-001 | 真实 parser runtime 拓扑未定。 | 真实执行保持在 adapter 后；mock contract 稳定。 |
| R-PA-002 | Parser output 可能包含 raw paths 或 logs。 | 持久化前校验 paths 并脱敏 safe messages。 |
| R-PA-003 | 过早创建 Wiki pages 会模糊 review/publish 边界。 | Parser-adapter 只写 Markdown/chunks；publish 拥有 `wiki_page`。 |
| R-PA-004 | Low-confidence threshold 可能需要调优。 | SDD 提交 `< 0.80`，并在 capability metadata 中暴露以便 review。 |

## 待确认问题

- OQ-PA-001：真实 `document-normalize` 执行拓扑。
- OQ-PA-002：未来 draft `wiki_page` rows 的归属。
- OQ-PA-003：产品确认 `< 0.80` low-confidence threshold。
