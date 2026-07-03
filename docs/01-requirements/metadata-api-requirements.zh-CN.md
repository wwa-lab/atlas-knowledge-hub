# 需求：Metadata API

## 状态

草稿。Phase 2（后端 metadata API 与持久化）。切片 `metadata-api`。所有权边界：后端 + 数据库。

## 目的

定义 Atlas 第一个真实后端切片：一个 Spring Boot **metadata 控制平面**，在 PostgreSQL（经 Flyway）中持久化 Knowledge Space、批次、文件、source chunk 和 Review metadata，并暴露内部 REST API，使前端从内存 mock 改为读写真实 metadata。本切片在**契约层面消除 mock**，但仍交付 mock/示例种子数据——不含任何真实公司内容。

本切片**只记录 metadata 和状态**，不执行转换、解析、模型、向量或存储工作——这些仍属于 Phase 3 adapter 切片。

## 来源文档

- `docs/00-context/slice-roadmap.md` —— Phase 2 API 行（验证、约束、"API guide required"）。
- `docs/01-requirements/requirement.md` —— Phase 2 产品需求：REQ-PROD-008、012、014、030、031、057、061、068–073、076。
- `docs/04-architecture/knowledge-space-data-model.md` —— space、batch、file_item、wiki_page、source_chunk、graph_node、graph_edge 的权威实体/字段形态。
- `docs/04-architecture/folder-upload-data-model.md` —— 前端已使用的 `FileStatus` / `ReviewStatus` 枚举与 metrics 形态。
- `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` —— 本切片正式化的候选端点命名。
- `docs/batch-processing-design.md` —— 权威 `FileStatus` 集合。
- `docs/markdown-standard.md` —— source trace / confidence / review status 字段。
- 前端基线（API 消费方）：`frontend/public/atlas-prototype.html`、`frontend/src/types.ts`、`frontend/src/data/atlasMock.ts`。

## 范围

### 范围内

- Spring Boot 服务骨架，按 `DEVELOPMENT_STANDARDS.md` 分层（controller → application service → repository → Flyway migration），并保留一个空的 adapter seam。
- 通过 Flyway migration 建立 PostgreSQL schema，覆盖**初始实体集**（REQ-PROD-071）：`space`、`batch`、`file_item`、`source_chunk`、`wiki_page`、`review_record`、`graph_node`、`graph_edge`。
- 为前端已渲染的 metadata 提供读写 REST 端点：Knowledge Space（列表 / 详情 / 创建）、Batch（按 space 列表 / 详情 / 创建）、File Item（按 batch 列表 / 详情）、Source Chunk（按 file 列表）、Review Record（按 target 列表 / 追加）。
- 一致的 API 响应信封；列表端点支持分页与过滤；边界处 DTO 校验；用户安全的错误信封。
- 一份与前端基线 mock 对齐的 Flyway 种子 metadata，使前端能从内存 mock 切换到 API 而无视觉漂移。
- 在每条持久化与返回的记录上保留 source trace、confidence 和 review status。

### 范围外（已延后；记录于溯源）

- Converter/parser/model/vector/storage **执行**——属 Phase 3 adapter 切片。本服务只存 metadata/状态。
- 真实字节上传、解压、流式或对象存储——属未来 storage adapter。`POST /batches` 只接受预先算好的 inventory metadata。
- 图谱抽取与图谱查询 API（`knowledge-graph`）、Ask/RAG（`ask-rag`）、完整 wiki 正文渲染与发布状态机（`review-publish`）——此处仅创建其 **metadata 表**。
- 认证、RBAC 强制执行与审计加固——Phase 4。
- 模型/引擎配置、成员、注册管理 API——各自后续切片。

## 需求

