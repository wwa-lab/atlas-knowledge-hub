# 设计：Frontend Componentization

## 状态

待 SDD 验收草案。

## 设计原则

组件化对产品用户应当是不可见的，对维护者应当是清楚的。可见 Atlas UI 不应改变；内部形态应变得按特性组织、可测试、易评审。

## 需求覆盖

- REQ-FRONTEND-COMPONENTIZATION-001
- REQ-FRONTEND-COMPONENTIZATION-002
- REQ-FRONTEND-COMPONENTIZATION-003
- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-007
- REQ-FRONTEND-COMPONENTIZATION-008

## 目标组件映射

| Current Region In `App.vue` | Proposed Component Boundary | Notes |
|---|---|---|
| Product page wrapper and sidebar | `layout/ProductShell.vue`, `layout/ProductSidebar.vue` | 保留 `vue-product-page` 和导航 actions。 |
| Home library and create space | `home/HomeView.vue`, `home/CreateSpacePanel.vue` | 保留 create-space selectors/status/errors。 |
| Global chat | `chat/GlobalChatView.vue` | 保留 selected spaces、question、Ask answer、quality chips。 |
| Space header and tabs | `space/SpaceDetailView.vue`, `space/SpaceTabs.vue` | 持有 tab buttons；不使用 router。 |
| Documents workflow | `documents/DocumentsTab.vue` 加更小 list/form/report components | 保留 API metadata、manual URL、mock upload、batch report。 |
| Connector sync | `connectors/ConnectorSyncTab.vue` | 保留 mock/local connector sync 展示。 |
| Review and recovery | `review/ReviewTab.vue`, `review/DeadLetterPanel.vue` | 保留 queue、approve、retry、acknowledge flows。 |
| Wiki | `wiki/WikiTab.vue` | 保留 publish action、page detail、issues。 |
| Graph | `graph/GraphTab.vue` | 保留 search、selected node、evidence detail。 |
| Settings modal | `settings/SettingsModal.vue` 加 panel components | 保留 panel navigation 与当前 capability gates。 |
| Repeated badges/cards/lists | `shared/*` | 只放小型展示原语。 |

实现可调整文件名，但应能识别同样的边界。

## 交互规则

- 产品导航按钮 emit actions；根组件更新 `productView`。
- 空间 tab 按钮 emit tab changes；根组件或 `SpaceDetailView` 更新 `activeSpaceTab`。
- Settings navigation emit panel changes；modal open/close 继续由状态驱动。
- 组件不隐式 mutate parent state；状态变化通过 props/events 或显式 composable actions。
- 现有 disabled states 与 capability checks 保持可见且有效。

## 选择器规则

- 现有 E2E/unit 选择器继续出现在渲染 DOM 中。
- 如果 markup 移动到组件中，选择器跟随同一个可见元素移动。
- 如果因语义元素变化必须修改选择器，则任务必须记录等价选择器，并在同一 commit 更新测试。

## 样式规则

- 复用 `frontend/src/styles.css` 中的现有 classes。
- 不创建新的视觉系统。
- 抽取期间避免 CSS churn。
- 保持响应式行为和稳定尺寸。
- 不添加与当前产品表面无关的装饰 UI。

## 测试设计

| Test Area | Expected Coverage |
|---|---|
| Pure domain helpers | 对从 `App.vue` 拆出的 mapping/formatting 做单元测试。 |
| Composables | 在可行处测试状态迁移、API sequencing、loading/error 行为。 |
| Components | 对带行为的拆出 views 做 component 或 App-level tests。 |
| App integration | 保持现有 `App.test.ts` 行为覆盖；只有有帮助时才拆分。 |
| E2E | 现有 Playwright flows 保持稳定；对改动 surface 跑聚焦 specs，并在 closeout 前跑完整 frontend E2E。 |

## 手动评审清单

- Home、Chat、Space Detail、Documents、Connectors、Review、Wiki、Graph 与 Settings 外观和行为保持不变。
- 内部导航期间 URL 不变化。
- Source trace/confidence/review status 继续可见。
- Masked secrets 继续脱敏。
- Coming-soon 与 disabled controls 继续不可操作。
- 不出现新的后端/API/provider 行为。
