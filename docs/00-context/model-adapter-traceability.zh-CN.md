# 溯源：模型适配器

## 状态

`model-adapter` 的已实现溯源。该切片现在包含 SDD 产物以及 backend adapter/API 实现证据。

## 切片契约

| 字段 | 值 |
|---|---|
| Goal | Atlas 可以通过产品面的模型适配器契约调用 LLM、embedding、rerank、vision、speech 模型能力，并保持配置脱敏、仅 mock 验证、输出默认需审核。 |
| Slice | `model-adapter` |
| Phase | 3 adapter |
| Scope | 模型能力元数据、adapter contract、model run API、mock model execution、run evidence、source references、review-required output、安全错误和 guard/verification plan。 |
| Exclusions | 真实 provider calls、Ask/RAG、vector indexing、graph extraction、Wiki publication、frontend changes、production auth/RBAC、production secret management、raw prompt storage 和 external network/cloud calls。 |
| Verification row | Phase 3 adapter：针对 mock engines 的 unit + integration tests。 |
| Constraints row | Parser/converter/model/vector/storage 只走产品面 adapters；禁止直接调用 tool/provider；禁止硬编码单一实现；secret 脱敏；保留 trace/review status。 |

## 已阅读来源

- `README.md`
- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/00-context/slice-roadmap.zh-CN.md`
- `docs/01-requirements/requirement.md`
- `docs/architecture.md`
- `docs/technology-decisions.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- 现有 converter/parser/storage adapter SDD 文档
- `frontend/public/atlas-prototype.html`、`prototypes/index.html`、`frontend/src/types.ts`、`frontend/src/data/atlasMock.ts` 中的 FE 模型设置参考

## 已应用技能链

| Skill | Applied How |
|---|---|
| `atlas-sdd-generate-all` | 编排完整双语 SDD 集与最终一致性门。 |
| `req-to-user-story` | 将需求转为能力域用户故事。 |
| `user-story-to-spec` | 将故事合并为行为唯一真相源。 |
| `spec-to-architecture` | 将 spec 转换为 adapter architecture、data flow、data model。 |
| `architecture-to-design` | 将 architecture 细化为 design 与 API/adapter contract。 |
| `design-to-tasks` | 将 design 转换为可执行 Codex 任务。 |
| `architecture-review` | 在设计过程中检查 adapter 可扩展性、解耦、secret safety 与 phase boundaries。 |
| `review-doc-quality` | 对最终 SDD 集进行 completeness、traceability、bilingual parity 与 readiness 检查。 |

## Requirement 到 Story / Spec / Task 矩阵

| Requirement | User Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-MODA-001 | US-MODA-001, US-MODA-005 | Adapter Boundary | T-MODA-003, T-MODA-005, T-MODA-009 |
| REQ-MODA-002 | US-MODA-001 | Adapter Boundary, Capability Metadata | T-MODA-003, T-MODA-004, T-MODA-005 |
| REQ-MODA-003 | US-MODA-001 | Capability Metadata, API / Interface Surface | T-MODA-002, T-MODA-003, T-MODA-005, T-MODA-008 |
| REQ-MODA-004 | US-MODA-001, US-MODA-004 | Capability Metadata, Validation And Failure Behavior | T-MODA-002, T-MODA-005, T-MODA-009 |
| REQ-MODA-005 | US-MODA-002 | Capability Metadata, Operation Outputs | T-MODA-003, T-MODA-004 |
| REQ-MODA-006 | US-MODA-002 | Model Run Lifecycle | T-MODA-001, T-MODA-006, T-MODA-008 |
| REQ-MODA-007 | US-MODA-002, US-MODA-003 | Operation Outputs | T-MODA-001, T-MODA-002, T-MODA-006, T-MODA-007, T-MODA-008 |
| REQ-MODA-008 | US-MODA-002, US-MODA-003 | Operation Outputs, State Model | T-MODA-001, T-MODA-006, T-MODA-007 |
| REQ-MODA-009 | US-MODA-003 | Source Reference Preservation, Operation Outputs | T-MODA-001, T-MODA-002, T-MODA-007 |
| REQ-MODA-010 | US-MODA-002, US-MODA-005 | Operation Outputs, Embedding Boundary | T-MODA-007, T-MODA-009 |
| REQ-MODA-011 | US-MODA-002, US-MODA-003 | Operation Outputs | T-MODA-004, T-MODA-007 |
| REQ-MODA-012 | US-MODA-004 | Validation And Failure Behavior | T-MODA-007, T-MODA-009 |
| REQ-MODA-013 | US-MODA-005 | Constraints, Acceptance Matrix | T-MODA-004, T-MODA-009, T-MODA-010 |
| REQ-MODA-014 | US-MODA-002, US-MODA-004 | Model Run Lifecycle, State Model | T-MODA-001, T-MODA-006, T-MODA-008, T-MODA-010 |
| REQ-MODA-015 | US-MODA-005 | Out Of Scope, Acceptance Matrix | T-MODA-009, T-MODA-010 |

