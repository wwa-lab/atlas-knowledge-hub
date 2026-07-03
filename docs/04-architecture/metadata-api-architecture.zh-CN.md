# 架构：Metadata API

## 状态

草稿。Phase 2。切片 `metadata-api`。派生自 `docs/03-spec/metadata-api-spec.md`。

## 概述

metadata 服务是 Atlas 的**控制平面**：一个 Spring Boot 应用，在 PostgreSQL 中拥有产品 metadata 与状态，并通过内部 REST API 提供。它是把 Atlas 从纯前端 mock 推向持久化契约的第一个组件。它刻意**不拥有任何处理**——转换、解析、模型、向量、存储与搜索执行属于 Phase 3 引入的独立 adapter/worker 平面。

## 组件所有权与边界

```
┌──────────────────────────────────────────────────────────────┐
│ 前端 (Vue) —— 消费方                                          │
│  通过 HTTP 读写 metadata；不拥有持久化                         │
└───────────────▲──────────────────────────────────────────────┘
                │ REST（JSON 信封）
┌───────────────┴──────────────────────────────────────────────┐
│ metadata-api (Spring Boot) —— 本切片                          │
│                                                                │
│  controller/  轻薄 controller + 校验                          │
│  service/     application service（编排、映射）               │
│  domain/ repository/ enums/  实体 + 数据访问                   │
│  dto/ exception/ validation/  信封、处理器、校验器            │
│  db/        Flyway migration（schema + 种子）                 │
│  adapter/   保留 SEAM —— 本切片为空（Phase 3）               │
└───────────────▲──────────────────────────────────────────────┘
                │ JDBC（配置驱动 datasource）
┌───────────────┴──────────────────────────────────────────────┐
│ PostgreSQL —— schema `atlas`，Flyway 管理                     │
└──────────────────────────────────────────────────────────────┘
```

- **`controller/`** 拥有 HTTP：请求 DTO 校验、响应信封组装、委派给 service。controller 无业务逻辑（REQ-MA-008）。
- **`service/`** 拥有编排与实体↔DTO 映射。`MetricsCalculator` 在此由 file item 计算批次 metrics（派生，不存储）。依赖方向：`controller → service → repository → domain`。
- **`domain/` + `enums/`** 拥有实体、枚举集合（`FileStatus`、`ReviewStatus`、`SourceKind`、`SourceType`、`SpaceType`、`IndexStrategy`、`SpaceStatus`、`GraphNodeType`、`GraphEdgeType`），以及不变量（如生成内容绝不默认 `APPROVED`）。
- **`repository/`** 经 Spring Data JPA 拥有数据访问；产品逻辑依赖 repository 接口，而非 JDBC 细节。**`dto/` / `exception/` / `validation/`** 拥有信封、`GlobalExceptionHandler` 与 `@RelativePath` 校验器。
- **db/** 拥有带版本 Flyway migration。`ddl-auto=validate` —— 共享环境无运行时 schema 变更。
- **adapter/** 是保留的空包。本切片任何处都不引用 converter/parser/model/vector/storage 引擎（REQ-MA-013）。

## Adapter 边界（关键规则）

本切片**不硬编码任何引擎**。`adapter/` 包仅用于标记 Phase 3 将引入 converter/parser/storage/model/vector adapter 的 seam。这些 adapter 未来会**调入**本服务以记录 file/chunk metadata 与状态；它们绝不从本切片被调用，产品逻辑依赖面向产品的 adapter 接口而非具体引擎（`document-normalize`、`trinity-office`、MinerU、pgvector、Ollama、S3 等）。

## Datasource 中立

datasource 完全外部化（`spring.datasource.*` 经 env/profile）。数据库 URL、用户名、密码均非硬编码字面量，PostgreSQL 是默认但非唯一可能目标（REQ-MA-012）。测试针对临时实例运行（Testcontainers PostgreSQL 或内存 Postgres 兼容 profile），使契约/集成测试无需共享服务器。

## Trace、Confidence 与 Review 保留

source trace、confidence 与 review status 是 `file_item`、`source_chunk`、`wiki_page` 上的一等列，映射时绝不丢弃。应用层强制不变量：生成/低置信度内容持久化为 `REVIEW_REQUIRED`；只有显式 review 动作才把目标移至 `APPROVED`/`NEED_FIX`/`OCR_REQUIRED`（REQ-MA-011）。`review_record` 只追加——历史行绝不被改写或删除。

## 安全与数据安全约束

- 仅用户安全错误：全局异常处理器从响应剥离堆栈、SQL、secret、内部主机名与私有绝对路径；详情仅服务端记录（REQ-MA-009、REQ-PROD-077）。
- 边界处输入校验（DTO 上的 Bean Validation）；`source_path` 须相对且无穿越（REQ-MA-009）。
- 暂无 auth/RBAC —— Phase 2 中这是仅内部服务；RBAC 属 Phase 4 切片。须明确声明，不得暗示已生产安全。
- 仅 mock/示例种子；无真实公司数据、凭证或私有路径（REQ-MA-012）。

## 与其他切片的范围边界

- **knowledge-space / folder-upload（Phase 1 FE）：** 定义本服务持久化的 mock 形态。枚举与字段名须与 `frontend/src/types.ts` 及 `docs/batch-processing-design.md` 对齐，而非分叉。
- **converter/parser/storage/vector/model-adapter（Phase 3）：** 拥有引擎执行；本切片只保留 seam。
- **review-publish / knowledge-graph / ask-rag（Phase 4）：** 拥有发布状态机、图谱查询与 Ask。本切片创建 `wiki_page`、`graph_node`、`graph_edge` 表（REQ-PROD-071）但不为其暴露端点（REQ-MA-014）。

## 技术决策

- Java + Spring Boot（Spring Web、Spring Data JPA、Bean Validation）。
- PostgreSQL + Flyway；`ddl-auto=validate`。
- 构建/验证：Maven（`mvn verify`）；集成/契约测试用 Testcontainers（或等价物）。
- 遵循 `PROJECT_RULES.md` "技术决策" 与 `DEVELOPMENT_STANDARDS.md` 后端/数据库标准；除 JDBC datasource 外无新增外部网络依赖。