| ID | 需求 | 优先级 | 追溯 |
|---|---|---|---|
| REQ-MA-001 | 提供 Spring Boot 内部 REST metadata 服务，成为 Knowledge Space、批次、文件、source chunk、Review metadata 的真实来源，在契约层面替代前端内存 mock。 | Must | REQ-PROD-068 |
| REQ-MA-002 | 持久化 Knowledge Space metadata：`id`、`name`、`description`、`type`（`document`/`faq`）、`index_strategy`（`rag`/`wiki`）、`owner`、`status`、`document_count`、`wiki_page_count`、`review_count`、`created_at`、`updated_at`。 | Must | REQ-PROD-008、001、005–007 |
| REQ-MA-003 | 持久化 Batch metadata：`id`、`space_id`、`name`、`source_kind`（`folder`/`zip`）、`owner`、`uploaded_at`、派生 metrics 与处理状态。 | Must | REQ-PROD-009、011 |
| REQ-MA-004 | 使用 `docs/batch-processing-design.md` 的确切 `FileStatus` 集合持久化 File Item metadata，含 `source_path`（仅相对路径）、`source_type`、产物路径 metadata、`confidence`、`review_status` 与用户安全的 `error_message`。 | Must | REQ-PROD-012、014 |
| REQ-MA-005 | 持久化 Source Chunk 溯源记录（`source_file`、`page`/`section`、`confidence`、`review_status`），关联到 file item，使 trace 在持久化后仍存续。 | Must | REQ-PROD-014、023 |
| REQ-MA-006 | 持久化只追加的 Review Record 日志（`reviewer`、`action`、`timestamp`、`comment`、`affected_chunks`）与允许的 review 状态集合。 | Must | REQ-PROD-030、031 |
| REQ-MA-007 | 通过 Flyway migration 为所有初始实体创建初始 schema：`space`、`batch`、`file_item`、`source_chunk`、`wiki_page`、`review_record`、`graph_node`、`graph_edge`。 | Must | REQ-PROD-070、071 |
| REQ-MA-008 | 后端分层：controller → application service → repository → migration，业务编排不放在 controller，并为 Phase 3 保留空的 adapter seam。 | Must | REQ-PROD-072 |
| REQ-MA-009 | 在 API 边界校验每个入站 DTO；以一致、用户安全的错误信封拒绝非法输入（不含堆栈、secret 或私有路径）。 | Must | REQ-PROD-076、077 |
| REQ-MA-010 | 返回一致的成功/错误响应信封；列表端点支持分页与按状态/space 过滤。 | Must | API 标准 |
| REQ-MA-011 | 在每条持久化与返回的 file/chunk 记录上保留 source trace、confidence 与 review status；生成内容绝不默认置为 `APPROVED`。 | Must | REQ-PROD-024、026、032；Trace & Review |
| REQ-MA-012 | 不把单一数据库硬编码为唯一实现：datasource 由配置驱动；源码、配置、种子数据中不含原始 secret、凭证、私有端点或真实公司数据（允许 status-only 字段）。 | Must | REQ-PROD-074、075；数据安全 |
| REQ-MA-013 | 服务不得调用或内嵌任何 converter/parser/model/vector/storage 引擎；只记录 metadata/状态，把这些留给未来 Phase 3 adapter。 | Must | REQ-PROD-015–017；Adapter gate |
| REQ-MA-014 | 图谱查询、Ask、wiki 正文渲染与发布状态机端点不在范围内；此处仅创建其 metadata 表。该省略记录于溯源。 | Must | 范围边界 |
| REQ-MA-015 | 交付一份与前端基线 mock 对齐的 Flyway 种子 metadata migration，使前端能从内存 mock 切换到 API 而无视觉漂移。 | Should | REQ-PROD-074；mock-only |

## 约束

- **验证（Phase 2 API）：** `mvn verify` · Flyway migration 校验 · API 契约测试。外加基线的 `git diff --check`、新网络/新依赖扫描、secret/私有路径扫描。
- **硬约束：** 不把单一 DB 作为唯一实现；secret 掩码/status-only，绝不原始；无真实公司数据；无外部云调用。
- **API guide 为必需**，且必须在实现开始前与数据模型一起被接受（REQ-PROD-073）。

## 待确认问题

- `POST /spaces` 与 `POST /batches` 在本切片是启用的写路径，还是种子数据的只读镜像（写延后）？（默认：为 space/batch/review 启用创建；file-item 写保持内部/仅种子，直到 ingestion adapter 存在。）
- 前端在本切片切换到 API，还是 API 切换落在后续 FE 集成切片？（默认：本切片交付 API + 契约测试；前端切换为独立任务，记于溯源。）
- 单一 Postgres schema 还是按域分 schema。（默认：单一 `atlas` schema，一套 migration。）
