# 设计：Knowledge Space

## 状态

草稿。此中文 companion 与当前 Phase 1 IA 基线及 Product Goal Batch 1 Phase A/B 对齐。

## 体验原则

- 真实 Vue 产品页是主路径，静态原型和 iframe 仅作为参考/调试入口。
- Atlas 是企业知识产品，不是脚本或营销页。
- 信息密度高、工作台式、可扫描，避免大幅 hero 和装饰性卡片堆叠。
- 可信度、source trace、confidence、review status 要靠近生成知识。
- Global Chat 易进入，但受 Processing Center 的质量门禁约束。
- 设置浮层不打断当前工作上下文。

## 首页

- 左侧为持久侧边栏，包含知识库、智能体、共享空间、对话、近期项和设置快捷入口。
- 主区域标题为 `知识库`，展示知识空间卡片网格。
- `IBM i Modernization` 卡片是 Phase B 的主入口。
- 卡片应保持紧凑、可扫描，展示名称、描述、文档数量、能力图标和所有者。
- 默认首页不得使用 iframe。

## Global Chat

- 从侧边栏 `对话` 进入。
- 页面中心是 Chat prompt 和知识空间选择控件。
- 已选择知识空间数量显示在输入工具栏。
- 选择/取消知识空间不离开 Chat 视图。
- 页面文案说明未通过质量门禁的内容不会进入索引。

## 知识空间详情

- 顶部显示面包屑、空间标题、描述、mock 状态摘要和返回首页按钮。
- 主要操作包含上传文件夹和上传 ZIP。
- 标签：`文档`、`处理中心`、`Wiki`、`图谱`。
- 默认进入 Wiki。
- 标签切换应保持当前空间上下文，不触发真实上传或外部调用。

## Documents

- 使用分类侧栏 + 文件/状态表格。
- 文件状态要能展示成功、需审核、转换失败等路径。
- 当前上传入口为 mock action；真实文件读取推迟到后续 slice。

## Processing Center

- 使用指标卡展示总量、解析失败、缺失 source_trace、需审核等质量状态。
- 队列行展示 issue 名称、类型、来源、数量、状态和 mock action。
- 设计目标是解释“为什么不能进入 Wiki/Graph/Ask”，而不是只显示计数。

## Wiki

- 使用左侧页面/索引导航 + 右侧内容区。
- 默认内容为 `IBM i Modernization Index`。
- source_trace block 必须突出来源文件、页码/section、review status 和 confidence。

## Graph

- 图谱在详情页内显示。
- 可以使用轻量 canvas/SVG/mock 节点布局。
- Detail panel 必须展示 relationship 或节点的 evidence/source trace 解释。

## 设置浮层

- 设置覆盖当前产品视图，以两栏布局显示：左侧设置导航，右侧内容。
- 空间信息位于“空间”分组，使用行式详情布局展示空间 ID、名称、描述、状态、创建时间、存储配额、已使用存储和使用率。
- 空间状态使用紧凑 badge；空间名称和描述提供小型编辑入口，保存/取消为行内操作。
- 空间信息编辑只更新当前 Vue mock 会话，不调用真实元数据接口；未来生产写入、RBAC 和审计必须由后端承担。
- 模型管理展示 Add Model、内置模型说明、类别筛选和模型卡片。
- API key 编辑流程只保存状态，不展示明文。
- 消息管理位于设置导航中，展示标题说明、`启用消息索引` 开关，以及 `索引统计` 空态；启用后可展示 mock-safe 的 Embedding 模型和索引统计。
- 消息管理的视觉重点是解释聊天历史语义搜索的配置状态，而不是执行真实 embedding、向量写入或 provider 调用。
- 其他设置 panel 当前可以是真实 Vue 结构占位，但必须清楚说明 mock/production 边界。

## 响应式与验证

- 桌面宽度下侧边栏与主内容并排。
- 卡片、详情内容、设置浮层使用响应式网格或堆叠布局。
- 截图证据至少覆盖 Phase A 首页和 Phase B 详情页。
- E2E 必须覆盖首页、Global Chat、设置浮层、空间详情和四个标签切换。
