# 溯源：全栈产品化

## 切片契约

- Slice: `full-stack-productization`
- Goal: 从 space list 到 Ask 的 P0 浏览器驱动 full-stack web service 闭环。
- Phase: 4 hardening / P0 productization。
- Status: Implemented and verified on 2026-07-03。

## 来源文档

- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/01-requirements/requirement.md`
- 现有 SDD slices：`metadata-api`、`review-publish`、`knowledge-graph`、`ask-rag`、`model-adapter`、`vector-adapter`、`provider-backed-e2e`
- FE baseline/reference：`frontend/public/atlas-prototype.html`、`prototypes/index.html`

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-FSP-001 | US-FSP-001 | T-FSP-002, T-FSP-003 |
| REQ-FSP-002 | US-FSP-001 | T-FSP-002, T-FSP-003 |
| REQ-FSP-003 | US-FSP-002 | T-FSP-004 |
| REQ-FSP-004 | US-FSP-002 | T-FSP-004 |
| REQ-FSP-005 | US-FSP-003 | T-FSP-005 |
| REQ-FSP-006 | US-FSP-003 | T-FSP-005 |
| REQ-FSP-007 | US-FSP-004, US-FSP-005 | T-FSP-006 |
| REQ-FSP-008 | US-FSP-004 | T-FSP-006 |
| REQ-FSP-009 | US-FSP-005 | T-FSP-007 |
| REQ-FSP-010 | US-FSP-006 | T-FSP-003, T-FSP-008 |
| REQ-FSP-011 | US-FSP-007 | T-FSP-009 |
| REQ-FSP-012 | US-FSP-007 | T-FSP-010 |

## 验证计划

- `npm run e2e:first-layer`
- `npm run e2e:second-layer`
- `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e`
- `cd backend && mvn verify`
- `git diff --check`
- 对 changed files 做 secret/private-path scan。
- 对新增 external calls 做 network/dependency scan。

## 验证证据

- `npm run e2e:first-layer` 已通过。
- `npm run e2e:second-layer` 已通过；为避免共享 live backend 状态竞争，已将 second-layer Playwright 串行执行。
- `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e` 已通过。
- `cd backend && mvn verify` 已通过。
- `git diff --check` 已通过。
- secret/private-path scan 仅发现 static prototype 中名为 `password` 的 UI 标签字符串；未引入 raw secret 值、private path 或真实公司数据。
- network/dependency scan 未发现新增依赖，也未发现新增直连外部 provider/cloud 的调用；UI 仍通过配置的 API base 调用 Atlas API。

## SDD 质量门

- 每个新增 SDD artifact 都存在 English 和 Chinese companion。
- 两种语言中的 IDs 一致。
- 因为 backend/API 编排在范围内，已包含 API guide。
- 已根据当前 controllers、services、DTOs、E2E scripts 对现有实现做 grounding。
- 已用 `review-doc-quality` checklist 自审；实现前无 critical SDD blockers。

## 延后工作

- 生产文件字节上传。
- 生产认证/RBAC。
- 生产 storage/vector/model providers。
- 真实公司数据摄取。

## Product Goal Batch 5 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase I1-I3 API-backed Vue 切换 | `T-FSP-011`；消费 `T-MA-014` 中 space/batch/file/chunk metadata 范围，以及 `review-publish` review queue API | L3 API-backed | `frontend/tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts`；`docs/00-context/evidence/phase-i1-i3-api-backed-metadata.png`；真实 Vue 首页、空间页头、Documents tab 与 Processing Center 消费 Atlas API responses，覆盖 Knowledge Space metadata、batch/file/chunk metadata 与 review queues。 |

2026-07-05 验证：`cd frontend && npm run typecheck && npm run test && npm run build`；focused Phase I1-I3 Playwright；Batch 1-5 Playwright regression；`cd backend && mvn verify`。该状态不代表最终产品验收通过，也不新增后端/API 契约。

## Product Goal Batch 6 状态

| Phase | 任务 ID | 成熟度 | 证据 |
|---|---|---|---|
| Phase I4-I7 API-backed Vue 切换 | `T-FSP-012`；消费既有 review-publish、knowledge-graph、ask-rag 与 model-adapter APIs | L3 API-backed | `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts`；`docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png`；真实 Vue Wiki、Graph、全局 Ask 与 model settings surfaces 消费 Atlas API responses。 |

2026-07-05 验证：`cd frontend && npm run typecheck && npm run test && npm run build`；focused Phase I4-I7 Playwright；Batch 1-6 Playwright regression；`cd backend && mvn verify`。该状态不代表最终产品验收通过，也不新增生产 provider 调用、明文 secret、真实公司数据或新的后端/API 契约。

## Core Knowledge Loop v1 Traceability 补充

| Requirement | Stories | Tasks | Verification |
|---|---|---|---|
| REQ-FSP-013 | 用户可正常配置 model key | T-FSP-014, T-FSP-019 | Masked configuration API/UI checks；backend adapter tests |
| REQ-FSP-014 | 用户可上传真实文档 | T-FSP-015, T-FSP-019 | Multipart PDF/ZIP ingestion tests；manual UI upload |
| REQ-FSP-015 | 上传文档通过 adapter boundary 解析 | T-FSP-016 | Parser adapter tests；source trace inspection |
| REQ-FSP-016 | File 与 chunks review state 一致 | T-FSP-017 | Review service tests 与 queue inspection |
| REQ-FSP-017 | 后端刷新 downstream evidence | T-FSP-018, T-FSP-019 | Downstream refresh tests；graph/vector evidence checks |
| REQ-FSP-018 | Ask 在 publish/refresh 后可运行 | T-FSP-014, T-FSP-018, T-FSP-019 | Ask API/UI smoke with citations or safe state |
| REQ-FSP-019 | 未支持功能可见 disabled | T-FSP-019 | Frontend disabled-state checks |
| REQ-FSP-020 | 闭环安全门有记录 | T-FSP-020 | Final verification report |
