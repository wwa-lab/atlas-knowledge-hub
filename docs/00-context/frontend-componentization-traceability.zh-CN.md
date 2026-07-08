# 溯源：Frontend Componentization

## 切片契约

- Slice: `frontend-componentization`
- Goal: 对单体 Vue `App.vue` 做行为不变的结构性拆分。
- Phase: 1 前端结构加固。
- Status: 2026-07-08 已生成 SDD 草案；实现需要先接受 SDD。

## 来源文档

- `README.md`
- `AGENTS.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/01-requirements/requirement.md`
- `docs/03-spec/knowledge-space-spec.md`
- `docs/03-spec/full-stack-productization-spec.md`
- `docs/06-tasks/knowledge-space-tasks.md`
- `docs/06-tasks/full-stack-productization-tasks.md`
- `frontend/src/App.vue`
- `frontend/src/App.test.ts`
- `frontend/src/api.ts`
- `frontend/src/types.ts`
- `frontend/src/data/atlasMock.ts`

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-FRONTEND-COMPONENTIZATION-001 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-003 | T-FRONTEND-COMPONENTIZATION-002, T-FRONTEND-COMPONENTIZATION-003, T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005 |
| REQ-FRONTEND-COMPONENTIZATION-002 | US-FRONTEND-COMPONENTIZATION-002 | T-FRONTEND-COMPONENTIZATION-003, T-FRONTEND-COMPONENTIZATION-004 |
| REQ-FRONTEND-COMPONENTIZATION-003 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-005 | T-FRONTEND-COMPONENTIZATION-001, T-FRONTEND-COMPONENTIZATION-003, T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005 |
| REQ-FRONTEND-COMPONENTIZATION-004 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-006 |
| REQ-FRONTEND-COMPONENTIZATION-005 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-002, T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005 |
| REQ-FRONTEND-COMPONENTIZATION-006 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-005, T-FRONTEND-COMPONENTIZATION-007 |
| REQ-FRONTEND-COMPONENTIZATION-007 | US-FRONTEND-COMPONENTIZATION-005 | T-FRONTEND-COMPONENTIZATION-002, T-FRONTEND-COMPONENTIZATION-006, T-FRONTEND-COMPONENTIZATION-007 |
| REQ-FRONTEND-COMPONENTIZATION-008 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-006, T-FRONTEND-COMPONENTIZATION-007 |

## API Guide 决策

API guide 省略。本切片仅前端，不新增或修改后端端点、request/response payload、持久化、adapter contract、provider/runtime 行为或部署行为。

## 验证计划

- `npm run agent:check-sdd -- --slice frontend-componentization`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test -- --run`
- `cd frontend && npm run build`
- `cd frontend && npm run e2e`
- `git diff --check`
- 对变更文件做 focused secret/private-path scan。
- 对变更文件做 focused new-network/dependency scan。
- `npm run agent:closeout`

## SDD 质量门

- 每个新增 SDD 产物都有英文与中文 companion。
- ID 跨语言一致。
- API guide 省略已记录在 requirements、data model、tasks 与 traceability。
- 明确排除 `vue-router`，除非 URL 语义、深链和浏览器前进/后退成为已接受产品能力。
- 已用 `review-doc-quality` 清单做自检；用户接受前未发现 critical SDD blocker。

## 延后工作

- URL 语义、深链、浏览器前进/后退行为和 route guards。
- Pinia/Vuex/global store。
- 视觉重设计。
- 新后端/API 契约。
- 生产 auth/RBAC、provider/runtime、storage/vector/model 变更。
- 真实公司数据摄取。
