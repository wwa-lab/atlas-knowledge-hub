# 用户故事：Metadata API

## 状态

草稿。Phase 2。切片 `metadata-api`。派生自 `docs/01-requirements/metadata-api-requirements.md`。

## 角色

- **前端应用** —— 目前读取内存 mock、将改为通过 HTTP 读写 metadata 的 Vue 客户端。
- **知识库管理员** —— 创建 space 与批次；期望持久化与一致读取。
- **SME Reviewer** —— 针对文件/chunk 追加 review 记录。
- **后端工程师（Codex）** —— 面向稳定契约与 migration 实现。
- **平台运维** —— 无需改代码即可将服务指向配置驱动的 datasource。

## 故事

### US-MA-001 —— 从 API 读取 Knowledge Space
作为**前端应用**，我希望从稳定端点获取 Knowledge Space 的列表与详情，以便首页知识库与 space 外壳从持久化 metadata 而非内存 mock 渲染。

**验收（Given/When/Then）**
- 给定已种子化的 space，当调用 `GET /api/spaces`，则返回分页信封，含 `name`、`description`、`owner`、`status` 与计数。
- 给定合法 `spaceId`，当调用 `GET /api/spaces/{spaceId}`，则返回完整 space 详情 metadata。
- 给定未知 `spaceId`，当调用详情端点，则返回 `404` 用户安全错误信封（无堆栈、无内部路径）。

追溯：REQ-MA-001、REQ-MA-002、REQ-MA-010。

### US-MA-002 —— 创建 Knowledge Space
作为**知识库管理员**，我希望以类型与索引策略创建 Knowledge Space，以便新 space 携服务端时间戳与状态持久化。

**验收**
- 给定合法创建载荷（`name`、`type`、`index_strategy`），当调用 `POST /api/spaces`，则持久化一个 space，`created_at`/`updated_at` 由服务端设置、`status` 取默认，并在成功信封中返回。
- 给定非法载荷（缺 `name` 或 `type` 错误），当调用端点，则返回 `400` 及字段级、用户安全的校验错误。

追溯：REQ-MA-002、REQ-MA-009、REQ-MA-010。

### US-MA-003 —— 读取某 space 的批次及 metrics
作为**前端应用**，我希望列出某 space 的批次并打开批次详情，以便 Documents 页展示持久化的批次 metrics 与状态。

**验收**
- 给定含批次的 space，当调用 `GET /api/spaces/{spaceId}/batches`，则返回分页批次及派生 metrics（total/pdfConverted/markdownGenerated/reviewRequired/failed/unsupported）。
- 给定 `batchId`，当调用 `GET /api/batches/{batchId}`，则返回批次详情及 metrics 与处理状态。
- 给定批次 metrics，则由持久化的 file item 计算得出，而非独立、易漂移的副本。

追溯：REQ-MA-003、REQ-MA-004、REQ-MA-010、REQ-MA-011。

### US-MA-004 —— 从预算 inventory 创建批次
作为**知识库管理员**，我希望提交 inventory metadata 创建批次，以便批次及其 file item 持久化，而服务不触碰真实字节或 parser。

**验收**
- 给定含 `source_kind` 与 inventory 文件 metadata 的 `POST /api/spaces/{spaceId}/batches` 载荷，当调用，则持久化批次与 file item，状态取自允许的 `FileStatus` 集合。
- 给定载荷，当持久化时，则不调用任何 converter/parser/storage 引擎（仅 metadata），且不读取文件字节。
- 给定处于 generated/低置信度状态的 file item，则其 `review_status` 持久化为 `REVIEW_REQUIRED`，绝不默认 `APPROVED`。

追溯：REQ-MA-003、REQ-MA-004、REQ-MA-011、REQ-MA-013。

### US-MA-005 —— 读取 file item 及其 source-chunk 溯源
作为**开发者/Reviewer**，我希望读取批次的 file item 及每个文件的 source chunk，以便 source trace 与 confidence 在持久化后仍存续并在 UI 可见。

**验收**
- 给定批次，当调用 `GET /api/batches/{batchId}/files`，则返回 file item，含 `source_path`（相对）、`status`、`confidence`、`review_status` 与产物路径 metadata。
- 给定 file item，当调用 `GET /api/files/{fileId}/chunks`，则返回 source chunk，含 `source_file`、`page`/`section`、`confidence`、`review_status`。
- 给定任一返回路径，则为相对路径——绝非私有绝对路径。

追溯：REQ-MA-004、REQ-MA-005、REQ-MA-011、REQ-MA-012。

### US-MA-006 —— 追加 review 记录
作为**SME Reviewer**，我希望针对文件或 chunk 追加 review 动作，以便 review 历史可审计，且目标的 review 状态反映最新决定。

**验收**
- 给定合法 review 载荷（`action` ∈ Approve/Need Fix/OCR Required、`reviewer`、可选 `comment`、`affected_chunks`），当调用 `POST /api/files/{fileId}/reviews`，则以服务端时间戳不可变地追加一条 review 记录。
- 给定该追加，则目标的 `review_status` 按允许的 review 状态集合更新。
- 给定 `GET /api/files/{fileId}/reviews`，则按时间顺序返回只追加历史。

追溯：REQ-MA-006、REQ-MA-009、REQ-MA-011。

### US-MA-007 —— 确定性地迁移与种子化 schema
作为**后端工程师**，我希望 Flyway 为所有初始实体创建初始 schema 并种子化 mock/示例 metadata，以便全新数据库无需手工步骤即与前端基线对齐。

**验收**
- 给定干净数据库，当应用启动（或运行 `mvn flyway:migrate`），则所有初始表（`space`、`batch`、`file_item`、`source_chunk`、`wiki_page`、`review_record`、`graph_node`、`graph_edge`）由带版本 migration 创建。
- 给定种子 migration，则插入与前端基线 mock 对齐的 mock/示例行——无真实公司数据、无 secret。
- 给定 `mvn verify`，则 Flyway migration 校验通过，migration 不可变/带版本。

追溯：REQ-MA-007、REQ-MA-012、REQ-MA-015。

### US-MA-008 —— 配置驱动 datasource，无泄露 secret
作为**平台运维**，我希望 datasource 经环境/配置设置，以便不把任何数据库硬编码为唯一实现，源码中也无原始凭证。

**验收**
- 给定服务配置，当检视时，则 datasource URL/凭证来自外部化配置（env/profile），而非硬编码字面量。
- 给定任何响应或日志，则绝不含原始 secret、凭证、私有端点或内部堆栈。
- 给定代码库，则不直接调用任何 converter/parser/model/vector/storage 引擎（仅 adapter seam）。

追溯：REQ-MA-008、REQ-MA-012、REQ-MA-013。

## 范围外（本切片）

- 图谱查询、Ask/RAG、wiki 正文渲染、发布状态机（仅创建 metadata 表）。
- 真实上传/解析/转换；认证/RBAC；模型/引擎/成员/注册 API。

## 依赖

- 已接受的 `metadata-api` 数据模型与 API 实现指南（REQ-PROD-073 gate）。
- 与 `docs/batch-processing-design.md` 及前端 `frontend/src/types.ts` 的枚举/字段对齐。

## 待确认问题

- 本切片是否启用 `POST` 写路径，还是只读 + 仅种子？（见需求待确认问题。）
- 前端在此切换到 API，还是在后续切片？（默认：后续。）
