# 溯源：Review Publish

## 状态

已实现。已为 Phase 4 hardening 生成完整双语 SDD 集，并完成实现验证。最后更新：2026-07-03。

## 切片契约

- **Goal：** 让 SME 和交付负责人只把已批准、可追溯 Markdown 发布到可信 Wiki metadata。
- **Slice：** `review-publish`
- **Phase：** 4 hardening
- **Scope：** Review queues、SME review 状态机、publish eligibility、Wiki metadata publishing、API/UI contracts、verification tasks。
- **Exclusions：** Production auth/RBAC、graph extraction、Ask/RAG、真实外部 adapter execution、真实公司数据、production secret rollout。
- **Verification：** 对 touched layer 运行 full unit + integration + E2E；另运行 `git diff --check`、network/dependency scan、secret/private-path scan。
- **Constraints：** 保留 source trace、confidence、review status；LLM output 在核验前保持 review-required；adapter boundaries；无 raw secrets 或 private paths。

## 来源

| Source | 用途 |
|---|---|
| `PROJECT_RULES.md` | SDD、phase、adapter、security、bilingual rules。 |
| `AGENTS.md` | 项目本地 agent execution rules。 |
| `DEVELOPMENT_STANDARDS.md` | Phase 4 verification 和 quality gates。 |
| `docs/00-context/sdd-profile.md` | 必需文档链和 ID 规则。 |
| `docs/01-requirements/requirement.md` | 产品级 review、Wiki、Graph、Ask、security requirements。 |
| `docs/review-workflow.md` | Review states 和 review actions。 |
| `docs/markdown-standard.md` | Front matter、source trace、confidence、review status。 |
| `docs/knowledge-graph-design.md` | 下游 evidence constraints。 |
| `docs/00-context/slice-roadmap.md` | Phase 4 verification 和 constraints row。 |
| `frontend/public/atlas-prototype.html`, `prototypes/index.html` | FE Processing Center、Wiki、Graph、Ask baseline。 |
| `docs/04-architecture/metadata-api-data-model.md` | 现有 metadata entities 和 deferred Wiki table。 |
| `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` | 现有 metadata API 和 deferred review-publish endpoints。 |

