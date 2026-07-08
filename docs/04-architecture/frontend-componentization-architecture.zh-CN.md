# 架构：Frontend Componentization

## 状态

待 SDD 验收草案。

## 架构意图

当前 Vue app 的产品方向是对的，但应用启动、API workflows、computed view models、settings 逻辑、mock upload 行为和整个 template 都集中在 `frontend/src/App.vue`。本切片引入前端结构，不改变运行时架构。

## 需求覆盖

- REQ-FRONTEND-COMPONENTIZATION-001
- REQ-FRONTEND-COMPONENTIZATION-002
- REQ-FRONTEND-COMPONENTIZATION-003
- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-007
- REQ-FRONTEND-COMPONENTIZATION-008

## 建议前端模块形态

```text
frontend/src/
  App.vue
  api.ts
  types.ts
  data/atlasMock.ts
  components/
    layout/
    home/
    chat/
    space/
    documents/
    connectors/
    review/
    wiki/
    graph/
    settings/
    shared/
  composables/
  domain/
```

实现期间可调整具体文件数量，但特性所有权必须清晰，任何拆出的组件都不应成为第二个单体。

## 所有权边界

| Boundary | Responsibility | Must Not Own |
|---|---|---|
| `App.vue` | 根启动、顶层编排、跨特性状态接线。 | 大型特性 template 或直连 engine/provider。 |
| `components/layout` | 产品外壳布局、sidebar navigation、modal 挂载。 | 特性业务 workflow。 |
| `components/home` | Knowledge Space 首页卡片与创建空间面板。 | 空间详情 tabs 或 settings 内部。 |
| `components/chat` | 全局多空间 Trusted Ask surface。 | 后端 API client 实现。 |
| `components/space` | 空间 header 与 tab shell。 | Documents/Wiki/Graph 内部。 |
| `components/documents` | Upload metadata UI、manual URL form、batches/files/chunks 展示、mock upload report。 | Parser/converter/storage runtime 调用。 |
| `components/connectors` | Connector sync registry/run/item UI。 | 真实 connector provider auth/fetching。 |
| `components/review` | Processing center、review queues、dead-letter recovery UI。 | 后端 worker 实现。 |
| `components/wiki` | Wiki index/page/issues/publish controls。 | Markdown generation 后端逻辑。 |
| `components/graph` | Graph list/detail/search/evidence 展示。 | Graph projection engine。 |
| `components/settings` | General、profile、space info、members、audit、API、messages、model、vector/parser/storage panels。 | 明文 secrets 或生产 provider 行为。 |
| `components/shared` | 小型可复用展示原语，不隐藏 domain side effects。 | 跨特性编排。 |
| `composables` | 为现有 workflow 提供共享 Vue state/effect 逻辑。 | 新产品行为或新 API 契约。 |
| `domain` | 从 `App.vue` 拆出的纯 mapping/formatting helpers。 | Vue side effects 或网络调用。 |

## 依赖规则

- 组件可以导入共享类型和纯 domain helpers。
- 组件可以 emit events 或接收 action callbacks。
- Composables 可以调用 `frontend/src/api.ts` 中的现有函数。
- 如果已有 API helper，组件/composables 不得直接调用 `fetch`。
- 组件/composables 不得新增后端 URL、provider URL、engine commands 或私有文件系统路径。
- Shared components 必须保持展示性，除非行为确实是通用的。

## 路由决策

本切片有意排除 `vue-router`。当前应用有 product views 和 tabs，但尚未接受以下产品需求：

- 可通过 URL 定位的产品状态。
- Knowledge Space/tab 深链。
- 浏览器前进/后退语义。
- Route-level guards。

这些能力需要独立需求和验收标准。

## 风险控制

- 按垂直小步抽取，每阶段保持测试绿色。
- 跨组件边界保留 `data-testid` 选择器。
- 第一轮拆分优先 props/events；只有状态所有权更清晰时才引入 composables。
- 除非为保持布局需要小范围修复，否则保持 CSS class names 稳定。
- 为带行为的拆出逻辑增加测试，尤其是 settings、upload/review/publish、graph selection 与 model state。

## 备选方案

| Alternative | Decision | Reason |
|---|---|---|
| 前端大爆破重写 | 拒绝 | 视觉/行为漂移风险高，且违反项目变更纪律。 |
| 现在引入 `vue-router` | 拒绝 | 用户明确将本切片限定为行为不变的结构拆分；URL 语义尚不是产品需求。 |
| 引入 Pinia/Vuex | 拒绝 | 超出当前重构需要，增加新架构面。 |
| 所有状态留在 `App.vue`、只拆 template | 只允许作为早期步骤 | 对低风险拆分有用，但最终应在合适处隔离非平凡 helpers/composables。 |
