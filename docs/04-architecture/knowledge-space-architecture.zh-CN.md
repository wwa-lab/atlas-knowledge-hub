# 架构：Knowledge Space

## 状态

草稿。此中文 companion 覆盖当前 `knowledge-space` Phase 1 前端架构和 Product Goal Batch 1 Phase A/B。

## 架构边界

- 当前主产品路径是 `frontend/src/App.vue` 中的真实 Vue 3 + TypeScript 产品页。
- `frontend/public/atlas-prototype.html` 和 `prototypes/index.html` 保留为视觉/交互参考，不是默认验收路径。
- 当前阶段允许 mock/sample 数据和浏览器内状态。
- 后端、数据库、认证、生产持久化和真实 provider 调用不在 Batch 1 范围内。

## 前端组件责任

- Product shell：负责侧边栏、主视图切换、设置浮层和全局布局。
- Home library：负责知识空间卡片和入口。
- Global Chat：负责工作区级多知识空间上下文选择和 mock 问答入口。
- Space detail：负责 `IBM i Modernization` 的详情页、面包屑、上传入口和标签状态。
- Documents / Processing Center / Wiki / Graph：负责详情页内四个真实 Vue 标签内容。
- Settings overlay：负责设置导航、模型管理和其他设置 panel 的 mock/production 边界说明。

## 适配器与 API 边界

- Batch 1 不新增后端/API contract。
- 后续 parser、converter、model、vector、storage、search 集成必须通过产品侧适配器。
- 模型 API key 不得进入前端持久化；当前只允许 configured/not_configured 状态。

## 数据安全

- 使用 mock/sample 数据。
- 不读取真实文件内容。
- 不提交真实公司资料、截图、日志、凭证、私有路径或机密数据。
- 不新增外部网络调用或外部运行时依赖。

## 验证架构

- 单元/组件测试验证真实 Vue product shell 行为。
- Playwright 验证 Phase A/B 真实产品路径、设置浮层、Global Chat 和详情页标签。
- 截图证据存放于 `docs/00-context/evidence/`。
