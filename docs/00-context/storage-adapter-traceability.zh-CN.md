# 溯源：Storage Adapter

## 状态

已实现。由 `atlas-sdd-generate-all` 工作流为切片 `storage-adapter` 生成，随后作为 Phase 3 adapter 切片按 `docs/03-spec/storage-adapter-spec.md` 与 `docs/06-tasks/storage-adapter-tasks.md` 实现。

## 切片契约

| 字段 | 值 |
|---|---|
| 目标 | Atlas 可以通过可替换 storage adapter，存储和读取按工作区分层的产物（raw、PDF、Markdown、assets、reports、Wiki），且产品层不耦合 S3/MinIO。 |
| 切片 | `storage-adapter` |
| 阶段 | 3 adapter |
| 范围 | Storage adapter 契约、能力、store/get/list/delete/exists 操作、工作区分层策略、descriptor、storage operation record、metadata 指针写回、mock-engine 验证、API guide、tasks。 |
| 排除项 | 真实存储运行、presigned URL、保留/生命周期策略、converter/parser 执行、OCR/LLM、图谱、Ask/RAG、发布到 Wiki 语义、vector/model adapter、前端 UI、生产认证/RBAC。 |
| 验证行 | 针对 mock engines 的单元测试 + 集成测试。 |
| 约束行 | Parser/converter/model/vector/storage 只走产品侧 adapter；无直连工具；无硬编码单一实现；secret 脱敏；trace/confidence/review 保留。 |

## 来源文档

| 来源 | 用途 |
|---|---|
| `README.md` | 产品 workflow 与技术栈方向。 |
| `PROJECT_RULES.md` | SDD、adapter、安全、数据、阶段纪律、Workspace Separation。 |
| `DEVELOPMENT_STANDARDS.md` | Phase 3 adapter 标准与验证。 |
| `docs/00-context/sdd-profile.md` | 必需 SDD 产物链与 ID 格式。 |
| `docs/00-context/slice-roadmap.md` | Phase 3 验证/约束与切片 backlog。 |
| `docs/01-requirements/requirement.md` | 产品级分层存储（REQ-PROD-014）、adapter（REQ-PROD-015/017）、引擎配置（REQ-PROD-060/061）与 secret 规则。 |
| `docs/architecture.md` | 基于 adapter 的架构与 worker plane。 |
| `docs/batch-processing-design.md` | 产物分层与报告。 |
| `docs/03-spec/converter-adapter-spec.md` | 同阶段 adapter 范式。 |
| `docs/03-spec/parser-adapter-spec.md` | 同阶段 adapter 范式。 |
| `docs/04-architecture/converter-adapter-data-model.md` | 同阶段 run/result 数据模型范式。 |
| 现有 backend metadata 代码 | grounding 当前实体、枚举、校验器、envelope 与 seam guard。 |

## 门禁说明

`docs/00-context/slice-roadmap.md` 已将 `metadata-api`、`converter-adapter` 与 `parser-adapter` 标记为已实现，因此 `storage-adapter` 的 Phase 2 前置条件对实现已满足。本轮将 storage 行更新为已实现，并继续推迟真实 storage runtime 接线。

## 实现证据

| 任务范围 | 证据 |
|---|---|
| T-SA-001 | 新增 storage 枚举、`StorageOperation`、`StorageObject`、repository，以及 Flyway migration `V5__storage_adapter.sql`。 |
| T-SA-002 | 新增 storage capability、create-operation、operation summary/response、object response、object-list DTO 与 `StorageMapper`。 |
| T-SA-003 | 新增产品侧 `StorageAdapter` 契约，以及 capability/request/ref/list/descriptor/result records。 |
| T-SA-004 | 新增确定性的 `MockInMemoryStorageAdapter` 与安全的 `ConfiguredS3CompatibleStorageAdapter` 边界，不执行真实网络调用。 |
| T-SA-005 | 新增 `StorageAdapterRegistry`，支持显式/default adapter 解析与脱敏 capability listing。 |
| T-SA-006 | 新增 `StorageService` operation 校验、active-run 冲突处理、namespace/key enforcement、adapter execution、descriptor validation、summary/status 计算与脱敏 fault handling。 |
| T-SA-007 | 持久化 `storage_object` descriptors，并通过 `FileItem.setArtifacts(...)` 写回 storage 指针，保留 status、confidence 与 review status。 |
| T-SA-008 | 新增 `StorageController` endpoints：capabilities、create/get storage operation、分页 descriptor listing。 |
| T-SA-009 | 更新 `AdapterSeamGuardTest`：adapter 范围允许 storage-engine 命名，同时在非 adapter 层禁止 storage SDK/client 引用，并加入 storage secret/private-path guard coverage。 |
| T-SA-010 | 已运行下方完整验证集，包括使用 mock/in-memory adapter execution 的 full Maven verification。 |