## 已创建文件

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/model-adapter-requirements.md` | `docs/01-requirements/model-adapter-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/model-adapter-stories.md` | `docs/02-user-stories/model-adapter-stories.zh-CN.md` |
| Spec | `docs/03-spec/model-adapter-spec.md` | `docs/03-spec/model-adapter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/model-adapter-architecture.md` | `docs/04-architecture/model-adapter-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/model-adapter-data-flow.md` | `docs/04-architecture/model-adapter-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/model-adapter-data-model.md` | `docs/04-architecture/model-adapter-data-model.zh-CN.md` |
| Design | `docs/05-design/model-adapter-design.md` | `docs/05-design/model-adapter-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/model-adapter-tasks.md` | `docs/06-tasks/model-adapter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/model-adapter-traceability.md` | `docs/00-context/model-adapter-traceability.zh-CN.md` |

## API Guide 决定

API guide 已包含。`model-adapter` 是 Phase 3 backend/API adapter contract 切片，且 `docs/00-context/slice-roadmap.md` 规定每个 adapter slice 需要 adapter contract。

## 实现证据

| Task Range | Evidence |
|---|---|
| T-MODA-001 | 新增 model run、output、source reference 持久化，并加入 Flyway migration `backend/src/main/resources/db/migration/V7__model_adapter.sql`。 |
| T-MODA-002 | 新增 model capability/run/source/output DTO 与 `ModelMapper`；响应保持在 `ApiEnvelope` 内。 |
| T-MODA-003, T-MODA-004 | 新增 `ModelAdapter` contract、确定性的 `MockModelAdapter` 和安全的 `ConfiguredModelAdapter` placeholder。 |
| T-MODA-005 | 新增 `ModelAdapterRegistry`，支持显式/默认模型选择、兼容性校验和能力列表。 |
| T-MODA-006, T-MODA-007 | 新增 `ModelService` run lifecycle、source trace 持久化、output validation、review-required enforcement、usage summary 和安全 adapter-fault handling。 |
| T-MODA-008 | 在 `ModelController` 中新增 `GET /api/model-adapters`、`POST /api/model-runs`、`GET /api/model-runs/{runId}`。 |
| T-MODA-009 | 扩展 `AdapterSeamGuardTest`，覆盖模型 provider/runtime markers、outbound client bans、vector write bans 和 secret/private-path scans。 |
| T-MODA-010 | 已通过 model unit、service、seam、API integration 和 backend full verification commands 验证。 |

## 验证证据

| Command | Result |
|---|---|
| `cd backend && mvn -Dtest=ModelAdapterContractTest,ModelAdapterRegistryTest,ModelSummaryCalculatorTest,ModelDomainInvariantTest test` | Pass |
| `cd backend && mvn -Dtest=ModelServiceTest,ModelSummaryCalculatorTest,ModelDomainInvariantTest test` | Pass |
| `cd backend && mvn -Dtest=AdapterSeamGuardTest test` | Pass |
| `cd backend && mvn -Dit.test=ModelApiContractIT verify` | Pass |
| `cd backend && mvn verify` | Pass |
| `git diff --check` | Pass |
| 针对 controller/service/repository/domain 的 model provider/client/vector seam grep | Pass |
| 针对 backend source 与 model SDD docs 的 secret、private path、private key grep | Pass |

## Review-Doc-Quality 门

| Check | Result |
|---|---|
| 每个改动 artifact 都存在英文和中文副本 | Pass |
| REQ/US/T IDs 跨语言一致 | Pass |
| Requirements 映射到 stories/spec/tasks | Pass |
| Tasks 可由 Codex 执行 | Pass |
| 阶段纪律保留 | Pass |
| mock-only/no-network/adapter/secret-masked constraints 明确 | Pass |
| API guide inclusion/omission 已记录 | Pass，已包含 |
| Open questions 明确 | Pass |

## 残留风险

- 真实 provider 选择未定，并已故意延后。
- Raw prompt retention 是产品/安全决策，本 SDD 不启用。
- Usage/cost governance 延后到未来 governance/hardening 切片。

## 推荐 Codex 交接命令

```text
Implement the model-adapter slice strictly against docs/03-spec/model-adapter-spec.md and docs/06-tasks/model-adapter-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```
