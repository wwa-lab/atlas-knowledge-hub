# 设计：Metadata API

## 状态

草稿。Phase 2。切片 `metadata-api`。派生自 `docs/04-architecture/metadata-api-architecture.md` 与数据模型。这是后端服务设计——无 UI。前端为消费方；线上契约见 API 实现指南。

## 模块布局（Maven / Spring Boot）

按功能分包（见 `docs/BACKEND_CODING_STANDARD.md` § 文件与包组织）：每个能力是自包含的包；跨切面代码放 `common/`。

```
backend/                         # 本切片从占位提升为可构建
  pom.xml
  src/main/java/com/atlas/metadata/
    MetadataApiApplication.java
    space/                       # Knowledge Space 功能
      SpaceController.java  SpaceService.java  Space.java  SpaceRepository.java
      SpaceMapper.java  SpaceType.java  IndexStrategy.java  SpaceStatus.java  dto/
    batch/                       # Batch 功能
      BatchController.java  BatchService.java  MetricsCalculator.java   # 派生 metrics
      Batch.java  BatchRepository.java  BatchMapper.java  dto/
    file/                        # File item + source chunk 功能
      FileController.java  FileService.java  FileItem.java  SourceChunk.java
      FileItemRepository.java  SourceChunkRepository.java  FileMapper.java  dto/
    review/                      # Review record 功能
      ReviewController.java  ReviewService.java  ReviewRecord.java
      ReviewRecordRepository.java  ReviewAction.java  dto/
    wiki/                        # 仅实体 + repository（本切片无端点）
      WikiPage.java  WikiPageRepository.java
    graph/                       # 仅实体（本切片无端点）
      GraphNode.java  GraphEdge.java  GraphNodeType.java  GraphEdgeType.java
    common/                      # 跨切面，2+ 功能共享
      web/ (ApiEnvelope、ErrorBody、PageMeta、GlobalExceptionHandler)
      validation/ (@RelativePath 校验器)
      enums/ (FileStatus、ReviewStatus、SourceKind、SourceType)   # 跨功能
    adapter/                     # 保留、为空（Phase 3 seam）—— 仅 package-info.java
  src/main/resources/
    application.yml              # 配置驱动 datasource（无字面量）
    db/migration/
      V1__init_schema.sql
      V2__seed_mock_metadata.sql
  src/test/java/com/atlas/metadata/
    space/ batch/ file/ review/  # 测试镜像功能包
    integration/                 # Testcontainers PostgreSQL：跨功能 + migration 校验
```

既有 `backend/README.md` 占位被取代；按 Phase 2 纪律，本切片是 `backend/` 变为可构建之处。

## 角色契约（每个功能内）

按功能分包保持角色依赖方向不变：controller → service → 实体，service → repository。跨功能访问只走 service→service（绝不进入另一功能的 repository/实体）。

### Controller（`*Controller`）
- 每功能一个 controller；轻薄——解析、校验（`@Valid`）、委派给功能 service、包进 `common/web` 信封。
- 绝不直接序列化 JPA 实体；controller 只返回 DTO。
- `common/web/GlobalExceptionHandler`（`@RestControllerAdvice`）把异常映射为 `{400,404,409,500}` 用户安全信封。
- 分页经 `page`/`size` 查询参数（有界默认 size，如 20；上限 200）。

### Service（`*Service`）
- 拥有事务（`@Transactional`）、编排、实体↔DTO 映射（经功能 `*Mapper`）。
- `batch/MetricsCalculator` 由批次的 file item 计算 `BatchMetrics`——metrics 的**唯一**产生处（无存储计数器）。它经 `FileService` 读取文件状态，而非直接用 `file/` 的 repository。
- 强制不变量：默认 `review_status=REVIEW_REQUIRED`；只有 `ReviewService` 经动作推进；创建时绝不设 `APPROVED`。

