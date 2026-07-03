# 任务：Metadata API

## 状态

草稿。Phase 2（后端 metadata API 与持久化）。切片 `metadata-api`。可执行清单，派生自 `docs/05-design/metadata-api-design.md`、`docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` 与 `docs/03-spec/metadata-api-spec.md`（行为真实来源）。

## 前置条件（gate —— REQ-PROD-073）

- [ ] 数据模型（`docs/04-architecture/metadata-api-data-model.md`）已接受。
- [ ] API guide（`docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`）已接受。
- [ ] 与 Phase 1 前端基线的枚举/字段形态已对齐（`frontend/src/types.ts`、`docs/batch-processing-design.md`）。

三项全部勾选前不得开始实现。

## 全局约束（适用于每个任务）

- **Phase 2 后端。** Java + Spring Boot；PostgreSQL + Flyway；`ddl-auto=validate`。
- **仅 metadata / adapter 中立：** 不调用任何 converter/parser/model/vector/storage 引擎；不读取文件字节；`adapter/` 保持为空（REQ-MA-013）。
- **不硬编码 DB / 无原始 secret：** datasource 来自外部化配置；源码、配置、日志、种子中无凭证、私有端点、主机名或真实公司数据（REQ-MA-012）。
- **保留 trace：** 仅相对 `source_path`；`confidence` + `review_status` 恒存在；生成内容默认 `REVIEW_REQUIRED`，绝不 `APPROVED`（REQ-MA-011）。
- **范围外科手术式：** 只触碰 `backend/**` 与 SDD 文档；本切片不改前端运行时代码（前端切换延后）。

## 任务

