# 规格：Metadata API

## 状态

草稿。Phase 2（后端 metadata API 与持久化）。`metadata-api` 切片的行为真实来源。派生自 `docs/02-user-stories/metadata-api-stories.md`。

## 来源文档

- `docs/01-requirements/metadata-api-requirements.md`
- `docs/02-user-stories/metadata-api-stories.md`
- `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` —— 请求/响应契约（本规格的配套）。
- `docs/04-architecture/metadata-api-data-model.md` —— 持久化 schema 与枚举。
- `docs/batch-processing-design.md` —— 权威 `FileStatus` 集合。
- `docs/markdown-standard.md` —— trace / confidence / review metadata。
- 前端消费方基线：`frontend/src/types.ts`、`frontend/src/data/atlasMock.ts`、`frontend/public/atlas-prototype.html`。

## 范围

一个由 PostgreSQL/Flyway 支撑的内部 Spring Boot REST metadata 服务。它持久化并提供 Knowledge Space、批次、file item、source chunk 与 review metadata，并创建（但不提供）wiki-page/graph 表。仅 metadata：本切片**不含**转换、解析、模型、向量、存储、上传字节、认证或图谱/Ask 行为。

## 约束

- **仅 metadata / adapter 中立：** 服务不得调用或内嵌任何 converter/parser/model/vector/storage 引擎；不读取文件字节（REQ-MA-013）。
- **不硬编码 DB / 无原始 secret：** datasource 由配置驱动；源码、配置、日志、种子中无原始凭证、私有端点或真实公司数据（REQ-MA-012）。
- **保留 trace：** 每条持久化/返回的 file 与 chunk 携带 `source_path`（相对）、`confidence` 与 `review_status`；生成内容绝不默认 `APPROVED`（REQ-MA-011）。
- **分层：** controller → application service → repository → migration；编排不放在 controller；保留空 adapter seam（REQ-MA-008）。
- **契约先行：** API guide 与数据模型须在实现前被接受（REQ-PROD-073）。

## API 面（行为摘要）

完整请求/响应形态见 API 实现指南。本规格固定可观察行为。

| 方法 + 路径 | 行为 | 故事 |
|---|---|---|
| `GET /api/spaces` | Knowledge Space 卡片分页列表；支持 `page`、`size`、`status` 过滤。 | US-MA-001 |
| `GET /api/spaces/{spaceId}` | 完整 space 详情；未知则 `404` 信封。 | US-MA-001 |
| `POST /api/spaces` | 创建 space；服务端设置 `id`、默认 `status`、时间戳；DTO 非法则 `400`。 | US-MA-002 |
| `GET /api/spaces/{spaceId}/batches` | 某 space 的分页批次，含派生 metrics。 | US-MA-003 |
| `GET /api/batches/{batchId}` | 批次详情，含 metrics 与处理状态。 | US-MA-003 |
| `POST /api/spaces/{spaceId}/batches` | 从 inventory metadata 创建批次 + file item；不触碰字节/引擎。 | US-MA-004 |
| `GET /api/batches/{batchId}/files` | 分页 file item，含 status/confidence/review/trace；支持 `status` 过滤。 | US-MA-005 |
| `GET /api/files/{fileId}` | file item 详情。 | US-MA-005 |
| `GET /api/files/{fileId}/chunks` | 某文件的 source chunk（溯源记录）。 | US-MA-005 |
| `POST /api/files/{fileId}/reviews` | 追加 review 记录；更新目标 `review_status`。 | US-MA-006 |
| `GET /api/files/{fileId}/reviews` | 按时间顺序返回只追加 review 历史。 | US-MA-006 |

wiki-page、graph-node、graph-edge 表存在（已迁移 + 种子化），但本切片**不暴露任何端点**（REQ-MA-014）。

## 响应信封

所有响应使用同一信封：

```json
{ "success": true, "data": { }, "error": null, "meta": { "page": 0, "size": 20, "total": 42 } }
```

- `success`：布尔。
- `data`：成功时为载荷，出错时为 `null`。
- `error`：失败时为 `{ "code": "VALIDATION_ERROR", "message": "用户安全文本", "fields": { "name": "must not be blank" }, "timestamp": 1750000000000, "path": "/api/spaces" }`，否则为 `null`。`timestamp`（epoch 毫秒）与 `path` 仅在错误响应出现，镜像 Spring 默认错误体；成功响应保持精简。
- `meta`：列表端点出现（分页）。`total` 反映完整过滤后计数。

## 校验规则

- `POST /spaces`：`name` 必填（非空，≤200）；`type` ∈ {`document`,`faq`}；`index_strategy` ∈ {`rag`,`wiki`}；未知字段按 DTO 拒绝或忽略。
- `POST /batches`：`source_kind` ∈ {`folder`,`zip`}；每个 inventory 文件需 `source_path`（相对）、`source_type`、`status` ∈ 允许的 `FileStatus`、`confidence` ∈ [0,1]。
- `POST /reviews`：`action` ∈ {`APPROVE`,`NEED_FIX`,`OCR_REQUIRED`}；`reviewer` 必填；`affected_chunks` 为可选 chunk id 列表。
- 任何含绝对路径、`..` 穿越或盘符/主机前缀的 `source_path` 一律拒绝为 `400`。
- 非法输入绝不到达持久化；响应为 `400` `VALIDATION_ERROR` 信封并附字段详情。

## 状态模型

### 文件状态（持久化）

