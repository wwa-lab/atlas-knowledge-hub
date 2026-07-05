# 溯源：Ask RAG

## 状态

已实现。该切片现在包含后端 Ask run 持久化、adapter-bound retrieval 与 model orchestration、API endpoints、Trusted Ask UI 状态映射、E2E 覆盖、seam guards 和验证证据。

## 切片契约

| 字段 | 值 |
|---|---|
| Goal | Atlas 用户可以在 Knowledge Space 内提问，并基于已审核知识获得带来源、Review 状态、confidence 与安全失败行为的可信回答。 |
| Slice | `ask-rag` |
| Phase | 4 hardening |
| Scope | Trusted Ask UI、Ask API、adapter-bound vector retrieval、adapter-bound model answer generation、evidence bundle、review-awareness、audit records、safe errors 和 verification tasks。 |
| Exclusions | 真实外部 model/vector 调用、新 parser/converter 行为、graph extraction、Wiki publish state machine、production SSO/RBAC、raw prompt/source retention、streaming 和 provider cost governance。 |
| Verification row | Phase 4 hardening：触及层的 full unit + integration + E2E。 |
| Constraints row | 所有 Markdown/metadata 保留 source trace、confidence、review status；LLM output 在验证前保持 review-required；adapters only；secret-masked；mock engines；no external network calls。 |

## 已读来源

- `README.md`
- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/01-requirements/requirement.md`
- `docs/product-vision.md`
- `docs/mvp-scope.md`
- `docs/markdown-standard.md`
- `docs/review-workflow.md`
- `docs/knowledge-graph-design.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/03-spec/vector-adapter-spec.md`
- `docs/03-spec/model-adapter-spec.md`
- `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`
- `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md`
- `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md`
- `frontend/public/atlas-prototype.html`
- `prototypes/index.html`

## 使用的技能链

| Skill | 使用方式 |
|---|---|
| `atlas-sdd-generate-all` | 编排完整双语 SDD 文档集和一致性归并。 |
| `req-to-user-story` | 将需求转换为能力域用户故事。 |
| `user-story-to-spec` | 将故事整合为行为源。 |
| `spec-to-architecture` | 推导高层架构与边界。 |
| `architecture-to-design` | 生成 design、data flow、data model 和 API contract。 |
| `design-to-tasks` | 转换为 Codex 可执行任务。 |
| `review-doc-quality` | 执行最终质量门；结果记录如下。 |

## SDD 产物

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/ask-rag-requirements.md` | `docs/01-requirements/ask-rag-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/ask-rag-stories.md` | `docs/02-user-stories/ask-rag-stories.zh-CN.md` |
| Spec | `docs/03-spec/ask-rag-spec.md` | `docs/03-spec/ask-rag-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/ask-rag-architecture.md` | `docs/04-architecture/ask-rag-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/ask-rag-data-flow.md` | `docs/04-architecture/ask-rag-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/ask-rag-data-model.md` | `docs/04-architecture/ask-rag-data-model.zh-CN.md` |
| Design | `docs/05-design/ask-rag-design.md` | `docs/05-design/ask-rag-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/ask-rag-tasks.md` | `docs/06-tasks/ask-rag-tasks.zh-CN.md` |
| Traceability | `docs/00-context/ask-rag-traceability.md` | `docs/00-context/ask-rag-traceability.zh-CN.md` |

## API Guide 决策

API guide 已包含。`ask-rag` 是 full-stack Phase 4 hardening 切片，并新增 `POST /api/spaces/{spaceId}/ask` 与 `GET /api/ask-runs/{runId}`。

## 需求追踪

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-ASKRAG-001 | US-ASKRAG-001 | Ask Request, UI Behavior | T-ASKRAG-003, T-ASKRAG-006 |
| REQ-ASKRAG-002 | US-ASKRAG-002 | Retrieval Policy | T-ASKRAG-003 |
| REQ-ASKRAG-003 | US-ASKRAG-002 | Evidence And Audit, UI Behavior | T-ASKRAG-002, T-ASKRAG-006 |
| REQ-ASKRAG-004 | US-ASKRAG-004 | Answer Generation, Constraints | T-ASKRAG-003, T-ASKRAG-008 |
| REQ-ASKRAG-005 | US-ASKRAG-002 | Evidence And Audit | T-ASKRAG-001, T-ASKRAG-002 |
| REQ-ASKRAG-006 | US-ASKRAG-005 | Answer Generation, State Model | T-ASKRAG-001, T-ASKRAG-005 |
| REQ-ASKRAG-007 | US-ASKRAG-006 | Failure Behavior, API Surface | T-ASKRAG-002, T-ASKRAG-004 |
| REQ-ASKRAG-008 | US-ASKRAG-006 | Ask Request, Failure Behavior | T-ASKRAG-002, T-ASKRAG-004 |
| REQ-ASKRAG-009 | US-ASKRAG-005 | Evidence And Audit | T-ASKRAG-001, T-ASKRAG-005 |
| REQ-ASKRAG-010 | US-ASKRAG-001 | UI Behavior | T-ASKRAG-006, T-ASKRAG-007 |
| REQ-ASKRAG-011 | US-ASKRAG-004 | Constraints, Acceptance Matrix | T-ASKRAG-007, T-ASKRAG-008 |
| REQ-ASKRAG-012 | US-ASKRAG-003 | Answer Generation, Failure Behavior | T-ASKRAG-003, T-ASKRAG-004 |
| REQ-ASKRAG-013 | US-ASKRAG-002 | Retrieval Policy, UI Behavior | T-ASKRAG-003, T-ASKRAG-006 |
| REQ-ASKRAG-014 | US-ASKRAG-006 | Acceptance Matrix | T-ASKRAG-008, T-ASKRAG-009, T-ASKRAG-010 |

