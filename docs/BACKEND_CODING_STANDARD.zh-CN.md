# 后端开发标准

Atlas Knowledge Hub Phase 2+ 后端开发标准，面向 Java + Spring Boot + PostgreSQL + Flyway。English: [BACKEND_CODING_STANDARD.md](BACKEND_CODING_STANDARD.md)。

本文档扩展 [DEVELOPMENT_STANDARDS.md](../DEVELOPMENT_STANDARDS.md) §§ Backend / API / Database / Adapter Standards，补充 Spring Boot 特有的具体模式与示例。它是 [docs/FRONTEND_CODING_STANDARD.md](FRONTEND_CODING_STANDARD.md) 的后端对应物。当某切片设计（如 `docs/05-design/metadata-api-design.md`）更具体时，该切片设计对该切片优先。

### 参考指南（已交叉核对）

本文规则对标并注明来源：[Google Java 风格指南](https://google.github.io/styleguide/javaguide.html)、[Spring](https://spring.io/guides) / [Spring Boot](https://docs.spring.io/spring-boot/reference/) 官方指南、[阿里巴巴 Java 开发手册（P3C）](https://github.com/alibaba/p3c)。当这些指南互相冲突或不符合 Atlas 的 mock-first、PostgreSQL、小团队画像时，偏离在下方 **有意偏离** 中明确说明——review 时不要"纠正"已记录的偏离。

### 相对参考指南的有意偏离

- **时间戳：** 列用 `created_at` / `updated_at`，**而非**阿里巴巴的 `gmt_create` / `gmt_modified`。理由：对齐前端领域模型（`frontend/src/types.ts`）与更通用的业界约定。
- **不强制 interface + `Impl`：** 阿里巴巴要求 `*Service` 接口 + `*ServiceImpl`。我们用单个具体 `*Service` 类，除非真的存在第二个实现——为单实现强加接口违反全局"避免臆测性抽象"规则（`~/.claude/rules` 编码风格）。第二实现出现时再引入接口。
- **成功信封不带 `timestamp`/`status`/`path`：** Spring 的 `{timestamp,status,error,message,path}` 是其*默认错误*体，不是成功包装。我们保持成功响应精简（`{success,data,error,meta}`），改为在 `ErrorBody` 上补 `timestamp` + `path`（见 API 响应信封）。

## 范围与阶段纪律

- 切片达到 **Phase 2**（后端 metadata API 与持久化）或之后才适用。在切片的接受 gate（数据模型 + API guide 被接受——REQ-PROD-073）之前，不搭建 Spring Boot、数据库或 migration。
- 后端是 **metadata 控制平面**。它拥有产品 metadata 与状态，不做处理。Converter/parser/model/vector/storage 执行属于 Phase 3 adapter 平面，必须留在 adapter 接口之后。

## 文件与包组织

**按层分包** —— 常规 Spring Boot 结构。按技术角色分组；保持 API/controller、application/service、repository、domain/实体、migration 职责分离（`DEVELOPMENT_STANDARDS.md` § Backend）。

```
backend/
├── pom.xml
├── src/main/java/com/atlas/metadata/
│   ├── MetadataApiApplication.java
│   ├── controller/             # 轻薄 controller —— 每资源一个
│   │   ├── SpaceController.java   BatchController.java
│   │   └── FileController.java    ReviewController.java
│   ├── service/                # application service：编排、映射、派生值
│   │   ├── SpaceService.java   BatchService.java  FileService.java  ReviewService.java
│   │   └── MetricsCalculator.java  # 由 file item 派生批次 metrics
│   ├── repository/             # Spring Data JPA repository
│   │   ├── SpaceRepository.java   BatchRepository.java  FileItemRepository.java
│   │   └── SourceChunkRepository.java  ReviewRecordRepository.java
│   ├── domain/                 # JPA 实体 + 不变量
│   │   ├── Space.java  Batch.java  FileItem.java  SourceChunk.java
│   │   └── ReviewRecord.java  WikiPage.java  GraphNode.java  GraphEdge.java
│   ├── enums/                  # FileStatus, ReviewStatus, ReviewAction, SourceKind, SourceType,
│   │                           #   SpaceType, IndexStrategy, SpaceStatus, GraphNodeType, GraphEdgeType
│   ├── dto/                    # *Request / *Response record, ApiEnvelope, ErrorBody, PageMeta
│   │   └── mapping/            # 实体 <-> DTO 映射
│   ├── exception/              # GlobalExceptionHandler, NotFoundException, ConflictException
│   ├── validation/             # @RelativePath 校验器
│   └── adapter/                # 保留 SEAM —— Phase 3 前为空（仅 package-info.java）
├── src/main/resources/
│   ├── application.yml         # 配置驱动；无字面量 secret
│   └── db/migration/           # Flyway V<n>__<desc>.sql
└── src/test/java/com/atlas/metadata/
    ├── controller/             # 契约测试（@WebMvcTest）
    ├── service/                # 单元测试（service、mapper、MetricsCalculator、校验器）
    └── integration/            # Testcontainers PostgreSQL：repository + migration 校验
```

- **按层分包，每包一种角色。** controller 在 `controller/`，service 在 `service/`，实体在 `domain/`，以此类推。不要把 controller 与 repository 混在同一包。
- **跨切面代码按种类归位：** 响应信封 + DTO 在 `dto/`，异常处理器与自定义异常在 `exception/`，校验器在 `validation/`，枚举在 `enums/`。
- **文件聚焦：** 通常 200–400 行，上限 800；方法 <50 行；嵌套 <4 层（全局编码风格）。

## 分层规则（强制）

依赖向内：`controller → service → repository → domain`。绝不反向。

| 层 | 可依赖 | 禁止 |
|---|---|---|
| `controller/` | `service/`、`dto/` | 直接碰 repository/实体；持有业务逻辑；带 `@Transactional` |
| `service/` | `domain/`、`repository/`、`dto` 映射、其他 service | 构建 HTTP 响应；知晓 `HttpServletRequest` |
| `domain/`（实体） | `enums/` | import Spring Web、JPA 查询逻辑或 DTO |
| `repository/` | `domain/` | 包含编排或映射 |
| `dto/` `exception/` `validation/` | 仅框架 + `domain`/`enums` | 持有业务逻辑 |
| `adapter/` | 仅面向产品的接口 | 引用具体引擎或开外联网络客户端（Phase 2：为空） |

- **Controller 轻薄：** 解析 → `@Valid` → 委派给 service → 包进信封。无 `@Transactional`，无查询。
- **Service 拥有事务与映射：** `@Transactional` 在此；派生值（如批次 metrics）在此产生，绝不存储。不滥用 `@Transactional`（阿里 P3C）——只标注跨 ≥2 语句变更的具体 service 方法；只读读取用 `@Transactional(readOnly = true)` 或不用。类级全包事务损害吞吐并隐藏边界。
- **绝不把 JPA 实体**经 HTTP 序列化——始终映射为 DTO。

## 命名

- 包：小写、单数角色名（`controller`、`service`、`repository`、`domain`、`dto`、`enums`、`exception`、`validation`、`adapter`）。
- 类：`PascalCase`；按角色加后缀——`*Controller`、`*Service`、`*Repository`、`*Request`、`*Response`、`*Mapper`。
- 枚举及其值**对 `FileStatus` 与 `frontend/src/types.ts` 完全一致**（相同字符串）。`ReviewStatus` 有意遵循更宽的 REQ-PROD-030 集合、与前端类型分叉——不得收敛（见 `metadata-api-data-model.md`）。
- 方法：动词短语（`createSpace`、`findByBatchIdAndStatus`、`computeMetrics`）。
- 常量：`UPPER_SNAKE_CASE`；不用编码产品行为的魔法数字/字符串。
- **Boolean 命名（阿里 P3C，强制）：** Java boolean 字段/record 组件**不得**以 `is` 开头（部分序列化器如 Jackson 会去掉 `is` 与 getter 不匹配）——用 `enabled`、`default`、`multimodalSupported`。对应 DB 列*可*用 `is_xxx` 形式（如 `is_default`）；mapper 桥接两个名字。
- 字段无特殊前后缀（`m`、`s_`、`_name`）——Google Java 风格禁止。

## 格式与 import

- **行长度：** 100 字符（Google Java 风格）。续行缩进 +4 空格；注释中长 URL 例外。
- **缩进：** 每级 2 空格（无制表符），UTF-8，K&R 花括号。
- **禁止通配符 import**（`import java.util.*;`）——始终显式导入（Google Java 风格；也避免 review 歧义）。
- **`@Override` 强制**用于每个覆盖接口/父类的方法（阿里 P3C）。
- **静态成员**按类名引用，而非通过实例（阿里 P3C）。
- 格式由 CI 中的 Spotless/`google-java-format` 强制，不手工做。

## 文档（Javadoc）

- **每个公开 class、method、record 组件有 Javadoc**（Google Java 风格底线）。自明的 getter/setter 可省。
- Javadoc 以摘要片段（名词/动词短语）开头，再为非平凡/公开方法记录参数、返回、抛出异常。
- 注释解释 **why 而非 what**（全局编码风格）。逻辑变更时更新注释；绝不留过时的注释掉的代码。
- 类级 `@author`/日期标签对本小团队为可选（有意放松阿里的强制）——作者信息以 Git 历史为准。

## DTO、实体与映射

- **请求/响应 DTO 是 Java `record`**，带 Bean Validation 注解。实体是 JPA `@Entity` 类。二者是不同类型——绝不跨边界共用一个类。
- 映射放 `dto/mapping/`（如 `SpaceMapper`）。mapper 保持纯净并做单元测试。
- 暴露 `camelCase` JSON；持久化 `snake_case` 列。mapper 桥接大小写。
- **Typed query 对象（阿里 P3C）：** 任何 2+ 过滤条件的读取用 typed `*Query` record——绝不用松散类型的 `Map<String,Object>`。如文件列表过滤变为 `record FileQuery(String batchId, FileStatus status, int page, int size)`。

```java
// web/dto/CreateSpaceRequest.java
public record CreateSpaceRequest(
    @NotBlank @Size(max = 200) String name,
    String description,
    @NotNull SpaceType type,
    @NotNull IndexStrategy indexStrategy,
    String owner
) {}
```

## API 响应信封（强制）

每个端点——成功与错误——返回同一信封（`docs/03-spec/metadata-api-spec.md`、API guide）：

```java
public record ApiEnvelope<T>(boolean success, T data, ErrorBody error, PageMeta meta) {
  public static <T> ApiEnvelope<T> ok(T data)            { return new ApiEnvelope<>(true, data, null, null); }
  public static <T> ApiEnvelope<T> ok(T data, PageMeta m){ return new ApiEnvelope<>(true, data, null, m); }
  public static <T> ApiEnvelope<T> fail(ErrorBody e)     { return new ApiEnvelope<>(false, null, e, null); }
}

public record ErrorBody(
    String code,                 // 机器可读，如 VALIDATION_FAILED
    String message,              // 用户安全文本
    Map<String, String> fields,  // 校验错误的字段级详情，否则 null
    long timestamp,              // epoch 毫秒 —— 对齐 Spring 默认错误体
    String path,                 // 请求路径，如 /api/spaces/xyz
    String correlationId,        // 需要服务端日志引用时出现
    Integer retryAfterSeconds    // RATE_LIMITED 时出现，否则 null
) {}
public record PageMeta(int page, int size, long total) {}
```

- `error.code` ∈ `AUTHENTICATION_REQUIRED`（401）· `PERMISSION_DENIED`（403）· `VALIDATION_FAILED`（400）· `NOT_FOUND`（404）· `CONFLICT`（409）· `RATE_LIMITED`（429）· `SAFE_SYSTEM_ERROR`（500）。
- **`timestamp` 与 `path` 仅在错误响应出现**（镜像 Spring Boot 的 `DefaultErrorAttributes`），给客户端与可观测工具审计上下文。成功响应保持精简——无 timestamp/status/path。HTTP 状态行已带状态码，故成功信封不重复。
- 列表端点始终含 `meta`；`total` 为完整过滤后计数。
- 分页经 `?page=&size=`（0 起；默认 20；上限 200）。过滤经文档化的查询参数。

## 校验（在边界）

- 用 Bean Validation 校验每个入站 DTO（controller 参数上 `@Valid`）。非法输入绝不到达持久化。
- 自定义 `@RelativePath` 校验器拒绝绝对路径、盘符/主机前缀与 `..` 穿越——`source_path` 或产物路径绝不为绝对（REQ-MA-009，数据安全）。
- 快速失败；返回字段级、用户安全的消息。

## 错误处理与安全

- 单一 `@RestControllerAdvice`（`GlobalExceptionHandler`）把异常映射为信封：

```java
@RestControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiEnvelope<Void>> onValidation(MethodArgumentNotValidException ex) {
    Map<String,String> fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(toMap(FieldError::getField, FieldError::getDefaultMessage, (a,b) -> a));
    return ResponseEntity.badRequest()
        .body(ApiEnvelope.fail(new ErrorBody("VALIDATION_FAILED", "Invalid request.", fields)));
  }
  // NotFoundException -> 404; ConflictException -> 409;
  // Exception -> 500 SAFE_SYSTEM_ERROR，通用消息 + correlation id
}
```

**异常纪律（阿里 P3C）：**
- 抛专门异常（`NotFoundException`、`ConflictException`、`ValidationException`）——处理器映射为 code。不抛裸 `RuntimeException`。
- 绝不吞异常：处理它，或带上下文重抛。无空 `catch` 块。
- 不用宽泛 `catch RuntimeException`/`Exception` 去掩盖本应由前置检查（null/边界/状态）避免的 bug。
- 优先 `Optional<T>` 而非返回 `null`；可能返回 null 的方法在 Javadoc 注明。
- 绝不从 `finally` 块 `return`；可关闭资源用 try-with-resources。

- **仅用户安全错误。** 响应绝不含堆栈、SQL、secret、内部主机名、源码片段或私有绝对路径。`SAFE_SYSTEM_ERROR` 返回通用消息；真实原因带 correlation id 记录于服务端（REQ-PROD-077）。`RATE_LIMITED` 只包含 retry metadata，不暴露 bucket 内部细节。
- **源码无 secret。** datasource URL/用户名/密码来自外部化配置（`${ATLAS_DB_URL}` 等）。`application.yml`、代码、日志、种子数据中无字面量凭证。任何未来 key 字段为 status-only（`configured` / `not_configured`），绝不原始。
- **Phase 2 无 auth**——服务仅内部；RBAC 属 Phase 4。在 `SECURITY.md`/类注释声明；不得按现状公开部署。
- **auth 到来时（Phase 4）：** 认证/授权失败由自定义 `AuthenticationEntryPoint` / `AccessDeniedHandler` 处理（返回同一信封，`401`/`403`），**与**业务 `GlobalExceptionHandler` **分离**——这是 Spring Security 对 CSRF、Bearer token、OAuth2 流程的约定。不要把 auth 失败并入校验/业务异常路径。

## Trace、Confidence 与 Review 不变量

- `source_path`、`confidence`、`review_status` 在每条持久化与返回的 file/chunk 记录上保留。
- 生成/低置信度内容持久化为 `REVIEW_REQUIRED`；只有显式 review 动作推进。**绝不**默认 `APPROVED`。`PUBLISHED` 只由后续发布切片设置。
- `review_record` 只追加——历史行绝不更新或删除。
- 派生值（批次 metrics）由持久化行计算，绝不作为可漂移计数器存储。

## 持久化与 Flyway

- **Flyway 拥有 schema。** 同时配置 Hibernate 守卫与 Flyway 校验（Spring Boot 生产指引）：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate        # Hibernate 绝不变更 schema
  flyway:
    locations: classpath:db/migration
    validate-on-migrate: true   # migration 校验和漂移则快速失败
    baseline-on-migrate: true   # 对已存在数据库安全
    out-of-order: false         # migration 严格按版本顺序应用
```
- migration 命名：`V<n>__<snake_case_description>.sql`（如 `V1__init_schema.sql`、`V2__seed_mock_metadata.sql`）。可重复 migration 用 `R__`。
- **migration 一旦合入即不可变。** 已发布的 migration 绝不编辑；新改动是新版本。
- 种子数据**仅 mock/示例**——无真实公司内容、凭证或私有路径；仅相对路径。
- 为被接受的查询路径有意加索引（如 `(batch_id, status)`）；说明原因。
- datasource 配置驱动；PostgreSQL 是默认目标，非硬编码唯一实现。

**Schema 约定（阿里 P3C，适配 PostgreSQL）：**
- 表名与列名为 `lower_snake_case`；表名**单数**（`space`、`file_item`），绝不复数。
- 避免用 SQL 保留字作标识符（`desc`、`order`、`user`、`range`）。
- 索引命名：主键 `pk_<table>`、唯一 `uk_<table>_<cols>`、普通 `idx_<table>_<cols>`。
- confidence 与类金额值用精确 `numeric`/`decimal`——绝不 `float`/`double`。
- **偏离（上文已注）：** 时间戳用 `created_at`/`updated_at`，而非 `gmt_create`/`gmt_modified`。
- 查询选择显式列/投影——绝不 `SELECT *`；所有参数绑定（JPA/JPQL 参数），绝不字符串拼接（防 SQL 注入）。

## Adapter 边界（关键）

- 产品逻辑不得直接调用 parser/converter/model/vector/storage 引擎。`adapter/` 包在 Phase 2 是**保留的空 seam**，仅含 `package-info.java`。
- 一个守卫测试断言 `adapter/` 不引用任何引擎、不接入外联网络客户端，使意外耦合在 CI 失败。
- Phase 3 加 adapter 时，它们暴露 Atlas 产品概念（capability metadata、config 形态、成功/失败输出、重试行为）——绝不供应商特定细节——并**调入** metadata 服务，而非反向。

## 测试（映射 `mvn verify`）

三层，随层变为真实而全部必需（`DEVELOPMENT_STANDARDS.md` § Testing；全局 80% 底线）：

- **单元（`service/`）：** service、mapper、`MetricsCalculator` 派生、action→status 映射、`REVIEW_REQUIRED` 默认不变量、`@RelativePath` 校验器。快速，尽量不启 Spring 上下文。
- **契约（`controller/`）：** `@WebMvcTest(XController.class)` + mock service（`@MockitoBean`）——快、仅 web 层。断言状态码、信封形态、分页 `meta`、`400` 字段级校验、`404`、用户安全错误体（无堆栈/SQL/secret/绝对路径）。
- **集成（`integration/`）：** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers PostgreSQL——端到端真实 bean：repository CRUD + 分页 + 过滤、Flyway `V1`+`V2` 干净应用、`ddl-auto=validate` 通过、种子行匹配前端基线预期。集成测试命名 `*IT`。

选择能证明行为的最窄层：controller 逻辑用 `@WebMvcTest`，repository 查询用 `@DataJpaTest`，仅当全链路接线重要时用 `@SpringBootTest`。用 **AssertJ** fluent 断言（`assertThat(...)`）提升可读性。
- **Adapter-seam 守卫：** 断言 `adapter/` 无引擎/网络依赖。

```java
// integration/MigrationValidationIT.java
@SpringBootTest
@Testcontainers
class MigrationValidationIT {
  @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");
  @DynamicPropertySource static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", pg::getJdbcUrl);
    r.add("spring.datasource.username", pg::getUsername);
    r.add("spring.datasource.password", pg::getPassword);
  }
  @Test void flywayMigratesAndSchemaValidates() { /* 上下文加载 == migrate + validate 通过 */ }
}
```

> 注：Testcontainers 在测试时拉取 Postgres 镜像（CI 中有网络）。这是有意的、阶段合适的依赖——区别于 Phase 0/1 的"无外部网络"规则，后者管的是原型/前端运行时，而非后端测试框架。

## 代码质量检查清单（提交前）

- [ ] **分层**：`controller → service → repository → domain`；每包一种角色；controller 轻薄；无实体经 HTTP 序列化。
- [ ] **信封**：每个端点返回 `ApiEnvelope`；列表含 `PageMeta`。
- [ ] **校验**：所有 DTO `@Valid`；路径相对 + 穿越检查；非法输入 → 400 含 fields。
- [ ] **错误/secret**：响应或日志无堆栈/SQL/secret/主机名/绝对路径；datasource 来自配置，无字面量。
- [ ] **Trace/review**：`source_path`+`confidence`+`review_status` 保留；生成内容 `REVIEW_REQUIRED`，绝不 `APPROVED`；`review_record` 只追加。
- [ ] **枚举**：`FileStatus` 与前端 + `batch-processing-design.md` 一致；`ReviewStatus` 遵循 REQ-PROD-030（已记录分叉）。
- [ ] **Flyway**：带版本、不可变 migration；`ddl-auto=validate`；种子仅 mock、相对路径。
- [ ] **Adapter seam**：`adapter/` 为空；无引擎调用；守卫测试存在。
- [ ] **测试**：改动行为有单元 + 契约 + 集成；修改代码覆盖率 ≥80%。
- [ ] **大小/命名**：文件 <800 行、方法 <50 行、嵌套 <4；类名带角色后缀。
- [ ] **Boolean/import/格式**：无 boolean Java 字段以 `is` 开头；无通配符 import；行 ≤100 字符；所有覆盖加 `@Override`。
- [ ] **Javadoc**：每个公开 class/method/record 组件有文档；注释解释 why。
- [ ] **错误体**：错误响应带 `code`、用户安全 `message`、`fields`（校验时）、`timestamp`、`path`；成功响应保持精简。
- [ ] **异常**：专门异常、不吞、`Optional` 替 null、`finally` 不 `return`。
- [ ] **DB 约定**：单数 snake_case 表、`pk_/uk_/idx_` 索引名、`numeric` 非 float、显式列（无 `SELECT *`）、参数绑定。
- [ ] **事务**：`@Transactional` 限于需要的方法，非类级全包。

## 构建、验证与 CI

```bash
cd backend
mvn -q compile          # 快速编译检查
mvn verify              # 编译 + Flyway 校验 + 单元 + 契约 + 集成
mvn -q flyway:migrate   # 可选：对 local/test datasource 显式迁移
git diff --check        # 空白/冲突卫生
```

推荐的 `.github/workflows/backend-ci.yml`（按 `DEVELOPMENT_STANDARDS.md` § CI Direction 生长）：

1. 格式 / diff 卫生（Spotless 或等价物）+ `git diff --check`。
2. `mvn -q compile`。
3. `mvn verify`（单元 + 契约 + 集成经 Testcontainers）带覆盖率报告（JaCoCo，修改代码 ≥80%）。
4. Flyway migration 校验。
5. Secret / 依赖扫描（无原始凭证，除 JDBC datasource 外无新增外部运行时依赖）。

## 示例与参考

- `docs/05-design/metadata-api-design.md` —— 模块布局、角色契约、DTO/信封设计。
- `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` —— 端点请求/响应契约。
- `docs/04-architecture/metadata-api-data-model.md` —— 持久化 schema、枚举、不变量。
- `docs/03-spec/metadata-api-spec.md` —— 行为真实来源（信封、错误、状态模型）。
- `DEVELOPMENT_STANDARDS.md` §§ Backend / API / Database / Adapter / Testing / CI —— 本文所具体化的治理标准。
