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