## Gate Note

`docs/00-context/slice-roadmap.md` 此前将 `ask-rag` 标记为依赖 Phase 3。实现是在仓库已具备并验证 vector 与 model adapter 切片后推进的；ask-rag 实现仍保持 mock-only 与 adapter-bound。

## 实现证据

| Area | Evidence |
|---|---|
| Domain and persistence | `AskRun`、`AskEvidence`、`AskRunRepository`、`AskEvidenceRepository` 与 `V8__ask_rag.sql` 增加 Ask audit records 和 evidence snapshots，且不修改 source files、chunks、Wiki、graph 或 review state。 |
| Service orchestration | `AskService` 验证安全问题文本，默认 `APPROVED_ONLY`，支持显式 `INCLUDE_REVIEW_REQUIRED`，通过 `VectorService` 检索证据，通过 `ModelService` 生成答案，并保持 generated answers 为 `REVIEW_REQUIRED`。 |
| API surface | `AskController` 提供 `POST /api/spaces/{spaceId}/ask` 与 `GET /api/ask-runs/{runId}`，使用 `ApiEnvelope` 和安全错误行为。 |
| Frontend state mapping | `frontend/public/atlas-prototype.html`、`prototypes/index.html`、`frontend/src/types.ts` 和 `frontend/src/data/atlasMock.ts` 暴露 answered、loading、no-evidence、review-warning 和 safe-error 状态。 |
| E2E coverage | `frontend/tests/e2e/phase1-smoke.spec.ts` 验证 Ask tab、answer state、evidence、review-required warning、no-evidence state 和 safe-error state。 |
| Seam guard | `AdapterSeamGuardTest` 扫描 ask-rag docs 与 product layers，防止直接 provider/client references、private paths 和 secret-like material。 |

## 验证证据

```bash
cd backend && mvn -Dtest=AskDomainInvariantTest,AskRequestValidationTest,AskServiceTest,AskSummaryCalculatorTest,AskStateImmutabilityTest test
cd backend && mvn -Dtest=AdapterSeamGuardTest test
cd backend && mvn -Dit.test=AskApiContractIT verify
cd frontend && npm run typecheck
cd frontend && npm run test
cd frontend && npm run build
cd frontend && npm run e2e
```

## Review-Doc-Quality Gate

| Check | Result |
|---|---|
| 每个 touched artifact 均有英文与中文文件 | Pass |
| REQ/US/T IDs 跨语言一致 | Pass |
| Requirements 映射到 stories/spec/tasks | Pass |
| Tasks 可由 Codex 执行 | Pass |
| Phase discipline 和 implementation gate 明确 | Pass |
| Adapter、mock-only、no-network、secret-masked、trace/review 约束明确 | Pass |
| API guide inclusion decision 已记录 | Pass |
| Open questions 明确 | Pass |
| 实现证据与验证命令已记录 | Pass |

## 文档验证计划

```bash
git diff --check
for f in docs/01-requirements/ask-rag-requirements.md docs/01-requirements/ask-rag-requirements.zh-CN.md docs/02-user-stories/ask-rag-stories.md docs/02-user-stories/ask-rag-stories.zh-CN.md docs/03-spec/ask-rag-spec.md docs/03-spec/ask-rag-spec.zh-CN.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-architecture.zh-CN.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-flow.zh-CN.md docs/04-architecture/ask-rag-data-model.md docs/04-architecture/ask-rag-data-model.zh-CN.md docs/05-design/ask-rag-design.md docs/05-design/ask-rag-design.zh-CN.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.zh-CN.md docs/06-tasks/ask-rag-tasks.md docs/06-tasks/ask-rag-tasks.zh-CN.md docs/00-context/ask-rag-traceability.md docs/00-context/ask-rag-traceability.zh-CN.md; do test -s "$f" || exit 1; done
! rg -n "T[O]DO|T[B]D|to be determine[d]|implementation will decid[e]|grep late[r]" docs/01-requirements/ask-rag-requirements.md docs/02-user-stories/ask-rag-stories.md docs/03-spec/ask-rag-spec.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-model.md docs/05-design/ask-rag-design.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/ask-rag-tasks.md docs/00-context/ask-rag-traceability.md
```

## 剩余风险

- 生产 RBAC 与角色级 evidence 可见性仍是待定产品决策。
- Reranking 和 answer review queue integration 延后，除非显式加入。

## 推荐 Codex 交接命令

```text
Implement the ask-rag slice strictly against docs/03-spec/ask-rag-spec.md and docs/06-tasks/ask-rag-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

## Product Goal Batch 3 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase G 可信问答 | `T-ASKRAG-011` | L2 Vue parity | `frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts`；`docs/00-context/evidence/phase-g-trusted-ask.png`；真实 Vue 全局 Chat 展示多空间上下文选择、问题输入、模型选择器、evidence citations、no-approved-evidence refusal 和 review-required warning。 |

该状态仅更新产品界面成熟度，不新增真实外部模型调用、生产 RAG 优化、raw prompt/vector/provider payload 存储或新的 backend/API contract。

## Product Goal Batch 6 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase I6 Ask runs and citations API-backed Vue 切换 | `T-FSP-012`，消费既有 Ask create/read APIs | L3 API-backed | `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts`；`docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png`；真实 Vue 全局 Ask 通过 Atlas API 提交，并展示 answer status、model run id 与 citations。 |

该状态只升级产品 Ask 界面，不新增生产 retrieval governance、真实 provider calls、真实公司数据或新的后端/API 契约。
