# 溯源：Vector Adapter

## 状态

已实现。初始 SDD 由 `atlas-sdd-generate-all` workflow 为 `vector-adapter` 切片生成；本轮实现已严格依据 `docs/03-spec/vector-adapter-spec.md` 与 `docs/06-tasks/vector-adapter-tasks.md` 完成 backend adapter/domain/API/test 范围。

## 切片契约

| Field | Value |
|---|---|
| Goal | Atlas 能通过可替换 vector adapter 索引 approved 或 review-aware source chunks，并返回可追溯 similarity evidence，同时不让产品层耦合到 pgvector、Milvus、Qdrant 或单一 vector engine。 |
| Slice | `vector-adapter` |
| Phase | 3 adapter |
| Scope | Vector adapter contract、capabilities、index/deindex/query operations、source-chunk trace preservation、vector run/result records、review-aware query policy、mock-engine verification、API guide、tasks。 |
| Exclusions | 真实 vector DB 执行、embedding/model provider calls、Ask/RAG answer generation、graph extraction、Wiki publication、frontend UI、production auth/RBAC。 |
| Verification row | Unit + integration tests against mock engines。 |
| Constraints row | Parser/converter/model/vector/storage 只走产品面 adapters；禁止 direct tool calls；不硬编码单一实现；secret masked；trace/confidence/review preserved。 |

## Source Documents

| Source | Use |
|---|---|
| `README.md` | 产品 workflow 与技术方向。 |
| `PROJECT_RULES.md` | SDD、adapter、安全、数据、阶段纪律、source trace、review rules。 |
| `DEVELOPMENT_STANDARDS.md` | Phase 3 adapter standards 与 verification。 |
| `docs/00-context/sdd-profile.md` | 必需 SDD artifact chain 与 ID format。 |
| `docs/00-context/slice-roadmap.md` | Phase 3 verification/constraints 与 slice backlog。 |
| `docs/01-requirements/requirement.md` | 产品级 adapter、vector DB setting、Ask evidence、trace、review rules。 |
| `docs/architecture.md` | Adapter-based architecture 与 worker plane。 |
| `docs/markdown-standard.md` | Source trace、confidence、review metadata。 |
| `docs/knowledge-graph-design.md` | Approved content 与 evidence-first graph/retrieval 方向。 |
| `docs/03-spec/parser-adapter-spec.md` | Source chunk production 与同阶段 adapter pattern。 |
| Existing backend metadata code | Grounded current source chunks、file items、parser run pattern、API envelope、seam guard。 |

## Gate Note

`docs/00-context/slice-roadmap.md` 仍将 `vector-adapter` 标记为 gated by Phase 2；同一 roadmap 已将 `metadata-api` Phase 2 以及早期 Phase 3 converter/parser adapters 标记为 implemented。本实现基于现有 metadata API、source chunk 与 adapter 基础设施，将 Phase 2 前置条件视为已满足。

## Implementation Evidence

| Area | Evidence |
|---|---|
| Domain and persistence | `VectorRun`、`VectorItemResult`、repositories 与 `V6__vector_adapter.sql` 增加 run/result evidence，且不修改 source metadata。 |
| Adapter boundary | `VectorAdapter`、`MockVectorAdapter`、`PgVectorAdapter` 与 `VectorAdapterRegistry` 将 vector engine 细节限制在产品面 contract 之后。 |
| Service and API | `VectorService` 与 `VectorController` 暴露 capability listing、index/deindex runs、run reports 与 immediate query evidence。 |
| Verification | `VectorAdapterContractTest`、`VectorAdapterRegistryTest`、`VectorServiceTest`、`VectorQueryServiceTest`、`VectorApiContractIT` 与 `AdapterSeamGuardTest` 覆盖 mock-only adapter behavior、review policy、invalid dimensions、masked failures、API contracts 与 seam guards。 |
| Seam guard update | `AdapterSeamGuardTest` 已有意只在 adapter source 中允许 vector engine names，同时继续在 controller/service/repository/domain 层禁止它们，并对 vector docs 执行 private paths/secrets scan。 |

## SDD Artifacts

