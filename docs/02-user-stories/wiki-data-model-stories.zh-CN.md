# 用户故事：Wiki 数据模型

## 状态

待用户接受的草案。由 `wiki-data-model` requirements 派生。

## 故事索引

| Story | Title | Requirements |
|---|---|---|
| US-WIKI-DATA-MODEL-001 | 编码前审阅 SDD 契约 | REQ-WIKI-DATA-MODEL-001 |
| US-WIKI-DATA-MODEL-002 | 用稳定 slug 和类型识别 Wiki 页面 | REQ-WIKI-DATA-MODEL-002, 003, 009 |
| US-WIKI-DATA-MODEL-003 | 保留可追溯 source 和 chunk refs | REQ-WIKI-DATA-MODEL-002, 012, 013 |
| US-WIKI-DATA-MODEL-004 | 浏览最小 Wiki folders | REQ-WIKI-DATA-MODEL-004, 008 |
| US-WIKI-DATA-MODEL-005 | 检查 generation runs、logs 和 issues | REQ-WIKI-DATA-MODEL-005, 006, 007, 008 |
| US-WIKI-DATA-MODEL-006 | 在 Vue 中安全查看新增 Wiki metadata | REQ-WIKI-DATA-MODEL-010, 011 |
| US-WIKI-DATA-MODEL-007 | 验证兼容性和安全门 | REQ-WIKI-DATA-MODEL-014, 015 |

## US-WIKI-DATA-MODEL-001: 编码前审阅 SDD 契约

作为产品负责人，
我希望完整双语 SDD 集先被接受，
以便 Wiki Foundation 范围不会漂移到 ingest、linkify、RBAC 或生产 provider 工作。

### 验收标准

1. **Given** 切片处于 SDD 模式
   **When** 产出 artifact set
   **Then** 每个必需英文文件都有 `.zh-CN.md` companion，并且 ID 一致。
2. **Given** 产品代码尚未开始
   **When** SDD 等待审阅
   **Then** implementation tasks 明确产品代码在用户接受前阻塞。

### 说明 / 假设

- 只要遵循项目 SDD 技能链，Codex 可以编写 SDD docs。

### 依赖

- `PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md` 和 `docs/00-context/sdd-profile.md`。

### 范围外

- SDD 接受前的产品代码变更。

### 待确认问题

- 无。

## US-WIKI-DATA-MODEL-002: 用稳定 slug 和类型识别 Wiki 页面

作为知识用户，
我希望 Wiki 页面具备稳定 slug 和 page type，
以便未来 Auto Wiki 流程不依赖展示标题也能定位和刷新页面。

### 验收标准

1. **Given** 某个 space 中存在已发布页面
   **When** 我在该 space 内按 slug 请求它
   **Then** API 只返回该 space 中匹配的页面 metadata。
2. **Given** 现有 publish/list 流程运行
   **When** 页面被创建或重新发布
   **Then** 现有 id/list API 仍返回该页面，并包含新增字段。
3. **Given** 两个 space 有相同 slug
   **When** slug lookup 按各自 space scoped
   **Then** 每个 space 只获得自己的页面。

### 说明 / 假设

- Slug 唯一性按 `space_id` 约束。

### 依赖

- 现有 review-publish Wiki APIs 和 `wiki_page` 表。

### 范围外

- 自动 slug 冲突解决 UI。

### 待确认问题

- OQ-WIKI-DATA-MODEL-003。

## US-WIKI-DATA-MODEL-003: 保留可追溯 source 和 chunk refs

作为 SME reviewer，
我希望每个 Wiki 页面都携带 source refs 和 chunk refs，
以便 review、graph 和 Ask evidence 能安全回指已批准知识。

### 验收标准

1. **Given** Wiki page response 包含 source refs
   **When** 前端渲染 metadata
   **Then** source refs 作为安全 ID/label 可见，而不是 raw document content。
2. **Given** 页面有 chunk refs
   **When** 后续 graph 或 Ask 流程消费 page metadata
   **Then** source trace、confidence、review status 仍可用。
3. **Given** legacy pages 只有 `sourceDocumentIds`
   **When** migration 应用
   **Then** 兼容值被 backfill 或安全默认化，没有数据丢失。

