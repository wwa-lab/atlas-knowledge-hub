# 溯源：Metadata API

## 状态

已实现。Phase 2（后端 metadata API 与持久化）。切片 `metadata-api`。

当前状态：

- 完整双语 SDD 产物集已完成（EN + `.zh-CN.md`）。
- **已包含 API guide**（Phase 2 后端切片——必需，REQ-PROD-073）。
- 已在 `backend/` 实现 T-MA-001→013；T-MA-014 仍延后为 FE 切换任务。
- `docs/00-context/slice-roadmap.md` 中切片状态：✅ 已实现。

## 切片

`metadata-api` —— Spring Boot metadata 控制平面，在 PostgreSQL（Flyway）中持久化 Knowledge Space、批次、file-item、source-chunk 与 review metadata，并通过内部 REST API 提供。仅 metadata；无引擎执行。

## 来源输入

| 来源 | 角色 |
|---|---|
| `docs/00-context/slice-roadmap.md` | Phase 2 API 行：验证（`mvn verify` · Flyway 校验 · API 契约测试）、约束（不硬编码单一 DB、secret 掩码/status-only、无真实数据）、"API guide required"。 |
| `docs/01-requirements/requirement.md` | Phase 2 产品需求：REQ-PROD-008、012、014、030、031、068–073、076、077。 |
| `docs/04-architecture/knowledge-space-data-model.md` | 8 个初始实体的权威实体/字段形态。 |
| `docs/04-architecture/folder-upload-data-model.md` | 前端使用的 `FileStatus`/`ReviewStatus` 枚举 + metrics 形态。 |
| `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` | 此处正式化的候选端点命名。 |
| `docs/batch-processing-design.md` | 权威 `FileStatus` 集合。 |
| `docs/markdown-standard.md` | trace/confidence/review 字段。 |
| `frontend/src/types.ts`、`frontend/src/data/atlasMock.ts`、`frontend/public/atlas-prototype.html` | 前端消费方基线，用于枚举值/形态对齐。 |

## 产物映射

| 阶段 | EN | zh-CN |
|---|---|---|
| Requirements | `docs/01-requirements/metadata-api-requirements.md` | `…metadata-api-requirements.zh-CN.md` |
| User Stories | `docs/02-user-stories/metadata-api-stories.md` | `…metadata-api-stories.zh-CN.md` |
| Specification | `docs/03-spec/metadata-api-spec.md` | `…metadata-api-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/metadata-api-architecture.md` | `…metadata-api-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/metadata-api-data-flow.md` | `…metadata-api-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/metadata-api-data-model.md` | `…metadata-api-data-model.zh-CN.md` |
| Design | `docs/05-design/metadata-api-design.md` | `…metadata-api-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` | `…metadata-api-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/metadata-api-tasks.md` | `…metadata-api-tasks.zh-CN.md` |
| Traceability | `docs/00-context/metadata-api-traceability.md` | `…metadata-api-traceability.zh-CN.md` |

## API Guide 包含（记录决定）

与 Phase 1 前端切片（`knowledge-space`、`folder-upload`）不同，这是 Phase 2 后端切片：API 实现指南**必需且已包含**，须在实现开始前与数据模型一起被接受（REQ-PROD-073，SDD profile "Gates"）。

## 需求 → 故事 → 规格 → 任务 链接

| 需求 | 故事 | 规格（验收） | 任务 |
|---|---|---|---|
| REQ-MA-001 | US-MA-001 | AC-MA-01 | T-MA-001 |
| REQ-MA-002 | US-MA-001、US-MA-002 | AC-MA-01、AC-MA-02 | T-MA-005、T-MA-008 |
| REQ-MA-003 | US-MA-003、US-MA-004 | AC-MA-03、AC-MA-04 | T-MA-005、T-MA-009 |
| REQ-MA-004 | US-MA-004、US-MA-005 | AC-MA-04、AC-MA-05 | T-MA-005、T-MA-009、T-MA-010 |
| REQ-MA-005 | US-MA-005 | AC-MA-06 | T-MA-005、T-MA-010 |
| REQ-MA-006 | US-MA-006 | AC-MA-07 | T-MA-005、T-MA-011 |
| REQ-MA-007 | US-MA-007 | AC-MA-08 | T-MA-003、T-MA-004、T-MA-005 |
| REQ-MA-008 | US-MA-008 | AC-MA-10 | T-MA-001、T-MA-002、T-MA-007 |
| REQ-MA-009 | US-MA-002、US-MA-006 | AC-MA-02、AC-MA-09 | T-MA-007 |
| REQ-MA-010 | US-MA-001、US-MA-003 | AC-MA-01、AC-MA-03 | T-MA-006、T-MA-007 |
| REQ-MA-011 | US-MA-004、US-MA-005、US-MA-006 | AC-MA-05、AC-MA-06、AC-MA-07 | T-MA-005、T-MA-009、T-MA-010、T-MA-011 |
| REQ-MA-012 | US-MA-007、US-MA-008 | AC-MA-08、AC-MA-10 | T-MA-002、T-MA-004 |
| REQ-MA-013 | US-MA-004、US-MA-008 | AC-MA-04、AC-MA-10 | T-MA-009、T-MA-012 |
| REQ-MA-014 | （范围边界） | AC-MA-11 | T-MA-012 |
| REQ-MA-015 | US-MA-007 | AC-MA-08 | T-MA-004、T-MA-014（延后） |

