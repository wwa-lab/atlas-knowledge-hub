# 任务：Parser Adapter

## 状态

已于 2026-07-03 实现。T-PA-001 至 T-PA-010 已依据 `docs/03-spec/parser-adapter-spec.md` 执行，并通过 mock parser 验证；真实 parser runtime contract 仍作为后续延后项。

## 来源设计

- Spec：`docs/03-spec/parser-adapter-spec.md`
- Design：`docs/05-design/parser-adapter-design.md`
- API guide：`docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model：`docs/04-architecture/parser-adapter-data-model.md`

## 每条任务的共同约束

- 自动化测试使用 mock/fake parser engines。
- Parser execution 保持在产品侧 adapter contract 后。
- Controller/service/repository/domain layers 不得调用 `document-normalize`、OCR engines、command runners 或 outbound HTTP clients。
- 不引入外部网络依赖或云调用。
- 保留 source trace、confidence、review status。
- 脱敏 secrets、private paths、raw parser logs、hostnames、stack traces 与 private endpoints。
- 只使用 mock/sample metadata。

## 工作流

| Workstream | Tasks |
|---|---|
| Domain and persistence | T-PA-001, T-PA-002 |
| Adapter contract | T-PA-003, T-PA-004 |
| Service behavior | T-PA-005, T-PA-006, T-PA-007 |
| API contract | T-PA-008 |
| Verification and guards | T-PA-009, T-PA-010 |

## 任务详情

### T-PA-001：新增 parser run domain model 与 migration

- **映射到：** REQ-PA-004, REQ-PA-013, REQ-PA-014；spec sections "Parser Run", "Metadata Write-Back And Validation"；data model `parser_run`, `parser_file_result`。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** None
- **范围：** 新增 parser run status enum、parser run entity、parser file result entity、repositories 与 Flyway migration，用于 parser execution evidence。复用现有 `FileStatus`、`SourceType`、`ReviewStatus`；不得新增 file statuses。
- **约束：** Adapter boundary；mock-only test data；source trace/review preservation；secret/path safety。
- **验证：**

```bash
cd backend && mvn verify
git diff --check
```

### T-PA-002：新增 parser DTO 与 mapping contracts

- **映射到：** REQ-PA-003, REQ-PA-013；spec sections "Capability Metadata", "API / Interface Surface"；API guide response shapes。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** T-PA-001
- **范围：** 新增 parser capability、create-run request、run response、summary response、file result response、chunk response mapping。所有 responses 通过 controller 使用 `ApiEnvelope`。
- **约束：** Secret-masked capability summaries；无 raw runtime details。
- **验证：**

```bash
cd backend && mvn test
git diff --check
```

### T-PA-003：定义 parser adapter interface 与 capability model

- **映射到：** REQ-PA-001, REQ-PA-002, REQ-PA-003；spec sections "Adapter Boundary", "Capability Metadata"；design "Parser Adapter Contract"。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** T-PA-002
- **范围：** 新增产品侧 parser adapter interface、capability record、request record、result record、chunk result record。包含 adapter key、safe status、supported input/output types、default marker、low-confidence threshold、masked config summary。
- **约束：** 只暴露产品概念；interface 中无 direct parser engine call；无 network/client dependency。
- **验证：**

```bash
cd backend && mvn -Dtest=ParserAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/adapter
```

### T-PA-004：新增 mock 与 configured parser adapter implementations

- **映射到：** REQ-PA-002, REQ-PA-012；spec sections "Adapter Boundary", "Status Mapping And Failure Behavior"；design "Parser Adapter Contract"。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** T-PA-003
- **范围：** 新增用于 CI 的 deterministic mock parser adapter，以及只报告安全 capability metadata 的 configured `document-normalize` adapter boundary。真实 parser execution 保持在 adapter 后，runtime topology 决定前可为安全 placeholder。
- **约束：** Mock-only verification；无外部网络；capability response 中无 raw command/path/secret。
- **验证：**

```bash
cd backend && mvn -Dtest=ParserAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-PA-005：实现 parser adapter registry 与 capability listing

- **映射到：** REQ-PA-001, REQ-PA-003；spec sections "Adapter Boundary", "Capability Metadata"；API guide `GET /api/parser-adapters`。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** T-PA-003, T-PA-004
- **范围：** 新增 explicit/default parser adapter key 的 registry resolution，以及 unavailable/misconfigured statuses。新增 capability listing service behavior，并只返回脱敏配置。
- **约束：** Adapter boundary；secret masking；不把单一实现硬编码为唯一未来选项。
- **验证：**