严格取自 `docs/batch-processing-design.md` 的允许集合：
`NEW`、`UPLOADED`、`PDF_CONVERTED`、`PDF_CONVERT_FAILED`、`MARKDOWN_GENERATED`、`OCR_REQUIRED`、`LOW_CONFIDENCE`、`REVIEW_REQUIRED`、`APPROVED`、`PUBLISHED`、`FAILED`、`UNSUPPORTED`。本切片不得引入新状态。

### Review 状态（持久化，按目标）

`REVIEW_REQUIRED`、`APPROVED`、`NEED_FIX`、`OCR_REQUIRED`、`PUBLISHED`（REQ-PROD-030）。

Review 动作 → 目标 `review_status`：

```
APPROVE       → APPROVED
NEED_FIX      → NEED_FIX
OCR_REQUIRED  → OCR_REQUIRED
```
`PUBLISHED` 只由后续发布切片设置，本 metadata 服务绝不设置。生成/低置信度项持久化为 `REVIEW_REQUIRED`，绝不自动 `APPROVED`。

### 批次 metrics（派生）

`total`、`pdfConverted`、`markdownGenerated`、`reviewRequired`、`failed`、`unsupported` 在读取时由批次的 file item 计算——而非作为独立、易漂移的列集存储（单一真实来源）。

## 错误行为

| 情况 | HTTP | 信封 error code |
|---|---|---|
| 未知 space/batch/file id | 404 | `NOT_FOUND` |
| 非法/畸形 DTO 或路径 | 400 | `VALIDATION_ERROR`（含 `fields`） |
| 重复/冲突创建 | 409 | `CONFLICT` |
| 未预期服务端故障 | 500 | `INTERNAL_ERROR`（通用消息；详情仅服务端记录，无堆栈/secret 泄露） |

错误消息用户安全：无堆栈、SQL、secret、内部主机名或私有绝对路径（REQ-MA-009、REQ-PROD-077）。

## 持久化与迁移行为

- schema 只由带版本 Flyway migration 创建；共享环境不用运行时自动 DDL（`spring.jpa.hibernate.ddl-auto=validate`）。
- 一个种子 migration 插入与前端基线 mock 对齐的 mock/示例行（spaces、至少一个 batch、file item、chunk、一条 review 记录，以及示例 wiki-page/graph 行）。无真实公司数据或 secret。
- migration 不可变、按版本排序；`mvn verify` 运行 Flyway 校验。

## Adapter 与未来真实说明（记录，不实现）

- 保留一个空的 adapter 包/接口 seam（如 `adapter/`，无具体引擎）。真实转换/解析/存储在 Phase 3 adapter 切片到来，并通过本服务写入 file/chunk metadata，而非绕过它。
- 确定性 metadata 先于任何未来 LLM enrichment；LLM 派生内容保持 `REVIEW_REQUIRED`。

## 验收矩阵

| 检查 | 需求 | 可观察结果 |
|---|---|---|
| AC-MA-01 | REQ-MA-001、002、010 | `GET /api/spaces` 返回已种子化 space 的分页信封；详情返回完整 metadata；未知 id → 404 信封。 |
| AC-MA-02 | REQ-MA-002、009 | `POST /api/spaces` 持久化 space，含服务端时间戳 + 默认状态；非法载荷 → 400 含字段错误。 |
| AC-MA-03 | REQ-MA-003、010 | `GET /api/spaces/{id}/batches` 与 `GET /api/batches/{id}` 返回批次，metrics 由 file item 派生。 |
| AC-MA-04 | REQ-MA-003、004、013 | `POST /api/spaces/{id}/batches` 从 inventory 持久化批次 + file item，无引擎调用、无字节读取。 |
| AC-MA-05 | REQ-MA-004、011 | file item 持久化/返回确切 `FileStatus` 值、相对 `source_path`、`confidence`、`review_status`；生成项为 `REVIEW_REQUIRED`。 |
| AC-MA-06 | REQ-MA-005、011 | `GET /api/files/{id}/chunks` 返回 source chunk，含 trace + confidence + review status。 |
| AC-MA-07 | REQ-MA-006、011 | `POST /api/files/{id}/reviews` 追加不可变记录并更新目标 review 状态；历史按时间顺序。 |
| AC-MA-08 | REQ-MA-007、015 | Flyway 创建全部 8 张初始表并种子化 mock 行；`mvn verify` 通过 Flyway 校验。 |
| AC-MA-09 | REQ-MA-009、077 | 所有错误信封用户安全（无堆栈/SQL/secret/私有路径）；`source_path` 路径穿越 → 400。 |
| AC-MA-10 | REQ-MA-008、012、013 | 分层结构成立；datasource 配置驱动；不调用任何 converter/parser/model/vector/storage 引擎；源码/配置/日志无原始 secret。 |
| AC-MA-11 | REQ-MA-014 | wiki-page/graph 表存在且已种子化但不暴露端点；该省略记录于溯源。 |

## 待确认问题

- 写路径（`POST`）在此启用 vs. 只读 + 仅种子（默认：启用 space/batch/review 创建）。
- 前端在本切片切换到 API vs. 后续（默认：后续；本切片交付 API + 契约测试）。
- `review_status` 分叉：持久化集合遵循 REQ-PROD-030（`NEED_FIX`/`OCR_REQUIRED`/`PUBLISHED`），比前端 `ReviewStatus`（含 `REJECTED`）更宽。默认映射：遗留前端 `REJECTED` → `NEED_FIX`；重新对齐前端类型延后至 T-MA-014。