## 与其他切片的边界

- `metadata-api` **拥有：** Spring Boot 服务骨架、8 个初始实体的 PostgreSQL/Flyway schema，以及 spaces、batches、files、chunks、reviews 的 REST 读写端点。
- **保留（为空）：** `adapter/` seam，供 Phase 3 converter/parser/storage/vector/model adapter——本切片无引擎调用。
- **建表但不暴露端点：** `wiki_page`（→ `review-publish`）、`graph_node`/`graph_edge`（→ `knowledge-graph`）；Ask（→ `ask-rag`）。按 REQ-MA-014 记录。
- **不触碰：** 前端运行时代码——从 `frontend/src/data/atlasMock.ts` 切换到 API 为延后后续（T-MA-014）。
- **对齐而非重定义：** `docs/04-architecture/knowledge-space-data-model.md`、`docs/batch-processing-design.md` 及 `frontend/src/types.ts` 枚举值。

## 验证证据

按 `docs/00-context/slice-roadmap.md` 的 Phase 2 API 行（实现期已执行，T-MA-013）：

- `cd backend && mvn verify` —— **通过**（2026-07-03）。证据：8 个单元/静态测试 + 7 个集成测试通过；Testcontainers 2.0.3 启动 `postgres:16-alpine`；Flyway 在 PostgreSQL 16.14 上应用 `V1__init_schema.sql` 与 `V2__seed_mock_metadata.sql`；Spring 上下文启动期间 Hibernate `ddl-auto=validate` 通过。
- PostgreSQL 集成说明：集成测试直接使用 Testcontainers PostgreSQL，与 SDD 验证契约一致。测试库凭据仅为运行期测试容器值，未提交到源码。
- `git diff --check` —— **通过**。
- 新依赖 / 无外部网络扫描 —— **产品代码通过**。发现项仅限 `pom.xml` 中 Maven schema URL；无产品 HTTP client、引擎 import 或出站网络 adapter。
- secret / 私有路径 / 真实数据扫描 —— **清理后通过**。运行期测试 DB secret 在内存中生成；backend 源码与 seed migration 中未提交明文凭据、私有绝对路径或真实公司数据。

延后工作：

- T-MA-014 FE 切换仍不在本切片范围内，作为后续任务跟踪。
- `wiki_page`、`graph_node`、`graph_edge` 表已迁移并种子化，但本切片未映射 wiki/graph/ask 端点。

## 使用的子技能（生成轮）

`atlas-sdd-generate-all` 编排链：`req-to-user-story → user-story-to-spec → spec-to-architecture → architecture-to-design → design-to-tasks → review-doc-quality`。

## 引入的关键假设

- 切片 ID 缩写为 `MA`（沿用 `FU`/`KS` 约定）；ID 为 `REQ-MA-###`、`US-MA-###`、`T-MA-###`、`AC-MA-###`。
- 数据模型覆盖全部 8 个初始实体（REQ-PROD-071）以满足 schema 需求，而 **API 面**限于 space/batch/file/chunk/review 以保持可评审的单次实现。图谱/wiki/ask API 延后至各自切片。
- 启用 `POST` 写路径（space/batch/review）；file-item 写经由从预算 inventory metadata 创建批次到达（无字节/引擎）。
- 枚举值以与前端类型完全相同的字符串持久化，以实现无漂移前端切换。
- Phase 2 无认证（仅内部）；RBAC 延后至 Phase 4。

## 待确认问题

- 现在启用 `POST` 写 vs. 只读 + 仅种子。（采用默认：启用 space/batch/review 创建。）
- 前端在本切片切换 vs. 后续。（采用默认：后续——T-MA-014 延后。）
- 枚举持久化：PG 原生 enum 类型 vs. text + CHECK。（采用默认：text + CHECK。）
- 单一 `atlas` schema vs. 按域 schema。（采用默认：单一 schema。）
- **`review_status` 分叉（评审中已核实）：** 持久化 `review_status` 遵循 REQ-PROD-030（`REVIEW_REQUIRED|APPROVED|NEED_FIX|OCR_REQUIRED|PUBLISHED`），比 `frontend/src/types.ts:44` 的 `ReviewStatus`（`REVIEW_REQUIRED|APPROVED|REJECTED`）更宽。持久化集合具权威性；DTO 把前端 `REJECTED`→`NEED_FIX`；重新对齐前端类型延后至 T-MA-014。仅 `file_status` 与前端严格值等同。

## 延后翻译

无。每个产物都有 EN 与 `.zh-CN.md` 副本，REQ/US/T/AC ID 完全一致。