## 验证证据

| 检查 | 结果 |
|---|---|
| `cd backend && mvn test` | 通过；56 个 unit/static tests 通过，包括 storage service、contract、registry、summary、domain invariant 与 adapter seam guard tests。 |
| `git diff --check` | 通过。 |
| 非 adapter storage/client seam scan | 通过：controller/service/repository/domain 层无 `AmazonS3`、`software.amazon.awssdk`、`MinioClient`、`MinIO`、`S3Client`、`putObject`、`getObject`、`WebClient`、`RestTemplate` 或 `HttpClient`。 |
| T-SA-010 full secret/private-path scan | 通过。 |
| `cd backend && mvn verify` | 通过；56 个 Surefire tests 与 25 个 Failsafe integration tests 通过。 |

## SDD 产物

| 阶段 | 英文 | 简体中文 |
|---|---|---|
| Requirements | `docs/01-requirements/storage-adapter-requirements.md` | `docs/01-requirements/storage-adapter-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/storage-adapter-stories.md` | `docs/02-user-stories/storage-adapter-stories.zh-CN.md` |
| Spec | `docs/03-spec/storage-adapter-spec.md` | `docs/03-spec/storage-adapter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/storage-adapter-architecture.md` | `docs/04-architecture/storage-adapter-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/storage-adapter-data-flow.md` | `docs/04-architecture/storage-adapter-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/storage-adapter-data-model.md` | `docs/04-architecture/storage-adapter-data-model.zh-CN.md` |
| Design | `docs/05-design/storage-adapter-design.md` | `docs/05-design/storage-adapter-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/storage-adapter-tasks.md` | `docs/06-tasks/storage-adapter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/storage-adapter-traceability.md` | `docs/00-context/storage-adapter-traceability.zh-CN.md` |

## API Guide 决定

包含 API guide。Storage-adapter 是 Phase 3 后端/API + adapter contract 切片，因此实现前需要 `docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md`。

## 需求溯源

| 需求 | 故事 | Spec 章节 | 任务 |
|---|---|---|---|
| REQ-SA-001 | US-SA-001、US-SA-005 | Adapter Boundary | T-SA-003、T-SA-005、T-SA-009 |
| REQ-SA-002 | US-SA-001、US-SA-002 | Adapter Boundary、Capability Metadata | T-SA-003、T-SA-004 |
| REQ-SA-003 | US-SA-002 | Capability Metadata、API / Interface Surface | T-SA-002、T-SA-005、T-SA-008 |
| REQ-SA-004 | US-SA-001、US-SA-003 | Storage Operations、Layer And Namespace Policy | T-SA-001、T-SA-006、T-SA-008 |
| REQ-SA-005 | US-SA-001、US-SA-004 | Storage Operations、Metadata Write-Back And Descriptors | T-SA-006、T-SA-007 |
| REQ-SA-006 | US-SA-001 | Storage Operations | T-SA-006 |
| REQ-SA-007 | US-SA-003 | Storage Operations、Layer And Namespace Policy | T-SA-006、T-SA-008 |
| REQ-SA-008 | US-SA-005 | Storage Operations、Validation And Failure Behavior | T-SA-006 |
| REQ-SA-009 | US-SA-003、US-SA-005 | Layer And Namespace Policy、Validation And Failure Behavior | T-SA-006、T-SA-007、T-SA-009 |
| REQ-SA-010 | US-SA-005 | Validation And Failure Behavior | T-SA-007、T-SA-009 |
| REQ-SA-011 | US-SA-004 | Metadata Write-Back And Descriptors | T-SA-007 |
| REQ-SA-012 | US-SA-001、US-SA-005 | Constraints、Acceptance Matrix | T-SA-004、T-SA-009、T-SA-010 |
| REQ-SA-013 | US-SA-004 | Storage Operations、Reporting | T-SA-001、T-SA-002、T-SA-006、T-SA-008、T-SA-010 |
| REQ-SA-014 | US-SA-004 | Metadata Write-Back And Descriptors | T-SA-001、T-SA-007、T-SA-010 |