### 实体 + 枚举
- JPA 实体（与其功能包共置）精确镜像数据模型表。`FileStatus` 值与 `frontend/src/types.ts` 完全相同字符串；`ReviewStatus` 有意遵循更宽的 REQ-PROD-030 集合，**不**匹配更窄的前端 `ReviewStatus`（见数据模型"与前端类型的关系"）。不得将其收敛为前端类型。
- 实体持有不变量辅助（如 `FileStatus.isGenerated()`），不涉及 DB 查询或 HTTP。跨功能枚举放 `common/enums`。

### Repository（`*Repository`）
- 功能包内的 Spring Data JPA 接口；分页/过滤读取的自定义查询（`findByBatchIdAndStatus`、`findBySpaceId`、时间顺序 `findByTargetTypeAndTargetIdOrderByCreatedAt`）。

### adapter/（保留）
- `package-info.java` 记录 seam；**无任何类引用任何引擎**。以测试断言其为空，防未来意外耦合。

## DTO 与信封设计

- `ApiEnvelope<T>` record：`{ success, data, error, meta }`。
- `ErrorBody`：`{ code, message, fields?, timestamp, path }`。`code` ∈ {`VALIDATION_ERROR`,`NOT_FOUND`,`CONFLICT`,`INTERNAL_ERROR`}。`timestamp`（epoch 毫秒）+ `path` 仅在错误响应出现（镜像 Spring `DefaultErrorAttributes`）；成功响应保持精简。见 `docs/BACKEND_CODING_STANDARD.md` § API Response Envelope。
- `PageMeta`：`{ page, size, total }`，仅列表响应出现。
- 请求 DTO 为带 Bean Validation 注解的 record（`@NotBlank`、`@Pattern`、`@DecimalMin/@DecimalMax`、自定义 `@RelativePath`）。
- `@RelativePath` 校验器拒绝绝对路径、盘符/主机前缀与 `..` 穿越。

## 配置设计

- `application.yml` 从环境/profile 占位符读取 `spring.datasource.url|username|password`（`${ATLAS_DB_URL}` 等）——无字面量、无提交 secret。
- `spring.jpa.hibernate.ddl-auto: validate`；启用 Flyway；`spring.flyway.locations: classpath:db/migration`。
- `local` profile 可指向开发者 Postgres；`test` profile 用 Testcontainers——均不提交凭证。

## 错误与安全设计

- 异常→信封映射表（见规格错误行为）。`INTERNAL_ERROR` 返回通用消息；真实原因带关联 id 记录，绝不返回。
- 任何响应、日志或种子不含 secret、凭证、私有端点、内部主机名或绝对路径。
- 本切片无认证——记为仅内部；`SECURITY.md` 说明或类注释声明 RBAC 延后至 Phase 4，服务不得按现状公开暴露。

## 测试设计（映射 `mvn verify`）

- **单元（app/）：** `MetricsCalculator` 派生；review-action→status 映射；`REVIEW_REQUIRED` 默认不变量；mapper；`@RelativePath` 校验器。
- **契约（web/）：** 每端点 MockMvc/WebTestClient，断言状态码、信封形态、分页 meta、校验 400、404 与用户安全错误体。
- **集成：** Testcontainers PostgreSQL —— repository CRUD、Flyway `V1`+`V2` 干净应用、`ddl-auto=validate` 通过、种子行匹配前端基线预期。
- **adapter-seam 守卫：** 测试断言 `adapter/` 包无引擎依赖、无外联网络客户端接入。

## 消费方（前端）集成说明

- 前端目前读取 `frontend/src/data/atlasMock.ts`。切换（把 mock provider 换成命中这些端点的 HTTP 客户端）是**独立任务**，已跟踪但此处不实现。信封 + 枚举值对齐的设计使切换无需视觉改动（REQ-MA-015）。

## 待确认问题

- 现在启用 `POST` 写 vs. 只读 + 种子（默认：启用 space/batch/review 创建）。
- 枚举以原生 PG enum 类型持久化 vs. text + CHECK（默认：text + CHECK 以利 migration 灵活性）。
