# 溯源：Frontend Componentization

## 切片契约

- Slice: `frontend-componentization`
- Goal: 对单体 Vue `App.vue` 做行为不变的结构性拆分。
- Phase: 1 前端结构加固。
- Status: 2026-07-08 已接受 SDD；T-FRONTEND-COMPONENTIZATION-001 至 T-FRONTEND-COMPONENTIZATION-011 已实现并完成本地验证，包含 `frontend-state-extraction` 追加工作。本切片是行为不变的前端结构化 checkpoint，不是 production-readiness 声明。

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
- `frontend/src/composables/useSpaces.ts`
- `frontend/src/composables/useBatches.ts`
- `frontend/src/composables/useReviewQueue.ts`
- `frontend/src/composables/useWikiPages.ts`
- `frontend/src/composables/useGraph.ts`
- `frontend/src/composables/useAsk.ts`
- `frontend/src/composables/useSettings.ts`
- `frontend/src/composables/useGlobalChat.ts`
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
| REQ-FRONTEND-COMPONENTIZATION-009 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-008, T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-010 |
| REQ-FRONTEND-COMPONENTIZATION-010 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-010 |
| REQ-FRONTEND-COMPONENTIZATION-011 | US-FRONTEND-COMPONENTIZATION-005 | T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-011 |

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

## 实现证据

| Task IDs | Status | Evidence |
|---|---|---|
| T-FRONTEND-COMPONENTIZATION-001 | Completed | 用户已于 2026-07-08 接受 SDD；实现前 `npm run agent:check-sdd -- --slice frontend-componentization --report docs/00-context/frontend-componentization-sdd-completion-report.md` 已通过。 |
| T-FRONTEND-COMPONENTIZATION-002 | Completed | 新增 `frontend/src/domain/viewModels.ts` 与 `frontend/src/domain/viewModels.test.ts`；抽取 Ask governance labels、metric ratios、Wiki/Graph source trace formatting、graph/model labels 和 secret status labels 等纯 view-model 类型与 helpers。 |
| T-FRONTEND-COMPONENTIZATION-003 | Completed | 新增 `ProductShell`、`ProductSidebar`、`ProductHomeView` 与 `GlobalChatView`；`App.vue` 保留 `productView` 与 handler 所有权，通过类型化 props/events 委托 shell/home/chat 模板。 |
| T-FRONTEND-COMPONENTIZATION-004 | Completed | 新增 `SpaceDetailView`、`SpaceDocumentsTab`、`SpaceConnectorsTab`、`SpaceReviewTab`、`SpaceWikiTab` 与 `SpaceGraphTab`。`App.vue` 仍拥有 `activeSpaceTab`、选中 ID、表单 draft、API 调用和 handler 副作用，Space 组件通过类型化 props/events 接线。 |
| T-FRONTEND-COMPONENTIZATION-005 | Completed | 新增 `SettingsModal`、带行为的 settings panel components 与 `ModelEditor`。`App.vue` 仍拥有 `settingsOpen`、`settingsPanel`、capability gates、表单状态、API/model 副作用和脱敏展示状态，settings 组件通过类型化 props/events 接线。 |
| T-FRONTEND-COMPONENTIZATION-006 | Completed | 新增 `frontend/src/composables/useProductUploadWorkflow.ts`，并为 mock upload/processing/report workflow 增加 focused tests。`App.vue` 仍拥有产品导航并传入 documents-tab callback；没有改变 API helper usage、后端契约、新依赖或 runtime 行为。 |
| T-FRONTEND-COMPONENTIZATION-007 | Completed | 已运行最终 frontend regression、Atlas workflow gates、closeout gate、diff hygiene、dependency scan、focused new-network/private-path scan 与 focused secret scan。由于本切片未触碰 backend/API/provider runtime 行为，未运行 backend/provider checks。 |
| T-FRONTEND-COMPONENTIZATION-008 | Completed | 已为 `frontend-state-extraction` 扩展双语 spec/tasks，覆盖八个领域 composables、显式 props/events 数据流、工厂函数规则、App.vue 行数目标，以及不引入 Pinia/router/provide-inject 的约束。 |
| T-FRONTEND-COMPONENTIZATION-009 | Completed | 新增工厂 composables：`useSpaces`、`useBatches`、`useReviewQueue`、`useWikiPages`、`useGraph`、`useAsk`、`useSettings` 与 `useGlobalChat`，API 调用仍经由 `frontend/src/api.ts`。Connector sync 保留在 batches/workflow 领域，避免引入第九个状态域。 |
| T-FRONTEND-COMPONENTIZATION-010 | Completed | 已将 `App.vue` 重接线为薄编排外壳，并抽出 `P0WorkbenchView` 与 `SettingsOrchestrator`；`wc -l frontend/src/App.vue` 为 581 行。现有子组件 props/events 与 E2E 选择器保持。 |
| T-FRONTEND-COMPONENTIZATION-011 | Completed | 已为每个拆出的状态 composable 增加 focused tests，并保留 `App.test.ts` 作为渲染编排/用户流程覆盖。完整 frontend lint、typecheck、unit、build 与 E2E gates 已通过。 |