```bash
cd backend && mvn -Dtest=ParserAdapterRegistryTest test
git diff --check
```

### T-PA-006：实现 parser run service 与 status mapping

- **映射到：** REQ-PA-004, REQ-PA-005, REQ-PA-006, REQ-PA-007, REQ-PA-008, REQ-PA-013, REQ-PA-014；spec sections "Parser Run", "Status Mapping And Failure Behavior"；design "Parser Service"。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** T-PA-001, T-PA-003, T-PA-005
- **范围：** 实现 parser run creation、target eligibility filtering、active-run conflict handling、adapter execution、`MARKDOWN_GENERATED` / `LOW_CONFIDENCE` / `OCR_REQUIRED` / `FAILED` mapping、summary calculation、safe adapter-fault handling。
- **约束：** 测试中使用 mock-engine execution；review status 保持 `REVIEW_REQUIRED`；不执行 OCR。
- **验证：**

```bash
cd backend && mvn -Dtest=ParserServiceTest,ParserSummaryCalculatorTest test
git diff --check
```

### T-PA-007：实现 metadata write-back 与 source chunk persistence

- **映射到：** REQ-PA-009, REQ-PA-010, REQ-PA-011, REQ-PA-014；spec sections "Markdown, Assets, And Chunks", "Metadata Write-Back And Validation"；design "Data Design"。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** T-PA-006
- **范围：** 使用安全 Markdown/assets paths、confidence、status、safe error 更新 file items。持久化已校验 source chunks。拒绝 unsafe paths、unknown file ids、cross-batch results、invalid pages、duplicate chunk ids、越界 confidence。
- **约束：** 保留 source trace、confidence、review status；secret/path masking；不创建 `wiki_page`。
- **验证：**

```bash
cd backend && mvn -Dtest=ParserDomainInvariantTest,ParserServiceTest test
git diff --check
```

### T-PA-008：新增 parser REST API endpoints

- **映射到：** REQ-PA-003, REQ-PA-004, REQ-PA-013；spec section "API / Interface Surface"；API guide all endpoints。
- **Owner type：** backend
- **Priority：** Must
- **Dependencies：** T-PA-005, T-PA-006, T-PA-007
- **范围：** 新增 parser controller endpoints：`GET /api/parser-adapters`、`POST /api/batches/{batchId}/parser-runs`、`GET /api/parser-runs/{runId}`。Responses 必须使用 `ApiEnvelope` 并匹配 API guide。
- **约束：** Internal-only；不实现 auth/RBAC；不返回 raw parser output。
- **验证：**

```bash
cd backend && mvn -Dit.test=ParserApiContractIT verify
git diff --check
```

### T-PA-009：扩展 adapter seam 与 safety guards

- **映射到：** REQ-PA-001, REQ-PA-008, REQ-PA-011, REQ-PA-012；spec sections "Adapter Boundary", "Metadata Write-Back And Validation"；design "Testing Considerations"。
- **Owner type：** QA/backend
- **Priority：** Must
- **Dependencies：** T-PA-003, T-PA-004, T-PA-008
- **范围：** 扩展 guard tests，使 parser engine names、command runners、outbound network clients 禁止出现在 non-adapter product layers。为 parser implementation 与 docs 增加 secret/private-path scans。
- **约束：** adapter 外无 direct parser calls；只允许 secret-masked output。
- **验证：**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

### T-PA-010：运行最终 parser-adapter verification

- **映射到：** REQ-PA-012, REQ-PA-013, REQ-PA-014；spec acceptance matrix AC-PA-01 到 AC-PA-09。
- **Owner type：** QA/backend
- **Priority：** Must
- **Dependencies：** T-PA-001 through T-PA-009
- **范围：** 运行完整验证集、review diff，并在实现后如文档/状态变化则更新 traceability evidence。
- **约束：** Mock-only；adapter only；product code 无外部网络/云调用；secret-masked；trace/review preserved。
- **验证：**

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

## Dependency Plan

- Critical path：T-PA-001 -> T-PA-002 -> T-PA-003 -> T-PA-004 -> T-PA-005 -> T-PA-006 -> T-PA-007 -> T-PA-008 -> T-PA-009 -> T-PA-010
- Parallel opportunities after T-PA-003：adapter contract tests 与 DTO mapper tests 可与 service tests 并行构建。

## 待确认问题 / 风险

- OQ-PA-001：真实 parser runtime topology 延后；mock contract 不得依赖它。
- OQ-PA-002：`wiki_page` 创建延后到 publish/review。
- OQ-PA-003：Low-confidence threshold 默认 `< 0.800`；已按 spec 实现，如产品后续希望其他阈值，可另行调整。
