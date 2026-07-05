# 用户故事：Knowledge Space

## 状态

草稿。此中文 companion 与 `knowledge-space` 历史 SDD 及 Product Goal Batch 1 Phase A/B 对齐。

## 角色

- SME：验证解析后的知识并批准发布。
- Developer：检查技术 source trace、依赖和图谱关系。
- BA：组织业务概念和需求证据。
- Delivery Lead：跟踪批处理进度、审核就绪度和知识覆盖。
- Knowledge Consumer：基于可信知识提问。

## 用户故事

### US-KS-001：浏览知识空间

作为交付负责人，我希望看到知识空间卡片，以便快速了解文档量、Wiki 就绪度、审核工作量、所有者和健康状态。

验收标准：

- Given 我打开 Atlas，when 首页加载，then 我看到知识空间库和知识空间卡片。
- Given 某个空间存在审核任务，when 我查看卡片，then 状态能清楚提示需要审核。
- Given 我点击 `IBM i Modernization`，when 操作完成，then 真实 Vue 详情页打开。

### US-KS-002：基于选择的知识上下文提问

作为知识消费者，我希望选择一个或多个知识空间并提问，以便从工作区级 Chat 开始使用 Atlas。

验收标准：

- Given 我打开 Global Chat，when 页面显示，then 我能识别已选择的知识空间上下文。
- Given 我选择或取消选择知识空间，then 输入区域附近的选择数量更新。
- Given 内容被 Processing Center 阻塞，then 它不会进入 mock Chat 上下文。

### US-KS-003：导航知识空间详情页

作为项目用户，我希望详情页包含 Documents、Processing Center、Wiki 和 Graph 标签，以便在同一上下文中完成知识工作流。

验收标准：

- Given 我进入 `IBM i Modernization`，then 我看到面包屑和返回首页按钮。
- Given 我打开该空间，then 默认标签为 Wiki。
- Given 我点击任一标签，then 只有该标签内容成为 active，且仍保持当前空间上下文。

### US-KS-004：跟踪批处理状态

作为交付负责人，我希望看到批次计数、进度和文件状态，以便理解转换和审核就绪度。

验收标准：

- Given Documents 标签打开，then 我看到上传文件夹和上传 ZIP 入口。
- Given 批次状态显示，then 我看到总文件、PDF converted、Markdown generated、review required 和 failed 等状态。
- Given 文件有解析/转换状态，then 状态标签显示在对应 source path 附近。

### US-KS-005：阅读可溯源 LM Wiki

作为 SME，我希望阅读带 source trace 和 confidence 的标准化 Wiki 内容，以便判断生成知识是否可信。

验收标准：

- Given 我打开 Wiki，then 我看到页面索引、内容和元数据。
- Given 内容来自生成或不确定来源，then review status 与 confidence 保持可见。
- Given source trace 存在，then 页面显示来源文件、页码或 section 信息。

### US-KS-006：探索知识图谱

作为开发者，我希望查看 Wiki Page、Entity、Concept、Document 和 Review Required 节点，以便检查关系和依赖。

验收标准：

- Given 我打开 Graph，then 我看到节点、图例和详情区域。
- Given 我点击节点，then 详情面板展示节点解释与 evidence/source trace。
- Given 关系缺少证据，then 它不能被当成已验证关系。

### US-KS-007：查看处理中心门禁

作为 SME，我希望 Processing Center 告诉我哪些问题阻塞发布，以便优先处理解析失败、OCR、低置信和缺失溯源。

验收标准：

- Given 我打开 Processing Center，then 我看到批量质量指标。
- Given 某个队列有问题，then 我看到来源、类型、数量、状态和 mock-safe 操作。
- Given issue 未解决，then 页面说明它会阻塞 Wiki、Graph 或 Ask 可用性。

### US-KS-008：带证据提问

作为知识消费者，我希望答案包含来源引用和 confidence，以便能验证回答。

验收标准：

- Given 我打开 Global Chat，then 我看到知识空间选择和问题输入。
- Given mock answer 显示，then 它包含 evidence references。
- Given 内容未审核且低置信，then 它被排除或明确标记为不可用。

### US-KS-009 至 US-KS-017

这些故事覆盖语言切换、day/night mode、模型管理、注册、成员管理、账户设置、引擎配置、侧边栏设置浮层和响应式使用。它们与英文文档中的 `US-KS-009` 至 `US-KS-017` 保持相同 ID、范围和验收意图；当前 Batch 1 仅验证 Phase A/B 相关的真实 Vue 产品主路径、Global Chat、设置浮层入口和知识空间详情页。