2026-07-08 已运行验证：

- `npm --prefix frontend run typecheck` 通过。
- `npm --prefix frontend run test -- --run` 通过，5 个文件 / 30 个测试。
- `npm --prefix frontend run build` 通过。
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/full-stack-productization.spec.ts --project=chromium` 通过，2 个测试。
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/full-stack-productization.spec.ts --project=chromium` 在 Space tab-content 抽取后通过，3 个测试。
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-h-settings-administration.spec.ts --project=chromium` 在 settings 抽取后通过，2 个测试。
- `npm --prefix frontend run test -- --run src/composables/useProductUploadWorkflow.test.ts` 在 mock upload workflow composable 抽取后通过，1 个文件 / 2 个测试。
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts --project=chromium` 在 mock upload workflow composable 抽取后通过，2 个测试。
- `npm --prefix frontend run e2e` 通过，16 个测试。
- `npm run agent:check-sdd -- --slice frontend-componentization --report docs/00-context/frontend-componentization-sdd-completion-report.md` 通过，保留预期的 frontend-only optional API guide warning。
- `npm run agent:check-workflow -- --changed-slices` 通过。
- `npm run agent:closeout` 通过。
- `git diff --check` 通过。
- Focused dependency、new-network/private-path 与 secret scans 通过。

2026-07-08 追加 `frontend-state-extraction` 验证：

- `npm run agent:check-sdd -- --slice frontend-componentization` 通过，保留预期的 frontend-only optional API guide warning。
- 每个领域抽取后 focused checks 已通过：
  - `npm --prefix frontend run test -- --run src/composables/useSpaces.test.ts` 与 `npm --prefix frontend run typecheck`。
  - `npm --prefix frontend run test -- --run src/composables/useBatches.test.ts` 与 `npm --prefix frontend run typecheck`。
  - `npm --prefix frontend run test -- --run src/composables/useReviewQueue.test.ts` 与 `npm --prefix frontend run typecheck`。
  - `npm --prefix frontend run test -- --run src/composables/useWikiPages.test.ts` 与 `npm --prefix frontend run typecheck`。
  - `npm --prefix frontend run test -- --run src/composables/useGraph.test.ts` 与 `npm --prefix frontend run typecheck`。
  - `npm --prefix frontend run test -- --run src/composables/useAsk.test.ts` 与 `npm --prefix frontend run typecheck`。
  - `npm --prefix frontend run test -- --run src/composables/useGlobalChat.test.ts` 与 `npm --prefix frontend run typecheck`。
  - `npm --prefix frontend run test -- --run src/composables/useSettings.test.ts` 与 `npm --prefix frontend run typecheck`。
- `npm --prefix frontend run lint` 通过。
- `npm --prefix frontend run typecheck` 通过。
- `npm --prefix frontend run test` 通过，14 个文件 / 56 个测试。
- `npm --prefix frontend run build` 通过。
- `npm --prefix frontend run e2e` 通过，16 个测试。
- `wc -l frontend/src/App.vue` 为 581 行。
- `rg -n "\bany\b|as any" frontend/src` 无命中。

跳过：

- Backend/API/provider checks，因为本 frontend-only 切片没有改变后端端点、request/response payload、持久化、adapter contract、provider/runtime 行为或部署行为。

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