## Grounding 证据

| 主张 | 证据 |
|---|---|
| 现有 file item 暴露 PDF/Markdown/assets 指针字段与保留 review 的产物写入辅助方法。 | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:45`、`:85`、`:92` |
| 现有相对路径校验器拒绝 unsafe path。 | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:1` |
| 现有 converter capability record 提供可脱敏 capability 形状。 | `backend/src/main/java/com/atlas/metadata/adapter/ConverterCapability.java:9` |
| 现有 converter adapter status 枚举提供 adapter-status 范式。 | `backend/src/main/java/com/atlas/metadata/enums/ConverterAdapterStatus.java:1` |
| 现有 converter API 范式提供 capability/run/report 先例。 | `backend/src/main/java/com/atlas/metadata/controller/ConversionController.java:21` |
| 现有 seam guard 在 adapter 范围禁止 `S3`，storage adapter 必须更新它。 | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:42` |
| 不需要新 `FileStatus` 值；storage 只更新指针字段。 | `backend/src/main/java/com/atlas/metadata/enums/FileStatus.java:6` |

## 生成技能链

- `atlas-sdd-generate-all`
- `req-to-user-story`
- `user-story-to-spec`
- `spec-to-architecture`
- `architecture-to-design`
- `design-to-tasks`
- 为 adapter 边界质量考虑 `architecture-review` 门（已记录 seam guard 更新决定）
- 以 `review-doc-quality` 门作为最终 SDD 质量审查

## 调和假设

- Layers 提交为 `raw`、`pdf`、`markdown`、`assets`、`reports`、`wiki`，与 `PROJECT_RULES.md` Workspace Separation 及 REQ-PROD-014 对齐。
- API 仅返回 descriptor；对象字节保留在 adapter 之后（OQ-SA-002）。
- 首个真实存储引擎（S3 兼容 vs 文件系统）推迟（OQ-SA-001）；mock 契约不依赖它。
- Seam guard 的 `S3` 例外必须仅对 storage adapter 包放宽；这作为 T-SA-009 与 DT-SA-001 记录，使其为刻意变更。

## 待确认问题

| ID | 问题 | 负责人 | 是否阻塞实现？ |
|---|---|---|---|
| OQ-SA-001 | S3 兼容 vs 文件系统作为首个真实存储引擎。 | 架构 / 平台 | 否；mock 契约可推进。 |
| OQ-SA-002 | 对象字节是否经过 API。 | 架构 | 否；SDD 提交为仅 descriptor 响应。 |
| OQ-SA-003 | Presigned/短时访问 URL 推迟到后续切片。 | 产品 / 架构 | 否；不在本契约内。 |

## 验证计划

文档轮检查：

```bash
git diff --check
for f in docs/01-requirements/storage-adapter-requirements.md docs/01-requirements/storage-adapter-requirements.zh-CN.md docs/02-user-stories/storage-adapter-stories.md docs/02-user-stories/storage-adapter-stories.zh-CN.md docs/03-spec/storage-adapter-spec.md docs/03-spec/storage-adapter-spec.zh-CN.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-architecture.zh-CN.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-flow.zh-CN.md docs/04-architecture/storage-adapter-data-model.md docs/04-architecture/storage-adapter-data-model.zh-CN.md docs/05-design/storage-adapter-design.md docs/05-design/storage-adapter-design.zh-CN.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md docs/06-tasks/storage-adapter-tasks.md docs/06-tasks/storage-adapter-tasks.zh-CN.md docs/00-context/storage-adapter-traceability.md docs/00-context/storage-adapter-traceability.zh-CN.md; do test -s "$f" || exit 1; done
! rg -n "T[O]DO|T[B]D|to be determine[d]|implementation will decid[e]|grep late[r]" docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md docs/00-context/storage-adapter-traceability.md
```

实现轮检查列于 `docs/06-tasks/storage-adapter-tasks.md`。

## 推荐 Codex 交接

```text
Implement the storage-adapter slice strictly against docs/03-spec/storage-adapter-spec.md and docs/06-tasks/storage-adapter-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```