| ID | 任务 | 负责 | 优先 | 依赖 | 映射（REQ / 规格） | 验证 |
|---|---|---|---|---|---|---|
| T-MA-001 | 将 `backend/` 提升为可构建的 Spring Boot Maven 项目：`pom.xml`（Spring Web、Spring Data JPA、Validation、Flyway、PostgreSQL 驱动、Testcontainers）、`MetadataApiApplication`、按设计与 `docs/BACKEND_CODING_STANDARD.md` 的**按功能分包**布局（`space/ batch/ file/ review/ wiki/ graph/ common/ adapter/`）。 | Codex | 高 | — | REQ-MA-001、REQ-MA-008 / 设计"模块布局" | `cd backend && mvn -q compile` 成功；包结构为 feature-first 且匹配设计。 |
| T-MA-002 | 添加配置驱动 `application.yml`：`spring.datasource.*` 取自 `${ATLAS_DB_*}` 占位符（无字面量），`ddl-auto=validate`，启用 Flyway，`test`/`local` profile。添加 `SECURITY.md`/类注释：无 auth、仅内部、RBAC 延后 Phase 4。 | Codex | 高 | T-MA-001 | REQ-MA-012、REQ-MA-008 / 设计"配置" | `grep -RnE '(jdbc:|password:\s*\S)' backend/src/main/resources` 无字面量 secret；`test` profile 下上下文加载。 |
| T-MA-003 | 编写 `V1__init_schema.sql`：枚举以 text+CHECK（或 PG enum）、全部 8 张表（`space`、`batch`、`file_item`、`source_chunk`、`review_record`、`wiki_page`、`graph_node`、`graph_edge`）、FK、按数据模型的索引。 | Codex | 高 | T-MA-001 | REQ-MA-007 / 数据模型"表" | Testcontainers PG 上 Flyway migrate 成功；`ddl-auto=validate` 对实体通过。 |
| T-MA-004 | 编写 `V2__seed_mock_metadata.sql`：与前端基线对齐的 mock/示例行（spaces、≥1 批次、跨代表性状态的 file item、chunk、一条 review 记录、示例 wiki-page/graph 行）。仅相对路径，无真实数据，无 secret。 | Codex | 高 | T-MA-003 | REQ-MA-015、REQ-MA-012 / 数据模型"Migration" | 集成测试断言种子计数/枚举值匹配前端 mock；secret/路径扫描干净。 |
| T-MA-005 | 实现实体 + 枚举，与其功能包共置（`Space` 在 `space/`、`Batch` 在 `batch/`、`FileItem`/`SourceChunk` 在 `file/`、`ReviewRecord` 在 `review/`、`WikiPage` 在 `wiki/`、`GraphNode`/`GraphEdge` 在 `graph/`）；跨功能枚举（`FileStatus`、`ReviewStatus`、`SourceKind`、`SourceType`）放 `common/enums`，功能内枚举（`SpaceType`、`IndexStrategy`、`SpaceStatus`、`ReviewAction`、`GraphNodeType`、`GraphEdgeType`）随其功能。枚举值与 `frontend/src/types.ts` / `docs/batch-processing-design.md` 完全一致。添加不变量辅助。 | Codex | 高 | T-MA-001 | REQ-MA-002–007、REQ-MA-011 / 数据模型"枚举" | 单元测试断言 `FileStatus` 集合 == 允许集合（无多余）；枚举值字符串匹配前端。 |
| T-MA-006 | 实现各功能的 Spring Data JPA repository（`space/SpaceRepository`、`batch/BatchRepository`、`file/FileItemRepository`+`SourceChunkRepository`、`review/ReviewRecordRepository`），含分页/过滤查询（`findBySpaceId`、`findByBatchIdAndStatus`、`findByFileItemId`、时间顺序 review 历史）。 | Codex | 高 | T-MA-003、T-MA-005 | REQ-MA-010 / 设计"Repository" | 集成测试（Testcontainers）覆盖 CRUD + 分页 + 过滤。 |
| T-MA-007 | 实现 `common/` 跨切面：`common/web` 信封 + DTO（`ApiEnvelope`、`ErrorBody`、`PageMeta`）+ `GlobalExceptionHandler`；`common/validation` 自定义 `@RelativePath` 校验器；各功能带 Bean Validation 的请求 record；异常→`{400,404,409,500}` 用户安全映射。 | Codex | 高 | T-MA-001 | REQ-MA-009、REQ-MA-010 / 规格"响应信封","错误行为" | 契约测试：坏 DTO → 400 含 `fields`；未知 id → 404；错误体不含堆栈/SQL/secret/绝对路径。 |
| T-MA-008 | 实现 Space 端点（`GET /api/spaces`、`GET /api/spaces/{id}`、`POST /api/spaces`）+ `SpaceService`：服务端设 id/status/时间戳、校验。 | Codex | 高 | T-MA-006、T-MA-007 | REQ-MA-002 / API guide"Knowledge Spaces"；AC-MA-01、AC-MA-02 | 列表/详情/创建的契约测试含 400 + 404。 |
| T-MA-009 | 实现 Batch 端点（`GET /api/spaces/{id}/batches`、`GET /api/batches/{id}`、`POST /api/spaces/{id}/batches`）+ `BatchService` + `MetricsCalculator`（metrics 由 file item 派生）。创建批次持久化 file item + 可选 chunk；无引擎/字节访问。 | Codex | 高 | T-MA-008 | REQ-MA-003、REQ-MA-004、REQ-MA-013 / API guide"批次"；AC-MA-03、AC-MA-04 | 单元测试：`MetricsCalculator` 派生。契约测试：从 inventory 创建持久化项；生成项保持 `REVIEW_REQUIRED`；adapter-seam 守卫测试通过。 |
| T-MA-010 | 实现 File 端点（`GET /api/batches/{id}/files`、`GET /api/files/{id}`、`GET /api/files/{id}/chunks`）+ `FileService`：相对路径，每个响应含 trace/confidence/review。 | Codex | 高 | T-MA-009 | REQ-MA-004、REQ-MA-005、REQ-MA-011 / API guide"File Items"；AC-MA-05、AC-MA-06 | 契约测试断言相对路径、trace 字段存在；`?status=` 过滤生效。 |
| T-MA-011 | 实现 Review 端点（`POST /api/files/{id}/reviews`、`GET /api/files/{id}/reviews`）+ `ReviewService`：只追加记录、action→review_status 映射（`APPROVE→APPROVED`、`NEED_FIX→NEED_FIX`、`OCR_REQUIRED→OCR_REQUIRED`；绝不 `PUBLISHED`）。 | Codex | 高 | T-MA-010 | REQ-MA-006、REQ-MA-011 / API guide"Review"；AC-MA-07 | 契约测试：追加更新目标状态；历史按时间顺序；记录不可变。 |
| T-MA-012 | 添加 adapter-seam 守卫测试 + 确认无 wiki/graph/ask 端点：断言 `adapter/` 无引擎/网络依赖，无 controller 映射延后路由。 | Codex | 中 | T-MA-011 | REQ-MA-013、REQ-MA-014 / 架构"Adapter 边界"；AC-MA-10、AC-MA-11 | 测试扫描 `adapter/` 中的外联 HTTP 客户端/引擎 import；路由表排除 wiki/graph/ask。 |
| T-MA-013 | 完整验证与证据：运行 `mvn verify`（Flyway validate + 单元 + 契约 + 集成）、`git diff --check`、新依赖/网络扫描、secret/私有路径扫描。结果记录于溯源。 | Codex | 高 | T-MA-001→012 | Phase 2 验证行；全部 AC-MA | `cd backend && mvn verify` 绿；扫描干净；证据写入 `docs/00-context/metadata-api-traceability.md`。 |
| T-MA-014 | （延后，已记录）前端切换：把前端 mock provider 换成命中这些端点的 HTTP 客户端。**本切片不实现**——记为后续 FE 集成任务。 | — | 低 | T-MA-013 | REQ-MA-015 / 设计"消费方集成" | 本切片 N/A；记于溯源待确认问题。 |

## 验证命令（Phase 2 API 行）

```bash
cd backend
mvn verify                 # 编译 + Flyway validate + 单元 + 契约 + 集成（Testcontainers）
mvn -q flyway:migrate      # 可选：对 local/test datasource 的显式迁移检查
git diff --check           # 空白/冲突卫生
# secret / 私有路径扫描（无原始凭证、无绝对路径、无真实公司数据）
grep -RnE '(BEGIN.*PRIVATE KEY|password\s*=\s*[^$]|/Users/|/home/|C:\\\\)' backend/src || echo "clean"
```

指明任何跳过的检查及原因；绝不暗示未运行的检查已通过。

## 完成定义

- T-MA-001→013 全部完成（T-MA-014 明确延后）。
- `mvn verify` 绿：Flyway 校验、单元、契约、集成测试通过。
- 每个验收检查 AC-MA-01→11 由测试演示。
- 无引擎调用、无字节读取、`adapter/` 为空、datasource 配置驱动、无原始 secret、仅相对路径、生成内容 `REVIEW_REQUIRED`。
- 证据 + 残余风险记录于 `docs/00-context/metadata-api-traceability.md`。
