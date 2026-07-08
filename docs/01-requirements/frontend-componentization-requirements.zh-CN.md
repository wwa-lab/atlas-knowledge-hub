# 需求：Frontend Componentization

## 状态

待 SDD 验收草案。切片：`frontend-componentization`。阶段：Phase 1 前端结构加固。

## 目标

将单体 Vue 3 `frontend/src/App.vue` 拆成特性组件与 composables，同时保持当前产品行为、视觉语义、API 用法、mock/sample-safe 约束和稳定测试选择器不变。

## 范围

### 范围内

- 对 `frontend/src/App.vue` 做行为不变的结构性拆分。
- 为当前产品外壳建立按特性组织的 Vue 组件：导航、首页、全局 Chat、空间详情、Documents、连接器、审核/恢复、Wiki、Graph、Settings 与共享 UI 原语。
- 当能降低 `App.vue` 复杂度且不改变行为时，将现有状态组和副作用抽为 composables 或聚焦 helper。
- 继续通过 `frontend/src/api.ts` 使用现有 API helper；不新增后端端点或 API 契约。
- 继续通过 `frontend/src/data/atlasMock.ts` 使用现有 mock/sample fallback；不引入真实公司文档。
- 增加或调整聚焦测试，证明拆出的组件/composables 保持当前行为和选择器稳定。

### 范围外

- `vue-router`、URL 语义、深链、路由守卫或浏览器前进/后退行为。
- 新产品能力、新 UX 流程或可见重设计。
- 引入 Pinia/Vuex/global store。
- 新后端/API 行为、新持久化、新迁移或新 adapter/runtime 行为。
- 新外部网络调用、云服务、生产 provider 调用或真实公司数据。
- 替换现有 Playwright 验收语义。

## 需求

| ID | Requirement | Priority |
|---|---|---|
| REQ-FRONTEND-COMPONENTIZATION-001 | 实现必须保持当前 Vue 产品路径的所有用户可见行为不变，同时将 `App.vue` 从单体组件拆成聚焦组件/composables。 | Must |
| REQ-FRONTEND-COMPONENTIZATION-002 | 实现必须保留当前基于 `productView`、`activeSpaceTab`、`settingsOpen` 和 `settingsPanel` 的内存导航语义；不得引入 `vue-router` 或 URL 驱动导航。 | Must |
| REQ-FRONTEND-COMPONENTIZATION-003 | 拆出的组件必须保留当前单元测试与 Playwright 使用的稳定 `data-testid` 选择器，除非在同一任务中明确记录等价替换并同步更新测试。 | Must |
| REQ-FRONTEND-COMPONENTIZATION-004 | 拆出的代码必须继续只通过 `frontend/src/api.ts` 这个现有前端 API 边界调用 Atlas API；禁止前端直连 parser、converter、model、vector、storage、provider 或 runtime 引擎。 | Must |
| REQ-FRONTEND-COMPONENTIZATION-005 | 组件边界必须保留产品 UI 中已有的 source trace、confidence、review status、safe error、capability 与 disabled/coming-soon 展示规则。 | Must |
| REQ-FRONTEND-COMPONENTIZATION-006 | 本切片必须保持现有 mock/sample-safe 数据约束，不引入真实公司文档、截图、凭据、日志、私有路径或明文 secret。 | Must |
| REQ-FRONTEND-COMPONENTIZATION-007 | 实现必须为拆出的 helper、composable 和带行为的组件增加或更新聚焦测试；现有 frontend unit 与 E2E 套件必须继续通过。 | Must |
| REQ-FRONTEND-COMPONENTIZATION-008 | 实现必须保持后端、数据库、adapter、部署和 provider-backed E2E 行为不变。 | Must |

## 约束

- 本切片不引入 `vue-router`。
- 除非后续已接受的 SDD 修订明确加入，否则不新增依赖。
- 不改变后端/API 契约；API guide 有意省略。
- 不新增外部网络调用或 provider 集成。
- 不做可见重设计；只有为保持拆分后的现有布局才允许改 CSS。
- 保持实现小而可评审；如果某个组件拆分过宽，应拆成更小任务。

## 假设

- 本切片完成后，`frontend/src/App.vue` 仍是根组合外壳，但应变薄为编排层。
- 现有 `frontend/src/api.ts`、`frontend/src/types.ts` 与 `frontend/src/data/atlasMock.ts` 分别继续作为 API 访问、共享类型和 mock/sample 数据边界。
- 当前测试描述了必须保留的行为；高风险拆分处如果缺少测试，应补充测试。

## 未决问题

- 无阻塞项。未来 URL 语义、深链与浏览器历史属于独立产品决策。
