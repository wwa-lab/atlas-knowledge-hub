# 用户故事：Frontend Componentization

## 状态

待 SDD 验收草案。来源于 `frontend-componentization` 需求。

## 用户故事

### US-FRONTEND-COMPONENTIZATION-001：拆分期间保持产品行为

作为 Atlas 用户，我希望组件化之后产品外壳行为保持一致，这样重构不会干扰当前可评审工作流。

验收标准：

1. 给定前端打开 Atlas 产品路径，则首页、全局 Chat、空间详情、tabs、settings 与 mock/sample-safe fallback 状态按拆分前相同行为渲染。
2. 给定用户执行现有已测流程，则现有稳定选择器仍可用。
3. 给定展示 API 或 mock fallback 状态，则 loading、empty、disabled 与 safe-error 行为保持不变。

### US-FRONTEND-COMPONENTIZATION-002：保留内存导航

作为产品负责人，我希望本切片保留当前内存导航而不是新增 URL routing，这样结构清理不会创造未经批准的产品语义。

验收标准：

1. 给定用户在 Home、Global Chat 与 Space Detail 之间移动，则导航仍由 Vue state 控制，而不是由 `vue-router` 控制。
2. 给定用户切换空间 tab，则 tab 选择仍使用本地组件状态，不改变浏览器 URL。
3. 给定浏览器前进/后退和深链不属于本切片，则不创建新 route 契约。

### US-FRONTEND-COMPONENTIZATION-003：安全抽取特性组件

作为前端维护者，我希望把大型 UI 区域抽成聚焦组件，这样后续产品修改不需要继续编辑一个 5k 行文件。

验收标准：

1. 给定组件化完成，则 `App.vue` 将主要区域委托给特性组件。
2. 给定某个组件拥有带行为的 UI，则通过 props/events 或 composables 明确依赖。
3. 给定拆分分阶段进行，则每个阶段都可评审且有聚焦验证路径。

### US-FRONTEND-COMPONENTIZATION-004：保留 API 与 adapter 边界

作为架构负责人，我希望拆出的组件使用同一个前端 API 边界，这样组件化不会绕过 Atlas 后端/适配器。

验收标准：

1. 给定组件需要后端数据，则它从 `App.vue` 或使用 `frontend/src/api.ts` 的 composable 接收数据/actions。
2. 给定需要 parser、converter、model、vector、storage、provider 或 runtime 行为，则前端仍只通过现有后端 API 触达。
3. 给定本切片不需要后端/API 行为变化，则不生成 API implementation guide。

### US-FRONTEND-COMPONENTIZATION-005：测试拆出行为

作为维护者，我希望为拆出的组件和 composables 增加聚焦测试，这样重构能防止细微行为漂移。

验收标准：

1. 给定从现有非平凡逻辑中拆出 helper/composable，则用聚焦单元测试覆盖该行为，或将已有覆盖随之迁移。
2. 给定抽取带行为的组件，则在风险需要处测试关键状态和 emitted actions。
3. 给定完整前端验证套件运行，则 typecheck、unit tests、build 和相关 E2E 通过；若有阻塞检查，需报告原因。
