# 数据流：Metadata API

## 状态

草稿。Phase 2。切片 `metadata-api`。派生自 `docs/03-spec/metadata-api-spec.md` 与 `docs/04-architecture/metadata-api-architecture.md`。

## 请求生命周期（所有端点）

```
Client ─▶ Controller (web/)
             │  对请求 DTO 做 Bean Validation
             │  非法 → GlobalExceptionHandler → 400 VALIDATION_ERROR 信封
             ▼
        Application Service (app/)
             │  编排、实体↔DTO 映射、派生 metrics、不变量
             ▼
        Repository (repo/, Spring Data JPA)
             │
             ▼
        PostgreSQL（Flyway 管理 schema `atlas`）
             ▲
             │  实体
        Application Service ── 组装成功信封 + meta（分页）
             ▲
        Controller ─▶ Client（JSON 信封）
```

本流程任何处都不出现 converter/parser/model/vector/storage 引擎，也不读取文件字节（REQ-MA-013）。

## 流程 1 —— 创建 Knowledge Space（`POST /api/spaces`）

```
DTO {name, description, type, index_strategy, owner}
  → 校验（name 非空；type∈document|faq；index_strategy∈rag|wiki）
  → app：构建 Space 实体；服务端设 id、status=默认、created_at/updated_at=now
  → repo.save
  → 201 信封 { data: SpaceDetail }
非法 → 400 VALIDATION_ERROR { fields }
```

## 流程 2 —— 从 inventory 创建批次（`POST /api/spaces/{spaceId}/batches`）

```
DTO { source_kind, name, owner, files:[{source_path, source_type, status, confidence, review_status, chunks?[]}] }
  → 校验 spaceId 存在（否则 404）；source_kind∈folder|zip
  → 每个文件：校验 source_path 相对且无穿越；status∈FileStatus；confidence∈[0,1]
  → 不变量：generated/低置信度文件 ⇒ review_status 默认 REVIEW_REQUIRED（绝不 APPROVED）
  → app：一个事务内持久化 Batch + FileItem（+ 提供的 SourceChunk）
  → metrics 不存储；读取时由持久化 file item 派生
  → 201 信封 { data: BatchDetail 含派生 metrics }
无支持/非法文件路径 → 400 VALIDATION_ERROR
```

只写 metadata；服务不做任何转换、解析、解压或字节读取。

## 流程 3 —— 读取批次及派生 metrics（`GET /api/batches/{batchId}`）

```
GET batchId
  → repo.findBatch + repo.findFileItemsByBatch
  → app.computeMetrics(fileItems)：{ total, pdfConverted, markdownGenerated, reviewRequired, failed, unsupported }
  → 200 信封 { data: BatchDetail(metrics) }
未知 → 404 NOT_FOUND
```

metrics 是 file-item 状态的纯函数——单一真实来源，绝非可漂移的存储计数器。

## 流程 4 —— 读取 file item + chunk（`GET /api/batches/{id}/files`、`GET /api/files/{id}/chunks`）

```
GET files?status=&page=&size=
  → repo.pageFileItemsByBatch(filter)
  → 逐条映射：{id, source_path(相对), source_type, status, confidence, review_status, 产物路径, error_message}
  → 200 信封 { data:[...], meta:{page,size,total} }

GET files/{id}/chunks
  → repo.findChunksByFile
  → 映射：{id, source_file, page/section, confidence, review_status}
  → 200 信封 { data:[...] }
```

每条返回路径为相对；trace + confidence + review status 恒存在（REQ-MA-011）。

## 流程 5 —— 追加 review 记录（`POST /api/files/{fileId}/reviews`）

```
DTO { action∈APPROVE|NEED_FIX|OCR_REQUIRED, reviewer, comment?, affected_chunks?[] }
  → 校验 fileId 存在（否则 404）；action 与 reviewer 必填
  → app：追加 ReviewRecord（不可变，服务端时间戳）
  → app：按动作映射更新 file_item.review_status
        APPROVE→APPROVED, NEED_FIX→NEED_FIX, OCR_REQUIRED→OCR_REQUIRED
        （此处绝不设 PUBLISHED）
  → 201 信封 { data: ReviewRecord }

GET files/{fileId}/reviews → 时间顺序只追加历史
```

## 迁移与种子流程（启动 / `mvn flyway:migrate`）

```
Flyway 于干净 DB
  → V1__init_schema.sql        创建 space、batch、file_item、source_chunk、
                                wiki_page、review_record、graph_node、graph_edge（+ 索引）
  → V2__seed_mock_metadata.sql 插入与前端基线对齐的 mock/示例行
                                （无真实数据、无 secret）
  → 应用以 ddl-auto=validate 启动（schema 与实体匹配否则启动失败）
mvn verify → Flyway validate + 契约/集成测试（Testcontainers）
```

## 错误流程（横切）

```
任一层抛出
  → GlobalExceptionHandler
      ValidationException/BindException → 400 VALIDATION_ERROR（+fields）
      NotFoundException                 → 404 NOT_FOUND
      ConflictException                 → 409 CONFLICT
      其他一切                          → 500 INTERNAL_ERROR（通用消息）
  → 响应信封不含任何堆栈、SQL、secret、主机名或私有路径
  → 完整详情仅服务端记录
```

## 跨流程数据安全不变量

- 仅 metadata：无引擎调用、无字节读取、除 JDBC 外无外部网络。
- `source_path` 持久化前恒相对且经穿越检查。
- 生成内容持久化 `REVIEW_REQUIRED`；只有显式 review 推进状态。
- `review_record` 只追加；metrics 派生而非存储。
- 种子与响应绝不含真实公司数据、原始 secret 或私有路径。