## 产物集

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/review-publish-requirements.md` | `docs/01-requirements/review-publish-requirements.zh-CN.md` |
| Stories | `docs/02-user-stories/review-publish-stories.md` | `docs/02-user-stories/review-publish-stories.zh-CN.md` |
| Spec | `docs/03-spec/review-publish-spec.md` | `docs/03-spec/review-publish-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/review-publish-architecture.md` | `docs/04-architecture/review-publish-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/review-publish-data-flow.md` | `docs/04-architecture/review-publish-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/review-publish-data-model.md` | `docs/04-architecture/review-publish-data-model.zh-CN.md` |
| Design | `docs/05-design/review-publish-design.md` | `docs/05-design/review-publish-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/review-publish-tasks.md` | `docs/06-tasks/review-publish-tasks.zh-CN.md` |

## 需求溯源

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-REVIEW-PUBLISH-001 | US-REVIEW-PUBLISH-001 | Review Queue And Processing Center | T-REVIEW-PUBLISH-001, T-REVIEW-PUBLISH-006 |
| REQ-REVIEW-PUBLISH-002 | US-REVIEW-PUBLISH-002 | SME Review State Machine | T-REVIEW-PUBLISH-002 |
| REQ-REVIEW-PUBLISH-003 | US-REVIEW-PUBLISH-002 | SME Review State Machine | T-REVIEW-PUBLISH-002 |
| REQ-REVIEW-PUBLISH-004 | US-REVIEW-PUBLISH-003 | Publish Eligibility | T-REVIEW-PUBLISH-003 |
| REQ-REVIEW-PUBLISH-005 | US-REVIEW-PUBLISH-003 | Publish Result | T-REVIEW-PUBLISH-004, T-REVIEW-PUBLISH-005, T-REVIEW-PUBLISH-007 |
| REQ-REVIEW-PUBLISH-006 | US-REVIEW-PUBLISH-004 | Publish Result | T-REVIEW-PUBLISH-003, T-REVIEW-PUBLISH-005 |
| REQ-REVIEW-PUBLISH-007 | US-REVIEW-PUBLISH-003, US-REVIEW-PUBLISH-004 | API Behavior | T-REVIEW-PUBLISH-004, T-REVIEW-PUBLISH-009 |
| REQ-REVIEW-PUBLISH-008 | US-REVIEW-PUBLISH-001 | Review Queue And Processing Center | T-REVIEW-PUBLISH-006, T-REVIEW-PUBLISH-007 |
| REQ-REVIEW-PUBLISH-009 | US-REVIEW-PUBLISH-001, US-REVIEW-PUBLISH-003 | Downstream Trust Boundary | T-REVIEW-PUBLISH-006, T-REVIEW-PUBLISH-007 |
| REQ-REVIEW-PUBLISH-010 | US-REVIEW-PUBLISH-004 | Non-Functional Requirements | T-REVIEW-PUBLISH-003, T-REVIEW-PUBLISH-009 |
| REQ-REVIEW-PUBLISH-011 | US-REVIEW-PUBLISH-004 | Acceptance Matrix | T-REVIEW-PUBLISH-008, T-REVIEW-PUBLISH-009 |

## API Guide 决定

已包含 API guide。本切片是 full-stack Phase 4 hardening，并引入新 endpoints。

## 质量门：review-doc-quality

- **文档类型：** 完整 SDD 集。
- **Readiness verdict：** Ready with minor fixes。
- **优点：** 双语产物完整、跨语言 ID 稳定、已包含 API guide、tasks 映射到 REQ IDs 和 spec sections、包含确切验证命令。
- **轻微残留风险：** Duplicate publish behavior 以及 file vs Wiki `PUBLISHED` ownership 已在实现中决策；剩余风险是 full-suite verification 当前受无关 ask/knowledge-graph 工作区改动影响。
- **无 critical blockers：** SDD 集已可进入人工 review，再开始实现。

## 推荐 Codex 交接命令

```text
严格依据 docs/03-spec/review-publish-spec.md 与 docs/06-tasks/review-publish-tasks.md 实现 review-publish 切片：按 ID 顺序完成每一条任务，遵守每条任务标注的 Constraints 与 Verification，以 docs/03-spec 为行为唯一真相源，不扩大范围；若实现将偏离 spec，停下并指出不一致，而不是绕过它编码。
```

## 实现证据

2026-07-03 Codex 实现验证后更新。

- 后端已增加 review queue 和 Wiki publish metadata endpoints，未直接调用 parser/converter/model/vector/storage/search。
- Review queue responses 已包含 spec 要求的有界代表项 metadata，仅暴露安全字段，并由后端 contract tests 覆盖。
- Duplicate publish 行为采用幂等处理。
- Publish 设置 `wiki_page.review_status=PUBLISHED`；file 与 source chunk review metadata 保持不变。
- 未新增 schema migration，因为 `atlas.wiki_page` 已包含所需 metadata 字段。
- 前端 typed mocks 和 prototype surfaces 已展示 ready-to-publish、blocked missing-trace、published Wiki metadata 状态。
- 已通过验证：`cd backend && mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT,DomainInvariantTest'`；`cd backend && mvn verify`；`cd frontend && npm run typecheck`；`cd frontend && npm run test -- --run`；`cd frontend && npm run build`；`cd frontend && npm run e2e`；prototype JavaScript syntax extraction；`git diff --check`。
- 先前阻塞项已解决：ask/knowledge-graph migration/route/projection 预期已对齐，`frontend/src/App.vue` lint errors 已修复。
- 网络/secret 扫描说明：扫描仍报告 prototype 中既有 mock provider URLs 以及 masked password/secret 文案；本切片未引入新的 runtime network call 或 raw secret。

## Product Goal Batch 2 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase D 处理中心 | `T-REVIEW-PUBLISH-010` | L2 Vue parity | `frontend/tests/e2e/phase-c-d-upload-processing.spec.ts`；`docs/00-context/evidence/phase-d-processing-center.png`；真实 Vue Processing Center 展示质量门禁和 Wiki/Graph/Ask 可用性队列解释。 |

该状态仅更新产品界面成熟度，不新增生产 RBAC、真实 remediation worker 或新的 backend/API contract。

## Product Goal Batch 3 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase E LM Wiki | `T-REVIEW-PUBLISH-011` | L2 Vue parity | `frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts`；`docs/00-context/evidence/phase-e-lm-wiki.png`；真实 Vue Wiki 标签页展示可浏览页面、metadata、实体链接、confidence、review status 和 source trace。 |

该状态仅更新产品界面成熟度，不新增真实 Markdown generator、真实公司数据或新的 backend/API contract。

## Product Goal Batch 5 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase I3 Review queues API-backed Vue 切换 | `T-FSP-011`，消费既有 review queue API | L3 API-backed | `frontend/tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts`；`docs/00-context/evidence/phase-i1-i3-api-backed-metadata.png`；真实 Vue Processing Center 展示 API review queues，未改变 review-publish 后端契约。 |

该状态只升级 review queue 产品界面，不新增生产 RBAC、真实 remediation workers、真实公司数据或新的后端/API 契约。

## Product Goal Batch 6 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase I4 Wiki pages API-backed Vue 切换 | `T-FSP-012`，消费既有 publish 与 Wiki page APIs | L3 API-backed | `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts`；`docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png`；真实 Vue Wiki tab 在产品路径 publish 后读取 API-published Wiki metadata。 |

该状态只升级 Wiki 产品界面，不新增真实 Markdown generator、生产 RBAC、真实公司数据或新的后端/API 契约。