### 说明 / 假设

- 本切片只存引用 metadata；不 dereference raw files。

### 依赖

- 现有 `source_chunk`、review-publish、graph 和 Ask 契约。

### 范围外

- Link validation 和 stale-source checks。

### 待确认问题

- OQ-WIKI-DATA-MODEL-002。

## US-WIKI-DATA-MODEL-004: 浏览最小 Wiki folders

作为知识空间管理员，
我希望 Wiki folders 用简单层级组织页面，
以便未来 Wiki navigation 可建立在持久 metadata 上。

### 验收标准

1. **Given** 某个 space 有 folders
   **When** 我调用 folder list/tree endpoint
   **Then** 我收到该 space 范围内的 folders 和 parent-child 关系。
2. **Given** 现有 pages 没有 folder
   **When** migration 运行
   **Then** pages 仍可读取，且不会被强制放入不安全默认 folder。

### 说明 / 假设

- 本切片不建模 folder permissions。

### 依赖

- Knowledge Space metadata。

### 范围外

- Drag/drop ordering、inheritance、RBAC 和 folder write UI。

### 待确认问题

- OQ-WIKI-DATA-MODEL-003。

## US-WIKI-DATA-MODEL-005: 检查 generation runs、logs 和 issues

作为 delivery lead，
我希望检查 generation run metadata、Wiki logs 和 page issues，
以便未来 Auto Wiki maintenance 从一开始就有安全运营记录。

### 验收标准

1. **Given** generation run records 存在
   **When** 我列出某个 space 的 runs
   **Then** 我看到 status、source mode、安全 counts、timestamps，且没有 raw provider payload。
2. **Given** log entries 存在
   **When** 我列出 page logs
   **Then** 我看到有边界的生命周期事件，不含 secrets、raw content 或 private paths。
3. **Given** page issues 存在
   **When** 我列出 issues
   **Then** 我看到 type、severity、status 和安全 evidence references。

### 说明 / 假设

- 初始实现可以没有 rows 或只使用 sample-safe rows；本切片没有 rule engine 创建 issues。

### 依赖

- Wiki page 和 Knowledge Space records。

### 范围外

- 运行 generation、linkify、lint 或 repair workflows。

### 待确认问题

- OQ-WIKI-DATA-MODEL-001。

## US-WIKI-DATA-MODEL-006: 在 Vue 中安全查看新增 Wiki metadata

作为知识用户，
我希望 Wiki tab 展示新的 API-backed metadata，
以便区分 generated、published、manual 和 refreshable Wiki pages。

### 验收标准

1. **Given** API Wiki pages 包含新增字段
   **When** 我打开 Wiki tab
   **Then** slug、page type、aliases、refs、links、version、source mode、refresh policy 可见。
2. **Given** 没有 API pages 可用
   **When** 我打开 Wiki tab
   **Then** 确定性的 sample-safe fallback content 仍可用。
3. **Given** 现有 graph 和 Ask E2E flows 运行
   **When** Wiki metadata display 变化
   **Then** 这些流程不回退。

### 说明 / 假设

- 大数组可显示 counts，小样本集合可显示具体 ID。

### 依赖

- 前端 API types 和现有 Wiki tab state。

### 范围外

- 完整 Wiki editor 或 navigation redesign。

### 待确认问题

- 无。

## US-WIKI-DATA-MODEL-007: 验证兼容性和安全门

作为实现 reviewer，
我希望切片证明兼容性和安全性，
以便 Wiki Foundation 不削弱现有可信知识流程。

### 验收标准

1. **Given** 实现完成
   **When** verification 运行
   **Then** 报告 backend、frontend、E2E、second-layer、diff、secret/path、network/dependency checks。
2. **Given** adapter boundaries 存在
   **When** 实现被扫描
   **Then** 没有 parser/converter/model/vector/storage/search engine calls 被引入 Atlas adapter 边界之外。

### 说明 / 假设

- 任何 skipped check 必须说明原因。

### 依赖

- 已接受的 SDD 和任务清单。

### 范围外

- 生产就绪声明。

### 待确认问题

- 无。