| Stage | English | Simplified Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/vector-adapter-requirements.md` | `docs/01-requirements/vector-adapter-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/vector-adapter-stories.md` | `docs/02-user-stories/vector-adapter-stories.zh-CN.md` |
| Spec | `docs/03-spec/vector-adapter-spec.md` | `docs/03-spec/vector-adapter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/vector-adapter-architecture.md` | `docs/04-architecture/vector-adapter-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/vector-adapter-data-flow.md` | `docs/04-architecture/vector-adapter-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/vector-adapter-data-model.md` | `docs/04-architecture/vector-adapter-data-model.zh-CN.md` |
| Design | `docs/05-design/vector-adapter-design.md` | `docs/05-design/vector-adapter-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/vector-adapter-tasks.md` | `docs/06-tasks/vector-adapter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/vector-adapter-traceability.md` | `docs/00-context/vector-adapter-traceability.zh-CN.md` |

## API Guide Decision

API guide 已包含。Vector-adapter 是 Phase 3 backend/API + adapter contract 切片，因此实现前需要 `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md`。

## Requirement Trace

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-VA-001 | US-VA-001, US-VA-005 | Adapter Boundary | T-VA-003, T-VA-005, T-VA-009 |
| REQ-VA-002 | US-VA-002 | Adapter Boundary, Capability Metadata | T-VA-003, T-VA-004 |
| REQ-VA-003 | US-VA-002 | Capability Metadata, API / Interface Surface | T-VA-002, T-VA-005, T-VA-008 |
| REQ-VA-004 | US-VA-001 | Indexing And Deindexing | T-VA-001, T-VA-006 |
| REQ-VA-005 | US-VA-001 | Indexing And Deindexing, State Model | T-VA-001, T-VA-006, T-VA-008 |
| REQ-VA-006 | US-VA-001 | Indexing And Deindexing | T-VA-001, T-VA-002, T-VA-006 |
| REQ-VA-007 | US-VA-003 | Similarity Query | T-VA-002, T-VA-007, T-VA-008 |
| REQ-VA-008 | US-VA-004 | Indexing And Deindexing | T-VA-006 |
| REQ-VA-009 | US-VA-002, US-VA-005 | Validation And Failure Behavior | T-VA-004, T-VA-007, T-VA-009 |
| REQ-VA-010 | US-VA-001, US-VA-005 | Constraints, Acceptance Matrix | T-VA-004, T-VA-006, T-VA-009, T-VA-010 |
| REQ-VA-011 | US-VA-003 | Similarity Query | T-VA-001, T-VA-006, T-VA-007, T-VA-010 |
| REQ-VA-012 | US-VA-004 | Constraints, Out Of Scope | T-VA-001, T-VA-006 |
| REQ-VA-013 | US-VA-005 | Adapter Boundary, Acceptance Matrix | T-VA-009, T-VA-010 |
| REQ-VA-014 | US-VA-005 | API / Interface Surface, Acceptance Matrix | T-VA-008, T-VA-010 |

## Grounding Evidence

| Claim | Evidence |
|---|---|
| Existing source chunks expose file item id, source file, page/section, confidence, and review status. | `backend/src/main/java/com/atlas/metadata/domain/SourceChunk.java:21`, `:24`, `:27`, `:30`, `:33`, `:36` |
| Existing file items expose batch id and review/confidence state. | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:24`, `:38`, `:41` |
| Parser run/result entities provide same-phase run evidence patterns. | `backend/src/main/java/com/atlas/metadata/domain/ParserRun.java:13`, `backend/src/main/java/com/atlas/metadata/domain/ParserFileResult.java:14` |
| Existing parser adapter and capability records provide adapter contract shape. | `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:3`, `backend/src/main/java/com/atlas/metadata/adapter/ParserCapability.java:9` |
| Existing registry supports explicit/default adapter resolution. | `backend/src/main/java/com/atlas/metadata/service/ParserAdapterRegistry.java:11` |
| Existing API envelope is the response wrapper. | `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:1` |
| Existing parser controller provides capability/run/report precedent. | `backend/src/main/java/com/atlas/metadata/controller/ParserController.java:18` |
| Existing seam guard forbids vector engine names and must be updated for vector adapter scope. | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15`, `:22`, `:23`, `:24`, `:41` |

## Generated Skill Chain

- `atlas-sdd-generate-all`
- `req-to-user-story`
- `user-story-to-spec`
- `spec-to-architecture`
- `architecture-to-design`
- `design-to-tasks`
- `architecture-review` gate considered for adapter boundary quality（已记录 seam guard update decision）
- `review-doc-quality` gate applied as final SDD quality review

## Reconciliation Assumptions

- Vector indexing 从 `source_chunk` evidence 开始，而不是 raw parser output。
- Query 默认 approved-only evidence；review-required chunks 需要显式 inclusion。
- Embeddings 在本切片中以 mock/precomputed payloads 提供；真实 embedding generation 属于 model-adapter 或 worker scope。
- 首个真实 vector engine 选择延后；mock adapter contract 不依赖它。
- Seam guard exception 必须只为 vector adapter package 放宽；已记录为 T-VA-009。

## Open Questions

| ID | Question | Owner | Blocks Implementation? |
|---|---|---|---|
| OQ-VA-001 | pgvector vs standalone vector DB 作为首个真实 engine。 | Architecture / platform | No；mock contract 可继续。 |
| OQ-VA-002 | Embeddings 来自 model-adapter 还是 offline worker。 | Architecture / model platform | No；本切片使用 mock/precomputed vectors。 |
| OQ-VA-003 | 未来 Ask 的 review-required query inclusion 默认值。 | Product / SME workflow | No；本 SDD 默认 approved-only。 |

## Verification Plan

文档 pass 检查：

```bash
git diff --check
for f in docs/01-requirements/vector-adapter-requirements.md docs/01-requirements/vector-adapter-requirements.zh-CN.md docs/02-user-stories/vector-adapter-stories.md docs/02-user-stories/vector-adapter-stories.zh-CN.md docs/03-spec/vector-adapter-spec.md docs/03-spec/vector-adapter-spec.zh-CN.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-architecture.zh-CN.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-flow.zh-CN.md docs/04-architecture/vector-adapter-data-model.md docs/04-architecture/vector-adapter-data-model.zh-CN.md docs/05-design/vector-adapter-design.md docs/05-design/vector-adapter-design.zh-CN.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md docs/06-tasks/vector-adapter-tasks.md docs/06-tasks/vector-adapter-tasks.zh-CN.md docs/00-context/vector-adapter-traceability.md docs/00-context/vector-adapter-traceability.zh-CN.md; do test -s "$f" || exit 1; done
! rg -n "T[O]DO|T[B]D|to be determine[d]|implementation will decid[e]|grep late[r]" docs/01-requirements/vector-adapter-requirements.md docs/02-user-stories/vector-adapter-stories.md docs/03-spec/vector-adapter-spec.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-model.md docs/05-design/vector-adapter-design.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/vector-adapter-tasks.md docs/00-context/vector-adapter-traceability.md
```

Implementation-pass checks 列在 `docs/06-tasks/vector-adapter-tasks.md`。

## Recommended Codex Handoff

```text
Implement the vector-adapter slice strictly against docs/03-spec/vector-adapter-spec.md and docs/06-tasks/vector-adapter-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```
