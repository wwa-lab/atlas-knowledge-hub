# 数据流：Knowledge Space

## 状态

草稿。此中文 companion 描述当前 Phase 1 / Product Goal Batch 1 的 mock 数据流。

## Phase A 首页与设置流

1. 用户打开 Vite app。
2. Vue 默认进入 `atlas` 产品体验。
3. Product shell 渲染持久侧边栏和首页知识空间库。
4. 用户点击侧边栏 Chat，进入 Global Chat。
5. 用户选择/取消知识空间，`selectedChatSpaces` 以不可变数组更新。
6. 用户点击设置快捷入口，设置浮层覆盖当前视图。
7. 用户关闭设置，返回之前的产品视图。

## Phase B 详情页流

1. 用户点击 `IBM i Modernization` 卡片。
2. Vue 将 `productView` 切换为 `space`，`selectedProductSpaceId` 设置为该空间。
3. 默认 `activeSpaceTab` 为 `wiki`。
4. 用户切换 `docs`、`review`（Processing Center）、`wiki`、`graph` 标签。
5. 每个标签读取当前 mock 状态并渲染真实 Vue 内容，不进入 iframe 或 workbench。

## 质量门禁流

1. Documents 展示文件与解析/转换状态。
2. Processing Center 汇总 parse failures、OCR required、low confidence、missing source_trace、review required、ready to publish。
3. Wiki/Graph/Global Chat 仅展示或使用已审核、已发布或明确可溯源的 mock 内容。
4. 被 Processing Center 阻塞的问题以文案和状态提示排除原因。

## 安全流

- API key 输入只在编辑态短暂存在，保存后清空并只保留状态。
- 当前 Phase A/B 不发送模型请求，不读取真实上传文件，不调用外部服务。
